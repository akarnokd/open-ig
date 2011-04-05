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
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageTabButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;



/**
 * The equipment screen.
 * @author akarnokd, 2010.01.11.
 */
public class EquipmentScreen extends ScreenBase {
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle();
	/** The left panel. */
	final Rectangle leftPanel = new Rectangle();
	/** The right panel. */
	final Rectangle rightPanel = new Rectangle();
	/** The info button. */
	UIImageButton infoButton;
	/** The production button. */
	UIImageButton productionButton;
	/** The research button. */
	UIImageButton researchButton;
	/** The bridge button. */
	UIImageButton bridgeButton;
	/** The large vertical starmap button right to the minimap. */
	UIImageButton starmapButton;
	/** The large vertical colony button right to the minimap. */
	UIImageButton colonyButton;
	/** The placeholder for the no-research-button case. */
	UIImage noResearch;
	/** The placeholder for the no-production-button case. */
	UIImage noProduction;
	/** Placeholder for the no-starmap-button case. */
	UIImage noStarmap;
	/** Placeholder for the no-colony-button case. */
	UIImage noColony;
	/** The previous button. */
	UIImageButton prev;
	/** The next button. */
	UIImageButton next;
	/** The fleet name. */
	UILabel fleetName;
	/** The spaceship count label. */
	UILabel spaceshipsLabel;
	/** The fighters count label. */
	UILabel fightersLabel;
	/** The vehicles count label. */
	UILabel vehiclesLabel;
	/** The spaceship count label. */
	UILabel spaceshipsMaxLabel;
	/** The fighters count label. */
	UILabel fightersMaxLabel;
	/** The vehicles count label. */
	UILabel vehiclesMaxLabel;
	/** The fleet status label. */
	UILabel fleetStatusLabel;
	/** Secondary label. */
	UILabel secondaryLabel;
	/** Secondary value.*/
	UILabel secondaryValue;
	/** Secondary fighters. */
	UILabel secondaryFighters;
	/** Secondary vehicles. */
	UILabel secondaryVehicles;
	/** Empty category image. */
	UIImage battleshipsAndStationsEmpty;
	/** Empty category  image. */
	UIImage cruisersEmpty;
	/** Empty category  image. */
	UIImage fightersEmpty;
	/** Empty category  image. */
	UIImage tanksEmpty;
	/** Empty category  image. */
	UIImage vehiclesEmpty;
	/** Battleships category. */
	UIImageTabButton battleships;
	/** Cruisers category. */
	UIImageTabButton cruisers;
	/** Fighters category. */
	UIImageTabButton fighters;
	/** Stations category. */
	UIImageTabButton stations;
	/** Tanks category. */
	UIImageTabButton tanks;
	/** Vehicles category. */
	UIImageTabButton vehicles;
	/** End splitting. */
	UIImageButton endSplit;
	/** End joining. */
	UIImageButton endJoin;
	/** No planet nearby error. */
	UIImage noPlanetNearby;
	/** No spaceport on the nearby planet. */
	UIImage noSpaceport;
	/** Not your planet. */
	UIImage notYourPlanet;
	/** New fleet button. */
	UIImageButton newButton;
	/** Add ship to fleet. */
	UIImageButton addButton;
	/** Delete ship from fleet. */
	UIImageButton deleteButton;
	/** Transfer ships between fleets or planet. */
	UIImageButton transferButton;
	/** Split a fleet. */
	UIImageButton splitButton;
	/** Join two fleets. */
	UIImageButton joinButton;
	/** Move one left. */
	UIImageButton left1;
	/** Move multiple left. */
	UIImageButton left2;
	/** Move all left. */
	UIImageButton left3;
	/** Move one right. */
	UIImageButton right1;
	/** Move multiple right.*/
	UIImageButton right2;
	/** Move all right.*/
	UIImageButton right3;
	/** Add one inner equipment. */
	UIImageButton addOne;
	/** Remove one inner equipment. */
	UIImageButton removeOne;
	/** The fleet listing button. */
	UIImageButton listButton;
	/** Inner equipment rectangle. */
	Rectangle innerEquipment;
	/** Show the inner equipment rectangle? */
	boolean innerEquipmentVisible;
	/** Inner equipment name. */
	UILabel innerEquipmentName;
	/** Inner equipment value. */
	UILabel innerEquipmentValue;
	/** Inner equipment separator. */
	UILabel innerEquipmentSeparator;
	/** The name and type of the current selected ship or equipment. */
	UILabel selectedNameAndType;
	/** The planet image. */
	UIImage planet;
	/** The equipment slot locations. */
	final List<TechnologySlot> slots = new ArrayList<TechnologySlot>();
	/** The rolling disk animation timer. */
	Timer animation;
	/** The current animation step counter. */
	int animationStep;
	/** The current equipment mode to render and behave. */
	public enum EquipmentMode {
		/** Manage an existing or new fleet. */
		MANAGE_FLEET,
		/** Share or combine existing fleets. */
		SHARE_OR_COMBINE,
		/** Manage a planet. */
		MANAGE_PLANET
	}
	/** The equipment categories. */
	public enum EquipmentCategory {
		/** Battleships. */
		BATTLESHIPS,
		/** Cruisers. */
		CRUISERS,
		/** Fighters. */
		FIGHTERS,
		/** Tanks. */
		TANKS,
		/** Vehicles. */
		VEHICLES,
		/** Stations. */
		STATIONS
	}
	/** The equipment mode. */
	Screens mode;
	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				commons.equipment().base.getWidth(), commons.equipment().base.getHeight());
		
		infoButton = new UIImageButton(commons.common().infoButton);
		bridgeButton = new UIImageButton(commons.common().bridgeButton);
		researchButton = new UIImageButton(commons.research().research);
		productionButton = new UIImageButton(commons.research().production);
		
		researchButton.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.RESEARCH);
			}
		};
		productionButton.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.PRODUCTION);
			}
		};
		bridgeButton.onClick = new Act() {
			@Override
			public void act() {
				displayPrimary(Screens.BRIDGE);
			}
		};
		infoButton.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.INFORMATION_INVENTIONS);
			}
		};
		
		starmapButton = new UIImageButton(commons.equipment().starmap);
		starmapButton.onClick = new Act() {
			@Override
			public void act() {
				displayPrimary(Screens.STARMAP);
			}
		};
		colonyButton = new UIImageButton(commons.equipment().planet);
		colonyButton.onClick = new Act() {
			@Override
			public void act() {
				displayPrimary(Screens.COLONY);
			}
		};
		
		noResearch = new UIImage(commons.common().emptyButton);
		noResearch.visible(false);
		noProduction = new UIImage(commons.common().emptyButton);
		noProduction.visible(false);
		noStarmap = new UIImage(commons.equipment().buttonMapEmpty);
		noStarmap.visible(false);
		noColony = new UIImage(commons.equipment().buttonMapEmpty);
		noColony.visible(false);
		
		endSplit = new UIImageButton(commons.equipment().endSplit);
		endSplit.visible(false);
		endJoin = new UIImageButton(commons.equipment().endJoin);
		endJoin.visible(false);
		
		prev = new UIImageButton(commons.starmap().backwards);
		next = new UIImageButton(commons.starmap().forwards);
		
		fleetName = new UILabel("Fleet1", 14, commons.text());
		fleetName.color(0xFFFF0000);
		
		spaceshipsLabel = new UILabel(format("equipment.spaceships", 0), 10, commons.text());
		fightersLabel = new UILabel(format("equipment.fighters", 0), 10, commons.text());
		vehiclesLabel = new UILabel(format("equipment.vehicles", 0), 10, commons.text());
		spaceshipsMaxLabel = new UILabel(format("equipment.max", 25), 10, commons.text());
		fightersMaxLabel = new UILabel(format("equipment.maxpertype", 30), 10, commons.text());
		vehiclesMaxLabel = new UILabel(format("equipment.max", 0), 10, commons.text());
		fleetStatusLabel = new UILabel("TODO", 10, commons.text());

		secondaryLabel = new UILabel(get("equipment.secondary"), 10, commons.text());
		secondaryValue = new UILabel("TODO", 10, commons.text());
		secondaryValue.color(0xFFFF0000);
		
		secondaryFighters = new UILabel(format("equipment.fighters", 0), 10, commons.text());
		secondaryVehicles = new UILabel(format("equipment.vehiclesandmax", 0, 8), 10, commons.text());
		
		battleshipsAndStationsEmpty = new UIImage(commons.equipment().categoryEmpty);
		battleshipsAndStationsEmpty.visible(false);
		cruisersEmpty = new UIImage(commons.equipment().categoryEmpty);
		cruisersEmpty.visible(false);
		fightersEmpty = new UIImage(commons.equipment().categoryEmpty);
		fightersEmpty.visible(false);
		tanksEmpty = new UIImage(commons.equipment().categoryEmpty);
		tanksEmpty.visible(false);
		vehiclesEmpty = new UIImage(commons.equipment().categoryEmpty);
		vehiclesEmpty.visible(false);
		
		battleships = new UIImageTabButton(commons.equipment().categoryBattleships);
		cruisers = new UIImageTabButton(commons.equipment().categoryCruisers);
		fighters = new UIImageTabButton(commons.equipment().categoryFighers);
		stations = new UIImageTabButton(commons.equipment().categorySpaceStations);
		stations.visible(false);
		tanks = new UIImageTabButton(commons.equipment().categoryTanks);
		vehicles = new UIImageTabButton(commons.equipment().categoryVehicles);
		
		battleships.onPress = categoryAction(EquipmentCategory.BATTLESHIPS);
		cruisers.onPress = categoryAction(EquipmentCategory.CRUISERS);
		fighters.onPress = categoryAction(EquipmentCategory.FIGHTERS);
		stations.onPress = categoryAction(EquipmentCategory.STATIONS);
		tanks.onPress = categoryAction(EquipmentCategory.TANKS);
		vehicles.onPress = categoryAction(EquipmentCategory.VEHICLES);
		
		slots.clear();
		// TODO for testing purposes only!
		for (int i = 0; i < 6; i++) {
			final int j = i;
			final TechnologySlot ts = new TechnologySlot(commons);
			ts.name = "TODO";
			ts.inventory = 1;
			ts.researching = true;
			ts.percent = 0.5f;
			ts.visible(true);
			ts.missingActiveLab = true;
			ts.image = rl.getImage("inventions/spaceships/fighters/fighter_" + (i + 1) + "");
			ts.onPress = new Act() {
				@Override
				public void act() {
					doSelectTechnology(ts, j);
				}
			};
			slots.add(ts);
		}
		slots.get(0).available = true;
		slots.get(0).researching = false;
		
		slots.get(2).missingActiveLab = false;
		slots.get(2).missingLab = true;

		slots.get(3).visible(false);
		
		slots.get(4).notResearchable = true;
		
		slots.get(5).visible(false);
		
		noPlanetNearby = new UIImage(commons.equipment().noPlanetNearby);
		noSpaceport = new UIImage(commons.equipment().noSpaceport);
		noSpaceport.visible(false);
		notYourPlanet = new UIImage(commons.equipment().notYourplanet);
		notYourPlanet.visible(false);
		
		newButton = new UIImageButton(commons.equipment().newFleet);
		newButton.visible(false);
		addButton = new UIImageButton(commons.equipment().add);
		addButton.visible(false);
		deleteButton = new UIImageButton(commons.equipment().delete);
		deleteButton.visible(false);
		transferButton = new UIImageButton(commons.equipment().transfer);
		transferButton.visible(false);
		splitButton = new UIImageButton(commons.equipment().split);
		splitButton.visible(false);

		addOne = new UIImageButton(commons.equipment().addOne);
		addOne.visible(false);
		removeOne = new UIImageButton(commons.equipment().removeOne);
		removeOne.visible(false);
		joinButton = new UIImageButton(commons.equipment().join);
		joinButton.visible(false);
		
		left1 = new UIImageButton(commons.equipment().moveLeft1);
		left1.visible(false);
		left2 = new UIImageButton(commons.equipment().moveLeft2);
		left2.visible(false);
		left3 = new UIImageButton(commons.equipment().moveLeft3);
		left3.visible(false);
		right1 = new UIImageButton(commons.equipment().moveRight1);
		right1.visible(false);
		right2 = new UIImageButton(commons.equipment().moveRight2);
		right2.visible(false);
		right3 = new UIImageButton(commons.equipment().moveRight3);
		right3.visible(false);

		listButton = new UIImageButton(commons.equipment().list);
		
		innerEquipment = new Rectangle();
		innerEquipmentName = new UILabel("TODO", 7, commons.text());
		innerEquipmentName.visible(false);
		innerEquipmentValue = new UILabel(format("equipment.innercount", 0, 0), 7, commons.text());
		innerEquipmentValue.visible(false);
		innerEquipmentSeparator = new UILabel("-----", 7, commons.text());
		innerEquipmentSeparator.visible(false);
		
		selectedNameAndType = new UILabel(format("equipment.selectednametype", "TODO", "TODO"), 10, commons.text());
		selectedNameAndType.color(0xFF6DB269);
		
		planet = new UIImage(commons.equipment().planetOrbit);
		
		animation = new Timer(100, new Act() {
			@Override
			public void act() {
				doAnimation();
			}
		});
		
		addThis();
		add(slots);
	}
	/**
	 * @param cat the category to set 
	 * @return Create an action which selects the given category. 
	 */
	Act categoryAction(final EquipmentCategory cat) {
		return new Act() {
			@Override
			public void act() {
				selectCategory(cat);
				askRepaint();
			}
		};
	}
	@Override
	public void onEnter(Screens mode) {
		onResize();
		if (mode == null) {
			this.mode = Screens.EQUIPMENT_FLEET;
		} else {
			this.mode = mode; 
		}
		animation.start();
	}

	@Override
	public void onLeave() {
		animation.stop();
	}

	@Override
	public void onFinish() {
		if (animation != null) {
			animation.stop();
			animation = null;
		}
	}

	@Override
	public void onResize() {
		RenderTools.centerScreen(base, width, height, true);
		
		leftPanel.setBounds(base.x + 1, base.y + 1, 318, 198);
		rightPanel.setBounds(leftPanel.x + leftPanel.width + 2, leftPanel.y, 318, 198);
		infoButton.location(base.x + 535, base.y + 303 - 20);
		productionButton.location(infoButton.x, infoButton.y + infoButton.height);
		researchButton.location(productionButton.x, productionButton.y + productionButton.height);
		bridgeButton.location(researchButton.x, researchButton.y + researchButton.height);
		
		starmapButton.location(base.x + 479, base.y + 303 - 20);
		colonyButton.location(starmapButton.x + 26, starmapButton.y);
		
		noResearch.location(researchButton.location());
		noProduction.location(productionButton.location());
		noStarmap.location(starmapButton.location());
		noColony.location(colonyButton.location());
		
		endJoin.location(infoButton.location());
		endSplit.location(infoButton.location());
		
		prev.location(base.x + 151, base.y + 304 - 20);
		next.location(base.x + 152 + 50, base.y + 304 - 20);
		
		fleetName.location(base.x + 3, base.y + 308 - 20);
		fleetName.width = 147;
		
		spaceshipsLabel.location(fleetName.x + 3, fleetName.y + 20);
		fightersLabel.location(spaceshipsLabel.x, spaceshipsLabel.y + 14);
		vehiclesLabel.location(fightersLabel.x, fightersLabel.y + 14);
		fleetStatusLabel.location(vehiclesLabel.x, vehiclesLabel.y + 14);
		
		spaceshipsMaxLabel.location(spaceshipsLabel.x + 110, spaceshipsLabel.y);
		fightersMaxLabel.location(fightersLabel.x + 110, fightersLabel.y);
		vehiclesMaxLabel.location(vehiclesLabel.x + 110, vehiclesLabel.y);
		
		secondaryLabel.location(fightersLabel.x, fleetStatusLabel.y + 22);
		secondaryValue.location(secondaryLabel.x + secondaryLabel.width + 8, secondaryLabel.y);
		secondaryFighters.location(secondaryLabel.x, secondaryLabel.y + 14);
		secondaryVehicles.location(secondaryLabel.x, secondaryFighters.y + 14);
		
		battleships.location(base.x + 2, base.y + 435 - 20);
		stations.location(battleships.location());
		cruisers.location(battleships.x + 50, battleships.y);
		fighters.location(cruisers.x + 50, cruisers.y);
		tanks.location(fighters.x + 50, fighters.y);
		vehicles.location(tanks.x + 50, tanks.y);
		
		battleshipsAndStationsEmpty.location(battleships.location());
		cruisersEmpty.location(cruisers.location());
		fightersEmpty.location(fighters.location());
		tanksEmpty.location(tanks.location());
		vehiclesEmpty.location(vehicles.location());
		
		for (int i = 0; i < 6; i++) {
			slots.get(i).location(base.x + 2 + i * 106, base.y + 219 - 20);
			slots.get(i).size(106, 82);
		}

		noPlanetNearby.location(base.x + 242, base.y + 194 - 20);
		noSpaceport.location(noPlanetNearby.location());
		notYourPlanet.location(noSpaceport.location());
		
		transferButton.location(base.x + 401, base.y + 194 - 20);
		joinButton.location(transferButton.location());
		splitButton.location(base.x + 480, base.y + 194 - 20);
		
		newButton.location(base.x + 560, base.y + 194 - 20);
		addButton.location(base.x + 322, base.y + 194 - 20);
		addOne.location(addButton.location());
		deleteButton.location(base.x + 242, base.y + 194 - 20);
		removeOne.location(deleteButton.location());
		
		listButton.location(base.x + 620, base.y + 49 - 20);
		
		right1.location(base.x + 322, base.y + 191 - 20);
		right2.location(right1.x + 48, right1.y);
		right3.location(right2.x + 48, right2.y);
		left1.location(base.x + 272, base.y + 191 - 20);
		left2.location(left1.x - 48, left1.y);
		left3.location(left2.x - 48, left2.y);
		
		innerEquipment.setBounds(base.x + 325, base.y + 156 - 20, 120, 35);
		innerEquipmentName.location(innerEquipment.x + 5, innerEquipment.y + 4);
		innerEquipmentSeparator.location(innerEquipmentName.x, innerEquipmentName.y + 10);
		innerEquipmentValue.location(innerEquipmentName.x, innerEquipmentName.y + 20);
		
		selectedNameAndType.location(base.x + 326, base.y + 28 - 20);
		
		planet.location(leftPanel.x - 1, leftPanel.y - 1);
	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(commons.equipment().base, base.x, base.y, null);
		
//		for (TechnologySlot r : slots) {
//			r.draw(g2);
//		}
		
		if (innerEquipmentVisible) {
			g2.setColor(new Color(0xFF4D7DB6));
			g2.drawRect(innerEquipment.x, innerEquipment.y, innerEquipment.width - 1, innerEquipment.height - 1);
		}
		super.draw(g2);
	}
	/**
	 * Set the equipment rendering mode. Use this before
	 * switching to the equipment screen.
	 * Based on this mode, the screen will use the current planet,
	 * the current fleet (with the nearest other fleet)
	 * @param mode the screen mode
	 */
	public void setEquipmentMode(EquipmentMode mode) {
		switch (mode) {
		case MANAGE_FLEET:
			spaceshipsLabel.visible(true);
			spaceshipsMaxLabel.visible(true);
			fleetStatusLabel.visible(true);
			secondaryLabel.visible(true);
			secondaryValue.visible(true);
			secondaryFighters.visible(true);
			secondaryVehicles.visible(true);
			
			battleships.visible(true);
			stations.visible(false);
			cruisers.visible(true);
			cruisersEmpty.visible(false);
			
			left1.visible(false);
			left2.visible(false);
			left3.visible(false);
			right1.visible(false);
			right2.visible(false);
			right3.visible(false);

			starmapButton.visible(true);
			colonyButton.visible(true);
			noStarmap.visible(false);
			noColony.visible(false);
			
			break;
		case MANAGE_PLANET:
			fleetStatusLabel.visible(false);
			secondaryLabel.visible(false);
			secondaryValue.visible(false);
			secondaryFighters.visible(false);
			secondaryVehicles.visible(false);
			spaceshipsLabel.visible(false);
			spaceshipsMaxLabel.visible(false);
			
			battleships.visible(false);
			stations.visible(true);
			cruisers.visible(false);
			cruisersEmpty.visible(true);
			
			battleships.down = false;
			cruisers.down = false;
			
			left1.visible(false);
			left2.visible(false);
			left3.visible(false);
			right1.visible(false);
			right2.visible(false);
			right3.visible(false);

			starmapButton.visible(true);
			colonyButton.visible(true);
			noStarmap.visible(false);
			noColony.visible(false);
			break;
		case SHARE_OR_COMBINE:
			spaceshipsLabel.visible(true);
			spaceshipsMaxLabel.visible(true);
			fleetStatusLabel.visible(true);
			secondaryLabel.visible(true);
			secondaryValue.visible(true);
			secondaryFighters.visible(true);
			secondaryVehicles.visible(true);
			
			battleships.visible(true);
			stations.visible(false);
			cruisers.visible(true);
			cruisersEmpty.visible(false);
			
			left1.visible(true);
			left2.visible(true);
			left3.visible(true);
			right1.visible(true);
			right2.visible(true);
			right3.visible(true);
			
			starmapButton.visible(false);
			colonyButton.visible(false);
			noStarmap.visible(true);
			noColony.visible(true);
			break;
		default:
		}
	}
	/**
	 * Select/deselect a category button. Does not ask for repaint.
	 * @param cat the category, use null to deselect all
	 */
	public void selectCategory(EquipmentCategory cat) {
		battleships.down = cat == EquipmentCategory.BATTLESHIPS;
		cruisers.down = cat == EquipmentCategory.CRUISERS;
		fighters.down = cat == EquipmentCategory.FIGHTERS;
		stations.down = cat == EquipmentCategory.STATIONS;
		tanks.down = cat == EquipmentCategory.TANKS;
		vehicles.down = cat == EquipmentCategory.VEHICLES;
	}
	/**
	 * Update animating components.
	 */
	void doAnimation() {
		if (animationStep == Integer.MAX_VALUE) {
			animationStep = 0;
		} else {
			animationStep ++;
		}
		for (TechnologySlot sl : slots) {
			sl.animationStep = animationStep;
		}
		askRepaint(base);
	}
	/**
	 * Select a technology slot.
	 * @param ts the target technology slot.
	 * @param j the slot index
	 */
	protected void doSelectTechnology(TechnologySlot ts, int j) {
		for (TechnologySlot ts0 : slots) {
			ts0.selected = ts0.visible() && ts0 == ts;
		}
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
}
