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
            keyName = "displayCurrentHit",
            name = "Display current hit in overlay",
            description = "If enabled, the overlay will include the latest damage hit",
            position = 2
    )
    default boolean displayCurrentHit()
    {
        return true;
    }

    @ConfigItem(
            keyName = "displayOverlayText",
            name = "Display overlay text",
            description = "If enabled, informative text is displayed in the overlay",
            position = 3
    )
    default boolean displayOverlayText()
    {
        return true;
    }

    @ConfigItem(
            keyName = "expiry",
            name = "Overlay Expiry",
            description = "Set the time until the overlay disappears",
            position = 4
    )
    @Units(Units.SECONDS)
    default int expiry() { return 10; }

    @ConfigItem(
            keyName = "precision",
            name = "Precision",
            description = "Set the number of decimal places to display. 0 is suggested unless you understand how the plugin and xp rounding works. Only affects the overlay, not replaced xp drops",
            position = 5
    )
    default int precision() { return 0; }

    @ConfigItem(
            keyName = "customBonusXP",
            name = "Custom NPC Bonus XP",
            description = "Add bonus XP modifiers for custom NPCs. Format is id:multiplier eg. \"12345:1.05\", once per line.",
            position = 6
    )
    default String customBonusXP() { return "// Phantom Muspah\n12077 : 2.075\n12078 : 2.075\n12079 : 2.075\n12080 : 2.075\n12082 : 2.075"; }

    @ConfigItem(
            keyName = "displayTotalDamageOverlay",
            name = "Display total damage overlay",
            description = "If enabled, an overlay is displayed which shows the total damage done, including the current hit. This total can then be reset using one of the following configurations. Can be useful for the Phantom Muspah boss",
            position = 7
    )
    default boolean displayTotalDamageOverlay()
    {
        return false;
    }

    @ConfigItem(
            keyName = "clearTotalOnOverlayExpiry",
            name = "Clear total damage on overlay expiry",
            description = "If enabled, the total damage counter will be set to 0 when the overlay expires.",
            position = 8
    )
    default boolean clearTotalOnOverlayExpiry()
    {
        return false;
    }

    @ConfigItem(
            keyName = "resetOnWeaponChange",
            name = "Reset total damage on weapon change",
            description = "If enabled with the \"Display total damage overlay\" setting, total damage will be reset whenever the equipped weapon is changed",
            position = 9
    )
    default boolean resetOnWeaponChange()
    {
        return false;
    }

    @ConfigItem(
            keyName = "resetOnPrayerChange",
            name = "Reset total damage on prayer change",
            description = "If enabled with the \"Display total damage overlay\" setting, total damage will be reset whenever the player activates a protection prayer different from the one they previously activated",
            position = 10
    )
    default boolean resetOnPrayerChange()
    {
        return false;
    }

    @ConfigItem(
            keyName = "resetOnBarrowsCryptEntry",
            name = "Reset total damage on Barrows Crypt entry",
            description = "If enabled with the \"Display total damage overlay\" setting, total damage will be reset whenever the player enters a Barrows crypt",
            position = 11
    )
    default boolean resetOnBarrowsCryptEntry()
    {
        return false;
    }

    @ConfigItem(
            keyName = "resetOnMuspahPhase",
            name = "Reset total damage on Muspah phase change",
            description = "If enabled with the \"Display total damage overlay\" setting, total damage will be reset whenever Muspah phase changes and will ignore teleport phase",
            position = 12
    )
    default boolean resetOnMuspahPhase()
    {
        return false;
    }

}
