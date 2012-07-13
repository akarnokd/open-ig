/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

import java.awt.Font;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

/**
 * This frame should pop up whenever something is printed on System.out or System.err.
 * @author akarnokd, 2010.09.13.
 */
public class ConsoleWatcher extends JFrame implements Closeable {
	/** */
	private static final long serialVersionUID = 8563889445922855434L;
	/** The text area for data. */
	JTextArea area;
	/** Original error. */
	private PrintStream originalErr;
	/** The original command line. */
	String[] commandLine;
	/** The program version. */
	String version;
	/** Language. */
	String language;
	/** A callback to invoke when the game crashes the first time. */
	Runnable onCrash;
	/**
	 * Create the gui.
	 * @param commandLine the command line
	 * @param version the game version
	 * @param language the startup language
	 * @param onCrash the action to invoke on first crash
	 */
	public ConsoleWatcher(String[] commandLine, 
			String version, 
			String language,
			final Runnable onCrash) {
		this.onCrash = onCrash;
		this.language = language;
		setTitle("Error");
		this.commandLine = commandLine;
		this.version = version;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		originalErr = System.err;
		
		area = new JTextArea();
		area.setEditable(false);
		area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		JScrollPane sp = new JScrollPane(area);
		
		getContentPane().add(sp);
		
		setSize(640, 480);
		setLocationRelativeTo(null);
		
		System.setErr(new PrintStream(new OutputStream() {
			/** First message, display diagnostic information. */
			volatile boolean first = true;
			@Override
			public void write(final int b) throws IOException {
				write(new byte[] { (byte)b }, 0, 1);
			}
			@Override
			public void write(final byte[] b, final int off, final int len) throws IOException {
				originalErr.write(b, off, len);
				final byte[] b2 = Arrays.copyOfRange(b, off, off + len);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (!isVisible()) {
							setVisible(true);
						}
						if (first) {
							first = false;
							if (onCrash != null) {
								try {
									onCrash.run();
								} catch (Throwable t) {
									final Throwable t2 = t;
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											t2.printStackTrace();
										}
									});
								}
							}
							writeDiagnosticInfo();
						}
						area.append(new String(b2, off, len));
					}
				});
			}
		}));
	}
	@Override
	public void close() throws IOException {
		System.setErr(originalErr);
		dispose();
	}
	/** Write the diagnostic info. */
	void writeDiagnosticInfo() {
		area.append("An unexpected error occurred.\r\n");
		area.append("You should consider submitting an error report via the project issue list:\r\nhttp://code.google.com/p/open-ig/issues/list\r\n");
		area.append("Please include the following diagnostic information followed by the error stacktrace(s):\r\n");
		area.append(String.format("   Java version: %s%n", System.getProperty("java.version")));
		area.append(String.format("   Java vendor: %s (%s)%n", System.getProperty("java.vendor"), System.getProperty("java.vendor.url")));
		area.append(String.format("   Java class version: %s%n", System.getProperty("java.class.version")));
		area.append(String.format("   Operating system: %s, %s, %s%n", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version")));
		area.append(String.format("   Game version: %s%n", version));
		area.append(String.format("   Command line: %s%n", Arrays.toString(commandLine)));
		area.append(String.format("   Available memory: %s MB%n", Runtime.getRuntime().freeMemory() / 1024 / 1024));
		area.append(String.format("   Maximum memory: %s MB%n", Runtime.getRuntime().maxMemory() / 1024 / 1024));
		area.append(String.format("   Parallelism: %s%n", Runtime.getRuntime().availableProcessors()));
		area.append(String.format("   Startup language: %s%n", language));
		if (onCrash != null) {
			area.append("A crash save has been created, please attach it in the issue report (zipped).\r\n");
		}
		area.append("----\r\n");
	}
}
