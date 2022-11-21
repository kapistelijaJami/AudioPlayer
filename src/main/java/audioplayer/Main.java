package audioplayer;

import audiofilereader.AudioFileReader;
import audiofilereader.MusicData;
import java.io.File;

//https://docs.fileformat.com/audio/wav/
//https://sites.google.com/site/musicgapi/technical-documents/wav-file-format#wavefilechunks
public class Main {
	public static void main(String[] args) {
		
		File file = new File("Staintune - Misery Business.wav");
		//File file = new File("Primer.wav");
		//File file = new File("Staintune - For A Pessimist, I'm Pretty Optimistic.wav");
		//File file = new File("file_example_WAV_1MG.wav");
		//File file = new File("08 - Kuutamohullu.wav");
		//File file = new File("02 - Mullonikäväsua.wav");
		//File file = new File("01 - Nokian takana.wav");
		//File file = new File("Clocks synthetized.wav");
		
		if (args.length != 0) {
			file = new File(args[0]);
		}
		
		AudioFileReader audioReader = new AudioFileReader();
		MusicData musicData = audioReader.read(file);
		
		AudioPlayerGUI audioPlayer = new AudioPlayerGUI(musicData);
		new Thread(audioPlayer).start();
	}
}
