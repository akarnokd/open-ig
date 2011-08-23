/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import hu.openig.sound.AudioThread;
import hu.openig.utils.IOUtils;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Play the original SMP files.
 * @author akarnokd, 2011.08.17.
 */
public final class PlaySMP {
	/** Utility class. */
	private PlaySMP() {
		// utility class
	}
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		//enumerateSounds();
		byte[] sample = IOUtils.load("c:/Games/IGHU/MUSIC/sample.smp");
		int dataLen = sample.length + (sample.length % 2 == 0 ? 0 : 1);

		DataOutputStream dout = new DataOutputStream(new FileOutputStream("audio/hu/ui/welcome.wav"));
		try {
			// HEADER
			dout.write("RIFF".getBytes("ISO-8859-1"));
			dout.writeInt(Integer.reverseBytes(36 + dataLen)); // chunk size
			dout.write("WAVE".getBytes("ISO-8859-1"));
			
			// FORMAT
			dout.write("fmt ".getBytes("ISO-8859-1"));
			dout.writeInt(Integer.reverseBytes(16)); // chunk size
			dout.writeShort(Short.reverseBytes((short)1)); // Format: PCM = 1
			dout.writeShort(Short.reverseBytes((short)1)); // Channels = 1
			dout.writeInt(Integer.reverseBytes(22050)); // Sample Rate = 22050
			dout.writeInt(Integer.reverseBytes(22050)); // Byte Rate = 22050
			dout.writeShort(Short.reverseBytes((short)1)); // Block alignment = 1
			dout.writeShort(Short.reverseBytes((short)8)); // Bytes per sample = 8
	
			// DATA
			dout.write("data".getBytes("ISO-8859-1"));
			dout.writeInt(Integer.reverseBytes(dataLen));
			for (byte b : sample) {
				dout.write(128 + b);
			}
		} finally {		
			dout.close();
		}
	}
	/**
	 * Enumerates sounds of SMP files.
	 * @throws IOException on error
	 * @throws InterruptedException on error
	 */
	static void enumerateSounds() throws IOException, InterruptedException {
		DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("c:/Games/IGHU/sound/"), "NOI85.SMP");
		for (Path p : ds) {
			System.out.println(p);
			AudioThread at = new AudioThread();
			at.start();
			at.startPlaybackNow();
			at.submit(IOUtils.load(p.toFile()), true);
			at.submit(new byte[0], false);
			at.join();
			Thread.sleep(200);
		}
		ds.close();
	}

}
