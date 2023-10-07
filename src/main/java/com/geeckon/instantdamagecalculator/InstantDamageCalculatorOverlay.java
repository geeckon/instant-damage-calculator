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
		if (!config.displayDamageOverlay() || (config.expiry() != 0 && plugin.isOverlayExpired())) {
			return null;
		}

		final Color strColor = new Color(238, 51, 51);
		final String str = String.valueOf(plugin.getHit());
		final String leftText = config.displayOverlayText() ? "Current hit:" : "";

		panelComponent.getChildren().add(LineComponent.builder()
				.left(leftText)
				.right(str)
				.rightColor(strColor)
				.build());


		if (config.displayTotalDamageOverlay()) {
			final String totalStr = String.valueOf(plugin.getTotalHit());
			final String leftTotalText = config.displayOverlayText() ? "Total hit:" : "";

			panelComponent.getChildren().add(LineComponent.builder()
					.left(leftTotalText)
					.right(totalStr)
					.rightColor(strColor)
					.build());
		}

		return super.render(graphics);
	}
}
