/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.ResourceLocator;
import hu.openig.utils.XElement;

import java.awt.image.BufferedImage;
import java.util.HashMap;
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
	/** The various parameters. */
	private final Map<String, Object> parameters = new HashMap<String, Object>();
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
		
		for (XElement param : root.childrenWithName("param")) {
			String t = param.get("type", "");
			if ("int".equals(t)) {
				result.parameters.put(param.get("name"), Integer.parseInt(param.get("value")));
			} else
			if ("long".equals(t)) {
				result.parameters.put(param.get("name"), Long.parseLong(param.get("value")));
			} else
			if ("float".equals(t)) {
				result.parameters.put(param.get("name"), Float.parseFloat(param.get("value")));
			} else
			if ("double".equals(t)) {
				result.parameters.put(param.get("name"), Double.parseDouble(param.get("value")));
			} else {
				result.parameters.put(param.get("name"), param.get("value"));
			}
		}
		
		return result;
	}
	/**
	 * Retrieve an integer parameter.
	 * @param name the parameter name.
	 * @return the value
	 */
	public int getInt(String name) {
		return (Integer)parameters.get(name);
	}
	/**
	 * Retrieve an long parameter.
	 * @param name the parameter name.
	 * @return the value
	 */
	public long getLong(String name) {
		return (Long)parameters.get(name);
	}
	/**
	 * Retrieve an float parameter.
	 * @param name the parameter name.
	 * @return the value
	 */
	public float getFloat(String name) {
		return (Float)parameters.get(name);
	}
	/**
	 * Retrieve an double parameter.
	 * @param name the parameter name.
	 * @return the value
	 */
	public double getDouble(String name) {
		return (Double)parameters.get(name);
	}
	/**
	 * Retrieve an parameter as string.
	 * @param name the parameter name.
	 * @return the value
	 */
	public String get(String name) {
		return parameters.get(name).toString();
	}
	/**
	 * Retrieve an integer parameter or the supplied default value.
	 * @param name the parameter name
	 * @param def the default value
	 * @return the value
	 */
	public int getInt(String name, int def) {
		Object o = parameters.get(name);
		return o != null ? (Integer)o : def;
	}
	/**
	 * Retrieve an long parameter or the supplied default value.
	 * @param name the parameter name
	 * @param def the default value
	 * @return the value
	 */
	public long getLong(String name, long def) {
		Object o = parameters.get(name);
		return o != null ? (Long)o : def;
	}
	/**
	 * Retrieve an float parameter or the supplied default value.
	 * @param name the parameter name
	 * @param def the default value
	 * @return the value
	 */
	public float getFloat(String name, float def) {
		Object o = parameters.get(name);
		return o != null ? (Float)o : def;
	}
	/**
	 * Retrieve an double parameter or the supplied default value.
	 * @param name the parameter name
	 * @param def the default value
	 * @return the value
	 */
	public double getDouble(String name, double def) {
		Object o = parameters.get(name);
		return o != null ? (Double)o : def;
	}
	/**
	 * Retrieve a parameter as string or the supplied default value.
	 * @param name the parameter name
	 * @param def the default value
	 * @return the value
	 */
	public String get(String name, String def) {
		Object o = parameters.get(name);
		return o != null ? o.toString() : def;
	}

}
