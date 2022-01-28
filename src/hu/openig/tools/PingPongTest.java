/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Test the ping-pong network call through connection(s).
 * @author akarnokd, 2013.04.26.
 */
public final class PingPongTest {
    /** The test program. */
    private PingPongTest() { }
    /**
     * @param args no arguments
     * @throws Exception ignored
     */
    public static void main(String[] args) throws Exception {

        ExecutorService exec = Executors.newCachedThreadPool();
        exec.shutdown();
    }

}
