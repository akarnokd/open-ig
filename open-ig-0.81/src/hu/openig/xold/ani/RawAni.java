/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.xold.ani;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * The direct access raw animation file.
 * @author karnokd, 2009.09.18.
 * @version $Revision 1.0$
 */
public class RawAni implements Closeable {
	/** The random access file. */
	RandomAccessFile raf;
	/** The image width. */
	int width;
	/** The image height. */
	int height;
	/** The number of frames. */
	int frames;
	/** The frame rate per second. */
	double fps;
	/** The input buffer for the frame data. */
	byte[] data;
	/**
	 * Constructor. Opens the file.
	 * @param fileName the file name
	 * @throws IOException on error
	 */
	public RawAni(String fileName) throws IOException {
		raf = new RandomAccessFile(fileName, "r");
		width = raf.readShort();
		height = raf.readShort();
		frames = raf.readShort();
		fps = raf.readShort() / 1000.0;
		data = new byte[width * height * 4];
	}
	/**
	 * @return the image width
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * @return the image height
	 */
	public int getHeight() {
		return height;
	}
	/**
	 * @return the number of frames
	 */
	public int getFrames() {
		return frames;
	}
	/**
	 * @return the frames per second
	 */
	public double getFPS() {
		return fps;
	}
	/**
	 * Read the indexth frame.
	 * @param index the index of the frame
	 * @param pixels the output RGBA pixel array of length width * height
	 * @throws IOException on error
	 */
	public void readFrame(int index, int[] pixels) throws IOException {
		raf.seek(8 + index * width * height * 4);
		raf.readFully(data);
		for (int i = 0; i < data.length / 4; i++) {
			for (int j = 0; j < 4; j++) {
				pixels[i] = (pixels[i] << 8) | (data[i * 4 + j] & 0xFF); 
			}
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override 
	public void close() throws IOException {
		raf.close();
	}
}
