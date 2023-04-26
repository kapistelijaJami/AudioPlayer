package audioplayer.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Locale;
import uilibrary.RenderMultilineText;
import uilibrary.RenderMultilineText.TextHorizontalAlign;

public class LoadingBox {
	public static void renderLoadingBox(Graphics2D g, Rectangle bounds, double percentage) {
		Dimension size = new Dimension(250, 150);
		int x = bounds.x + (bounds.width - size.width) / 2;
		int y = bounds.y + (bounds.height - size.height) / 2;
		
		g.setColor(Color.DARK_GRAY);
		g.fillRect(x - 3, y - 3, size.width + 6, size.height + 6);
		
		g.setColor(Color.GRAY);
		g.fillRect(x, y, size.width, size.height);
		
		g.setColor(Color.BLACK);
		String s = "LOADING...\n" + String.format(Locale.US, "%.1f%%", percentage);
		RenderMultilineText.drawMultilineText(g, s, new Rectangle(x, y, size.width, size.height), null, false, TextHorizontalAlign.CENTER);
	}
}
