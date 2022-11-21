package audioplayer;

import audiofilereader.MusicData;
import java.awt.Canvas;
import java.awt.Color;
import uilibrary.enums.DividerOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.List;
import lc.kra.system.mouse.GlobalMouseHook;
import lc.kra.system.mouse.event.GlobalMouseEvent;
import uilibrary.Divider;
import uilibrary.DragAndDrop;
import uilibrary.RenderMultilineText;
import uilibrary.Window;
import uilibrary.thread.ThreadExecutor;

public class AudioPlayerGUI implements Runnable {
	public Window window;
	public boolean running = true;
	public static int FPS = 120;
	public static final int WIDTH = 1280;
	public static final int HEIGHT = 720;
	public static final boolean FULLSCREEN = false;
	
	public WaveformDrawer waveformDrawer;
	public VolumeDrawer volumeDrawer;
	public Divider horizontalDivider;
	public Divider verticalDivider;
	public VolumeSlider volumeSlider;
	public Playbar playbar;
	
	private boolean unlimitedFPS = true;
	
	public AudioPlayer audioPlayer;
	
	private File updateAudioFile = null;
	private boolean LOADING = false;
	
	public AudioPlayerGUI(MusicData musicData) {
		window = new Window(WIDTH, HEIGHT, "Audio Player", 0, FULLSCREEN);
		
		int volumeDrawerHeight = 70;
		int playbarHeight = 50;
		int totalWaveFormHeight = HEIGHT - volumeDrawerHeight - playbarHeight;
		int volumeSliderWidth = 100;
		
		audioPlayer = new AudioPlayer(musicData);
		waveformDrawer = new WaveformDrawer(0, 0, WIDTH - volumeSliderWidth, totalWaveFormHeight, audioPlayer);
		volumeDrawer = new VolumeDrawer(0, 0, WIDTH - volumeSliderWidth, volumeDrawerHeight, this);
		
		horizontalDivider = new Divider(totalWaveFormHeight, 5, 40, waveformDrawer, volumeDrawer, DividerOrientation.HORIZONTAL);
		
		
		volumeSlider = new VolumeSlider(0, 0, volumeSliderWidth, HEIGHT - playbarHeight, audioPlayer);
		verticalDivider = new Divider(horizontalDivider.getLength(), 5, volumeSliderWidth, horizontalDivider, volumeSlider, DividerOrientation.VERTICAL);
		verticalDivider.setMovable(false);
		
		playbar = new Playbar(0, HEIGHT - playbarHeight, WIDTH, playbarHeight, audioPlayer, waveformDrawer, o -> this.togglePause(), o -> this.stopTheMusic());
	}
	
	public boolean openFile(List<File> fileList) {
		File file = fileList.get(0);
		if (!file.exists() || !file.isFile()) return false; //no folders
		
		System.out.println();
		
		System.out.println("Trying to load a file: " + file.getName());
		System.out.println();

		updateAudioFile = file;
		
		return true;
	}
	
	public void stop() {
		running = false;
		System.exit(0);
	}
	
