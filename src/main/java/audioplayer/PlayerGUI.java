package audioplayer;

import audiofilereader.MusicData;
import static audioplayer.Constants.PLAYBAR_HEIGHT;
import audioplayer.audio.AudioPlayer;
import audioplayer.ui.Playbar;
import audioplayer.volume.VolumeController;
import audioplayer.waveform.WaveformDrawer;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.File;
import java.util.List;
import lc.kra.system.mouse.GlobalMouseHook;
import lc.kra.system.mouse.event.GlobalMouseEvent;
import uilibrary.DragAndDrop;
import uilibrary.GameLoop;
import uilibrary.Window;
import uilibrary.thread.ThreadExecutor;

public abstract class PlayerGUI<T extends WaveformDrawer> extends GameLoop {
	protected enum State {
		NORMAL, NO_AUDIO, LOADING; //no audio, means you can't use musicData, loading means the same, but it should render a loading box.
		
		public boolean canPlayAudio() {
			return this == NORMAL;
		}
	}
	
	protected Window window;
	
	protected T waveformDrawer;
	protected VolumeController volumeController;
	protected Playbar playbar;
	
	protected AudioPlayer audioPlayer;
	
	protected File updateAudioFile = null;
	protected State state = State.NORMAL;
	
	protected PlayerInput input;
	
	public PlayerGUI(int width, int height, boolean fullscreen, MusicData musicData) {
		this(width, height, 0, fullscreen, musicData);
	}
	
	public PlayerGUI(int width, int height, int spawnScreen, boolean fullscreen, MusicData musicData) {
		super(Constants.FPS);
		
		window = new Window(width, height, "", spawnScreen, fullscreen);
		window.setCanvasBackground(Color.LIGHT_GRAY); //so that waveformDrawer isn't completely black when state is LOADING.
		
		if (musicData == null) {
			state = State.NO_AUDIO;
		}
		
		audioPlayer = new AudioPlayer(musicData, !state.canPlayAudio());
		audioPlayer.setFinishedCallback(this::stopTheMusic);
		
		volumeController = new VolumeController(audioPlayer);
		volumeController.setVolume(50);
		
		playbar = new Playbar(0, window.getCanvasHeight() - PLAYBAR_HEIGHT, window.getCanvasWidth(), PLAYBAR_HEIGHT, audioPlayer);
		
		playbar.addButton("Pause", this::togglePause);
		playbar.addButton("Stop", this::stopTheMusic);
		playbar.addButton("<<", this::goToBeginning);
	}
	
	public void mousePressed(MouseEvent e) {}
	public void mouseDragged(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void keyTyped(KeyEvent e) {}
	public void keyPressed(KeyEvent e) {}
	public void mouseMoved(MouseEvent e) {}
	public void mouseMoved(GlobalMouseEvent event) {}
	public void mouseWheelMoved(MouseWheelEvent e) {}
	public void windowResized(int w, int h) {}
	
	protected abstract void render(Graphics2D g);
	
	@Override
	protected void init() {
		window.setTransferHandler(new DragAndDrop(this::openFile));
		
		if (input != null) {
			Canvas canvas = window.getCanvas();
			canvas.addMouseListener(input);
			canvas.addMouseMotionListener(input);
			canvas.addMouseWheelListener(input);
			canvas.addComponentListener(input);
			canvas.addKeyListener(input);

			GlobalMouseHook mouseHook = new GlobalMouseHook();
			mouseHook.addMouseListener(input);
		}
		
		if (Constants.FULLSCREEN) {
			window.setFullscreen(false);
			window.setFullscreen(true); //to resize elements if they werent loaded in time
		}
		
		new Thread(audioPlayer).start();
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
	protected void update() {
		if (state != State.LOADING && updateAudioFile != null) {
			updateAudioFile();
			return;
		}
		
		audioPlayer.update(); //TODO: see if this throws a nullptr sometimes
		if (state.canPlayAudio()) waveformDrawer.update();
		
		playbar.update();
	}
	
	@Override
	protected void render() {
		Graphics2D g = window.getGraphics2D();
		render(g);
		window.display(g);
	}
	
	@Override
	protected void shutdown() {
		window.close();
		super.shutdown();
	}
	
	public AudioPlayer getAudioPlayer() {
		return audioPlayer;
	}
	
	public int getVolume() {
		return volumeController.getCurrentVolumePercent();
	}
	
	public void togglePause() {
		audioPlayer.togglePause();
	}
	
	public void stopTheMusic() {
		audioPlayer.stopTheMusic(waveformDrawer.getPlayStartFrame());
	}
	
	public void goToBeginning() {
		audioPlayer.setCurrentTimeByMicros(0);
		waveformDrawer.setPlayStartFrame(0);
	}
	
	public boolean isFullscreen() {
		return window.isFullscreen();
	}
	
	protected void updateAudioFile() {
		if (updateAudioFile == null) return;
		state = State.LOADING; //has to put here so it doesn't go inside any of the methods right after this
		new ThreadExecutor(o -> updateAudioFileTask(updateAudioFile)).start();
	}
	
	protected void updateAudioFileTask(File file) {
		audioPlayer.stopTheMusic(0);
		
		waveformDrawer.reset();
		audioPlayer.getMusicData().clearData();
		
		System.gc();
		
		MusicData musicData = MusicData.createMusicData(file);
		audioPlayer.setMusicData(musicData);
		volumeController.reapplyVolume();
		
		audioPlayer.unpause();
		
		System.out.println("");
		updateAudioFile = null;
		
		window.focus();
		state = State.NORMAL;
	}
	
	public void toggleFullscreen() {
		window.setFullscreen(!isFullscreen());
	}
	
	public void changeCursor(int cursor) {
		changeCursor(new Cursor(cursor));
	}
	
	public void changeCursor(Cursor cursor) {
		window.setCursor(cursor);
	}
	
	public Point pointRelativeToWindow(Point p) {
		Rectangle r = window.getCanvasBounds();
		return new Point(p.x - r.x, p.y - r.y);
	}
}
