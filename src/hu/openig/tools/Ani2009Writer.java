/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.core.Func1E;
import hu.openig.core.Pair;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Utility class which creates the new ANI 2009 formatted videos from sequences of BufferedImages
 * provided by a callback.
 * @author akarnokd, 2012.10.22.
 */
public class Ani2009Writer implements Closeable {
    /** The frames per second. */
    double fps;
    /** The total frame count. */
    int frameCount;
    /** The frame callback. */
    Func1E<Integer, BufferedImage, IOException> getFrame;
    /**
     * The output stream.
     */
    DataOutputStream bout;
    /**
     * Constructor. Initializes the fields.
     * @param output the output file
     * @param fps the framerate
     * @param frameCount the frame count
     * @param getFrame the callback to get the nth frame
     * @throws IOException on error
     */
    public Ani2009Writer(
            File output,

            double fps,

            int frameCount,

            Func1E<Integer, BufferedImage, IOException> getFrame) throws IOException {
        bout = new DataOutputStream(new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(output))));
        this.fps = fps;
        this.frameCount = frameCount;
        this.getFrame = getFrame;
    }
    /**
     * Execute the conversion loop.
     * @throws IOException on error
     */
    public void run() throws IOException {
        BufferedImage i0 = getFrame.invoke(0);

        // header
        bout.writeInt(Integer.reverseBytes(i0.getWidth()));
        bout.writeInt(Integer.reverseBytes(i0.getHeight()));
        bout.writeInt(Integer.reverseBytes(frameCount));
        bout.writeInt(Integer.reverseBytes((int)(fps * 1000)));

        Pair<int[], Map<Integer, Integer>> uc = uniqueColors(i0);
        writePalette(bout, uc.first, uc.second.size());

        bout.write('I');
        byte[] b0 = colorToPalette(i0, uc.second);
        bout.write(b0);

        if (frameCount > 1) {
            for (int idx = 1; idx < frameCount; idx++) {
                System.out.printf("Frame: %d ", idx);
                BufferedImage i2 = getFrame.invoke(idx);
                if (i2 == null) {
                    System.out.printf(" no more%n");
                    break;
                }

                Pair<int[], Map<Integer, Integer>> uc2 = uniqueColors(i2);

                if (!Arrays.equals(uc.first, uc2.first)) {
                    writePalette(bout, uc2.first, uc2.second.size());
                }

                byte[] b2 = colorToPalette(i2, uc2.second);

                bout.write('I');

                for (int i = 0; i < b2.length; i++) {
                    int p0 = b0[i] & 0xFF;
                    int p1 = b2[i] & 0xFF;

                    int c0 = uc.first[p0];
                    int c1 = uc2.first[p1];
                    if (c0 == c1) {
                        bout.writeByte(255);
                    } else {
                        bout.writeByte(p1);
                    }
                }

                uc = uc2;
                b0 = b2;
                System.out.printf(" done.%n");
            }
        }

        // end of data
        bout.write('X');

    }
    /**
     * Convert the given RGBA image into an array of 8-bit palette-based image.
     * @param img the image
     * @param pal the palette
     * @return the bytes
     */
    static byte[] colorToPalette(BufferedImage img, Map<Integer, Integer> pal) {
        int[] pxs = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        byte[] result = new byte[pxs.length];
        for (int i = 0; i < pxs.length; i++) {
            int c = pxs[i];
            int b = pal.get(c);
            result[i] = (byte)b;
        }
        return result;
    }
    /**
     * Write the palette settings.
     * @param bout the data output
     * @param palette the RGBA palette

     * @param n the number of entries
     * @throws IOException on error
     */
    static void writePalette(DataOutput bout, int[] palette, int n) throws IOException {
        bout.write('P');
        bout.writeByte(n);
        for (int i = 0; i < n; i++) {
            int c = palette[i];

            bout.writeByte((c & 0xFF0000) >> 16);
            bout.writeByte((c & 0xFF00) >> 8);
            bout.writeByte((c & 0xFF));
        }
    }
    /**
     * Extract the unique colors into a palette.
     * @param img the image
     * @return the palette and maximum color count
     */
    static Pair<int[], Map<Integer, Integer>> uniqueColors(BufferedImage img) {
        int[] pxs = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
        Map<Integer, Integer> colors = new HashMap<>();
        int[] result = new int[256];

        for (int c : pxs) {
            if (!colors.containsKey(c)) {
                int idx = colors.size();
                colors.put(c, idx);
                result[idx] = c;
            }
        }
        return Pair.of(result, colors);
    }
    @Override
    public void close() throws IOException {
        bout.close();
    }
}
