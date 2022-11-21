package audioplayer;

import java.awt.Color;
import java.awt.Graphics2D;
import uilibrary.Panel;

public class VolumeDrawer extends Panel {
	public double volumeLeft, volumeRight;
	private AudioPlayerGUI game;
	
	public VolumeDrawer(int x, int y, int width, int height, AudioPlayerGUI game) {
		super(x, y, width, height);
		
		this.game = game;
	}
	
	@Override
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
}
