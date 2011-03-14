/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Configuration;
import hu.openig.core.Labels;
import hu.openig.core.ResourceLocator;
import hu.openig.gfx.BackgroundGFX;
import hu.openig.gfx.ColonyGFX;
import hu.openig.gfx.CommonGFX;
import hu.openig.gfx.DatabaseGFX;
import hu.openig.gfx.EquipmentGFX;
import hu.openig.gfx.InfoGFX;
import hu.openig.gfx.ResearchGFX;
import hu.openig.gfx.SpacewarGFX;
import hu.openig.gfx.StarmapGFX;
import hu.openig.gfx.StatusbarGFX;
import hu.openig.model.World;
import hu.openig.render.TextRenderer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;



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
	private Labels labels;
	/** The status bar graphics. */
	private StatusbarGFX statusbar;
	/** The background graphics. */
	private BackgroundGFX background;
	/** The equipment graphics. */
	private EquipmentGFX equipment;
	/** The space war graphics. */
	private SpacewarGFX spacewar;
	/** The info graphics. */
	private InfoGFX info;
	/** The research graphics. */
	private ResearchGFX research;
	/** The colony graphics. */
	private ColonyGFX colony;
	/** The starmap graphics. */
	private StarmapGFX starmap;
	/** The database graphics. */
	private DatabaseGFX database;
	/** The common graphics. */
	private CommonGFX common;
	/** The text renderer. */
	private TextRenderer text;
	/** The general control interface. */
	public GameControls control;
	// --------------------------------------------
	// The various screen objects
	// --------------------------------------------
	/** The record of screens. */
	public final AllScreens screens = new AllScreens();
	/** The record of screens. */
	public final class AllScreens {
		/** Private constructor. */
		private AllScreens() {

		}
		/** Main menu. */
		public MainScreen main;
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
	/** The game world. */
	public World world;
	/** Flag to indicate the game world is loading. */
	public boolean worldLoading;
	/** The common executor service. */
	public final ScheduledExecutorService pool;
	/**
	 * Constructor. Initializes and loads all resources.
	 * @param config the configuration object.
	 * @param control the general control
	 */
	public CommonResources(Configuration config, GameControls control) {
		this.config = config;
		this.control = control;

		ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
		scheduler.setKeepAliveTime(1, TimeUnit.SECONDS);
		scheduler.allowCoreThreadTimeOut(true);

		/* 
		 * the setRemoveOnCancelPolicy() was introduced in Java 7 to
		 * allow the option to remove tasks from work queue if its initial delay hasn't
		 * elapsed -> therfore, if no other tasks are present, the scheduler might go idle earlier
		 * instead of waiting for the initial delay to pass to discover there is nothing to do.
		 * Because the library is currenlty aimed at Java 6, we use a reflection to set this policy
		 * on a Java 7 runtime. 
		 */
		try {
			Method m = scheduler.getClass().getMethod("setRemoveOnCancelPolicy", Boolean.TYPE);
			m.invoke(scheduler, true);
		} catch (InvocationTargetException ex) {

		} catch (NoSuchMethodException e) {
		} catch (SecurityException e) {
		} catch (IllegalAccessException e) {
		} catch (IllegalArgumentException e) {
		}
		pool = scheduler;

		init();
	}
	/** Initialize the resources in parallel. */
	private void init() {
		rl = config.newResourceLocator();
		//		final ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		//		try {
		//			try {
		//				Future<Labels> loadLabels = exec.submit(new Callable<Labels>() {
		//					@Override
		//					public Labels call() throws Exception {
		//						Labels result = new Labels();
		//						result.load(rl, config.language, null);
		//						return result;
		//					}
		//				});
		//				Future<StatusbarGFX> loadSB = exec.submit(new Callable<StatusbarGFX>() {
		//					@Override
		//					public StatusbarGFX call() throws Exception {
		//						StatusbarGFX result = new StatusbarGFX(rl);
		//						result.load(config.language);
		//						return result;
		//					}
		//				});
		//				Future<BackgroundGFX> loadB = exec.submit(new Callable<BackgroundGFX>() {
		//					@Override
		//					public BackgroundGFX call() throws Exception {
		//						BackgroundGFX result = new BackgroundGFX(rl);
		//						result.load(config.language);
		//						return result;
		//					}
		//				});
		//				Future<EquipmentGFX> loadEq = exec.submit(new Callable<EquipmentGFX>() {
		//					@Override
		//					public EquipmentGFX call() throws Exception {
		//						EquipmentGFX result = new EquipmentGFX(rl);
		//						result.load(config.language);
		//						return result;
		//					}
		//				});
		//				Future<SpacewarGFX> loadSW = exec.submit(new Callable<SpacewarGFX>() {
		//					@Override
		//					public SpacewarGFX call() throws Exception {
		//						SpacewarGFX result = new SpacewarGFX(rl);
		//						result.load(config.language);
		//						return result;
		//					}
		//				});
		//				Future<InfoGFX> loadI = exec.submit(new Callable<InfoGFX>() {
		//					@Override
		//					public InfoGFX call() throws Exception {
		//						InfoGFX result = new InfoGFX(rl);
		//						result.load(config.language);
		//						return result;
		//					}
		//				});
		//				Future<ResearchGFX> loadR = exec.submit(new Callable<ResearchGFX>() {
		//					@Override
		//					public ResearchGFX call() throws Exception {
		//						ResearchGFX result = new ResearchGFX(rl);
		//						result.load(config.language);
		//						return result;
		//					}
		//				});
		//				Future<ColonyGFX> loadC = exec.submit(new Callable<ColonyGFX>() {
		//					@Override
		//					public ColonyGFX call() throws Exception {
		//						ColonyGFX result = new ColonyGFX(rl);
		//						result.load(config.language);
		//						return result;
		//					}
		//				});
		//				Future<StarmapGFX> loadSM = exec.submit(new Callable<StarmapGFX>() {
		//					@Override
		//					public StarmapGFX call() throws Exception {
		//						StarmapGFX result = new StarmapGFX();
		//						result.load(rl, config.language);
		//						return result;
		//					}
		//				});
		//				Future<DatabaseGFX> loadDB = exec.submit(new Callable<DatabaseGFX>() {
		//					@Override
		//					public DatabaseGFX call() throws Exception {
		//						DatabaseGFX result = new DatabaseGFX(rl);
		//						result.load(config.language);
		//						return result;
		//					}
		//				});
		//				Future<TextRenderer> loadTR = exec.submit(new Callable<TextRenderer>() {
		//					@Override
		//					public TextRenderer call() throws Exception {
		//						return new TextRenderer(rl);
		//					}
		//				});
		//
		//				labels = loadLabels.get();
		//				statusbar = loadSB.get();
		//				background = loadB.get();
		//				equipment = loadEq.get();
		//				spacewar = loadSW.get();
		//				info = loadI.get();
		//				research = loadR.get();
		//				colony = loadC.get();
		//				starmap = loadSM.get();
		//				database = loadDB.get();
		//				text = loadTR.get();
		//				createCustomImages();
		//			} catch (ExecutionException ex) { 
		//				config.log("ERROR", ex.getMessage(), ex);
		//			} catch (InterruptedException ex) { 
		//				config.log("ERROR", ex.getMessage(), ex);
		//			} 
		//		} finally {
		//			exec.shutdown();
		//		}
		labels = null;
		statusbar = null;
		background = null;
		equipment = null;
		spacewar = null;
		info = null;
		research = null;
		colony = null;
		starmap = null;
		database = null;
		text = null;
		common = null;
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
			control.displayPrimary(Screens.BRIDGE);
		} else
		if ("*starmap".equals(to)) {
			control.displayPrimary(Screens.STARMAP);
		} else
		if ("*colony".equals(to)) {
			control.displayPrimary(Screens.COLONY);
		} else
		if ("*equipment".equals(to)) {
			control.displaySecondary(Screens.EQUIPMENT);
		} else
		if ("*research".equals(to)) {
			control.displaySecondary(Screens.RESEARCH);
		} else
		if ("*production".equals(to)) {
			control.displaySecondary(Screens.PRODUCTION);
		} else
		if ("*information".equals(to)) {
			control.displaySecondary(Screens.INFORMATION);
		} else
		if ("*database".equals(to)) {
			control.displayPrimary(Screens.DATABASE);
		} else
		if ("*bar".equals(to)) {
			control.displayPrimary(Screens.BAR);
		} else
		if ("*diplomacy".equals(to)) {
			control.displayPrimary(Screens.DIPLOMACY);
		}

	}
	/**
	 * Retrieve the result of the future and convert any exception
	 * to runtime exception.
	 * @param <T> the value type
	 * @param future the future for the computation
	 * @return the value
	 */
	public static <T> T get(Future<? extends T> future) {
		try {
			return future.get();
		} catch (ExecutionException ex) {
			throw new RuntimeException(ex);
		} catch (InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}
	/** @return lazily initialize the labels or return the existing one. */
	public Labels labels() {
		if (labels == null) {
			System.out.println("Loading labels");
			labels = get(pool.submit(new Callable<Labels>() {
				@Override
				public Labels call() {
					Labels result = new Labels();
					result.load(rl, config.language, null);
					return result;
				}
			}));
		}
		return labels;
	}
	/** @return lazily initialize the status bar or return the existing one. */
	public StatusbarGFX statusbar() {
		if (statusbar == null) {
			System.out.println("Loading statusbar");
			statusbar = get(
					pool.submit(new Callable<StatusbarGFX>() {
						@Override
						public StatusbarGFX call() throws Exception {
							StatusbarGFX result = new StatusbarGFX(rl);
							result.load(config.language);
							return result;
						}
					})					
			);
		}
		return statusbar;
	}
	/** @return lazily initialize the background or return the existing one. */
	public BackgroundGFX background() {
		if (background == null) {
			System.out.println("Loading background");
			background = get(
					pool.submit(new Callable<BackgroundGFX>() {
						@Override
						public BackgroundGFX call() throws Exception {
							BackgroundGFX result = new BackgroundGFX(rl);
							result.load(config.language);
							return result;
						}
					})					
			);
		}
		return background;
	}
	/** @return lazily initialize the equipment or return the existing one. */
	public EquipmentGFX equipment() {
		if (equipment == null) {
			System.out.println("Loading equipment");
			equipment = get(
					pool.submit(new Callable<EquipmentGFX>() {
						@Override
						public EquipmentGFX call() throws Exception {
							EquipmentGFX result = new EquipmentGFX(rl);
							result.load(config.language);
							return result;
						}
					})		
			);
		}
		return equipment;
	}
	/** @return lazily initialize the spacewar or return the existing one. */
	public SpacewarGFX spacewar() {
		if (spacewar == null) {
			System.out.println("Loading spacewar");
			spacewar = get(
					pool.submit(new Callable<SpacewarGFX>() {
						@Override
						public SpacewarGFX call() throws Exception {
							SpacewarGFX result = new SpacewarGFX(rl);
							result.load(config.language);
							return result;
						}
					})		
			);
		}
		return spacewar;
	}
	/** @return lazily initialize the info or return the existing one. */
	public InfoGFX info() {
		if (info == null) {
			System.out.println("Loading info");
			info = get(
					pool.submit(new Callable<InfoGFX>() {
						@Override
						public InfoGFX call() throws Exception {
							InfoGFX result = new InfoGFX(rl);
							result.load(config.language);
							return result;
						}
					})
			);
		}
		return info;
	}
	/** @return lazily initialize the research or return the existing one. */
	public ResearchGFX research() {
		if (research == null) {
			System.out.println("Loading research");
			research = get(pool.submit(new Callable<ResearchGFX>() {
				@Override
				public ResearchGFX call() throws Exception {
					ResearchGFX result = new ResearchGFX(rl);
					result.load(config.language);
					return result;
				}
			})
			);
		}
		return research;
	}
	/** @return lazily initialize the colony or return the existing one. */
	public ColonyGFX colony() {
		if (colony == null) {
			System.out.println("Loading colony");
			colony = get(
					pool.submit(new Callable<ColonyGFX>() {
						@Override
						public ColonyGFX call() throws Exception {
							ColonyGFX result = new ColonyGFX(rl);
							result.load(config.language);
							return result;
						}
					})	
			);
		}
		return colony;
	}
	/** @return lazily initialize the starmap or return the existing one. */
	public StarmapGFX starmap() {
		if (starmap == null) {
			System.out.println("Loading starmap");
			starmap = get(
					pool.submit(new Callable<StarmapGFX>() {
						@Override
						public StarmapGFX call() throws Exception {
							StarmapGFX result = new StarmapGFX();
							result.load(rl, config.language);
							return result;
						}
					})	
			);
		}
		return starmap;
	}
	/** @return lazily initialize the database or return the existing one. */
	public DatabaseGFX database() {
		if (database == null) {
			System.out.println("Loading database");
			database = get(
					pool.submit(new Callable<DatabaseGFX>() {
						@Override
						public DatabaseGFX call() throws Exception {
							DatabaseGFX result = new DatabaseGFX(rl);
							result.load(config.language);
							return result;
						}
					})	
			);
		}
		return database;
	}
	/** @return lazily initialize the text or return the existing one. */
	public TextRenderer text() {
		if (text == null) {
			System.out.println("Loading text");
			text = get(pool.submit(new Callable<TextRenderer>() {
				@Override
				public TextRenderer call() throws Exception {
					return new TextRenderer(rl);
				}
			}));
		}
		return text;
	}
	/** @return lazily initialize the text or return the existing one. */
	public CommonGFX common() {
		if (common == null) {
			System.out.println("Loading common");
			common = get(pool.submit(new Callable<CommonGFX>() {
				@Override
				public CommonGFX call() throws Exception {
					return new CommonGFX().load(rl, config.language);
				}
			}));
		}
		return common;
	}

}
