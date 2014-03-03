/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;


import hu.openig.core.Action0;
import hu.openig.core.Location;
import hu.openig.core.Pair;
import hu.openig.mechanics.Allocator;
import hu.openig.model.AutoBuild;
import hu.openig.model.BattleInfo;
import hu.openig.model.Building;
import hu.openig.model.BuildingType;
import hu.openig.model.GroundwarGun;
import hu.openig.model.GroundwarUnit;
import hu.openig.model.GroundwarUnitType;
import hu.openig.model.ModelUtils;
import hu.openig.model.Planet;
import hu.openig.model.PlanetGround;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.PlanetSurface;
import hu.openig.model.Player;
import hu.openig.model.ResourceAllocationStrategy;
import hu.openig.model.Screens;
import hu.openig.model.SelectionBoxMode;
import hu.openig.model.SoundType;
import hu.openig.model.SurfaceCell;
import hu.openig.model.SurfaceEntity;
import hu.openig.model.Tile;
import hu.openig.model.Trait;
import hu.openig.model.TraitKind;
import hu.openig.model.WeatherType;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.api.SurfaceEvents;
import hu.openig.screen.panels.SurfaceRenderer;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageFill;
import hu.openig.ui.UIImageTabButton;
import hu.openig.ui.UIImageTabButton2;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.ui.UIMouse.Type;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * The planet surface rendering screen.
 * @author akarnokd, 2010.01.11.
 */
public class PlanetScreenMP extends ScreenBase implements SurfaceEvents {
	/** Indicate if a component is drag sensitive. */
	@Retention(RetentionPolicy.RUNTIME)
	@interface DragSensitive { }
	/** The animation timer. */
	Closeable animationTimer;
	/** The animation timer. */
	Closeable earthQuakeTimer;
	/** The base rectangle. */
	final Rectangle base = new Rectangle();
	/** The planet view window. */
	final Rectangle window = new Rectangle();
	/** The buildings sidebar button. */
	@DragSensitive
	UIImageButton sidebarBuildings;
	/** The radar sidebar button. */
	@DragSensitive
	UIImageButton sidebarRadar;
	/** The building info sidebar button. */
	@DragSensitive
	UIImageButton sidebarBuildingInfo;
	/** The colony info sidebar button. */
	@DragSensitive
	UIImageButton sidebarColonyInfo;
	/** The empty image for the buildings. */
	@DragSensitive
	UIImage sidebarBuildingsEmpty;
	/** The empty image for the radar. */
	@DragSensitive
	UIImage sidebarRadarEmpty;
	/** Button to show/hide navigation buttons. */
	@DragSensitive
	UIImageButton sidebarNavigation;
	/** The colony info navigation button. */
	@DragSensitive
	UIImageButton colonyInfo;
	/** The bridge button. */
	@DragSensitive
	UIImageButton bridge;
	/** The planets button navigation. */
	@DragSensitive
	UIImageButton planets;
	/** The starmap button navigation. */
	@DragSensitive
	UIImageButton starmap;
	/** The surface renderer. */
	SurfaceRenderer render;
	/** The left filler region. */
	@DragSensitive
	UIImageFill leftFill;
	/** The right filler region. */
	@DragSensitive
	UIImageFill rightFill;
	/** The buildings panel. */
	@DragSensitive
	BuildingsPanel buildingsPanel;
	/** The radar panel. */
	@DragSensitive
	UIImage radarPanel;
	/** The buildingInfoPanel. */
	@DragSensitive
	BuildingInfoPanel buildingInfoPanel;
	/** The drawable radar rectangle. */
	@DragSensitive
	RadarRender radar;
	/** The currently selected building. */
	Building currentBuilding;
	/** The information panel. */
	InfoPanel infoPanel;
	/** The upgrade panel. */
	@DragSensitive
	UpgradePanel upgradePanel;
	/** Go to next planet. */
	@DragSensitive
	UIImageButton prev;
	/** Go to previous planet. */
	@DragSensitive
	UIImageButton next;
	/** Show the building list panel? */
	boolean showBuildingList = true;
	/** Show the building info panel? */
	boolean showBuildingInfo = true;
	/** Show the information panel. */
	boolean showInfo = true;
	/** Show the screen navigation buttons? */
	boolean showSidebarButtons = true;
	/** The groundwar animation simulator. */
	Closeable simulator;
	/** Start the battle. */
	@DragSensitive
	UIImageButton startBattle;
	/** Stop the selected units. */
	UIImageTabButton2 stopUnit;
	/** Move the selected units. */
	UIImageTabButton2 moveUnit;
	/** Attack with the selected units. */
	UIImageTabButton2 attackUnit;
	/** The tank panel. */
	@DragSensitive
	TankPanel tankPanel;
	/** The zoom button. */
	@DragSensitive
	UIImageButton zoom;
	/** The zoom direction. */
	boolean zoomDirection;
	/** Zoom to normal. */
	boolean zoomNormal;
	/** The weather sound is running? */
	Action0 weatherSoundRunning;
	/** The last surface. */
	PlanetSurface lastSurface;
	@Override
	public void onFinish() {
		onEndGame();
	}
	
