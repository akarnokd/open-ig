/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.Difficulty;
import hu.openig.core.ResourceLocator;
import hu.openig.model.Bridge.Level;
import hu.openig.utils.WipPort;
import hu.openig.utils.XElement;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The world object.
 * @author karnok, 2009.10.25.
 * @version $Revision 1.0$
 */
public class World {
	/** The current world level. */
	public int level;
	/** The current player. */
	public Player player;
	/** The list of all players. */
	public final List<Player> players = new ArrayList<Player>();
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
	/**
	 * Load the game world's resources.
	 * @param rl the resource locator
	 * @param language the current language
	 * @param game the game directory
	 */
	public void load(final ResourceLocator rl, final String language, final String game) {
		final ExecutorService exec = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(), Integer.MAX_VALUE, 1, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
		final WipPort wip = new WipPort(5);
		try {
			level = definition.startingLevel;
	//		Element races = rl.getXML(language, game + "/races");
	//		Element tech = rl.getXML(language, game + "/tech");
	//		Element planets = rl.getXML(language, game + "/planets");
			
			
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
			try {
				wip.await();
			} catch (InterruptedException ex) {
				
			}
			exec.shutdown();
			try {
				exec.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
			} catch (InterruptedException ex) {
				
			}
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
}
