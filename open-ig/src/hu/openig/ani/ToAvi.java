/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ani;

import hu.openig.ani.SpidyAniFile.Algorithm;
import hu.openig.ani.SpidyAniFile.Block;
import hu.openig.ani.SpidyAniFile.Data;
import hu.openig.ani.SpidyAniFile.Palette;
import hu.openig.ani.SpidyAniFile.Sound;
import hu.openig.compress.LZSS;
import hu.openig.compress.RLE;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/**
 * Convert the SpidyAni2 files into uncompressed AVI.
 * @author karnokd, 2009.02.20.
 * @version $Revision 1.0$
 */
public final class ToAvi {
	/** Private constructor. */
	private ToAvi() {
		// utility class
	}
	/** 
	 * Change between little endian and big endian coding.
	 * @param val the value to rotate
	 * @return the rotated value 
	 */
	private static int rotate(int val) {
		return (val & 0xFF000000) >> 24 | (val & 0xFF0000) >> 8 
		| (val & 0xFF00) << 8 | (val & 0xFF) << 24;
	}
	/** 
	 * Change between little endian and big endian coding.
	 * @param val the value to rotate
	 * @return the rotated value 
	 */
	private static int rotateShort(int val) {
		return (val & 0xFF00) >> 8 | (val & 0xFF) << 8;
	}
	/**
	 * Main program.
	 * @param args arguments, format: ToAvi inFile outFile
	 * @throws IOException ignored
	 */
	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			System.out.printf("Usage:%nToAvi inFile outFile%n");
			return;
		}
		SpidyAniFile saf = new SpidyAniFile();
		FileInputStream fin = new FileInputStream(args[0]); 
		saf.open(fin);
		saf.load();
		// audio data
		ByteArrayOutputStream bout = new ByteArrayOutputStream(102400);
		// video frames
		List<int[]> frames = new ArrayList<int[]>();
		try {
			Palette palette = null;
			int imageHeight = 0;
			Algorithm alg = saf.getAlgorithm();
			int dst = 0;
			int[] rawImage = new int[saf.getWidth() * saf.getHeight()];
			while (true) {
				Block b = saf.next();
				if (b instanceof Palette) {
					palette = (Palette)b;
				} else
				if (b instanceof Sound) {
					bout.write(b.data);
				} else
				if (b instanceof Data) {
					if (true) {
						continue;
					}
					Data d = (Data)b;
					imageHeight += d.height;
					// decompress the image
					byte[] rleInput = d.data;
					if (saf.isLZSS() && !d.specialFrame) {
						rleInput = new byte[d.bufferSize];
						LZSS.decompress(d.data, 0, rleInput, 0);
					}
					switch (alg) {
					case RLE_TYPE_1:
						int newDst = RLE.decompress1(rleInput, 0, rawImage, dst, palette);
						dst = newDst;
						break;
					case RLE_TYPE_2:
						newDst = RLE.decompress2(rleInput, 0, rawImage, dst, palette);
						dst = newDst;
						break;
					default:
					}
					// we reached the number of subimages per frame?
					if (imageHeight >= saf.getHeight()) {
						frames.add(rawImage);
						rawImage = new int[saf.getWidth() * saf.getHeight()];
						imageHeight = 0;
						dst = 0;
					}
				}
			}
		} catch (EOFException ex) {
			
		}
		fin.close();
		
		writeWaveFile(args, bout);
		//writeAviFile(saf, args, frames, bout, 17.89);
	}
	/**
	 * Write avi file.
	 * @param saf the spidyani file
	 * @param args the arguments
	 * @param frames the frame pixels
	 * @param sound the sound stream
	 * @param fps the target frame/sec
	 * @throws IOException passed along
	 */
	static void writeAviFile(SpidyAniFile saf, String[] args, 
			List<int[]> frames, ByteArrayOutputStream sound, double fps)
	throws IOException {

		RandomAccessFile rf = new RandomAccessFile(args[1], "rw");
		rf.write("RIFF".getBytes("Latin1"));
		rf.writeInt(rotate(0));
		rf.write("AVI ".getBytes("Latin1"));
		// list of hdrl
		rf.write("LIST".getBytes("Latin1"));
		rf.writeInt(rotate(0));
		rf.write("hdrl".getBytes("Latin1"));
		// -----
		rf.write("avih".getBytes("Latin1"));
		rf.writeInt(rotate(56));
		// avi header
		rf.writeInt(rotate((int)(1000 / fps))); // frame delay
		rf.writeInt(0); // data rate
		rf.writeInt(rotate(1)); // padding unit
		rf.writeInt(rotate(0)); // flags
		rf.writeInt(rotate(saf.getFrameCount())); // total number of frames
		rf.writeInt(rotate(0)); // number of initial frames
		rf.writeInt(rotate(2)); // number of data streams in chunk
		rf.writeInt(rotate(0)); // suggested buffer size
		rf.writeInt(rotate(saf.getWidth())); // width
		rf.writeInt(rotate(saf.getHeight())); // height
		rf.writeInt(rotate(1)); // time scale
		rf.writeInt(rotate(0)); // data rate of playback
		rf.writeInt(rotate(0)); // avi starting time
		rf.writeInt(rotate(0)); // size of data chunk
		// end of avi header
		// list of stream ----
		rf.write("LIST".getBytes("Latin1"));
		rf.writeInt(rotate(0));
		rf.write("strl".getBytes("Latin1"));
		// -----
		rf.writeInt(rotate(0));
		rf.write("strh".getBytes("Latin1")); // stream header
		rf.writeInt(rotate(48));
		rf.write("vids".getBytes("Latin1"));
		rf.writeInt(rotate(0)); // data handler
		rf.writeInt(rotate(0)); // flags
		rf.writeInt(rotate(0)); // priority
		rf.writeInt(rotate(0)); // initial frames
		rf.writeInt(rotate(1)); // time scale
		rf.writeInt(rotate(1)); // data rate
		rf.writeInt(rotate(0)); // start time
		rf.writeInt(rotate(0)); // data length
		rf.writeInt(rotate(0)); // suggested buffer size
		rf.writeInt(rotate(0)); // sample quality
		rf.writeInt(rotate(0)); // sample size
		
		// -----
		rf.write("strf".getBytes("Latin1")); // stream format
		rf.writeInt(rotate(0));
		// for video
		
		// video format end
		// list of stream AUDIO ----
		rf.write("LIST".getBytes("Latin1"));
		rf.writeInt(rotate(0));
		rf.write("strl".getBytes("Latin1"));
		// -----
		rf.writeInt(rotate(0));
		rf.write("strh".getBytes("Latin1")); // stream header
		rf.writeInt(rotate(48));
		rf.write("auds".getBytes("Latin1"));
		rf.writeInt(rotate(0)); // data handler
		rf.writeInt(rotate(0)); // flags
		rf.writeInt(rotate(0)); // priority
		rf.writeInt(rotate(0)); // initial frames
		rf.writeInt(rotate(1)); // time scale
		rf.writeInt(rotate(1)); // data rate
		rf.writeInt(rotate(0)); // start time
		rf.writeInt(rotate(0)); // data length
		rf.writeInt(rotate(0)); // suggested buffer size
		rf.writeInt(rotate(0)); // sample quality
		rf.writeInt(rotate(0)); // sample size
		
		// -----
		rf.write("strf".getBytes("Latin1")); // stream format
		rf.writeInt(rotate(18));
		// for audio
		rf.writeShort(rotateShort(1)); // audioformat
		rf.writeShort(rotateShort(1)); // channels
		rf.writeInt(rotate(22050)); // samplerate
		rf.writeInt(rotate(22050)); // byterate
		rf.writeShort(rotateShort(1)); // block alignment
		rf.writeShort(rotateShort(8)); // bytes per sample
		rf.writeInt(rotate(0)); // extra information size
		// hdlr end
		// movi chunk start
		rf.write("LIST".getBytes("Latin1"));
		rf.writeInt(rotate(0));
		rf.write("movi".getBytes("Latin1"));
		// movie data
		int audiochunks = sound.size() / saf.getFrameCount();
		byte[] adata = sound.toByteArray();
		int audioIdx = 0;
		for (int i = 0; i < saf.getFrameCount(); i++) {
			// write sound sample
			int audiosize = audioIdx + audiochunks > sound.size() ? sound.size() - audioIdx : audiochunks;
			rf.write("01wb".getBytes("Latin1"));
			rf.writeInt(rotate(audiosize));
			// pad if necessary
			if (audiosize % 2 == 1) {
				rf.write(0);
			}
			rf.write(adata, audioIdx, audiosize);
			audioIdx += audiosize;
			// write frame data
			int[] frame = frames.get(i);
			rf.write("00db".getBytes("Latin1"));
			rf.writeInt(rotate(frame.length * 4));
			for (int j = 0; j < frame.length; j++) {
				rf.writeInt(frame[j]);
			}
		}
		
		rf.close();
	}
	/**
	 * Writes a wav file based on the raw bytes.
	 * @param args the arguments
	 * @param bout the audio stream
	 * @throws IOException passed along
	 */
	static void writeWaveFile(String[] args, ByteArrayOutputStream bout)
			throws IOException {
		RandomAccessFile rf = new RandomAccessFile(args[1], "rw");
		rf.write("RIFF".getBytes("Latin1"));
		rf.writeInt(rotate(bout.size() + 36)); // size
		rf.write("WAVE".getBytes("Latin1"));
		rf.write("fmt ".getBytes("Latin1"));
		rf.writeInt(rotate(16));
		rf.writeShort(rotateShort(1)); // audioformat
		rf.writeShort(rotateShort(1)); // channels
		rf.writeInt(rotate(22050)); // samplerate
		rf.writeInt(rotate(22050)); // byterate
		rf.writeShort(rotateShort(1)); // block alignment
		rf.writeShort(rotateShort(8)); // bytes per sample
		rf.write("data".getBytes("Latin1"));
		rf.writeInt(rotate(bout.size()));
		for (byte b : bout.toByteArray()) {
			rf.write(b + 128);
		}
		rf.close();
	}

}
