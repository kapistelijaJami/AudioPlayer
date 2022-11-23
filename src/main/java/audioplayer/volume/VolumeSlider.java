package audioplayer.volume;

import audioplayer.audio.AudioPlayer;
import audioplayer.HelperFunctions;
import uilibrary.enums.Alignment;
import java.awt.BasicStroke;
import java.awt.Color;
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
	
	public VolumeSlider(int x, int y, int width, int height, AudioPlayer audioPlayer) {
		super(x, y, width, height);
		
		volumeController = new VolumeController(audioPlayer);
	}
	
	/**
	 * Change volume by little bit with scroll.
	 * @param scrollDirection Should be -e.getWheelRotation()
	 */
	public void scroll(int scrollDirection) {
		volumeController.scroll(scrollDirection);
	}
	
	public void setVolume(int percent) {
		volumeController.setVolume(percent);
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
	
	public int getCurrentVolumePercent() {
		return volumeController.getCurrentVolumePercent();
	}
	
	public void reapplyVolume() {
		volumeController.reapplyVolume();
	}

	public boolean hover(MouseEvent e) {
		if (dragging) {
			return true;
		}
		Rectangle r = getButtonHitbox();
		return r.contains(e.getX(), e.getY());
	}

	public void setDragging(boolean b) {
		dragging = b;
	}

	public void mousePressed(MouseEvent e) {
		if (hover(e)) {
			dragging = true;
		}
		
		if (!dragging && isInside(e.getX(), e.getY())) {
			Rectangle r = getButtonHitbox();
			if (e.getY() < r.getY()) {
				scroll(1);
			} else {
				scroll(-1);
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
			
			setVolume(getClosestVolumePercent(e.getY()));
			return true;
		}
		return false;
	}
}
