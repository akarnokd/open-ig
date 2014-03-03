/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools;

import hu.openig.utils.PACFile;
import hu.openig.utils.PACFile.PACEntry;

import java.io.File;
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
		String src = "c:/games/igru/data";
		String dst = "c:/games/igru/data/text";
		new File(dst).mkdirs();
		List<PACFile.PACEntry> pf = PACFile.parseFully(src + "/text.pac");
		for (PACEntry pe : pf) {
			try (FileOutputStream out = new FileOutputStream(dst + "/" + pe.filename)) {
				out.write(pe.data);
			}
		}
	}

}
