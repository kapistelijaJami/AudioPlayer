package audioplayer.waveform;

import audiofilereader.MusicData;

public class Camera {
	private double zoom = 1;
	private int firstSample = 0;
	private double maxCap = -1;
	
	public double getZoom() {
		return zoom;
	}
	
	public void setZoom(double z) {
		zoom = z;
		capZoom();
	}
	
	public void setMaxCap(double maxCap) {
		this.maxCap = maxCap;
	}
	
	public int getFirstSample() {
		return firstSample;
	}
	
	public void setFirstSample(int firstSample, MusicData musicData) {
		this.firstSample = (int) Math.max(0, Math.min(musicData.getFrameCount() - getVisibleSampleCount(musicData), firstSample));
	}
	
	public void setFirstSampleByTime(long milliseconds, MusicData musicData) {
		setFirstSample(musicData.millisToFrameNumber(milliseconds), musicData);
	}
	
	public void resetFirstSample() {
		this.firstSample = 0;
	}
	
	public double zoom(int amount) {
		if (amount == 1) {
			zoom *= 1.5;
		} else {
			zoom /= 1.5;
		}
		
		capZoom();
		return zoom;
	}
	
	private void capZoom() {
		zoom = Math.max(1, zoom);
		if (maxCap != -1) {
			zoom = Math.min(maxCap, zoom);
		}
	}
	
	public int getVisibleSampleCount(MusicData musicData) {
		return (int) (musicData.getFrameCount() / zoom);
	}
	
	public int getLastSample(MusicData musicData) {
		return firstSample + getVisibleSampleCount(musicData);
	}
	
	public void setZoomByDuration(long millis, MusicData musicData) {
		setZoom(getZoomLevelByDuration(millis, musicData));
	}
	
	public static double getZoomLevelByLength(int length, int frameCount) {
		return frameCount / (double) length;
	}
	
	public static double getZoomLevelByDuration(long millis, MusicData musicData) {
		int frames = musicData.millisToFrameNumber(millis);
		int frameCount = musicData.getFrameCount();
		if (frameCount == 0) {
			return 1;
		}
		return getZoomLevelByLength(frames, frameCount);
	}
}
