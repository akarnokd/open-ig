/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors;

import hu.openig.core.Func0;
import hu.openig.core.Location;
import hu.openig.gfx.ColonyGFX;
import hu.openig.model.Building;
import hu.openig.model.BuildingModel;
import hu.openig.model.BuildingType;
import hu.openig.model.Configuration;
import hu.openig.model.GalaxyModel;
import hu.openig.model.Labels;
import hu.openig.model.OriginalBuilding;
import hu.openig.model.PlanetSurface;
import hu.openig.model.PlanetType;
import hu.openig.model.ResearchType;
import hu.openig.model.Resource;
import hu.openig.model.ResourceLocator;
import hu.openig.model.SurfaceEntity;
import hu.openig.model.SurfaceEntityType;
import hu.openig.model.SurfaceFeature;
import hu.openig.model.Tile;
import hu.openig.model.TileSet;
import hu.openig.render.TextRenderer;
import hu.openig.utils.ConsoleWatcher;
import hu.openig.utils.Exceptions;
import hu.openig.utils.ImageUtils;
import hu.openig.utils.WipPort;
import hu.openig.utils.XElement;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
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
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import javax.xml.stream.XMLStreamException;

/**
 * Map editor.
 * @author akarnokd, 2010.09.08.
 */
public class MapEditor extends JFrame {
	/** */
	private static final long serialVersionUID = -5949479655359917254L;
	/** The minimum memory required to run Open-IG. */
	private static final long MINIMUM_MEMORY = 512L;
	/** The map editor's JAR file version. */
	public static final String VERSION = "0.60";
	/** The title text. */
	public static final String TITLE = "Open-IG MapEditor v" + VERSION;
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
	JSplitPane featuresSplit;
	/** The preview image. */
	ImagePaint preview;
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
	/** The import dialog. */
	ImportDialog imp;
	/** The current base tile object. */
	SurfaceFeature currentBaseTile;
	/** The current building type object. */
	BuildingType currentBuildingType;
	/** The current building race. */
	String currentBuildingRace;
	/** The configuration object. */
	final Configuration config;
	/** The building that is currently under edit. */
	Building currentBuilding;
	/** The current save settings. */
	MapSaveSettings saveSettings;
	/** The alpha slider. */
	JSlider alphaSlider;
	/** The labels. */
	Labels labels;
	/** The User Interface elements to rename. */
	static class UIElements {
		/** Show/hide buildings. */
		@Rename(to = "mapeditor.show_hide_buildings")
		JCheckBoxMenuItem viewShowBuildings;
		/** Are we in placement mode? */
		@Rename(to = "mapeditor.object_placement_mode")
		JToggleButton buildButton;
		/** Is the placement mode active? */
		@Rename(to = "mapeditor.object_placement_mode")
		JCheckBoxMenuItem editPlaceMode;
		/** View buildings in minimap mode. */
		@Rename(to = "mapeditor.symbolic_buildings")
		JCheckBoxMenuItem viewSymbolicBuildings;
		/** View text naming backgrounds. */
		@Rename(to = "mapeditor.view_text_background")
		JCheckBoxMenuItem viewTextBackgrounds;
		/** The building info panel. */
		BuildingInfoPanel buildingInfoPanel;
		/** The allocation panel. */
		AllocationPanel allocationPanel;
		/** Place roads of a race around the buildings. */
		@Rename(to = "mapeditor.place_roads")
		JMenu editPlaceRoads;
		/** File menu. */
		@Rename(to = "mapeditor.file")
		JMenu fileMenu;
		/** Edit menu. */
		@Rename(to = "mapeditor.edit")
		JMenu editMenu;
		/** View menu. */
		@Rename(to = "mapeditor.view")
		JMenu viewMenu;
		/** Help menu. */
		@Rename(to = "mapeditor.help")
		JMenu helpMenu;
		/** File new. */
		@Rename(to = "mapeditor.file_new")
		JMenuItem fileNew;
		/** File open. */
		@Rename(to = "mapeditor.file_open")
		JMenuItem fileOpen;
		/** File import. */
		@Rename(to = "mapeditor.file_import")
		JMenuItem fileImport;
		/** File save. */
		@Rename(to = "mapeditor.file_save")
		JMenuItem fileSave;
		/** File save as. */
		@Rename(to = "mapeditor.file_save_as")
		JMenuItem fileSaveAs;
		/** File exit. */
		@Rename(to = "mapeditor.file_exit")
		JMenuItem fileExit;
		/** Edit undo. */
		@Rename(to = "mapeditor.edit_undo")
		JMenuItem editUndo;
		/** Edit redo. */
		@Rename(to = "mapeditor.edit_redo")
		JMenuItem editRedo;
		/** Edit cut. */
		@Rename(to = "mapeditor.edit_cut")
		JMenuItem editCut;
		/** Edit copy. */
		@Rename(to = "mapeditor.edit_copy")
		JMenuItem editCopy;
		/** Edit paste. */
		@Rename(to = "mapeditor.edit_paste")
		JMenuItem editPaste;
		/** Edit delete building. */
		@Rename(to = "mapeditor.edit_delete_building")
		JMenuItem editDeleteBuilding;
		/** Edit delete surface. */
		@Rename(to = "mapeditor.edit_delete_surface")
		JMenuItem editDeleteSurface;
		/** Edit delete both. */
		@Rename(to = "mapeditor.edit_delete_both")
		JMenuItem editDeleteBoth;
		/** Edit clear buildings. */
		@Rename(to = "mapeditor.edit_clear_buildings")
		JMenuItem editClearBuildings;
		/** Edit clear surface. */
		@Rename(to = "mapeditor.edit_clear_surface")
		JMenuItem editClearSurface;
		/** View zoom in. */
		@Rename(to = "mapeditor.view_zoom_in")
		JMenuItem viewZoomIn;
		/** View zoom out. */
		@Rename(to = "mapeditor.view_zoom_out")
		JMenuItem viewZoomOut;
		/** View zoom normal. */
		@Rename(to = "mapeditor.view_zoom_normal")
		JMenuItem viewZoomNormal;
		/** View bright. */
		@Rename(to = "mapeditor.view_bright")
		JMenuItem viewBrighter;
		/** View dark. */
		@Rename(to = "mapeditor.view_dark")
		JMenuItem viewDarker;
		/** View more light. */
		@Rename(to = "mapeditor.view_more_light")
		JMenuItem viewMoreLight;
		/** View less light. */
		@Rename(to = "mapeditor.view_less_light")
		JMenuItem viewLessLight;
		/** Help online. */
		@Rename(to = "mapeditor.help_online")
		JMenuItem helpOnline;
		/** Help about. */
		@Rename(to = "mapeditor.help_about")
		JMenuItem helpAbout;
		/** Language EN. */
		@Rename(to = "mapeditor.language_en")
		JRadioButtonMenuItem languageEn;
		/** Language HU. */
		@Rename(to = "mapeditor.language_hu")
		JRadioButtonMenuItem languageHu;
		/** Language. */
		@Rename(to = "mapeditor.language")
		JMenu languageMenu;
		/** File recent. */
		@Rename(to = "mapeditor.recent")
		JMenu fileRecent;
		/** Clear recent. */
		@Rename(to = "mapeditor.clear_recent")
		JMenuItem clearRecent;
		/** The toolbar. */
		JToolBar toolbar;
		/** Toolbar's placement mode. */
		@Rename(to = "", tip = "mapeditor.object_placement_mode")
		public AbstractButton toolbarPlacementMode;
		/** Toolbar cut. */
		@Rename(to = "", tip = "mapeditor.edit_cut")
		public AbstractButton toolbarCut;
		/** Toolbar copy. */
		@Rename(to = "", tip = "mapeditor.edit_copy")
		public AbstractButton toolbarCopy;
		/** Toolbar paste. */
		@Rename(to = "", tip = "mapeditor.edit_paste")
		public AbstractButton toolbarPaste;
		/** Toolbar delete. */
		@Rename(to = "", tip = "mapeditor.edit_delete_building")
		public AbstractButton toolbarRemove;
		/** Toolbar undo. */
		@Rename(to = "", tip = "mapeditor.edit_undo")
		public AbstractButton toolbarUndo;
		/** Toolbar redo. */
		@Rename(to = "", tip = "mapeditor.edit_redo")
		public AbstractButton toolbarRedo;
		/** Toolbar new. */
		@Rename(to = "", tip = "mapeditor.file_new")
		public AbstractButton toolbarNew;
		/** Toolbar open. */
		@Rename(to = "", tip = "mapeditor.file_open")
		public AbstractButton toolbarOpen;
		/** Toolbar save. */
		@Rename(to = "", tip = "mapeditor.file_save")
		public AbstractButton toolbarSave;
		/** Toolbar file import. */
		@Rename(to = "", tip = "mapeditor.file_import")
		public AbstractButton toolbarImport;
		/** Toolbar save as. */
		@Rename(to = "", tip = "mapeditor.file_save_as")
		public AbstractButton toolbarSaveAs;
		/** Toolbar zoom normal. */
		@Rename(to = "", tip = "mapeditor.view_zoom_normal")
		public AbstractButton toolbarZoomNormal;
		/** Toolbar zoom in. */
		@Rename(to = "", tip = "mapeditor.view_zoom_in")
		public AbstractButton toolbarZoomIn;
		/** Toolbar zoom out. */
		@Rename(to = "", tip = "mapeditor.view_zoom_out")
		public AbstractButton toolbarZoomOut;
		/** Toolbar bright. */
		@Rename(to = "", tip = "mapeditor.view_bright")
		public AbstractButton toolbarBrighter;
		/** Toolbar dark. */
		@Rename(to = "", tip = "mapeditor.view_dark")
		public AbstractButton toolbarDarker;
		/** Toolbar online. */
		@Rename(to = "", tip = "mapeditor.help_online")
		public AbstractButton toolbarHelp;
		/** Resize map. */
		@Rename(to = "mapeditor.edit_resize", tip = "")
		public JMenuItem editResize;
		/** Clear outbound objects. */
		@Rename(to = "mapeditor.edit_cleanup", tip = "")
		public JMenuItem editCleanup;
		/** View standard fonts. */
		@Rename(to = "mapeditor.view_standard_fonts", tip = "")
		public JCheckBoxMenuItem viewStandardFonts;
		/** View the placement hints on the map? */
		@Rename(to = "mapeditor.view_placement_hints", tip = "")
		public JCheckBoxMenuItem viewPlacementHints;
		/** Paste buildings only. */
		@Rename(to = "mapeditor.edit_paste_building", tip = "")
		public JMenuItem editPasteBuilding;
		/** Paste surface only. */
		@Rename(to = "mapeditor.edit_paste_surface", tip = "")
		public JMenuItem editPasteSurface;
		/** Paste buildings only. */
		@Rename(to = "mapeditor.edit_cut_building", tip = "")
		public JMenuItem editCutBuilding;
		/** Paste surface only. */
		@Rename(to = "mapeditor.edit_cut_surface", tip = "")
		public JMenuItem editCutSurface;
		/** Paste surface only. */
		@Rename(to = "mapeditor.edit_copy_surface", tip = "")
		public JMenuItem editCopySurface;
		/** Paste surface only. */
		@Rename(to = "mapeditor.edit_copy_building", tip = "")
		public JMenuItem editCopyBuilding;
		/** The current selection position. */
		public JLabel cursorPosition;
		/** The current map size. */
		public JLabel mapsize;
	}
	/** The User Interface elements to rename. */
	final UIElements ui = new UIElements();
	/** Tabbed pane. */
	private JTabbedPane propertyTab;
	/** The undo manager. */
	UndoManager undoManager;
	/** The custom surface names. */
	final List<TileEntry> customSurfaceNames = new ArrayList<>();
	/** The custom building names. */
	final List<TileEntry> customBuildingNames = new ArrayList<>();
	/** The deferred language change. */
	String deferredLanguage = "en";
	/** The set of recent files. */
	final Set<String> recent = new HashSet<>();
	/** Load the resource locator. */
	void loadResourceLocator() {
		final BackgroundProgress bgp = new BackgroundProgress();
		bgp.setLocationRelativeTo(this);
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			/** The resource locator. */
			private ResourceLocator rl;
			/** The galaxy model. */
			private GalaxyModel  galaxyMap;
			/** The building model. */
			private BuildingModel buildingMap;
			/** The colony graphics. */
			private ColonyGFX colonyGraphics;
			/** Surface list. */
			private final List<TileEntry> surfaces = new ArrayList<>();
			/** Buildings list. */
			private final List<TileEntry> buildings = new ArrayList<>();
			/** Races. */
			private final Set<String> races = new HashSet<>();
			/** The loaded text renderer. */
			TextRenderer txt;
			/** The loaded labels. */
			private Labels lbl2;
			@Override
			protected Void doInBackground() throws Exception {
				try {
					rl = config.newResourceLocator();
					final ResourceLocator rl0 = rl;
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							MapEditor.this.rl = rl0;
							setLabels(deferredLanguage);							
						}
					});
					lbl2 = new Labels();
					lbl2.load(rl, Arrays.asList("labels", "campaign/main/labels"));
					final ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
					final WipPort wip = new WipPort(3);
					try {
						exec.execute(new Runnable() {
							@Override
							public void run() {
								try {
									galaxyMap = new GalaxyModel(config);
									galaxyMap.processGalaxy(rl, "campaign/main/galaxy", exec, wip);
								} finally {
									wip.dec();
								}
							}
						});
						exec.execute(new Runnable() {
							@Override
							public void run() {
								try {
									buildingMap = new BuildingModel(config);
									buildingMap.processBuildings(rl, "campaign/main/buildings", 
											new HashMap<String, ResearchType>(), lbl2, exec, wip);
								} finally {
									wip.dec();
								}
							}
						});
					} finally {
						wip.dec();
						wip.await();
					}
					exec.shutdown();
					exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);

					colonyGraphics = new ColonyGFX().load(rl);
					
					txt = new TextRenderer(rl, config.useStandardFonts);
					
					prepareLists(galaxyMap, buildingMap, surfaces, buildings, races);
				} catch (Throwable t) {
					Exceptions.add(t);
				}
				return null;
			}
			@Override
			protected void done() {
				bgp.dispose();
				MapEditor.this.galaxyModel = galaxyMap;
				MapEditor.this.buildingModel = buildingMap;
				renderer.colonyGFX = colonyGraphics;
				MapEditor.this.labels = lbl2;
				
				if (colonyGraphics != null) {
                    renderer.selection = new Tile(1, 1, ImageUtils.recolor(colonyGraphics.tileEdge, 0xFFFFFF00), null);
                    renderer.areaAccept = new Tile(1, 1, ImageUtils.recolor(colonyGraphics.tileEdge, 0xFF00FFFF), null);
                    renderer.areaEmpty = new Tile(1, 1, ImageUtils.recolor(colonyGraphics.tileEdge, 0xFF808080), null);
                    renderer.areaDeny = new Tile(1, 1, ImageUtils.recolor(colonyGraphics.tileCrossed, 0xFFFF0000), null);
                    renderer.areaCurrent  = new Tile(1, 1, ImageUtils.recolor(colonyGraphics.tileCrossed, 0xFFFFCC00), null);
                }
				renderer.selection.alpha = 1.0f;
				renderer.areaAccept.alpha = 1.0f;
				renderer.areaDeny.alpha = 1.0f;
				renderer.areaCurrent.alpha = 1.0f;
			
				renderer.txt = this.txt;
				renderer.txt.setUseStandardFonts(ui.viewStandardFonts.isSelected());
				buildTables(surfaces, buildings, races);
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
//		System.setProperty("sun.java2d.opengl", "true");
//		System.setProperty("sun.java2d.trace", "log,timestamp,count,out:graphicstrace.log");
//		System.setProperty("sun.java2d.accthreshold", "0");
//		System.setProperty("sun.java2d.d3d", "false");
//		System.setProperty("sun.java2d.noddraw", "true");
//		System.setProperty("sun.java2d.translaccel", "true");
		Set<String> argset = new HashSet<>(Arrays.asList(args));
		long maxMem = Runtime.getRuntime().maxMemory();
		if (maxMem < MINIMUM_MEMORY * 1024 * 1024 * 95 / 100) {
			if (!argset.contains("-memonce")) {
				if (!doLowMemory()) {
					doWarnLowMemory(maxMem);
				}
				return;
			}
		}
		final Configuration config = new Configuration("open-ig-config.xml");
		if (config.load()) {
			doStartProgram(config, args);
		}
	}
	/**
	 * Start the program.
	 * @param config the configuration.
	 * @param args the program arguments
	 */
	static void doStartProgram(final Configuration config, final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Func0<String> languageFn = new Func0<String>() {
					@Override
					public String invoke() {
						return config.language;
					}
				};

				config.watcherWindow = new ConsoleWatcher(args, VERSION, languageFn, null);
				MapEditor editor = new MapEditor(config);
				editor.setLocationRelativeTo(null);
				editor.setVisible(true);
			}
		});
	}
	/**
	 * Put up warning dialog for failed attempt to run the program with appropriate memory.
	 * @param maxMem the detected memory
	 */
	private static void doWarnLowMemory(final long maxMem) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				JOptionPane.showMessageDialog(null, "<html><p>Unable to auto-start Open Imperium Galactica MapEditor for version " + Configuration.VERSION + ".<br>Please make sure you have at least " 
						+ MINIMUM_MEMORY + "MB defined for running a Java program in either your<br>"
						+ "operating system's configuration for Java programs,<br> or run the program from command line using the <code>-Xmx" + MINIMUM_MEMORY + "M</code> parameter.</p><br>"
				);
			}
		});
	}
	/**
	 * Restart the program using the proper memory settings.
	 * @return true if the re initialization was successful
	 */
	private static boolean doLowMemory() {
		ProcessBuilder pb = new ProcessBuilder();
		if (!new File("open-ig-mapeditor-" + VERSION + ".jar").exists()) {
			pb.command(System.getProperty("java.home") + "/bin/java", "-Xmx" + MINIMUM_MEMORY + "M", "-cp", "./bin", "-splash:bin/hu/openig/xold/res/OpenIG_Splash.png", "hu.openig.editors.MapEditor", "-memonce");
		} else {
			pb.command(System.getProperty("java.home") + "/bin/java", "-Xmx" + MINIMUM_MEMORY + "M", "-cp", "open-ig-mapeditor-" + VERSION + ".jar", "-splash:hu/openig/xold/res/OpenIG_Splash.png", "hu.openig.editors.MapEditor", "-memonce");
		}
		try {
			pb.start();
			return true;
		} catch (IOException e) {
			Exceptions.add(e);
			return false;
		}
	}
	/**
	 * Create a background stream copy thread for the given input and output streams.
	 * @param in the input stream
	 * @param out the output stream
	 * @return the thread
	 */
	static Thread createBackgroundReader(final InputStream in, final OutputStream out) {
		return new Thread() {
			@Override
			public void run() {
				int c;
				try {
					while ((c = in.read()) != -1) {
						out.write(c);
						if (c == 10) {
							out.flush();
						}
					}
				} catch (IOException ex) {
					// ignored
				}
			}
		};
	}
	/** 
	 * Build the GUI. 
	 * @param config the configuration
	 */
	public MapEditor(final Configuration config) {
		super(TITLE);
		this.config = config;
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		toolSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		featuresSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		renderer = new MapRenderer();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				doExit();
			}
		});

		labels = new Labels();
		
		renderer.labels = labels;
		
		surfaceTableModel = new TileList();
		buildingTableModel = new TileList();
		buildingTableModel.colNames[3] = "Race";
		
		surfaceTable = new JTable(surfaceTableModel);
		surfaceTable.setRowHeight(32);
		buildingTable = new JTable(buildingTableModel);
		buildingTable.setRowHeight(32);

		surfaceTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		surfaceSorter = new TableRowSorter<>(surfaceTableModel);
		surfaceTable.setRowSorter(surfaceSorter);
		
		buildingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		buildingSorter = new TableRowSorter<>(buildingTableModel);
		buildingTable.setRowSorter(buildingSorter);

		surfaceTable.getColumnModel().getColumn(0).setPreferredWidth(45);
		surfaceTable.getColumnModel().getColumn(1).setPreferredWidth(32);
		buildingTable.getColumnModel().getColumn(0).setPreferredWidth(45);
		buildingTable.getColumnModel().getColumn(1).setPreferredWidth(32);

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
		GroupLayout glb = new GroupLayout(buildingPanel);
		buildingPanel.setLayout(glb);
		glb.setHorizontalGroup(glb.createParallelGroup().addComponent(filterBuilding).addComponent(sp1));
		glb.setVerticalGroup(glb.createSequentialGroup().addComponent(filterBuilding, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE).addComponent(sp1));
		
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

		filterSurface.setToolTipText("Filter by size, name and surface. Example: '1x earth' means search for tiles with width=1 and surface=earth");
		filterBuilding.setToolTipText("Filter by size, name and race. Example: '2x human' means search for tiles with width=2 and race=human");
		alphaSlider.setToolTipText("Adjust brightness level. Buildings should turn on lights below 51 by default.");

		ui.buildButton = new JToggleButton("Object placement mode");
		ui.buildButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ui.editPlaceMode.setSelected(ui.buildButton.isSelected());
				renderer.placementMode = ui.buildButton.isSelected();
				ui.toolbarPlacementMode.setSelected(ui.buildButton.isSelected());
			}
		});
		GroupLayout gl2 = new GroupLayout(previewPanel);
		previewPanel.setLayout(gl2);
		gl2.setAutoCreateContainerGaps(true);
		
		gl2.setHorizontalGroup(
				gl2.createParallelGroup(Alignment.CENTER)
				.addComponent(alphaSlider)
				.addComponent(preview)
				.addComponent(ui.buildButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		gl2.setVerticalGroup(
				gl2.createSequentialGroup()
				.addComponent(alphaSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(preview)
				.addComponent(ui.buildButton, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		
		
		propertyTab = new JTabbedPane();
		
		split.setLeftComponent(toolSplit);
		split.setRightComponent(renderer);
		split.setDividerLocation(450);
		
		toolSplit.setTopComponent(previewPanel);
		toolSplit.setBottomComponent(propertyTab);
		toolSplit.setDividerLocation(350);
		
		featuresSplit.setTopComponent(surfacePanel);
		featuresSplit.setBottomComponent(buildingPanel);
		featuresSplit.setDividerLocation(150);

		propertyTab.addTab("Surfaces & Buildings", featuresSplit);
		propertyTab.addTab("Building properties", createBuildingPropertiesPanel());
		ui.allocationPanel = new AllocationPanel();
		propertyTab.addTab("Allocation", ui.allocationPanel);
		
		split.setDoubleBuffered(true);
		split.setOneTouchExpandable(true);
		
		Container c = getContentPane();
		c.setLayout(new BorderLayout());
		c.add(split, BorderLayout.CENTER);
		setSize(1024, 768);
		
		renderer.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.isControlDown()) {
					double pre = renderer.scale;
					double mx = (e.getX() - renderer.offsetX) * pre;
					double my = (e.getY() - renderer.offsetY) * pre;
					if (e.getUnitsToScroll() < 0) {
						doZoomIn();
					} else {
						doZoomOut();
					}
					double mx0 = (e.getX() - renderer.offsetX) * renderer.scale;
					double my0 = (e.getY() - renderer.offsetY) * renderer.scale;
					double dx = (mx - mx0) / pre;
					double dy = (my - my0) / pre;
					renderer.offsetX += (int)(dx);
					renderer.offsetY += (int)(dy);
					renderer.repaint();
				}
			}
		});
		renderer.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e) && !renderer.placementMode) {
					doSelectBuilding(e.getX(), e.getY());
				}
			}
		});
		
		renderer.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isLeftMouseButton(e)) {
					if (ui.buildButton.isSelected()) {
						doPlaceObject();
					}
				}
			}
		});
		renderer.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if (renderer.current != null) {
					ui.cursorPosition.setText(renderer.current.x + ", " + renderer.current.y);
				} else {
					ui.cursorPosition.setText("");
				}
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				this.mouseMoved(e);
			}
		});
		
		undoManager = new UndoManager();
		
		buildMenu();
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				loadConfig();
				loadResourceLocator();
			}
		});
		
	}
	/**
	 * Exit the application.
	 */
	private void doExit() {
		try {
			saveConfig();
		} finally {
			dispose();
			renderer.stopAnimations();
			if (config.watcherWindow != null) {
				try {
					config.watcherWindow.close();
				} catch (IOException ex) {
					
				}
			}
		}
	}
	/**
	 * Construct the menu tree.
	 */
	private void buildMenu() {
		JMenuBar mainmenu = new JMenuBar();
		setJMenuBar(mainmenu);

		ui.fileMenu = new JMenu("File");
		ui.editMenu = new JMenu("Edit");
		ui.viewMenu = new JMenu("View");
		ui.helpMenu = new JMenu("Help");
		
		ui.fileNew = new JMenuItem("New...");
		ui.fileOpen = new JMenuItem("Open...");
		ui.fileImport = new JMenuItem("Import...");
		ui.fileSave = new JMenuItem("Save");
		ui.fileSaveAs = new JMenuItem("Save as...");
		ui.fileExit = new JMenuItem("Exit");

		ui.fileOpen.setAccelerator(KeyStroke.getKeyStroke('O', InputEvent.CTRL_DOWN_MASK));
		ui.fileSave.setAccelerator(KeyStroke.getKeyStroke('S', InputEvent.CTRL_DOWN_MASK));
		
		ui.fileImport.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doImport(); } });
		ui.fileExit.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doExit();  } });
		
		ui.fileOpen.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doLoad();
			}
		});
		ui.fileSave.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSave();
			}
		});
		ui.fileSaveAs.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSaveAs();
			}
		});
		
		ui.editUndo = new JMenuItem("Undo");
		ui.editUndo.setEnabled(undoManager.canUndo()); 
		
		ui.editRedo = new JMenuItem("Redo");
		ui.editRedo.setEnabled(undoManager.canRedo()); 
		
		
		ui.editCut = new JMenuItem("Cut");
		ui.editCopy = new JMenuItem("Copy");
		ui.editPaste = new JMenuItem("Paste");
		
		ui.editCut.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doCut(true, true); } });
		ui.editCopy.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doCopy(true, true); } });
		ui.editPaste.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doPaste(true, true); } });
		
		ui.editPlaceMode = new JCheckBoxMenuItem("Placement mode");
		
		ui.editDeleteBuilding = new JMenuItem("Delete building");
		ui.editDeleteSurface = new JMenuItem("Delete surface");
		ui.editDeleteBoth = new JMenuItem("Delete both");
		
		ui.editClearBuildings = new JMenuItem("Clear buildings");
		ui.editClearSurface = new JMenuItem("Clear surface");

		ui.editUndo.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doUndo(); } });
		ui.editRedo.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doRedo(); } });
		ui.editClearBuildings.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doClearBuildings(true); } });
		ui.editClearSurface.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doClearSurfaces(true); } });
		

		ui.editUndo.setAccelerator(KeyStroke.getKeyStroke('Z', InputEvent.CTRL_DOWN_MASK));
		ui.editRedo.setAccelerator(KeyStroke.getKeyStroke('Y', InputEvent.CTRL_DOWN_MASK));
		ui.editCut.setAccelerator(KeyStroke.getKeyStroke('X', InputEvent.CTRL_DOWN_MASK));
		ui.editCopy.setAccelerator(KeyStroke.getKeyStroke('C', InputEvent.CTRL_DOWN_MASK));
		ui.editPaste.setAccelerator(KeyStroke.getKeyStroke('V', InputEvent.CTRL_DOWN_MASK));
		ui.editPlaceMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		
		ui.editDeleteBuilding.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
		ui.editDeleteSurface.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK));
		ui.editDeleteBoth.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		
		ui.editDeleteBuilding.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doDeleteBuilding(); } });
		ui.editDeleteSurface.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doDeleteSurface(); } });
		ui.editDeleteBoth.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doDeleteBoth(); } });
		ui.editPlaceMode.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doPlaceMode(ui.editPlaceMode.isSelected()); } });
		
		ui.editPlaceRoads = new JMenu("Place roads");
		
		ui.editResize = new JMenuItem("Resize map");
		ui.editCleanup = new JMenuItem("Remove outbound objects");
		
		ui.editResize.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doResize();
			}
		});
		ui.editCleanup.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doCleanup();
			}
		});
		
		ui.editCutBuilding = new JMenuItem("Cut: building");
		ui.editCutSurface = new JMenuItem("Cut: surface");
		ui.editPasteBuilding = new JMenuItem("Paste: building");
		ui.editPasteSurface = new JMenuItem("Paste: surface");
		ui.editCopyBuilding = new JMenuItem("Copy: building");
		ui.editCopySurface = new JMenuItem("Copy: surface");
		
		ui.editCutBuilding.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		ui.editCopyBuilding.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		ui.editPasteBuilding.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
		
		ui.editCutBuilding.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doCut(false, true); } });
		ui.editCutSurface.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doCut(true, false); } });
		ui.editCopyBuilding.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doCopy(false, true); } });
		ui.editCopySurface.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doCopy(true, false); } });
		ui.editPasteBuilding.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doPaste(false, true); } });
		ui.editPasteSurface.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doPaste(true, false); } });
		
		ui.viewZoomIn = new JMenuItem("Zoom in");
		ui.viewZoomOut = new JMenuItem("Zoom out");
		ui.viewZoomNormal = new JMenuItem("Zoom normal");
		ui.viewBrighter = new JMenuItem("Daylight (1.0)");
		ui.viewDarker = new JMenuItem("Night (0.5)");
		ui.viewMoreLight = new JMenuItem("More light (+0.05)");
		ui.viewLessLight = new JMenuItem("Less light (-0.05)");
		
		ui.viewShowBuildings = new JCheckBoxMenuItem("Show/hide buildings", renderer.showBuildings);
		ui.viewShowBuildings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
		
		ui.viewSymbolicBuildings = new JCheckBoxMenuItem("Minimap rendering mode", renderer.minimapMode);
		ui.viewSymbolicBuildings.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
		
		ui.viewTextBackgrounds = new JCheckBoxMenuItem("Show/hide text background boxes", renderer.textBackgrounds);
		ui.viewTextBackgrounds.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		ui.viewTextBackgrounds.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doToggleTextBackgrounds(); } });
		
		ui.viewZoomIn.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doZoomIn(); } });
		ui.viewZoomOut.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doZoomOut(); } });
		ui.viewZoomNormal.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doZoomNormal(); } });
		ui.viewShowBuildings.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doToggleBuildings(); } });
		ui.viewSymbolicBuildings.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doToggleMinimap(); } });
		
		ui.viewBrighter.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doBright(); } });
		ui.viewDarker.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doDark(); } });
		ui.viewMoreLight.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doMoreLight(); } });
		ui.viewLessLight.addActionListener(new ActionListener() { @Override public void actionPerformed(ActionEvent e) { doLessLight(); } });
		
		ui.viewZoomIn.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, InputEvent.CTRL_DOWN_MASK));
		ui.viewZoomOut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, InputEvent.CTRL_DOWN_MASK));
		ui.viewZoomNormal.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD0, InputEvent.CTRL_DOWN_MASK));
		ui.viewMoreLight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, InputEvent.CTRL_DOWN_MASK));
		ui.viewLessLight.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, InputEvent.CTRL_DOWN_MASK));
		ui.viewBrighter.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, InputEvent.CTRL_DOWN_MASK));
		ui.viewDarker.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, InputEvent.CTRL_DOWN_MASK));
		
		ui.viewStandardFonts = new JCheckBoxMenuItem("Use standard fonts", false);
		
		ui.viewStandardFonts.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doStandardFonts();
			}
		});
		ui.viewPlacementHints = new JCheckBoxMenuItem("View placement hints", renderer.placementHints);
		ui.viewPlacementHints.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doViewPlacementHints();
			}
		});
		
		ui.helpOnline = new JMenuItem("Online wiki...");
		ui.helpOnline.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doHelp();
			}
		});
		ui.helpAbout = new JMenuItem("About...");
		ui.helpAbout.setEnabled(false); // TODO implement
		
		ui.languageMenu = new JMenu("Language");
		
		ui.languageEn = new JRadioButtonMenuItem("English", true);
		ui.languageEn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setLabels("en");
			}
		});
		ui.languageHu = new JRadioButtonMenuItem("Hungarian", false);
		ui.languageHu.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setLabels("hu");
			}
		});
		ButtonGroup bg = new ButtonGroup();
		bg.add(ui.languageEn);
		bg.add(ui.languageHu);
		
		ui.fileRecent = new JMenu("Recent");
		ui.clearRecent = new JMenuItem("Clear recent");
		ui.clearRecent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doClearRecent();
			}
		});
		
		addAll(ui.fileRecent, ui.clearRecent);
		
		addAll(mainmenu, ui.fileMenu, ui.editMenu, ui.viewMenu, ui.languageMenu, ui.helpMenu);
		addAll(ui.fileMenu, ui.fileNew, null, ui.fileOpen, ui.fileRecent, ui.fileImport, null, ui.fileSave, ui.fileSaveAs, null, ui.fileExit);
		addAll(ui.editMenu, ui.editUndo, ui.editRedo, null, 
				ui.editCut, ui.editCopy, ui.editPaste, null, 
				ui.editCutBuilding, ui.editCopyBuilding, ui.editPasteBuilding, null, 
				ui.editCutSurface, ui.editCopySurface, ui.editPasteSurface, null, 
				ui.editPlaceMode, null, ui.editDeleteBuilding, ui.editDeleteSurface, ui.editDeleteBoth, null, 
				ui.editClearBuildings, ui.editClearSurface, null, ui.editPlaceRoads, null, ui.editResize, ui.editCleanup);
		addAll(ui.viewMenu, ui.viewZoomIn, ui.viewZoomOut, ui.viewZoomNormal, null, 
				ui.viewBrighter, ui.viewDarker, ui.viewMoreLight, ui.viewLessLight, null, 
				ui.viewShowBuildings, ui.viewSymbolicBuildings, ui.viewTextBackgrounds, ui.viewStandardFonts, ui.viewPlacementHints);
		addAll(ui.helpMenu, ui.helpOnline, null, ui.helpAbout);
		
		addAll(ui.languageMenu, ui.languageEn, ui.languageHu);
		
		ui.fileNew.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doNew();
			}
		});
		
		ui.toolbar = new JToolBar("Tools");
		Container c = getContentPane();
		c.add(ui.toolbar, BorderLayout.PAGE_START);

		ui.toolbarCut = createFor("res/Cut24.gif", "Cut", ui.editCut, false);
		ui.toolbarCopy = createFor("res/Copy24.gif", "Copy", ui.editCopy, false);
		ui.toolbarPaste = createFor("res/Paste24.gif", "Paste", ui.editPaste, false);
		ui.toolbarRemove = createFor("res/Remove24.gif", "Remove", ui.editDeleteBuilding, false);
		ui.toolbarUndo = createFor("res/Undo24.gif", "Undo", ui.editUndo, false);
		ui.toolbarRedo = createFor("res/Redo24.gif", "Redo", ui.editRedo, false);
		ui.toolbarPlacementMode = createFor("res/Down24.gif", "Placement mode", ui.editPlaceMode, true);

		ui.toolbarUndo.setEnabled(false);
		ui.toolbarRedo.setEnabled(false);
		
		ui.toolbarNew = createFor("res/New24.gif", "New", ui.fileNew, false);
		ui.toolbarOpen = createFor("res/Open24.gif", "Open", ui.fileOpen, false);
		ui.toolbarSave = createFor("res/Save24.gif", "Save", ui.fileSave, false);
		ui.toolbarImport = createFor("res/Import24.gif", "Import", ui.fileImport, false);
		ui.toolbarSaveAs = createFor("res/SaveAs24.gif", "Save as", ui.fileSaveAs, false);
		ui.toolbarZoomNormal = createFor("res/Zoom24.gif", "Zoom normal", ui.viewZoomNormal, false);
		ui.toolbarZoomIn = createFor("res/ZoomIn24.gif", "Zoom in", ui.viewZoomIn, false);
		ui.toolbarZoomOut = createFor("res/ZoomOut24.gif", "Zoom out", ui.viewZoomOut, false);
		ui.toolbarBrighter = createFor("res/TipOfTheDay24.gif", "Daylight", ui.viewBrighter, false);
		ui.toolbarDarker = createFor("res/TipOfTheDayDark24.gif", "Night", ui.viewDarker, false);
		ui.toolbarHelp = createFor("res/Help24.gif", "Help", ui.helpOnline, false);

		ui.cursorPosition = new JLabel();
		ui.mapsize = new JLabel();
		
		
		ui.toolbar.add(ui.toolbarNew);
		ui.toolbar.add(ui.toolbarOpen);
		ui.toolbar.add(ui.toolbarSave);
		ui.toolbar.addSeparator();
		ui.toolbar.add(ui.toolbarImport);
		ui.toolbar.add(ui.toolbarSaveAs);
		ui.toolbar.addSeparator();
		ui.toolbar.add(ui.toolbarCut);
		ui.toolbar.add(ui.toolbarCopy);
		ui.toolbar.add(ui.toolbarPaste);
		
		ui.toolbar.add(ui.toolbarRemove);
		ui.toolbar.addSeparator();
		ui.toolbar.add(ui.toolbarUndo);
		ui.toolbar.add(ui.toolbarRedo);
		ui.toolbar.addSeparator();
		ui.toolbar.add(ui.toolbarPlacementMode);
		ui.toolbar.addSeparator();
		ui.toolbar.add(ui.toolbarZoomNormal);
		ui.toolbar.add(ui.toolbarZoomIn);
		ui.toolbar.add(ui.toolbarZoomOut);
		ui.toolbar.addSeparator();
		ui.toolbar.add(ui.toolbarBrighter);
		ui.toolbar.add(ui.toolbarDarker);
		ui.toolbar.addSeparator();
		ui.toolbar.add(ui.toolbarHelp);
		ui.toolbar.addSeparator();
		ui.toolbar.add(ui.cursorPosition);
		ui.toolbar.addSeparator();
		ui.toolbar.add(ui.mapsize);
		
	}
	/**
	 * Create a imaged button for the given menu item.
	 * @param graphicsResource the graphics resource location.
	 * @param tooltip the tooltip text
	 * @param inMenu the menu item to relay the click to.
	 * @param toggle create a toggle button?
	 * @return the button
	 */
	AbstractButton createFor(String graphicsResource, String tooltip, final JMenuItem inMenu, boolean toggle) {
		AbstractButton result = toggle ? new JToggleButton() : new JButton();
		URL res = MapEditor.class.getResource(graphicsResource);
		if (res != null) {
			result.setIcon(new ImageIcon(res));
		}
		result.setToolTipText(tooltip);
		result.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				inMenu.doClick();
			}
		});
		return result;
	}
	/** Toggle the rendering of text backgrounds. */
	protected void doToggleTextBackgrounds() {
		renderer.textBackgrounds = ui.viewTextBackgrounds.isSelected();
		renderer.repaint();
	}
	/** Toggle minimap display mode. */
	protected void doToggleMinimap() {
		renderer.minimapMode = ui.viewSymbolicBuildings.isSelected();
		renderer.repaint();
	}
	/**
	 * Toggle between the placement mode and selection mode.
	 * @param selected is placement mode on?
	 */
	protected void doPlaceMode(boolean selected) {
		ui.buildButton.setSelected(selected);
		renderer.placementMode = selected;
		ui.toolbarPlacementMode.setSelected(ui.buildButton.isSelected());
	}
	/**
	 * Zoom to 100%.
	 */
	protected void doZoomNormal() {
		renderer.scale = 1.0;
		renderer.repaint();
	}
	/**
	 * 
	 */
	protected void doZoomOut() {
		renderer.scale = Math.max(0.1, renderer.scale - 0.1);
		renderer.repaint();
	}
	/**
	 * 
	 */
	protected void doZoomIn() {
		renderer.scale = Math.min(2.0, renderer.scale + 0.1);
		renderer.repaint();
	}
	/** Set the state of the undo and redo menu items. */
	void setUndoRedoMenu() {
		ui.editUndo.setEnabled(undoManager.canUndo());
		ui.editRedo.setEnabled(undoManager.canRedo());
		ui.toolbarUndo.setEnabled(undoManager.canUndo());
		ui.toolbarRedo.setEnabled(undoManager.canRedo());
	}
	/** Redo last operation. */
	protected void doRedo() {
		undoManager.redo();
		setUndoRedoMenu();
	}
	/** Undo last operation. */
	protected void doUndo() {
		undoManager.undo();
		setUndoRedoMenu();
	}
	/** 
	 * Add an undoable map edit and update menus accordingly.
	 * @param undo the undo object
	 */
	void addUndo(UndoableMapEdit undo) {
		undoManager.addEdit(undo);
		setUndoRedoMenu();
	}
	/** 
	 * Clear surfaces. 
	 * @param redoable save a redo point?
	 */
	protected void doClearSurfaces(boolean redoable) {
		if (renderer.surface != null) {
			UndoableMapEdit undo = redoable ? new UndoableMapEdit(renderer.surface) : null;
			renderer.surface.basemap.clear();
			renderer.surface.features.clear();
			if (undo != null) {
				undo.setAfter();
				addUndo(undo);
			}
			renderer.repaint();
		}
	}
	/** 
	 * Clear buildings. 
	 * @param redoable save a redo point?
	 */
	protected void doClearBuildings(boolean redoable) {
		if (renderer.surface != null) {
			UndoableMapEdit undo = redoable ? new UndoableMapEdit(renderer.surface) : null;
			renderer.surface.buildingmap.clear();
			renderer.surface.buildings.clear();
			if (undo != null) {
				undo.setAfter();
				addUndo(undo);
			}
			renderer.repaint();
		}
	}
	/**
	 * Decrease the light amount on the tiles.
	 */
	protected void doLessLight() {
		renderer.alpha = Math.max(0, renderer.alpha - 0.05f);
		repaint();
	}
	/**
	 * Increase the light amount on the tiles.
	 */
	protected void doMoreLight() {
		renderer.alpha = Math.min(1f, renderer.alpha + 0.05f);
        repaint();
	}
	/** Set lighting to half. */
	protected void doDark() {
		renderer.alpha = 0.5f;
		repaint();
	}
	/** Set lighting to full. */
	protected void doBright() {
		renderer.alpha = 1.0f;
		repaint();
	}
	/**
	 * Toggle the visibility of the buildings.
	 */
	protected void doToggleBuildings() {
		renderer.showBuildings = ui.viewShowBuildings.getState();
		repaint();
	}
	/** Delete buildings and surface elements of the selection rectangle. */
	protected void doDeleteBoth() {
		if (renderer.surface != null) {
			UndoableMapEdit undo = new UndoableMapEdit(renderer.surface);
			deleteEntitiesOf(renderer.surface.basemap, renderer.selectedRectangle, false);
			deleteEntitiesOf(renderer.surface.buildingmap, renderer.selectedRectangle, true);
			undo.setAfter();
			addUndo(undo);
			repaint();
		}
	}
	/** Delete surface surface. */
	protected void doDeleteSurface() {
		if (renderer.surface != null) {
			UndoableMapEdit undo = new UndoableMapEdit(renderer.surface);
			deleteEntitiesOf(renderer.surface.basemap, renderer.selectedRectangle, false);
			undo.setAfter();
			addUndo(undo);
			repaint();
		}
	}
	/**
	 * Delete buildings falling into the current selection rectangle.
	 */
	protected void doDeleteBuilding() {
		if (renderer.surface != null) {
			UndoableMapEdit undo = new UndoableMapEdit(renderer.surface);
			deleteEntitiesOf(renderer.surface.buildingmap, renderer.selectedRectangle, true);
			undo.setAfter();
			addUndo(undo);
			repaint();
		}
	}
	/**
	 * Delete entries of the given map.
	 * @param map the map
	 * @param rect the target rectangle
	 * @param checkBuildings check for the actual buildings
	 */
	void deleteEntitiesOf(Map<Location, SurfaceEntity> map, Rectangle rect, boolean checkBuildings) {
		// find buildings falling into the selection box
		if (rect != null) {
			Building bld = null;
			for (int a = rect.x; a < rect.x + rect.width; a++) {
				for (int b = rect.y; b > rect.y - rect.height; b--) {
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
						for (Building b0 : renderer.surface.buildings.list()) {
							bld = b0;
							if (bld.containsLocation(a, b)) {
								renderer.surface.buildings.remove(b0);
								if (bld == currentBuilding) {
									renderer.buildingBox = null;
									currentBuilding = null;
								}
							}
						}
					} else {
						for (int i = renderer.surface.features.size() - 1; i >= 0; i--) {
							SurfaceFeature sf = renderer.surface.features.get(i);
							if (sf.containsLocation(a, b)) {
								renderer.surface.features.remove(i);
//								if (sf.equals(currentBaseTile)) {
//									currentBaseTile = null;
//								}
							}
						}
					}
				}
			}
			if (checkBuildings && bld != null) {
				renderer.surface.placeRoads(bld.race, buildingModel);
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
			buildingSorter.setRowFilter(new RowFilterByWords(words));
		} else {
			buildingSorter.setRowFilter(null);
		}
	}
	/** Filter the surface list. */
	protected void doFilterSurface() {
		if (filterSurface.getText().length() > 0) {
			final String[] words = filterSurface.getText().split("\\s+");
			surfaceSorter.setRowFilter(new RowFilterByWords(words));
			
		} else {
			surfaceSorter.setRowFilter(null);
		}
	}
	/**
	 * A tile list table model.
	 * @author akarnokd
	 */
	public static class TileList extends AbstractTableModel {
		/** */
		private static final long serialVersionUID = 1870030483025880490L;
		/** The column names. */
		final String[] colNames = { "Preview", "Size", "Name", "Surface" };
		/** The column classes. */
		final Class<?>[] colClasses = { ImageIcon.class, String.class, String.class, String.class };
		/** The list of rows. */
		public List<TileEntry> rows = new ArrayList<>();
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
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (columnIndex == 2) {
				TileEntry te = rows.get(rowIndex);
				te.name = String.valueOf(aValue);
			}
		}
	}
	/**
	 * Build table contents.
	 * @param surfaces the list of surface objects
	 * @param buildings the list of building objects
	 * @param races the set of races
	 */
	void buildTables(List<TileEntry> surfaces, List<TileEntry> buildings, Set<String> races) {
		surfaceTableModel.rows = surfaces;
		buildingTableModel.rows = buildings;
		surfaceTableModel.fireTableDataChanged();
		buildingTableModel.fireTableDataChanged();
		List<String> racesList = new ArrayList<>(races);
		Collections.sort(racesList);
		for (final String s : racesList) {
			JMenuItem mnuPlaceRoad = new JMenuItem(s);
			mnuPlaceRoad.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					renderer.surface.placeRoads(s, buildingModel);
					repaint();
				}
			});
			
			
			ui.editPlaceRoads.add(mnuPlaceRoad);
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
			TileEntry tileEntry = surfaceTableModel.rows.get(idx);
			preview.setImage(tileEntry.previewTile);
			renderer.placementRectangle.width = tileEntry.tile.width;
			renderer.placementRectangle.height = tileEntry.tile.height;
			buildingTable.getSelectionModel().clearSelection();
			
			currentBaseTile = new SurfaceFeature(); 
			currentBaseTile.tile = tileEntry.tile;
			currentBaseTile.id = tileEntry.id;
			currentBaseTile.type = tileEntry.surface;
			
			currentBuildingType = null;
		}
	}
	/** Select a surface image. */
	void doSelectBuilding() {
		int idx = buildingTable.getSelectedRow();
		if (idx >= 0) {
			idx = buildingTable.convertRowIndexToModel(idx);
			TileEntry tileEntry = buildingTableModel.rows.get(idx);
			preview.setImage(tileEntry.previewTile);
			renderer.placementRectangle.width = tileEntry.tile.width + 2;
			renderer.placementRectangle.height = tileEntry.tile.height + 2;
			surfaceTable.getSelectionModel().clearSelection();
			currentBuildingType = tileEntry.buildingType;
			currentBuildingRace = tileEntry.surface;

			currentBaseTile = null;
		}
	}
	/**
	 * Create a new, empty surface map.
	 */
	void doNew() {
		NewResizeDialog dlg = new NewResizeDialog(labels, true);
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);
		if (dlg.success) {
			setTitle(TITLE);
			createPlanetSurface(dlg.width, dlg.height);
			renderer.repaint();
			saveSettings = null;
			undoManager.discardAllEdits();
			setUndoRedoMenu();
		}
	}
	/**
	 * Create an empty planet surface with the given size.
	 * @param width the horizontal width (and not in coordinate amounts!)
	 * @param height the vertical height (and not in coordinate amounts!) 
	 */
	void createPlanetSurface(int width, int height) {
		ui.mapsize.setText(width + " x " + height);
		renderer.surface = new PlanetSurface();
		renderer.surface.setSize(width, height);
		ui.allocationPanel.buildings.addAll(renderer.surface.buildings.list());
	}
