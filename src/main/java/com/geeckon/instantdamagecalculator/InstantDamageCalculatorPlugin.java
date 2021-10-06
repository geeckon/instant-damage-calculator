package com.geeckon.instantdamagecalculator;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.chatfilter.ChatFilterPlugin;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

import static net.runelite.api.ScriptID.XPDROPS_SETDROPSIZE;

@Slf4j
@PluginDescriptor(
	name = "InstantDamageCalculator",
	tags = {"experience", "levels", "prayer", "xpdrop", "damage", "damagedrop"},
	conflicts = "XP Drop"
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

	/**
	 * CHAOS_FANATIC(NpcID.CHAOS_FANATIC),
	 * CRAZY_ARCHAEOLOGIST(NpcID.CRAZY_ARCHAEOLOGIST),
	 * SCORPIA(NpcID.SCORPIA),
	 * KING_BLACK_DRAGON(NpcID.KING_BLACK_DRAGON, NpcID.KING_BLACK_DRAGON_2642, NpcID.KING_BLACK_DRAGON_6502),
	 * CHAOS_ELEMENTAL(NpcID.CHAOS_ELEMENTAL, NpcID.CHAOS_ELEMENTAL_6505),
	 * VETION(NpcID.VETION, NpcID.VETION_REBORN),
	 * SKELETON_HELLHOUND(NpcID.SKELETON_HELLHOUND_6613),
	 * GREATER_SKELETON_HELLHOUND(NpcID.GREATER_SKELETON_HELLHOUND),
	 * VENENATIS(NpcID.VENENATIS, NpcID.VENENATIS_6610),
	 * CALLISTO(NpcID.CALLISTO, NpcID.CALLISTO_6609),
	 * OBOR(NpcID.OBOR),
	 * BRYOPHYTA(NpcID.BRYOPHYTA),
	 * THE_MIMIC(NpcID.THE_MIMIC, NpcID.THE_MIMIC_8633),
	 * SKOTIZO(NpcID.SKOTIZO),
	 * TZKAL_ZUK(NpcID.TZKALZUK),
	 * AHRIM_THE_BLIGHTED(NpcID.AHRIM_THE_BLIGHTED),
	 * DHAROK_THE_WRETCHED(NpcID.DHAROK_THE_WRETCHED),
	 * GUTHAN_THE_INFESTED(NpcID.GUTHAN_THE_INFESTED),
	 * TORAG_THE_CORRUPTED(NpcID.TORAG_THE_CORRUPTED),
	 * VERAC_THE_DEFILED(NpcID.VERAC_THE_DEFILED),
	 * GIANT_MOLE(NpcID.GIANT_MOLE, NpcID.GIANT_MOLE_6499),
	 * DERANGED_ARCHAEOLOGIST(NpcID.DERANGED_ARCHAEOLOGIST),
	 * DAGANNOTH_REX(NpcID.DAGANNOTH_REX, NpcID.DAGANNOTH_REX_6498),
	 * DAGANNOTH_PRIME(NpcID.DAGANNOTH_PRIME, NpcID.DAGANNOTH_PRIME_6497),
	 * SARACHNIS(NpcID.SARACHNIS),
	 * KALPHITE_QUEEN_CRAWLING(NpcID.KALPHITE_QUEEN, NpcID.KALPHITE_QUEEN_963, NpcID.KALPHITE_QUEEN_4303, NpcID.KALPHITE_QUEEN_6500),
	 * KALPHITE_QUEEN_AIRBORNE(NpcID.KALPHITE_QUEEN_965, NpcID.KALPHITE_QUEEN_4304, NpcID.KALPHITE_QUEEN_6501),
	 * KREE_ARRA(NpcID.KREEARRA, NpcID.KREEARRA_6492),
	 * COMMANDER_ZILYANA(NpcID.COMMANDER_ZILYANA, NpcID.COMMANDER_ZILYANA_6493),
	 * GENERAL_GRAARDOR(NpcID.GENERAL_GRAARDOR, NpcID.GENERAL_GRAARDOR_6494),
	 * KRIL_TSUTSAROTH(NpcID.KRIL_TSUTSAROTH, NpcID.KRIL_TSUTSAROTH_6495),
	 * CORPOREAL_BEAST(NpcID.CORPOREAL_BEAST),
	  
	 * TEKTON(NpcID.TEKTON, NpcID.TEKTON_7541, NpcID.TEKTON_7542, NpcID.TEKTON_7545),
	 * TEKTON_ENRAGED(NpcID.TEKTON_ENRAGED, NpcID.TEKTON_ENRAGED_7544),
	 * ICE_DEMON(NpcID.ICE_DEMON, NpcID.ICE_DEMON_7585),
	 * LIZARDMAN_SHAMAN(NpcID.LIZARDMAN_SHAMAN, NpcID.LIZARDMAN_SHAMAN_6767, NpcID.LIZARDMAN_SHAMAN_7573, NpcID.LIZARDMAN_SHAMAN_7574, NpcID.LIZARDMAN_SHAMAN_7744, NpcID.LIZARDMAN_SHAMAN_7745, NpcID.LIZARDMAN_SHAMAN_8565),
	 * VANGUARD_MELEE(NpcID.VANGUARD_7527),
	 * VANGUARD_RANGED(NpcID.VANGUARD_7528),
	 * VANGUARD_MAGIC(NpcID.VANGUARD_7529),
	 * GUARDIAN(NpcID.GUARDIAN, NpcID.GUARDIAN_7570, NpcID.GUARDIAN_7571, NpcID.GUARDIAN_7572),
	 * VASA_NISTIRIO(NpcID.VASA_NISTIRIO, NpcID.VASA_NISTIRIO_7567),
	 * SKELETAL_MYSTIC(NpcID.SKELETAL_MYSTIC, NpcID.SKELETAL_MYSTIC_7605, NpcID.SKELETAL_MYSTIC_7606),
	 * MUTTADILE_SMALL(NpcID.MUTTADILE_7562),
	 * MUTTADILE_LARGE(NpcID.MUTTADILE_7563),
	 *
	 *
	 *
	 *
	 * //    private final Set<Integer> ids;
	 * //
	 * //    NPCWithXpBoost(Integer... ids)
	 * //    {
	 * //        this.ids = Sets.newHashSet(ids);
	 * //    }
	 * //
	 * //    static NPCWithXpBoost getNpc(int id)
	 * //    {
	 * //        for (NPCWithXpBoost npc : values())
	 * //        {
	 * //            if (npc.ids.contains(id))
	 * //            {
	 * //                return npc;
	 * //            }
	 * //        }
	 * //
	 * //        return null;
	 * //    }
	 */

	private int xp = -1;
	private NPCWithXpBoost lastOpponent;
	@Getter
	private int hit = 0;
	private int mode = 0;
	private boolean correctPrayer;
	private static final ImmutableMap<String, Double> XP_MODIFIERS = ImmutableMap.<String, Double>builder().
			put(NPCWithXpBoost.CERBERUS, 1.15).
			put(NPCWithXpBoost.ABYSSAL_SIRE, 1.125).
			put(NPCWithXpBoost.ALCHEMICAL_HYDRA, 1.2).

			put(NPCWithXpBoost.CHAOS_FANATIC, 1.125).
			put(NPCWithXpBoost.CRAZY_ARCHAEOLOGIST, 1.25).
			put(NPCWithXpBoost.SCORPIA, 1.3).
			put(NPCWithXpBoost.KING_BLACK_DRAGON, 1.075).
			put(NPCWithXpBoost.CHAOS_ELEMENTAL, 1.075).
			put(NPCWithXpBoost.VETION, 1.225).
			put(NPCWithXpBoost.SKELETON_HELLHOUND_6613, 1.05).
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
	private static final ImmutableMap<String, Double[]> XP_MODIFIERS_WITH_MODES = ImmutableMap.<String, Double[]>builder().
			put(NPCWithXpBoost.TEKTON, new Double[] {1.35, 1.5}).
			put(NPCWithXpBoost.TEKTON_ENRAGED, new Double[] {1.525, 1.775}).
			put(NPCWithXpBoost.ICE_DEMON, new Double[] {1.525, 1.775}).
			put(NPCWithXpBoost.LIZARDMAN_SHAMAN, new Double[] {1.175, 1.275}).
			put(NPCWithXpBoost.VANGUARD_MELEE, new Double[] {1.075, 1.125}).
			put(NPCWithXpBoost.VANGUARD_RANGED, new Double[] {1.05, 1.075}).
			put(NPCWithXpBoost.VANGUARD_MAGIC, new Double[] {1.275, 1.40}).
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
						mode = 1;
						log.info("Starting challenge mode raid");
					} else {
						mode = 0;
						log.info("Starting normal mode raid");
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

	@Subscribe
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

		PrayerType prayer = getActivePrayerType();

		final IntStream spriteIDs =
				Arrays.stream(children)
						.skip(1) // skip text
						.filter(Objects::nonNull)
						.mapToInt(Widget::getSpriteId);

		if (prayer == null)
		{
			replaceXpDrops(text, spriteIDs);
			hideSkillIcons(xpdrop);
			resetTextColor(text);
			return;
		}

		int color = 0;

		switch (prayer)
		{
			case MELEE:
				if (correctPrayer || spriteIDs.anyMatch(id ->
						id == SpriteID.SKILL_ATTACK || id == SpriteID.SKILL_STRENGTH || id == SpriteID.SKILL_DEFENCE))
				{
					color = config.getMeleePrayerColor().getRGB();
					correctPrayer = true;
				}
				break;
			case RANGE:
				if (correctPrayer || spriteIDs.anyMatch(id -> id == SpriteID.SKILL_RANGED))
				{
					color = config.getRangePrayerColor().getRGB();
					correctPrayer = true;
				}
				break;
			case MAGIC:
				if (correctPrayer || spriteIDs.anyMatch(id -> id == SpriteID.SKILL_MAGIC))
				{
					color = config.getMagePrayerColor().getRGB();
					correctPrayer = true;
				}
				break;
		}

		if (color != 0)
		{
			text.setTextColor(color);
		}
		else
		{
			resetTextColor(text);
		}

		replaceXpDrops(text, spriteIDs);
		hideSkillIcons(xpdrop);
	}

	private void replaceXpDrops(Widget widget, IntStream spriteIDs)
	{
		if (!config.replaceXpDrops()) {
			return;
		}

		// Only display hit on the HP xp drop. Remove all others
		if (spriteIDs.anyMatch(id -> id == SpriteID.SKILL_HITPOINTS))
		{
			widget.setText(hit + "");
		} else {
			widget.setText("");
		}
	}

	private void resetTextColor(Widget widget)
	{
		Color standardColor = config.standardColor();
		if (standardColor != null)
		{
			int color = standardColor.getRGB();
			widget.setTextColor(color);
		}
		else
		{
			EnumComposition colorEnum = client.getEnum(EnumID.XPDROP_COLORS);
			int defaultColorId = client.getVar(Varbits.EXPERIENCE_DROP_COLOR);
			int color = colorEnum.getIntValue(defaultColorId);
			widget.setTextColor(color);
		}
	}

	private void hideSkillIcons(Widget xpdrop)
	{
		if (config.hideSkillIcons())
		{
			Widget[] children = xpdrop.getChildren();
			// keep only text
			Arrays.fill(children, 1, children.length, null);
		}
	}

	private PrayerType getActivePrayerType()
	{
		for (XpPrayer prayer : XpPrayer.values())
		{
			if (client.isPrayerActive(prayer.getPrayer()))
			{
				return prayer.getType();
			}
		}
		return null;
	}
}
