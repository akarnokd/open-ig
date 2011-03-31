/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;
import hu.openig.core.Labels;
import hu.openig.core.PlanetType;
import hu.openig.core.ResourceLocator;
import hu.openig.mechanics.ResourceAllocator;
import hu.openig.model.Bridge.Level;
import hu.openig.utils.WipPort;
import hu.openig.utils.XElement;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The world object.
 * @author akarnokd, 2009.10.25.
 */
public class World {
	/** The name of the world. */
	public String name;
	/** The current world level. */
	public int level;
	/** The current player. */
	public Player player;
	/** The map of player-id to player object. */
	public final Map<String, Player> players = new HashMap<String, Player>();
	/** The time. */
	public final GregorianCalendar time = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	/** All planets on the starmap. */
	public final List<Planet> planets = new ArrayList<Planet>();
	/** All fleets on the starmap. */
	public final List<Fleet> fleets = new ArrayList<Fleet>();
	/** The list of available researches. */
	public final List<ResearchType> researches = new ArrayList<ResearchType>();
	/** Achievements. */
	public final List<Achievement> achievements = new ArrayList<Achievement>();
	/** The available crew-talks. */
	public Talks talks;
	/** The ship-walk definitions. */
	public Walks walks;
	/** The game definition. */
	public GameDefinition definition;
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
	/** The language. */
	public String language;
	/** The common resource allocator. */
	public ResourceAllocator allocator;
	/**
	 * Load the game world's resources.
	 * @param resLocator the resource locator
	 * @param lang the current language
	 * @param game the game directory
	 */
	public void load(final ResourceLocator resLocator, final String lang, final String game) {
		this.name = game;
		this.rl = resLocator;
		this.language = lang;
		final ExecutorService exec = 
			new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), 
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
		final WipPort wip = new WipPort(5);
		try {
			level = definition.startingLevel;
			
			processPlayers(rl.getXML(language, game + "/players"));
			
			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						long t = System.nanoTime();
						talks = new Talks();
						talks.load(rl, language, definition.talk);
						System.out.printf("Loading talks: %d%n", (System.nanoTime() - t) / 1000000);
					} catch (Throwable t) {
						t.printStackTrace();
					} finally {
						wip.dec();
					}
				}
			});
			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						long t = System.nanoTime();
						walks = new Walks();
						walks.load(rl, language, definition.walk);
						System.out.printf("Loading walks: %d%n", (System.nanoTime() - t) / 1000000);
						
						t = System.nanoTime();
						bridge = new Bridge();
						processBridge(rl, language, definition.bridge);
						System.out.printf("Loading bridge: %d%n", (System.nanoTime() - t) / 1000000);
					} catch (Throwable t) {
						t.printStackTrace();
					} finally {
						wip.dec();
					}
				}
			});
			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						long t = System.nanoTime();
						buildingModel = new BuildingModel();
						buildingModel.processBuildings(rl, language, definition.build, exec, wip);
						System.out.printf("Loading building: %d%n", (System.nanoTime() - t) / 1000000);
					} catch (Throwable t) {
						t.printStackTrace();
					} finally {
						wip.dec();
					}
				}
			});
			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						long t = System.nanoTime();
						galaxyModel = new GalaxyModel();
						galaxyModel.processGalaxy(rl, language, definition.galaxy, exec, wip);
						System.out.printf("Loading galaxy: %d%n", (System.nanoTime() - t) / 1000000);
					} catch (Throwable t) {
						t.printStackTrace();
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
								XElement map = rl.getXML(language, n);
								PlanetSurface ps = new PlanetSurface();
								ps.parseMap(map, galaxyModel, buildingModel);
								synchronized (pt.surfaces) {
									pt.surfaces.put(j, ps);
								}
							} catch (Throwable t) {
								System.err.println(n);
								t.printStackTrace();
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
		
		processPlanets(rl.getXML(language, game + "/planets"));

		try {
			exec.shutdown();
		} finally {
			try {
				exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ex) {
				
			}
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
	 * @param language the language
	 * @param data the data resource to load
	 */
	protected void processBridge(ResourceLocator rl, String language, String data) {
		XElement root = rl.getXML(language, data);
		XElement graphics = root.childElement("graphics");
		for (XElement level : graphics.childrenWithName("level")) {
			Bridge.Level lvl = new Bridge.Level();
			lvl.id = Integer.parseInt(level.get("id"));
			lvl.image = rl.getImage(language, level.get("image"));
			lvl.ship = walks.ships.get(level.get("ship-id"));
			lvl.walk = lvl.ship.positions.get("*bridge");
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
			lvl.up[0] = rl.getImage(language, up);
			lvl.up[0] = rl.getImage(language, up + "_pressed");
			lvl.up[0] = rl.getImage(language, up + "_empty");
			String down = mpButtons.get("down");
			lvl.down[0] = rl.getImage(language, down);
			lvl.down[0] = rl.getImage(language, down + "_pressed");
			lvl.down[0] = rl.getImage(language, down + "_empty");
			String send = mpButtons.get("send");
			lvl.send[0] = rl.getImage(language, send);
			lvl.send[0] = rl.getImage(language, send + "_pressed");
			String receive = mpButtons.get("receive");
			lvl.receive[0] = rl.getImage(language, receive);
			lvl.receive[0] = rl.getImage(language, receive + "_pressed");
			
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
			Bridge.Message msg = new Bridge.Message();
			msg.id = message.get("id");
			msg.media = message.get("media");
			msg.title = message.get("title");
			msg.description = message.get("description");
			bridge.sendMessages.add(msg);
		}
		XElement receive = messages.childElement("receive");
		for (XElement message : receive.childrenWithName("message")) {
			Bridge.Message msg = new Bridge.Message();
			msg.id = message.get("id");
			msg.media = message.get("media");
			msg.title = message.get("title");
			msg.description = message.get("description");
			bridge.receiveMessages.add(msg);
		}
	}
	/**
	 * @return the ship for the current level
	 */
	public WalkShip getShip() {
		// TODO Auto-generated method stub
		return getCurrentLevel().ship;
	}
	/**
	 * Process the players XML.
	 * @param players the players node
	 */
	public void processPlayers(XElement players) {
		for (XElement player : players.childrenWithName("player")) {
			processPlayer(player);
		}
	}
	/**
	 * Process a player.
	 * @param player the player
	 */
	public void processPlayer(XElement player) {
		Player p = new Player();
		p.id = player.get("id");
		p.color = (int)Long.parseLong(player.get("color"), 16);
		p.race = player.get("race");
		p.name = labels.get(player.get("name"));
		
		p.fleetIcon = rl.getImage(language, player.get("icon"));
		String pic = player.get("picture");
		if (pic != null) {
			p.picture = rl.getImage(language, pic);
		}
		
		if ("true".equals(player.get("user"))) {
			this.player = p;
		}
		this.players.put(p.id, p);
	}
	/**
	 * Process the planets listing XML.
	 * @param planets the planets node
	 */
	public void processPlanets(XElement planets) {
		for (XElement planet : planets.childrenWithName("planet")) {
			processPlanet(planet);
		}
	}
	/**
	 * Process a planet node.
	 * @param planet the 
	 */
	public void processPlanet(XElement planet) {
		Planet p = new Planet();
		p.id = planet.get("id");
		p.name = planet.get("name");
		String nameLabel = planet.get("label");
		if (nameLabel != null) {
			p.name = labels.get(nameLabel); 
		}
		p.owner = players.get(planet.get("owner"));
		p.race = planet.get("race");
		p.x = Integer.parseInt(planet.get("x"));
		p.y = Integer.parseInt(planet.get("y"));
		
		p.diameter = Integer.parseInt(planet.get("size"));
		p.population = Integer.parseInt(planet.get("population"));
		
		p.allocation = getEnum(ResourceAllocationStrategy.class, planet.get("allocation"));
		p.autoBuild = getEnum(AutoBuild.class, planet.get("autobuild"));
		p.tax = getEnum(TaxLevel.class, planet.get("tax"));
		p.rotationDirection = getEnum(RotationDirection.class, planet.get("rotate"));
		p.morale = Integer.parseInt(planet.get("morale"));
		p.taxIncome = Integer.parseInt(planet.get("tax-income"));
		p.tradeIncome = Integer.parseInt(planet.get("trade-income"));
		
		String populationDelta = planet.get("population-last");
		if (populationDelta != null && !populationDelta.isEmpty()) {
			p.lastPopulation = Integer.parseInt(populationDelta);
		} else {
			p.lastPopulation = p.population;
		}
		String lastMorale = planet.get("morale-last");
		if (lastMorale != null && !lastMorale.isEmpty()) {
			p.lastMorale = Integer.parseInt(lastMorale);
		} else {
			p.lastMorale = p.morale;
		}
		
		XElement surface = planet.childElement("surface");
		String si = surface.get("id");
		String st = surface.get("type");
		p.type = galaxyModel.planetTypes.get(st);
		p.surface = p.type.surfaces.get(Integer.parseInt(si)).copy();
		p.surface.parseMap(planet, null, buildingModel);
		
		planets.add(p);
		if (p.owner != null) {
			p.owner.planets.put(p, PlanetKnowledge.FULL);
			player.planets.put(p, PlanetKnowledge.OWNED);
		}
	}
	/**
	 * Locate an enumeration based on its string name.
	 * @param <T> the enum type
	 * @param e the enum class
	 * @param value the string
	 * @return the enum or null if not found
	 */
	public static <T extends Enum<T>> T getEnum(Class<T> e, String value) {
		for (T ec : e.getEnumConstants()) {
			if (ec.name().equals(value)) {
				return ec;
			}
		}
		return null;
	}
}
