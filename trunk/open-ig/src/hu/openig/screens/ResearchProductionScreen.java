/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.core.Action1;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Research;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchState;
import hu.openig.model.ResearchSubCategory;
import hu.openig.model.ResearchType;
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
		Screens mode();
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
	@ModeUI(mode = Screens.PRODUCTION)
	UIImageButton addButton;
	/** The remove production button. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImageButton removeButton;
	/** The empty button. */
	UIImage emptyButton;
	/** The production button. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImageButton productionButton;
	/** The research button. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImageButton researchButton;
	/** The equipment button. */
	UIImageButton equipmentButton;
	/** The bridge button. */
	UIImageButton bridgeButton;
	/** The video output. */
	UIImage video;
	/** The video renderer. */
	TechnologyVideoRenderer videoRenderer;
	/** The main category panel. */
	UIImage mainCategory;
	/** The subcategory panel for the research screen. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage subCategorySmall;
	/** The subcategory panel for the production screen. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImage subCategoryWide;
	/** The prerequisites panel for the research screen. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage prerequisites;
	/** The base panel for the production listings. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImage productionBase;
	/** The research settings for the selected technology base panel. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage selectedResearchBase;
	/** The currenly running research base panel. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage activeResearchBase;
	/** The description of the selected technology base panel. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage descriptionBase;
	/** Start a new research button. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImageButton startNew;
	/** Stop the current research. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImageButton stopActive;
	/** Stop active empty button. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage stopActiveEmpty;
	/** View the active research. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImageButton viewActive;
	/** View active empty button. */
	@ModeUI(mode = Screens.RESEARCH)
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
	@ModeUI(mode = Screens.RESEARCH)
	UIImage requiresLabel;
	/** The requirement #1 .*/
	@ModeUI(mode = Screens.RESEARCH)
	UILabel requires1;
	/** The requirement #2 .*/
	@ModeUI(mode = Screens.RESEARCH)
	UILabel requires2;
	/** The requirement #3 .*/
	@ModeUI(mode = Screens.RESEARCH)
	UILabel requires3;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage selectedTechName;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage selectedTechStatus;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage selectedComplete;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage selectedTime;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage selectedCivilLab;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage selectedMechLab;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage selectedCompLab;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage selectedAILab;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage selectedMilLab;
	
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage activeTechName;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage activeMoney;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage activeCivilLab;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage activeMechLab;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage activeCompLab;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage activeAILab;
	/** Static label. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImage activeMilLab;

	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel selectedTechNameValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel selectedTechStatusValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel selectedCompleteValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel selectedTimeValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel selectedCivilLabValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel selectedMechLabValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel selectedCompLabValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel selectedAILabValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel selectedMilLabValue;
	
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel activeTechNameValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel activeMoneyValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel activeMoneyPercentValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel activeCivilLabValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel activeMechLabValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel activeCompLabValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel activeAILabValue;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel activeMilLabValue;

	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel descriptionTitle;
	/** Dynamic value label. */
	@ModeUI(mode = Screens.RESEARCH)
	UILabel descriptionBody;
	/** Increase / decrease money. */
	@ModeUI(mode = Screens.RESEARCH)
	UIImageButton moneyButton;
	/** The last mouse event on the funding button. */
	UIMouse moneyMouseLast;
	/** The product name. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImage productName;
	/** The assigned capacity. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImage capacity;
	/** The assigned capacity percent. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImage capacityPercent;
	/** The completion. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImage productComplete;
	/** Remove ten units. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImageButton removeTen;
	/** Remove one unit. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImageButton removeOne;
	/** Add one unit. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImageButton addOne;
	/** Add ten units. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImageButton addTen;
	/** Sell. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImageButton sell;
	/** The production lines. */
	@ModeUI(mode = Screens.PRODUCTION)
	final List<ProductionLine> productionLines = new ArrayList<ProductionLine>();
	/** The total capacity label. */
	@ModeUI(mode = Screens.PRODUCTION)
	UIImage capacityLabel;
	/** The available capacity value. */
	@ModeUI(mode = Screens.PRODUCTION)
	UILabel availableCapacityValue;
	/** The total capacity value. */
	@ModeUI(mode = Screens.PRODUCTION)
	UILabel totalCapacityValue;
	/** The labels associated with various main categories. */
	final Map<ResearchMainCategory, UIImageTabButton> mainComponents = new HashMap<ResearchMainCategory, UIImageTabButton>();
	/** The labels associated with various sub categories. */
	final Map<ResearchSubCategory, UIImageTabButton> subComponents = new HashMap<ResearchSubCategory, UIImageTabButton>();
	/** The screen mode mode. */
	Screens mode;
	/** The animated research type. */
	ResearchType animationResearch;
	/** The research status when the animation began. */
	boolean animationResearchReady;
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
		emptyButton.z = -1;
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
		startNew.onClick = new Act() {
			@Override
			public void act() {
				doStartNew();
			}
		};
		stopActive = new UIImageButton(commons.research().stop);
		stopActive.onClick = new Act() {
			@Override
			public void act() {
				displayResearch(player().runningResearch);
				player().runningResearch = null;
			}
		};
		viewActive = new UIImageButton(commons.research().view);
		viewActive.onClick = new Act() {
			@Override
			public void act() {
				displayResearch(player().runningResearch);
			}
		};
		
		stopActiveEmpty = new UIImage(commons.research().emptySmall);
		stopActiveEmpty.z = -1;
		viewActiveEmpty = new UIImage(commons.research().emptySmall);
		viewActiveEmpty.z = -1;
		
		video = new UIImage();
		
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

		researchButton.onClick = new Act() {
			@Override
			public void act() {
				setMode(Screens.RESEARCH);
			}
		};
		productionButton.onClick = new Act() {
			@Override
			public void act() {
				setMode(Screens.PRODUCTION);
			}
		};
		equipmentButton.onClick = new Act() {
			@Override
			public void act() {
				displaySecondary(Screens.EQUIPMENT_FLEET);
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
				displayPrimary(Screens.BRIDGE);
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
		selectedCompleteValue.horizontally(HorizontalAlignment.CENTER);
		selectedTimeValue = new UILabel("----", 14, commons.text());
		selectedTimeValue.color(textColor);
		selectedTimeValue.horizontally(HorizontalAlignment.CENTER);
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
		activeMoneyValue.color(textColor);
		activeMoneyValue.horizontally(HorizontalAlignment.CENTER);
		activeMoneyPercentValue = new UILabel("100", 10, commons.text());
		activeMoneyPercentValue.color(textColor);
		activeMoneyPercentValue.horizontally(HorizontalAlignment.CENTER);
		
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
				doAdjustMoney(2.0f * (moneyMouseLast.x) / moneyButton.width - 1);
			}
		};
		moneyButton.setHoldDelay(100);
		moneyButton.setDisabledPattern(commons.common().disabledPattern);
		
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

		productionLines.clear();
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
		for (int i = 0; i < 6; i++) {
			TechnologySlot slot = new TechnologySlot(commons);
			slot.visible(false);
			slots.add(slot);
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
	 * Navigate the view to the given research and select it.
	 * @param rt the research to display
	 */
	void displayResearch(ResearchType rt) {
		world().selectResearch(rt);
		selectMainCategory(rt.category.main);
		selectSubCategory(rt.category);
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

		video.bounds(base.x + 2, base.y + 2, 316, 196);
		
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
		askRepaint(base);
	}

	@Override
	public void onEnter(Screens mode) {
		onResize();
		if (mode == null || mode == Screens.PRODUCTION) {
			setMode(Screens.PRODUCTION);
		} else {
			setMode(Screens.RESEARCH);
		}
		animation.start();
		video.image(null);
		update();
		if (player().currentResearch != null) {
			displayCategory(player().currentResearch.category);
		}
	}

	@Override
	public void onLeave() {
		if (animation != null) {
			animation.stop();
		}
		if (videoRenderer != null) {
			videoRenderer.stop();
			videoRenderer = null;
		}
	}

	@Override
	public void onFinish() {
		animation = null;
	}

	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);
		g2.drawImage(commons.research().basePanel, base.x, base.y, null);
		
		update();
		updateActive();
		
		super.draw(g2);
		
		drawResearchArrow(g2);
	}
	/** 
	 * Paint the research arrow for the actualSubCategory. 
	 * @param g2 the graphics context
	 */
	void drawResearchArrow(Graphics2D g2) {
		if (player().runningResearch == null) {
			return;
		}
		UIImageTabButton c = mainComponents.get(player().runningResearch.category.main);
		g2.drawImage(commons.research().current, 
				mainCategory.x + 5, c.y + (c.height - commons.research().current.getHeight()) / 2, null);
		if (c.down) {
			c = subComponents.get(player().runningResearch.category);
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
	void setMode(Screens m) {
		this.mode = m;
		setUIVisibility();
		// adjust the visibility further
		switch (mode) {
		case PRODUCTION:
			update();
			if (player().currentResearch != null) {
				update();
				displayResearch(player().currentResearch);
			}
			break;
		case RESEARCH:
			update();
			if (player().currentResearch != null) {
				update();
				displayResearch(player().currentResearch);
			}
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
		displayCategory(cat);
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
		Research r = player().research.get(player().currentResearch);
		r.assignedMoney += scale * r.type.researchCost / 20;
		r.assignedMoney = Math.max(Math.min(r.assignedMoney, r.remainingMoney), r.remainingMoney / 8);
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
	@Override
	public boolean mouse(UIMouse e) {
		if (!base.contains(e.x, e.y) && e.has(Type.DOWN)) {
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
	/** Display values based on the current technology. */
	public void update() {
		ResearchType rt = player().currentResearch;
		if (rt == null) {
			List<ResearchType> rts = world().getResearch();
			if (rts.size() > 0) {
				rt = rts.get(0);
				world().selectResearch(rt);
				selectMainCategory(rt.category.main);
				selectSubCategory(rt.category);
				playAnim(rt);
			}
		}
		final ResearchType rt1 = rt;
		if (rt != null) {
			if (rt.prerequisites.size() > 0) {
				requires1.text(rt.prerequisites.get(0).name, true).visible(true);
				requires1.color(world().getResearchColor(rt.prerequisites.get(0)));
				requires1.onPress = new Act() {
					@Override
					public void act() {
						displayResearch(rt1.prerequisites.get(0));
					}
				};
			} else {
				requires1.visible(false);
			}
			if (rt.prerequisites.size() > 1) {
				requires2.text(rt.prerequisites.get(1).name, true).visible(true);
				requires2.color(world().getResearchColor(rt.prerequisites.get(1)));
				requires2.onPress = new Act() {
					@Override
					public void act() {
						displayResearch(rt1.prerequisites.get(1));
					}
				};
			} else {
				requires2.visible(false);
			}
			if (rt.prerequisites.size() > 2) {
				requires3.text(rt.prerequisites.get(2).name, true).visible(true);
				requires3.color(world().getResearchColor(rt.prerequisites.get(2)));
				requires3.onPress = new Act() {
					@Override
					public void act() {
						displayResearch(rt1.prerequisites.get(2));
					}
				};
			} else {
				requires3.visible(false);

			}
			
			descriptionTitle.text(rt.longName);
			if (player().isAvailable(rt) || world().canResearch(rt)) {
				descriptionBody.text(rt.description);
			} else {
				descriptionBody.text("");
			}
			
			selectedCivilLabValue.text("" + rt.civilLab);
			selectedMechLabValue.text("" + rt.mechLab);
			selectedCompLabValue.text("" + rt.compLab);
			selectedAILabValue.text("" + rt.aiLab);
			selectedMilLabValue.text("" + rt.milLab);
			
			PlanetStatistics ps = player().getPlanetStatistics();

			selectedCivilLabValue.color(labColor(ps.civilLab, ps.civilLabActive, rt.civilLab));
			selectedMechLabValue.color(labColor(ps.mechLab, ps.mechLabActive, rt.mechLab));
			selectedCompLabValue.color(labColor(ps.compLab, ps.compLabActive, rt.compLab));
			selectedAILabValue.color(labColor(ps.aiLab, ps.aiLabActive, rt.aiLab));
			selectedMilLabValue.color(labColor(ps.milLab, ps.milLabActive, rt.milLab));

			selectedTechNameValue.text(rt.name);
			if (player().isAvailable(rt)) {
				selectedTechStatusValue.text(get("researchinfo.progress.done"));
				selectedCompleteValue.text(get("researchinfo.progress.done"));
			} else {
				if (world().canResearch(rt)) {
					if (player().research.containsKey(rt)) {
						Research rs = player().research.get(rt);
						switch (rs.state) {
						case RUNNING:
							selectedTechStatusValue.text(format("researchinfo.progress.running", (int)rs.getPercent()), true).visible(true);
							break;
						case STOPPED:
							selectedTechStatusValue.text(format("researchinfo.progress.paused", (int)rs.getPercent()), true).visible(true);
							break;
						case LAB:
							selectedTechStatusValue.text(format("researchinfo.progress.lab", (int)rs.getPercent()), true).visible(true);
							break;
						case MONEY:
							selectedTechStatusValue.text(format("researchinfo.progress.money", (int)rs.getPercent()), true).visible(true);
							break;
						default:
							selectedTechStatusValue.text("");
						}
						selectedCompleteValue.text((int)(rs.getPercent()) + "%");
						selectedTimeValue.text("" + rs.getTime());
						selectedTimeValue.color(
								player().hasEnoughActiveLabs(rs.type) ? TextRenderer.GREEN 
										: (player().hasEnoughLabs(rs.type) ? TextRenderer.YELLOW : TextRenderer.RED));
					} else {
						selectedTechStatusValue.text(get("researchinfo.progress.can"));
						selectedCompleteValue.text("----");
						selectedTimeValue.text("" + (rt.researchCost / 20));
						selectedTimeValue.color(
								player().hasEnoughActiveLabs(rt) ? TextRenderer.GREEN 
										: (player().hasEnoughLabs(rt) ? TextRenderer.YELLOW : TextRenderer.RED));
					}
				} else {
					selectedTechStatusValue.text(get("researchinfo.progress.cant"));
					selectedCompleteValue.text("----");
					selectedTimeValue.text("----");
					selectedTimeValue.color(TextRenderer.GREEN);
				}
			}
			
			startNew.visible(player().runningResearch != rt && world().canResearch(rt));
			
			if (getCurrentCategory() == rt.category) {
				updateSlot(rt);
			}
		} else {
			for (TechnologySlot slot : slots) {
				slot.visible(false);
			}
			startNew.visible(false);
			requires1.visible(false);
			requires2.visible(false);
			requires3.visible(false);
			descriptionTitle.text("");
			descriptionBody.text("");
			
			selectedCivilLabValue.text("");
			selectedMechLabValue.text("");
			selectedCompLabValue.text("");
			selectedAILabValue.text("");
			selectedMilLabValue.text("");

			selectedCompleteValue.text("----");
			selectedTechStatusValue.text("----");
			selectedTechNameValue.text("-");
			selectedTechStatusValue.text("-");
			selectedTimeValue.text("----");
		}
	}
	/** @return the currently selected category. */
	ResearchSubCategory getCurrentCategory() {
		for (Map.Entry<ResearchSubCategory, UIImageTabButton> btn : subComponents.entrySet()) {
			if (btn.getValue().down) {
				return btn.getKey();
			}
		}
		return null;
	}
	/**
	 * Returns the coloring for the lab amounts. 
	 * @param total the total lab amount
	 * @param active the active lab amount
	 * @param required the required lab amount
	 * @return the color
	 */
	int labColor(int total, int active, int required) {
		if (total < required) {
			return TextRenderer.RED;
		} else
		if (active < required) {
			return TextRenderer.YELLOW;
		}
		return TextRenderer.GREEN;
	}
	/**
	 * Display technologies in the slots.
	 * @param cat the category
	 */
	public void displayCategory(ResearchSubCategory cat) {
		for (TechnologySlot slot : slots) {
			slot.visible(false);
		}
		for (final ResearchType rt : world().researches.values()) {
			if (rt.category == cat) {
				if (world().canDisplayResearch(rt)) {
					if (rt.index >= 0 && rt.index < 6) {
						if (updateSlot(rt).selected) {
							playAnim(rt);
						}
					}
				}
			}
		}
	}
	/**
	 * Update the slot belonging to the specified technology.
	 * @param rt the research technology
	 * @return the slot
	 */
	public TechnologySlot updateSlot(final ResearchType rt) {
		final TechnologySlot slot = slots.get(rt.index);
		slot.visible(true);
		Integer inv = player().inventory.get(rt);
		slot.inventory = inv != null ? inv.intValue() : 0;
		slot.name = rt.name;
		slot.selected = player().currentResearch == rt;
		slot.available = player().isAvailable(rt);
		slot.percent = 0;
		if (!slot.available) {
			slot.researching = player().research.containsKey(rt);
			if (slot.researching) {
				slot.percent =  player().research.get(rt).getPercent() / 100.0f;
			}
			slot.missingActiveLab = !player().hasEnoughActiveLabs(rt);
			slot.missingLab = !player().hasEnoughLabs(rt);
			slot.notResearchable = !world().canResearch(rt);
			slot.cost = mode == Screens.PRODUCTION ? -1 : rt.researchCost / 2;
		} else {
			slot.cost = mode == Screens.PRODUCTION ? rt.productionCost : -1;
			slot.percent = 1;
			slot.researching = false;
			slot.notResearchable = false;
			slot.missingActiveLab = false;
			slot.missingLab = false;
		}
		
		slot.image = rt.image;
		
		slot.onPress = new Act() {
			@Override
			public void act() {
				displayResearch(rt);
				doSelectTechnology(slot, rt.index);
				playAnim(rt);
			}
		};
		if (animationResearch == rt && animationResearchReady != slot.available) {
			playAnim(rt);
		}
		return slot;
	}
	/**
	 * Play animation for the given research.
	 * @param rt the target research
	 */
	public void playAnim(ResearchType rt) {
		if (videoRenderer != null) {
			videoRenderer.stop();
			videoRenderer = null;
		}
		video.image(null);
		if (rt != null) {
			animationResearch = rt;
			if (player().isAvailable(rt)) {
				video.image(rt.infoImage);
				animationResearchReady = true;
			} else
			if (world().canResearch(rt)) {
				video.image(rt.infoImageWired);
				animationResearchReady = false;
			}
			video.center(true);
			String vid = null;
			if (rt.video != null) {
				if (player().isAvailable(rt)) {
					vid = rt.video;
					if (commons.video(vid) == null) {
						vid = null;
					}
				} else
				if (world().canResearch(rt)) {
					vid = rt.video + "_wired";
					if (commons.video(vid) == null) {
						vid = null;
					}
				} else {
					vid = "technology/unknown_invention";
				}
			}
			if (vid != null) {
				videoRenderer = new TechnologyVideoRenderer(commons.video(vid), 
				new Action1<BufferedImage>() {
					@Override
					public void invoke(BufferedImage value) {
						video.image(value);
						askRepaint(video);
					}
				}
				);
				videoRenderer.start(commons.pool);
			}
		}
	}
	/** Update values for the active research. */
	void updateActive() {
		ResearchType rt = player().runningResearch;
		PlanetStatistics ps = player().getPlanetStatistics();
		
		activeCivilLabValue.text("" + ps.civilLabActive);
		activeMechLabValue.text("" + ps.mechLabActive);
		activeCompLabValue.text("" + ps.compLabActive);
		activeAILabValue.text("" + ps.aiLabActive);
		activeMilLabValue.text("" + ps.milLabActive);
		
		if (rt != null) {

			activeCivilLabValue.color(labColor(ps.civilLab, ps.civilLabActive, rt.civilLab));
			activeMechLabValue.color(labColor(ps.mechLab, ps.mechLabActive, rt.mechLab));
			activeCompLabValue.color(labColor(ps.compLab, ps.compLabActive, rt.compLab));
			activeAILabValue.color(labColor(ps.aiLab, ps.aiLabActive, rt.aiLab));
			activeMilLabValue.color(labColor(ps.milLab, ps.milLabActive, rt.milLab));

			activeTechNameValue.text(rt.name, true).visible(true);

			Research rs = player().research.get(rt);
			
			activeMoneyValue.text(rs.assignedMoney + "/" + rs.remainingMoney).visible(true);
			activeMoneyPercentValue.text(((int)rs.getPercent()) + "%").visible(true);
			
			moneyButton.enabled(true);
			viewActive.visible(true);
			stopActive.visible(true);
		} else {
			viewActive.visible(false);
			stopActive.visible(false);
			moneyButton.enabled(false);
			activeTechNameValue.visible(false);
			activeMoneyValue.visible(false);
			activeMoneyPercentValue.visible(false);

			activeCivilLabValue.color(labColor(ps.civilLab, ps.civilLabActive, ps.civilLab));
			activeMechLabValue.color(labColor(ps.mechLab, ps.mechLabActive, ps.mechLab));
			activeCompLabValue.color(labColor(ps.compLab, ps.compLabActive, ps.compLab));
			activeAILabValue.color(labColor(ps.aiLab, ps.aiLabActive, ps.aiLab));
			activeMilLabValue.color(labColor(ps.milLab, ps.milLabActive, ps.milLab));
		}
	}
	/** Start a new research. */
	void doStartNew() {
		if (player().runningResearch != null) {
			player().research.get(player().runningResearch).state = ResearchState.STOPPED;
		}
		ResearchType rt = player().currentResearch;
		player().runningResearch = rt;
		Research rs = player().research.get(rt);
		if (rs == null) {
			rs = new Research();
			rs.type = rt;
			player().research.put(rt, rs);
			rs.remainingMoney = rt.researchCost;
			rs.assignedMoney = rt.researchCost / 2;
			displayCategory(rt.category);
		}
		rs.state = ResearchState.RUNNING;
		update();
	}
}