	@Override
	public boolean keyboard(KeyEvent e) {
		boolean rep = false;
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			render.offsetY += 28;
			rep = true;
			break;
		case KeyEvent.VK_DOWN:
			render.offsetY -= 28;
			rep = true;
			break;
		case KeyEvent.VK_LEFT:
			render.offsetX += 54;
			rep = true;
			break;
		case KeyEvent.VK_RIGHT:
			render.offsetX -= 54;
			rep = true;
			break;
		case KeyEvent.VK_ESCAPE:
			if (render.placementMode) {
				render.placementMode = false;
				buildingsPanel.build.down = false;
				e.consume();
				rep = true;
			}
			break;
		case KeyEvent.VK_N:
			if (e.isControlDown()) {
				config.showBuildingName = !config.showBuildingName;
				e.consume();
				rep = true;
			}
			break;
		case KeyEvent.VK_R:
			if (e.isControlDown()) {
				if (planet().weatherTTL <= 0) {
					planet().weatherTTL = 120;
					rep = true;
				} else {
					planet().weatherTTL = 0;
					rep = true;
				}
			}
			break;
		case KeyEvent.VK_D:
			doMineLayerDeploy();
			rep = true;
			break;
		case KeyEvent.VK_C:
			if (e.isControlDown()) {
				render.showCommand = !render.showCommand;
				rep = true;
				e.consume();
			}
			break;
		case KeyEvent.VK_X:
			if (currentBuilding != null) {
				if (e.isControlDown()) {
					currentBuilding.hitpoints -= currentBuilding.type.hitpoints / 10;
					currentBuilding.hitpoints = Math.max(0, currentBuilding.hitpoints);
					if (currentBuilding.hitpoints == 0) {
						doDemolish();
					}
				} else 
				if (e.isShiftDown()) {
					currentBuilding.hitpoints += currentBuilding.type.hitpoints / 10;
					currentBuilding.hitpoints = Math.min(currentBuilding.hitpoints, currentBuilding.type.hitpoints);
				}
				rep = true;
				
			}
			break;
		case KeyEvent.VK_B:
			if (buildingsPanel.build.enabled()) {
				buildingsPanel.build.onPress.invoke();
				rep = true;
			}
			break;
		case KeyEvent.VK_S:
			if (!e.isControlDown()) {
				doStopSelectedUnits();
				e.consume();
				rep = true;
			}
			break;
		case KeyEvent.VK_A:
			selectCommand(attackUnit);
			e.consume();
			rep = true;
			break;
		case KeyEvent.VK_M:
			selectCommand(moveUnit);
			e.consume();
			rep = true;
			break;
		default:
		}
		if (e.isControlDown()) {
			if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) {
				planet().ground.assignGroup(e.getKeyCode() - KeyEvent.VK_0, player());
				e.consume();
				rep = true;
			}
		}
		if (e.isShiftDown()) {
			if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) {
				planet().ground.recallGroup(e.getKeyCode() - KeyEvent.VK_0);
				e.consume();
				rep = true;
			}
		}
		if (commons.battleMode && e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
			// back out of fight instantly
			commons.restoreMainSimulationSpeedFunction();
			commons.battleMode = false;
			displayPrimary(Screens.STARMAP);
			commons.playRegularMusic();
			return true;
		}
		return rep;
	}
	/**
	 * Issue mine laying orders to the selected mine layers.
	 */
	void doMineLayerDeploy() {
		PlanetGround ground = planet().ground;
		for (GroundwarUnit u : ground.selectedUnits) {
			if (u.owner == player()) {
				if (u.model.type == GroundwarUnitType.MINELAYER) {
					ground.minelayers.add(u);
				}
			}
		}
	}

	@Override
	public void onEnter(Screens mode) {
		animationTimer = commons.register(150, new Action0() {
			@Override
			public void invoke() {
				doAnimation();
				doAnimation2();
			}
		});
		earthQuakeTimer = commons.register(150, new Action0() {
			@Override
			public void invoke() {
				doEarthquake();
			}
		});
		if (surface() != null) {
			render.offsetX = -(int)((surface().boundingRectangle.width * render.scale - width) / 2);
			render.offsetY = -(int)((surface().boundingRectangle.height * render.scale - height) / 2);
		}
		focused = render;
		
		render.moveSelect = false;
		render.attackSelect = false;
		attackUnit.enabled(false);
		moveUnit.enabled(false);
		stopUnit.enabled(false);
	}

	@Override
	public void onLeave() {
		
		cancelWeatherSound();
		
		render.placementMode = false;
		buildingsPanel.build.down = false;

		close0(animationTimer);
		animationTimer = null;
		
		close0(earthQuakeTimer);
		earthQuakeTimer = null;

		close0(simulator);
		simulator = null;
	}

	
	/**
	 * The radar renderer component.
	 * @author akarnokd, Mar 27, 2011
	 */
	class RadarRender extends UIComponent {
		/** The surface cell helper. */
		final SurfaceCell cell = new SurfaceCell();
		/** The jammer frame counter. */
		int jammerCounter;
		/** The pre-rendered noise. */
		BufferedImage[] noises = new BufferedImage[0];
		/** Prepare the noise images. */
		public void prepareNoise() {
			noises = new BufferedImage[3];
			Random rnd = new Random();
			for (int k = 0; k < noises.length; k++) {
				noises[k] = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				for (int i = 0; i < width; i += 1) {
					for (int j = 0; j < height; j += 1) {
						if (rnd.nextDouble() < 0.20) {
							noises[k].setRGB(i, j, 0xFF000000);
						}
					}
				}
			}
		}
		@Override
		public void draw(Graphics2D g2) {
			
			float alpha = render.alpha;
			Tile areaEmpty = render.areaEmpty;
			
			RenderTools.setInterpolation(g2, true);
			
			Shape save0 = g2.getClip();
			g2.clipRect(0, 0, width, height);
			
			PlanetSurface surface = surface();
			
			if (surface == null) {
				return;
			}
			Rectangle br = surface.boundingRectangle;

			AffineTransform at = g2.getTransform();
			
			float scalex = width * 1.0f / br.width;
			float scaley = height * 1.0f / br.height;
			float scale = Math.min(scalex, scaley);
			g2.translate(-(br.width * scale - width) / 2, -(br.height * scale - height) / 2);
			g2.scale(scale, scale);
			
			int x0 = surface.baseXOffset;
			int y0 = surface.baseYOffset;

			g2.setColor(new Color(96 * alpha / 255, 96 * alpha / 255, 96 * alpha / 255));
			g2.fillRect(br.x, br.y, br.width, br.height);
			
			if (knowledge(planet(), PlanetKnowledge.NAME) >= 0) {
				BufferedImage empty = areaEmpty.getStrip(0);
				Rectangle renderingWindow = new Rectangle(0, 0, width, height);
				for (int i = 0; i < surface.renderingOrigins.size(); i++) {
					Location loc = surface.renderingOrigins.get(i);
					for (int j = 0; j < surface.renderingLength.get(i); j++) {
						int x = x0 + Tile.toScreenX(loc.x - j, loc.y);
						int y = y0 + Tile.toScreenY(loc.x - j, loc.y);
						Location loc1 = Location.of(loc.x - j, loc.y);
						SurfaceEntity se = surface.buildingmap.get(loc1);
						if (se == null || knowledge(planet(), PlanetKnowledge.OWNER) < 0) {
							se = surface.basemap.get(loc1);
						}
						if (se != null) {
							render.getImage(se, true, loc1, cell);
							int yref = y0 + Tile.toScreenY(cell.a, cell.b) + cell.yCompensation;
							if (renderingWindow.intersects(x * scale, yref * scale, 57 * scale, se.tile.imageHeight * scale)) {
								if (cell.image != null) {
									g2.drawImage(cell.image, x, yref, null);
								}
							}
						} else {
							if (renderingWindow.intersects(x * scale, y * scale, 57 * scale, 27 * scale)) {
								g2.drawImage(empty, x, y, null);
							}
						}
					}
				}
				g2.setColor(Color.RED);
				if (knowledge(planet(), PlanetKnowledge.OWNER) >= 0) {
					Rectangle buildingBox = render.buildingBox;
					if (buildingBox != null) {
						g2.drawRect(buildingBox.x, buildingBox.y, buildingBox.width, buildingBox.height);
					}
				}
				g2.setColor(Color.WHITE);
				g2.drawRect(
						(int)(-render.offsetX / render.scale), 
						(int)(-render.offsetY / render.scale), 
						(int)(render.width / render.scale - 1), 
						(int)(render.height / render.scale - 1));
			}
			boolean jammed = false;
			for (GroundwarUnit u : planet().ground.units) {
				if (u.model.type == GroundwarUnitType.RADAR_JAMMER 
						&& u.owner != player() && u.paralizedTTL == 0) {
					jammed = true;
					break;
				}
			}			
			if (!jammed) {
				for (GroundwarUnit u : planet().ground.units) {
					if (render.blink) {
						int px = (int)(x0 + Tile.toScreenX(u.x + 0.5, u.y - 0.5)) - 11;
						int py = (int)(y0 + Tile.toScreenY(u.x + 0.5, u.y - 0.5));
						
						g2.setColor(u.owner == player() ? Color.GREEN : Color.RED);
						g2.fillRect(px, py, 40, 40);
					}
				}
			}
			
			
			g2.setTransform(at);
			
			if (jammed) {
				g2.drawImage(noises[render.animation % noises.length], 0, 0, null);
			}
			
			g2.setClip(save0);
			RenderTools.setInterpolation(g2, false);
		}
		@Override
		public boolean mouse(UIMouse e) {
			switch (e.type) {
			case WHEEL:
				if (e.has(Modifier.CTRL)) {
					if (moveViewPort(e)) {
						if (e.z < 0) {
							render.zoomIn();
						} else {
							render.zoomOut();
						}
					
						return true;
					}
				}
				return false;
			case DRAG:
			case DOWN:
				return moveViewPort(e);
			default:
				return super.mouse(e);
			}
		}
		/**
		 * Move the viewport based on the mouse event.
		 * @param e the mouse event
		 * @return true if the viewport was moved
		 */
		boolean moveViewPort(UIMouse e) {
			Rectangle br = surface().boundingRectangle;

			double scalex = width * 1.0 / br.width;
			double scaley = height * 1.0 / br.height;
			double scale = Math.min(scalex, scaley);
			
			double dx = -(br.width * scale - width) / 2;
			double dy = -(br.height * scale - height) / 2;
			double dw = br.width * scale;
			double dh = br.height * scale;
			
			if (e.within((int)dx, (int)dy, (int)dw, (int)dh)) {
				double rw = render.width * scale / render.scale / 2;
				double rh = render.height * scale / render.scale / 2;
				
				render.offsetX = -(int)((e.x - dx - rw) * render.scale / scale);
				render.offsetY = -(int)((e.y - dy - rh) * render.scale / scale);
				return true;
			}
			return false;
		}
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.UP) && e.has(Button.RIGHT)) {
			render.drag = false;
			onDrag(false);
		}
		return super.mouse(e);
	}
	@Override
	public void onDrag(boolean dragging) {
		for (Field f : getClass().getDeclaredFields()) {
			if (f.isAnnotationPresent(DragSensitive.class)) {
				try {
					Object o = f.get(this);
					if (o != null) {
						UIComponent.class.cast(o).enabled(!dragging);
					} else {
						System.out.println(f.getName());
					}
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Exceptions.add(e);
				}
			}
		}
	}
	/**
	 * Update the UI based on the selected building.
	 * @param b the building selected
	 */
	void doSelectBuilding(Building b) {
		currentBuilding = b;
		if (b != null) {
			player().currentBuilding = b.type;
			if (b.type.research != null) {
				research(b.type.research);
			}
			setBuildingList(0);
		}
		buildingInfoPanel.update();
	}
	/**
	 * Return the unit label for the given resource type.
	 * @param type the resource type
	 * @return the unit string
	 */
	String getUnit(String type) {
		return get("building.resource.type." + type);
	}
	/** The building preview component. */
	class BuildingPreview extends UIComponent {
		/** The building image. */
		public BufferedImage building;
		/** The building cost. */
		public int cost;
		/** The count on this planet. */
		public int count;
		@Override
		public void draw(Graphics2D g2) {
			if (building != null) {
				int dx = (width - building.getWidth()) / 2;
				int dy = (height - building.getHeight()) / 2;
				g2.drawImage(building, dx, dy, null);
				if (!enabled) {
					g2.setColor(Color.RED);
					g2.drawLine(0, 0, width - 1, height - 1);
					g2.drawLine(width - 1, 0, 0, height - 1);
				}
				String cs = cost + " cr";
				int w = commons.text().getTextWidth(10, cs);
				
				int color = TextRenderer.YELLOW;
//				if (player().money < cost) {
//					color = 0xFFFF8080;
//				}
				
				commons.text().paintTo(g2, width - w - 2, height - 12, 10, color, cs);
				commons.text().paintTo(g2, 2, height - 12, 10, TextRenderer.GREEN, Integer.toString(count));
			}
		}
	}
	/** The building preview panel. */
	class BuildingsPanel extends UIContainer {
		/** The building preview. */
		BuildingPreview preview;
		/** The building list up button. */
		UIImageButton buildingUp;
		/** The building list up button empty. */
		UIImage buildingUpEmpty;
		/** The building list down button. */
		UIImageButton buildingDown;
		/** The building list down button empty. */
		UIImage buildingDownEmpty;
		/** The building list button. */
		UIImageButton buildingList;
		/** The build button. */
		UIImageTabButton build;
		/** The building name. */
		UILabel buildingName;
		/** Indicate a larger jump. */
		boolean buildingDown10;
		/** Indicate a larger jum. */
		boolean buildingUp10;
		/** Construct and place the UI. */
		public BuildingsPanel() {
			preview = new BuildingPreview();
			
			buildingUp = new UIImageButton(commons.colony().upwards) {
				@Override
				public boolean mouse(UIMouse e) {
					buildingUp10 = e.has(Button.RIGHT);
					return super.mouse(e);
				}
			};
			buildingDown = new UIImageButton(commons.colony().downwards) {
				@Override
				public boolean mouse(UIMouse e) {
					buildingDown10 = e.has(Button.RIGHT);
					return super.mouse(e);
				}
			};
			
			
			buildingUpEmpty = new UIImage(commons.colony().empty);
			buildingUpEmpty.visible(false);
			buildingDownEmpty = new UIImage(commons.colony().empty);
			buildingDownEmpty.visible(false);
			build = new UIImageTabButton(commons.colony().build);
			build.setDisabledPattern(commons.common().disabledPattern);
			build.enabled(false);
			buildingList = new UIImageButton(commons.colony().list);
			buildingName = new UILabel("", 10, commons.text());
			buildingName.color(TextRenderer.YELLOW);
			buildingName.horizontally(HorizontalAlignment.CENTER);
			
			preview.bounds(7, 7, 140, 103);
			
			buildingUp.location(153, 7);
			buildingUpEmpty.location(buildingUp.location());
			buildingDown.location(153, 62);
			buildingDownEmpty.location(buildingDown.location());
			buildingName.bounds(8, 117, 166, 18);
			buildingList.location(7, 142);
			build.location(94, 142);
			
			width = commons.colony().buildingsPanel.getWidth();
			height = commons.colony().buildingsPanel.getHeight();
			
			buildingDown.onClick = new Action0() {
				@Override
				public void invoke() {
					buttonSound(SoundType.CLICK_HIGH_2);
					setBuildingList(buildingDown10 ? 10 : 1);
				}
			};
			
			buildingDown.setHoldDelay(150);
			buildingUp.onClick = new Action0() {
				@Override
				public void invoke() {
					buttonSound(SoundType.CLICK_HIGH_2);
					setBuildingList(buildingUp10 ? -10 : -1);
				}
			};
			buildingUp.setHoldDelay(150);
			
			buildingList.onClick = new Action0() {
				@Override
				public void invoke() {
					render.placementMode = false;
					build.down = false;
					upgradePanel.hideUpgradeSelection();
					displaySecondary(Screens.INFORMATION_BUILDINGS);
				}
			};
			build.onPress = new Action0() {
				@Override
				public void invoke() {
					render.placementMode = !render.placementMode;
					if (render.placementMode) {
						buttonSound(SoundType.CLICK_HIGH_2);
						build.down = true;
						currentBuilding = null;
						render.buildingBox = null;
						Tile t = player().currentBuilding.tileset.get(race()).normal;
						render.placementRectangle.setSize(t.width + 2, t.height + 2);
					} else {
						build.down = false;
					}
				}
			};
			
			addThis();
		}
		@Override
		public void draw(Graphics2D g2) {
			g2.drawImage(commons.colony().buildingsPanel, 0, 0, null);
			super.draw(g2);
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.WHEEL)) {
				if (e.z < 0) {
					setBuildingList(-1);
				} else {
					setBuildingList(+1);
				}
				return true;
			}
			return super.mouse(e);
		}
	}
	/** The building info panel. */
	class BuildingInfoPanel extends UIContainer {
		/** The building name on the info panel. */
		UILabel buildingInfoName;
		/** The energy allocation percent. */
		UILabel energyPercent;
		/** The energy total. */
		UILabel energy;
		/** The worker allocated percent. */
		UILabel workerPercent;
		/** The worker total. */
		UILabel worker;
		/** The current production level. */
		UILabel production;
		/** The operation percent. */
		UILabel operationPercent;
		/** Demolish the building. */
		UIImageButton demolish;
		/** Construction Indicator with percent. */
		UIImageButton constructing;
		/** Damage indicator with percent. */
		UIImageButton damaged;
		/** Repairing indicator with percent. */
		UIImageButton repairing;
		/** The building is in normal operational condition. */
		UIImageButton undamaged;
		/** The building was manually put offline. */
		UIImageButton stateOffline;
		/** The building is inoperable due non-energy reasons. */
		UIImageButton stateInactive;
		/** The building is inoperable due low energy. */
		UIImageButton stateNoEnergy;
		/** The building is damaged. */
		UIImageButton stateDamaged;
		/** The active indicator. */
		UIImageButton stateActive;
		/** The energy label. */
		UIImage energyLabel;
		/** The worker label. */
		UIImage workerLabel;
		/** The production label. */
		UIImage productionLabel;
		/** The operation label. */
		UIImage operationLabel;
		/** The build/damage/repair percent upper. */
		UILabel progressUpper;
		/** The build/damage/repair percent lower. */
		UILabel progressLower;
		/** Construct the sub-elements. */
		public BuildingInfoPanel() {
			buildingInfoName = new UILabel("-", 10, commons.text());
			buildingInfoName.bounds(8, 6, 182, 16);
			
			energyPercent = new UILabel("-", 10, commons.text());
			energyPercent.bounds(70, 29, 28, 12);
			
			energy = new UILabel("-", 10, commons.text());
			energy.bounds(119, 29, 42, 12);
			
			workerPercent = new UILabel("-", 10, commons.text());
			workerPercent.bounds(70, 45, 28, 12);
			
			worker = new UILabel("-", 10, commons.text());
			worker.bounds(119, 45, 28, 12);
			
			operationPercent = new UILabel("-", 10, commons.text());
			operationPercent.bounds(70, 61, 28, 12);
			
			production = new UILabel("-", 10, commons.text());
			production.bounds(70, 77, 77, 12);
			
			energyLabel = new UIImage(commons.colony().energy);
			leftTo(energyPercent, energyLabel);
			
			workerLabel = new UIImage(commons.colony().workers);
			leftTo(workerPercent, workerLabel);

			operationLabel = new UIImage(commons.colony().operational);
			leftTo(operationPercent, operationLabel);

			productionLabel = new UIImage(commons.colony().production);
			leftTo(production, productionLabel);
			
			stateDamaged = new UIImageButton(commons.colony().statusDamaged);
			stateInactive = new UIImageButton(commons.colony().statusInactive);
			stateNoEnergy = new UIImageButton(commons.colony().statusNoEnergy);
			stateActive = new UIImageButton(commons.colony().active);
			stateOffline = new UIImageButton(commons.colony().statusOffline);
			constructing = new UIImageButton(commons.colony().constructing);
			damaged = new UIImageButton(commons.colony().damaged);
			repairing = new UIImageButton(commons.colony().repairing);
			undamaged = new UIImageButton(commons.colony().undamaged);

			damaged.location(8, 98);
			repairing.location(8, 98);
			undamaged.location(8, 98);

			stateActive.location(8, 122);
			stateOffline.location(8, 122);
			stateInactive.location(8, 122);
			stateNoEnergy.location(8, 122);
			stateDamaged.location(8, 122);
			constructing.location(8, 122);

			
			progressUpper = new UILabel("-", 10, commons.text());
			progressUpper.bounds(8 + 96, 98 + 3, 28, 10);
			progressUpper.visible(false);
			
			progressLower = new UILabel("-", 10, commons.text());
			progressLower.bounds(8 + 96, 122 + 3, 28, 10);
			progressLower.visible(false);
			
			demolish = new UIImageButton(commons.colony().demolish);
			demolish.location(161, 50);
			demolish.setDisabledPattern(commons.common().disabledPattern);
			stateActive.setDisabledPattern(commons.common().disabledPattern);
			stateInactive.setDisabledPattern(commons.common().disabledPattern);
			stateNoEnergy.setDisabledPattern(commons.common().disabledPattern);
			stateDamaged.setDisabledPattern(commons.common().disabledPattern);
			stateOffline.setDisabledPattern(commons.common().disabledPattern);

			centerYellow(buildingInfoName, energyPercent, energy, 
					workerPercent, worker, operationPercent, 
					progressUpper, progressLower);

			production.color(TextRenderer.YELLOW);
			
			width = commons.colony().buildingInfoPanel.getWidth();
			height = commons.colony().buildingInfoPanel.getHeight();
			
			addThis();
		}
		/**
		 * Set the location of the target component so that it is left
		 * to the source component directly and is centered along the same line.
		 * @param source the source component
		 * @param target the target component to set the location
		 */
		void leftTo(UIComponent source, UIComponent target) {
			target.location(source.x - target.width - 4, source.y + (source.height - target.height) / 2);
		}
		/**
		 * Center and set yellow color on the labels.
		 * @param labels the labels
		 */
		void centerYellow(UILabel... labels) {
			for (UILabel l : labels) {
				l.color(TextRenderer.YELLOW);
				l.horizontally(HorizontalAlignment.CENTER);
			}
		}
		@Override
		public void draw(Graphics2D g2) {
			g2.drawImage(commons.colony().buildingInfoPanel, 0, 0, null);
			super.draw(g2);
		}
		/** Hide the state and progress buttons. */
		public void hideStates() {
			stateActive.visible(false);
			stateDamaged.visible(false);
			stateInactive.visible(false);
			stateNoEnergy.visible(false);
			stateOffline.visible(false);
			
			
			constructing.visible(false);
			damaged.visible(false);
			repairing.visible(false);
			progressUpper.visible(false);
			progressLower.visible(false);
			undamaged.visible(false);
		}
		/**
		 * Update the UI based on the current status.
		 */
		public void update() {
			Building b = currentBuilding;
			if (b != null) {
				buildingInfoName.text(b.type.name);
				
				Set<UIComponent> tohide = U.newSet(
						undamaged,
						damaged,		
						repairing,		
						constructing,		
						stateActive,		
						stateDamaged,		
						stateInactive,		
						stateOffline,		
						stateNoEnergy,		
						progressLower,		
						progressUpper		
				);
				
				if (b.isConstructing()) {
					energy.text("-");
					energyPercent.text("-");
					worker.text("-");
					workerPercent.text("-");
					operationPercent.text("-");
					production.text("-");
					
					constructing.visible(true);
					progressLower.visible(true);
					progressLower.text(Integer.toString(b.buildProgress * 100 / b.type.hitpoints));
					
					tohide.remove(constructing);
					tohide.remove(progressLower);
					
					if (b.isDamaged()) {
						buildingInfoPanel.damaged.visible(true);
						buildingInfoPanel.progressUpper.visible(true);
						
						tohide.remove(buildingInfoPanel.damaged);
						tohide.remove(buildingInfoPanel.progressUpper);
						
						if (b.hitpoints > 0) {
							buildingInfoPanel.progressUpper.text(Integer.toString((b.hitpoints - b.buildProgress) * 100 / b.hitpoints));
						} else {
							buildingInfoPanel.progressUpper.text("0");
						}
					} else {
						buildingInfoPanel.undamaged.visible(true);
						tohide.remove(buildingInfoPanel.undamaged);
						
					}
				} else {
					if (!b.enabled || b.isSeverlyDamaged()) {
						buildingInfoPanel.energy.text("-");
						buildingInfoPanel.energyPercent.text("-");
						buildingInfoPanel.worker.text("-");
						buildingInfoPanel.workerPercent.text("-");
						buildingInfoPanel.operationPercent.text("-");
						buildingInfoPanel.production.text("-");
					} else {
						if (b.getEnergy() < 0) {
							buildingInfoPanel.energy.text(Integer.toString(-b.getEnergy()));
							buildingInfoPanel.energyPercent.text(Integer.toString(Math.abs(b.assignedEnergy * 100 / b.getEnergy())));
						} else {
							buildingInfoPanel.energy.text("-");
							buildingInfoPanel.energyPercent.text("-");
						}
						if (b.getWorkers() < 0) {
							buildingInfoPanel.worker.text(Integer.toString(-b.getWorkers()));
							buildingInfoPanel.workerPercent.text(Integer.toString(Math.abs(b.assignedWorker)));
						} else {
							buildingInfoPanel.worker.text("-");
							buildingInfoPanel.workerPercent.text("-");
						}
						buildingInfoPanel.operationPercent.text(Integer.toString((int)(b.getEfficiency() * 100)));
						if (b.type.primary != null) {
							double f = b.getPrimary();
							// apply trait override
							if (b.hasResource("spaceship")) {
								f = planet().owner.traits.apply(TraitKind.SHIP_PRODUCTION, 0.01d, f);
							} else
							if (b.hasResource("weapon")) {
								f = planet().owner.traits.apply(TraitKind.WEAPON_PRODUCTION, 0.01d, f);
							} else
							if (b.hasResource("equipment")) {
								f = planet().owner.traits.apply(TraitKind.EQUIPMENT_PRODUCTION, 0.01d, f);
							}
							
							String s;
							if (f < 10) {
								s = String.format("%.1f", f);
							} else {
								s = String.format("%.0f", f);
							}
							
							buildingInfoPanel.production.text(s + getUnit(b.type.primary));
						} else {
							buildingInfoPanel.production.text("");
						}
					}
					
					// set the upper status indicators
					
					if (b.isDamaged()) {
						if (b.repairing) {
							buildingInfoPanel.repairing.visible(true);
							tohide.remove(buildingInfoPanel.repairing);
						} else {
							buildingInfoPanel.damaged.visible(true);
							tohide.remove(buildingInfoPanel.damaged);
						}
						buildingInfoPanel.progressUpper.visible(true);
						tohide.remove(buildingInfoPanel.progressUpper);
						buildingInfoPanel.progressUpper.text(Integer.toString((b.type.hitpoints - b.hitpoints) * 100 / b.type.hitpoints));
					} else {
						buildingInfoPanel.undamaged.visible(true);
						tohide.remove(buildingInfoPanel.undamaged);
					}
					
					// set the lower status indicator
					
					if (b.enabled) {
						if (b.isDamaged()) {
							buildingInfoPanel.stateDamaged.visible(true);
							tohide.remove(buildingInfoPanel.stateDamaged);
						} else
						if (b.isEnergyShortage()) {
							buildingInfoPanel.stateNoEnergy.visible(true);
							tohide.remove(buildingInfoPanel.stateNoEnergy);
						} else
						if (b.isWorkerShortage()) {
							buildingInfoPanel.stateInactive.visible(true);
							tohide.remove(buildingInfoPanel.stateInactive);
						} else {
							buildingInfoPanel.stateActive.visible(true);
							tohide.remove(buildingInfoPanel.stateActive);
						}

					} else {
						buildingInfoPanel.stateOffline.visible(true);
						tohide.remove(buildingInfoPanel.stateOffline);
					}
				}
				for (UIComponent c : tohide) {
					c.visible(false);
				}
				
			} else {
				buildingInfoPanel.buildingInfoName.text("-");
				buildingInfoPanel.energy.text("-");
				buildingInfoPanel.energyPercent.text("-");
				buildingInfoPanel.worker.text("-");
				buildingInfoPanel.workerPercent.text("-");
				buildingInfoPanel.operationPercent.text("-");
				buildingInfoPanel.production.text("-");
				buildingInfoPanel.undamaged.visible(true);
				buildingInfoPanel.stateInactive.visible(true);
				buildingInfoPanel.progressLower.visible(false);
				buildingInfoPanel.progressUpper.visible(false);
			}
			buildingInfoPanel.demolish.enabled(planet().owner == player() && currentBuilding != null);
			buildingInfoPanel.stateActive.enabled(planet().owner == player() && currentBuilding != null);
			buildingInfoPanel.stateDamaged.enabled(planet().owner == player() && currentBuilding != null);
			buildingInfoPanel.stateNoEnergy.enabled(planet().owner == player() && currentBuilding != null);
			buildingInfoPanel.stateInactive.enabled(planet().owner == player() && currentBuilding != null);
			buildingInfoPanel.stateOffline.enabled(planet().owner == player() && currentBuilding != null);
			buildingInfoPanel.repairing.enabled(planet().owner == player());
			buildingInfoPanel.damaged.enabled(planet().owner == player());
			
			upgradePanel.visible(
					b != null && b.type.upgrades.size() > 0 
					&& buildingInfoPanel.visible() 
					&& b.isComplete()
					&& !b.isSeverlyDamaged()
					&& planet().owner == player()
			);
			
			if (demolish.enabled()) {
				setTooltip(demolish, "buildings.demolish.tooltip", (currentBuilding.upgradeLevel + 1) * currentBuilding.type.cost / 2);
			} else {
				setTooltip(demolish, null);
			}
		}
	}
	/** Perform the animation. */
	void doAnimation() {
		//wrap animation index
		if (render.animation == Integer.MAX_VALUE) {
			render.animation = -1;
		}
		render.animation++;
		render.blink = render.animation / 4 % 2 == 0;

		render.weatherOverlay.update();
		
		askRepaint();
	}
	/** Perform the faster animation. */
	void doAnimation2() {
		if (planet().weatherTTL > 0 && planet().type.weatherDrop == WeatherType.RAIN) {
			if (weatherSoundRunning == null) {
				weatherSoundRunning = commons.sounds.playSound(
						SoundType.RAIN, new Action0() {
					@Override
					public void invoke() {
						weatherSoundRunning = null;
					}
				}, true);
			}
			if (ModelUtils.randomInt(60) < 1) {
				effectSound(SoundType.THUNDER);
			}
		} else {
			cancelWeatherSound();
		}
		render.weatherOverlay.update();
		askRepaint();
	}
	/**
	 * Cancel the weather sound.
	 */
	void cancelWeatherSound() {
		if (weatherSoundRunning != null) {
			weatherSoundRunning.invoke();
			weatherSoundRunning = null;
		}
	}
	/** Animate the shaking during an earthquake. */
	void doEarthquake() {
		if (planet().earthQuakeTTL > 0) {
			if (!commons.simulation.paused()) {
				render.offsetX += (2 - ModelUtils.randomInt(5)) * 2;
				render.offsetY += (1 - ModelUtils.randomInt(3));
				askRepaint();
			}
		}
	}
	/** Demolish the selected building. */
	void doDemolish() {
		boolean fortif = false;
		for (GroundwarGun g : new ArrayList<>(planet().ground.guns)) {
			if (g.building == currentBuilding) {
				planet().ground.remove(g);
				fortif = true;
			}
		}
		if (fortif) {
//				battle.defenderFortificationLosses++;
			for (GroundwarUnit gu : planet().ground.units) {
				if (gu.attackBuilding == currentBuilding) {
					gu.attackBuilding = null;
				}
			}
		}
		
		planet().demolish(currentBuilding);
		

		doAllocation();
		render.buildingBox = null;
		doSelectBuilding(null);
		effectSound(SoundType.DEMOLISH_BUILDING);
	}
	/** Action for the Active button. */
	void doActive() {
		currentBuilding.enabled = false;
		doSelectBuilding(currentBuilding);
		doAllocation();
	}
	/** Action for the Offline button. */
	void doOffline() {
		currentBuilding.enabled = true;
		doSelectBuilding(currentBuilding);
		doAllocation();
	}
	/** Toggle the repair state. */
	void doToggleRepair() {
		currentBuilding.repairing = !currentBuilding.repairing;
		buildingInfoPanel.repairing.visible(currentBuilding.repairing);
		buildingInfoPanel.damaged.visible(!currentBuilding.repairing);
		doSelectBuilding(currentBuilding);
	}
	/** Perform the resource allocation now! */
	void doAllocation() {
		if (enemyOnSurface()) {
			Allocator.computeNow(planet(), ResourceAllocationStrategy.BATTLE);
		} else {
			Allocator.computeNow(planet());
		}
	}
	/**
	 * Check if there are enemy units on the surface.
	 * @return true if enemy units are present
	 */
	boolean enemyOnSurface() {
		for (GroundwarUnit u : planet().ground.units) {
			if (u.owner != player()) {
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
		UILabel taxTradeIncome;
		/** Label field. */
		UILabel taxInfo;
//		/** Label field. */
//		UILabel allocation;
		/** Label field. */
		UILabel autobuild;
		/** Label field. */
		UILabel other;
		/** Label field. */
		UILabel needed;
		/** The labels. */
		List<UILabel> lines;
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
			taxTradeIncome = new UILabel("-", textSize, commons.text());
			taxInfo = new UILabel("-", textSize, commons.text());
//			allocation = new UILabel("-", textSize, commons.text());
			autobuild = new UILabel("-", textSize, commons.text());
			other = new UILabel("-", 7, commons.text());
			other.wrap(true);
			needed = new UILabel("-", 7, commons.text());
			needed.wrap(true);
			needed.color(TextRenderer.RED);
			
			lines = Arrays.asList(
					owner, race, surface, population, housing, worker, hospital, food, energy, police,
					taxTradeIncome, taxInfo, autobuild
			);
			
			enabled(false);
			addThis();
		}
		@Override
		public void draw(Graphics2D g2) {
			Composite c = g2.getComposite();
			g2.setComposite(AlphaComposite.SrcOver.derive(0.85f));
			g2.setColor(Color.BLACK);
			g2.fillRoundRect(0, 0, width, height, 10, 10);
			g2.setComposite(c);
			
			super.draw(g2);
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
					c.y = 25 + (textSize + 3) * i;
					c.size(textSize);
					c.height = textSize;
					w = Math.max(w, c.x + c.width);
					h = Math.max(h, c.y + c.height);
					i++;
				}
			}
			w = Math.max(w, this.planet.x + this.planet.width);
			other.bounds(10, 25 + (textSize + 3) * i, w, 0);
			other.size(w, other.getWrappedHeight());
			h = Math.max(h, other.y + other.height);
//			i++;
			
			if (needed.visible()) {
				needed.bounds(10, other.y + other.height + 3, w, 0);
				needed.size(w, needed.getWrappedHeight());
				h = Math.max(h, needed.y + needed.height);
			}
			
			width = w + 10;
			height = h + 5;
		}
		/**
		 * Update the display values based on the current planet's settings.
		 * @return the planet statistics used
		 */
		public PlanetStatistics update() {
			Planet p = planet();
			PlanetStatistics ps = p.getStatistics();
			
			planet.text(p.name(), true);
			
			if (knowledge(p, PlanetKnowledge.OWNER) >= 0) {
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
				owner.visible(true);
				race.visible(true);
			} else {
				owner.visible(false);
				race.visible(false);
				planet.color(TextRenderer.GRAY);
			}
			
			String surfaceText = format("colonyinfo.surface", U.firstUpper(get(p.type.label)));
			if (p.owner == null && knowledge(p, PlanetKnowledge.OWNER) >= 0) {
				double g = world().galaxyModel.getGrowth(p.type.type, player().race);
				Trait t = player().traits.trait(TraitKind.FERTILE);
				if (t != null) {
					g *= 1 + t.value / 100;
				}
				surfaceText = format("colonyinfo.surface2", 
						U.firstUpper(get(p.type.label)), (int)(g * 100));
			} else
			if (p.owner == player()) {
				double g = world().galaxyModel.getGrowth(p.type.type, p.race) * ps.populationGrowthModifier;

				Trait t = player().traits.trait(TraitKind.FERTILE);
				if (t != null) {
					g *= 1 + t.value / 100;
				}

				surfaceText = format("colonyinfo.surface2", 
						U.firstUpper(get(p.type.label)), (int)(g * 100));
			}
			surface.text(surfaceText, true);
			
			population.visible(false);
			housing.visible(false);
			worker.visible(false);
			hospital.visible(false);
			food.visible(false);
			energy.visible(false);
			police.visible(false);
			taxTradeIncome.visible(false);
			taxInfo.visible(false);
			autobuild.visible(false);
			
			if (p.isPopulated()) {
			
				if (p.owner == player()) {
					population.text(format("colonyinfo.population", 
							(int)p.population(), get(p.getMoraleLabel()), withSign((int)(p.population() - p.lastPopulation()))
					), true).visible(true);
					
					setLabel(housing, "colonyinfo.housing", ps.houseAvailable, (int)p.population()).visible(true);
					setLabel(worker, "colonyinfo.worker", (int)p.population(), ps.workerDemand).visible(true);
					setLabel(hospital, "colonyinfo.hospital", ps.hospitalAvailable, (int)p.population()).visible(true);
					setLabel(food, "colonyinfo.food", ps.foodAvailable, (int)p.population()).visible(true);
					setLabel(energy, "colonyinfo.energy", ps.energyAvailable, ps.energyDemand).visible(true);
					setLabel(police, "colonyinfo.police", ps.policeAvailable, (int)p.population()).visible(true);
					
					taxTradeIncome.text(format("colonyinfo.tax-trade", 
							(int)p.taxIncome(), (int)p.tradeIncome()
					), true).visible(true);
					
					taxInfo.text(format("colonyinfo.tax-info",
							get(p.getTaxLabel()), (int)p.morale, withSign((int)(p.morale - p.lastMorale))
					), true).visible(true);
					
					autobuild.text(format("colonyinfo.autobuild",
							get(p.getAutoBuildLabel())
					), true).visible(true);
					if (p.autoBuild != AutoBuild.OFF) {
						autobuild.color(TextRenderer.YELLOW);
					} else {
						autobuild.color(TextRenderer.GREEN);
					}
				} else {
					if (knowledge(p, PlanetKnowledge.BUILDING) >= 0) {
						population.text(format("colonyinfo.population.alien", 
								(int)p.population()
						), true).visible(true);
					}
				}
			}
			String oi = world().getOtherItems();
			other.visible(true);
			other.text(format("colonyinfo.other", oi.isEmpty() ? "-" : oi), true);
			
			if (p.owner == player()) {
				String nd = world().getNeeded(ps);
				needed.visible(!nd.isEmpty());
				needed.text(format("colonyinfo.needed", nd), true);
			} else {
				needed.visible(false);
				needed.text("", true);
			}

			computeSize();
			location(sidebarColonyInfo.x - width - 2, sidebarColonyInfo.y + sidebarColonyInfo.height - height - 2);
			
			return ps;
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
	/**
	 * The upgrade panel.
	 * @author akarnokd, 2011.03.31.
	 */
	class UpgradePanel extends UIContainer {
		/** The upgrade static label. */
		UIImage upgradeLabel;
		/** The upgrade steps. */
		final List<UIImageButton> steps = new ArrayList<>();
		/** No upgrades. */
		private UIImageButton none;
		/** Construct the panel. */
		public UpgradePanel() {
			size(commons.colony().upgradePanel.getWidth(), commons.colony().upgradePanel.getHeight());
			upgradeLabel = new UIImage(commons.colony().upgradeLabel);
			upgradeLabel.location(8, 5);
			
			none = new UIImageButton(commons.colony().upgradeNone);
			none.onClick = new Action0() {
				@Override
				public void invoke() {
					doUpgrade(0);
				}
			};
			none.location(upgradeLabel.x + upgradeLabel.width + 16, upgradeLabel.y - 2);
			for (int i = 1; i < 5; i++) {
				UIImageButton up = new UIImageButton(commons.colony().upgradeDark);
				up.location(upgradeLabel.x + upgradeLabel.width + i * 16 + 16, upgradeLabel.y - 2);
				steps.add(up);
				final int j = i;
				up.onClick = new Action0() {
					@Override
					public void invoke() {
						doUpgrade(j);
					}
				};
				add(up);
			}
			none.visible(false);
			addThis();
		}
		@Override
		public void draw(Graphics2D g2) {
			g2.drawImage(commons.colony().upgradePanel, 0, 0, null);
			for (int i = 0; i < steps.size(); i++) {
				UIImageButton up = steps.get(i);
				up.visible(i + 1 <= currentBuilding.type.upgrades.size());
				if (currentBuilding.upgradeLevel <= i) {
					up.normal(commons.colony().upgradeDark);
					up.hovered(commons.colony().upgradeDark);
					up.pressed(commons.colony().upgradeDark);
					if (up.visible()) {
						int dl = i - currentBuilding.upgradeLevel + 1;
						long m = currentBuilding.type.cost * dl;
						setTooltip(up, "buildings.upgrade.cost", 
								currentBuilding.type.upgrades.get(i).description, 
								m <= player().money() ? "FFFFFFFF" : "FFFF0000",
								m);
					}
				} else {
					up.normal(commons.colony().upgrade);
					up.hovered(commons.colony().upgrade);
					up.pressed(commons.colony().upgrade);
					if (up.visible()) {
						setTooltip(up, "buildings.upgrade.bought", 
								currentBuilding.type.upgrades.get(i).description);
					}
				}
				if (!up.visible()) {
					setTooltip(up, null);
				}
			}
			if (currentBuilding.upgradeLevel != 0) {
				setTooltip(this, "buildings.upgrade.has", currentBuilding.type.upgrades.get(currentBuilding.upgradeLevel - 1).description);
			} else {
				setTooltip(this, "buildings.upgrade.none");
			}
			setTooltipText(upgradeLabel, this.tooltip);
			setTooltip(none, "buildings.upgrade.default.description");
			
			super.draw(g2);
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.LEAVE)) {
				hideUpgradeSelection();
				return true;
			}
			return super.mouse(e);
		}
		/** Hide the selection of the upgrade panel. */
		public void hideUpgradeSelection() {
			for (UIImageButton img : upgradePanel.steps) {
				img.over = false;
			}
		}
		@Override
		public UIComponent visible(boolean state) {
			if (!state) {
				hideUpgradeSelection();
			}
			return super.visible(state);
		}
	}
	/** 
	 * Upgrade the current building. 
	 * @param j level
	 */
	void doUpgrade(int j) {
		if (currentBuilding != null && currentBuilding.upgradeLevel < j) {
			int delta = (j - currentBuilding.upgradeLevel) * currentBuilding.type.cost;
			if (player().money() >= delta) {
				player().addMoney(-delta);
				player().today.buildCost += delta;
				
				currentBuilding.buildProgress = currentBuilding.type.hitpoints * 1 / 4;
				currentBuilding.hitpoints = currentBuilding.buildProgress;
				
				doAllocation();
				
				upgradePanel.hideUpgradeSelection();
				
				player().statistics.upgradeCount.value += j - currentBuilding.upgradeLevel;
				player().statistics.moneyUpgrade.value += delta;
				player().statistics.moneySpent.value += delta;
				
				world().statistics.upgradeCount.value += j - currentBuilding.upgradeLevel;
				world().statistics.moneyUpgrade.value += delta;
				world().statistics.moneySpent.value += delta;

				currentBuilding.setLevel(j);
				buttonSound(SoundType.CLICK_MEDIUM_2);
			} else {
				buttonSound(SoundType.NOT_AVAILABLE);
				commons.control().displayError(get("message.not_enough_money"));
			}
		}
	}
	@Override
	public void onInitialize() {
		render = new SurfaceRenderer(commons, this);
		render.z = -1;
		
		sidebarBuildings = new UIImageButton(commons.colony().sidebarBuildings);
		sidebarBuildingsEmpty = new UIImage(commons.colony().sidebarBuildingsEmpty);
		sidebarBuildingsEmpty.visible(false);
		sidebarRadar = new UIImageButton(commons.colony().sidebarRadar);
		sidebarRadarEmpty = new UIImage(commons.colony().sidebarRadarEmpty);
		sidebarRadarEmpty.visible(false);
		sidebarBuildingInfo = new UIImageButton(commons.colony().sidebarBuildingInfo);
		sidebarColonyInfo = new UIImageButton(commons.colony().sidebarColonyInfo);
		sidebarNavigation = new UIImageButton(commons.colony().sidebarButtons);
		startBattle = new UIImageButton(commons.colony().startBattle);
		startBattle.visible(false);
		
		colonyInfo = new UIImageButton(commons.colony().colonyInfo);
		bridge = new UIImageButton(commons.colony().bridge);
		planets = new UIImageButton(commons.colony().planets);
		starmap = new UIImageButton(commons.colony().starmap);
		
		leftFill = new UIImageFill(commons.colony().sidebarLeftTop, commons.colony().sidebarLeftFill, commons.colony().sidebarLeftBottom, false);
		rightFill = new UIImageFill(commons.colony().sidebarRightTop, commons.colony().sidebarRightFill, commons.colony().sidebarRightBottom, false);
		
		radarPanel = new UIImage(commons.colony().radarPanel);
		radar = new RadarRender();
		radar.size(154, 134);
		radar.prepareNoise();
		radar.z = 1;
		
		buildingsPanel = new BuildingsPanel();
		buildingsPanel.z = 1;
		buildingInfoPanel = new BuildingInfoPanel();
		buildingInfoPanel.z = 1;
		
		infoPanel = new InfoPanel();
		
		sidebarNavigation.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.GROUNDWAR_TOGGLE_PANEL);
				showSidebarButtons = !showSidebarButtons;
			}
		};
		sidebarRadar.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.GROUNDWAR_TOGGLE_PANEL);
				radar.visible(!radar.visible());
				radarPanel.visible(!radarPanel.visible());
			}
		};
		sidebarBuildingInfo.onClick = new Action0() {
			@Override
			public void invoke() {
				if (planet().owner == player()) {
					buttonSound(SoundType.GROUNDWAR_TOGGLE_PANEL);
					showBuildingInfo = !showBuildingInfo;
					upgradePanel.visible(currentBuilding != null 
							&& currentBuilding.type.upgrades.size() > 0 
							&& buildingInfoPanel.visible());
				}
			}
		};
		sidebarBuildings.onClick = new Action0() {
			@Override
			public void invoke() {
				if (planet().owner == player()) {
					buttonSound(SoundType.GROUNDWAR_TOGGLE_PANEL);
					showBuildingList = !showBuildingList;
				}
			}
		};
		colonyInfo.onClick = new Action0() {
			@Override
			public void invoke() {
				render.placementMode = false;
				buildingsPanel.build.down = false;
				upgradePanel.hideUpgradeSelection();
				displaySecondary(Screens.INFORMATION_COLONY);
			}
		};
		planets.onClick = new Action0() {
			@Override
			public void invoke() {
				render.placementMode = false;
				buildingsPanel.build.down = false;
				upgradePanel.hideUpgradeSelection();
				displaySecondary(Screens.INFORMATION_PLANETS);
			}
		};
		starmap.onClick = new Action0() {
			@Override
			public void invoke() {
				upgradePanel.hideUpgradeSelection();
				displayPrimary(Screens.STARMAP);
			}
		};
		bridge.onClick = new Action0() {
			@Override
			public void invoke() {
				upgradePanel.hideUpgradeSelection();
				displayPrimary(Screens.BRIDGE);
			}
		};
		
		buildingInfoPanel.demolish.onClick = new Action0() {
			@Override
			public void invoke() {
				upgradePanel.hideUpgradeSelection();
				doDemolish();
			}
		};
		buildingInfoPanel.stateActive.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				doActive();
			}
		};
		buildingInfoPanel.stateNoEnergy.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				doActive();
			}
		};
		buildingInfoPanel.stateDamaged.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				doActive();
			}
		};
		buildingInfoPanel.stateInactive.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				doActive();
			}
		};

		buildingInfoPanel.stateOffline.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				doOffline();
			}
		};
		buildingInfoPanel.repairing.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				doToggleRepair();
			}
		};
		buildingInfoPanel.damaged.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_MEDIUM_2);
				doToggleRepair();
			}
		};
		
		sidebarColonyInfo.onClick = new Action0() {
			@Override
			public void invoke() {
				if (knowledge(planet(), PlanetKnowledge.VISIBLE) > 0) {
					buttonSound(SoundType.GROUNDWAR_TOGGLE_PANEL);
					showInfo = !showInfo;
				}
			}
		};
		upgradePanel = new UpgradePanel();
		
		prev = new UIImageButton(commons.starmap().backwards);
		prev.setHoldDelay(250);
		prev.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_HIGH_2);
				player().movePrevPlanet();
			}
		};
		next = new UIImageButton(commons.starmap().forwards);
		next.setHoldDelay(250);
		next.onClick = new Action0() {
			@Override
			public void invoke() {
				buttonSound(SoundType.CLICK_HIGH_2);
				player().moveNextPlanet();
			}
		};
		
		startBattle.onClick = new Action0() {
			@Override
			public void invoke() {
				doStartBattle();
			}
		};
		
		
		stopUnit = new UIImageTabButton2(commons.spacewar().stop);
		stopUnit.disabledPattern(commons.common().disabledPattern);
