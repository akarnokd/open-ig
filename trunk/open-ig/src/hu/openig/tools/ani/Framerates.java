/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.tools.ani;

import hu.openig.utils.XElement;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

/**
 * Class to manage frame rate and delay constants in framerates.xml.
 * @author karnokd, 2009.02.22.
 * @version $Revision 1.0$
 */
public class Framerates {
	/**
	 * The particular FPS and sound delay values for an
	 * animation file and type.
	 * @author karnokd, 2009.02.22.
	 * @version $Revision 1.0$
	 */
	public static class Rates {
		/** The frames per second. */
		public final double fps;
		/** The delay in 1/22050 seconds. */
		public final int delay;
		/**
		 * Constructor. Initializes the final fields
		 * @param fps the frame rate
		 * @param delay the sound delay in 1/22050 seconds
		 */
		public Rates(double fps, int delay) {
			this.fps = fps;
			this.delay = delay;
		}
	}
	/** The default rates. */
	private static final Rates DEFAULT_RATES = new Rates(17.89, 0);
	/** The map of rates. */
	private final Map<String, Map<Integer, Rates>> rates;
	/**
	 * Constructor. Initializes the map and fills it.
	 */
	public Framerates() {
		rates = new HashMap<>();
		loadFromResource("framerates.xml");
	}
	/**
	 * Load values from resource.
	 * @param resource the resource path
	 */
	private void loadFromResource(String resource) {
		try {
			processDoc(XElement.parseXML(Framerates.class.getResource(resource)));
		} catch (IOException | XMLStreamException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Process the xml document.
	 * @param root the element root
	 */
	private void processDoc(XElement root) {
		for (XElement ani : root.childrenWithName("ani")) {
			String name = ani.get("name");
			Map<Integer, Rates> rt = rates.get(name);
			if (rt == null) {
				rt = new HashMap<>();
				rates.put(name, rt);
			}
			for (XElement type : ani.childrenWithName("type")) {
				int typeInt = Integer.parseInt(type.get("value"));
				double fps = Double.parseDouble(type.get("fps"));
				int delay = Integer.parseInt(type.get("delay"));
				rt.put(typeInt, new Rates(fps, delay));
			}
		}
	}
	/**
	 * Get the rates for a filename and type.
	 * @param filename the file name
	 * @param type the type index
	 * @return the rates, not null
	 */
	public Rates getRates(String filename, int type) {
		// extract last part
		int idx = filename.lastIndexOf('/');
		if (idx < 0) {
			idx = filename.lastIndexOf('\\');
		}
		if (idx >= 0) {
			filename = filename.substring(idx + 1);
		}
		Rates r = null;
		Map<Integer, Rates> typeRates = rates.get(filename.toUpperCase(Locale.ENGLISH));
		if (typeRates != null) {
			r = typeRates.get(type);
		}
		return r != null ? r : DEFAULT_RATES;
	}
}
