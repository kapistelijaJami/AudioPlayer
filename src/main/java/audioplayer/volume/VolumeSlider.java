package audioplayer.volume;

import audioplayer.audio.AudioPlayer;
import audioplayer.HelperFunctions;
import audioplayer.MyCursor;
import uilibrary.enums.Alignment;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import uilibrary.Panel;
import uilibrary.RenderText;

public class VolumeSlider extends Panel {
	private VolumeController volumeController;
	private int yOffset = 20;
	private boolean dragging = false;
	
	
	public VolumeSlider(int x, int y, int width, int height, VolumeController volumeController) {
		super(x, y, width, height);
		
		this.volumeController = volumeController;
	}
	
	@Override
	public void render(Graphics2D g) {
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(x, y, width, height);
		
		renderSlider(g);
	}
	
	private void renderSlider(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(2f));
		g.drawLine(getMidLineX(), yOffset, getMidLineX(), (height - yOffset));
		
		renderValueLines(g);
		
		g.setColor(Color.DARK_GRAY);
		g.setStroke(new BasicStroke(1f));
		Rectangle hitbox = getButtonHitbox();
		g.fillRect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
		
		g.setColor(Color.BLACK);
		g.drawLine(hitbox.x, hitbox.y + hitbox.height / 2, hitbox.x + hitbox.width - 1, hitbox.y + hitbox.height / 2);
	}
	
	private void renderValueLines(Graphics2D g) {
		g.setStroke(new BasicStroke(1));
		int lineLength = 30;
		int sliderHeight = height - 2 * yOffset;
		
		for (int i = 0; i <= 40; i++) {
			if (i % 4 == 0) {
				g.setStroke(new BasicStroke(2f));
			} else if (i % 2 == 0) {
				lineLength = 25;
				g.setStroke(new BasicStroke(1f));
			} else {
				lineLength = 15;
				g.setStroke(new BasicStroke(1f));
			}
			
			int y = (int) (yOffset + i * (sliderHeight / 40.0));
			g.drawLine(getMidLineX() - lineLength / 2, y, getMidLineX() + lineLength / 2, y);
			
			lineLength = 30;
			
			if (i % 2 == 0) {
				Rectangle rect = new Rectangle(getMidLineX() + lineLength / 2 + 5, y - 30, 60, 60);
				RenderText.drawStringWithAlignment(g, 200 - (i * 5) + " %", rect, new Font("Serif", Font.PLAIN, 15), Alignment.LEFT);
			}
		}
	}
	
	public boolean isInside(int x, int y) {
		return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
	}
	
	public boolean isInsideY(int y) {
		return y >= this.y && y <= this.y + height;
	}
	
	private Rectangle getButtonHitbox() {
		int centerPoint = (int) (height - yOffset - (volumeController.getCurrentVolumePercent() / 200.0) * (height - yOffset * 2));
		int w = 21;
		int h = 11;
		return new Rectangle(getMidLineX() - w/2, centerPoint - h/2, w, h);
	}
	
	private int getPercentCenterY(int percent) {
		return (int) (height - yOffset - (percent / 200.0) * (height - yOffset * 2));
	}
	
	private int getMidLineX() {
		return x + width / 2 - 15;
	}

	public boolean hover(MouseEvent e, MyCursor cursor) {
		if (dragging) {
			return true;
		}
		Rectangle r = getButtonHitbox();
		if (r.contains(e.getX(), e.getY())) {
			cursor.type = Cursor.N_RESIZE_CURSOR;
			return true;
		}
		return false;
	}

	public void setDragging(boolean b) {
		dragging = b;
	}

	public void mousePressed(MouseEvent e) {
		if (hover(e, new MyCursor())) {
			dragging = true;
		}
		
		if (!dragging && isInside(e.getX(), e.getY())) {
			Rectangle r = getButtonHitbox();
			if (e.getY() < r.getY()) {
				volumeController.scroll(1);
			} else {
				volumeController.scroll(-1);
			}
		}
	}
	
	private int getClosestVolumePercent(int y) {
		int closest = 0;
		double minDist = Integer.MAX_VALUE;
		int vol = 0;

		while (true) {
			int center = getPercentCenterY(vol);
			double dist = HelperFunctions.dist(center, y);
			if (dist < minDist) {
				minDist = dist;
				closest = vol;
			} else {
				break;
			}
			vol = VolumeController.nextVolumePercent(vol, 1);
		}
		return closest;
	}
	
	public boolean mouseDragged(MouseEvent e) {
		if (dragging) {
			Rectangle r = getButtonHitbox();
			
			if (e.getY() >= r.getY() && e.getY() <= r.getY() + r.getHeight()) {
				return true;
			}
			
			volumeController.setVolume(getClosestVolumePercent(e.getY()));
			return true;
		}
		return false;
	}
}
