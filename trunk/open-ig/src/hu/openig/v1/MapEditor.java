/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import hu.openig.core.Location;
import hu.openig.v1.core.Configuration;
import hu.openig.v1.core.PlanetType;
import hu.openig.v1.core.ResourceLocator;
import hu.openig.v1.core.Tile;
import hu.openig.v1.gfx.ColonyGFX;
import hu.openig.v1.model.BuildingModel;
import hu.openig.v1.model.BuildingType;
import hu.openig.v1.model.GalaxyModel;
import hu.openig.v1.model.PlanetSurface;
import hu.openig.v1.model.BuildingType.TileSet;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

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
	ImagePaint preview;
	/** The alpha slider. */
	JSlider alphaSlider;
	/** The building model. */
	BuildingModel buildingModel;
	/** Filter surface table. */
	JTextField filterSurface;
	/** Filter buildings table. */
	JTextField filterBuilding;
	/** The row sorter for the surface table. */
	TableRowSorter<TileList> surfaceSorter;
	/** The row sorter for the building sorter. */
	TableRowSorter<TileList> buildingSorter;
	/** The colony graphics. */
	ColonyGFX colonyGFX;
	/** The selection tile. */
	Tile selection;
	/** The placement tile for allowed area. */
	Tile areaAccept;
	/** The placement tile for denied area. */
	Tile areaDeny;
	/** The current cell tile. */
	Tile areaCurrent;
	/** Load the resource locator. */
	void loadResourceLocator() {
		final BackgroundProgress bgp = new BackgroundProgress();
		bgp.setLocationRelativeTo(this);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			private ResourceLocator rl;
			private GalaxyModel  galaxyMap;
			private BuildingModel buildingMap;
			private ColonyGFX colonyGraphics;
			@Override
			protected Void doInBackground() throws Exception {
				final Configuration config = new Configuration("open-ig-config.xml");
				config.load();
				rl = config.newResourceLocator();
				galaxyMap = new GalaxyModel();
				galaxyMap.processGalaxy(rl, "en", "campaign/main/galaxy");
				buildingMap = new BuildingModel();
				buildingMap.processBuildings(rl, "en", "campaign/main/buildings");
				colonyGraphics = new ColonyGFX(rl);
				colonyGraphics.load("en");
				return null;
			}
			@Override
			protected void done() {
				bgp.dispose();
				MapEditor.this.rl = rl;
				MapEditor.this.galaxyModel = galaxyMap;
				MapEditor.this.buildingModel = buildingMap;
				MapEditor.this.colonyGFX = colonyGraphics;
				
				selection = new Tile(1, 1, colonyGFX.recolor(colonyGFX.tileEdge, 0xFFFFFF00), null);
				areaAccept = new Tile(1, 1, colonyGFX.recolor(colonyGFX.tileEdge, 0xFF00FFFF), null);
				areaDeny = new Tile(1, 1, colonyGFX.recolor(colonyGFX.tileCrossed, 0xFF00FFFF), null);
				areaCurrent  = new Tile(1, 1, colonyGFX.recolor(colonyGFX.tileCrossed, 0xFFFFCC00), null);
				
				selection.alpha = 1.0f;
				areaAccept.alpha = 1.0f;
				areaDeny.alpha = 1.0f;
				
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

		surfaceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		surfaceTable.getColumnModel().getColumn(0).setPreferredWidth(45);
		surfaceTable.getColumnModel().getColumn(1).setPreferredWidth(32);
		surfaceSorter = new TableRowSorter<TileList>(surfaceTableModel);
		surfaceTable.setRowSorter(surfaceSorter);
		
		buildingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		buildingTable.getColumnModel().getColumn(0).setPreferredWidth(45);
		buildingTable.getColumnModel().getColumn(1).setPreferredWidth(32);
		buildingSorter = new TableRowSorter<TileList>(buildingTableModel);
		buildingTable.setRowSorter(buildingSorter);

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
		
		JPanel surfacePanel = new JPanel();
		surfacePanel.setLayout(new BoxLayout(surfacePanel, BoxLayout.PAGE_AXIS));
		filterSurface = new JTextField();
		filterSurface.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFilterSurface();
			}
		});
		filterSurface.setToolTipText("Filter by size, name and surface. Example: '1x earth' means search for tiles with width=1 and surface=earth");
		surfacePanel.add(filterSurface);
		surfacePanel.add(sp0);
		
		JPanel buildingPanel = new JPanel();
		buildingPanel.setLayout(new BoxLayout(buildingPanel, BoxLayout.PAGE_AXIS));
		filterBuilding = new JTextField();
		filterBuilding.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFilterBuilding();
			}
		});
		filterBuilding.setToolTipText("Filter by size, name and race. Example: '2x human' means search for tiles with width=2 and race=human");
		buildingPanel.add(filterBuilding);
		buildingPanel.add(sp1);
		
		previewSplit.setBottomComponent(surfacePanel);
		toolSplit.setBottomComponent(buildingPanel);
		
		previewSplit.setDividerLocation(300);
		
		
		JPanel previewPanel = new JPanel();
		
		alphaSlider = new JSlider(0, 100, 100);
		alphaSlider.setMajorTickSpacing(10);
		alphaSlider.setMinorTickSpacing(1);
		alphaSlider.setPaintLabels(true);
		alphaSlider.setPaintTicks(true);
		alphaSlider.setToolTipText("Adjust brightness level. Buildings should turn on lights below 51 by default.");
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
		
		buildMenu();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				loadResourceLocator();
			}
		});
		
	}
	/**
	 * Construct the menu tree.
	 */
	private void buildMenu() {
		JMenuBar mainmenu = new JMenuBar();
		setJMenuBar(mainmenu);

		JMenu fileMenu = new JMenu("File");
		JMenu editMenu = new JMenu("Edit");
		JMenu viewMenu = new JMenu("View");
		JMenu helpMenu = new JMenu("Help");
		
		JMenuItem fileNew = new JMenuItem("New...");
		JMenuItem fileOpen = new JMenuItem("Open...");
		JMenuItem fileImport = new JMenuItem("Import...");
		JMenuItem fileSave = new JMenuItem("Save");
		JMenuItem fileSaveAs = new JMenuItem("Save as...");
		JMenuItem fileExit = new JMenuItem("Exit");

		fileOpen.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
		fileSave.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK));
		
		JMenuItem editCut = new JMenuItem("Cut");
		JMenuItem editCopy = new JMenuItem("Copy");
		JMenuItem editPaste = new JMenuItem("Paste");
		JMenuItem editDelete = new JMenuItem("Delete");
		
		editCut.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK));
		editCopy.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK));
		editPaste.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK));
		
		JMenuItem viewZoomIn = new JMenuItem("Zoom in");
		JMenuItem viewZoomOut = new JMenuItem("Zoom out");
		JMenuItem viewZoomNormal = new JMenuItem("Zoom normal");
		JMenuItem viewZoomValue = new JMenuItem("Zoom...");
		JMenuItem viewBright = new JMenuItem("Daylight (1.0)");
		JMenuItem viewDark = new JMenuItem("Night (0.5)");
		JMenuItem viewMoreLight = new JMenuItem("More light (+0.05)");
		JMenuItem viewLessLight = new JMenuItem("Less light (-0.05)");
		
		viewZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
		viewZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
		viewZoomNormal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, InputEvent.CTRL_DOWN_MASK));
		viewMoreLight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_DOWN_MASK));
		viewLessLight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_DOWN_MASK));
		
		JMenuItem helpOnline = new JMenuItem("Online wiki...");
		helpOnline.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Desktop desktop = Desktop.getDesktop();
				if (desktop != null) {
					try {
						desktop.browse(new URI("http://code.google.com/p/open-ig/wiki/MapEditor"));
					} catch (IOException ex) {
						JOptionPane.showMessageDialog(MapEditor.this, "Exception", stacktraceToString(ex), JOptionPane.ERROR_MESSAGE);
					} catch (URISyntaxException ex) {
						JOptionPane.showMessageDialog(MapEditor.this, "Exception", stacktraceToString(ex), JOptionPane.ERROR_MESSAGE);
					}
				} else {
					JOptionPane.showMessageDialog(MapEditor.this, "Desktop not supported. Please navigate to http://code.google.com/p/open-ig/wiki/MapEditor manually.");
				}
			}
		});
		JMenuItem helpAbout = new JMenuItem("About...");
		
		addAll(mainmenu, fileMenu, editMenu, viewMenu, helpMenu);
		addAll(fileMenu, fileNew, null, fileOpen, fileImport, null, fileSave, fileSaveAs, null, fileExit);
		addAll(editMenu, editCut, editCopy, editPaste, editDelete);
		addAll(viewMenu, viewZoomIn, viewZoomOut, viewZoomNormal, viewZoomValue, null, viewBright, viewDark, viewMoreLight, viewLessLight);
		addAll(helpMenu, helpOnline, null, helpAbout);
		
		fileNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doNew();
			}
		});
	}
	/**
	 * Extract the stacktrace as a string.
	 * @param t the throwable
	 * @return the string
	 */
	String stacktraceToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		t.printStackTrace(out);
		out.flush();
		return sw.toString();
	}
	/**
	 * Add multiple items to a menu.
	 * @param menu the menu parent
	 * @param items the array of items, null represents a separator
	 */
	void addAll(JMenu menu, JMenuItem... items) {
		for (JMenuItem mi : items) {
			if (mi != null) {
				menu.add(mi);
			} else {
				menu.addSeparator();
			}
		}
	}
	/**
	 * Add multiple items to a menubar.
	 * @param menu the menu parent
	 * @param items the array of items
	 */
	void addAll(JMenuBar menu, JMenuItem... items) {
		for (JMenuItem mi : items) {
			if (mi != null) {
				menu.add(mi);
			}
		}
	}
	/** Filter the building list. */
	protected void doFilterBuilding() {
		if (filterBuilding.getText().length() > 0) {
			final String[] words = filterBuilding.getText().split("\\s+");
			buildingSorter.setRowFilter(new RowFilter<TileList, Integer>() {
				@Override
				public boolean include(
						javax.swing.RowFilter.Entry<? extends TileList, ? extends Integer> entry) {
					TileEntry e = entry.getModel().rows.get(entry.getIdentifier());
					String rowtext = (e.name + " " + e.surface + " " + e.tile.width + "x" + e.tile.height).toLowerCase(); 
					for (String s : words) {
						if (!rowtext.contains(s.toLowerCase())) {
							return false;
						}
					}
					return true;
				}
			});
		} else {
			buildingSorter.setRowFilter(null);
		}
	}
	/** Filter the surface list. */
	protected void doFilterSurface() {
		if (filterSurface.getText().length() > 0) {
			final String[] words = filterSurface.getText().split("\\s+");
			surfaceSorter.setRowFilter(new RowFilter<TileList, Integer>() {
				@Override
				public boolean include(
						javax.swing.RowFilter.Entry<? extends TileList, ? extends Integer> entry) {
					TileEntry e = entry.getModel().rows.get(entry.getIdentifier());
					String rowtext = (e.name + " " + e.surface + " " + e.tile.width + "x" + e.tile.height).toLowerCase(); 
					for (String s : words) {
						if (!rowtext.contains(s.toLowerCase())) {
							return false;
						}
					}
					return true;
				}
			});
			
		} else {
			surfaceSorter.setRowFilter(null);
		}
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
			buildingTable.getSelectionModel().clearSelection();
		}
	}
	/** Select a surface image. */
	void doSelectBuilding() {
		int idx = buildingTable.getSelectedRow();
		if (idx >= 0) {
			idx = buildingTable.convertRowIndexToModel(idx);
			preview.setImage(buildingTableModel.rows.get(idx).tile);
			surfaceTable.getSelectionModel().clearSelection();
		}
	}
	/**
	 * Create a new, empty surface map.
	 */
	void doNew() {
		renderer.surface = new PlanetSurface();
		renderer.surface.width = 33;
		renderer.surface.height = 65;
		renderer.surface.computeRenderingLocations();
		renderer.repaint();
	}
	/** The map renderer. */
	class MapRenderer extends JComponent {
		/** */
		private static final long serialVersionUID = 5058274675379681602L;
		/** The planet surface definition. */
		PlanetSurface surface;
		/** The offset X. */
		int offsetX;
		/** The offset Y. */
		int offsetY;
		/** The current location based on the mouse pointer. */
		Location current;
		/** Preset. */
		public MapRenderer() {
			MouseAdapter ma = new MouseAdapter() {
				int lastX;
				int lastY;
				boolean drag;
				@Override
				public void mousePressed(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						drag = true;
						lastX = e.getX();
						lastY = e.getY();
					} else
					if (SwingUtilities.isMiddleMouseButton(e)) {
						offsetX = 0;
						offsetY = 0;
						repaint();
					}
				}
				@Override
				public void mouseReleased(MouseEvent e) {
					if (SwingUtilities.isRightMouseButton(e)) {
						drag = false;
					}
				}
				@Override
				public void mouseDragged(MouseEvent e) {
					if (drag) {
						offsetX += e.getX() - lastX;
						offsetY += e.getY() - lastY;
						
						lastX = e.getX();
						lastY = e.getY();
						repaint();
					}
				}
				@Override
				public void mouseMoved(MouseEvent e) {
					current = getLocationAt(e.getX(), e.getY());
					repaint();
				}
			};
			addMouseListener(ma);
			addMouseMotionListener(ma);
		}
		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;

			g2.setColor(new Color(96, 96, 96));
			g2.fillRect(0, 0, getWidth(), getHeight());
			
			if (surface == null) {
				return;
			}
			int x0 = surface.baseXOffset;
			int y0 = surface.baseYOffset;

			Rectangle br = surface.boundingRectangle;
			g2.setColor(new Color(128, 128, 128));
			g2.fillRect(br.x + offsetX, br.y + offsetY, br.width, br.height);
			g2.setColor(Color.YELLOW);
			g2.drawRect(br.x + offsetX, br.y + offsetY, br.width, br.height);
			BufferedImage empty = areaAccept.alphaBlendImage(); 
