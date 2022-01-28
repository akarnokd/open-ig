/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools.ani;

/**
 * Simple utility class to decompress an LZSS compressed data.
 * @author karnokd, 2009.01.11.
 * @version $Revision 1.0$
 */
public final class LZSS {
    /** Private constructor. */
    private LZSS() {
        throw new AssertionError("Utility class!");
    }
    /**
     * Decompress the given byte array using the LZSS algorithm and
     * produce the output into the given out array.
     * @param data the compressed input data
     * @param src the index to start the decompression
     * @param out the output array to store the bytes
     * @param dst the output index into this array
     */
    public static void decompress(byte[] data, int src, byte[] out, int dst) {
        int marker;
        int nextChar = 0xFEE;
        final int windowSize = 4096;
        byte[] slidingWindow = new byte[windowSize];
        while (src < data.length) {
            marker = data[src++] & 0xFF;
            for (int i = 0; i < 8 && src < data.length; i++) {
                boolean type = (marker & (1 << i)) != 0;
                if (type) {
                    byte d = data[src++];
                    out[dst++] = d;
                    slidingWindow[nextChar] = d;
                    nextChar = (nextChar + 1) % windowSize;
                } else {
                    int offset = data[src++] & 0xFF;
                    int len = data[src++] & 0xFF;
                    offset = offset | (len & 0xF0) << 4;
                    len = (len & 0x0F) + 3;
                    for (int j = 0; j < len; j++) {
                        byte d = slidingWindow[(offset + j) % windowSize];
                        out[dst++] = d;
                        slidingWindow[nextChar] = d;
                        nextChar = (nextChar + 1) % windowSize;
                    }
                }
            }
        }
    }
}
