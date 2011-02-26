/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Btn2;
import hu.openig.core.Configuration;
import hu.openig.core.GFXLoader;
import hu.openig.core.Img;
import hu.openig.core.Labels;
import hu.openig.core.ResourceLocator;
import hu.openig.gfx.BackgroundGFX;
import hu.openig.gfx.ColonyGFX;
import hu.openig.gfx.DatabaseGFX;
import hu.openig.gfx.EquipmentGFX;
import hu.openig.gfx.InfoGFX;
import hu.openig.gfx.ResearchGFX;
import hu.openig.gfx.SpacewarGFX;
import hu.openig.gfx.StarmapGFX;
import hu.openig.gfx.StatusbarGFX;
import hu.openig.model.World;
import hu.openig.render.GenericMediumButton;
import hu.openig.render.TextRenderer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.swing.JFrame;



/**
 * Contains all common ang game specific graphical and textual resources.
 * @author akarnokd, 2009.12.25.
 */
public class CommonResources {
	/** The main configuration object. */
	public Configuration config;
	/** The main resource locator object. */
	public ResourceLocator rl;
	/** The global and game specific labels. */
	public Labels labels;
	/** The status bar graphics. */
	public StatusbarGFX statusbar;
	/** The background graphics. */
	public BackgroundGFX background;
	/** The equipment graphics. */
	public EquipmentGFX equipment;
	/** The space war graphics. */
	public SpacewarGFX spacewar;
	/** The info graphics. */
	public InfoGFX info;
	/** The research graphics. */
	public ResearchGFX research;
	/** The colony graphics. */
	public ColonyGFX colony;
	/** The starmap graphics. */
	public StarmapGFX starmap;
	/** The database graphics. */
	public DatabaseGFX database;
	/** The text renderer. */
	public TextRenderer text;
	/** The general control interface. */
	public GameControls control;
	/** The disabled pattern. */
	public BufferedImage disabledPattern;
	/** The normal button renderer. */
	public GenericMediumButton mediumButton;
	/** The pressed button renderer. */
	public GenericMediumButton mediumButtonPressed;
	// --------------------------------------------
	// The general images usable by multiple places
	// --------------------------------------------
	/** The achievement icon. */
	@Img(name = "achievement")
	public BufferedImage achievement;
	/** The achievement icon grayed out. */
	public BufferedImage achievementGrayed;
	/** The empty background of the info panel. */
	@Img(name = "info/info_empty")
	public BufferedImage infoEmpty;
	/** Move up arrow. */
	@Btn2(name = "button_up")
	public BufferedImage[] moveUp;
	/** Move down arrow. */
	@Btn2(name = "button_down")
	public BufferedImage[] moveDown;
	/** Move left arrow. */
	@Btn2(name = "button_left")
	public BufferedImage[] moveLeft;
	/** Move right arrow. */
	@Btn2(name = "button_right")
	public BufferedImage[] moveRight;
	/** Energy icon. */
	@Img(name = "energy-icon")
	public BufferedImage energyIcon;
	/** Food icon. */
	@Img(name = "food-icon")
	public BufferedImage foodIcon;
	/** Worker icon. */
	@Img(name = "worker-icon")
	public BufferedImage workerIcon;
	/** Hospital icon. */
	@Img(name = "hospital-icon")
	public BufferedImage hospitalIcon;
	/** Housing icon. */
	@Img(name = "house-icon")
	public BufferedImage houseIcon;
	// --------------------------------------------
	// The various screen objects
	// --------------------------------------------
	/** The record of screens. */
	public final Screens screens = new Screens();
	/** The record of screens. */
	public final class Screens {
		/** Private constructor. */
		private Screens() {
			
		}
		/** Main menu. */
		public MainMenu mainmenu;
		/** Videos. */
		public VideoScreen videos;
		/** Bridge. */
		public BridgeScreen bridge;
		/** Starmap. */
		public StarmapScreen starmap;
		/** Colony. */
		public PlanetScreen colony;
		/** Equipment. */
		public EquipmentScreen equipment;
		/** Research and production. */
		public ResearchProductionScreen researchProduction;
		/** Information. */
		public InfoScreen info;
		/** Diplomacy. */
		public DiplomacyScreen diplomacy;
		/** Database. */
		public DatabaseScreen database;
		/** Bar. */
		public BarScreen bar;
		/** Statistics and achievements. */
		public AchievementsScreen statisticsAchievements;
		/** Spacewar. */
		public SpacewarScreen spacewar;
		/** Single player. */
		public SingleplayerScreen singleplayer;
		/** Load and save. */
		public LoadSaveScreen loadSave;
		/** Battle finish screen. */
		public BattlefinishScreen battleFinish;
		/** The movie screens. */
		public MovieScreen movie;
		/** The loading in progress screen. */
		public LoadingScreen loading;
		/** The ship walk screen. */
		public ShipwalkScreen shipwalk;
		/** The status bar screen. */
		public StatusbarScreen statusbar;
	}
	/** The game window. */
	public JFrame window;
	/** The game world. */
	public World world;
	/** Flag to indicate the game world is loading. */
	public boolean worldLoading;
	/**
	 * Constructor. Initializes and loads all resources.
	 * @param config the configuration object.
	 * @param control the general control
	 */
	public CommonResources(Configuration config, GameControls control) {
		this.config = config;
		this.control = control;
		init();
	}
	/** Initialize the resources in parallel. */
	private void init() {
		rl = config.newResourceLocator();
		final ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		try {
			try {
				Future<Labels> loadLabels = exec.submit(new Callable<Labels>() {
					@Override
					public Labels call() throws Exception {
						Labels result = new Labels();
						result.load(rl, config.language, null);
						return result;
					}
				});
				Future<StatusbarGFX> loadSB = exec.submit(new Callable<StatusbarGFX>() {
					@Override
					public StatusbarGFX call() throws Exception {
						StatusbarGFX result = new StatusbarGFX(rl);
						result.load(config.language);
						return null;
					}
				});
				Future<BackgroundGFX> loadB = exec.submit(new Callable<BackgroundGFX>() {
					@Override
					public BackgroundGFX call() throws Exception {
						BackgroundGFX result = new BackgroundGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<EquipmentGFX> loadEq = exec.submit(new Callable<EquipmentGFX>() {
					@Override
					public EquipmentGFX call() throws Exception {
						EquipmentGFX result = new EquipmentGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<SpacewarGFX> loadSW = exec.submit(new Callable<SpacewarGFX>() {
					@Override
					public SpacewarGFX call() throws Exception {
						SpacewarGFX result = new SpacewarGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<InfoGFX> loadI = exec.submit(new Callable<InfoGFX>() {
					@Override
					public InfoGFX call() throws Exception {
						InfoGFX result = new InfoGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<ResearchGFX> loadR = exec.submit(new Callable<ResearchGFX>() {
					@Override
					public ResearchGFX call() throws Exception {
						ResearchGFX result = new ResearchGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<ColonyGFX> loadC = exec.submit(new Callable<ColonyGFX>() {
					@Override
					public ColonyGFX call() throws Exception {
						ColonyGFX result = new ColonyGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<StarmapGFX> loadSM = exec.submit(new Callable<StarmapGFX>() {
					@Override
					public StarmapGFX call() throws Exception {
						StarmapGFX result = new StarmapGFX();
						result.load(rl, config.language);
						return result;
					}
				});
				Future<DatabaseGFX> loadDB = exec.submit(new Callable<DatabaseGFX>() {
					@Override
					public DatabaseGFX call() throws Exception {
						DatabaseGFX result = new DatabaseGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<TextRenderer> loadTR = exec.submit(new Callable<TextRenderer>() {
					@Override
					public TextRenderer call() throws Exception {
						return new TextRenderer(rl);
					}
				});

				labels = loadLabels.get();
				statusbar = loadSB.get();
				background = loadB.get();
				equipment = loadEq.get();
				spacewar = loadSW.get();
				info = loadI.get();
				research = loadR.get();
				colony = loadC.get();
				starmap = loadSM.get();
				database = loadDB.get();
				text = loadTR.get();
				createCustomImages();
			} catch (ExecutionException ex) { 
				config.log("ERROR", ex.getMessage(), ex);
			} catch (InterruptedException ex) { 
				config.log("ERROR", ex.getMessage(), ex);
			} 
		} finally {
			exec.shutdown();
		}
	}
	/** Create any custom images. */
	private void createCustomImages() {
		int[] disabled = { 0xFF000000, 0xFF000000, 0, 0, 0xFF000000, 0, 0, 0, 0 };
		disabledPattern = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
		disabledPattern.setRGB(0, 0, 3, 3, disabled, 0, 3);
		GFXLoader.loadResources(this, rl, config.language);
		
		achievementGrayed = new BufferedImage(achievement.getWidth(), achievement.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = achievementGrayed.createGraphics();
		g2.drawImage(achievement, 0, 0, null);
		g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, achievement.getWidth(), achievement.getHeight());
		g2.dispose();
		
		mediumButton = new GenericMediumButton("/hu/openig/gfx/button_medium.png");
		mediumButtonPressed = new GenericMediumButton("/hu/openig/gfx/button_medium_pressed.png");

	}
	/**
	 * Reinitialize the resources by reloading them in the new language.
	 * @param newLanguage the new language
	 */
	public void reinit(String newLanguage) {
		config.language = newLanguage;
		config.save();
		init();
	}
	/**
	 * @return the current language code
	 */
	public String language() {
		return config.language;
	}
	/**
	 * Switch to the screen named.
	 * @param to the screen name
	 */
	public void switchScreen(String to) {
		if ("*bridge".equals(to)) {
			screens.bridge.displayPrimary();
		} else
		if ("*starmap".equals(to)) {
			screens.starmap.displayPrimary();
		} else
		if ("*colony".equals(to)) {
			screens.colony.displayPrimary();
		} else
		if ("*equipment".equals(to)) {
			screens.equipment.displaySecondary();
		} else
		if ("*research".equals(to)) {
			screens.researchProduction.displaySecondary();
		} else
		if ("*production".equals(to)) {
			screens.researchProduction.displaySecondary();
		} else
		if ("*information".equals(to)) {
			screens.info.displaySecondary();
		} else
		if ("*database".equals(to)) {
			screens.database.displayPrimary();
		} else
		if ("*bar".equals(to)) {
			screens.bar.displayPrimary();
		} else
		if ("*diplomacy".equals(to)) {
			screens.diplomacy.displayPrimary();
		}
		
	}
}
