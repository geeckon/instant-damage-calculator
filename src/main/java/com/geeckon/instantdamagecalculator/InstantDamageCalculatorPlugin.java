package com.geeckon.instantdamagecalculator;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import java.awt.*;
import java.util.*;
import java.util.stream.IntStream;

import static net.runelite.api.ScriptID.XPDROPS_SETDROPSIZE;

@Slf4j
@PluginDescriptor(
	name = "InstantDamageCalculator",
	tags = {"experience", "levels", "prayer", "xpdrop", "damage", "damagedrop"}
)
public class InstantDamageCalculatorPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private InstantDamageCalculatorConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InstantDamageCalculatorOverlay overlay;

	private int xp = -1;
	private NPCWithXpBoost lastOpponent;
	@Getter
	private int hit = 0;
	private int mode = 0;
	private static final ImmutableMap<NPCWithXpBoost, Double> XP_MODIFIERS = ImmutableMap.<NPCWithXpBoost, Double>builder().
		put(NPCWithXpBoost.CERBERUS, 1.15).
		put(NPCWithXpBoost.ABYSSAL_SIRE, 1.125).
		put(NPCWithXpBoost.ALCHEMICAL_HYDRA, 1.2).
		put(NPCWithXpBoost.CHAOS_FANATIC, 1.125).
		put(NPCWithXpBoost.CRAZY_ARCHAEOLOGIST, 1.25).
		put(NPCWithXpBoost.SCORPIA, 1.3).
		put(NPCWithXpBoost.KING_BLACK_DRAGON, 1.075).
		put(NPCWithXpBoost.CHAOS_ELEMENTAL, 1.075).
		put(NPCWithXpBoost.VETION, 1.225).
		put(NPCWithXpBoost.SKELETON_HELLHOUND, 1.05).
		put(NPCWithXpBoost.GREATER_SKELETON_HELLHOUND, 1.125).
		put(NPCWithXpBoost.VENENATIS, 1.525).
		put(NPCWithXpBoost.CALLISTO, 1.225).
		put(NPCWithXpBoost.OBOR, 1.075).
		put(NPCWithXpBoost.BRYOPHYTA, 1.025).
		put(NPCWithXpBoost.THE_MIMIC, 1.25).
		put(NPCWithXpBoost.SKOTIZO, 1.375).
		put(NPCWithXpBoost.TZKAL_ZUK, 1.575).
		put(NPCWithXpBoost.AHRIM_THE_BLIGHTED, 1.025).
		put(NPCWithXpBoost.DHAROK_THE_WRETCHED, 1.15).
		put(NPCWithXpBoost.GUTHAN_THE_INFESTED, 1.15).
		put(NPCWithXpBoost.TORAG_THE_CORRUPTED, 1.125).
		put(NPCWithXpBoost.VERAC_THE_DEFILED, 1.125).
		put(NPCWithXpBoost.GIANT_MOLE, 1.075).
		put(NPCWithXpBoost.DERANGED_ARCHAEOLOGIST, 1.375).
		put(NPCWithXpBoost.DAGANNOTH_REX, 1.3).
		put(NPCWithXpBoost.DAGANNOTH_PRIME, 1.3).
		put(NPCWithXpBoost.SARACHNIS, 1.075).
		put(NPCWithXpBoost.KALPHITE_QUEEN_CRAWLING, 1.05).
		put(NPCWithXpBoost.KALPHITE_QUEEN_AIRBORNE, 1.125).
		put(NPCWithXpBoost.KREE_ARRA, 1.4).
		put(NPCWithXpBoost.COMMANDER_ZILYANA, 1.375).
		put(NPCWithXpBoost.GENERAL_GRAARDOR, 1.325).
		put(NPCWithXpBoost.KRIL_TSUTSAROTH, 1.375).
		put(NPCWithXpBoost.CORPOREAL_BEAST, 1.55).
		build();
	private static final ImmutableMap<NPCWithXpBoost, Double[]> XP_MODIFIERS_WITH_MODES = ImmutableMap.<NPCWithXpBoost, Double[]>builder().
		put(NPCWithXpBoost.TEKTON, new Double[] {1.35, 1.5}).
		put(NPCWithXpBoost.TEKTON_ENRAGED, new Double[] {1.525, 1.775}).
		put(NPCWithXpBoost.ICE_DEMON, new Double[] {1.525, 1.775}).
		put(NPCWithXpBoost.LIZARDMAN_SHAMAN, new Double[] {1.175, 1.275}).
		put(NPCWithXpBoost.VANGUARD_MELEE, new Double[] {1.075, 1.125}).
		put(NPCWithXpBoost.VANGUARD_RANGED, new Double[] {1.05, 1.075}).
		put(NPCWithXpBoost.VANGUARD_MAGIC, new Double[] {1.275, 1.40}).
		put(NPCWithXpBoost.GUARDIAN, new Double[] {1.075, 1.1}).
		put(NPCWithXpBoost.VASA_NISTIRIO, new Double[] {1.075, 1.1}).
		put(NPCWithXpBoost.SKELETAL_MYSTIC, new Double[] {1.2, 1.3}).
		put(NPCWithXpBoost.MUTTADILE_SMALL, new Double[] {1.125, 1.225}).
		put(NPCWithXpBoost.MUTTADILE_LARGE, new Double[] {1.2, 1.35}).
		build();

	@Provides
	InstantDamageCalculatorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InstantDamageCalculatorConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);

		log.info("InstantDamageCalculator started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);

		log.info("InstantDamageCalculator stopped!");
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage)
	{
		if (chatMessage.getType() == ChatMessageType.GAMEMESSAGE) {
			switch (Text.removeTags(chatMessage.getMessage())) {
				case "You enter the Theatre of Blood (Entry Mode)...":
					mode = 2;
					break;
				case "You enter the Theatre of Blood (Normal Mode)...":
					mode = 0;
					break;
				case "You enter the Theatre of Blood (Hard Mode)...":
					mode = 1;
					break;
			}
		}
		else if (chatMessage.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION) {
			if (Text.removeTags(chatMessage.getMessage()).equals("The raid has begun!")) {
				{
					if (client.getVarbitValue(6385) > 0) {
						// Starting challenge mode raid
						mode = 1;
					} else {
						// Starting normal mode raid
						mode = 0;
					}
				}
			}
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged)
	{
		if (statChanged.getSkill() == Skill.HITPOINTS)
		{
			int newXp = client.getSkillExperience(Skill.HITPOINTS);

			if (xp == -1)
			{
				xp = newXp;
				return;
			}

			long diff = newXp - xp;

			if (diff > 0)
			{
				double modifier = 1.0;

				if (XP_MODIFIERS_WITH_MODES.containsKey(lastOpponent)) {
					modifier = XP_MODIFIERS_WITH_MODES.get(lastOpponent)[mode];
				} else {
					modifier = XP_MODIFIERS.getOrDefault(lastOpponent, 1.0);
				}

				hit = (int) Math.round(diff / 1.33 / modifier);
				xp = newXp;
			}
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event)
	{
		// Get current opponent to apply boss xp boost modifiers
		if (event.getSource() != client.getLocalPlayer())
		{
			return;
		}

		Actor opponent = event.getTarget();

		if (!(opponent instanceof NPC))
		{
			lastOpponent = null;
			return;
		}

		NPC npc = (NPC) opponent;
		
		lastOpponent = NPCWithXpBoost.getNpc(npc.getId());
	}

	@Subscribe(
		priority = 1
	)
	public void onScriptPreFired(ScriptPreFired scriptPreFired)
	{
		if (scriptPreFired.getScriptId() == XPDROPS_SETDROPSIZE)
		{
			final int[] intStack = client.getIntStack();
			final int intStackSize = client.getIntStackSize();
			// This runs prior to the proc being invoked, so the arguments are still on the stack.
			// Grab the first argument to the script.
			final int widgetId = intStack[intStackSize - 4];
			processXpDrop(widgetId);
		}
	}

	private void processXpDrop(int widgetId)
	{
		final Widget xpdrop = client.getWidget(widgetId);
		final Widget[] children = xpdrop.getChildren();
		// child 0 is the xpdrop text, everything else are sprite ids for skills
		final Widget text = children[0];

		final int[] spriteIDs =
			Arrays.stream(children)
				.skip(1) // skip text
				.filter(Objects::nonNull)
				.mapToInt(Widget::getSpriteId)
				.toArray();

		replaceXpDrops(text, spriteIDs);
	}

	private void replaceXpDrops(Widget text, int[] spriteIDs)
	{
		if (!config.replaceXpDrops()) {
			return;
		}

		boolean hasOtherCombatDrop = false;

		for (int i = 1; i < spriteIDs.length; i++) {
			int spriteId = spriteIDs[i];
			if (spriteId == SpriteID.SKILL_HITPOINTS) {
				// If xp drop contains HITPOINTS sprite, replace it with the hit
				text.setText(hit + "");
				return;
			} else if (spriteId == SpriteID.SKILL_ATTACK || spriteId == SpriteID.SKILL_STRENGTH ||
					spriteId == SpriteID.SKILL_DEFENCE || spriteId == SpriteID.SKILL_RANGED ||
					spriteId == SpriteID.SKILL_MAGIC) {
				hasOtherCombatDrop = true;
			}
		}

		// If xp drop contains any other combat sprite, remove it
		if (hasOtherCombatDrop) {
			text.setText("");
		}
	}
}
