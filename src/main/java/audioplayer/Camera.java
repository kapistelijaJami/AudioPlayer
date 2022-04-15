package audioplayer;

import audiofilereader.MusicData;

public class Camera {
	private double zoom = 1;
	private int firstSample = 0;
	
	public double getZoom() {
		return zoom;
	}
	
	public void setZoom(double z) {
		zoom = z;
		capZoom();
	}
	
	public int getFirstSample() {
		return firstSample;
	}
	
	public void setFirstSample(int firstSample, MusicData musicData) {
		this.firstSample = (int) Math.max(0, Math.min(musicData.getFrameCount() - getSampleCount(musicData), firstSample));
	}
	
	public double zoom(int amount) {
		if (amount == 1) {
			zoom *= 2;
		} else {
			zoom /= 2.0;
		}
		
		capZoom();
		return zoom;
	}
	
	private void capZoom() {
		zoom = Math.max(1, zoom); //TODO: do max cap too
	}
	
	public int getSampleCount(MusicData musicData) {
		return (int) (musicData.getFrameCount() / zoom);
	}
	
	public int getLastSample(MusicData musicData) {
		return firstSample + getSampleCount(musicData);
	}
}
