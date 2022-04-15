package dataanalysis;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public class MyLineListener implements LineListener {
	
	@Override
	public void update(LineEvent event) {
		System.out.println("Line event update: " + event.getType() + ", frame position: " + event.getFramePosition());
	}
}
