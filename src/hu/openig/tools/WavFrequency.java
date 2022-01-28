/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.IOUtils;

/**
 * Change the wav frequency within the files.
 * @author akarnokd, 2011.08.16.
 */
public final class WavFrequency {
    /** Utility class. */
    private WavFrequency() {
        // utility class
    }
    /**
     * Main program.
     * @param args no arguments
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {
        // TODO Auto-generated method stub
        int freq = 11025;
        String[] files = {
                "audio/generic/ui/acknowledge_1",
                "audio/generic/ui/acknowledge_2",
                "audio/generic/ui/click_high_1",
                "audio/generic/ui/click_high_2",
                "audio/generic/ui/click_high_3",
                "audio/generic/ui/click_low_1",
                "audio/generic/ui/click_medium_1",
                "audio/generic/ui/click_medium_2",
                "audio/generic/ui/panel_slow",
                "audio/generic/spacewar/acknowledge_1",
                "audio/generic/spacewar/click_1",
                "audio/generic/spacewar/explosion_long",
                "audio/generic/spacewar/explosion_medium",
                "audio/generic/spacewar/explosion_short",
                "audio/generic/spacewar/fire_1",
                "audio/generic/spacewar/fire_2",
                "audio/generic/spacewar/fire_3",
                "audio/generic/spacewar/fire_laser",
                "audio/generic/spacewar/fire_meson",
                "audio/generic/spacewar/fire_particle",
                "audio/generic/spacewar/fire_rocket_1",
                "audio/generic/spacewar/fire_rocket_2",
                "audio/generic/spacewar/hit",
                "audio/generic/groundwar/acknowledge_1",
                "audio/generic/groundwar/acknowledge_2",
                "audio/generic/groundwar/explosion_long",
                "audio/generic/groundwar/explosion_medium",
                "audio/generic/groundwar/explosion_short",
                "audio/generic/groundwar/fire_1",
                "audio/generic/groundwar/fire_2",
                "audio/generic/groundwar/fire_3",
                "audio/generic/groundwar/fire_4",
                "audio/generic/groundwar/fire_5",
                "audio/generic/groundwar/fire_6",
                "audio/generic/groundwar/fire_7",
                "audio/generic/groundwar/fire_rocket",
                "audio/generic/groundwar/toggle_panel",
        };
        for (String s : files) {
            process(s + ".wav", freq);
        }
        process("audio/generic/ui/click_medium_2.wav", 22050);
    }
    /**
     * Process the specified file.
     * @param file the file to process
     * @param newFrequency the new frequency
     * @throws Exception on error
     */
    static void process(String file, int newFrequency) throws Exception {
        System.out.print(file);
        byte[] data = IOUtils.load(file);
        for (int i = 0; i < data.length - 3; i++) {
            if (data[i] == 'f' && data[i + 1] == 'm' && data[i + 2] == 't' && data[i + 3] == ' ') {

                int oldFrequency = (data[i + 12] & 0xFF) |  ((data[i + 13] & 0xFF) << 8) | ((data[i + 14] & 0xFF) << 16) | ((data[i + 15] & 0xFF) << 24);
                if (oldFrequency != newFrequency) {
                    System.out.printf("  %d Hz -> %d Hz%n", oldFrequency, newFrequency);
                    // samplerate
                    data[i + 12] = (byte)(newFrequency & 0xFF);
                    data[i + 13] = (byte)((newFrequency & 0xFF00) >> 8);
                    data[i + 14] = (byte)((newFrequency & 0xFF0000) >> 16);
                    data[i + 15] = (byte)((newFrequency & 0xFF000000) >> 24);
                    // byterate
                    data[i + 16] = data[i + 12];
                    data[i + 17] = data[i + 13];
                    data[i + 18] = data[i + 14];
                    data[i + 19] = data[i + 15];

                    IOUtils.save(file, data);
                } else {
                    System.out.printf("  %d Hz OK%n", oldFrequency);
                }
                break;
            }
        }
    }
}
