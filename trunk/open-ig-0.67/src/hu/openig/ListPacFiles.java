/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig;

import hu.openig.res.Texts;
import hu.openig.utils.PACFile;
import hu.openig.utils.PCXImage;
import hu.openig.utils.PACFile.PACEntry;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Utility program to list image contents of all pack files in a directory.
 * @author karnokd
 */
public final class ListPacFiles {
	/** Private constructor. */
	private ListPacFiles() {
		// utility program
	}
	/**
	 * Image renderer for list cells.
	 * @author karnokd
	 */
	static class ImageRenderer extends DefaultListCellRenderer {
	    /** */
		private static final long serialVersionUID = 6224396689689734489L;
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Component getListCellRendererComponent(JList list,
	                                                  Object value,
	                                                  int index,
	                                                  boolean isSelected,
	                                                  boolean cellHasFocus) {
	        // for default cell renderer behavior
	        Component c = super.getListCellRendererComponent(list, value,
	                                       index, isSelected, cellHasFocus);
	        // set icon for cell image
	        //((JLabel)c).setIcon(((PACEntry)value).icon);
	        ((JLabel)c).setText(((PACEntry)value).filename);
	        return c;
	    }
	}
	/**
	 * The main program. Scans a directory given by a parameter or the default location.
	 * @param args the arguments: target directory
	 * @throws Exception if any database error occurs
	 */
	public static void main(String[] args) throws Exception {
		DefaultListModel lm = new DefaultListModel();
		final JList lst = new JList(lm);
		List<PACEntry> lpe = new ArrayList<PACEntry>();
		
		File fd = new File(args.length > 0 ? args[0] : "c:\\games\\ighu\\data");
		for (File f : fd.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toUpperCase().endsWith(".PAC");
			}
		})) {
			for (PACEntry pe : PACFile.parseFully(f)) {
				pe.filename = f.getName() + " " + pe.filename;
				lpe.add(pe);
			}
		}
		Collections.sort(lpe, new Comparator<PACEntry>() {
			@Override
			public int compare(PACEntry o1, PACEntry o2) {
				return o1.filename.compareTo(o2.filename);
			}
		});
		for (PACEntry e : lpe) {
			lm.addElement(e);
		}
		// create GUI for 
		JFrame fr = new JFrame("Contents of " + fd);
		fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		lst.setCellRenderer(new ImageRenderer());
		fr.setLayout(new BorderLayout());
		fr.getContentPane().add(new JScrollPane(lst), BorderLayout.WEST);
		final JLabel imgLabel = new JLabel();
		imgLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					PACEntry pe = (PACEntry)lst.getSelectedValue();
					if (pe != null) {
						try {
							FileOutputStream fout = new FileOutputStream(pe.filename);
							try {
								fout.write(pe.data);
							} finally {
								fout.close();
							}
						} catch (IOException ex) {
							
						}
					}					
				} else
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
					PACEntry pe = (PACEntry)lst.getSelectedValue();
					if (pe != null) {
						if (pe.filename.toUpperCase().endsWith(".PCX")) {
							ImageIcon ii = new ImageIcon(PCXImage.parse(pe.data, -2));
							imgLabel.setIcon(ii);
						} else {
							imgLabel.setIcon(null);
						}
					}
				}
			}
		});
		fr.getContentPane().add(new JScrollPane(imgLabel), BorderLayout.CENTER);
		lst.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					PACEntry pe = (PACEntry)lst.getSelectedValue();
					if (pe != null && pe.filename.toUpperCase().endsWith(".PCX")) {
						ImageIcon ii = new ImageIcon(PCXImage.parse(pe.data, -1));
						imgLabel.setIcon(ii);
						imgLabel.setText(String.format("%d x %d", ii.getIconWidth(), ii.getIconHeight()));
					} else 
					if (pe != null) {
						imgLabel.setIcon(null);
						try {
							imgLabel.setText("<html>" + Texts.fixHungarianChars(new String(pe.data, "ISO-8859-1")).replaceAll("\r", "<br>"));
						} catch (UnsupportedEncodingException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		});
		lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fr.setSize(650, 490);
		fr.setLocationRelativeTo(null);
		fr.setVisible(true);
	}

}