//			BufferedImage deny = areaDeny.alphaBlendImage();
			for (int i = 0; i < surface.renderingOrigins.size(); i++) {
				Location loc = surface.renderingOrigins.get(i);
				for (int j = 0; j < surface.renderingLength.get(i); j++) {
					int x = offsetX + x0 + Tile.toScreenX(loc.x - j, loc.y);
					int y = offsetY + y0 + Tile.toScreenY(loc.x - j, loc.y);
					g2.drawImage(empty, x, y, null);
				}
			}
			if (current != null) {
				int x = offsetX + x0 + Tile.toScreenX(current.x, current.y);
				int y = offsetY + y0 + Tile.toScreenY(current.x, current.y);
				g2.drawImage(areaCurrent.alphaBlendImage(), x, y, null);
			}
		}
		/**
		 * Get a location based on the mouse coordinates.
		 * @param mx the mouse X coordinate
		 * @param my the mouse Y coordinate
		 * @return the location
		 */
		Location getLocationAt(int mx, int my) {
			if (surface != null) {
				int mx0 = mx - offsetX - surface.baseXOffset;
				int my0 = my - offsetY - surface.baseYOffset;
				return Location.of((int)Tile.toTileX(mx0, my0), (int)Tile.toTileY(mx0, my0));
			}
			return null;
		}
	}
}
