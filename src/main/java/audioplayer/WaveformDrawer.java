package audioplayer;

import audiofilereader.MusicData;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import uilibrary.Panel;

//TODO: try to optimize the waveform with longer audios more. (with over 1h wav files it's pretty laggy)
//TODO: Also for subtitle editor, create waveform where it's easier to see the parts where they talk. Mute the others etc for the render.
public class WaveformDrawer extends Panel {
	protected final AudioPlayer audioPlayer;
	private final int xOffset = 5;
	private int widthOffset = 10; //can change based on how big the waveform render was. Edit. doesnt change currently, it was buggy
	
	private Integer playStartFrame;
	private Integer lastMouseX;
	private boolean hovering = false;
	
	private BufferedImage waveformLeft;
	private BufferedImage waveformRight;
	
	private boolean waveformChanged = false;
	private boolean rendering = false;
	private boolean abortRender = false;
	private Camera cam;
	private long playbackLineUpdateTime;
	private long lastPlaybackLineFrame;
	
	private boolean mono = false;
	
	public WaveformDrawer(int x, int y, int width, int height, AudioPlayer audioPlayer) {
		super(x, y, width, height);
		
		this.audioPlayer = audioPlayer;
		cam = new Camera();
		
		createWaveformImages();
	}
	
	public void reset() {
		playStartFrame = null;
		lastMouseX = null;
		waveformChanged = true;
		resetZoom();
	}
	
	public void setMono(boolean b) {
		mono = b;
		waveformChanged = true;
	}
	
	public void setWaveformChanged(boolean b) {
		this.waveformChanged = b;
	}
	
	@Override
	public void update() {
		int frame = (int) audioPlayer.getCurrentFrame();
		
		while (!audioPlayer.isPaused() && getTValueByFrameInWaveArea(frame) > 0.9 && !lastSampleIsVisible()) {
			int newStart = getFrameByTValueInWaveArea(0.8);
			cam.setFirstSample(newStart, audioPlayer.getMusicData());
			waveformChanged = true;
		}
		
		if (waveformChanged) {
			createWaveformImages();
		}
	}
	
	@Override
	public void render(Graphics2D g) {
		renderWaveform(g);
		renderPlaybackLines(g);
	}
	
	protected void renderWaveform(Graphics2D g) {
		int h = height / 2;
		
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(x, y, width, h);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(x, y + h, width, h);
		
		g.drawImage(waveformLeft, getWaveformX(), y, null);
		if (!mono) {
			g.drawImage(waveformRight, getWaveformX(), y + h, null);
		}
		
		g.setColor(Color.BLACK);
		g.drawLine(x, y + h, width, y + h);
		
	}
	
	protected void renderPlaybackLines(Graphics2D g) {
		renderPlaybackLines(g, true);
	}
	
	protected void renderPlaybackLines(Graphics2D g, boolean renderHoverLine) {
		renderPlayStartLine(g);
		renderPlaybackLine(g);
		if (renderHoverLine) {
			renderHoverLine(g);
		}
	}
	
	private void renderPlaybackLine(Graphics2D g) {
		if (audioPlayer.isActive()) {
			g.setColor(Color.red);
			g.setStroke(new BasicStroke(2f));
			
			renderLine(g, (int) audioPlayer.getCurrentFrame());
		}
	}
	
	private void renderPlayStartLine(Graphics2D g) {
		if (playStartFrame != null) {
			g.setColor(Color.BLACK);
			g.setStroke(new BasicStroke(1.5f));
			renderLine(g, playStartFrame);
		}
	}
	
	private void renderHoverLine(Graphics2D g) {
		if (lastMouseX != null && hovering) {
			g.setColor(Color.DARK_GRAY);
			g.drawLine(lastMouseX, y, lastMouseX, y + height);
		}
	}
	
	private void renderLine(Graphics2D g, int frame) {
		double t = getTValueByFrameInWaveArea(frame);
		
		if (t < 0 || t > 1) {
			return;
		}
		
		int xx = getWaveformX() + (int) (t * getWaveformAreaWidth());
		g.drawLine(xx, y, xx, y + height);
	}
	
	private double getTValueByFrameInWaveArea(int frame) {
		int sampleCount = cam.getSampleCount(audioPlayer.getMusicData());
		double t = (frame - cam.getFirstSample()) / (double) sampleCount;
		return t;
	}
	
	private int getFrameByTValueInWaveArea(double t) {
		int frame = (int) (t * cam.getSampleCount(audioPlayer.getMusicData()) + cam.getFirstSample());
		return frame;
	}
	
	private boolean lastSampleIsVisible() {
		double t = getTValueByFrameInWaveArea(audioPlayer.getMusicData().getFrameCount());
		return t <= 1;
	}
	
	public int getSamplesPerPixel(int sampleCount) {
		return (int) Math.ceil(sampleCount / (double) getWaveformAreaWidth());
	}
	
	public double getSamplesPerPixelDouble(int sampleCount) {
		return sampleCount / (double) getWaveformAreaWidth();
	}
	
	public static double normalize(int sampleVal, int maxVal) {
		return sampleVal / (double) maxVal;
	}
	
	public boolean isInside(int x, int y) {
		return x >= this.x && x <= this.x + width && y >= this.y && y <= this.y + height;
	}
	
	private void setPlayStartFrameWithCoords(int x, int y) {
		if (!isInside(x, y)) {
			return;
		}
		
		playStartFrame = (int) xCoordToFrame(x + 2);
	}
	
	public void setPlayStartFrame(int frame) {
		playStartFrame = frame;
	}
	
