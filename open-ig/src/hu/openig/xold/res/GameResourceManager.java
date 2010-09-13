/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.res;

import hu.openig.xold.core.BuildingLookup;
import hu.openig.xold.core.ResearchLookup;
import hu.openig.xold.core.Tile;
import hu.openig.xold.res.gfx.CommonGFX;
import hu.openig.xold.res.gfx.InformationGFX;
import hu.openig.xold.res.gfx.MenuGFX;
import hu.openig.xold.res.gfx.OptionsGFX;
import hu.openig.xold.res.gfx.PlanetGFX;
import hu.openig.xold.res.gfx.ProductionGFX;
import hu.openig.xold.res.gfx.ResearchGFX;
import hu.openig.xold.res.gfx.StarmapGFX;
import hu.openig.utils.PCXImage;
import hu.openig.utils.ResourceMapper;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Graphics manager for the old game's graphical resources.
 * @author karnokd
 */
public final class GameResourceManager implements BuildingLookup, ResearchLookup {
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
	/** The research graphics objects. */
	public final ResearchGFX researchGFX;
	/** The production graphics objects. */
	public final ProductionGFX productionGFX;
	/** The game labels. */
	public final Labels labels;
	/** The old games texts. */
	public final Texts texts;
	/** The resource mapper. */
	private final ResourceMapper resMap;
	/**
	 * Constructor. Initializes various components.
	 * @param resMap the resource mapper.
	 */
	public GameResourceManager(final ResourceMapper resMap) {
		this.resMap = resMap;
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
			
			Future<Labels> labelsInit = exec.submit(new Callable<Labels>() { 
				@Override public Labels call() throws Exception { return Labels.parse("/hu/openig/res/labels.xml"); } });
			
			Future<Texts> textsInit = exec.submit(new Callable<Texts>() { 
				@Override public Texts call() throws Exception { return new Texts(resMap); } });
			
			Future<ResearchGFX> resInit = exec.submit(new Callable<ResearchGFX>() { 
				@Override public ResearchGFX call() throws Exception { return new ResearchGFX(resMap); } });
			
			Future<ProductionGFX> prodInit = exec.submit(new Callable<ProductionGFX>() { 
				@Override public ProductionGFX call() throws Exception { return new ProductionGFX(resMap); } });
			
			try {
				sounds = soundsInit.get();
				commonGFX = cgfxInit.get();
				starmapGFX = starmapInit.get();
				planetGFX = planetInit.get();
				infoGFX = infoInit.get();
				menuGFX = menuInit.get();
				optionsGFX = optionsInit.get();
				labels = labelsInit.get();
				texts = textsInit.get();
				researchGFX = resInit.get();
				productionGFX = prodInit.get();
			} catch (ExecutionException ex) {
				throw new RuntimeException(ex);
			} catch (InterruptedException ex) {
				throw new RuntimeException(ex);
			}
		} finally {
			exec.shutdown();
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, List<Tile>> getBuildingPhases() {
		return planetGFX.buildupTiles;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, List<Tile>> getDamagedBuildingPhases() {
		return planetGFX.buildupDamagedTiles;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public PCXImage getBuildingTile(String techId, int index, boolean damaged) {
		return (damaged ? planetGFX.damagedBuildings : planetGFX.regularBuildings).get(techId).get(index);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getDescriptionLabels(int index) {
		return texts.buildingInfo.get(index);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getNameLabel(int index) {
		return texts.buildingName.get(index);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage getThumbnail(String techId, int index) {
		return planetGFX.buildingThumbnails.get(techId).get(index);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getResearchDescription(int index) {
		return texts.equipmentInfo.get(index);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getResearchName(int index) {
		return texts.equipmentName.get(index);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage getInfoImage(int index) {
		return infoGFX.researchInfoImage.get(index);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage getWiredInfoImage(int index) {
		return infoGFX.researchWiredInfoImage.get(index);
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getAnimation(int imageIndex) {
		return resMap.get("EQ_ANIMS/INV" + imageIndex + ".ANI");
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getAnimationWired(int imageIndex) {
		return resMap.get("EQ_ANIMW/WIR" + imageIndex + ".ANI");
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public BufferedImage getSmallImage(int imageIndex) {
		return researchGFX.smallImages.get(imageIndex);
	}
	/**
	 * Returns a map containing the rotation images for various zoom levels.
	 * @param planetString the planet surface type string
	 * @return the map from zoom level to list of rotation images
	 */
	public Map<Integer, List<BufferedImage>> getRotations(String planetString) {
		return starmapGFX.starmapPlanets.get(planetString);
	}
}
