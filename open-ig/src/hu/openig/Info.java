/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

import hu.openig.gfx.CommonGFX;
import hu.openig.gfx.InformationGFX;
import hu.openig.gfx.InformationRenderer;
import hu.openig.sound.UISounds;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;

/**
 * Planetary surface renderer test file.
 * @author karnokd, 2009.01.14.
 * @version $Revision 1.0$
 */
public class Info {
	/**
	 * Main test program
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String root = ".";
		if (args.length > 0) {
			root = args[0];
		}
		final UISounds uis = new UISounds(root);
		final InformationRenderer pr = new InformationRenderer(new InformationGFX(root), new CommonGFX(root), uis);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
				public void run() {
					JFrame fm = new JFrame("Open-IG: Information");
					fm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					fm.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosed(WindowEvent e) {
							uis.close();
						}
					});
					Container c = fm.getContentPane();
					GroupLayout gl = new GroupLayout(c);
					c.setLayout(gl);
					gl.setHorizontalGroup(
						gl.createParallelGroup(Alignment.CENTER)
						.addComponent(pr, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
					);
					gl.setVerticalGroup(
						gl.createParallelGroup(Alignment.CENTER)
						.addComponent(pr, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
					);
					fm.pack();
					fm.setLocationRelativeTo(null);
					final int inW = fm.getWidth();
					final int inH = fm.getHeight();
					fm.setMinimumSize(new Dimension(inW, inH));
					fm.setVisible(true);
				}
		});
	}

}
