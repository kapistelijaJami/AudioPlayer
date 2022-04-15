package dataanalysis.menu;

import dataanalysis.RenderText;
import dataanalysis.Window;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.function.Consumer;

import java.awt.BasicStroke;
import java.awt.RenderingHints;

public class Button {
	protected boolean isMouseOver = false;
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected boolean selected = false;
	protected boolean inactive = false;
	
	protected Color color;
	protected Color selectedColor = Color.YELLOW;
	protected Color highlightedColor;
	protected Color edgeColor = null;
	
	protected int textPadding = 5;
	
	protected ArrayList<StringAlignment> texts = new ArrayList<>();
	
	protected Consumer<Object> action;
	protected Consumer<Object> hoverAction = null;
	
	public Button(int x, int y, int width, int height, Color color) {
		this(x, y, width, height, color, null);
	}
	
	public Button(int x, int y, int width, int height, Color color, Consumer<Object> action) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
		this.action = action;
		
		highlightedColor = HelperFunctions.getDarkerColor(color, 100);
	}
	
	public void setAction(Consumer<Object> action) {
		this.action = action;
	}
	
	public void setSelectedColor(Color color) {
		selectedColor = color;
	}
	
	public void setHighlightedColor(Color color) {
		highlightedColor = color;
	}
	
	public void setEdgeColor(Color color) {
		edgeColor = color;
	}
	
	public void addStringAlignment(StringAlignment s) {
		this.texts.add(s);
	}
	
	public void setStringAlignment(StringAlignment s) {
		if (texts.isEmpty()) {
			texts.add(s);
		} else {
			this.texts.set(0, s);
		}
	}
	
	public String getMainText() {
		return texts.get(0).getText();
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public void setIsMouseOver(boolean isMouseOver) {
		this.isMouseOver = isMouseOver;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
	
	public Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}
	
	public void setHoverAction(Consumer<Object> action) {
		this.hoverAction = action;
	}
	
	public boolean hover(int x, int y) {
		if (inactive) {
			return false;
		}
		if (isInside(x, y)) {
			if (isMouseOver) {
				return true;
			}
			isMouseOver = true;
			if (hoverAction != null) {
				hoverAction.accept(null);
			}
			return true;
		}
		isMouseOver = false;
		return false;
	}
	
	public boolean isInside(int x, int y) {
		return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
	}
	
	public void click() {
		if (inactive) {
			return;
		}
		if (action != null) {
			action.accept(null);
		}
	}
	
	public void setActionMethod(Consumer<Object> action) {
		this.action = action;
	}
	
	public void update() {}
	
	public void render(Graphics2D g) {
		render(g, inactive);
	}
	
	public void render(Graphics2D g, boolean inactive) {
		renderBox(g, isMouseOver, inactive);
		
		g.setColor(Color.black);
		renderTexts(g, inactive);
	}
	
	public void render(Graphics2D g, int x, int y) {}
	
	public void renderBox(Graphics2D g, boolean highlighted, boolean inactive) {
		if (inactive) {
			g.setColor(Color.decode("#747474"));
			g.fillRect(x, y, width, height);
		} else if (highlighted) {
			g.setColor(highlightedColor);
			g.fillRect(x, y, width, height);
		} else {
			g.setColor(color);
			g.fillRect(x, y, width, height);
		}
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		if (selected) {
			g.setColor(selectedColor);
			int thickness = 2;
			g.setStroke(new BasicStroke(thickness));
			g.drawRect(x + thickness / 2, y + thickness / 2, width - thickness, height - thickness);
		} else if (edgeColor != null) {
			g.setColor(edgeColor);
			int thickness = 2;
			g.setStroke(new BasicStroke(thickness));
			g.drawRect(x + thickness / 2, y + thickness / 2, width - thickness, height - thickness);
		}
		
		Window.setGraphicsRenderingHints(g);
	}
	
	public void renderTexts(Graphics2D g, boolean inactive) {
		g.setColor(HelperFunctions.getDarkerColor(Color.decode("#747474"), 70));
		
		for (StringAlignment sa : texts) {
			if (!inactive) {
				g.setColor(sa.getColor());
			}
			RenderText.drawStringWithAlignment(g, sa.getText(), new Rectangle(x + textPadding, y + textPadding, width - textPadding * 2, height - textPadding * 2), sa.getFont(), sa.getAlign(), sa.getAlign2());
		}
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setInactive(boolean b) {
		inactive = b;
	}
}
