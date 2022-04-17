package audioplayer;

import audiofilereader.MusicData;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioPlayer implements Runnable {
	private MusicData musicData;
	private SourceDataLine line;
	private boolean paused = false;
	private boolean stopped = false;
	private AudioFormat audioFormat;
	private FloatControl gain;
	private int chunkSize;
	private int currentHEAD;
	private CountDownLatch latch;
	
	public int currentSampleRateMultiplierPercent = 100;
	
	private AudioLevel audioLevel;
	
	public AudioPlayer(MusicData musicData) {
		this.musicData = musicData;
		audioLevel = new AudioLevel();
		
		latch = new CountDownLatch(1);
		init();
	}
	
	@Override
	public void run() {
		start();
		
		while (true) {
			while (!paused) {
				play();
			}
			
			try {
				latch.await(2, TimeUnit.SECONDS); //waits for other thread, or max 2 secs
				latch = new CountDownLatch(1); //creates new countDownLatch because they are one use only
			} catch (InterruptedException e) {
				
			}
		}
	}
	
	public void update() {
		updateAudioLevel(!paused);
	}
	
	private AudioFormat getAudioFormat(int sampleRate) {
		return new AudioFormat(sampleRate, musicData.bitsPerSample, musicData.numberOfChannels, true, false);
	}
	
	private void init() {
		int sampleRate = (int) (musicData.sampleRate * (currentSampleRateMultiplierPercent / 100.0));
		audioFormat = getAudioFormat(sampleRate);
		
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		System.out.println("\n" + info + "\n");
		
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);
			line.addLineListener(new MyLineListener());
			
			//chunkSize = 512; //too low for some songs, 2k started to be fine for others too, but 8k is probably safe
			chunkSize = (int) (musicData.microsToByteNumber((long) (45 * 1000 * (currentSampleRateMultiplierPercent / 100.0)))); //def: 45ms. This makes it relative to the sampleRate. About 8k for 44.1kHz
			//chunkSize = 8192;  //8192 and other 8192 * 3 seems relly good for responsiveness and quality. But dragging without flushing buffer makes it update the location really slow if sample rate is low.
			int lineBufferSize = chunkSize * 3; //was chunkSize * 32, but made some stuff very delayed, tried same as chunk size, but it sometimes skipped, or lagged a bit. * 2 was skipping sometimes in nightcore. * 3 is the way.
			
			line.open(audioFormat, lineBufferSize); //tää on bufferikoko linelle miten paljon dataa sinne voi kerralla mahduttaa. Tän ainakin pitää olla vähän isompi. Varmaan tärkein on olla suhteessa tietyn verran isompi kun chunkSize.
			
			gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
			
			
			//setCurrentHEADByMicros((long) (0 * 1e6));
		} catch (LineUnavailableException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void stop() {
		line.drain();
		line.stop();
		stopped = true;
	}
	
	private void close() {
		//shut down audio
		stop();
		line.close();
	}
	
	public boolean isActive() {
		return line.isActive(); //line is literally playing the music right now. It is reading or writing from the buffer
	}
	
	private void start() {
		line.start(); //makes line isRunning() true, "isRunning" should be called "isStarted", because that's what it checks.
		stopped = false;
	}
	
	private void play() {
		while (currentHEAD < musicData.dataBytes.length && !paused) {
			int remainingDataLength = musicData.dataBytes.length - (int) currentHEAD; //uses this to write all to the end when chunkSize was too large
			
			//this made it much faster with bigger dataBytes array length
			byte[] buf = new byte[Math.min(chunkSize, remainingDataLength)];
			for (int i = 0; i < buf.length; i++) {
				buf[i] = musicData.dataBytes[(int) currentHEAD + i];
			}
			int written = line.write(buf, 0, buf.length);
			
			if (!paused) {
				currentHEAD += written;
			}
		}
		
		if (!paused) {
			paused = true;
			stop();
		}
	}
	
	public void refreshAudioFormat() {
		paused = true;
		line.flush();
		float vol = gain.getValue();
		
		init();
		
		gain.setValue(vol);
		line.start();
		
		paused = false;
		latch.countDown();
	}
	
	public FloatControl getGain() {
		return gain;
	}
	
	public AudioLevel getAudioLevel() {
		return audioLevel;
	}
	
	public void setCurrentHEADByMicros(long micros) {
		currentHEAD = (int) musicData.microsToByteNumber(micros);
	}
	
	public long getCurrentMillis() {
		return musicData.frameToMillis(getCurrentFrame());
	}
	
	public long getCurrentMicros() {
		return musicData.frameToMicros(getCurrentFrame());
	}
	
	public int getCurrentFrame() {
		if (line == null || paused) {
			return musicData.bytesToFrameNumber(currentHEAD);
		}
		
		return musicData.bytesToFrameNumber(currentHEAD - line.getBufferSize() / 2);
	}
	
	public void updateCurrentLocationByFrame(int frame) {
		updateCurrentLocationByFrame(frame, true);
	}
	
	/**
	 * This will move to the given frame.
	 * @param frame
	 * @param flushBuffer When jumping somewhere instantly you want to flush the buffer. But if you are dragging with mouse, it will be crackly, so then false.
	 */
	public void updateCurrentLocationByFrame(int frame, boolean flushBuffer) {
		if (line == null) {
			return;
		}
		boolean origPause = paused;
		paused = true;
		if (!line.isActive()) {
			start();
		}
		
		if (flushBuffer) {
			line.flush(); //faster responsiveness for moving around with one click, but dragging around makes it crackly.
		}
		
		currentHEAD = musicData.frameToByteNumber(frame);
		
		if (!origPause) {
			paused = false;
			latch.countDown(); //this releases the thread from waiting if it was already there
		}
	}
	
	//Returns the current audio volume between 0 and 1
	public double getLevelLeft() {
		int frameCount = musicData.millisToFrameNumber(audioLevel.updateInterval); //100ms in frames
		return getLevel(musicData.getSamplesChannel(true, getCurrentFrame() - frameCount, frameCount * 2));
	}
	
	public double getLevelRight() {
		int frameCount = musicData.millisToFrameNumber(audioLevel.updateInterval); //100ms in frames
		return getLevel(musicData.getSamplesChannel(false, getCurrentFrame() - frameCount, frameCount * 2));
	}
	
	private double getLevel(short[] samples) {
		if (line == null || !line.isActive()) {
			return 0;
		}
		
		int max = 0;
		long total = 0;
		int count = 0;
		
		for (int i = 0; i < samples.length; i++) {
			if (i < 0) {
				continue;
			}
			
			if (i < samples.length) {
				short val = (short) Math.abs(samples[i]);
				if (val > max) {
					max = val;
				}
				total += Math.abs(val);
				count++;
			}
		}
		
		int maxValue = musicData.getMaxValue();
		double level = max / (double) maxValue;
		double average = (total / (double) count) / (double) maxValue;
		
		//return level;
		return (average + level * 9) / 10.0; //this is ok
	}
	
	private void updateAudioLevel(boolean update) {
		if (!update) {
			audioLevel.setLevels(0, 0);
			return;
		}
		audioLevel.update();
		audioLevel.setLevels(getLevelLeft(), getLevelRight()); //this is really slow method. Edit. shouldn't be that slow anymore
	}

	public void togglePause() {
		if (!paused) {
			line.flush();
			paused = true;
		} else {
			if (!line.isRunning()) {
				start();
			}
			paused = false;
			latch.countDown();
		}
	}
	
	public void stopTheMusic(int continueFrame) { //continueFrame is used for resetting location to last clicked point, or to start
		if (!stopped) {
			if (!paused) {
				togglePause();
			}
			stop();
			updateCurrentLocationByFrame(continueFrame);
		}
	}
	
	public boolean isPaused() {
		return paused;
	}
	
	public void backMilliSeconds(int ms) {
		setCurrentHEADByMicros(Math.max(0, getCurrentMicros() - ms * 1000));
	}
	
	public void forwardMilliSeconds(int ms) {
		setCurrentHEADByMicros(Math.max(0, getCurrentMicros() + ms * 1000));
	}
}
