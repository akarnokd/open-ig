/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.res.gfx;

import hu.openig.utils.ImageUtils;
import hu.openig.utils.PACFile;
import hu.openig.utils.PCXImage;
import hu.openig.utils.ResourceMapper;
import hu.openig.utils.PACFile.PACEntry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * Common graphical objects which is used by multiple screens.
 * @author karnokd, 2009.01.18.
 * @version $Revision 1.0$
 */
public class CommonGFX {
	/** The top infobar. */
	public StarmapBar top;
	/** The bottom statusbar. */
	public StarmapBar bottom;
	/** The cursors. */
	public CursorsGFX cursors;
	/** The text drawing. */
	public TextGFX text;
	/** The minimap. */
	public BufferedImage minimap;
	/** The minimaps for the various ranks (zero based). */
	public BufferedImage[] minimapInfo = new BufferedImage[5];
	/** Map of race index to (building name or building index) to building small picture. */
	public Map<Integer, Map<Object, BufferedImage>> buildingPictures = new LinkedHashMap<Integer, Map<Object, BufferedImage>>();
	/** The human race index. */
	public static final int RACE_HUMANS = 1;
	/** The building names for the various building indexes. */
	private static final String[] BUILDING_NAMES = {
		"ColonyHub",
		"PrefabHousing",
		"ApartmentBlock", 
		"Arcology",
		"NuclearPlant",
		"FusionPlant",
		"SolarPlant",
		"WaterWaporator",
		"HydroponicFoodFarm",
		"PhoodFactory",
		
		"SpaceshipFactory",
		"EquipmentFactory",
		"WeaponFactory",
		"CivilEngDevCentre",
		"MechanicsDevCentre",
		"ComputerDevCentre",
		"AIDevCentre",
		"MilitaryDevCentre",
		"TradersSpaceport",
		"MilitarySpaceport",

		"Bank",
		"TradeCentre",
		"Hospital",
		"PoliceStation",
		"FireBrigade",
		"RadarTelescope",
		"FieldTelescope",
		"PasedTelescope",
		"Bunker",
		"IonProjector",
		
		"PlasmaProjector",
		"FusionProjector",
		"MesonProjector",
		"InversionShield",
		"HyperShield",
		"Barracks",
		"Fortress",
		"Stronghold",
		"RecreationCentre",
		"Park",
		
		"Church",
		"Bar",
		"Stadium",
	};
	/** The dotted grid color. */
	public static final Color GRID_COLOR = new Color(0x783C5C);
	/** The dotted grid stroke. */
	public static final BasicStroke GRID_STROKE = new BasicStroke(1f, BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_MITER, 10f, new float[] { 1f }, 0f);
	/** The array for the ship icon images for starmap and minimaps. */
	public BufferedImage[] shipImages;
	/** The array for various zoom level rada dots. */
	public BufferedImage[] radarDots;
	/** The research rotating CD images. */
	public BufferedImage[][] researchCDs;
	/** Index for full research cd images. */
	public static final int RESEARCH_CD_FULL = 0;
	/** Index for full research cd images with red exclamation mark. */
	public static final int RESEARCH_CD_FULL_RED = 1;
	/** Index for full research cd images with orange exclamation mark. */
	public static final int RESEARCH_CD_FULL_ORANGE = 2;
	/** Index for small research cd images. */
	public static final int RESEARCH_CD = 3;
	/** Index for small research cd images with red exclamation mark. */
	public static final int RESEARCH_CD_RED = 4;
	/** Index for small research cd images with orange exclamation mark. */
	public static final int RESEARCH_CD_ORANGE = 5;
	/** Research is not allowed image. */
	public BufferedImage researchDisallowed;
	/** Time normal speed icon. */
	public BufferedImage timeNormal;
	/** Time fast speed icon. */
	public BufferedImage timeFast;
	/** Time ultrafast speed icon. */
	public BufferedImage timeUltrafast;
	/** Time none icon. */
	public BufferedImage timeNone;
	/** Time pause icon. */
	public BufferedImage timePause;
	/** Time normal speed selected icon. */
	public BufferedImage timeNormalSelected;
	/** Time fast speed selected icon. */
	public BufferedImage timeFastSelected;
	/** Time ultrafast speed selected icon. */
	public BufferedImage timeUltrafastSelected;
	/** Time pause selected icon. */
	public BufferedImage timePauseSelected;
	/** The full map background. */
	public BufferedImage fullMap;
	/** The color of the map background. */
	public Color mapBackground;
	/** Pattern used for indicating disabled buttons. */
	public BufferedImage disablingPattern;
	/** The achievement badge icon. */
	public BufferedImage achievement;
	/** Low living space warning icon. */
	public BufferedImage livingSpaceIcon;
	/** Low worker count icon. */
	public BufferedImage workerIcon;
	/** Low food icon. */
	public BufferedImage foodIcon;
	/** Low hospital icon. */
	public BufferedImage hospitalIcon;
	/** Low energy icon. */
	public BufferedImage energyIcon;
	/**
	 * Constructor. Loads the images from the specified home IG directory.
	 * @param resMap the resource mapper object
	 */
	public CommonGFX(ResourceMapper resMap) {
		BufferedImage basic = PCXImage.from(resMap.get("GFX/ALAP.PCX"), -1);
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// TOP BAR
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// get the colors from the joining point
		top = new StarmapBar();
		top.left = ImageUtils.subimage(basic, 0, 0, 400, 20);
		top.right = ImageUtils.subimage(basic, 400, 0, 240, 20);
		top.link = ImageUtils.subimage(basic, 399, 0, 1, 20);
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// BOTTOM BAR
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		bottom = new StarmapBar();
		// get the colors from the joining point
		bottom = new StarmapBar();
		bottom.left = ImageUtils.subimage(basic, 0, 20, 400, 18);
		bottom.right = ImageUtils.subimage(basic, 400, 20, 240, 18);
		bottom.link = ImageUtils.subimage(basic, 399, 20, 1, 18);
		
		BufferedImage cursorImage = PCXImage.from(resMap.get("GFX/ICONMAIN.PCX"), 0);
		cursors = new CursorsGFX();
		int idx = 0;

		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// CURSORS
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		
		cursors.pointer = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(0, 0), "Pointer");
		cursors.hand = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(0, 0), "Hand");
		cursors.target = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(10, 10), "Target");
		cursors.move = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(10, 10), "Move");
		cursors.select = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(10, 10), "Select");
		cursors.northwest = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(0, 0), "NorthWest");
		cursors.north = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(10, 0), "North");
		cursors.northeast = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(19, 0), "NorthEast");
		cursors.east = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(19, 8), "East");
		cursors.west = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(0, 8), "West");
		cursors.back = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(9, 10), "Back");
		cursors.southwest = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(0, 19), "SouthWest");
		cursors.south = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(10, 19), "South");
		cursors.southeast = toolkit.createCustomCursor(ImageUtils.subimage(cursorImage, idx++ * 32, 1, 32, 31), 
				new Point(19, 19), "SouthWest");
		
		
		text = new TextGFX(resMap.get("GFX/CHARSET1.PCX").getAbsolutePath());
		
		minimap = PCXImage.from(resMap.get("GFX/ZOOM2.PCX"), -1);
		// fix image
		minimap = minimap.getSubimage(0, 0, minimap.getWidth() - 1, minimap.getHeight());
		
		for (int i = 0; i < 5; i++) {
			minimapInfo[i] = PCXImage.from(resMap.get("GFX/INFO" + (i + 1) + ".PCX"), -1);
		}
		
		// BUILDING PICTURES
		for (int i = 1; i <= 8; i++) {
			Map<String, PACEntry> buildingPics = PACFile.mapByName(PACFile.parseFully(resMap.get("DATA/COLONYP" + i + ".PAC")));
			for (PACEntry e : buildingPics.values()) {
				int idx1 = e.filename.indexOf('.');
				int value = Integer.parseInt(e.filename.substring(0, idx1));
				setBuildingImage(i, value, BUILDING_NAMES[value], PCXImage.parse(buildingPics.get(e.filename).data, -1));
			}
		}
		
		shipImages = new BufferedImage[15];
		BufferedImage fleets = PCXImage.from(resMap.get("GFX/FLEETS.PCX"), -2);
		for (int i = 0; i < shipImages.length; i++) {
			shipImages[i] = ImageUtils.subimage(fleets, 28 + (i % 12) * 20, 7 * (i / 12), 20, 7);
		}
		radarDots = new BufferedImage[4];
		radarDots[0] = ImageUtils.subimage(fleets, 144, 13, 3, 3);
		radarDots[1] = ImageUtils.subimage(fleets, 150, 13, 3, 3);
		radarDots[2] = ImageUtils.subimage(fleets, 157, 13, 3, 3);
		radarDots[3] = ImageUtils.subimage(fleets, 164, 13, 3, 3);
		
		// Research CDs
		BufferedImage rescd = PCXImage.from(resMap.get("SCREENS/FEJL_CD.PCX"), -2);
		researchCDs = new BufferedImage[6][];
		for (int i = 0; i < 3; i++) {
			researchCDs[i] = new BufferedImage[16];
			for (int j = 0; j < researchCDs[i].length; j++) {
				researchCDs[i][j] = ImageUtils.subimage(rescd, j * 20, i * 20, 20, 19);
			}
		}
		for (int i = 0; i < 3; i++) {
			researchCDs[i] = new BufferedImage[16];
			for (int j = 0; j < researchCDs[i].length; j++) {
				researchCDs[i][j] = ImageUtils.subimage(rescd, j * 20, 60 + i * 17, 20, 17);
			}
		}
		researchDisallowed = PCXImage.from(resMap.get("SCREENS/FEJL_TAK.PCX"), -2);
		
		BufferedImage timeAnim = PCXImage.from(resMap.get("GFX/IDOANIM.PCX"), -1);
		timeNormal = ImageUtils.subimage(timeAnim, 0, 0, 14, 16);
		timeFast = ImageUtils.subimage(timeAnim, 14, 0, 14, 16);
		timeUltrafast = ImageUtils.subimage(timeAnim, 28, 0, 14, 16);
		timeNone = ImageUtils.subimage(timeAnim, 42, 0, 14, 16);
		timePause = ImageUtils.subimage(timeAnim, 42, 16, 14, 16);
		
		timeNormalSelected = ImageUtils.subimage(timeAnim, 0 * 14, 6 * 16, 14, 16);
		timeFastSelected = ImageUtils.subimage(timeAnim, 1 * 14, 6 * 16, 14, 16);
		timeUltrafastSelected = ImageUtils.subimage(timeAnim, 2 * 14, 6 * 16, 14, 16);
		timePauseSelected = ImageUtils.subimage(timeAnim, 3 * 14, 2 * 16, 14, 16);
		
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// BIGMAP
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		fullMap = PCXImage.from(resMap.get("GFX/ZOOM.PCX"), -1);
		// fix image
		fullMap = fullMap.getSubimage(0, 0, fullMap.getWidth(), 662);
		
		mapBackground = new Color(fullMap.getRGB(0, 0));
		
		disablingPattern = new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB);
		for (int i = 0; i < disablingPattern.getWidth(); i++) {
			for (int j = 0; j < disablingPattern.getHeight(); j++) {
				if ((i == 0 && j < 2) || (j == 0 && i < 2)) {
					disablingPattern.setRGB(j, i, 0xFF000000);
				}
			}
		}
		try {
			achievement = ImageIO.read(CommonGFX.class.getResource("/hu/openig/res/achievement.png"));
			workerIcon = ImageIO.read(CommonGFX.class.getResource("/hu/openig/res/worker-icon.png"));
			livingSpaceIcon = ImageIO.read(CommonGFX.class.getResource("/hu/openig/res/house-icon.png"));
			foodIcon = ImageIO.read(CommonGFX.class.getResource("/hu/openig/res/food-icon.png"));
			hospitalIcon = ImageIO.read(CommonGFX.class.getResource("/hu/openig/res/hospital-icon.png"));
			energyIcon = ImageIO.read(CommonGFX.class.getResource("/hu/openig/res/energy-icon.png"));
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	/**
	 * Set building image for a race and building type.
	 * @param race the race index
	 * @param index the building index
	 * @param name the building name
	 * @param picture the building's image
	 */
	private void setBuildingImage(int race, int index, String name, BufferedImage picture) {
		Map<Object, BufferedImage> raceBuildings = buildingPictures.get(race);
		if (raceBuildings == null) {
			raceBuildings = new LinkedHashMap<Object, BufferedImage>();
			buildingPictures.put(race, raceBuildings);
		}
		raceBuildings.put(index, picture);
		raceBuildings.put(name, picture);
	}
	/**
	 * Returns a copy of the building names array.
	 * @return the building names array 
	 */
	public String[] getBuildingNames() {
		return BUILDING_NAMES.clone();
	}
	/**
	 * Mix two colors with a factor.
	 * @param c1 the first color
	 * @param c2 the second color
	 * @param rate the mixing factor
	 * @return the mixed color
	 */
	public int mixColors(int c1, int c2, float rate) {
		return
			((int)((c1 & 0xFF0000) * rate + (c2 & 0xFF0000) * (1 - rate)) & 0xFF0000)
			| ((int)((c1 & 0xFF00) * rate + (c2 & 0xFF00) * (1 - rate)) & 0xFF00)
			| ((int)((c1 & 0xFF) * rate + (c2 & 0xFF) * (1 - rate)) & 0xFF);
	}
}
