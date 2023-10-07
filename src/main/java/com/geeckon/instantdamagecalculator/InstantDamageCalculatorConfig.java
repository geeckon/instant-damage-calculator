package com.geeckon.instantdamagecalculator;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Units;

@ConfigGroup("instantDamageCalculator")
public interface InstantDamageCalculatorConfig extends Config
{
    @ConfigItem(
            keyName = "replaceXpDrops",
            name = "Replace XP drops",
            description = "If enabled, combat xp drops will be replaced with the damage dealt",
            position = 0
    )
    default boolean replaceXpDrops()
    {
        return true;
    }

    @ConfigItem(
            keyName = "displayDamageOverlay",
            name = "Display damage overlay",
            description = "If enabled, an overlay is displayed which shows the latest damage hit",
            position = 1
    )
    default boolean displayDamageOverlay()
    {
        return false;
    }

    @ConfigItem(
            keyName = "displayOverlayText",
            name = "Display overlay text",
            description = "If enabled, informative text is displayed in the overlay",
            position = 2
    )
    default boolean displayOverlayText()
    {
        return true;
    }

    @ConfigItem(
            keyName = "expiry",
            name = "Overlay Expiry",
            description = "Set the time until the overlay disappears",
            position = 3
    )
    @Units(Units.SECONDS)
    default int expiry() { return 10; }

    @ConfigItem(
            keyName = "customBonusXP",
            name = "Custom NPC Bonus XP",
            description = "Add bonus XP modifiers for custom NPCs. Format is id:multiplier eg. \"12345:1.05\", once per line.",
            position = 4
    )
    default String customBonusXP() { return "// Phantom Muspah\n12077 : 2.075\n12078 : 2.075\n12079 : 2.075\n12080 : 2.075\n12082 : 2.075"; }

    @ConfigItem(
            keyName = "displayTotalDamageOverlay",
            name = "Display total damage overlay",
            description = "If enabled, an overlay is displayed which shows the total damage done, including the current hit. This total can then be reset using one of the following configurations. Can be useful for the Phantom Muspah boss",
            position = 5
    )
    default boolean displayTotalDamageOverlay()
    {
        return false;
    }

    @ConfigItem(
            keyName = "resetOnWeaponChange",
            name = "Reset total damage on weapon change",
            description = "If enabled with the \"Display total damage overlay\" setting, total damage will be reset whenever the equipped weapon is changed",
            position = 6
    )
    default boolean resetOnWeaponChange()
    {
        return false;
    }

    @ConfigItem(
            keyName = "resetOnPrayerChange",
            name = "Reset total damage on prayer change",
            description = "If enabled with the \"Display total damage overlay\" setting, total damage will be reset whenever the player activates a protection prayer different from the one they previously activated",
            position = 7
    )
    default boolean resetOnPrayerChange()
    {
        return false;
    }

}