	public void mousePressed(MouseEvent e) {
		if (!isInside(e.getX(), e.getY())) {
			return;
		}
		
		setPlayStartFrameWithCoords(e.getX(), e.getY());
		
		int frame = HelperFunctions.clamp(playStartFrame, 0, audioPlayer.getMusicData().getFrameCount());
		audioPlayer.updateCurrentLocationByFrame(frame, true);
	}
	
	public void mouseDragged(MouseEvent e) {
		if (!isInside(e.getX(), e.getY())) {
			return;
		}
		
		setPlayStartFrameWithCoords(e.getX(), e.getY());
		
		int frame = HelperFunctions.clamp(playStartFrame, 0, audioPlayer.getMusicData().getFrameCount());
		audioPlayer.updateCurrentLocationByFrame(frame, false);
	}
	
	public int frameToXCoord(int frame) {
		int sampleCount = cam.getSampleCount(audioPlayer.getMusicData());
		double t = (frame - cam.getFirstSample()) / (double) sampleCount;
		int xx = getWaveformX() + (int) (t * getWaveformAreaWidth());
		
		return HelperFunctions.clamp(xx, getWaveformX(), getWaveformX() + getWaveformAreaWidth());
	}
	
	public int xCoordToFrame(int x) {
		double t = (x - getWaveformX()) / (double) getWaveformAreaWidth();
		int frame = getFrameByTValueInWaveArea(t);
		
		return HelperFunctions.clamp(frame, 0, audioPlayer.getMusicData().getFrameCount());
	}
	
	public void createWaveformImages() {
		rendering = true;
		waveformChanged = false;
		int h = height / (mono ? 1 : 2);
		MusicData musicData = audioPlayer.getMusicData();
		waveformLeft = new BufferedImage(getWaveformAreaWidth(), h, BufferedImage.TYPE_INT_ARGB);
		//TODO: make renderWaveform render it without using so big buffer, do with small buffers or streams, or just use musicData samples with indexes.
		renderWaveform(waveformLeft.createGraphics(), musicData.getSamplesByChannel(true, cam.getFirstSample(), cam.getSampleCount(musicData)), h);
		
		if (!mono) {
			waveformRight = new BufferedImage(getWaveformAreaWidth(), h, BufferedImage.TYPE_INT_ARGB);
			renderWaveform(waveformRight.createGraphics(), musicData.getSamplesByChannel(false, cam.getFirstSample(), cam.getSampleCount(musicData)), h);
		}
		
		if (abortRender) {
			waveformChanged = true;
		}
		rendering = false;
		abortRender = false;
	}
	
	public void abortRender() {
		if (rendering) {
			abortRender = true;
		}
	}
	
	private void renderWaveform(Graphics2D g, short[] samples, int height) {
		int sampleCount = samples.length;
		int samplesPerPixel = getSamplesPerPixel(sampleCount);
		int maxValue = audioPlayer.getMusicData().getMaxValue();
		
		g.setColor(Color.BLUE);
		g.setStroke(new BasicStroke(1f));
		
		
		//draws all the lines (one loop is one line)
		int xx = 0;
		for (int i = 0; (int) i < sampleCount; i += samplesPerPixel, xx++) {
			if (abortRender) {
				return;
			}
			
			int highest = Integer.MIN_VALUE;
			int lowest = Integer.MAX_VALUE;
			
			//finds min and max values inside this line (pixel)
			for (int j = 0; j < samplesPerPixel; j++) {
				if (i + j >= sampleCount) {
					break;
				}
				short val = samples[i + j];
				
				if (val > highest) {
					highest = val;
				}
				if (val < lowest) {
					lowest = val;
				}
			}
			
			double yMultiplierH = normalize(highest, maxValue);
			double yMultiplierL = normalize(lowest, maxValue); //will be negative most likely
			int halfHeight = height / 2;
			
			int hOffset = (int) (yMultiplierH * halfHeight);
			int lOffset = (int) (yMultiplierL * halfHeight);
			
			g.drawLine(xx, y + halfHeight - hOffset, xx, y + halfHeight - lOffset);
		}
		
		//widthOffset = width - (xx - xOffset); //TODO: this was causing problems, see why it was there
	}
	
	public int getWaveformX() {
		return x + xOffset;
	}
	
	public int getWaveformAreaWidth() { //render area width in pixels
		return width - widthOffset;
	}
	
	@Override
	public void setWidth(int width) {
		this.width = Math.max(widthOffset * 2, width);
		waveformChanged = true;
	}
	
	@Override
	public void setHeight(int height) {
		this.height = height;
		waveformChanged = true;
	}

	public boolean hover(MouseEvent e) {
		if (isInside(e.getX(), e.getY())) {
			lastMouseX = e.getX() + 2; //+ 2 to correct for cursor offset
			hovering = true;
			return true;
		} else {
			hovering = false;
			return false;
		}
	}
	
	public void resetHover() {
		hovering = false;
	}
	
	public int getPlayStartFrame() {
		if (playStartFrame == null) {
			return 0;
		}
		return playStartFrame;
	}
	
	public boolean zoom(int amountScrolled) {
		double zoom = cam.getZoom();
		int hoverFrame = xCoordToFrame(lastMouseX);
		//lastMouseX is the location where hoverFrame should be after zoom
		
		double t = (lastMouseX - getWaveformX()) / (double) getWaveformAreaWidth();
		
		cam.zoom(amountScrolled);
		
		int sampleOffset = (int) (cam.getSampleCount(audioPlayer.getMusicData()) * t);
		int startFrame = hoverFrame - sampleOffset;
		cam.setFirstSample(startFrame, audioPlayer.getMusicData());
		
		//System.out.println("ZOOM! " + cam.getZoom());
		
		return zoom != cam.getZoom(); //zoom has changed
	}
	
	public void resetZoom() {
		cam.setZoom(1);
		cam.setFirstSample(0, audioPlayer.getMusicData());
	}
}
