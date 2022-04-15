package dataanalysis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class WaveformDrawer implements Panel {
	private int x, y, width, height;
	private final Game game;
	private final int xOffset = 5;
	private int widthOffset = 10; //can change based on how big the waveform render was. Edit. doesnt change currently, it was buggy
	
	private Integer clickedFrame;
	private Integer lastMouseX;
	private boolean hovering = false;
	
	private BufferedImage waveformLeft;
	private BufferedImage waveformRight;
	
	private boolean dragging = false;
	public boolean waveformChanged = false;
	private boolean rendering = false;
	private boolean abortRender = false;
	private Camera cam;
	
	public WaveformDrawer(int x, int y, int width, int height, Game game) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.game = game;
		cam = new Camera();
		
		createWaveformImages();
	}
	
	public void update() {
		int frame = (int) game.audioPlayer.getCurrentFrame();
		
		while (!game.audioPlayer.isPaused() && getTValueByFrameInWaveArea(frame) > 0.9 && !lastSampleIsVisible()) {
			int newStart = getFrameByTValueInWaveArea(0.8);
			cam.setFirstSample(newStart, game.musicData);
			waveformChanged = true;
		}
		
		if (waveformChanged) {
			createWaveformImages();
		}
	}
	
	public void render(Graphics2D g) {
		int h = height / 2;
		
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(x, y, width, h);
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(x, y + h, width, h);
		
		g.drawImage(waveformLeft, getWaveformX(), y, null);
		g.drawImage(waveformRight, getWaveformX(), y + h, null);
		
		g.setColor(Color.BLACK);
		g.drawLine(x, y + h, width, y + h);
		
		
		renderPlayStartLine(g);
		renderPlaybackLine(g);
		renderHoverLine(g);
	}
	
	private void renderPlaybackLine(Graphics2D g) {
		if (game.audioPlayer.isActive()) {
			g.setColor(Color.red);
			g.setStroke(new BasicStroke(2f));
			
			renderLine(g, (int) game.audioPlayer.getCurrentFrame());
		}
	}
	
	private void renderPlayStartLine(Graphics2D g) {
		if (clickedFrame != null) {
			g.setColor(Color.BLACK);
			g.setStroke(new BasicStroke(1.5f));
			renderLine(g, clickedFrame);
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
		int sampleCount = cam.getSampleCount(game.musicData);
		double t = (frame - cam.getFirstSample()) / (double) sampleCount;
		return t;
	}
	
	private int getFrameByTValueInWaveArea(double t) {
		int frame = (int) (t * cam.getSampleCount(game.musicData) + cam.getFirstSample());
		return frame;
	}
	
	private boolean lastSampleIsVisible() {
		double t = getTValueByFrameInWaveArea(game.musicData.getFrameCount());
		return t <= 1;
	}
	
	public int getMaxValue() {
		return (int) Math.pow(2, game.musicData.bitsPerSample) / 2;
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
	
	public void mousePressed(MouseEvent e) {
		if (!dragging && !isInside(e.getX(), e.getY())) {
			return;
		}
		
		clickedFrame = (int) xCoordToFrame(e.getX() + 2);
		
		long frame = Math.min(game.musicData.getFrameCount(), Math.max(0, clickedFrame));
		game.updateCurrentLocationByFrame(frame);
		
		dragging = true; //this is after game.updateCurrentLocationByFrame (which will pause the audioPlayer, and try to continue from new location) because first time after mouse down we want it to flush the buffer, and second time not while dragging.
	}
	
	public void mouseReleased(MouseEvent e) {
		
	}
	
	public int frameToXCoord(int frame) {
		int sampleCount = cam.getSampleCount(game.musicData);
		double t = (frame - cam.getFirstSample()) / (double) sampleCount;
		int xx = getWaveformX() + (int) (t * getWaveformAreaWidth());
		
		return Math.max(getWaveformX(), Math.min(getWaveformX() + getWaveformAreaWidth(), xx));
	}
	
	public int xCoordToFrame(int x) {
		double t = (x - getWaveformX()) / (double) getWaveformAreaWidth();
		int frame = getFrameByTValueInWaveArea(t);
		
		return (int) Math.max(0, Math.min(game.musicData.getFrameCount(), frame));
	}
	
	public void createWaveformImages() {
		rendering = true;
		waveformChanged = false;
		int h = height / 2;
		waveformLeft = new BufferedImage(getWaveformAreaWidth(), h, BufferedImage.TYPE_INT_ARGB);
		waveformRight = new BufferedImage(getWaveformAreaWidth(), h, BufferedImage.TYPE_INT_ARGB);
		
		renderWaveform(waveformLeft.createGraphics(), game.musicData.getSamplesChannel(true, cam.getFirstSample(), cam.getSampleCount(game.musicData)), h);
		renderWaveform(waveformRight.createGraphics(), game.musicData.getSamplesChannel(false, cam.getFirstSample(), cam.getSampleCount(game.musicData)), h);
		
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
		int maxValue = getMaxValue();
		
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
	
	@Override
	public int getWidth() {
		return width;
	}
	
	@Override
	public int getHeight() {
		return height;
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
	
	public boolean getDragging() {
		return dragging;
	}
	
	public void setDragging(boolean dragging) {
		this.dragging = dragging;
	}
	
	public int getClickedFrame() {
		return clickedFrame;
	}
	
	public boolean zoom(int amount) {
		double zoom = cam.getZoom();
		int hoverFrame = xCoordToFrame(lastMouseX);
		//lastMouseX is the location where hoverFrame should be after zoom
		
		double t = (lastMouseX - getWaveformX()) / (double) getWaveformAreaWidth();
		
		cam.zoom(amount);
		
		int sampleOffset = (int) (cam.getSampleCount(game.musicData) * t);
		int startFrame = hoverFrame - sampleOffset;
		cam.setFirstSample(startFrame, game.musicData);
		
		System.out.println("ZOOM! " + cam.getZoom());
		
		return zoom != cam.getZoom(); //zoom has changed
	}
}
