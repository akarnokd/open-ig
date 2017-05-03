/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.core.Func0;
import hu.openig.core.Func1;
import hu.openig.core.ResourceType;
import hu.openig.core.SaveMode;
import hu.openig.core.SimulationSpeed;
import hu.openig.core.CursorResource;
import hu.openig.gfx.BackgroundGFX;
import hu.openig.gfx.ColonyGFX;
import hu.openig.gfx.CommonGFX;
import hu.openig.gfx.DatabaseGFX;
import hu.openig.gfx.DiplomacyGFX;
import hu.openig.gfx.EquipmentGFX;
import hu.openig.gfx.InfoGFX;
import hu.openig.gfx.ResearchGFX;
import hu.openig.gfx.SpacewarGFX;
import hu.openig.gfx.StarmapGFX;
import hu.openig.gfx.StatusbarGFX;
import hu.openig.mechanics.Allocator;
import hu.openig.mechanics.Radar;
import hu.openig.mechanics.Simulator;
import hu.openig.model.AIManager;
import hu.openig.model.Configuration;
import hu.openig.model.GameDefinition;
import hu.openig.model.GameEnvironment;
import hu.openig.model.Labels;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.Player;
import hu.openig.model.Profile;
import hu.openig.model.ResearchType;
import hu.openig.model.ResourceLocator;
import hu.openig.model.ResourceLocator.ResourcePlace;
import hu.openig.model.Screens;
import hu.openig.model.SkirmishAIMode;
import hu.openig.model.SkirmishDefinition;
import hu.openig.model.SkirmishPlayer;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.model.Traits;
import hu.openig.model.World;
import hu.openig.music.Music;
import hu.openig.render.TextRenderer;
import hu.openig.screen.api.EquipmentScreenAPI;
import hu.openig.screen.api.ResearchProductionAnimation;
import hu.openig.sound.Sounds;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;
import hu.openig.utils.WipPort;
import hu.openig.utils.XElement;

import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.SwingUtilities;
import javax.swing.Timer;



/**
 * Contains all common ang game specific graphical and textual resources.
 * @author akarnokd, 2009.12.25.
 */
