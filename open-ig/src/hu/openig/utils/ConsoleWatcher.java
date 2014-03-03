/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

import hu.openig.core.Func0;
import hu.openig.utils.XElement.Appender;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Date;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
	/** The issue list URL. */
	public static final String ISSUE_LIST = "https://code.google.com/p/open-ig/issues/list";
	/** The text area for data. */
	JTextArea area;
	/** The copy to clipboard button. */
	JButton copy;
	/** The clear text field. */
	JButton clear;
	/** The report button. */
	JButton report;
	/** Original error. */
	private PrintStream originalErr;
	/** The original command line. */
	String[] commandLine;
	/** The program version. */
	String version;
	/** Language. */
	Func0<String> language;
	/** A callback to invoke when the game crashes the first time. It may return text to be printed. */
	Func0<String> onCrash;
	/** First message, display diagnostic information. */
	volatile boolean first = true;
	/**
	 * Create the gui.
	 * @param commandLine the command line
	 * @param version the game version
	 * @param language the startup language
	 * @param onCrash the action to invoke on first crash
	 */
	public ConsoleWatcher(String[] commandLine, 
			String version, 
			Func0<String> language,
			final Func0<String> onCrash) {
		this.onCrash = onCrash;
		this.language = language;
		setTitle("Error");
		this.commandLine = commandLine;
		this.version = version;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		originalErr = System.err;
		
		copy = new JButton("Copy to clipboard");
		copy.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int s1 = area.getSelectionStart();
				int s2 = area.getSelectionEnd();

				area.selectAll();
				area.copy();
				
				area.setSelectionStart(s1);
				area.setSelectionEnd(s2);
			}
		});
		clear = new JButton("Clear text");
		clear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				area.setText("");
				first = true;
			}
		});
		report = new JButton("Report issue");
		report.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doReport();
			}
		});
		
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		p.add(Box.createHorizontalGlue());
		p.add(copy);
		p.add(Box.createHorizontalStrut(10));
		p.add(clear);
		p.add(Box.createHorizontalStrut(10));
		p.add(report);
		p.add(Box.createHorizontalGlue());
		
		area = new JTextArea();
		area.setEditable(false);
		area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		JScrollPane sp = new JScrollPane(area);
		
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(sp, BorderLayout.CENTER);
		cp.add(p, BorderLayout.NORTH);
		
		setSize(640, 480);
		setLocationRelativeTo(null);
		
		System.setErr(new PrintStream(new OutputStream() {
			@Override
			public void write(final int b) throws IOException {
				write(new byte[] { (byte)b }, 0, 1);
			}
			@Override
			public void write(final byte[] b, final int off, final int len) throws IOException {
				writeData(onCrash, b, off, len);
			}
		}));
	}
	@Override
	public void close() throws IOException {
		System.setErr(originalErr);
		dispose();
	}
	/** 
	 * Write the diagnostic info.
	 * @param out the output
	 */
	void writeDiagnosticInfo(Appender out) {
		out.append("An unexpected error occurred.\r\n");
		out.append("You should consider submitting an error report via the project issue list:\r\n");
		out.append(ISSUE_LIST);
		out.append("\r\n");
		out.append("Please include the following diagnostic information followed by the error stacktrace(s):\r\n");
		out.append(String.format("   Java version: %s%n", System.getProperty("java.version")));
		out.append(String.format("   Java vendor: %s (%s)%n", System.getProperty("java.vendor"), System.getProperty("java.vendor.url")));
		out.append(String.format("   Java class version: %s%n", System.getProperty("java.class.version")));
		out.append(String.format("   Operating system: %s, %s, %s%n", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("os.version")));
		out.append(String.format("   Game version: %s%n", version));
		out.append(String.format("   Command line: %s%n", Arrays.toString(commandLine)));
		out.append(String.format("   Available memory: %s MB%n", Runtime.getRuntime().freeMemory() / 1024 / 1024));
		out.append(String.format("   Maximum memory: %s MB%n", Runtime.getRuntime().maxMemory() / 1024 / 1024));
		out.append(String.format("   Parallelism: %s%n", Runtime.getRuntime().availableProcessors()));
		out.append(String.format("   Language: %s%n", language.invoke()));
		out.append(String.format("   Date and time: %s%n", XElement.formatDateTime(new Date())));
		out.append("----\r\n");
	}
	/**
	 * Write the data to the console and text area.
	 * @param onCrash the optional crash event handler
	 * @param b the byte array
	 * @param off the offset
	 * @param len the length
	 */
	protected void writeData(final Func0<String> onCrash, final byte[] b,
			final int off, final int len) {
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
					writeDiagnosticInfo(new Appender() {
						@Override
						public Appender append(Object o) {
							areaAppend(String.valueOf(o));
							return this;
						}
					});
					crashSave(onCrash);
				}
				areaAppend(new String(b2, off, len));
				// print crash log
			}

			/**
			 * Save crash situation.
			 * @param onCrash the crash action
			 */
			protected void crashSave(final Func0<String> onCrash) {
				if (onCrash != null) {
					String s = null;
					try {
						s = onCrash.invoke();
					} catch (Throwable t) {
						final Throwable t2 = t;
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								t2.printStackTrace();
							}
						});
					}
					areaAppend("A crash save may have been created. Please attach it in the issue report (zipped).\r\n");
					if (s != null) {
						areaAppend(s);
						areaAppend("\r\n");
					}
				}
			}
		});
	}
	/**
	 * Append data to the text area and the log file.
	 * @param s the text
	 */
	void areaAppend(String s) {
		try (PrintWriter out = new PrintWriter(new FileWriter("open-ig.log", true))) {
			out.print(s);
		} catch (Throwable t) {
			// ignored
		}
		area.append(s);
	}
	/** Open the project issue page. */
	void doReport() {
		try {
			Desktop d = Desktop.getDesktop();
			d.browse(new URI(ISSUE_LIST));
		} catch (Throwable ex) {
			doReportDialog();
		}
	}
	/** Show the URL in a message dialog. */ 
	void doReportDialog() {
		JOptionPane.showInputDialog("<html>Your platform doesn't support opening a web page from Java.<br>Please navigate to the URL below manually:", ISSUE_LIST);
	}
	/**
	 * @return the current contents of the crash log or null if still empty
	 */
	public String getCrashLog() {
		final StringBuilder b = new StringBuilder();
		writeDiagnosticInfo(new Appender() {
			@Override
			public Appender append(Object o) {
				b.append(o);
				return this;
			}
		});
		for (String s : Exceptions.HISTORY.keySet()) {
			b.append(s).append("\r\n");
		}
		return b.length() > 0 ? b.toString() : null;
	}
}
