/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import hu.openig.v1.core.Configuration;
import hu.openig.v1.core.ResourceLocator;
import hu.openig.v1.model.GalaxyModel;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.GroupLayout.Alignment;
import javax.swing.table.AbstractTableModel;

/**
 * Map editor.
 * @author karnokd, 2010.09.08.
 * @version $Revision 1.0$
 */
public class MapEditor extends JFrame {
	/** */
	private static final long serialVersionUID = -5949479655359917254L;
	/** The main resource locator. */
	ResourceLocator rl;
	/** The horizontal split between the tool listing and the rendering. */
	JSplitPane split;
	/** The tool splitting. */
	JSplitPane toolSplit;
	/** The map renderer. */
	MapRenderer renderer;
	/** The galaxy model. */
	GalaxyModel galaxyModel;
	/** A window with indeterminate backgound progress indicator. */
	static class BackgroundProgress extends JDialog {
		/** */
		private static final long serialVersionUID = -5795494140780969300L;
		/** The text label. */
		JLabel label;
		/** The progress indicator. */
		JProgressBar progress;
		/** Build the dialog. */
		public BackgroundProgress() {
			setTitle("Work in background");
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
			setModal(true);
			
			label = new JLabel("Working...");
			progress = new JProgressBar();
			progress.setIndeterminate(true);
			
			Container c = getContentPane();
			GroupLayout gl = new GroupLayout(c);
			c.setLayout(gl);
			gl.setAutoCreateContainerGaps(true);
			gl.setAutoCreateGaps(true);
			
			gl.setHorizontalGroup(gl.createParallelGroup(Alignment.CENTER)
					.addComponent(label).addComponent(progress));
			gl.setVerticalGroup(gl.createSequentialGroup().addComponent(label).addComponent(progress));
			
			pack();
			setResizable(false);
		}
		/**
		 * Set the label text.
		 * @param text the text
		 */
		public void setLabelText(String text) {
			label.setText(text);
		}
	}
	/** Load the resource locator. */
	void loadResourceLocator() {
		final BackgroundProgress bgp = new BackgroundProgress();
		bgp.setLocationRelativeTo(this);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			private ResourceLocator rl;
			private GalaxyModel  galaxyMap;
			@Override
			protected Void doInBackground() throws Exception {
				final Configuration config = new Configuration("open-ig-config.xml");
				config.load();
				rl = config.newResourceLocator();
				galaxyMap = new GalaxyModel();
				galaxyMap.processGalaxy(rl, "en", "campaign/main/galaxy");
				return null;
			}
			@Override
			protected void done() {
				bgp.dispose();
				MapEditor.this.rl = rl;
				MapEditor.this.galaxyModel = galaxyMap;
			}
		};
		worker.execute();
		bgp.setVisible(true);
	}
	/**
	 * Program entry point.
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				MapEditor editor = new MapEditor();
				editor.setLocationRelativeTo(null);
				editor.setVisible(true);
			}
		});
	}
	/** Build the GUI. */
	public MapEditor() {
		super("Open-IG Map Editor");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		toolSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		renderer = new MapRenderer();
		
		split.setLeftComponent(toolSplit);
		split.setRightComponent(renderer);
		
		setSize(1024, 768);

		split.setDividerLocation(200);
		toolSplit.setDividerLocation(400);
		
		getContentPane().add(split);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				loadResourceLocator();
			}
		});
	}
	/** The map renderer. */
	static class MapRenderer extends JComponent {
		/** */
		private static final long serialVersionUID = 5058274675379681602L;
		/** Preset. */
		public MapRenderer() {
		}
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;

			g2.setColor(new Color(96, 96, 96));
			g2.fillRect(0, 0, getWidth(), getHeight());
		}
	}
	/**
	 * A tile entry.
	 * @author karnokd
	 */
	static class TileEntry {
		/** The preview image. */
		public BufferedImage image;
		public BufferedImage preview;
		public int id;
		public int width;
		public int height;
		public String name;
		public String surface;
	}
	/**
	 * A tile list table model.
	 * @author karnokd
	 */
	static class TileList extends AbstractTableModel {
		/** The column names. */
		final String[] colNames = { "Preview", "Size", "Name", "Surface" };
		/** The column classes. */
		final Class<?>[] colClasses = { Image.class, String.class, String.class, String.class };
		/** The list of rows. */
		public final List<TileEntry> rows = new ArrayList<TileEntry>();
		@Override
		public int getColumnCount() {
			return colNames.length;
		}
		@Override
		public int getRowCount() {
			return rows.size();
		}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			TileEntry te = rows.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return te.preview;
			case 1:
				return te.width + " x " + te.height;
			case 2:
				return te.name;
			case 3:
				return te.surface;
			default:
			}
			return null;
		}
		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex != 2;
		}
	}
}
