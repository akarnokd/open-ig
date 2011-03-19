/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

/**
 * @author akarnokd, Mar 19, 2011
 */
public class SnapBug extends JFrame {
	/** */
	private static final long serialVersionUID = 2331770914890030366L;
	/** Constructor. */
	public SnapBug() {
		super("Snap bug");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel p = new JPanel();
		p.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		p.setPreferredSize(new Dimension(640, 480));
		p.setBackground(Color.GREEN);
		getContentPane().add(p, BorderLayout.CENTER);
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				System.out.printf("%d x %d%n", getWidth(), getHeight());
			}
		});
		pack();
	}
	/**
	 * @param args no args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SnapBug b = new SnapBug();
				b.setVisible(true);
			}
		});
	}
}
