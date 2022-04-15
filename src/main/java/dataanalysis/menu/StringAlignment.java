package dataanalysis.menu;

import dataanalysis.RenderText.Alignment;
import static dataanalysis.RenderText.Alignment.CENTER;
import java.awt.Color;

import java.awt.Font;

public class StringAlignment {
	private String text;
	private Alignment align;
	private Alignment align2;
	private Color color;
	private Font font;
	public static Font defaultFont = new Font("Serif", Font.BOLD, 20);
	
	public StringAlignment(String text, Color color) {
		this(text, color, defaultFont, CENTER, CENTER);
	}
	
	public StringAlignment(String text, Color color, Font font) {
		this(text, color, font, CENTER, CENTER);
	}
	
	public StringAlignment(String text, Color color, Alignment align) {
		this(text, color, defaultFont, align, CENTER);
	}
	
	public StringAlignment(String text, Color color, Font font, Alignment align) {
		this(text, color, font, align, CENTER);
	}
	
	public StringAlignment(String text, Color color, Alignment align, Alignment align2) {
		this(text, color, defaultFont, align, align2);
	}
	
	public StringAlignment(String text, Color color, Font font, Alignment align, Alignment align2) {
		this.text = text;
		this.color = color;
		this.font = font;
		this.align = align;
		this.align2 = align2;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Alignment getAlign() {
		return align;
	}

	public void setAlign(Alignment align) {
		this.align = align;
	}

	public Alignment getAlign2() {
		return align2;
	}

	public void setAlign2(Alignment align2) {
		this.align2 = align2;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}
}
