package audioplayer;

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

public class Input implements MouseInputListener, MouseWheelListener, KeyListener, GlobalMouseListener, ComponentListener {
	private Game game;
	
	public Input(Game game) {
		this.game = game;
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		game.mousePressed(e);
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		game.mouseReleased(e);
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		game.mouseDragged(e);
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		game.mouseMoved(e);
	}
	
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		game.MouseWheelMoved(e);
	}
	
	@Override
	public void mousePressed(GlobalMouseEvent event) {
		
	}
	
	@Override
	public void mouseReleased(GlobalMouseEvent event) {
		
	}
	
	@Override
	public void mouseMoved(GlobalMouseEvent event) {
		game.mouseMoved(event);
	}
	
	@Override
	public void mouseWheel(GlobalMouseEvent event) {
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		int w = e.getComponent().getWidth();
		int h = e.getComponent().getHeight();
		game.windowResized(w, h);
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
		int key = e.getKeyCode();
		switch (key) {
			case KeyEvent.VK_F11:
				game.toggleFullscreen();
				break;
			case KeyEvent.VK_ESCAPE:
				game.stop();
				break;
			case KeyEvent.VK_SPACE:
				game.togglePause();
				break;
			case KeyEvent.VK_LEFT:
				game.backMilliSeconds(5000);
				break;
			case KeyEvent.VK_RIGHT:
				game.forwardMilliSeconds(5000);
				break;
			case KeyEvent.VK_F:
				game.audioPlayer.currentSampleRateMultiplierPercent = Math.min(400, Math.max(25, game.audioPlayer.currentSampleRateMultiplierPercent + 25));
				game.audioPlayer.refreshAudioFormat();
				break;
				
			case KeyEvent.VK_D:
				game.audioPlayer.currentSampleRateMultiplierPercent = Math.min(400, Math.max(25, game.audioPlayer.currentSampleRateMultiplierPercent - 25));
				game.audioPlayer.refreshAudioFormat();
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
