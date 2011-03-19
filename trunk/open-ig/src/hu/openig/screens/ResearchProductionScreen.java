/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.core.ResourceType;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchSubCategory;
import hu.openig.render.RenderTools;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageTabButton;
import hu.openig.ui.UIVideoImage;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import javax.swing.Timer;



/**
 * The combined research and production screen.
 * @author akarnokd, 2010.01.11.
 */
public class ResearchProductionScreen extends ScreenBase {
	/** The equipment slot locations. */
	final List<TechnologySlot> slots = new ArrayList<TechnologySlot>();
	/** The rolling disk animation timer. */
	Timer animation;
	/** The current animation step counter. */
	int animationStep;
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle();
	/** The add production button. */
	UIImageButton addButton;
	/** The remove production button. */
	UIImageButton removeButton;
	/** The empty button. */
	UIImage emptyButton;
	/** The production button. */
	UIImageButton productionButton;
	/** The research button. */
	UIImageButton researchButton;
	/** The equipment button. */
	UIImageButton equipmentButton;
	/** The bridge button. */
	UIImageButton bridgeButton;
	/** The video output. */
	UIVideoImage video;
	/** The video renderer. */
	VideoRenderer videoRenderer;
	/** The main category panel. */
	UIImage mainCategory;
	/** The subcategory panel for the research screen. */
	UIImage subCategorySmall;
	/** The subcategory panel for the production screen. */
	UIImage subCategoryWide;
	/** The prerequisites panel for the research screen. */
	UIImage prerequisites;
	/** The base panel for the production listings. */
	UIImage productionBase;
	/** The research settings for the selected technology base panel. */
	UIImage selectedResearchBase;
	/** The currenly running research base panel. */
	UIImage activeResearchBase;
	/** The description of the selected technology base panel. */
	UIImage descriptionBase;
	/** Start a new research button. */
	UIImageButton startNew;
	/** Stop the current research. */
	UIImageButton stopActive;
	/** Stop active empty button. */
	UIImage stopActiveEmpty;
	/** View the active research. */
	UIImageButton viewActive;
	/** View active empty button. */
	UIImage viewActiveEmpty;
	/** The spaceships main category. */
	UIImageTabButton spaceshipsLabel;
	/** The equipments main category. */
	UIImageTabButton equipmentsLabel;
	/** The weapons main category. */
	UIImageTabButton weaponsLabel;
	/** The buildings main category. */
	UIImageTabButton buildingsLabel;
	/** Screen mode. */
	public enum RPMode {
		/** Production. */
		PRODUCTION,
		/** Research. */
		RESEARCH,
	}
	/** The labels associated with various main categories. */
	final Map<ResearchMainCategory, UIImageTabButton> mainComponents = new HashMap<ResearchMainCategory, UIImageTabButton>();
	/** The labels associated with various sub categories. */
	final Map<ResearchSubCategory, UIImageTabButton> subComponents = new HashMap<ResearchSubCategory, UIImageTabButton>();
	/** The indicator for the currently running research. */
	ResearchSubCategory activeCategory = ResearchSubCategory.EQUIPMENT_HYPERDRIVES;
	/** The screen mode mode. */
	RPMode mode;
	@Override
	public void onInitialize() {
		base.setBounds(0, 0, 
				commons.equipment().base.getWidth(), commons.equipment().base.getHeight());
		animation = new Timer(100, new Act() {
			@Override
			public void act() {
				doAnimation();
			}
		});
		
		addButton = new UIImageButton(commons.research().add);
		removeButton = new UIImageButton(commons.research().remove);
		removeButton.visible(false);
		emptyButton = new UIImage(commons.research().emptyElevated);
		emptyButton.visible(false);
		bridgeButton = new UIImageButton(commons.common().bridgeButton);
		researchButton = new UIImageButton(commons.research().research);
		productionButton = new UIImageButton(commons.research().production);
		equipmentButton = new UIImageButton(commons.research().equipmentButton);

		mainCategory = new UIImage(commons.research().mainClassPanel);
		mainCategory.z = -1;
		subCategorySmall = new UIImage(commons.research().subtypePanel);
		subCategorySmall.z = -1;
		subCategoryWide = new UIImage(commons.research().subtypeWidePanel);
		subCategoryWide.z = -1;
		prerequisites = new UIImage(commons.research().requirementsPanel);
		prerequisites.z = -1;
		productionBase = new UIImage(commons.research().productionBasePanel);
		productionBase.z = -1;
		selectedResearchBase = new UIImage(commons.research().selectedResearchPanel);
		selectedResearchBase.z = -1;
		activeResearchBase = new UIImage(commons.research().activeResearchPanel);
		activeResearchBase.z = -1;
		descriptionBase = new UIImage(commons.research().researchInfoPanel);
		descriptionBase.z = -1;
		
		startNew = new UIImageButton(commons.research().start);
		stopActive = new UIImageButton(commons.research().stop);
		viewActive = new UIImageButton(commons.research().view);
		
		stopActiveEmpty = new UIImage(commons.research().emptySmall);
		stopActiveEmpty.z = -1;
		viewActiveEmpty = new UIImage(commons.research().emptySmall);
		viewActiveEmpty.z = -1;
		
		video = new UIVideoImage();
		
		spaceshipsLabel = new UIImageTabButton(commons.research().spaceships);
		spaceshipsLabel.onPress = new Act() {
			@Override
			public void act() {
				selectMainCategory(ResearchMainCategory.SPACESHIPS);
			}
		};
		equipmentsLabel = new UIImageTabButton(commons.research().equipment);
		equipmentsLabel.onPress = new Act() {
			@Override
			public void act() {
				selectMainCategory(ResearchMainCategory.EQUIPMENT);
			}
		};
		weaponsLabel = new UIImageTabButton(commons.research().weapons);
		weaponsLabel.onPress = new Act() {
			@Override
			public void act() {
				selectMainCategory(ResearchMainCategory.WEAPONS);
			}
		};
		buildingsLabel = new UIImageTabButton(commons.research().buildings);
		buildingsLabel.onPress = new Act() {
			@Override
			public void act() {
				selectMainCategory(ResearchMainCategory.BUILDINS);
			}
		};
		mainComponents.put(ResearchMainCategory.SPACESHIPS, spaceshipsLabel);
		mainComponents.put(ResearchMainCategory.EQUIPMENT, equipmentsLabel);
		mainComponents.put(ResearchMainCategory.WEAPONS, weaponsLabel);
		mainComponents.put(ResearchMainCategory.BUILDINS, buildingsLabel);
		
		createSubCategory(ResearchSubCategory.SPACESHIPS_FIGHTERS, commons.research().fighters);
		createSubCategory(ResearchSubCategory.SPACESHIPS_CRUISERS, commons.research().cruisers);
		createSubCategory(ResearchSubCategory.SPACESHIPS_BATTLESHIPS, commons.research().battleships);
		createSubCategory(ResearchSubCategory.SPACESHIPS_SATELLITES, commons.research().satellites);
		createSubCategory(ResearchSubCategory.SPACESHIPS_STATIONS, commons.research().spaceStations);

		createSubCategory(ResearchSubCategory.EQUIPMENT_HYPERDRIVES, commons.research().hyperdrives);
		createSubCategory(ResearchSubCategory.EQUIPMENT_MODULES, commons.research().modules);
		createSubCategory(ResearchSubCategory.EQUIPMENT_RADARS, commons.research().radars);
		createSubCategory(ResearchSubCategory.EQUIPMENT_SHIELDS, commons.research().shields);

		createSubCategory(ResearchSubCategory.WEAPONS_LASERS, commons.research().lasers);
		createSubCategory(ResearchSubCategory.WEAPONS_CANNONS, commons.research().cannons);
		createSubCategory(ResearchSubCategory.WEAPONS_PROJECTILES, commons.research().projectiles);
		createSubCategory(ResearchSubCategory.WEAPONS_TANKS, commons.research().tanks);
		createSubCategory(ResearchSubCategory.WEAPONS_VEHICLES, commons.research().vehicles);

		createSubCategory(ResearchSubCategory.BUILDINGS_CIVIL, commons.research().civilBuildings);
		createSubCategory(ResearchSubCategory.BUILDINGS_MILITARY, commons.research().militaryBuildings);
		createSubCategory(ResearchSubCategory.BUILDINGS_RADARS, commons.research().radarBuildings);
		createSubCategory(ResearchSubCategory.BUILDINGS_GUNS, commons.research().planetaryGuns);

		// TODO for testing purposes only!
		for (int i = 0; i < 6; i++) {
			TechnologySlot ts = new TechnologySlot(commons);
			ts.name = "TODO";
			ts.inventory = 1;
			ts.cost = 1000;
			ts.researching = true;
			ts.percent = 0.5f;
			ts.visible(true);
			ts.missingLab = true;
			ts.image = rl.getImage(commons.language(), "inventions/spaceships/fighters/fighter_" + (i + 1) + "");
			slots.add(ts);
		}
		slots.get(0).available = true;
		slots.get(0).researching = false;
		
		slots.get(2).missingLab = false;
		slots.get(2).missingPrerequisite = true;

		slots.get(3).visible(false);
		
		slots.get(4).notResearchable = true;
		
		slots.get(5).visible(false);

		researchButton.onClick = new Act() {
			@Override
			public void act() {
				setMode(RPMode.RESEARCH);
			}
		};
		productionButton.onClick = new Act() {
			@Override
			public void act() {
				setMode(RPMode.PRODUCTION);
			}
		};
		equipmentButton.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displaySecondary(Screens.EQUIPMENT);
			}
		};
		addButton.onClick = new Act() {
			@Override
			public void act() {
				// TODO
			}
		};
		removeButton.onClick = new Act() {
			@Override
			public void act() {
				// TODO
			}
		};
		bridgeButton.onClick = new Act() {
			@Override
			public void act() {
				commons.control.displayPrimary(Screens.BRIDGE);
			}
		};
		
		addThis();
		add(slots);
	}
	
	/**
	 * Distribute the given components equally within the given target rectangle.
	 * @param left the common left coordinate to use
	 * @param target the target rectangle
	 * @param items the list of components
	 */
	void distributeVertically(int left, Rectangle target, List<? extends UIComponent> items) {
		int sum = 0;
		for (UIComponent c : items) {
			c.x = left;
			sum += c.height;
		}
		float space = (target.height - sum) * 1.0f / (items.size() + 1);
		float top = space + target.y;
		for (int i = 0; i < items.size(); i++) {
			UIComponent c = items.get(i);
			c.y = (int)top;
			top += space + c.height;
		}
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
		askRepaint();
	}

	@Override
	public void onEnter(Object mode) {
		onResize();
		if (mode == null || mode == RPMode.PRODUCTION) {
			setMode(RPMode.PRODUCTION);
		} else {
			setMode(RPMode.RESEARCH);
		}
		animation.start();
		
		videoRenderer = new VideoRenderer(new CyclicBarrier(1), new CyclicBarrier(1), video, 
				rl.get(commons.language(), "technology/spaceships/fighters/fighter_1", ResourceType.VIDEO), "Research-Production-Video");
		videoRenderer.setRepeat(true);
//		videoRenderer.setFpsOverride(15d);
		videoRenderer.start();
	}

	@Override
	public void onLeave() {
		animation.stop();
		if (videoRenderer != null) {
			videoRenderer.stopPlayback();
		}
	}

	@Override
	public void onFinish() {
		onLeave();
		videoRenderer = null;
		animation = null;
	}

	@Override
	public void onResize() {
		RenderTools.centerScreen(base, getInnerWidth(), getInnerHeight(), true);

		addButton.location(base.x + 535, base.y + 303 - 20);
		startNew.location(addButton.location());
		removeButton.location(addButton.location());
		emptyButton.location(addButton.location());

		video.location(base.x + 2, base.y + 2);
		video.size(316, 196);
		
		equipmentButton.location(addButton.x, addButton.y + addButton.height);
		
		productionButton.location(equipmentButton.x, equipmentButton.y + equipmentButton.height);
		researchButton.location(productionButton.location());
		
		bridgeButton.location(researchButton.x, researchButton.y + researchButton.height);
		
		for (int i = 0; i < 6; i++) {
			slots.get(i).location(base.x + 2 + i * 106, base.y + 219 - 20);
			slots.get(i).size(106, 82);
		}

		mainCategory.location(base.x + 323, base.y + 3);
		subCategorySmall.location(mainCategory.x, base.y + 103);
		subCategoryWide.location(subCategorySmall.location());
		prerequisites.location(base.x + 533, subCategorySmall.y);
		
		productionBase.location(base.x + 3, base.y + 303 - 20);
		
		selectedResearchBase.location(productionBase.location());
		activeResearchBase.location(selectedResearchBase.x, selectedResearchBase.y + 50);
		descriptionBase.location(activeResearchBase.x, activeResearchBase.y + 55);
		
		stopActive.location(activeResearchBase.x + 288, activeResearchBase.y + 27);
		viewActive.location(activeResearchBase.x + 288 + 119, activeResearchBase.y + 27);
		stopActiveEmpty.location(stopActive.location());
		viewActiveEmpty.location(viewActive.location());
		
		distributeVertically(mainCategory.x + 18, mainCategory.bounds(), 
				Arrays.asList(spaceshipsLabel, equipmentsLabel, weaponsLabel, buildingsLabel));
		
		for (ResearchMainCategory cat : ResearchMainCategory.values()) {
			List<UIComponent> comps = new ArrayList<UIComponent>();
			for (ResearchSubCategory scat : ResearchSubCategory.values()) {
				if (scat.main == cat) {
					comps.add(subComponents.get(scat));
				}
			}
			distributeVertically(subCategorySmall.x + 18, subCategorySmall.bounds(), comps);
		}

	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(commons.research().basePanel, base.x, base.y, null);
		super.draw(g2);
		
		drawResearchArrow(g2);
	}
	/** 
	 * Paint the research arrow for the actualSubCategory. 
	 * @param g2 the graphics context
	 */
	void drawResearchArrow(Graphics2D g2) {
		if (activeCategory == null) {
			return;
		}
		UIImageTabButton c = mainComponents.get(activeCategory.main);
		g2.drawImage(commons.research().current, 
				mainCategory.x + 5, c.y + (c.height - commons.research().current.getHeight()) / 2, null);
		if (c.down) {
			c = subComponents.get(activeCategory);
			if (c != null) {
				g2.drawImage(commons.research().current, 
						subCategorySmall.x + 5, c.y + (c.height - commons.research().current.getHeight()) / 2, null);
			}
		}
	}
	/**
	 * Change and set the visibility of components based on the mode.
	 * @param m the new mode
	 */
	void setMode(RPMode m) {
		this.mode = m;
		switch (mode) {
		case PRODUCTION:
			productionButton.visible(false);
			researchButton.visible(true);
			
			subCategorySmall.visible(false);
			prerequisites.visible(false);
			subCategoryWide.visible(true);
			
			selectedResearchBase.visible(false);
			activeResearchBase.visible(false);
			descriptionBase.visible(false);
			
			productionBase.visible(true);

			startNew.visible(false);
			stopActive.visible(false);
			stopActiveEmpty.visible(false);
			viewActive.visible(false);
			viewActiveEmpty.visible(false);
			
			break;
		case RESEARCH:
			productionButton.visible(true);
			researchButton.visible(false);
			
			subCategorySmall.visible(true);
			prerequisites.visible(true);
			subCategoryWide.visible(false);
			
			selectedResearchBase.visible(true);
			activeResearchBase.visible(true);
			descriptionBase.visible(true);
			
			productionBase.visible(false);
			
			startNew.visible(true);
			stopActive.visible(true);
			stopActiveEmpty.visible(true);
			viewActive.visible(true);
			viewActiveEmpty.visible(true);
			
			break;
		default:
		}
	}
	/**
	 * Select the given research category and display its subcategory labels.
	 * @param cat the main category
	 */
	void selectMainCategory(ResearchMainCategory cat) {
		for (Map.Entry<ResearchMainCategory, UIImageTabButton> e : mainComponents.entrySet()) {
			e.getValue().down = cat == e.getKey();
		}
		for (Map.Entry<ResearchSubCategory, UIImageTabButton> e : subComponents.entrySet()) {
			e.getValue().visible(e.getKey().main == cat);
		}
	}
	/**
	 * Perform actions when the specified sub-category is selected,
	 * e.g., change the technology slot contents, etc.
	 * @param cat the sub category
	 */
	void selectSubCategory(ResearchSubCategory cat) {
		for (Map.Entry<ResearchSubCategory, UIImageTabButton> e : subComponents.entrySet()) {
			e.getValue().down = (e.getKey() == cat);
		}
	}
	/**
	 * Create a sub category image button with the given graphics.
	 * @param cat the target category
	 * @param buttonImage the button images
	 */
	void createSubCategory(final ResearchSubCategory cat, BufferedImage[] buttonImage) {
		UIImageTabButton b = new UIImageTabButton(buttonImage);
		b.onPress = new Act() {
			@Override
			public void act() {
				selectSubCategory(cat);
			}
		};
		b.visible(false);
		add(b);
		subComponents.put(cat, b);
	}
}
