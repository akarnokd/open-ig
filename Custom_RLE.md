
# Introduction #

This page describes the two custom [RLE](http://en.wikipedia.org/wiki/RLE) decompression used in the second phase of the animation's data decompression.

# Details #

The difference to the classic RLE is that the Imperium Galactica's implementation uses SKIP codes to skip large portion of the output stream. This allows to store only the colors which have changed since the last frame.

There is two of this RLE algorithm in which the difference lies how many uncompressable literals needs to be described in the compressed file. Algorithm 1 uses byte values 0 to 127 to indicate: put one pixel of that value into the output stream; Algorithm 2 used byte values 0 to 127 to indicate: copy N bytes as pixels into the output stream.

The ANI file's flags field determines which decoding to apply within that particular file. Both algorithm requires a 768 bytes 6-6-6 bit RGB palette. The output is in 32 bit RGBA format which can be later used on [BufferedImage.setRGB()](http://java.sun.com/javase/6/docs/api/java/awt/image/BufferedImage.html).

## The first RLE algorithm ##
```
public static int getColor(byte[] pal, int c) {
	return 0xFF000000 | (pal[c * 3 + 0] & 0xFF) << 18 | (pal[c * 3 + 1] & 0xFF) << 10 | (pal[c * 3 + 2] & 0xFF) << 2;
}
public static void decompressRLE1(byte[] buffer, byte[] pal, int[] img) {
	int src = 0;
	int dst = 0;
	while (src < buffer.length && dst < img.length) {
		int c = buffer[src++] & 0xFF;
		if (c >= 0xC0) {
			if (c == 0xC0) {
				int x = (buffer[src++] & 0xFF) | (buffer[src++] & 0xFF) << 8;
				c = buffer[src++] & 0xFF;
				Arrays.fill(img, dst, dst + x, getColor(pal, c));
				dst += x;
			} else {
				int x = c & 0x3F;
				c = buffer[src++] & 0xFF;
				Arrays.fill(img, dst, dst + x, getColor(pal, c));
				dst += x;
			}
		} else
		if (c >= 0x80) {
			if (c == 0x80) {
				int x = (buffer[src++] & 0xFF) | (buffer[src++] & 0xFF) << 8;
				dst += + x;
			} else {
				dst += c & 0x3F;
			}
		} else {
			img[dst++] = getColor(pal, c);
		}
	}
}

```
## The second RLE algorithm ##
```
public static int getColor(byte[] pal, int c) {
	return 0xFF000000 | (pal[c * 3 + 0] & 0xFF) << 18 | (pal[c * 3 + 1] & 0xFF) << 10 | (pal[c * 3 + 2] & 0xFF) << 2;
}
public static void decompressRLE2(byte[] buffer, byte[] pal, int[] img) {
	int dst = 0;
	while (src < buffer.length && dst < img.length) {
		int c = buffer[src++] & 0xFF;
		if (c >= 0xC0) {
			if (c == 0xC0) {
				int x = (buffer[src++] & 0xFF) | (buffer[src++] & 0xFF) << 8;
				c = buffer[src++] & 0xFF;
				Arrays.fill(img, dst, dst + x, getColor(pal, c));
				dst += x;
			} else {
				int x = c & 0x3F;
				c = buffer[src++] & 0xFF;
				Arrays.fill(img, dst, dst + x, getColor(pal, c));
				dst += x;
			}
		} else
		if (c >= 0x80) {
			if (c == 0x80) {
				int x = (buffer[src++] & 0xFF) | (buffer[src++] & 0xFF) << 8;
				dst += x;
			} else {
				dst += c & 0x3F;
			}
		} else
		if (c == 0) {
			int x = (buffer[src++] & 0xFF) | (buffer[src++] & 0xFF) << 8;
			for (int i = 0; i < x; i++) {
				c = buffer[src++] & 0xFF;
				img[dst++] = getColor(pal, c);
			}
		} else {
			int x = c;
			for (int i = 0; i < x; i++) {
				c = buffer[src++] & 0xFF;
				img[dst++] = getColor(pal, c);
			}
		}
	}
}
```