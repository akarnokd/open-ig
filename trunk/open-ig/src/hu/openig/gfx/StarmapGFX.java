/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.gfx;

import hu.openig.utils.ImageUtils;
import hu.openig.utils.PACFile;
import hu.openig.utils.PCXImage;
import hu.openig.utils.ResourceMapper;
import hu.openig.utils.PACFile.PACEntry;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Record to store the graphical elements of the starmap screen. */
public class StarmapGFX {
	public StarmapContents contents;
	/** Map from surface type to drawing size to list of animation phase images. */
	public Map<String, Map<Integer, List<BufferedImage>>> starmapPlanets;
	public BufferedImage btnBorder;
	public BufferedImage btnRadars;
	public BufferedImage btnStars;
	public BufferedImage btnFleets;
	public BufferedImage btnDest;
	public BufferedImage btnGrids;

	public BufferedImage btnBorderLight;
	public BufferedImage btnRadarsLight;
	public BufferedImage btnStarsLight;
	public BufferedImage btnFleetsLight;
	public BufferedImage btnDestLight;
	public BufferedImage btnGridsLight;
	public BufferedImage btnNameOff;
	public BufferedImage btnNameColony;
	public BufferedImage btnNameFleets;
	public BufferedImage btnNameBoth;
	public BufferedImage btnColonise;
	public BufferedImage btnColoniseDisabled;
	/** Add satelite button. */
	public BufferedImage btnAddSat;
	public BufferedImage btnAddSpySat1;
	public BufferedImage btnAddSpySat2;
	public BufferedImage btnAddHubble2;
	
	public BufferedImage btnEquipmentDisabled;
	public BufferedImage btnEquipmentDown;
	public BufferedImage btnColonyDown;
	/** Zoom button hightlighted. */
	public BufferedImage btnMagnifyLight;
	public BufferedImage btnMagnifyDisabled;
	
	public BufferedImage btnNormalDisabled;
	public BufferedImage btnScrollDisabled;
	public BufferedImage btnZoomDisabled;
	public BufferedImage btnNormalLight;
	public BufferedImage btnScrollLight;
	public BufferedImage btnZoomLight;
	
	public BufferedImage btnPrevDisabled;
	public BufferedImage btnPrevDown;
	public BufferedImage btnNextDisabled;
	public BufferedImage btnNextDown;
	
