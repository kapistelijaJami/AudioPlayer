package audioplayer;

import audioplayer.audio.AudioPlayer;
import audioplayer.ui.Playbar;
import audioplayer.waveform.WaveformDrawer;
import audioplayer.volume.VolumeDrawer;
import audioplayer.volume.VolumeSlider;
import audiofilereader.MusicData;
import audioplayer.ui.LoadingBox;
import java.awt.Canvas;
import java.awt.Color;
import uilibrary.enums.DividerOrientation;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics2D;
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
import uilibrary.GameLoop;
import uilibrary.RenderMultilineText;
import uilibrary.Window;
import uilibrary.thread.ThreadExecutor;

public class AudioPlayerGUI extends GameLoop { //TODO: add music playlist, and visual playtime in bottom windows bar like in potplayer
	private enum State {
		NORMAL, NO_AUDIO, LOADING; //no audio, means you can't use musicData, loading means the same, but it should render a loading box.
		
		public boolean canPlayAudio() {
			return this == NORMAL;
		}
	}
	
	private Window window;
	
	private WaveformDrawer waveformDrawer;
	private VolumeDrawer volumeDrawer;
	private Divider horizontalDivider;
	private Divider verticalDivider;
	private VolumeSlider volumeSlider;
	private Playbar playbar;
	
	private AudioPlayer audioPlayer;
	
	private File updateAudioFile = null;
	private State state = State.NORMAL;
	
	private static final String TITLE = "Audio Player";
	
	public AudioPlayerGUI() {
		this(null);
	}
	
	public AudioPlayerGUI(MusicData musicData) {
		super(Constants.FPS);
		
		window = new Window(Constants.WIDTH, Constants.HEIGHT, "Audio Player", 0, Constants.FULLSCREEN);
		window.setCanvasBackground(Color.LIGHT_GRAY); //so that waveformDrawer isn't completely black when state is LOADING.
		
		if (musicData == null) {
			state = State.NO_AUDIO;
		}
		
		int volumeDrawerHeight = 70;
		int playbarHeight = 50;
		int totalWaveFormHeight = window.getCanvasHeight() - volumeDrawerHeight - playbarHeight;
		int volumeSliderWidth = 100;
		
		
		audioPlayer = new AudioPlayer(musicData, !state.canPlayAudio());
		waveformDrawer = new WaveformDrawer(0, 0, window.getCanvasWidth() - volumeSliderWidth, totalWaveFormHeight, audioPlayer);
		volumeDrawer = new VolumeDrawer(0, 0, window.getCanvasWidth() - volumeSliderWidth, volumeDrawerHeight, this);
		
		horizontalDivider = new Divider(totalWaveFormHeight, 5, 40, waveformDrawer, volumeDrawer, DividerOrientation.HORIZONTAL);
		
		
		volumeSlider = new VolumeSlider(0, 0, volumeSliderWidth, window.getCanvasHeight() - playbarHeight, audioPlayer);
		verticalDivider = new Divider(horizontalDivider.getLength(), 5, volumeSliderWidth, horizontalDivider, volumeSlider, DividerOrientation.VERTICAL);
		verticalDivider.setMovable(false);
		
		playbar = new Playbar(0, window.getCanvasHeight() - playbarHeight, window.getCanvasWidth(), playbarHeight, audioPlayer, waveformDrawer, o -> this.togglePause(), o -> this.stopTheMusic());
	}
	
	public boolean openFile(List<File> fileList) {
		File file = fileList.get(0);
		if (!file.exists() || !file.isFile()) return false; //no folders
		
		System.out.println();
		
		System.out.println("Trying to load a file: " + file.getName());
		System.out.println();

		updateAudioFile = file;
		
		window.focus();
		
		return true;
	}
	
	@Override
	protected void init() {
		Input input = new Input(this);
		Canvas canvas = window.getCanvas();
		canvas.addMouseListener(input);
		canvas.addMouseMotionListener(input);
		canvas.addMouseWheelListener(input);
		canvas.addComponentListener(input);
		canvas.addKeyListener(input);
		
		
		window.setTransferHandler(new DragAndDrop(this::openFile));
		
		GlobalMouseHook mouseHook = new GlobalMouseHook();
		mouseHook.addMouseListener(input);
		
		volumeSlider.setVolume(20);
		
		new Thread(audioPlayer).start();
		
		if (Constants.FULLSCREEN) {
			window.setFullscreen(false);
			window.setFullscreen(true); //to resize elements if they werent loaded in time
		}
	}
	
	@Override
	protected void lazyUpdate(int fps) {
		String fileName = audioPlayer.getMusicData().filename;
		window.setTitle(TITLE + " " + fileName + " fps: " + fps);
	}
	
	@Override
	protected void shutdown() {
		window.close();
		super.shutdown();
	}
	
	@Override
	public void update() {
		if (state != State.LOADING && updateAudioFile != null) {
			updateAudioFile();
			return;
		}
		
		audioPlayer.update(); //TODO: see why this throws nullptr without if
		if (state.canPlayAudio()) waveformDrawer.update();
		horizontalDivider.update();
		verticalDivider.update();
		
		playbar.update();
	}
	
	@Override
	public void render() {
		Graphics2D g = window.getGraphics2D();
		
		if (state.canPlayAudio()) waveformDrawer.render(g);
		volumeDrawer.render(g);
		horizontalDivider.render(g);
		volumeSlider.render(g);
		verticalDivider.render(g);
		playbar.render(g, state.canPlayAudio());
		
		if (state == State.LOADING) LoadingBox.renderLoadingBox(g, waveformDrawer.getBounds(), MusicData.CONVERT_PROGRESS);
		
		window.display(g);
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
	}
	
	public void setGainValue(float value) {
		if (audioPlayer == null || audioPlayer.getGain() == null) {
			return;
		}
		audioPlayer.getGain().setValue(value);
	}
	
	public AudioPlayer getAudioPlayer() {
		return audioPlayer;
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
		state = State.LOADING; //has to put here so it doesn't go inside any of the methods right after this
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
		volumeSlider.reapplyVolume();
		
		audioPlayer.unpause();
		
		System.out.println("");
		updateAudioFile = null;
		
		window.focus();
		state = State.NORMAL;
	}
}