//		stopUnit.visible(false);
		stopUnit.onClick = new Action0() {
			@Override
			public void invoke() {
				doStopSelectedUnits();
			}
		};
		
		moveUnit = new UIImageTabButton2(commons.spacewar().move);
		moveUnit.disabledPattern(commons.common().disabledPattern);
		moveUnit.onClick = new Action0() {
			@Override
			public void invoke() {
				selectCommand(moveUnit);
			}
		};
//		moveUnit.visible(false);

		attackUnit = new UIImageTabButton2(commons.spacewar().attack);
		attackUnit.disabledPattern(commons.common().disabledPattern);
//		attackUnit.visible(false);
		attackUnit.onClick = new Action0() {
			@Override
			public void invoke() {
				selectCommand(attackUnit);
			}
		};

		tankPanel = new TankPanel();
		
		zoom = new UIImageButton(commons.colony().zoom) {
			@Override
			public boolean mouse(UIMouse e) {
				zoomDirection = (e.has(Button.LEFT));
				zoomNormal = e.has(Button.MIDDLE);
				return super.mouse(e);
			}
        };
		zoom.setHoldDelay(100);
		zoom.onClick = new Action0() {
			@Override
			public void invoke() {
				if (zoomNormal) {
					render.zoomNormal();
				} else
				if (zoomDirection) {
					render.zoomIn();
				} else {
					render.zoomOut();
				}
			}
		};

		addThis();
	}
	@Override
	public void onResize() {
		base.setBounds(0, 20, getInnerWidth(), getInnerHeight() - 38);
		window.setBounds(base.x + 20, base.y, base.width - 40, base.height);
		
		sidebarBuildings.location(base.x, base.y);
		sidebarBuildingsEmpty.location(base.x, base.y);
		sidebarRadar.location(base.x, base.y + base.height - sidebarRadar.height);
		sidebarRadarEmpty.location(sidebarRadar.location());
		
		sidebarBuildingInfo.location(base.x + base.width - sidebarBuildingInfo.width, base.y);
		sidebarNavigation.location(base.x + base.width - sidebarNavigation.width, base.y + base.height - sidebarNavigation.height);
		sidebarColonyInfo.location(sidebarNavigation.x, sidebarNavigation.y - sidebarColonyInfo.height);
		
		bridge.location(sidebarNavigation.x - bridge.width, base.y + base.height - bridge.height);
		starmap.location(bridge.x - starmap.width, base.y + base.height - starmap.height);
		planets.location(starmap.x - planets.width, base.y + base.height - planets.height);
		colonyInfo.location(planets.x - colonyInfo.width, base.y + base.height - colonyInfo.height);
		startBattle.location(sidebarNavigation.x - startBattle.width, sidebarNavigation.y + sidebarNavigation.height - startBattle.height);

		render.bounds(window.x, window.y, window.width, window.height);
		
		leftFill.bounds(sidebarBuildings.x, sidebarBuildings.y + sidebarBuildings.height, 
				sidebarBuildings.width, sidebarRadar.y - sidebarBuildings.y - sidebarBuildings.height);
		rightFill.bounds(sidebarBuildingInfo.x, sidebarBuildingInfo.y + sidebarBuildingInfo.height, 
				sidebarBuildingInfo.width, sidebarColonyInfo.y - sidebarBuildingInfo.y - sidebarBuildingInfo.height);
		
		radarPanel.location(sidebarRadar.x + sidebarRadar.width - 1, sidebarRadar.y);
		radar.location(radarPanel.x + 13, radarPanel.y + 13);
		buildingsPanel.location(sidebarBuildings.x + sidebarBuildings.width - 1, sidebarBuildings.y);
		buildingInfoPanel.location(sidebarBuildingInfo.x - buildingInfoPanel.width, sidebarBuildingInfo.y);
		
		upgradePanel.location(buildingInfoPanel.x, buildingInfoPanel.y + buildingInfoPanel.height);
		
		prev.location(sidebarRadar.x + sidebarRadar.width + 1, sidebarRadar.y - prev.height - 3);
		next.location(prev.x + prev.width + 2, prev.y);
		zoom.location(prev.x, prev.y - 4 - zoom.height);
	}
	/**
	 * @return the current planet surface or selects one from the player's list.
	 */
	public PlanetSurface surface() {
		if (planet() != null) {
			return planet().surface;
		}
		Planet p = player().moveNextPlanet();
		return p != null ? p.surface : null;
	}
	/** 
	 * Prepare the controls of the buildings panel. 
	 * @param delta to go to the previous or next
	 */
	void setBuildingList(int delta) {
		List<BuildingType> list = commons.world().listBuildings();
		int idx = list.indexOf(building());
		if (list.size() > 0) {
			BuildingType bt;
			if (idx < 0) {
				idx = 0;
				bt = list.get(idx);
				player().currentBuilding = bt;
			} else {
				idx = Math.min(Math.max(0, idx + delta), list.size() - 1);
				bt = list.get(idx);
			}
			if (delta != 0) {
				player().currentBuilding = bt;
			}
			String race = race();
			Tile t = player().currentBuilding.tileset.get(race).normal;
			render.placementRectangle.setSize(t.width + 2, t.height + 2);
			
			buildingsPanel.preview.building = bt.tileset.get(race).preview;
			buildingsPanel.preview.cost = bt.cost;
			buildingsPanel.preview.count = planet().countBuilding(bt);
			buildingsPanel.preview.enabled(planet().canBuild(bt) && planet().owner == player());
			render.placementMode = render.placementMode && planet().canBuild(bt);
			buildingsPanel.build.down = buildingsPanel.build.down && planet().canBuild(bt);
			buildingsPanel.buildingName.text(bt.name);
			buildingsPanel.build.enabled(buildingsPanel.preview.enabled());
			
			if (bt.research != null && delta != 0) {
				research(bt.research);
			}
		} else {
			buildingsPanel.build.enabled(false);
			buildingsPanel.preview.building = null;
		}
		
		boolean b0 = buildingsPanel.buildingDown.visible();
		buildingsPanel.buildingDown.visible(idx < list.size() - 1);
		boolean b1 = buildingsPanel.buildingUp.visible();
		buildingsPanel.buildingUp.visible(idx > 0);
		
		buildingsPanel.buildingDownEmpty.visible(!buildingsPanel.buildingDown.visible());
		buildingsPanel.buildingUpEmpty.visible(!buildingsPanel.buildingUp.visible());
		
		if (b0 != buildingsPanel.buildingDown.visible()) {
			commons.control().tooltipChanged(buildingsPanel.buildingDown);
		}
		if (b1 != buildingsPanel.buildingUp.visible()) {
			commons.control().tooltipChanged(buildingsPanel.buildingUp);
		}
	}
	@Override
	public void placeBuilding(boolean more) {
		if (surface().placement.canPlaceBuilding(render.placementRectangle)
				&& player().money() >= player().currentBuilding.cost
				&& planet().canBuild(player().currentBuilding)
		) {
				
			Building b = planet().build(building().id, race(), render.placementRectangle.x + 1, render.placementRectangle.y - 1);
				
			render.placementMode = more && planet().canBuild(building());
			buildingsPanel.build.down = render.placementMode;

			render.buildingBox = surface().getBoundingRect(b.location);
			doSelectBuilding(b);
			
			buildingInfoPanel.update();
			setBuildingList(0);

			effectSound(SoundType.DEPLOY_BUILDING);
		} else {
			if (player().money() < player().currentBuilding.cost) {
				buttonSound(SoundType.NOT_AVAILABLE);
				
				commons.control().displayError(get("message.not_enough_money"));
			} else
			if (!surface().placement.canPlaceBuilding(render.placementRectangle)) {
				buttonSound(SoundType.NOT_AVAILABLE);
				
				commons.control().displayError(get("message.cant_build_there"));
			} else
			if (!planet().canBuild(player().currentBuilding)) {
				buttonSound(SoundType.NOT_AVAILABLE);
				commons.control().displayError(get("message.cant_build_more"));
			}
		}
	}
	@Override
	public Screens screen() {
		return Screens.COLONY;
	}
	@Override
	public void onEndGame() {
		currentBuilding = null;
		render.buildingBox = null;
		lastSurface = null;
	}
	
