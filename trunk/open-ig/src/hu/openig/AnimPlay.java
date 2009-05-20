/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig;

import hu.openig.ani.GifSequenceWriter;
import hu.openig.ani.MovieSurface;
import hu.openig.ani.ProgressCallback;
import hu.openig.ani.SpidyAniDecoder;
import hu.openig.ani.SpidyAniDecoder.SpidyAniCallback;
import hu.openig.sound.AudioThread;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Window;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import javax.imageio.ImageIO;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JProgressBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.GroupLayout.Alignment;
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
	/** Save the animation as GIF. */
	private static JMenuItem saveAsGif;
	/** Save the animation as WAV. */
	private static JMenuItem saveAsWav;
	/** Save the animation as AVI. */
	private static JMenuItem saveAsAvi;
	/**
	 * Save frames as PNG images.
	 */
	private static JMenuItem saveAsPng;
	/** The last opened file directory. */
	private static File lastPath;
	/** The last opened file directory. */
	private static File lastSavePath;
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
					
					saveAsGif = new JMenuItem("Save as animated GIF...");
					saveAsGif.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { doSaveAsGif(); } });
					saveAsPng = new JMenuItem("Save as PNGs...");
					saveAsPng.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { doSaveAsPng(); } });
					saveAsWav = new JMenuItem("Save as WAV...");
					saveAsWav.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { doSaveAsWav(); } });
					saveAsAvi = new JMenuItem("Save as AVI...");
					saveAsAvi.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { doSaveAsAvi(); } });
					saveAsAvi.setEnabled(false);
					
					file.add(menuOpen);
					file.add(menuReplay);
					file.add(menuStop);
					file.addSeparator();
					file.add(saveAsGif);
					file.add(saveAsPng);
					file.add(saveAsWav);
					file.add(saveAsAvi);
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
	 * The progress indicator dialog.
	 * @author karnokd
	 */
	static class ProgressFrame extends JDialog implements ActionListener {
		/** Serial version. */
		private static final long serialVersionUID = -537904934073232256L;
		/** The progress bar. */
		private JProgressBar bar;
		/** The progress label. */
		private JLabel label;
		/** The cancel button. */
		private JButton cancel;
		/** The operation was cancelled. */
		private volatile boolean cancelled;
		/**
		 * Constructor. Sets the dialog's title. 
		 * @param title the title
		 * @param owner the owner
		 */
		public ProgressFrame(String title, Window owner) {
			super(owner, title);
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			setModalityType(ModalityType.APPLICATION_MODAL);
			bar = new JProgressBar();
			label = new JLabel();
			cancel = new JButton("Cancel");
			cancel.addActionListener(this);
			Container c = getContentPane();
			GroupLayout gl = new GroupLayout(c);
			c.setLayout(gl);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);
			
			gl.setHorizontalGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(bar)
				.addComponent(label)
				.addComponent(cancel)
			);
			gl.setVerticalGroup(
				gl.createSequentialGroup()
				.addComponent(bar)
				.addComponent(label)
				.addComponent(cancel)
			);
			setSize(350, 150);
			setLocationRelativeTo(owner);
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			cancelled = true;
		}
		/**
		 * Set the progress bar's maximum value.
		 * @param max the maximum value
		 */
		public void setMax(int max) {
			bar.setMaximum(max);
		}
		/**
		 * Update the progress bar's current value.
		 * @param value the current value
		 * @param text the text to display in label
		 */
		public void setCurrent(int value, String text) {
			bar.setValue(value);
			label.setText(text);
		}
		/**
		 * Was the operation cancelled by the user?
		 * @return the cancellation status
		 */
		public boolean isCancelled() {
			return cancelled;
		}
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
		final ProgressFrame pf = new ProgressFrame("Save as WAV: " + sel, frame);
		
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				transcodeToWav(current.getAbsolutePath(), sel.getAbsolutePath(), new ProgressCallback() {
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
		final ProgressFrame pf = new ProgressFrame("Save as PNG: " + sel, frame);
		
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				transcodeToPng(current.getAbsolutePath(), sel.getAbsolutePath(), new ProgressCallback() {
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
	 * Save animation as AVI.
	 */
	private static void doSaveAsAvi() {
		if (current == null) {
			return;
		}
		JFileChooser fc = new JFileChooser(lastSavePath);
		fc.setAcceptAllFileFilterUsed(true);
		fc.setFileFilter(new FileNameExtensionFilter("AVI files", "AVI"));
		if (fc.showSaveDialog(frame) == JFileChooser.CANCEL_OPTION) {
			return;
		}
//		final File sel = fc.getSelectedFile();
//		final ProgressFrame pf = new ProgressFrame("Save as AVI: " + sel, frame);
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
		try {
			final RandomAccessFile rf = new RandomAccessFile(outFile, "rw");
			try {
				rf.write("RIFF".getBytes("Latin1"));
				final long size1 = rf.getFilePointer();
				rf.writeInt(rotate(0 + 36)); // WE write this value later
				rf.write("WAVE".getBytes("Latin1"));
				rf.write("fmt ".getBytes("Latin1"));
				rf.writeInt(rotate(16));
				rf.writeShort(rotateShort(1)); // audioformat
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
					public void imageDate(int[] image) {
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
			} finally {
				rf.close();
			}
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
		final ExecutorService exec = new ThreadPoolExecutor(
				Runtime.getRuntime().availableProcessors(), 
				Runtime.getRuntime().availableProcessors(),
				0, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(Runtime.getRuntime().availableProcessors()),
				// in case of rejection, simply do a blocking put onto the queue
				new RejectedExecutionHandler() {
					@Override
					public void rejectedExecution(Runnable r,
							ThreadPoolExecutor executor) {
						try {
							executor.getQueue().put(r);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
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
			public void imageDate(int[] image) {
				if (progress != null) {
					progress.progress(frameCount, maxFrameCount);
				}
				final BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				img.setRGB(0, 0, width, height, image, 0, width);
				
				final int frameIndex = frameCount;
				// convert asynchronously
				exec.submit(new Runnable() {
					@Override
					public void run() {
						File f = new File(outFile + String.format("-%05d", frameIndex) + ".png");
						try {
							ImageIO.write(img, "png", f);
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
	 * Main program. Accepts 1 optional argument: the file name to play.
	 * @param args the arguments
	 * @throws IOException ignored
	 */
	public static void main(String[] args) throws IOException {
		String filename = null;
		if (args.length == 0) {
			lastPath = new File("/games/ighu");
			lastSavePath = lastPath;
			filename = showOpenDialog();
		} else {
			filename = args[0];
		}
		// the number of images per second
		if (filename != null) {
			current = new File(filename);
			lastPath = current.getParentFile();
			lastSavePath = lastPath;
			playFile(current);
		}
	}

}
