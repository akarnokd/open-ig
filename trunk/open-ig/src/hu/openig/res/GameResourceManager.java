/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.res;

import hu.openig.res.gfx.CommonGFX;
import hu.openig.res.gfx.InformationGFX;
import hu.openig.res.gfx.MenuGFX;
import hu.openig.res.gfx.OptionsGFX;
import hu.openig.res.gfx.PlanetGFX;
import hu.openig.res.gfx.StarmapGFX;
import hu.openig.utils.ResourceMapper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Graphics manager for the old game's graphical resources.
 * @author karnokd
 */
public final class GameResourceManager {
	/** The sound component. */
	public final Sounds sounds;
	/** The common graphics objects. */
	public final CommonGFX commonGFX;
	/** The starmap related graphics objects. */
	public final StarmapGFX starmapGFX;
	/** The planet related graphics objects. */
	public final PlanetGFX planetGFX;
	/** The information related graphics objects. */
	public final InformationGFX infoGFX;
	/** The menu related graphics objects. */
	public final MenuGFX menuGFX;
	/** The options related graphics objects. */
	public final OptionsGFX optionsGFX;
	/**
	 * Constructor. Initializes various components.
	 * @param resMap the resource mapper.
	 */
	public GameResourceManager(final ResourceMapper resMap) {
		ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		try {
			Future<Sounds> soundsInit = exec.submit(new Callable<Sounds>() { 
				@Override public Sounds call() throws Exception { return new Sounds(resMap); } });
			
			Future<CommonGFX> cgfxInit = exec.submit(new Callable<CommonGFX>() { 
				@Override public CommonGFX call() throws Exception { return new CommonGFX(resMap); } });
			
			Future<StarmapGFX> starmapInit = exec.submit(new Callable<StarmapGFX>() { 
				@Override public StarmapGFX call() throws Exception { return new StarmapGFX(resMap); } });
			
			Future<PlanetGFX> planetInit = exec.submit(new Callable<PlanetGFX>() { 
				@Override public PlanetGFX call() throws Exception { return new PlanetGFX(resMap); } });
			
			Future<InformationGFX> infoInit = exec.submit(new Callable<InformationGFX>() { 
				@Override public InformationGFX call() throws Exception { return new InformationGFX(resMap); } });
			
			Future<MenuGFX> menuInit = exec.submit(new Callable<MenuGFX>() { 
				@Override public MenuGFX call() throws Exception { return new MenuGFX(resMap); } });

			Future<OptionsGFX> optionsInit = exec.submit(new Callable<OptionsGFX>() { 
				@Override public OptionsGFX call() throws Exception { return new OptionsGFX(resMap); } });
			
			try {
				sounds = soundsInit.get();
				commonGFX = cgfxInit.get();
				starmapGFX = starmapInit.get();
				planetGFX = planetInit.get();
				infoGFX = infoInit.get();
				menuGFX = menuInit.get();
				optionsGFX = optionsInit.get();
			} catch (ExecutionException ex) {
				throw new RuntimeException(ex);
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		} finally {
			exec.shutdown();
		}
	}
}
