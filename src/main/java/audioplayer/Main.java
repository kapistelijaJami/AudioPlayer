package audioplayer;

import audiofilereader.AudioFileReader;
import audiofilereader.MusicData;
import java.io.File;

//https://docs.fileformat.com/audio/wav/
//https://sites.google.com/site/musicgapi/technical-documents/wav-file-format#wavefilechunks
public class Main {
	public static void main(String[] args) {
		
		File file = null;
		
		file = new File("Staintune - Misery Business.wav");
		//file = new File("Primer.wav");
		//file = new File("Staintune - For A Pessimist, I'm Pretty Optimistic.wav");
		//file = new File("file_example_WAV_1MG.wav");
		//file = new File("08 - Kuutamohullu.wav");
		//file = new File("02 - Mullonikäväsua.wav");
		//file = new File("01 - Nokian takana.wav");
		//file = new File("Clocks synthetized.wav");
		
		if (args.length != 0) {
			file = new File(args[0]);
		}
		
		//fully my implementation, other one covers way more formats
		/*AudioFileReader audioReader = new AudioFileReader();
		MusicData musicData = audioReader.read(file);*/
		
		MusicData musicData = MusicData.createMusicData(file);
		
		AudioPlayerGUI audioPlayer = new AudioPlayerGUI(musicData);
		audioPlayer.start();
	}
}