//	/**
//	 * Place a tile onto the current surface map.
//	 * @param tile the tile
//	 * @param x the tile's leftmost coordinate
//	 * @param y the tile's leftmost coordinate
//	 * @param type the tile type
//	 * @param building the building object to assign
//	 */
//	void placeTile(Tile tile, int x, int y, SurfaceEntityType type, Building building) {
//		for (int a = x; a < x + tile.width; a++) {
//			for (int b = y; b > y - tile.height; b--) {
//				SurfaceEntity se = new SurfaceEntity();
//				se.type = type;
//				se.virtualRow = y - b;
//				se.virtualColumn = a - x;
//				se.tile = tile;
//				se.tile.alpha = alpha;
//				se.building = building;
//				if (type != SurfaceEntityType.BASE) {
//					renderer.surface.buildingmap.put(Location.of(a, b), se);
//				} else {
//					renderer.surface.basemap.put(Location.of(a, b), se);
//				}
//			}
//		}
//	}
	/**
	 * Place the selected surface tile into the selection rectangle.
	 */
	void doPlaceSurface() {
		int idx = surfaceTable.getSelectedRow();
		if (idx >= 0) {
			idx = surfaceTable.convertRowIndexToModel(idx);
			TileEntry te = surfaceTableModel.rows.get(idx);
			if (renderer.selectedRectangle != null && renderer.selectedRectangle.width > 0) {
				UndoableMapEdit undo = new UndoableMapEdit(renderer.surface);
				
				Rectangle clearRect = new Rectangle(renderer.selectedRectangle);
				clearRect.width = ((clearRect.width + te.tile.width - 1) / te.tile.width) * te.tile.width;
				clearRect.height = ((clearRect.height + te.tile.height - 1) / te.tile.height) * te.tile.height;
				deleteEntitiesOf(renderer.surface.basemap, clearRect, false);
				
				for (int x = renderer.selectedRectangle.x; x < renderer.selectedRectangle.x + renderer.selectedRectangle.width; x += te.tile.width) {
					for (int y = renderer.selectedRectangle.y; y > renderer.selectedRectangle.y - renderer.selectedRectangle.height; y -= te.tile.height) {
						renderer.surface.placeBase(te.tile, x, y, te.id, te.surface);
					}
				}
				undo.setAfter();
				addUndo(undo);
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
				UndoableMapEdit undo = new UndoableMapEdit(renderer.surface);
				
				Rectangle clearRect = new Rectangle(renderer.selectedRectangle);
				clearRect.width = ((clearRect.width + te.tile.width) / (te.tile.width + 1)) * (te.tile.width + 1) + 1;
				clearRect.height = ((clearRect.height + te.tile.height) / (te.tile.height + 1)) * (te.tile.height + 1) + 1;
				deleteEntitiesOf(renderer.surface.buildingmap, clearRect, true);
				
				for (int x = renderer.selectedRectangle.x; x < renderer.selectedRectangle.x + renderer.selectedRectangle.width; x += te.tile.width + 1) {
					for (int y = renderer.selectedRectangle.y; y > renderer.selectedRectangle.y - renderer.selectedRectangle.height; y -= te.tile.height + 1) {
						Building bld = new Building(-1, te.buildingType, te.surface);
						bld.makeFullyBuilt();
						bld.location = Location.of(x + 1, y - 1);
						renderer.surface.placeBuilding(te.tile, bld.location.x, bld.location.y, bld);
					}
				}
				renderer.surface.placeRoads(te.surface, buildingModel);
				undo.setAfter();
				addUndo(undo);
				renderer.repaint();
			}
		}
	}
	/**
	 * Display the help in a browser window.
	 */
	void doHelp() {
		if (Desktop.isDesktopSupported()) {
    		Desktop desktop = Desktop.getDesktop();
			try {
				desktop.browse(new URI("http://code.google.com/p/open-ig/wiki/MapEditor"));
			} catch (IOException | URISyntaxException ex) {
				JOptionPane.showMessageDialog(MapEditor.this, "Exception", stacktraceToString(ex), JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showInputDialog(MapEditor.this, "Desktop not supported. Please navigate to http://code.google.com/p/open-ig/wiki/MapEditor manually.", "http://code.google.com/p/open-ig/wiki/MapEditor");
		}
	}
	/**
	 * Show the import dialog.
	 */
	void doImport() {
		if (imp == null) {
			imp = new ImportDialog(rl);
		}
		imp.success = false;
		imp.setLabels(labels);
		imp.setLocationRelativeTo(this);
		imp.setVisible(true);
		if (imp.success) {
			if (renderer.surface == null) {
				createPlanetSurface(33, 66);
			}
			UndoableMapEdit undo = new UndoableMapEdit(renderer.surface);
			if (imp.selected != null) {
				if (imp.replaceSurface) {
					doClearSurfaces(false);
				}
				placeTilesFromOriginalMap(imp.selected.fullPath, imp.selected.surfaceType, imp.shiftXValue, imp.shiftYValue);
			}
			if (imp.planet != null) {
				if (imp.replaceBuildings) {
					doClearBuildings(false);
				}
				if (imp.withSurface) {
					if (imp.replaceSurface) {
						doClearSurfaces(false);
					}
					placeTilesFromOriginalMap("colony/" + imp.planet.getMapName(), imp.planet.surfaceType.toLowerCase(Locale.ENGLISH), imp.shiftXValue, imp.shiftYValue);
				}

				for (OriginalBuilding ob : imp.planet.buildings) {
					BuildingType bt = buildingModel.buildings.get(ob.getName()); 
					String r = imp.planet.getRaceTechId();
					TileSet t = bt.tileset.get(r);
					Building bld = new Building(-1, bt, r);
					bld.makeFullyBuilt();
					bld.location = Location.of(ob.location.x + imp.shiftXValue, ob.location.y + imp.shiftYValue);
					
					renderer.surface.placeBuilding(t.normal, ob.location.x + imp.shiftXValue, ob.location.y + imp.shiftYValue, bld);
				}
				renderer.buildingBox = null;
				currentBuilding = null;
				renderer.surface.placeRoads(imp.planet.getRaceTechId(), buildingModel);
			} else {
				doClearBuildings(false);
			}
			undo.setAfter();
			addUndo(undo);
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
		byte[] map = rl.getData(path);
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
					renderer.surface.placeBase(t, loc.x + shiftX, 
							loc.y + shiftY, tile, surfaceType);
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
	 * Place the currently selected object onto the surface.
	 */
	void doPlaceObject() {
		if (renderer.surface == null) {
			return;
		}
		SurfaceFeature cbt = currentBaseTile;
		if (cbt != null) {
			UndoableMapEdit undo = new UndoableMapEdit(renderer.surface);
			deleteEntitiesOf(renderer.surface.basemap, renderer.placementRectangle, false);
			
			renderer.surface.placeBase(cbt.tile, 
					renderer.placementRectangle.x, 
					renderer.placementRectangle.y, cbt.id, cbt.type);
			
			undo.setAfter();
			addUndo(undo);
			renderer.repaint();
		} else
		if (currentBuildingType != null && renderer.surface.placement.canPlaceBuilding(renderer.placementRectangle) && renderer.placementRectangle.width > 0) {
			UndoableMapEdit undo = new UndoableMapEdit(renderer.surface);
			Building bld = new Building(-1, currentBuildingType, currentBuildingRace);
			bld.makeFullyBuilt();
			bld.location = Location.of(renderer.placementRectangle.x + 1, renderer.placementRectangle.y - 1); // leave room for the roads!
			
			renderer.surface.placeBuilding(bld.tileset.normal, 
					bld.location.x, bld.location.y, bld);
			renderer.surface.placeRoads(currentBuildingRace, buildingModel);
			
			undo.setAfter();
			addUndo(undo);
			renderer.repaint();
		}
	}
	/**
	 * Select a building.
	 * @param mx the mouse coordinate
	 * @param my the mouse coordinate
	 */
	void doSelectBuilding(int mx, int my) {
		if (renderer.surface == null) {
			return;
		}
		Location loc = renderer.getLocationAt(mx, my);
		SurfaceEntity se = renderer.surface.buildingmap.get(loc);
		currentBuilding = null;
		if (renderer.showBuildings && se != null && se.type == SurfaceEntityType.BUILDING) {
			currentBuilding = se.building;
		} else { 
			se = renderer.surface.basemap.get(loc);
			if (se != null) {
				selectSurfaceEntity(se.tile);
			}
		}
		loadBuildingProperties();
		renderer.buildingBox = renderer.getBoundingRect(loc);
		renderer.repaint();
	}
	/**
	 * Select the surface tile in the lists and preview.
	 * @param tile the tile
	 */
	private void selectSurfaceEntity(Tile tile) {
		int i = 0;
		for (TileEntry te : surfaceTableModel.rows) {
			if (te.tile == tile) {
				int idx = surfaceTable.convertRowIndexToView(i);
				surfaceTable.getSelectionModel().addSelectionInterval(idx, idx);
				surfaceTable.scrollRectToVisible(surfaceTable.getCellRect(idx, 0, true));
			}
			i++;
		}		
	}
	/**
	 * Prepare the lists.
	 * @param galaxyModel the galaxy model
	 * @param buildingModel the building model
	 * @param surfaces the surfaces list
	 * @param buildings the buildings list
	 * @param races the races set
	 */
	void prepareLists(GalaxyModel galaxyModel, BuildingModel buildingModel, 
			List<TileEntry> surfaces, List<TileEntry> buildings, Set<String> races) {
		for (Map.Entry<String, PlanetType> pt : galaxyModel.planetTypes.entrySet()) {
			for (Map.Entry<Integer, Tile> te : pt.getValue().tiles.entrySet()) {
				TileEntry e = new TileEntry();
				e.id = te.getKey();
				e.surface = pt.getKey();
				e.name = findCustomName(customSurfaceNames, e.id, e.surface, "" + e.id);
				e.tile = te.getValue();
				e.tile.alpha = 1.0f;
				e.previewTile = e.tile.copy();
				e.preview = new ImageIcon(scaledImage(e.tile.getFullImage(), 32, 32));
				surfaces.add(e);
			}
		}
		Collections.sort(surfaces, TileEntry.DEFAULT_ORDER);
		
		int idx = 0;
		for (Map.Entry<String, BuildingType> bt : buildingModel.buildings.entrySet()) {
			for (Map.Entry<String, TileSet> tss : bt.getValue().tileset.entrySet()) {
				TileEntry e = new TileEntry();
				e.id = idx;
				e.surface = tss.getKey();
				e.name = findCustomName(customBuildingNames, e.id, e.surface, bt.getKey());
				e.tile = tss.getValue().normal;
				e.tile.alpha = 1.0f;
				e.previewTile = e.tile.copy();
				e.preview = new ImageIcon(scaledImage(e.tile.getFullImage(), 32, 32));
				e.buildingType = bt.getValue();
				buildings.add(e);
				
//				e = new TileEntry();
//				e.id = idx;
//				e.surface = tss.getKey();
//				e.name = bt.getKey() + " damaged";
//				e.tile = tss.getValue().damaged;
//				e.tile.alpha = 1.0f;
//				e.previewTile = e.tile.copy();
//				e.preview = new ImageIcon(scaledImage(e.tile.getFullImage(), 32, 32));
//				e.buildingType = bt.getValue();
//				buildings.add(e);
				races.add(tss.getKey());
			}
			idx++;
		}
		Collections.sort(buildings, TileEntry.DEFAULT_ORDER);
	}
	/**
	 * Find a custom name in the given entry list.
	 * @param list the entry list
	 * @param id the tile identifier
	 * @param type the tile type (e.g., suface or race)
	 * @param defaultName the default name to use if not found
	 * @return the custom name
	 */
	String findCustomName(List<TileEntry> list, int id, String type, String defaultName) {
		for (TileEntry te : list) {
			if (te.id == id && te.surface.equals(type)) {
				return te.name;
			}
		}
		return defaultName;
	}
	/**
	 * @return the building properties panel
	 */
	JPanel createBuildingPropertiesPanel() {
		ui.buildingInfoPanel = new BuildingInfoPanel();
		
		ui.buildingInfoPanel.apply.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doApplyBuildingSettings();
			}
		});
		ui.buildingInfoPanel.refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				displayBuildingInfo();
			}
		});

		
		return ui.buildingInfoPanel;
	}
	/**
	 * Set the GUI values from the current selected building object.
	 */
	void loadBuildingProperties() {
		if (currentBuilding != null) {
			// locate the building in the buildings list
			int i = 0;
			for (TileEntry te : buildingTableModel.rows) {
				if (te.buildingType.tileset.get(te.surface) == currentBuilding.tileset) {
					int idx = buildingTable.convertRowIndexToView(i);
					buildingTable.getSelectionModel().addSelectionInterval(idx, idx);
					buildingTable.scrollRectToVisible(buildingTable.getCellRect(idx, 0, true));
					break;
				}
				i++;
			}
			
			displayBuildingInfo();
		} else {
			ui.buildingInfoPanel.apply.setEnabled(false);
		}
	}
	/**
	 * Display the building info.
	 */
	private void displayBuildingInfo() {
		if (currentBuilding == null) {
			return;
		}
		ui.buildingInfoPanel.buildingName.setText(currentBuilding.type.name);
		ui.buildingInfoPanel.completed.setText("" + currentBuilding.buildProgress);
		ui.buildingInfoPanel.completedTotal.setText("" + currentBuilding.type.hitpoints);
		ui.buildingInfoPanel.hitpoints.setText("" + currentBuilding.hitpoints);
		ui.buildingInfoPanel.hitpointsTotal.setText("" + currentBuilding.buildProgress);
		ui.buildingInfoPanel.assignedWorkers.setText("" + currentBuilding.assignedWorker);
		ui.buildingInfoPanel.workerTotal.setText("" + currentBuilding.getWorkers());
		ui.buildingInfoPanel.assignedEnergy.setText("" + currentBuilding.assignedEnergy);
		ui.buildingInfoPanel.energyTotal.setText("" + currentBuilding.getEnergy());
		ui.buildingInfoPanel.efficiency.setText(String.format("%.3f%%", currentBuilding.getEfficiency() * 100));
		ui.buildingInfoPanel.tech.setText(currentBuilding.race);
		ui.buildingInfoPanel.cost.setText("" + currentBuilding.type.cost);
		ui.buildingInfoPanel.locationX.setText("" + currentBuilding.location.x);
		ui.buildingInfoPanel.locationY.setText("" + currentBuilding.location.y);
		ui.buildingInfoPanel.apply.setEnabled(true);
		ui.buildingInfoPanel.buildingEnabled.setSelected(currentBuilding.enabled);
		ui.buildingInfoPanel.buildingRepairing.setSelected(currentBuilding.repairing);
		
		ui.buildingInfoPanel.completionPercent.setText(String.format("  %.3f%%", currentBuilding.buildProgress * 100.0 / currentBuilding.type.hitpoints));
		ui.buildingInfoPanel.hitpointPercent.setText(String.format("  %.3f%%", currentBuilding.hitpoints * 100.0 / currentBuilding.buildProgress));
		ui.buildingInfoPanel.workerPercent.setText(String.format("  %.3f%%", currentBuilding.assignedWorker * 100.0 / currentBuilding.getWorkers()));
		ui.buildingInfoPanel.energyPercent.setText(String.format("  %.3f%%", currentBuilding.assignedEnergy * 100.0 / currentBuilding.getEnergy()));
		
		ui.buildingInfoPanel.upgradeList.removeAllItems();
		ui.buildingInfoPanel.upgradeList.addItem("None");
		for (int j = 0; j < currentBuilding.type.upgrades.size(); j++) {
			ui.buildingInfoPanel.upgradeList.addItem(currentBuilding.type.upgrades.get(j).description);
		}
		ui.buildingInfoPanel.upgradeList.setSelectedIndex(currentBuilding.upgradeLevel);
		
		ui.buildingInfoPanel.resourceTableModel.rows.clear();
		for (String r : currentBuilding.type.resources.keySet()) {
			if ("worker".equals(r) || "energy".equals(r)) {
				continue;
			}
			Resource res = new Resource();
			res.type = r;
			res.amount = currentBuilding.getResource(r);
			ui.buildingInfoPanel.resourceTableModel.rows.add(res);
		}
		ui.buildingInfoPanel.resourceTableModel.fireTableDataChanged();
	}
	/**
	 * Apply the building settings.
	 */
	void doApplyBuildingSettings() {
		if (currentBuilding == null) {
			return;
		}
		
		currentBuilding.enabled = ui.buildingInfoPanel.buildingEnabled.isSelected();
		currentBuilding.repairing = ui.buildingInfoPanel.buildingRepairing.isSelected();
		
		currentBuilding.upgradeLevel = ui.buildingInfoPanel.upgradeList.getSelectedIndex();
		if (currentBuilding.upgradeLevel > 0) {
			currentBuilding.currentUpgrade = currentBuilding.type.upgrades.get(currentBuilding.upgradeLevel - 1);
		} else {
			currentBuilding.currentUpgrade = null;
		}
		
		currentBuilding.buildProgress = Math.min(Integer.parseInt(ui.buildingInfoPanel.completed.getText()), currentBuilding.type.hitpoints);
		currentBuilding.hitpoints = Math.min(currentBuilding.buildProgress, Integer.parseInt(ui.buildingInfoPanel.hitpoints.getText()));
		currentBuilding.assignedWorker = Math.min(0, Math.max(Integer.parseInt(ui.buildingInfoPanel.assignedWorkers.getText()), currentBuilding.getWorkers()));
		currentBuilding.assignedEnergy = Math.min(0, Math.max(Integer.parseInt(ui.buildingInfoPanel.assignedEnergy.getText()), currentBuilding.getEnergy()));

		displayBuildingInfo();
		renderer.repaint();
	}
	/**
	 * Save the current planet definition to the output.
	 * @param settings the save settings
	 */
	void savePlanet(MapSaveSettings settings) {
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(settings.fileName), 1024 * 1024), "UTF-8"))) {
			out.printf("<?xml version='1.0' encoding='UTF-8'?>%n");
			XElement map = new XElement("map");
			map.set("version", "1.0");
			renderer.surface.storeMap(map, settings.surface, settings.buildings);
			out.println(map.toString());
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
	}
	/** Perform the save. */
	void doSave() {
		if (saveSettings == null) {
			doSaveAs();
		} else {
			savePlanet(saveSettings);
			setTitle(TITLE + ": " + saveSettings.fileName.getAbsolutePath());
		}
	}
	/** Perform the save as. */
	void doSaveAs() {
		MapOpenSaveDialog dlg = new MapOpenSaveDialog(true, labels, saveSettings);
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);
		if (dlg.saveSettings != null) {
			saveSettings = dlg.saveSettings;
			final String fn = dlg.saveSettings.fileName.getPath();
			addRecentEntry(fn);
			doSave();
		}
	}
	/** Perform the load. */
	void doLoad() {
		MapOpenSaveDialog dlg = new MapOpenSaveDialog(false, labels, saveSettings);
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);
		if (dlg.saveSettings != null) {
			
			final String fn = dlg.saveSettings.fileName.getPath();
			addRecentEntry(fn);
			
			saveSettings = dlg.saveSettings;
			loadPlanet(saveSettings);
			setTitle(TITLE + ": " + saveSettings.fileName.getAbsolutePath());
			undoManager.discardAllEdits();
			setUndoRedoMenu();
		}		
	}
    /** A constant negative one. */
    private static final Func0<Integer> CONST_MINUS_1 = new Func0<Integer>() {
        @Override
        public Integer invoke() {
            return -1;
        }
    };

	/**
	 * Load a planet definition.
	 * @param settings the settings
	 */
	void loadPlanet(MapSaveSettings settings) {
		try {
			if (renderer.surface == null) {
				createPlanetSurface(33, 66);
			}
			XElement root = XElement.parseXML(settings.fileName.getAbsolutePath());
			XElement planet = root;
			if ("planets".equals(root.name)) {
				planet = root.childElement("planet");
			}
			if (settings.surface) {
				doClearSurfaces(false);
				renderer.surface.parseMap(planet, galaxyModel, null, CONST_MINUS_1);
			}
			if (settings.buildings) {
				doClearBuildings(false);
				String tech = renderer.surface.getTechnology();
				renderer.surface.parseMap(planet, null, buildingModel, CONST_MINUS_1);
				if (tech != null) {
					renderer.surface.placeRoads(tech, buildingModel);
				}
			}
			renderer.buildingBox = null;
			currentBuilding = null;
			ui.mapsize.setText(renderer.surface.width + " x " + renderer.surface.height);
			renderer.repaint();
		} catch (XMLStreamException ex) {
			Exceptions.add(ex);
		}
	}
	/**
	 * Set the UI labels for the given language.
	 * @param language the new language
	 */
	void setLabels(String language) {
		if (rl == null) {
			deferredLanguage = language;
			return;
		}
		labels.load(rl, Arrays.asList("labels", "campaign/main/labels"));
		renameFieldsOf(ui);
		renameFieldsOf(ui.allocationPanel);
		renameFieldsOf(ui.buildingInfoPanel);
		propertyTab.setTitleAt(0, labels.get("mapeditor.surface_and_buildings"));
		propertyTab.setTitleAt(1, labels.get("mapeditor.building_properties"));
		propertyTab.setTitleAt(2, labels.get("mapeditor.allocation"));
		surfaceTableModel.colNames[0] = labels.get("mapeditor.tile_preview");
		surfaceTableModel.colNames[1] = labels.get("mapeditor.tile_size");
		surfaceTableModel.colNames[2] = labels.get("mapeditor.tile_name");
		surfaceTableModel.colNames[3] = labels.get("mapeditor.tile_surface");

		buildingTableModel.colNames[0] = labels.get("mapeditor.tile_preview");
		buildingTableModel.colNames[1] = labels.get("mapeditor.tile_size");
		buildingTableModel.colNames[2] = labels.get("mapeditor.tile_name");
		buildingTableModel.colNames[3] = labels.get("mapeditor.tile_race");
		
		surfaceTableModel.fireTableStructureChanged();
		buildingTableModel.fireTableStructureChanged();
		
		filterSurface.setToolTipText(labels.get("mapeditor.filter_surface"));
		filterBuilding.setToolTipText(labels.get("mapeditor.filter_building"));
		alphaSlider.setToolTipText(labels.get("mapeditor.preview_brightness"));
		
		surfaceTable.getColumnModel().getColumn(0).setPreferredWidth(45);
		surfaceTable.getColumnModel().getColumn(1).setPreferredWidth(32);
		buildingTable.getColumnModel().getColumn(0).setPreferredWidth(45);
		buildingTable.getColumnModel().getColumn(1).setPreferredWidth(32);
	}
	/**
	 * Rename the annotated fields of the object.
	 * @param o the target object
	 */
	void renameFieldsOf(Object o) {
		for (Field f : o.getClass().getDeclaredFields()) {
			Rename fr = f.getAnnotation(Rename.class);
			if (fr != null) {
				try {
					if (AbstractButton.class.isAssignableFrom(f.getType())) {
						AbstractButton btn = AbstractButton.class.cast(f.get(o));
						if (fr.to().length() > 0) {
							btn.setText(labels.get(fr.to()));
						}
						if (fr.tip().length() > 0) {
							btn.setToolTipText(labels.get(fr.tip()));
						}
					}
					if (JLabel.class.isAssignableFrom(f.getType())) {
						JLabel lbl = JLabel.class.cast(f.get(o));
						if (fr.to().length() > 0) {
							lbl.setText(labels.get(fr.to()));
						}
						if (fr.tip().length() > 0) {
							lbl.setToolTipText(labels.get(fr.tip()));
						}
					}
					if (String.class.isAssignableFrom(f.getType())) {
						f.set(o, labels.get(fr.to()));
					}
				} catch (IllegalAccessException ex) {
					Exceptions.add(ex);
				}
			}
		}
	}
	/**
	 * The state capture for the surface editing.
	 * @author akarnokd
	 */
	class UndoableMapEdit implements UndoableEdit {
		/** The reference surface. */
		final PlanetSurface surface;
		/** The before state of the basemap. */
		Map<Location, SurfaceEntity> basemapBefore;
		/** The after state of the basemap. */
		Map<Location, SurfaceEntity> basemapAfter;
		/** The building map before. */
		Map<Location, SurfaceEntity> buildingmapBefore;
		/** The building map after. */
		Map<Location, SurfaceEntity> buildingmapAfter;
		/** The surface features before. */
		List<SurfaceFeature> surfaceBefore;
		/** The surface features after. */
		List<SurfaceFeature> surfaceAfter;
		/** The buildings before. */
		List<Building> buildingsBefore;
		/** The buildings after. */
		List<Building> buildingsAfter;
		/**
		 * Constructor.
		 * @param surface sets the surface object
		 */
		public UndoableMapEdit(PlanetSurface surface) {
			this.surface = surface;
			basemapBefore = new HashMap<>(surface.basemap);
			surfaceBefore = new ArrayList<>(surface.features);
			
			buildingmapBefore = new HashMap<>(surface.buildingmap);
			buildingsBefore = surface.buildings.list();
		}
		/**
		 * Set the after status of the surface.
		 */
		public void setAfter() {
			basemapAfter = new HashMap<>(surface.basemap);
			surfaceAfter = new ArrayList<>(surface.features);
			
			buildingmapAfter = new HashMap<>(surface.buildingmap);
			buildingsAfter = surface.buildings.list();
		}
		/**
		 * Restore the state to before.
		 */
		void restoreBefore() {
			surface.basemap.clear();
			surface.basemap.putAll(basemapBefore);
			surface.buildingmap.clear();
			surface.buildingmap.putAll(buildingmapBefore);
			surface.features.clear();
			surface.features.addAll(surfaceBefore);
			surface.buildings.clear();
			surface.buildings.addAll(buildingsBefore);
		}
		/**
		 * Restore the state after.
		 */
		void restoreAfter() {
			surface.basemap.clear();
			surface.basemap.putAll(basemapAfter);
			surface.buildingmap.clear();
			surface.buildingmap.putAll(buildingmapAfter);
			surface.features.clear();
			surface.features.addAll(surfaceAfter);
			surface.buildings.clear();
			surface.buildings.addAll(buildingsAfter);
		}
		@Override
		public boolean addEdit(UndoableEdit anEdit) {
			return false;
		}
		@Override
		public boolean canRedo() {
			return true;
		}
		@Override
		public boolean canUndo() {
			return true;
		}
		@Override
		public void die() {
			
		}
		@Override
		public String getPresentationName() {
			return "Map editing";
		}
		@Override
		public String getRedoPresentationName() {
			return "Redo map editing";
		}
		@Override
		public String getUndoPresentationName() {
			return "Undo map editing";
		}
		@Override
		public boolean isSignificant() {
			return true;
		}
		@Override
		public void redo() {
			restoreAfter();
			renderer.repaint();
		}
		@Override
		public boolean replaceEdit(UndoableEdit anEdit) {
			return false;
		}
		@Override
		public void undo() {
			restoreBefore();
			renderer.repaint();
		}
	}
	/** Save the current editor state. */
	void saveConfig() {
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("open-ig-mapeditor-config.xml"), 64 * 1024), "UTF-8"))) {
			out.printf("<?xml version='1.0' encoding='UTF-8'?>%n");
			out.printf("<mapeditor-config>%n");
			out.printf("  <window x='%d' y='%d' width='%d' height='%d' state='%d'/>%n", getX(), getY(), getWidth(), getHeight(), getExtendedState());
			out.printf("  <language id='%s'/>%n", ui.languageEn.isSelected() ? "en" : "hu");
			out.printf("  <splitters main='%d' preview='%d' surfaces='%d'/>%n", split.getDividerLocation(), toolSplit.getDividerLocation(), featuresSplit.getDividerLocation());
			out.printf("  <editmode type='%s'/>%n", ui.buildButton.isSelected());
			out.printf("  <tabs selected='%d'/>%n", propertyTab.getSelectedIndex());
			out.printf("  <lights preview='%d' map='%s'/>%n", alphaSlider.getValue(), Float.toString(renderer.alpha));
			out.printf("  <filter surface='%s' building='%s'/>%n", XElement.sanitize(filterSurface.getText()), XElement.sanitize(filterBuilding.getText()));
			out.printf("  <allocation worker='%s' strategy='%d'/>%n", ui.allocationPanel.availableWorkers.getText(), ui.allocationPanel.strategies.getSelectedIndex());
			out.printf("  <view buildings='%s' minimap='%s' textboxes='%s' zoom='%s' standard-fonts='%s' placement-hints='%s'/>%n", ui.viewShowBuildings.isSelected(), 
					ui.viewSymbolicBuildings.isSelected(), ui.viewTextBackgrounds.isSelected(), Double.toString(renderer.scale), ui.viewStandardFonts.isSelected()
					, ui.viewPlacementHints.isSelected());
			out.printf("  <custom-surface-names>%n");
			for (TileEntry te : surfaceTableModel.rows) {
				out.printf("    <tile id='%s' type='%s' name='%s'/>%n", te.id, XElement.sanitize(te.surface), XElement.sanitize(te.name));
			}
			out.printf("  </custom-surface-names>%n");
			out.printf("  <custom-building-names>%n");
			for (TileEntry te : buildingTableModel.rows) {
				out.printf("    <tile id='%s' type='%s' name='%s'/>%n", te.id, XElement.sanitize(te.surface), XElement.sanitize(te.name));
			}
			out.printf("  </custom-building-names>%n");
			out.printf("  <recent>%n");
			for (int i = ui.fileRecent.getItemCount() - 1; i >= 2 ; i--) {
				out.printf("    <entry file='%s'/>%n", XElement.sanitize(ui.fileRecent.getItem(i).getText()));
			}
			out.printf("  </recent>%n");
			out.printf("</mapeditor-config>%n");
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
	}
	/** Load the editor configuration. */
	void loadConfig() {
		File file = new File("open-ig-mapeditor-config.xml");
		if (file.canRead()) {
			try {
				XElement root = XElement.parseXML(file.getAbsolutePath());
				
				// reposition the window
				XElement eWindow = root.childElement("window");
				if (eWindow != null) {
					setBounds(
						Integer.parseInt(eWindow.get("x")),
						Integer.parseInt(eWindow.get("y")),
						Integer.parseInt(eWindow.get("width")),
						Integer.parseInt(eWindow.get("height"))
					);
					setExtendedState(Integer.parseInt(eWindow.get("state")));
				}
				XElement eLanguage = root.childElement("language");
				if (eLanguage != null) {
					String langId = eLanguage.get("id");
					if ("hu".equals(langId)) {
						ui.languageHu.setSelected(true);
						ui.languageHu.doClick();
					} else {
						ui.languageEn.setSelected(true);
						ui.languageEn.doClick();
					}
				}
				
				XElement eSplitters = root.childElement("splitters");
				if (eSplitters != null) {
					split.setDividerLocation(Integer.parseInt(eSplitters.get("main")));
					toolSplit.setDividerLocation(Integer.parseInt(eSplitters.get("preview")));
					featuresSplit.setDividerLocation(Integer.parseInt(eSplitters.get("surfaces")));
				}

				XElement eTabs = root.childElement("tabs");
				if (eTabs != null) {
					propertyTab.setSelectedIndex(Integer.parseInt(eTabs.get("selected")));
				}
				
				XElement eLights = root.childElement("lights");
				if (eLights != null) {
					alphaSlider.setValue(Integer.parseInt(eLights.get("preview")));
					renderer.alpha = Float.parseFloat(eLights.get("map"));
				}
				
				XElement eMode = root.childElement("editmode");
				if (eMode != null) {
					if ("true".equals(eMode.get("type"))) {
						ui.buildButton.doClick();
					}
				}
				
				XElement eView = root.childElement("view");
				if (eView != null) {
					ui.viewShowBuildings.setSelected(false);
					if ("true".equals(eView.get("buildings"))) {
						ui.viewShowBuildings.doClick();
					}
					ui.viewSymbolicBuildings.setSelected(false);
					if ("true".equals(eView.get("minimap"))) {
						ui.viewSymbolicBuildings.doClick();
					}
					
					ui.viewTextBackgrounds.setSelected(false);
					if ("true".equals(eView.get("textboxes"))) {
						ui.viewTextBackgrounds.doClick();
					}
					renderer.scale = Double.parseDouble(eView.get("zoom"));
					
					ui.viewStandardFonts.setSelected("true".equals(eView.get("standard-fonts")));
					ui.viewPlacementHints.setSelected(!"true".equals(eView.get("placement-hints")));
					ui.viewPlacementHints.doClick();
				}
				
				XElement eSurfaces = root.childElement("custom-surface-names");
				if (eSurfaces != null) {
					for (XElement tile : eSurfaces.childrenWithName("tile")) {
						TileEntry te = new TileEntry();
						te.id = Integer.parseInt(tile.get("id"));
						te.surface = tile.get("type");
						te.name = tile.get("name");
						customSurfaceNames.add(te);
					}
				}
				
				XElement eBuildigns = root.childElement("custom-building-names");
				if (eBuildigns != null) {
					for (XElement tile : eBuildigns.childrenWithName("tile")) {
						TileEntry te = new TileEntry();
						te.id = Integer.parseInt(tile.get("id"));
						te.surface = tile.get("type");
						te.name = tile.get("name");
						customBuildingNames.add(te);
					}
				}
				XElement eFilter = root.childElement("filter");
				if (eFilter != null) {
					filterSurface.setText(eFilter.get("surface"));
					filterBuilding.setText(eFilter.get("building"));
				}
				XElement eAlloc = root.childElement("allocation");
				if (eAlloc != null) {
					ui.allocationPanel.availableWorkers.setText(eAlloc.get("worker"));
					ui.allocationPanel.strategies.setSelectedIndex(Integer.parseInt(eAlloc.get("strategy")));
				}
				XElement eRecent = root.childElement("recent");
				if (eRecent != null) {
					for (XElement r : eRecent.childrenWithName("entry")) {
						addRecentEntry(r.get("file")); 
					}
				}
			} catch (XMLStreamException ex) {
				Exceptions.add(ex);
			}
		}
	}
	/** Clear all recent entries. */
	void doClearRecent() {
		ui.fileRecent.removeAll();
		ui.fileRecent.add(ui.clearRecent);
	}
	/**
	 * Open a recent file.
	 * @param fileName the filename
	 */
	void doOpenRecent(String fileName) {
		if (saveSettings == null) {
			saveSettings = new MapSaveSettings();
			saveSettings.buildings = true;
			saveSettings.surface = true;
		}
		saveSettings.fileName = new File(fileName);
		doLoad();
	}
	/**
	 * Add a recent file entry.
	 * @param fileName the filename
	 */
	void addRecentEntry(final String fileName) {
		if (recent.add(fileName)) {
			if (ui.fileRecent.getItemCount() < 2) {
				ui.fileRecent.addSeparator();
			}
			JMenuItem item = new JMenuItem(fileName);
			item.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doOpenRecent(fileName);
				}
			});
			ui.fileRecent.insert(item, 2);
		}
	}
	/**
	 * Remove outbound objects.
	 */
	protected void doCleanup() {
		if (renderer.surface == null) {
			return;
		}
		int buildingCount = 0;
		UndoableMapEdit undo = new UndoableMapEdit(renderer.surface);
		for (Building b : renderer.surface.buildings.list()) {
			inner:
			for (int x = b.location.x; x < b.location.x + b.tileset.normal.width; x++) {
				for (int y = b.location.y; y > b.location.y - b.tileset.normal.height; y--) {
					if (!renderer.surface.cellInMap(x, y)) {
						renderer.surface.buildings.remove(b);
						if (currentBuilding == b) {
							currentBuilding = null;
						}
						buildingCount++;
						removeTiles(renderer.surface.buildingmap, b.location.x, b.location.y, b.tileset.normal.width, b.tileset.normal.height);
						break inner;
					}
				}
			}
		}
		int tileCount = 0;
		for (int i = renderer.surface.features.size() - 1; i >= 0; i--) {
			SurfaceFeature f = renderer.surface.features.get(i);
			inner:
			for (int x = f.location.x; x < f.location.x + f.tile.width; x++) {
				for (int y = f.location.y; y > f.location.y - f.tile.height; y--) {
					if (!renderer.surface.cellInMap(x, y)) {
						renderer.surface.features.remove(i);
//						if (currentBaseTile == f) {
//							currentBaseTile = null;
//						}
						tileCount++;
						removeTiles(renderer.surface.basemap, f.location.x, f.location.y, f.tile.width, f.tile.height);
						break inner;
					}
				}
			}
		}		
		if (renderer.surface.buildings.size() > 0) {
			renderer.surface.placeRoads(renderer.surface.getTechnology(), buildingModel);
		}
		undo.setAfter();
		addUndo(undo);
		repaint();
		JOptionPane.showMessageDialog(this, String.format(labels.format("mapeditor.cleanup_result", buildingCount + tileCount, buildingCount, tileCount)));
	}
	/**
	 * Remove tiles from the given rectangle.
	 * @param map the map
	 * @param x the origin
	 * @param y the origin
	 * @param width the width
	 * @param height the height
	 */
	void removeTiles(Map<Location, SurfaceEntity> map, int x, int y, int width, int height) {
		for (int i = x; i < x + width; i++) {
			for (int j = y; j > y - height; j--) {
				map.remove(Location.of(i, j));
			}
		}
	}
	/**
	 * Resize the current surface.
	 */
	protected void doResize() {
		if (renderer.surface == null) {
			doNew();
			return;
		}
		final NewResizeDialog dlg = new NewResizeDialog(labels, false);
		dlg.widthText.setValue(renderer.surface.width);
		dlg.heightText.setValue(renderer.surface.height);
		int w0 = renderer.surface.width;
		int h0 = renderer.surface.height;
		// live adjustment
		ChangeListener cl = new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				renderer.surface.setSize((Integer)dlg.widthText.getValue(), (Integer)dlg.heightText.getValue());
				ui.mapsize.setText(renderer.surface.width + " x " + renderer.surface.height);
				renderer.repaint();
			}
		};
		dlg.widthText.addChangeListener(cl);
		dlg.heightText.addChangeListener(cl);
		dlg.setLocationRelativeTo(this);
		dlg.setVisible(true);
		if (dlg.success) {
			renderer.surface.setSize((Integer)dlg.widthText.getValue(), (Integer)dlg.heightText.getValue());
			ui.mapsize.setText(renderer.surface.width + " x " + renderer.surface.height);
			renderer.repaint();
		} else {
			renderer.surface.setSize(w0, h0);
			ui.mapsize.setText(renderer.surface.width + " x " + renderer.surface.height);
			renderer.repaint();
		}
	}
	/** Toggle the standard font display mode. */
	protected void doStandardFonts() {
		renderer.txt.setUseStandardFonts(ui.viewStandardFonts.isSelected());
		renderer.repaint();
	}
	/** View placement hints. */
	protected void doViewPlacementHints() {
		renderer.placementHints = ui.viewPlacementHints.isSelected();
		renderer.repaint();
	}
	/** 
	 * Copy contents of the current selection box. 
	 * @param surface paste surface
	 * @param building paste building
	 */
	void doCopy(boolean surface, boolean building) {
		if (renderer.surface == null || renderer.selectedRectangle == null || renderer.selectedRectangle.width == 0) {
			return;
		}
		StringWriter sw = new StringWriter();
		PrintWriter out = new PrintWriter(sw);
		
		out.printf("<?xml version='1.0' encoding='UTF-8'?>%n");
		out.printf("<map x='%d' y='%d' width='%d' height='%d'>%n", renderer.selectedRectangle.x, renderer.selectedRectangle.y, renderer.selectedRectangle.width, renderer.selectedRectangle.height);
		
		Map<Object, Object> memory = new IdentityHashMap<>();
		for (int i = renderer.selectedRectangle.x; i < renderer.selectedRectangle.x + renderer.selectedRectangle.width; i++) {
			for (int j = renderer.selectedRectangle.y; j > renderer.selectedRectangle.y - renderer.selectedRectangle.height; j--) {
				if (surface) {
					for (SurfaceFeature sf : renderer.surface.features) {
						if (sf.containsLocation(i, j)) {
							if (memory.put(sf, sf) == null) {
								out.printf("  <tile x='%d' y='%d' id='%s' type='%s'/>%n", sf.location.x, sf.location.y, sf.id, sf.type);
							}
							break;
						}
					}
				}
				if (building) {
					for (Building b : renderer.surface.buildings.iterable()) {
						if (b.containsLocation(i, j)) {
							if (memory.put(b, b) == null) {
								out.printf("    <building id='%s' tech='%s' x='%d' y='%d' build='%d' hp='%d' level='%d' worker='%d' energy='%d' enabled='%s' repairing='%s' />%n",
										b.type.id, b.race, b.location.x, b.location.y, b.buildProgress, b.hitpoints, b.upgradeLevel, b.assignedWorker, b.assignedEnergy, b.enabled, b.repairing);
							}
							break;
						}
					}
				}
			}
			out.printf("%n");
		}
		
		out.printf("</map>%n");
		out.flush();
		
		StringSelection sel = new StringSelection(sw.toString());
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
	}
	/** 
	 * Perform the cut operation. 
	 * @param surface paste surface
	 * @param building paste building
	 */
	void doCut(boolean surface, boolean building) {
		doCopy(surface, building);
		if (surface && building) {
			doDeleteBoth();
		} else
		if (surface) {
			doDeleteSurface();
		} else
		if (building) {
			doDeleteBuilding();
		}
	}
	/** 
	 * Perform the paste operation. 
	 * @param surface paste surface
	 * @param building paste building
	 */
	void doPaste(boolean surface, boolean building) {
		if (renderer.surface == null || renderer.selectedRectangle == null || renderer.selectedRectangle.width == 0) {
			return;
		}
		String s = getClipboardText();
		if (s != null && s.startsWith("<?xml version='1.0' encoding='UTF-8'?>")) {
			int originX = renderer.selectedRectangle.x;
			int originY = renderer.selectedRectangle.y;
			
			try {
				XElement root = XElement.parseXML(new StringReader(s));
				
				int ox = Integer.parseInt(root.get("x"));
				int oy = Integer.parseInt(root.get("y"));
				
				UndoableMapEdit undo = new UndoableMapEdit(renderer.surface);
				
				String tech = null;
				
				if (surface) {
					for (XElement tile : root.childrenWithName("tile")) {
						String type = tile.get("type");
						int id = Integer.parseInt(tile.get("id"));
						int x = Integer.parseInt(tile.get("x"));
						int y = Integer.parseInt(tile.get("y"));
						
						Tile t = galaxyModel.planetTypes.get(type).tiles.get(id);
						Location l0 = Location.of(x - ox + originX, y - oy + originY);
						deleteEntitiesOf(renderer.surface.basemap, new Rectangle(l0.x, l0.y, t.width, t.height), false);

						renderer.surface.placeBase(t, l0.x, l0.y, id, type);
								
					}
				}
				if (building) {
					for (XElement tile : root.childrenWithName("building")) {
						String id = tile.get("id");
						tech = tile.get("tech");
						
						Building b = new Building(-1, buildingModel.buildings.get(id), tech);
						int x = Integer.parseInt(tile.get("x"));
						int y = Integer.parseInt(tile.get("y"));
					
						b.location = Location.of(x - ox + originX, y - oy + originY);
						
						b.buildProgress = Integer.parseInt(tile.get("build"));
						b.hitpoints = Integer.parseInt(tile.get("hp"));
						b.setLevel(Integer.parseInt(tile.get("level")));
						b.assignedEnergy = Integer.parseInt(tile.get("energy"));
						b.assignedWorker = Integer.parseInt(tile.get("worker"));
						b.enabled = "true".equals(tile.get("enabled"));
						b.repairing = "true".equals(tile.get("repairing"));
					
						
						deleteEntitiesOf(renderer.surface.buildingmap, new Rectangle(b.location.x, b.location.y, b.tileset.normal.width, b.tileset.normal.height), true);

						renderer.surface.placeBuilding(b.tileset.normal, b.location.x, b.location.y, b);
						
					}
				}
				
				if (tech != null) {
					renderer.surface.placeRoads(tech, buildingModel);
				}
				
				undo.setAfter();
				addUndo(undo);
				repaint();
			} catch (XMLStreamException ex) {
				Exceptions.add(ex);
			}
		}
	}
	/**
	 * @return returns the contents of the clipboard as string
	 */
	String getClipboardText() {
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable clipData = clipboard.getContents(clipboard);
		if (clipData != null) {
			try {
				if (clipData.isDataFlavorSupported(DataFlavor.stringFlavor)) {
					return (String)(clipData.getTransferData(DataFlavor.stringFlavor));
				}
			} catch (UnsupportedFlavorException ufe) {
				System.err.println("Flavor unsupported: " + ufe);
			} catch (IOException ioe) {
				System.err.println("Data not available: " + ioe);
			}
		}
		return null;
	}
    /** The row filter based on keywords. */
    private static class RowFilterByWords extends RowFilter<TileList, Integer> {
        /** The words to filter. */
        private final String[] words;
        /**
         * Constructor, initializes the world list.
         * @param words the worlds
         */
        public RowFilterByWords(String[] words) {
            this.words = words;
        }

        @Override
        public boolean include(
            javax.swing.RowFilter.Entry<? extends TileList, ? extends Integer> entry) {
            TileEntry e = entry.getModel().rows.get(entry.getIdentifier());
            String rowtext = (e.name + " " + e.surface + " " + e.tile.width + "x" + e.tile.height).toLowerCase(Locale.ENGLISH);
            for (String s : words) {
                if (!rowtext.contains(s.toLowerCase(Locale.ENGLISH))) {
                    return false;
                }
            }
            return true;
        }
    }
}
