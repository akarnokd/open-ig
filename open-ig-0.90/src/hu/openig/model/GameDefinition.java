/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.ResourceLocator;
import hu.openig.utils.XElement;

import java.awt.image.BufferedImage;

/**
 * The game definition used on the single player screen.
 * @author akarnokd, 2010.01.16.
 */
public class GameDefinition {
	/**
	 * The teaser image to display.
	 */
	public BufferedImage image;
	/** The intro media to play on start. */
	public String intro;
	/** The title text. */
	public String title;
	/** The campaign description. */
	public String description;
	/** The game name. */
	public String name;
	/** The starting level of the game. */
	public int startingLevel;
	/** The labels associated with this game if any. */
	public String labels;
	/** The galaxy description. */
	public String galaxy;
	/** The reaces description. */
	public String races;
	/** The technology description. */
	public String tech;
	/** The building description. */
	public String build;
	/** The planets description. */
	public String planets;
	/** The bridge description. */
	public String bridge;
	/** The walk description. */
	public String walk;
	/** The talk description. */
	public String talk;
	/**
	 * Parse the game definition from.
	 * @param rl the resource locator
	 * @param name the definition/game name
	 * @return the parsed definition.
	 */
	public static GameDefinition parse(ResourceLocator rl, String name) {
		GameDefinition result = new GameDefinition();
		result.name = name;
		XElement root = rl.getXML(name + "/definition");
		for (XElement texts : root.childrenWithName("texts")) {
			if (rl.language.equals(texts.get("language"))) {
				result.title = texts.childValue("title");
				result.description = texts.childValue("description");
				break;
			}
		}
		result.intro = root.childValue("intro");
		result.image = rl.getImage(root.childValue("image"));
		result.startingLevel = Integer.parseInt(root.childValue("level"));
		result.labels = root.childValue("labels");
		result.galaxy = root.childValue("galaxy");
		result.races = root.childValue("races");
		result.tech = root.childValue("tech");
		result.build = root.childValue("build");
		result.planets = root.childValue("planets");
		result.bridge = root.childValue("bridge");
		result.walk = root.childValue("walk");
		result.talk = root.childValue("talk");
		
		return result;
	}


}