//	/** Set the spacewar time controls. */
//	void setGroundWarTimeControls() {
//		commons.replaceSimulation(new Action0() {
//			@Override
//			public void invoke() {
//				doGroundWarSimulation();
//			}
//		},
//		new Func1<SimulationSpeed, Integer>() {
//			@Override
//			public Integer invoke(SimulationSpeed value) {
//				switch (value) {
//				case NORMAL: return SIMULATION_DELAY;
//				case FAST: return SIMULATION_DELAY / 2;
//				case ULTRA_FAST: return SIMULATION_DELAY / 4;
//				default:
//					throw new AssertionError("" + value);
//				}
//			};
//		}
//		);
//	}
//	/** Add guns for the buildings. */
//	void doAddGuns() {
//		guns.clear();
//		for (Building b : planet().surface.buildings.iterable()) {
//			if (b.type.kind.equals("Defensive") && b.isComplete()) {
//				List<BattleGroundTurret> turrets = world().battle.getTurrets(b.type.id, planet().race);
//				int n = b.hitpoints * 2 < b.type.hitpoints ? turrets.size() / 2 : turrets.size();
//				for (int j = 0; j < turrets.size(); j++) {
//					BattleGroundTurret bt = turrets.get(j);
//					GroundwarGun g = new GroundwarGun(bt);
//					g.rx = b.location.x + bt.rx;
//					g.ry = b.location.y + bt.ry;
//					g.building = b;
//					g.owner = planet().owner;
//					g.index = j;
//					g.count = n;
//					guns.add(g);
//				}
//			}
//		}
//	}
//	/** Place various units around the colony hub. */
//	void doAddUnits() {
//		units.clear();
//		unitsAtLocation.clear();
//		unitsForPathfinding.clear();
//		LinkedList<Location> locs = new LinkedList<>();
//		for (Building b : surface().buildings.iterable()) {
//			if (b.type.kind.equals("MainBuilding")) {
//				Set<Location> locations = new HashSet<>();
//				
//				for (int x = b.location.x - 1; x < b.location.x + b.tileset.normal.width + 1; x++) {
//					for (int y = b.location.y + 1; y > b.location.y - b.tileset.normal.height - 1; y--) {
//						Location loc = Location.of(x, y);
//						if (surface().placement.canPlaceBuilding(loc.x, loc.y)) {
//							locations.add(loc);
//						}
//					}
//				}
//				// place enemy units
//				locs.addAll(locations);
//				for (ResearchType rt : world().researches.values()) {
//					if (rt.category == ResearchSubCategory.WEAPONS_VEHICLES
//							|| rt.category == ResearchSubCategory.WEAPONS_TANKS) {
//						BattleGroundVehicle bgv = world().battle.groundEntities.get(rt.id);
//						
//						GroundwarUnit u = new GroundwarUnit(planet().owner == player() ? bgv.normal : bgv.alternative);
//						Location loc = locs.removeFirst();
//						updateUnitLocation(u, loc.x, loc.y, false);
//						
//						u.selected = true;
//						u.owner = planet().owner;
//						u.planet = planet();
//						
//						u.model = bgv;
//						u.hp = u.model.hp;
//						
//						units.add(u);
//					}
//				}
//				
//				break;
//			}
//		}
//		Player enemy = player();
//		if (planet().owner == player()) {
//			for (Player p : world().players.values()) {
//				if (p != player()) {
//					enemy = p;
//					break;
//				}
//			}
//		}
//		for (int d = 0; d < 4; d++) {
//			locs.clear();
//			for (int x = 0; x > -surface().height; x--) {
//				if (surface().placement.canPlaceBuilding(x + d, x - 1 - d)) {
//					locs.add(Location.of(x + d, x - 1 - d));
//				}
//			}
//			for (ResearchType rt : world().researches.values()) {
//				if (rt.category == ResearchSubCategory.WEAPONS_VEHICLES
//						|| rt.category == ResearchSubCategory.WEAPONS_TANKS) {
//					BattleGroundVehicle bgv = world().battle.groundEntities.get(rt.id);
//					GroundwarUnit u = new GroundwarUnit(enemy == player() ? bgv.normal : bgv.alternative);
//					Location loc = locs.removeFirst();
//					updateUnitLocation(u, loc.x, loc.y, false);
//					
//					u.selected = true;
//					u.owner = enemy;
//					u.planet = planet();
//					
//					u.model = bgv;
//					u.hp = u.model.hp;
//					
//					units.add(u);
//				}
//			}
//		}
//		
//	}
	@Override
	public void selectUnitType(int mx, int my, SelectionBoxMode mode) {
		PlanetGround ground = planet().ground;
		for (GroundwarUnit u : ground.units) {
			Rectangle r = render.unitRectangle(u);
			
			render.scaleToScreen(r);

			if (r.contains(mx, my)) {
				for (GroundwarUnit u2 : ground.units) {
					if (u2.owner == u.owner && u2.model.id.equals(u.model.id)) {
						ground.select(u2);
					} else {
						ground.deselect(u2);
					}
				}
				return;
			}
		}
		// select guns of the same building
		for (GroundwarGun g : ground.guns) {
			Rectangle r = render.gunRectangle(g);
			render.scaleToScreen(r);
			
			if (r.contains(mx, my)) {
				for (GroundwarGun g2 : ground.guns) {
					if (g2.building == g.building) {
						ground.select(g2);
					} else {
						ground.deselect(g2);
					}
				}
				return;
			}
		}		
	}
	@Override
	public void selectUnits(SelectionBoxMode mode) {
		PlanetGround ground = planet().ground;
		boolean allowCommands = false; 
		Rectangle sr = render.selectionRectangle();
		if (sr.width < 4 && sr.height < 4) {
			GroundwarUnit u2 = null;
			Rectangle u2r = null;
			ground.deselectAll();
			for (GroundwarUnit u : ground.units) {
				Rectangle r = render.unitRectangle(u);
				render.scaleToScreen(r);
				
				if (r.intersects(sr)) {
					if (u2 == null || U.closerToCenter(sr, r, u2r)) {
						u2 = u;
						u2r = r;
					}
				}
			}			
			GroundwarGun g2 = null;
			for (GroundwarGun g : ground.guns) {
				ground.deselect(g);
				Rectangle r = render.gunRectangle(g);
				render.scaleToScreen(r);

				if (r.intersects(sr)) {
					if (g2 == null || U.closerToCenter(sr, r, u2r)) {
						g2 = g;
					}
				}
			}
			if (u2 != null && g2 == null) {
				allowCommands = u2.owner == player();
				ground.select(u2);
			} else
			if (g2 != null && u2 == null) {
				allowCommands = g2.owner == player();
				ground.select(g2);
			} else
			if (g2 != null && u2 != null) {
				Rectangle r = render.unitRectangle(u2);
				render.scaleToScreen(r);
				
				Rectangle r2 = render.gunRectangle(g2);
				render.scaleToScreen(r2);
				
				if (U.closerToCenter(sr, r, r2)) {
					allowCommands = u2.owner == player();
					ground.select(u2);
				} else {
					allowCommands = g2.owner == player();
					ground.select(g2);
				}
			}
		} else {
			boolean own = false;
			boolean enemy = false;
			
			for (GroundwarUnit u : ground.units) {
				Rectangle r = render.unitRectangle(u);
				
				render.scaleToScreen(r);
			
				
				boolean in = sr.contains(r.x + r.width / 2, r.y + r.height / 2);
				if (mode == SelectionBoxMode.NEW) {
					ground.select(u, in);
				} else
				if (mode == SelectionBoxMode.SUBTRACT) {
					ground.select(u, ground.isSelected(u) && !in);
				} else
				if (mode == SelectionBoxMode.ADD) {
					ground.select(u, ground.isSelected(u) || in);
				}
				
				if (ground.isSelected(u)) {
					own |= u.owner == player();
					enemy |= u.owner != player();
				}
			}
			for (GroundwarGun g : ground.guns) {
				Rectangle r = render.gunRectangle(g);
				render.scaleToScreen(r);
				
				boolean in = sr.contains(r.x + r.width / 2, r.y + r.height / 2);
				if (mode == SelectionBoxMode.NEW) {
					ground.select(g, in);
				} else
				if (mode == SelectionBoxMode.SUBTRACT) {
					ground.select(g, ground.isSelected(g) && !in);
				} else
				if (mode == SelectionBoxMode.ADD) {
					ground.select(g, ground.isSelected(g) || in);
				}
	
				if (ground.isSelected(g)) {
					own |= g.owner == player();
					enemy |= g.owner != player();
				}
			}
			
			// if mixed selection, deselect aliens
			if (own && enemy) {
				for (GroundwarUnit u : ground.units) {
					ground.select(u, ground.isSelected(u) && u.owner == player());
				}			
				for (GroundwarGun g : ground.guns) {
					ground.select(g, ground.isSelected(g) && g.owner == player());
				}			
			}
			allowCommands = own || enemy;
		}
		moveUnit.enabled(allowCommands);
		attackUnit.enabled(allowCommands);
		stopUnit.enabled(allowCommands);
	}
	/**
	 * Cancel movements and attacks of the selected units.
	 */
	void doStopSelectedUnits() {
		if (planet().war.stopSelectedObjects(player())) {
			effectSound(SoundType.NOT_AVAILABLE);
			selectCommand(stopUnit);
		}
		
	}
	/**
	 * Select a command button and prepare modes.
	 * @param component the component to select
	 */
	void selectCommand(UIComponent component) {
		stopUnit.selected = false; //component == stopUnit;
		if (stopUnit.selected) {
			render.attackSelect = false;
			render.moveSelect = false;
		}
		moveUnit.selected = component == moveUnit;
		if (moveUnit.selected) {
			render.attackSelect = false;
			render.moveSelect = true;
		}
		attackUnit.selected = component == attackUnit;
		if (attackUnit.selected) {
			render.attackSelect = true;
			render.moveSelect = false;
		}
		if (component == null) {
			render.attackSelect = false;
			render.moveSelect = false;
		}
	}
	@Override
	public void unitsAttackMove(final int mx, final int my) {
		Location lm = render.getLocationAt(mx, my);
		if (planet().war.attackMoveSelectedUnits(lm, player())) {
			effectSound(SoundType.ACKNOWLEDGE_1);
		}
		selectCommand(null);
	}
	@Override
	public void unitsAttack(final int mx, final int my) {
		Location lm = render.getLocationAt(mx, my);
		Building b = surface().getBuildingAt(lm);
		GroundwarUnit gu = findNearest(mx, my, player());
		if (planet().war.attackSelectedUnits(gu, b, player())) {
			effectSound(SoundType.ACKNOWLEDGE_1);
		}
		selectCommand(null);
	}
	/**
	 * Find the nearest enemy of the given mouse coordinates.
	 * @param mx the mouse coordinates
	 * @param my the mouse coordinates
	 * @param enemyOf the player whose enemies needs to be found
	 * @return the nearest or null if no units nearby
	 */
	GroundwarUnit findNearest(final int mx, final int my, Player enemyOf) {
		PlanetGround ground = planet().ground;
		List<GroundwarUnit> us = new ArrayList<>();
		for (GroundwarUnit u1 : ground.units) {
			if (u1.owner != enemyOf) {
				Rectangle r = render.unitRectangle(u1);
				render.scaleToScreen(r);
				if (r.contains(mx, my)) {
					us.add(u1);
				}
			}
		}
		if (!us.isEmpty()) {
			return Collections.min(us, new Comparator<GroundwarUnit>() {
				@Override
				public int compare(GroundwarUnit o1,
						GroundwarUnit o2) {
					Rectangle r1 = render.unitRectangle(o1);
					render.scaleToScreen(r1);

					double d1 = Math.hypot(mx - r1.x - r1.width / 2d, my - r1.y - r1.height / 2);

					Rectangle r2 = render.unitRectangle(o2);
					render.scaleToScreen(r2);

					double d2 = Math.hypot(mx - r2.x - r2.width / 2d, my - r2.y - r2.height / 2);
					
					return Double.compare(d1, d2);
				}
			});
		}
		return null;
	}
	/**
	 * Initiate a battle with the given settings.
	 * @param battle the battle information
	 */
	public void initiateBattle(BattleInfo battle) {
		// FIXME
//		this.battle = battle;
//		
//		player().currentPlanet = battle.targetPlanet;
//
//		if (!BattleSimulator.groundBattleNeeded(battle.targetPlanet)) {
//			battle.targetPlanet.takeover(battle.attacker.owner);
//			BattleSimulator.applyPlanetConquered(battle.targetPlanet, BattleSimulator.PLANET_CONQUER_LOSS);
//			battle.groundwarWinner = battle.attacker.owner;
//			BattleInfo bi = battle;
//			battle = null;
//			BattlefinishScreen bfs = (BattlefinishScreen)displaySecondary(Screens.BATTLE_FINISH);
//			bfs.displayBattleSummary(bi);
//			return;
//		}
//		
//		setGroundWarTimeControls();
//		
//		player().ai.groundBattleInit(this);
//		nonPlayer().ai.groundBattleInit(this);
//		
//		battlePlacements.clear();
//		battlePlacements.addAll(getDeploymentLocations(planet().owner == player(), false));
//
//		unitsToPlace.clear();
//		boolean atBuildings = planet().owner == player();
//		InventoryItems iis = (atBuildings ? planet() : battle.attacker).inventory();
//		createGroundUnits(atBuildings, iis.iterable(), unitsToPlace);
//		
//		startBattle.visible(true);
//		
//		battle.incrementGroundBattles();
	}
	/**
	 * Start the battle. 
	 */
	void doStartBattle() {
		planet().war.unitsToPlace.clear();
		planet().war.battlePlacements.clear();

//		doAddGuns();
//		deployNonPlayerVehicles();
		
		startBattle.visible(false);
		
		world().scripting.onGroundwarStart(planet().war);
		
		commons.simulation.resume();
	}
	@Override
	public void toggleUnitPlacement(int mx, int my) {
		if (startBattle.visible()) {
			render.deploySpray = false;
			render.undeploySpray = false;
			if (canPlaceUnitAt(mx, my)) {
				placeUnitAt(mx, my);
				render.deploySpray = true;
			} else {
				removeUnitAt(mx, my);
				render.undeploySpray = true;
			}
		}
	}
	/**
	 * Test if the given cell pointed by the mouse coordinate contains a placed unit.
	 * @param mx the mouse X coordinate
	 * @param my the mouse Y coordinate
	 * @return true if unit can be placed
	 */
	boolean canPlaceUnitAt(int mx, int my) {
		Location lm = render.getLocationAt(mx, my);
		return planet().war.battlePlacements.contains(lm);
	}
	@Override
	public void placeUnitAt(int mx, int my) {
		Location lm = render.getLocationAt(mx, my);
		if (planet().war.battlePlacements.contains(lm)) {
			if (!planet().war.unitsToPlace.isEmpty()) {
				GroundwarUnit u = planet().war.unitsToPlace.removeFirst();
				u.x = lm.x;
				u.y = lm.y;
				planet().ground.add(u);
				planet().war.battlePlacements.remove(lm);
			}
		}
	}
	@Override
	public void removeUnitAt(int mx, int my) {
		Location lm = render.getLocationAt(mx, my);
		for (GroundwarUnit u : new ArrayList<>(planet().ground.units)) {
			if (u.owner == player()) {
				if ((int)Math.floor(u.x) == lm.x && (int)Math.floor(u.y) == lm.y) {
					planet().war.battlePlacements.add(lm);
					planet().war.unitsToPlace.addFirst(u);
					planet().ground.remove(u);
				}
			}
		}
	}
	/**
	 * A panel showing the current selected unit(s). 
	 * @author akarnokd, 2012.06.02.
	 */
	class TankPanel extends UIContainer {
		/** Background image. */
		BufferedImage background;
		/** The group buttons. */
		final List<UIImageButton> groupButtons = new ArrayList<>();
		/** Construct the tank panel. */
		public TankPanel() {
			background = commons.colony().tankPanel;
			width = background.getWidth();
			height = background.getHeight() + 22;
			
			for (int i = -1; i < 10; i++) {
				final int j = i;
				
				UIImageButton ib = new UIImageButton(commons.common().shield) {
					@Override
					public void draw(Graphics2D g2) {
						super.draw(g2);
						String s = Integer.toString(j);
						if (j < 0) {
							s = "*";
						}
						commons.text().paintTo(g2, 7, 3, 10, TextRenderer.WHITE, s);
					}
					@Override
					public boolean mouse(UIMouse e) {
						if (e.has(Button.RIGHT) && e.has(Type.DOWN)) {
							if (j >= 0) {
								planet().ground.removeGroup(j);
							} else {
								planet().ground.deselectAll();
							}
							return true;
						}
						return super.mouse(e);
					}
				};
				ib.onClick = new Action0() {
					@Override
					public void invoke() {
						if (j >= 0) {
							planet().ground.recallGroup(j);
						} else {
							planet().ground.selectAll(player());
						}
					}
				};
				groupButtons.add(ib);
				add(ib);
				
				if (i == -1) {
					ib.tooltip(get("battle.selectall.tooltip"));
				} else {
					ib.tooltip(format("battle.selectgroup.tooltip", i));
				}
			}

		}
		@Override
		public void draw(Graphics2D g2) {
			int h0 = height - 22;
			g2.setColor(Color.BLACK);
			Stroke saves = g2.getStroke();
			g2.setStroke(new BasicStroke(2));
			g2.drawRect(-1, -1, width + 2, h0 + 2);
			g2.drawRect(-1, -1, width + moveUnit.width + 4, h0 + 2);
			g2.setStroke(saves);
			
			g2.drawImage(background, 0, 0, null);
			GroundwarUnit u = null;
			int count = 0;
			double sumHp = 0;
			for (GroundwarUnit u2 : planet().ground.selectedUnits) {
				if (u == null) {
					u = u2;
					count++;
					sumHp = u2.hp;
				} else 
				if (u.model.id.equals(u2.model.id)) {
					count++;
					sumHp += u2.hp;
				}
			}
			String n;
			if (u != null) {
				n = world().researches.get(u.model.id).name;
				if (count > 1) {
					n += " x " + count;
				}
				BufferedImage img = u.model.normalStaticImage();
				int iw = (width - img.getWidth()) / 2;
				g2.drawImage(img, iw, 25, null);
				
				double dmg = u.damage();
				commons.text().paintTo(g2, 10, h0 - 25, 7, u.owner.color, 
						format("spacewar.selection.firepower_dps", dmg * count, 
						String.format("%.1f", dmg * count * 1000d / u.model.delay)));
				commons.text().paintTo(g2, 10, h0 - 15, 7, u.owner.color, get("spacewar.selection.defense_values") + ((int)sumHp));
				
			} else {
				n = get("spacewar.ship_status_none");
			}
			int w = commons.text().getTextWidth(10, n);
			int tsize = 10;
			if (w >= width - 10) {
				w = commons.text().getTextWidth(7, n);
				tsize = 7;
			}
			commons.text().paintTo(g2, (width - w) / 2 + 1, 10, tsize, TextRenderer.RED, n);
			commons.text().paintTo(g2, (width - w) / 2, 10, tsize, TextRenderer.YELLOW, n);
			
			Set<Integer> selgr = new HashSet<>(planet().ground.groups.values());
			selgr.add(-1);
			int ibx = 5;
			int ibi = 0;
			for (UIImageButton ib : groupButtons) {
				ib.x = ibx;
				ib.y = tankPanel.height - 20;
				boolean v0 = ib.visible();
				ib.visible(tankPanel.visible() && selgr.contains(ibi - 1));
				if (v0 != ib.visible()) {
					commons.control().moveMouse();
				}
				ibx += 22;
				ibi++;
			}
			super.draw(g2);
		}
	}

	@Override
	public PlanetStatistics update(PlanetSurface surface) {
		if (lastSurface != surface) {
			render.buildingBox = null;
			currentBuilding = null;
			lastSurface = surface;
			render.placementMode = false;
			buildingsPanel.build.down = false;
			upgradePanel.hideUpgradeSelection();
		}
		// check if the AI has removed any building while we were looking at its planet
		if (currentBuilding != null) {
			if (!planet().surface.buildings.contains(currentBuilding)) {
				render.buildingBox = null;
				currentBuilding = null;
				// FIXME more actions?
			}
		}
		
		buildingsPanel.visible(planet().owner == player() 
				&& showBuildingList);
		buildingInfoPanel.visible(planet().owner == player() && showBuildingInfo);
		infoPanel.visible(knowledge(planet(), PlanetKnowledge.NAME) >= 0 && showInfo);
		
		PlanetGround ground = planet().ground;
		
		boolean showTankPanel = (!ground.units.isEmpty() || !ground.guns.isEmpty()) && !startBattle.visible();
		tankPanel.visible(showTankPanel);
		moveUnit.visible(showTankPanel);
		attackUnit.visible(showTankPanel);
		stopUnit.visible(showTankPanel);
		if (buildingsPanel.visible()) {
			tankPanel.location(buildingsPanel.x + buildingsPanel.width, base.y + 2);
			moveUnit.location(tankPanel.x + tankPanel.width + 2, tankPanel.y);
			attackUnit.location(moveUnit.x, moveUnit.y + moveUnit.height);
			stopUnit.location(moveUnit.x, attackUnit.y + attackUnit.height);
		} else {
			tankPanel.location(prev.x - 2, base.y + 2);
			moveUnit.location(tankPanel.x + tankPanel.width + 2, tankPanel.y);
			attackUnit.location(moveUnit.x, moveUnit.y + moveUnit.height);
			stopUnit.location(moveUnit.x, attackUnit.y + attackUnit.height);
		}
		
		// FIXME battle
		starmap.visible(showSidebarButtons /* && battle == null */);
		colonyInfo.visible(showSidebarButtons /* && battle == null */);
		bridge.visible(showSidebarButtons /* && battle == null */);
		planets.visible(showSidebarButtons /* && battle == null */);
		
		/*
		next.visible(battle == null);
		prev.visible(battle == null);
		*/
		
		setBuildingList(0);
		buildingInfoPanel.update();
		PlanetStatistics ps = infoPanel.update();
		
		setTooltip(zoom, "colony.zoom.tooltip");

		Pair<Planet, Planet> pn = player().prevNextPlanet();
		if (pn != null) {
			setTooltip(prev, "colony.prev.tooltip", pn.first.owner.color, pn.first.name());
			setTooltip(next, "colony.next.tooltip", pn.second.owner.color, pn.second.name());
		} else {
			setTooltip(prev, null);
			setTooltip(next, null);
		}
		
		setTooltip(colonyInfo, "colony.info.tooltip");
		setTooltip(planets, "colony.planets.tooltip");
		setTooltip(starmap, "colony.starmap.tooltip");
		setTooltip(bridge, "colony.bridge.tooltip");
		setTooltip(buildingsPanel.buildingUp, "colony.building.prev.tooltip");
		setTooltip(buildingsPanel.buildingDown, "colony.building.next.tooltip");
		setTooltip(buildingsPanel.buildingList, "colony.building.list.tooltip");
		setTooltip(buildingsPanel.build, "colony.building.build.tooltip");
		BuildingType bt = player().currentBuilding;
		if (bt != null) {
			setTooltipText(buildingsPanel.preview, bt.description);
			setTooltipText(buildingsPanel.buildingName, bt.description);
		} else {
			setTooltip(buildingsPanel.preview, null);
			setTooltip(buildingsPanel.buildingName, null); 
		}
		
		setTooltip(sidebarBuildings, "colony.buildings.tooltip");
		setTooltip(sidebarRadar, "colony.radars.tooltip");
		setTooltip(sidebarBuildingInfo, "colony.buildinginfos.tooltip");
		setTooltip(sidebarColonyInfo, "colony.infos.tooltip");
		setTooltip(sidebarNavigation, "colony.buttons.tooltip");
		
		setTooltip(buildingInfoPanel.stateActive, "colony.active.tooltip");
		setTooltip(buildingInfoPanel.stateOffline, "colony.inactive.tooltip");
		setTooltip(buildingInfoPanel.damaged, "colony.damaged.tooltip");
		setTooltip(buildingInfoPanel.repairing, "colony.repairing.tooltip");
		
		return ps;
	}
	@Override
	public void selectBuilding(Building b) {
		doSelectBuilding(b);
		if (currentBuilding != null) {
			buttonSound(SoundType.CLICK_MEDIUM_2);
		}
	}
	@Override
	public void unitsMove(int mx, int my) {
		Location lm = render.getLocationAt(mx, my);
		if (planet().war.moveSelectedUnits(lm, player())) {
			effectSound(SoundType.ACKNOWLEDGE_2);
			selectCommand(null);
		}
	}
}

