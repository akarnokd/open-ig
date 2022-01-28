/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.PCXImage;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

/**
 * Figure out which concrete image came from where in the original PCX images.
 * @author akarnokd, 2012.10.22.
 */
public final class ImageMatcher {
    /** Utility class. */
    private ImageMatcher() { }
    /**
     * @param args no arguments
     * @throws Exception exception
     */
    public static void main(String[] args) throws Exception {
        final Map<String, BufferedImage> images = new LinkedHashMap<>();

        System.out.println("Scanning images");
        final Path base = Paths.get("images/hu");
        Files.walkFileTree(base, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs) throws IOException {
                String fn = file.toString().toLowerCase();
                if (fn.endsWith(".png")) {
                    String resource = base.relativize(file).toString();
                    images.put(resource, ImageIO.read(file.toFile()));
                }
                return FileVisitResult.CONTINUE;
            }
        });

        final Map<String, BufferedImage> pcxs = new LinkedHashMap<>();

        System.out.println("Scanning PCXs");
        final Path base2 = Paths.get("c:/games/IGHU");
        Files.walkFileTree(base2, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs) throws IOException {
                String fn = file.toString().toLowerCase();
                if (fn.contains("screens")) {
                    if (fn.endsWith(".pcx")) {
                        String resource = base2.relativize(file).toString();
                        pcxs.put(resource, PCXImage.from(file.toFile(), -1));
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });

        final ConcurrentMap<String, String> unused = new ConcurrentHashMap<>();
        for (String k : images.keySet()) {
            unused.put(k, k);
        }

        ExecutorService exec = Executors.newFixedThreadPool(4);

        for (final Map.Entry<String, BufferedImage> img1 : pcxs.entrySet()) {
            exec.execute(new Runnable() {
                @Override
                public void run() {
                    StringWriter sw = new StringWriter();
                    PrintWriter out = new PrintWriter(sw);
                    boolean once = true;
                    for (Map.Entry<String, BufferedImage> img2 : images.entrySet()) {
                        Rectangle coords = containsImage(img1.getValue(), img2.getValue());
                        if (coords != null) {
                            if (unused.remove(img2.getKey()) != null) {
                                if (once) {
                                    out.printf("\t<image src='%s'>%n", img1.getKey());
                                    once = false;
                                }
                                out.printf("\t\t<area coords='%d,%d,%d,%d' dst='%s'/>%n", coords.x, coords.y, coords.width, coords.height, img2.getKey());
                            }
                        }
                    }
                    if (!once) {
                        out.printf("\t</image>%n");
                        out.flush();
                        System.out.println(sw);
                    }
                }
            });
        }

        exec.shutdown();
        exec.awaitTermination(1, TimeUnit.HOURS);
        for (String s : unused.keySet()) {
            System.err.println(s);
        }
    }
    /**
     * Checks if the image contains another image.
     * @param in the image to check
     * @param what what image to find
     * @return the coordinates the what image was found
     */
    static Rectangle containsImage(BufferedImage in, BufferedImage what) {
        for (int y0 = 0; y0 < in.getHeight() - what.getHeight(); y0++) {
            for (int x0 = 0; x0 < in.getWidth() - what.getWidth(); x0++) {
                boolean found = true;
                inner:
                for (int y1 = 0; y1 < what.getHeight(); y1++) {
                    for (int x1 = 0; x1 < what.getWidth(); x1++) {
                        int c1 = in.getRGB(x0 + x1, y0 + y1) & 0xFFFFFF;
                        int c2 = what.getRGB(x1, y1);
                        int c3 = c2 & 0xFFFFFF;
                        int c4 = (c2 & 0xFF000000) >> 24;
                        if (c1 != c3 || (c1 == 0 && c4 == 0)) {
                            found = false;
                            break inner;
                        }
                    }
                }
                if (found) {
                    return new Rectangle(x0, y0, what.getWidth(), what.getHeight());
                }
            }
        }

        return null;
    }
}
