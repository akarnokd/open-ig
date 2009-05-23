/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.res.gfx;

import hu.openig.core.RoadType;
import hu.openig.core.Tile;
import hu.openig.utils.ImageUtils;
import hu.openig.utils.PACFile;
import hu.openig.utils.PCXImage;
import hu.openig.utils.ResourceMapper;
import hu.openig.utils.PACFile.PACEntry;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains images for rendering the planetary surfaces.
 * @author karnokd, 2009.01.18.
 * @version $Revision 1.0$
 */
public class PlanetGFX {
	/** A map from surface type to tile index to tile object. */
	private final Map<Integer, Map<Integer, Tile>> surfaceImages;
	/** The map name to the map pack. */
	private final Map<String, PACEntry> maps;
	/** The various rectangular frames for highlight. */
	private final BufferedImage[] frames;
	/** Building button image. */
	public final BufferedImage buildingButton;
	/** Left top image. */
	public final BufferedImage leftTop;
	/** Left bottom image. */
	public final BufferedImage leftBottom;
	/** Left filler image. */
	public final BufferedImage leftFiller;
	/** Radar button image. */
	public final BufferedImage radarButton;
	/** Building info button image. */
	public final BufferedImage buildingInfoButton;
	/** Top right image. */
	public final BufferedImage rightTop;
	/** Right filler image. */
	public final BufferedImage rightFiller;
	/** Right bottom image. */
	public final BufferedImage rightBottom;
	/** Button for showing/hiding the bottom right buttons of colony, info, etc. */
	public final BufferedImage screenButtons;
	/** Colony info button image. */
	public final BufferedImage colonyInfoButton;
	/** Planet button image. */
	public final BufferedImage planetButton;
	/** Starmap button image. */
	public final BufferedImage starmapButton;
	/** Bridge button image. */
	public final BufferedImage bridgeButton;
	/** Colony info button down image. */
	public final BufferedImage colonyInfoButtonDown;
	/** Planet button down image. */
	public final BufferedImage planetButtonDown;
	/** Starmap button down image. */
	public final BufferedImage starmapButtonDown;
	/** Bridge button down image. */
	public final BufferedImage bridgeButtonDown;
	/** Build panel image. */
	public final BufferedImage buildPanel;
	/** Radar panel image. */
	public final BufferedImage radarPanel;
	/** Building info panel. */
	public final BufferedImage buildingInfoPanel;
	/** The build scroll up button down state. */
	public final BufferedImage buildScrollUpDown;
	/** The building scroll down butoon down state. */
	public final BufferedImage buildScrollDownDown;
	/** An unclickabel scroll image. */
	public final BufferedImage buildScrollNone;
	/** The build button pressed state. */
	public final BufferedImage buildDown;
	/** The build list button pressed state. */
	public final BufferedImage listDown;
	/** The demolish button pressed. */
	public final BufferedImage demolishDown;
	/** Repair percentage viewer. */
	public final BufferedImage repairPercent;
	/** Damage percent image. */
	public final BufferedImage damagedPercent;
	/** Not active image. */
	public final BufferedImage notActive;
	/** Offline image. */
	public final BufferedImage offline;
	/** Damaged image. */
	public final BufferedImage damaged;
	/** No energy image. */
	public final BufferedImage noEnergy;
	/** Build completed percent. */
	public final BufferedImage completedPercent;
	/** The vehicle selection mini window. */
	public final BufferedImage vehicleWindow;
	/** The start battle button. */
	public final BufferedImage startBattle;
	/** The start battle button pressed. */
	public final BufferedImage startBattleDown;
	/** The empty building button. */
	public final BufferedImage buildingNoButton;
	/** Map from tech id to building index to the thumbnail image. */
	public final Map<String, Map<Integer, BufferedImage>> buildingThumbnails = new HashMap<String, Map<Integer, BufferedImage>>();
	/** The regular building images from tech id to building index to the PCX image. */
	public final Map<String, Map<Integer, PCXImage>> regularBuildings = new HashMap<String, Map<Integer, PCXImage>>();
	/** The damaged building images from tech id to building index to the PCX image. */
	public final Map<String, Map<Integer, PCXImage>> damagedBuildings = new HashMap<String, Map<Integer, PCXImage>>();
	/** The road tile map from tech id to road type to tile. */
	public final Map<String, Map<RoadType, Tile>> roadTiles = new HashMap<String, Map<RoadType, Tile>>();
	/** The road tile to road type reverse lookup table. */
	public final Map<String, Map<Tile, RoadType>> tileRoads = new HashMap<String, Map<Tile, RoadType>>();
	/** Tech id to list of various buildup phase tiles. */
	public final Map<String, List<Tile>> buildupTiles = new HashMap<String, List<Tile>>();
	/** Tech id to list of various buildup damaged phase tiles. */
	public final Map<String, List<Tile>> buildupDamagedTiles = new HashMap<String, List<Tile>>();
	/** Two images for the no energy icon above buildings. */
	public final BufferedImage[] buildingNoEnergy = new BufferedImage[2];
	/** Two images for the building offline icon above buildings. */
	public final BufferedImage[] buildingOff = new BufferedImage[2];
	/** Two images for the building repairing icon above buildings. */
	public final BufferedImage[] buildingRepair = new BufferedImage[2];
	/**
	 * Constructor. Loads all graphics necessary for planetary rendering.
	 * @param resMap the resource mapper
	 */
	public PlanetGFX(ResourceMapper resMap) {
		maps = PACFile.mapByName(PACFile.parseFully(resMap.get("DATA/MAP.PAC")));
		surfaceImages = new HashMap<Integer, Map<Integer, Tile>>();
		for (int i = 1; i < 8; i++) {
			Map<Integer, Tile> actual = new HashMap<Integer, Tile>();
			surfaceImages.put(i, actual);
			for (PACEntry e : PACFile.parseFully(resMap.get("DATA/FELSZIN" + i + ".PAC"))) {
				int idx = e.filename.indexOf('.');
				Tile t = new Tile();
				t.rawImage = new PCXImage(e.data);
//				t.image = PCXImage.parse(e.data, -2);
				actual.put(Integer.parseInt(e.filename.substring(0, idx)), t);
			}
		}
		adjustTileParams();

		frames = new BufferedImage[4];
		BufferedImage keretek = PCXImage.from(resMap.get("GFX/KERET.PCX"), -2);
		frames[0] = ImageUtils.subimage(keretek, 0, 0, 57, 28);
		frames[1] = ImageUtils.subimage(keretek, 58, 0, 57, 28);
		frames[2] = ImageUtils.subimage(keretek, 116, 0, 57, 28);
		frames[3] = ImageUtils.subimage(keretek, 174, 0, 57, 28);
		
		BufferedImage colony = PCXImage.from(resMap.get("SCREENS/COLONY.PCX"), -1);
		buildingButton = ImageUtils.subimage(colony, 0, 0, 19, 170);
		leftTop = ImageUtils.subimage(colony, 0, 169, 20, 57);
		leftFiller = ImageUtils.subimage(colony, 0, 226, 20, 2);
		leftBottom = ImageUtils.subimage(colony, 0, 226, 20, 57);
		radarButton = ImageUtils.subimage(colony, 0, 283, 19, 159);
		
		buildingInfoButton = ImageUtils.subimage(colony, 620, 0, 20, 146);
		rightTop = ImageUtils.subimage(colony, 620, 146, 20, 130);
		rightFiller = ImageUtils.subimage(colony, 620, 276, 20, 2);
		rightBottom = ImageUtils.subimage(colony, 620, 276, 20, 137);
		screenButtons = ImageUtils.subimage(colony, 620, 413, 20, 29);
		
		colonyInfoButton = ImageUtils.subimage(colony, 200, 414, 105, 28);
		//planetButton = ImageUtils.subimage(colony, 305, 414, 105, 28);
		starmapButton = ImageUtils.subimage(colony, 410, 414, 105, 28);
		bridgeButton = ImageUtils.subimage(colony, 515, 414, 105, 28);
		
		buildPanel = ImageUtils.subimage(colony, 19, 0, 181, 170);
		radarPanel = ImageUtils.subimage(colony, 19, 282, 181, 160);
		buildingInfoPanel = ImageUtils.subimage(colony, 424, 0, 196, 147);
		
		BufferedImage colonyx = PCXImage.from(resMap.get("SCREENS/COLONY_X.PCX"), -1);

		colonyInfoButtonDown = ImageUtils.subimage(colonyx, 80, 198, 105, 28);
		planetButton = ImageUtils.subimage(colonyx, 185, 170, 105, 28);
		planetButtonDown = ImageUtils.subimage(colonyx, 185, 198, 105, 28);
		starmapButtonDown = ImageUtils.subimage(colonyx, 290, 198, 105, 28);
		bridgeButtonDown = ImageUtils.subimage(colonyx, 395, 198, 105, 28);
		
		buildScrollUpDown = ImageUtils.subimage(colonyx, 501, 163, 21, 45);
		buildScrollDownDown = ImageUtils.subimage(colonyx, 523, 163, 21, 45);
		buildScrollNone = ImageUtils.subimage(colonyx, 581, 181, 22, 48);
		
		buildDown = ImageUtils.subimage(colonyx, 544, 160, 81, 21);
		listDown = ImageUtils.subimage(colonyx, 500, 208, 81, 21);
		demolishDown = ImageUtils.subimage(colonyx, 611, 181, 29, 90);
		
		repairPercent = ImageUtils.subimage(colonyx, 0, 226, 145, 18);
		damagedPercent = ImageUtils.subimage(colonyx, 145, 226, 145, 18);
		notActive = ImageUtils.subimage(colonyx, 290, 226, 145, 18);
		offline = ImageUtils.subimage(colonyx, 0, 244, 145, 18);
		damaged = ImageUtils.subimage(colonyx, 145, 244, 145, 18);
		noEnergy = ImageUtils.subimage(colonyx, 290, 244, 145, 18);
		completedPercent = ImageUtils.subimage(colonyx, 435, 244, 145, 18);

		BufferedImage colonzx = PCXImage.from(resMap.get("SCREENS/COLONZ_X.PCX"), -1);

		vehicleWindow = ImageUtils.subimage(colonzx, 80, 0, 140, 79);
		buildingNoButton = ImageUtils.subimage(colonzx, 0, 0, 19, 170);
		startBattle = ImageUtils.subimage(colonzx, 394, 170, 106, 28);
		startBattleDown = ImageUtils.subimage(colonzx, 394, 198, 106, 28);
		
		for (int i = 1; i < 9; i++) {
			Map<Integer, BufferedImage> thumbs = new HashMap<Integer, BufferedImage>();
			// thumbnails
			buildingThumbnails.put(String.valueOf(i), thumbs);
			for (PACEntry e : PACFile.parseFully(resMap.get("DATA/COLONYP" + i + ".PAC"))) {
				int idx = Integer.parseInt(e.filename.substring(0, 3));
				thumbs.put(idx, PCXImage.parse(e.data, -2));
			}
			// colony buildings
			Map<String, PACEntry> entries = PACFile.mapByName(PACFile.parseFully(resMap.get("DATA/COLONY" + i + ".PAC")));
			Map<Integer, PCXImage> regulars = new HashMap<Integer, PCXImage>();
			regularBuildings.put(String.valueOf(i), regulars);
			Map<Integer, PCXImage> damageds = new HashMap<Integer, PCXImage>();
			damagedBuildings.put(String.valueOf(i), damageds);
			for (int j = 0; j < 43; j++) {
				PACEntry b1 = entries.get(String.format("%03d.PCX", j));
				String s1 = String.format("D%02d.PCX", j);
				PACEntry d1 = entries.get(s1);
				if (b1 != null && d1 != null) {
					PACEntry b2 = entries.get(String.format("%03dB.PCX", j));
					PCXImage img = new PCXImage(b1.data);
					if (b2 != null) { // not all building types are available for all techs
						PCXImage img1 = new PCXImage(b2.data);
						img.addRight(img1.getRaw(), img1.getPalette(), img1.getWidth());
					}
					regulars.put(j, img);
					// damaged image
					String s2 = String.format("D%02dB.PCX", j);
					PACEntry d2 = entries.get(s2);
					img = new PCXImage(d1.data);
					if (b2 != null) {
						PCXImage img1 = new PCXImage(d2.data);
						img.addRight(img1.getRaw(), img1.getPalette(), img1.getWidth());
					}
					damageds.put(j, img);
				}
			}
			
			Map<RoadType, Tile> roads = new HashMap<RoadType, Tile>();
			roadTiles.put(String.valueOf(i), roads);
			Map<Tile, RoadType> tiles = new HashMap<Tile, RoadType>();
			tileRoads.put(String.valueOf(i), tiles);
			
			for (RoadType rt : RoadType.values()) {
				Tile t = new Tile();
				t.rawImage = new PCXImage(entries.get(String.format("U%02d.PCX", rt.index)).data);
				t.width = 1;
				t.height = 1;
				roads.put(rt, t);
				tiles.put(t, rt);
			}
			switch (i) {
			case 1:
				tech1Build(entries);
				break;
			case 2:
				tech2Build(entries);
				break;
			case 3:
				tech3Build(entries);
				break;
			case 4:
				tech4Build(entries);
				break;
			case 5:
				tech5Build(entries);
				break;
			case 6:
				tech6Build(entries);
				break;
			case 7:
				tech7Build(entries);
				break;
			case 8:
				tech8Build(entries);
				break;
			default:
			}
		}
	}
	/**
	 * Ajusts varios surface tile geometry. These tiles are not the
	 * default assumed 1x1 tiles. Some of them even needs pixel height adjustments.
	 */
	private void adjustTileParams() {
		// DESERT TYPE SURFACE TILE ADJUSTMENTS
		for (int i = 55; i <= 68; i++) {
			setParams(1, i, 2, 2);
		}
		setParams(1, 69, 2, 1);
		setParams(1, 70, 3, 2);
		for (int i = 71; i <= 74; i++) {
			setParams(1, i, 3, 3);
		}
		setParams(1, 75, 4, 3);
		for (int i = 76; i <= 79; i++) {
			setParams(1, i, 4, 4);
		}
		// ICE TYPE SURFACE TILE ADJUSTMENTS
		for (int i = 33; i <= 37; i++) {
			setParams(2, i, 1, 2);
		}
		for (int i = 38; i <= 39; i++) {
			setParams(2, i, 2, 1);
		}
		for (int i = 40; i <= 54; i++) {
			setParams(2, i, 2, 2);
		}
		for (int i = 55; i <= 59; i++) {
			setParams(2, i, 2, 3);
		}
		for (int i = 60; i <= 62; i++) {
			setParams(2, i, 2, 4);
		}
		for (int i = 63; i <= 67; i++) {
			setParams(2, i, 3, 2);
		}
		setParams(2, 68, 3, 3);
		for (int i = 69; i <= 73; i++) {
			setParams(2, i, 4, 2);
		}
		for (int i = 74; i <= 78; i++) {
			setParams(2, i, 4, 4);
		}
		setParams(2, 79, 2, 2);
		// CRATER TYPE SURFACE TILE ADJUSTMENTS
		setParams(3, 61, 1, 2);
		setParams(3, 62, 2, 1);
		for (int i = 63; i <= 86; i++) {
			setParams(3, i, 2, 2);
		}
		for (int i = 87; i <= 89; i++) {
			setParams(3, i, 2, 3);
		}
		setParams(3, 91, 3, 2);
		for (int i = 92; i <= 94; i++) {
			setParams(3, i, 3, 3);
		}
		setParams(3, 95, 3, 4);
		setParams(3, 96, 4, 2);
		setParams(3, 97, 4, 4);
		// ROCKY TYPE SURFACE TILE ADJUSTMENTS
		for (int i = 42; i <= 45; i++) {
			setParams(4, i, 1, 2);
		}
		for (int i = 46; i <= 58; i++) {
			setParams(4, i, 2, 2);
		}
		for (int i = 59; i <= 64; i++) {
			setParams(4, i, 2, 4);
		}
		for (int i = 65; i <= 71; i++) {
			setParams(4, i, 3, 3);
		}
		for (int i = 72; i <= 73; i++) {
			setParams(4, i, 4, 4);
		}
		surfaceImages.get(4).get(72).heightCorrection = 1;
		setParams(4, 74, 5, 5);
		// LIQUID TYPE SURFACE TILE ADJUSTMENTS
		for (int i = 70; i <= 72; i++) {
			setParams(5, i, 1, 2);
		}
		setParams(5, 73, 2, 1);
		for (int i = 74; i <= 79; i++) {
			setParams(5, i, 2, 2);
		}
		for (int i = 86; i <= 103; i++) {
			setParams(5, i, 2, 2);
		}
		for (int i = 104; i <= 105; i++) {
			setParams(5, i, 3, 3);
		}
		setParams(5, 106, 2, 3);
		for (int i = 107; i <= 109; i++) {
			setParams(5, i, 3, 2);
		}
		for (int i = 110; i <= 116; i++) {
			setParams(5, i, 3, 3);
		}
		setParams(5, 117, 4, 3);
		setParams(5, 118, 3, 3);
		for (int i = 119; i <= 121; i++) {
			setParams(5, i, 4, 4);
		}
		for (int i = 122; i <= 123; i++) {
			setParams(5, i, 3, 3);
		}
		
		// EARTH TYPE SURFACE TILE ADJUSTMENTS
		setParams(6, 108, 2, 1);
		for (int i = 109; i <= 130; i++) {
			setParams(6, i, 2, 2);
		}
		for (int i = 131; i <= 139; i++) {
			setParams(6, i, 2, 3);
		}
		for (int i = 140; i <= 147; i++) {
			setParams(6, i, 3, 2);
		}
		for (int i = 148; i <= 149; i++) {
			setParams(6, i, 3, 3);
		}
		for (int i = 150; i <= 158; i++) {
			setParams(6, i, 4, 4);
		}
		surfaceImages.get(6).get(157).heightCorrection = 1;
		setParams(6, 159, 5, 3);
		setParams(6, 160, 6, 5);
		
		setParams(6, 161, 6, 6);
		// NECTOPLASM TYPE SURFACE TILE ADJUSTMENTS
		for (int i = 27; i <= 42; i++) {
			setParams(7, i, 2, 2);
		}
		setParams(7, 43, 2, 3);
		setParams(7, 44, 3, 2);
		setParams(7, 45, 3, 3);
		for (int i = 46; i <= 48; i++) {
			setParams(7, i, 3, 2);
		}
		for (int i = 49; i <= 57; i++) {
			setParams(7, i, 4, 4);
		}
		surfaceImages.get(7).get(57).heightCorrection = 1;
		
		// call createimage on the 1x1 tiles
		for (Map<Integer, Tile> tiles : surfaceImages.values()) {
			for (Tile t : tiles.values()) {
				if (t.width == 1 && t.height == 1) {
					t.createImage(1.0f);
				}
			}
		}
	}
	/**
	 * Set geimetrical parameters on a specific surface type and tile index.
	 * @param surface the surface type index
	 * @param tile the tile index
	 * @param width the tile width
	 * @param height the tile height
	 */
	private void setParams(int surface, int tile, int width, int height) {
		Tile t = surfaceImages.get(surface).get(tile);
		t.width = width;
		t.height = height;
		// tile size doesnt change, so create the strips a priory
//		if (width + height - 1 > 1) {
//			t.strips = new BufferedImage[width + height - 1];
//		}
		t.createImage(1.0f);
	}
	/**
	 * Returns the map for a surface name.
	 * @param name the surface name
	 * @return the package entry
	 */
	public PACEntry getMap(String name) {
		return maps.get(name);
	}
	/**
	 * Returns the surface tile for a given surface type and tile index. 
	 * @param surfaceType the surface type index
	 * @param index the tile index
	 * @return the tile
	 */
	public Tile getSurfaceTile(int surfaceType, int index) {
		Map<Integer, Tile> actual = surfaceImages.get(surfaceType);
		return actual != null ? actual.get(index) : null;
	}
	/**
	 * Returns a map for surface image tiles for a particular surface type.
	 * @param surfaceType the surface type index
	 * @return the map from tile index to tile
	 */
	public Map<Integer, Tile> getSurfaceTiles(int surfaceType) {
		return surfaceImages.get(surfaceType);
	}
	/**
	 * Returns a default colored frame.
	 * @param index the index of the frame
	 * @return the RGBA image of the frame
	 */
	public BufferedImage getFrame(int index) {
		return frames[index];
	}
	/**
	 * Extract build progress, and damaged build progress from the tech 1 data file.
	 * (Remark: Each tech id has different layout in these files.
	 * @param entries the entries in the pac file
	 */
	private void tech1Build(Map<String, PACEntry> entries) {
		List<Tile> tiles = new ArrayList<Tile>();
		buildupTiles.put("1", tiles);
		
		PCXImage buildup1 = new PCXImage(entries.get("EPULES1.PCX").data);
		tiles.add(new Tile(buildup1.subimage(1, 1, 55, 27), 1, 1));
		tiles.add(new Tile(buildup1.subimage(1, 29, 55, 30), 1, 1));
		tiles.add(new Tile(buildup1.subimage(1, 60, 55, 40), 1, 1));
		tiles.add(new Tile(buildup1.subimage(1, 101, 55, 27), 1, 1));
		tiles.add(new Tile(buildup1.subimage(1, 129, 55, 39), 1, 1));
		tiles.add(new Tile(buildup1.subimage(1, 169, 55, 36), 1, 1));
		
		tiles.add(new Tile(buildup1.subimage(57, 1, 55, 27), 1, 1));
		tiles.add(new Tile(buildup1.subimage(57, 29, 55, 30), 1, 1));
		tiles.add(new Tile(buildup1.subimage(57, 60, 55, 36), 1, 1));
		tiles.add(new Tile(buildup1.subimage(57, 97, 55, 33), 1, 1));
		tiles.add(new Tile(buildup1.subimage(57, 131, 55, 33), 1, 1));
		tiles.add(new Tile(buildup1.subimage(57, 165, 55, 41), 1, 1));
		
		tiles.add(new Tile(buildup1.subimage(113, 1, 55, 49), 1, 1));
		tiles.add(new Tile(buildup1.subimage(113, 51, 55, 38), 1, 1));
		tiles.add(new Tile(buildup1.subimage(113, 90, 55, 67), 1, 1));
		tiles.add(new Tile(buildup1.subimage(113, 158, 55, 51), 1, 1));
		
		tiles.add(new Tile(buildup1.subimage(169, 1, 55, 83), 1, 1));
		tiles.add(new Tile(buildup1.subimage(169, 85, 55, 48), 1, 1));
		tiles.add(new Tile(buildup1.subimage(169, 134, 55, 68), 1, 1));
		
		tiles.add(new Tile(buildup1.subimage(225, 1, 55, 73), 1, 1));
		tiles.add(new Tile(buildup1.subimage(225, 75, 55, 49), 1, 1));
		tiles.add(new Tile(buildup1.subimage(225, 125, 55, 41), 1, 1));
		tiles.add(new Tile(buildup1.subimage(225, 167, 55, 48), 1, 1));
		
		PCXImage buildup2 = new PCXImage(entries.get("EPULES2.PCX").data);
		tiles.add(new Tile(buildup2.subimage(1, 1, 55, 82), 1, 1));
		tiles.add(new Tile(buildup2.subimage(1, 84, 55, 54), 1, 1));
		tiles.add(new Tile(buildup2.subimage(1, 139, 55, 54), 1, 1));
		tiles.add(new Tile(buildup2.subimage(57, 1, 55, 54), 1, 1));
		tiles.add(new Tile(buildup2.subimage(57, 56, 55, 66), 1, 1));
		
		List<Tile> damagedTiles = new ArrayList<Tile>();
		buildupDamagedTiles.put("1", damagedTiles);
		PCXImage damage = new PCXImage(entries.get("DAMAGE.PCX").data);
		damagedTiles.add(new Tile(damage.subimage(1, 1, 55, 31), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 33, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 61, 55, 34), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 96, 55, 36), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 133, 55, 41), 1, 1));
		
		damagedTiles.add(new Tile(damage.subimage(57, 1, 55, 33), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 35, 55, 31), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 67, 55, 37), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 105, 55, 51), 1, 1));
		
		damagedTiles.add(new Tile(damage.subimage(113, 1, 55, 47), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(113, 49, 55, 40), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(113, 90, 55, 42), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(113, 133, 55, 36), 1, 1));
		
		PCXImage icons = new PCXImage(entries.get("ICON.PCX").data);
		buildingNoEnergy[0] = icons.subimage(0, 0, 20, 30).toBufferedImage(-2);
		buildingNoEnergy[1] = icons.subimage(21, 0, 20, 30).toBufferedImage(-2);

		buildingOff[0] = icons.subimage(42, 0, 20, 30).toBufferedImage(-2);
		buildingOff[1] = icons.subimage(63, 0, 20, 30).toBufferedImage(-2);

		buildingRepair[0] = icons.subimage(84, 0, 20, 30).toBufferedImage(-2);
		buildingRepair[1] = icons.subimage(105, 0, 20, 30).toBufferedImage(-2);
}
	/**
	 * Extract build progress, and damaged build progress from the tech 2 data file.
	 * (Remark: Each tech id has different layout in these files.
	 * @param entries the entries in the pac file
	 */
	private void tech2Build(Map<String, PACEntry> entries) {
		List<Tile> tiles = new ArrayList<Tile>();
		buildupTiles.put("2", tiles);
		
		PCXImage buildup = new PCXImage(entries.get("EPULES1.PCX").data);
		tiles.add(new Tile(buildup.subimage(1, 1, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 29, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 57, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 85, 55, 44), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 130, 55, 36), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 167, 55, 43), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 211, 55, 50), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 262, 55, 46), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 309, 55, 46), 1, 1));
		
		tiles.add(new Tile(buildup.subimage(57, 1, 55, 64), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 66, 55, 31), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 98, 55, 38), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 137, 55, 38), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 176, 55, 47), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 224, 55, 42), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 267, 55, 60), 1, 1));
		
		List<Tile> damagedTiles = new ArrayList<Tile>();
		buildupDamagedTiles.put("2", damagedTiles);
		PCXImage damage = new PCXImage(entries.get("DAMAGE.PCX").data);
		damagedTiles.add(new Tile(damage.subimage(1, 1, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 29, 55, 44), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 74, 55, 34), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 109, 55, 28), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 138, 55, 64), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 203, 55, 43), 1, 1));
		
		damagedTiles.add(new Tile(damage.subimage(57, 1, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 29, 55, 46), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 76, 55, 47), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 124, 55, 39), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 164, 55, 38), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 203, 55, 27), 1, 1));
	}
	/**
	 * Extract build progress, and damaged build progress from the tech 3 data file.
	 * (Remark: Each tech id has different layout in these files.
	 * @param entries the entries in the pac file
	 */
	private void tech3Build(Map<String, PACEntry> entries) {
		List<Tile> tiles = new ArrayList<Tile>();
		buildupTiles.put("3", tiles);
		
		PCXImage buildup = new PCXImage(entries.get("EPULES1.PCX").data);
		tiles.add(new Tile(buildup.subimage(1, 1, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 29, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 57, 55, 40), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 98, 55, 37), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 136, 55, 40), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 177, 55, 40), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 218, 55, 57), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 276, 55, 57), 1, 1));

		tiles.add(new Tile(buildup.subimage(57, 1, 55, 39), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 41, 55, 50), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 92, 55, 50), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 143, 55, 50), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 194, 55, 58), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 253, 55, 58), 1, 1));
		
		List<Tile> damagedTiles = new ArrayList<Tile>();
		buildupDamagedTiles.put("3", damagedTiles);
		PCXImage damage = new PCXImage(entries.get("DAMAGE.PCX").data);
		damagedTiles.add(new Tile(damage.subimage(1, 1, 55, 37), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 39, 55, 58), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 98, 55, 57), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 156, 55, 39), 1, 1));

		damagedTiles.add(new Tile(damage.subimage(57, 1, 55, 50), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 52, 55, 40), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 93, 55, 40), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 134, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 162, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 190, 55, 50), 1, 1));

		damagedTiles.add(new Tile(damage.subimage(113, 1, 55, 50), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(113, 52, 55, 58), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(113, 111, 55, 40), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(113, 152, 55, 57), 1, 1));
	}
	/**
	 * Extract build progress, and damaged build progress from the tech 4 data file.
	 * (Remark: Each tech id has different layout in these files.
	 * @param entries the entries in the pac file
	 */
	private void tech4Build(Map<String, PACEntry> entries) {
		List<Tile> tiles = new ArrayList<Tile>();
		buildupTiles.put("4", tiles);
		
		PCXImage buildup = new PCXImage(entries.get("EPULES1.PCX").data);
		// seems to be the same as techid 3
		tiles.add(new Tile(buildup.subimage(1, 1, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 29, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 57, 55, 40), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 98, 55, 37), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 136, 55, 40), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 177, 55, 40), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 218, 55, 57), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 276, 55, 57), 1, 1));

		tiles.add(new Tile(buildup.subimage(57, 1, 55, 39), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 41, 55, 50), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 92, 55, 50), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 143, 55, 50), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 194, 55, 58), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 253, 55, 58), 1, 1));
		
		
		List<Tile> damagedTiles = new ArrayList<Tile>();
		buildupDamagedTiles.put("4", damagedTiles);
		PCXImage damage = new PCXImage(entries.get("DAMAGE.PCX").data);
		// seems to be the same as techid 3
		damagedTiles.add(new Tile(damage.subimage(1, 1, 55, 37), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 39, 55, 58), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 98, 55, 57), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 156, 55, 39), 1, 1));

		damagedTiles.add(new Tile(damage.subimage(57, 1, 55, 50), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 52, 55, 40), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 93, 55, 40), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 134, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 162, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 190, 55, 50), 1, 1));

		damagedTiles.add(new Tile(damage.subimage(113, 1, 55, 50), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(113, 52, 55, 58), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(113, 111, 55, 40), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(113, 152, 55, 57), 1, 1));
	}
	/**
	 * Extract build progress, and damaged build progress from the tech 5 data file.
	 * (Remark: Each tech id has different layout in these files.
	 * @param entries the entries in the pac file
	 */
	private void tech5Build(Map<String, PACEntry> entries) {
		List<Tile> tiles = new ArrayList<Tile>();
		buildupTiles.put("5", tiles);
		
		PCXImage buildup = new PCXImage(entries.get("EPULES1.PCX").data);
		tiles.add(new Tile(buildup.subimage(1, 1, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 29, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 57, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 85, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 113, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 141, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 169, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 198, 55, 36), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 235, 55, 33), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 269, 55, 41), 1, 1));
		
		tiles.add(new Tile(buildup.subimage(57, 1, 55, 56), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 58, 55, 34), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 93, 55, 40), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 134, 55, 58), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 193, 55, 36), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 230, 55, 64), 1, 1));
		
		tiles.add(new Tile(buildup.subimage(113, 1, 55, 67), 1, 1));
		tiles.add(new Tile(buildup.subimage(113, 69, 55, 77), 1, 1));
		tiles.add(new Tile(buildup.subimage(113, 147, 55, 99), 1, 1));
		tiles.add(new Tile(buildup.subimage(113, 247, 55, 55), 1, 1));
		tiles.add(new Tile(buildup.subimage(113, 289, 55, 54), 1, 1));
		
		List<Tile> damagedTiles = new ArrayList<Tile>();
		buildupDamagedTiles.put("5", damagedTiles);
		PCXImage damage = new PCXImage(entries.get("DAMAGE.PCX").data);
		damagedTiles.add(new Tile(damage.subimage(1, 1, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 29, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 57, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 85, 55, 27), 1, 1));
		
		damagedTiles.add(new Tile(damage.subimage(57, 1, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 29, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 57, 55, 28), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 86, 55, 27), 1, 1));
	}
	/**
	 * Extract build progress, and damaged build progress from the tech 6 data file.
	 * (Remark: Each tech id has different layout in these files.
	 * @param entries the entries in the pac file
	 */
	private void tech6Build(Map<String, PACEntry> entries) {
		List<Tile> tiles = new ArrayList<Tile>();
		buildupTiles.put("6", tiles);
		
		PCXImage buildup = new PCXImage(entries.get("EPULES1.PCX").data);
		// seems to be the same as techid 2
		tiles.add(new Tile(buildup.subimage(1, 1, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 29, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 57, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 85, 55, 44), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 130, 55, 36), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 167, 55, 43), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 211, 55, 50), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 262, 55, 46), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 309, 55, 46), 1, 1));
		
		tiles.add(new Tile(buildup.subimage(57, 1, 55, 64), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 66, 55, 31), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 98, 55, 38), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 137, 55, 38), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 176, 55, 47), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 224, 55, 42), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 267, 55, 60), 1, 1));
		
		List<Tile> damagedTiles = new ArrayList<Tile>();
		buildupDamagedTiles.put("6", damagedTiles);
		// seems to be the same as techid 2
		PCXImage damage = new PCXImage(entries.get("DAMAGE.PCX").data);
		damagedTiles.add(new Tile(damage.subimage(1, 1, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 29, 55, 44), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 74, 55, 34), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 109, 55, 28), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 138, 55, 64), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 203, 55, 43), 1, 1));
		
		damagedTiles.add(new Tile(damage.subimage(57, 1, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 29, 55, 46), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 76, 55, 47), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 124, 55, 39), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 164, 55, 38), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 203, 55, 27), 1, 1));
	}
	/**
	 * Extract build progress, and damaged build progress from the tech 7 data file.
	 * (Remark: Each tech id has different layout in these files.
	 * @param entries the entries in the pac file
	 */
	private void tech7Build(Map<String, PACEntry> entries) {
		List<Tile> tiles = new ArrayList<Tile>();
		buildupTiles.put("7", tiles);
		
		PCXImage buildup = new PCXImage(entries.get("EPULES1.PCX").data);
		// seems to be the same as techid 5
		tiles.add(new Tile(buildup.subimage(1, 1, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 29, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 57, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 85, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 113, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 141, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 169, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 198, 55, 36), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 235, 55, 33), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 269, 55, 41), 1, 1));
		
		tiles.add(new Tile(buildup.subimage(57, 1, 55, 56), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 58, 55, 34), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 93, 55, 40), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 134, 55, 58), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 193, 55, 36), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 230, 55, 64), 1, 1));
		
		tiles.add(new Tile(buildup.subimage(113, 1, 55, 67), 1, 1));
		tiles.add(new Tile(buildup.subimage(113, 69, 55, 77), 1, 1));
		tiles.add(new Tile(buildup.subimage(113, 147, 55, 99), 1, 1));
		tiles.add(new Tile(buildup.subimage(113, 247, 55, 55), 1, 1));
		tiles.add(new Tile(buildup.subimage(113, 289, 55, 54), 1, 1));
		
		List<Tile> damagedTiles = new ArrayList<Tile>();
		buildupDamagedTiles.put("7", damagedTiles);
		PCXImage damage = new PCXImage(entries.get("DAMAGE.PCX").data);
		// seems to be the same as techid 5
		damagedTiles.add(new Tile(damage.subimage(1, 1, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 29, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 57, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 85, 55, 27), 1, 1));
		
		damagedTiles.add(new Tile(damage.subimage(57, 1, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 29, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 57, 55, 28), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 86, 55, 27), 1, 1));
	}
	/**
	 * Extract build progress, and damaged build progress from the tech 8 data file.
	 * (Remark: Each tech id has different layout in these files.
	 * @param entries the entries in the pac file
	 */
	private void tech8Build(Map<String, PACEntry> entries) {
		List<Tile> tiles = new ArrayList<Tile>();
		buildupTiles.put("8", tiles);
		
		PCXImage buildup = new PCXImage(entries.get("EPULES1.PCX").data);
		// seems to be the same as techid 2
		tiles.add(new Tile(buildup.subimage(1, 1, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 29, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 57, 55, 27), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 85, 55, 44), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 130, 55, 36), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 167, 55, 43), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 211, 55, 50), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 262, 55, 46), 1, 1));
		tiles.add(new Tile(buildup.subimage(1, 309, 55, 46), 1, 1));
		
		tiles.add(new Tile(buildup.subimage(57, 1, 55, 64), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 66, 55, 31), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 98, 55, 38), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 137, 55, 38), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 176, 55, 47), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 224, 55, 42), 1, 1));
		tiles.add(new Tile(buildup.subimage(57, 267, 55, 60), 1, 1));
		
		List<Tile> damagedTiles = new ArrayList<Tile>();
		buildupDamagedTiles.put("8", damagedTiles);
		// seems to be the same as techid 2
		PCXImage damage = new PCXImage(entries.get("DAMAGE.PCX").data);
		damagedTiles.add(new Tile(damage.subimage(1, 1, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 29, 55, 44), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 74, 55, 34), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 109, 55, 28), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 138, 55, 64), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(1, 203, 55, 43), 1, 1));
		
		damagedTiles.add(new Tile(damage.subimage(57, 1, 55, 27), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 29, 55, 46), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 76, 55, 47), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 124, 55, 39), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 164, 55, 38), 1, 1));
		damagedTiles.add(new Tile(damage.subimage(57, 203, 55, 27), 1, 1));
	}
}
