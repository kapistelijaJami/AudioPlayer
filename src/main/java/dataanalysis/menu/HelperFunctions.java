package dataanalysis.menu;

import java.awt.Color;

public class HelperFunctions {
	public static Color getDarkerColor(Color c, int amount) {
		int r = Math.max(40, c.getRed() - amount);
		int g = Math.max(40, c.getGreen() - amount);
		int b = Math.max(40, c.getBlue() - amount);
		return new Color(r, g, b);
	}
	
	public static String preZeros(int i, int numberOfDigits) {
		return String.format("%0" + numberOfDigits + "d", i);
	}
	
	public static int howManyDigits(int i) {
		return (int) Math.log10(i) + 1;
	}
	
	public static double dist(double a, double b) {
		return Math.abs(a - b);
	}
}
