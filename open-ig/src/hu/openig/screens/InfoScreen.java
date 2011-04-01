/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.render.RenderTools;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageTabButton2;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.Iterator;



/**
 * The information screen renderer.
 * @author akarnokd, 2010.01.11.
 */
public class InfoScreen extends ScreenBase {
	/** 
	 * The annotation to indicate which UI elements should by default
	 * be visible on the screen. Mark only uncommon elements.
	 * @author akarnokd, Mar 19, 2011
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@interface ModeUI {
		/** The expected screen mode. */
		Screens[] mode();
	}
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle();
	
	/** Tab button. */
	UIImageTabButton2 planetsTab;
	/** Tab button. */
	UIImageTabButton2 colonyTab;
	/** Tab button. */
	UIImageTabButton2 militaryTab;
	/** Tab button. */
	UIImageTabButton2 financialTab;
	/** Tab button. */
	UIImageTabButton2 fleetsTab;
	/** Tab button. */
	UIImageTabButton2 buildingsTab;
	/** Tab button. */
	UIImageTabButton2 inventionsTab;
	/** Tab button. */
	UIImageTabButton2 aliensTab;
	/** Empty inventions. */
	UIImage inventionsEmpty;
	/** Empty aliens. */
	UIImage aliensEmpty;
	
	/** Screen switch button. */
	@ModeUI(mode = { 
			Screens.INFORMATION_PLANETS, 
			Screens.INFORMATION_BUILDINGS, 
			Screens.INFORMATION_FINANCIAL, 
			Screens.INFORMATION_MILITARY, 
			Screens.INFORMATION_COLONY 
	})
	UIImageButton colony;
	/** Screen switch button. */
	@ModeUI(mode = { Screens.INFORMATION_INVENTIONS })
	UIImageButton research;
	/** Screen switch button. */
	@ModeUI(mode = { Screens.INFORMATION_INVENTIONS })
	UIImageButton production;
	/** Screen switch button. */
	@ModeUI(mode = { 
			Screens.INFORMATION_PLANETS, 
			Screens.INFORMATION_BUILDINGS, 
			Screens.INFORMATION_FINANCIAL, 
			Screens.INFORMATION_MILITARY, 
			Screens.INFORMATION_FLEETS, 
			Screens.INFORMATION_COLONY 
	})
	UIImageButton starmap;
	/** Screen switch button. */
	@ModeUI(mode = { Screens.INFORMATION_ALIENS })
	UIImageButton diplomacy;
	/** Screen switch button. */
	@ModeUI(mode = { Screens.INFORMATION_FLEETS })
	UIImageButton equipment;
	/** The screen switch button. */
	UIImage empty1;
	/** The empty switch button. */
	UIImage empty2;
	/** The display mode. */
	Screens mode = Screens.INFORMATION_PLANETS;
	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				commons.info().base.getWidth(), 
				commons.info().base.getHeight());
		
		planetsTab = changeMode(new UIImageTabButton2(commons.info().planets), Screens.INFORMATION_PLANETS);
		colonyTab = changeMode(new UIImageTabButton2(commons.info().colonyInfo), Screens.INFORMATION_COLONY);
		militaryTab = changeMode(new UIImageTabButton2(commons.info().militaryInfo), Screens.INFORMATION_MILITARY);
		financialTab = changeMode(new UIImageTabButton2(commons.info().financialInfo), Screens.INFORMATION_FINANCIAL);
		fleetsTab = changeMode(new UIImageTabButton2(commons.info().fleets), Screens.INFORMATION_FLEETS);
		buildingsTab = changeMode(new UIImageTabButton2(commons.info().buildings), Screens.INFORMATION_BUILDINGS);
		inventionsTab = changeMode(new UIImageTabButton2(commons.info().inventions), Screens.INFORMATION_INVENTIONS);
		aliensTab = changeMode(new UIImageTabButton2(commons.info().aliens), Screens.INFORMATION_ALIENS);
		
		inventionsEmpty = new UIImage(commons.info().emptyButton);
		inventionsEmpty.z = -1;
		aliensEmpty = new UIImage(commons.info().emptyButton);
		aliensEmpty.z = -1;
		
		colony = new UIImageButton(commons.info().colony);
		colony.onClick = new Act() {
			@Override
			public void act() {
				displayPrimary(Screens.COLONY);
			}
		};
		research = new UIImageButton(commons.info().research);
		research.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.RESEARCH);
			}
		};
		production = new UIImageButton(commons.info().production);
		production.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.PRODUCTION);
			}
		};
		starmap = new UIImageButton(commons.info().starmap);
		starmap.onClick = new Act() {
			@Override
			public void act() {
				displayPrimary(Screens.STARMAP);
			}
		};
		diplomacy = new UIImageButton(commons.info().diplomacy);
		diplomacy.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.DIPLOMACY);
			}
		};
		equipment = new UIImageButton(commons.info().equipment);
		equipment.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.COLONY);
			}
		};
		
		empty1 = new UIImage(commons.common().emptyButton);
		empty2 = new UIImage(commons.common().emptyButton);
		
		inventionsEmpty.z = -1;
		aliensEmpty.z = -1;
		empty1.z = -1;
		empty2.z = -1;
		
		addThis();
	}
	/**
	 * Add an onPress handler which changes the display mode.
	 * @param button the button to append to
	 * @param mode the new mode
	 * @return the button
	 */
	UIImageTabButton2 changeMode(UIImageTabButton2 button, final Screens mode) {
		button.onPress = new Act() {
			@Override
			public void act() {
				InfoScreen.this.mode = mode;
				applyMode();
			}
		};
		return button;
	}
	@Override
	public void onEnter(Screens mode) {
		this.mode = mode != null ? mode : Screens.INFORMATION_PLANETS;
		applyMode();
	}
	/** Adjust the visibility of fields and buttons. */
	void applyMode() {
		setUIVisibility();
		production.visible(production.visible() && commons.world().level >= 2);
		research.visible(research.visible() && commons.world().level >= 3);

		inventionsTab.visible(commons.world().level >= 2);
		aliensTab.visible(commons.world().level >= 4);
		
		planetsTab.selected = mode == Screens.INFORMATION_PLANETS;
		colonyTab.selected = mode == Screens.INFORMATION_COLONY;
		militaryTab.selected = mode == Screens.INFORMATION_MILITARY;
		financialTab.selected = mode == Screens.INFORMATION_FINANCIAL;
		fleetsTab.selected = mode == Screens.INFORMATION_FLEETS;
		buildingsTab.selected = mode == Screens.INFORMATION_BUILDINGS;
		inventionsTab.selected = mode == Screens.INFORMATION_INVENTIONS;
		aliensTab.selected = mode == Screens.INFORMATION_ALIENS;
		
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
		RenderTools.centerScreen(base, width, height, true);
		
		planetsTab.location(base.x, base.y + base.height - planetsTab.height - fleetsTab.height);
		fleetsTab.location(base.x, base.y + base.height - fleetsTab.height);
		
		colonyTab.location(planetsTab.x + planetsTab.width + 1, planetsTab.y);
		militaryTab.location(colonyTab.x + colonyTab.width + 1, colonyTab.y);
		financialTab.location(militaryTab.x + militaryTab.width + 1, militaryTab.y);
		
		buildingsTab.location(fleetsTab.x + fleetsTab.width + 1, fleetsTab.y);
		inventionsTab.location(buildingsTab.x + buildingsTab.width + 1, buildingsTab.y);
		aliensTab.location(inventionsTab.x + inventionsTab.width + 1, inventionsTab.y);
		
		inventionsEmpty.location(inventionsTab.location());
		aliensEmpty.location(aliensTab.location());
		
		empty1.location(aliensEmpty.x + aliensEmpty.width + 2, base.y + base.height - empty1.height);
		empty2.location(empty1.x + empty1.width, empty1.y);
		
		colony.location(empty1.location());
		starmap.location(empty2.location());
		
		production.location(empty1.location());
		research.location(empty2.location());
		diplomacy.location(empty1.location());
		equipment.location(empty1.location());
	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(commons.info().base, base.x, base.y, null);
		
		super.draw(g2);
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (!base.contains(e.x, e.y) && e.has(Type.UP)) {
			hideSecondary();
			return true;
		} else {
			return super.mouse(e);
		}
	}
	@Override
	public Screens screen() {
		return mode;
	}
	/**
	 * Set the visibility of UI components based on their annotation.
	 */
	void setUIVisibility() {
		for (Field f : getClass().getDeclaredFields()) {
			ModeUI mi = f.getAnnotation(ModeUI.class);
			if (mi != null) {
				if (UIComponent.class.isAssignableFrom(f.getType())) {
					try {
						UIComponent c = UIComponent.class.cast(f.get(this));
						if (c != null) {
							c.visible(contains(mode, mi.mode()));
						}
					} catch (IllegalAccessException ex) {
						ex.printStackTrace();
					}
				} else
				if (Iterable.class.isAssignableFrom(f.getType())) {
					try {
						Iterable<?> it = Iterable.class.cast(f.get(this));
						if (it != null) {
							Iterator<?> iter = it.iterator();
							while (iter.hasNext()) {
								Object o = iter.next();
								if (UIComponent.class.isAssignableFrom(o.getClass())) {
									UIComponent c = UIComponent.class.cast(o);
									c.visible(contains(mode, mi.mode()));
								}
							}
						}
					} catch (IllegalAccessException ex) {
						ex.printStackTrace();
					}
				}
			}
		}
	}
	/**
	 * Test if the array contains the give item.
	 * @param <T> the element type
	 * @param item the item to find
	 * @param array the array of Ts
	 * @return true if the item is in the array
	 */
	static <T> boolean contains(T item, T... array) {
		for (T a : array) {
			if (item == a || (item != null && item.equals(a))) {
				return true;
			}
		}
		return false;
	}
}
