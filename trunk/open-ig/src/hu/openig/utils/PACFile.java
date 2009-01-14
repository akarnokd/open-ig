/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.utils;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;

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
 * Class to handle Imperium Galactica's PAC files.
 * <p>
 * The format:<br>
 * <pre>
 * WORD itemcount
 * Entries {
 *   0x00 BYTE filename_length
 *        BYTE(* filename_length) filename
 *        BYTE(* 13 - filename_length) 0x2E padding
 *   0x0E WORD data_length
 *   0x10 DWORD data_absolute_offset
 * }
 * </pre> 
 *
 */
public class PACFile {
	static class PACEntry {
		public String filename;
		public long offset;
		public int size;
		public byte[] icon;
	}
	static class ImageRenderer extends DefaultListCellRenderer {
	    /**
		 * 
		 */
		private static final long serialVersionUID = 6224396689689734489L;

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
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		DefaultListModel lm = new DefaultListModel();
		final JList lst = new JList(lm);

		
		File fd = new File("c:\\letoltes\\ig11\\data");
		for (File f : fd.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toUpperCase().endsWith(".PAC");
			}
		})) {
			RandomAccessFile fin = new RandomAccessFile(f, "r");
			byte[] entry = new byte[20];
			fin.readFully(entry, 0, 2);
			int count = (entry[1] & 0xFF) << 8 | (entry[0] & 0xFF);
			for (int i = 0 ; i < count; i++) {
				PACEntry pe = new PACEntry();
				fin.readFully(entry);
				pe.filename = new String(entry, 1, entry[0], "ISO-8859-1");
				pe.size = (entry[0x0E] & 0xFF) | (entry[0x0F] & 0xFF) << 8;
				pe.offset = (entry[0x10] & 0xFF) | (entry[0x11] & 0xFF) << 8 | (entry[0x12] & 0xFF) << 16 | (entry[0x13] & 0xFF) << 24;
				if (pe.filename.toUpperCase().endsWith(".PCX")) {
					long p = fin.getFilePointer();
					fin.seek(pe.offset);
					pe.icon = new byte[pe.size];
					fin.readFully(pe.icon);
					
//					FileOutputStream fout = new FileOutputStream("pcx/" + f.getName()+ "_" + pe.filename);
//					fout.write(pe.icon);
//					fout.close();
					
					fin.seek(p);
					lm.addElement(pe);
				}
			}
			fin.close();
		}
		
		// create GUI for 
		JFrame fr = new JFrame("Contents of " + fd);
		fr.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		lst.setCellRenderer(new ImageRenderer());
		fr.setLayout(new BorderLayout());
		fr.getContentPane().add(new JScrollPane(lst), BorderLayout.WEST);
		final JLabel imgLabel = new JLabel();
		fr.getContentPane().add(new JScrollPane(imgLabel), BorderLayout.CENTER);
		lst.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					PACEntry pe = (PACEntry)lst.getSelectedValue();
					if (pe != null) {
						ImageIcon ii = new ImageIcon(PCXImage.parse(pe.icon, -1));
						imgLabel.setIcon(ii);
						imgLabel.setText(String.format("%d x %d", ii.getIconWidth(), ii.getIconHeight()));
					}
				}
			}
		});
		lst.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fr.setSize(650, 490);
		fr.setVisible(true);
	}

}
