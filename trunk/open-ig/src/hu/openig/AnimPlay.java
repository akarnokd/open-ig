/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig;

import hu.openig.ani.Framerates;
import hu.openig.ani.MovieSurface;
import hu.openig.ani.SpidyAniFile;
import hu.openig.ani.Framerates.Rates;
import hu.openig.ani.SpidyAniFile.Algorithm;
import hu.openig.ani.SpidyAniFile.Block;
import hu.openig.ani.SpidyAniFile.Data;
import hu.openig.ani.SpidyAniFile.Palette;
import hu.openig.ani.SpidyAniFile.Sound;
import hu.openig.compress.LZSS;
import hu.openig.compress.RLE;
import hu.openig.core.PaletteDecoder;
import hu.openig.sound.AudioThread;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
	private static File lastPath = new File("c:/games/ig");
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
	public static void playFile(File f) {
		double fps = 0;
		try {
			AudioThread ad = new AudioThread();
			ad.start();
			FileInputStream rf = new FileInputStream(f);
			try {
				final SpidyAniFile saf = new SpidyAniFile();
//				saf.open(rf);
//				saf.load();
//				saf.walkBlocks();
//		   		
//		   		fps = saf.getFPS();
//				rf.close();
				Framerates fr = new Framerates();
				rf = new FileInputStream(f);
				saf.open(rf);
				saf.load();
				createFrame(f);
				Rates r = fr.getRates(f.getAbsolutePath(), saf.getLanguageCode());
				fps = r.fps;
				int delay = r.delay;
				frame.setTitle(String.format("%s | FPS: %.4f | Delay: %d", frame.getTitle(), fps, delay));
				
				
				PaletteDecoder palette = null;
				int[] rawImage = new int[saf.getWidth() * saf.getHeight()];
				int imageHeight = 0;
				int dst = 0;
				Algorithm alg = saf.getAlgorithm();
				// initialize the painting surface
				imageLabel.init(saf.getWidth(), saf.getHeight());
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						if (frame.getExtendedState() != JFrame.MAXIMIZED_BOTH) {
							if (imageLabel.getWidth() < saf.getWidth() || imageLabel.getHeight() < saf.getHeight()) {
								imageLabel.setPreferredSize(new Dimension(saf.getWidth(), saf.getHeight()));
								frame.pack();
								frame.setLocationRelativeTo(null);
							}
						}
						
					}
				});
				try {
			   		double starttime = System.currentTimeMillis();
			   		int framecounter = 0;
			   		boolean firstframe = true;
			   		while (!stop) {
						Block b = saf.next();
						if (b instanceof Palette) {
							palette = (Palette)b;
						} else
						if (b instanceof Sound) {
							ad.submit(b.data);
						} else
						if (b instanceof Data) {
							if (firstframe) {
								starttime = System.currentTimeMillis();
								firstframe = false;
							}
							Data d = (Data)b;
							imageHeight += d.height;
							// decompress the image
							byte[] rleInput = d.data;
							if (saf.isLZSS() && !d.specialFrame) {
								rleInput = new byte[d.bufferSize];
								LZSS.decompress(d.data, 0, rleInput, 0);
							}
							switch (alg) {
							case RLE_TYPE_1:
								int newDst = RLE.decompress1(rleInput, 0, rawImage, dst, palette);
								dst = newDst;
								break;
							case RLE_TYPE_2:
								newDst = RLE.decompress2(rleInput, 0, rawImage, dst, palette);
								dst = newDst;
								break;
							default:
							}
							// we reached the number of subimages per frame?
							if (imageHeight >= saf.getHeight()) {
								imageLabel.getBackbuffer().setRGB(0, 0, saf.getWidth(), saf.getHeight(), rawImage, 0, saf.getWidth());
								imageLabel.swap();
								if (framecounter == delay) {
									ad.startPlaybackNow();
								}
								imageHeight = 0;
								dst = 0;
								
								starttime += (1000.0 / fps);
			           			LockSupport.parkNanos((long)(Math.max(0, starttime - System.currentTimeMillis()) * 1000000));
			           			framecounter++;
							}
						}
					}
				} catch (EOFException ex) {
					// we reached the end of file
					ad.submit(new byte[0]);
					ad.interrupt();
				}
				//System.out.printf("%.2f", audioCount * 0x4F6 / 22050f / saf.getFrameCount());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} finally {
				if (stop) {
					ad.stopPlaybackNow();
				}
				try {
					ad.interrupt();
					ad.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				stop = false;
				rf.close();
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						menuOpen.setEnabled(true);
						menuReplay.setEnabled(true);
						menuStop.setEnabled(false);
					}
				});
			}
		} catch (IOException ex) {
			
		}
	}
	/**
	 * Main program. Accepts 1 optional argument: the file name to play.
	 * @param args the arguments
	 * @throws IOException ignored
	 */
	public static void main(String[] args) throws IOException {
		String filename = null;
		if (args.length == 0) {
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
