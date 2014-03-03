/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

import hu.openig.core.Action0;
import hu.openig.core.Func0;
import hu.openig.core.Func1;
import hu.openig.core.SaveMode;
import hu.openig.model.Configuration;
import hu.openig.screen.CommonResources;
import hu.openig.utils.ConsoleWatcher;
import hu.openig.utils.Exceptions;
import hu.openig.utils.IOUtils;
import hu.openig.utils.U;

import java.awt.Frame;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


/**
 * The main startup class.
 * @author akarnokd, 2009.09.22.
 */
public final class Startup {
	/** The minimum memory required to run Open-IG. */
	public static final long MINIMUM_MEMORY = 768;
	/** Constructor. */
	private Startup() {
		// private constructor.
	}
	/**
	 * The main entry point.
	 * @param args arguments
	 */
	public static void main(String[] args) {
		Set<String> argset = new LinkedHashSet<>(Arrays.asList(args));
		long maxMem = Runtime.getRuntime().maxMemory();
		if (maxMem < MINIMUM_MEMORY * 1024 * 1024 * 95 / 100) {
			if (!argset.contains("-memonce")) {
				if (!doLowMemory(argset)) {
					doWarnLowMemory(maxMem);
				}
				return;
			}
		}
		final Configuration config = new Configuration("open-ig-config.xml");
		Func0<String> languageFn = new Func0<String>() {
			@Override
			public String invoke() {
				return config.language;
			}
		};
		Func0<String> onCrash = new Func0<String>() {
			@Override
			public String invoke() {
				for (Frame f : JFrame.getFrames()) {
					if (f instanceof GameWindow) {
						GameWindow gw = (GameWindow)f;
						if (gw.commons != null && gw.commons.world() != null && gw.isVisible()) {
							try {
								File file = ((GameWindow)f).saveWorld("Crash", SaveMode.MANUAL).get();
								String wd = new File(".").getAbsolutePath();
								String fp = file.getAbsolutePath();
								if (fp.startsWith(wd)) {
									return wd.substring(fp.length());
								}
								return fp;
							} catch (ExecutionException | InterruptedException ex) {
								// ignored
							}
						}
					}
				}
				return null;
			}
		};
		@SuppressWarnings("resource")
		final ConsoleWatcher cw = new ConsoleWatcher(args, 
				Configuration.VERSION,
				languageFn, 
				onCrash);
		config.watcherWindow = cw;
		config.crashLog = new Func0<String>() {
			@Override
			public String invoke() {
				return cw.getCrashLog();
			}
		};
		config.load();

		Set<String> langCodes = U.newSet(
				"hu", "en", "de", "fr", "ru", "es");

		for (String lc : langCodes) {
			if (argset.contains("-" + lc)) {
				config.language = lc;
				break;
			}
		}
		if (argset.contains("-maximized")) {
			config.maximized = true;
		}
		if (argset.contains("-fullscreen")) {
			config.fullScreen = true;
		}
		if (argset.contains("-fullscreenvideos")) {
			config.movieScale = true;
		}
		if (argset.contains("-clickskip")) {
			config.movieClickSkip = true;
		}
		if (argset.contains("-noclickskip")) {
			config.movieClickSkip = false;
		}
		if (argset.contains("-continue")) {
			config.continueLastGame = true;
		}

		doStartGame(config);
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
	 * @param args the application arguments
	 * @return true if the re initialization was successful
	 */
	private static boolean doLowMemory(Set<String> args) {
		ProcessBuilder pb = new ProcessBuilder();
		List<String> cmdLine = new ArrayList<>();
		cmdLine.add(System.getProperty("java.home") + "/bin/java");
		cmdLine.add("-Xmx" + MINIMUM_MEMORY + "M");
		cmdLine.add("-cp");
		if (!new File("open-ig-" + Configuration.VERSION + ".jar").exists()) {
			cmdLine.add("./bin");
		} else {
			cmdLine.add("open-ig-" + Configuration.VERSION + ".jar");
		}
		cmdLine.add("-splash:open-ig-splash.png");
		cmdLine.add("hu.openig.Startup");
		cmdLine.add("-memonce");
		cmdLine.addAll(args);

		pb.command(cmdLine);
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
			Exceptions.add(e);
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
		// setup troubleshooting flags
		if (config.disableD3D) {
			System.setProperty("sun.java2d.d3d", "false");
		}
		if (config.disableDirectDraw) {
			System.setProperty("sun.java2d.noddraw", "true");
		}
		if (config.disableOpenGL) {
			System.setProperty("sun.java2d.opengl", "false");
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (checkInstall()) {
					try {
						final boolean cont = config.continueLastGame;
						config.continueLastGame = cont && !config.intro;
						CommonResources commons = new CommonResources(config, null);
						
						convertSaves();
						
						final GameWindow gw = new GameWindow(config, commons);
						gw.setVisible(true);
						if (config.intro) {
							config.intro = false;
							config.save();
							gw.playVideos(new Action0() {
								@Override
								public void invoke() {
									if (cont) {
										config.continueLastGame = cont;
										gw.continueLastGame();
									}
								}
							}, "intro/gt_interactive_intro", "intro/intro_1", "intro/intro_2", "intro/intro_3");
						}
					} catch (Throwable t) {
						t.printStackTrace();
						runLauncher(config);
					}
				} else {
					runLauncher(config);
				}
			}
		});
	}
	/**
	 * Check if the most relevant data files are available.
	 * @return true if the file check succeded.
	 */
	static boolean checkInstall() {
		File installDir = new File(".");
		Set<String> testFiles = new HashSet<>(Arrays.asList(
				"generic/options_1.png",
				"generic/campaign/main/definition.xml",
				"generic/ui/achievement.wav",
				"generic/intro/intro_1.ani.gz"
				));
		List<String> directDirs = Arrays.asList("data", "images", "audio", "video");

		File[] files = installDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith("open-ig") && name.endsWith(".zip");
			}
		});
		if (files != null) {
			for (File f : files) {
				try (ZipFile zf = new ZipFile(f)) {
					Enumeration<? extends ZipEntry> en = zf.entries();
					while (en.hasMoreElements()) {
						ZipEntry ze = en.nextElement();
						String zn = ze.getName().replace('\\', '/');
						testFiles.remove(zn);
					}
				} catch (IOException ex) {
					return false;
				}
			}
		}
		for (String tf : U.newArrayList(testFiles)) {
			for (String d : directDirs) {
				File f2 = new File(installDir, d + File.separator + tf);
				if (f2.canRead()) {
					testFiles.remove(tf);
				}
			}
		}

		return testFiles.isEmpty();
	}
	/**
	 * Attempt to run the launcher.
	 * @param config the current configuration
	 */
	static void runLauncher(Configuration config) {
		if (JOptionPane.showConfirmDialog(null, 
				"<html>Unable to locate some game resource files.<br>I'll try to run the Launcher to repair the install."
				, "", JOptionPane.YES_NO_OPTION
		) == JOptionPane.NO_OPTION) {
			return;
		}
		config.crashLog = null;
		U.close(config.watcherWindow);
		File launcher = new File("open-ig-launcher.jar");
		if (launcher.canRead()) {
			String self = String.format("%s/open-ig-launcher.jar", new File(".").getAbsolutePath());
			ProcessBuilder pb = new ProcessBuilder();
			pb.command(System.getProperty("java.home") + "/bin/java", 
					"-jar", self, "-verify");
			try {
				pb.start();
				return;
			} catch (IOException ex) {

			}
		}
		JOptionPane.showMessageDialog(null, "<html>"
				+ "Unable to locate some game resource files and<br>"
				+ "unable to locate the Open-IG Launcher.<br>"
				+ "Please make sure you run the game from the proper working directory<br>"
				+ "or please download the Launcher from the project site<br><br>"
				+ "http://open-ig.googlecode.com<br><br> and perform a proper install with it."
				+ "(The current directory is: " + new File(".").getAbsolutePath());
	}
	/**
	 * Convert the plain saves into GZIP-ped saves.
	 */
	static void convertSaves() {
		File saveDir = new File("save");
		if (saveDir.exists() && saveDir.isDirectory()) {
			for (File profileDir : U.listFiles(saveDir)) {
				if (profileDir.isDirectory()) {
					for (File save : U.listFiles(profileDir, new Func1<File, Boolean>() {
						@Override
						public Boolean invoke(File value) {
							String n = value.getName();
							return n.startsWith("save-") && n.endsWith(".xml");
						}
					})) {
						byte[] data = IOUtils.load(save);
						File out = new File(profileDir, save.getName() + ".gz");
						try (GZIPOutputStream gout = new GZIPOutputStream(new FileOutputStream(out))) {
							gout.write(data);
						} catch (IOException ex) {
							Exceptions.add(ex);
						}
						if (!save.delete()) {
							System.err.println("Unable to delete " + save.getAbsolutePath());
						}
					}
				}
			}
		}
	}
}
