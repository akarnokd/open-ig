/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.tools.ani;

import hu.openig.core.MovieSurface;
import hu.openig.core.MovieSurface.ScalingMode;
import hu.openig.sound.AudioThread;
import hu.openig.tools.ani.SpidyAniDecoder.SpidyAniCallback;

import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Standalone ANI file player of Imperium Galactica's
 * various .ANI formats.
 * @author karnokd, 2009.01.11.
 * @version $Revision 1.0$
 */
public final class AnimPlay {
    /** The frame form the images. */
    static JFrame frame;
    /** The playlist. */
    static AnimPlayList playList;
    /** The label for the player. */
    static MovieSurface imageLabel;
    /** The menu item for open. */
    static JMenuItem menuOpen;
    /** The menu item for open. */
    static JMenuItem menuStop;
    /** The replay current item. */
    static JMenuItem menuReplay;
    /** Save the animation as GIF. */
    static JMenuItem saveAsGif;
    /** Save the animation as WAV. */
    static JMenuItem saveAsWav;
    /**
     * Save frames as PNG images.
     */
    static JMenuItem saveAsPng;
    /** The last opened file directory. */
    static File lastPath;
    /** The last opened file directory. */
    static File lastSavePath;
    /** Stop the playback. */
    static volatile boolean stop;
    /** Current file. */
    static volatile File current;
    /** Private constructor. */
    private AnimPlay() {
        // utility program
    }
    /**
     * Execute the SwingUtilities.invokeAndWait() method but strip of
     * the exceptions. The exceptions will be ignored.
     * @param r the runnable to pass along
     */
    static void swingInvokeAndWait(final Runnable r) {
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
            return;
        }
        try {
            SwingUtilities.invokeAndWait(r);
        } catch (InterruptedException | InvocationTargetException ex) {

        }
    }
    /**
     * Create the player's frame.
     * @param f the file that will be played
     */
    static void createFrame(final File f) {
        swingInvokeAndWait(new Runnable() {
            @Override
            public void run() {
                if (frame == null) {
                    frame = new JFrame();
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    Container c = frame.getContentPane();
                    imageLabel = new MovieSurface();
                    c.add(imageLabel);
                    frame.setResizable(true);
                    JMenuBar mb = new JMenuBar();
                    frame.setJMenuBar(mb);

                    JMenu file = new JMenu("File");
                    menuOpen = new JMenuItem("Open...");
                    menuOpen.setEnabled(true);
                    menuOpen.setAccelerator(KeyStroke.getKeyStroke("A"));
                    menuOpen.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            String file = showOpenDialog();
                            if (file == null) {
                                menuOpen.setEnabled(true);
                                menuReplay.setEnabled(current != null);
                                menuStop.setEnabled(false);
                            } else {
                                doPlayFile(file);
                            }
                        }
                    });
                    menuReplay = new JMenuItem("Replay");
                    menuReplay.setAccelerator(KeyStroke.getKeyStroke("D"));
                    menuReplay.setEnabled(false);
                    menuReplay.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            if (current != null) {
                                Thread t = new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        playFile(current);
                                    }
                                });
                                t.start();
                                menuOpen.setEnabled(false);
                                menuReplay.setEnabled(false);
                                menuStop.setEnabled(true);
                                saveAsGif.setEnabled(true);
                                saveAsPng.setEnabled(true);
                                saveAsWav.setEnabled(true);
                            }
                        }
                    });
                    menuStop = new JMenuItem("Stop");
                    menuStop.setAccelerator(KeyStroke.getKeyStroke("S"));
                    menuStop.setEnabled(false);
                    menuStop.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            stop = true;
                        }
                    });

                    saveAsGif = new JMenuItem("Save as animated GIF...");
                    saveAsGif.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) {

                        doSaveAsGif();

                    } });
                    saveAsGif.setEnabled(false);

                    saveAsPng = new JMenuItem("Save as PNGs...");
                    saveAsPng.setEnabled(false);
                    saveAsPng.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) {

                        doSaveAsPng();

                    } });

                    saveAsWav = new JMenuItem("Save as WAV...");
                    saveAsWav.setEnabled(false);
                    saveAsWav.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) {

                        doSaveAsWav();

                    } });

                    file.add(menuOpen);
                    file.add(menuReplay);
                    file.add(menuStop);
                    file.addSeparator();
                    file.add(saveAsGif);
                    file.add(saveAsPng);
                    file.add(saveAsWav);
                    mb.add(file);

                    JMenu view = new JMenu("View");
                    JRadioButtonMenuItem keepAspect = new JRadioButtonMenuItem("Keep aspect", true);
                    setAL(keepAspect, "setKeepAspect", null);
                    JRadioButtonMenuItem fitWindow = new JRadioButtonMenuItem("Fit window");
                    setAL(fitWindow, "setFitWindow", null);
                    JRadioButtonMenuItem noscale = new JRadioButtonMenuItem("No scaling");
                    setAL(noscale, "setNoScale", null);

                    JMenuItem showPlayList = new JMenuItem("Show playlist");
                    setAL(showPlayList, "showPlayList", null);

                    ButtonGroup bg = new ButtonGroup();
                    bg.add(keepAspect);
                    bg.add(fitWindow);
                    bg.add(noscale);

                    view.add(keepAspect);
                    view.add(fitWindow);
                    view.add(noscale);
                    view.addSeparator();
                    view.add(showPlayList);

                    mb.add(view);
                    frame.setSize(400, 300);
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                }
                frame.setTitle(String.format("Playing: %s", f));

                if (playList == null) {
                    playList = new AnimPlayList(lastPath);
                    playList.setLocation(frame.getX() - playList.getWidth() - 5, frame.getY());
                    playList.setVisible(true);
                }

            }
        });
    }
    /** Displays the playlist window. */
    static void showPlayList() {
        playList.setLocation(frame.getX() - playList.getWidth() - 5, frame.getY());
        playList.setVisible(true);
    }
    /**
     * Set playback scaling mode to keep aspect.
     */
    static void setKeepAspect() {
        imageLabel.setScalingMode(ScalingMode.KEEP_ASPECT);
    }
    /** Set scaling mode to fit window. */
    static void setFitWindow() {
        imageLabel.setScalingMode(ScalingMode.WINDOW_SIZE);
    }
    /** Set scaling mode to none. */
    static void setNoScale() {
        imageLabel.setScalingMode(ScalingMode.NONE);
    }
    /**
     * Sets a reflective action listener to the given abstract button. for convinience.
     * @param c the abstract button
     * @param action the method name within AnimPlay
     * @param o the target object or null for AnimPlay itself
     */
    static void setAL(AbstractButton c, final String action, final Object o) {
        c.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Method m =  (o != null ? o.getClass() : AnimPlay.class).getDeclaredMethod(action, ActionEvent.class);
                    m.invoke(o, e);
                } catch (NoSuchMethodException e1) {
                } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
                    e1.printStackTrace();
                }
                try {
                    Method m = (o != null ? o.getClass() : AnimPlay.class).getDeclaredMethod(action);
                    m.invoke(o);
                } catch (NoSuchMethodException e1) {
                } catch (SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
    /**
     * Show a file open dialog.
     * @return the selected file;
     */
    static String showOpenDialog() {
        JFileChooser jfc = new JFileChooser(lastPath);
        jfc.setFileFilter(new FileNameExtensionFilter("ANI files", "ANI"));
        if (jfc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return jfc.getSelectedFile().getAbsolutePath();
        }
        return null;
    }
    /**
     * Plays the given file.
     * @param f the file to play
     */
    public static void playFile(final File f) {
        final AudioThread ad = new AudioThread();
        ad.start();
        SpidyAniCallback callback = new SpidyAniCallback() {
            /** Time calculation for proper frame delay. */
            double startTime;
            /** The current frame number. */
            int frameCount;
            /** The audio frame delay. */
            int frameDelay;
            /** Frame width. */
            int width;
            /** Frame height. */
            int height;
            /** The frame/second. */
            double fps;
            @Override
            public void audioData(byte[] data) {
                ad.submit(data, true);
            }

            @Override
            public void fatal(Throwable t) {
                t.printStackTrace();
                stopped();
            }

            @Override
            public void finished() {
                ad.stopPlayback();
                // wait for the audio thread to finish
                try {
                    ad.join();
                } catch (InterruptedException e) {
                    // ignored here
                }
                done();
            }

            @Override
            public String getFileName() {
                return f.getName();
            }

            @Override
            public InputStream getNewInputStream() {
                try {
                    return new FileInputStream(f);
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException("Missing file? " + f);
                }
            }

            @Override
            public void imageData(int[] image) {
                if (frameCount == 0) {
                    startTime = System.currentTimeMillis();
                }
                if (frameCount++ == frameDelay) {
                    ad.startPlaybackNow();
                }
                imageLabel.getBackbuffer().setRGB(0, 0, width, height, image, 0, width);
                imageLabel.swap();
                // wait the frame/sec
                startTime += (1000.0 / fps);
                   LockSupport.parkNanos((long)(Math.max(0, startTime - System.currentTimeMillis()) * 1000000));
            }

            @Override
            public void initialize(int width, int height, int frames,
                    int languageCode, double fps, int audioDelay) {
                this.frameDelay = audioDelay;
                this.width = width;
                this.height = height;
                this.fps = fps;
                imageLabel.init(width, height);
                // clear backbuffer
                imageLabel.getBackbuffer().setRGB(0, 0, width, height, new int[width * height], 0, width);
                imageLabel.swap();
                frame.setTitle(String.format("%s | FPS: %.4f | Delay: %d", frame.getTitle(), fps, frameDelay));
                final int fwidth = width;
                final int fheight = height;
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override

                        public void run() {
                            if (frame.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                                if (imageLabel.getWidth() < fwidth || imageLabel.getHeight() < fheight) {
                                    imageLabel.setPreferredSize(new Dimension(fwidth, fheight));
                                    frame.pack();
//                                    frame.setLocationRelativeTo(null);
                                }
                            }

                        }
                    });
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (InvocationTargetException ex) {
                    Thread.currentThread().interrupt();
                    ex.printStackTrace();
                }
            }

            @Override
            public boolean isPaused() {
                return false;
            }

            @Override
            public boolean isStopped() {
                return stop;
            }

            @Override
            public void stopped() {
                ad.stopPlaybackNow();
                try {
                    ad.join();
                } catch (InterruptedException e) {
                    // ignored here
                }
                done();
            }
            /** Re-enable controls on either stop or finish outcome. */
            private void done() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        menuOpen.setEnabled(true);
                        menuReplay.setEnabled(true);
                        menuStop.setEnabled(false);
                        stop = false;
                    }
                });
            }
        };
        createFrame(f);
        SpidyAniDecoder.decodeLoop(callback);
    }
    /**
     * Save animation as GIF.

     */
    private static void doSaveAsGif() {
        if (current == null) {
            return;
        }
        JFileChooser fc = new JFileChooser(lastSavePath);
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new FileNameExtensionFilter("GIF files", "GIF"));
        if (fc.showSaveDialog(frame) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        final File sel = fc.getSelectedFile();
        lastSavePath = sel.getParentFile();
        final ProgressFrame pf = new ProgressFrame("Save as GIF: " + sel, frame);

        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                GifSequenceWriter.transcodeToGif(current.getAbsolutePath(), sel.getAbsolutePath(), new ProgressCallback() {
                    @Override
                    public void progress(final int value, final int max) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                pf.setMax(max);
                                pf.setCurrent(value, "Progress: " + value + " / " + max + " frames");
                            }
                        });
                    }
                    @Override
                    public boolean cancel() {
                        return pf.isCancelled();
                    }
                });
                return null;
            }
            @Override
            protected void done() {
                // delay window close a bit further
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        pf.dispose();
                    }
                });
            }
        };
        worker.execute();
        pf.setVisible(true);
    }
    /**
     * Save animation as Wav.
     */
    private static void doSaveAsWav() {
        if (current == null) {
            return;
        }
        JFileChooser fc = new JFileChooser(lastSavePath);
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new FileNameExtensionFilter("WAV files", "WAV"));
        if (fc.showSaveDialog(frame) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        final File sel = fc.getSelectedFile();
        lastSavePath = sel.getParentFile();

        saveAsWavWorker(current, sel, true, frame);
    }
    /**
     * Save the selected file's audio as Wave.
     * @param what the file to use as input
     * @param target the target output filename
     * @param modal show the dialog as modal?
     * @param parent the parent frame
     * @return the worker
     */
    static SwingWorker<Void, Void> saveAsWavWorker(final File what, final File target, boolean modal, JFrame parent) {
        final ProgressFrame pf = new ProgressFrame("Save as WAV: " + target, parent);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                transcodeToWav(what.getAbsolutePath(), target.getAbsolutePath(), new ProgressCallback() {
                    @Override
                    public void progress(final int value, final int max) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                pf.setMax(max);
                                pf.setCurrent(value, "Progress: " + value + " / " + max + " frames");
                            }
                        });
                    }
                    @Override
                    public boolean cancel() {
                        return pf.isCancelled();
                    }
                });
                return null;
            }
            @Override
            protected void done() {
                // delay window close a bit further
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        pf.dispose();
                    }
                });
            }
        };
        worker.execute();
        pf.setModalityType(modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
        pf.setVisible(true);
        return worker;
    }
    /**
     * Save animation as Wav.
     */
    private static void doSaveAsPng() {
        if (current == null) {
            return;
        }
        JFileChooser fc = new JFileChooser(lastSavePath);
        fc.setAcceptAllFileFilterUsed(true);
        fc.setFileFilter(new FileNameExtensionFilter("PNG files", "PNG"));
        if (fc.showSaveDialog(frame) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        final File sel = fc.getSelectedFile();
        lastSavePath = sel.getParentFile();

        saveAsPNGWorker(current, sel, true, frame);
    }
    /**
     * Worker for save as PNG.
     * @param what the file to convert
     * @param target the target filename
     * @param modal show the progress as a modal dialog?
     * @param parent the parent frame
     * @return the worker
     */
    static SwingWorker<Void, Void> saveAsPNGWorker(final File what, final File target, boolean modal, JFrame parent) {
        final ProgressFrame pf = new ProgressFrame("Save as PNG: " + target, parent);
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                transcodeToPng(what.getAbsolutePath(), target.getAbsolutePath(), new ProgressCallback() {
                    @Override
                    public void progress(final int value, final int max) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                pf.setMax(max);
                                pf.setCurrent(value, "Progress: " + value + " / " + max + " frames");
                            }
                        });
                    }
                    @Override
                    public boolean cancel() {
                        return pf.isCancelled();
                    }
                });
                return null;
            }
            @Override
            protected void done() {
                // delay window close a bit further
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        pf.dispose();
                    }
                });
            }
        };
        worker.execute();
        pf.setModalityType(modal ? ModalityType.APPLICATION_MODAL : ModalityType.MODELESS);
        pf.setVisible(true);
        return worker;
    }
    /**
     * Change between little endian and big endian coding.
     * @param val the value to rotate
     * @return the rotated value

     */
    private static int rotate(int val) {
        return (val & 0xFF000000) >> 24 | (val & 0xFF0000) >> 8

        | (val & 0xFF00) << 8 | (val & 0xFF) << 24;
    }
    /**

     * Change between little endian and big endian coding.
     * @param val the value to rotate
     * @return the rotated value

     */
    private static int rotateShort(int val) {
        return (val & 0xFF00) >> 8 | (val & 0xFF) << 8;
    }
    /**
     * Transcode the input ANI file to a WAV file.
     * @param infile the input file
     * @param outFile the output file
     * @param progress the progress indicator callback.
     */
    public static void transcodeToWav(final String infile, final String outFile, final ProgressCallback progress) {
        try (RandomAccessFile rf = new RandomAccessFile(outFile, "rw")) {
            rf.write("RIFF".getBytes("Latin1"));
            final long size1 = rf.getFilePointer();
            rf.writeInt(rotate(0 + 36)); // WE write this value later
            rf.write("WAVE".getBytes("Latin1"));
            rf.write("fmt ".getBytes("Latin1"));
            rf.writeInt(rotate(16));
            rf.writeShort(rotateShort(1)); // audio format
            rf.writeShort(rotateShort(1)); // channels
            rf.writeInt(rotate(22050)); // samplerate
            rf.writeInt(rotate(22050)); // byterate
            rf.writeShort(rotateShort(1)); // block alignment
            rf.writeShort(rotateShort(8)); // bytes per sample
            rf.write("data".getBytes("Latin1"));
            final long size2 = rf.getFilePointer();
            rf.writeInt(rotate(0)); // We write this value later
            SpidyAniCallback callback = new SpidyAniCallback() {
                /** The current frame index. */
                private int frameCount;
                /** The maximum frame count. */
                private int maxFrameCount;
                /** The total byte counter. */
                private int byteCount;
                @Override
                public void audioData(byte[] data) {
                    try {
                        for (int i = 0; i < data.length; i++) {
                            data[i] = (byte)(data[i] + 128); // offset to unsigned
                        }
                        rf.write(data);
                        byteCount += data.length;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public void fatal(Throwable t) {
                    t.printStackTrace();
                    stopped();
                }

                @Override
                public void finished() {
                    try {
                        // data size must be even!
                        if (byteCount % 2 != 0) {
                            rf.write(0x80);
                            byteCount++;
                        }
                        rf.seek(size1);
                        rf.writeInt(rotate(byteCount + 36));
                        rf.seek(size2);
                        rf.writeInt(rotate(byteCount));
                        rf.close();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public String getFileName() {
                    return infile;
                }

                @Override
                public InputStream getNewInputStream() {
                    try {
                        return new FileInputStream(getFileName());
                    } catch (FileNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public void imageData(int[] image) {
                    if (progress != null) {
                        progress.progress(frameCount, maxFrameCount);
                    }
                    frameCount++;
                }

                @Override
                public void initialize(int width, int height, int frames,
                        int languageCode, double fps, int audioDelay) {
                    this.maxFrameCount = frames;
                    // introduce the audio delay as no sound
                    try {
                        int delay = (int)(audioDelay / fps * 22050);
                        byte[] silence = new byte[delay];
                        for (int i = 0; i < silence.length; i++) {
                            silence[i] = -128;
                        }
                        rf.write(silence);
                        byteCount += delay;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                @Override
                public boolean isPaused() {
                    return false;
                }

                @Override
                public boolean isStopped() {
                    return progress != null && progress.cancel();
                }

                @Override
                public void stopped() {
                    finished();
                }

            };
            SpidyAniDecoder.decodeLoop(callback);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }
    /**
     * Transcode the input ANI file to a WAV file.
     * @param infile the input file
     * @param outFile the output file
     * @param progress the progress indicator callback.
     */
    public static void transcodeToPng(final String infile, final String outFile, final ProgressCallback progress) {
        // use all available processors for PNG encoding, but not more for the queue
        final int n = Runtime.getRuntime().availableProcessors();
        @SuppressWarnings("resource")
        final ExecutorService exec = new ThreadPoolExecutor(
                n, /* Runtime.getRuntime().availableProcessors() ,*/

                n, /* Runtime.getRuntime().availableProcessors() ,*/
                2000, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(n),
                // in case of rejection, simply do a blocking put onto the queue
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r,
                            ThreadPoolExecutor executor) {
                        try {
                            executor.getQueue().put(r);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                );
        SpidyAniCallback callback = new SpidyAniCallback() {
            /** The current frame index. */
            private int frameCount;
            /** The maximum frame count. */
            private int maxFrameCount;
            /** The image width. */
            private int width;
            /** The image height. */
            private int height;
            @Override
            public void audioData(byte[] data) {
                // ignored
            }

            @Override
            public void fatal(Throwable t) {
                t.printStackTrace();
                stopped();
            }

            @Override
            public void finished() {
                exec.shutdown();
            }

            @Override
            public String getFileName() {
                return infile;
            }

            @Override
            public InputStream getNewInputStream() {
                try {
                    return new FileInputStream(getFileName());
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }

            @Override
            public void imageData(int[] image) {
                if (progress != null) {
                    progress.progress(frameCount, maxFrameCount);
                }
                final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                img.setAccelerationPriority(0.0f);
                img.setRGB(0, 0, width, height, image, 0, width);

                final int frameIndex = frameCount;
                // convert asynchronously
                exec.submit(new Runnable() {
                    @Override
                    public void run() {
                        File f = new File(String.format("%s-%05d.png", outFile, frameIndex));
                        try {
                            ImageIO.write(img, "BMP", f); // FIXME: for less CPU usage but more disk space
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });

                frameCount++;
            }

            @Override
            public void initialize(int width, int height, int frames,
                    int languageCode, double fps, int audioDelay) {
                this.maxFrameCount = frames;
                this.width = width;
                this.height = height;
                // introduce the audio delay as no sound
            }

            @Override
            public boolean isPaused() {
                return false;
            }

            @Override
            public boolean isStopped() {
                return progress != null && progress.cancel();
            }

            @Override
            public void stopped() {
                finished();
            }

        };
        SpidyAniDecoder.decodeLoop(callback);
    }
    /**
     * Main program. no arguments whatsoever.
     * @param args the arguments
     * @throws IOException ignored
     */
    public static void main(String[] args) throws IOException {
        lastPath = new File("d:\\games\\ighu");
        if (!lastPath.exists()) {
            lastPath = new File("c:\\games\\ighu");
            if (!lastPath.exists()) {
                lastPath = new File(".");
            }
        }
        createFrame(lastPath);
    }
    /**
     * Play the given filename and set menu settings accordingly.
     * @param file the file to play
     */
    static void doPlayFile(String file) {
        current = new File(file);
        lastPath = current.getParentFile();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                playFile(current);
            }
        });
        t.start();
        menuOpen.setEnabled(false);
        menuReplay.setEnabled(false);
        menuStop.setEnabled(true);
        saveAsGif.setEnabled(true);
        saveAsPng.setEnabled(true);
        saveAsWav.setEnabled(true);
    }

}
