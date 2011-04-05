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
import hu.openig.model.Bridge.Level;
import hu.openig.render.TextRenderer;
import hu.openig.utils.ImageUtils;
import hu.openig.utils.WipPort;
import hu.openig.utils.XElement;

import java.awt.image.BufferedImage;
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
	/** The list of available researches. */
	public final Map<String, ResearchType> researches = new HashMap<String, ResearchType>();
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
	/** The common resource allocator. */
	public final ResourceAllocator allocator;
	/** The radar computation. */
	public final Radar radar;
	/** The progress simulator. */
	public final Simulator simulator;
	/**
	 * Construct the world.
	 * @param pool the executor service
	 */
	public World(ExecutorService pool) {
		allocator = new ResourceAllocator(pool, planets);
		radar = new Radar(1000, this);
		simulator = new Simulator(1000, this);
	}
	/**
	 * Load the game world's resources.
	 * @param resLocator the resource locator
	 * @param game the game directory
	 */
	public void load(final ResourceLocator resLocator, final String game) {
		this.name = game;
		this.rl = resLocator;
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
			
			processPlayers(rl.getXML(game + "/players"));
			
			processResearches(rl.getXML(definition.tech));
			
			exec.submit(new Runnable() {
				@Override
				public void run() {
					try {
						talks = new Talks();
						talks.load(rl, definition.talk);
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
						walks = new Walks();
						walks.load(rl, definition.walk);
						
						bridge = new Bridge();
						processBridge(rl, definition.bridge);
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
						buildingModel = new BuildingModel();
						buildingModel.processBuildings(rl, definition.build, researches, labels, exec, wip);
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
						galaxyModel = new GalaxyModel();
						galaxyModel.processGalaxy(rl, definition.galaxy, exec, wip);
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
								XElement map = rl.getXML(n);
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
		
		processPlanets(rl.getXML(game + "/planets"));

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
	 * @param data the data resource to load
	 */
	protected void processBridge(ResourceLocator rl, String data) {
		XElement root = rl.getXML(data);
		XElement graphics = root.childElement("graphics");
		for (XElement level : graphics.childrenWithName("level")) {
			Bridge.Level lvl = new Bridge.Level();
			lvl.id = Integer.parseInt(level.get("id"));
			lvl.image = rl.getImage(level.get("image"));
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
			lvl.up[0] = rl.getImage(up);
			lvl.up[0] = rl.getImage(up + "_pressed");
			lvl.up[0] = rl.getImage(up + "_empty");
			String down = mpButtons.get("down");
			lvl.down[0] = rl.getImage(down);
			lvl.down[0] = rl.getImage(down + "_pressed");
			lvl.down[0] = rl.getImage(down + "_empty");
			String send = mpButtons.get("send");
			lvl.send[0] = rl.getImage(send);
			lvl.send[0] = rl.getImage(send + "_pressed");
			String receive = mpButtons.get("receive");
			lvl.receive[0] = rl.getImage(receive);
			lvl.receive[0] = rl.getImage(receive + "_pressed");
			
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
		
		p.fleetIcon = rl.getImage(player.get("icon"));
		String pic = player.get("picture");
		if (pic != null) {
			p.picture = rl.getImage(pic);
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
		
		p.allocation = ResourceAllocationStrategy.valueOf(planet.get("allocation"));
		p.autoBuild = AutoBuild.valueOf(planet.get("autobuild"));
		p.tax = TaxLevel.valueOf(planet.get("tax"));
		p.rotationDirection = RotationDirection.valueOf(planet.get("rotate"));
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
			p.owner.planets.put(p, PlanetKnowledge.BUILDING);
			if (p.owner == player) {
				PlanetInventoryItem sat = new PlanetInventoryItem();
				sat.owner = player;
				sat.count = 1;
				sat.type = researches.get("Hubble2");
				p.inventory.add(sat);
			}
		}
		if (p.owner == null || p.owner != player) {
			// FIXME for testing the radar/info
			PlanetInventoryItem sat = new PlanetInventoryItem();
			sat.owner = player;
			sat.count = 1;
			sat.type = researches.get("Satellite");
			p.inventory.add(sat);
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
		tech.infoImage = rl.getImage(image + "_large", true);
		tech.infoImageWired = rl.getImage(image + "_wired_large", true);
		
		tech.factory = item.get("factory");
		tech.race = item.get("race");
		tech.productionCost = Integer.parseInt(item.get("production-cost"));
		tech.researchCost = Integer.parseInt(item.get("research-cost"));
		tech.level = Integer.parseInt(item.get("level"));
		
		tech.civilLab = item.getInt("civil", 0);
		tech.mechLab = item.getInt("mech", 0);
		tech.compLab = item.getInt("comp", 0);
		tech.aiLab = item.getInt("ai", 0);
		tech.milLab = item.getInt("mil", 0);
		
		String prereqs = item.get("requires");
		if (prereqs != null) {
			for (String si : prereqs.split("\\s*,\\s*")) {
				tech.prerequisites.add(getResearch(si));
			}
		}
		
		for (XElement slot : item.childrenWithName("slot")) {
			EquipmentSlot s = new EquipmentSlot();
			
			s.x = slot.getInt("x", 0);
			s.y = slot.getInt("y", 0);
			s.width = slot.getInt("width", 0);
			s.height = slot.getInt("height", 0);
			
			for (String si : slot.get("items").split("\\s*,\\s*")) {
				s.items.add(getResearch(si));
			}
			
			tech.slots.add(s);
		}
		for (XElement prop : item.childrenWithName("property")) {
			tech.properties.put(prop.get("name"), prop.get("value"));
		}
		
		tech.equipmentImage = rl.getImage(image + "_tiny", true);
		tech.equipmentCustomizeImage = rl.getImage(image + "_small", true);
		tech.spaceBattleImage = rl.getImage(image + "_huge", true);
		tech.index = item.getInt("index");
		tech.video = item.get("video");
		
		BufferedImage rot = rl.getImage(image + "_rotate", true);
		if (rot != null) {
			tech.rotation = ImageUtils.splitByWidth(rot, rot.getHeight());
		}
		BufferedImage matrix = rl.getImage(image + "_matrix", true);
		if (matrix != null) {
			tech.fireAndTotation = ImageUtils.split(matrix, matrix.getHeight() / 5, matrix.getHeight() / 5);
		}
		for (Player p : players.values()) {
			if (p.race.equals(tech.race) && tech.level == 0) {
				p.availableResearch.add(tech);
			}
		}
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
		List<BuildingType> result = new ArrayList<BuildingType>();
		
		for (BuildingType bt : buildingModel.buildings.values()) {
			if (bt.tileset.containsKey(planet.isPopulated() ? planet.race : player.race)) {
				if (bt.research == null || (planet.owner != player)
						|| (player.isAvailable(bt.research) || bt.research.level == 0)) {
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
	/** Close the resources. */
	public void close() {
		allocator.stop();
		radar.stop();
		simulator.stop();
	}
	/** Start the timed actions. */
	public void start() {
		allocator.start();
		radar.start();
		simulator.start();
	}
	/**
	 * Returns true if all prerequisites of the given research type have been met.
	 * If a research is available, it will result as false
	 * @param rt the research type
	 * @return true
	 */
	public boolean canResearch(ResearchType rt) {
		if (!player.isAvailable(rt)) {
			if (rt.level <= level) {
				for (ResearchType rt0 : rt.prerequisites) {
					if (!player.isAvailable(rt0)) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	/**
	 * Can the research be shown in listings?
	 * @param rt the research type
	 * @return true if display
	 */
	public boolean canDisplayResearch(ResearchType rt) {
		return player.isAvailable(rt) || rt.level <= level;
	}
	/**
	 * Get the research color for the given research type.
	 * @param rt the research type
	 * @return the color
	 */
	public int getResearchColor(ResearchType rt) {
		int c = TextRenderer.GRAY;
		if (player.isAvailable(rt)) {
			c = TextRenderer.ORANGE;
		} else
		if (player.research.containsKey(rt)) {
			c = TextRenderer.YELLOW;
		} else
		if (canResearch(rt)) {
			c = TextRenderer.GREEN;
		}
		return c;
	}

}
