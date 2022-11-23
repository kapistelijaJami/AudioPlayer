package audioplayer.waveform;

public class OptimizedSamples {
	public HiLow[] optimizedSamplesLeft;
	public HiLow[] optimizedSamplesRight;
	
	public OptimizedSamples createOptimizedSamples(short[] samplesLeft, short[] samplesRight, int amount) { //amount is how many samples are crammed together in one HiLow.
		int optimizedSamplesCount = howManyOptimizedSamples(samplesLeft.length, amount);
		optimizedSamplesLeft = new HiLow[optimizedSamplesCount];
		optimizedSamplesRight = new HiLow[optimizedSamplesCount];
		
		int counter = 0;
		int i;
		for (i = 0; i + amount < samplesLeft.length; i += amount, counter++) {
			HiLow hiLowLeft = new HiLow();
			HiLow hiLowRight = new HiLow();
			
			for (int j = i; j < i + amount; j++) {
				short left = samplesLeft[j];
				short right = samplesRight[j];
				
				hiLowLeft.updateValues(left);
				hiLowRight.updateValues(right);
			}
			
			optimizedSamplesLeft[counter] = hiLowLeft;
			optimizedSamplesRight[counter] = hiLowRight;
		}
		
		HiLow hiLowLeft = new HiLow();
		HiLow hiLowRight = new HiLow();
		
		//remainder times added to last sample: length % amount times...
		int remainder = samplesLeft.length % amount;
		for (int j = i; j < i + remainder; j++) {
			short left = samplesLeft[j];
			short right = samplesRight[j];

			if (left > hiLowLeft.h) {
				hiLowLeft.h = left;
			}

			if (left < hiLowLeft.l) {
				hiLowLeft.l = left;
			}

			if (right > hiLowRight.h) {
				hiLowRight.h = right;
			}

			if (right < hiLowRight.l) {
				hiLowRight.l = right;
			}
		}
		
		optimizedSamplesLeft[counter] = hiLowLeft;
		optimizedSamplesRight[counter] = hiLowRight;
		return this;
	}
	
	public static int howManyOptimizedSamples(int samples, int amount) {
		return (int) Math.ceil(samples / (double) amount);
	}
	
	
	/**
	 * How many samples are per pixel with given width.
	 * @param width This has to be the width of the whole waveform rendered even if it is rendered out of screen because of zoom.
	 *				So zooming might be best to actually scale the waveform instead of using a camera or scale the graphics object.
	 * @return 
	 */
	public int getResolution(int width) {
		return (int) Math.ceil(getSampleCount() / (double) width);
	}
	
	public long getSampleCount() {
		return optimizedSamplesLeft.length;
	}
}
