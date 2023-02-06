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
import net.runelite.client.events.ConfigChanged;
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
	private int lastOpponentID = -1;

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
		put(NPCWithXpBoost.BANDIT_130, 1.025).
		put(NPCWithXpBoost.BLACK_DRAGON, 1.05).
		put(NPCWithXpBoost.BRUTAL_BLACK_DRAGON, 1.1).
		put(NPCWithXpBoost.DARK_WARRIOR_135,1.1).
		put(NPCWithXpBoost.ENT_WILDERNESS,1.025).
		put(NPCWithXpBoost.ENT_WC_GUILD,1.05).
		put(NPCWithXpBoost.FIRE_GIANT_104_109,1.025).
		put(NPCWithXpBoost.BRUTAL_GREEN_DRAGON, 1.05).
		put(NPCWithXpBoost.LAVA_DRAGON, 1.075).
		put(NPCWithXpBoost.MOSS_GIANT_84, 1.025).
		put(NPCWithXpBoost.GREATER_NECHRYAEL, 1.025).
		put(NPCWithXpBoost.REVENANT_HOBGOBLIN, 1.025).
		put(NPCWithXpBoost.REVENANT_CYCLOPS, 1.075).
		put(NPCWithXpBoost.REVENANT_HELLHOUND, 1.075).
		put(NPCWithXpBoost.REVENANT_DEMON, 1.075).
		put(NPCWithXpBoost.REVENANT_ORK, 1.1).
		put(NPCWithXpBoost.REVENANT_DARK_BEAST, 1.125).
		put(NPCWithXpBoost.REVENANT_KNIGHT, 1.175).
		put(NPCWithXpBoost.REVENANT_DRAGON, 1.2).
		put(NPCWithXpBoost.REVENANT_MALEDICTUS, 1.65).
		put(NPCWithXpBoost.SPIRITUAL_WARRIOR_BANDOS, 1.025).
		put(NPCWithXpBoost.DEVIANT_SPECTRE, 1.025).
		put(NPCWithXpBoost.ADAMANT_DRAGON, 1.1).
		put(NPCWithXpBoost.DEMONIC_GORILLA, 1.075).
		put(NPCWithXpBoost.TORTURED_GORILLA, 1.125).
		put(NPCWithXpBoost.BLUE_DRAGON, 1.025).
		put(NPCWithXpBoost.BRUTAL_BLUE_DRAGON, 1.05).
		put(NPCWithXpBoost.DARK_BEAST, 1.025).
		put(NPCWithXpBoost.DRAKE, 1.075).
		put(NPCWithXpBoost.ELVES, 1.025).
		put(NPCWithXpBoost.SPITTING_WYVERN, 1.025).
		put(NPCWithXpBoost.TALONED_WYVERN, 1.025).
		put(NPCWithXpBoost.LONGTAILED_WYVERN, 1.025).
		put(NPCWithXpBoost.ANCIENT_WYVERN, 1.05).
		put(NPCWithXpBoost.IRON_DRAGON, 1.05).
		put(NPCWithXpBoost.LIZARDMAN_SHAMAN_OVERWORLD, 1.05).
		put(NPCWithXpBoost.MITHRIL_DRAGON, 1.075).
		put(NPCWithXpBoost.RED_DRAGON, 1.025).
		put(NPCWithXpBoost.BRUTAL_RED_DRAGON, 1.075).
		put(NPCWithXpBoost.RUNE_DRAGON, 1.1).
		put(NPCWithXpBoost.SKELETAL_WYVERN, 1.05).
		put(NPCWithXpBoost.STEEL_DRAGON, 1.05).
		put(NPCWithXpBoost.SUQAH, 1.025).
		put(NPCWithXpBoost.BRONZE_DRAGON, 1.025).
		put(NPCWithXpBoost.ICE_TROLLS, 1.05).
		put(NPCWithXpBoost.TROLL_GENERAL, 1.075).
		put(NPCWithXpBoost.WYRM, 1.025).
		put(NPCWithXpBoost.LOCUST_RIDER, 1.025).
		put(NPCWithXpBoost.SCARAB_MAGE, 1.025).
		put(NPCWithXpBoost.HYDRA, 1.075).
		put(NPCWithXpBoost.REPUGNANT_SPECTRE, 1.075).
		put(NPCWithXpBoost.BASILISK_SENTINEL, 1.075).
		put(NPCWithXpBoost.KING_KURASK, 1.025).
		put(NPCWithXpBoost.MARBLE_GARGOYLE, 1.025).
		put(NPCWithXpBoost.NECHRYACH, 1.025).
		put(NPCWithXpBoost.GREATER_ABYSSAL_DEMON, 1.05).
		put(NPCWithXpBoost.NIGHT_BEAST, 1.175).
		put(NPCWithXpBoost.CORRUPT_LIZARDMAN_NMZ, 1.025).
		put(NPCWithXpBoost.ELVARG_NMZ, 1.025).
		put(NPCWithXpBoost.MOSS_GUARDIAN_NMZ, 1.025).
		put(NPCWithXpBoost.MOSS_GUARDIAN_HARD_NMZ, 1.075).
		put(NPCWithXpBoost.SLAGILITH_NMZ, 1.03).
		put(NPCWithXpBoost.SLAGILITH_HARD_NMZ, 1.025).
		put(NPCWithXpBoost.DAGANNOTH_MOTHER_NMZ, 1.05).
		put(NPCWithXpBoost.DAGANNOTH_MOTHER_HARD_NMZ, 1.125).
		put(NPCWithXpBoost.DAD_NMZ, 1.05).
		put(NPCWithXpBoost.DAD_HARD_NMZ, 1.1).
		put(NPCWithXpBoost.ARRG_NMZ, 1.075).
		put(NPCWithXpBoost.ARRG_HARD_NMZ, 1.175).
		put(NPCWithXpBoost.BLACK_KNIGHT_TITAN_NMZ, 1.025).
		put(NPCWithXpBoost.BLACK_KNIGHT_TITAN_HARD_NMZ, 1.05).
		put(NPCWithXpBoost.ICE_TROLL_KING_NMZ, 1.075).
		put(NPCWithXpBoost.ICE_TROLL_KING_HARD_NMZ, 1.125).
		put(NPCWithXpBoost.GLOD_NMZ, 1.05).
		put(NPCWithXpBoost.GLOD_HARD_NMZ, 1.1).
		put(NPCWithXpBoost.EVIL_CHICKEN_NMZ, 0.025).
		put(NPCWithXpBoost.AGRITHNANA_NMZ, 1.125).
		put(NPCWithXpBoost.AGRITHNANA_HARD_NMZ, 1.175).
		put(NPCWithXpBoost.FLAMBEED_NMZ, 1.125).
		put(NPCWithXpBoost.FLAMBEED_HARD_NMZ, 1.175).
		put(NPCWithXpBoost.KARAMEL_NMZ, 1.05).
		put(NPCWithXpBoost.DESSOURT_NMZ, 1.1).
		put(NPCWithXpBoost.DESSOURT_HARD_NMZ, 1.2).
		put(NPCWithXpBoost.GELATINNOTH_MOTHER_NMZ, 1.075).
		put(NPCWithXpBoost.GELATINNOTH_MOTHER_HARD_NMZ, 1.125).
		put(NPCWithXpBoost.CHRONOZON_NMZ, 0.1).
		put(NPCWithXpBoost.DESSOUS_NMZ, 1.1).
		put(NPCWithXpBoost.DESSOUS_HARD_NMZ, 1.175).
		put(NPCWithXpBoost.DAMIS_FIRST_NMZ, 1.05).
		put(NPCWithXpBoost.DAMIS_SECOND_NMZ, 1.15).
		put(NPCWithXpBoost.DAMIS_FIRST_HARD_NMZ, 1.1).
		put(NPCWithXpBoost.DAMIS_SECOND_HARD_NMZ, 1.225).
		put(NPCWithXpBoost.FAREED_NMZ, 1.15).
		put(NPCWithXpBoost.FAREED_HARD_NMZ, 1.25).
		put(NPCWithXpBoost.KAMIL_NMZ, 1.125).
		put(NPCWithXpBoost.KAMIL_HARD_NMZ, 1.225).
		put(NPCWithXpBoost.BARRELCHEST_NMZ, 1.1).
		put(NPCWithXpBoost.BARRELCHEST_HARD_NMZ, 1.175).
		put(NPCWithXpBoost.GIANT_SCARAB_NMZ, 1.05).
		put(NPCWithXpBoost.GIANT_SCARAB_HARD_NMZ, 1.1).
		put(NPCWithXpBoost.JUNGLE_DEMON_NMZ, 1.075).
		put(NPCWithXpBoost.JUNGLE_DEMON_HARD_NMZ, 1.15).
		put(NPCWithXpBoost.ARIANWYN_NMZ, 1.025).
		put(NPCWithXpBoost.ESSYLLT_NMZ, 1.05).
		put(NPCWithXpBoost.ESSYLLT_HARD_NMZ, 1.1).
		put(NPCWithXpBoost.DAGANNOTH_WATERBIRTH_MELEE, 1.05).
		put(NPCWithXpBoost.GIANT_ROCK_CRAB, 1.1).
		put(NPCWithXpBoost.WALLASALKI, 1.025).
		put(NPCWithXpBoost.ROCK_LOBSTER, 1.025).
		put(NPCWithXpBoost.JUSTICIAR_ZACHARIAH, 1.55).
		put(NPCWithXpBoost.DERWEN, 1.2).
		put(NPCWithXpBoost.PORAZDIR, 1.2).
		put(NPCWithXpBoost.GALVEK, 1.425).
		put(NPCWithXpBoost.FRAGMENT_OF_SEREN, 1.55).
		put(NPCWithXpBoost.GLOUGH, 1.325).
		put(NPCWithXpBoost.MONKEY_GUARD, 1.025).
		put(NPCWithXpBoost.MONKEY_SKELETON, 1.025).
		put(NPCWithXpBoost.DOUBLE_AGENT_108, 1.025).
		put(NPCWithXpBoost.DOUBLE_AGENT_141, 1.05).
		put(NPCWithXpBoost.SARADOMIN_WIZARD, 1.025).
		put(NPCWithXpBoost.ANCIENT_WIZARD_MELEE, 1.05).
		put(NPCWithXpBoost.UNDEAD_DRUID, 1.05).
		put(NPCWithXpBoost.FEROCIOUS_BARBARIAN_SPIRIT, 1.05).
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
		put(NPCWithXpBoost.PESTILENT_BLOAT, new Double[] {1.85, 1.975, 1.075}).
		put(NPCWithXpBoost.NYLOCAS_VASILIAS, new Double[] {1.225, 1.225, 1.025}).
		put(NPCWithXpBoost.SOTETSEG, new Double[] {1.675, 1.675, 1.045}).
		put(NPCWithXpBoost.VERZIK_VITUR_P1, new Double[] {1.05, 1.05, 1.005}).
		put(NPCWithXpBoost.VERZIK_VITUR_P2, new Double[] {1.425, 1.425, 1.025}).
		put(NPCWithXpBoost.VERZIK_VITUR_P3, new Double[] {1.85, 1.85, 1.125}).
		build();

	private HashMap<Integer, Double> CUSTOM_XP_MODIFIERS = new HashMap<Integer, Double>();

	@Provides
	InstantDamageCalculatorConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(InstantDamageCalculatorConfig.class);
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(overlay);
		reloadCustomXP();

		log.info("InstantDamageCalculator started!");
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);

		log.info("InstantDamageCalculator stopped!");
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged)
	{
		if (configChanged.getKey().equals("customBonusXP")) {
			reloadCustomXP();
		}
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

				if(CUSTOM_XP_MODIFIERS.containsKey(lastOpponentID)) {
					modifier = CUSTOM_XP_MODIFIERS.get(lastOpponentID);
				} else if (XP_MODIFIERS_WITH_MODES.containsKey(lastOpponent)) {
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
			lastOpponentID = -1;
			return;
		}

		NPC npc = (NPC) opponent;

		lastOpponentID = npc.getId();
		lastOpponent = NPCWithXpBoost.getNpc(lastOpponentID);
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

		for (int i = 0; i < spriteIDs.length; i++) {
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

	private void reloadCustomXP()
	{
		CUSTOM_XP_MODIFIERS.clear();

		for (String customRaw : config.customBonusXP().split("\n"))
		{
			if (customRaw.trim().equals("")) continue;
			String[] split = customRaw.split(":");
			if (split.length != 2) continue;

			Integer customID;
			Double customXP;
			try
			{
				customID = Integer.parseInt(split[0].trim());
				customXP = Double.parseDouble(split[1].trim());
				if(customID > 0 && customXP > 0) {
					CUSTOM_XP_MODIFIERS.put(customID, customXP);
				}
			}
			catch (NumberFormatException e)
			{
			}
		}

	}
}
