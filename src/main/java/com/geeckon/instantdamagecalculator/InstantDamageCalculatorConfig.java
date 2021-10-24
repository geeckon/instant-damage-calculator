package com.geeckon.instantdamagecalculator;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

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
}
