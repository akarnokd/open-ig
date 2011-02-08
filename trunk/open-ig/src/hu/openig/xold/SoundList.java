/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold;

import hu.openig.sound.AudioThread;
import hu.openig.utils.IOUtils;

import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Utility program to play sound files from Imperium Galactica's SOUND directory.
 * @author karnokd, 2009.01.18.
 * @version $Revision 1.0$
 */
@SuppressWarnings({ "unchecked", "rawtypes" }) // since 6u20 or so, most jlist models became generic, but if I put the type in, it won't compile on older versions
public final class SoundList {
	/** The audio playback thread. */
	static AudioThread ad;
	/** The IG root directory. */
	static String root;
	/** Private constructor. */
	private SoundList() {
		// utility program
	}
	/** 
	 * Fill list with filenames from the SOUND directory. 
	 * @param lst the JList object to fill in 
	 */
	private static void populateList(JList lst) {
		DefaultListModel mdl = new DefaultListModel();
		File[] files = new File(root + "/SOUND").listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.getName().toUpperCase().endsWith(".SMP")) {
					mdl.addElement(root + "/SOUND/" + f.getName());
				}
			}
		}
		lst.setModel(mdl);
	}
	/**
	 * Action method for list selection change event.
	 * @param e the event
	 */
	private static void doSelectionChange(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			JList lst = (JList)e.getSource();
			String s = (String)lst.getSelectedValue();
			byte[] data = IOUtils.load(s);
			ad.submit(data);
		}
	}
	/**
	 * Main program.
	 * @param args arguments, optionally one argument containing the root directory for IG
	 */
	public static void main(String[] args) {
		root = "./";
		if (args.length > 0) {
			root = args[0];
		}
		ad = new AudioThread();
		ad.start();
		ad.startPlaybackNow();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JFrame fm = new JFrame("Sound player");
				fm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				JList lst = new JList();
				lst.addListSelectionListener(new ListSelectionListener() {
					@Override
					public void valueChanged(ListSelectionEvent e) {
						doSelectionChange(e);
					}
				});
				populateList(lst);
				fm.getContentPane().add(new JScrollPane(lst));
				fm.pack();
				fm.setVisible(true);
			}
		});
	}
}
