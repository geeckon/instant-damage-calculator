package com.geeckon.instantdamagecalculator;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Map;

/**
 * Draws the text of xp drops with our own font when "Scale XP drops" is enabled.
 *
 * The game can only draw xp drop text in a handful of fixed fonts, so to scale the text freely the
 * plugin blanks the game's text and draws it here instead, at the position of the (now empty) text
 * widget of each drop. The icons and the movement of the drop are still done by the game.
 */
class XpDropTextOverlay extends Overlay
{
	private final Client client;
	private final InstantDamageCalculatorConfig config;
	private final InstantDamageCalculatorPlugin plugin;

	@Inject
	private XpDropTextOverlay(Client client, InstantDamageCalculatorConfig config, InstantDamageCalculatorPlugin plugin)
	{
		this.client = client;
		this.config = config;
		this.plugin = plugin;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.scaleXpDrops())
		{
			return null;
		}

		graphics.setFont(plugin.getXpDropFont());
		final FontMetrics fm = graphics.getFontMetrics();

		for (Map.Entry<Integer, String> entry : plugin.getXpDropTexts().entrySet())
		{
			final Widget drop = client.getWidget(entry.getKey());
			if (drop == null || drop.isHidden())
			{
				continue;
			}

			final Widget[] children = drop.getChildren();
			if (children == null || children.length == 0 || children[0] == null)
			{
				continue;
			}

			final Widget text = children[0];
			if (text.isHidden())
			{
				continue;
			}

			final String str = entry.getValue();
			if (str.isEmpty())
			{
				continue;
			}

			// The text widget spans the whole drop; the game right-aligns the text in it
			final Rectangle bounds = text.getBounds();
			final int x = bounds.x + bounds.width - fm.stringWidth(str);
			final int y = bounds.y + (bounds.height - fm.getHeight()) / 2 + fm.getAscent();

			graphics.setColor(Color.BLACK);
			graphics.drawString(str, x + 1, y + 1);
			graphics.setColor(new Color(text.getTextColor()));
			graphics.drawString(str, x, y);
		}

		return null;
	}
}
