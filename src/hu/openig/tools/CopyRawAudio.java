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
 * Copy raw segments of the audio into another audio file.
 * @author akarnokd, 2012.04.16.
 */
public final class CopyRawAudio {
    /** Utility class. */
    private CopyRawAudio() { }
    /**
     * Main program.
     * @param args no arguments
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {
        String fdst = "audio/de/messages/douglas_reinforcements_again_denied.wav";
        byte[] src = IOUtils.load("audio/de/messages/douglas_escort_thorin_reinforcements.wav");
        byte[] dst = IOUtils.load(fdst);

        int srcOffset = 0x2C + 0;
        int srcLen = (int)(22050 * 2.65);
        int dstOffset = 0x2C + 0;

        System.arraycopy(src, srcOffset, dst, dstOffset, srcLen);

        IOUtils.save(fdst, dst);
    }

}
