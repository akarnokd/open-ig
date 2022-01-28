/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.core.Func1E;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Utility class to take a sequence of PNG files and compose an
 * ani2009 type video file.
 */
public final class PNGToVideo {
    /** Utility class. */
    private PNGToVideo() { }
    /**
     * Main program.
     * @param args no arguments
     * @throws Exception on error
     */
    public static void main(String[] args) throws Exception {
//        int count = 41;
//        double fps = 17.89;
//

//        String[] mode = { "appear", "open", "close" };
//        String[] mode2 = { "0", "", "F" };
//

//        for (int l = 1; l < 4; l++) {
//            for (int m = 0; m < 3; m++) {
//                String filename = "c:/Games/IGDE/1/" + l + "_HID" + mode2[m] + ".ANI-%05d.PNG";
//                String output = "video/de/bridge/messages_" + mode[m] + "_level_" + l + ".ani.gz";
//

//                convert(filename, count, fps, output);
//            }
//        }
        convert("e:/temp/dipfej9_%03d.png", 0, 122, 17.89, "video/generic/diplomacy/dipfej9.ani.gz");
    }
    /**
     * Convert a sequence of PNG files into a video.
     * @param filename the filename pattern
     * @param start the start index
     * @param count the number of frames
     * @param fps the framerate
     * @param output the output file
     * @throws IOException on error
     */
    public static void convert(final String filename, int start, int count, double fps,
            String output) throws IOException {

        try (Ani2009Writer out = new Ani2009Writer(new File(output), fps, count, new Func1E<Integer, BufferedImage, IOException>() {
            @Override
            public BufferedImage invoke(Integer value) throws IOException {
                return get(filename, value);
            }
        })) {
            out.run();
        }
    }
    /**
     * Read the given image file.
     * @param format the file pattern.
     * @param index the index
     * @return the image
     * @throws IOException on error
     */
    static BufferedImage get(String format, int index) throws IOException {
        File f = new File(String.format(format, index));
        if (f.canRead()) {
            return ImageIO.read(f);
        }
        return null;
    }
}
