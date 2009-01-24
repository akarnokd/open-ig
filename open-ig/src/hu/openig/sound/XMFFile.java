/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.sound;

import hu.openig.utils.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.SourceDataLine;
/**
 * XML file decoder class.
 * @author karnokd
 *
 */
public class XMFFile {
	/**
	 * Returns the audio samples from the supplied XMF data.
	 * @param data the data file
	 * @return the list of audio samples
	 */
	public static List<byte[]> getSamples(byte[] data) {
		List<Integer> sampleSizes = new ArrayList<Integer>();
		int src = 0;
		while (true) {
			int start = (data[src + 7] & 0xFF) | (data[src + 8] & 0xFF) << 8 | (data[src + 9] & 0xFF) << 16;
			int end = (data[src + 10] & 0xFF) | (data[src + 11] & 0xFF) << 8 | (data[src + 12] & 0xFF) << 16;
			if (start == 0 && end == 0) {
				break;
			}
			sampleSizes.add(end - start);
			src += 16;
		}
		src = data.length;
		List<byte[]> result = new ArrayList<byte[]>();
		for (int i = sampleSizes.size() - 1; i >= 0; i--) {
			int len = sampleSizes.get(i);
			result.add(0, Arrays.copyOfRange(data, src - len, src));
			src -= len;
		}
		return result;
	}
	public static void main(String[] args) throws IOException {
		SourceDataLine sdl = AudioThread.createAudioOutput();
		int i = 0;
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		for (byte[] sample : getSamples(IOUtils.load("c:/games/ig/music/main3.xmf"))) {
			System.out.printf("%d: %d", ++i, sample.length);
			sdl.start();
			sdl.write(sample, 0, sample.length);
			sdl.drain();
			sdl.stop();
			in.readLine();
		}
		sdl.drain();
		sdl.stop();
		sdl.close();
	}
}
