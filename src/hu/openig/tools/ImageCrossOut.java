/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.imageio.ImageIO;

/**
 * Cross out every image under a directory. Used to visually detect missin language specific images.
 * @author akarnokd, 2012.10.22.
 */
public final class ImageCrossOut {
    /** Utility class. */
    private ImageCrossOut() { }

    /**
     * @param args no arguments
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {
        final Path base = Paths.get("images/fr");
        Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs) throws IOException {
                String fn = file.toString().toLowerCase();
                if (fn.endsWith(".png")) {
                    BufferedImage img = ImageIO.read(file.toFile());
                    BufferedImage img2 = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2 = img2.createGraphics();
                    g2.drawImage(img, 0, 0, null);
                    g2.setColor(Color.RED);
                    g2.drawLine(0, 0, img.getWidth() - 1, img.getHeight() - 1);
                    g2.drawLine(img.getWidth() - 1, 0, 0, img.getHeight() - 1);
                    g2.dispose();

                    ImageIO.write(img2, "png", file.toFile());
                }
                return FileVisitResult.CONTINUE;
            }
        });

    }

}
