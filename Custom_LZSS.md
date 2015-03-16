
# Introduction #

This page describes the custom [LZSS](http://en.wikipedia.org/wiki/LZSS) decompression algorithm used in the Imperium Galactica ANI files. It was (up to day) the most challenging information to obtain.

# Details #
The custom thing about this decompressor is that it stores the code/literal flags in one byte followed by the actual literals/codes. Each byte's 8 bits tell which of the following bytes to consider literal or code. The least significant bit describes the first following byte. The sliding window is 4096 bytes long and the length field is 4 bits long and the actual repetition count must be biased by 3. Note that the algorithm doesn't include a sign for the End of File, the length of the data array must be correct.

Here is a Java source for a method which decompresses this custom LZSS binary data.

```
public static void DecompressLZSS(byte[] data, byte[] out) {
	int marker = 0;
	int src = 0;
	int dst = 0;
	int nextChar = 0xFEE;
	final int WINDOW_SIZE = 4096;
	byte[] slidingWindow = new byte[WINDOW_SIZE];
	while (src < data.length) {
		marker = data[src++] & 0xFF;
		for (int i = 0; i < 8 && src < data.length; i++) {
			boolean type = (marker & (1 << i)) != 0;
			if (type) {
				byte d = data[src++];
				out[dst++] = d;
				slidingWindow[nextChar] = d;
				nextChar = (nextChar + 1) % WINDOW_SIZE;
			} else {
				int offset = data[src++] & 0xFF;
				int len = data[src++] & 0xFF;
				offset = offset | (len & 0xF0) << 4;
				len = (len & 0x0F) + 3;
				for (int j = 0; j < len; j++) {
					byte d = slidingWindow[(offset + j) % WINDOW_SIZE];
					out[dst++] = d;
					slidingWindow[nextChar] = d;
					nextChar = (nextChar + 1) % WINDOW_SIZE;
				}
			}
		}
	}
}

```