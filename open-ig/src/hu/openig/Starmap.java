/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

import hu.openig.gfx.CommonGFX;
import hu.openig.gfx.StarmapGFX;
import hu.openig.gfx.StarmapRenderer;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;


/**
 * @author karnokd, 2009.01.05.
 * @version $Revision 1.0$
 */
public class Starmap {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String igroot = ".";
		if (args.length > 0) {
			igroot = args[0];
		}
		final StarmapRenderer smr = new StarmapRenderer(new StarmapGFX(igroot), new CommonGFX(igroot));
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame fm = new JFrame("Open-IG: Starmap");
				fm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				Container c = fm.getContentPane();
				GroupLayout gl = new GroupLayout(c);
				c.setLayout(gl);
				gl.setHorizontalGroup(gl.createSequentialGroup()
					.addComponent(smr, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				);
				gl.setVerticalGroup(
					gl.createSequentialGroup()
					.addComponent(smr, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
