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
import hu.openig.model.InventoryItem;
import hu.openig.model.Planet;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.Screens;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.panels.ListSpinBox;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageTabButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
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
	/** Scroll up button. */
	UIImageButton scrollUp;
	/** Scroll down button. */
	UIImageButton scrollDown;
	/** The top index of the list. */
	int topIndex;
	/** The visible row count. */
	int rowCount = 20;
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
		all.down = true;
		
		scrollUp = new UIImageButton(commons.common().moveUp);
		scrollUp.setDisabledPattern(commons.common().disabledPattern);
		scrollUp.setHoldDelay(150);
		scrollDown = new UIImageButton(commons.common().moveDown);
		scrollDown.setDisabledPattern(commons.common().disabledPattern);
		scrollDown.setHoldDelay(150);
		
		scrollUp.onClick = new Action0() {
			@Override
			public void invoke() {
				doScroll(-1);
				askRepaint();
			}
		};
		scrollDown.onClick = new Action0() {
			@Override
			public void invoke() {
				doScroll(1);
				askRepaint();
			}
		};

		
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

	/**
	 * Scroll the list into the direction by the given amount.
	 * @param direction the number of rows to scroll up (&lt;0) or down (&gt;0)
	 */
	void doScroll(int direction) {
		int idx2 = topIndex + direction;
		if (idx2 < 0) {
			idx2 = 0;
		} else {
			List<InventoryItem> itemList = itemList();
			if (idx2 + rowCount >= itemList.size()) {
				idx2 = itemList.size() - rowCount - 1;
			}
		}
		
		topIndex = idx2;
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
		
		scrollUp.location(base.x + 382, base.y + 30);
		scrollDown.location(scrollUp.x, base.y + 325);
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

		int nameColumn = 5;
		int typeColumn = 190;
		int costColumn = 310;
		int endColumn = 378;
		int headerY = 32;
		
		commons.text().paintTo(g2, base.x + nameColumn, base.y + headerY, 10, TextRenderer.YELLOW, get("trade.name"));
		commons.text().paintTo(g2, base.x + typeColumn, base.y + headerY, 10, TextRenderer.YELLOW, get("trade.type"));
		String countTitle = get("trade.count");
		int countTitleWidth = commons.text().getTextWidth(10, countTitle);
		commons.text().paintTo(g2, base.x + costColumn - countTitleWidth, base.y + headerY, 10, TextRenderer.YELLOW, countTitle);
		String costTitle = get("trade.cost");
		int costTitleWidth = commons.text().getTextWidth(10, costTitle);
		commons.text().paintTo(g2, base.x + endColumn - costTitleWidth, base.y + headerY, 10, TextRenderer.YELLOW, costTitle);
		
		update();
		
		super.draw(g2);
		
		if (planets.itemCount() == 0) {
			BufferedImage img = commons.equipment().noSpaceport;
			g2.drawImage(img, planets.x + (planets.width - img.getWidth()) / 2, planets.y + 3, null);
		}
		
		Shape shape0 = g2.getClip();
		
		g2.clipRect(base.x + 5, base.y + 40, 400, 300);
		
		List<InventoryItem> itemList = itemList();
		int ty = base.y + 50;
		for (int i = topIndex; i < itemList.size(); i++) {
			InventoryItem ii = itemList.get(i);
			
			commons.text().paintTo(g2, base.x + nameColumn, ty, 10, TextRenderer.ORANGE, ii.type.name);

			String countStr = Integer.toString(ii.count);
			int countWidth = commons.text().getTextWidth(10, countStr);
			commons.text().paintTo(g2, base.x + costColumn - countWidth, ty, 10, TextRenderer.ORANGE, countStr);

			String costStr = Long.toString(ii.sellValue() * 4);
			int costWidth = commons.text().getTextWidth(10, costStr);
			commons.text().paintTo(g2, base.x + endColumn - costWidth, ty, 10, TextRenderer.ORANGE, costStr);
			
			ty += 14;
		}
		
		g2.setClip(shape0);
		
		g2.setTransform(savea);
	}
	/** Update the UI elements to reflect the current world state. */
	void update() {
		List<Planet> ps = player().ownPlanets();
		for (int i = ps.size() - 1; i >= 0; i--) {
			Planet p = ps.get(i);
			if (!p.hasMilitarySpaceport()) {
				ps.remove(i);
			}
		}
		planets.setList(ps);
		planets.update();
		
		List<InventoryItem> itemList = itemList();
		
		if (topIndex + rowCount > itemList.size()) {
			topIndex = 0;
		}
		scrollUp.enabled(topIndex != 0);
		scrollDown.enabled(topIndex + rowCount + 1 < itemList.size());
	}
	/**
	 * @return the possibly filtered list of the black market.
	 */
	List<InventoryItem> itemList() {
		List<InventoryItem> items = new ArrayList<>();
		
		for (InventoryItem ii : player().blackMarket) {
			boolean add = false;
			if (all.down) {
				add = true;
			} else
			if (stationsAndSatellites.down) {
				add = ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS
						|| ii.type.category == ResearchSubCategory.SPACESHIPS_SATELLITES;
			} else
			if (battleships.down) {
				add = ii.type.category == ResearchSubCategory.SPACESHIPS_BATTLESHIPS;
			} else
			if (cruisers.down) {
				add = ii.type.category == ResearchSubCategory.SPACESHIPS_CRUISERS;
			} else
			if (fighters.down) {
				add = ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS;
			} else
			if (tanksAndVehicles.down) {
				add = ii.type.category == ResearchSubCategory.WEAPONS_TANKS
						|| ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES;
			} else
			if (weapons.down) {
				add = ii.type.category == ResearchSubCategory.WEAPONS_LASERS
						|| ii.type.category == ResearchSubCategory.WEAPONS_CANNONS
						|| ii.type.category == ResearchSubCategory.WEAPONS_PROJECTILES;
			} else
			if (equipment.down) {
				add = ii.type.category.main == ResearchMainCategory.EQUIPMENT;
			}
				
			if (add) {
				items.add(ii);
			}
		}
		
		return items;
	}
	@Override
	public boolean mouse(UIMouse e) {
		scaleMouse(e, base, margin());
		if (!base.contains(e.x, e.y) && e.has(Type.DOWN)) {
			hideSecondary();
			return true;
		}
		if (e.has(Type.WHEEL)) {
			if (e.z < 0) {
				doScroll(-1);
				return true;
			} else
			if (e.z > 0) {
				doScroll(1);
				return true;
			}
		}
		return super.mouse(e);
	}
	// FIXME keyboard up/down
	/**
	 * Action to perform if a category button was pressed.
	 * @param button the button pressed
	 */
	void doCategoryPressAction(UIImageTabButton button) {
		for (UIImageTabButton btn : categories) {
			btn.down = btn == button;
		}
	}
}
