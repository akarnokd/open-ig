/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.Configuration;
import hu.openig.core.ResourceLocator;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

		final ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		List<Future<?>> ends = new LinkedList<Future<?>>();
		for (String lang1 : new String[] { "hu", "en" }) {
			final String lang = lang1;
			// -----------------------------------------------------
			ends.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					StatusbarGFX statusbarGFX = new StatusbarGFX(rl);
					statusbarGFX.load(lang);
					verifyNull(statusbarGFX);
				}
			}));
			ends.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					BackgroundGFX backgroundGFX = new BackgroundGFX(rl);
					backgroundGFX.load(lang);
					verifyNull(backgroundGFX);
				}
			}));
			ends.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					EquipmentGFX equipmentGFX = new EquipmentGFX(rl);
					equipmentGFX.load(lang);
					verifyNull(equipmentGFX);
				}
			}));
			ends.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					SpacewarGFX spacewarGFX = new SpacewarGFX(rl);
					spacewarGFX.load(lang);
					verifyNull(spacewarGFX);
				}
			}));
			ends.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					InfoGFX infoGFX = new InfoGFX(rl);
					infoGFX.load(lang);
					verifyNull(infoGFX);
				}
			}));
			ends.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					ResearchGFX researchGFX = new ResearchGFX(rl);
					researchGFX.load(lang);
					verifyNull(researchGFX);
				}
			}));
			ends.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					ColonyGFX colonyGFX = new ColonyGFX(rl);
					colonyGFX.load(lang);
					verifyNull(colonyGFX);
				}
			}));
			ends.add(exec.submit(new Runnable() {
				@Override
				public void run() {
					StarmapGFX starmapGFX = new StarmapGFX();
					starmapGFX.load(rl, lang);
					verifyNull(starmapGFX);
				}
			}));
			// -----------------------------------------------------
		}
		for (Future<?> f : ends) {
			try {
				f.get();
			} catch (Exception ex) { }
		}
		exec.shutdown();
		time = System.currentTimeMillis() -  time;
		System.out.printf("Load time: %d%n", time);
	}
	
}
