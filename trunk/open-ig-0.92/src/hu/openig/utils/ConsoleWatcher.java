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
	/** The error watcher thread. */
	Thread errWatcher;
	/** The text area for data. */
	JTextArea area;
	/** Original error. */
	private PrintStream originalErr;
	/**
	 * Create the gui.
	 */
	public ConsoleWatcher() {
		setTitle("Error");
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
						area.append(new String(b2, off, len));
					}
				});
			}
		}));
	}
	@Override
	public void close() throws IOException {
		if (errWatcher != null) {
			errWatcher.interrupt();
		}
		System.setErr(originalErr);
		dispose();
	}
}
