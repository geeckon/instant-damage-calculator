package com.geeckon.instantdamagecalculator;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static net.runelite.api.ScriptID.XPDROPS_SETDROPSIZE;

@Slf4j
@PluginDescriptor(
		name = "InstantDamageCalculator",
		tags = {"experience", "levels", "prayer", "xpdrop", "damage", "damagedrop", "muspah"}
)
public class InstantDamageCalculatorPlugin extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private InstantDamageCalculatorConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InstantDamageCalculatorOverlay overlay;

	private int xp = -1;
	private NPCWithXpBoost lastOpponent;
	private int lastOpponentID = -1;
	private int lastMuspahPhase = -1;

	@Getter
	private double hit = 0;

	@Getter
	private double totalHit = 0;
	private int equippedWeaponTypeVarbit = -1;
	private int activeProtectionPrayerVarbit = -1;
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
			put(NPCWithXpBoost.THIRD_AGE_WARRIOR, 1.075).
			put(NPCWithXpBoost.SKOTIZO, 1.375).
			put(NPCWithXpBoost.TZKAL_ZUK, 1.575).
			put(NPCWithXpBoost.JAL_AK, 1.025).
			put(NPCWithXpBoost.JAL_IMKOT, 1.075).
			put(NPCWithXpBoost.RABBIT, 2.125).
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
			put(NPCWithXpBoost.SPAWN_OF_SARACHNIS, 1.025).
			put(NPCWithXpBoost.KALPHITE_QUEEN_CRAWLING, 1.05).
			put(NPCWithXpBoost.KALPHITE_QUEEN_AIRBORNE, 1.125).
			put(NPCWithXpBoost.KREE_ARRA, 1.4).
			put(NPCWithXpBoost.WINGMAN_SKREE, 1.025).
			put(NPCWithXpBoost.COMMANDER_ZILYANA, 1.375).
			put(NPCWithXpBoost.STARLIGHT, 1.05).
			put(NPCWithXpBoost.GENERAL_GRAARDOR, 1.325).
			put(NPCWithXpBoost.KRIL_TSUTSAROTH, 1.375).
			put(NPCWithXpBoost.SNAKELING, 1.025).
			put(NPCWithXpBoost.PHANTOM_MUSPAH, 2.075).
			put(NPCWithXpBoost.CORPOREAL_BEAST, 1.55).
			put(NPCWithXpBoost.NEX, 2.525).
			put(NPCWithXpBoost.UMBRA, 1.075).
			put(NPCWithXpBoost.CRUOR, 1.05).
			put(NPCWithXpBoost.GLACIES, 1.05).
			put(NPCWithXpBoost.DUKE_SUCELLUS, 1.55).
			put(NPCWithXpBoost.VARDORVIS, 1.55).
			put(NPCWithXpBoost.LEVIATHAN, 1.975).
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
		put(NPCWithXpBoost.VASA_CRYSTALS, new Double[] {1.025, 1.025}).
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

	// TOA XP multipliers are derived from an NPC's stats; to calculate these for a particular raid level, the base
	// stats must be known. Format is:
	// [HP, Attack, Strength, Defense, Stab Defense, Slash Defense, Crush Defense, Attack Bonus, Strength Bonus]
	private static final ImmutableMap<NPCWithXpBoost, Integer[]> TOA_NPC_BASE_STATS = ImmutableMap.<NPCWithXpBoost, Integer[]>builder().
		put(NPCWithXpBoost.KEPHRI,               new Integer[] {150,   0,   0,  80,  60, 300, 100,   0,   0}).
		put(NPCWithXpBoost.AGILE_SCARAB,         new Integer[] { 30,  60,  20,   5,   0,   0,   0,   0,  25}).
		put(NPCWithXpBoost.SOLDIER_SCARAB,       new Integer[] { 40,  75,  80,  80,  15, 250,  30, 100,  55}).
		put(NPCWithXpBoost.SPITTING_SCARAB,      new Integer[] { 40,   1,  80,  80,  15, 250,  30,   0,  55}).
		put(NPCWithXpBoost.ARCANE_SCARAB,        new Integer[] { 40,  75,  80,  80,  15, 250,  30,   0,  55}).
		put(NPCWithXpBoost.SCARAB_TOA,           new Integer[] { 12,  20,  32,  28,   0,   0,   0,   0,   0}).
		put(NPCWithXpBoost.AKKHA,                new Integer[] {400, 100, 140,  80,  60, 120, 120, 115,  30}).
		put(NPCWithXpBoost.AKKHAS_SHADOW,        new Integer[] { 70, 100, 140,  30,  60, 120, 120, 115,  30}).
		put(NPCWithXpBoost.BABA,                 new Integer[] {380, 150, 160,  80,  80, 150, 240,   0,  26}).
		put(NPCWithXpBoost.BABOON_TOA,           new Integer[] {  0,   0,   0,   0,   0,   0,   0,   0,   0}).
		put(NPCWithXpBoost.BABOON_BRAWLER_SMALL, new Integer[] {  4,  40,  40,  12, 900, 900, 900,  20,   0}).
		put(NPCWithXpBoost.BABOON_BRAWLER_LARGE, new Integer[] {  8,  60,  60,  20, 900, 900, 900,  25,   0}).
		put(NPCWithXpBoost.BABOON_THROWER_SMALL, new Integer[] {  4,  40,  40,  12, -50, -50, -50,  20,   0}).
		put(NPCWithXpBoost.BABOON_THROWER_LARGE, new Integer[] {  8,  60,  60,  20, -50, -50, -50,  25,   0}).
		put(NPCWithXpBoost.BABOON_MAGE_SMALL,    new Integer[] {  4,  40,  40,  12, 900, 900, 900,  20,   0}).
		put(NPCWithXpBoost.BABOON_MAGE_LARGE,    new Integer[] {  8,  60,  60,  20, 900, 900, 900,  25,   0}).
		put(NPCWithXpBoost.BABOON_SHAMAN,        new Integer[] { 16,  60,  60,  20, 900, 900, 900,  25,   0}).
		put(NPCWithXpBoost.CURSED_BABOON,        new Integer[] { 10,  60,  60,  20, 900, 900, 900,  25,   0}).
		put(NPCWithXpBoost.VOLATILE_BABOON,      new Integer[] {  8,  60,  60,  20, 900, 900, 900,  25,   0}).
		put(NPCWithXpBoost.BABOON_THRALL,        new Integer[] {  2,  40,  40,  12,   0,   0,   0,  20,   0}).
		put(NPCWithXpBoost.ZEBAK,                new Integer[] {580, 250, 140,  70, 160, 160, 260, 160, 100}).
		put(NPCWithXpBoost.CROCODILE_TOA,        new Integer[] { 30, 150,  60, 100, 150, 350, 350,   0, 100}).
		put(NPCWithXpBoost.OBELISK,              new Integer[] {260, 200, 150, 100,  70,  70,  70,   0,   0}).
		put(NPCWithXpBoost.CORE,                 new Integer[] {  0,   0,   0,   0,   0,   0,   0,   0,   0}).
		put(NPCWithXpBoost.ELIDINIS_WARDEN_P2,   new Integer[] {140, 300, 150, 100,  70,  70,  70,   0,  10}).
		put(NPCWithXpBoost.TUMEKENS_WARDEN_P2,   new Integer[] {140, 300, 150, 100,  70,  70,  70,   0,  25}).
		put(NPCWithXpBoost.WARDENS_P3,           new Integer[] {880, 150, 150, 150,  40,  40,  20,   0,  40}).
		build();

	private HashMap<NPCWithXpBoost, Double> TOA_XP_MODIFIERS = new HashMap<NPCWithXpBoost, Double>();

	private HashMap<Integer, Double> CUSTOM_XP_MODIFIERS = new HashMap<Integer, Double>();

	private static final List<Integer> MUSPAH_IDS = new ArrayList<>(Arrays.asList(NpcID.PHANTOM_MUSPAH,
			NpcID.PHANTOM_MUSPAH_12078, NpcID.PHANTOM_MUSPAH_12079, NpcID.PHANTOM_MUSPAH_12080, NpcID.PHANTOM_MUSPAH_12082));

	private Instant expiryTimer;

	@Getter
	private boolean overlayExpired = true;

	@Provides
	InstantDamageCalculatorConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(InstantDamageCalculatorConfig.class);
	}

	@Override
	protected void startUp() throws Exception {
		overlayManager.add(overlay);
		updateCustomXP();
		clientThread.invoke(() -> updateToaModifiers());

		log.info("InstantDamageCalculator started!");
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay);
		lastMuspahPhase = -1;
		lastOpponentID = -1;
		lastOpponent = null;

		log.info("InstantDamageCalculator stopped!");
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged configChanged) {
		if (configChanged.getKey().equals("customBonusXP")) {
			updateCustomXP();
		}
		if (configChanged.getKey().equals("expiry") && config.expiry() != 0) {
			expireOverlay();
		}
		if (configChanged.getKey().equals("precision")) {
			hit = roundToPrecision(getHit());
			totalHit = roundToPrecision(getTotalHit());
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage chatMessage) {
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
				case "You've broken into a crypt!":
					if (config.resetOnBarrowsCryptEntry()) {
						resetTotalHit();
					}
			}
		} else if (chatMessage.getType() == ChatMessageType.FRIENDSCHATNOTIFICATION) {
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
	public void onFakeXpDrop(FakeXpDrop event) {
		// Need this event for players with 200M hitpoints xp
		if (event.getSkill() == Skill.HITPOINTS) {
			long diff = event.getXp();

			if (diff > 0) {
				handleHitpointsXpDrop(diff);
			}
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged statChanged) {
		if (statChanged.getSkill() == Skill.HITPOINTS) {
			int newXp = client.getSkillExperience(Skill.HITPOINTS);

			if (xp == -1) {
				xp = newXp;
				return;
			}

			long diff = newXp - xp;
			xp = newXp;

			if (diff > 0) {
				handleHitpointsXpDrop(diff);
			}
		}
	}

	private void handleHitpointsXpDrop(long diff) {
		if (config.resetOnMuspahPhase() && lastOpponentID == NpcID.PHANTOM_MUSPAH_12082) {
			return;
		}

		double modifier = 1.0;

		if(CUSTOM_XP_MODIFIERS.containsKey(lastOpponentID))
		{
			modifier = CUSTOM_XP_MODIFIERS.get(lastOpponentID);
		}
		else if (XP_MODIFIERS_WITH_MODES.containsKey(lastOpponent))
		{
			modifier = XP_MODIFIERS_WITH_MODES.get(lastOpponent)[mode];
		}
		else if (TOA_XP_MODIFIERS.containsKey(lastOpponent))
		{
			modifier = TOA_XP_MODIFIERS.get(lastOpponent);
		}
		else
		{
			modifier = XP_MODIFIERS.getOrDefault(lastOpponent, 1.0);
		}

		hit = roundToPrecision(diff / 1.33 / modifier);
		totalHit = roundToPrecision(totalHit + hit);

		enableExpiryTimer();
	}

	@Subscribe
	public void onNpcChanged(NpcChanged event) {
		int oldNpcID = event.getOld().getId();
		int newNpcId = event.getNpc().getId();
		if (config.resetOnMuspahPhase() && MUSPAH_IDS.contains(oldNpcID) && oldNpcID == lastMuspahPhase) {
			handleMuspahUpdate(newNpcId);
		}
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged event) {
		// Get current opponent to apply boss xp boost modifiers
		if (event.getSource() != client.getLocalPlayer()) {
			return;
		}

		Actor opponent = event.getTarget();

		if (!(opponent instanceof NPC)) {
			lastOpponent = null;
			lastOpponentID = -1;
			return;
		}

		NPC npc = (NPC) opponent;

		lastOpponentID = npc.getId();
		lastOpponent = NPCWithXpBoost.getNpc(lastOpponentID);

		if (config.resetOnMuspahPhase() && MUSPAH_IDS.contains(npc.getId())) {
			handleMuspahUpdate(npc.getId());
		}
	}

	private void handleMuspahUpdate(int muspahID) {
		if (muspahID == NpcID.PHANTOM_MUSPAH || muspahID == NpcID.PHANTOM_MUSPAH_12078) {
			if (lastMuspahPhase != muspahID) {
				resetTotalHit();
				lastMuspahPhase = muspahID;
			}
		} else if (muspahID == NpcID.PHANTOM_MUSPAH_12079 || muspahID == NpcID.PHANTOM_MUSPAH_12080) {
			lastMuspahPhase = -1;
		}
	}

	@Subscribe(
			priority = 1
	)
	public void onScriptPreFired(ScriptPreFired scriptPreFired) {
		if (scriptPreFired.getScriptId() == XPDROPS_SETDROPSIZE) {
			final int[] intStack = client.getIntStack();
			final int intStackSize = client.getIntStackSize();
			// This runs prior to the proc being invoked, so the arguments are still on the stack.
			// Grab the first argument to the script.
			final int widgetId = intStack[intStackSize - 4];
			processXpDrop(widgetId);
		}
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded widgetLoaded)
	{
		int toaWidgetID = 481;
		if(widgetLoaded.getGroupId() == toaWidgetID)
		{
			updateToaModifiers();
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

	private void replaceXpDrops(Widget text, int[] spriteIDs) {
		if (!config.replaceXpDrops()) {
			return;
		}

		boolean hasOtherCombatDrop = false;

		for (int i = 0; i < spriteIDs.length; i++) {
			int spriteId = spriteIDs[i];
			if (spriteId == SpriteID.SKILL_HITPOINTS) {
				// If xp drop contains HITPOINTS sprite, replace it with the hit
				text.setText((int) Math.round(hit) + "");
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

	private void updateCustomXP()
	{
		CUSTOM_XP_MODIFIERS.clear();

		for (String customRaw : config.customBonusXP().split("\n")) {
			if (customRaw.trim().equals("")) continue;
			String[] split = customRaw.split(":");
			if (split.length < 2) continue;

			Integer customID;
			Double customXP;
			try {
				customID = Integer.parseInt(split[0].trim());
				customXP = Double.parseDouble(split[1].trim());
				if(customID > 0 && customXP > 0) {
					CUSTOM_XP_MODIFIERS.put(customID, customXP);
				}
			} catch (NumberFormatException e) {
			}
		}

	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event) {
		if (!config.displayTotalDamageOverlay()) {
			return;
		}
		if (event.getVarbitId() == Varbits.EQUIPPED_WEAPON_TYPE) {
			if (!config.resetOnWeaponChange()) {
				return;
			}

			final int currentEquippedWeaponTypeVarbit = client.getVarbitValue(Varbits.EQUIPPED_WEAPON_TYPE);

			if (currentEquippedWeaponTypeVarbit == equippedWeaponTypeVarbit) {
				return;
			}

			equippedWeaponTypeVarbit = currentEquippedWeaponTypeVarbit;

			resetTotalHit();
		}

		if ((event.getVarbitId() == Varbits.PRAYER_PROTECT_FROM_MAGIC
				|| event.getVarbitId() == Varbits.PRAYER_PROTECT_FROM_MISSILES
				|| event.getVarbitId() == Varbits.PRAYER_PROTECT_FROM_MELEE) && event.getValue() == 1) {
			if (!config.resetOnPrayerChange()) {
				return;
			}

			if (event.getVarbitId() == activeProtectionPrayerVarbit) {
				return;
			}

			activeProtectionPrayerVarbit = event.getVarbitId();

			resetTotalHit();
		}
	}
	private void resetTotalHit() {
		totalHit = 0;
  }

	private void updateToaModifiers()
	{
		// only update if inside the central ToA room, where all four path levels can be seen
		int[] regions = client.getMapRegions();
		if (regions == null || regions.length != 1) return;
		if (regions[0] != 14160) return;

		// Need raid level, path level, group size to calculate xp modifiers
		int parentWidget = 481;
		int scabarasWidget = 49;
		int hetWidget = 51;
		int apmekenWidget = 53;
		int crondisWidget = 55;

		int toaRaidLevel = client.getVarbitValue(Varbits.TOA_RAID_LEVEL);
		int toaGroupSize = 0;
		if (client.getVarbitValue(Varbits.TOA_MEMBER_0_HEALTH) > 0) toaGroupSize++;
		if (client.getVarbitValue(Varbits.TOA_MEMBER_1_HEALTH) > 0) toaGroupSize++;
		if (client.getVarbitValue(Varbits.TOA_MEMBER_2_HEALTH) > 0) toaGroupSize++;
		if (client.getVarbitValue(Varbits.TOA_MEMBER_3_HEALTH) > 0) toaGroupSize++;
		if (client.getVarbitValue(Varbits.TOA_MEMBER_4_HEALTH) > 0) toaGroupSize++;
		if (client.getVarbitValue(Varbits.TOA_MEMBER_5_HEALTH) > 0) toaGroupSize++;
		if (client.getVarbitValue(Varbits.TOA_MEMBER_6_HEALTH) > 0) toaGroupSize++;
		if (client.getVarbitValue(Varbits.TOA_MEMBER_7_HEALTH) > 0) toaGroupSize++;

		// failsafe?
		if (toaGroupSize == 0) toaGroupSize = 1;

		int toaScabarasLevel;
		int toaHetLevel;
		int toaApmekenLevel;
		int toaCrondisLevel;

		try
		{
			toaScabarasLevel = Integer.parseInt(client.getWidget(parentWidget, scabarasWidget).getText());
			toaHetLevel      = Integer.parseInt(client.getWidget(parentWidget, hetWidget     ).getText());
			toaApmekenLevel  = Integer.parseInt(client.getWidget(parentWidget, apmekenWidget ).getText());
			toaCrondisLevel  = Integer.parseInt(client.getWidget(parentWidget, crondisWidget ).getText());
		}
		catch (NullPointerException e)
		{
			return;
		}
		catch (NumberFormatException e)
		{
			return;
		}

		// HP multiplier from raid level
		// 5 raid levels is worth +2% health
		double raidMultiplier = 1 + 0.004*toaRaidLevel;

		// HP multiplier from group size
		// Group member 1 is worth 1.0, members 2-3 are worth 0.9, members 4-8 are worth 0.6
		double groupMultiplier = 1;
		if(toaGroupSize >= 2) groupMultiplier += 0.9;
		if(toaGroupSize >= 3) groupMultiplier += 0.9;
		if(toaGroupSize >= 4) groupMultiplier += (toaGroupSize - 3)*0.6;

		// HP multiplier from path level
		// Path level 1 is worth +8%; 2-6 are worth +5% each
		double scabarasMultiplier = 1 + (toaScabarasLevel > 0 ? 0.08 : 0) + (toaScabarasLevel > 1 ? 0.05*(toaScabarasLevel - 1) : 0);
		double hetMultiplier      = 1 + (     toaHetLevel > 0 ? 0.08 : 0) + (     toaHetLevel > 1 ? 0.05*(     toaHetLevel - 1) : 0);
		double apmekenMultiplier  = 1 + ( toaApmekenLevel > 0 ? 0.08 : 0) + ( toaApmekenLevel > 1 ? 0.05*( toaApmekenLevel - 1) : 0);
		double crondisMultiplier  = 1 + ( toaCrondisLevel > 0 ? 0.08 : 0) + ( toaCrondisLevel > 1 ? 0.05*( toaCrondisLevel - 1) : 0);

		// Universal multiplier (used by NPCs in puzzles such as monkeys/crocodiles, as well as Wardens)
		double commonMultiplier = raidMultiplier*groupMultiplier;

		// Path-specific multipliers (used by path bosses + adds)
		double kephriMultiplier = commonMultiplier*scabarasMultiplier;
		double akkhaMultiplier  = commonMultiplier*hetMultiplier;
		double babaMultiplier   = commonMultiplier*apmekenMultiplier;
		double zebakMultiplier  = commonMultiplier*crondisMultiplier;

		// Path of Scabaras
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.KEPHRI,          calculateToaModifier(NPCWithXpBoost.KEPHRI,          kephriMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.SOLDIER_SCARAB,  calculateToaModifier(NPCWithXpBoost.SOLDIER_SCARAB,  kephriMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.SPITTING_SCARAB, calculateToaModifier(NPCWithXpBoost.SPITTING_SCARAB, kephriMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.ARCANE_SCARAB,   calculateToaModifier(NPCWithXpBoost.ARCANE_SCARAB,   kephriMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.AGILE_SCARAB,    calculateToaModifier(NPCWithXpBoost.AGILE_SCARAB,    kephriMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.SCARAB_TOA,      calculateToaModifier(NPCWithXpBoost.SCARAB_TOA,      commonMultiplier));

		// Path of Het
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.AKKHA,         calculateToaModifier(NPCWithXpBoost.AKKHA,         akkhaMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.AKKHAS_SHADOW, calculateToaModifier(NPCWithXpBoost.AKKHAS_SHADOW, akkhaMultiplier));

		// Path of Apmeken
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.BABA,                 calculateToaModifier(NPCWithXpBoost.BABA,                 babaMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.BABOON_TOA,           1.0); // Baboons in Ba-ba fight do not seem to scale
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.BABOON_BRAWLER_SMALL, calculateToaModifier(NPCWithXpBoost.BABOON_BRAWLER_SMALL, commonMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.BABOON_BRAWLER_LARGE, calculateToaModifier(NPCWithXpBoost.BABOON_BRAWLER_LARGE, commonMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.BABOON_THROWER_SMALL, calculateToaModifier(NPCWithXpBoost.BABOON_THROWER_SMALL, commonMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.BABOON_THROWER_LARGE, calculateToaModifier(NPCWithXpBoost.BABOON_THROWER_LARGE, commonMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.BABOON_MAGE_SMALL,    calculateToaModifier(NPCWithXpBoost.BABOON_MAGE_SMALL,    commonMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.BABOON_MAGE_LARGE,    calculateToaModifier(NPCWithXpBoost.BABOON_MAGE_LARGE,    commonMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.BABOON_SHAMAN,        calculateToaModifier(NPCWithXpBoost.BABOON_SHAMAN,        commonMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.CURSED_BABOON,        calculateToaModifier(NPCWithXpBoost.CURSED_BABOON,        commonMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.VOLATILE_BABOON,      calculateToaModifier(NPCWithXpBoost.VOLATILE_BABOON,      commonMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.BABOON_THRALL,        calculateToaModifier(NPCWithXpBoost.BABOON_THRALL,        commonMultiplier));

		// Path of Crondis
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.ZEBAK,         calculateToaModifier(NPCWithXpBoost.ZEBAK,         zebakMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.CROCODILE_TOA, calculateToaModifier(NPCWithXpBoost.CROCODILE_TOA, commonMultiplier));

		// Wardens
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.OBELISK,            calculateToaModifier(NPCWithXpBoost.OBELISK,            commonMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.CORE,               1.0); // No bonus XP on the core
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.ELIDINIS_WARDEN_P2, calculateToaModifier(NPCWithXpBoost.ELIDINIS_WARDEN_P2, commonMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.TUMEKENS_WARDEN_P2, calculateToaModifier(NPCWithXpBoost.TUMEKENS_WARDEN_P2, commonMultiplier));
		TOA_XP_MODIFIERS.put(NPCWithXpBoost.WARDENS_P3,         calculateToaModifier(NPCWithXpBoost.WARDENS_P3,         commonMultiplier));
	}

	private double calculateToaModifier(NPCWithXpBoost npc, double multiplier)
	{
		Integer[] baseStats = TOA_NPC_BASE_STATS.get(npc);
		int attack        = baseStats[1];
		int strength      = baseStats[2];
		int defense       = baseStats[3];
		int stabDefense   = baseStats[4];
		int slashDefense  = baseStats[5];
		int crushDefense  = baseStats[6];
		int attackBonus   = baseStats[7];
		int strengthBonus = baseStats[8];

		// HP is the only stat affected by the raid multiplier (and is then rounded to the nearest 10)
		double rawHP = ((double)baseStats[0])*multiplier;
		int hp = (int) Math.round(rawHP/10)*10;

		// ToA bonus XP multiplier works in multiples of 0.025 (call them bands)
		// First step is to figure out which band an enemy is scaled to:
		//     band = floor(M*(D+A+S)/5120), where:
		//         M is the average of HP/attack/strength/defense, floored
		//         D is the average of stab/slash/crush defense, floored
		//         A is the attack bonus
		//         S is the strength bonus
		// Then the final bonus XP bonus is:
		//     bonusXP = 1 + band*0.025
		int M = (hp + attack + strength + defense)/4;
		int D = (stabDefense + slashDefense + crushDefense)/3;
		int MDAS = M*(D + attackBonus + strengthBonus);
		// defensive bonuses can be negative; if D + A + S < 0 then it's set to 0 instead
		if(MDAS < 0) MDAS = 0;
		int band = MDAS/5120;
		return  1 + band*0.025;
	}

	@Subscribe
	public void onGameTick(GameTick tick)
	{
		if (expiryTimer != null && config.expiry() != 0)
		{
			Duration timeSinceUpdate = Duration.between(expiryTimer, Instant.now());
			Duration expiryTimeout = Duration.ofSeconds(config.expiry());

			if (timeSinceUpdate.compareTo(expiryTimeout) >= 0)
			{
				expireOverlay();
			}
		}
	}

	private void expireOverlay()
	{
		overlayExpired = true;
	}

	private void enableExpiryTimer()
	{
		expiryTimer = Instant.now();
		overlayExpired = false;
	}

	private double roundToPrecision(double hit)
	{
		int scale = (int) Math.pow(10, config.precision());
		return (double) Math.round(hit * scale) / scale;
	}

}
