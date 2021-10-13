package com.geeckon.instantdamagecalculator;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Units;

import java.awt.*;

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
            keyName = "hideSkillIcons",
            name = "Hide skill icons",
            description = "Configure if XP drops will show their respective skill icons",
            position = 2
    )
    default boolean hideSkillIcons()
    {
        return false;
    }

    @ConfigItem(
            keyName = "standardColor",
            name = "Standard Color",
            description = "XP drop color when no prayer is active",
            position = 3
    )
    Color standardColor();

    @ConfigItem(
            keyName = "meleePrayerColor",
            name = "Melee Prayer Color",
            description = "XP drop color when a melee prayer is active",
            position = 4
    )
    default Color getMeleePrayerColor()
    {
        return new Color(0x15, 0x80, 0xAD);
    }

    @ConfigItem(
            keyName = "rangePrayerColor",
            name = "Range Prayer Color",
            description = "XP drop color when a range prayer is active",
            position = 5
    )
    default Color getRangePrayerColor()
    {
        return new Color(0x15, 0x80, 0xAD);
    }

    @ConfigItem(
            keyName = "magePrayerColor",
            name = "Mage Prayer Color",
            description = "XP drop color when a mage prayer is active",
            position = 6
    )
    default Color getMagePrayerColor()
    {
        return new Color(0x15, 0x80, 0xAD);
    }
}
