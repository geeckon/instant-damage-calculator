package com.geeckon.instantdamagecalculator;

import com.google.common.collect.Sets;
import net.runelite.api.NpcID;

import java.util.Set;

public enum NPCWithXpBoost {
    CERBERUS(NpcID.CERBERUS, NpcID.CERBERUS_5863, NpcID.CERBERUS_5866),
    ABYSSAL_SIRE(NpcID.ABYSSAL_SIRE, NpcID.ABYSSAL_SIRE_5887, NpcID.ABYSSAL_SIRE_5888, NpcID.ABYSSAL_SIRE_5889, NpcID.ABYSSAL_SIRE_5890, NpcID.ABYSSAL_SIRE_5891, NpcID.ABYSSAL_SIRE_5908),
    ALCHEMICAL_HYDRA(NpcID.ALCHEMICAL_HYDRA, NpcID.ALCHEMICAL_HYDRA_8616, NpcID.ALCHEMICAL_HYDRA_8617, NpcID.ALCHEMICAL_HYDRA_8618, NpcID.ALCHEMICAL_HYDRA_8619, NpcID.ALCHEMICAL_HYDRA_8620, NpcID.ALCHEMICAL_HYDRA_8621, NpcID.ALCHEMICAL_HYDRA_8622, NpcID.ALCHEMICAL_HYDRA_8634),
    CHAOS_FANATIC(NpcID.CHAOS_FANATIC),
    CRAZY_ARCHAEOLOGIST(NpcID.CRAZY_ARCHAEOLOGIST),
    SCORPIA(NpcID.SCORPIA),
    KING_BLACK_DRAGON(NpcID.KING_BLACK_DRAGON, NpcID.KING_BLACK_DRAGON_2642, NpcID.KING_BLACK_DRAGON_6502),
    CHAOS_ELEMENTAL(NpcID.CHAOS_ELEMENTAL, NpcID.CHAOS_ELEMENTAL_6505),
    VETION(NpcID.VETION, NpcID.VETION_REBORN),
    SKELETON_HELLHOUND(NpcID.SKELETON_HELLHOUND, NpcID.SKELETON_HELLHOUND_6613),
    GREATER_SKELETON_HELLHOUND(NpcID.GREATER_SKELETON_HELLHOUND),
    VENENATIS(NpcID.VENENATIS, NpcID.VENENATIS_6610),
    CALLISTO(NpcID.CALLISTO, NpcID.CALLISTO_6609),
    OBOR(NpcID.OBOR),
    BRYOPHYTA(NpcID.BRYOPHYTA),
    THE_MIMIC(NpcID.THE_MIMIC, NpcID.THE_MIMIC_8633),
    SKOTIZO(NpcID.SKOTIZO),
    TZKAL_ZUK(NpcID.TZKALZUK),
    AHRIM_THE_BLIGHTED(NpcID.AHRIM_THE_BLIGHTED),
    DHAROK_THE_WRETCHED(NpcID.DHAROK_THE_WRETCHED),
    GUTHAN_THE_INFESTED(NpcID.GUTHAN_THE_INFESTED),
    TORAG_THE_CORRUPTED(NpcID.TORAG_THE_CORRUPTED),
    VERAC_THE_DEFILED(NpcID.VERAC_THE_DEFILED),
    GIANT_MOLE(NpcID.GIANT_MOLE, NpcID.GIANT_MOLE_6499),
    DERANGED_ARCHAEOLOGIST(NpcID.DERANGED_ARCHAEOLOGIST),
    DAGANNOTH_REX(NpcID.DAGANNOTH_REX, NpcID.DAGANNOTH_REX_6498),
    DAGANNOTH_PRIME(NpcID.DAGANNOTH_PRIME, NpcID.DAGANNOTH_PRIME_6497),
    SARACHNIS(NpcID.SARACHNIS),
    KALPHITE_QUEEN_CRAWLING(NpcID.KALPHITE_QUEEN, NpcID.KALPHITE_QUEEN_963, NpcID.KALPHITE_QUEEN_4303, NpcID.KALPHITE_QUEEN_6500),
    KALPHITE_QUEEN_AIRBORNE(NpcID.KALPHITE_QUEEN_965, NpcID.KALPHITE_QUEEN_4304, NpcID.KALPHITE_QUEEN_6501),
    KREE_ARRA(NpcID.KREEARRA, NpcID.KREEARRA_6492),
    COMMANDER_ZILYANA(NpcID.COMMANDER_ZILYANA, NpcID.COMMANDER_ZILYANA_6493),
    GENERAL_GRAARDOR(NpcID.GENERAL_GRAARDOR, NpcID.GENERAL_GRAARDOR_6494),
    KRIL_TSUTSAROTH(NpcID.KRIL_TSUTSAROTH, NpcID.KRIL_TSUTSAROTH_6495),
    CORPOREAL_BEAST(NpcID.CORPOREAL_BEAST),

    TEKTON(NpcID.TEKTON, NpcID.TEKTON_7541, NpcID.TEKTON_7542, NpcID.TEKTON_7545),
    TEKTON_ENRAGED(NpcID.TEKTON_ENRAGED, NpcID.TEKTON_ENRAGED_7544),
    ICE_DEMON(NpcID.ICE_DEMON, NpcID.ICE_DEMON_7585),
    LIZARDMAN_SHAMAN(NpcID.LIZARDMAN_SHAMAN, NpcID.LIZARDMAN_SHAMAN_6767, NpcID.LIZARDMAN_SHAMAN_7573, NpcID.LIZARDMAN_SHAMAN_7574, NpcID.LIZARDMAN_SHAMAN_7744, NpcID.LIZARDMAN_SHAMAN_7745, NpcID.LIZARDMAN_SHAMAN_8565),
    VANGUARD_MELEE(NpcID.VANGUARD_7527),
    VANGUARD_RANGED(NpcID.VANGUARD_7528),
    VANGUARD_MAGIC(NpcID.VANGUARD_7529),
    GUARDIAN(NpcID.GUARDIAN, NpcID.GUARDIAN_7570, NpcID.GUARDIAN_7571, NpcID.GUARDIAN_7572),
    VASA_NISTIRIO(NpcID.VASA_NISTIRIO, NpcID.VASA_NISTIRIO_7567),
    SKELETAL_MYSTIC(NpcID.SKELETAL_MYSTIC, NpcID.SKELETAL_MYSTIC_7605, NpcID.SKELETAL_MYSTIC_7606),
    MUTTADILE_SMALL(NpcID.MUTTADILE_7562),
    MUTTADILE_LARGE(NpcID.MUTTADILE_7563);
     
    private final Set<Integer> ids;

    NPCWithXpBoost(Integer... ids)
    {
        this.ids = Sets.newHashSet(ids);
    }

    static NPCWithXpBoost getNpc(int id)
    {
        for (NPCWithXpBoost npc : values())
        {
            if (npc.ids.contains(id))
            {
                return npc;
            }
        }

        return null;
    }
    
}
