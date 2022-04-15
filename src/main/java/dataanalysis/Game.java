package dataanalysis;

import audiofilereader.MusicData;
import dataanalysis.enums.DividerOrientation;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import lc.kra.system.mouse.GlobalMouseHook;
import lc.kra.system.mouse.event.GlobalMouseEvent;

public class Game implements Runnable {
	public Window window;
	public boolean running = true;
	public static int FPS = 120;
	public static int WIDTH = 1280;
	public static int HEIGHT = 720;
	public static boolean FULLSCREEN = false;
	
	public MusicData musicData;
	public WaveformDrawer waveformDrawer;
	public VolumeDrawer volumeDrawer;
	public Divider horizontalDivider;
	public Divider verticalDivider;
	public VolumeSlider volumeSlider;
	public Playbar playbar;
	
	private boolean unlimitedFPS = false;
	
	public AudioPlayer audioPlayer;
	
	public Game(MusicData musicData) {
		window = new Window(WIDTH, HEIGHT, "Waveform testi", this, 0, FULLSCREEN);
		this.musicData = musicData;
		
		int volumeDrawerHeight = 70;
		int playbarHeight = 50;
		int totalWaveFormHeight = HEIGHT - volumeDrawerHeight - playbarHeight;
		int volumeSliderWidth = 100;
		
		waveformDrawer = new WaveformDrawer(0, 0, WIDTH - volumeSliderWidth, totalWaveFormHeight, this);
		volumeDrawer = new VolumeDrawer(0, totalWaveFormHeight, WIDTH - volumeSliderWidth, volumeDrawerHeight, this);
		
		//Horizontal divider
		horizontalDivider = new Divider(totalWaveFormHeight, 5, 40, new PanelContainer(waveformDrawer), new PanelContainer(volumeDrawer), DividerOrientation.HORIZONTAL);
		
		audioPlayer = new AudioPlayer(musicData, waveformDrawer);
		
		
		volumeSlider = new VolumeSlider(WIDTH - volumeSliderWidth, 0, volumeSliderWidth, HEIGHT - playbarHeight, this);
		//Vertical divider
		verticalDivider = new Divider(horizontalDivider.getLength(), 5, volumeSliderWidth, new PanelContainer(horizontalDivider), new PanelContainer(volumeSlider), DividerOrientation.VERTICAL);
		verticalDivider.setMovable(false);
		
		playbar = new Playbar(0, HEIGHT - playbarHeight, WIDTH, playbarHeight, this);
	}
	
	public void stop() {
		running = false;
		System.exit(0);
	}
	
	private void init() {
		Input input = new Input(this);
		window.getCanvas().addMouseListener(input);
		window.getCanvas().addMouseMotionListener(input);
		window.getCanvas().addMouseWheelListener(input);
		window.getCanvas().addComponentListener(input);
		window.getCanvas().addKeyListener(input);
		
		//window.getFrame().addComponentListener(input);
		
		GlobalMouseHook mouseHook = new GlobalMouseHook();
		mouseHook.addMouseListener(input);
		
		audioPlayer.init();
		
		volumeSlider.setVolume(20);
		
		new Thread(audioPlayer).start();
	}
	
	@Override
	public void run() {
		init();
		window.getCanvas().requestFocus();
		
		long now = System.nanoTime();
		long nsBetweenFrames = (long) (1e9 / (unlimitedFPS ? 30000 : FPS));
		
		long time = System.currentTimeMillis();
		int frames = 0;
		
		while (running) {
			if (now + nsBetweenFrames <= System.nanoTime()) {
				now += nsBetweenFrames;
				update();
				render();
				frames++;
				
				if (time + 1000 < System.currentTimeMillis()) {
					time += 1000;
					window.setTitle("Waveform testi fps: " + frames);
					frames = 0;
				}
			}
		}
		window.close();
	}
	
	public void update() {
		audioPlayer.update();
		waveformDrawer.update();
		horizontalDivider.update();
		verticalDivider.update();
		
		playbar.update();
	}
	
	public void render() {
		Graphics2D g = window.getGraphics2D();
		
		waveformDrawer.render(g);
		volumeDrawer.render(g);
		horizontalDivider.render(g);
		volumeSlider.render(g);
		verticalDivider.render(g);
		playbar.render(g);
		
		//System.out.println("rendered");
		
		window.display(g);
	}
	
	public void mousePressed(MouseEvent e) {
		if (!horizontalDivider.mousePressed(e) && !verticalDivider.mousePressed(e)) {
			waveformDrawer.mousePressed(e);
			volumeSlider.mousePressed(e);
		}
		
		playbar.mousePressed(e);
	}
	
	public void mouseDragged(MouseEvent e) { //TODO: do middle mouse move sideways
		if (!horizontalDivider.mouseDragged(e) && !verticalDivider.mouseDragged(e) && !volumeSlider.mouseDragged(e)) {
			if (waveformDrawer.getDragging()) {
				waveformDrawer.mousePressed(e);
			}
		}
		
		mouseMoved(e);
	}
	
