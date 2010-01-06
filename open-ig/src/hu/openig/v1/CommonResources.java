/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.v1;

import hu.openig.v1.gfx.BackgroundGFX;
import hu.openig.v1.gfx.ColonyGFX;
import hu.openig.v1.gfx.DatabaseGFX;
import hu.openig.v1.gfx.EquipmentGFX;
import hu.openig.v1.gfx.Galaxy;
import hu.openig.v1.gfx.InfoGFX;
import hu.openig.v1.gfx.ResearchGFX;
import hu.openig.v1.gfx.SpacewarGFX;
import hu.openig.v1.gfx.StarmapGFX;
import hu.openig.v1.gfx.StatusbarGFX;
import hu.openig.v1.render.TextRenderer;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;



/**
 * Contains all common ang game specific graphical and textual resources.
 * @author karnokd, 2009.12.25.
 * @version $Revision 1.0$
 */
public class CommonResources {
	/** The main configuration object. */
	public Configuration config;
	/** The main resource locator object. */
	public ResourceLocator rl;
	/** The global and game specific labels. */
	public Labels labels;
	/** The status bar graphics. */
	public StatusbarGFX statusbar;
	/** The background graphics. */
	public BackgroundGFX background;
	/** The equipment graphics. */
	public EquipmentGFX equipment;
	/** The space war graphics. */
	public SpacewarGFX spacewar;
	/** The info graphics. */
	public InfoGFX info;
	/** The research graphics. */
	public ResearchGFX research;
	/** The colony graphics. */
	public ColonyGFX colony;
	/** The starmap graphics. */
	public StarmapGFX starmap;
	/** The database graphics. */
	public DatabaseGFX database;
	/** The game specific galaxy settings. */
	public Galaxy galaxy;
	/** The text renderer. */
	public TextRenderer text;
	/** The general control interface. */
	public GameControls control;
	/** The disabled pattern. */
	public BufferedImage disabledPattern;
	/**
	 * Constructor. Initializes and loads all resources.
	 * @param config the configuration object.
	 * @param control the general control
	 */
	public CommonResources(Configuration config, GameControls control) {
		this.config = config;
		this.control = control;
		init();
	}
	/** Initialize the resources in parallel. */
	private void init() {
		rl = config.newResourceLocator();
		final ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		try {
			try {
				Future<Labels> loadLabels = exec.submit(new Callable<Labels>() {
					@Override
					public Labels call() throws Exception {
						Labels result = new Labels();
						result.load(rl, config.language, null);
						return result;
					}
				});
				Future<StatusbarGFX> loadSB = exec.submit(new Callable<StatusbarGFX>() {
					@Override
					public StatusbarGFX call() throws Exception {
						StatusbarGFX result = new StatusbarGFX(rl);
						result.load(config.language);
						return null;
					}
				});
				Future<BackgroundGFX> loadB = exec.submit(new Callable<BackgroundGFX>() {
					@Override
					public BackgroundGFX call() throws Exception {
						BackgroundGFX result = new BackgroundGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<EquipmentGFX> loadEq = exec.submit(new Callable<EquipmentGFX>() {
					@Override
					public EquipmentGFX call() throws Exception {
						EquipmentGFX result = new EquipmentGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<SpacewarGFX> loadSW = exec.submit(new Callable<SpacewarGFX>() {
					@Override
					public SpacewarGFX call() throws Exception {
						SpacewarGFX result = new SpacewarGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<InfoGFX> loadI = exec.submit(new Callable<InfoGFX>() {
					@Override
					public InfoGFX call() throws Exception {
						InfoGFX result = new InfoGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<ResearchGFX> loadR = exec.submit(new Callable<ResearchGFX>() {
					@Override
					public ResearchGFX call() throws Exception {
						ResearchGFX result = new ResearchGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<ColonyGFX> loadC = exec.submit(new Callable<ColonyGFX>() {
					@Override
					public ColonyGFX call() throws Exception {
						ColonyGFX result = new ColonyGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<StarmapGFX> loadSM = exec.submit(new Callable<StarmapGFX>() {
					@Override
					public StarmapGFX call() throws Exception {
						StarmapGFX result = new StarmapGFX();
						result.load(rl, config.language);
						return result;
					}
				});
				Future<DatabaseGFX> loadDB = exec.submit(new Callable<DatabaseGFX>() {
					@Override
					public DatabaseGFX call() throws Exception {
						DatabaseGFX result = new DatabaseGFX(rl);
						result.load(config.language);
						return result;
					}
				});
				Future<TextRenderer> loadTR = exec.submit(new Callable<TextRenderer>() {
					@Override
					public TextRenderer call() throws Exception {
						return new TextRenderer(rl);
					}
				});
				
				labels = loadLabels.get();
				statusbar = loadSB.get();
				background = loadB.get();
				equipment = loadEq.get();
				spacewar = loadSW.get();
				info = loadI.get();
				research = loadR.get();
				colony = loadC.get();
				starmap = loadSM.get();
				database = loadDB.get();
				text = loadTR.get();
				createCustomImages();
			} catch (ExecutionException ex) { 
				config.log("ERROR", ex.getMessage(), ex);
			} catch (InterruptedException ex) { 
				config.log("ERROR", ex.getMessage(), ex);
			} 
		} finally {
			exec.shutdown();
		}
	}
	/** Create any custom images. */
	private void createCustomImages() {
		int[] disabled = { 0xFF000000, 0xFF000000, 0, 0, 0xFF000000, 0, 0, 0, 0 };
		disabledPattern = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
		disabledPattern.setRGB(0, 0, 3, 3, disabled, 0, 3);
	}
	/**
	 * Reinitialize the resources by reloading them in the new language.
	 * @param newLanguage the new language
	 */
	public void reinit(String newLanguage) {
		config.language = newLanguage;
		config.save();
		init();
	}
}
