package audioplayer.waveform;

import java.util.ArrayList;
import java.util.List;

public class AllOptimizedSamples {
	private List<OptimizedSamples> list;
	
	public AllOptimizedSamples(short[] samplesLeft, short[] samplesRight) {
		list = new ArrayList<>();
		
		list.add(new OptimizedSamples().createOptimizedSamples(samplesLeft, samplesRight, 1));
		//list.add(new OptimizedSamples().createOptimizedSamples(samplesLeft, samplesRight, 100));
		//list.add(new OptimizedSamples().createOptimizedSamples(samplesLeft, samplesRight, 1000));
	}
	
	public OptimizedSamples get(int i) {
		return list.get(i);
	}
	
	public OptimizedSamples getWithResolution(int fullyRenderedWidth, int minResolution) {
		//TODO: kesken
		return null;
	}
}
