/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

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
 * @author karnok, 2009.09.22.
 * @version $Revision 1.0$
 */
public final class Startup {
	/** The minimum memory required to run Open-IG. */
	private static final long MINIMUM_MEMORY = 160L;
	/** The version number. */
	public static final String VERSION = "0.8"; 
	/** Constructor. */
	private Startup() {
		// private constructor.
	}
	/**
	 * The main entry point.
	 * @param args arguments
	 */
	public static void main(String[] args) {
		long maxMem = Runtime.getRuntime().maxMemory();
		if (maxMem < MINIMUM_MEMORY * 1024 * 1024 * 95 / 100) {
			if (!doLowMemory()) {
				doWarnLowMemory(maxMem);
			}
			return;
		}
		Configuration config = new Configuration("open-ig-config.xml");
		Set<String> argset = new HashSet<String>(Arrays.asList(args));
		if (!config.load() || argset.contains("-config")) {
			doStartConfiguration(config);
		} else {
			doStartGame();
		}
	}
	/**
	 * Put up warning dialog for failed attempt to run the program with appropriate memory.
	 * @param maxMem the detected memory
	 */
	private static void doWarnLowMemory(final long maxMem) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(null, "<html><p>Unable to auto-start Open Imperium Galactica version " + VERSION + ".<br>Please make sure you have at least " 
						+ MINIMUM_MEMORY + "MB defined for running a Java program in either your<br>"
						+ "operating system's configuration for Java programs,<br> or run the program from command line using the <code>-Xmx" + MINIMUM_MEMORY + "M</code> parameter.</p><br>"
						+ "<p>Nem sikerült automatikusan elindítani az Open Imperium Galactika " + VERSION + " programot.<br>Kérem ellenõrizze, hogy alapértelmezésben a Java programok futtatásához "
						+ "legalább " + MINIMUM_MEMORY + "MB memória<br> van beállítva az Operációs Rendszerben,<br> vagy indítsa a program parancssorból a <code>-Xmx" + MINIMUM_MEMORY + "M</code> "
						+ "paraméter megadásával.</p>"
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
		if (!new File("open-ig-" + VERSION + ".jar").exists()) {
			pb.command(System.getProperty("java.home") + "/bin/java", "-Xmx" + MINIMUM_MEMORY + "M", "-cp", "./bin", "-splash:bin/hu/openig/res/OpenIG_Splash.png", "hu.openig.v1.Startup");
		} else {
			pb.command(System.getProperty("java.home") + "/bin/java", "-Xmx" + MINIMUM_MEMORY + "M", "-cp", "open-ig-" + VERSION + ".jar", "-splash:hu/openig/res/OpenIG_Splash.png", "hu.openig.v1.Startup");
		}
		try {
			Process p = pb.start();
			createBackgroundReader(p.getInputStream(), System.out).start();
			createBackgroundReader(p.getErrorStream(), System.err).start();
			p.waitFor();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	/**
	 * Create a background stream copy thread for the given input and output streams.
	 * @param in the input stream
	 * @param out the output stream
	 * @return the thread
	 */
	private static Thread createBackgroundReader(final InputStream in, final OutputStream out) {
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
	 * Display the configuration window for setup.
	 * @param config the configuration
	 */
	private static void doStartConfiguration(final Configuration config) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Setup setup = new Setup(config);
				setup.setLocationRelativeTo(null);
				setup.setVisible(true);
				setup.pack();
			}
		});
	}
	/**
	 * Start the game.
	 */
	private static void doStartGame() {
		
	}
}
