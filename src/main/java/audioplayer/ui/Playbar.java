package audioplayer.ui;

import audioplayer.waveform.WaveformDrawer;
import audiofilereader.MusicData;
import audioplayer.MyCursor;
import audioplayer.audio.AudioPlayer;
import uilibrary.enums.Alignment;
import uilibrary.menu.Button;
import uilibrary.menu.StringAlignment;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import uilibrary.RenderText;
import static uilibrary.enums.Alignment.*;
import static uilibrary.enums.ReferenceType.OUTSIDE;

//Current time, play, stop etc buttons. maybe other information
public class Playbar {
	private int x, y, width, height;
	private List<Button> buttons = new ArrayList<>();
	private int xMargin = 2;
	private int yMargin = 2;
	
	private final AudioPlayer audioPlayer;
	private final int buttonHeight;
	private final int buttonWidth;
	
	/**
	 * Create playbar.
	 * <p>
	 * Add buttons with:
	 * <pre>
	 * {@code 
	 *	playbar.addButton("Pause", this::togglePause);
	 *	playbar.addButton("Stop", this::stopTheMusic);
	 *	playbar.addButton("<<", this::goToBeginning);
	 * }
	 * </pre>
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param audioPlayer 
	 */
	public Playbar(int x, int y, int width, int height, AudioPlayer audioPlayer) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.audioPlayer = audioPlayer;
		
		buttonHeight = this.height - yMargin * 2;
		buttonWidth = buttonHeight + 15;
		
		/*addButton("Pause", this::togglePause);
		addButton("Stop", o -> audioPlayer.stopTheMusic(buttonHeight));
		addButton("<<", function);
		
		Button b = new Button(x + xMargin, y + yMargin, buttonWidth, buttonHeight, Color.GRAY, pause);
		b.addStringAlignment(new StringAlignment("Pause", Color.BLACK));
		buttons.add(b);
		
		b = new Button(x + xMargin + buttonWidth + xMargin, y + yMargin, buttonWidth, buttonHeight, Color.GRAY, stop);
		b.addStringAlignment(new StringAlignment("Stop", Color.BLACK));
		buttons.add(b);
		
		b = new Button(x + xMargin + buttonWidth * 2 + xMargin * 2, y + yMargin, buttonWidth, buttonHeight, Color.GRAY, o -> { audioPlayer.setCurrentTimeByMicros(0); waveformDrawer.setPlayStartFrame(0); });
		b.addStringAlignment(new StringAlignment("<<", Color.BLACK));
		buttons.add(b);
		
		speedMultiplierSpace = new Rectangle(x + xMargin * 3 + buttonWidth * 3 + 10, y + yMargin, buttonWidth, buttonHeight);*/
	}
	
	public final void addButton(String text, Runnable function) {
		Button previous = buttons.isEmpty() ? null : buttons.get(buttons.size() - 1);
		
		Button b = new Button(buttonWidth, buttonHeight, Color.GRAY, function);
		if (previous == null) {
			b.arrange(this.x, this.y).setMargin(xMargin, yMargin).align(TOP, LEFT);
		} else {
			b.arrange().setReference(previous, OUTSIDE).setMargin(xMargin, 0).align(RIGHT);
		}
		
		b.addStringAlignment(new StringAlignment(text, Color.BLACK));
		buttons.add(b);
	}
	
	public void removeButton(int i) {
		
	}
	
	private int getButtonsWidth() {
		return (xMargin + buttonWidth) * buttons.size();
	}
	
	private Rectangle getSpeedMultiplierSpace() {
		return new Rectangle(getButtonsWidth() + 10, y + yMargin, buttonWidth, buttonHeight);
	}
	
	public void render(Graphics2D g, boolean canPlayAudio) {
		g.setColor(new Color(25, 25, 25));
		g.fillRect(x, y, width, height);
		
		for (Button button : buttons) {
			button.render(g);
		}
		
		g.setColor(Color.LIGHT_GRAY);
		
		Rectangle speedMultiplierSpace = getSpeedMultiplierSpace();
		Rectangle neededSpace = RenderText.drawStringWithAlignment(g, (audioPlayer.currentSamplerateMultiplierPercent / 100.0) + "x", speedMultiplierSpace, null, Alignment.LEFT);
		
		String timeDurationString = "";
		
		if (canPlayAudio) {
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
		
		if (!buttons.isEmpty()) {
			Button b = buttons.get(0);
			b.arrange(b.getX() + diffX, b.getY());
		}
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		int diffY = y - this.y;
		this.y = y;
		
		if (!buttons.isEmpty()) {
			Button b = buttons.get(0);
			b.arrange(b.getX(), b.getY() + diffY);
		}
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
			if (button.click(e.getX(), e.getY())) {
				return true;
			}
		}
		
		return false;
	}

	public boolean hover(MouseEvent e, MyCursor cursor) {
		boolean wasHover = false;
		for (Button button : buttons) {
			if (button.hover(e.getX(), e.getY())) {
				wasHover = true;
				cursor.type = Cursor.HAND_CURSOR;
				break;
			}
		}
		return wasHover;
	}
	
	public void overridePauseButton(Runnable pause) {
		if (buttons.size() >= 1) {
			buttons.get(0).setAction(pause);
		}
	}
	
	public void overrideStopButton(Runnable stop) {
		if (buttons.size() >= 2) {
			buttons.get(1).setAction(stop);
		}
	}
	
	public void overrideGoToStartButton(Runnable goToStart) {
		if (buttons.size() >= 3) {
			buttons.get(2).setAction(goToStart);
		}
	}
	
	public void update() {
		for (Button button : buttons) {
			String text = button.getMainText();
			if (text.equals("Play") || text.equals("Pause")) {
				if (audioPlayer.isPaused()) {
					button.setStringAlignment(new StringAlignment("Play", Color.BLACK));
				} else {
					button.setStringAlignment(new StringAlignment("Pause", Color.BLACK));
				}
				break;
			}
		}
	}
}
