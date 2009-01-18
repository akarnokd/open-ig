/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig;

import java.io.File;

import javax.swing.JOptionPane;

/**
 * The main entry point for now.
 * @author karnokd
 *
 */
public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception {
		String root = ".";
		if (args.length > 0) {
			root = args[0];
		}
		File file = new File(root + "/IMPERIUM.EXE");
		if (!file.exists()) {
			JOptionPane.showMessageDialog(null, "Please place this program into the Imperium Galactica directory or specify the location via the first command line parameter.");
			return;
		}
		Starmap.main(args);
		Planet.main(args);
	}

}
