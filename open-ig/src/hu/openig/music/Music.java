package hu.openig.music;

import hu.openig.utils.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Music {
	/**
	 * Replaces the array with a moving difference values. 
	 * Modifies the input array!
	 * @param data the data to differential encode
	 * @return the input array
	 */
	public static byte[] difference8(byte[] data) {
		byte start = 0;
		// replace entries with the difference
		for (int i = 0; i < data.length; i++) {
			byte val = data[i];
			data[i] = (byte)(data[i] - start);
			start = val;
		}
		return data;
	}
	/**
	 * Replaces the array with a moving sum values.
	 * Modifies the input array!
	 * @param data the differenced array
	 * @return the input array
	 */
	public static byte[] undifference8(byte[] data) {
		int start = 0;
		// un differentiate
		for (int i = 0; i < data.length; i++) {
			data[i] = (byte)(start + data[i]);
			start = data[i];
		}
		return data;
	}
	/**
	 * Compresses an 8 bit per sample audio data first using a differential coding then GZIP.
	 * @param raw the raw bytes
	 * @return the generated compressed bytes
	 */
	public static byte[] compress8(byte[] raw) {
		return compressGZIP(difference8(raw.clone()));
	}
	/**
	 * Compresses a 16 bit sample using differential and GZIP compression.
	 * @param raw the raw data to compress
	 * @return the compressed data
	 */
	public static byte[] compress16(byte[] raw) {
		return compressGZIP(difference16(raw.clone()));
	}
	/**
	 * Replaces the array values with the 16 bit difference values.
	 * Modifies the input array!
	 * @param raw the raw data
	 * @return the same as raw
	 */
	public static byte[] difference16(byte[] raw) {
		short start = 0;
		for (int i = 0; i < raw.length; i += 2) {
			short rd = (short)((raw[i] & 0xFF) | (raw[i + 1] & 0xFF) << 8);
			short rdd = (short)(rd - start); 
			raw[i] = (byte)(rdd & 0xFF);
			raw[i + 1] = (byte)((rdd >> 8)& 0xFF);
			start = rd;
		}
		return raw;
	}
	/**
	 * Decompresses a GZIP and differentially coded stream of 8 bit data
	 * @param raw the raw data to decompress
	 * @return the uncompressed data
	 */
	public static byte[] decompress8(byte[] raw) {
		return undifference8(decompressGZIP(raw));
	}
	/**
	 * Decompresses a GZIP and differentially coded stream of 16 bit data
	 * @param raw the raw data to decompress
	 * @return the uncompressed data
	 */
	public static byte[] decompress16(byte[] raw) {
		return undifference16(decompressGZIP(raw));
	}
	/**
	 * Undifferences the given array of 16 bit values.
	 * Modifies the input array!
	 * @param data the data to undifference
	 * @return same as data
	 */
	public static byte[] undifference16(byte[] data) {
		int start = 0;
		// un differentiate
		for (int i = 0; i < data.length; i += 2) {
			short rd = (short)((data[i] & 0xFF) | (data[i + 1] & 0xFF) << 8);
			short rdd = (short)(rd + start); 
			data[i] = (byte)(rdd & 0xFF);
			data[i + 1] = (byte)((rdd >> 8) & 0xFF);
			start = rdd;
		}
		return data;
	}
	/**
	 * Compress the data using GZIP compression.
	 * @param raw the raw data to compress
	 * @return the compressed bytes
	 */
	public static byte[] compressGZIP(byte[] raw) {
		try {
			ByteArrayOutputStream bout = new ByteArrayOutputStream(4096);
			GZIPOutputStream gout = new GZIPOutputStream(bout);
			gout.write(raw);
			gout.finish();
			gout.close();
			return bout.toByteArray();
		} catch (IOException ex) {
			throw new AssertionError("IO Exception on a ByteArrayOutputStream?");
		}
	}
	/**
	 * Decompress the data using GZIP decompression.
	 * @param raw the raw data to decompress
	 * @return the decompressed bytes
	 */
	public static byte[] decompressGZIP(byte[] raw) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream(4096);
		try {
			GZIPInputStream gin = new GZIPInputStream(new ByteArrayInputStream(raw));
			byte[] buffer = new byte[4096];
			int read;
			do {
				read = gin.read(buffer);
				if (read > 0) {
					bout.write(buffer, 0, read);
				}
			} while (read >= 0);
			return bout.toByteArray();
		} catch (IOException ex) {
			throw new AssertionError("IO Exception on a ByteArrayOutputStream?");
		}
	}
	/**
	 * Interleaves the raw data by putting each even bytes to the beginning of the
	 * result, then each odd bytes.
	 * Modifies the input array!
	 * @param raw the raw data to interleave
	 * @return the interleaved data
	 */
	public static byte[] interleave16(byte[] raw) {
		byte[] data = new byte[raw.length];
		int half = raw.length / 2;
		int j = 0;
		for (int i = 0; i < data.length; i += 2) {
			data[j] = raw[i];
			data[half + j] = raw[i + 1];
			j++;
		}
		return data;
	}
	/**
	 * Uninterleaves the data interleaved by interleave16().
	 * Modifies the input array!
	 * @param raw the data to uninterleave
	 * @return the uninterleaved data
	 */
	public static byte[] uninterleave16(byte[] raw) {
		byte[] data = new byte[raw.length];
		int half = raw.length / 2;
		int j = 0;
		for (int i = 0; i < data.length; i += 2) {
			data[i] = raw[j];
			data[i + 1] = raw[j + half];
			j++;
		}
		return data;
	}
	/**
	 * Run-length encode the given array of bytes using the simple count based version.
	 * @param raw the raw data
	 * @return the encoded version
	 */
	public static byte[] rle(byte[] raw) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream(4096);
		int src = 0;
		while (src < raw.length) {
			int b = raw[src] & 0xFF;
			// check if there are more than 3 subsequent
			int count = 0;
			for (int i = src; i < raw.length; i++) {
				int lb = raw[i] & 0xFF;
				if (lb == b && count < 65535) {
					count++;
				} else {
					break;
				}
			}
			if (count < 2) {
				// find out how many different values are in a succession
				int lb = b;
				for (int i = src + 1; i < raw.length; i++) {
					b = raw[i] & 0xFF;
					if (b != lb && count < 65535) {
						count++;
						lb = b;
					} else {
						count--;
						break;
					}
				}
				if (count < 128) {
					bout.write(0x80 + count);
				} else {
					bout.write(0x80);
					bout.write(count & 0xFF);
					bout.write((count & 0xFF00) >> 8);
				}
				for (int i = 0; i < count; i++) {
					bout.write(raw[src++] & 0xFF);
				}
			} else {
				if (count < 128) {
					bout.write(count);
				} else {
					bout.write(0x00);
					bout.write(count & 0xFF);
					bout.write((count & 0xFF00) >> 8);
				}
				bout.write(b);
				src += count;
			}
		}
		return bout.toByteArray();
	}
	/**
	 * Uncompress the data using a simple run-length encoding algorithm.
	 * @param raw the data to uncompress
	 * @return the uncompressed data
	 */
	public static byte[] unRle(byte[] raw) {
		ByteArrayOutputStream bout = new ByteArrayOutputStream(4096);
		int src = 0;
		while (src < raw.length) {
			int b = raw[src++] & 0xFF;
			int count = 0;
			if (b >= 0x80) {
				if (b == 0x80) {
					count = raw[src++] & 0xFF | (raw[src++] & 0xFF) << 8;
				} else {
					count = b & 0x7F;
				}
				for (int i = 0; i < count; i++) {
					bout.write(raw[src++] & 0xFF);
				}
			} else {
				if (b == 0) {
					count = raw[src++] & 0xFF | (raw[src++] & 0xFF) << 8;
				} else {
					count = b & 0x7F;
				}
				int c = raw[src++] & 0xFF;
				for (int i = 0; i < count; i++) {
					bout.write(c);
				}
			}
			
		}
		return bout.toByteArray();
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		byte[] orig = IOUtils.load("gt_title16.wav");
		byte[] sound = Arrays.copyOfRange(orig, 0x2C, orig.length);
		System.out.printf("Original: %d%n", sound.length);
		byte[] comp;
		byte[] rest;
		long time;
//		time = System.nanoTime();
//		comp = compress8(sound);
//		System.out.printf("Compress8: %d in %d ms, %.2f%%%n", comp.length, (System.nanoTime() - time) / 1000000, comp.length * 100.0 / sound.length);
//		
//		time = System.nanoTime();
//		rest = decompress8(comp);
//		System.out.printf("Decompress8: %d in %d ms%n", rest.length, (System.nanoTime() - time) / 1000000);
//		if (!Arrays.equals(sound, rest)) {
//			System.err.println("Differs!");
//		}
//
//		time = System.nanoTime();
//		comp = compress16(sound);
//		System.out.printf("Compress16: %d in %d ms, %.2f%%%n", comp.length, (System.nanoTime() - time) / 1000000, comp.length * 100.0 / sound.length);
//		
//		time = System.nanoTime();
//		rest = decompress16(comp);
//		System.out.printf("Decompress16: %d in %d ms%n", rest.length, (System.nanoTime() - time) / 1000000);
//		if (!Arrays.equals(sound, rest)) {
//			System.err.println("Differs!");
//		}
//		
//		time = System.nanoTime();
//		comp = compressGZIP(sound);
//		System.out.printf("CompressGZIP: %d in %d ms, %.2f%%%n", comp.length, (System.nanoTime() - time) / 1000000, comp.length * 100.0 / sound.length);
//		
//		time = System.nanoTime();
//		rest = decompressGZIP(comp);
//		System.out.printf("DecompressGZIP: %d in %d ms%n", rest.length, (System.nanoTime() - time) / 1000000);
//		if (!Arrays.equals(sound, rest)) {
//			System.err.println("Differs!");
//		}
		
		time = System.nanoTime();
		comp = compressGZIP(interleave16(difference16(sound.clone())));
		System.out.printf("Compress16+IL: %d in %d ms, %.2f%%%n", comp.length, (System.nanoTime() - time) / 1000000, comp.length * 100.0 / sound.length);
		
		time = System.nanoTime();
		rest = undifference16(uninterleave16(decompressGZIP(comp)));
		System.out.printf("Decompress16+IL: %d in %d ms%n", rest.length, (System.nanoTime() - time) / 1000000);
		if (!Arrays.equals(sound, rest)) {
			System.err.println("Differs!");
		}
		
		time = System.nanoTime();
		comp = compressGZIP(difference8(interleave16(difference16(sound.clone()))));
		System.out.printf("Compress16+IL+D8: %d in %d ms, %.2f%%%n", comp.length, (System.nanoTime() - time) / 1000000, comp.length * 100.0 / sound.length);
		
		time = System.nanoTime();
		rest = undifference16(uninterleave16(undifference8(decompressGZIP(comp))));
		System.out.printf("Decompress16+IL+D8: %d in %d ms%n", rest.length, (System.nanoTime() - time) / 1000000);
		if (!Arrays.equals(sound, rest)) {
			System.err.println("Differs!");
		}

		time = System.nanoTime();
		comp = rle(interleave16(sound));
		System.out.printf("RLE: %d in %d ms, %.2f%%%n", comp.length, (System.nanoTime() - time) / 1000000, comp.length * 100.0 / sound.length);
		
		time = System.nanoTime();
		rest = uninterleave16(unRle(comp));
		System.out.printf("UNRLE: %d in %d ms%n", rest.length, (System.nanoTime() - time) / 1000000);
		if (!Arrays.equals(sound, rest)) {
			System.err.println("Differs!");
		}
		//compressLoop(sound);
	}
	/** Find the minimum of repeated compression loops. */
	public static void compressLoop(byte[] sound) {
		byte[] comp;
		double ratio = 1;
		byte[] inp = sound;
		int loop = 1;
		while (true) {
			  byte[] diff = difference16(inp);
			  comp = compressGZIP(diff);
			  double newRatio = comp.length / (double)inp.length;
			  System.out.printf("Compress %d: %d -> %d, %.2f%%%n", loop, inp.length, comp.length, newRatio * 100);
			  if (newRatio > 1 || (Math.abs(newRatio - ratio) < 0.001)) {
				  break;
			  }
			  ratio = newRatio;
			  // re differentiate
			  inp = diff;
			  loop++;
		}
	}

}
