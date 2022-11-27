package audioplayer;

import audioplayer.waveform.WaveformDrawer;
import audioplayer.volume.VolumeDrawer;
import audioplayer.volume.VolumeSlider;
import audiofilereader.MusicData;
import static audioplayer.Constants.PLAYBAR_HEIGHT;
import audioplayer.ui.LoadingBox;
import uilibrary.enums.DividerOrientation;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import lc.kra.system.mouse.event.GlobalMouseEvent;
import uilibrary.Divider;

public class AudioPlayerGUI extends PlayerGUI { //TODO: add music playlist, and visual playtime in bottom windows bar like in potplayer
	private VolumeDrawer volumeDrawer;
	private Divider horizontalDivider;
	private Divider verticalDivider;
	private VolumeSlider volumeSlider;
	
	private static final String TITLE = "Audio Player";
	
	public AudioPlayerGUI() {
		this(null);
	}
	
	public AudioPlayerGUI(MusicData musicData) {
		super(Constants.WIDTH, Constants.HEIGHT, Constants.FULLSCREEN, musicData);
		
		int volumeDrawerHeight = 70;
		int totalWaveFormHeight = window.getCanvasHeight() - volumeDrawerHeight - PLAYBAR_HEIGHT;
		int volumeSliderWidth = 100;
		
		waveformDrawer = new WaveformDrawer(0, 0, window.getCanvasWidth() - volumeSliderWidth, totalWaveFormHeight, audioPlayer);
		volumeDrawer = new VolumeDrawer(0, 0, window.getCanvasWidth() - volumeSliderWidth, volumeDrawerHeight, this);
		
		horizontalDivider = new Divider(totalWaveFormHeight, 5, 40, waveformDrawer, volumeDrawer, DividerOrientation.HORIZONTAL);
		
		volumeSlider = new VolumeSlider(0, 0, volumeSliderWidth, window.getCanvasHeight() - PLAYBAR_HEIGHT, volumeController);
		verticalDivider = new Divider(horizontalDivider.getLength(), 5, volumeSliderWidth, horizontalDivider, volumeSlider, DividerOrientation.VERTICAL);
		verticalDivider.setMovable(false);
		
		volumeController.setVolume(20);
		input = new PlayerInput(this);
	}
	
	@Override
	protected void lazyUpdate(int fps) {
		String fileName = audioPlayer.getMusicData().filename;
		window.setTitle(TITLE + " " + fileName + " fps: " + fps);
	}
	
	@Override
	public void update() {
		super.update();
		
		horizontalDivider.update();
		verticalDivider.update();
	}
	
	@Override
	public void render(Graphics2D g) {
		if (state.canPlayAudio()) waveformDrawer.render(g);
		
		volumeDrawer.render(g);
		horizontalDivider.render(g);
		volumeSlider.render(g);
		verticalDivider.render(g);
		playbar.render(g, state.canPlayAudio());
		
		if (state == State.LOADING) LoadingBox.renderLoadingBox(g, waveformDrawer.getBounds(), MusicData.convertProgress);
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (!horizontalDivider.mousePressed(e) && !verticalDivider.mousePressed(e) && !playbar.mousePressed(e)) {
			waveformDrawer.mousePressed(e);
			volumeSlider.mousePressed(e);
		}
	}
	
	@Override
	public void mouseDragged(MouseEvent e) { //TODO: do right mouse move sideways
		if (!horizontalDivider.mouseDragged(e) && !verticalDivider.mouseDragged(e) && !volumeSlider.mouseDragged(e)) {
			waveformDrawer.mouseDragged(e);
		}
		
		mouseMoved(e);
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		if (!horizontalDivider.mouseReleased(e) && !verticalDivider.mouseReleased(e)) {
			//here if dont want to execute when one of the above returned true
		}
		volumeSlider.setDragging(false);
	}
	
	@Override
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
	@Override
	public void mouseMoved(GlobalMouseEvent event) {
		Point p = pointRelativeToWindow(new Point(event.getX(), event.getY()));
		if (!waveformDrawer.isInside(p.x, p.y)) {
			waveformDrawer.resetHover();
		}
	}
	
	public void updateCurrentLocationByFrame(int frame) {
		audioPlayer.updateCurrentLocationByFrame(frame);
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (e.isAltDown()) {
			waveformDrawer.zoom(-e.getWheelRotation());
		} else {
			volumeController.scroll(-e.getWheelRotation());
		}
	}
	
	@Override
	public void windowResized(int w, int h) {
		super.windowResized(w, h);
		
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
}
