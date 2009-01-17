/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.utils;

/**
 * Class to manage surface map description of the *.MAP files in MAP.PAC.
 * @author karnokd, 2009.01.16.
 * @version $Revision 1.0$
 */
public class SurfaceMap {
	public static void main(String[] args) {
		byte[] data = IOUtils.load("MAP.PAC MAP_G1.MAP");
		int max = 0;
		int min = 255;
		for (int i = 4; i < data.length; i += 2) {
			int b = data[i] & 0xFF;
			if (b > max && b != 255) {
				max = b;
			}
			if (b < min) {
				min = b;
			}
		}
		System.out.printf("%d - %d%n", min, max);
	}
}
