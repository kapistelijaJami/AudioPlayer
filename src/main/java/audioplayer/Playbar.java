package audioplayer;

import audiofilereader.MusicData;
import uilibrary.enums.Alignment;
import uilibrary.menu.Button;
import uilibrary.menu.StringAlignment;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import uilibrary.RenderText;

//Current time, play, stop etc buttons. maybe other information
public class Playbar {
	private int x, y, width, height;
	private List<Button> buttons = new ArrayList<>();
	private Rectangle speedMultiplierSpace;
	private int xMargin = 2;
	private int yMargin = 2;
	
	private final AudioPlayer audioPlayer;
	
	public Playbar(int x, int y, int width, int height, AudioPlayer audioPlayer, WaveformDrawer waveformDrawer, Consumer<Object> pause, Consumer<Object> stop) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.audioPlayer = audioPlayer;
		
		int buttonHeight = height - yMargin * 2;
		int buttonWidth = buttonHeight + 15;
		
		Button b = new Button(x + xMargin, y + yMargin, buttonWidth, buttonHeight, Color.GRAY, pause);
		b.addStringAlignment(new StringAlignment("Pause", Color.BLACK));
		buttons.add(b);
		
		b = new Button(x + xMargin + buttonWidth + xMargin, y + yMargin, buttonWidth, buttonHeight, Color.GRAY, stop);
		b.addStringAlignment(new StringAlignment("Stop", Color.BLACK));
		buttons.add(b);
		
		b = new Button(x + xMargin + buttonWidth * 2 + xMargin * 2, y + yMargin, buttonWidth, buttonHeight, Color.GRAY, o -> { audioPlayer.setCurrentTimeByMicros(0); waveformDrawer.setPlayStartFrame(0); });
		b.addStringAlignment(new StringAlignment("<<", Color.BLACK));
		buttons.add(b);
		
		speedMultiplierSpace = new Rectangle(x + xMargin * 3 + buttonWidth * 3 + 10, y + yMargin, buttonWidth, buttonHeight);
	}
	
	public void render(Graphics2D g, boolean LOADING) {
		g.setColor(new Color(25, 25, 25));
		g.fillRect(x, y, width, height);
		
		for (Button button : buttons) {
			button.render(g);
		}
		
		g.setColor(Color.LIGHT_GRAY);
		
		Rectangle neededSpace = RenderText.drawStringWithAlignment(g, (audioPlayer.currentSampleRateMultiplierPercent / 100.0) + "x", speedMultiplierSpace, null, Alignment.LEFT);
		
		String timeDurationString = "";
		
		if (!LOADING) {
			MusicData musicData = audioPlayer.getMusicData();
			long currentMicros = Math.min(musicData.getDurationMicros(), Math.max(0, audioPlayer.getCurrentMicros()));
			timeDurationString = musicData.microsToDurationString(currentMicros) + " / " + musicData.microsToDurationString(musicData.getDurationMicros());
		}
		
		
		RenderText.drawStringWithAlignment(g,
				timeDurationString,
				new Rectangle(neededSpace.x + neededSpace.width + 15, speedMultiplierSpace.y, speedMultiplierSpace.width, speedMultiplierSpace.height),
				null, Alignment.LEFT);
	}
	
	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		int diffX = x - this.x;
		this.x = x;
		
		for (Button button : buttons) {
			button.setX(button.getX() + diffX);
		}
		
		speedMultiplierSpace.x += diffX;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		int diffY = y - this.y;
		this.y = y;
		
		for (Button button : buttons) {
			button.setY(button.getY() + diffY);
		}
		
		speedMultiplierSpace.y += diffY;
	}
	
	public int getWidth() {
		return width;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}

	public boolean mousePressed(MouseEvent e) {
		for (Button button : buttons) {
			if (button.isInside(e.getX(), e.getY())) {
				button.click();
				return true;
			}
		}
		
		return false;
	}

	public boolean hover(MouseEvent e) {
		boolean wasHover = false;
		for (Button button : buttons) {
			if (button.hover(e.getX(), e.getY())) {
				wasHover = true;
			}
		}
		return wasHover;
	}
	
	public void overridePauseButton(Consumer<Object> pause) {
		if (buttons.size() >= 1) {
			buttons.get(0).setAction(pause);
		}
	}
	
	public void overrideStopButton(Consumer<Object> stop) {
		if (buttons.size() >= 2) {
			buttons.get(1).setAction(stop);
		}
	}
	
	public void overrideGoToStartButton(Consumer<Object> goToStart) {
		if (buttons.size() >= 3) {
			buttons.get(2).setAction(goToStart);
		}
	}
	
	public void update() {
		if (audioPlayer.isPaused()) {
			buttons.get(0).setStringAlignment(new StringAlignment("Play", Color.BLACK));
		} else {
			buttons.get(0).setStringAlignment(new StringAlignment("Pause", Color.BLACK));
		}
	}
}
