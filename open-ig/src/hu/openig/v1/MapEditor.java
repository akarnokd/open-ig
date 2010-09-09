/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import hu.openig.v1.core.Configuration;
import hu.openig.v1.core.PlanetType;
import hu.openig.v1.core.ResourceLocator;
import hu.openig.v1.core.Tile;
import hu.openig.v1.model.BuildingModel;
import hu.openig.v1.model.BuildingType;
import hu.openig.v1.model.GalaxyModel;
import hu.openig.v1.model.BuildingType.TileSet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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
	/** The surface tiles. */
	JTable surfaceTable;
	/** The surface tile table model. */
	TileList surfaceTableModel;
	/** The building tiles. */
	JTable buildingTable;
	/** The building tile table model. */
	TileList buildingTableModel;
	/** Preview splitter. */
	JSplitPane previewSplit;
	/** The preview image. */
	private ImagePaint preview;
	/** The alpha slider. */
	private JSlider alphaSlider;
	/** The building model. */
	protected BuildingModel buildingModel;
	/** Load the resource locator. */
	void loadResourceLocator() {
		final BackgroundProgress bgp = new BackgroundProgress();
		bgp.setLocationRelativeTo(this);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			private ResourceLocator rl;
			private GalaxyModel  galaxyMap;
			private BuildingModel buildingMap;
			@Override
			protected Void doInBackground() throws Exception {
				final Configuration config = new Configuration("open-ig-config.xml");
				config.load();
				rl = config.newResourceLocator();
				galaxyMap = new GalaxyModel();
				galaxyMap.processGalaxy(rl, "en", "campaign/main/galaxy");
				buildingMap = new BuildingModel();
				buildingMap.processBuildings(rl, "en", "campaign/main/buildings");
				return null;
			}
			@Override
			protected void done() {
				bgp.dispose();
				MapEditor.this.rl = rl;
				MapEditor.this.galaxyModel = galaxyMap;
				MapEditor.this.buildingModel = buildingMap;
				buildTables();
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
		

		split.setDividerLocation(450);
		toolSplit.setDividerLocation(550);

		previewSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		toolSplit.setTopComponent(previewSplit);
		
		surfaceTableModel = new TileList();
		buildingTableModel = new TileList();
		buildingTableModel.colNames[3] = "Race";
		
		surfaceTable = new JTable(surfaceTableModel);
		surfaceTable.setRowHeight(32);
		buildingTable = new JTable(buildingTableModel);
		buildingTable.setRowHeight(32);

		surfaceTable.setAutoCreateRowSorter(true);
		surfaceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		surfaceTable.getColumnModel().getColumn(0).setPreferredWidth(45);
		surfaceTable.getColumnModel().getColumn(1).setPreferredWidth(32);
		
		buildingTable.setAutoCreateRowSorter(true);
		buildingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		buildingTable.getColumnModel().getColumn(0).setPreferredWidth(45);
		buildingTable.getColumnModel().getColumn(1).setPreferredWidth(32);

		surfaceTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				doSelectSurface();
			}
		});
		buildingTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				doSelectBuilding();
			}
		});
		
		JScrollPane sp0 = new JScrollPane(surfaceTable);
		JScrollPane sp1 = new JScrollPane(buildingTable);
		
		
		
		previewSplit.setBottomComponent(sp0);
		toolSplit.setBottomComponent(sp1);
		
		previewSplit.setDividerLocation(300);
		
		
		JPanel previewPanel = new JPanel();
		
		alphaSlider = new JSlider(0, 100, 100);
		alphaSlider.setMajorTickSpacing(10);
		alphaSlider.setMinorTickSpacing(1);
		alphaSlider.setPaintLabels(true);
		alphaSlider.setPaintTicks(true);
		
		alphaSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				preview.setAlpha(1.0f * alphaSlider.getValue() / alphaSlider.getMaximum());
			}
		});
		
		preview = new ImagePaint();
		
		previewPanel.setLayout(new BoxLayout(previewPanel, BoxLayout.PAGE_AXIS));
		previewPanel.add(alphaSlider);
		previewPanel.add(preview);
		previewSplit.setTopComponent(previewPanel);
		
		getContentPane().add(split);
		setSize(1024, 768);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				loadResourceLocator();
			}
		});
		
	}
	/**
	 * Paint a buffered image.
	 * @author karnokd
	 */
	static class ImagePaint extends JComponent {
		/** */
		private static final long serialVersionUID = 3168477795343188089L;
		/** The image. */
		Tile tile;
		/** The brighteness factor. */
		float alpha = 1.0f;
		@Override
		public Dimension getPreferredSize() {
			if (tile != null) {
				return new Dimension(tile.imageWidth + 4, tile.imageHeight + 4);
			}
			return new Dimension(440, 300);
		}
		@Override
		public void paint(Graphics g) {
			if (tile != null) {
				int x = (getWidth() - tile.imageWidth) / 2;
				int y = (getHeight() - tile.imageHeight) / 2;
				tile.alpha = alpha;
				g.drawImage(tile.alphaBlendImage(), x, y, null);
			}
		}
		/**
		 * Set the image from the tile.
		 * @param tile the tile
		 */
		public void setImage(Tile tile) {
			this.tile = tile;
			revalidate();
			repaint();
		}
		/**
		 * Set the brightness value.
		 * @param alpha between 0.0 and 1.0
		 */
		public void setAlpha(float alpha) {
			this.alpha = alpha;
			repaint();
		}
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
		/** A smaller preview image. */
		public ImageIcon preview;
		/** The identifier. */
		public int id;
		/** The user-definable name. */
		public String name;
		/** The surface name. */
		public String surface;
		/** The related tile object. */
		public Tile tile;
	}
	/**
	 * A tile list table model.
	 * @author karnokd
	 */
	static class TileList extends AbstractTableModel {
		/** */
		private static final long serialVersionUID = 1870030483025880490L;
		/** The column names. */
		final String[] colNames = { "Preview", "Size", "Name", "Surface" };
		/** The column classes. */
		final Class<?>[] colClasses = { ImageIcon.class, String.class, String.class, String.class };
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
		public Class<?> getColumnClass(int columnIndex) {
			return colClasses[columnIndex];
		}
		@Override
		public String getColumnName(int column) {
			return colNames[column];
		}
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			TileEntry te = rows.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return te.preview;
			case 1:
				return te.tile.width + " x " + te.tile.height;
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
			return columnIndex == 2;
		}
	}
	/** Build the tile table contents. */
	void buildTables() {
		for (Map.Entry<String, PlanetType> pt : galaxyModel.planetTypes.entrySet()) {
			for (Map.Entry<Integer, Tile> te : pt.getValue().tiles.entrySet()) {
				TileEntry e = new TileEntry();
				e.id = te.getKey();
				e.surface = pt.getKey();
				e.tile = te.getValue();
				e.tile.alpha = 1.0f;
				e.preview = new ImageIcon(scaledImage(e.tile.alphaBlendImage(), 32, 32));
				surfaceTableModel.rows.add(e);
			}
		}
		Collections.sort(surfaceTableModel.rows, new Comparator<TileEntry>() {
			@Override
			public int compare(TileEntry o1, TileEntry o2) {
				int c = o1.surface.compareTo(o2.surface);
				return c != 0 ? c : (o1.id - o2.id);
			}
		});
		
		int idx = 0;
		for (Map.Entry<String, BuildingType> bt : buildingModel.buildings.entrySet()) {
			for (Map.Entry<String, TileSet> tss : bt.getValue().tileset.entrySet()) {
				TileEntry e = new TileEntry();
				e.id = idx;
				e.surface = tss.getKey();
				e.name = bt.getKey();
				e.tile = tss.getValue().normal;
				e.tile.alpha = 1.0f;
				e.preview = new ImageIcon(scaledImage(e.tile.alphaBlendImage(), 32, 32));
				buildingTableModel.rows.add(e);
				
				e = new TileEntry();
				e.id = idx;
				e.surface = tss.getKey();
				e.name = bt.getKey() + " damaged";
				e.tile = tss.getValue().damaged;
				e.tile.alpha = 1.0f;
				e.preview = new ImageIcon(scaledImage(e.tile.alphaBlendImage(), 32, 32));
				buildingTableModel.rows.add(e);
				
			}
			idx++;
		}
		Collections.sort(buildingTableModel.rows, new Comparator<TileEntry>() {
			@Override
			public int compare(TileEntry o1, TileEntry o2) {
				int c = o1.surface.compareTo(o2.surface);
				return c != 0 ? c : (o1.id - o2.id);
			}
		});
		
		surfaceTableModel.fireTableDataChanged();
		buildingTableModel.fireTableDataChanged();
	}
	/**
	 * Create a scaled image.
	 * @param image the base image
	 * @param width the target width
	 * @param height the target height
	 * @return the scaled image
	 */
	BufferedImage scaledImage(BufferedImage image, int width, int height) {
		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = result.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		
		double sx = (double)width / image.getWidth();
		double sy = (double)height / image.getHeight();
		double scale = Math.min(sx, sy);
		int x = (int)((width - image.getWidth() * scale) / 2);
		int y = (int)((height - image.getHeight() * scale) / 2);
		// center the image
		g2.drawImage(image, x, y, (int)(image.getWidth() * scale), (int)(image.getHeight() * scale), null);
		
		g2.dispose();
		return result;
	}
	/** Select a surface image. */
	void doSelectSurface() {
		int idx = surfaceTable.getSelectedRow();
		if (idx >= 0) {
			idx = surfaceTable.convertRowIndexToModel(idx);
			preview.setImage(surfaceTableModel.rows.get(idx).tile);
		}
	}
	/** Select a surface image. */
	void doSelectBuilding() {
		int idx = buildingTable.getSelectedRow();
		if (idx >= 0) {
			idx = buildingTable.convertRowIndexToModel(idx);
			preview.setImage(buildingTableModel.rows.get(idx).tile);
		}
	}
}
