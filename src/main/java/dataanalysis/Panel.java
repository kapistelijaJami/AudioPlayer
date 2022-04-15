package dataanalysis;

import dataanalysis.enums.DividerOrientation;
import dataanalysis.enums.DividerOrientation.ScalingDirection;

public interface Panel {
	public int getWidth();
	public int getHeight();
	public int getX();
	public int getY();
	public void setX(int x);
	public void setY(int y);
	public void setWidth(int width);
	public void setHeight(int height);
	
	public default int getSpace(DividerOrientation dir) {
		switch (dir) {
			case HORIZONTAL:
				return getHeight();
			case VERTICAL:
				return getWidth();
		}
		
		return 0;
	}
	
	public default void setSpace(int val, ScalingDirection dir) {
		switch (dir) {
			case TOP:
				int diff = getHeight() - val;
				setHeight(val);
				setY(getY() + diff);
				break;
			case LEFT:
				diff = getWidth() - val;
				setWidth(val);
				setX(getX() + diff);
				break;
			case BOTTOM:
				setHeight(val);
				break;
			case RIGHT:
				setWidth(val);
				break;
		}
	}
}
