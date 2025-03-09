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
		final String intStr = String.valueOf((int) plugin.getHit());
		final String str = String.valueOf(plugin.getHit());
		final String leftText = config.displayOverlayText() ? "Current hit:" : "";

		/**
		 * This is a mess because I couldn't figure out how to condense this, so I wrote it very explicitly. I'm tired
		 */
		if (config.displayCurrentHit()) {
			final LineComponent.LineComponentBuilder hitLine = LineComponent.builder()
				.left(leftText)
				.rightColor(strColor);

			if (config.precision() == 0) {
				hitLine.right(intStr);
			} else {
				hitLine.right(str);
			}
			panelComponent.getChildren().add(hitLine.build());
		}


		if (config.displayTotalDamageOverlay()) {
			final String intTotalStr = String.valueOf((int) plugin.getTotalHit());
			final String totalStr = String.valueOf(plugin.getTotalHit());
			final String leftTotalText = config.displayOverlayText() ? "Total hit:" : "";

			final LineComponent.LineComponentBuilder totalHitLine = LineComponent.builder()
					.left(leftTotalText)
					.rightColor(strColor);

			if (config.precision() == 0) {
				totalHitLine.right(intTotalStr);
			} else {
				totalHitLine.right(totalStr);
			}
			panelComponent.getChildren().add(totalHitLine.build());
		}

		return super.render(graphics);
	}
}
