/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig;

import hu.openig.gfx.CommonGFX;
import hu.openig.gfx.PlanetGFX;
import hu.openig.gfx.PlanetRenderer;
import hu.openig.sound.UISounds;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Planetary surface renderer test file.
 * @author karnokd, 2009.01.14.
 * @version $Revision 1.0$
 */
public class Planet {
	/** 
	 * Calculates the RGB average of two RGB color.
	 * @param c1 the first color
	 * @param c2 the second color
	 * @param scale the weight (0..1) of the first color
	 * @return the averaged RGB color
	 */
	protected static int average(int c1, int c2, float scale) {
		int r = (int)((c1 & 0xFF0000) * scale + (c2 & 0xFF0000) * (1 - scale));
		int g = (int)((c1 & 0xFF00) * scale + (c2 & 0xFF00) * (1 - scale));
		int b = (int)((c1 & 0xFF) * scale  + (c2 & 0xFF) * (1 - scale));
		return (r & 0xFF0000) | (g & 0xFF00) | (b & 0xFF);
	}
	/**
	 * Main test program
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String root = ".";
		if (args.length > 0) {
			root = args[0];
		}
		UISounds uis = new UISounds(root);
		final PlanetRenderer pr = new PlanetRenderer(new PlanetGFX(root), new CommonGFX(root), uis);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
				public void run() {
					JFrame fm = new JFrame("Open-IG: Planet");
					fm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					Container c = fm.getContentPane();
					GroupLayout gl = new GroupLayout(c);
					c.setLayout(gl);
					gl.setHorizontalGroup(gl.createSequentialGroup()
						.addComponent(pr, 640, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					);
					gl.setVerticalGroup(
						gl.createSequentialGroup()
						.addComponent(pr, 480, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
