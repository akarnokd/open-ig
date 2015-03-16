/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools.ani;

import hu.openig.tools.ani.Framerates.Rates;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog.ModalityType;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * List which contains all ANI files from the currently selected directory - 1.
 * @author karnok, 2009.08.21.
 * @version $Revision 1.0$
 */
public class AnimPlayList extends JFrame {
	/** */
	private static final long serialVersionUID = -6982010495745618213L;
	/** The directory. */
	JTextField directory;
	/** The language code. */
	JComboBox<String> languageCode;
	/** The table model. */
	AnimFilesModel model;
	/** The table. */
	JTable table;
	/** The scan button. */
	JButton scan;
	/** The count for entries. */
	JLabel count;
	/** The save directory. */
	JTextField saveDirectory;
	/** Save selected videos as PNGs and WAV files to a directory. */
	JButton savePNGWAV;
	/** Save batch into subdirectories? */
	JCheckBox useSubdirs;
	/**
	 * Animation list table model.
	 * @author karnok, 2009.08.21.
	 * @version $Revision 1.0$
	 */
	class AnimFilesModel extends AbstractTableModel {
		/** */
		private static final long serialVersionUID = 866676136521268534L;
		/** the list of files. */
		final List<File> rows = new ArrayList<>();
		/** The column names. */
		final String[] colNames = {
			"Name", "Size", "Path", "Date", "FPS", "Delay"
		};
		/** The column types. */
		final Class<?>[] colTypes = {
			String.class, Long.class, String.class, String.class, Double.class, Integer.class
		};
		/** The framerates for the various animations. */
		final Framerates fr = new Framerates();
		/** The formatter for dates. */
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return colTypes[columnIndex];
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getColumnCount() {
			return 6;
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public int getRowCount() {
			return rows.size();
		}
		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			File f = rows.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return f.getName();
			case 1:
				return f.length();
			case 2:
				return f.getParent();
			case 3:
				return sdf.format(new Timestamp(f.lastModified()));
			case 4:
				Rates r = fr.getRates(f.getName(), languageCode.getSelectedIndex() + 1);
				if (r != null) {
					return r.fps;
				}
				return null;
			case 5:
				r = fr.getRates(f.getName(), languageCode.getSelectedIndex() + 1);
				if (r != null) {
					return r.delay;
				}
				return null;
			default:
			}
			return null;
		}
	}
	/** 
	 * Constructor. Initializes the layout. 
	 * @param root the base directory
	 */
	public AnimPlayList(File root) {
		setTitle("Playlist");
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		directory = new JTextField();
		directory.setText(root.getAbsolutePath());
		languageCode = new JComboBox<>(new String[] { "English", "Hungarian" });
		languageCode.setToolTipText("Select the language code to be used for displaying the FPS/Delay values");
		scan = new JButton("Scan");
		AnimPlay.setAL(scan, "scanDirectory", this);

		model = new AnimFilesModel();
		table = new JTable(model);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				doMouseClicked(e);
			}
		});
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				doKeyTyped(e);
			}
		});
		table.setAutoCreateRowSorter(true);
		JScrollPane sp = new JScrollPane(table);
		count = new JLabel("Entries: 0");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		saveDirectory = new JTextField(root.getAbsolutePath() + "/savedanim");
		savePNGWAV = new JButton("Save PNG & WAV");
		AnimPlay.setAL(savePNGWAV, "doSavePNGWAV", this);
		
		useSubdirs = new JCheckBox("Subdirectories", true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(directory)
				.addComponent(languageCode, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(scan)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(saveDirectory)
				.addComponent(useSubdirs)
				.addComponent(savePNGWAV)
			)
			.addComponent(sp)
			.addComponent(count)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(directory)
				.addComponent(languageCode)
				.addComponent(scan)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(saveDirectory)
				.addComponent(useSubdirs)
				.addComponent(savePNGWAV)
	        )
			.addComponent(sp)
			.addComponent(count)
		);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				scanDirectory();
				pack();
			}
		});
	}
	/**
	 * Action on key typed within the table.
	 * @param e the keyboard event
	 */
	protected void doKeyTyped(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE || e.getKeyCode() == KeyEvent.VK_ENTER) {
			doPlaySelected();
			e.consume();
		} else
		if (e.getKeyCode() == KeyEvent.VK_S) {
			AnimPlay.menuStop.doClick();
		} else
		if (e.getKeyCode() == KeyEvent.VK_D) {
			AnimPlay.menuReplay.doClick();
		}
	}
	/**
	 * Action on mouse click within the table.
	 * @param e the mouse event
	 */
	protected void doMouseClicked(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
			doPlaySelected();
		}
	}
	/** Scan the current directory for ANI files recursively. */
	void scanDirectory() {
		model.rows.clear();
		scan.setEnabled(false);
		final File dir = new File(directory.getText());
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			final List<File> files = new LinkedList<>();
			@Override
			protected Void doInBackground() throws Exception {
				try {
					processFiles(dir, files);
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return null;
			}
			@Override
			protected void done() {
				model.rows.addAll(files);
				model.fireTableDataChanged();
				scan.setEnabled(true);
				autoResizeColWidth(table, model);
//				pack();
				setCount();
			}
		};
		worker.execute();
	}
	/** Set the list count. */
	void setCount() {
		count.setText(String.format("Entries: %d", model.rows.size()));
	}
	/**
	 * Processes files in the given directory recursively.
	 * @param dir the director
	 * @param files the files
	 */
	void processFiles(File dir, List<File> files) {
		File[] fa = dir.listFiles();
		if (fa != null) {
			for (File f : fa) {
				if (f.isDirectory() && f.canRead()) {
					processFiles(f, files);
				} else
				if (f.isFile() && f.canRead() && f.getName().toUpperCase(Locale.ENGLISH).endsWith(".ANI")) {
					files.add(f);
				}
			}
		}
	}
	/**
	 * Play the selected entry from the list.
	 */
	private void doPlaySelected() {
		if (table.getSelectedRow() >= 0) {
			final File f = model.rows.get(table.convertRowIndexToModel(table.getSelectedRow()));
			if (AnimPlay.menuStop.isEnabled()) {
				AnimPlay.menuStop.doClick();
			}
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
				@Override
				protected Void doInBackground() throws Exception {
					try {
						while (AnimPlay.stop) {
							TimeUnit.MILLISECONDS.sleep(100);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					return null;
				}
				@Override
				protected void done() {
					AnimPlay.createFrame(f);
					AnimPlay.doPlayFile(f.getAbsolutePath());
				}
			};
			worker.execute();
		}
	}
	/**
	 * Resizes the table columns based on the column and data preferred widths.
	 * @param table the original table
	 * @param model the data model
	 * @return the table itself
	 */
    public static JTable autoResizeColWidth(JTable table, AbstractTableModel model) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(model);
 
        int margin = 5;
 
        for (int i = 0; i < table.getColumnCount(); i++) {
            int                     vColIndex = i;
            DefaultTableColumnModel colModel  = (DefaultTableColumnModel) table.getColumnModel();
            TableColumn             col       = colModel.getColumn(vColIndex);
            int                     width;
 
            // Get width of column header
            TableCellRenderer renderer = col.getHeaderRenderer();
 
            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }
 
            Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
 
            width = comp.getPreferredSize().width;
 
            // Get maximum width of column data
            for (int r = 0; r < table.getRowCount(); r++) {
                renderer = table.getCellRenderer(r, vColIndex);
                comp     = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false,
                        r, vColIndex);
                width = Math.max(width, comp.getPreferredSize().width);
            }
 
            // Add margin
            width += 2 * margin;
 
            // Set the width
            col.setPreferredWidth(width);
        }
 
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(
            SwingConstants.LEFT);
 
        return table;
    }
    /**
     * Save PNGs and WAVs for the selected files.
     */
    void doSavePNGWAV() {
    	final int[] idxs = table.getSelectedRows();
    	final File[] files = new File[idxs.length];
    	for (int i = 0; i < idxs.length; i++) {
    		files[i] = model.rows.get(table.convertRowIndexToModel(idxs[i]));
    	}
		final ProgressFrame pf = new ProgressFrame("Save as PNG & WAV: ", this);
		pf.setMax(idxs.length);
		final String targetStr = saveDirectory.getText();
		final boolean usesubdirs = useSubdirs.isSelected();
    	Thread t = new Thread("SaveAsPNGWAV") {
			@Override
			public void run() {
				for (int i = 0; i < files.length; i++) {
					if (pf.isCancelled()) {
						break;
					}
					final int j = i + 1;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							pf.setCurrent(j, "Progress: " + (j) + " / " + files.length + ": " + files[j - 1]);
						}
					});
					try {
						File target = new File(targetStr + "/" + files[i].getName());
						// put them into a subdirectory?
						if (usesubdirs) {
							target = new File(targetStr + "/" + files[i].getName() + "/" + files[i].getName());
						}
						if (target.getParentFile() != null) {
							target.getParentFile().mkdirs();
						}
						AnimPlay.saveAsPNGWorker(files[i], target, false, AnimPlayList.this).get();
						if (pf.isCancelled()) {
							break;
						}
						target = new File(targetStr + "/" + files[i].getName() + ".wav");
						if (usesubdirs) {
							target = new File(targetStr + "/" + files[i].getName() + "/" + files[i].getName() + ".wav");
						}
						AnimPlay.saveAsWavWorker(files[i], target, false, AnimPlayList.this).get();
					} catch (Throwable t) {
						t.printStackTrace();
					}
					// delay window close a bit further
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							pf.dispose();
						}
					});
				}
			}
    	};
		pf.setModalityType(ModalityType.MODELESS);
    	pf.setVisible(true);
    	t.start();
    }
}
