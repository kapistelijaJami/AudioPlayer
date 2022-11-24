package audioplayer;

import audioplayer.audio.AudioPlayer;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.event.MouseInputListener;
import lc.kra.system.mouse.event.GlobalMouseEvent;
import lc.kra.system.mouse.event.GlobalMouseListener;

public class PlayerInput implements MouseInputListener, MouseWheelListener, KeyListener, GlobalMouseListener, ComponentListener {
	protected PlayerGUI gui;
	
	public PlayerInput(PlayerGUI game) {
		this.gui = game;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		gui.mousePressed(e);
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		gui.mouseReleased(e);
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		gui.mouseDragged(e);
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		gui.mouseMoved(e);
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		gui.mouseWheelMoved(e);
	}
	
	@Override
	public void mousePressed(GlobalMouseEvent event) {
		
	}
	
	@Override
	public void mouseReleased(GlobalMouseEvent event) {
		
	}
	
	@Override
	public void mouseMoved(GlobalMouseEvent event) {
		gui.mouseMoved(event);
	}
	
	@Override
	public void mouseWheel(GlobalMouseEvent event) {
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		int w = e.getComponent().getWidth();
		int h = e.getComponent().getHeight();
		gui.windowResized(w, h);
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		
	}

	@Override
	public void keyPressed(KeyEvent e) {
		AudioPlayer audioPlayer = gui.getAudioPlayer();
		
		int key = e.getKeyCode();
		switch (key) {
			case KeyEvent.VK_F11:
				gui.toggleFullscreen();
				break;
			case KeyEvent.VK_ESCAPE:
				if (gui.isFullscreen()) {
					gui.toggleFullscreen();
				} else {
					gui.stop();
				}
				break;
			case KeyEvent.VK_SPACE:
				gui.togglePause();
				break;
			case KeyEvent.VK_LEFT:
				audioPlayer.backMilliSeconds(5000);
				break;
			case KeyEvent.VK_RIGHT:
				audioPlayer.forwardMilliSeconds(5000);
				break;
			case KeyEvent.VK_F:
				audioPlayer.currentSamplerateMultiplierPercent = HelperFunctions.clamp(audioPlayer.currentSamplerateMultiplierPercent + 25, 25, 400);
				audioPlayer.refreshAudioFormat();
				break;
			case KeyEvent.VK_D:
				audioPlayer.currentSamplerateMultiplierPercent = HelperFunctions.clamp(audioPlayer.currentSamplerateMultiplierPercent - 25, 25, 400);
				audioPlayer.refreshAudioFormat();
				break;
			case KeyEvent.VK_ALT:
				e.consume(); //makes alt not do anything it would do normally (like unfocus the window, and change the cursor).
				break;
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		
	}
}
