/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;

import javax.imageio.ImageIO;

/**
 * Draw a black border around text in the research labels.
 * @author akarnokd, 2012.06.23.
 */
public final class TechLabelColorizer {
    /** Utility class. */
    private TechLabelColorizer() { }
    /**
     * @param args none
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {
        File[] files = new File("images/fr/research").listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith("label_") && name.endsWith("_selected.png");
            }
        });
        if (files == null) {
            return;
        }
        for (File f : files) {
            BufferedImage img = ImageIO.read(f);

            int w = img.getWidth();
            int h = img.getHeight();

            int xmin = Integer.MAX_VALUE;
            int ymin = Integer.MAX_VALUE;
            int xmax = 0;
            int ymax = 0;
            // remove non-orange pixels
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int c = img.getRGB(x, y);
                    if (c != 0xFFFFBE00) {
                        img.setRGB(x, y, 0);
                    } else {
                        xmin = Math.min(xmin, x);
                        ymin = Math.min(ymin, y);
                        xmax = Math.max(xmax, x);
                        ymax = Math.max(ymax, y);
                    }
                }
            }

            int nw = xmax - xmin + 3;
            int nh = h;
            if (ymin == 0) {
                nh++;
            }
            if (ymax == h - 1) {
                nh++;
            }
            BufferedImage img2 = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = img2.createGraphics();
            g2.drawImage(img, 1 - xmin, ymin == 0 ? 1 : 0, null);
            g2.dispose();

            // set a pixel black if any of the four neighbors is orange
            for (int y = 0; y < nh; y++) {
                for (int x = 0; x < nw; x++) {
                    int c = img2.getRGB(x, y);
                    int c0 = get(img2, x - 1, y);
                    int c1 = get(img2, x + 1, y);
                    int c2 = get(img2, x, y - 1);
                    int c3 = get(img2, x, y + 1);

                    if ((c & 0xFF000000) == 0

                            && (c0 == 0xFFFFBE00

                            || c1 == 0xFFFFBE00
                            || c2 == 0xFFFFBE00
                            || c3 == 0xFFFFBE00)) {
                        img2.setRGB(x, y, 0xFF000000);
                    }
                }
            }

            ImageIO.write(img2, "png", f);

            File f2 = new File(f.getAbsolutePath().replace("_selected", ""));

            for (int y = 0; y < nh; y++) {
                for (int x = 0; x < nw; x++) {
                    int c = img2.getRGB(x, y);
                    if (c == 0xFF000000) {
                        img2.setRGB(x, y, 0x00000000);
                    } else
                    if (c == 0xFFFFBE00) {
                        img2.setRGB(x, y, 0xFF000000);
                    }
                }
            }

            ImageIO.write(img2, "png", f2);
        }
    }
    /**
     * Return a pixel color or 0 if X, Y is outside the image.
     * @param img the image
     * @param x the X coordinate
     * @param y the Y coordinate
     * @return the color
     */
    static int get(BufferedImage img, int x, int y) {
        if (x < 0 || y < 0 || x >= img.getWidth() || y >= img.getHeight()) {
            return 0;
        }
        return img.getRGB(x, y);
    }
}
