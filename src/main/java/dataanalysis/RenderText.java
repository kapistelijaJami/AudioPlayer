package dataanalysis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class RenderText {
	public enum Alignment {
		TOP, CENTER, LEFT, RIGHT, BOTTOM;
	}
	
	public static Font defaultFont = new Font("Serif", Font.BOLD, 25);
	
	protected static Font checkIfFontIsNull(Font font) {
		if (font == null) {
			font = defaultFont;
		}
		return font;
	}
	
	public static void renderText(Graphics2D g, String text, int x, int y, Font font, Color textColor, Color outlineColor, double outlineSize) {
		FontRenderContext frc = g.getFontRenderContext();
		g.setColor(textColor);
		font = checkIfFontIsNull(font);
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setStroke(new BasicStroke((float) outlineSize));
		
		TextLayout tl = new TextLayout(text, font, frc);
		
		Rectangle rect = tl.getOutline(null).getBounds();
		x -= rect.getWidth() / 2;
		y -= rect.getHeight() / 2;
		
		g.translate(x, y);
		tl.draw(g, 0, 0);
		
		g.setColor(outlineColor);
		g.draw(tl.getOutline(null));
		g.translate(-x, -y);
	}
	
	public static void renderTextSpeed(Graphics2D g, String text, int x, int y, Font font, Color textColor, Color outlineColor, double outlineSize, int size) {
		font = checkIfFontIsNull(font);
		
		GlyphVector gv = font.createGlyphVector(g.getFontRenderContext(), text);
		
		int width = 0;
		ArrayList<Shape> letters = new ArrayList<>();
		for (int i = 0; i < gv.getNumGlyphs(); i++) {
			Shape outline = gv.getGlyphOutline(i);
			width += outline.getBounds().width;
			letters.add(outline);
		}
		
		AffineTransform oldAt = g.getTransform();
		
		g.translate(x - width/2, y - size);
		g.setStroke(new BasicStroke((float) outlineSize));
		for (Shape shape : letters) {
			g.setColor(textColor);
			g.fill(shape);
			g.setColor(outlineColor);
			g.draw(shape);
		}
		
		g.setTransform(oldAt);
	}
	
	public static void renderNumbers(Graphics2D g, String number, int x, int y, ArrayList<BufferedImage> numbers, double scale) {
		if (Integer.parseInt(number) < 0) {
			return;
		}
		AffineTransform old = g.getTransform();
		g.translate(x, y);
		g.scale(scale, scale);
		g.translate(-x, -y);
		for (char c : number.toCharArray()) {
			x += renderNumber(g, "" + c, numbers, x, y);
		}
		g.setTransform(old);
	}
	
	private static int renderNumber(Graphics2D g, String c, ArrayList<BufferedImage> numbers, int x, int y) {
		int n = Integer.parseInt(c);
		BufferedImage im = numbers.get(n);
		g.drawImage(im, x, y, null);
		return im.getWidth();
	}
	
	//MAIN function
	public static Rectangle drawStringWithAlignment(Graphics2D g, String text, Rectangle rect, Font font, Alignment... aligns) {
		font = checkIfFontIsNull(font);
		FontMetrics metrics = g.getFontMetrics(font);
		int width = metrics.stringWidth(text);
		int height = getFontHeight(g, font);
		int x = rect.x + (rect.width - width) / 2; //These are already centered
		int y = rect.y + rect.height / 2 + height / 2;
		
		for (Alignment align : aligns) {
			switch (align) {
				case CENTER:
					break;
				case TOP:
					y = rect.y + getFontHeight(g, font);
					break;
				case BOTTOM:
					y = rect.y + rect.height;
					break;
				case LEFT:
					x = rect.x;
					break;
				case RIGHT:
					x = rect.x + rect.width - metrics.stringWidth(text);
					break;
			}
		}
		
		g.setFont(font);
		g.drawString(text, x, y);
		
		return new Rectangle(x, y - height, width, height);
	}
	
	public static int getFontHeight(Graphics2D g, Font font) {
		if (font == null) {
			font = defaultFont;
		}
		FontMetrics metrics = g.getFontMetrics(font);
		
		return metrics.getAscent() - metrics.getDescent() - metrics.getLeading() * 2;
	}
	
	public static int getStringWidth(Graphics2D g, Font font, String text) {
		if (font == null) {
			font = defaultFont;
		}
		FontMetrics metrics = g.getFontMetrics(font);
		
		return metrics.stringWidth(text);
	}
	
	//Line height
	public static int getFontLineInterval(Graphics2D g, Font font) {
		if (font == null) {
			font = defaultFont;
		}
		FontMetrics metrics = g.getFontMetrics(font);
		
		return metrics.getAscent() + metrics.getDescent() + metrics.getLeading();
	}
}
