/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.PCXImage;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

/**
 * Utility class to process some of the original images.
 * @author akarnokd, Jul 30, 2011
 */
public final class TransformImage {
    /** Utility class. */
    private TransformImage() { }
    /**

     * Main program.
     * @param args no arguments
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {

        ImageIO.write(PCXImage.from("c:\\games\\IG\\SP_WAR.PAC ION300X1.PCX" , -2), "png", new File("meson_projectile_matrix.png"));

        BufferedImage[] bimg = {
            PCXImage.from("c:\\games\\IG\\SP_WAR.PAC CRG200X1.PCX" , -2),
            PCXImage.from("c:\\games\\IG\\SP_WAR.PAC CRG200X2.PCX" , -2),
            PCXImage.from("c:\\games\\IG\\SP_WAR.PAC CRG200X3.PCX" , -2),
        };

        int w = 50;

        BufferedImage img2 = new BufferedImage(64 * w, w, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = img2.createGraphics();

        int k = 0;
        int j = 0;
        for (int i = 0; i < 64; i++) {
            BufferedImage src = bimg[k];
            if (j * w >= src.getHeight()) {
                j = 0;
                k++;
                src = bimg[k];
            }
            g2.drawImage(src, i * w, 0, i * w + w - 1, w - 1, 0, j * w, w - 1, j * w + w - 1, null);
            j++;
        }

        g2.dispose();

        ImageIO.write(img2, "png", new File("traders_freight_2.png"));
    }

}
