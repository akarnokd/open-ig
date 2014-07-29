/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Func1;
import hu.openig.model.Planet;
import hu.openig.model.Screens;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.panels.ListSpinBox;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImageTabButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The trading management screen.
 * @author akarnokd, 2013.10.02.
 */
public class TradeScreen extends ScreenBase {
	/** The screen origin. */
	final Rectangle base = new Rectangle(0, 0, 620, 442);
	/** All categories. */
	UIImageTabButton all;
	/** Battleships category. */
	UIImageTabButton battleships;
	/** Cruisers category. */
	UIImageTabButton cruisers;
	/** Fighters category. */
	UIImageTabButton fighters;
	/** Stations category. */
	UIImageTabButton stationsAndSatellites;
	/** Tanks category. */
	UIImageTabButton tanksAndVehicles;
	/** Vehicles category. */
	UIImageTabButton weapons;
	/** Vehicles category. */
	UIImageTabButton equipment;
	/** Categories. */
	List<UIImageTabButton> categories;
	/** The spin box listing planets with military spaceport. */
	ListSpinBox<Planet> planets;
	/** Buy 1 item of the selected item. */
	UIGenericButton buy;
	/** Deliver to label. */
	UILabel deliverTo;
	@Override
	public void onInitialize() {
		// TODO Auto-generated method stub
		all = new UIImageTabButton(commons.equipment().categoryAll);
		stationsAndSatellites = new UIImageTabButton(commons.equipment().categorySpaceStations);
		battleships = new UIImageTabButton(commons.equipment().categoryBattleships);
		cruisers = new UIImageTabButton(commons.equipment().categoryCruisers);
		fighters = new UIImageTabButton(commons.equipment().categoryFighers);
		tanksAndVehicles = new UIImageTabButton(commons.equipment().categoryTanks);
		weapons = new UIImageTabButton(commons.equipment().categoryWeapons);
		equipment = new UIImageTabButton(commons.equipment().categoryEquipment);

		
		categories = Arrays.asList(
				all,
				stationsAndSatellites,
				battleships,
				cruisers,
				fighters,
				tanksAndVehicles,
				weapons,
				equipment
		);
		
		for (final UIImageTabButton tb : categories) {
			tb.onPress = new Action0() {
				@Override
				public void invoke() {
					doCategoryPressAction(tb);
				}
			};
		}
		
		deliverTo = new UILabel(get("trade.deliver_to"), 14, commons.text());
		
		buy = new UIGenericButton(get("trade.buy"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		buy.disabledPattern(commons.common().disabledPattern);

		planets = new ListSpinBox<>(commons, new ArrayList<Planet>(), new Func1<Planet, String>() {
			@Override
			public String invoke(Planet value) {
				return value.name();
			}
		});
		planets.update();
		
		setTooltip(all, "equipment.all.tooltip");
		setTooltip(stationsAndSatellites, "equipment.starbases_and_satellites.tooltip");
		setTooltip(battleships, "equipment.battleships.tooltip");
		setTooltip(cruisers, "equipment.cruisers.tooltip");
		setTooltip(fighters, "equipment.fighters.tooltip");
		setTooltip(tanksAndVehicles, "equipment.tanks_and_vehicles.tooltip");
		setTooltip(weapons, "equipment.weapons.tooltip");
		setTooltip(equipment, "equipment.equipment.tooltip");
		
		addThis();
	}

	@Override
	public void onEnter(Screens mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResize() {
		scaleResize(base, margin());
		
		categories.get(0).location(base.x + 3, base.y + 3);
		
		for (int i = 1; i < categories.size(); i++) {
			UIComponent c0 = categories.get(i - 1);
			UIComponent c1 = categories.get(i);
			
			c1.location(c0.x + c0.width + 2, c0.y);
		}
		
		deliverTo.location(base.x + 5, base.y + 365);
		planets.location(base.x + 5, deliverTo.y + 20);
		buy.location(base.x + 411 - buy.width, deliverTo.y - 5);
		
		planets.width(buy.x - deliverTo.x - 5);
	}

	@Override
	public Screens screen() {
		return Screens.TRADE;
	}

	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub

	}
	@Override
	public void draw(Graphics2D g2) {
		AffineTransform savea = scaleDraw(g2, base, margin());
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);

		g2.setColor(Color.BLACK);
		g2.fillRect(base.x, base.y, 620, 420);
		g2.drawImage(commons.info().baseTop, base.x, base.y, null);
		g2.drawImage(commons.info().baseLeft, base.x, base.y, null);
		g2.drawImage(commons.info().baseMiddle, base.x + 413, base.y, null);
		g2.drawImage(commons.info().baseRight, base.x + 618, base.y, null);
		g2.drawImage(commons.info().baseDivider1, base.x + 413, base.y + 28, null);
		g2.drawImage(commons.info().baseDivider2, base.x + 2, base.y + 355, 411, 2, null);

		int headerY = 32;
		commons.text().paintTo(g2, base.x + 5, base.y + headerY, 10, TextRenderer.YELLOW, get("trade.name"));
		commons.text().paintTo(g2, base.x + 120, base.y + headerY, 10, TextRenderer.YELLOW, get("trade.race"));
		commons.text().paintTo(g2, base.x + 190, base.y + headerY, 10, TextRenderer.YELLOW, get("trade.type"));
		commons.text().paintTo(g2, base.x + 280, base.y + headerY, 10, TextRenderer.YELLOW, get("trade.count"));
		commons.text().paintTo(g2, base.x + 330, base.y + headerY, 10, TextRenderer.YELLOW, get("trade.cost"));
		
		super.draw(g2);
		
		if (planets.itemCount() == 0) {
			BufferedImage img = commons.equipment().noSpaceport;
			g2.drawImage(img, planets.x + (planets.width - img.getWidth()) / 2, planets.y + 3, null);
		}
		
		g2.setTransform(savea);
	}
	@Override
	public boolean mouse(UIMouse e) {
		scaleMouse(e, base, margin());
		if (!base.contains(e.x, e.y) && e.has(Type.DOWN)) {
			hideSecondary();
			return true;
		}
		return super.mouse(e);
	}
	/**
	 * Action to perform if a category button was pressed.
	 * @param button the button pressed
	 */
	void doCategoryPressAction(UIImageTabButton button) {
		int index = -1;
		int j = 0;
		for (UIImageTabButton btn : categories) {
			if (btn == button) {
				index = j;
			} else {
				btn.down = false;
			}
			j++;
		}
		System.out.println(index);
		// TODO redisplay the list with filtering based on index
	}
}
