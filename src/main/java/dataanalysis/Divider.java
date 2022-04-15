package dataanalysis;

import dataanalysis.enums.DividerOrientation;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;

//Tee tähän viel sillei, että pystyy pilkkoo useamminkin, ja sillon jos dividerin toisella puolella on 2 panelia, niin ottaa varmaan toiseksi sen niiden dividerin
public class Divider {
	private int value; //actual vertical/horizontal distance from top
	private int length; //how long the divider is
	private int thickness; //how thick it is for mouse to grab onto
	private int minSpace; //how much space both panels have to have minimum.
	private int maxSpace;
	
	public DividerOrientation dir;
	
	private boolean dragging = false;
	private boolean valueChanged = false;
	private Point lastMouseLocation;
	private boolean lengthChanged = false;
	private boolean maxSpaceChanged = false;
	
	private boolean movable = true;
	
	private PanelContainer first, second; //panels that this scales and is in between at.
	
	public Divider(int value, int thickness, int minSpace, PanelContainer first, PanelContainer second, DividerOrientation dir) {
		this.value = value;
		this.thickness = thickness;
		this.minSpace = minSpace;
		
		this.first = first;
		this.second = second;
		
		this.maxSpace = first.getSpace(dir) + second.getSpace(dir);
		
		
		this.dir = dir;
		changePosition();
		
		switch (dir) {
			case HORIZONTAL:
				length = first.getWidth();
				break;
			case VERTICAL:
				length = first.getHeight();
				break;
		}
	}
	
	public void setMovable(boolean b) {
		movable = b;
	}
	
	public void update() {
		if (dragging || valueChanged) {
			calculateNewValueFromCoordinates(lastMouseLocation.x, lastMouseLocation.y);
		}
		if (lengthChanged) {
			updateLength();
		}
		if (maxSpaceChanged) {
			updateMaxSpace();
		}
	}
	
	public void render(Graphics2D g) {
		g.setColor(Color.BLACK);
		Rectangle rect = getHitbox();
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
	}
	
	public void updateValue(int newVal) {
		newVal = Math.max(0 + minSpace, Math.min(getTotalSpace() - minSpace, newVal));
		
		value = newVal;
		changePosition();
	}
	
	private void changePosition() {
		int totalSpace = getTotalSpace(); //have to take it into a variable, because the first method call already changes this result, and I need it in the second one.
		first.setSpace(value, dir.getFirst());
		second.setSpace(totalSpace - value, dir.getSecond());
	}
	
	public int getTotalSpace() {
		return first.getSpace(dir) + second.getSpace(dir);
	}
	
	public int getX() {
		return this.first.getX() + value - thickness / 2;
	}
	
	public int getY() {
		return this.first.getY() + value - thickness / 2;
	}
	
	public void setLength(int length, boolean instantUpdate) {
		//System.out.println("dividerLengthSet: " + length);
		this.length = length;
		if (instantUpdate) {
			updateLength();
		} else {
			lengthChanged = true;
		}
	}
	
	public Rectangle getHitbox() {
		Rectangle rect = new Rectangle();
		switch (dir) {
			case HORIZONTAL:
				rect.x = first.getX();
				rect.y = first.getY() + value - thickness / 2;
				rect.width = length;
				rect.height = thickness;
				break;
			case VERTICAL:
				rect.x = first.getX() + value - thickness / 2;
				rect.y = first.getY();
				rect.width = thickness;
				rect.height = length;
				break;
		}
		return rect;
	}
	
	public boolean isInside(int x, int y) {
		return getHitbox().contains(x, y);
	}
	
	private void calculateNewValueFromCoordinates(int x, int y) {
		switch (dir) {
			case HORIZONTAL:
				updateValue(y - first.getY());
				break;
			case VERTICAL:
				updateValue(x - first.getX());
				break;
		}
		
		valueChanged = false;
	}
	
	public boolean mousePressed(MouseEvent e) {
		if (!movable) {
			return false;
		}
		if (dragging || isInside(e.getX(), e.getY())) {
			lastMouseLocation = e.getPoint();
			
			valueChanged = true;
			dragging = true;
			return true;
		}
		
		return false;
	}
	
	public boolean mouseDragged(MouseEvent e) { //almost the same as mousePressed, except we dont want to initiate the drag.
		if (!movable) {
			return false;
		}
		if (!dragging) {
			return false;
		}
		
		return mousePressed(e);
	}
	
	public boolean mouseReleased(MouseEvent e) {
		if (!movable) {
			return false;
		}
		if (dragging) {
			lastMouseLocation = e.getPoint();
			valueChanged = true;
			dragging = false;
			return true;
		}
		
		return false;
	}
	
	public boolean hover(MouseEvent e) {
		if (!movable) {
			return false;
		}
		return dragging || isInside(e.getX(), e.getY());
	}
	
	public int getCursorTypeForHover() {
		switch (dir) {
			case HORIZONTAL:
				return Cursor.N_RESIZE_CURSOR;
			case VERTICAL:
				return Cursor.W_RESIZE_CURSOR;
		}
		return Cursor.DEFAULT_CURSOR;
	}
	
	private void updateLength() {
		switch (dir) {
			case HORIZONTAL:
				first.setWidth(length);
				second.setWidth(length);
				break;
			case VERTICAL:
				first.setHeight(length);
				second.setHeight(length);
				break;
		}
		
		lengthChanged = false;
	}
	
	private void updateMaxSpace() {
		int totalSpace = getTotalSpace();
		double firstCoefficient = first.getSpace(dir) / (double) totalSpace;
		
		first.setSpace((int) (firstCoefficient * maxSpace), dir.getFirst());
		
		value = first.getSpace(dir);
		switch (dir) {
			case HORIZONTAL:
				second.setY(value);
				break;
			case VERTICAL:
				second.setX(value);
				break;
		}
		
		second.setSpace((int) (maxSpace - first.getSpace(dir)), dir.getFirst()); //this is dir.getFirst() on purpose, to change the space so that only space changes, not the start location. We changed start location manually.
		
		if (first.getSpace(dir) < minSpace) {
			updateValue(minSpace);
		}
		
		if (second.getSpace(dir) < minSpace) {
			updateValue(getTotalSpace() - minSpace);
		}
		
		maxSpaceChanged = false;
	}
	
	public void setMaxSpace(int space, boolean instantUpdate) {
		maxSpace = space;
		if (instantUpdate) {
			updateMaxSpace();
		} else {
			maxSpaceChanged = true;
		}
	}

	public int getLength() {
		return length;
	}

	public PanelContainer getFirst() {
		return first;
	}

	public void setX(int x) {
		switch (dir) {
			case HORIZONTAL:
				first.setX(x);
				second.setX(x);
				break;
			case VERTICAL:
				first.setX(x);
				break;
		}
	}

	public void setY(int y) {
		switch (dir) {
			case HORIZONTAL:
				first.setY(y);
				break;
			case VERTICAL:
				first.setY(y);
				second.setY(y);
				break;
		}
	}
}
