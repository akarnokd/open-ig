/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.PACFile;
import hu.openig.utils.PACFile.PACEntry;

import java.io.FileOutputStream;
import java.util.List;

/**
 * Unpacks the original PAC files.
 * @author akarnokd, 2012.04.20.
 */
public final class Unpack {
	/** Utility class. */
	private Unpack() { }
	/**
	 * @param args no arguments
	 * @throws Exception ignored
	 */
	public static void main(String[] args) throws Exception {
		List<PACFile.PACEntry> pf = PACFile.parseFully("g:/games/ighu/data/text.pac");
		for (PACEntry pe : pf) {
			FileOutputStream out = new FileOutputStream("g:/games/ighu/data/" + pe.filename);
			out.write(pe.data);
			out.close();
		}
	}

}
