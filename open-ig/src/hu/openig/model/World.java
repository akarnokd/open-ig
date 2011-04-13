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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
	{
		time.set(GregorianCalendar.YEAR, 3427);
		time.set(GregorianCalendar.MONTH, GregorianCalendar.AUGUST);
		time.set(GregorianCalendar.DATE, 13);
		time.set(GregorianCalendar.HOUR_OF_DAY, 8);
		time.set(GregorianCalendar.MINUTE, 50);
	}
	/** All planets on the starmap. */
	public final Map<String, Planet> planets = new LinkedHashMap<String, Planet>();
	/** The list of available researches. */
	public final Map<String, ResearchType> researches = new HashMap<String, ResearchType>();
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
	/** The global world statistics. */
	public final WorldStatistics statistics = new WorldStatistics();
	/** The date formatter. */
	public SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	{
		dateFormat.setCalendar(time);
	}
	/** The sequence to assign unique ids to fleets. */
	public int fleetIdSequence;
	/**
	 * Load the game world's resources.
	 * @param resLocator the resource locator
	 * @param game the game directory
	 */
	public void load(final ResourceLocator resLocator, final String game) {
		this.name = game;
		this.rl = resLocator;
		final ThreadPoolExecutor exec = 
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
		exec.allowCoreThreadTimeOut(true);
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
		p.shortName = labels.get(player.get("name") + ".short");
		
		p.money = player.getInt("money");
		p.initialStance = player.getInt("initial-stance");
		
		p.fleetIcon = rl.getImage(player.get("icon"));
		String pic = player.get("picture");
		if (pic != null) {
			p.picture = rl.getImage(pic);
		}
		
		if ("true".equals(player.get("user", "false"))) {
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
		String nameLabel = planet.get("label", null);
		if (nameLabel != null) {
			p.name = labels.get(nameLabel); 
		}
		p.owner = players.get(planet.get("owner", null));
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
		
		String populationDelta = planet.get("population-last", null);
		if (populationDelta != null && !populationDelta.isEmpty()) {
			p.lastPopulation = Integer.parseInt(populationDelta);
		} else {
			p.lastPopulation = p.population;
		}
		String lastMorale = planet.get("morale-last", null);
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
		
		planets.put(p.id, p);

		if (p.owner != null) {
			p.owner.planets.put(p, PlanetKnowledge.BUILDING);
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
		// make the default researches available to each respective players
		for (Player p : players.values()) {
			for (ResearchType rt : researches.values()) {
				if (p.race.equals(rt.race) && rt.level == 0) {
					p.setAvailable(rt);
				}
			}
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
		
		String prereqs = item.get("requires", null);
		if (prereqs != null) {
			for (String si : prereqs.split("\\s*,\\s*")) {
				tech.prerequisites.add(getResearch(si));
			}
		}
		
		for (XElement slot : item.childrenWithName("slot")) {
			EquipmentSlot s = new EquipmentSlot();
			s.id = slot.get("id");
			s.x = slot.getInt("x");
			s.y = slot.getInt("y");
			s.width = slot.getInt("width");
			s.height = slot.getInt("height");
			
			for (String si : slot.get("items").split("\\s*,\\s*")) {
				s.items.add(getResearch(si));
			}
			
			tech.slots.put(s.id, s);
		}
		for (XElement prop : item.childrenWithName("property")) {
			tech.properties.put(prop.get("name"), prop.get("value"));
		}
		
		tech.equipmentImage = rl.getImage(image + "_tiny", true);
		tech.spaceBattleImage = rl.getImage(image + "_huge", true);
		tech.equipmentCustomizeImage = rl.getImage(image + "_small", true);
		if (tech.equipmentCustomizeImage == null) {
			tech.equipmentCustomizeImage = tech.spaceBattleImage;
		}
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
	/**
	 * @return Returns an ordered list of the research types.
	 */
	public List<ResearchType> getResearch() {
		List<ResearchType> res = new ArrayList<ResearchType>(researches.values());
		Collections.sort(res, new Comparator<ResearchType>() {
			@Override
			public int compare(ResearchType o1, ResearchType o2) {
				int c = o1.category.main.ordinal() - o2.category.main.ordinal();
				if (c == 0) {
					c = o1.category.ordinal() - o2.category.ordinal();
					if (c == 0) {
						c = o1.index - o2.index;
					}
				}
				return c;
			}
		});
		return res;
	}
	/** 
	 * Select the given research and its building type if any.
	 * @param rt the non-null research type
	 */
	public void selectResearch(ResearchType rt) {
		player.currentResearch(rt);
		if (rt.category.main == ResearchMainCategory.BUILDINS) {
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
	 * Save the world state.
	 * @return the world state as XElement tree.
	 */
	public XElement saveState() {
		XElement world = new XElement("world");

		world.set("level", level);
		world.set("game", name);
		world.set("player", player.id);
		world.set("difficulty", difficulty);
		world.set("time", dateFormat.format(time.getTime()));
		
		statistics.save(world.add("statistics"));
		
		
		for (Player p : players.values()) {
			XElement xp = world.add("player");
			xp.set("id", p.id);
			xp.set("money", p.money);
			xp.set("planet", p.currentPlanet != null ? p.currentPlanet.id : null);
			xp.set("fleet", p.currentFleet != null ? p.currentFleet.id : null);
			xp.set("building", p.currentBuilding != null ? p.currentBuilding.id : null);
			xp.set("research", p.currentResearch() != null ? p.currentResearch().id : null);
			xp.set("running", p.runningResearch != null ? p.runningResearch.id : null);
			xp.set("mode", p.selectionMode);
			
			p.statistics.save(xp.add("statistics"));

			if (p.knownPlayers.size() > 0) {
				XElement stances = xp.add("stance");
				for (Map.Entry<Player, Integer> se : p.knownPlayers.entrySet()) {
					XElement st1 = stances.add("with");
					st1.set("player", se.getKey().id);
					st1.set("value", se.getValue());
				}
			}
			if (p.messageQueue.size() > 0) {
				XElement xqueue = xp.add("message-queue");
				for (Message msg : p.messageQueue) {
					XElement xmessage = xqueue.add("message");
					msg.save(xmessage, dateFormat);
				}
			}
			if (p.messageHistory.size() > 0) {
				XElement xqueue = xp.add("message-history");
				for (Message msg : p.messageHistory) {
					XElement xmessage = xqueue.add("message");
					msg.save(xmessage, dateFormat);
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
			for (Map.Entry<ResearchType, Research> res : p.research.entrySet()) {
				XElement xres = xp.add("research");
				xres.set("id", res.getKey().id);
				xres.set("assigned", res.getValue().assignedMoney);
				xres.set("remaining", res.getValue().remainingMoney);
			}
			
			XElement res = xp.add("available");
			for (Map.Entry<ResearchType, List<ResearchType>> ae : p.available().entrySet()) {
				XElement av = res.add("type");
				av.set("id", ae.getKey().id);
				if (ae.getValue().size() > 0) {
					StringBuilder sb = new StringBuilder();
					for (ResearchType aert : ae.getValue()) {
						if (sb.length() > 0) {
							sb.append(", ");
						}
						sb.append(aert.id);
					}
					
					av.set("list", sb.toString());
				}
			}
			for (Map.Entry<Fleet, FleetKnowledge> fl : p.fleets.entrySet()) {
				if (fl.getKey().owner == p) {
					XElement xfleet = xp.add("fleet");
					xfleet.set("id", fl.getKey().id);
					xfleet.set("x", fl.getKey().x);
					xfleet.set("y", fl.getKey().y);
					xfleet.set("name", fl.getKey().name);
					for (InventoryItem fii : fl.getKey().inventory) {
						XElement xfii = xfleet.add("item");
						xfii.set("id", fii.type.id);
						xfii.set("count", fii.count);
						xfii.set("hp", fii.hp);
						xfii.set("shield", fii.shield);
						for (InventorySlot fis : fii.slots) {
							XElement xfs = xfii.add("slot");
							xfs.set("id", fis.slot.id);
							if (fis.type != null) {
								xfs.set("type", fis.type.id);
								xfs.set("count", fis.count);
								xfs.set("hp", fis.hp);
							}
						}
					}
				}
			}
			// save discovered planets only
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<Planet, PlanetKnowledge> pk : p.planets.entrySet()) {
				if (pk.getKey().owner != p) {
					if (sb.length() > 0) {
						sb.append(", ");
					}
					sb.append(pk.getKey().id);
				}
			}
			xp.set("discovered", sb.toString());
			
			for (Map.Entry<ResearchType, Integer> inv : p.inventory.entrySet()) {
				XElement xinv = xp.add("inventory");
				xinv.set("id", inv.getKey().id);
				xinv.set("count", inv.getValue());
			}
		}
		
		for (Planet p : planets.values()) {
			if (p.surface.buildings.size() > 0) {
				XElement xp = world.add("planet");
				xp.set("id", p.id);
				xp.set("owner", p.owner.id);
				xp.set("race", p.race);
				xp.set("quarantine", p.quarantine);
				xp.set("allocation", p.allocation);
				xp.set("tax", p.tax);
				xp.set("morale", p.morale);
				xp.set("morale-last", p.lastMorale);
				xp.set("population", p.population);
				xp.set("population-last", p.lastPopulation);
				xp.set("autobuild", p.autoBuild);
				xp.set("tax-income", p.taxIncome);
				xp.set("trade-income", p.tradeIncome);
				for (InventoryItem pii : p.inventory) {
					XElement xpii = xp.add("item");
					xpii.set("id", pii.type.id);
					xpii.set("owner", pii.owner.id);
					xpii.set("count", pii.count);
					xpii.set("hp", pii.hp);
				}
				for (Building b : p.surface.buildings) {
					XElement xb = xp.add("building");
					xb.set("x", b.location.x);
					xb.set("y", b.location.y);
					xb.set("id", b.type.id);
					xb.set("tech", b.techId);
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
		
		return world;
	}
	/**
	 * Load the world state.
	 * @param xworld the world XElement
	 */
	public void loadState(XElement xworld) {
		difficulty = Difficulty.valueOf(xworld.get("difficulty"));
		level = xworld.getInt("level");
		fleetIdSequence = 0;
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		sdf.setCalendar(time);
		try {
			time.setTime(sdf.parse(xworld.get("time")));
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		
		player = players.get(xworld.get("player"));
		
		XElement stats = xworld.childElement("statistics");
		if (stats != null) {
			statistics.load(stats);
		}
		
		for (XElement xplayer : xworld.childrenWithName("player")) {
			Player p = players.get(xplayer.get("id"));
			
			p.money = xplayer.getInt("money");
			p.currentPlanet = planets.get(xplayer.get("planet", null));
			
			p.currentBuilding = buildingModel.buildings.get(xplayer.get("building", null));
			p.currentResearch(researches.get(xplayer.get("research", null)));
			p.runningResearch = researches.get(xplayer.get("running", null));
			p.selectionMode = SelectionMode.valueOf(xplayer.get("mode", SelectionMode.PLANET.toString()));
			
			XElement xyesterday = xplayer.childElement("yesterday");
			
			p.yesterday.buildCost = xyesterday.getInt("build");
			p.yesterday.repairCost = xyesterday.getInt("repair");
			p.yesterday.researchCost = xyesterday.getInt("research");
			p.yesterday.productionCost = xyesterday.getInt("production");
			p.yesterday.taxIncome = xyesterday.getInt("tax");
			p.yesterday.tradeIncome = xyesterday.getInt("trade");
			p.yesterday.taxMorale = xyesterday.getInt("morale");
			p.yesterday.taxMoraleCount = xyesterday.getInt("count");
			
			XElement xtoday = xplayer.childElement("today");
			p.today.buildCost = xtoday.getInt("build");
			p.today.repairCost = xtoday.getInt("repair");
			p.today.researchCost = xtoday.getInt("research");
			p.today.productionCost = xtoday.getInt("production");

			for (Map<ResearchType, Production> prod : p.production.values()) {
				prod.clear();
			}
			
			XElement pstats = xplayer.childElement("statistics");
			if (pstats != null) {
				p.statistics.load(pstats);
			}
			
			XElement xstance = xplayer.childElement("stance");
			if (xstance != null) {
				for (XElement xwith : xstance.childrenWithName("with")) {
					Player pl = players.get(xwith.get("player"));
					if (pl != null) {
						p.setStance(pl, xwith.getInt("value"));
					} else {
						throw new AssertionError("Missing player for stance " + p.name + " vs. " + xwith.get("player"));
					}
				}
			}

			XElement xqueue = xplayer.childElement("message-queue");
			if (xqueue != null) {
				for (XElement xmessage : xqueue.childrenWithName("message")) {
					Message msg = new Message();
					msg.load(xmessage, dateFormat);
					player.messageQueue.add(msg);
				}
			}
			XElement xhistory = xplayer.childElement("message-queue");
			if (xhistory != null) {
				for (XElement xmessage : xhistory.childrenWithName("message")) {
					Message msg = new Message();
					msg.load(xmessage, dateFormat);
					player.messageHistory.add(msg);
				}
			}

			for (XElement xprod : xplayer.childrenWithName("production")) {
				ResearchMainCategory cat = ResearchMainCategory.valueOf(xprod.get("category"));
				Map<ResearchType, Production> prod = new LinkedHashMap<ResearchType, Production>();
				for (XElement xline : xprod.childrenWithName("line")) {
					ResearchType rt = researches.get(xline.get("id"));
					Production pr = new Production();
					pr.type = rt;
					pr.count = xline.getInt("count");
					pr.priority = xline.getInt("priority");
					pr.progress = xline.getInt("progress");
					prod.put(rt, pr);
				}
				p.production.put(cat, prod);
			}
			p.research.clear();
			for (XElement xres : xplayer.childrenWithName("research")) {
				ResearchType rt = researches.get(xres.get("id"));
				if (rt == null) {
					throw new IllegalArgumentException("research technology not found: " + xres.get("id"));
				}
				Research rs = new Research();
				rs.type = rt;
				rs.state = rt == p.currentResearch() ? ResearchState.RUNNING : ResearchState.STOPPED; 
				rs.assignedMoney = xres.getInt("assigned");
				rs.remainingMoney = xres.getInt("remaining");
				p.research.put(rt, rs);
			}
			
			// remove non-zero researches
			for (Iterator<ResearchType> it = player.research.keySet().iterator(); it.hasNext();) {
				ResearchType rt = it.next();
				if (rt.level != 0) {
					it.remove();
				}
			}
			XElement xavail0 = xplayer.childElement("available");
			if (xavail0 != null) {
				for (XElement xavail : xplayer.childrenWithName("type")) {
					ResearchType rt = researches.get(xavail.get("id"));
					if (rt == null) {
						throw new IllegalArgumentException("available technology not found: " + xavail);
					}
					player.add(rt);
					
					for (String liste : xavail.get("list", "").split("\\s*,\\s*")) {
						if (liste.length() > 0) {
							ResearchType rt0 = researches.get(liste);
							if (rt0 == null) {
								throw new IllegalArgumentException("available technology not found: " + liste + " in " + xavail);
							}
							player.availableLevel(rt).add(rt0);
						}
					}
				}
			}
			for (XElement xfleet : xplayer.childrenWithName("fleet")) {
				Fleet f = new Fleet();
				f.owner = p;
				f.id = xfleet.getInt("id");
				
				fleetIdSequence = Math.max(fleetIdSequence, f.id);
				
				f.x = xfleet.getInt("x");
				f.y = xfleet.getInt("y");
				f.name = xfleet.get("name");
				for (XElement xfii : xfleet.childrenWithName("item")) {
					InventoryItem fii = new InventoryItem();
					fii.type = researches.get(xfii.get("id"));
					fii.count = xfii.getInt("count");
					fii.shield = xfii.getInt("shield");
					fii.hp = xfii.getInt("hp");
					for (XElement xfis : xfii.childrenWithName("slot")) {
						InventorySlot fis = new InventorySlot();
						fis.slot = fii.type.slots.get(xfis.get("id"));
						fis.type = researches.get(xfis.get("type", null));
						if (fis.type != null) {
							fis.count = xfis.getInt("count");
							fis.hp = xfis.getInt("hp");
						}
						fii.slots.add(fis);
					}
					f.inventory.add(fii);
				}
				p.fleets.put(f, FleetKnowledge.FULL);
			}
			
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
			p.inventory.clear();
			for (XElement xinv : xplayer.childrenWithName("inventory")) {
				p.inventory.put(researches.get(xinv.get("id")), xinv.getInt("count"));
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
		}
		Set<String> allPlanets = new HashSet<String>(planets.keySet());
		for (XElement xplanet : xworld.childrenWithName("planet")) {
			Planet p = planets.get(xplanet.get("id"));

			p.owner = players.get(xplanet.get("owner"));
			p.race = xplanet.get("race");
			p.quarantine = "true".equals(xplanet.get("quarantine"));
			p.allocation = ResourceAllocationStrategy.valueOf(xplanet.get("allocation"));
			p.tax = TaxLevel.valueOf(xplanet.get("tax"));
			p.morale = xplanet.getInt("morale");
			p.lastMorale = xplanet.getInt("morale-last", p.morale);
			p.population = xplanet.getInt("population");
			p.lastPopulation = xplanet.getInt("population-last", p.population);
			p.autoBuild = AutoBuild.valueOf(xplanet.get("autobuild"));
			p.taxIncome = xplanet.getInt("tax-income");
			p.tradeIncome = xplanet.getInt("trade-income");

			p.inventory.clear();
			p.surface.buildings.clear();
			p.surface.buildingmap.clear();

			for (XElement xpii : xplanet.childrenWithName("item")) {
				InventoryItem pii = new InventoryItem();
				pii.owner = players.get(xpii.get("owner"));
				pii.type = researches.get(xpii.get("id"));
				pii.count = xpii.getInt("count");
				pii.hp = xpii.getInt("hp");
				p.inventory.add(pii);
			}

			p.surface.setBuildings(buildingModel, xplanet);
			
			if (p.owner != null) {
				p.owner.planets.put(p, PlanetKnowledge.BUILDING);
			}
			
			allPlanets.remove(p.id);
		}
		for (String rest : allPlanets) {
			Planet p = planets.get(rest);
			p.die();
		}
		fleetIdSequence++;
	}
	/** @return Return the list of other important items. */
	public String getOtherItems() {
		StringBuilder os = new StringBuilder();
		for (InventoryItem pii : player.currentPlanet.inventory) {
			if (pii.owner == player && pii.type.category == ResearchSubCategory.SPACESHIPS_SATELLITES) {
				if (os.length() > 0) {
					os.append(", ");
				}
				os.append(pii.type.name);
			} else
			if (pii.owner == player && pii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
				if (os.length() > 0) {
					os.append(", ");
				}
				os.append(pii.type.name);
			}
		}
		return os.toString();
	}
}
