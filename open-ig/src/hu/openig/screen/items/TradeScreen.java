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
import hu.openig.core.Pair;
import hu.openig.model.InventoryItem;
import hu.openig.model.InventorySlot;
import hu.openig.model.Planet;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.panels.ListSpinBox;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageTabButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;
import hu.openig.utils.U;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	int rowCount = 21;
	/** The row height. */
	int rowHeight = 14;
	/** The main list rectangle. */
	final Rectangle mainList = new Rectangle();
	/** The loadout rectangle. */
	final Rectangle loadoutList = new Rectangle();
	/** The selected inventory item identifier. */
	int selected;
	/** The selected item's image. */
	UIImage image;
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
		
		image = new UIImage();
		
		buy = new UIGenericButton(get("trade.buy"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		buy.disabledPattern(commons.common().disabledPattern);
		buy.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_HIGH_2);
				doBuy();
			}
		};

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
			if (idx2 + rowCount > itemList.size()) {
				idx2 = itemList.size() - rowCount;
			}
		}
		
		topIndex = idx2;
	}
	
	@Override
	public void onEnter(Screens mode) {

	}

	@Override
	public void onLeave() {
		
	}

	@Override
	public void onFinish() {
		selected = -1;
		image.image(null);
		planets.setList(Collections.<Planet>emptyList());
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
		
		mainList.x = base.x + 5;
		mainList.y = base.y + 50;
		mainList.width = 378;
		mainList.height = rowCount * rowHeight;
		
		loadoutList.x = base.x + 415;
		loadoutList.width = 200;
		loadoutList.y = base.y;
		loadoutList.height = 0;
		
		image.location(base.x + 415, base.y + 30);
		image.crop(true);
		image.size(203, 170);
	}

	@Override
	public Screens screen() {
		return Screens.TRADE;
	}

	@Override
	public void onEndGame() {
		selected = -1;
		image.image(null);
		planets.setList(Collections.<Planet>emptyList());
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
		g2.drawImage(commons.info().baseDivider1, base.x + 413, base.y + 28, null);
		g2.drawImage(commons.info().baseDivider2, base.x + 2, base.y + 355, 411, 2, null);

		int nameColumn = 7;
		int costColumn = 280;
		int endColumn = 378;
		int headerY = 32;
		
		commons.text().paintTo(g2, base.x + nameColumn, base.y + headerY, 10, TextRenderer.YELLOW, get("trade.name"));
		String countTitle = get("trade.count");
		int countTitleWidth = commons.text().getTextWidth(10, countTitle);
		commons.text().paintTo(g2, base.x + costColumn - countTitleWidth, base.y + headerY, 10, TextRenderer.YELLOW, countTitle);
		String costTitle = get("trade.cost");
		int costTitleWidth = commons.text().getTextWidth(10, costTitle);
		commons.text().paintTo(g2, base.x + endColumn - costTitleWidth, base.y + headerY, 10, TextRenderer.YELLOW, costTitle);
		
		Shape shape0 = g2.getClip();
		
		g2.clipRect(mainList.x, mainList.y, mainList.width, mainList.height);
		
		int color = TextRenderer.ORANGE;
		
		List<InventoryItem> itemList = itemList();
		int ty = mainList.y;
		InventoryItem selectedItem = null;
		for (int i = 0; i < itemList.size(); i++) {
			InventoryItem ii = itemList.get(i);
			if (ii.id == selected) {
				selectedItem = ii;
			}
			if (i >= topIndex) {
				int ty0 = ty + 2;
				commons.text().paintTo(g2, base.x + nameColumn, ty0, 10, color, ii.type.name);
	
				String countStr = Integer.toString(ii.count);
				int countWidth = commons.text().getTextWidth(10, countStr);
				commons.text().paintTo(g2, base.x + costColumn - countWidth, ty0, 10, color, countStr);
	
				String costStr = String.format("%,d", player().blackMarketCost(ii));
				int costWidth = commons.text().getTextWidth(10, costStr);
				commons.text().paintTo(g2, base.x + endColumn - costWidth, ty0, 10, color, costStr);
				
				if (selected == ii.id) {
					g2.setColor(new Color(color));
					g2.drawRect(mainList.x, ty, mainList.width - 4, rowHeight - 1);
				}
				
				ty += rowHeight;
			}
		}
		
		g2.setClip(shape0);

		boolean deliverToVisible = false; 

		if (selectedItem != null) {
			String nameStr = selectedItem.type.name;
			int nameStrWidth = commons.text().getTextWidth(14, nameStr);
			commons.text().paintTo(g2, base.x + 415 + (203 - nameStrWidth) / 2, base.y + 8, 14, TextRenderer.RED, nameStr);
			
			image.image(selectedItem.type.infoImage);
			setTooltipText(image, selectedItem.type.description);

			int ty1 = base.y + 200;

			if (selectedItem.nickname != null) {
				String fullName = selectedItem.type.longName;
				int fullNameWidth = commons.text().getTextWidth(10, fullName);
				commons.text().paintTo(g2, base.x + 415 + (203 - fullNameWidth) / 2, base.y + 200, 10, TextRenderer.RED, fullName);

				String nick;
				if (selectedItem.nicknameIndex == 0) {
					nick = selectedItem.nickname;
				} else {
					nick = selectedItem.nickname + " " + U.intToRoman(selectedItem.nicknameIndex + 1);
				}
				int nickWidth = commons.text().getTextWidth(7, nick);
				commons.text().paintTo(g2, base.x + 415 + (203 - nickWidth) / 2, base.y + 215, 7, TextRenderer.ORANGE, nick);
				ty1 += 30;
			}
			
			
			String itemCostStr = commons.format("trade.base_cost", selectedItem.type.productionCost);
			
			commons.text().paintTo(g2, base.x + 417, ty1 + 2, 10, TextRenderer.YELLOW, itemCostStr);
			
			loadoutList.y = ty1 + 20;
			loadoutList.height = base.y + 400 - ty1;
			
			Shape shape1 = g2.getClip();
			g2.clipRect(loadoutList.x, loadoutList.y, loadoutList.width, loadoutList.height);
			int ty2 = loadoutList.y;
			for (InventorySlot is : selectedItem.slots.values()) {
				int scolor = is.count == 0 ? TextRenderer.GRAY : (is.count == is.slot.max ? TextRenderer.ORANGE : TextRenderer.GREEN);

				String sn = is.type != null ? is.type.name : get("inventoryslot." + is.slot.id.toLowerCase());
				commons.text().paintTo(g2, loadoutList.x + 2, ty2, 7, scolor, sn);

				String count = is.count + " / " + is.slot.max;
				int countWidth = commons.text().getTextWidth(7, count);
				
				commons.text().paintTo(g2, loadoutList.x + loadoutList.width - countWidth, ty2, 7, TextRenderer.LIGHT_GREEN, count);
				
				ty2 += 10;
			}
			ty2 += 10;
			
			drawDPS(g2, selectedItem, ty2);
			
			g2.setClip(shape1);
			
			deliverToVisible = player().blackMarketRequiresPlanet(selectedItem.type);
			boolean canBuy = false;
			if (player().money() >= player().blackMarketCost(selectedItem)) {
				Planet p = planets.selected();
				if (deliverToVisible) {
					if (p != null) {
						if (selectedItem.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
							canBuy = p.getAddLimit(selectedItem.type, p.owner) > 0;
							if (!canBuy) {
								String noroom = get("trade.no_room");
								int noroomWidth = commons.text().getTextWidth(10, noroom);
								commons.text().paintTo(g2, buy.x - 5 - noroomWidth, buy.y + 5, 10, TextRenderer.RED, noroom);
							}
						} else {
							canBuy = true;
						}
					}
				} else {
					canBuy = true;
				}
			} else {
				String nomoney = get("trade.no_money");
				int noroomWidth = commons.text().getTextWidth(10, nomoney);
				commons.text().paintTo(g2, buy.x - 5 - noroomWidth, buy.y + 5, 10, TextRenderer.RED, nomoney);
			}
			buy.enabled(canBuy);
		} else {
			g2.drawImage(commons.spacewar().panelIg, base.x + 415, base.y + 30, 204, 170, null);
			buy.enabled(false);
			image.image(null);
			image.tooltip(null);
		}
		
		g2.drawImage(commons.info().baseRight, base.x + 618, base.y, null);

		deliverTo.visible(deliverToVisible);
		planets.visible(deliverToVisible);
		update();
		
		super.draw(g2);
		
		if (planets.itemCount() == 0 && deliverToVisible) {
			BufferedImage img = commons.equipment().noSpaceport;
			g2.drawImage(img, planets.x + (planets.width - img.getWidth()) / 2, planets.y + 3, null);
		}

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
			topIndex = itemList.size() - rowCount + 1;
		}
		if (topIndex < 0) {
			topIndex = 0;
		}
		scrollUp.enabled(topIndex != 0);
		scrollDown.enabled(topIndex + rowCount < itemList.size());
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
			if (mainList.contains(e.x, e.y)) {
				if (e.z < 0) {
					doScroll(-1);
					return true;
				} else
				if (e.z > 0) {
					doScroll(1);
					return true;
				}
			}
		} else
		if (e.has(Type.DOWN)) {
			if (mainList.contains(e.x, e.y)) {
				InventoryItem ii = inventoryAt(e.x, e.y);
				if (ii != null) {
					selected = ii.id;
				}
				return true;
			}
		}
		return super.mouse(e);
	}
	/**
	 * Contains the current navigation location.
	 * @author akarnokd, 2014-07-30
	 */
	static class RowNavigation {
		/** The current selected row's index. */
		int index;
		/** All items. */
		List<InventoryItem> list; 
		/** The current selected row inventory item if not null. */
		InventoryItem current;
	}
	/**
	 * @return the current selection's coordinates
	 */
	RowNavigation getSelectedRow() {
		RowNavigation r = new RowNavigation();
		r.index = -1;
		r.list = itemList();
		int j = 0;
		for (InventoryItem ii : r.list) {
			if (ii.id == selected) {
				r.index = j;
				r.current = ii;
				break;
			}
			j++;
		}
		
		return r;
	}
	/**
	 * Scroll the list to show the selection.
	 * @param i the row index to make visible
	 */
	void scrollToVisible(int i) {
		if (i < topIndex) {
			topIndex = i;
		} else
		if (i >= topIndex + rowCount) {
			topIndex = i - rowCount + 1;
		}
	}
	/**
	 * Select the inventory item row relative to the current selection row.
	 * @param delta the delta to select
	 */
	void selectByRow(int delta) {
		RowNavigation rn = getSelectedRow();
		if (!rn.list.isEmpty()) {
			int i = Math.max(0, Math.min(rn.index + delta, rn.list.size() - 1));
			selected = rn.list.get(i).id;
			scrollToVisible(i);
		} else {
			selected = -1;
		}
	}
	@Override
	public boolean keyboard(KeyEvent e) {
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP: {
			selectByRow(-1);
			e.consume();
			return true;
		}
		case KeyEvent.VK_PAGE_UP: {
			selectByRow(-rowCount);
			e.consume();
			return true;
		}
		case KeyEvent.VK_DOWN: {
			selectByRow(1);
			e.consume();
			return true;
		}
		case KeyEvent.VK_PAGE_DOWN: {
			selectByRow(rowCount);
			e.consume();
			return true;
		}
		default:
		}
		return super.keyboard(e);
	}
	/**
	 * Action to perform if a category button was pressed.
	 * @param button the button pressed
	 */
	void doCategoryPressAction(UIImageTabButton button) {
		for (UIImageTabButton btn : categories) {
			btn.down = btn == button;
		}
		topIndex = 0;
		int j = 0;
		for (InventoryItem ii : itemList()) {
			if (ii.id == selected) {
				scrollToVisible(j);
				break;
			}
			j++;
		}
	}
	/**
	 * Returns the inventory at the specified mouse coordinates or null if not in the list.
	 * @param mx the mouse X coordinate
	 * @param my the mouse Y coordinate
	 * @return the inventory or null
	 */
	InventoryItem inventoryAt(int mx, int my) {
		if (mainList.contains(mx, my)) {
			int row = (my - mainList.y) / rowHeight + topIndex;
			List<InventoryItem> list = itemList();
			if (row >= 0 && row < list.size()) {
				return list.get(row);
			}
		}
		return null;
	}
	/**
	 * Buy the current selected item and deliver it to the selected planet (if applicable).
	 */
	void doBuy() {
		Planet p = planets.selected();
		InventoryItem ii = null;
		for (InventoryItem ii0 : itemList()) {
			if (ii0.id == selected) {
				ii = ii0;
				break;
			}
		}
		if (ii == null) {
			buttonSound(SoundType.NOT_AVAILABLE);
			return;
		} else
		if (p == null && (player().blackMarketRequiresPlanet(ii.type))) {
			buttonSound(SoundType.NOT_AVAILABLE);
			return;
		} else
		if (player().money() < player().blackMarketCost(ii)) {
			buttonSound(SoundType.NOT_AVAILABLE);
			return;
		}
		player().buy(ii.id, p);
		if (ii.type.category.main == ResearchMainCategory.SPACESHIPS) {
			screenSound(SoundType.SHIP_DEPLOYED);
		} else {
			screenSound(SoundType.CLICK_HIGH_2);
		}
	}
	/**
	 * Draw the health and dps values.
	 * @param g2 the graphics context
	 * @param ii the inventory item
	 * @param ty2 the text y position
	 */
	void drawDPS(Graphics2D g2, InventoryItem ii, int ty2) {
		if (ii.type.category.main != ResearchMainCategory.SPACESHIPS
				|| ii.type.category == ResearchSubCategory.SPACESHIPS_SATELLITES) {
			return;
		}
		int hpm = ii.hpMax();
		int spm = ii.shieldMax();
		String hp = format("trade.hp", hpm);
		String sp = format("trade.sp", spm);
		Pair<Double, Double> dmgDps = ii.maxDamageDPS();
		
		String def = format("trade.defense", hpm + spm);
		String dps = format("trade.dps", Math.round(dmgDps.first), Math.round(dmgDps.second));
		
		int x0 = base.x + 420;
		
		int y1 = ty2;
		commons.text().paintTo(g2, x0 + 5, y1, 7, TextRenderer.GREEN, hp);
		y1 += 10;
		if (spm >= 0) {
			commons.text().paintTo(g2, x0 + 5, y1, 7, TextRenderer.GREEN, sp);
			y1 += 10;
			commons.text().paintTo(g2, x0 + 5, y1, 7, TextRenderer.GREEN, def);
			y1 += 10;
		}
		commons.text().paintTo(g2, x0 + 5, y1, 7, TextRenderer.GREEN, dps);
	}
}