	private void init() {
		Input input = new Input(this);
		Canvas canvas = window.getCanvas();
		canvas.addMouseListener(input);
		canvas.addMouseMotionListener(input);
		canvas.addMouseWheelListener(input);
		canvas.addComponentListener(input);
		canvas.addKeyListener(input);
		
		//window.getFrame().addComponentListener(input);
		
		window.setTransferHandler(new DragAndDrop(this::openFile));
		
		GlobalMouseHook mouseHook = new GlobalMouseHook();
		mouseHook.addMouseListener(input);
		
		//audioPlayer.init();
		
		volumeSlider.setVolume(20);
		
		new Thread(audioPlayer).start();
		
		if (FULLSCREEN) {
			window.setFullscreen(false);
			window.setFullscreen(true); //to resize elements if they werent loaded in time
		}
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
					window.setTitle("Audio Player fps: " + frames);
					frames = 0;
				}
			}
		}
		window.close();
	}
	
	public void update() {
		if (LOADING) {
			return;
		}
		if (updateAudioFile != null) {
			updateAudioFile();
			return;
		}
		
		audioPlayer.update();
		waveformDrawer.update();
		horizontalDivider.update();
		verticalDivider.update();
		
		playbar.update();
	}
	
	public void render() {
		Graphics2D g = window.getGraphics2D();
		
		if (!LOADING) waveformDrawer.render(g);
		volumeDrawer.render(g);
		horizontalDivider.render(g);
		volumeSlider.render(g);
		verticalDivider.render(g);
		playbar.render(g, LOADING);
		
		if (LOADING) renderLoadingBox(g);
		
		window.display(g);
	}
	
	private void renderLoadingBox(Graphics2D g) {
		Rectangle bounds = waveformDrawer.getBounds();
		Dimension size = new Dimension(250, 150);
		int x = bounds.x + (bounds.width - size.width) / 2;
		int y = bounds.y + (bounds.height - size.height) / 2;
		
		g.setColor(Color.DARK_GRAY);
		g.fillRect(x - 3, y - 3, size.width + 6, size.height + 6);
		
		g.setColor(Color.GRAY);
		g.fillRect(x, y, size.width, size.height);
		
		g.setColor(Color.BLACK);
		RenderMultilineText.drawMultilineText(g, "LOADING...", null, new Rectangle(x, y, size.width, size.height), false);
	}
	
	public void mousePressed(MouseEvent e) {
		if (!horizontalDivider.mousePressed(e) && !verticalDivider.mousePressed(e) && !playbar.mousePressed(e)) {
			waveformDrawer.mousePressed(e);
			volumeSlider.mousePressed(e);
		}
	}
	
	public void mouseDragged(MouseEvent e) { //TODO: do middle mouse move sideways
		if (!horizontalDivider.mouseDragged(e) && !verticalDivider.mouseDragged(e) && !volumeSlider.mouseDragged(e)) {
			waveformDrawer.mouseDragged(e);
		}
		
		mouseMoved(e);
	}
	
	public void mouseReleased(MouseEvent e) {
		if (!horizontalDivider.mouseReleased(e) && !verticalDivider.mouseReleased(e)) {
			//here if dont want to execute when one of the above returned true
		}
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
	
	public void updateCurrentLocationByFrame(int frame) {
		audioPlayer.updateCurrentLocationByFrame(frame);
	}
	
	public void changeCursor(int cursor) {
		changeCursor(new Cursor(cursor));
	}
	
	public void changeCursor(Cursor cursor) {
		window.setCursor(cursor);
	}
	
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.isAltDown()) {
			boolean changed = waveformDrawer.zoom(-e.getWheelRotation());
			if (changed) {
				waveformDrawer.setWaveformChanged(true);
			}
		} else {
			volumeSlider.scroll(-e.getWheelRotation());
		}
	}
	
	public Point pointRelativeToWindow(Point p) {
		Rectangle r = window.getCanvasBounds();
		return new Point(p.x - r.x, p.y - r.y);
	}
	
	public void windowResized(int w, int h) {
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
		window.setFullscreen(!isFullscreen());
		
		
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
		audioPlayer.stopTheMusic(waveformDrawer.getPlayStartFrame());
	}

	public boolean isFullscreen() {
		return window.isFullscreen();
	}

	private void updateAudioFile() {
		if (updateAudioFile == null) return;
		LOADING = true; //has to put here so it doesn't go inside any of the methods right after this
		new ThreadExecutor(o -> updateAudioFileTask(updateAudioFile)).start();
	}
	
	private void updateAudioFileTask(File file) {
		audioPlayer.stopTheMusic(0);
		
		waveformDrawer.reset();
		audioPlayer.getMusicData().clearData();
		
		System.gc();
		
		MusicData musicData = MusicData.createMusicData(file);
		audioPlayer.setMusicData(musicData);
		playbar.update();
		
		audioPlayer.unpause();
		
		System.out.println("");
		updateAudioFile = null;
		LOADING = false;
	}
}
