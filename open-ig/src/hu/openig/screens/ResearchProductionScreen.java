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
import hu.openig.render.TextRenderer;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageTabButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;
import hu.openig.ui.UIVideoImage;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;

import javax.swing.Timer;



/**
 * The combined research and production screen.
 * @author akarnokd, 2010.01.11.
 */
public class ResearchProductionScreen extends ScreenBase {
	/** 
	 * The annotation to indicate which UI elements should by default
	 * be visible on the screen. Mark only uncommon elements.
	 * @author akarnokd, Mar 19, 2011
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@interface ModeUI {
		/** The expected screen mode. */
		RPMode mode();
	}
	/** The equipment slot locations. */
	final List<TechnologySlot> slots = new ArrayList<TechnologySlot>();
	/** The rolling disk animation timer. */
	Timer animation;
	/** The current animation step counter. */
	int animationStep;
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle();
	/** The add production button. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImageButton addButton;
	/** The remove production button. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImageButton removeButton;
	/** The empty button. */
	UIImage emptyButton;
	/** The production button. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImageButton productionButton;
	/** The research button. */
	@ModeUI(mode = RPMode.PRODUCTION)
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
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage subCategorySmall;
	/** The subcategory panel for the production screen. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImage subCategoryWide;
	/** The prerequisites panel for the research screen. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage prerequisites;
	/** The base panel for the production listings. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImage productionBase;
	/** The research settings for the selected technology base panel. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage selectedResearchBase;
	/** The currenly running research base panel. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage activeResearchBase;
	/** The description of the selected technology base panel. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage descriptionBase;
	/** Start a new research button. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImageButton startNew;
	/** Stop the current research. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImageButton stopActive;
	/** Stop active empty button. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage stopActiveEmpty;
	/** View the active research. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImageButton viewActive;
	/** View active empty button. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage viewActiveEmpty;
	/** The spaceships main category. */
	UIImageTabButton spaceshipsLabel;
	/** The equipments main category. */
	UIImageTabButton equipmentsLabel;
	/** The weapons main category. */
	UIImageTabButton weaponsLabel;
	/** The buildings main category. */
	UIImageTabButton buildingsLabel;
	/** The 3 requirements label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage requiresLabel;
	/** The requirement #1 .*/
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel requires1;
	/** The requirement #2 .*/
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel requires2;
	/** The requirement #3 .*/
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel requires3;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage selectedTechName;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage selectedTechStatus;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage selectedComplete;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage selectedTime;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage selectedCivilLab;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage selectedMechLab;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage selectedCompLab;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage selectedAILab;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage selectedMilLab;
	
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage activeTechName;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage activeMoney;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage activeCivilLab;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage activeMechLab;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage activeCompLab;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage activeAILab;
	/** Static label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImage activeMilLab;

	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel selectedTechNameValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel selectedTechStatusValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel selectedCompleteValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel selectedTimeValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel selectedCivilLabValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel selectedMechLabValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel selectedCompLabValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel selectedAILabValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel selectedMilLabValue;
	
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel activeTechNameValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel activeMoneyValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel activeMoneyPercentValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel activeCivilLabValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel activeMechLabValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel activeCompLabValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel activeAILabValue;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel activeMilLabValue;

	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel descriptionTitle;
	/** Dynamic value label. */
	@ModeUI(mode = RPMode.RESEARCH)
	UILabel descriptionBody;
	/** Increase / decrease money. */
	@ModeUI(mode = RPMode.RESEARCH)
	UIImageButton moneyButton;
	/** The last mouse event on the funding button. */
	UIMouse moneyMouseLast;
	/** The product name. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImage productName;
	/** The assigned capacity. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImage capacity;
	/** The assigned capacity percent. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImage capacityPercent;
	/** The completion. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImage productComplete;
	/** Remove ten units. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImageButton removeTen;
	/** Remove one unit. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImageButton removeOne;
	/** Add one unit. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImageButton addOne;
	/** Add ten units. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImageButton addTen;
	/** Sell. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImageButton sell;
	/** The production lines. */
	@ModeUI(mode = RPMode.PRODUCTION)
	final List<ProductionLine> productionLines = new ArrayList<ProductionLine>();
	/** The total capacity label. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UIImage capacityLabel;
	/** The available capacity value. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UILabel availableCapacityValue;
	/** The total capacity value. */
	@ModeUI(mode = RPMode.PRODUCTION)
	UILabel totalCapacityValue;
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
			final int j = i;
			final TechnologySlot ts = new TechnologySlot(commons);
			ts.name = "TODO";
			ts.inventory = 1;
			ts.cost = 1000;
			ts.researching = true;
			ts.percent = 0.5f;
			ts.visible(true);
			ts.missingLab = true;
			ts.image = rl.getImage(commons.language(), "inventions/spaceships/fighters/fighter_" + (i + 1) + "");
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
		
		requiresLabel = new UIImage(commons.research().requirements);
		
		int textColor = TextRenderer.GREEN;
		selectedTechName = new UIImage(commons.research().projectName);
		selectedTechStatus = new UIImage(commons.research().projectStatus);
		selectedComplete = new UIImage(commons.research().projectCompleted);
		selectedTime = new UIImage(commons.research().remaining);
		
		selectedCivilLab = new UIImage(commons.research().civilLab);
		selectedMechLab = new UIImage(commons.research().mechLab);
		selectedCompLab = new UIImage(commons.research().compLab);
		selectedAILab = new UIImage(commons.research().aiLab);
		selectedMilLab = new UIImage(commons.research().milLab);

		activeTechName = new UIImage(commons.research().project);
		activeMoney = new UIImage(commons.research().money);

		activeCivilLab = new UIImage(commons.research().civilLab);
		activeMechLab = new UIImage(commons.research().mechLab);
		activeCompLab = new UIImage(commons.research().compLab);
		activeAILab = new UIImage(commons.research().aiLab);
		activeMilLab = new UIImage(commons.research().milLab);


		selectedTechNameValue = new UILabel("TODO", 10, commons.text());
		selectedTechNameValue.color(textColor);
		selectedTechStatusValue = new UILabel("TODO", 10, commons.text());
		selectedTechStatusValue.color(textColor);
		selectedCompleteValue = new UILabel("Kesz", 14, commons.text());
		selectedCompleteValue.color(textColor);
		selectedTimeValue = new UILabel("----", 14, commons.text());
		selectedTimeValue.color(textColor);
		selectedCivilLabValue = new UILabel("0", 14, commons.text());
		selectedCivilLabValue.color(textColor);
		selectedMechLabValue = new UILabel("0", 14, commons.text());
		selectedMechLabValue.color(textColor);
		selectedCompLabValue = new UILabel("0", 14, commons.text());
		selectedCompLabValue.color(textColor);
		selectedAILabValue = new UILabel("0", 14, commons.text());
		selectedAILabValue.color(textColor);
		selectedMilLabValue = new UILabel("0", 14, commons.text());
		selectedMilLabValue.color(textColor);
		
		activeCivilLabValue = new UILabel("0", 14, commons.text());
		activeCivilLabValue.color(textColor);
		activeMechLabValue = new UILabel("0", 14, commons.text());
		activeMechLabValue.color(textColor);
		activeCompLabValue = new UILabel("0", 14, commons.text());
		activeCompLabValue.color(textColor);
		activeAILabValue = new UILabel("0", 14, commons.text());
		activeAILabValue.color(textColor);
		activeMilLabValue = new UILabel("0", 14, commons.text());
		activeMilLabValue.color(textColor);
		
		activeMoneyValue = new UILabel("TODO", 10, commons.text());
		activeMilLabValue.color(textColor);
		activeMoneyPercentValue = new UILabel("100", 10, commons.text());
		activeMoneyPercentValue.color(textColor);
		
		descriptionTitle = new UILabel("TODO", 10, commons.text());
		descriptionTitle.color(0xFFFF0000);
		descriptionTitle.horizontally(HorizontalAlignment.CENTER);
		descriptionBody = new UILabel("TODO todo todo todo todo todo todo todo todo todo todo todo todo todo todo todo todo todo", 7, commons.text());
		descriptionBody.color(textColor);
		descriptionBody.wrap(true);
		descriptionBody.spacing(5);
		
		activeTechNameValue = new UILabel("TODO", 10, commons.text());
		
		moneyButton = new UIImageButton(commons.research().fund) {
			@Override
			public boolean mouse(UIMouse e) {
				moneyMouseLast = e;
				super.mouse(e);
				return true;
			};
		};
		moneyButton.onClick = new Act() {
			@Override
			public void act() {
				doAdjustMoney(2.0f * (moneyMouseLast.x - moneyButton.x) / moneyButton.width - 1);
			}
		};
		moneyButton.setHoldDelay(100);
		
		requires1 = new UILabel("TODO", 7, commons.text());
		requires1.color(textColor);
		requires2 = new UILabel("TODO", 7, commons.text());
		requires2.color(textColor);
		requires3 = new UILabel("TODO", 7, commons.text());
		requires3.color(textColor);
		
		productName = new UIImage(commons.research().inventionName);
		capacity = new UIImage(commons.research().cap);
		capacityPercent = new UIImage(commons.research().capPercent);
		productComplete = new UIImage(commons.research().completed);
		
		removeTen = new UIImageButton(commons.research().minusTen);
		removeOne = new UIImageButton(commons.research().minusOne);
		addOne = new UIImageButton(commons.research().plusOne);
		addTen = new UIImageButton(commons.research().plusTen);
		sell = new UIImageButton(commons.research().sell);
		
		for (int i = 0; i < 5; i++) {
			final int j = i;
			final ProductionLine pl = new ProductionLine();
			pl.onPress = new Act() {
				@Override
				public void act() {
					doSelectProductionLine(pl, j);
				}
			};
			productionLines.add(pl);
		}
		
		capacityLabel = new UIImage(commons.research().capacity);
		availableCapacityValue = new UILabel("1000", 14, commons.text());
		availableCapacityValue.color(textColor);
		totalCapacityValue = new UILabel("2000", 10, commons.text());
		totalCapacityValue.horizontally(HorizontalAlignment.RIGHT);
		totalCapacityValue.color(textColor);
		
		addThis();
		add(slots);
		add(productionLines);
	}
	/**
	 * Select a specific production line.
	 * @param pl the line
	 * @param j the line index
	 */
	protected void doSelectProductionLine(ProductionLine pl, int j) {
		for (ProductionLine pl0 : productionLines) {
			pl0.select(pl0 == pl);
		}
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
		requiresLabel.location(prerequisites.x + (prerequisites.width - requiresLabel.width) / 2, prerequisites.y + (34 - requiresLabel.height) / 2);
		
		requires1.location(prerequisites.x + 6, prerequisites.y + 37);
		requires2.location(requires1.x, requires1.y + 18);
		requires3.location(requires2.x, requires2.y + 18);
		
		selectedTechName.location(selectedResearchBase.x + 123 - selectedTechName.width, selectedResearchBase.y + 5);
		selectedTechNameValue.location(selectedResearchBase.x + 127, selectedResearchBase.y + 9);
		selectedTechNameValue.size(152, 10); 
		selectedTechStatus.location(selectedResearchBase.x + 123 - selectedTechStatus.width, selectedResearchBase.y + 24);
		selectedTechStatusValue.location(selectedResearchBase.x + 127, selectedResearchBase.y + 28);
		selectedTechStatusValue.size(152, 10); 
		
		moneyButton.location(activeResearchBase.x + 6, activeResearchBase.y + 28);
		
		selectedComplete.location(selectedResearchBase.x + 335 - selectedComplete.width, selectedResearchBase.y + 7);
		selectedCompleteValue.location(selectedResearchBase.x + 340, selectedResearchBase.y + 7);
		selectedCompleteValue.size(48, 14);
		selectedTime.location(selectedResearchBase.x + 464 - selectedTime.width, selectedResearchBase.y + 7);
		selectedTimeValue.location(selectedResearchBase.x + 470, selectedResearchBase.y + 7);
		selectedTimeValue.size(48, 14);
		
		selectedCivilLab.location(selectedResearchBase.x + 314 - selectedCivilLab.width, selectedResearchBase.y + 28);
		activeCivilLab.location(activeResearchBase.x + 314 - activeCivilLab.width, activeResearchBase.y + 9);
		
		selectedMechLab.location(selectedResearchBase.x + 368 - selectedMechLab.width, selectedResearchBase.y + 28);
		activeMechLab.location(activeResearchBase.x + 368 - activeMechLab.width, activeResearchBase.y + 9);
		
		selectedCompLab.location(selectedResearchBase.x + 425 - selectedCompLab.width, selectedResearchBase.y + 28);
		activeCompLab.location(activeResearchBase.x + 425 - activeCompLab.width, activeResearchBase.y + 9);
		
		selectedAILab.location(selectedResearchBase.x + 466 - selectedAILab.width, selectedResearchBase.y + 28);
		activeAILab.location(activeResearchBase.x + 466 - activeAILab.width, activeResearchBase.y + 9);
		
		selectedMilLab.location(selectedResearchBase.x + 507 - selectedMilLab.width, selectedResearchBase.y + 28);
		activeMilLab.location(activeResearchBase.x + 507 - activeMilLab.width, activeResearchBase.y + 9);
		
		activeTechName.location(activeResearchBase.x + 122 - activeTechName.width, activeResearchBase.y + 7);
		activeMoney.location(activeResearchBase.x + 122 - activeMoney.width, activeResearchBase.y + 28);
		
		descriptionTitle.location(descriptionBase.x + 7, descriptionBase.y + 8);
		descriptionTitle.size(513, 10);
		
		descriptionBody.location(descriptionBase.x + 7, descriptionBase.y + 24);
		descriptionBody.size(513, 20);
	
		activeTechNameValue.location(activeResearchBase.x + 127, activeResearchBase.y + 11);
		activeTechNameValue.size(152, 10);
		
		activeMoneyValue.location(activeResearchBase.x + 127, activeResearchBase.y + 32);
		activeMoneyValue.size(120, 10);
		
		activeMoneyPercentValue.location(activeResearchBase.x + 127 + 120 + 11, activeResearchBase.y + 32);
		activeMoneyPercentValue.size(23, 10);
		
		selectedCivilLabValue.location(selectedResearchBase.x + 316, selectedResearchBase.y + 28);
		selectedCivilLabValue.size(11, 14);

		activeCivilLabValue.location(activeResearchBase.x + 316, activeResearchBase.y + 9);
		activeCivilLabValue.size(11, 14);
		
		selectedMechLabValue.location(selectedCivilLabValue.x + 54, selectedCivilLabValue.y);
		selectedMechLabValue.size(11, 14);
		
		activeMechLabValue.location(activeCivilLabValue.x + 54, activeCivilLabValue.y);
		activeMechLabValue.size(11, 14);

		selectedCompLabValue.location(selectedMechLabValue.x + 57, selectedMechLabValue.y);
		selectedCompLabValue.size(11, 14);
		
		activeCompLabValue.location(activeMechLabValue.x + 57, activeMechLabValue.y);
		activeCompLabValue.size(11, 14);

		selectedAILabValue.location(selectedCompLabValue.x + 41, selectedCompLabValue.y);
		selectedAILabValue.size(11, 14);
		
		activeAILabValue.location(activeCompLabValue.x + 41, activeCompLabValue.y);
		activeAILabValue.size(11, 14);

		selectedMilLabValue.location(selectedAILabValue.x + 41, selectedAILabValue.y);
		selectedMilLabValue.size(11, 14);
		
		activeMilLabValue.location(activeAILabValue.x + 41, activeAILabValue.y);
		activeMilLabValue.size(11, 14);
		
		productName.location(productionBase.x + 5 + (168 - productName.width) / 2, productionBase.y + 3);
		capacity.location(productionBase.x + 236 + (56 - capacity.width) / 2, productionBase.y + 4);
		capacityPercent.location(productionBase.x + 236 + 57 + (56 - capacityPercent.width) / 2, productionBase.y + 4);
		productComplete.location(productionBase.x + 404, productionBase.y + 4);
		
		int py = productionBase.y + 19;
		for (ProductionLine pl : productionLines) {
			pl.location(productionBase.x + 2, py);
			py += 23;
		}
		removeTen.location(productionBase.x + 2, productionBase.y + 134);
		removeOne.location(productionBase.x + 2 + 54, productionBase.y + 134);
		addOne.location(productionBase.x + 2 + 54 * 2, productionBase.y + 134);
		addTen.location(productionBase.x + 2 + 54 * 3, productionBase.y + 134);
		sell.location(productionBase.x + 2 + 54 * 4, productionBase.y + 134);
		
		capacityLabel.location(productionBase.x + 394 - capacityLabel.width, productionBase.y + 133);
		availableCapacityValue.location(productionBase.x + 398, productionBase.y + 137);
		availableCapacityValue.size(60, 14);
		totalCapacityValue.location(productionBase.x + 398 + 60, productionBase.y + 139);
		totalCapacityValue.size(59, 10);
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
		setUIVisibility();
		// adjust the visibility further
		switch (mode) {
		case PRODUCTION:
			break;
		case RESEARCH:
			break;
		default:
		}
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
							c.visible(mi.mode() == mode);
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
									c.visible(mi.mode() == mode);
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
	/**
	 * Adjust money based on the scale.
	 * @param scale the scale factor -1.0 ... +1.0
	 */
	void doAdjustMoney(float scale) {
		// TODO
	}
	/**
	 * A concrete production line.
	 * @author akarnokd, Mar 19, 2011
	 */
	class ProductionLine extends UIContainer {
		/** The base image. */
		UIImage base;
		/** Less priority button. */
		UIImageButton lessPriority;
		/** More priority button. */
		UIImageButton morePriority;
		/** Less build button. */
		UIImageButton lessBuild;
		/** More build button. */
		UIImageButton moreBuild;
		/** Invention name. */
		UILabel name;
		/** Priority value. */
		UILabel priority;
		/** Assigned capacity. */
		UILabel capacity;
		/** Capacity percent. */
		UILabel capacityPercent;
		/** Production count. */
		UILabel count;
		/** Completion info. */
		UILabel completion;
		/** The activation event. */
		public Act onPress;
		/** Initialize the inner fields. */
		public ProductionLine() {
			base = new UIImage(commons.research().productionLine);
			base.z = -1;
			width = base.width;
			height = base.height;
			
			lessPriority = new UIImageButton(commons.research().less);
			lessBuild = new UIImageButton(commons.research().less);
			morePriority = new UIImageButton(commons.research().more);
			moreBuild = new UIImageButton(commons.research().more);
			
			name = new UILabel("TODO", 7, commons.text());
			priority = new UILabel("50", 7, commons.text());
			priority.horizontally(HorizontalAlignment.CENTER);
			capacity = new UILabel("1000", 7, commons.text());
			capacityPercent = new UILabel("50%", 7, commons.text());
			capacityPercent.horizontally(HorizontalAlignment.CENTER);
			count = new UILabel("1", 7, commons.text());
			count.horizontally(HorizontalAlignment.CENTER);
			completion = new UILabel("TODO", 7, commons.text());
			
			name.bounds(5, 4, 166, 14);
			lessPriority.location(190, 5);
			priority.bounds(198, 4, 24, 14);
			morePriority.location(223, 5);
			capacity.bounds(234, 2, 56, 18);
			capacityPercent.bounds(234 + 57, 2, 56, 18);
			lessBuild.location(350, 5);
			count.bounds(358, 4, 24, 14);
			moreBuild.location(383, 5);
			completion.bounds(394, 2, 125, 18);
			
			addThis();
		}
		@Override
		public boolean mouse(UIMouse e) {
			boolean rep = false;
			if (e.has(Type.DOWN)) {
				select(true);
				if (onPress != null) {
					onPress.act();
				}
				rep = true;
			}
			rep |= super.mouse(e);
			return rep;
		}
		/** 
		 * Change the coloring to select this line.
		 * @param state the selection state
		 */
		public void select(boolean state) {
			name.color(state ? TextRenderer.RED : TextRenderer.GREEN);
			priority.color(state ? TextRenderer.RED : TextRenderer.GREEN);
			capacity.color(state ? TextRenderer.RED : TextRenderer.GREEN);
			capacityPercent.color(state ? TextRenderer.RED : TextRenderer.GREEN);
			count.color(state ? TextRenderer.RED : TextRenderer.GREEN);
			completion.color(state ? TextRenderer.RED : TextRenderer.GREEN);
		}
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
}
