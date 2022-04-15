package audioplayer;

public class HelperFunctions {
	public static String preZeros(int i, int numberOfDigits) {
		return String.format("%0" + numberOfDigits + "d", i);
	}
	
	public static int howManyDigits(int i) {
		return (int) Math.log10(i) + 1;
	}
	
	public static double dist(double a, double b) {
		return Math.abs(a - b);
	}
	
	public static double clamp(double val, double min, double max) {
		return Math.min(max, Math.max(min, val));
	}
	
	public static int clamp(int val, int min, int max) {
		return Math.min(max, Math.max(min, val));
	}
}
