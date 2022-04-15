package dataanalysis;

public class AudioLevel {
	private double levelLeft;
	private double levelRight;
	private double drainage = 0.0005;
	
	public int updateInterval = 50;
	
	public void update() {
		levelLeft = Math.max(0, levelLeft - drainage);
		levelRight = Math.max(0, levelRight - drainage);
	}
	
	public double getLeft() {
		return levelLeft;
	}
	
	public double getRight() {
		return levelRight;
	}
	
	public void setLevels(double left, double right) {
		this.levelLeft = left;
		this.levelRight = right;
	}
}
