/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1.gfx;

import hu.openig.v1.Configuration;
import hu.openig.v1.ResourceLocator;

import java.lang.reflect.Field;

/**
 * Test graphics loading.
 * @author karnok, 2009.11.09.
 * @version $Revision 1.0$
 */
public final class TestGFXLoad {
	/** Private constructor. */
	private TestGFXLoad() {
		// utility class
	}
	/**
	 * Verify if any field is null or the array contains null entries.
	 * @param obj the object
	 */
	static void verifyNull(Object obj) {
		for (Field f : obj.getClass().getFields()) {
			// test for nullness:
			try {
				if (f.get(obj) == null) {
					System.err.printf("Field %s in %s is null%n", f.getName(), obj.getClass());
				} else
				if (f.getType().isArray()) {
					Object[] arr = (Object[])f.get(obj);
					for (int i = 0; i < arr.length; i++) {
						if (arr[i] == null) {
							System.err.printf("Entry %s[%d] in %s is null%n", f.getName(), i, obj.getClass());
						}
					}
				}
			} catch (Exception ex) {
				
			}
		}
	}
	/**
	 * @param args no arguments
	 */
	public static void main(String[] args) {
		long time = System.currentTimeMillis();
		Configuration config = new Configuration("open-ig-config.xml");
		config.load();
		final ResourceLocator rl = new ResourceLocator();
		rl.setContainers(config.containers);
		rl.scanResources();

		String lang = "hu";
		// -----------------------------------------------------
		StatusbarGFX statusbarGFX = new StatusbarGFX(rl);
		statusbarGFX.load(lang);
		verifyNull(statusbarGFX);
		
		BackgroundGFX backgroundGFX = new BackgroundGFX(rl);
		backgroundGFX.load(lang);
		verifyNull(backgroundGFX);
		
		EquipmentGFX equipmentGFX = new EquipmentGFX(rl);
		equipmentGFX.load(lang);
		verifyNull(equipmentGFX);
		
		SpacewarGFX spacewarGFX = new SpacewarGFX(rl);
		spacewarGFX.load(lang);
		verifyNull(equipmentGFX);
		
		// -----------------------------------------------------
		time = System.currentTimeMillis() -  time;
		System.out.printf("Load time: %d%n", time);
	}

}