public class CommonResources implements GameEnvironment {
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
	/** The diplomacy graphics. */
	private DiplomacyGFX diplomacy;
	/** The text renderer. */
	private TextRenderer text;
	/** The general control interface. */
	private GameControls control;
	/** The current player's profile. */
	public Profile profile;
	/**
	 * The queue for notifying the user about achievements.
	 */
	public final Deque<String> achievementNotifier = new LinkedList<>();
	// --------------------------------------------
	// The various screen objects
	// --------------------------------------------
	/** The game world. */
	private World world;
	/** Flag to indicate the game world is loading. */
	public volatile boolean worldLoading;
	/** The game is in battle mode. */
	public boolean battleMode;
	/** Flag indicating the statusbar screen to show a non-game statusbar. */
	public boolean nongame;
	/** The common executor service. */
	public final ScheduledExecutorService pool;
	/** The combined timer for synchronized frequency updates. */
	public Timer timer;
//	/** The periodic timer. */
//	public Future<?> timerFuture;
	/** The timer delay in milliseconds. */
	public static final int TIMER_DELAY = 25;
	/** The timer tick. */
	long tick;
	/** The registration map. */
	final Map<Closeable, TimerAction> timerHandlers = new ConcurrentHashMap<>();
	/** Caches the created cursors. */
	final Map<hu.openig.model.Cursors, java.awt.Cursor> cursorCache = new HashMap<>();
	/** The timer action. */
	static class TimerAction {
		/** The operation frequency. */
		public int delay;
		/** The action to invoke. */
		public final Action0 action;
		/** The flag to indicate the action was cancelled. */
		public boolean cancelled;
        /**
         * Constructor, initializes the action.
         * @param action the action to call after the delay
         */
        public TimerAction(Action0 action) {
            this.action = Objects.requireNonNull(action);
        }
	}
	/** The radar handler. */
	protected Closeable radarHandler;
	/** The allocator handler. */
	protected Closeable allocatorHandler;
	/** The sound objects.*/
	public Sounds sounds;
	/** The music player. */
	public Music music;
	/** The current simulation controls. */
	public SimulationTimer simulation;
	/** Map of currently running AIs. */
	public final Map<Player, Future<?>> runningAI = new HashMap<>();
	/** Indicate if an asynchronous save is in operation. */
	public final WipPort saving = new WipPort();
	/** Disable controls and force watching the video. */
	public boolean force;
	/** Counts the current simulation step and invokes the simulator on every 4th. */
	protected long simulationStep;
	/** The global traits. */
	public final Traits traits = new Traits();
	/** The pool thread factory. */
	static final class BasicThreadFactory implements ThreadFactory {
		/** Thread number counter. */
		final AtomicInteger count = new AtomicInteger();
		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r, "CPU-Pool-" + count.incrementAndGet());
			t.setDaemon(true);
			return t;
		}
	}
	/**
	 * Constructor. Initializes and loads all resources.
	 * @param config the configuration object.
	 * @param control the general control
	 */
	public CommonResources(Configuration config, GameControls control) {
		this.config = config;
		this.control = control;
		this.profile = new Profile();
		this.profile.name = config.currentProfile;
		
		int ncpu = Runtime.getRuntime().availableProcessors();
		ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(ncpu, new BasicThreadFactory());
//		scheduler.setKeepAliveTime(1500, TimeUnit.MILLISECONDS);
//		scheduler.allowCoreThreadTimeOut(true);
		scheduler.setRemoveOnCancelPolicy(true);
		pool = scheduler;

		init();
	}
	/**
	 * Initiate the timer task.
	 */
	public void startTimer() {
		stopTimer();
		timer = new Timer(TIMER_DELAY, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tick++;
				doTimerTick();
			}
		});
		timer.start();
	}
	/**
	 * Stop the timer task.
	 */
	public void stopTimer() {
		if (timer != null) {
			timer.stop();
		}
	}
	/** Initialize the resources in parallel. */
	private void init() {
		final ResourceLocator rl = config.newResourceLocator();
		final ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		try {
			final Future<Labels> labelFuture = exec.submit(new Callable<Labels>() {
				@Override
				public Labels call() throws Exception {
					return new Labels().load(rl, Collections.singleton("labels"));
				}
			});
			final Future<StatusbarGFX> statusbarFuture = exec.submit(new Callable<StatusbarGFX>() {
				@Override
				public StatusbarGFX call() throws Exception {
					return new StatusbarGFX().load(rl);
				}
			});
			final Future<BackgroundGFX> backgroundFuture = exec.submit(new Callable<BackgroundGFX>() {
				@Override
				public BackgroundGFX call() throws Exception {
					return new BackgroundGFX().load(rl);
				}
			});
			final Future<EquipmentGFX> equipmentFuture = exec.submit(new Callable<EquipmentGFX>() {
				@Override
				public EquipmentGFX call() throws Exception {
					return new EquipmentGFX().load(rl);
				}
			});
			final Future<SpacewarGFX> spacewarFuture = exec.submit(new Callable<SpacewarGFX>() {
				@Override
				public SpacewarGFX call() throws Exception {
					return new SpacewarGFX().load(rl);
				}
			});
			final Future<InfoGFX> infoFuture = exec.submit(new Callable<InfoGFX>() {
				@Override
				public InfoGFX call() throws Exception {
					return new InfoGFX().load(rl);
				}
			});
			final Future<ResearchGFX> researchFuture = exec.submit(new Callable<ResearchGFX>() {
				@Override
				public ResearchGFX call() throws Exception {
					return new ResearchGFX().load(rl);
				}
			});
			final Future<ColonyGFX> colonyFuture = exec.submit(new Callable<ColonyGFX>() {
				@Override
				public ColonyGFX call() throws Exception {
					return new ColonyGFX().load(rl);
				}
			});
			final Future<StarmapGFX> starmapFuture = exec.submit(new Callable<StarmapGFX>() {
				@Override
				public StarmapGFX call() throws Exception {
					return new StarmapGFX().load(rl);
				}
			});
			final Future<DatabaseGFX> databaseFuture = exec.submit(new Callable<DatabaseGFX>() {
				@Override
				public DatabaseGFX call() throws Exception {
					return new DatabaseGFX().load(rl);
				}
			});
			final Future<TextRenderer> textFuture = exec.submit(new Callable<TextRenderer>() {
				@Override
				public TextRenderer call() throws Exception {
					return new TextRenderer(rl, config.useStandardFonts, config.textCacheSize);
				}
			});
			final Future<DiplomacyGFX> diplomacyFuture = exec.submit(new Callable<DiplomacyGFX>() {
				@Override
				public DiplomacyGFX call() throws Exception {
					return new DiplomacyGFX().load(rl);
				}
			});
			final Future<CommonGFX> commonFuture = pool.submit(new Callable<CommonGFX>() {
				@Override
				public CommonGFX call() throws Exception {
					return new CommonGFX().load(rl);
				}
			});
			labels = get(labelFuture);
			statusbar = get(statusbarFuture);
			background = get(backgroundFuture);
			equipment = get(equipmentFuture);
			spacewar = get(spacewarFuture);
			info = get(infoFuture);
			research = get(researchFuture);
			colony = get(colonyFuture);
			starmap = get(starmapFuture);
			database = get(databaseFuture);
			text = get(textFuture);
			diplomacy = get(diplomacyFuture);
			common = get(commonFuture);

			sounds = new Sounds(rl);
			sounds.initialize(config.audioChannels, new Func0<Integer>() {
				@Override
				public Integer invoke() {
					return config.muteEffect ? 0 : config.effectVolume;
				}
			});
			
			music = new Music(rl);
			music.setVolume(config.musicVolume);
			music.setMute(config.muteMusic);

			traits.load(rl.getXML("traits"));
			
			text.setFontScaling(config.uiScale / 100d);
			
			// FIXME during translation
			
			this.rl = rl;
//			labelReloader = new Thread() {
//				@Override
//				public void run() {
//					watchLabels();
//				}
//			};
//			labelReloader.setDaemon(true);
//			labelReloader.start();
			
			setupCursors();
		} finally {
			exec.shutdown();
		}
		startTimer();
	}
	/** Setup the custom cursors. */
	void setupCursors() {
		for (Field f : common.getClass().getFields()) {
			CursorResource res = f.getAnnotation(CursorResource.class);
			if (res != null) {
				try {
					cursorCache.put(res.key(),
						Toolkit.getDefaultToolkit().createCustomCursor(
							(BufferedImage)f.get(common), new Point(res.x(), res.y()), res.key().name())
					);
				} catch (IllegalAccessException e) {
					Exceptions.add(e);
				}
			}
		}
	}
	/**
	 * Reinitialize the resources by reloading them in the new language.
	 * @param newLanguage the new language
	 */
	public void reinit(String newLanguage) {
		config.language = newLanguage;
		config.save();
		sounds.close();
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
		switch (to) {
		case "*bridge":
			control.displayPrimary(Screens.BRIDGE);
			break;
		case "*starmap":
			control.displayPrimary(Screens.STARMAP);
			break;
		case "*colony":
			control.displayPrimary(Screens.COLONY);
			break;
		case "*equipment":
			control.displaySecondary(Screens.EQUIPMENT);
			break;
		case "*research":
			control.displaySecondary(Screens.RESEARCH);
			break;
		case "*production":
			control.displaySecondary(Screens.PRODUCTION);
			break;
		case "*information":
			control.displaySecondary(Screens.INFORMATION_PLANETS);
			break;
		case "*database":
			control.displaySecondary(Screens.DATABASE);
			break;
		case "*bar":
			control.displaySecondary(Screens.BAR);
			break;
		case "*diplomacy":
			control.displaySecondary(Screens.DIPLOMACY);
			break;
		default:
			throw new IllegalArgumentException(to);
		}

	}
	/**
	 * Set custom mouse cursor.
	 * @param cursor the cursor name
	 */
	public void setCursor(hu.openig.model.Cursors cursor) {
		if (config.customCursors) {
			control.renderingComponent().setCursor(cursorCache.get(cursor));
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
		} catch (ExecutionException | InterruptedException ex) {
			throw new RuntimeException(ex);
		}
	}
	/** @return lazily initialize the labels or return the existing one. */
	@Override
	public Labels labels() {
		return labels;
	}
	/** @return lazily initialize the status bar or return the existing one. */
	public StatusbarGFX statusbar() {
		return statusbar;
	}
	/** @return lazily initialize the background or return the existing one. */
	public BackgroundGFX background() {
		return background;
	}
	/** @return lazily initialize the equipment or return the existing one. */
	public EquipmentGFX equipment() {
		return equipment;
	}
	/** @return lazily initialize the spacewar or return the existing one. */
	public SpacewarGFX spacewar() {
		return spacewar;
	}
	/** @return lazily initialize the info or return the existing one. */
	public InfoGFX info() {
		return info;
	}
	/** @return lazily initialize the research or return the existing one. */
	public ResearchGFX research() {
		return research;
	}
	/** @return lazily initialize the colony or return the existing one. */
	public ColonyGFX colony() {
		return colony;
	}
	/** @return lazily initialize the starmap or return the existing one. */
	public StarmapGFX starmap() {
		return starmap;
	}
	/** @return lazily initialize the database or return the existing one. */
	public DatabaseGFX database() {
		return database;
	}
	/** @return lazily initialize the text or return the existing one. */
	public TextRenderer text() {
		return text;
	}
	/** @return lazily initialize the common graphics or return the existing one. */
	public CommonGFX common() {
		return common;
	}
	/** @return lazily initialize the diplomacy graphics or return the existing one. */
	public DiplomacyGFX diplomacy() {
		return diplomacy;
	}
	/**
	 * Convenience method to return a video for the current language.
	 * @param name the video name.
	 * @return the resource place for the video
	 */
	public ResourcePlace video(String name) {
		return rl.get(name, ResourceType.VIDEO);
	}
	/**
	 * Convenience method to return a video for the current language.
	 * @param name the video name.
	 * @param fallback fall back to english if not found?
	 * @return the resource place for the video
	 */
	public ResourcePlace video(String name, boolean fallback) {
		ResourcePlace rp = rl.get(name, ResourceType.VIDEO);
		if (rp == null) {
			rp = rl.getExactly("en", name, ResourceType.VIDEO);
		}
		return rp;
	}
	/**
	 * Convenience method to return an audio for the current language.
	 * @param name the video name.
	 * @return the resource place for the video
	 */
	public ResourcePlace audio(String name) {
		return rl.get(name, ResourceType.AUDIO);
	}
	/** Close the resources. */
	public void stop() {
//		timer.stop();
		stopTimer();
		
		close0(allocatorHandler);
		close0(radarHandler);
		close0(simulation);


		for (Future<?> sw : runningAI.values()) {
			sw.cancel(true);
		}
		runningAI.clear();
				
		allocatorHandler = null;
		radarHandler = null;
		simulation = null;
		
		stopMusic();
	}
	/**
	 * Close the given closeable silently.
	 * @param c the closeable
	 */
	void close0(Closeable c) {
		try {
			if (c != null) {
				c.close();
			}
		} catch (IOException ex) {
			// Ignored
		}
	}
	/** Restore the main simulation speed function. Call this function after the battle completes. */
	public void restoreMainSimulationSpeedFunction() {
		replaceSimulation(
				new Action0() {
					@Override
					public void invoke() {
						simulation();
					}
				},
				new Func1<SimulationSpeed, Integer>() {
					@Override
					public Integer invoke(SimulationSpeed value) {
						return simulationMilliseconds(value);
					}
				}
		);
	}
	/**
	 * Returns the milliseconds between simulation steps.
	 * @param value the simulation speed indicator
	 * @return the step size in milliseconds
	 */
	public int simulationMilliseconds(SimulationSpeed value) {
		switch (value) {
		case NORMAL: return roundMillis(1000 / world.params().simulationRatio());
		case FAST: return roundMillis(500 / world.params().simulationRatio());
		case ULTRA_FAST: return roundMillis(250 / world.params().simulationRatio());
		default:
			throw new AssertionError("" + value);
		}
	}
	/**
	 * Rounds a millisecond value upwards to the next 25-multiple value.
	 * @param n the original value
	 * @return the rounded value.
	 */
	int roundMillis(int n) {
		if (n < TIMER_DELAY) {
			return TIMER_DELAY;
		} else
		if (n % TIMER_DELAY == 0) {
			return n;
		}
		return (int)(Math.round(n / (double)TIMER_DELAY) * TIMER_DELAY); 
		
	}
	/**
	 * Invoke the AI for the player if not already running.
	 * @param p the player
	 * @param wip the work in progress port
	 * @return AI was executed?
	 */
	boolean prepareAI(final Player p, final WipPort wip) {

		Future<?> sw = runningAI.get(p);
		// if not present or finished, start a new
		if (sw == null) {
			wip.inc();
			Runnable run = new Runnable() {
				@Override
				public void run() {
					prepareAIAsync(p, wip);
				}
			};
			runningAI.put(p, pool.submit(run));
			return true;
		}
		return false;
	}
	/**
	 * Invoke the actual AI management code.
	 * @param p the player
	 */
	void invokeAI(final Player p) {
		Runnable run = new Runnable() {
			@Override
			public void run() {
				runAIAsync(p);
			}
		};
		runningAI.put(p, pool.submit(run));
	}
	/**
	 * Prepare the AI state in parallel.
	 * @param p the player
	 * @param wip the completion port
	 */
	void prepareAIAsync(final Player p, final WipPort wip) {
		try {
			try {
				// parallel convert world state
				p.ai.prepare();
			} finally {
				// wait for all to read world state
				wip.dec();
			}
		} catch (Throwable t) {
			Exceptions.add(t);
		}
	}
	/**
	 * Run the AI body function.
	 * @param p the player
	 */
	void runAIAsync(final Player p) {
		try {
			// act on the world state
			p.ai.manage();
			// issue commands
			completeAIAsync(p);
		} catch (Throwable t) {
			Exceptions.add(t);
		}
	}
	/**
	 * Complete the AI activities on the EDT.
	 * @param p the player
	 */
	void completeAIAsync(final Player p) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					if (!battleMode) {
						p.ai.apply();
					}
				} catch (Throwable t) {
					Exceptions.add(t);
				} finally {
					runningAI.remove(p);
				}
			}
		});
	}
	/**
	 * Execute a step of simulation.
	 */
	public void simulation() {
		boolean repaint = false;
		if (simulationStep++ % world.params().simulationRatio() == 0) {
			if (Simulator.compute(world)) {
				if (world.scripting.mayAutoSave()) {
					control.save(null, SaveMode.AUTO);
				}
			}
			// run AI routines in background
			final WipPort wip = new WipPort(1);
			
			List<Player> ais = new ArrayList<>();
			for (final Player p : world.players.values()) {
				if (p.ai != null) {
					if (prepareAI(p, wip)) {
						ais.add(p);
					}
				}
			}
			wip.dec();
			try {
				wip.await();
			} catch (InterruptedException ex) {
				// ignored
			}
			for (final Player p : ais) {
				invokeAI(p);
			}
			repaint = true;
		}
		// let the screen's natural frequency update the on-screen location
		/* repaint |= */Simulator.moveFleets(world);
		
		if (!world.pendingBattles.isEmpty()) {
			world.env.startBattle();
		}
		if (repaint) {
			control.repaintInner();
		}
	}
	/**
	 * Replace the current simulation controls with a new
	 * simulation controls.
	 * @param action the new simulation action
	 * @param delay the function to tell the delay value from speed enumeration.
	 */
	public void replaceSimulation(Action0 action, Func1<SimulationSpeed, Integer> delay) {
		close0(simulation);
		simulation = newSimulationTimer(action, delay);
	}
	/** 
	 * Start the timed actions.
	 * @param withMusic set if play music 
	 */
	public void start(boolean withMusic) {
		restoreMainSimulationSpeedFunction();

		radarHandler = register(1000, new Action0() {
			@Override
			public void invoke() {
				world.statistics.playTime.value++;
				if (!simulation.paused()) {
					world.statistics.simulationTime.value++;
				}
				Radar.compute(world);
				if (control.primary() == Screens.STARMAP) {
					control.repaintInner();
				}
			}
		});
		allocatorHandler = register(1000, new Action0() {
			@Override
			public void invoke() {
				Allocator.compute(world, pool);
				control.repaintInner();
			}
		});
		Allocator.compute(world, pool);
		Radar.compute(world);
		
		simulation.resume();
		
		startTimer();
//		timer.start();
		if (withMusic) {
			playRegularMusic();
		}
		
		battleMode = false;
	}
	/** @return the world instance. */
	@Override
	public World world() {
		return world;
	}
	/**
	 * Set the world.
	 * @param w the new world
	 * @return this
	 */
	public CommonResources world(World w) {
		if (this.world != null && w == null) {
			world.scripting.done();
		}
		this.world = w;
		return this;
	}
	/**
	 * Set the game control peer.
	 * @param ctrl the new game control peer
	 * @return this
	 */
	public CommonResources control(GameControls ctrl) {
		this.control = ctrl;
		return this;
	}
	/** @return the control object */
	public GameControls control() {
		return control;
	}
	/** Execute the timer tick actions. */
	void doTimerTick() {
		for (TimerAction act : new ArrayList<>(timerHandlers.values())) {
			if (!act.cancelled) {
				if ((tick * TIMER_DELAY) % act.delay == 0) {
					try {
						act.action.invoke();
					} catch (CancellationException ex) {
						act.cancelled = true;
					} catch (Throwable t) {
						Exceptions.add(t);
					}
				}
			}
		}
	}
	/**
	 * Register a repeating action with the given delay.
	 * @param delay the requested frequency in milliseconds
	 * @param action the action to invoke
	 * @return the handler to close this instance
	 */
	public Closeable register(int delay, Action0 action) {
		if (delay % TIMER_DELAY != 0 || delay == 0) {
			throw new IllegalArgumentException("The delay must be in multiples of " + TIMER_DELAY + " milliseconds!");
		}
		if (action == null) {
			throw new IllegalArgumentException("action is null");
		}
		final TimerAction ta = new TimerAction(action);
		ta.delay = delay;
		Closeable res = new Closeable() {
			@Override
			public void close() throws IOException {
				ta.cancelled = true;
				timerHandlers.remove(this);
			}
		};
		timerHandlers.put(res, ta);
		return res;
	}
	/**
	 * Convenience method to start playing the original three musics.
	 */
	public void playRegularMusic() {
		stopMusic();
		
		List<String> musicList = new ArrayList<>();
		int maxLen = 0;
		for (ResourcePlace rp : rl.list(language(), "music/Music")) {
			musicList.add(rp.getName());
			maxLen = Math.max(rp.getName().length(), maxLen);
		} 
		
		final int fMaxLen = maxLen;
		Collections.sort(musicList, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				String s1 = U.padLeft(o1.substring(11), fMaxLen, ' ');
				String s2 = U.padLeft(o2.substring(11), fMaxLen, ' ');
				return s1.compareTo(s2);
			}
		});
		
		String[] musics = musicList.toArray(new String[musicList.size()]);
		music.playLooped(musics);
	}
	/** Convenience method to start playing the original battle music. */
	public void playBattleMusic() {
		stopMusic();
		music.playLooped("music/War");
	}
	@Override
	public void stopMusic() {
		music.close();
	}
	/**
	 * Create a new simulation timer controls with the given action.
	 * @param action the simulation action
		 * @param delay the function which tells the delay from the speed enumeration
	 * @return the new simulation timer
	 */
	public SimulationTimer newSimulationTimer(Action0 action, Func1<SimulationSpeed, Integer> delay) {
		return new SimulationTimer(action, delay);
	}
	/**
	 * The class to manage simulation timer related commands (pause, resume, current).
	 * @author akarnokd, 2011.09.01.
	 */
	public class SimulationTimer implements Closeable {
		/** The handler for the timer. */
		protected Closeable handler;
		/** The current speed value. */
		protected SimulationSpeed speed = SimulationSpeed.NORMAL;
		/** Is the simulation paused? */
		protected boolean paused = true;
		/** The timer action. */
		protected final Action0 action;
		/** The delay computation function. */
		protected final Func1<SimulationSpeed, Integer> delay;
		/**
		 * Constructor.
		 * @param action the timer action.
		 * @param delay the function which tells the delay from the speed enumeration
		 */
		public SimulationTimer(Action0 action, Func1<SimulationSpeed, Integer> delay) {
			this.action = action;
			this.delay = delay;
		}
		/**
		 * Pauses the simulation if not already paused.
		 */
		public void pause() {
			if (!paused) {
				paused = true;
				close();
			}
		}
		/** Resumes the simulation if not already running. */
		public void resume() {
			if (paused) {
				speed(speed);
			}
		}
		/** Register the action with the timer and delay. */
		void registerAction() {
			close();
			handler = register(delay.invoke(speed), action);
		}
		/**
		 * Sets a new simulation speed and resumes the simulation.
		 * @param newSpeed the new speed
		 */
		public void speed(SimulationSpeed newSpeed) {
			if (newSpeed != speed || paused) {
				speed = newSpeed;
				paused = false;
				registerAction();
			}
		}
		/** @return the current simulation speed. */
		public SimulationSpeed speed() {
			return speed;
		}
		/** @return true if the simulation is paused. */
		public boolean paused() {
			return paused;
		}
		@Override
		public void close() {
			close0(handler);
			handler = null;
		}
		/**
		 * Returns the simulation speed in milliseconds.
		 * @return the simulation speed in milliseconds.
		 */
		public int speedValue() {
			return delay.invoke(speed);
		}
	}
	@Override
	public AIManager getAI(Player player) {
		return control.aiFactory().invoke(player);
	}
	@Override
	public void startBattle() {
		control.startBattle();
	}
	@Override
	public void playAudio(String name, final Action0 action) {
		Music m = new Music(rl);
		m.onComplete = new Action1<String>() {
			@Override
			public void invoke(String value) {
				if (world != null) {
					world.scripting.onSoundComplete(value);
				}
				if (action != null) {
					action.invoke();
				}
			}
		};
		m.setVolume(config.effectVolume);
		m.setMute(config.muteEffect);
		m.playSequence(name);
	}
	@Override
	public Action0 playSound(SoundTarget target, SoundType type, Action0 action) {
		switch (target) {
		case COMPUTER:
			if (config.computerVoiceNotify) {
				return sounds.playSound(type, action);
			}
			if (action != null) {
				action.invoke();
			}
			break;
		case BUTTON:
			if (config.buttonSounds) {
				return sounds.playSound(type, action);
			}
			if (action != null) {
				action.invoke();
			}
			break;
		case EFFECT:
			return sounds.playSound(type, action);
		case SCREEN:
			if (config.computerVoiceScreen) {
				return sounds.playSound(type, action);
			}
			if (action != null) {
				action.invoke();
			}
			break;
		default:
			if (action != null) {
				action.invoke();
			}
		}
		return null;
	}
	@Override
	public void playVideo(final String name, final Action0 action) {
		final boolean running = simulation.paused();
		simulation.pause();
		control.playVideos(new Action0() {
			@Override
			public void invoke() {
				if (world != null) {
					world.scripting.onVideoComplete(name);
				}
				if (!running) {
					simulation.resume();
				}
				if (action != null) {
					action.invoke();
				}
			}
		}, name);
	}
	@Override
	public Configuration config() {
		return config;
	}
	@Override
	public Deque<String> achievementQueue() {
		return achievementNotifier;
	}
	@Override
	public Profile profile() {
		return profile;
	}
	@Override
	public void forceMessage(String messageId, Action0 onSeen) {
		control().forceMessage(messageId, onSeen);
	}
	@Override
	public void loseGame() {
		control().loseGame();
	}
	@Override
	public void winGame() {
		control().winGame();
	}
	@Override
	public void showObjectives(boolean state) {
		control().showObjectives(state);
	}
	@Override
	public int simulationSpeed() {
		return simulation.speedValue();
	}
	@Override
	public void pause() {
		simulation.pause();
	}
	@Override
	public void speed1() {
		if (!simulation.paused()) {
			simulation.speed(SimulationSpeed.NORMAL);
		}
	}
	@Override
	public void playMusic() {
		playRegularMusic();
	}
	@Override
	public boolean isBattle() {
		return battleMode;
	}
	/**
	 * Execute a function on the EDT and return its value.
	 * @param <T> the value type
	 * @param func the function to call
	 * @return the value returned
	 * @throws InterruptedException on interrupt
	 * @throws InvocationTargetException on error
	 */
	public static <T> T callEDT(final Func0<? extends T> func) 
			throws InterruptedException, InvocationTargetException {
		if (SwingUtilities.isEventDispatchThread()) {
			return func.invoke();
		}
		final AtomicReference<T> result = new AtomicReference<>();
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				result.set(func.invoke());
			}
		});
		return result.get();
	}
	/**
	 * Cleanup all resources.
	 */
	public void done() {
	}
	@Override
	public void save(SaveMode mode) {
		control.save(null, mode);
	}
	@Override
	public String version() {
		return Configuration.VERSION;
	}
	/**
	 * Play a given sound for buttons if the effect is enabled in options.
	 * @param type the sound type
	 */
	public void buttonSound(SoundType type) {
		playSound(SoundTarget.BUTTON, type, null);
	}
	/**
	 * Play a given sound if the computer notification voice is enabled.
	 * @param type the sound type
	 */
	public void computerSound(SoundType type) {
		playSound(SoundTarget.COMPUTER, type, null);
	}
	/**
	 * Play sound effects (space and ground wars).
	 * @param type the sound type
	 */
	public void effectSound(SoundType type) {
		playSound(SoundTarget.EFFECT, type, null);
	}
	/**
	 * Play a screen-switching related sound.
	 * @param type the sound type
	 */
	public void screenSound(SoundType type) {
		playSound(SoundTarget.SCREEN, type, null);
	}
	/**
	 * Retrieve a label translation.
	 * @param label the label
	 * @return the translation
	 */
	public String get(String label) {
		return labels().get(label);
	}
	/**
	 * Format a label with parameters.
	 * @param label the label
	 * @param params the parameters
	 * @return the translation
	 */
	public String format(String label, Object... params) {
		return labels().format(label, params);
	}
	/** @return the main player */
	public Player player() {
		return world().player;
	}
	/**
	 * Instruct the R/P screen to play the animation of the technology.
	 * @param rt the technology
	 */
	public void researchChanged(ResearchType rt) {
		Screens sb = control().secondary();
		if (sb == Screens.RESEARCH || sb == Screens.PRODUCTION) {
			((ResearchProductionAnimation)control().getScreen(sb)).playAnim(rt, true);
		}
		if (sb == Screens.EQUIPMENT) {
			EquipmentScreenAPI eq = (EquipmentScreenAPI)control().getScreen(Screens.EQUIPMENT);
			eq.onResearchChanged();
		}
	}
	@Override
	public Traits traits() {
		return traits;
	}
	@Override
	public boolean isLoading() {
		return worldLoading;
	}
	/**
	 * Start a new campaign game.
	 * @param game the game definition to load
	 */
	public void startGame(GameDefinition game) {
		// TODO refactor/implement
	}
	/**
	 * Start a new skirmish game.
	 * @param game the game definition to load
	 */
	public void startGame(SkirmishDefinition game) {
		// TODO refactor/implement
	}
	/**
	 * Start a new mulitplayer game.
	 * @param game the game to start
	 */
	public void startGame(MultiplayerDefinition game) {
		// TODO implement
	}
	/**
	 * Check if the mouse event is a panning event.
	 * @param e the event
	 * @return true if panning event
	 */
	public boolean isPanningEvent(UIMouse e) {
		return (e.has(Button.RIGHT) && !config.classicControls && !e.has(Modifier.CTRL))
				|| (e.has(Button.MIDDLE) && config.classicControls);
	}
	@Override
	public <T> Future<T> schedule(Callable<T> call) {
		return pool.submit(call);
	}
	@Override
	public Future<?> schedule(Runnable run) {
		return pool.submit(run);
	}
	/**
	 * Extract skirmishable players from the given definition.
	 * @param def the definition
	 * @return the set of players
	 */
	public List<SkirmishPlayer> getPlayersFrom(GameDefinition def) {
		List<SkirmishPlayer> result = new ArrayList<>();
		Set<SkirmishPlayer> rs = new HashSet<>();
		
		XElement xplayers = rl.getXML(def.players);
		
		Labels lbl = new Labels();
		lbl.load(rl, U.startWith(def.labels, "labels"));
		
		for (XElement xplayer : xplayers.childrenWithName("player")) {
			if (xplayer.getBoolean("noskirmish", false)) {
				continue;
			}
			SkirmishPlayer sp = new SkirmishPlayer();
			
			sp.originalId = xplayer.get("id");
			sp.race = xplayer.get("race");
			sp.name = lbl.get(xplayer.get("name") + ".short");
			sp.description = lbl.get(xplayer.get("name"));
			sp.iconRef = xplayer.get("icon");
			sp.icon = rl.getImage(sp.iconRef);
			sp.color = (int)Long.parseLong(xplayer.get("color"), 16);
			sp.nodatabase = xplayer.getBoolean("nodatabase", false);
			sp.nodiplomacy = xplayer.getBoolean("nodiplomacy", false);
			sp.diplomacyHead = xplayer.get("diplomacy-head", null);
			sp.picture = xplayer.get("picture", null);
			
			String ai = xplayer.get("ai", null);
			if (xplayer.getBoolean("user", false)) {
				sp.ai = SkirmishAIMode.USER;
			} else
			if ("TRADERS".equals(ai)) {
				sp.ai = SkirmishAIMode.TRADER;
			} else
			if ("PIRATES".equals(ai)) {
				sp.ai = SkirmishAIMode.PIRATE;
			} else {
				sp.ai = SkirmishAIMode.AI_NORMAL;
			}
			
			if (rs.add(sp)) {
				result.add(sp);
			}
		}
		
		return result;
	}
}
