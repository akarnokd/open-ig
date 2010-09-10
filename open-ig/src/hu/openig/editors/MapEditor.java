/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors;

import hu.openig.core.Location;
import hu.openig.core.RoadType;
import hu.openig.core.Sides;
import hu.openig.core.Configuration;
import hu.openig.core.PlanetType;
import hu.openig.core.ResourceLocator;
import hu.openig.core.Tile;
import hu.openig.editors.ImportDialog.OriginalBuilding;
import hu.openig.gfx.ColonyGFX;
import hu.openig.model.Building;
import hu.openig.model.BuildingModel;
import hu.openig.model.BuildingType;
import hu.openig.model.GalaxyModel;
import hu.openig.model.PlanetSurface;
import hu.openig.model.SurfaceEntity;
import hu.openig.model.BuildingType.TileSet;
import hu.openig.model.SurfaceEntity.SurfaceEntityType;

import java.awt.Desktop;
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
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
	/** The current alpha level. */
	float alpha = 1.0f;
	/** Show/hide buildings. */
	private JCheckBoxMenuItem viewShowBuildings;
	/** Place roads of a race around the buildings. */
	private JMenu editPlaceRoads;
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
				
				renderer.selection = new Tile(1, 1, colonyGFX.recolor(colonyGFX.tileEdge, 0xFFFFFF00), null);
				renderer.areaAccept = new Tile(1, 1, colonyGFX.recolor(colonyGFX.tileEdge, 0xFF00FFFF), null);
				renderer.areaDeny = new Tile(1, 1, colonyGFX.recolor(colonyGFX.tileCrossed, 0xFF00FFFF), null);
				renderer.areaCurrent  = new Tile(1, 1, colonyGFX.recolor(colonyGFX.tileCrossed, 0xFFFFCC00), null);
				
				renderer.selection.alpha = 1.0f;
				renderer.areaAccept.alpha = 1.0f;
				renderer.areaDeny.alpha = 1.0f;
				renderer.areaCurrent.alpha = 1.0f;
				
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
		surfaceTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					doPlaceSurface();
				}
			}
		});
		buildingTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				doSelectBuilding();
			}
		});
		buildingTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					doPlaceBuilding();
				}
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

		GroupLayout gls = new GroupLayout(surfacePanel);
		surfacePanel.setLayout(gls);
		gls.setHorizontalGroup(gls.createParallelGroup().addComponent(filterSurface).addComponent(sp0));
		gls.setVerticalGroup(gls.createSequentialGroup().addComponent(filterSurface, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(sp0));

		JPanel buildingPanel = new JPanel();
		filterBuilding = new JTextField();
		filterBuilding.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFilterBuilding();
			}
		});
		filterBuilding.setToolTipText("Filter by size, name and race. Example: '2x human' means search for tiles with width=2 and race=human");
		GroupLayout glb = new GroupLayout(buildingPanel);
		buildingPanel.setLayout(glb);
		glb.setHorizontalGroup(glb.createParallelGroup().addComponent(filterBuilding).addComponent(sp1));
		glb.setVerticalGroup(glb.createSequentialGroup().addComponent(filterBuilding, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(sp1));
		
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
		
		fileImport.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doImport(); } });
		
		
		JMenuItem editUndo = new JMenuItem("Undo");
		JMenuItem editRedo = new JMenuItem("Redo");
		JMenuItem editCut = new JMenuItem("Cut");
		JMenuItem editCopy = new JMenuItem("Copy");
		JMenuItem editPaste = new JMenuItem("Paste");
		JMenuItem editDeleteBuilding = new JMenuItem("Delete building");
		JMenuItem editDeleteSurface = new JMenuItem("Delete surface");
		JMenuItem editDeleteBoth = new JMenuItem("Delete both");
		
		JMenuItem editClearBuildings = new JMenuItem("Clear buildings");
		JMenuItem editClearSurface = new JMenuItem("Clear surface");

		editUndo.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doUndo(); } });
		editRedo.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doRedo(); } });
		editClearBuildings.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doClearBuildings(); } });
		editClearSurface.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doClearSurfaces(); } });
		

		editUndo.setAccelerator(KeyStroke.getKeyStroke('Z', InputEvent.CTRL_DOWN_MASK));
		editRedo.setAccelerator(KeyStroke.getKeyStroke('Y', InputEvent.CTRL_DOWN_MASK));
		editCut.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK));
		editCopy.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK));
		editPaste.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK));
		
		editDeleteBuilding.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		editDeleteSurface.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK));
		editDeleteBoth.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		
		editDeleteBuilding.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doDeleteBuilding(); } });
		editDeleteSurface.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doDeleteSurface(); } });
		editDeleteBoth.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doDeleteBoth(); } });
		
		editPlaceRoads = new JMenu("Place roads");
		
		JMenuItem viewZoomIn = new JMenuItem("Zoom in");
		JMenuItem viewZoomOut = new JMenuItem("Zoom out");
		JMenuItem viewZoomNormal = new JMenuItem("Zoom normal");
		JMenuItem viewZoomValue = new JMenuItem("Zoom...");
		JMenuItem viewBright = new JMenuItem("Daylight (1.0)");
		JMenuItem viewDark = new JMenuItem("Night (0.5)");
		JMenuItem viewMoreLight = new JMenuItem("More light (+0.05)");
		JMenuItem viewLessLight = new JMenuItem("Less light (-0.05)");
		
		viewShowBuildings = new JCheckBoxMenuItem("Show/hide buildings", true);
		viewShowBuildings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
		
		viewShowBuildings.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doToggleBuildings(); } });
		
		viewBright.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doBright(); } });
		viewDark.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doDark(); } });
		viewMoreLight.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doMoreLight(); } });
		viewLessLight.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doLessLight(); } });
		
		viewZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
		viewZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
		viewZoomNormal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, InputEvent.CTRL_DOWN_MASK));
		viewMoreLight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, InputEvent.CTRL_DOWN_MASK));
		viewLessLight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, InputEvent.CTRL_DOWN_MASK));
		viewBright.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, InputEvent.CTRL_DOWN_MASK));
		viewDark.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, InputEvent.CTRL_DOWN_MASK));
		
		JMenuItem helpOnline = new JMenuItem("Online wiki...");
		helpOnline.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doHelp();
			}
		});
		JMenuItem helpAbout = new JMenuItem("About...");
		
		addAll(mainmenu, fileMenu, editMenu, viewMenu, helpMenu);
		addAll(fileMenu, fileNew, null, fileOpen, fileImport, null, fileSave, fileSaveAs, null, fileExit);
		addAll(editMenu, editCut, editCopy, editPaste, null, editDeleteBuilding, editDeleteSurface, editDeleteBoth, null, editClearBuildings, editClearSurface, null, editPlaceRoads);
		addAll(viewMenu, viewZoomIn, viewZoomOut, viewZoomNormal, viewZoomValue, null, viewBright, viewDark, viewMoreLight, viewLessLight, null, viewShowBuildings);
		addAll(helpMenu, helpOnline, null, helpAbout);
		
		fileNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doNew();
			}
		});
	}
	/** Redo last operation. */
	protected void doRedo() {
		// TODO Auto-generated method stub
		
	}
	/** Undo last operation. */
	protected void doUndo() {
		// TODO Auto-generated method stub
		
	}
	/** Clear surfaces. */
	protected void doClearSurfaces() {
		if (renderer.surface != null) {
			renderer.surface.basemap.clear();
			renderer.repaint();
		}
	}
	/** Clear buildings. */
	protected void doClearBuildings() {
		if (renderer.surface != null) {
			renderer.surface.buildingmap.clear();
			renderer.surface.buildings.clear();
			renderer.repaint();
		}
	}
	/**
	 * Decrease the light amount on the tiles.
	 */
	protected void doLessLight() {
		alpha = Math.max(0, alpha - 0.05f);
		setAlphaOnTiles();
		repaint();
	}
	/**
	 * 
	 */
	private void setAlphaOnTiles() {
		if (renderer.surface != null) {
			for (SurfaceEntity se : renderer.surface.basemap.values()) {
				se.tile.alpha = alpha; 
			}
			for (SurfaceEntity se : renderer.surface.buildingmap.values()) {
				se.tile.alpha = alpha;
			}
		}
	}
	/**
	 * Increase the light amount on the tiles.
	 */
	protected void doMoreLight() {
		alpha = Math.min(1f, alpha + 0.05f);;
		setAlphaOnTiles();
		repaint();
	}
	/** Set lighting to half. */
	protected void doDark() {
		alpha = 0.5f;
		setAlphaOnTiles();
		repaint();
	}
	/** Set lighting to full. */
	protected void doBright() {
		alpha = 1.0f;
		setAlphaOnTiles();
		repaint();
	}
	/**
	 * Toggle the visibility of the buildings.
	 */
	protected void doToggleBuildings() {
		renderer.showBuildings = viewShowBuildings.getState();
		repaint();
	}
	/** Delete buildings and surface elements of the selection rectangle. */
	protected void doDeleteBoth() {
		deleteEntitiesOf(renderer.surface.basemap, false);
		deleteEntitiesOf(renderer.surface.buildingmap, true);
		repaint();
	}
	/** Delete surface surface. */
	protected void doDeleteSurface() {
		deleteEntitiesOf(renderer.surface.basemap, false);
		repaint();
	}
	/**
	 * Delete buildings falling into the current selection rectangle.
	 */
	protected void doDeleteBuilding() {
		deleteEntitiesOf(renderer.surface.buildingmap, true);
		repaint();
	}
	/**
	 * Delete entries of the given map.
	 * @param map the map
	 * @param checkBuildings check for the actual buildings
	 */
	void deleteEntitiesOf(Map<Location, SurfaceEntity> map, boolean checkBuildings) {
		// find buildings falling into the selection box
		if (renderer.selectedRectangle != null) {
			for (int a = renderer.selectedRectangle.x; a < renderer.selectedRectangle.x + renderer.selectedRectangle.width; a++) {
				for (int b = renderer.selectedRectangle.y; b < renderer.selectedRectangle.y + renderer.selectedRectangle.height; b++) {
					SurfaceEntity se = map.get(Location.of(a, b));
					if (se != null) {
						int x = a - se.virtualColumn;
						int y = b + se.virtualRow;
						for (int x0 = x; x0 < x + se.tile.width; x0++) {
							for (int y0 = y; y0 > y - se.tile.height; y0--) {
								map.remove(Location.of(x0, y0));
							}
						}
					}
					if (checkBuildings) {
						for (int i = renderer.surface.buildings.size() - 1; i >= 0; i--) {
							if (renderer.surface.buildings.get(i).containsLocation(a, b)) {
								renderer.surface.buildings.remove(i);
							}
						}
					}
				}
			}
		}
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
	 * A tile list table model.
	 * @author karnokd
	 */
	public static class TileList extends AbstractTableModel {
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
				e.name = "" + e.id;
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
		
		Set<String> races = new HashSet<String>();
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
				e.buildingType = bt.getValue();
				buildingTableModel.rows.add(e);
				
				e = new TileEntry();
				e.id = idx;
				e.surface = tss.getKey();
				e.name = bt.getKey() + " damaged";
				e.tile = tss.getValue().damaged;
				e.tile.alpha = 1.0f;
				e.preview = new ImageIcon(scaledImage(e.tile.alphaBlendImage(), 32, 32));
				e.buildingType = bt.getValue();
				buildingTableModel.rows.add(e);
				races.add(tss.getKey());
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
		List<String> racesList = new ArrayList<String>(races);
		Collections.sort(racesList);
		for (final String s : racesList) {
			JMenuItem mnuPlaceRoad = new JMenuItem(s);
			mnuPlaceRoad.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					placeRoads(s);
					repaint();
				}
			});
			
			
			editPlaceRoads.add(mnuPlaceRoad);
		}
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
	/**
	 * Place a tile onto the current surface map.
	 * @param tile the tile
	 * @param x the tile's leftmost coordinate
	 * @param y the tile's leftmost coordinate
	 * @param type the tile type
	 */
	void placeTile(Tile tile, int x, int y, SurfaceEntityType type) {
		for (int a = x; a < x + tile.width; a++) {
			for (int b = y; b > y - tile.height; b--) {
				SurfaceEntity se = new SurfaceEntity();
				se.type = type;
				se.virtualRow = y - b;
				se.virtualColumn = a - x;
				se.bottomRow = tile.height - 1;
				se.tile = tile.copy();
				se.tile.alpha = alpha;
				if (type != SurfaceEntityType.BASE) {
					Building bld = new Building();
					bld.location = Location.of(a, b);
					renderer.surface.buildingmap.put(bld.location, se);
				} else {
					renderer.surface.basemap.put(Location.of(a, b), se);
				}
			}
		}
	}
	/**
	 * Place the selected surface tile into the selection rectangle.
	 */
	void doPlaceSurface() {
		int idx = surfaceTable.getSelectedRow();
		if (idx >= 0) {
			idx = surfaceTable.convertRowIndexToModel(idx);
			TileEntry te = surfaceTableModel.rows.get(idx);
			if (renderer.selectedRectangle != null && renderer.selectedRectangle.width > 0) {
				for (int x = renderer.selectedRectangle.x; x < renderer.selectedRectangle.x + renderer.selectedRectangle.width; x += te.tile.width) {
					for (int y = renderer.selectedRectangle.y; y < renderer.selectedRectangle.y + renderer.selectedRectangle.height; y += te.tile.height) {
						placeTile(te.tile, x, y, SurfaceEntityType.BASE);
					}
				}
			}
			renderer.repaint();
		}
	}
	/**
	 * Place the selected building tile into the selection rectangle.
	 */
	void doPlaceBuilding() {
		int idx = buildingTable.getSelectedRow();
		if (idx >= 0) {
			idx = buildingTable.convertRowIndexToModel(idx);
			TileEntry te = buildingTableModel.rows.get(idx);
			if (renderer.selectedRectangle != null && renderer.selectedRectangle.width > 0) {
				for (int x = renderer.selectedRectangle.x; x < renderer.selectedRectangle.x + renderer.selectedRectangle.width; x += te.tile.width) {
					for (int y = renderer.selectedRectangle.y; y < renderer.selectedRectangle.y + renderer.selectedRectangle.height; y += te.tile.height) {
						placeTile(te.tile, x, y, SurfaceEntityType.BUILDING);
						Building bld = new Building();
						bld.location = Location.of(x, y);
						bld.type = te.buildingType;
						bld.tileset = te.buildingType.tileset.get(te.surface);
						renderer.surface.buildings.add(bld);
					}
				}
			}
			renderer.repaint();
		}
	}
	/**
	 * 
	 */
	void doHelp() {
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
	/**
	 * Show the import dialog.
	 */
	void doImport() {
		ImportDialog imp = new ImportDialog(rl);
		imp.setLocationRelativeTo(this);
		imp.setVisible(true);
		if (imp.success) {
			if (renderer.surface == null) {
				renderer.surface = new PlanetSurface();
				renderer.surface.width = 33;
				renderer.surface.height = 65;
				renderer.surface.computeRenderingLocations();
			}
			if (imp.selected != null) {
				if (imp.replaceSurface) {
					doClearSurfaces();
				}
				placeTilesFromOriginalMap(imp.selected.fullPath, imp.selected.surfaceType, imp.shiftXValue, imp.shiftYValue);
			}
			if (imp.planet != null) {
				if (imp.replaceBuildings) {
					doClearBuildings();
				}
				if (imp.withSurface) {
					if (imp.replaceSurface) {
						doClearSurfaces();
					}
					placeTilesFromOriginalMap("colony/" + imp.planet.getMapName(), imp.planet.surfaceType.toLowerCase(), imp.shiftXValue, imp.shiftYValue);
				}

				for (OriginalBuilding ob : imp.planet.buildings) {
					BuildingType bt = buildingModel.buildings.get(ob.getName()); 
					String r = imp.planet.getRace();
					TileSet t = bt.tileset.get(r);
					placeTile(t.normal, ob.location.x + imp.shiftXValue, ob.location.y + imp.shiftYValue, SurfaceEntityType.BUILDING);
					Building bld = new Building();
					bld.location = Location.of(ob.location.x + imp.shiftXValue, ob.location.y + imp.shiftYValue);
					bld.type = bt;
					bld.tileset = t;
					renderer.surface.buildings.add(bld);
				}
				placeRoads(imp.planet.getRace());
			}
			repaint();
		}
	}
	/**
	 * Place an original map tiles onto the current surface.
	 * @param path the path to the original map file
	 * @param surfaceType the surface type
	 * @param shiftX the shift in X coordinates to place the map elements
	 * @param shiftY the shift in Y coordinates to place the map elements
	 */
	private void placeTilesFromOriginalMap(String path, String surfaceType, int shiftX, int shiftY) {
		byte[] map = rl.getData("en", path);
		PlanetType pt = galaxyModel.planetTypes.get(surfaceType);
		int bias = 41; 
		if ("neptoplasm".equals(surfaceType)) {
			bias = 84;
		}
		for (int i = 0; i < 65 * 65; i++) {
			int tile = (map[4 + i * 2] & 0xFF) - bias;
			int strip = map[5 + i * 2] & 0xFF;
			if (strip == 0 && tile != 255) {
				Location loc = toOriginalLocation(i);
				Tile t = pt.tiles.get(tile);
				if (t != null) {
					placeTile(t, loc.x + shiftX, loc.y + shiftY, SurfaceEntityType.BASE);
				}
			}
		}
	}
	/**
	 * Converts the tile x and y coordinates to map offset.
	 * @param x the X coordinate
	 * @param y the Y coordinate
	 * @return the map offset
	 */
	public int toOriginalMapOffset(int x, int y) {
		return (x - y) * 65 + (x - y + 1) / 2 - x;
	}
	/**
	 * Convert the original index location of the map to actual (x, y) location.
	 * @param index the index into the map block, starting at 0
	 * @return the location
	 */
	public Location toOriginalLocation(int index) {
		int row = index % 65;
		int col = index / 65;
		
		int x0 = (col + 1) / 2;
		int y0 = - col / 2;
		
		int x = x0 - row;
		int y = y0 - row;
		return Location.of(x, y);
	}
	/**
	 * Place roads around buildings for the given race.
	 * @param raceId the race who builds the roads
	 */
	void placeRoads(String raceId) {
		Map<RoadType, Tile> rts = buildingModel.roadTiles.get(raceId);
		Map<Tile, RoadType> trs = buildingModel.tileRoads.get(raceId);
		// remove all roads
		Iterator<SurfaceEntity> it = renderer.surface.buildingmap.values().iterator();
		while (it.hasNext()) {
			SurfaceEntity se = it.next();
			if (se.type == SurfaceEntityType.ROAD) {
				it.remove();
			}
		}
		Set<Location> corners = new HashSet<Location>();
		for (Building bld : renderer.surface.buildings) {
			Rectangle rect = new Rectangle(bld.location.x - 1, bld.location.y + 1, bld.tileset.normal.width + 2, bld.tileset.normal.height + 2);
			addRoadAround(rts, rect, corners);
		}
		SurfaceEntity[] neighbors = new SurfaceEntity[9];
		for (Location l : corners) {
			SurfaceEntity se = renderer.surface.buildingmap.get(l);
			if (se == null || se.type != SurfaceEntityType.ROAD) {
				continue;
			}
			setNeighbors(l.x, l.y, renderer.surface.buildingmap, neighbors);
			int pattern = 0;
			
			RoadType rt1 = null;
			if (neighbors[1] != null && neighbors[1].type == SurfaceEntityType.ROAD) {
				pattern |= Sides.TOP;
				rt1 = trs.get(neighbors[1].tile);
			}
			RoadType rt3 = null;
			if (neighbors[3] != null && neighbors[3].type == SurfaceEntityType.ROAD) {
				pattern |= Sides.LEFT;
				rt3 = trs.get(neighbors[3].tile);
			}
			RoadType rt5 = null;
			if (neighbors[5] != null && neighbors[5].type == SurfaceEntityType.ROAD) {
				pattern |= Sides.RIGHT;
				rt5 = trs.get(neighbors[5].tile);
			}
			RoadType rt7 = null;
			if (neighbors[7] != null && neighbors[7].type == SurfaceEntityType.ROAD) {
				pattern |= Sides.BOTTOM;
				rt7 = trs.get(neighbors[7].tile);
			}
			RoadType rt = RoadType.get(pattern);
			// place the new tile fragment onto the map
			// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
			se = createRoadEntity(rts.get(rt));
			renderer.surface.buildingmap.put(l, se);
			// alter the four neighboring tiles to contain road back to this
			if (rt1 != null) {
				rt1 = RoadType.get(rt1.pattern | Sides.BOTTOM);
				renderer.surface.buildingmap.put(l.delta(0, 1), createRoadEntity(rts.get(rt1)));
			}
			if (rt3 != null) {
				rt3 = RoadType.get(rt3.pattern | Sides.RIGHT);
				renderer.surface.buildingmap.put(l.delta(-1, 0), createRoadEntity(rts.get(rt3)));
			}
			if (rt5 != null) {
				rt5 = RoadType.get(rt5.pattern | Sides.LEFT);
				renderer.surface.buildingmap.put(l.delta(1, 0), createRoadEntity(rts.get(rt5)));
			}
			if (rt7 != null) {
				rt7 = RoadType.get(rt7.pattern | Sides.TOP);
				renderer.surface.buildingmap.put(l.delta(0, -1), createRoadEntity(rts.get(rt7)));
			}
			
		}
	}
	/**
	 * Fills the fragment array of the 3x3 rectangle centered around x and y.
	 * @param x the x coordinate
	 * @param y the y coordinate
	 * @param map the map
	 * @param fragments the fragments
	 */
	void setNeighbors(int x, int y, Map<Location, SurfaceEntity> map, SurfaceEntity[] fragments) {
		fragments[0] = map.get(Location.of(x - 1, y + 1));
		fragments[1] = map.get(Location.of(x, y + 1));
		fragments[2] = map.get(Location.of(x + 1, y + 1));
		
		fragments[3] = map.get(Location.of(x - 1, y));
		fragments[4] = map.get(Location.of(x, y));
		fragments[5] = map.get(Location.of(x + 1, y));
		
		fragments[6] = map.get(Location.of(x - 1, y - 1));
		fragments[7] = map.get(Location.of(x, y - 1));
		fragments[8] = map.get(Location.of(x + 1, y - 1));
	}
	/**
	 * Places a road frame around the tilesToHighlight rectangle.
	 * @param rts the road to tile map for a concrete race
	 * @param rect the rectangle to use
	 * @param corners where to place the created corners
	 */
	void addRoadAround(Map<RoadType, Tile> rts, Rectangle rect, Collection<Location> corners) {
		Location la = Location.of(rect.x, rect.y);
		Location lb = Location.of(rect.x + rect.width - 1, rect.y);
		Location lc = Location.of(rect.x, rect.y - rect.height + 1);
		Location ld = Location.of(rect.x + rect.width - 1, rect.y - rect.height + 1);
		
		corners.add(la);
		corners.add(lb);
		corners.add(lc);
		corners.add(ld);
		
		renderer.surface.buildingmap.put(la, createRoadEntity(rts.get(RoadType.RIGHT_TO_BOTTOM)));
		renderer.surface.buildingmap.put(lb, createRoadEntity(rts.get(RoadType.LEFT_TO_BOTTOM)));
		renderer.surface.buildingmap.put(lc, createRoadEntity(rts.get(RoadType.TOP_TO_RIGHT)));
		renderer.surface.buildingmap.put(ld, createRoadEntity(rts.get(RoadType.TOP_TO_LEFT)));
		// add linear segments
		
		Tile ht = rts.get(RoadType.HORIZONTAL);
		for (int i = rect.x + 1; i < rect.x + rect.width - 1; i++) {
			renderer.surface.buildingmap.put(Location.of(i, rect.y), createRoadEntity(ht));
			renderer.surface.buildingmap.put(Location.of(i, rect.y - rect.height + 1), createRoadEntity(ht));
		}
		Tile vt = rts.get(RoadType.VERTICAL);
		for (int i = rect.y - 1; i > rect.y - rect.height + 1; i--) {
			renderer.surface.buildingmap.put(Location.of(rect.x, i), createRoadEntity(vt));
			renderer.surface.buildingmap.put(Location.of(rect.x + rect.width - 1, i), createRoadEntity(vt));
		}
	}
	/**
	 * Create a road entity for the tile.
	 * @param tile the tile
	 * @return the entity
	 */
	SurfaceEntity createRoadEntity(Tile tile) {
		SurfaceEntity result = new SurfaceEntity();
		result.tile = tile;
		result.tile.alpha = alpha;
		result.type = SurfaceEntityType.ROAD;
		return result;
	}
}