	public void mouseReleased(MouseEvent e) {
		if (!horizontalDivider.mouseReleased(e) && !verticalDivider.mouseReleased(e)) {
			waveformDrawer.mouseReleased(e);
		}
		waveformDrawer.setDragging(false);
		volumeSlider.setDragging(false);
	}
	
	public void mouseMoved(MouseEvent e) {
		waveformDrawer.resetHover();
		
		int cursor = Cursor.DEFAULT_CURSOR;
		if (horizontalDivider.hover(e)) {
			cursor = horizontalDivider.getCursorTypeForHover();
		} else if (verticalDivider.hover(e)) {
			cursor = verticalDivider.getCursorTypeForHover();
		} else {
			if (waveformDrawer.hover(e)) {
				cursor = Cursor.TEXT_CURSOR;
			}
		}
		if (volumeSlider.hover(e)) {
			cursor = Cursor.N_RESIZE_CURSOR;
		}
		if (playbar.hover(e)) {
			cursor = Cursor.HAND_CURSOR;
		}
		
		changeCursor(cursor);
	}
	
	//how to create fake mouseEvent from GlobalMouseEvent object: new MouseEvent(this, 0, System.currentTimeMillis(), 0, p.x, p.y, 1, false, event.getButton());
	public void mouseMoved(GlobalMouseEvent event) {
		Point p = pointRelativeToWindow(new Point(event.getX(), event.getY()));
		if (!waveformDrawer.isInside(p.x, p.y)) {
			waveformDrawer.resetHover();
		}
	}
	
	public void mouseExited(MouseEvent e) {
		waveformDrawer.resetHover();
	}
	
	public long getCurrentFrame() {
		return audioPlayer.getCurrentFrame();
	}
	
	public void updateCurrentLocationByFrame(long frame) {
		audioPlayer.updateCurrentLocationByFrame(frame);
	}
	
	public void changeCursor(int cursor) {
		changeCursor(new Cursor(cursor));
	}
	
	public void changeCursor(Cursor cursor) {
		window.setCursor(cursor);
	}
	
	public void MouseWheelMoved(MouseWheelEvent e) {
		if (e.isAltDown()) {
			boolean changed = waveformDrawer.zoom(-e.getWheelRotation());
			if (changed) {
				waveformDrawer.waveformChanged = true;
			}
		} else {
			volumeSlider.scroll(-e.getWheelRotation());
		}
	}
	
	public Point pointRelativeToWindow(Point p) {
		Rectangle r = window.getBounds();
		Insets inset = window.getInsets();
		return new Point(p.x - r.x - inset.left, p.y - r.y - inset.top);
	}
	
	public void windowResized(int w, int h) {
		WIDTH = w;
		HEIGHT = h;
		
		if (horizontalDivider.dir == DividerOrientation.HORIZONTAL) {
			horizontalDivider.setLength(w, false);
			horizontalDivider.setMaxSpace(h - playbar.getHeight(), false);
		} else {
			horizontalDivider.setLength(h - playbar.getHeight(), false);
			horizontalDivider.setMaxSpace(w, false);
		}
		
		if (verticalDivider.dir == DividerOrientation.HORIZONTAL) {
			verticalDivider.setLength(w, false);
			verticalDivider.setMaxSpace(h - playbar.getHeight(), false);
		} else {
			//System.out.println("window width set: " + w);
			verticalDivider.setLength(h - playbar.getHeight(), false);
			verticalDivider.setMaxSpace(w, false);
		}
		
		playbar.setY(h - playbar.getHeight());
		playbar.setWidth(w);
	}
	
	public void toggleFullscreen() {
		FULLSCREEN = !FULLSCREEN;
		window.setFullscreen(FULLSCREEN);
		
		
		/*GraphicsDevice monitor = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
		Rectangle bounds = monitor.getDefaultConfiguration().getBounds();*/
		
		/*Rectangle screen = */
		/*System.out.println(screen.x + " " + screen.y);
		window.setFullscreen(this, screen.x, screen.y, FULLSCREEN);*/
		//windowResized(bounds.width, bounds.height); //might not be necessary
		
		window.getCanvas().requestFocus();
	}
	
	public void setGainValue(float value) {
		if (audioPlayer == null || audioPlayer.getGain() == null) {
			return;
		}
		audioPlayer.getGain().setValue(value);
	}
	
	public int getVolume() {
		return volumeSlider.getCurrentVolumePercent();
	}
	
	public void togglePause() {
		audioPlayer.togglePause();
	}
	
	public void stopTheMusic() {
		audioPlayer.stopTheMusic();
	}
	
	public void backMilliSeconds(int ms) {
		audioPlayer.setCurrentHEADByMicros(Math.max(0, audioPlayer.getCurrentMicros() - ms * 1000));
	}
	
	public void forwardMilliSeconds(int ms) {
		audioPlayer.setCurrentHEADByMicros(Math.max(0, audioPlayer.getCurrentMicros() + ms * 1000));
	}
}
