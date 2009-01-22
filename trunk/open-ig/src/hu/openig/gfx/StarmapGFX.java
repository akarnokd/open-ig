/*
 * Copyright 2008, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.gfx;

import hu.openig.utils.PACFile;
import hu.openig.utils.PCXImage;
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
	/**
	 * Constructor. Loads all images belonging to the starmap.
	 * @param root the file system root directory of IG.
	 */
	public StarmapGFX(String root) {
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// STARMAP BODY
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		BufferedImage body = PCXImage.from(root + "/SCREENS/STARMAP.PCX", -1);
		
		contents = new StarmapContents();
		
		contents.bottomLeft = body.getSubimage(0, 331, 33, 111);
		contents.bottomFiller = body.getSubimage(33, 331, 2, 111);
		contents.bottomRight = body.getSubimage(33, 331, 607, 111);
		contents.shipControls = body.getSubimage(285, 359, 106, 83);
		contents.rightTop = body.getSubimage(505, 0, 135, 42);
		contents.rightFiller = body.getSubimage(505, 42, 135, 2);
		contents.rightBottom = body.getSubimage(505, 42, 135, 289);
		
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// SCROLLBAR SEGMENTS
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		contents.hscrollLeft = body.getSubimage(37, 334, 18, 18);
		contents.hscrollFiller = body.getSubimage(55, 334, 2, 18);
		contents.hscrollRight = body.getSubimage(55, 334, 17, 18);
		
		contents.vscrollTop = body.getSubimage(508, 291, 18, 18);
		contents.vscrollFiller = body.getSubimage(508, 309, 18, 2);
		contents.vscrollBottom = body.getSubimage(508, 309, 18, 17);
		
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// BIGMAP AND MINIMAP
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		contents.minimap = PCXImage.from(root + "/GFX/ZOOM2.PCX", -1);
		// fix image
		contents.minimap = contents.minimap.getSubimage(0, 0, contents.minimap.getWidth() - 1, contents.minimap.getHeight());
		contents.fullMap = PCXImage.from(root + "/GFX/ZOOM.PCX", -1);
		// fix image
		contents.fullMap = contents.fullMap.getSubimage(0, 0, contents.fullMap.getWidth(), 662);
		
		contents.mapBackground = new Color(contents.fullMap.getRGB(0, 0));

		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
		// LOADING VARIOUS PLANET ANIMATIONS
		// oooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo

		starmapPlanets = new HashMap<String, Map<Integer, List<BufferedImage>>>();
		for (PACEntry pe : PACFile.parseFully(root + "/DATA/BOLYGOK.PAC")) {
			String type = pe.filename.substring(0, 4);
			Map<Integer, List<BufferedImage>> aplanet = starmapPlanets.get(type);
			if (aplanet == null) {
				aplanet = new HashMap<Integer, List<BufferedImage>>();
			}
			BufferedImage planetImg = PCXImage.parse(pe.data, -2);
			
			int width = planetImg.getWidth();
			int count = planetImg.getHeight() / width;
			List<BufferedImage> list = new ArrayList<BufferedImage>(count);
			for (int i = 0; i < count; i++) {
				list.add(planetImg.getSubimage(0, width * i, width, width));
			}
			
			aplanet.put(planetImg.getWidth(), list);
		}
		
		BufferedImage starx = PCXImage.from(root + "/SCREENS/STAR_X.PCX", -1);
		
		btnBorderLight = starx.getSubimage(0, 0, 53, 18);
		btnRadarsLight = starx.getSubimage(53, 0, 53, 18);
		btnStarsLight = starx.getSubimage(0, 18, 53, 18);
		btnFleetsLight = starx.getSubimage(53, 18, 53, 18);
		btnDestLight = starx.getSubimage(0, 36, 53, 18);
		btnGridsLight = starx.getSubimage(53, 36, 53, 18);

		btnBorder = starx.getSubimage(161, 0, 53, 18);
		btnRadars = starx.getSubimage(53, 0, 53, 18);
		btnStars = starx.getSubimage(161, 18, 53, 18);
		btnFleets = starx.getSubimage(53, 18, 53, 18);
		btnDest = starx.getSubimage(161, 36, 53, 18);
		btnGrids = starx.getSubimage(53, 36, 53, 18);

		btnNameOff = starx.getSubimage(0, 54, 108, 18);
		btnNameColony = starx.getSubimage(0, 72, 108, 18);
		btnNameFleets = starx.getSubimage(0, 90, 108, 18);
		btnNameBoth = starx.getSubimage(0, 108, 108, 18);
		
		btnColonise = starx.getSubimage(0, 126, 108, 15);
		btnColoniseDisabled = starx.getSubimage(108, 126, 108, 15);
		
		btnAddSat = starx.getSubimage(109, 54, 84, 17);
		btnAddSpySat1 = starx.getSubimage(109, 71, 84, 17);
		btnAddSpySat2 = starx.getSubimage(109, 88, 84, 17);
		btnAddHubble2 = starx.getSubimage(109, 105, 84, 17);
		
		btnEquipmentDisabled = starx.getSubimage(216, 0, 103, 28);
		btnEquipmentDown = starx.getSubimage(216, 28, 103, 28);
		btnColonyDown = starx.getSubimage(216, 84, 103, 28);
	}
}