	public BufferedImage btnInfoDown;
	public BufferedImage btnBridgeDown;
	/** Ship move light. */
	public BufferedImage btnMoveLight;
	public BufferedImage btnAttackLight;
	public BufferedImage btnStopLight;
	/**
	 * Constructor. Loads all images belonging to the starmap.
	 * @param resMap the resource mapper object.
	 */
	public StarmapGFX(ResourceMapper resMap) {
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// STARMAP BODY
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		BufferedImage body = PCXImage.from(resMap.get("SCREENS/STARMAP.PCX"), -1);
		
		contents = new StarmapContents();
		
		contents.bottomLeft = ImageUtils.subimage(body, 0, 331, 33, 111);
		contents.bottomFiller = ImageUtils.subimage(body, 33, 331, 2, 111);
		contents.bottomRight = ImageUtils.subimage(body, 33, 331, 607, 111);
		contents.shipControls = ImageUtils.subimage(body, 285, 359, 106, 83);
		contents.rightTop = ImageUtils.subimage(body, 505, 0, 135, 42);
		contents.rightFiller = ImageUtils.subimage(body, 505, 42, 135, 2);
		contents.rightBottom = ImageUtils.subimage(body, 505, 42, 135, 289);
		
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// SCROLLBAR SEGMENTS
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		contents.hscrollLeft = ImageUtils.subimage(body, 37, 334, 18, 18);
		contents.hscrollFiller = ImageUtils.subimage(body, 55, 334, 2, 18);
		contents.hscrollRight = ImageUtils.subimage(body, 55, 334, 17, 18);
		
		contents.vscrollTop = ImageUtils.subimage(body, 508, 291, 18, 18);
		contents.vscrollFiller = ImageUtils.subimage(body, 508, 309, 18, 2);
		contents.vscrollBottom = ImageUtils.subimage(body, 508, 309, 18, 17);
		
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// BIGMAP
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		contents.fullMap = PCXImage.from(resMap.get("GFX/ZOOM.PCX"), -1);
		// fix image
		contents.fullMap = contents.fullMap.getSubimage(0, 0, contents.fullMap.getWidth(), 662);
		
		contents.mapBackground = new Color(contents.fullMap.getRGB(0, 0));

		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// LOADING VARIOUS PLANET ANIMATIONS
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

		starmapPlanets = new HashMap<String, Map<Integer, List<BufferedImage>>>();
		for (PACEntry pe : PACFile.parseFully(resMap.get("DATA/BOLYGOK.PAC"))) {
			String type = pe.filename.substring(0, 4);
			Map<Integer, List<BufferedImage>> aplanet = starmapPlanets.get(type);
			if (aplanet == null) {
				aplanet = new HashMap<Integer, List<BufferedImage>>();
				starmapPlanets.put(type, aplanet);
			}
			BufferedImage planetImg = PCXImage.parse(pe.data, -2);
			
			int width = planetImg.getWidth();
			int count = planetImg.getHeight() / width;
			List<BufferedImage> list = new ArrayList<BufferedImage>(count);
			for (int i = 0; i < count; i++) {
				list.add(ImageUtils.subimage(planetImg, 0, width * i, width, width));
			}
			
			aplanet.put(planetImg.getWidth(), list);
		}
		
		BufferedImage starx = PCXImage.from(resMap.get("SCREENS/STAR_X.PCX"), -1);
		
		btnBorderLight = ImageUtils.subimage(starx, 0, 0, 53, 18);
		btnRadarsLight = ImageUtils.subimage(starx, 53, 0, 53, 18);
		btnStarsLight = ImageUtils.subimage(starx, 0, 18, 53, 18);
		btnFleetsLight = ImageUtils.subimage(starx, 53, 18, 53, 18);
		btnDestLight = ImageUtils.subimage(starx, 0, 36, 53, 18);
		btnGridsLight = ImageUtils.subimage(starx, 53, 36, 53, 18);

		btnBorder = ImageUtils.subimage(starx, 108, 0, 53, 18);
		btnRadars = ImageUtils.subimage(starx, 161, 0, 53, 18);
		btnStars = ImageUtils.subimage(starx, 108, 18, 53, 18);
		btnFleets = ImageUtils.subimage(starx, 161, 18, 53, 18);
		btnDest = ImageUtils.subimage(starx, 108, 36, 53, 18);
		btnGrids = ImageUtils.subimage(starx, 161, 36, 53, 18);

		btnNameOff = ImageUtils.subimage(starx, 0, 54, 108, 18);
		btnNameColony = ImageUtils.subimage(starx, 0, 72, 108, 18);
		btnNameFleets = ImageUtils.subimage(starx, 0, 90, 108, 18);
		btnNameBoth = ImageUtils.subimage(starx, 0, 108, 108, 18);
		
		btnColonise = ImageUtils.subimage(starx, 0, 126, 108, 15);
		btnColoniseDisabled = ImageUtils.subimage(starx, 108, 126, 108, 15);
		
		btnAddSat = ImageUtils.subimage(starx, 109, 54, 84, 17);
		btnAddSpySat1 = ImageUtils.subimage(starx, 109, 71, 84, 17);
		btnAddSpySat2 = ImageUtils.subimage(starx, 109, 88, 84, 17);
		btnAddHubble2 = ImageUtils.subimage(starx, 109, 105, 84, 17);
		
		btnEquipmentDisabled = ImageUtils.subimage(starx, 216, 0, 103, 28);
		btnEquipmentDown = ImageUtils.subimage(starx, 216, 28, 103, 28);
		btnColonyDown = ImageUtils.subimage(starx, 216, 84, 103, 28);
		
		btnMagnifyLight = ImageUtils.subimage(starx, 352, 60, 33, 64);
		btnMagnifyDisabled = ImageUtils.subimage(starx, 599, 78, 33, 64);
		
		btnNormalDisabled = ImageUtils.subimage(starx, 319, 138, 66, 20);
		btnScrollDisabled = ImageUtils.subimage(starx, 385, 138, 66, 20);
		btnZoomDisabled = ImageUtils.subimage(starx, 451, 138, 66, 20);
		
		btnNormalLight = ImageUtils.subimage(starx, 533, 78, 66, 20);
		btnScrollLight = ImageUtils.subimage(starx, 533, 98, 66, 20);
		btnZoomLight = ImageUtils.subimage(starx, 533, 118, 66, 20);
		
		btnPrevDisabled = ImageUtils.subimage(starx, 483, 0, 50, 20);
		btnPrevDown = ImageUtils.subimage(starx, 483, 41, 50, 18);
		btnNextDisabled = ImageUtils.subimage(starx, 483, 60, 49, 20);
		btnNextDown = ImageUtils.subimage(starx, 483, 101, 49, 18);
		
		btnInfoDown = ImageUtils.subimage(starx, 533, 0, 102, 39);
		btnBridgeDown = ImageUtils.subimage(starx, 533, 39, 102, 39);
		
		btnMoveLight = ImageUtils.subimage(starx, 385, 69, 98, 23);
		btnAttackLight = ImageUtils.subimage(starx, 385, 92, 98, 23);
		btnStopLight = ImageUtils.subimage(starx, 385, 115, 98, 23);
	}
}