package dataanalysis;

import static dataanalysis.PanelContainer.ContainerType.DIVIDER;
import static dataanalysis.PanelContainer.ContainerType.PANEL;

public class PanelContainer implements Panel {
	public enum ContainerType {
		PANEL, DIVIDER;
	}
	
	public ContainerType type;
	public Panel panel;
	public Divider divider;
	
	public PanelContainer(Panel panel) {
		type = ContainerType.PANEL;
		this.panel = panel;
	}
	
	public PanelContainer(Divider divider) {
		type = ContainerType.DIVIDER;
		this.divider = divider;
	}
	
	@Override
	public int getWidth() {
		if (type == PANEL) {
			return panel.getWidth();
		} else if (type == DIVIDER) {
			switch (divider.dir) {
				case HORIZONTAL:
					return divider.getLength();
				case VERTICAL:
					return divider.getTotalSpace();
			}
		}
		
		return 0;
	}
	
	@Override
	public int getHeight() {
		if (type == PANEL) {
			return panel.getHeight();
		} else if (type == DIVIDER) {
			switch (divider.dir) {
				case HORIZONTAL:
					return divider.getTotalSpace();
				case VERTICAL:
					return divider.getLength();
			}
		}
		
		return 0;
	}
	
	@Override
	public int getX() {
		if (type == PANEL) {
			return panel.getX();
		} else if (type == DIVIDER) {
			divider.getFirst().getX();
		}
		
		return 0;
	}
	
	@Override
	public int getY() {
		if (type == PANEL) {
			return panel.getY();
		} else if (type == DIVIDER) {
			divider.getFirst().getY();
		}
		
		return 0;
	}
	
	@Override
	public void setX(int x) {
		if (type == PANEL) {
			panel.setX(x);
		} else if (type == DIVIDER) {
			divider.setX(x);
		}
	}
	
	@Override
	public void setY(int y) {
		if (type == PANEL) {
			panel.setY(y);
		} else if (type == DIVIDER) {
			divider.setY(y);
		}
	}
	
	@Override
	public void setWidth(int width) {
		if (type == PANEL) {
			panel.setWidth(width);
		} else if (type == DIVIDER) {
			switch (divider.dir) {
				case HORIZONTAL:
					divider.setLength(width, true);
					break;
				case VERTICAL:
					divider.setMaxSpace(width, true);
					break;
			}
		}
	}
	
	@Override
	public void setHeight(int height) {
		if (type == PANEL) {
			panel.setHeight(height);
		} else if (type == DIVIDER) {
			switch (divider.dir) {
				case HORIZONTAL:
					divider.setMaxSpace(height, true);
					break;
				case VERTICAL:
					divider.setLength(height, true);
					break;
			}
		}
	}
}
