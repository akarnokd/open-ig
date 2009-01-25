/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.gfx;

import hu.openig.core.InfoBarRegions;
import hu.openig.utils.PACFile;
import hu.openig.utils.PCXImage;
import hu.openig.utils.PACFile.PACEntry;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComponent;

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
	public GFXCursors cursors;
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
	public static final String[] BUILDING_NAMES = {
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
	public Color GRID_COLOR = new Color(0x783C5C);
	/** The dotted grid stroke. */
	public BasicStroke GRID_STROKE = new BasicStroke(1f, BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_MITER, 10f, new float[] { 1f }, 0f);
	/**
	 * Constructor. Loads the images from the specified home IG directory.
	 * @param root
	 */
	public CommonGFX(String root) {
		BufferedImage basic = PCXImage.from(root + "/GFX/ALAP.PCX", -1);
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// TOP BAR
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// get the colors from the joining point
		top = new StarmapBar();
		top.left = basic.getSubimage(0, 0, 400, 20);
		top.right = basic.getSubimage(400, 0, 240, 20);
		top.link = basic.getSubimage(399, 0, 1, 20);
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// BOTTOM BAR
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		bottom = new StarmapBar();
		// get the colors from the joining point
		bottom = new StarmapBar();
		bottom.left = basic.getSubimage(0, 20, 400, 18);
		bottom.right = basic.getSubimage(400, 20, 240, 18);
		bottom.link = basic.getSubimage(399, 20, 1, 18);
		
		BufferedImage cursorImage = PCXImage.from(root + "/GFX/ICONMAIN.PCX", 0);
		cursors = new GFXCursors();
		int idx = 0;

		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// CURSORS
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		
		cursors.pointer = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(0, 0), "Pointer");
		cursors.hand = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(0, 0), "Hand");
		cursors.target = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(10, 10), "Target");
		cursors.move = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(10, 10), "Move");
		cursors.select = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(10, 10), "Select");
		cursors.northwest = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(0, 0), "NorthWest");
		cursors.north = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(10, 0), "North");
		cursors.northeast = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(19, 0), "NorthEast");
		cursors.east = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(19, 8), "East");
		cursors.west = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(0, 8), "West");
		cursors.back = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(9, 10), "Back");
		cursors.southwest = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(0, 19), "SouthWest");
		cursors.south = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(10, 19), "South");
		cursors.southeast = toolkit.createCustomCursor(cursorImage.getSubimage(idx++ * 32, 1, 32, 31), 
				new Point(19, 19), "SouthWest");
		
		
		text = new TextGFX(root + "/GFX/CHARSET1.PCX");
		
		minimap = PCXImage.from(root + "/GFX/ZOOM2.PCX", -1);
		// fix image
		minimap = minimap.getSubimage(0, 0, minimap.getWidth() - 1, minimap.getHeight());
		
		for (int i = 0; i < 5; i++) {
			minimapInfo[i] = PCXImage.from(root + "/GFX/INFO" + (i + 1) + ".PCX", -1);
		}
		
		// BUILDING PICTURES
		for (int i = 1; i <= 8; i++) {
			Map<String, PACEntry> buildingPics = PACFile.mapByName(PACFile.parseFully(root + "/DATA/COLONYP" + i + ".PAC"));
			for (PACEntry e : buildingPics.values()) {
				int idx1 = e.filename.indexOf('.');
				int value = Integer.parseInt(e.filename.substring(0, idx1));
				setBuildingImage(i, value, BUILDING_NAMES[value], PCXImage.parse(buildingPics.get(e.filename).data, -1));
			}
		}
	}
	/**
	 * Set building image for a race and building type.
	 * @param race
	 * @param index
	 * @param name
	 * @param picture
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
	public void renderInfoBars(JComponent c, Graphics2D g2) {
		int w = c.getWidth();
		int h = c.getHeight();
		g2.drawImage(top.left, 0, 0, null);
		g2.drawImage(bottom.left, 0, h - bottom.left.getHeight(), null);
		g2.drawImage(top.right, w - top.right.getWidth(), 0, null);
		g2.drawImage(bottom.right, w - bottom.right.getWidth(), h - bottom.left.getHeight(), null);

		// check if the rendering width is greater than the default 640
		// if so, draw the link lines
		int lr = top.left.getWidth() + top.right.getWidth();
		if (w > lr) {
			AffineTransform at = g2.getTransform();
			g2.translate(top.left.getWidth(), 0);
			g2.scale(w - lr, 1);
			g2.drawImage(top.link, 0, 0, null);

			g2.setTransform(at);
			g2.translate(bottom.left.getWidth(), 0);
			g2.scale(w - lr, 1);
			g2.drawImage(bottom.link, 0, h - bottom.link.getHeight(), null);
			g2.setTransform(at);
		}
	}
	public void updateRegions(JComponent c, InfoBarRegions reg) {
		int w = c.getWidth();
		//int h = c.getHeight();
		// location of the top info area
		reg.topInfoArea.x = 387;
		reg.topInfoArea.y = 2;
		reg.topInfoArea.width = w - reg.topInfoArea.x - 11;
		reg.topInfoArea.height = 16;
	}
}
