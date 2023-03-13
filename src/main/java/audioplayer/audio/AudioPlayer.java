package audioplayer.audio;

import audiofilereader.MusicData;
import audioplayer.volume.AudioLevel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
	
	private long lastCurrentHEADUpdateTime;
	
	public int currentSamplerateMultiplierPercent = 100; //How fast to play the audio file in percentage. 100 is 1x speed. 150 is 1.5x speed etc.
	
	private AudioLevel audioLevel;
	
	private Runnable finishedFunction;
	
	public AudioPlayer(MusicData musicData, boolean stopped) {
		if (musicData == null) {
			musicData = MusicData.createDefault();
		}
		
		this.musicData = musicData;
		audioLevel = new AudioLevel();
		latch = new CountDownLatch(1);
		
		if (stopped) {
			stopped = true;
			paused = true;
		}
		
		init();
	}
	
	/**
	 * Callback for when the music finishes playing because it reached the end.
	 * @param finishedFunction 
	 */
	public void setFinishedCallback(Runnable finishedFunction) {
		this.finishedFunction = finishedFunction;
	}
	
	public void setMusicData(MusicData musicData) {
		stop();
		this.musicData = musicData;
		currentHEAD = 0;
		if (musicData == null) return;
		
		init();
	}
	
	public MusicData getMusicData() {
		return musicData;
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
		return new AudioFormat(sampleRate, musicData.bitsPerSample, musicData.getChannels(), true, false);
	}
	
	private void start() {
		line.start(); //makes line isRunning() true, "isRunning" should be called "isStarted", because that's what it checks.
		stopped = false;
	}
	
	private void stop() {
		pause();
		line.flush();
		line.stop();
		stopped = true;
	}
	
	private void close() {
		//shut down audio
		stop();
		line.close();
	}
	
	private void init() {
		double speedMultiplier = getSpeedMultiplier();
		int sampleRate = (int) (musicData.sampleRate * speedMultiplier);
		audioFormat = getAudioFormat(sampleRate);
		
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
		//System.out.println("\n" + info + "\n");
		
		try {
			line = (SourceDataLine) AudioSystem.getLine(info);
			//line.addLineListener(new MyLineListener()); //React to line updates, like stop play pause etc.
			
			//chunkSize explanations:
			//chunkSize is how much at a time we write to the line buffer.
			//512 was too low for some songs, 2k started to be fine for others too, but 8k is probably safe
			//8192 and lineBufferSize 8192 * 3 seems relly good for responsiveness and quality.
			//But dragging without flushing buffer makes it update the location really slow if sample rate is low, so make it depend on the sampleRate, or use milliseconds to calculate.
			
			chunkSize = (int) (musicData.microsToByteNumber((long) (45 * 1000 * speedMultiplier))); //def: 45ms. This makes it relative to the sampleRate. About 8k for 44.1kHz. (secs * sampleRate * bytesPerFrame = 0.045 * 44100 * 4 = 7938)
			
			int lineBufferSize = chunkSize * 3; //was chunkSize * 32, but made some stuff very delayed, tried same as chunk size, but it sometimes skipped, or lagged a bit. * 2 was skipping sometimes in nightcore. * 3 is the way.
			line.open(audioFormat, lineBufferSize); //this is buffer size for line, how much data can you fit at once. This has to be at least a bit bigger than chunkSize. Probably should be couple times bigger in relation to chunkSize.
			
			gain = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
		} catch (LineUnavailableException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private void play() {
		while (currentHEAD < musicData.getDataLength() && !paused) {
			int remainingDataLength = (int) (musicData.getDataLength() - currentHEAD); //uses this to write all to the end when chunkSize was too large
			
			//this made it much faster with bigger dataBytes array length
			byte[] buf = new byte[Math.min(chunkSize, remainingDataLength)];
			for (int i = 0; i < buf.length; i++) {
				buf[i] = musicData.getDataBytes()[(int) currentHEAD + i];
			}
			int written = line.write(buf, 0, buf.length);
			
			if (!paused) {
				lastCurrentHEADUpdateTime = System.currentTimeMillis();
				currentHEAD += written; //this sometimes (like once a second) happens twice before waveformDrawer has had time to render current time once so the line jumps a bit.
			}
		}
		
		if (!paused) { //music ended
			stop();
			if (finishedFunction != null) {
				finishedFunction.run();
			}
		}
	}
	
	/**
	 * Line is literally playing the music right now.
	 * It is reading or writing from the buffer.
	 * @return 
	 */
	public boolean isActive() {
		return line.isActive();
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
	
	public long getCurrentMillis() {
		return musicData.frameToMillis(getCurrentFrame());
	}
	
	public long getCurrentMicros() {
		return musicData.frameToMicros(getCurrentFrame());
	}
	
	public double getCurrentSeconds() {
		return musicData.frameToSeconds(getCurrentFrame());
	}
	
	public int getCurrentFrame() {
		if (line == null || paused) {
			return musicData.bytesToFrameNumber(currentHEAD);
		}
		
		int currentByte = currentHEAD + (int) musicData.millisToByteNumber((long) ((System.currentTimeMillis() - lastCurrentHEADUpdateTime) * getSpeedMultiplier())); //to move more smoothly
		
		return musicData.bytesToFrameNumber(currentByte - line.getBufferSize() / 2);
	}
	
	public void setCurrentTimeByMillis(long millis) {
		int frame = musicData.millisToFrameNumber(millis);
		updateCurrentLocationByFrame(frame);
	}
	
	public void setCurrentTimeByMicros(long micros) {
		int frame = musicData.microsToFrameNumber(micros);
		updateCurrentLocationByFrame(frame);
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
		return getLevel(musicData.getSamplesByChannel(true, getCurrentFrame() - frameCount, frameCount * 2));
	}
	
	public double getLevelRight() {
		int frameCount = musicData.millisToFrameNumber(audioLevel.updateInterval); //100ms in frames
		return getLevel(musicData.getSamplesByChannel(false, getCurrentFrame() - frameCount, frameCount * 2));
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
		if (paused) {
			if (!line.isRunning()) {
				start();
			}
			paused = false;
			latch.countDown();
		} else {
			line.flush();
			paused = true;
		}
	}
	
	public void pause() {
		if (!paused) {
			togglePause();
		}
	}
	
	public void unpause() {
		if (paused) {
			togglePause();
		}
	}
	
	/**
	 * Stops the music.
	 * Resets the next playback point to 0.
	 */
	public void stopTheMusic() {
		stopTheMusic(0);
	}
	
	/**
	 * Stops the music.
	 * Resets the next playback point to continueFrame.
	 * @param continueFrame Is used for resetting location to last clicked point, or to start
	 */
	public void stopTheMusic(int continueFrame) {
		if (!stopped) {
			pause();
			stop();
		}
		updateCurrentLocationByFrame(continueFrame);
	}
	
	/**
	 * AudioPlayer is paused.
	 * It might still be emptying it's buffer and playing it, but either it's already stopped,
	 * or it will be completely stopped in couple milliseconds.
	 * @return 
	 */
	public boolean isPaused() {
		return paused;
	}
	
	public void backMilliSeconds(int ms) {
		setCurrentTimeByMicros(Math.max(0, getCurrentMicros() - ms * 1000));
	}
	
	public void forwardMilliSeconds(int ms) {
		setCurrentTimeByMicros(Math.max(0, getCurrentMicros() + ms * 1000));
	}
	
	public double getSpeedMultiplier() {
		return currentSamplerateMultiplierPercent / 100.0;
	}
}
