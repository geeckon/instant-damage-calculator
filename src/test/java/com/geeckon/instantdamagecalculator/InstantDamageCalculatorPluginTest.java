package com.geeckon.instantdamagecalculator;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class InstantDamageCalculatorPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(InstantDamageCalculatorPlugin.class);
		RuneLite.main(args);
	}
}