/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.model;

import hu.openig.utils.XML;
import hu.openig.v1.core.Difficulty;
import hu.openig.v1.core.ResourceLocator;
import hu.openig.v1.model.Bridge.Level;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.w3c.dom.Element;

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
	public void load(ResourceLocator rl, String language, String game) {
		level = definition.startingLevel;
//		Element races = rl.getXML(language, game + "/races");
//		Element tech = rl.getXML(language, game + "/tech");
//		Element planets = rl.getXML(language, game + "/planets");
		talks = new Talks();
		talks.load(rl, language, definition.talk);
		walks = new Walks();
		walks.load(rl, language, definition.walk);
		bridge = new Bridge();
		processBridge(rl, language, definition.bridge);
		galaxyModel = new GalaxyModel();
		galaxyModel.processGalaxy(rl, language, definition.galaxy);
		buildingModel = new BuildingModel();
		buildingModel.processBuildings(rl, language, definition.build);

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
		Element root = rl.getXML(language, data);
		Element graphics = XML.childElement(root, "graphics");
		for (Element level : XML.childrenWithName(graphics, "level")) {
			Bridge.Level lvl = new Bridge.Level();
			lvl.id = Integer.parseInt(level.getAttribute("id"));
			lvl.image = rl.getImage(language, level.getAttribute("image"));
			lvl.ship = walks.ships.get(level.getAttribute("ship-id"));
			lvl.walk = lvl.ship.positions.get("*bridge");
			Element mp = XML.childElement(level, "message-panel");
			
			Element mpAppear = XML.childElement(mp, "appear");
			lvl.messageAppear.video = mpAppear.getAttribute("video");
			lvl.messageAppear.audio = mpAppear.getAttribute("audio");
			
			Element mpOpen = XML.childElement(mp, "open");
			lvl.messageOpen.video = mpOpen.getAttribute("video");
			lvl.messageOpen.audio = mpOpen.getAttribute("audio");
			
			Element mpClose = XML.childElement(mp, "close");
			lvl.messageClose.video = mpClose.getAttribute("video");
			lvl.messageClose.audio = mpClose.getAttribute("audio");
			
			Element mpButtons = XML.childElement(mp, "buttons");
			String up = mpButtons.getAttribute("up");
			lvl.up[0] = rl.getImage(language, up);
			lvl.up[0] = rl.getImage(language, up + "_pressed");
			lvl.up[0] = rl.getImage(language, up + "_empty");
			String down = mpButtons.getAttribute("down");
			lvl.down[0] = rl.getImage(language, down);
			lvl.down[0] = rl.getImage(language, down + "_pressed");
			lvl.down[0] = rl.getImage(language, down + "_empty");
			String send = mpButtons.getAttribute("send");
			lvl.send[0] = rl.getImage(language, send);
			lvl.send[0] = rl.getImage(language, send + "_pressed");
			String receive = mpButtons.getAttribute("receive");
			lvl.receive[0] = rl.getImage(language, receive);
			lvl.receive[0] = rl.getImage(language, receive + "_pressed");
			
			Element cp = XML.childElement(level, "comm-panel");
			Element cpOpen = XML.childElement(cp, "open");
			lvl.projectorOpen.video = cpOpen.getAttribute("video");
			lvl.projectorOpen.audio = cpOpen.getAttribute("audio");
			
			Element cpClose = XML.childElement(cp, "close");
			lvl.projectorClose.video = cpClose.getAttribute("video");
			lvl.projectorClose.audio = cpClose.getAttribute("audio");
			bridge.levels.put(lvl.id, lvl);
		}
		Element messages = XML.childElement(root, "messages");
		Element send = XML.childElement(messages, "send");
		for (Element message : XML.childrenWithName(send, "message")) {
			Bridge.Message msg = new Bridge.Message();
			msg.id = message.getAttribute("id");
			msg.media = message.getAttribute("media");
			msg.title = message.getAttribute("title");
			msg.description = message.getAttribute("description");
			bridge.sendMessages.add(msg);
		}
		Element receive = XML.childElement(messages, "receive");
		for (Element message : XML.childrenWithName(receive, "message")) {
			Bridge.Message msg = new Bridge.Message();
			msg.id = message.getAttribute("id");
			msg.media = message.getAttribute("media");
			msg.title = message.getAttribute("title");
			msg.description = message.getAttribute("description");
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
