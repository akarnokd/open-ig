/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools.ani;

import hu.openig.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Class to parse and decode the Imperium Galactica's .ANI
 * animation files.
 * @author karnokd, 2009.01.11.
 * @version $Revision 1.0$
 */
public class SpidyAniFile {
	/**
	 * Class to store block data.
	 * @author karnokd, 2009.01.11.
	 * @version $Revision 1.0$
	 */
	public abstract static class Block {
		/** The block data bytes. */
		public byte[] data;
	}
	/**
	 * Class to represent Data blocks. The <code>type</code> field contains "Data".
	 * @author karnokd, 2009.01.11.
	 * @version $Revision 1.0$
	 */
	public static class Data extends Block {
		/** The image width of the current block. */
		public int width;
		/** The image height of the current block. */
		public int height;
		/** The optional buffer size for an LZSS decompression. */
		public int bufferSize;
		/** Indicator if the file contained the special size marker. */
		public boolean specialFrame;
	}
	/**
	 * Class to represent a sound block. The <code>type</code> field contains "Hang"
	 * (which btw. means sound in hungarian).
	 * @author karnokd, 2009.01.11.
	 * @version $Revision 1.0$
	 */
	public static class Sound extends Block {
		
	}
	/**
	 * Class to represent palette block. The <code>type</code> field contains "Pal ".
	 * @author karnokd, 2009.01.11.
	 * @version $Revision 1.0$
	 */
	public static class Palette extends Block implements PaletteDecoder {
		/** The translated RGB values. */
		private int[] rgb = new int[256];
		/**
		 * Convert the byte data to rgb ints.
		 */
		public void map() {
			for (int i = 0; i < rgb.length; i++) {
				rgb[i] = 0xFF000000 | (data[i * 3] & 0xFF) << 18 
				| (data[i * 3 + 1] & 0xFF) << 10 
				| (data[i * 3 + 2] & 0xFF) << 2;
			}
		}
		/**
		 * Returns the given palette entry as an RGBA color. The palette is expected
		 * to store RGB values in a 6 bit form.
		 * @param index the index of the color
		 * @return the RGBA value of the color
		 */
		@Override 
		public int getColor(int index) {
			return rgb[index];
		}
	}
	/**
	 * Describes the image compression algorithm.
	 * @author karnokd, 2009.01.11.
	 * @version $Revision 1.0$
	 */
	public static enum Algorithm {
		/** Use RLE algorithm No 1 (e.g codes between 0..127 are considered literals). */
		RLE_TYPE_1,
		/** Use RLE algorithm No 2 (e.g. codes between 0..127 are considered copy following N literals). */
		RLE_TYPE_2
    }
	/** Palette type block. */
	public static final String PAL_BLOCK = "Pal ";
	/** Data type block. */
	public static final String DATA_BLOCK = "Data";
	/** Sound type block. */
	public static final String SOUND_BLOCK = "Hang";
	/** The pallet block length. */
	private static final int PAL_BLOCK_LENGTH = 768;
	/** The fixed length of a sound block. */
	private static final int SOUND_BLOCK_LENGTH = 0x4F6;
	/** The data input stream to use. */
	private DataInputStream in;
	/** The data is pre-compressed with LZSS? */
	private boolean lzssUsed;
	/** The data is compressed? */
	private boolean compressed;
	/** Which algorithm to use to uncompress the final image. */
	private Algorithm algorithm;
	/** The magic string. */
	private String magic;
	/** The magic string at the beginning of the file to indicate the type. */
	public static final String MAGIC_STRING = "SpidyAni";
	/** The file version. */
	private int version;
	/** The image width. */
	private int width;
	/** The image height. */
	private int height;
	/** The number of frames. */
	private int frameCount;
	/** Indicate if the Data blocks contain only a part for each frame (because of the 64KB limit). */
	private boolean partialData;
	/** Unknown field after the height. */
	private int languageCode;
	/** Number of bytes in the sound block. */
	private int soundSize;
	/**
	 * Use the given DataInputStream as the data source.
	 * @param in the DataInputStream object
	 */
	public void open(InputStream in) {
		this.in = new DataInputStream(new BufferedInputStream(in));
	}
	/** 
	 * Loads the ANI header bytes. 
	 * @throws IOException if
	 * <ul> 
	 * <li>the underlying stream does not hold 
	 * enough bytes to load the 19 byte header,</li>
	 * <li>the datastream does not start with the <code>MAGIC_STRING</code>,</li>
	 * <li>the version number is not 2,</li>
	 * <li>the compression flag is not set</li>
	 * </ul>
	 */
	public void load() throws IOException {
		byte[] buffer = new byte[19];
		in.readFully(buffer);
		magic = new String(buffer, 0, 8, "ISO-8859-1");
		if (!MAGIC_STRING.equals(magic)) {
			throw new IOException(String.format("Magic string mismatch. Expected '%s' got '%s'", MAGIC_STRING, magic));
		}
		version = (buffer[8] & 0xFF) - 0x30;
		if (version != 2) {
			throw new IOException(String.format("Unsupported version: %d", version));
		}
		int flags = buffer[9] & 0xFF | (buffer[10] & 0xFF) << 8;
		lzssUsed = (flags & 0x20) != 0;
		compressed = (flags & 0x01) != 0;
		if (!compressed) {
			throw new IOException("Unsupported format: uncompressed?");
		}
		algorithm = (flags & 0x04) != 0 ? Algorithm.RLE_TYPE_2 : Algorithm.RLE_TYPE_1;
		frameCount = buffer[11] & 0xFF | (buffer[12] & 0xFF) << 8;
		width = buffer[13] & 0xFF | (buffer[14] & 0xFF) << 8;
		height = buffer[15] & 0xFF | (buffer[16] & 0xFF) << 8;
		partialData = width * height > 65536;
		languageCode = buffer[17] & 0xFF | (buffer[18] & 0xFF) << 8;
		soundSize = 0;
	}
	/**
	 * Returns the next block from the input stream.
	 * @return the next block from the input stream
	 * @throws IOException if
	 * <ul>
	 * <li>the input stream unexpectedly terminates</li>
	 * <li>the detected block type is unsupported</li>
	 * </ul>
	 */
	public Block next() throws IOException {
		// load next block marker
		byte[] entry = new byte[4];
		in.readFully(entry);
		String entryStr = new String(entry, "ISO-8859-1");
        switch (entryStr) {
        case PAL_BLOCK: {
            Palette block = new Palette();
            block.data = new byte[PAL_BLOCK_LENGTH];
            in.readFully(block.data);
            block.map();
            return block;
        }
        case SOUND_BLOCK: {
            Sound block = new Sound();
            block.data = new byte[SOUND_BLOCK_LENGTH];
            in.readFully(block.data);
            soundSize += SOUND_BLOCK_LENGTH;
            return block;
        }
        case DATA_BLOCK: {
            Data block = new Data();
            if (lzssUsed) {
                // if lzss is used, the data block contains a lzssLen, len, width, height values
                block.bufferSize = readWord();
            }
            int len = readWord();
            block.width = readWord();
            block.height = readWord();
            // handle special case of len
            if (len == 0xFFFF) {
                // the following data is not LZSS compressed
                block.specialFrame = true;
                len = block.bufferSize;
            }
            block.data = new byte[len];
            in.readFully(block.data);
            return block;
        }
        default:
        }
		throw new IOException(String.format("Unsupported block: %s", entryStr));
	}
	/**
	 * Read a little endian word from the input stream.
	 * @return the read word value of 0 to 65535
	 * @throws IOException if The underlying input stream throws exception
	 */
	private int readWord() throws IOException {
		return (in.readByte() & 0xFF) | (in.readByte() & 0xFF) << 8;
	}
	/**
	 * Returns the width of the animation in pixels.
	 * @return the width of the animation in pixels
	 */
	public int getWidth() {
		return width;
	}
	/**
	 * Returns the height of the animation in pixels.
	 * @return the height of the animation in pixels
	 */
	public int getHeight() {
		return height;
	}
	/**
	 * Returns the number of frames in the animation.
	 * @return the number of frames in the animation
	 */
	public int getFrameCount() {
		return frameCount;
	}
	/**
	 * Returns true, if the blocks contain only a part of the entire image of the frames.
	 * @return true, if the blocks contain only a part of the entire image of the frames
	 */
	public boolean isPartialData() {
		return partialData;
	}
	/**
	 * Returns the compression algorithm used to encode the actual image.
	 * @return the compression algorithm used to encode the actual image
	 */
	public Algorithm getAlgorithm() {
		return algorithm;
	}
	/**
	 * Returns true if the data blocks need to be LZSS
	 * decompressed before trying the actual RLE decompression.
	 * @return true if the data blocks need LZSS decompression first
	 */
	public boolean isLZSS() {
		return lzssUsed;
	}
	/**
	 * Returns the possibly language code field value. 1=English, 2=Hungarian, etc.
	 * @return the language field value
	 */
	public int getLanguageCode() {
		return languageCode;
	}
	/**
	 * Returns the number of bytes the sound occupies.
	 * @return the number of bytes the sound occupies
	 */
	public int getSoundSize() {
		return soundSize;
	}
	/**
	 * Walks all blocks within the file.
	 * @throws IOException if the underlying IO stream throws it
	 */
	public void walkBlocks() throws IOException {
		byte[] entry = new byte[4];
		try {
			while (true) {
				// load next block marker
				in.readFully(entry);
				String entryStr = new String(entry, "ISO-8859-1");
                switch (entryStr) {
                case PAL_BLOCK:
                    IOUtils.skipFully(in, PAL_BLOCK_LENGTH);
                    break;
                case SOUND_BLOCK:
                    IOUtils.skipFully(in, SOUND_BLOCK_LENGTH);
                    soundSize += SOUND_BLOCK_LENGTH;
                    break;
                case DATA_BLOCK:
                    int bufferSize = 0;
                    if (lzssUsed) {
                        // if lzss is used, the data block contains a lzssLen, len, width, height values
                        bufferSize = readWord();
                    }
                    int len = readWord();
                    //				readWord();
                    //				readWord();
                    // handle special case of len
                    if (len == 0xFFFF) {
                        // the following data is not LZSS compressed
                        len = bufferSize;
                    }
                    IOUtils.skipFully(in, len + 4);
                    break;
                default:
                }
			}
		} catch (EOFException ex) {
			// ignored
		}
	}
	/**
	 * Returns the frames per second to play this file.
	 * @return the frames per second to play this file
	 */
	public double getFPS() {
		return soundSize > 0 ? (frameCount + 0) * 22050.0 / soundSize : 17.89; 
	}
}
