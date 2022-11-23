package audioplayer.volume;

import audioplayer.audio.AudioPlayer;

public class VolumeController {
	private int currentVolumePercent = 50;
	private AudioPlayer audioPlayer;
	
	public VolumeController(AudioPlayer audioPlayer) {
		this.audioPlayer = audioPlayer;
	}
	
	/**
	 * Change volume by little bit with scroll.
	 * @param scrollDirection Should be -e.getWheelRotation()
	 */
	public void scroll(int scrollDirection) {
		setVolume(nextVolumePercent(currentVolumePercent, scrollDirection));
	}
	
	protected static int nextVolumePercent(int prev, int dir) {
		int multiplier = prev + dir <= 5 ? 1 : 5;
		return prev + (dir * multiplier);
	}
	
	public void setVolume(int percent) {
		currentVolumePercent = Math.min(200, Math.max(0, percent));
		double volume = currentVolumePercent / 100.0f;
		
		audioPlayer.getGain().setValue(20f * (float) Math.log10(volume));
	}
	
	public int getCurrentVolumePercent() {
		return currentVolumePercent;
	}
	
	public void reapplyVolume() {
		setVolume(currentVolumePercent);
	}
}
