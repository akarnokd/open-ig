/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig;

import hu.openig.ani.MovieSurface;
import hu.openig.ani.SpidyAniDecoder;
import hu.openig.ani.SpidyAniDecoder.SpidyAniCallback;
import hu.openig.sound.AudioThread;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.locks.LockSupport;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Standalone ANI file player of Imperium Galactica's
 * various .ANI formats.
 * @author karnokd, 2009.01.11.
 * @version $Revision 1.0$
 */
public final class AnimPlay {
	/** The frame form the images. */
	private static JFrame frame;
	/** The label for the player. */
	private static MovieSurface imageLabel;
	/** The menu item for open. */
	private static JMenuItem menuOpen;
	/** The menu item for open. */
	private static JMenuItem menuStop;
	/** The replay current item. */
	private static JMenuItem menuReplay;
	/** The last opened file directory. */
	private static File lastPath;
	/** Stop the playback. */
	private static volatile boolean stop;
	/** Current file. */
	private static volatile File current;
	/** Private constructor. */
	private AnimPlay() {
		// utility program
	}
	/**
	 * Execute the SwingUtilities.invokeAndWait() method but strip of
	 * the exceptions. The exceptions will be ignored.
	 * @param r the runnable to pass along
	 */
	private static void swingInvokeAndWait(final Runnable r) {
		try {
			SwingUtilities.invokeAndWait(r);
		} catch (InterruptedException ex) {
			
		} catch (InvocationTargetException ex) {
			
		}
	}
	/**
	 * Create the player's frame.
	 * @param f the file that will be played
	 */
	private static void createFrame(final File f) {
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
					menuOpen.setEnabled(false);
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
							}
						}
					});
					menuStop = new JMenuItem("Stop");
					menuStop.setAccelerator(KeyStroke.getKeyStroke("S"));
					menuStop.setEnabled(true);
					menuStop.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							stop = true;
						}
					});
					
					file.add(menuOpen);
					file.add(menuReplay);
					file.add(menuStop);
					mb.add(file);
				}
				frame.setTitle(String.format("Playing: %s", f));
				frame.setVisible(true);
				//frame.pack();
			}
		});
	}
	/**
	 * Show a file open dialog.
	 * @return the selected file;
	 */
	private static String showOpenDialog() {
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
			double starttime;
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
				ad.submit(data);
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
			public void imageDate(int[] image) {
				if (frameCount == 0) {
					starttime = System.currentTimeMillis();
				}
				if (frameCount++ == frameDelay) {
					ad.startPlaybackNow();
				}
				imageLabel.getBackbuffer().setRGB(0, 0, width, height, image, 0, width);
				imageLabel.swap();
				// wait the frame/sec
				starttime += (1000.0 / fps);
       			LockSupport.parkNanos((long)(Math.max(0, starttime - System.currentTimeMillis()) * 1000000));
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
						public void run() {
							if (frame.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
								if (imageLabel.getWidth() < fwidth || imageLabel.getHeight() < fheight) {
									imageLabel.setPreferredSize(new Dimension(fwidth, fheight));
									frame.pack();
									frame.setLocationRelativeTo(null);
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
				stop = false;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						menuOpen.setEnabled(true);
						menuReplay.setEnabled(true);
						menuStop.setEnabled(false);
					}
				});
			}
		};
		createFrame(f);
		SpidyAniDecoder.decodeLoop(callback);
	}
	/**
	 * Main program. Accepts 1 optional argument: the file name to play.
	 * @param args the arguments
	 * @throws IOException ignored
	 */
	public static void main(String[] args) throws IOException {
		String filename = null;
		if (args.length == 0) {
			lastPath = new File("/games/ighu");
			filename = showOpenDialog();
		} else {
			filename = args[0];
		}
		// the number of images per second
		if (filename != null) {
			current = new File(filename);
			lastPath = current.getParentFile();
			playFile(current);
		}
	}

}
