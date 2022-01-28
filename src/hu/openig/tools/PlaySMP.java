/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.sound.AudioThread;
import hu.openig.utils.IOUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Play the original SMP files.
 * @author akarnokd, 2011.08.17.
 */
public final class PlaySMP {
    /** Utility class. */
    private PlaySMP() {
        // utility class
    }
    /**
     * @param args no arguments
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {
        //enumerateSounds();
        String fn = "g:/Games/IGFR/Sound";
        FilenameFilter ff = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".smp");
            }
        };
        for (File f : new File(fn).listFiles(ff)) {
            byte[] sample = IOUtils.load(f);
            int dataLen = sample.length + (sample.length % 2 == 0 ? 0 : 1);

            try (DataOutputStream dout = new DataOutputStream(
                    new FileOutputStream(fn + "/" + f.getName() + ".wav"))) {
                // HEADER
                dout.write("RIFF".getBytes("ISO-8859-1"));
                dout.writeInt(Integer.reverseBytes(36 + dataLen)); // chunk size
                dout.write("WAVE".getBytes("ISO-8859-1"));

                // FORMAT
                dout.write("fmt ".getBytes("ISO-8859-1"));
                dout.writeInt(Integer.reverseBytes(16)); // chunk size
                dout.writeShort(Short.reverseBytes((short)1)); // Format: PCM = 1
                dout.writeShort(Short.reverseBytes((short)1)); // Channels = 1
                dout.writeInt(Integer.reverseBytes(22050)); // Sample Rate = 22050
                dout.writeInt(Integer.reverseBytes(22050)); // Byte Rate = 22050
                dout.writeShort(Short.reverseBytes((short)1)); // Block alignment = 1
                dout.writeShort(Short.reverseBytes((short)8)); // Bytes per sample = 8

                // DATA
                dout.write("data".getBytes("ISO-8859-1"));
                dout.writeInt(Integer.reverseBytes(dataLen));
                for (byte aSample : sample) {
                    dout.write(128 + aSample);
                }
                for (int i = sample.length; i < dataLen; i++) {
                    dout.write(0);
                }
            }
        }
    }
    /**
     * Enumerates sounds of SMP files.
     * @throws IOException on error
     * @throws InterruptedException on error
     */
    static void enumerateSounds() throws IOException, InterruptedException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("c:/Games/IGHU/sound/"), "NOI85.SMP")) {
            for (Path p : ds) {
                System.out.println(p);
                AudioThread at = new AudioThread();
                at.start();
                at.startPlaybackNow();
                at.submit(IOUtils.load(p.toFile()), true);
                at.submit(new byte[0], false);
                at.join();
                Thread.sleep(200);
            }
        }
    }

}
