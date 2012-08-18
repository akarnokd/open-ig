/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.utils.Exceptions;
import hu.openig.utils.XElement;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

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
	/** Resource location. */
	@LoadField
	public String battle;
	/** Resource location. */
	@LoadField
	public String bridge;
	/** Resource location. */
	@LoadField
	public String buildings;
	/** Resource location. */
	@LoadField
	public String diplomacy;
	/** Resource location. */
	@LoadField
	public String galaxy;
	/** Resource location. */
	@LoadField
	public String planets;
	/** Resource location. */
	@LoadField
	public String players;
	/** Resource location. */
	@LoadField
	public String talks;
	/** Resource location. */
	@LoadField
	public String tech;
	/** Resource location. */
	@LoadField
	public String test;
	/** Resource location. */
	@LoadField
	public String walks;
	/** The scripting definition. */
	@LoadField
	public String scripting;
	/** The optional chat definition. */
	@LoadField
	public String chats;
	/** The game parameters. */
	public final Map<String, String> parameters = new LinkedHashMap<String, String>();
	/** The traits to apply to the main player. */
	public Traits traits;
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
		String titleEn = null;
		String descEn = null;
		for (XElement texts : root.childrenWithName("texts")) {
			if (rl.language.equals(texts.get("language"))) {
				result.title = texts.childValue("title");
				result.description = texts.childValue("description");
			}
			if ("en".equals(texts.get("language"))) {
				titleEn = texts.childValue("title");
				descEn = texts.childValue("description");
			}
		}
		if (result.title == null) {
			result.title = titleEn;
			result.description = descEn;
		}
		result.intro = root.childValue("intro");
		String image = root.childValue("image");
		if (image != null) {
			result.image = rl.getImage(image);
		}
		result.startingLevel = Integer.parseInt(root.childValue("level"));
		
		for (Field f : GameDefinition.class.getDeclaredFields()) {
			if (f.isAnnotationPresent(LoadField.class)) {
				try {
					f.set(result, root.childValue(f.getName()));
				} catch (IllegalAccessException ex) {
					Exceptions.add(ex);
				}
			}
		}
		XElement xp = root.childElement("parameters");
		if (xp != null) {
			result.parameters.putAll(xp.attributes());
		}
		
		return result;
	}
}
