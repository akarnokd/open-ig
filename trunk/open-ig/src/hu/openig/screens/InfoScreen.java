/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.TaxLevel;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageTabButton2;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;



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
	/** Colony info page. */
	@ModeUI(mode = { Screens.INFORMATION_COLONY })
	InfoPanel colonyInfo;
	/** The display mode. */
	Screens mode = Screens.INFORMATION_PLANETS;
	/** The minimap renderer. */
	@ModeUI(mode = { 
			Screens.INFORMATION_PLANETS, 
			Screens.INFORMATION_FINANCIAL, 
			Screens.INFORMATION_MILITARY, 
			Screens.INFORMATION_FLEETS, 
			Screens.INFORMATION_COLONY,
			Screens.INFORMATION_ALIENS
	})
	Minimap minimap;
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
		
		colonyInfo = new InfoPanel();
		
		minimap = new Minimap();
		
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
		
		minimap.displayFleets = mode == Screens.INFORMATION_FLEETS;
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
		
		colonyInfo.location(base.x + 10, base.y + 10);
		
		minimap.bounds(base.x + 415, base.y + 211, 202, 169);
	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(commons.info().base, base.x, base.y, null);
		
		if (mode == Screens.INFORMATION_COLONY) {
			colonyInfo.update();
		}
		
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
	/**
	 * The information panel showing some details.
	 * @author akarnokd
	 */
	class InfoPanel extends UIContainer {
		/** The planet name. */
		UILabel planet;
		/** Label field. */
		UILabel owner;
		/** Label field. */
		UILabel race;
		/** Label field. */
		UILabel surface;
		/** Label field. */
		UILabel population;
		/** Label field. */
		UILabel housing;
		/** Label field. */
		UILabel worker;
		/** Label field. */
		UILabel hospital;
		/** Label field. */
		UILabel food;
		/** Label field. */
		UILabel energy;
		/** Label field. */
		UILabel police;
		/** Label field. */
		UILabel taxIncome;
		/** Label field. */
		UILabel tradeIncome;
		/** Label field. */
		UILabel taxMorale;
		/** Label field. */
		UILabel taxLevel;
		/** Label field. */
		UILabel allocation;
		/** Label field. */
		UILabel autobuild;
		/** Label field. */
		UILabel other;
		/** The labels. */
		List<UILabel> lines;
		/** More tax. */
		UIImageButton taxMore;
		/** Less tax. */
		UIImageButton taxLess;
		/** Construct the label elements. */
		public InfoPanel() {
			int textSize = 10;
			planet = new UILabel("-", 14, commons.text());
			planet.location(10, 5);
			owner = new UILabel("-", textSize, commons.text());
			race = new UILabel("-", textSize, commons.text());
			surface = new UILabel("-", textSize, commons.text());
			population = new UILabel("-", textSize, commons.text());
			housing = new UILabel("-", textSize, commons.text());
			worker = new UILabel("-", textSize, commons.text());
			hospital = new UILabel("-", textSize, commons.text());
			food = new UILabel("-", textSize, commons.text());
			energy = new UILabel("-", textSize, commons.text());
			police = new UILabel("-", textSize, commons.text());
			taxIncome = new UILabel("-", textSize, commons.text());
			tradeIncome = new UILabel("-", textSize, commons.text());
			taxMorale = new UILabel("-", textSize, commons.text());
			taxLevel = new UILabel("-", textSize, commons.text());
			allocation = new UILabel("-", textSize, commons.text());
			autobuild = new UILabel("-", textSize, commons.text());
			other = new UILabel("-", textSize, commons.text());
			
			lines = Arrays.asList(
					owner, race, surface, population, housing, worker, hospital, food, energy, police,
					taxIncome, tradeIncome, taxMorale, taxLevel, allocation, autobuild, other
			);

			taxMore = new UIImageButton(commons.info().taxMore);
			taxMore.onClick = new Act() {
				@Override
				public void act() {
					doTaxMore();
				}
			};
			taxLess = new UIImageButton(commons.info().taxLess);
			taxLess.onClick = new Act() {
				@Override
				public void act() {
					doTaxLess();
				}
			};
			taxMore.setDisabledPattern(commons.common().disabledPattern);
			taxLess.setDisabledPattern(commons.common().disabledPattern);

			addThis();
		}
		/** Compute the panel size based on its visible component sizes. */
		public void computeSize() {
			int textSize = 10;
			int w = 0;
			int h = 0;
			int i = 0;
			for (UILabel c : lines) {
				if (c.visible()) {
					c.x = 10;
					c.y = 25 + (textSize + 5) * i;
					c.size(textSize);
					c.height = textSize;
					w = Math.max(w, c.x + c.width);
					h = Math.max(h, c.y + c.height);
					i++;
				}
			}
			w = Math.max(w, this.planet.x + this.planet.width);
			
//			taxLess.location(taxLevel.x + 240, taxLevel.y + (taxLevel.height - taxLess.height) / 2);
			taxLess.location(taxIncome.x + 240, taxIncome.y);
			taxMore.location(taxLess.x + taxLess.width + 5, taxLess.y);
			
			w = Math.max(w, taxMore.x + taxMore.width);
			
			width = w + 10;
			height = h + 5;
		}
		/**
		 * Update the display values based on the current planet's settings.
		 */
		public void update() {
			Planet p = planet();
			
			if (p == null) {
				return;
			}
			
			planet.text(p.name, true);
			
			String s = p.owner != null ? p.owner.name : "-";
			owner.text(format("colonyinfo.owner", s), true);
			if (p.owner != null) {
				planet.color(p.owner.color);
				owner.color(TextRenderer.GREEN);
			} else {
				planet.color(TextRenderer.GRAY);
				owner.color(TextRenderer.GREEN);
			}
			s = p.isPopulated() ? get(p.getRaceLabel()) : "-";
			race.text(format("colonyinfo.race", s), true);
			
			s = get(p.type.label);
			surface.text(format("colonyinfo.surface", s), true);
			
			if (p.isPopulated()) {
			
				population.text(format("colonyinfo.population", 
						p.population, get(p.getMoraleLabel()), withSign(p.population - p.lastPopulation)
				), true).visible(true);
				
				PlanetStatistics ps = p.getStatistics();
				
				setLabel(housing, "colonyinfo.housing", ps.houseAvailable, p.population).visible(true);
				setLabel(worker, "colonyinfo.worker", p.population, ps.workerDemand).visible(true);
				setLabel(hospital, "colonyinfo.hospital", ps.hospitalAvailable, p.population).visible(true);
				setLabel(food, "colonyinfo.food", ps.foodAvailable, p.population).visible(true);
				setLabel(energy, "colonyinfo.energy", ps.energyAvailable, ps.energyDemand).visible(true);
				setLabel(police, "colonyinfo.police", ps.policeAvailable, p.population).visible(true);
				
				taxIncome.text(format("colonyinfo.tax", 
						p.taxIncome
				), true).visible(true);
				tradeIncome.text(format("colonyinfo.trade",
						p.tradeIncome
				), true).visible(true);
				
				taxMorale.text(format("colonyinfo.tax-morale",
						p.morale, withSign(p.morale - p.lastMorale)
				), true).visible(true);
				taxLevel.text(format("colonyinfo.tax-level",
						get(p.getTaxLabel())
				), true).visible(true);
				
				allocation.text(format("colonyinfo.allocation",
						get(p.getAllocationLabel())
				), true).visible(true);
				
				autobuild.text(format("colonyinfo.autobuild",
						get(p.getAutoBuildLabel())
				), true).visible(true);
				
				doAdjustTaxButtons();
				taxLess.visible(p.owner == player());
				taxMore.visible(p.owner == player());
			} else {
				population.visible(false);
				housing.visible(false);
				worker.visible(false);
				hospital.visible(false);
				food.visible(false);
				energy.visible(false);
				police.visible(false);
				taxIncome.visible(false);
				tradeIncome.visible(false);
				taxMorale.visible(false);
				taxLevel.visible(false);
				allocation.visible(false);
				autobuild.visible(false);
				taxLess.visible(false);
				taxMore.visible(false);
			}
			other.text(format("colonyinfo.other",
					"" // FIXME list others
			), true);

			computeSize();
		}
		/** 
		 * Color the label according to the relation between the demand and available.
		 * @param label the target label
		 * @param format the format string to use
		 * @param demand the demand amount
		 * @param avail the available amount
		 * @return the label
		 */
		UILabel setLabel(UILabel label, String format, int avail, int demand) {
			label.text(format(format, avail, demand), true);
			if (demand <= avail) {
				label.color(TextRenderer.GREEN);
			} else
			if (demand < avail * 2) {
				label.color(TextRenderer.YELLOW);
			} else {
				label.color(TextRenderer.RED);
			}
			return label;
		}
		/**
		 * Add the +/- sign for the given integer value.
		 * @param i the value
		 * @return the string
		 */
		String withSign(int i) {
			if (i < 0) {
				return Integer.toString(i);
			} else
			if (i > 0) {
				return "+" + i;
			}
			return "0";
		}
	}
	/** Increase the taxation level. */
	void doTaxMore() {
		Planet p = planet();
		if (p != null) {
			TaxLevel l = p.tax;
			if (l.ordinal() < TaxLevel.values().length - 1) {
				p.tax = TaxLevel.values()[l.ordinal() + 1];
			}
		}
	}
	/** Adjust the tax button based on the current taxation level. */
	void doAdjustTaxButtons() {
		Planet p = planet();
		if (p != null) {
			TaxLevel l = p.tax;
			colonyInfo.taxMore.enabled(l.ordinal() < TaxLevel.values().length - 1);
			colonyInfo.taxLess.enabled(l.ordinal() > 0);
		} else {
			colonyInfo.taxMore.enabled(false);
			colonyInfo.taxLess.enabled(false);
		}
	}
	/** Decrease the taxation level. */
	void doTaxLess() {
		Planet p = planet();
		if (p != null) {
			TaxLevel l = p.tax;
			if (l.ordinal() > 0) {
				p.tax = TaxLevel.values()[l.ordinal() - 1];
			}
		}
	}
	/**
	 * The minimap renderer.
	 * @author akarnokd, Apr 2, 2011
	 */
	class Minimap extends UIComponent {
		/** Display fleets? */
		public boolean displayFleets;
		@Override
		public void draw(Graphics2D g2) {
			g2.drawImage(commons.world().galaxyModel.map, 0, 0, width, height, null);
			Shape save0 = g2.getClip();
			g2.clipRect(0, 0, width, height);
			RenderTools.paintGrid(g2, new Rectangle(0, 0, width, height), commons.starmap().gridColor, commons.text());
			// render planets
			for (Planet p : commons.world().planets) {
				if (knowledge(p, PlanetKnowledge.DISCOVERED) < 0) {
					continue;
				}
				int x0 = (p.x * width / commons.starmap().background.getWidth());
				int y0 = (p.y * height / commons.starmap().background.getHeight());
				int labelColor = TextRenderer.GRAY;
				if (p.owner != null && knowledge(p, PlanetKnowledge.OWNED) >= 0) {
					labelColor = p.owner.color;
				}
				g2.setColor(new Color(labelColor));
				g2.fillRect(x0 - 1, y0 - 1, 3, 3);
				if (p == planet()) {
					g2.setColor(new Color(TextRenderer.GRAY));
					g2.drawRect(x0 - 3, y0 - 3, 6, 6);
				}
			}
			if (displayFleets) {
				for (Fleet f : player().fleets.keySet()) {
					if (knowledge(f, FleetKnowledge.VISIBLE) >= 0) {
						int x0 = (f.x * width / commons.starmap().background.getWidth());
						int y0 = (f.y * height / commons.starmap().background.getHeight());
						int x1 = x0 - f.owner.fleetIcon.getWidth() / 2;
						int y1 = y0 - f.owner.fleetIcon.getHeight() / 2;
						g2.drawImage(f.owner.fleetIcon, x1, y1, null);
						if (f == fleet()) {
							g2.setColor(new Color(f.owner.color));
							g2.drawRect(x1 - 2, y1 - 2, f.owner.fleetIcon.getWidth() + 4, f.owner.fleetIcon.getHeight() + 4);
						}
					}
				}
			}
			g2.setClip(save0);
		}
		/** 
		 * Locate a planet at the given coordinates.
		 * @param x the mouse X coordinate
		 * @param y the mouse Y coordinate
		 * @return the planet or null if no planet is present
		 */
		public Planet getPlanetAt(int x, int y) {
			for (Planet p : commons.world().planets) {
				if (knowledge(p, PlanetKnowledge.DISCOVERED) < 0) {
					continue;
				}
				int x0 = (p.x * width / commons.starmap().background.getWidth());
				int y0 = (p.y * height / commons.starmap().background.getHeight());
				
				if (x >= x0 - 2 && x <= x0 + 2 && y >= y0 - 2 && y <= y0 + 2) {
					return p;
				}
			}		
			return null;
		}
		/** 
		 * Locate a fleet at the given coordinates.
		 * @param x the mouse X coordinate
		 * @param y the mouse Y coordinate
		 * @return the fleet or null if no planet is present
		 */
		public Fleet getFleetAt(int x, int y) {
			for (Fleet f : player().fleets.keySet()) {
				if (knowledge(f, FleetKnowledge.VISIBLE) >= 0) {
					int x0 = (f.x * width / commons.starmap().background.getWidth());
					int y0 = (f.y * height / commons.starmap().background.getHeight());
					int w = f.owner.fleetIcon.getWidth();
					int h = y0 - f.owner.fleetIcon.getHeight();
					int x1 = x0 - w / 2;
					int y1 = h / 2;
					
					if (x >= x1 - 1 && x <= x1 + w + 2 && y >= y1 - 1 && y <= y1 + h + 2) {
						return f;
					}
				}
			}		
			return null;
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.DOWN)) {
				if (displayFleets) {
					Fleet f = getFleetAt(e.x, e.y);
					if (f != null) {
						player().currentFleet = f;
						return true;
					}
				}
				Planet p = getPlanetAt(e.x, e.y);
				if (p != null) {
					player().currentPlanet = p;
					return true;
				}
			} else
			if (e.has(Type.DOUBLE_CLICK)) {
				if (displayFleets) {
					Fleet f = getFleetAt(e.x, e.y);
					if (f != null) {
						player().currentFleet = f;
						displaySecondary(Screens.EQUIPMENT_FLEET);
						return true;
					}
				}
				Planet p = getPlanetAt(e.x, e.y);
				if (p != null) {
					player().currentPlanet = p;
					displayPrimary(Screens.COLONY);
					return true;
				}
			}
			return super.mouse(e);
		}
	}
}
