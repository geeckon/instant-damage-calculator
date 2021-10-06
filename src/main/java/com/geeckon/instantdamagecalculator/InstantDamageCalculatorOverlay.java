package com.geeckon.instantdamagecalculator;

import net.runelite.api.Client;
import net.runelite.client.ui.overlay.OverlayMenuEntry;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;

import javax.inject.Inject;
import java.awt.*;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

class InstantDamageCalculatorOverlay extends OverlayPanel
{
	private final Client client;
	private final InstantDamageCalculatorConfig config;
	private final InstantDamageCalculatorPlugin plugin;

	@Inject
	private InstantDamageCalculatorOverlay(Client client, InstantDamageCalculatorConfig config, InstantDamageCalculatorPlugin plugin)
	{
		super(plugin);
		this.plugin = plugin;
		this.client = client;
		this.config = config;
		setPosition(OverlayPosition.TOP_RIGHT);
		setPriority(OverlayPriority.MED);
		getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Instant damage overlay"));
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.displayDamageOverlay()) {
			return null;
		}

		final Color strColor = new Color(238, 51, 51);
		String str = plugin.getHit() + "";

		panelComponent.getChildren().add(LineComponent.builder()
				.left("Current hit")
				.right(str)
				.rightColor(strColor)
				.build());

		return super.render(graphics);
	}
}
