/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;
import hu.openig.core.Func0;
import hu.openig.core.Location;
import hu.openig.core.Pair;
import hu.openig.model.GalaxyGenerator.PlanetCandidate;
import hu.openig.render.TextRenderer;
import hu.openig.utils.Exceptions;
import hu.openig.utils.ImageUtils;
import hu.openig.utils.U;
import hu.openig.utils.WipPort;
import hu.openig.utils.XElement;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The world object.
 * @author akarnokd, 2009.10.25.
 */
public class World implements ModelLookup {
	/** Minimum planet density. */
	public static final int MIN_PLANET_DENSITY = 45;
	/** The version when the game was created. */
	public String createVersion;
	/** The name of the world. */
	public String name;
	/** Contains the skirmish definition, if non-null. */
	public SkirmishDefinition skirmishDefinition;
	/** The current world level. */
	public int level;
	/** The current player. */
	public Player player;
	/** The available players. */
	public final Players players = new Players();
	/** The time. */
	public final GregorianCalendar time = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	/** The initial game date. */
	public final Date initialDate;
	/** The available planets. */
	public final Planets planets = new Planets();
	/** The researches. */
	public final Researches researches = new Researches();
	/** The available crew-talks. */
	public Talks talks;
	/** The current talk to display, null if none. */
	public String currentTalk;
	/** Indicate that the database screen should allow recording. */
	public boolean allowRecordMessage;
	/** The message record is in progress. */
	public boolean messageRecording;
	/** The ship-walk definitions. */
	public Walks walks;
	/** The game definition. */
	public GameDefinition definition;
	/** The game simulation's parameters. */
	private final Parameters params;
	/** The difficulty of the game. */
	public Difficulty difficulty;
	/** The bridge definition. */
	public Bridge bridge;
	/** The galaxy model. */
	public GalaxyModel galaxyModel;
	/** The buildings model. */
	public BuildingModel buildingModel;
	/** The game specific labels. */
	public Labels labels;
	/** The resource locator. */
	public ResourceLocator rl;
	/** The game configuration. */
	public final Configuration config;
	/** The global world statistics. */
	public final WorldStatistics statistics = new WorldStatistics();
	/** The chat settings. */
	public Chats chats;
	/** The global id sequence. */
	protected final AtomicInteger idSequence = new AtomicInteger();
	/** The test questions. */
	public Map<String, TestQuestion> test;
	/** The test is needed. */
	public boolean testNeeded;
	/** The test has been completed. */
	public boolean testCompleted;
	/** The diplomacy definition. */
	public Map<String, Diplomacy> diplomacy;
	/** The battle object. */
	public BattleModel battle;
	/** The list of pending battles. */
	public final Deque<BattleInfo> pendingBattles = new LinkedList<>();
	/** The game environment. */
	public final GameEnvironment env;
	/** The campaign scripting. */
	public GameScripting scripting;
	/** The IDs for infected fleets and their source of infection. */
	public final Map<Integer, String> infectedFleets = new HashMap<>();
	/** The map of all diplomatic relations. */
	public final List<DiplomaticRelation> relations = new ArrayList<>();
	/** The list of all messages received, including history and duplicates. */
	public final List<VideoMessage> receivedMessages = new ArrayList<>();
	/** The global map of fleets. */
	public final Map<Integer, Fleet> fleets = new LinkedHashMap<>();
	/** The array of main categories that allow a technology to be produced. */
	public static final EnumSet<ResearchMainCategory> PRODUCTION_CATEGORIES = EnumSet.of(
			ResearchMainCategory.SPACESHIPS,
			ResearchMainCategory.EQUIPMENT,
			ResearchMainCategory.WEAPONS
	);
	/**
	 * Constructs a world under the given game environment.
	 * @param env the environment, not null
	 */
	public World(GameEnvironment env) {
		this.createVersion = env.version();
		this.env = env;
		config = env.config();
		params = new Parameters(new Func0<Integer>() {
			@Override
			public Integer invoke() {
				return config.timestep;
			}
		});
		time.set(GregorianCalendar.YEAR, 3427);
		time.set(GregorianCalendar.MONTH, GregorianCalendar.AUGUST);
		time.set(GregorianCalendar.DATE, 13);
		time.set(GregorianCalendar.HOUR_OF_DAY, 8);
		time.set(GregorianCalendar.MINUTE, 50);
		time.set(GregorianCalendar.SECOND, 0);
		time.set(GregorianCalendar.MILLISECOND, 0);
		initialDate = time.getTime();
	}
	/**
	 * Load the game world's resources.
	 * @param resLocator the resource locator
	 */
	public void loadCampaign(final ResourceLocator resLocator) {
		this.name = definition.name;
		this.rl = resLocator;
		this.params.load(definition.parameters);
		final ThreadPoolExecutor exec = 
			new ThreadPoolExecutor(Math.min(4, Runtime.getRuntime().availableProcessors()), 
					Integer.MAX_VALUE, 1, TimeUnit.SECONDS, 
					new LinkedBlockingQueue<Runnable>(),
					new ThreadFactory() {
				/** The thread count. */
				final AtomicInteger count = new AtomicInteger();
				@Override
				public Thread newThread(Runnable r) {
					Thread t = new Thread(r, "World-Loader-" + count.incrementAndGet());
					t.setPriority(Thread.MIN_PRIORITY);
					return t;
				}
			});
		exec.allowCoreThreadTimeOut(true);
		final WipPort wip = new WipPort(8);
		try {
			level = definition.level;
			
			processResearches(rl.getXML(definition.tech));

			battle = new BattleModel();
			processBattle(rl.getXML(definition.battle));

			processPlayers(rl.getXML(definition.players)); 

			// remove unused technologies.
			Set<String> races = new HashSet<>();
			for (Player p : players.values()) {
				races.add(p.race);
			}
			
			outer:
			for (ResearchType rt : new ArrayList<>(researches.values())) {
				for (String r : races) {
					if (rt.race.contains(r)) {
						continue outer;
					}
				}
				researches.map().remove(rt.id);
			}

			
			talks = new Talks();
			walks = new Walks();
			buildingModel = new BuildingModel(env.config(), 
					skirmishDefinition != null && skirmishDefinition.allowAllBuildings, races);
			galaxyModel = new GalaxyModel(env.config());
			test = new LinkedHashMap<>();
			diplomacy = new LinkedHashMap<>();
			chats = new Chats();

			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						if (definition.chats != null) {
							chats.load(rl.getXML(definition.chats));
						}
					} catch (Throwable t) {
						Exceptions.add(t);
					} finally {
						wip.dec();
					}
				}
			});

			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						TestQuestion.parse(rl.getXML(definition.test), test);
					} catch (Throwable t) {
						Exceptions.add(t);
					} finally {
						wip.dec();
					}
				}
			});

			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						Diplomacy.parse(rl.getXML(definition.diplomacy), diplomacy);
					} catch (Throwable t) {
						Exceptions.add(t);
					} finally {
						wip.dec();
					}
				}
			});

			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						talks.load(rl, definition.talks);
					} catch (Throwable t) {
						Exceptions.add(t);
					} finally {
						wip.dec();
					}
				}
			});
			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						walks.load(rl, definition.walks);
						
						bridge = new Bridge();
						processBridge(rl, definition.bridge);
					} catch (Throwable t) {
						Exceptions.add(t);
					} finally {
						wip.dec();
					}
				}
			});
			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						buildingModel.processBuildings(rl, definition.buildings, researches.map(), labels, exec, wip);
					} catch (Throwable t) {
						Exceptions.add(t);
					} finally {
						wip.dec();
					}
				}
			});
			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						galaxyModel.processGalaxy(rl, definition.galaxy, exec, wip, labels);
					} catch (Throwable t) {
						Exceptions.add(t);
					} finally {
						wip.dec();
					}
				}
			});
	
		} finally {
			wip.dec();
		}
		await(wip);
		wip.inc();
		try {
			for (final PlanetType pt : galaxyModel.planetTypes.values()) {
				for (int i = pt.start; i <= pt.end; i++) {
					final int j = i;
					wip.inc();
					final String n = String.format(pt.pattern, i);
					exec.execute(new Runnable() {
						@Override
						public void run() {
							try {
								XElement map = rl.getXML(n);
								PlanetSurface ps = new PlanetSurface();
								ps.variant = j;
								ps.parseMap(map, galaxyModel, buildingModel, newIdFunc);
								synchronized (pt.surfaces) {
									pt.surfaces.put(j, ps);
								}
							} catch (Throwable t) {
								System.err.println(n);
								Exceptions.add(t);
							} finally {
								wip.dec();
							}
						}
					});
				}
			}
		} finally {
			wip.dec();
		}
		await(wip);
		
		processPlanets(rl.getXML(definition.planets));

		// create AI for the players
		for (Player p : players.values()) {
			p.ai = env.getAI(p);
			p.ai.init(p);
			p.populateProductionHistory();
		}
		
		try {
			exec.shutdown();
		} finally {
			try {
				exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ex) {
				
			}
		}
		
		applyTraits();
		
		if (definition.scripting != null) {
			processScripting(rl.getXML(definition.scripting));
		}
		
	}
	/**
	 * Initialize the scripting.
	 * @param xscript the scripting XML
	 */
	void processScripting(XElement xscript) {
		String clazz = xscript.get("class");
		try {
			Class<?> c = Class.forName(clazz);
			if (GameScripting.class.isAssignableFrom(c)) {
				this.scripting = GameScripting.class.cast(c.newInstance());
				scripting.init(this.player, xscript);
			}
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
			Exceptions.add(ex);
		}
	}
	/**
	 * Await the port.
	 * @param wip the port
	 */
	void await(WipPort wip) {
		try {
			wip.await();
		} catch (InterruptedException ex) {
			
		}
	}
	/**
	 * Returns the current level graphics.
	 * @return the current level graphics
	 */
	public Level getCurrentLevel() {
		return bridge.levels.get(level);
	}
	/**
	 * Process the bridge definition resources.
	 * @param rl the resource locator
	 * @param data the data resource to load
	 */
	protected void processBridge(ResourceLocator rl, String data) {
		XElement root = rl.getXML(data);
		XElement graphics = root.childElement("graphics");
		for (XElement level : graphics.childrenWithName("level")) {
			Level lvl = new Level();
			lvl.id = Integer.parseInt(level.get("id"));
			lvl.image = rl.getImage(level.get("image"));
			lvl.ship = walks.ships.get(level.get("ship-id"));
			XElement mp = level.childElement("message-panel");
			
			XElement mpAppear = mp.childElement("appear");
			lvl.messageAppear.video = mpAppear.get("video");
			lvl.messageAppear.audio = mpAppear.get("audio");
			
			XElement mpOpen = mp.childElement("open");
			lvl.messageOpen.video = mpOpen.get("video");
			lvl.messageOpen.audio = mpOpen.get("audio");
			
			XElement mpClose = mp.childElement("close");
			lvl.messageClose.video = mpClose.get("video");
			lvl.messageClose.audio = mpClose.get("audio");
			
			XElement mpButtons = mp.childElement("buttons");
			String up = mpButtons.get("up");
			lvl.up[0] = rl.getImage(up);
			lvl.up[1] = rl.getImage(up + "_pressed");
			lvl.upEmpty = rl.getImage(up + "_empty");
			String down = mpButtons.get("down");
			lvl.down[0] = rl.getImage(down);
			lvl.down[1] = rl.getImage(down + "_pressed");
			lvl.downEmpty = rl.getImage(down + "_empty");
			String send = mpButtons.get("send");
			lvl.send[0] = rl.getImage(send);
			lvl.send[1] = rl.getImage(send + "_pressed");
			String receive = mpButtons.get("receive");
			lvl.receive[0] = rl.getImage(receive);
			lvl.receive[1] = rl.getImage(receive + "_pressed");
			
			XElement cp = level.childElement("comm-panel");
			XElement cpOpen = cp.childElement("open");
			lvl.projectorOpen.video = cpOpen.get("video");
			lvl.projectorOpen.audio = cpOpen.get("audio");
			
			XElement cpClose = cp.childElement("close");
			lvl.projectorClose.video = cpClose.get("video");
			lvl.projectorClose.audio = cpClose.get("audio");
			bridge.levels.put(lvl.id, lvl);
		}
		XElement messages = root.childElement("messages");
		XElement send = messages.childElement("send");
		for (XElement message : send.childrenWithName("message")) {
			VideoMessage msg = new VideoMessage();
			msg.id = message.get("id");
			msg.media = message.get("media");
			msg.title = message.get("title");
			msg.description = message.get("description");
			bridge.sendMessages.put(msg.id, msg);
		}
		XElement receive = messages.childElement("receive");
		for (XElement message : receive.childrenWithName("message")) {
			VideoMessage msg = new VideoMessage();
			msg.id = message.get("id");
			msg.media = message.get("media");
			msg.title = message.get("title");
			msg.description = message.get("description");
			bridge.receiveMessages.put(msg.id, msg);
		}
	}
	/**
	 * @return the ship for the current level
	 */
	public WalkShip getShip() {
		return getCurrentLevel().ship;
	}
	/**
	 * Process the players XML.
	 * @param xplayers the players node
	 */
	public void processPlayers(XElement xplayers) {
		
		// in skirmish, don't load the player which was not referenced
		Set<String> accept = null;
		if (skirmishDefinition != null) {
			accept = new HashSet<>();
			for (SkirmishPlayer sp : skirmishDefinition.players) {
				accept.add(sp.originalId);
			}
		}
		
		Map<Fleet, Integer> deferredFleets = new HashMap<>();
		
		int g = 1;
		for (XElement xplayer : xplayers.childrenWithName("player")) {
			String id = xplayer.get("id");
			if (accept != null && !accept.contains(id)) {
				continue;
			}
			Player p = new Player(this, id);
			p.color = (int)Long.parseLong(xplayer.get("color"), 16);
			p.race = xplayer.get("race");
			p.name = labels.get(xplayer.get("name"));
			p.shortName = labels.get(xplayer.get("name") + ".short");
			
			p.money(xplayer.getLong("money"));
			p.initialStance = xplayer.getInt("initial-stance");
			
			p.taxBase = xplayer.getDouble("tax-base", 0.0);
			
			p.group = g++;
			
			p.fleetIcon = rl.getImage(xplayer.get("icon"));
			String pic = xplayer.get("picture");
			if (pic != null) {
				p.picture = rl.getImage(pic);
			}
			
			if ("true".equals(xplayer.get("user", "false"))) {
				this.player = p;
			}
			
			if ("true".equals(xplayer.get("nodiplomacy", "false"))) {
				p.noDiplomacy = true;
			}
			if ("true".equals(xplayer.get("nodatabase", "false"))) {
				p.noDatabase = true;
			}
			
			p.diplomacyHead = xplayer.get("diplomacy-head", null);
			
			String aim = xplayer.get("ai", AIMode.DEFAULT.toString());
			if (aim.length() > 0) {
				p.aiMode = AIMode.valueOf(aim);
			}
			String rat = xplayer.get("ratios", "");
			if (rat.length() > 0) {
				String[] rts = rat.split("\\s*,\\s*");
				if (rts.length == 3) {
					double r1 = Double.parseDouble(rts[0]);
					double r2 = Double.parseDouble(rts[1]);
					double r3 = Double.parseDouble(rts[2]);
					double sum = r1 + r2 + r3;
					p.aiDefensiveRatio = r1 / sum;
					p.aiOffensiveRatio = r2 / sum;
				}
			}
			
			// add starting technology and fleet only if not in skirmish mode
			// skirmish initials are determined by the level=0 tech only.
			if (skirmishDefinition == null) {
				for (XElement xinventory : xplayer.childrenWithName("inventory")) {
					String rid = xinventory.get("id");
					ResearchType rt = researches.researches.get(rid);
					if (rt == null) {
						System.err.printf("Missing research %s for player %s%n", rid, player.id);
					} else {
						p.changeInventoryCount(rt, xinventory.getInt("count"));
					}
				}
				for (ResearchType rt : p.inventory.keySet()) {
					p.add(rt);
				}
				setTechAvailability(xplayer, p, EnumSet.allOf(ResearchMainCategory.class));
			
				loadFleets(deferredFleets, xplayer, p);
			} else
			if (!skirmishDefinition.allowAllBuildings) {
			    // enable preset building types for skirmish without all buildings
                setTechAvailability(xplayer, p, EnumSet.of(ResearchMainCategory.BUILDINGS));
			}
			
			this.players.put(p.id, p);
			
			for (ResearchType rt : researches.researches.values()) {
				if (rt.race.contains(p.race) && rt.level <= definition.techLevel) {
					p.add(rt);
				}
			}
			
			for (ResearchType rt : p.available()) {
				p.setRelated(rt);
			}
			
			String xpInnerLimit = xplayer.get("exploration-inner-limit", "");
			if (!xpInnerLimit.isEmpty()) {
				p.explorationInnerLimit = rectangleOf(xpInnerLimit);
			} else {
				p.explorationInnerLimit = null;
			}
			String xpOuterLimit = xplayer.get("exploration-outer-limit", "");
			if (!xpOuterLimit.isEmpty()) {
				p.explorationOuterLimit = rectangleOf(xpOuterLimit);
			} else {
				p.explorationOuterLimit = null;
			}
			
			p.colonizationLimit = xplayer.getInt("colonization-limit", -1);
			p.warThreshold = xplayer.getInt("war-threshold", 45);
			
			p.policeRatio = xplayer.getDouble("police-ratio", 5);
			
			
			p.difficulty = difficulty;
			
			loadTraits(p, xplayer.childElement("traits"));

			for (XElement xnicknames : xplayer.childrenWithName("nicknames")) {
				for (XElement xnickname : xnicknames.childrenWithName("nickname")) {
					p.nicknames.add(labels.get(xnickname.content));
				}
			}
			
			// override main player's trait definition
			if (player == p) {
				player.traits.replace(definition.traits);
			}
		}
		linkDeferredFleetTargets(deferredFleets);
	}
	/**
	 * Load traits for the given player.
	 * @param p the player
	 * @param xtraits the traits
	 */
	protected void loadTraits(Player p, XElement xtraits) {
		p.traits.clear();
		if (xtraits != null) {
			for (XElement xtr : xtraits.childrenWithName("trait")) {
				String id = xtr.get("id");
				Trait t = env.traits().trait(id);
				if (t != null) {
					p.traits.add(t);
				} else {
					Exceptions.add(new AssertionError("Missing trait " + id + " for player " + p.id));
				}
			}
		}
	}
	/**
	 * Save the traits into an XML definition.
	 * @param p the player
	 * @param xplayer the XML output
	 */
	protected void saveTraits(Player p, XElement xplayer) {
		XElement xtraits = xplayer.add("traits");
		for (Trait t : p.traits) {
			xtraits.add("trait").set("id", t.id);
		}
	}
	/**
	 * Create a rectangle from the given string of comma separated x, y, width and height.
	 * @param s the string
	 * @return the rectangle or null if the string is invalid
	 */
	public static Rectangle rectangleOf(String s) {
		String[] coords = s.split("\\s*,\\s*");
		if (coords.length == 4) {
			return new Rectangle(
				Integer.parseInt(coords[0]),
				Integer.parseInt(coords[1]),
				Integer.parseInt(coords[2]),
				Integer.parseInt(coords[3])
			);
		}
		return null;
	}
	/**
	 * Process the planets listing XML.
	 * @param xplanets the planets node
	 */
	public void processPlanets(XElement xplanets) {
		for (XElement xplanet : xplanets.childrenWithName("planet")) {
			Planet p = new Planet(xplanet.get("id"), this);
			p.name0 = xplanet.get("name");
			String nameLabel = xplanet.get("label", null);
			if (nameLabel != null) {
				p.name0 = labels.get(nameLabel); 
			}
			p.customName = xplanet.get("custom-name", null);
			
			p.x = Integer.parseInt(xplanet.get("x"));
			p.y = Integer.parseInt(xplanet.get("y"));
			
			p.diameter = Integer.parseInt(xplanet.get("size"));

			p.allocation = ResourceAllocationStrategy.valueOf(xplanet.get("allocation"));
			p.autoBuild = AutoBuild.valueOf(xplanet.get("autobuild"));
			p.tax = TaxLevel.valueOf(xplanet.get("tax"));
			p.rotationDirection = RotationDirection.valueOf(xplanet.get("rotate"));
			p.morale(xplanet.getDouble("morale"));
			p.taxIncome(xplanet.getDouble("tax-income"));
			p.tradeIncome(xplanet.getDouble("trade-income"));

			if (!definition.noPlanetOwner) {
				p.owner = players.get(xplanet.get("owner", null));
				p.race = xplanet.get("race", null);
				p.population(xplanet.getDouble("population"));
	
				String populationDelta = xplanet.get("population-last", null);
				if (populationDelta != null && !populationDelta.isEmpty()) {
					p.lastPopulation(Double.parseDouble(populationDelta));
				} else {
					p.lastPopulation(p.population());
				}
				p.lastMorale(xplanet.getDouble("morale-last", p.morale()));
			}
			
			XElement surface = xplanet.childElement("surface");
			String si = surface.get("id");
			String st = surface.get("type");
			p.type = galaxyModel.planetTypes.get(st);
			p.surface = p.type.surfaces.get(Integer.parseInt(si)).copy(newIdFunc);
			
			if (!definition.noPlanetBuildings) {
				p.surface.parseMap(xplanet, null, buildingModel, newIdFunc);
			}
			
			if (p.owner != null) {
				// enable placed building's researches
				for (Building b : p.surface.buildings.iterable()) {
					if (b.type.research != null && !p.owner.isAvailable(b.type.research)) {
						p.owner.setAvailable(b.type.research);
					}
				}
			}
			if (!definition.noPlanetInventory) {
				for (XElement xinv : xplanet.childElement("inventory").childrenWithName("item")) {
					int id = xinv.getInt("id", -1);
					if (id < 0) {
						id = newId();
					}
					String ownerStr = xinv.get("owner");

					InventoryItem ii = new InventoryItem(id, player(ownerStr), research(xinv.get("type")));
					ii.init();
					if (ii.owner == null) {
						Exceptions.add(new AssertionError("Planet " + p.id + " inventory owner missing: " + xinv));
					}
					ii.tag = xinv.get("tag", null);
					ii.count = xinv.getInt("count");
					ii.hp = Math.min(xinv.getDouble("hp", getHitpoints(ii.type, ii.owner)), getHitpoints(ii.type, ii.owner));
					ii.shield = xinv.getDouble("shield", Math.max(0, ii.shieldMax()));
	
					ii.nickname = xinv.get("nickname", null);
					ii.nicknameIndex = xinv.getInt("nickname-index", 0);
					ii.kills = xinv.getInt("kills", 0);
					ii.killsCost = xinv.getLong("kills-cost", 0L);
					
					p.inventory.add(ii);
						
					if (!ii.owner.isAvailable(ii.type)) {
						ii.owner.setAvailable(ii.type);
					}
				}
			}
			
			this.planets.put(p.id, p);

			if (p.owner != null) {
				p.owner.planets.put(p, PlanetKnowledge.BUILDING);
			}
		}
	}
	/**
	 * Process a tech XML.
	 * @param tech the root node of the tech XML
	 */
	public void processResearches(XElement tech) {
		for (XElement item : tech.childrenWithName("item")) {
			processResearch(item);
		}

	}
	/**
	 * Process a research/technology node.
	 * @param item the <code>item</code> node
	 */
	public void processResearch(XElement item) {
		ResearchType tech = getResearch(item.get("id"));
		
		tech.category = ResearchSubCategory.valueOf(item.get("category"));
		
		tech.name = labels.get(item.get("name"));
		tech.longName = labels.get(item.get("long-name"));
		tech.description = labels.get(item.get("description"));
		
		String image = item.get("image");
		
		tech.image = rl.getImage(image);
		tech.infoImage = placeholder(rl.getImage(image + "_large", true), tech.image);
		tech.infoImageWired = placeholder(rl.getImage(image + "_wired_large", true), tech.infoImage);
		
		tech.factory = item.get("factory");
		String raceStr = item.get("race");
		if (!raceStr.isEmpty()) {
			tech.race.addAll(Arrays.asList(raceStr.split("\\s*,\\s*")));
		}
		
		if (skirmishDefinition != null 
				&& (tech.category.main != ResearchMainCategory.BUILDINGS 
				|| skirmishDefinition.allowAllBuildings)) {
			String skirmishRace = item.get("skirmish-race", "");
			if (!skirmishRace.isEmpty()) {
				tech.race.addAll(Arrays.asList(skirmishRace.split("\\s*,\\s*")));
			}
		}
		
		tech.productionCost = Integer.parseInt(item.get("production-cost"));
		tech.researchCost = Integer.parseInt(item.get("research-cost"));
		tech.level = Integer.parseInt(item.get("level"));
		
		tech.civilLab = item.getInt("civil", 0);
		tech.mechLab = item.getInt("mech", 0);
		tech.compLab = item.getInt("comp", 0);
		tech.aiLab = item.getInt("ai", 0);
		tech.milLab = item.getInt("mil", 0);
		
		String prereqs = item.get("requires", null);
		if (prereqs != null) {
			for (String si : prereqs.split("\\s*,\\s*")) {
				tech.prerequisites.add(getResearch(si));
			}
		}
		
		for (XElement slot : item.childrenWithName("slot")) {
			EquipmentSlot s = new EquipmentSlot(slot.get("id"), false);
			s.x = slot.getInt("x");
			s.y = slot.getInt("y");
			s.width = slot.getInt("width");
			s.height = slot.getInt("height");
			s.max = slot.getInt("max");
			
			for (String si : slot.get("items").split("\\s*,\\s*")) {
				s.items.add(getResearch(si));
			}
			
			tech.slots.put(s.id, s);
		}
		for (XElement slotFixed : item.childrenWithName("slot-fixed")) {
			EquipmentSlot s = new EquipmentSlot(slotFixed.get("id"), true);
			s.max = slotFixed.getInt("count");
			s.items.add(getResearch(slotFixed.get("item")));
			tech.slots.put(s.id, s);
		}
		for (XElement prop : item.childrenWithName("property")) {
			tech.properties.put(prop.get("name"), prop.get("value"));
		}
		
		tech.equipmentImage = rl.getImage(image + "_tiny", true);
		tech.equipmentCustomizeImage = rl.getImage(image + "_small", true);
		if (tech.equipmentCustomizeImage == null) {
			tech.equipmentCustomizeImage = placeholder(rl.getImage(image + "_huge", true), tech.image);
		}
		tech.index = item.getInt("index");
		tech.video = item.get("video", null);
		
		tech.nobuild = item.getBoolean("nobuild", false);
	}
	/**
	 * Creates a placeholder image if the given image is null.
	 * @param image the input image
	 * @param alternative image
	 * @return the image or a placeholder
	 */
	BufferedImage placeholder(BufferedImage image, BufferedImage alternative) {
		if (image != null) {
			return image;
		}
		if (alternative != null) {
			return alternative;
		}
		return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	}
	/**
	 * Retrieve or create a research type.
	 * @param id the id
	 * @return the research type
	 */
	ResearchType getResearch(String id) {
		ResearchType tech = researches.get(id);
		if (tech == null) {
			tech = new ResearchType();
			tech.id = id;
			researches.put(id, tech);
		}
		return tech;
	}
	/**
	 * List the available building types for the given player.
	 * @param player the player
	 * @param planet the target planet
	 * @return the list of available building types
	 */
	public List<BuildingType> listBuildings(Player player, Planet planet) {
		List<BuildingType> result = new ArrayList<>();
		
		if (player.knowledge(planet, PlanetKnowledge.BUILDING) >= 0) {
			for (BuildingType bt : buildingModel.buildings.values()) {
				if (bt.tileset.containsKey(planet.isPopulated() ? planet.race : player.race)) {
					if (bt.research == null || (planet.owner != player)
							|| (player.isAvailable(bt.research) || bt.research.level == 0)) {
						result.add(bt);
					}
				}
			}
		} else
		if (player.knowledge(planet, PlanetKnowledge.OWNER) >= 0 && !planet.isPopulated()) {
			for (BuildingType bt : buildingModel.buildings.values()) {
				if (bt.tileset.containsKey(player.race)
						&& (bt.research == null || player.isAvailable(bt.research))) {
					result.add(bt);
				}
			}			
		}
		
		return result;
	}
	/**
	 * List the available building types for the current player for the current planet.
	 * @return the list of available building types
	 */
	public List<BuildingType> listBuildings() {
		return listBuildings(player, 
				player.currentPlanet);
	}
	/**
	 * Can the research be shown in listings?
	 * @param rt the research type
	 * @return true if display
	 */
	public boolean canDisplayResearch(ResearchType rt) {
		return rt.race.contains(player.race) && (player.isAvailable(rt) || rt.level <= level);
	}
	/**
	 * Get the research color for the given research type.
	 * @param rt the research type
	 * @param stats the global statistics
	 * @param colorActive color the running research differently?
	 * @return the color
	 */
	public int getResearchColor(ResearchType rt, PlanetStatistics stats, boolean colorActive) {
		int c = TextRenderer.GRAY;
		if (player.isAvailable(rt)) {
			c = TextRenderer.ORANGE;
		} else
		if (player.researches.containsKey(rt) && colorActive) {
			c = TextRenderer.YELLOW;
		} else
		if (player.canResearch(rt)) {
			if (rt.hasEnoughLabs(stats)) {
				c = TextRenderer.LIGHT_BLUE;
			} else 
			if (rt.labCount() <= stats.planetCount) {
				c = TextRenderer.LIGHT_GREEN;
			} else {
				c = TextRenderer.GREEN;
			}
		}
		return c;
	}
	/**
	 * Get the research color for the given research type.
	 * @param rt the research type
	 * @param stats the global statistics
	 * @return the color
	 */
	public int getResearchColor(ResearchType rt, PlanetStatistics stats) {
		return getResearchColor(rt, stats, true);
	}
	/**
	 * @return Returns an ordered list of the research types.
	 */
	public List<ResearchType> getResearch() {
		List<ResearchType> res = new ArrayList<>();
		for (ResearchType rt0 : researches.values()) {
			if (canDisplayResearch(rt0)) {
				res.add(rt0);
			}
		}
		Collections.sort(res, ResearchType.LISTING_SORT);
		return res;
	}
	/** 
	 * Select the given research and its building type if any.
	 * @param rt the non-null research type
	 */
	public void selectResearch(ResearchType rt) {
		player.currentResearch(rt);
		if (rt.category.main == ResearchMainCategory.BUILDINGS) {
			// select the appropriate building type
			for (BuildingType bt : buildingModel.buildings.values()) {
				if (bt.research == rt) {
					player.currentBuilding = bt;
					break;
				}
			}
		}
	}
	/**
	 * Derive a short state for the loading screen.
	 * @param worldSave the full world state.
	 * @return the short state
	 */
	public static XElement deriveShortWorldState(XElement worldSave) {
		XElement sstate = new XElement("world-short");
		sstate.set("level", worldSave.get("level"));
		sstate.set("difficulty", worldSave.get("difficulty"));
		sstate.set("time", worldSave.get("time"));
		String pid = worldSave.get("player");
		for (XElement pl : worldSave.childrenWithName("player")) {
			if (pid.equals(pl.get("id"))) {
				sstate.set("money", pl.get("money"));
				break;
			}
		}
		sstate.set("save-name", worldSave.get("save-name", null));
		sstate.set("save-mode", worldSave.get("save-mode", null));
		sstate.set("skirmish", worldSave.childElement("skirmish-definition") != null);

		return sstate;
	}
	/**
	 * Save the world state.
	 * @return the world state as XElement tree.
	 */
	public XElement saveState() {
		XElement xworld = new XElement("world");

		xworld.set("level", level);
		xworld.set("game", name);
		xworld.set("player", player.id);
		xworld.set("difficulty", difficulty);
		xworld.set("time", ModelUtils.format(time.getTime()));
		xworld.set("test-needed", testNeeded);
		xworld.set("test-completed", testCompleted);
		xworld.set("allow-record-message", allowRecordMessage);
		xworld.set("message-recording", messageRecording);
		xworld.set("current-talk", currentTalk);
		xworld.set("create-version", createVersion);
		xworld.set("id-sequence", idSequence.get());
		
		if (skirmishDefinition != null) {
			skirmishDefinition.save(xworld.add("skirmish-definition"));
		}
		
		statistics.save(xworld.add("statistics"));

		// save talk states
		XElement xtalk = xworld.add("talks");
		for (TalkPerson tp : talks.persons.values()) {
			for (TalkState ts : tp.states.values()) {
				for (TalkSpeech tsp : ts.speeches) {
					XElement xsp = xtalk.add("speech");
					xsp.set("person", tp.id);
					xsp.set("state", ts.id);
					xsp.set("id", tsp.id);
					xsp.set("value", tsp.spoken);
				}
			}
		}

		XElement test = xworld.add("test");
		for (TestQuestion tq : this.test.values()) {
			for (TestAnswer ta : tq.answers) {
				if (ta.selected) {
					XElement testentry = test.add("q-a");
					testentry.set("question", tq.id);
					testentry.set("answer", ta.id);
				}
			}
		}
		
		XElement xreceived = xworld.add("player-received-messages");
		for (VideoMessage vmsg : receivedMessages) {
			XElement xr = xreceived.add("message");
			xr.set("id", vmsg.id);
			xr.set("seen", vmsg.seen);
		}

		
		// save infection sources
		XElement infected = xworld.add("infected");
		for (Map.Entry<Integer, String> e : this.infectedFleets.entrySet()) {
			XElement xi = infected.add("fleet");
			xi.set("id", e.getKey());
			xi.set("source", e.getValue());
		}
		
		saveDiplomaticRelations(xworld);
		
		for (Player p : players.values()) {
			XElement xp = xworld.add("player");
			xp.set("id", p.id);
			xp.set("money", p.money());
			xp.set("planet", p.currentPlanet != null ? p.currentPlanet.id : null);
			xp.set("fleet", p.currentFleet != null ? p.currentFleet.id : null);
			xp.set("building", p.currentBuilding != null ? p.currentBuilding.id : null);
			xp.set("research", p.currentResearch() != null ? p.currentResearch().id : null);
			xp.set("running", p.runningResearch() != null ? p.runningResearch().id : null);
			xp.set("mode", p.selectionMode);
			xp.set("ai", p.aiMode);
			xp.set("pause-research", p.pauseResearch);
			xp.set("pause-production", p.pauseProduction);
			xp.set("ai-difficulty", p.difficulty);

			if (!p.colonizationTargets.isEmpty()) {
				xp.add("colonization-targets").content = U.join(p.colonizationTargets, ",");
			}
			
			if (p.explorationInnerLimit != null) {
				Rectangle r = p.explorationInnerLimit;
				xp.set("exploration-inner-limit", String.format("%d, %d, %d, %d", r.x, r.y, r.width, r.height));
			}
			if (p.explorationOuterLimit != null) {
				Rectangle r = p.explorationOuterLimit;
				xp.set("exploration-outer-limit", String.format("%d, %d, %d, %d", r.x, r.y, r.width, r.height));
			}
			// save AI state
			if (p.ai != null) {
				p.ai.save(xp.add("ai"));
			}
//			XElement xdipl = xp.add("diplomacy");
			// FIXME save pending diplomatic offers
			
			p.statistics.save(xp.add("statistics"));

			if (p.messageQueue.size() > 0) {
				XElement xqueue = xp.add("message-queue");
				for (Message msg : p.messageQueue) {
					XElement xmessage = xqueue.add("message");
					msg.save(xmessage);
				}
			}
			if (p.messageHistory.size() > 0) {
				XElement xqueue = xp.add("message-history");
				for (Message msg : p.messageHistory) {
					XElement xmessage = xqueue.add("message");
					msg.save(xmessage);
				}
			}
			
			XElement xyesterday = xp.add("yesterday");
			xyesterday.set("build", p.yesterday.buildCost);
			xyesterday.set("repair", p.yesterday.repairCost);
			xyesterday.set("research", p.yesterday.researchCost);
			xyesterday.set("production", p.yesterday.productionCost);
			xyesterday.set("tax", p.yesterday.taxIncome);
			xyesterday.set("trade", p.yesterday.tradeIncome);
			xyesterday.set("morale", p.yesterday.taxMorale);
			xyesterday.set("count", p.yesterday.taxMoraleCount);
			
			XElement xtoday = xp.add("today");
			xtoday.set("build", p.today.buildCost);
			xtoday.set("repair", p.today.repairCost);
			xtoday.set("research", p.today.researchCost);
			xtoday.set("production", p.today.productionCost);

			// save production history (backwards!)
			XElement xprodHist = xp.add("production-history");
			for (List<ResearchType> rts : p.productionHistory.values()) {
				for (int j = rts.size() - 1; j >= 0; j--) {
					XElement xtech = xprodHist.add("tech");
					xtech.set("id", rts.get(j).id);
				}
			}
			
			for (Map.Entry<ResearchMainCategory, Map<ResearchType, Production>> prods : p.production.entrySet()) {
				if (prods.getValue().size() > 0) {
					XElement xprod = xp.add("production");
					xprod.set("category", prods.getKey());
					for (Map.Entry<ResearchType, Production> pe : prods.getValue().entrySet()) {
						XElement xproditem = xprod.add("line");
						xproditem.set("id", pe.getKey().id);
						xproditem.set("count", pe.getValue().count);
						xproditem.set("priority", pe.getValue().priority);
						xproditem.set("progress", pe.getValue().progress);
					}
				}
			}
			for (Map.Entry<ResearchType, Research> res : p.researches.entrySet()) {
				XElement xres = xp.add("research");
				xres.set("id", res.getKey().id);
				xres.set("assigned", res.getValue().assignedMoney);
				xres.set("remaining", res.getValue().remainingMoney);
			}
			
			XElement res = xp.add("available");
			for (ResearchType ae : p.available()) {
				XElement av = res.add("type");
				av.set("id", ae.id);
				List<ResearchType> rel = p.availableResearch.get(ae);
				if (rel.size() > 0) {
					StringBuilder sb = new StringBuilder();
					for (ResearchType aert : rel) {
						if (sb.length() > 0) {
							sb.append(", ");
						}
						sb.append(aert.id);
					}
					
					av.set("list", sb.toString());
				}
			}
			for (Map.Entry<Fleet, FleetKnowledge> fl : p.fleets.entrySet()) {
				Fleet f = fl.getKey();
				if (f.owner == p && !f.inventory.isEmpty()) {
					XElement xfleet = xp.add("fleet");
					saveFleet(f, xfleet);
				}
			}
			// save discovered planets only
			StringBuilder sb1 = new StringBuilder();
			StringBuilder sb2 = new StringBuilder();
			for (Map.Entry<Planet, PlanetKnowledge> pk : p.planets.entrySet()) {
				if (pk.getKey().owner != p) {
					if (pk.getValue().ordinal() >= PlanetKnowledge.NAME.ordinal()) {
						if (sb2.length() > 0) {
							sb2.append(", ");
						}
						sb2.append(pk.getKey().id);
					} else {
						if (sb1.length() > 0) {
							sb1.append(", ");
						}
						sb1.append(pk.getKey().id);
					}
				}
			}
			xp.set("discovered", sb1.toString());
			xp.set("discovered-named", sb2.toString());
			
			for (Map.Entry<ResearchType, Integer> inv : p.inventory.entrySet()) {
				XElement xinv = xp.add("inventory");
				xinv.set("id", inv.getKey().id);
				xinv.set("count", inv.getValue());
			}
			
			// pending diplomatic offers
			
			XElement xdipls = xp.add("diplomatic-offers");
			for (Map.Entry<String, DiplomaticOffer> e : p.offers.entrySet()) {
				XElement xdipl = xdipls.add("offer");
				xdipl.set("from", e.getKey());
				DiplomaticOffer dio = e.getValue();
				xdipl.set("call", dio.callType);
				xdipl.set("approach", dio.approach);
				xdipl.set("value", dio.value());
			}
			
			saveTraits(p, xp);
			
			XElement xbm = xp.add("blackmarket");
			if (p.blackMarketRestock != null) {
				xbm.set("restock", XElement.formatDateTime(p.blackMarketRestock));
			}
			for (InventoryItem ii : p.blackMarket) {
				XElement xitem = xbm.add("item");
				saveInventoryItem(ii, xitem);
			}
		}
		
		for (Planet p : planets.values()) {
			XElement xp = xworld.add("planet");
			xp.set("id", p.id);
			xp.set("x", p.x);
			xp.set("y", p.y);
			xp.set("size", p.diameter);
			xp.set("surface-type", p.type.type);
			xp.set("surface-variant", p.surface.variant);
			xp.set("custom-name", p.customName);
			for (InventoryItem pii : p.inventory.iterable()) {
				XElement xpii = xp.add("item");
				xpii.set("id", pii.id);
				xpii.set("type", pii.type.id);
				xpii.set("owner", pii.owner.id);
				xpii.set("count", pii.count);
				xpii.set("hp", pii.hp);
				xpii.set("shield", pii.shield);
				xpii.set("tag", pii.tag);
				xpii.set("nickname", pii.nickname);
				xpii.set("nickname-index", pii.nicknameIndex);
				xpii.set("kills", pii.kills);
				xpii.set("kills-cost", pii.killsCost);
				
				Integer ttl = p.timeToLive.get(pii); 
				if (ttl != null) {
					xpii.set("ttl", ttl);
				}
				
				saveInventorySlot(pii.slots.values(), xpii);
			}
			if (p.owner != null) {
				xp.set("owner", p.owner.id);
				xp.set("race", p.race);
				xp.set("quarantine-ttl", p.quarantineTTL);
				xp.set("allocation", p.allocation);
				xp.set("tax", p.tax);
				xp.set("morale", p.morale());
				xp.set("morale-last", p.lastMorale());
				xp.set("population", p.population());
				xp.set("population-last", p.lastPopulation());
				xp.set("autobuild", p.autoBuild);
				xp.set("tax-income", p.taxIncome());
				xp.set("trade-income", p.tradeIncome());
				xp.set("earthquake-ttl", p.earthQuakeTTL);
				xp.set("weather-ttl", p.weatherTTL);
				for (Building b : p.surface.buildings.iterable()) {
					XElement xb = xp.add("building");
					xb.set("id", b.id);
					xb.set("x", b.location.x);
					xb.set("y", b.location.y);
					xb.set("type", b.type.id);
					xb.set("race", b.race);
					xb.set("enabled", b.enabled);
					xb.set("repairing", b.repairing);
					xb.set("hp", b.hitpoints);
					xb.set("build", b.buildProgress);
					xb.set("level", b.upgradeLevel);
					xb.set("energy", b.assignedEnergy);
					xb.set("worker", b.assignedWorker);
				}
			}
		}
		
		XElement xscript = xworld.add("scripting");
		xscript.set("class", scripting.getClass().getName());
		scripting.save(xscript);
		
		// save pending battles
		XElement xbattles = xworld.add("pending-battles");
		for (BattleInfo bi : pendingBattles) {
			XElement xbattle = xbattles.add("battle");
			xbattle.set("attacker", bi.attacker.id);
			if (bi.targetFleet != null) {
				xbattle.set("target-fleet", bi.targetFleet.id);
			}
			if (bi.targetPlanet != null) {
				xbattle.set("target-planet", bi.targetPlanet.id);
			}
		}
		
		if (config.crashLog != null) {
			String crashLog = config.crashLog.invoke();
			if (crashLog != null) {
				xworld.add("crash-log").content = crashLog;
			}
		}
		return xworld;
	}
	/**
	 * Save the fleet into XML.
	 * @param f the fleet object
	 * @param xfleet the XML
	 */
	void saveFleet(Fleet f, XElement xfleet) {
		xfleet.set("id", f.id);
		xfleet.set("x", f.x);
		xfleet.set("y", f.y);
		xfleet.set("name", f.name());
		xfleet.set("task", f.task);
		xfleet.set("refill-once", f.refillOnce);
		xfleet.set("formation", f.formation);
		if (f.targetFleet != null) {
			xfleet.set("target-fleet", f.targetFleet.id);
		} else
		if (f.targetPlanet() != null) {
			xfleet.set("target-planet", f.targetPlanet().id);
		}
		if (f.arrivedAt != null) {
			xfleet.set("arrived-at", f.arrivedAt.id);
		}
		xfleet.set("mode", f.mode);
		if (f.waypoints.size() > 0) {
			StringBuilder wp = new StringBuilder();
			for (Point2D.Double pt : f.waypoints) {
				if (wp.length() > 0) {
					wp.append(" ");
				}
				wp.append(pt.x).append(";").append(pt.y);
			}
			xfleet.set("waypoints", wp.toString());
		}
		
		for (InventoryItem fii : f.inventory.iterable()) {
			XElement xfii = xfleet.add("item");
			saveInventoryItem(fii, xfii);
		}
	}
	/**
	 * Save the inventory item into the XML element.
	 * @param fii the inventory item to save
	 * @param xfii the target XElement
	 */
	void saveInventoryItem(InventoryItem fii, XElement xfii) {
		xfii.set("id", fii.id);
		xfii.set("type", fii.type.id);
		xfii.set("count", fii.count);
		xfii.set("hp", fii.hp);
		xfii.set("shield", fii.shield);
		xfii.set("tag", fii.tag);
		xfii.set("nickname", fii.nickname);
		xfii.set("nickname-index", fii.nicknameIndex);
		xfii.set("kills", fii.kills);
		xfii.set("kills-cost", fii.killsCost);
		saveInventorySlot(fii.slots.values(), xfii);
	}
	/**
	 * Saves the contents of the inventory slots.
	 * @param slots the slots list
	 * @param xparent the parent XElement to store
	 */
	void saveInventorySlot(Iterable<InventorySlot> slots, XElement xparent) {
		for (InventorySlot fis : slots) {
			if (!fis.slot.fixed) {
				XElement xfs = xparent.add("slot");
				xfs.set("id", fis.slot.id);
				xfs.set("hp", fis.hp);
				if (fis.type != null) {
					xfs.set("type", fis.type.id);
					xfs.set("count", fis.count);
				}
			}
		}
	}
	/**
	 * Load the world state.
	 * @param xworld the world XElement
	 */
	public void loadState(XElement xworld) {
		difficulty = Difficulty.valueOf(xworld.get("difficulty"));
		level = xworld.getInt("level");
		
		// FIXME all tricks and patches should be removed in gamma
		patchWorld(xworld);
		
		idSequence.set(xworld.getInt("id-sequence"));
		testNeeded = xworld.getBoolean("test-needed", false);
		testCompleted = xworld.getBoolean("test-completed", false);
		allowRecordMessage = xworld.getBoolean("allow-record-message", false);
		this.messageRecording = xworld.getBoolean("message-recording", false);
		currentTalk = xworld.get("current-talk", null);
		createVersion = xworld.get("create-version", env.version());
		
		try {
			time.setTime(ModelUtils.parse(xworld.get("time")));
			time.set(GregorianCalendar.MINUTE, (time.get(GregorianCalendar.MINUTE) / 10) * 10);
			time.set(GregorianCalendar.SECOND, 0);
			time.set(GregorianCalendar.MILLISECOND, 0);
		} catch (ParseException ex) {
			Exceptions.add(ex);
		}
		
		player = players.get(xworld.get("player"));
		
		XElement stats = xworld.childElement("statistics");
		if (stats != null) {
			statistics.load(stats);
		}

		// clear previous test
		for (TestQuestion tq : test.values()) {
			for (TestAnswer ta : tq.answers) {
				ta.selected = false;
			}
		}
		XElement test = xworld.childElement("test");
		if (test != null) {
			for (XElement qa : test.childrenWithName("q-a")) {
				TestQuestion tq = this.test.get(qa.get("question"));
				tq.choose(qa.get("answer"));
			}
		}
		
		XElement xreceived = xworld.childElement("player-received-messages");
		receivedMessages.clear();
		if (xreceived != null) {
			for (XElement xr : xreceived.childrenWithName("message")) {
				VideoMessage vmsg = bridge.receiveMessages.get(xr.get("id")).copy();
				vmsg.seen = xr.getBoolean("seen");
				receivedMessages.add(vmsg);
			}
		}
		
		// reset talks
		for (TalkPerson tp : talks.persons.values()) {
			for (TalkState ts : tp.states.values()) {
				for (TalkSpeech tsp : ts.speeches) {
					tsp.spoken = false;
				}
			}
		}
		// load talk states
		XElement xtalk = xworld.childElement("talks");
		if (xtalk != null) {
			for (XElement xsp : xtalk.childrenWithName("speech")) {
				String person = xsp.get("person");
				String state = xsp.get("state");
				String id = xsp.get("id");
				boolean spoken = xsp.getBoolean("value");
				TalkState ts = talks.persons.get(person).states.get(state);
				for (TalkSpeech tsp : ts.speeches) {
					if (tsp.id.equals(id)) {
						tsp.spoken = spoken;
					}
				}
			}
		}
		
		fleets.clear();
		
		// restore known infected fleets
		infectedFleets.clear();
		XElement infected = xworld.childElement("infected");
		if (infected != null) {
			for (XElement xi : infected.childrenWithName("fleet")) {
				int id = xi.getInt("id");
				String source = xi.get("source");
				infectedFleets.put(id, source);
			}
		}
		
		loadDiplomaticRelations(xworld);
		
		Map<Player, XElement[]> deferredMessages = new HashMap<>();
		/** The deferred fleet-to-fleet targeting. */
		Map<Fleet, Integer> deferredTargets = new HashMap<>();
		
		for (Player p : players.values()) {
			p.fleets.clear(); 
		}
		
		for (XElement xplayer : xworld.childrenWithName("player")) {
			Player p = players.get(xplayer.get("id"));
			// clear player variables
			p.messageHistory.clear();
			p.messageQueue.clear();
			p.colonizationTargets.clear();
			
			
			p.money(xplayer.getLong("money"));
			p.currentPlanet = planets.get(xplayer.get("planet", null));
			
			p.currentBuilding = buildingModel.buildings.get(xplayer.get("building", null));
			p.currentResearch(researches.get(xplayer.get("research", null)));
			p.runningResearch(researches.get(xplayer.get("running", null)));
			p.selectionMode = SelectionMode.valueOf(xplayer.get("mode", SelectionMode.PLANET.toString()));
			
			XElement xyesterday = xplayer.childElement("yesterday");
			
			p.yesterday.buildCost = xyesterday.getInt("build");
			p.yesterday.repairCost = xyesterday.getInt("repair");
			p.yesterday.researchCost = xyesterday.getInt("research");
			p.yesterday.productionCost = xyesterday.getInt("production");
			p.yesterday.taxIncome = xyesterday.getInt("tax");
			p.yesterday.tradeIncome = xyesterday.getInt("trade");
			p.yesterday.taxMorale = xyesterday.getDouble("morale");
			p.yesterday.taxMoraleCount = xyesterday.getInt("count");
			
			XElement xtoday = xplayer.childElement("today");
			p.today.buildCost = xtoday.getInt("build");
			p.today.repairCost = xtoday.getInt("repair");
			p.today.researchCost = xtoday.getInt("research");
			p.today.productionCost = xtoday.getInt("production");
			
			p.pauseProduction = xplayer.getBoolean("pause-production", false);
			p.pauseResearch = xplayer.getBoolean("pause-research", false);

			XElement xcolonize = xplayer.childElement("colonization-targets");
			if (xcolonize != null && xcolonize.content != null) {
				p.colonizationTargets.addAll(Arrays.asList(U.split(xcolonize.content, ",")));
			}
			
			p.difficulty = Difficulty.valueOf(xplayer.get("ai-difficulty", difficulty.toString()));
			
			String xpInnerLimit = xplayer.get("exploration-inner-limit", "");
			if (!xpInnerLimit.isEmpty()) {
				p.explorationInnerLimit = rectangleOf(xpInnerLimit);
			} else {
				p.explorationInnerLimit = null;
			}
			
			String xpOuterLimit = xplayer.get("exploration-outer-limit", "");
			if (!xpOuterLimit.isEmpty()) {
				p.explorationOuterLimit = rectangleOf(xpOuterLimit);
			} else {
				p.explorationOuterLimit = null;
			}

			String aim = xplayer.get("ai", "NONE");
			if (aim.length() > 0) {
				p.aiMode = AIMode.valueOf(aim);
			}

			p.productionHistory.clear();
			XElement xprodHist = xplayer.childElement("production-history");
			if (xprodHist != null) {
				for (XElement xph : xprodHist.childrenWithName("tech")) {
					String id = xph.get("id");
					ResearchType rt = researches.get(id);
					if (rt == null) {
						System.out.println("Warning: unknown technology in production history: " + id);
					} else {
						p.addProductionHistory(rt);
					}
					
				}
			}
			
//			XElement xdipl = xplayer.childElement("diplomacy");
			// FIXME load pending diplomatic offers
			
			for (Map<ResearchType, Production> prod : p.production.values()) {
				prod.clear();
			}
			
			XElement pstats = xplayer.childElement("statistics");
			if (pstats != null) {
				p.statistics.load(pstats);
			}
			
			for (XElement xprod : xplayer.childrenWithName("production")) {
				for (XElement xline : xprod.childrenWithName("line")) {
					ResearchType rt = researches.get(xline.get("id"));
					if (rt != null) {
						Production pr = new Production();
						pr.type = rt;
						pr.count = xline.getInt("count");
						pr.priority = xline.getInt("priority");
						pr.progress = xline.getInt("progress");
						
						Map<ResearchType, Production> prod = p.production.get(rt.category.main);
						if (prod == null) {
							prod = new LinkedHashMap<>();
							p.production.put(rt.category.main, prod);
						}
						prod.put(rt, pr);
					} else {
						System.out.println("WARN | Unknown technology in production: " + xline);
					}
				}
			}
			p.researches.clear();
			for (XElement xres : xplayer.childrenWithName("research")) {
				ResearchType rt = researches.get(xres.get("id"));
				if (rt == null) {
					System.out.println("WARN | research technology not found: " + xres.get("id"));
				} else {
					Research rs = new Research();
					rs.type = rt;
					rs.state = rt == p.runningResearch() ? ResearchState.RUNNING : ResearchState.STOPPED; 
					rs.assignedMoney = xres.getInt("assigned");
					rs.remainingMoney = xres.getInt("remaining");
					p.researches.put(rt, rs);
				}
			}
			
			p.availableResearch.clear();
            // add free technologies
//			for (ResearchType rt : researches.values()) {
//				if (rt.level == 0 && rt.race.contains(p.race)) {
//					p.add(rt);
//				}
//			}
			
			setTechAvailability(xplayer, p, EnumSet.allOf(ResearchMainCategory.class));
			
			loadFleets(deferredTargets, xplayer, p);
			
			p.planets.clear();
			for (String pl : xplayer.get("discovered").split("\\s*,\\s*")) {
				if (pl.length() > 0) {
					Planet p0 = planets.get(pl);
					if (p0 == null) {
						throw new IllegalArgumentException("discovered planet not found: " + pl);
					}
					p.planets.put(p0, PlanetKnowledge.VISIBLE);
				}
			}
			for (String pl : xplayer.get("discovered-named", "").split("\\s*,\\s*")) {
				if (pl.length() > 0) {
					Planet p0 = planets.get(pl);
					if (p0 != null) {
						// FIXME: silently ignore planets with unknown name for now
						p.planets.put(p0, PlanetKnowledge.NAME);
					}
				}
			}
			p.inventory.clear();
			for (XElement xinv : xplayer.childrenWithName("inventory")) {
				ResearchType key = researches.get(xinv.get("id"));
				if (key != null) {
					p.inventory.put(key, xinv.getInt("count"));
				} else {
					System.out.println("WARN | Unknown inventory technology: " + xinv);
				}
			}
			p.currentFleet = null;
			int currentFleet = xplayer.getInt("fleet", -1);
			if (currentFleet >= 0) {
				for (Fleet f : p.fleets.keySet()) {
					if (f.id == currentFleet) {
						p.currentFleet = f;
						break;
					}
				}
			}
			XElement xqueue = xplayer.childElement("message-queue");
			XElement xhistory = xplayer.childElement("message-history");
			deferredMessages.put(p, new XElement[] { xqueue, xhistory });
			
			// diplomatic offers
			p.offers.clear();
			XElement xdipls = xplayer.childElement("diplomatic-offers");
			if (xdipls != null) {
				for (XElement xdipl : xdipls.childrenWithName("offer")) {
					String from = xdipl.get("from");
					CallType nt = CallType.valueOf(xdipl.get("call"));
					ApproachType at = ApproachType.valueOf(xdipl.get("approach"));
					int value = xdipl.getInt("value", 10000);
					
					p.offers.put(from, new DiplomaticOffer(nt, at).value(value));
				}
			}
			
			loadTraits(p, xplayer.childElement("traits"));
			
			for (XElement xbm : xplayer.childrenWithName("blackmarket")) {
				p.blackMarket.clear();
				p.blackMarketRestock = null;
				if (xbm.has("restock")) {
					try {
						p.blackMarketRestock = XElement.parseDateTime(xbm.get("restock"));
					} catch (ParseException ex) {
						ex.printStackTrace();
					}
				}
				for (XElement xitem : xbm.childrenWithName("item")) {
					InventoryItem ii = parseInventory(xitem, p); // owner is required
					if (ii != null) {
						p.blackMarket.add(ii);
					}
				}
			}
		}
		Set<String> allPlanets = new HashSet<>(planets.keySet());
		for (XElement xplanet : xworld.childrenWithName("planet")) {
			String pid = xplanet.get("id");
			Planet p = planets.get(pid);
			if (p == null) {
				System.out.println("Unknown planet: " + pid);
				continue;
			}
			Player lo = p.owner;
			p.die();
			if (lo != null) {
				lo.planets.remove(p);
			}

			if (xplanet.has("x") && xplanet.has("y") && xplanet.has("size")) {
				p.x = xplanet.getInt("x");
				p.y = xplanet.getInt("y");
				p.diameter = xplanet.getInt("size");
			}
			
			p.inventory.clear();
			p.surface.buildings.clear();
			p.surface.buildingmap.clear();
			p.timeToLive.clear();

			// change surface type
			String stype = xplanet.get("surface-type", p.type.type);
			int svar = xplanet.getInt("surface-variant", p.surface.variant);
			
			if (!stype.equals(p.type.type) || svar != p.surface.variant) {
				p.type = galaxyModel.planetTypes.get(stype);
				PlanetSurface psf = p.type.surfaces.get(svar);
				if (psf == null && !p.type.surfaces.isEmpty()) {
					psf = p.type.surfaces.values().iterator().next();
				}
				if (psf != null) {
					p.surface = psf.copy(newIdFunc);
				} else {
					Exceptions.add(new IllegalArgumentException("Surface variant " + svar + " of surface type " + stype + " not found for planet " + p.id));
				}
			}
			
			for (XElement xpii : xplanet.childrenWithName("item")) {
				int id = xpii.getInt("id");
				
				ResearchType rt = research(xpii.get("type"));
				if (rt != null) {
					InventoryItem pii = new InventoryItem(id, player(xpii.get("owner")), rt);
					pii.init();
					pii.tag = xpii.get("tag", null);
					pii.count = xpii.getInt("count");
					pii.hp = Math.min(xpii.getDouble("hp", getHitpoints(pii.type, pii.owner)), getHitpoints(pii.type, pii.owner));
					pii.shield = xpii.getDouble("shield", Math.max(0, pii.shieldMax()));
	
					pii.nickname = xpii.get("nickname", null);
					pii.nicknameIndex = xpii.getInt("nickname-index", 0);
					pii.kills = xpii.getInt("kills", 0);
					pii.killsCost = xpii.getLong("kills-cost", 0L);
	
					int ttl = xpii.getInt("ttl", getSatelliteTTL(pii.owner, pii.type));
					if (ttl > 0) {
						p.timeToLive.put(pii, ttl);
					}
					
					loadInventorySlots(pii, xpii);
					
					p.inventory.add(pii);
				} else {
					System.out.println("WARN | Unknow planet invenetory technology: " + xpii);
				}
			}
			String sowner = xplanet.get("owner", null);
			p.customName = xplanet.get("custom-name", null);

			if (sowner != null) {
				p.owner = players.get(sowner);
				p.race = xplanet.get("race");
				p.quarantineTTL = xplanet.getInt("quarantine-ttl", 0);
				p.allocation = ResourceAllocationStrategy.valueOf(xplanet.get("allocation"));
				p.tax = TaxLevel.valueOf(xplanet.get("tax"));
				p.morale(xplanet.getDouble("morale", 50d));
				p.lastMorale(xplanet.getDouble("morale-last", 50d));
				p.population(xplanet.getDouble("population"));
				p.lastPopulation(xplanet.getDouble("population-last", p.population()));
				p.autoBuild = AutoBuild.valueOf(xplanet.get("autobuild"));
				p.taxIncome(xplanet.getDouble("tax-income"));
				p.tradeIncome(xplanet.getDouble("trade-income"));
				p.earthQuakeTTL = xplanet.getInt("earthquake-ttl", 0);
				p.weatherTTL = xplanet.getInt("weather-ttl", 0);
				
				p.surface.setBuildings(buildingModel, xplanet, newIdFunc);
				p.owner.planets.put(p, PlanetKnowledge.BUILDING);
				// make owned technology available, just in case
				for (Building b : p.surface.buildings.iterable()) {
					if (b.type.research != null) {
						p.owner.setAvailable(b.type.research);
					}
				}
			}
			
			allPlanets.remove(p.id);
		}
		for (String rest : allPlanets) {
			Planet p = planets.get(rest);
			p.die();
		}
		for (Map.Entry<Player, XElement[]> e : deferredMessages.entrySet()) {
			if (e.getValue()[0] != null) {
				e.getKey().messageQueue.clear();
				for (XElement xmessage : e.getValue()[0].childrenWithName("message")) {
					Message msg = new Message();
					msg.load(xmessage, this);
					e.getKey().messageQueue.add(msg);
				}
			}
			if (e.getValue()[1] != null) {
				e.getKey().messageHistory.clear();
				for (XElement xmessage : e.getValue()[1].childrenWithName("message")) {
					Message msg = new Message();
					msg.load(xmessage, this);
					e.getKey().messageHistory.add(msg);
				}
				e.getKey().sortHistory();
			}
		}
		linkDeferredFleetTargets(deferredTargets);

		// restore AI for players, restore stance
		for (XElement xplayer : xworld.childrenWithName("player")) {
			XElement xai = xplayer.childElement("ai");
			if (xai != null) {
				Player p = players.get(xplayer.get("id"));
				p.ai = env.getAI(p);
				p.ai.init(p);
				p.ai.load(xai);
				
			}
		}
		XElement xscript = xworld.childElement("scripting");
		if (xworld != null) {
			scripting.load(xscript);
		}
		
		checkUniqueFleets();
		
		// restore pending battles
		pendingBattles.clear();
		XElement xbattles = xworld.childElement("pending-battles");
		if (xbattles != null) {
			for (XElement xbattle : xbattles.childrenWithName("battle")) {
				BattleInfo bi = new BattleInfo();
				bi.attacker = findFleet(xbattle.getInt("attacker"));
				
				int tf = xbattle.getInt("target-fleet", -1);
				if (tf >= 0) {
					bi.targetFleet = findFleet(tf);
					if (bi.targetFleet == null) {
						Exceptions.add(new AssertionError("Pending battle missing target fleet: " + tf));
						continue;
					}
				}
				String tp = xbattle.get("target-planet", null);
				if (tp != null) {
					bi.targetPlanet = planets.get(tp); 
					if (bi.targetPlanet == null) {
						Exceptions.add(new AssertionError("Pending battle missing target planet: " + tp));
						continue;
					}
				}
				pendingBattles.add(bi);
			}
		}
	}
	/** Check for unique fleet IDs. */
	void checkUniqueFleets() {
		Set<Integer> ids = new HashSet<>();
		
		for (Player p : players.values()) {
			for (Fleet f : p.ownFleets()) {
				if (!ids.add(f.id)) {
					System.err.printf("Fleet conflict ID = %d, Name = %s, Owner = %s %n", f.id, f.name(), f.owner.id);
				}
			}
		}
	}
	/**
	 * Calculate the satellite TTL value.
	 * @param owner the owner of the satellite
	 * @param satellite the satellite type
	 * @return the TTL in simulation steps
	 */
	public int getSatelliteTTL(Player owner, ResearchType satellite) {
		// find cheapest satellite
		ResearchType minSatellite = null;
		for (ResearchType rt : researches.values()) {
			if (rt.has(ResearchType.PARAMETER_DETECTOR)) {
				if (minSatellite == null || minSatellite.productionCost > rt.productionCost) {
					minSatellite = rt;
				}
			}
		}
		if (minSatellite == null || !satellite.has(ResearchType.PARAMETER_DETECTOR)) {
			return 0;
		}
		
		int ttl = 12 * 6 * 4 * satellite.productionCost / minSatellite.productionCost;
		if (owner == player) {
			if (difficulty == Difficulty.EASY) {
				ttl *= 4;
			} else
			if (difficulty == Difficulty.NORMAL) {
				ttl *= 2;
			}
		} else {
			if (difficulty == Difficulty.HARD) {
				ttl *= 4;
			} else
			if (difficulty == Difficulty.NORMAL) {
				ttl *= 2;
			}
		}
		return ttl;
	}
	/**
	 * Link the fleet's targetFleet value with the fleet given by the ID.
	 * @param deferredTargets the map from source fleet to target fleet ID
	 */
	private void linkDeferredFleetTargets(Map<Fleet, Integer> deferredTargets) {
		for (Map.Entry<Fleet, Integer> ft : deferredTargets.entrySet()) {
			outer:
			for (Player p : players.values()) {
				for (Fleet f : p.ownFleets()) {
					if (f.id == ft.getValue()) {
						ft.getKey().targetFleet = f;
						break outer;
					}
				}
			}
		}
	}
	/**
	 * Set the fleets from the given player definition into the player object.
	 * @param deferredTargets the deferred targets if a fleet targets another
	 * @param xplayer the source definition
	 * @param p the target player object
	 */
	private void loadFleets(Map<Fleet, Integer> deferredTargets,
			XElement xplayer, Player p) {
		for (XElement xfleet : xplayer.childrenWithName("fleet")) {
			int id = xfleet.getInt("id", -1);
			if (id < 0) {
				id = newId();
			}
			
			Fleet f = new Fleet(id, p);

			f.x = xfleet.getFloat("x");
			f.y = xfleet.getFloat("y");
			f.name(xfleet.get("name", ""));
			if (f.name().startsWith("@")) {
				f.name(labels.get(f.name()));
			}
			
			String s0 = xfleet.get("target-fleet", null);
			if (s0 != null) {
				deferredTargets.put(f, Integer.parseInt(s0));
			}
			s0 = xfleet.get("target-planet", null);
			if (s0 != null) {
				f.targetPlanet(planets.get(s0));
			}
			s0 = xfleet.get("arrived-at", null);
			if (s0 != null) {
				f.arrivedAt = planets.get(s0);
			}
			
			s0 = xfleet.get("mode", null);
			if (s0 != null) {
				f.mode = FleetMode.valueOf(s0);
			}
			
			f.task = FleetTask.valueOf(xfleet.get("task", FleetTask.IDLE.toString()));
			
			f.refillOnce = xfleet.getBoolean("refill-once", true);
			
			f.formation = xfleet.getInt("formation", -1);
			
			s0 = xfleet.get("waypoints", null);
			if (s0 != null) {
				for (String wp : s0.split("\\s+")) {
					String[] xy = wp.split(";");
					f.waypoints.add(new Point2D.Double(Double.parseDouble(xy[0]), Double.parseDouble(xy[1])));
				}
			}
			for (XElement xfii : xfleet.childrenWithName("item")) {
				InventoryItem fii = parseInventory(xfii, f.owner);
				if (fii != null) {
					if (f.inventory.isEmpty()) {
						p.fleets.put(f, FleetKnowledge.FULL);
					}
					f.inventory.add(fii);
				}
			}
		}
	}
	/**
	 * Parse an inventory node.
	 * @param xfii the inventory XML element
	 * @param owner the owner of the inventory item
	 * @return the inventory or null if there was an anomaly
	 */
	InventoryItem parseInventory(XElement xfii, Player owner) {
		int count = xfii.getInt("count");
		if (count <= 0) {
			return null;
		}
		int itemid = xfii.getInt("id");
		if (itemid < 0) {
			itemid = newId();
		}
		ResearchType research = research(xfii.get("type"));
		if (research == null) {
			System.out.println("WARN | Inventory technology not found: " + xfii);
			return null;
		}
		InventoryItem fii = new InventoryItem(itemid, owner, research);
		
		fii.nickname = xfii.get("nickname", fii.nickname);
		fii.nicknameIndex = xfii.getInt("nickname-index", 0);

		fii.init();
		fii.count = count;
		fii.tag = xfii.get("tag", null);
		
		
		fii.kills = xfii.getInt("kills", 0);
		fii.killsCost = xfii.getLong("kills-cost", 0L);

		loadInventorySlots(fii, xfii);
		
		int shieldMax = Math.max(0, fii.shieldMax());
		fii.shield = Math.min(shieldMax, xfii.getDouble("shield", shieldMax));
		fii.hp = Math.min(xfii.getDouble("hp", getHitpoints(fii.type, fii.owner)), getHitpoints(fii.type, fii.owner));

		return fii;
	}
	/**
	 * Loads the inventory slot contents from the supplied XML parent.
	 * @param fii the target inventory item
	 * @param xfii the parent XElement of the slot items
	 */
	void loadInventorySlots(InventoryItem fii, XElement xfii) {
		for (XElement xfis : xfii.childrenWithName("slot")) {
			String sid = xfis.get("id");

			InventorySlot fis = fii.getSlot(sid);
			if (fis == null || fis.slot.fixed) {
				continue; // ignore nonexistent slots
			}
			
			fis.type = researches.get(xfis.get("type", null));
			if (fis.type != null) {
				fis.count = Math.max(0, Math.min(xfis.getInt("count"), fis.slot.max));
			} else {
				fis.count = 0;
			}
			
			int hp0 = fis.hpMax(fii.owner);
			fis.hp = Math.min(xfis.getDouble("hp", hp0), hp0);
		}
	}
	/**
	 * Check if the fleet identifier is already present.
	 * @param id the identifier
	 * @return true if present
	 */
	boolean checkDuplicate(int id) {
		return findFleet(id) != null;
	}
	/**
	 * Set the available technologies for the given player.
	 * @param xplayer the player definition
	 * @param p the player object to load
	 * @param categoryFilter the set of main categories to pick up
	 */
	private void setTechAvailability(XElement xplayer, Player p, EnumSet<ResearchMainCategory> categoryFilter) {
		XElement xavail0 = xplayer.childElement("available");
		if (xavail0 != null) {
			for (XElement xavail : xavail0.childrenWithName("type")) {
				String id = xavail.get("id");
				ResearchType rt = researches.get(id);
				if (rt == null) {
					System.out.println("WARN | Available technology not found: " + xavail);
				} else 
				if (categoryFilter.contains(rt.category.main)) {
					p.add(rt);
					for (String liste : xavail.get("list", "").split("\\s*,\\s*")) {
						if (liste.length() > 0) {
							ResearchType rt0 = researches.get(liste);
							if (rt0 == null) {
								System.out.println("WARN | available technology not found: " + liste + " in " + xavail);
							} else {
								p.availableLevel(rt).add(rt0);
							}
						}
					}
				}
			}
		}
	}
	/** @return Return the list of other important items. */
	public String getOtherItems() {
		StringBuilder os = new StringBuilder();
		for (InventoryItem pii : player.currentPlanet.inventory.iterable()) {
			if (pii.owner == player && pii.type.category == ResearchSubCategory.SPACESHIPS_SATELLITES) {
				if (os.length() > 0) {
					os.append(", ");
				}
				os.append(pii.type.name);
			} else
			if (pii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS
			&& (pii.owner == player || player.knowledge(player.currentPlanet, PlanetKnowledge.STATIONS) >= 0)) {
				if (os.length() > 0) {
					os.append(", ");
				}
				os.append(pii.type.name);
			}
		}
		return os.toString();
	}
	/** 
	 * @param ps the planet statistics
	 * @return the list of needed items. 
	 */
	public String getNeeded(PlanetStatistics ps) {
		StringBuilder os = new StringBuilder();
		
		if (ps.hasAnyProblem(PlanetProblems.FIRE_BRIGADE)) {
			os.append(", ").append(env.labels().get("buildings.fire_brigade"));
		}
		if (ps.hasAnyProblem(PlanetProblems.COLONY_HUB)) {
			os.append(", ").append(env.labels().get("buildings.colony_hub"));
		}
		if (ps.hasAnyProblem(PlanetProblems.STADIUM)) {
			os.append(", ").append(env.labels().get("buildings.stadium"));
		}
		if (ps.hasAnyProblem(PlanetProblems.VIRUS)) {
			os.append(", ").append(env.labels().get("virus_infection"));
		}
		
		if (os.length() > 0) {
			os.replace(0, 2, "");
		}
		return os.toString();
	}
	/**
	 * Locate a fleet with the given ID.
	 * @param id the fleet unique id
	 * @return the fleet or null if not found
	 */
	public Fleet findFleet(int id) {
		for (Player p : players.values()) {
			for (Fleet f : p.fleets.keySet()) {
				if (f.id == id) {
					return f;
				}
			}
		}
		return null;
	}
	/**
	 * Construct a new message without target and only the label.
	 * @param text the label identifier
	 * @return the message
	 */
	public Message newMessage(String text) {
		Message message = new Message();
		message.gametime = time.getTimeInMillis();
		message.timestamp = System.currentTimeMillis();
		message.text = text;
		return message;
	}
	/** @return Compute the maximum test points. */
	public int testMax() {
		int sum = 0;
		for (TestQuestion tq : test.values()) {
			int max = 0;
			for (TestAnswer ta : tq.answers) {
				max = Math.max(max, ta.points);
			}
			sum += max;
		}
		return sum;
	}
	/** @return compute the test score based on the user selections. */
	public int testScore() {
		int sum = 0;
		for (TestQuestion tq : test.values()) {
			for (TestAnswer ta : tq.answers) {
				if (ta.selected) {
					sum += ta.points;
				}
			}
		}
		return sum;
		
	}
	/**
	 * Process the battle definition XML.
	 * @param xbattle the battle definition
	 */
	void processBattle(XElement xbattle) {
		for (XElement xproj : xbattle.childElement("projectiles").childrenWithName("projectile")) {
			BattleProjectile bp = new BattleProjectile();
			
			bp.id = xproj.get("id");
			int nx = xproj.getInt("width");
			int ny = xproj.getInt("height");
			
			BufferedImage m = rl.getImage(xproj.get("matrix"));
			bp.matrix = ImageUtils.split(m, m.getWidth() / nx, m.getHeight() / ny);
			trimTransparencyOnSides(bp.matrix);
			if (xproj.has("alternative")) {
				m = rl.getImage(xproj.get("alternative"));
				bp.alternative = ImageUtils.split(m, m.getWidth() / nx, m.getHeight() / ny);
				trimTransparencyOnSides(bp.alternative);
			} else {
				bp.alternative = bp.matrix;
			}
			if (xproj.has("sound")) {
				bp.sound = SoundType.valueOf(xproj.get("sound"));
			}
			bp.baseDamage = xproj.getInt("damage");
			bp.range = xproj.getInt("range");
			bp.delay = xproj.getInt("delay");
			if (xproj.has("area")) {
				bp.area = xproj.getInt("area");
			} else {
				bp.area = 1;
			}
			bp.mode = BattleProjectile.Mode.valueOf(xproj.get("mode"));
			
			bp.movementSpeed = xproj.getInt("movement-speed");
			if (xproj.has("rotation-time")) {
				bp.rotationTime = xproj.getFloat("rotation-time");
			}
			
			battle.projectiles.put(bp.id, bp);
			
		}
		for (XElement xspace : xbattle.childElement("space-entities").childrenWithName("tech")) {
			String id = xspace.get("id");
			int nx = xspace.getInt("width");
			
			BattleSpaceEntity se = new BattleSpaceEntity();
			
			BufferedImage ni = rl.getImage(xspace.get("normal"));
			se.normal = ImageUtils.splitByWidth(ni, ni.getWidth() / nx);
			
//			trimTransparencyOnSides(se.normal);
			if (xspace.has("alternative")) {
				BufferedImage ai = rl.getImage(xspace.get("alternative"));
				se.alternative = ImageUtils.splitByWidth(ai, ai.getWidth() / nx);
//				trimTransparencyOnSides(se.alternative);
			} else {
				se.alternative = se.normal;
			}
			
			se.infoImageName = xspace.get("image");
			se.destruction = SoundType.valueOf(xspace.get("sound"));
			if (xspace.has("movement-speed")) {
				se.movementSpeed = xspace.getInt("movement-speed");
			}
			if (xspace.has("rotation-time")) {
				se.rotationTime = xspace.getInt("rotation-time");
			}
			
			se.hp = xspace.getInt("hp");
			if (skirmishDefinition != null && xspace.has("hp-skirmish")) {
				se.hp = xspace.getInt("hp-skirmish");
			}
			
			ResearchType rt = researches.get(id);
			if (rt != null) {
				// if no equipment image, use the first frame of the rotation
				if (rt.equipmentImage == null) {
					rt.equipmentImage = ImageUtils.cutTransparentBorder(se.normal[0]);
				}
			} else {
				Exceptions.add(new AssertionError("Warning: Missing technology referenced by battle.xml: " + id));
			}
			
			loadEfficiencyModel(se.efficiencies, xspace);
			
			battle.spaceEntities.put(id, se);
		}
		for (XElement xdefense : xbattle.childElement("ground-projectors").childrenWithName("tech")) {
			String id = xdefense.get("id");
			int nx = xdefense.getInt("width");

			BattleGroundProjector se = new BattleGroundProjector();
			
			BufferedImage ni = rl.getImage(xdefense.get("normal"));
			se.normal = ImageUtils.splitByWidth(ni, ni.getWidth() / nx);
			trimTransparencyOnSides(se.normal);
			
			if (xdefense.has("alternative")) {
				BufferedImage ai = rl.getImage(xdefense.get("alternative"));
				se.alternative = ImageUtils.splitByWidth(ai, ai.getWidth() / nx);
				trimTransparencyOnSides(se.alternative);
			} else {
				se.alternative = se.normal;
			}
			se.infoImageName = xdefense.get("image");
			if (xdefense.has("sound")) {
				se.destruction = SoundType.valueOf(xdefense.get("sound"));
			} else {
				System.err.println("Missing sound for " + id);
			}
			se.projectile = xdefense.get("projectile");
			se.rotationTime = xdefense.getInt("rotation-time");
			se.baseDamage = xdefense.getInt("damage");
			
			// load efficiency model
			loadEfficiencyModel(se.efficiencies, xdefense);
			
			battle.groundProjectors.put(id, se);
		}
		for (XElement xdefense : xbattle.childElement("ground-shields").childrenWithName("tech")) {
			String id = xdefense.get("id");

			BattleGroundShield se = new BattleGroundShield();
			
			BufferedImage ni = rl.getImage(xdefense.get("normal"));
			se.normal = ni;
			
			if (xdefense.has("alternative")) {
				BufferedImage ai = rl.getImage(xdefense.get("alternative"));
				se.alternative = ai;
			} else {
				se.alternative = se.normal;
			}
			se.infoImageName = xdefense.get("image");
			if (xdefense.has("sound")) {
				se.destruction = SoundType.valueOf(xdefense.get("sound"));
			} else {
				System.err.println("Missing sound for " + id);
			}
			se.shields = xdefense.getInt("shield");
			
			battle.groundShields.put(id, se);
		}
		for (XElement xlayout : xbattle.childElement("layouts").childrenWithName("layout")) {
			BattleSpaceLayout ly = new BattleSpaceLayout(rl.getImage(xlayout.get("map")));
			ly.parse();
			
			battle.layouts.add(ly);
		}
		for (XElement xground : xbattle.childElement("ground-vehicles").childrenWithName("tech")) {
			String id = xground.get("id");
			int nx = xground.getInt("width");
			int ny = xground.getInt("height");
			BattleGroundVehicle ge = new BattleGroundVehicle();

			ge.id = id;
			
			BufferedImage ni = rl.getImage(xground.get("normal"));
			ge.normal = ImageUtils.split(ni, ni.getWidth() / nx, ni.getHeight() / ny);
			ge.width = ni.getWidth() / nx;
			ge.height = ni.getHeight() / ny;
			
			trimTransparencyOnSides(ge.normal);
			
			if (xground.has("alternative")) {
				BufferedImage ai = rl.getImage(xground.get("alternative"));
				ge.alternative = ImageUtils.split(ai, ai.getWidth() / nx, ai.getHeight() / ny);
				trimTransparencyOnSides(ge.normal);
			} else {
				ge.alternative = ge.normal;
			}
			ge.destroy = SoundType.valueOf(xground.get("destroy"));
			if (xground.has("fire")) {
				ge.fire = SoundType.valueOf(xground.get("fire"));
			}
			
			ge.hp = xground.getInt("hp");
			
			ge.baseDamage = xground.getInt("damage");
			ge.type = GroundwarUnitType.valueOf(xground.get("type"));
			ge.minRange = Double.parseDouble(xground.get("min-range"));
			ge.maxRange = Double.parseDouble(xground.get("max-range"));
			ge.area = xground.getInt("area");
			ge.movementSpeed = xground.getInt("movement-speed");
			ge.rotationTime = xground.getInt("rotation-time");
			ge.delay = xground.getInt("delay");
			if (xground.has("repair-time")) {
				ge.selfRepairTime = xground.getInt("repair-time");
			}

			ResearchType rt = researches.get(id);
			// if no equipment image, use the first frame of the rotation
			if (rt.equipmentImage == null) {
				rt.equipmentImage = ge.normal[0][0];
			}

			battle.groundEntities.put(id, ge);
		}
		Map<String, BufferedImage[][]> matrices = new HashMap<>();
		for (XElement xmatrix : xbattle.childElement("buildings").childrenWithName("matrix")) {
			int nx = xmatrix.getInt("width");
			int ny = xmatrix.getInt("height");
			BufferedImage m = rl.getImage(xmatrix.get("image"));
			BufferedImage[][] matrix = ImageUtils.split(m, m.getWidth() / nx, m.getHeight() / ny);
			String id = xmatrix.get("id");
			matrices.put(id, matrix);
		}
		for (XElement xturret : xbattle.childElement("buildings").childrenWithName("building-turret")) {
			for (XElement xrace : xturret.childrenWithName("race")) {
				String rid = xrace.get("id");
				for (XElement xport : xrace.childrenWithName("port")) {
					BattleGroundTurret tr = new BattleGroundTurret();
					tr.rx = xport.getInt("rx");
					tr.ry = xport.getInt("ry");
					tr.px = xport.getInt("px");
					tr.py = xport.getInt("py");
					
					tr.fire = SoundType.valueOf(xport.get("fire"));
					tr.maxRange = Double.parseDouble(xport.get("max-range"));
					tr.baseDamage = xport.getInt("damage");
					tr.rotationTime = xport.getInt("rotation-time");
					tr.delay = xport.getInt("delay");
					
					String id = xturret.get("id");
					
					String mid = xport.get("matrix");
					tr.matrix = matrices.get(mid);
					if (tr.matrix == null) {
						System.err.printf("Missing matrix: %s%n", mid);
					}
					battle.addTurret(id, rid, tr);
				}
			}
		}
		// the building hitpoints
		for (XElement xhp : xbattle.childElement("buildings").childrenWithName("hitpoints")) {
			String id = xhp.get("id");
			String player = xhp.get("player", null);
			battle.groundHitpoints.put(Pair.of(id, player), xhp.getInt("ground"));
			int hp = xhp.getInt("space");
			if (skirmishDefinition != null && xhp.has("space-skirmish")) {
				hp = xhp.getInt("space-skirmish");
			}
			battle.spaceHitpoints.put(Pair.of(id, player), hp);
		}
		for (XElement xprops : xbattle.childrenWithName("properties")) {
			for (XElement xprop : xprops.childrenWithName("property")) {
				String id = xprop.get("id");
				String player = xprop.get("player", null);
				for (Map.Entry<String, String> e : xprop.attributes().entrySet()) {
					String n = e.getKey();
					String v = e.getValue();
					if (!n.equals("id") && !n.equals("player")) {
						battle.addProperty(id, player, n, v);
					}
				}
			}
		}
		
		for (XElement xvss : xbattle.childrenWithName("ecm-vs-matrix")) {
			Difficulty diff = Difficulty.valueOf(xvss.get("difficulty"));
			Map<Pair<Integer, Integer>, Double> matrix = new HashMap<>();
			battle.ecmMatrix.put(diff, matrix);
			if (xvss.has("backfire")) {
				battle.backfires.put(diff, xvss.getDoubleObject("backfire"));
			}
			for (XElement xvs : xvss.childrenWithName("vs")) {
				int a = xvs.getInt("anti-ecm");
				int e = xvs.getInt("ecm");
				double v = xvs.getDouble("value");
				matrix.put(Pair.of(a, e), v);
			}
		}
		
		for (ExplosionType et : ExplosionType.values()) {
			BufferedImage img = rl.getImage(et.image);
			battle.groundExplosions.put(et, ImageUtils.splitByWidth(img, img.getWidth() / et.frames));
		}
		
		BufferedImage rimg = rl.getImage("inventions/weapons/vehicles/rocket_matrix");
		battle.groundRocket = ImageUtils.split(rimg, rimg.getWidth() / 16, rimg.getHeight() / 3);

	}
	/**
	 * Load a list of efficiency settings from the given parent XML node.
	 * @param out the output list
	 * @param parent the parent XML where the "efficiency" nodes are
	 */
	void loadEfficiencyModel(List<? super BattleEfficiencyModel> out, XElement parent) {
		for (XElement xeff : parent.childrenWithName("efficiency")) {
			BattleEfficiencyModel bem = new BattleEfficiencyModel();
			
			bem.id = xeff.get("id", null);
			if (xeff.has("category")) {
				bem.category = ResearchSubCategory.valueOf(xeff.get("category"));
			}
			bem.owner = xeff.get("owner", null);
			
			bem.damageMultiplier = xeff.getDouble("damage-multiplier");
			
			out.add(bem);
		}
	}
	/**
	 * Trim the image items symmetrically to free up some unnecessary memory
	 * and rendering time.
	 * @param images the image matrix
	 */
	void trimTransparencyOnSides(BufferedImage[][] images) {
		for (BufferedImage[] img : images) {
			trimTransparencyOnSides(img);
		}
	}
	/**
	 * Trim the image items symmetrically to free up some unnecessary memory
	 * and rendering time.
	 * @param images the image arrays
	 */
	void trimTransparencyOnSides(BufferedImage[] images) {
		for (int i = 0; i < images.length; i++) {
			BufferedImage img = images[i];

			int tx = 0;
			int ty = 0;
			
			int w = img.getWidth();
			int h = img.getHeight();
			
			// horizontal
			outer:
			for (int x = 0; x < w / 2; x++) {
				for (int y = 0; y < h; y++) {
					int c = img.getRGB(x, y);
					if ((c & 0xFF000000) != 0) {
						tx = x - 1;
						break outer;
					}
					c = img.getRGB(w - x - 1, y);
					if ((c & 0xFF000000) != 0) {
						tx = x - 1;
						break outer;
					}
				}
			}

			// vertical
			outer2:
			for (int y = 0; y < h / 2; y++) {
				for (int x = 0; x < w; x++) {
					int c = img.getRGB(x, y);
					if ((c & 0xFF000000) != 0) {
						ty = y - 1;
						break outer2;
					}
					c = img.getRGB(x, h - y - 1);
					if ((c & 0xFF000000) != 0) {
						ty = y - 1;
						break outer2;
					}
				}
			}
			tx = Math.max(0, tx);
			ty = Math.max(0, ty);
			if (tx > 0 || ty > 0) {
				images[i] = ImageUtils.newSubimage(img, tx, ty, w - 2 * tx, h - 2 * ty);
			}
		}
	}
	/**
	 * Compute the distance square between two points.
	 * @param x1 the first X
	 * @param y1 the first Y
	 * @param x2 the second X
	 * @param y2 the second Y
	 * @return the distance square
	 */
	public static double dist(double x1, double y1, double x2, double y2) {
		return (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);
	}
	/**
	 * Remove the specified fleet from existence of all player's knowledge.
	 * @param fleet the fleet
	 */
	public void removeFleet(Fleet fleet) {
		fleets.remove(fleet.id);
		infectedFleets.remove(fleet.id);
		for (Player p : players.values()) {
			p.fleets.remove(fleet);
			if (p.currentFleet == fleet) {
				p.currentFleet = null;
				p.selectionMode = SelectionMode.PLANET;
				if (p == fleet.owner) {
					List<Fleet> of = p.ownFleets();
					if (of.size() > 0) {
						p.currentFleet = of.iterator().next();
					}
				}
			}
			// if someone targeted this fleet
			for (Fleet f : p.fleets.keySet()) {
				if (f.owner == p) {
					if (f.targetFleet == fleet) {
						// change it to move to its last known location
						f.targetFleet = null;
						f.mode = FleetMode.MOVE;
						f.waypoints.clear();
						f.waypoints.add(new Point2D.Double(fleet.x, fleet.y));
					}
				}
			}
		}
	}
	/**
	 * Returns the hitpoints of the given research type.
	 * @param rt the research type
	 * @param owner the owner
	 * @return the hitpoints
	 */
	public int getHitpoints(ResearchType rt, Player owner) {
		Trait sd = owner.traits.trait(TraitKind.SPACE_DEFENSE);
		double hpMultiply = 1;
		
		if (sd != null && (rt.category == ResearchSubCategory.SPACESHIPS_STATIONS)) {
			hpMultiply = 1 + sd.value / 100;
		}
		BattleSpaceEntity se = battle.spaceEntities.get(rt.id);
		if (se != null) {
			return (int)(se.hp * hpMultiply);
		}
		BattleGroundVehicle e = battle.groundEntities.get(rt.id);
		if (e != null) {
			return (int)(e.hp * hpMultiply);
		}
		return (int)(rt.productionCost * hpMultiply / params().costToHitpoints());
	}
	/**
	 * Returns the hitpoints of the given building type.
	 * @param rt the research type
	 * @param owner the building owner
	 * @param space the space hitpoints?
	 * @return the hitpoints
	 */
	public int getHitpoints(BuildingType rt, Player owner, boolean space) {
		Trait sd = owner.traits.trait(TraitKind.SPACE_DEFENSE);
		double hpMultiply = 1;
		
		if (sd != null && (rt.kind.equals("Gun") || rt.kind.equals("Shield"))) {
			hpMultiply = 1 + sd.value / 100;
		}
		
		Map<Pair<String, String>, Integer> map = space ? battle.spaceHitpoints : battle.groundHitpoints;
		Integer hp = map.get(Pair.of(rt.id, owner.id));
		if (hp != null) {
			return (int)(hp * hpMultiply);
		}
		hp = map.get(Pair.of(rt.id, (Integer)null));
		if (hp != null) {
			return (int)(hp * hpMultiply);
		}
		return (int)(rt.hitpoints * hpMultiply / params().costToHitpoints());
	}
	/**
	 * Calculate the current fleet health.
	 * @param f the fleet
	 * @return the health percent 0..1
	 */
	public double fleetHealth(Fleet f) {
		if (f.inventory.isEmpty()) {
			return 0.0;
		}
		double max = 0;
		double hp = 0;
		for (InventoryItem fi : f.inventory.iterable()) {
			max += getHitpoints(fi.type, fi.owner);
			int s = fi.shieldMax();
			if (s >= 0) {
				max += s;
			}
			hp += fi.hp + fi.shield;
		}
		return hp / max;
	}
	/**
	 * Returns the colony health in percentages.
	 * @param p the planet
	 * @return the colony health percentage of 0..1
	 */
	public double colonyHealth(Planet p) {
		if (p.surface.buildings.isEmpty()) {
			return 0.0;
		}
		double max = 0;
		double hp = 0;
		for (Building b : p.surface.buildings.iterable()) {
			max += getHitpoints(b.type, p.owner, false);
			hp += b.hitpoints;
		}
		return hp / max;
	}
	/**
	 * Check if the given player should be considered for AI computations or not.
	 * <p>For example, trader's and pirate's objects may be safely ignored.</p>
	 * @param player the target player to test
	 * @return true if can be accepted
	 */
	public boolean aiAccept(Player player) {
		return player != null && player.aiMode != AIMode.TRADERS && player.aiMode != AIMode.PIRATES;
	}
	/**
	 * @return the various game parameters
	 */
	public Parameters params() {
		return this.params;
	}
	/**
	 * Cure fleets which were infected by the given planet.
	 * @param planet the planet
	 */
	public void cureFleets(Planet planet) {
		Iterator<Map.Entry<Integer, String>> it = infectedFleets.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, String> e = it.next();
			if (planet.id.equals(e.getValue())) {
				it.remove();
			}
		}
	}
	/**
	 * Retrieve a diplomatic relation record between two parties.
	 * @param first the first party
	 * @param second the second party
	 * @return the relation or null if no two-sided relation exists
	 */
	public DiplomaticRelation getRelation(Player first, Player second) {
		if (first != null && second != null) {
			for (DiplomaticRelation dr : relations) {
				if ((dr.first.equals(first.id) && dr.second.equals(second.id)) 
						|| (dr.first.equals(second.id) && dr.second.equals(first.id))) {
					return dr;
				}
			}
		}
		return null;
	}
	/**
	 * Establish or fill a relation between two parties.
	 * @param first the first player
	 * @param second the second player
	 * @return the existing or newly created relation
	 */
	public DiplomaticRelation establishRelation(Player first, Player second) {
		DiplomaticRelation dr = getRelation(first, second);
		if (dr != null) {
			// establish full relation
			dr.full |= dr.second.equals(first.id);
		}  else {
			dr = createDiplomaticRelation(first, second);
		}
		return dr;
	}
	/**
	 * Create a new diplomatic relation entry.
	 * @param first the first party
	 * @param second the second party
	 * @return the new relation
	 */
	DiplomaticRelation createDiplomaticRelation(Player first, Player second) {
		DiplomaticRelation dr = new DiplomaticRelation();
		dr.first = first.id;
		dr.second = second.id;
		dr.value = (first.initialStance + second.initialStance) / 2d;
		relations.add(dr);
		return dr;
	}
	/**
	 * Save the diplomatic relations.
	 * @param xworld the output XML
	 */
	void saveDiplomaticRelations(XElement xworld) {
		XElement xrels = xworld.add("relations");
		for (DiplomaticRelation dr : relations) {
			XElement xrel = xrels.add("relation");
			xrel.set("first", dr.first);
			xrel.set("second", dr.second);
			xrel.set("full", dr.full);
			xrel.set("trade-agreement", dr.tradeAgreement);
			xrel.set("strong-alliance", dr.strongAlliance);
			xrel.set("wont-talk", dr.wontTalk());
			if (dr.lastContact != null) {
				xrel.set("last-contact", XElement.formatDateTime(dr.lastContact));
			}
			xrel.set("value", dr.value);
			
			StringBuilder sb = new StringBuilder();
			
			for (String p2 : dr.alliancesAgainst) {
				if (sb.length() > 0) {
					sb.append(",");
				}
				sb.append(p2);
			}
			
			xrel.set("ally-against", sb.toString());
		}
	}
	/**
	 * Loads the diplomatic relations from the world.
	 * @param xworld the world XML
	 */
	void loadDiplomaticRelations(XElement xworld) {
		relations.clear();
		// load diplomatic relations
		for (XElement xrels : xworld.childrenWithName("relations")) {
			for (XElement xrel : xrels.childrenWithName("relation")) {
				String first = xrel.get("first");
				String second = xrel.get("second");
				
				Player pfirst = players.get(first);
				if (pfirst == null) {
					System.out.println("WARNING: DiplomaticRelation.first unknown: " + xrel);
				}
				Player psecond = players.get(second);
				if (psecond == null) {
					System.out.println("WARNING: DiplomaticRelation.second unknown: " + xrel);
				}
				
				if (pfirst == psecond) {
					System.out.println("WARNING: DiplomaticRelation.party equals: " + xrel);
				}
				
				// skip bogous relation entries
				if (pfirst == null || psecond == null || pfirst == psecond) {
					continue;
				}
				
				DiplomaticRelation dr = establishRelation(pfirst, psecond);
				
				dr.full = xrel.getBoolean("full");
				dr.value = Math.max(0, Math.min(100, xrel.getDouble("value")));
				dr.tradeAgreement = xrel.getBoolean("trade-agreement", false);
				dr.strongAlliance = xrel.getBoolean("strong-alliance", false);
				
				dr.wontTalk(xrel.getBoolean("wont-talk"));
				String lc = xrel.get("last-contact", null);
				if (lc != null) {
					try {
						dr.lastContact = XElement.parseDateTime(lc);
					} catch (ParseException ex) {
						Exceptions.add(ex);
					}
				}
				String ally = xrel.get("ally-against", "");
				for (String aa : ally.split("\\s*,\\s*")) {
					if (!aa.isEmpty()) {
						Player p2 = players.get(aa);
						if (p2 == null) {
							throw new AssertionError("DiplomaticRelation.allyAgainst unknown: " + xrel);
						}
						if (p2 == pfirst || p2 == psecond) {
							throw new AssertionError("DiplomaticRelation.allyAgainstSelf: " + xrel);
						}
						dr.alliancesAgainst.add(p2.id);
					}
				}
			}
		}
	}
	/**
	 * Grant an achievement with the given ID if not already awarded.
	 * @param a the achievement id, e.g., "achievement.i_robot"
	 */
	public void achievement(String a) {
		if (!env.profile().hasAchievement(a)) {
			env.achievementQueue().add(a);
			env.profile().grantAchievement(a);
		}
	}
	/**
	 * Apply traits of the players.
	 */
	public void applyTraits() {
		for (Player p : players.values()) {
			// remove warp technology
			if (p.traits.has(TraitKind.PRE_WARP)) {
				// remove hyperdrive as equipment
				for (Fleet f : p.ownFleets()) {
					for (InventoryItem ii : f.inventory.iterable()) {
						for (InventorySlot is : ii.slots.values()) {
							if (is.type != null && is.type.has(ResearchType.PARAMETER_SPEED)) {
								is.type = null;
								is.count = 0;
							}
						}
					}
				}
				// remove technology
				for (ResearchType at : U.newArrayList(p.available())) {
					for (ResearchType rt1 : U.newArrayList(p.availableResearch.get(at))) {
						if (rt1.has(ResearchType.PARAMETER_SPEED) /* && rt1.level == 0*/) {
							p.availableResearch.get(at).remove(rt1);
						}
					}
					if (at.has(ResearchType.PARAMETER_SPEED)) {
						at.level = Math.max(at.level, 1);
						p.availableResearch.remove(at);
					}
				}
			}
			if (p.traits.has(TraitKind.ASTRONOMER)) {
				for (Planet pl : U.newArrayList(p.ownPlanets())) {
					List<Planet> pls = planetsFromPlanet(pl);
					for (Planet pl2 : pls) {
						if (pl2.owner != p && !p.planets.containsKey(pl2)) {
							p.planets.put(pl2, PlanetKnowledge.NAME);
							break;
						}
					}
				}
			}
			if (p.traits.has(TraitKind.ASTRONOMER_PLUS)) {
				int cnt = Math.max(planets.planets.size() - p.ownPlanets().size(), 0);
				p.changeInventoryCount(researches.get("Satellite"), cnt);
				
				for (Planet pl : planets.values()) {
					if (pl.owner != p && !p.planets.containsKey(pl)) {
						p.planets.put(pl, PlanetKnowledge.NAME);
					}
				}
			}
		}
	}
	/**
	 * Create a sorted list of planets based on their distance from the center planet.
	 * @param center the center
	 * @return the list of planets
	 */
	List<Planet> planetsFromPlanet(final Planet center) {
		List<Planet> result = U.newArrayList(planets.values());
		result.remove(center);
		
		Collections.sort(result, new Comparator<Planet>() {
			@Override
			public int compare(Planet o1, Planet o2) {
				double d1 = Point.distance(o1.x, o1.y, center.x, center.y);
				double d2 = Point.distance(o2.x, o2.y, center.x, center.y);
				return Double.compare(d1, d2);
			}
		});
		
		return result;
	}
	/**
	 * Clears all game structures before restarting.
	 */
	protected void cleanup() {
		player = null;
		players.clear();
		planets.clear();
		fleets.clear();
		researches.clear();
		buildingModel = null;
		galaxyModel = null;
		battle = null;
		bridge = null;
		chats = null;
		test = null;
		testCompleted = false;
		testNeeded = false;
		diplomacy = null;
		idSequence.set(0);
		currentTalk = null;
		infectedFleets.clear();
		time.setTime(initialDate);
		messageRecording = false;
		allowRecordMessage = false;
		pendingBattles.clear();
		receivedMessages.clear();
		relations.clear();
		talks = null;
		statistics.clear();
		walks = null;
		
		
		scripting = null;
	}
	/**
	 * Initializes the world via the loadCampaign, then performs the alterations.
	 * @param rl the resource locator
	 * @param scripting the scripting to use
	 */
	public void loadSkirmish(final ResourceLocator rl,
			GameScripting scripting) {
		cleanup();
		
		definition = skirmishDefinition.createDefinition(rl);
		labels = new Labels();
		labels.load(rl, U.startWith(definition.labels, "labels"));
		difficulty = skirmishDefinition.initialDifficulty;
		
		loadCampaign(rl);
		
		prepareLayout();
		
		// fix players
		Map<Player, Integer> groups = new HashMap<>();
		
		Map<String, Player> originalPlayers = new HashMap<>(players.players);
		players.players.clear();
		
		player = null;
		int id = 0;
		Map<Player, SkirmishPlayer> createdPlayers = new HashMap<>();
		
		for (SkirmishPlayer sp : skirmishDefinition.players) {
			Player p = new Player(this, sp.originalId + "-" + id);

			createdPlayers.put(p, sp);
			
			if (sp.ai == SkirmishAIMode.USER) {
				player = p;
			}
			switch (sp.ai) {
			case AI_EASY:
				p.aiMode = AIMode.DEFAULT;
				p.difficulty = Difficulty.EASY;
				break;
			case AI_HARD:
				p.aiMode = AIMode.DEFAULT;
				p.difficulty = Difficulty.HARD;
				break;
			case AI_NORMAL:
				p.aiMode = AIMode.DEFAULT;
				p.difficulty = Difficulty.NORMAL;
				break;
			case PIRATE:
				p.aiMode = AIMode.PIRATES;
				p.difficulty = difficulty;
				break;
			case TRADER:
				p.aiMode = AIMode.TRADERS;
				p.difficulty = difficulty;
				break;
			case USER:
				p.aiMode = AIMode.NONE;
				p.difficulty = difficulty;
				break;
			default:
				break;
			
			}
			p.name = sp.description;
			p.fleetIcon = rl.getImage(sp.iconRef);
			p.color = sp.iconColor(p.fleetIcon);
			p.money(skirmishDefinition.initialMoney);
			p.noDatabase = sp.nodatabase;
			p.noDiplomacy = sp.nodiplomacy;
			p.diplomacyHead = sp.diplomacyHead;
			p.race = sp.race;
			p.shortName = sp.name;
			if (sp.picture != null) {
				p.picture = rl.getImage(sp.picture);
			}

			p.group = sp.group;
			p.taxBase = skirmishDefinition.taxBase;
			p.taxScale = skirmishDefinition.taxScale;
			
			groups.put(p, sp.group);
			
			
			p.traits.replace(sp.traits);
			if (sp.ai == SkirmishAIMode.USER) {
				definition.traits = new Traits();
				definition.traits.replace(sp.traits);
			}

			// fix original pre-enabled tech
			Player op = originalPlayers.get(sp.originalId);
			Diplomacy odo = diplomacy.get(sp.originalId);
			if (odo != null) {
				diplomacy.put(p.id, odo);
			}
			
			p.nicknames.addAll(op.nicknames);
			
			Set<String> opExcept = U.newSet("ColonyShip");
			// enable tech originally inteded by the definition
			for (ResearchType rt : op.available()) {
				if (!opExcept.contains(rt.id)) {
					p.add(rt);
				}
			}
			
			for (ResearchType rt : researches.researches.values()) {
				if (rt.race.contains(p.race) && rt.level <= definition.techLevel) {
					p.add(rt);
				}
				rt.nobuild = false;
			}
			
			for (ResearchType rt : p.available()) {
				p.setRelated(rt);
			}

			if (p.aiMode != AIMode.PIRATES && p.aiMode != AIMode.TRADERS) {
				// create initial fleets
				p.changeInventoryCount(researches.get("ColonyShip"), Math.max(0, skirmishDefinition.initialColonyShips - 1));
				p.changeInventoryCount(researches.get("OrbitalFactory"), Math.max(0, skirmishDefinition.initialOrbitalFactories - 1));
	
				if (skirmishDefinition.grantColonyShip) {
					p.setAvailable(researches.get("ColonyShip"));
				}
				if (skirmishDefinition.grantOrbitalFactory) {
					p.setAvailable(researches.get("OrbitalFactory"));
				}
				if (skirmishDefinition.initialColonyShips > 0) {
					ResearchType rt = researches.get("ColonyShip");
					Fleet f = new Fleet(p);
					f.name(labels.get("@Colonizer"));
					p.changeInventoryCount(rt, 1);
					List<InventoryItem> iis = f.deployItem(rt, f.owner, 1);
					fleetAddRadar(p, iis);
					
					// add as many satellites as there were colony ships to help jumpstart first colonizations
					rt = researches.get("Satellite");
					if (rt != null) {
						p.changeInventoryCount(rt, skirmishDefinition.initialColonyShips);
					}
				}
				
				createStartingFleet(p);
			}

			players.players.put(p.id, p);
			id++;
		}
		
		if (player == null) {
			player = ModelUtils.random(players.values());
		}
		
		int labLimit = Math.max(skirmishDefinition.initialPlanets, skirmishDefinition.initialColonyShips);
		
		for (BuildingType bt : buildingModel.buildings.values()) {
			if (!bt.skirmishHardLimit) {
				if (bt.kind.equals("Science") && skirmishDefinition.noLabLimit) {
					bt.limit = Integer.MAX_VALUE;
				} else
				if (bt.kind.equals("Factory") && skirmishDefinition.noFactoryLimit) {
					bt.limit = Integer.MAX_VALUE;
				} else
			    if (bt.kind.equals("Economic") && skirmishDefinition.noEconomicLimit) {
			    	bt.limit = Integer.MAX_VALUE;
			    }
			}
		}
		// fix research requirements of colony ship and orbital factory
		for (ResearchType rt : Arrays.asList(researches.get("ColonyShip"), researches.get("OrbitalFactory"))) {
			rt.civilLab = labLimit > 0 ? 1 : 0;
			rt.mechLab = labLimit > 1 ? 1 : 0;
			rt.compLab = labLimit > 2 ? 1 : 0;
			rt.aiLab = labLimit > 3 ? 1 : 0;
			rt.milLab = labLimit > 4 ? 1 : 0;
			rt.prerequisites.clear();
		}
		
		establishDiplomacy(groups);
		// strip planets.
		for (Planet p : planets.values()) {
			if (p.owner != null) {
				p.die();
			}
			if (skirmishDefinition.galaxyRandomSurface) {
				p.type = ModelUtils.random(galaxyModel.planetTypes.values());
				p.surface = ModelUtils.random(p.type.surfaces.values()).copy(newIdFunc);
			}
		}

		assignPlanetsToPlayers(createdPlayers);
		
		for (Player p : players.values()) {
			p.ai = env.getAI(p);
			p.ai.init(p);
		}

		applyTraits();

		for (Player p : players.values()) {
			p.populateProductionHistory();
		}

		this.scripting = scripting;
		this.scripting.init(player, null);
	}
	/**
	 * Prepare the galaxy layout.
	 */
	void prepareLayout() {
		if (skirmishDefinition.galaxyRandomLayout) {
			List<Planet> modelPlanets = new ArrayList<>(planets.values());
			int count = modelPlanets.size();
			if (skirmishDefinition.galaxyCustomPlanets) {
				count = skirmishDefinition.galaxyPlanetCount;
			}
			
			GalaxyGenerator gg = new GalaxyGenerator();
			gg.setSeed(skirmishDefinition.galaxyRandomSeed);
			gg.setPlanetCount(count);
			gg.setSize(this.galaxyModel.map.getWidth(), this.galaxyModel.map.getHeight());
			gg.setMinDensity(MIN_PLANET_DENSITY);
			gg.setSingleNames(this.galaxyModel.singleNames);
			gg.setMultiNames(this.galaxyModel.multiNames);
			
			List<PlanetCandidate> pcs = gg.generate();
			
			int j = 0;
			for (PlanetCandidate pc : pcs) {
				
				Planet p0 = modelPlanets.get(j);
				pc.type = p0.type.type;
				pc.variant = p0.surface.variant;
				
				j = (j + 1) % modelPlanets.size();
			}
			
			planets.clear();
			
			for (PlanetCandidate pc : pcs) {
				Planet p = new Planet(pc.id, this);
				p.name0 = pc.name;
				p.x = pc.x;
				p.y = pc.y;
				p.diameter = pc.r;
				p.type = galaxyModel.planetTypes.get(pc.type);
				p.surface = p.type.surfaces.get(pc.variant).copy(newIdFunc);
				
				planets.put(p.id, p);
			}
		}
		int pscale = skirmishDefinition.planetScale;
		if (pscale > 1) {
			for (PlanetType pt : galaxyModel.planetTypes.values()) {
				for (PlanetSurface ps : pt.surfaces.values()) {
					int w0 = ps.width;
					int h0 = ps.height;

					List<SurfaceFeature> features = ps.features;
					Map<Location, SurfaceEntity> map = ps.basemap;
					ps.basemap = new HashMap<>(map.size() * pscale * pscale);
					ps.features = new ArrayList<>(features.size() * pscale * pscale);
					
					for (int i = 0; i < pscale; i++) {
						for (int j = 0; j < pscale; j++) {
							for (Map.Entry<Location, SurfaceEntity> e : map.entrySet()) {
								if (i == 0 && j == 0) {
									ps.basemap.put(e.getKey(), e.getValue());
								} else {
									Location loc = e.getKey();
									int x2 = loc.x + i * w0 - j * h0;
									int y2 = loc.y - i * w0 - j * h0;
									
									Location loc2 = Location.of(x2, y2);
									ps.basemap.put(loc2, e.getValue());
								}
							}
							for (SurfaceFeature sf : features) {
								if (i == 0 && j == 0) {
									ps.features.add(sf);
								} else {
									SurfaceFeature sf2 = sf.copy();
									
									Location loc = sf2.location;
									int x2 = loc.x + i * w0 - j * h0;
									int y2 = loc.y - i * w0 - j * h0;
									
									sf2.location = Location.of(x2, y2);
									
									ps.features.add(sf2);
								}
							}
							if (i > 0) {
								int kmax = h0;
								if (j + 1 == pscale) {
									kmax--;
								}
								for (int k = 0; k < kmax; k++) {
									int x3 = i * w0 - j * h0 - k - 1;
									int y3 = - i * w0 - j * h0 - k;
									
									Location loc3 = Location.of(x3, y3);
									if (!ps.basemap.containsKey(loc3)) {
										Location loc4 = Location.of(x3 + 1, y3);
										SurfaceEntity se = ps.basemap.get(loc4);
										ps.basemap.put(loc3, se);
									}
								}
							}
							if (j > 0) {
								int kmax = w0;
								if (i + 1 == pscale) {
									kmax--;
								}
								for (int k = 0; k < kmax; k++) {
									int x3 = i * w0 - j * h0 + k + 1;
									int y3 = - i * w0 - j * h0 - k;
									
									Location loc3 = Location.of(x3, y3);
									if (!ps.basemap.containsKey(loc3)) {
										Location loc4 = Location.of(x3 + 1, y3);
										SurfaceEntity se = ps.basemap.get(loc4);
										ps.basemap.put(loc3, se);
									}
								}

							}
						}
					}
					
					ps.setSize(w0 * pscale, h0 * pscale);
				}
			}
			for (Planet p : planets.values()) {
				PlanetSurface ps0 = p.type.surfaces.get(p.surface.variant);
				p.surface.basemap = ps0.basemap;
				p.surface.features = ps0.features;
				p.surface.setSize(ps0.width, ps0.height);
			}
		}
	}
	/**
	 * Establish a war state between the two players.
	 * @param p1 the first player
	 * @param p2 the second player
	 */
	public void establishWar(Player p1, Player p2) {
		DiplomaticRelation dr = establishRelation(p1, p2);
		dr.full = true;
		if (!dr.strongAlliance) {
			int minWar = Math.min(p1.warThreshold, p2.warThreshold);
			int maxWar = Math.max(p1.warThreshold, p2.warThreshold);
			// each attack degrades relations a bit
			if (dr.value >= maxWar) {
				dr.value = minWar - 1;
			} else {
				dr.value -= 1;
			}
			dr.value = Math.max(0, dr.value);
			dr.tradeAgreement = false;
			dr.wontTalk(true);
			dr.lastContact = time.getTime();
			dr.alliancesAgainst.clear();
		}
	}
	/**
	 * Establish diplomatic relation between groups.
	 * @param groups the group mapping
	 */
	protected void establishDiplomacy(Map<Player, Integer> groups) {
		// establish strong alliances and basic enemies
		for (Map.Entry<Player, Integer> pg : groups.entrySet()) {
            Player p1 = pg.getKey();
			Integer g1 = pg.getValue();
			for (Map.Entry<Player, Integer> pg2 : groups.entrySet()) {
                Player p2 = pg2.getKey();
                Integer g2 = pg2.getValue();
				if (p1 != p2) {
					DiplomaticRelation dr = establishRelation(p1, p2);
					dr.full = true;
					if (g1.equals(g2)) {
						dr.value = 100;
						dr.strongAlliance = true;
					} else {
                        if (skirmishDefinition != null) {
                            switch (skirmishDefinition.initialDiplomaticRelation) {
                            case PEACEFUL:
                                dr.value = 100;
                                break;
                            case WAR:
                                dr.value = 0;
                                break;
                            default:
                                dr.value = p2.initialStance;
                                break;
                            }
                        } else {
                            dr.value = 50;
                        }
					}
				}
			}
		}
		// establish common alliances
		// establish strong alliances and basic enemies
		for (Map.Entry<Player, Integer> e : groups.entrySet()) {
            Player p1 = e.getKey();
			Integer g1 = e.getValue();
			for (Map.Entry<Player, Integer> e2 : groups.entrySet()) {
                Player p2 = e2.getKey();
				Integer g2 = e2.getValue();
				if (p1 != p2 && g1.equals(g2)) {
					for (Player p3 : groups.keySet()) {
						Integer g3 = groups.get(p3);
						if (!g3.equals(g1)) {
							DiplomaticRelation dr = getRelation(p1, p2);
							dr.alliancesAgainst.add(p3.id);
						}
					}
				}
			}
		}
	}
	/** 
	 * Assign players to preferred or random planets. 
	 * @param createdPlayers the player-skirmish player association map
	 */
	void assignPlanetsToPlayers(Map<Player, SkirmishPlayer> createdPlayers) {
		// assign planets to players
		int zones;
		int pc = players.values().size();
		for (int i = 1;; i++) {
			if (pc <= i * i) {
				zones = i;
				break;
			}
		}
		List<Player> pls = new ArrayList<>();
		for (Player p : players.values()) {
			if (p.aiMode != AIMode.PIRATES && p.aiMode != AIMode.TRADERS) {
				pls.add(p);
			}
		}
		
		List<Integer> zoneIndex = new ArrayList<>();
		for (int zi = 0; zi < zones * zones; zi++) {
			zoneIndex.add(zi);
		}
		ModelUtils.shuffle(zoneIndex);

		double gw = galaxyModel.map.getWidth();
		double gh = galaxyModel.map.getHeight();
		Rectangle2D.Double rect = new Rectangle2D.Double();
		int zi = 0;
		
		Set<String> preferredPlanets = new HashSet<>();
		for (Player p : pls) {
			SkirmishPlayer skp = createdPlayers.get(p);
			if (skp.initialPlanet != null) {
				if (preferredPlanets.add(skp.initialPlanet)) {
					
					Planet pl = planets.get(skp.initialPlanet);
					
					pl.owner = p;
					pl.race = p.race;
					pl.population(skirmishDefinition.initialPopulation);
					pl.lastPopulation(pl.population());

					p.currentPlanet = pl;
					p.selectionMode = SelectionMode.PLANET;

					p.planets.put(pl, PlanetKnowledge.BUILDING);
				}
			}
		}
		
		for (Player p : pls) {
			// planet already assigned
			if (!p.planets.isEmpty()) {
				continue;
			}
			int z = zoneIndex.get(zi);
			
			int gr = z / zones;
			int gc = z % zones;

			double dgw = gw / zones;
			double dgh = gh / zones;
			
			rect.x = gr * dgw;
			rect.y = gc * dgh;
			rect.width = dgw;
			rect.height = dgh;
			
			List<Planet> candidates = new ArrayList<>();
			for (Planet pl : planets.values()) {
				if (rect.contains(pl.x, pl.y) && pl.owner == null && !preferredPlanets.contains(pl.id)) {
					candidates.add(pl);
				}
			}
			
			if (candidates.isEmpty()) {
				// now try on all planets
				for (Planet pl : planets.values()) {
					if (pl.owner == null && !preferredPlanets.contains(pl.id)) {
						candidates.add(pl);
					}
				}
			}
			
			Planet pl = ModelUtils.random(candidates);
			
			p.currentPlanet = pl;
			p.selectionMode = SelectionMode.PLANET;
			pl.owner = p;
			pl.race = p.race;
			pl.population(skirmishDefinition.initialPopulation);
			pl.lastPopulation(pl.population());
			
			p.planets.put(pl, PlanetKnowledge.BUILDING);
			zi++;
		}
		// locate additional planets nearby
		for (Player p : pls) {
			List<Planet> op = p.ownPlanets();
			if (op.isEmpty()) {
				continue;
			}
			final Planet pl = op.get(0);
			List<Planet> rest = U.newArrayList(planets.values());
			rest.remove(pl);
			
			Collections.sort(rest, new Comparator<Planet>() {
				@Override
				public int compare(Planet o1, Planet o2) {
					double d1 = Point.distance(o1.x, o1.y, pl.x, pl.y);
					double d2 = Point.distance(o2.x, o2.y, pl.x, pl.y);
					return Double.compare(d1, d2);
				}
			});
			int n = skirmishDefinition.initialPlanets - 1;
			int i = 0;
			while (n > 0 && i < rest.size()) {
				Planet p2 = rest.get(i);
				if (p2.owner == null) {
					p2.owner = p;
					p2.race = p.race;
					p2.population(skirmishDefinition.initialPopulation);
					p2.lastPopulation(p2.population());
					p.planets.put(p2, PlanetKnowledge.BUILDING);
					n--;
				}
				i++;
			}
			
			for (Fleet f : p.ownFleets()) {
				double r = 4 + ModelUtils.random() * 3;
				double a = ModelUtils.random() * 2 * Math.PI;
				
				f.x = pl.x + r * Math.cos(a);
				f.y = pl.y + r * Math.sin(a);
			}
			if (skirmishDefinition.initialOrbitalFactories > 0) {
				pl.changeInventory(researches.get("OrbitalFactory"), p, 1);
			}
		}
		if (skirmishDefinition.placeColonyHubs) {
			for (Planet pl : planets.values()) {
				if (pl.owner != null) {
					pl.buildColonyHub();
				}
			}
		}
	}
	/**
	 * Create the starting fleet.
	 * @param p the player
	 */
	void createStartingFleet(Player p) {
		Fleet f = new Fleet(p);
		f.name(labels.get("@MainFleet"));
		for (ResearchType rt : researches.values()) {
			if (rt.race.contains(p.race)) {
				if (p.isAvailable(rt) || rt.level == 0) {
					if (rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
						p.changeInventoryCount(rt, 10);
						f.deployItem(rt, f.owner, 10);
					} else
					if (rt.category == ResearchSubCategory.SPACESHIPS_CRUISERS) {
						p.changeInventoryCount(rt, 3);
						List<InventoryItem> iss = f.deployItem(rt, f.owner, 3);
						fleetAddRadar(p, iss);
					} else
					if (rt.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS) {
						if (!"ColonyShip".equals(rt.id)) {
							p.changeInventoryCount(rt, 1);
							List<InventoryItem> iss = f.deployItem(rt, f.owner, 1);
							fleetAddRadar(p, iss);
						}
					}
				}
			}
		}
		p.currentFleet = f;
	}
	/**
	 * Adds 1 radar to one of the medium and large ships.
	 * @param p the player
	 * @param iss the list of inventory items
	 */
	void fleetAddRadar(Player p, List<InventoryItem> iss) {
		r1:
		for (InventoryItem ii : iss) {
			for (InventorySlot is : ii.slots.values()) {
				if (is.type == null) {
					for (ResearchType rt0 : is.slot.items) {
						if (rt0.has(ResearchType.PARAMETER_RADAR) && p.isAvailable(rt0)) {
							is.type = rt0;
							is.count = 1;
							break r1;
						}
					}
				}
			}
		}
	}
	/**
	 * Creates a new identifier.
	 * @return the new unique identifier
	 */
	public int newId() {
		return idSequence.incrementAndGet();
	}
	/** A function that generates a globally unique identifier. */
	public final Func0<Integer> newIdFunc = new Func0<Integer>() {
		@Override
		public Integer invoke() {
			return newId();
		}
	};
	/**
	 * Add the missing identifiers to inventory items and building instances,
	 * and remap fleet ids to conform the new single global id sequence.
	 * @param xworld the world object
	 */
	protected void patchWorld(XElement xworld) {
		if (!xworld.has("id-sequence")) {
			Map<Integer, Integer> fleetRemap = new HashMap<>();
			// fix player fleets
			for (XElement xplayer : xworld.childrenWithName("player")) {
				for (XElement xfleet : xplayer.childrenWithName("fleet")) {
					// replace fleet id with an unique number
					int current = xfleet.getInt("id", -1);
					if (current >= 0) {
						int newId = newId();
						fleetRemap.put(current, newId);
						xfleet.set("id", newId);
					}
					// move the inventory.id into inventory.type and set an id with unique number
					for (XElement xfii : xfleet.childrenWithName("item")) {
						xfii.set("type", xfii.get("id"));
						xfii.set("id", newId());
					}
				}
			}
			// fix player fleet targets
			for (XElement xplayer : xworld.childrenWithName("player")) {
				for (XElement xfleet : xplayer.childrenWithName("fleet")) {
					int tf = xfleet.getInt("target-fleet", -1);
					if (tf >= 0) {
						Integer nfi = fleetRemap.get(tf);
						if (nfi != null) {
							xfleet.set("target-fleet", nfi);
						} else {
							xfleet.set("target-fleet", null);
							xfleet.set("mode", null);
							xfleet.set("task", null);
						}
					}
				}
			}
			// fix planet inventory and buildings
			for (XElement xplanet : xworld.childrenWithName("planet")) {
				for (XElement xpii : xplanet.childrenWithName("item")) {
					xpii.set("type", xpii.get("id"));
					xpii.set("id", newId());
				}
				for (XElement xpii : xplanet.childrenWithName("building")) {
					String type = xpii.get("id");
					String race = xpii.get("tech");
					// name change
					if (type.equals("FusionProjector")) {
						type = "ParticleProjector";
					}
					// police split
					if (buildingModel.buildings.get(type).tileset.get(race) == null) {
						type += "2";
					}

					xpii.set("type", type);
					xpii.set("race", race);
					xpii.set("id", newId());

				}
			}
			xworld.set("id-sequence", idSequence.get());
		}
	}
	/**
	 * Returns a planet by the given id.
	 * @param planetId the planet id
	 * @return the planet object, or null if not found
	 */
	public Planet planet(String planetId) {
		return planets.get(planetId);
	}
	/**
	 * Finds a fleet by its id.
	 * @param fleetId the fleet id
	 * @return the fleet object, or null if not found
	 */
	public Fleet fleet(int fleetId) {
		return fleets.get(fleetId);
	}
	@Override
	public Player player(String playerId) {
		return players.get(playerId);
	}
	@Override
	public ResearchType research(String typeId) {
		return researches.get(typeId);
	}
	@Override
	public BuildingType building(String buildingTypeId) {
		return buildingModel.get(buildingTypeId);
	}
	/**
	 * @return allow building multiple labs per planet?
	 */
	public boolean noLabLimit() {
		return skirmishDefinition != null ? skirmishDefinition.noLabLimit : false;
	}
	/**
	 * @return Allow building multiple factories per planet?
	 */
	public boolean noFactoryLimit() {
		return skirmishDefinition != null ? skirmishDefinition.noFactoryLimit : false;
	}
	/** @return Allow building multiple economic buildings per planet? */
	public boolean noEconomicLimit() {
		return skirmishDefinition != null ? skirmishDefinition.noEconomicLimit : false;
	}
	/**
	 * Check if the diplomacy option is available.
	 * @return true if available
	 */
	public boolean hasDiplomacy() {
		return getShip().positions.containsKey("*diplomacy");
	}
	/**
	 * Check if the current gameplay is a skirmish play.
	 * @return true if this is a skirmish play.
	 */
	public boolean isSkirmish() {
		return skirmishDefinition != null;
	}
}
