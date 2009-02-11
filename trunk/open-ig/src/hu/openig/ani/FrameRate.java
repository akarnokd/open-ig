/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ani;


import hu.openig.ani.SpidyAniFile.Block;
import hu.openig.ani.SpidyAniFile.Sound;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Utility program to calculate the frame rate for an animation based on the sound length.
 * @author karnokd, 2009.02.11.
 * @version $Revision 1.0$
 */
public class FrameRate {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		SpidyAniFile saf = new SpidyAniFile();
		FileInputStream fin = new FileInputStream("c:/games/ig/message/digi206.ani");
		saf.open(fin);
		int len = 0;
		try {
			saf.load();
			while (true) {
				Block b = saf.next();
				if (b instanceof Sound) {
					len += b.data.length;
				}
			}
		} catch (EOFException ex) {
			
		}
		fin.close();
		System.out.printf("Framerate 1 : %.4f %d %.4f%n", saf.getFrameCount() * 22050.0 / len, saf.getFrameCount(),  len / 22050.0);
		System.out.printf("Framerate 2 : %.4f, %d %.4f%n", (saf.getFrameCount() + 1) * 22050.0 / len, saf.getFrameCount() + 1, len / 22050.0);
	}

}
