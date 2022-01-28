/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.core.Func1;
import hu.openig.utils.U;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Surrounds a cleared database image with a black border.
 * @author akarnokd, 2012.12.23.
 */
public final class DatabaseButtonColorer {
    /** Utility class. */
    private DatabaseButtonColorer() { }

    /**
     * @param args no arguments
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {
        for (File f : U.listFiles(new File("images/fr/database"), new Func1<File, Boolean>() {
            @Override
            public Boolean invoke(File value) {
                return value.getName().endsWith(".png");
            }
        })) {
            BufferedImage img = ImageIO.read(f);
            int[] cm = new int[8];
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int c0 = color(img, x, y);
                    if ((c0 & 0xFF000000) == 0) {

                        cm[0] = color(img, x - 1, y - 1);
                        cm[1] = color(img, x - 1, y);
                        cm[2] = color(img, x - 1, y + 1);

                        cm[3] = color(img, x + 1, y - 1);
                        cm[4] = color(img, x + 1, y);
                        cm[5] = color(img, x + 1, y + 1);

                        cm[6] = color(img, x, y - 1);
                        cm[7] = color(img, x, y + 1);

                        for (int c : cm) {
                            if ((c & 0xFF000000) != 0 && c != 0xFF000000) {
                                color(img, x, y, 0xFF000000);
                                break;
                            }
                        }

                    }
                }
            }
            ImageIO.write(img, "png", f);
        }
    }
    /**
     * Get a color at the given position or zero if outside the image.
     * @param img the image
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the current color
     */
    static int color(BufferedImage img, int x, int y) {
        if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight()) {
            return 0;
        }
        return img.getRGB(x, y);
    }
    /**
     * Get a color at the given position or zero if outside the image.
     * @param img the image
     * @param x the x coordinate
     * @param y the y coordinate
     * @param color the new color
     */
    static void color(BufferedImage img, int x, int y, int color) {
        if (x < 0 || x >= img.getWidth() || y < 0 || y >= img.getHeight()) {
            return;
        }
        img.setRGB(x, y, color);
    }
}
