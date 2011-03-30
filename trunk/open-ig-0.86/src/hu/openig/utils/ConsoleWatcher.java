/*
 * Copyright 2008-2011, David Karnok 
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
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

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
	/** The out watcher thread. */
	Thread outWatcher;
	/** The error watcher thread. */
	Thread errWatcher;
	/** The text area for data. */
	JTextArea area;
	/** Original stream. */
	private PrintStream originalOut;
	/** Original error. */
	private PrintStream originalErr;
	/**
	 * Create the gui.
	 */
	public ConsoleWatcher() {
		setTitle("Console Watcher");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		originalOut = System.out;
		originalErr = System.err;
		
		area = new JTextArea();
		area.setEditable(false);
		area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		JScrollPane sp = new JScrollPane(area);
		
		getContentPane().add(sp);
		
		setSize(640, 480);
		setLocationRelativeTo(null);
		
//		PipedOutputStream outPO = new PipedOutputStream();
		PipedOutputStream errPO = new PipedOutputStream();
		
		try {
//			final PipedInputStream outPI = new PipedInputStream(outPO);
//			System.setOut(new PrintStream(outPO));
//			outWatcher = new Thread(new Runnable() {
//				@Override
//				public void run() {
//					readEverything(outPI, originalOut);
//				}			
//			});

			final PipedInputStream errPI = new PipedInputStream(errPO);
			System.setErr(new PrintStream(errPO));
			errWatcher = new Thread(new Runnable() {
				@Override
				public void run() {
					readEverything(errPI, originalErr);
				}			
			});
			
//			outWatcher.start();
			errWatcher.start();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Read everything from the piped input stream.
	 * @param pin the piped input stream
	 * @param original the original output stream
	 */
	void readEverything(PipedInputStream pin, OutputStream original) {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				final int c = pin.read();
				if (c >= 0) {
					original.write(c);
				}
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if (!isVisible()) {
							setVisible(true);
						}
						area.append(Character.toString((char)c));
					}
				});
			}
		} catch (IOException ex) {
			
		}
	}
	@Override
	public void close() throws IOException {
		if (errWatcher != null) {
			errWatcher.interrupt();
		}
		if (outWatcher != null) {
			outWatcher.interrupt();
		}
		System.setErr(originalErr);
		System.setOut(originalOut);
		dispose();
	}
}
