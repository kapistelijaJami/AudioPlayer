package dataanalysis;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import javax.swing.JFrame;

public class Window extends JFrame {
	private Canvas canvas;
	
	public Window(int width, int height, String title, Game game) {
		this(width, height, title, game, 0, false);
	}
	
	public Window(int width, int height, String title, Game game, int screen, boolean fullscreen) {
		super(title);
		canvas = new Canvas();
		
		super.setPreferredSize(new Dimension(width, height));	//laitetaan ikkunan koko
		super.setMinimumSize(new Dimension(256, 144));
		//frame.setMaximumSize(new Dimension(width, height));
		
		//FULLSCREEN STUFF
		if (fullscreen) {
			super.setExtendedState(JFrame.MAXIMIZED_BOTH);
			super.setUndecorated(true);
		}
		
		super.addWindowListener(new WindowAdapter() { //ikkuna sulkeutuu rastista ja pystyy kutsua myös metodia
			@Override
			public void windowClosing(WindowEvent e) {
				game.stop();
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
				//minimized
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
				//back from minimized
			}
		});
		
		
		
		//setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);	//ikkuna sulkeutuu rastista
		//super.setResizable(false);							//ikkunaa ei voi venyttää
		super.add(canvas);										//lisätään peli ikkunaan
		super.pack();											//tehdään JFrame Windowista halutun kokoinen
		super.setLocationRelativeTo(null);						//ikkuna syntyy näytön keskelle
		super.setVisible(true);									//laitetaan ikkuna näkyväksi
		
		Game.WIDTH = width;
		Game.HEIGHT = height;
		
		Insets inset = super.getInsets();
		/*Game.WIDTH -= inset.left + inset.right;
		Game.HEIGHT -= inset.top + inset.bottom;*/
		super.setPreferredSize(new Dimension(Game.WIDTH + inset.left + inset.right, Game.HEIGHT + inset.top + inset.bottom));
		super.pack();
		
		setScreen(this, screen);
	}
	
	public Canvas getCanvas() {
		return canvas;
	}
	
	public void close() {
		dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
	}
	
	public static void setScreen(JFrame frame, int screen) {
		GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
		//GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		
		GraphicsDevice monitor;
		
		if (screen >= 0 && screen < devices.length) {
			monitor = devices[screen];
		} else if (devices.length > 0) {
			monitor = devices[0];
		} else {
			throw new RuntimeException("No Screens Found");
		}
		
		Rectangle gcBounds = monitor.getDefaultConfiguration().getBounds();
		
		Dimension windowSize = frame.getSize();
		Point centerPoint = new Point((int) gcBounds.getCenterX(), (int) gcBounds.getCenterY());
		int dx = centerPoint.x - windowSize.width / 2;
		int dy = centerPoint.y - windowSize.height / 2;
		
		
		// Avoid being placed off the edge of the screen:
		if (dy + windowSize.height > gcBounds.y + gcBounds.height) { //bottom
			dy = gcBounds.y + gcBounds.height - windowSize.height;
		}
		
		if (dy < gcBounds.y) { //top
			dy = gcBounds.y;
		}
		
		if (dx + windowSize.width > gcBounds.x + gcBounds.width) { //right
			dx = gcBounds.x + gcBounds.width - windowSize.width;
		}
		
		if (dx < gcBounds.x) { //left
			dx = gcBounds.x;
		}
		
		frame.setLocation(dx, dy);
	}
	
	public void setFullscreen(boolean fullscreen) {
		GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow(fullscreen ? this : null);
	}
	
	public Graphics2D getGraphics2D() {
		BufferStrategy bs = canvas.getBufferStrategy();
		while (bs == null) {
			canvas.createBufferStrategy(3);
			bs = canvas.getBufferStrategy();
		}
		
		Graphics2D g = (Graphics2D) bs.getDrawGraphics();
		setGraphicsRenderingHints(g);
		
		g.setColor(Color.black);
		g.fillRect(0, 0, WIDTH, HEIGHT);
		
		return g;
	}
	
	public static void setGraphicsRenderingHints(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR); //for image scaling to look sharp, might not be good for all images
	}
	
	public void display(Graphics2D g) {
		g.dispose();
		canvas.getBufferStrategy().show();
	}
}
