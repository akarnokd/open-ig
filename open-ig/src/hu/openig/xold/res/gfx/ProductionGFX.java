/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.res.gfx;

import hu.openig.utils.ImageUtils;
import hu.openig.utils.PCXImage;
import hu.openig.utils.ResourceMapper;

import java.awt.image.BufferedImage;

/**
 * Production screen related graphics.
 * @author karnokd
 */
public class ProductionGFX {
	/** The base screen. */
	public final BufferedImage screen;
	/** Equipment button down image. */
	public final BufferedImage btnEquipmentDown;
	/** Research button down image. */
	public final BufferedImage btnResearchDown;
	/** Bridge button down image. */
	public final BufferedImage btnBridgeDown;
	/** Empty elevated button image. */
	public final BufferedImage btnEmptyElevated;
	/** Add button image. */
	public final BufferedImage btnAdd;
	/** Remove button image. */
	public final BufferedImage btnRemove;
	/** Add button down image. */
	public final BufferedImage btnAddDown;
	/** Remove button down image. */
	public final BufferedImage btnRemoveDown;
	/** Empty button image. */
	public final BufferedImage btnEmpty;
	/** Sell button down image. */
	public final BufferedImage btnSellDown;
	/** Sell button disabled image. */
	public final BufferedImage btnSellDisabled;
	/** Minus ten button image. */
	public final BufferedImage btnMinusTen;
	/** Minus ten button down image. */
	public final BufferedImage btnMinusTenDown;
	/** Minus ten button disabled image. */
	public final BufferedImage btnMinusTenDisabled;
	/** Minus one button image. */
	public final BufferedImage btnMinusOne;
	/** Minus one button down image. */
	public final BufferedImage btnMinusOneDown;
	/** Minus one button disabled image. */
	public final BufferedImage btnMinusOneDisabled;
	/** Plus ten button image. */
	public final BufferedImage btnPlusTen;
	/** Plus ten button down image. */ 
	public final BufferedImage btnPlusTenDown;
	/** Plus ten button disabled image. */
	public final BufferedImage btnPlusTenDisabled;
	/** Plus one button image. */
	public final BufferedImage btnPlusOne;
	/** Plus one button down image. */
	public final BufferedImage btnPlusOneDown;
	/** Plus one button disabled image. */
	public final BufferedImage btnPlusOneDisabled;
	/** Empty sub option image. */
	public final BufferedImage emptySubOption;
	/**
	 * Constructor.
	 * @param resMap the resource mapper
	 */
	public ProductionGFX(ResourceMapper resMap) {
		screen = PCXImage.from(resMap.get("SCREENS/PRODUCT.PCX"), -1);
		BufferedImage x = PCXImage.from(resMap.get("SCREENS/PROD_X.PCX"), -1);
		
		btnEquipmentDown = ImageUtils.subimage(x, 336, 1, 102, 39);
		btnResearchDown = ImageUtils.subimage(x, 336, 39, 102, 39);
		btnBridgeDown = ImageUtils.subimage(x, 336, 78, 102, 39);
		btnEmptyElevated = ImageUtils.subimage(x, 336, 117, 102, 39);
		btnAdd = ImageUtils.subimage(x, 336, 156, 102, 39);
		btnRemove = ImageUtils.subimage(x, 336, 195, 102, 39);
		btnAddDown = ImageUtils.subimage(x, 336, 234, 102, 39);
		btnRemoveDown = ImageUtils.subimage(x, 336, 273, 102, 39);
		btnEmpty = ImageUtils.subimage(x, 438, 273, 102, 39);
		btnSellDown = ImageUtils.subimage(x, 440, 192, 52, 21);
		btnSellDisabled = ImageUtils.subimage(x, 440, 216, 52, 21);
		
		btnMinusTen = ImageUtils.subimage(x, 550, 0, 52, 21);
		btnMinusOne = ImageUtils.subimage(x, 550, 21, 52, 21);
		btnPlusOne = ImageUtils.subimage(x, 550, 42, 52, 21);
		btnPlusTen = ImageUtils.subimage(x, 550, 63, 52, 21);
		
		btnMinusTenDown = ImageUtils.subimage(x, 550, 84, 52, 21);
		btnMinusOneDown = ImageUtils.subimage(x, 550, 105, 52, 21);
		btnPlusOneDown = ImageUtils.subimage(x, 550, 126, 52, 21);
		btnPlusTenDown = ImageUtils.subimage(x, 550, 147, 52, 21);
		
		btnMinusTenDisabled = ImageUtils.subimage(x, 550, 168, 52, 21);
		btnMinusOneDisabled = ImageUtils.subimage(x, 550, 189, 52, 21);
		btnPlusOneDisabled = ImageUtils.subimage(x, 550, 210, 52, 21);
		btnPlusTenDisabled = ImageUtils.subimage(x, 550, 231, 52, 21);
		
		emptySubOption = ImageUtils.subimage(x, 0, 142, 167, 14);
	}
}
