/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

import hu.openig.core.Configuration;
import hu.openig.utils.ConsoleWatcher;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


/**
 * The main startup class.
 * @author akarnokd, 2009.09.22.
 */
public final class Startup {
	/** The minimum memory required to run Open-IG. */
	public static final long MINIMUM_MEMORY = 640L;
	/** Constructor. */
	private Startup() {
		// private constructor.
	}
	/**
	 * The main entry point.
	 * @param args arguments
	 */
	public static void main(String[] args) {
		Set<String> argset = new HashSet<String>(Arrays.asList(args));
		long maxMem = Runtime.getRuntime().maxMemory();
		if (maxMem < MINIMUM_MEMORY * 1024 * 1024 * 95 / 100) {
			if (!argset.contains("-memonce")) {
				if (!doLowMemory()) {
					doWarnLowMemory(maxMem);
				}
				return;
			}
		}
		Configuration config = new Configuration("open-ig-config.xml");
		config.watcherWindow = new ConsoleWatcher(args, Configuration.VERSION);
		config.load();
//		if (!config.load() || argset.contains("-config")) {
//			doStartConfiguration(config);
//		} else {
			doStartGame(config);
//		}
	}
	/**
	 * Put up warning dialog for failed attempt to run the program with appropriate memory.
	 * @param maxMem the detected memory
	 */
	private static void doWarnLowMemory(final long maxMem) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(null, "<html><p>Unable to auto-start Open Imperium Galactica version " + Configuration.VERSION + ".<br>Please make sure you have at least " 
						+ MINIMUM_MEMORY + "MB defined for running a Java program in either your<br>"
						+ "operating system's configuration for Java programs,<br> or run the program from command line using the <code>-Xmx" + MINIMUM_MEMORY + "M</code> parameter.</p>"
				);
			}
		});
	}
	/**
	 * Restart the program using the proper memory settings.
	 * @return true if the re initialization was successful
	 */
	private static boolean doLowMemory() {
		ProcessBuilder pb = new ProcessBuilder();
		if (!new File("open-ig-" + Configuration.VERSION + ".jar").exists()) {
			pb.command(System.getProperty("java.home") + "/bin/java", "-Xmx" + MINIMUM_MEMORY + "M", "-cp", "./bin", "-splash:bin/hu/openig/gfx/OpenIG_Splash.png", "hu.openig.Startup", "-memonce");
		} else {
			pb.command(System.getProperty("java.home") + "/bin/java", "-Xmx" + MINIMUM_MEMORY + "M", "-jar", "open-ig-" + Configuration.VERSION + ".jar", "-memonce");
		}
		try {
			pb.start();
//			Process p = pb.start();
//			Thread t = createBackgroundReader(p.getErrorStream(), System.err);
//			t.start();
//			BufferedReader bin = new BufferedReader(new InputStreamReader(p.getInputStream()));
//			do {
//				String line = bin.readLine();
//				if (line == null || line.equals("OKAY")) {
//					break;
//				}
//			} while (!Thread.currentThread().isInterrupted());
//			t.interrupt();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	/**
	 * Create a background stream copy thread for the given input and output streams.
	 * @param in the input stream
	 * @param out the output stream
	 * @return the thread
	 */
	static Thread createBackgroundReader(final InputStream in, final OutputStream out) {
		return new Thread() {
			@Override
			public void run() {
				int c;
				try {
					while ((c = in.read()) != -1) {
						out.write(c);
						if (c == 10) {
							out.flush();
						}
					}
				} catch (IOException ex) {
					// ignored
				}
			}
		};
	}
	/**
	 * Start the game.
	 * @param config the configuration
	 */
	private static void doStartGame(final Configuration config) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				GameWindow gw = new GameWindow(config);
				gw.setVisible(true);
			}
		});
	}
}
