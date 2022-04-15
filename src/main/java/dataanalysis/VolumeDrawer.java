package dataanalysis;

import java.awt.Color;
import java.awt.Graphics2D;

//TODO: I think this is buggy, it doesn't display correct values etc.
public class VolumeDrawer implements Panel {
	public int x, y, width, height;
	public double volumeLeft, volumeRight;
	private Game game;
	
	public VolumeDrawer(int x, int y, int width, int height, Game game) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.game = game;
	}
	
	public void render(Graphics2D g) {
		int h = height / 2;
		
		renderVolume(g, game.audioPlayer.getAudioLevel().getLeft(), 0, h);
		renderVolume(g, game.audioPlayer.getAudioLevel().getRight(), h, h);
		
		g.setColor(Color.BLACK);
		g.drawLine(x, y + h, width, y + h);
	}
	
	public void renderVolume(Graphics2D g, double volume, int yOffset, int height) {
		g.setColor(Color.GRAY);
		g.fillRect(x, y + yOffset, width, height);
		
		g.setColor(Color.RED);
		g.fillRect(x, y + yOffset, (int) (width * volume), height);
		
		g.setColor(Color.ORANGE);
		g.fillRect(x, y + yOffset, (int) (width * Math.min(0.9, volume)), height);
		
		g.setColor(Color.GREEN);
		g.fillRect(x, y + yOffset, (int) (width * Math.min(0.6, volume)), height);
	}
	
	@Override
	public int getX() {
		return x;
	}
	
	@Override
	public void setX(int x) {
		this.x = x;
	}
	
	@Override
	public int getY() {
		return y;
	}
	
	@Override
	public void setY(int y) {
		this.y = y;
	}
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public void setWidth(int width) {
		this.width = width;
	}
	
	@Override
	public int getHeight() {
		return height;
	}
	
	@Override
	public void setHeight(int height) {
		this.height = height;
	}
}
