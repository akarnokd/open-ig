/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.List;

import hu.openig.core.*;
import hu.openig.mechanics.*;
import hu.openig.model.*;
import hu.openig.render.*;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.panels.WeatherOverlay;
import hu.openig.ui.*;
import hu.openig.ui.UIMouse.*;
import hu.openig.ui.UIMouse.Button;
import hu.openig.utils.*;

/**
 * The planet surface rendering screen.
 * @author akarnokd, 2010.01.11.
 */
public class PlanetScreen extends ScreenBase implements GroundwarWorld {
    /** Indicate if a component is drag sensitive. */
    @Retention(RetentionPolicy.RUNTIME)
    @interface DragSensitive { }
    /**

     * The selected rectangular region. The X coordinate is the smallest, the Y coordinate is the largest
     * the width points to +X and height points to -Y direction
     */
//    Rectangle selectedRectangle;
    /** The selection tile. */
    Tile selection;
    /** The placement tile for allowed area. */
    Tile areaAccept;
    /** The empty tile indicator. */
    Tile areaEmpty;
    /** The area where to deploy. */
    Tile areaDeploy;
    /** The placement tile for denied area. */
    Tile areaDeny;
    /** DEBUG tile showing an area taken up by a unit. */
    Tile areaTaken;
    /** DEBUG tile showing an area reserved for moving into by a unit. */
    Tile areaReserved;
    /** The current cell tile. */
    Tile areaCurrent;
    /** Paved area. */
    BufferedImage areaPaved;
    /** Paved area, may be paved. */
    BufferedImage areaPavedGhost;
    /** Used to place buildings on the surface. */
    final Rectangle placementRectangle = new Rectangle();
    /** The building bounding box. */
    Rectangle buildingBox;
    /** Are we in placement mode? */
    boolean placementMode;
    /** Are we in pavement mode? */
    boolean pavementMode;
    /** The simple blinking state. */
    boolean blink;
    /** The animation index. */
    int animation;
    /** The animation timer. */
    Closeable animationTimer;
    /** The animation timer. */
    Closeable earthQuakeTimer;
    /** The unit selection rectangle start. */
    Point selectionStart;
    /** The unit selection rectangle end. */
    Point selectionEnd;
    /** The surface cell image. */
    static class SurfaceCell {
        /** The tile target. */
        public int a;
        /** The tile target. */
        public int b;
        /** The image to render. */
        public BufferedImage image;
        /** The Y coordinate compensation. */
        public int yCompensation;
    }
    /** The surface cell helper. */
    final SurfaceCell cell = new SurfaceCell();
    /** The last mouse coordinate. */
    int lastX;
    /** The last mouse coordinate. */
    int lastY;
    /** Is the map dragged. */
    boolean drag;
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
    /** The last surface. */
    PlanetSurface lastSurface;
    /** The lighting level. */
    float alpha = 1.0f;
    /** The currently selected building. */
    Building currentBuilding;
    /** The information panel. */
    InfoPanel infoPanel;
    /** The upgrade panel. */
    @DragSensitive
    UpgradePanel upgradePanel;
    /** The colony management side-panel. */
    @DragSensitive
    ColonyManagementPanel colonyManagementPanel;
    /** Go to next planet. */
    @DragSensitive
    UIImageButton prev;
    /** Go to previous planet. */
    @DragSensitive
    UIImageButton next;
    /** Retreat button. */
    @DragSensitive
    UIImageButton retreat;
    /** Confirm retreat. */
    @DragSensitive
    UIImageButton confirmRetreat;
    /** Show the building list panel? */
    boolean showBuildingList = true;
    /** Show the building info panel? */
    boolean showBuildingInfo = true;
    /** Show the information panel. */
    boolean showInfo = true;
    /** Show the screen navigation buttons? */
    boolean showSidebarButtons = true;
    /** The set where the vehicles may be placed. */
    final Set<Location> battlePlacements = new HashSet<>();
    /** The ground war units. */
    final List<GroundwarUnit> units = new ArrayList<>();
    /** The guns. */
    final List<GroundwarGun> guns = new ArrayList<>();
    /** The user is dragging a selection box. */
    boolean selectionMode;
    /** The current animating explosions. */
    Set<GroundwarExplosion> explosions = new HashSet<>();
    /** The active rockets. */
    Set<GroundwarRocket> rockets = new HashSet<>();
    /** The groundwar animation simulator. */
    Closeable simulator;
    /** The direct attack units. */
    final EnumSet<GroundwarUnitType> directAttackUnits = EnumSet.of(
            GroundwarUnitType.ARTILLERY,
            GroundwarUnitType.TANK,
            GroundwarUnitType.ROCKET_SLED,
            GroundwarUnitType.SELF_REPAIR_TANK,
            GroundwarUnitType.KAMIKAZE,
            GroundwarUnitType.PARALIZER,
            GroundwarUnitType.ROCKET_JAMMER
    );
    /** Units to ignore for winner checks. */
    final EnumSet<GroundwarUnitType> winIngoreUnits = EnumSet.of(
            GroundwarUnitType.RADAR,
            GroundwarUnitType.RADAR_JAMMER
    );
    /** The set of units that may attempt to get closer to their target. */
    final EnumSet<GroundwarUnitType> getCloserUnits = EnumSet.of(
            GroundwarUnitType.TANK,

            GroundwarUnitType.KAMIKAZE,
            GroundwarUnitType.SELF_REPAIR_TANK,
            GroundwarUnitType.PARALIZER,
            GroundwarUnitType.ROCKET_JAMMER
    );
    /** The list of remaining units to place. */
    final LinkedList<GroundwarUnit> unitsToPlace = new LinkedList<>();
    /** Start the battle. */
    @DragSensitive
    UIImageButton startBattle;
    /** The simulation delay on normal speed. */
    static final int SIMULATION_DELAY = 100;
    /** The battle information. */
    BattleInfo battle;
    /** The time in sumulations steps during the paralize effect is in progress. */
    static final int PARALIZED_TTL = 15 * 1000 / SIMULATION_DELAY;
    /** The movement handler object responsible for handling ground war unit movements. */
    SimpleWarMovementHandler movementHandler;
    /** A mine. */
    public static class Mine {
        /** The owner. */
        public Player owner;
        /** The damage to inflict. */
        public double damage;
    }
    /** The grouping of structures. */
    final Map<Object, Integer> groups = new HashMap<>();
    /**
     * The mine locations.
     */
    final Map<Location, Mine> mines = new HashMap<>();
    /** Set of minelayers currently placing a mine. */
    final Set<GroundwarUnit> minelayers = new HashSet<>();
    /** Indicate the deploy spray mode. */
    boolean deploySpray;
    /** Indicate the undeploy spray mode. */
    boolean undeploySpray;
    /** Display the commands given to units. */
    boolean showCommand;
    /** Display debug information. */
    boolean showDebug;
    /** Stop the selected units. */
    UIImageTabButton2 stopUnit;
    /** Move the selected units. */
    UIImageTabButton2 moveUnit;
    /** Attack with the selected units. */
    UIImageTabButton2 attackUnit;
    /** The tank panel. */
    @DragSensitive
    TankPanel tankPanel;
    /** Indicate the left click will select an attack target. */
    boolean attackSelect;
    /** Indicate the left click will select a move target. */
    boolean moveSelect;
    /** The zoom button. */
    @DragSensitive
    UIImageButton zoom;
    /** The zoom direction. */
    boolean zoomDirection;
    /** Zoom to normal. */
    boolean zoomNormal;
    /** The weather overlay. */
    WeatherOverlay weatherOverlay;
    /** The weather sound is running? */
    Action0 weatherSoundRunning;
    /** Disable AI unit management. */
    boolean noAI;
    /** Cancel the retreat. */
    boolean cancelRetreat;
    /** Indicates the screen is in preparation mode. */
    private boolean preparingGroundBattle;
    /** Timing action for constant frame redraw. */
    Closeable frameTimer;
    /** The time of the current ground war simulation tick. */
    double currentSimulationTime = 0;
    /** The time of the previous ground war simulation tick. */
    double previousSimulationTime = 0;
    /** Ratio of the frame time since last simulation and simulation time since last simulation step. */
    double simFrameRatio = 0;
    /** The ratio of the X coordinates of the center of the rendering screen and the center of the planet surface. */
    double xViewRatio = 0;
    /** The ratio of the Y coordinates of the center of the rendering screen and the center of the planet surface. */
    double yViewRatio = 0;

    @Override
    public void onFinish() {
        onEndGame();
    }

    @Override
    public boolean keyboard(KeyEvent e) {
        boolean rep = false;
        switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
            render.incrementOffsetY(28);
            rep = true;
            break;
        case KeyEvent.VK_DOWN:
            render.incrementOffsetY(-28);
            rep = true;
            break;
        case KeyEvent.VK_LEFT:
            render.incrementOffsetX(54);
            rep = true;
            break;
        case KeyEvent.VK_RIGHT:
            render.incrementOffsetX(-54);
            rep = true;
            break;
        case KeyEvent.VK_ESCAPE:
            if (placementMode || pavementMode) {
                placementMode = false;
                pavementMode = false;
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
                e.consume();
            }
            break;
        case KeyEvent.VK_D:
            if (e.isControlDown() && e.isShiftDown()) {
                noAI = !noAI;
            } else
            if (e.isControlDown()) {
                if (battle == null) {
                    if (simulator != null) {
                        close0(simulator);
                        simulator = null;
                    }
                    doAddGuns();
                    doAddUnits();
                    movementHandler = new GroundWarMovementHandler(commons.pool, 28, SIMULATION_DELAY, units, surface().width, surface().height, surface().placement);
                    planet().allocation = ResourceAllocationStrategy.BATTLE;
                    startBattle.visible(false);
                    simulator = commons.register(SIMULATION_DELAY, new Action0() {
                        @Override
                        public void invoke() {
                            doGroundWarSimulation();
                        }
                    });
                    rep = true;
                }
            } else {
                doMineLayerDeploy();
                rep = true;
            }
            e.consume();
            break;
        case KeyEvent.VK_C: {
            // toggle battle placements indicator
            if (e.isControlDown() && e.isShiftDown()) {
                if (battlePlacements.isEmpty()) {
                    battlePlacements.addAll(getDeploymentLocations(false, true)); // skip edge always
                    battlePlacements.addAll(getDeploymentLocations(true, true)); // skip edge always
                } else {
                    battlePlacements.clear();
                }
                rep = true;
                e.consume();
            } else
            if (e.isControlDown()) {
                showCommand = !showCommand;
                rep = true;
                e.consume();
            }
            break;
        }
        case KeyEvent.VK_V: {
            if (e.isControlDown() && e.isShiftDown()) {
                showDebug = !showDebug;
                rep = true;
                e.consume();
            }
        }
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
                e.consume();
            }
            break;
        case KeyEvent.VK_B:
            if (buildingsPanel.build.enabled()) {
                buildingsPanel.build.onPress.invoke();
                rep = true;
            }
            break;
        case KeyEvent.VK_P: {
            if (togglePavementMode()) {
                rep = true;
                e.consume();
            }
            break;
        }
        case KeyEvent.VK_S:
            if (!e.isControlDown()) {
                doStopSelectedUnits();
                e.consume();
                rep = true;
            }
            break;
        case KeyEvent.VK_A:
            if (e.isControlDown()) {
                doSelectAll();
            } else {
                selectCommand(attackUnit);
            }
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
                assignGroup(e.getKeyCode() - KeyEvent.VK_0);
                e.consume();
                rep = true;
            }
        }
        if (e.isShiftDown()) {
            if (e.getKeyCode() >= KeyEvent.VK_0 && e.getKeyCode() <= KeyEvent.VK_9) {
                recallGroup(e.getKeyCode() - KeyEvent.VK_0);
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
        for (GroundwarUnit u : units) {
//            if (u.owner != player()) {
//                continue;
//            }
            if (u.selected && u.model.type == GroundwarUnitType.MINELAYER) {
                minelayers.add(u);
            }
        }
    }
    /**
     * Zoom to 100%.
     */
    protected void doZoomNormal() {
        doZoom(1.0 - render.scale);
        askRepaint();
    }
    /**
     * Zoom out by decreasing the scale by 0.1.
     */
    protected void doZoomOut() {
        doZoom(-0.1);
        askRepaint();
    }
    /**
     * Perform a central zoom with a zoom delta.
     * @param scaleDelta the delta
     */
    protected void doZoom(double scaleDelta) {
        double pre = render.scale;

        int ex = render.width / 2;
        int ey = render.height / 2;

        double mx = (ex - render.offsetX) * pre;
        double my = (ey - render.offsetY) * pre;

        render.scale = Math.max(0.1, Math.min(2, render.scale + scaleDelta));

        double mx0 = (ex - render.offsetX) * render.scale;
        double my0 = (ey - render.offsetY) * render.scale;
        double dx = (mx - mx0) / pre;
        double dy = (my - my0) / pre;
        render.offsetX += (int)(dx);
        render.offsetY += (int)(dy);
    }
    /**
     * Zoom in by increasing the scale by 0.1.
     */
    protected void doZoomIn() {
        doZoom(0.1);
        askRepaint();
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
            setViewLocation();
        }
        focused = render;
        moveSelect = false;
        attackSelect = false;
        attackUnit.enabled(false);
        moveUnit.enabled(false);
        stopUnit.enabled(false);
    }

    @Override
    public void onLeave() {

        cancelWeatherSound();
        placementMode = false;
        pavementMode = false;
        buildingsPanel.build.down = false;

        close0(animationTimer);
        animationTimer = null;

        close0(earthQuakeTimer);
        earthQuakeTimer = null;

        clearGroundBattle();

        battle = null;
        close0(simulator);
        simulator = null;
        close0(frameTimer);
        frameTimer = null;

        cancelRetreat = false;
        retreat.visible(false);
        confirmRetreat.visible(false);

        commons.setCursor(Cursors.POINTER);
    }

    /**
     * Clear the fields of ground battle.
     */
    void clearGroundBattle() {
        battlePlacements.clear();
        guns.clear();
        units.clear();
        mines.clear();
        minelayers.clear();
        explosions.clear();
        rockets.clear();
        unitsToPlace.clear();
        groups.clear();
        commons.setCursor(Cursors.POINTER);
        movementHandler = null;
    }

    /**
     * Return the image (strip) representing this surface entry.
     * The default behavior returns the tile strips along its lower 'V' arc.
     * This method to be overridden to handle the case of damaged or in-progress buildings
     * @param se the surface entity
     * @param symbolic display a symbolic tile instead of the actual building image.
     * @param loc1 the the target cell location
     * @param cell the output for the image and the Y coordinate compensation
     */
    public void getImage(SurfaceEntity se, boolean symbolic, Location loc1, SurfaceCell cell) {
        if (se.type != SurfaceEntityType.BUILDING) {
            cell.yCompensation = 27 - se.tile.imageHeight;
            cell.a = loc1.x - se.virtualColumn;
            cell.b = loc1.y + se.virtualRow - se.tile.height + 1;
            if (se.virtualColumn == 0 && se.virtualRow < se.tile.height) {
                se.tile.alpha = alpha;
                cell.image = se.tile.getStrip(se.virtualRow);
                return;
            } else
            if (se.virtualRow == se.tile.height - 1) {
                se.tile.alpha = alpha;
                cell.image = se.tile.getStrip(se.tile.height - 1 + se.virtualColumn);
                return;
            }
            cell.image = null;
            return;
        }
        if (symbolic) {
            Tile tile;
            if (knowledge(planet(), PlanetKnowledge.BUILDING) < 0) {
                tile = se.building.type.minimapTiles.destroyed;
            } else
            if (se.building.isConstructing()) {
                if (se.building.isSeverlyDamaged()) {
                    tile = se.building.type.minimapTiles.constructingDamaged;
                } else {
                    tile = se.building.type.minimapTiles.constructing;
                }
            } else {
                if (se.building.isDestroyed()) {
                    tile = se.building.type.minimapTiles.destroyed;
                } else
                if (se.building.isDamaged()) {
                    tile = se.building.type.minimapTiles.damaged;
                } else
                if (se.building.isOperational()) {
                    tile = se.building.type.minimapTiles.normal;
                } else {
                    tile = se.building.type.minimapTiles.inoperable;
                }
            }

            cell.yCompensation = 27 - tile.imageHeight;
            cell.image = tile.getStrip(0);
            cell.a = loc1.x;
            cell.b = loc1.y;

            return;
        } else
        if (knowledge(planet(), PlanetKnowledge.BUILDING) < 0 && (battle == null || preparingGroundBattle)) {
            Tile tile =  se.building.scaffolding.normal.get(0);
            cell.yCompensation = 27 - tile.imageHeight;
            tile.alpha = alpha;
            cell.image = tile.getStrip(0);
            cell.a = loc1.x;
            cell.b = loc1.y;
            return;
        }

        if (se.building.isConstructing()) {
            Tile tile;
            List<Tile> scaffolding;
            if (se.building.isSeverlyDamaged()) {
                scaffolding = se.building.scaffolding.damaged;
            } else {
                scaffolding = se.building.scaffolding.normal;
            }

            int index0 = (int)(se.building.hitpoints * 1L * scaffolding.size() / se.building.type.hitpoints);
            int area = se.building.width() * se.building.height();

            int index1 = (se.building.width() * se.virtualRow + se.virtualColumn) * scaffolding.size() / area;

            if (se.building.hitpoints * 100 < se.building.type.hitpoints) {
                index0 = 0;
                index1 = 0;
            }

            tile = scaffolding.get((index0 + index1) % scaffolding.size());

            cell.yCompensation = 27 - tile.imageHeight;
            tile.alpha = alpha;
            cell.image = tile.getStrip(0);
            cell.a = loc1.x;
            cell.b = loc1.y;
        } else {
            Tile tile;
            if (se.building.isSeverlyDamaged()) {
                tile = se.building.tileset.damaged;
            } else

            if (se.building.isOperational()) {
                tile = se.building.tileset.normal;
            } else {
                tile = se.building.tileset.nolight;
            }
            cell.yCompensation = 27 - tile.imageHeight;
            cell.a = loc1.x - se.virtualColumn;
            cell.b = loc1.y + se.virtualRow - se.tile.height + 1;
            if (se.virtualColumn == 0 && se.virtualRow < se.tile.height) {
                tile.alpha = alpha;
                cell.image = tile.getStrip(se.virtualRow);
                return;
            } else
            if (se.virtualRow == se.tile.height - 1) {
                tile.alpha = alpha;
                cell.image = tile.getStrip(se.tile.height - 1 + se.virtualColumn);
                return;
            }
            cell.image = null;
        }

    }
    /**
     * Compute the bounding rectangle of the rendered building object.
     * @param loc the location to look for a building.
     * @return the bounding rectangle or null if the target does not contain a building
     */
    public Rectangle getBoundingRect(Location loc) {
        SurfaceEntity se = surface().buildingmap.get(loc);
        if (se != null && se.type == SurfaceEntityType.BUILDING) {
            int a0 = loc.x - se.virtualColumn;
            int b0 = loc.y + se.virtualRow;

            int x = surface().baseXOffset + Tile.toScreenX(a0, b0);
            int y = surface().baseYOffset + Tile.toScreenY(a0, b0 - se.tile.height + 1) + 27;

            return new Rectangle(x, y - se.tile.imageHeight, se.tile.imageWidth, se.tile.imageHeight);
        }
        return null;
    }
    /**
     * Returns the bounding rectangle of the building in non-scaled screen coordinates.
     * @param b the building to test
     * @return the bounding rectangle
     */
    public Rectangle buildingRectangle(Building b) {
        int a0 = b.location.x;
        int b0 = b.location.y;
        int x = surface().baseXOffset + Tile.toScreenX(a0, b0);
        int y = surface().baseYOffset + Tile.toScreenY(a0, b0 - b.tileset.normal.height + 1) + 27;

        return new Rectangle(x, y - b.tileset.normal.imageHeight, b.tileset.normal.imageWidth, b.tileset.normal.imageHeight);
    }
    /**
     * Returns the bounding rectangle of the given surface feature in non-scaled screen coordinates.
     * @param f the surface feature
     * @return the bounding rectangle
     */
    public Rectangle featureRectangle(SurfaceFeature f) {
        int a0 = f.location.x;
        int b0 = f.location.y;
        int x = surface().baseXOffset + Tile.toScreenX(a0, b0);
        int y = surface().baseYOffset + Tile.toScreenY(a0, b0 - f.tile.height + 1) + 27;
        return new Rectangle(x, y - f.tile.imageHeight, f.tile.imageWidth, f.tile.imageHeight);
    }
    /**
     * Return a building instance at the specified location.
     * @param loc the location
     * @return the building object or null
     */
    public Building getBuildingAt(Location loc) {
        SurfaceEntity se = surface().buildingmap.get(loc);
        if (se != null && se.type == SurfaceEntityType.BUILDING) {
            return se.building;
        }
        return null;
    }
    /**
     * The surface renderer component.
     * @author akarnokd, Mar 27, 2011
     */
    class SurfaceRenderer extends UIComponent {
        /** The current scaling factor. */
        double scale = 1;
        /** The offset X. */
        int offsetX;
        /** The offset Y. */
        int offsetY;
        /**
         * Get a location based on the mouse coordinates.
         * @param mx the mouse X coordinate
         * @param my the mouse Y coordinate
         * @return the location
         */
        public Location getLocationAt(int mx, int my) {
            if (surface() != null) {
                double mx0 = mx - (surface().baseXOffset + 28) * scale - offsetX; // Half left
                double my0 = my - (surface().baseYOffset + 27) * scale - offsetY; // Half up
                int a = (int)Math.floor(Tile.toTileX((int)mx0, (int)my0) / scale);
                int b = (int)Math.floor(Tile.toTileY((int)mx0, (int)my0) / scale) ;
                return Location.of(a, b);
            }
            return null;
        }

        /**
         * Increment the rendering screen's X offset by the give amount.
         * @param offset the offset to increment by
         */
        public void incrementOffsetX(int offset) {
            setOffsetX(offsetX + offset);
        }
        /** Set the rendering screen's X offset to the given new offset. Only update the offset
         * if with the new offset the current rendering screen's center is still within the planet surface's
         * area.
         * @param newOffset the offset to increment by
         */
        public void setOffsetX(int newOffset) {
            double testViewRatio = (newOffset - width / 2) / render.scale / surface().boundingRectangle.width;
            if (testViewRatio <= 0 && testViewRatio >= -1) {
                offsetX = newOffset;
                xViewRatio = (offsetX - width / 2) / scale / surface().boundingRectangle.width;
            }
        }
        /**
         * Increment the rendering screen's Y offset by the give amount.
         * @param offset the offset to increment by
         */
        public void incrementOffsetY(int offset) {
            setOffsetY(offsetY + offset);
        }
        /** Set the rendering screen's Y offset to the given new offset. Only update the offset
         * if with the new offset the current rendering screen's center is still within the planet surface's
         * area.
         * @param newOffset the offset to increment by
         */
        public void setOffsetY(int newOffset) {
            double testViewRatio = (newOffset - height / 2) / render.scale / surface().boundingRectangle.height;
            if (testViewRatio <= 0 && testViewRatio >= -1) {
                offsetY = newOffset;
                yViewRatio = (offsetY - height / 2) / render.scale / surface().boundingRectangle.height;
            }
        }
        /**
         * Retrieve the gun at the given location.
         * @param mx the mouse X
         * @param my the mouse Y
         * @return the gun or null if empty
         */
        GroundwarGun gunAt(int mx, int my) {
            Rectangle sel = new Rectangle(0, 0, commons.colony().selectionBoxLight.getWidth(), commons.colony().selectionBoxLight.getHeight());
            for (GroundwarGun g : guns) {
                Rectangle r = gunRectangle(g);
                Rectangle drawRect = new Rectangle((int)(r.x + (r.width - sel.getWidth()) / 2) + offsetX,
                        (int)(r.y + (r.height - sel.getHeight()) / 2) + offsetY,
                        sel.width, sel.height);
                if (drawRect.intersects(new Rectangle(mx, my, 1, 1))) {
                    return g;
                }
            }
            return null;
        }
        /**
         * Get if any unit is selected.
         * @return sum of "flags": -1 enemy unit, -2 enemy gun, 1 own unit, 2 own gun, 0 no selection
         */
        int getSelection() {
            int selection = 0;
            for (GroundwarUnit gwu : units) {
                if (gwu.selected) {
                    selection = (gwu.owner == player()) ? 1 : -1;
                    break;
                }
            }
            for (GroundwarGun gwg : guns) {
                if (gwg.selected) {
                    if (selection == 0) {
                        selection = (gwg.owner == player()) ? 2 : -2;
                        break;
                    } else
                    if (selection > 0 && gwg.owner == player()) {
                        selection += 2;
                        break;
                    } else
                    if (selection < 0 && gwg.owner != player()) {
                        selection += -2;
                        break;
                    }
                }
            }
            return selection;
        }
        @Override
        public boolean mouse(UIMouse e) {
            boolean rep = false;
            switch (e.type) {
            case MOVE:
                if (commons.config().customCursors /*&& battle != null FIXME only if battle*/) {
                    GroundwarUnit overUnit = unitAt(e.x, e.y);
                    GroundwarGun overGun = gunAt(e.x, e.y);
                    int sel = getSelection();
                    //Deployment
                    if (!unitsToPlace.isEmpty()) {
                        if (overUnit != null) {
                            commons.setCursor(Cursors.HAND);
                        } else
                        if (canPlaceUnitAt(e.x, e.y)) {
                            commons.setCursor(Cursors.MOVE);
                        } else {
                            commons.setCursor(Cursors.POINTER);
                        }
                    } else
                    if (sel > 0) {
                        if (overUnit != null && overUnit.owner != player()) {
                            commons.setCursor(Cursors.TARGET);
                        } else
                        if (overGun != null && overGun.owner != player()) {
                            commons.setCursor(Cursors.TARGET);
                        } else
                        if (overUnit == null && overGun == null && buildingAt(e.x, e.y) == null) {
                            commons.setCursor(Cursors.MOVE);
                        } else
                        if (overUnit != null && !overUnit.selected
                                || overGun != null && !overGun.selected) {
                            commons.setCursor(Cursors.HAND);
                        } else {
                            commons.setCursor(Cursors.POINTER);
                        }
                    } else {
                        if (overGun != null || overUnit != null) {
                            commons.setCursor(Cursors.HAND);
                        } else {
                            commons.setCursor(Cursors.POINTER);
                        }
                    }
                }
            case DRAG:
                if (drag || commons.isPanningEvent(e)) {
                    if (!drag) {
                        drag = true;
                        lastX = e.x;
                        lastY = e.y;
                        doDragMode(true);
                    }
                    incrementOffsetX(e.x - lastX);
                    incrementOffsetY(e.y - lastY);

                    lastX = e.x;
                    lastY = e.y;
                    rep = true;
                } else
                if (placementMode) {
                    int mx = e.x;
                    int my = e.y;
                    if (placementRectangle.width % 2 == 0) {
                        mx += 28;
                    }
                    if (placementRectangle.height % 2 == 0) {
                        my += 13;
                    }
                    Location current = getLocationAt(mx, my);
                    if (current != null) {
                        placementRectangle.x = current.x - placementRectangle.width / 2;
                        placementRectangle.y = current.y + placementRectangle.height / 2;
                        rep = true;
                    }
                } else
                if (selectionMode) {
                    selectionEnd = new Point(e.x, e.y);
                    rep = true;
                }
                if (deploySpray) {
                    placeUnitAt(e.x, e.y);
                    rep = true;
                } else
                if (undeploySpray) {
                    removeUnitAt(e.x, e.y);
                    rep = true;
                }
                break;
            case DOUBLE_CLICK:
                if (e.has(Button.LEFT)) {
                    SelectionBoxMode mode = SelectionBoxMode.NEW;
                    if (e.has(Modifier.SHIFT)) {
                        mode = SelectionBoxMode.ADD;
                    } else
                    if (e.has(Modifier.CTRL)) {
                        mode = SelectionBoxMode.SUBTRACT;
                    }
                    doSelectUnitType(e.x, e.y, mode);
                    rep = true;
                }
                break;
            case DOWN:
//                if (battle != null) { FIXME only if battle
                    if (e.has(Button.LEFT)) {
                        if (moveSelect) {
                            doMoveSelectedUnits(e.x, e.y);
                            rep = true;
                            break;
                        } else
                        if (attackSelect) {
                            if (e.has(Modifier.CTRL)) {
                                doAttackMoveSelectedUnits(e.x, e.y);
                            } else {
                                doAttackWithSelectedUnits(e.x, e.y);
                            }
                            rep = true;
                            break;
                        } else {
                            toggleUnitPlacementAt(e.x, e.y);
                        }
                    } else
                    if (e.has(Button.RIGHT)) {
                        if (config.classicControls) {
                            Building b = buildingAt(e.x, e.y);
                            if (b != null) {
                                if (planet().owner == selectionOwner()) {
                                    doMoveSelectedUnits(e.x, e.y);
                                    rep = true;
                                } else {
                                    doAttackWithSelectedUnits(e.x, e.y);
                                    rep = true;
                                }
                            } else {
                                GroundwarUnit u = unitAt(e.x, e.y);
                                if (u != null && u.owner != selectionOwner()) {
                                    doAttackWithSelectedUnits(e.x, e.y);
                                    rep = true;
                                } else {
                                    if (e.has(Modifier.CTRL)) {
                                        doAttackMoveSelectedUnits(e.x, e.y);
                                    } else {
                                        doMoveSelectedUnits(e.x, e.y);
                                    }
                                    rep = true;
                                }
                            }
                        } else {
                            if (e.has(Modifier.SHIFT) && !e.has(Modifier.CTRL)) {
                                doMoveSelectedUnits(e.x, e.y);
                                rep = true;
                            } else

                            if (e.has(Modifier.CTRL) && !e.has(Modifier.SHIFT)) {
                                doAttackWithSelectedUnits(e.x, e.y);
                                rep = true;
                            } else

                            if (e.has(Modifier.CTRL) && e.has(Modifier.SHIFT)) {
                                doAttackMoveSelectedUnits(e.x, e.y);
                                rep = true;
                            } else {
                                drag = true;
                                lastX = e.x;
                                lastY = e.y;
                                doDragMode(true);
                            }
                        }
                    }
//                } else FIXME only if battle
                if (e.has(Button.MIDDLE) && config.classicControls == e.has(Modifier.CTRL)) {
                    centerScreen();
                    scale = 1;
                    rep = true;
                }
                if (e.has(Button.LEFT)) {
                    if (placementMode) {
                        placeBuilding(e.has(Modifier.SHIFT));
                    } else {
                        if (pavementMode) {
                            Location loc = getLocationAt(e.x, e.y);
                            if (!surface().pavements.contains(loc)) {
                                SurfaceFeature se = findSurfaceFeature(loc);
                                if (se != null) {
                                    int price = pavementPrice(se.tile);
                                    if (price <= player().money()) {
                                        player().addMoney(-price);
                                        player().statistics.moneySpent.value += price;
                                        world().statistics.moneySpent.value += price;

                                        paveSurfaceFeature(se);
                                        rep = true;
                                        buttonSound(SoundType.DEPLOY_BUILDING);
                                    } else {
                                        buttonSound(SoundType.FAIL);
                                    }
                                }
                            }
                        }
                        if (knowledge(planet(), PlanetKnowledge.OWNER) >= 0
                                || planet().owner == player()) {
                            Location loc = getLocationAt(e.x, e.y);
                            buildingBox = getBoundingRect(loc);
                            doSelectBuilding(getBuildingAt(loc));
                            if (currentBuilding != null) {
                                buttonSound(SoundType.CLICK_MEDIUM_2);
                            }
                            rep = true;
                            /*
                            if (battlePlacements.contains(loc)) {
                                battlePlacements.remove(loc);
                                System.out.printf("    <exclude x='%d' y='%d'/>\r\n", loc.x, loc.y);
                            }
                            */
                        } else {
                            doSelectBuilding(null);
                        }
//                        if (battle != null && !startBattle.visible()) { FIXME only if battle
                            if (!deploySpray && !undeploySpray) {
                                selectionMode = true;
                                selectionStart = new Point(e.x, e.y);
                                selectionEnd = selectionStart;
                                rep = true;
                                doDragMode(true);
                            }
//                        } FIXME only if battle
                    }
                }
                commons.control().moveMouse();
                break;
            case UP:
                if (e.has(Button.RIGHT) || e.has(Button.MIDDLE)) {
                    drag = false;
                    doDragMode(false);
                }
                if (e.has(Button.LEFT) && selectionMode) {
                    selectionMode = false;
                    selectionEnd = new Point(e.x, e.y);
                    SelectionBoxMode mode = SelectionBoxMode.NEW;
                    if (e.has(Modifier.SHIFT)) {
                        mode = SelectionBoxMode.ADD;
                    } else
                    if (e.has(Modifier.CTRL)) {
                        mode = SelectionBoxMode.SUBTRACT;
                    }
                    selectUnits(mode);
                    doDragMode(false);
                }
                if (e.has(Button.LEFT)) {
                    deploySpray = false;
                    undeploySpray = false;
                }
                rep = true;
                break;
            case LEAVE:
                drag = false;
                doDragMode(false);
                if (selectionMode) {
                    selectionMode = false;
                    selectionEnd = new Point(e.x, e.y);
                    SelectionBoxMode mode = SelectionBoxMode.NEW;
                    if (e.has(Modifier.SHIFT)) {
                        mode = SelectionBoxMode.ADD;
                    } else
                    if (e.has(Modifier.CTRL)) {
                        mode = SelectionBoxMode.SUBTRACT;
                    }
                    selectUnits(mode);
                    rep = true;
                }
                break;
            case WHEEL:
                if (e.has(Modifier.CTRL)) {
                    double pre = scale;
                    int ox = offsetX;
                    int oy = offsetY;
                    double mx = (e.x - ox) / pre;
                    double my = (e.y - oy) / pre;
                    if (e.z < 0) {
                        doZoomIn();
                    } else {
                        doZoomOut();
                    }
                    setOffsetX((int)(e.x - scale * mx));
                    setOffsetY((int)(e.y - scale * my));
                    rep = true;
                } else
                if (e.has(Modifier.SHIFT)) {
                    if (e.z < 0) {
                        incrementOffsetX(54);
                    } else {
                        incrementOffsetX(-54);
                    }
                    rep = true;
                } else {
                    if (e.z < 0) {
                        incrementOffsetY(28 * 3);
                    } else {
                        incrementOffsetY(-28 * 3);
                    }
                    rep = true;
                }
                break;
            default:
            }
            return rep;
        }

        @Override
        public void draw(Graphics2D g2) {
            PlanetSurface surface = surface();
            if (surface == null) {
                return;
            }
            if (!commons.simulation.paused() && battle != null) {
                calculateObjectPositionInterpolation();
            }
            PlanetStatistics ps = update(surface);

            float alphaSave = alpha;
            computeAlpha();
            if (alphaSave != alpha) {
                if (alpha > 0.99) {
                    areaPaved = commons.colony().tilePavement;
                } else {
                    areaPaved = ImageUtils.withNight(commons.colony().tilePavement, alpha, Tile.LIGHT_THRESHOLD);
                }
            }

            RenderTools.setInterpolation(g2, true);

            Shape save0 = g2.getClip();
            g2.clipRect(0, 0, width, height);

            g2.setColor(new Color(96 * alpha / 255, 96 * alpha / 255, 96 * alpha / 255));
            g2.fillRect(0, 0, width, height);

            AffineTransform at = g2.getTransform();
            g2.translate(offsetX, offsetY);
            g2.scale(scale, scale);

            int x0 = surface.baseXOffset;
            int y0 = surface.baseYOffset;

            if (knowledge(planet(), PlanetKnowledge.NAME) >= 0) {

                drawTiles(g2, surface, x0, y0);
                if (battlePlacements.size() > 0) {
                    for (Location loc : battlePlacements) {
                        int x = x0 + Tile.toScreenX(loc.x, loc.y);
                        int y = y0 + Tile.toScreenY(loc.x, loc.y);
                        g2.drawImage(areaDeploy.getStrip(0), x, y, null);
                    }
                }
                if (placementMode) {
                    if (placementRectangle.width > 0) {
                        for (int i = placementRectangle.x; i < placementRectangle.x + placementRectangle.width; i++) {
                            for (int j = placementRectangle.y; j > placementRectangle.y - placementRectangle.height; j--) {

                                BufferedImage img = areaAccept.getStrip(0);
                                // check for existing building
                                if (!surface().placement.canPlaceBuilding(i, j)) {
                                    img = areaDeny.getStrip(0);
                                }

                                int x = x0 + Tile.toScreenX(i, j);
                                int y = y0 + Tile.toScreenY(i, j);
                                g2.drawImage(img, x, y, null);
                            }
                        }
                    }
                }
                drawBattleHelpers(g2, x0, y0);
                if (config.showBuildingName
                        && (knowledge(planet(), PlanetKnowledge.BUILDING) >= 0
                        || (battle != null && !preparingGroundBattle && !battle.isGroundwarComplete()))) {
                    drawBuildingHelpers(g2, surface);
                }
                if (pavementMode) {
                    for (SurfaceFeature se : surface.features) {
                        if (!surface.pavements.contains(se.location)) {

                            int price = pavementPrice(se.tile);
                            String priceStr = String.format("%,d cr", price);
                            int priceWidth = commons.text().getTextWidth(10, priceStr);

                            int px1 = x0 + Tile.toScreenX(se.location.x, se.location.y);
                            int px2 = x0 + Tile.toScreenX(se.location.x + se.tile.width, se.location.y - se.tile.height);
                            int py1 = y0 + Tile.toScreenY(se.location.x + se.tile.width, se.location.y);
                            int py2 = y0 + Tile.toScreenY(se.location.x - 1, se.location.y - se.tile.height - 1);
                            int pc = 0xFF80FF80;
                            if (price > player().money()) {
                                pc = 0xFFFF4040;
                            }
                            int px0 = px1 + (px2 - px1 - priceWidth) / 2;
                            int py0 = py1 + (py2 - py1 - 10) / 2;

                            if (config.buildingTextBackgrounds) {
                                Composite compositeSave = g2.getComposite();
                                AlphaComposite a1 = AlphaComposite.SrcOver.derive(0.5f);
                                g2.setComposite(a1);
                                g2.setColor(Color.BLACK);
                                g2.fillRect(px0 - 2, py0 - 2, priceWidth + 4, 14);
                                g2.setComposite(compositeSave);
                            }

                            commons.text().paintTo(g2, px0, py0, 10, pc, priceStr);
                        }
                    }
                }
                // paint red on overlapping images of buildings, land-features and vehicles

                if (!units.isEmpty()) {
                    drawHiddenUnitIndicators(g2);
                }

                drawWeather(g2, surface);

                if (knowledge(planet(), PlanetKnowledge.OWNER) >= 0 && buildingBox != null) {
                    g2.setColor(Color.RED);
                    g2.drawRect(buildingBox.x, buildingBox.y, buildingBox.width, buildingBox.height);
                }

                g2.setTransform(at);
                g2.setColor(Color.BLACK);

                String pn = planet().name();
                int nameHeight = 14;
                int nameWidth = commons.text().getTextWidth(nameHeight, pn);
                int nameLeft = (width - nameWidth) / 2;
                g2.fillRect(nameLeft - 5, 0, nameWidth + 10, nameHeight + 4);

                int pc = TextRenderer.GRAY;
                if (knowledge(planet(), PlanetKnowledge.OWNER) >= 0 && planet().owner != null) {
                    pc = planet().owner.color;
                }
                commons.text().paintTo(g2, nameLeft, 2, nameHeight, pc, pn);

                if (ps != null && planet().owner == player()) {
                    renderProblems(g2, ps);
                }
            } else {
                g2.setTransform(at);

                g2.setColor(new Color(0, 0, 0, 128));

                String installSatellite = get("planet.install_satellite_1");
                int tw = commons.text().getTextWidth(14, installSatellite);
                int tx = (width - tw) / 2;
                int ty = (height - 14) / 2 - 12;
                g2.fillRect(tx - 5, ty - 5, tw + 10, 24);
                commons.text().paintTo(g2, tx, ty, 14, TextRenderer.WHITE, installSatellite);

                installSatellite = get("planet.install_satellite_2");
                tw = commons.text().getTextWidth(14, installSatellite);
                tx = (width - tw) / 2;
                ty = (height - 14) / 2 + 12;
                g2.fillRect(tx - 5, ty - 5, tw + 10, 24);
                commons.text().paintTo(g2, tx, ty, 14, TextRenderer.WHITE, installSatellite);
            }

            if (selectionMode) {
                g2.setColor(new Color(255, 255, 255, 128));
                g2.fill(selectionRectangle());
            }

            g2.setClip(save0);
            RenderTools.setInterpolation(g2, false);

            if (prev.visible() && next.visible()) {
                g2.setColor(Color.BLACK);
                g2.fillRect(prev.x - this.x - 2, zoom.y - this.y - 2,
                        prev.width + next.width + 6, prev.height + zoom.height + 8);
            } else
            if (zoom.visible()) {
                g2.fillRect(prev.x - this.x - 2, zoom.y - this.y - 2,
                        zoom.width + 4, zoom.height + 2);
            }
            if (retreat.visible() || confirmRetreat.visible()) {
                g2.fillRect(retreat.x - this.x - 2, retreat.y - this.y - 2,
                        retreat.width + 5, retreat.height + 2);
            }
            if (battle != null && preparingGroundBattle) {
                drawNextVehicleToDeploy(g2);
            }
        }
        /** Cost of paving a surface location. */
        int pavementPrice(Tile tile) {
            return 5000 * tile.width * tile.height;
        }
        SurfaceFeature findSurfaceFeature(Location loc) {
            for (SurfaceFeature sf : surface().features) {
                if (loc.x >= sf.location.x && loc.y <= sf.location.y
                        && loc.x < sf.location.x + sf.tile.width
                        && loc.y > sf.location.y - sf.tile.height) {
                    return sf;
                }
            }
            return null;
        }

        void paveSurfaceFeature(SurfaceFeature sf) {
            for (int i = 0; i < sf.tile.width; i++) {
                for (int j = 0; j < sf.tile.height; j++) {
                    surface().pavements.add(Location.of(sf.location.x + i, sf.location.y - j));
                }
            }
        }
        /**
         * Draw the next vehicle's image to deploy.
         * @param g2 the graphics context
         */
        void drawNextVehicleToDeploy(Graphics2D g2) {
            if (!unitsToPlace.isEmpty()) {
                BufferedImage img = commons.colony().smallInfoPanel;
                int x = buildingsPanel.x;
                int y = buildingsPanel.y;
                int w = img.getWidth();
                int h = img.getHeight();
                g2.drawImage(img, x, y, null);

                String s = unitsToPlace.size() + " / " + (unitsToPlace.size() + units.size());
                int sx = x + (w - commons.text().getTextWidth(7, s)) / 2;
                commons.text().paintTo(g2, sx, y + 10, 7, TextRenderer.YELLOW, s);

                GroundwarUnit u = unitsToPlace.getFirst();

                BufferedImage ui = u.staticImage();
                int ux = x + (w - ui.getWidth()) / 2;
                int uy = y + 24;

                g2.drawImage(ui, ux, uy, null);

                s = u.item.type.name;
                sx = x + (w - commons.text().getTextWidth(7, s)) / 2;
                commons.text().paintTo(g2, sx, y + h - 14, 7, TextRenderer.YELLOW, s);
            }
        }
        /**
         * Draw hidden unit indicator red pixels.
         * @param g2 the graphics context
         */
        void drawHiddenUnitIndicators(Graphics2D g2) {
            for (GroundwarUnit u : units) {
                Rectangle ur = unitRectangle(u);

                // compensate rectangle to have only the trimmed image
                BufferedImage bi = u.get();
                int tx = (u.model.width - bi.getWidth()) / 2;
                int ty = (u.model.height - bi.getHeight()) / 2;

                ur.x += tx;
                ur.y += ty;
                ur.width = bi.getWidth();
                ur.height = bi.getHeight();

                for (Building b : surface().buildings.iterable()) {
                    if (u.y <= b.location.y - b.tileset.normal.height
                            || u.x < b.location.x
                            ) {
                        continue;
                    }
                    Rectangle bur = buildingRectangle(b);
                    if (ur.intersects(bur)) {
                        Rectangle is = ur.intersection(bur);
                        BufferedImage ci = collisionImage(u, ur, b.tileset.normal, bur, is);
                        if (ci != null) {
                            g2.drawImage(ci, is.x, is.y, null);
                        }
                    }
                }
                for (SurfaceFeature sf : surface().features) {
                    if (surface().pavements.contains(sf.location)) {
                        continue;
                    }
                    if (sf.tile.width > 1 || sf.tile.height > 1) {
                        if (u.y <= sf.location.y - sf.tile.height
                                || u.x < sf.location.x
                                ) {
                            continue;
                        }
                        Rectangle fur = featureRectangle(sf);
                        if (ur.intersects(fur)) {
                            Rectangle is = ur.intersection(fur);
                            BufferedImage ci = collisionImage(u, ur, sf.tile, fur, is);
                            if (ci != null) {
                                g2.drawImage(ci, is.x, is.y, null);
                            }
                        }
                    }
                }
            }
        }
        /**
         * Draw building name, upgrade level, damage and allocation status.
         * @param g2 the graphics context
         * @param surface the surface object
         */
        void drawBuildingHelpers(Graphics2D g2, PlanetSurface surface) {
            for (Building b : surface.buildings.iterable()) {
                Rectangle r = getBoundingRect(b.location);
                if (r == null) {
                    // workaround for changed footprints with the new custom alien structures introduced in 0.95.242
                    continue;
                }
                int nameSize = 10;
                int nameLen = commons.text().getTextWidth(nameSize, b.type.name);
                int h = (r.height - nameSize) / 2;
                int nx = r.x + (r.width - nameLen) / 2;
                int ny = r.y + h;

                Composite compositeSave = null;
                Composite a1 = null;

                if (config.buildingTextBackgrounds) {
                    compositeSave = g2.getComposite();
                    a1 = AlphaComposite.SrcOver.derive(0.8f);
                    g2.setComposite(a1);
                    g2.setColor(Color.BLACK);
                    g2.fillRect(nx - 2, ny - 2, nameLen + 4, nameSize + 5);
                    g2.setComposite(compositeSave);
                }

                commons.text().paintTo(g2, nx + 1, ny + 1, nameSize, 0xFF8080FF, b.type.name);
                commons.text().paintTo(g2, nx, ny, nameSize, 0xD4FC84, b.type.name);

                // paint upgrade level indicator
                int uw = b.upgradeLevel * commons.colony().upgrade.getWidth();
                int ux = r.x + (r.width - uw) / 2;
                int uy = r.y + h - commons.colony().upgrade.getHeight() - 4;

                String percent = null;
                int color = 0xFF8080FF;
                if (b.isConstructing()) {
                    percent = (b.buildProgress * 100 / b.type.hitpoints) + "%";
                } else
                if (b.hitpoints < b.type.hitpoints) {
                    percent = ((b.type.hitpoints - b.hitpoints) * 100 / b.type.hitpoints) + "%";
                    if (!blink) {
                        color = 0xFFFF0000;
                    }
                }
                if (percent != null) {
                    int pw = commons.text().getTextWidth(10, percent);
                    int px = r.x + (r.width - pw) / 2;
                    int py = uy - 14;

                    if (config.buildingTextBackgrounds) {
                        g2.setComposite(a1);
                        g2.setColor(Color.BLACK);
                        g2.fillRect(px - 2, py - 2, pw + 4, 15);
                        g2.setComposite(compositeSave);
                    }

                    commons.text().paintTo(g2, px + 1, py + 1, 10, color, percent);
                    commons.text().paintTo(g2, px, py, 10, 0xD4FC84, percent);
                }
                if (knowledge(planet(), PlanetKnowledge.BUILDING) >= 0) {
                    for (int i = 1; i <= b.upgradeLevel; i++) {
                        g2.drawImage(commons.colony().upgrade, ux, uy, null);
                        ux += commons.colony().upgrade.getWidth();
                    }

                    if (b.enabled) {
                        int ey = r.y + h + 11;
                        int w = 0;
                        if (b.isEnergyShortage()) {
                            w += commons.colony().unpowered[0].getWidth();
                        }
                        if (b.isWorkerShortage()) {
                            w += commons.colony().worker[0].getWidth();
                        }
                        if (b.repairing) {
                            w += commons.colony().repair[0].getWidth();
                        }
                        int ex = r.x + (r.width - w) / 2;

                        // paint power shortage
                        if (b.isEnergyShortage()) {
                            g2.drawImage(commons.colony().unpowered[blink ? 0 : 1], ex, ey, null);
                            ex += commons.colony().unpowered[0].getWidth();
                        }
                        if (b.isWorkerShortage()) {
                            g2.drawImage(commons.colony().worker[blink ? 0 : 1], ex, ey, null);
                            ex += commons.colony().worker[0].getWidth();
                        }
                        if (b.repairing) {
                            g2.drawImage(commons.colony().repair[(animation % 3)], ex, ey, null);
                            ex += commons.colony().repair[0].getWidth();
                        }
                    } else {
                        int ey = r.y + h + 13;
                        String offline = get("buildings.offline");
                        int w = commons.text().getTextWidth(10, offline);
                        color = 0xFF8080FF;
                        if (!blink) {
                            color = 0xFFFF0000;
                        }
                        int ex = r.x + (r.width - w) / 2;
                        if (config.buildingTextBackgrounds) {
                            g2.setComposite(a1);
                            g2.setColor(Color.BLACK);
                            g2.fillRect(ex - 2, ey - 2, w + 4, 15);
                            g2.setComposite(compositeSave);
                        }

                        commons.text().paintTo(g2, ex + 1, ey + 1, 10, color, offline);
                        commons.text().paintTo(g2, ex, ey, 10, 0xD4FC84, offline);

                        if (b.repairing) {
                            g2.drawImage(commons.colony().repair[(animation % 3)], ex + w + 3, ey, null);
                        }

                    }
                }
            }
        }
        /**
         * Draw battle helper objects such as paths and attack targets.
         * @param g2 the graphics context
         * @param x0 the base x offset
         * @param y0 the base y offset
         */
        void drawBattleHelpers(Graphics2D g2, int x0, int y0) {
            // unit selection boxes}
            BufferedImage selBox = commons.colony().selectionBoxLight;
            for (GroundwarUnit u : units) {
                if (showDebug) {
                    g2.setColor(Color.WHITE);
                    for (int i = 0; i < u.path.size() - 1; i++) {
                        Location l0 = u.path.get(i);
                        Location l1 = u.path.get(i + 1);

                        int xa = x0 + Tile.toScreenX(l0.x, l0.y) + 27;
                        int ya = y0 + Tile.toScreenY(l0.x, l0.y) + 14;
                        int xb = x0 + Tile.toScreenX(l1.x, l1.y) + 27;
                        int yb = y0 + Tile.toScreenY(l1.x, l1.y) + 14;

                        g2.drawLine(xa, ya, xb, yb);
                    }
                }
                if (u.selected && showCommand && (showDebug || u.owner == player())) {
                    Point up = centerOf(u);
                    if (u.attackBuilding != null) {
                        Point gp = centerOf(u.attackBuilding);
                        g2.setColor(Color.RED);
                        g2.drawLine(gp.x, gp.y, up.x, up.y);

                    } else
                    if (u.attackUnit != null) {
                        Point gp = centerOf(u.attackUnit);
                        g2.setColor(Color.RED);
                        g2.drawLine(gp.x, gp.y, up.x, up.y);
                    } else
                    if (u.path.size() > 0) {
                        g2.setColor(u.attackMove != null ? Color.ORANGE : Color.GREEN);
                        Location gl = u.path.peekLast();
                        int gx = x0 + Tile.toScreenX(gl.x, gl.y) + 27;
                        int gy = y0 + Tile.toScreenY(gl.x, gl.y) + 14;
                        g2.drawLine(gx, gy, up.x, up.y);
                    }
                }
                if (u.selected) {
                    Point p = unitPosition(u);
                    g2.drawImage(selBox, p.x, p.y, null);
                }
            }
            for (GroundwarGun g : guns) {
                if (g.selected) {
                    Rectangle r = gunRectangle(g);
                    g2.drawImage(selBox, r.x + (r.width - selBox.getWidth()) / 2, r.y + (r.height - selBox.getHeight()) / 2, null);
                }
            }
        }
        /**
         * Render the weather effects.
         * @param g2 the graphics context
         * @param surface the current surface
         */
        void drawWeather(Graphics2D g2, PlanetSurface surface) {
            if (config.allowWeather) {
                if (planet().weatherTTL > 0 && planet().type.weatherDrop != null) {
                    weatherOverlay.updateBounds(surface.boundingRectangle.width, surface.boundingRectangle.height);
                    weatherOverlay.type = planet().type.weatherDrop;
                    weatherOverlay.alpha = alpha + 0.3;
                    weatherOverlay.draw(g2);
                }
            }
        }
        /**
         * Draw surface tiles and battle units.
         * @param g2 the graphics context
         * @param surface the surface object
         * @param x0 the base x offset
         * @param y0 the base y offset
         */
        void drawTiles(Graphics2D g2, PlanetSurface surface, int x0, int y0) {
            Rectangle br = surface.boundingRectangle;
            g2.setColor(Color.YELLOW);
            g2.drawRect(br.x, br.y, br.width, br.height);

            Rectangle renderingWindow = new Rectangle(0, 0, width, height);
            for (int i = 0; i < surface.renderingOrigins.size(); i++) {
                Location loc = surface.renderingOrigins.get(i);
                for (int j = 0; j < surface.renderingLength.get(i) + 2; j++) {
                    int x = x0 + Tile.toScreenX(loc.x - j, loc.y);
                    Location loc1 = Location.of(loc.x - j, loc.y);
                    boolean isPaved = surface.pavements.contains(loc1);
                    boolean isBuilding = true;
                    SurfaceEntity se = surface.buildingmap.get(loc1);
                    if (se == null || knowledge(planet(), PlanetKnowledge.OWNER) < 0) {
                        se = surface.basemap.get(loc1);
                        isBuilding = false;
                    }
                    if (se != null) {
                        getImage(se, false, loc1, cell);
                        int yref = y0 + Tile.toScreenY(cell.a, cell.b) + cell.yCompensation;
                        if (renderingWindow.intersects(x * scale + offsetX, yref * scale + offsetY, 57 * scale, se.tile.imageHeight * scale)) {
                            if (isPaved && !isBuilding) {
                                int yp = y0 + Tile.toScreenY(loc1.x, loc1.y);
                                g2.drawImage(areaPaved, x, yp, null);
                            } else {
                                if (cell.image != null) {
                                    g2.drawImage(cell.image, x, yref, null);
                                }
                            }
                        }
                        // add smoke
                        if (se.building != null) {
                            drawBuildingSmokeFire(g2, x0, y0, loc1, se);
                        }
                        // place guns on buildings or roads
                        if ((se.building != null
                                && "Defensive".equals(se.building.type.kind))
                                || se.type == SurfaceEntityType.ROAD) {
                            drawGuns(g2, loc.x - j, loc.y);
                        }
                    }

                    if (showDebug) {
                        int y = y0 + Tile.toScreenY(loc1.x, loc1.y);
                        Graphics2D gt = (Graphics2D) g2.create();
                        Font font = new Font("Serif", Font.PLAIN, 6);
                        gt.setFont(font);
                        gt.setColor(Color.GREEN);
                        gt.drawString(loc1.x + "," + loc1.y, x + 20, y + 15);
                        if (movementHandler != null) {
                            gt.setColor(Color.WHITE);
                            gt.drawString(String.valueOf(movementHandler.pathWeightMap.weightMap[loc1.x + movementHandler.pathWeightMap.offsetX][loc1.y + movementHandler.pathWeightMap.offsetY]), x + 20, y + 20);
                            if (movementHandler.reservedCells.get(Location.of(loc.x - j, loc.y)) != null) {
                                //add slight offset, so it can be seen if a tile is both reserved and taken
                                g2.drawImage(areaReserved.getStrip(0), x, y + 5, null);
                            }
                        }
                        gt.dispose();
                    }
                }
            }
//          if (battle != null) { FIXME during battle only
            drawMine(g2);
            drawUnits(g2);
            drawExplosions(g2);
            drawRockets(g2);
//                    } FIXME during battle only
            if (pavementMode) {
                for (SurfaceFeature loc : surface.features) {
                    if (!surface.pavements.contains(loc.location)) {
                        for (int i = 0; i < loc.tile.width; i++) {
                            for (int j = 0; j < loc.tile.height; j++) {
                                int xp = x0 + Tile.toScreenX(loc.location.x + i, loc.location.y - j);
                                int yp = y0 + Tile.toScreenY(loc.location.x + i, loc.location.y - j);
                                g2.drawImage(areaPavedGhost, xp, yp, null);
                            }
                        }
                    }
                }
            }
        }
        /**
         * Draws the smoke and fire on damaged buildings.
         * @param g2 the graphics context
         * @param x0 the render origin
         * @param y0 the render origin
         * @param loc1 the cell location
         * @param se the surface entity
         */
        void drawBuildingSmokeFire(Graphics2D g2, int x0, int y0,
                Location loc1, SurfaceEntity se) {
            int dr = se.building.hitpoints * 100 / se.building.type.hitpoints;
            if (dr < 100 && se.building.isComplete()) {
                if (se.virtualColumn == 0 && se.virtualRow + 1 == se.building.height()) {
                    int len = se.building.width() * se.building.height();
                    int nsmoke;
                    if (dr < 50) {
                        nsmoke = (50 - dr) / 5 + 1;
                    } else {
                        nsmoke = (100 - dr) / 10 + 1;
                    }

                    double sep = 1.0 * len  / (nsmoke + 1);
                    int cnt = 0;
                    for (double sj = sep; sj < len; sj += sep) {
                        int si = (int)Math.round(sj);
                        if (si < len) {
                            Point zz = deZigZag(si, se.building.width(), se.building.height());

                            int mix = Math.abs(loc1.x + zz.x) + Math.abs(loc1.y + zz.y) + animation;
                            BufferedImage[] animFrames;
                            if (dr < 50) {
                                if ((cnt % 3) == (mix / 320 % 3)) {
                                    animFrames = commons.colony().buildingSmoke;
                                } else {
                                    animFrames = commons.colony().buildingFire;
                                }
                            } else {
                                animFrames = commons.colony().buildingSmoke;
                            }

                            BufferedImage smokeFire = animFrames[mix % animFrames.length];

                            int smx = x0 + Tile.toScreenX(loc1.x + se.virtualColumn + zz.x, loc1.y + se.virtualRow - zz.y);
                            int smy = y0 + Tile.toScreenY(loc1.x + se.virtualColumn + zz.x, loc1.y + se.virtualRow - zz.y);
                            int dx = 27 - smokeFire.getWidth() / 2;
                            g2.drawImage(smokeFire, smx + dx, smy - 14, null);
                        }
                        cnt++;
                    }
                }
            }
        }
        /**
         * Render any mine in the specified location.
         * @param g2 the graphics context
         */
        void drawMine(Graphics2D g2) {
            int x0 = planet().surface.baseXOffset;
            int y0 = planet().surface.baseYOffset;
            for (Location minedLoc : mines.keySet()) {
                Mine m = mines.get(minedLoc);
                if (m != null) {
                    int px = (x0 + Tile.toScreenX(minedLoc.x, minedLoc.y));
                    int py = (y0 + Tile.toScreenY(minedLoc.x, minedLoc.y));
                    BufferedImage bimg = commons.colony().mine[0][0];
                    g2.drawImage(bimg, px + 21, py + 12, null);
                }
            }
        }
        /**
         * Render the problem icons.
         * @param g2 the graphics
         * @param ps the statistics
         */
        void renderProblems(Graphics2D g2, PlanetStatistics ps) {
            Set<PlanetProblems> combined = new HashSet<>();
            combined.addAll(ps.problems);
            combined.addAll(ps.warnings);

            if (combined.size() > 0) {
                int w = combined.size() * 11 - 1;
                g2.setColor(Color.BLACK);
                g2.fillRect((width - w) / 2 - 2, 18, w + 4, 15);
                int i = 0;
                for (PlanetProblems pp : combined) {
                    BufferedImage icon = null;
                    BufferedImage iconDark = null;
                    switch (pp) {
                    case HOUSING:
                        icon = commons.common().houseIcon;
                        iconDark = commons.common().houseIconDark;
                        break;
                    case FOOD:
                        icon = commons.common().foodIcon;
                        iconDark = commons.common().foodIconDark;
                        break;
                    case HOSPITAL:
                        icon = commons.common().hospitalIcon;
                        iconDark = commons.common().hospitalIconDark;
                        break;
                    case ENERGY:
                        icon = commons.common().energyIcon;
                        iconDark = commons.common().energyIconDark;
                        break;
                    case WORKFORCE:
                        icon = commons.common().workerIcon;
                        iconDark = commons.common().workerIconDark;
                        break;
                    case STADIUM:
                        icon = commons.common().stadiumIcon;
                        iconDark = commons.common().stadiumIconDark;
                        break;
                    case VIRUS:
                        icon = commons.common().virusIcon;
                        iconDark = commons.common().virusIconDark;
                        break;
                    case REPAIR:
                        icon = commons.common().repairIcon;
                        iconDark = commons.common().repairIconDark;
                        break;
                    case COLONY_HUB:
                        icon = commons.common().colonyHubIcon;
                        iconDark = commons.common().colonyHubIconDark;
                        break;
                    case POLICE:
                        icon = commons.common().policeIcon;
                        iconDark = commons.common().policeIconDark;
                        break;
                    case FIRE_BRIGADE:
                        icon = commons.common().fireBrigadeIcon;
                        iconDark = commons.common().fireBrigadeIcon;
                        break;
                    default:
                    }
                    if (ps.hasProblem(pp)) {
                        g2.drawImage(icon, (width - w) / 2 + i * 11, 20, null);
                    } else
                    if (ps.hasWarning(pp)) {
                        g2.drawImage(iconDark, (width - w) / 2 + i * 11, 20, null);
                    }
                    i++;
                }
            }

        }

    }
    /** The cached zigzag map. */
    static final Map<Pair<Integer, Integer>, Pair<int[], int[]>> ZIGZAGS = new HashMap<>();
    /**
     * Compute the X, Y coordinates of the linear address in the zig-zagged coordinate system
     * enclosed by a rectangle of width and height.
     * @param linear the linear address, &lt; width * height;
     * @param width the width of the enclosing rectangle
     * @param height the height of the rectangle
     * @return the point
     */
    static Point deZigZag(int linear, int width, int height) {

        Pair<Integer, Integer> key = Pair.of(width, height);

        int[] xs;
        int[] ys;

        Pair<int[], int[]> value = ZIGZAGS.get(key);
        if (value == null) {
            int wh = width * height;

            xs = new int[wh];
            ys = new int[wh];

            int base = width <= height ? width : height;
            int s = 0;
            for (int i = 0; i < base; i++) {
                int s2 = s + i + 1;
                for (int j = s; j < s2; j++) {
                    xs[j] = i - j + s;
                    ys[j] = j - s;

                    xs[wh - 1 - j] = (width - 1) - xs[j];
                    ys[wh - 1 - j] = (height - 1) - ys[j];
                }
                s = s2;
            }
            if (width <= height) {
                for (int i = 0; i < height - width - 1; i++) {
                    for (int j = 0; j < width; j++) {
                        xs[s] = width - 1 - j;
                        ys[s] = i + 1 + j;
                        s++;
                    }
                }
            } else {
                for (int i = 0; i < width - height - 1; i++) {
                    for (int j = 0; j < height; j++) {
                        xs[s] = height + i - j;
                        ys[s] = j;
                        s++;
                    }
                }
            }
            ZIGZAGS.put(key, Pair.of(xs, ys));
        } else {
            xs = value.first;
            ys = value.second;
        }
        return new Point(xs[linear], ys[linear]);
    }

    /**
     * The radar renderer component.
     * @author akarnokd, Mar 27, 2011
     */
    class RadarRender extends UIComponent {
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
                            getImage(se, true, loc1, cell);
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
            for (GroundwarUnit u : units) {
                if (u.model.type == GroundwarUnitType.RADAR_JAMMER

                        && u.owner != player() && u.paralizedTTL == 0) {
                    jammed = true;
                    break;
                }
            }

            if (!jammed) {
                for (GroundwarUnit u : units) {
                    if (blink) {
                        int px = (int)(x0 + Tile.toScreenX(u.x + 0.5, u.y - 0.5)) - 11;
                        int py = (int)(y0 + Tile.toScreenY(u.x + 0.5, u.y - 0.5));

                        g2.setColor(u.owner == player() ? Color.GREEN : Color.RED);
                        g2.fillRect(px, py, 40, 40);
                    }
                }
            }

            g2.setTransform(at);

            if (jammed) {
                g2.drawImage(noises[animation % noises.length], 0, 0, null);
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
                            doZoomIn();
                        } else {
                            doZoomOut();
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
            drag = false;
            doDragMode(drag);
        }
        return super.mouse(e);
    }
    /**

     * Set drag mode UI settings.

     * @param dragging the dragging indicator.
     */
    void doDragMode(boolean dragging) {
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
//                if (player().money < cost) {
//                    color = 0xFFFF8080;
//                }

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
        BuildingsPanel() {
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
                    pavementMode = false;
                    placementMode = false;
                    build.down = false;
                    upgradePanel.hideUpgradeSelection();
                    displaySecondary(Screens.INFORMATION_BUILDINGS);
                }
            };
            build.onPress = new Action0() {
                @Override
                public void invoke() {
                    pavementMode = false;
                    placementMode = !placementMode;
                    if (placementMode) {
                        buttonSound(SoundType.CLICK_HIGH_2);
                        build.down = true;
                        currentBuilding = null;
                        buildingBox = null;
                        Tile t = player().currentBuilding.tileset.get(race()).normal;
                        placementRectangle.setSize(t.width + 2, t.height + 2);
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
        BuildingInfoPanel() {
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

            boolean upgradeVisible =

                    b != null && b.type.upgrades.size() > 0

                    && buildingInfoPanel.visible()

                    && b.isComplete()
                    && !b.isSeverlyDamaged()
                    && planet().owner == player();

            upgradePanel.visible(upgradeVisible);
            if (!upgradeVisible) {
                upgradePanel.clearTooltips();
            }

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
        if (animation == Integer.MAX_VALUE) {
            animation = -1;
        }
        animation++;
        blink = animation / 4 % 2 == 0;

        weatherOverlay.update();

        askRepaint();
    }
    /** Perform the faster animation. */
    void doAnimation2() {
        if (config.allowWeather && planet().weatherTTL > 0 && planet().type.weatherDrop == WeatherType.RAIN) {
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
        weatherOverlay.update();
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
        for (GroundwarGun g : new ArrayList<>(guns)) {
            if (g.building == currentBuilding) {
                guns.remove(g);
                g.selected = false;
                fortif = true;
            }
        }
        if (battle != null && fortif) {
            battle.defenderFortificationLosses++;
            for (GroundwarUnit gu : units) {
                if (gu.attackBuilding == currentBuilding) {
                    gu.attackBuilding = null;
                }
            }
        }

        planet().demolish(currentBuilding);

        doAllocation();
        buildingBox = null;
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
        if (battle != null || !units.isEmpty()) {
            Allocator.computeNow(planet(), ResourceAllocationStrategy.BATTLE);
        } else {
            Allocator.computeNow(planet());
        }
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
//        /** Label field. */
//        UILabel allocation;
        /** Label field. */
        UILabel autobuild;
        /** Label field. */
        UILabel military;
        /** Label field. */
        UILabel other;
        /** Label field. */
        UILabel needed;
        /** The labels. */
        List<UILabel> lines;
        /** Construct the label elements. */
        InfoPanel() {
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
//            allocation = new UILabel("-", textSize, commons.text());
            autobuild = new UILabel("-", textSize, commons.text());
            military = new UILabel("-", textSize, commons.text());
            other = new UILabel("-", 7, commons.text());
            other.wrap(true);
            needed = new UILabel("-", 7, commons.text());
            needed.wrap(true);
            needed.color(TextRenderer.RED);

            lines = Arrays.asList(
                    owner, race, surface, population, housing, worker, hospital, food, energy, police,
                    taxTradeIncome, taxInfo, autobuild, military
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
                    c.textSize(textSize);
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
//            i++;

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
            military.visible(false);

            if (p.isPopulated()) {

                if (p.owner == player()) {
                    population.text(format("colonyinfo.population",

                            (int)p.population(), get(p.getMoraleLabel()), withSign((int)p.population() - (int)p.lastPopulation())
                    ), true).visible(true);

                    setLabel(housing, "colonyinfo.housing", ps.houseAvailable, (int)p.population()).visible(true);
                    setLabel(worker, "colonyinfo.worker", (int)p.population(), ps.workerDemand).visible(true);
                    setLabel(hospital, "colonyinfo.hospital", ps.hospitalAvailable, (int)p.population()).visible(true);
                    setLabel(food, "colonyinfo.food", ps.foodAvailable, (int)p.population()).visible(true);
                    setLabel(energy, "colonyinfo.energy", ps.energyAvailable, ps.energyDemand).visible(true);
                    setLabel(police, "colonyinfo.police", ps.policeAvailable, (int)p.population()).visible(true);

                    int req = (int)(ps.nativeWorkerDemand - p.population());
                    if (req > 0 && ps.workerDemand < p.population()) {
                        String wt = worker.text();
                        wt += " (-" + (req) + ")";
                        worker.text(wt, true);
                        worker.color(TextRenderer.LIGHT_GREEN);
                    }

                    taxTradeIncome.text(format("colonyinfo.tax-trade",

                            (int)p.taxIncome(), (int)p.tradeIncome()
                    ), true).visible(true);

                    taxInfo.text(format("colonyinfo.tax-info",
                            get(p.getTaxLabel()), (int)p.morale(), withSign((int)(p.morale()) - (int)(p.lastMorale()))
                    ), true).visible(true);

                    autobuild.text(format("colonyinfo.autobuild",
                            get(p.getAutoBuildLabel())
                    ), true).visible(true);
                    if (p.autoBuild != AutoBuild.OFF) {
                        autobuild.color(TextRenderer.YELLOW);
                    } else {
                        autobuild.color(TextRenderer.GREEN);
                    }
                    military.text(format("colonyinfo.military", ps.vehicleCount, ps.vehicleMax), true).visible(true);
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
     * The panel hosting the pavement, colonists movement and abandon buttons.
     * @author akarnokd, 2022. jan. 30.
     */
    class ColonyManagementPanel extends UIContainer {
        /** Button to enable pavement mode. */
        UIGenericButton pavementButton;
        /** Button to open the move-in dialog. */
        UIGenericButton moveInButton;
        /** Button to open the move-out dialog. */
        UIGenericButton moveOutButton;
        /** Button to open the abandon colony dialog. */
        UIGenericButton abandonButton;

        ColonyManagementPanel() {
            size(commons.colony().colonyManagementPanel.getWidth(), commons.colony().colonyManagementPanel.getHeight());

            pavementButton = new UIGenericButton("   ", fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
            pavementButton.disabledPattern(commons.common().disabledPattern);
            pavementButton.onClick = new Action0() {
                @Override
                public void invoke() {
                    if (togglePavementMode()) {
                        askRepaint();
                    }
                }
            };
            pavementButton.icon(commons.colony().pavementButton);
            pavementButton.fitIcon(true);
            pavementButton.tooltip(get("colony.pavementmodebutton.tooltip"));

            moveInButton = new UIGenericButton("   ", fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
            moveInButton.disabledPattern(commons.common().disabledPattern);
            moveInButton.onClick = new Action0() {
                @Override
                public void invoke() {
                    displaySecondary(Screens.MOVE_COLONISTS_IN);
                    buttonSound(SoundType.CLICK);
                }
            };
            moveInButton.icon(commons.colony().moveInColonistsButton);
            moveInButton.fitIcon(true);
            moveInButton.tooltip(get("colony.moveinbutton.tooltip"));

            moveOutButton = new UIGenericButton("   ", fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
            moveOutButton.disabledPattern(commons.common().disabledPattern);
            moveOutButton.onClick = new Action0() {
                @Override
                public void invoke() {
                    displaySecondary(Screens.MOVE_COLONISTS_OUT);
                    buttonSound(SoundType.CLICK);
                }
            };
            moveOutButton.icon(commons.colony().moveOutColonistsButton);
            moveOutButton.fitIcon(true);
            moveOutButton.tooltip(get("colony.moveoutbutton.tooltip"));

            abandonButton = new UIGenericButton("   ", fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
            abandonButton.disabledPattern(commons.common().disabledPattern);
            abandonButton.onClick = new Action0() {
                @Override
                public void invoke() {
                    displaySecondary(Screens.ABANDON_COLONY);
                    buttonSound(SoundType.CLICK);
                }
            };
            abandonButton.icon(commons.colony().abandonColonyButton);
            abandonButton.fitIcon(true);
            abandonButton.tooltip(get("colony.abandonbutton.tooltip"));

            addThis();
        }

        @Override
        public void draw(Graphics2D g2) {
            g2.drawImage(commons.colony().colonyManagementPanel, 0, 0, null);
            int btop = 6;
            int bleft = 5;
            int bwidth = 40;
            int bheight = 35;
            int pad = 2;
            pavementButton.location(bleft, btop);
            pavementButton.size(bwidth, bheight);
            moveInButton.location(bleft + bwidth + pad, btop);
            moveInButton.size(bwidth, bheight);
            moveOutButton.location(bleft + 2 * (bwidth + pad), btop);
            moveOutButton.size(bwidth, bheight);
            abandonButton.location(bleft + 3 * (bwidth + pad), btop);
            abandonButton.size(bwidth, bheight);
            moveOutButton.enabled(planet().population() > 0);

            super.draw(g2);
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
        UpgradePanel() {
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
                                m
                        );
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

        public void clearTooltips() {
            for (UIImageButton up : steps) {
                up.tooltip(null);
            }
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

                currentBuilding.buildProgress = currentBuilding.type.hitpoints / 4;
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
        selection = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileEdge, 0xFFFFFF00), null);
        areaAccept = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileEdge, 0xFF00FFFF), null);
        areaEmpty = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileEdge, 0xFF808080), null);
        areaDeploy = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileEdge, 0xFF00FFFF), null);
        areaDeny = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileCrossed, 0xFFFF0000), null);
        areaReserved = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileCrossed, 0xFF0000FF), null);
        areaTaken = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileCrossed, 0xFFFF00FF), null);
        areaCurrent  = new Tile(1, 1, ImageUtils.recolor(commons.colony().tileCrossed, 0xFFFFCC00), null);
        areaPaved = commons.colony().tilePavement;
        areaPavedGhost  = ImageUtils.realpha(commons.colony().tilePavement, 0x80);

        selection.alpha = 1.0f;
        areaAccept.alpha = 1.0f;
        areaDeny.alpha = 1.0f;
        areaCurrent.alpha = 1.0f;

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

        render = new SurfaceRenderer();
        render.z = -1;

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
                pavementMode = false;
                placementMode = false;
                buildingsPanel.build.down = false;
                upgradePanel.hideUpgradeSelection();
                displaySecondary(Screens.INFORMATION_COLONY);
            }
        };
        planets.onClick = new Action0() {
            @Override
            public void invoke() {
                pavementMode = false;
                placementMode = false;
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
        colonyManagementPanel = new ColonyManagementPanel();

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

        retreat = new UIImageButton(commons.spacewar().retreat);
        retreat.visible(false);
        retreat.onClick = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.CLICK_HIGH_2);
                retreat.visible(false);
                confirmRetreat.visible(true);
                cancelRetreat = false;
            }
        };

        confirmRetreat = new UIImageButton(commons.spacewar().sure) {
            @Override
            public boolean mouse(UIMouse e) {
                cancelRetreat = e.has(Button.RIGHT);
                return super.mouse(e);
            }
        };
        confirmRetreat.visible(false);
        confirmRetreat.onClick = new Action0() {
            @Override
            public void invoke() {
                if (cancelRetreat) {
                    retreat.visible(true);
                    confirmRetreat.visible(false);
                    buttonSound(SoundType.NOT_AVAILABLE);
                } else {
                    buttonSound(SoundType.CLICK_HIGH_2);
                    confirmRetreat.visible(false);
                    doRetreat();
                }
                cancelRetreat = false;
            }
        };
        confirmRetreat.tooltip(get("groundwar.retreat_confirm.tooltip"));

        stopUnit = new UIImageTabButton2(commons.spacewar().stop);
        stopUnit.disabledPattern(commons.common().disabledPattern);
//        stopUnit.visible(false);
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
//        moveUnit.visible(false);

        attackUnit = new UIImageTabButton2(commons.spacewar().attack);
        attackUnit.disabledPattern(commons.common().disabledPattern);
//        attackUnit.visible(false);
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
                    doZoomNormal();
                } else
                if (zoomDirection) {
                    doZoomIn();
                } else {
                    doZoomOut();
                }
            }
        };

        weatherOverlay = new WeatherOverlay(new Dimension(640, 480));

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
        // When resizing fix the location of the rendering screen so it does not end up far outside the planet surface area
        double testViewRatioX = (render.offsetX - width / 2) / render.scale / surface().boundingRectangle.width;
        double testViewRatioY = (render.offsetY - height / 2) / render.scale / surface().boundingRectangle.height;
        if (testViewRatioX > 0) {
            render.offsetX = width / 2;
        }
        if (testViewRatioX < -1) {
            render.offsetX = (int) -(surface().boundingRectangle.width * render.scale - width / 2);
        }
        if (testViewRatioY > 0) {
            render.offsetY = height / 2;
        }
        if (testViewRatioY < -1) {
            render.offsetY = (int) -(surface().boundingRectangle.height * render.scale - height / 2);
        }

        leftFill.bounds(sidebarBuildings.x, sidebarBuildings.y + sidebarBuildings.height,

                sidebarBuildings.width, sidebarRadar.y - sidebarBuildings.y - sidebarBuildings.height);
        rightFill.bounds(sidebarBuildingInfo.x, sidebarBuildingInfo.y + sidebarBuildingInfo.height,

                sidebarBuildingInfo.width, sidebarColonyInfo.y - sidebarBuildingInfo.y - sidebarBuildingInfo.height);

        radarPanel.location(sidebarRadar.x + sidebarRadar.width - 1, sidebarRadar.y);
        radar.location(radarPanel.x + 13, radarPanel.y + 13);
        buildingsPanel.location(sidebarBuildings.x + sidebarBuildings.width - 1, sidebarBuildings.y);
        buildingInfoPanel.location(sidebarBuildingInfo.x - buildingInfoPanel.width, sidebarBuildingInfo.y);

        upgradePanel.location(buildingInfoPanel.x, buildingInfoPanel.y + buildingInfoPanel.height);
        colonyManagementPanel.location(buildingsPanel.x, buildingsPanel.y + buildingsPanel.height);

        prev.location(sidebarRadar.x + sidebarRadar.width + 1, sidebarRadar.y - prev.height - 3);
        next.location(prev.x + prev.width + 2, prev.y);
        zoom.location(prev.x, prev.y - 4 - zoom.height);

        retreat.location(zoom.x + 1, zoom.y - retreat.height - 2);
        confirmRetreat.location(retreat.location());
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
            placementRectangle.setSize(t.width + 2, t.height + 2);

            buildingsPanel.preview.building = bt.tileset.get(race).preview;
            buildingsPanel.preview.cost = bt.cost;
            buildingsPanel.preview.count = planet().countBuilding(bt);
            buildingsPanel.preview.enabled(planet().canBuild(bt) && planet().owner == player());
            placementMode = placementMode && planet().canBuild(bt);
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
    /**
     * Try placing a building to the current placementRectangle.
     * @param more cancel the building mode on successful place?
     */
    void placeBuilding(boolean more) {
        if (surface().placement.canPlaceBuilding(placementRectangle)
                && player().money() >= player().currentBuilding.cost
                && planet().canBuild(player().currentBuilding)
        ) {

            Building b = planet().build(building().id, race(), placementRectangle.x + 1, placementRectangle.y - 1);

            placementMode = more && planet().canBuild(building());
            buildingsPanel.build.down = placementMode;

            buildingBox = getBoundingRect(b.location);
            doSelectBuilding(b);

            buildingInfoPanel.update();
            setBuildingList(0);

            effectSound(SoundType.DEPLOY_BUILDING);
        } else {
            if (player().money() < player().currentBuilding.cost) {
                buttonSound(SoundType.NOT_AVAILABLE);

                commons.control().displayError(get("message.not_enough_money"));
            } else
            if (!surface().placement.canPlaceBuilding(placementRectangle)) {
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
        buildingBox = null;
        lastSurface = null;
    }

    /** Set the groundwar time controls. */
    void setGroundWarTimeControls() {
        commons.replaceSimulation(new Action0() {
            @Override
            public void invoke() {
                doGroundWarSimulation();
            }
        },
        new Func1<SimulationSpeed, Integer>() {
            @Override
            public Integer invoke(SimulationSpeed value) {
                switch (value) {
                case NORMAL: return SIMULATION_DELAY;
                case FAST: return SIMULATION_DELAY / 2;
                case ULTRA_FAST: return SIMULATION_DELAY / 4;
                default:
                    throw new AssertionError("" + value);
                }
            }
        }
        );
    }
    /**

     * Generate the set of deployment locations.
     * @param atBuildings should the placement locations around buildings?
     * @param skipEdge skip the most outer location
     * @return the set of locations

     */
    Set<Location> getDeploymentLocations(boolean atBuildings, boolean skipEdge) {
        Set<Location> result = new HashSet<>();
        if (atBuildings) {
            // make sure planets with no buildings on it can deploy tanks
            if (planet().surface.buildings.isEmpty()) {
                Point center = surface().placement.findLocation(3, 3);
                int maxPlaces = 64;
                for (int radius = 1; radius < 10 && maxPlaces > 0; radius++) {
                    for (int x = center.x - radius; x <= center.x + radius; x++) {
                        for (int y = center.y - radius; y <= center.y + radius; y++) {
                            Location loc = Location.of(x, y);
                            if (!result.contains(loc) && surface().placement.canPlaceBuilding(x, y)) {
                                result.add(loc);
                                --maxPlaces;
                            }
                        }
                    }
                }
            } else {
                for (Building b : planet().surface.buildings.iterable()) {
                    result.addAll(placeAround(b));
                }
            }
            for (Location loc : new ArrayList<>(result)) {
                if (surface().deploymentExclusions.contains(loc)) {
                    result.remove(loc);
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                if (skipEdge) {
                    result.addAll(placeEdge(i + 1));
                } else {
                    result.addAll(placeEdge(i));
                }
            }
            for (Location loc : new ArrayList<>(result)) {
                if (!surface().placement.canPlaceBuilding(loc.x - 1, loc.y)
                        || !surface().placement.canPlaceBuilding(loc.x + 1, loc.y)
                        || !surface().placement.canPlaceBuilding(loc.x, loc.y + 1)
                        || !surface().placement.canPlaceBuilding(loc.x, loc.y - 1)
                        || surface().deploymentExclusions.contains(loc)) {
                    result.remove(loc);
                }
            }
        }
        return result;
    }
    /**
     * Place around building.
     * @param b the building
     * @return the set of available placement locations
     */
    Set<Location> placeAround(Building b) {
        Set<Location> result = new HashSet<>();
        for (int x = b.location.x - 3; x < b.location.x + b.tileset.normal.width + 3; x++) {
            for (int y = b.location.y + 3; y > b.location.y - b.tileset.normal.height - 3; y--) {
                if (surface().placement.canPlaceBuilding(x, y)) {
                    result.add(Location.of(x, y));
                }
            }
        }
        return result;
    }
    /**
     * Place deployment indicators.
     * @param distance the distance from edge
     * @return the set of available placement locations
     */
    Set<Location> placeEdge(int distance) {
        Set<Location> result = new HashSet<>();
        int w = planet().surface.width;
        int h = planet().surface.height;
        int n = 0;
        int x = 0;
        int y = -distance;
        while (n < w) {
            if (surface().placement.canPlaceBuilding(x, y)) {
                result.add(Location.of(x, y));
            }

            x++;
            y--;
            n++;
        }
        n = 0;
        x = 0;
        y = -distance;
        while (n < h) {
            if (surface().placement.canPlaceBuilding(x, y)) {
                result.add(Location.of(x, y));
            }

            x--;
            y--;
            n++;
        }
        n = 0;
        x = -h + distance + 1;
        y = -h + 1;
        while (n < w) {
            if (surface().placement.canPlaceBuilding(x, y)) {
                result.add(Location.of(x, y));
            }

            x++;
            y--;
            n++;
        }
        n = 0;
        x = w - distance - 1;
        y = -w + 1;
        while (n < h) {
            if (surface().placement.canPlaceBuilding(x, y)) {
                result.add(Location.of(x, y));
            }

            x--;
            y--;
            n++;
        }
        return result;
    }
    /** Add guns for the buildings. */
    void doAddGuns() {
        guns.clear();
        for (Building b : planet().surface.buildings.iterable()) {
            if (b.type.kind.equals("Defensive") && b.isComplete()) {
                List<BattleGroundTurret> turrets = world().battle.getTurrets(b.type.id, planet().race);
                int n = b.hitpoints * 2 < b.type.hitpoints ? turrets.size() / 2 : turrets.size();
                for (int j = 0; j < turrets.size(); j++) {
                    BattleGroundTurret bt = turrets.get(j);
                    GroundwarGun g = new GroundwarGun(bt);
                    g.rx = b.location.x + bt.rx;
                    g.ry = b.location.y + bt.ry;
                    g.building = b;
                    g.owner = planet().owner;
                    g.index = j;
                    g.count = n;
                    guns.add(g);
                }
            }
        }
    }
    /** Place various units around the colony hub. */
    void doAddUnits() {
        units.clear();
        LinkedList<Location> locs = new LinkedList<>();
        for (Building b : surface().buildings.iterable()) {
            if (b.type.kind.equals("MainBuilding")) {
                Set<Location> locations = new HashSet<>();

                for (int x = b.location.x - 1; x < b.location.x + b.tileset.normal.width + 1; x++) {
                    for (int y = b.location.y + 1; y > b.location.y - b.tileset.normal.height - 1; y--) {
                        Location loc = Location.of(x, y);
                        if (surface().placement.canPlaceBuilding(loc.x, loc.y)) {
                            locations.add(loc);
                        }
                    }
                }
                // place enemy units
                locs.addAll(locations);
                for (ResearchType rt : world().researches.values()) {
                    if (rt.category == ResearchSubCategory.WEAPONS_VEHICLES
                            || rt.category == ResearchSubCategory.WEAPONS_TANKS) {
                        BattleGroundVehicle bgv = world().battle.groundEntities.get(rt.id);

                        GroundwarUnit u = new GroundwarUnit(planet().owner == player() ? bgv.normal : bgv.alternative);
                        Location loc = locs.removeFirst();
                        u.x = loc.x;
                        u.y = loc.y;
                        u.selected = true;
                        u.owner = planet().owner;

                        u.model = bgv;
                        u.hp = u.model.hp;

                        units.add(u);
                    }
                }

                break;
            }
        }
        Player enemy = player();
        if (planet().owner == player()) {
            for (Player p : world().players.values()) {
                if (p != player()) {
                    enemy = p;
                    break;
                }
            }
        }
        for (int d = 0; d < 4; d++) {
            locs.clear();
            for (int x = 0; x > -surface().height; x--) {
                if (surface().placement.canPlaceBuilding(x + d, x - 1 - d)) {
                    locs.add(Location.of(x + d, x - 1 - d));
                }
            }
            for (ResearchType rt : world().researches.values()) {
                if (rt.category == ResearchSubCategory.WEAPONS_VEHICLES
                        || rt.category == ResearchSubCategory.WEAPONS_TANKS) {
                    BattleGroundVehicle bgv = world().battle.groundEntities.get(rt.id);
                    GroundwarUnit u = new GroundwarUnit(enemy == player() ? bgv.normal : bgv.alternative);
                    Location loc = locs.removeFirst();
                    u.x = loc.x;
                    u.y = loc.y;
                    u.selected = true;
                    u.owner = enemy;

                    u.model = bgv;
                    u.hp = u.model.hp;

                    units.add(u);
                }
            }
        }

    }
    /**
     * Draw units into the cell.
     * @param g2 the graphics context
     */
    void drawUnits(Graphics2D g2) {

        for (GroundwarUnit u : units) {
            Point p = unitPosition(u);
            BufferedImage img = u.get();

            // compensate for trimmed image
            int tx = (u.model.width - img.getWidth()) / 2;
            int ty = (u.model.height - img.getHeight()) / 2;

            g2.drawImage(img, p.x + tx, p.y + ty, null);
            if (showDebug) {
                Location loc = u.location();
                int x = surface().baseXOffset + Tile.toScreenX(loc.x, loc.y);
                int y = surface().baseYOffset + Tile.toScreenY(loc.x, loc.y);
                g2.drawImage(areaTaken.getStrip(0), x, y , null);
            }
            if (u.paralizedTTL > 0) {
                // draw green paralization effect
                BufferedImage[] expl = world().battle.groundExplosions.get(ExplosionType.GROUND_GREEN);
                int idx = (PARALIZED_TTL - u.paralizedTTL) % expl.length;

                BufferedImage icon = expl[idx];
                int dx = p.x + tx + (img.getWidth() - icon.getWidth()) / 2;
                int dy = p.y + ty + (img.getHeight() - icon.getHeight()) / 2;
                g2.drawImage(icon, dx, dy, null);
            }
            // paint health bar
            g2.setColor(Color.BLACK);
            g2.fillRect(p.x + 4, p.y + 3, u.model.width - 7, 5);
            if (u.owner == player()) {
                g2.setColor(new Color(0x458AAA));
            } else {
                g2.setColor(new Color(0xAE6951));
            }
            g2.fillRect(p.x + 5, p.y + 4, (int)(u.hp * (u.model.width - 9) / u.model.hp), 3);

            BufferedImage smokeFire = null;
            if (u.hp * 3 <= u.model.hp) {
                smokeFire = commons.colony().buildingFire[animation % commons.colony().buildingFire.length];
            } else
            if (u.hp * 3 <= u.model.hp * 2) {
                smokeFire = commons.colony().buildingSmoke[animation % commons.colony().buildingSmoke.length];
            }
            if (smokeFire != null) {
                int dx = p.x + tx + (img.getWidth() - smokeFire.getWidth()) / 2;
                int dy = p.y + ty + (img.getHeight() / 2 - smokeFire.getHeight());
                g2.drawImage(smokeFire, dx, dy, null);
            }
        }
    }
    /**
     * Draw rockets into the cell.
     * @param g2 the graphics context
     */
    void drawRockets(Graphics2D g2) {
        for (GroundwarRocket u : rockets) {
            Point p = unitPosition(u);
            BufferedImage img = u.get();
            g2.drawImage(img, p.x, p.y, null);
        }
    }
    /**
     * Computes the unit bounding rectangle's left-top position.
     * @param u the unit
     * @return the position
     */
    Point unitPosition(GroundwarUnit u) {
        //Calculate interpolated location if needed
        if (simFrameRatio > 1) {
            simFrameRatio = commons.simulationSpeed() / SIMULATION_DELAY;
        }
        double projectedX = u.lastX + ((u.x - u.lastX) * (simFrameRatio));
        double projectedY = u.lastY + ((u.y - u.lastY) * (simFrameRatio));
        return new Point((int)(planet().surface.baseXOffset + Tile.toScreenX(projectedX, projectedY)),

                (int)(planet().surface.baseYOffset + Tile.toScreenY(projectedX, projectedY)) + 27 - u.model.height);
    }
    /**
     * Computes the unit bounding rectangle's left-top position.
     * @param u the unit
     * @return the position
     */
    Point unitPosition(GroundwarRocket u) {
        //Calculate interpolated location if needed
        if (simFrameRatio > 1) {
            simFrameRatio = commons.simulationSpeed() / SIMULATION_DELAY;
        }
        double projectedX = u.lastX + ((u.x - u.lastX) * (simFrameRatio));
        double projectedY = u.lastY + ((u.y - u.lastY) * (simFrameRatio));
        return new Point(
                (int)(planet().surface.baseXOffset + Tile.toScreenX(projectedX + 0.5, projectedY)),

                (int)(planet().surface.baseYOffset + Tile.toScreenY(projectedX + 0.5, projectedY)));
    }
    /**
     * The on-screen rectangle of the ground unit.
     * @param u the unit to test
     * @return the rectangle
     */
    public Rectangle unitRectangle(GroundwarUnit u) {
//        BufferedImage img = u.get();
        Point p = unitPosition(u);
        return new Rectangle(p.x, p.y, u.model.width, u.model.height);
    }
    /**

     * Draw guns at the specified location.
     * @param g2 the graphics context
     * @param cx the cell X coordinate
     * @param cy the cell Y coordinate

     */
    void drawGuns(Graphics2D g2, int cx, int cy) {
        int x0 = planet().surface.baseXOffset;
        int y0 = planet().surface.baseYOffset;
        for (GroundwarGun u : guns) {
            if (u.rx - 1 == cx && u.ry - 1 == cy) {
                int px = (x0 + Tile.toScreenX(u.rx, u.ry));
                int py = (y0 + Tile.toScreenY(u.rx, u.ry));
                BufferedImage img = u.get();

                int ux = px + (54 - 90) / 2 + u.model.px;
                int uy = py + (28 - 55) / 2 + u.model.py;

                g2.drawImage(img, ux, uy, null);

                if (u.attack != null && showCommand && (showDebug || u.owner == player())) {
                    Point gp = centerOf(u.attack);
                    Point up = centerOf(u);
                    g2.setColor(Color.RED);
                    g2.drawLine(gp.x, gp.y, up.x, up.y);
                }
            }
        }

    }
    /**
     * Draw explosions for the given cell.
     * @param g2 the graphics context
     */
    void drawExplosions(Graphics2D g2) {
        for (GroundwarExplosion exp : explosions) {
                BufferedImage img = exp.get();
                g2.drawImage(img, (int)(exp.x - img.getWidth() / 2d), (int)(exp.y - img.getHeight() / 2d), null);
        }
    }
    /**
     * Compute the collision image.
     * @param u the ground unit object
     * @param ur the ground unit bounding rectangle
     * @param t the tile
     * @param tr the tile bounding rectangle
     * @param is the intersection rectangle
     * @return the collision image
     */
    BufferedImage collisionImage(GroundwarUnit u, Rectangle ur, Tile t, Rectangle tr, Rectangle is) {
        BufferedImage bi = u.get();
        BufferedImage result = new BufferedImage(is.width, is.height, BufferedImage.TYPE_INT_ARGB);
        boolean wasCollision = false;

        for (int y = is.y; y < is.y + is.height; y++) {
            for (int x = is.x; x < is.x + is.width; x++) {
                int urgb = bi.getRGB(x - ur.x, y - ur.y);
                if ((urgb & 0xFF000000) != 0) {
                    int trgb = t.image[(y - tr.y) * t.imageWidth + (x - tr.x)];
                    if ((trgb & 0xFF000000) != 0) {
                        result.setRGB(x - is.x, y - is.y, 0xFFFF0000);
                        wasCollision = true;
                    }
                }
            }

        }
        return wasCollision ? result : null;
    }
    /**
     * @return Computes the selection rectangle.
     */
    Rectangle selectionRectangle() {
        int x0 = Math.min(selectionStart.x, selectionEnd.x);
        int y0 = Math.min(selectionStart.y, selectionEnd.y);
        int x1 = Math.max(selectionStart.x, selectionEnd.x);
        int y1 = Math.max(selectionStart.y, selectionEnd.y);

        return new Rectangle(x0, y0, x1 - x0 + 1, y1 - y0 + 1);
    }
    /**
     * Select units of the same type.
     * @param mx the mouse coordinate
     * @param my the mouse coordinate
     * @param mode the selection mode
     */
    void doSelectUnitType(int mx, int my, SelectionBoxMode mode) {
        for (GroundwarUnit u : units) {
            Rectangle r = unitRectangle(u);

            scaleToScreen(r);

            if (r.contains(mx, my)) {
                for (GroundwarUnit u2 : units) {
                    u2.selected = u2.owner == u.owner && u2.model.id.equals(u.model.id);
                }
                return;
            }
        }
        // select guns of the same building
        for (GroundwarGun g : guns) {
            Rectangle r = gunRectangle(g);
            scaleToScreen(r);

            if (r.contains(mx, my)) {
                for (GroundwarGun g2 : guns) {
                    g2.selected = g2.building == g.building;
                }
                return;
            }
        }

    }
    /**
     * Select units within the selection rectangle.
     * @param mode the selection mode
     */
    void selectUnits(SelectionBoxMode mode) {
        boolean allowCommands = false;

        Rectangle sr = selectionRectangle();
        if (sr.width < 4 && sr.height < 4) {
            GroundwarUnit u2 = null;
            Rectangle u2r = null;
            if (mode == SelectionBoxMode.NEW) {
                deselectAll();
            }
            for (GroundwarUnit u : units) {
                Rectangle r = unitRectangle(u);
                scaleToScreen(r);

                if (r.intersects(sr)) {
                    if (u2 == null || closerToCenter(sr, r, u2r)) {
                        u2 = u;
                        u2r = r;
                    }
                }
            }

            GroundwarGun g2 = null;
            for (GroundwarGun g : guns) {
                Rectangle r = gunRectangle(g);
                scaleToScreen(r);

                if (r.intersects(sr)) {
                    if (g2 == null || closerToCenter(sr, r, u2r)) {
                        g2 = g;
                    }
                }
            }
            if (u2 != null && g2 == null) {
                allowCommands = u2.owner == player();
                u2.selected = mode != SelectionBoxMode.SUBTRACT;
            } else
            if (g2 != null && u2 == null) {
                allowCommands = g2.owner == player();
                g2.selected = mode != SelectionBoxMode.SUBTRACT;
            } else
            if (g2 != null && u2 != null) {
                Rectangle r = unitRectangle(u2);
                scaleToScreen(r);

                Rectangle r2 = gunRectangle(g2);
                scaleToScreen(r2);

                if (closerToCenter(sr, r, r2)) {
                    allowCommands = u2.owner == player();
                    u2.selected = true;
                } else {
                    allowCommands = g2.owner == player();
                    g2.selected = true;
                }
            }
        } else {
            boolean own = false;
            boolean enemy = false;

            for (GroundwarUnit u : units) {
                Rectangle r = unitRectangle(u);

                scaleToScreen(r);

                boolean in = sr.contains(r.x + r.width / 2, r.y + r.height / 2);
                if (mode == SelectionBoxMode.NEW) {
                    u.selected = in;
                } else
                if (mode == SelectionBoxMode.SUBTRACT) {
                    u.selected = u.selected && !in;
                } else
                if (mode == SelectionBoxMode.ADD) {
                    u.selected |= in;
                }

                if (u.selected) {
                    own |= u.owner == player();
                    enemy |= u.owner != player();
                }
            }
            for (GroundwarGun g : guns) {
                Rectangle r = gunRectangle(g);
                scaleToScreen(r);

                boolean in = sr.contains(r.x + r.width / 2, r.y + r.height / 2);
                if (mode == SelectionBoxMode.NEW) {
                    g.selected = in;
                } else
                if (mode == SelectionBoxMode.SUBTRACT) {
                    g.selected = g.selected && !in;
                } else
                if (mode == SelectionBoxMode.ADD) {
                    g.selected |= in;
                }

                if (g.selected) {
                    own |= g.owner == player();
                    enemy |= g.owner != player();
                }
            }

            // if mixed selection, deselect aliens
            if (own && enemy) {
                for (GroundwarUnit u : units) {
                    u.selected = u.selected && u.owner == player();
                }

                for (GroundwarGun u : guns) {
                    u.selected = u.selected && u.owner == player();
                }

            }
            allowCommands = own || enemy;
        }
        moveUnit.enabled(allowCommands);
        attackUnit.enabled(allowCommands);
        stopUnit.enabled(allowCommands);
    }
    /**
     * Check if r1's center is closer to r0's center than r2's center is.
     * @param r0 the base rectangle
     * @param r1 the first rectangle
     * @param r2 the second rectangle
     * @return true if r1 is closer than r2
     */
    boolean closerToCenter(Rectangle r0, Rectangle r1, Rectangle r2) {
        double d1 = Math.hypot(r0.x + r0.width / 2d - r1.x - r1.width / 2d, r0.y + r0.height / 2d - r1.y - r1.height / 2d);
        double d2 = Math.hypot(r0.x + r0.width / 2d - r2.x - r2.width / 2d, r0.y + r0.height / 2d - r2.y - r2.height / 2d);
        return d1 < d2;
    }
    /**
     * Scale and position the given rectangle according to the current offset and scale.
     * @param r the target rectangle
     */
    void scaleToScreen(Rectangle r) {
        r.x = (int)(r.x * render.scale + render.offsetX);
        r.y = (int)(r.y * render.scale + render.offsetY);
        r.width *= render.scale;
        r.height *= render.scale;
    }
    /**
     * Returns the unscaled bounding rectangle of the gun in reference to the surface map.
     * @param g the gun object
     * @return the bounding rectangle
     */
    Rectangle gunRectangle(GroundwarGun g) {
        int x0 = planet().surface.baseXOffset;
        int y0 = planet().surface.baseYOffset;
        int px = (x0 + Tile.toScreenX(g.rx, g.ry));
        int py = (y0 + Tile.toScreenY(g.rx, g.ry));
        BufferedImage img = g.get();

        int ux = px + (54 - img.getWidth()) / 2 + g.model.px;
        int uy = py + (28 - img.getHeight()) / 2 + g.model.py;

        return new Rectangle(ux, uy, img.getWidth(), img.getHeight());
    }

    /**

     * Compute a path for selected unit or units.
     * In case of multiple units a set of destinations are generated in a rectangular
     * area around the target destination. This is to avoid units lining up in a single
     * file and competing for the same target location.
     * @param mx the mouse x
     * @param my the mouse y
     */
    void doMoveSelectedUnits(int mx, int my) {
        boolean moved = false;
        final List<GroundwarUnit> selection = new ArrayList<>();
        for (GroundwarUnit u : units) {
            if (u.selected /* && u.owner == player() */) {  // FIXME player only
                selection.add(u);
            }
        }
        Location lm = render.getLocationAt(mx, my);

        if (selection.size() == 1) {
            GroundwarUnit u = selection.get(0);
            moved = true;
            stop(u);
            move(u, lm.x, lm.y);
        } else if (!selection.isEmpty()) {
            // Group units up nicely so paths to the generated destinations will intersect each other less
            Collections.sort(selection, new Comparator<GroundwarUnit>() {
                @Override
                public int compare(GroundwarUnit o1, GroundwarUnit o2) {
                    if (o1.y > o2.y) {
                        return -1;
                    } else if (o1.y < o2.y) {
                        return 1;
                    } else if (o1.x > o2.x) {
                        return -1;
                    } else if (o1.x < o2.x) {
                        return 1;
                    }
                    return 0;
                }
            });
            int baseX = lm.x;
            int baseY = lm.y;
            int locationsNeeded = selection.size();
            final List<Location> targetLocations = new ArrayList<>();
            if (isPassable(baseX, baseY)) {
                targetLocations.add(Location.of(baseX, baseY));
                locationsNeeded--;
            }
            outer:
            for (int n = 1; n < surface().width / 2; n++) {
                for (int x = baseX - n; x < baseX + n; x++) {
                    if (isPassable(x, baseY + n)) {
                        targetLocations.add(Location.of(x, baseY + n));
                        locationsNeeded--;
                        if (locationsNeeded == 0) {
                            break outer;
                        }
                    }
                }
                for (int y = baseY + n; y > baseY - n; y--) {
                    if (isPassable(baseX + n, y)) {
                        targetLocations.add(Location.of(baseX + n, y));
                        locationsNeeded--;
                        if (locationsNeeded == 0) {
                            break outer;
                        }
                    }
                }
                for (int x = baseX + n; x > baseX - n; x--) {
                    if (isPassable(x, baseY - n)) {
                        targetLocations.add(Location.of(x, baseY - n));
                        locationsNeeded--;
                        if (locationsNeeded == 0) {
                            break outer;
                        }
                    }
                }
                for (int y = baseY - n; y < baseY + n; y++) {
                    if (isPassable(baseX - n, y)) {
                        targetLocations.add(Location.of(baseX - n, y));
                        locationsNeeded--;
                        if (locationsNeeded == 0) {
                            break outer;
                        }
                    }
                }
            }
            // sort target locations the same way
            Collections.sort(targetLocations, new Comparator<Location>() {
                @Override
                public int compare(Location o1, Location o2) {
                    if (o1.y > o2.y) {
                        return -1;
                    } else if (o1.y < o2.y) {
                        return 1;
                    } else if (o1.x > o2.x) {
                        return -1;
                    } else if (o1.x < o2.x) {
                        return 1;
                    }
                    return 0;
                }
            });
            Iterator<Location> iter = targetLocations.iterator();
            for (GroundwarUnit unit : selection) {
                stop(unit);
                Location assignedLocation = iter.next();
                move(unit, assignedLocation.x, assignedLocation.y);
            }
            moved = true;
        }
        if (moved) {
            effectSound(SoundType.ACKNOWLEDGE_2);
            selectCommand(null);
        }
    }

    /** The ground war simulation. */
    void doGroundWarSimulation() {
        previousSimulationTime = currentSimulationTime;
        currentSimulationTime = System.currentTimeMillis();
        if (preparingGroundBattle || commons.simulation.paused()) {
            return;
        }

        if (!noAI) {
            Player np = nonPlayer();
            if (np != null) {
                np.ai.groundBattle(this);
            }
        }
        player().ai.groundBattle(this);


        // destruction animations
        for (GroundwarExplosion exp : new ArrayList<>(explosions)) {
            updateExplosion(exp);
        }
        for (GroundwarRocket rocket : new ArrayList<>(rockets)) {
            rocket.lastX = rocket.x;
            rocket.lastY = rocket.y;
            updateRocket(rocket);
        }
        Iterator<GroundwarGun> itg = guns.iterator();
        while (itg.hasNext()) {
            GroundwarGun g = itg.next();
            if (g.building == null || g.building.hitpoints <= 0) {
                g.selected = false;
                itg.remove();
            } else {
                updateGun(g);
            }
        }


        for (GroundwarUnit u : units) {
            updateUnit(u);
        }

        // execute path plannings
        movementHandler.doPathPlannings();

        moveUnits();

        Player winner = checkWinner();
        if (winner != null) {
            for (GroundwarUnit u : units) {
                stop(u);
            }
            if (explosions.size() == 0 && rockets.size() == 0) {
                commons.simulation.pause();
                concludeBattle(winner);
            }
        }
    }

    /** Handle movement stepping for all units. */
    void moveUnits() {
        for (GroundwarUnit u : units) {
            u.lastX = u.x;
            u.lastY = u.y;
            if (u.hasPlannedMove) {
                if (movementHandler.moveUnit(u)) {
                    u.guard = true;
                }
                //                if (u.nextMove == null) {
                Location loc = u.location();
                Mine m = mines.get(loc);
                if (m != null && m.owner != u.owner) {
                    effectSound(SoundType.EXPLOSION_MEDIUM);
                    Point pt = centerOf(loc);
                    createExplosion(pt.x, pt.y, ExplosionType.GROUND_RED);
                    damageArea(u.x, u.y, m.damage, 1, m.owner);
                    mines.remove(loc);
                }
                //                }
            } else {
                if (u.advanceOnBuilding != null) {
                    u.attackBuilding = u.advanceOnBuilding;
                    u.advanceOnBuilding = null;
                }
                if (u.advanceOnUnit != null) {
                    u.attackUnit = u.advanceOnUnit;
                    u.advanceOnUnit = null;
                }
            }
        }

    }

    /**
     * Update the graphical state of an explosion.
     * @param exp the explosion
     */
    void updateExplosion(GroundwarExplosion exp) {
        if (exp.next()) {
            if (exp.half()) {
                if (exp.target != null) {
                    units.remove(exp.target);
                    movementHandler.removeUnit(exp.target);
                    if (battle != null) {
                        battle.groundLosses.add(exp.target);
                    }
                }
            }
        } else {
            explosions.remove(exp);
        }
    }
    /**
     * Conclude the battle.
     * @param winner the winner
     */
    void concludeBattle(Player winner) {
        final BattleInfo bi = battle;

        bi.groundwarWinner = winner;

        for (GroundwarUnit u : bi.groundLosses) {
            u.item.count--;
            if (u.owner == planet().owner) {
                bi.defenderGroundLosses++;
                if (u.item.count <= 0) {
                    planet().inventory.remove(u.item);
                }
            } else {
                bi.attackerGroundLosses++;
                if (u.item.count <= 0) {
                    bi.attacker.inventory.remove(u.item);
                }
            }
        }

        Player np = nonPlayer();

        if (bi.attacker.owner == winner) {
            planet().takeover(winner);

            BattleSimulator.applyPlanetConquered(planet(), BattleSimulator.PLANET_CONQUER_LOSS);
        } else {
            BattleSimulator.applyPlanetDefended(planet(), BattleSimulator.PLANET_DEFENSE_LOSS);
        }

        planet().rebuildRoads();

        player().ai.groundBattleDone(this);
        np.ai.groundBattleDone(this);

        world().scripting.onGroundwarFinish(this);

        battle = null;

        commons.setCursor(Cursors.POINTER);

        BattlefinishScreen bfs = (BattlefinishScreen)displaySecondary(Screens.BATTLE_FINISH);
        bfs.displayBattleSummary(bi);
    }
    /** @return Check if one of the fighting parties has run out of units/structures. */
    Player checkWinner() {
        if (battle == null) {
            return null;
        }
        int attackerCount = 0;
        int defenderCount = 0;
        for (GroundwarGun g : guns) {
            if (g.building.enabled && g.building.assignedEnergy != 0) {
                defenderCount++;
            }
        }
        for (GroundwarUnit u : units) {
            if (u.owner == planet().owner) {
                if (!winIngoreUnits.contains(u.model.type)) {
                    defenderCount++;
                }
            } else {
                if (!winIngoreUnits.contains(u.model.type)) {
                    attackerCount++;
                }
            }
        }
        // if attacker looses all of its units, the winner is always the defender
        if (attackerCount == 0) {
            return planet().owner;
        } else
        if (defenderCount == 0) {
            return battle.attacker.owner;
        } else
        if (battle.groundRetreated) {
            return planet().owner;
        }
        return null;
    }
    /**
     * Is the given target within the min-max range of the unit.
     * @param u the unit
     * @param target the target unit
     * @return true if within the min-max range
     */
    boolean unitWithinRange(GroundwarUnit u, GroundwarUnit target) {
        return unitInRange(u, target, u.model.maxRange)
                && !unitInRange(u, target, u.model.minRange);
    }
    /**
     * Is the given target within the min-max range of the unit.
     * @param u the unit
     * @param target the target unit
     * @return true if within the min-max range
     */
    boolean unitWithinRange(GroundwarUnit u, Building target) {
        return unitInRange(u, target, u.model.maxRange)
                && !unitInRange(u, target, u.model.minRange);
    }
    /**
     * Apply groundwar damage to the given building.
     * @param b the target building
     * @param damage the damage amount
     */
    void damageBuilding(Building b, double damage) {
        int hpBefore = b.hitpoints;
        int maxHp = world().getHitpoints(b.type, planet().owner, false);
        b.hitpoints = (int)Math.max(0, b.hitpoints - 1L * damage * b.type.hitpoints / maxHp);
        // if damage passes the half mark
        if ("Defensive".equals(b.type.kind)) {
            if (hpBefore * 2 >= b.type.hitpoints && b.hitpoints * 2 < b.type.hitpoints) {
                int count = world().battle.getTurrets(b.type.id, planet().race).size() / 2;
                int i = guns.size() - 1;
                while (i >= 0 && count > 0) {
                    // remove half of the guns
                    if (guns.get(i).building == b) {
                        count--;
                        guns.remove(i);
                    }
                    i--;
                }
            }
        }
        // if building got destroyed
        if (hpBefore > 0 && b.hitpoints <= 0) {
            for (int i = guns.size() - 1; i >= 0; i--) {
                if (guns.get(i).building == b) {
                    // remove guns
                    guns.get(i).selected = false;
                    guns.remove(i);
                }
            }
            if (battle != null && "Defensive".equals(b.type.kind)) {
                battle.defenderFortificationLosses++;
            }
            effectSound(SoundType.EXPLOSION_LONG);
            destroyBuilding(b);
        }
        if (!"Defensive".equals(b.type.kind)) {
            doAllocation();
        }
    }
    /**
     * Destroy the given building and apply statistics.
     * @param b the target building
     */
    void destroyBuilding(Building b) {
        surface().removeBuilding(b);
        b.hitpoints = 0;

        planet().owner.statistics.buildingsLost.value++;
        planet().owner.statistics.buildingsLostCost.value += b.type.cost * (1 + b.upgradeLevel);

        if (battle != null) {
            battle.attacker.owner.statistics.buildingsDestroyed.value++;
            battle.attacker.owner.statistics.buildingsDestroyedCost.value += b.type.cost * (1 + b.upgradeLevel);
        }
        doAllocation();
    }
    /**
     * Update the properties of the target unit.
     * @param u the unit to update
     */
    void updateUnit(GroundwarUnit u) {
        if (u.paralizedTTL > 0) {
            u.paralizedTTL--;
            if (u.paralizedTTL == 0) {
                u.paralized = null;
            }
        }
        if (u.isDestroyed()) {
            return;
        }
        if (u.model.selfRepairTime > 0) {
            if (u.hp < u.model.hp) {
                u.hp = Math.min(u.model.hp, u.hp + 1.0 * u.model.hp / u.model.selfRepairTime);
            }
        }
        if (minelayers.contains(u) && u.path.size() == 0) {
            Location loc = u.location();
            if (!mines.containsKey(loc)) {
                u.fireAnimPhase++;
                if (u.fireAnimPhase >= u.maxPhase()) {
                    Mine m = new Mine();
                    m.damage = u.damage();
                    m.owner = u.owner;
                    mines.put(loc, m);
                    minelayers.remove(u);
                    u.fireAnimPhase = 0;
                }
            } else {
                minelayers.remove(u);
            }
        } else if (u.fireAnimPhase > 0) {
            u.fireAnimPhase++;
            if (u.fireAnimPhase >= u.maxPhase()) {
                if (u.attackUnit != null) {
                    attackUnitEndPhase(u);
                } else
                if (u.attackBuilding != null) {
                    attackBuildingEndPhase(u);
                }
                u.fireAnimPhase = 0;

                if (u.hasValidTarget()
                        && config.aiGroundAttackGetCloser
                        && !u.guard
                        && u.attackMove == null
                        && getCloserUnits.contains(u.model.type)) {
                    moveOneCellCloser(u);
                }
            }
        } else

        if (u.paralizedTTL == 0) {
            Location am = u.attackMove;
            if (u.attackUnit != null && !u.attackUnit.isDestroyed()) {
                approachTargetUnit(u);
            } else if (u.attackBuilding != null && !u.attackBuilding.isDestroyed()) {
                approachTargetBuilding(u);
            } else {
                if (u.attackBuilding != null && u.attackBuilding.isDestroyed()) {
                    u.attackBuilding = null;
                    if (am != null) {
                        move(u, am.x, am.y);
                        u.attackMove = am;
                    } else {
                        stop(u);
                    }
                } else if (u.attackUnit != null && u.attackUnit.isDestroyed()) {
                    u.attackUnit = null;
                    if (am != null) {
                        move(u, am.x, am.y);
                        u.attackMove = am;
                    } else {
                        stop(u);
                    }
                }
                if ((am != null || (!u.hasPlannedMove() && u.guard)) && directAttackUnits.contains(u.model.type)) {
                    // find a new target in range
                    List<GroundwarUnit> targets = unitsInRange(u);
                    if (targets.size() > 0) {
                        attack(u, Collections.min(targets, GroundwarUnit.MOST_DAMAGED));
                        u.attackMove = am;
                    } else {
                        List<Building> targets2 = buildingsInRange(u);
                        if (targets2.size() > 0) {
                            attack(u, ModelUtils.random(targets2));
                            u.attackMove = am;
                        }
                    }
                    if (u.attackUnit == null && u.attackBuilding == null && am != null && !u.hasPlannedMove()) {
                        if (!am.equals(u.location())) {
                            attackMove(u, am.x, am.y);
                        } else {
                            stop(u);
                        }
                    }
                }
            }
        }
    }
    /**
     * Try to move the unit one cell closer to its target.
     * @param u the unit
     */
    void moveOneCellCloser(GroundwarUnit u) {
        Location source = u.location();
        double tx = 0d;
        double ty = 0d;
        if (u.attackBuilding != null) {
            tx = u.attackBuilding.location.x + u.attackBuilding.width() / 2d;
            ty = u.attackBuilding.location.y - u.attackBuilding.height() / 2d;
        } else
        if (u.attackUnit != null) {
            Location target = u.attackUnit.location();
            tx = target.x;
            ty = target.y;
        }
        double currentDistance = Math.hypot(tx - source.x, ty - source.y);
        if (currentDistance <= 1.42) {
            return;
        }
        List<Location> neighbors = u.getPathingMethod().neighbors.invoke(source);
        if (!neighbors.isEmpty()) {
            Location cellCloserToTarget = null;
            double cellDistance = currentDistance;
            for (Location loc : neighbors) {
                double newDistance = Math.hypot(tx - loc.x, ty - loc.y);
                if (newDistance < cellDistance) {
                    cellDistance = newDistance;
                    cellCloserToTarget = loc;
                }
            }
            if (cellCloserToTarget != null) {
                if (u.attackUnit != null) {
                    u.advanceOnUnit = u.attackUnit;
                    u.attackUnit = null;
                }
                if (u.attackBuilding != null) {
                    u.advanceOnBuilding = u.attackBuilding;
                    u.attackBuilding = null;
                }
                movementHandler.setMovementGoal(u, cellCloserToTarget);
            }
        }
    }
    /**
     * Approach the target building.
     * @param u the unit who is attacking
     */
    void approachTargetBuilding(GroundwarUnit u) {
        if (unitWithinRange(u, u.attackBuilding)) {
            if (u.nextMove != null) {
                movementHandler.clearUnitGoal(u);
            } else {
                Location centerOfBuilding = centerCellOf(u.attackBuilding);
                if (movementHandler.rotateStep(u, centerOfBuilding.x, centerOfBuilding.y)) {
                    if (u.cooldown <= 0) {
                        u.fireAnimPhase++;
                        if (u.model.fireSound != null) {
                            effectSound(u.model.fireSound);
                        }

                        if (u.model.type == GroundwarUnitType.ROCKET_SLED) {
                            Location loc = centerCellOf(u.attackBuilding);
                            createRocket(u, loc.x, loc.y);
                        }
                        u.cooldown = u.model.delay;
                    } else {
                        u.cooldown -= SIMULATION_DELAY;
                    }
                }
            }
        } else {
            if (!u.hasPlannedMove()) {
                Location ul = u.location();
                if (!unitInRange(u, u.attackBuilding, u.model.maxRange)) {
                    // plot path to the building
                    movementHandler.setMovementGoal(u, buildingNearby(u.attackBuilding, ul));
                } else {
                    // plot path outside the minimum range
                    Location c = buildingNearby(u.attackBuilding, ul);
                    double angle = ModelUtils.random() * 2 * Math.PI;
                    Location c1 = Location.of(
                            (int)(c.x + (u.model.minRange + 1.4142) * Math.cos(angle)),
                            (int)(c.y + (u.model.minRange + 1.4142) * Math.sin(angle))
                    );

                    movementHandler.setMovementGoal(u, c1);
                }
            }
        }
    }
    /**
     * Returns a reachable location of the building in relation to the source.
     * @param b the building
     * @param initial the source location
     * @return the nearby reachable location
     */
    Location buildingNearby(Building b, final Location initial) {
        final Location destination = centerCellOf(b);

        final Func2<Location, Location, Integer> trueDistance = new Func2<Location, Location, Integer>() {
            @Override
            public Integer invoke(Location t, Location u) {
                return (int)(1000 * Math.hypot(t.x - u.x, t.y - u.y));
            }
        };

        Comparator<Location> nearestComparator = new Comparator<Location>() {
            @Override
            public int compare(Location o1, Location o2) {
                int d1 = trueDistance.invoke(destination, o1);
                int d2 = trueDistance.invoke(destination, o2);
                int c = Integer.compare(d1, d2);
                if (c == 0) {
                    d1 = trueDistance.invoke(initial, o1);
                    d2 = trueDistance.invoke(initial, o2);
                    c = Integer.compare(d1, d2);
                }
                return c;
            }
        };
        Location result = null;
        for (int x = b.location.x - 1; x < b.location.x + b.width(); x++) {
            Location loc = Location.of(x, b.location.y + 1);
            if (result == null || nearestComparator.compare(result, loc) > 0) {
                result = loc;
            }
            loc = Location.of(x, b.location.y - b.height());
            if (result == null || nearestComparator.compare(result, loc) > 0) {
                result = loc;
            }
        }
        for (int y = b.location.y; y > b.location.y - b.height(); y--) {
            Location loc = Location.of(b.location.x - 1, y);
            if (result == null || nearestComparator.compare(result, loc) > 0) {
                result = loc;
            }
            loc = Location.of(b.location.x + b.width(), y);
            if (result == null || nearestComparator.compare(result, loc) > 0) {
                result = loc;
            }
        }
        return result;
    }
    /**
     * Approach the target unit.
     * @param u the unit who is attacking
     */
    void approachTargetUnit(GroundwarUnit u) {
        // if within range
        if (unitWithinRange(u, u.attackUnit)) {
            if (u.nextMove != null) {
                movementHandler.clearUnitGoal(u);
            } else if (movementHandler.rotateStep(u, u.attackUnit.location().x, u.attackUnit.location().y)) {
                if (u.cooldown <= 0) {
                    u.fireAnimPhase++;
                    if (u.model.fireSound != null) {
                        effectSound(u.model.fireSound);
                    }
                    if (u.model.type == GroundwarUnitType.PARALIZER) {
                        if (u.attackUnit.paralized == null) {
                            u.attackUnit.paralized = u;
                            u.attackUnit.paralizedTTL = PARALIZED_TTL; // FIXME paralize time duration
                            // deparalize target of a paralizer
                            if (u.attackUnit.model.type == GroundwarUnitType.PARALIZER) {
                                for (GroundwarUnit u2 : units) {
                                    if (u2.paralized == u.attackUnit) {
                                        u2.paralized = null;
                                        u2.paralizedTTL = 0;
                                    }
                                }
                            }
                        } else {
                            // if target already paralized, look for another
                            u.attackUnit = null;
                        }
                    }
                    if (u.model.type == GroundwarUnitType.ROCKET_SLED) {
                        createRocket(u, u.attackUnit.x, u.attackUnit.y);
                    }
                } else {
                    u.cooldown -= SIMULATION_DELAY;
                }
            }
        } else {
            if (!u.hasPlannedMove()) {
                if (u.attackMove == null) {
                    // plot path
                    movementHandler.setMovementGoal(u, u.attackUnit.location());
                } else {
                    movementHandler.setMovementGoal(u, u.attackMove);
                }
            } else {
                if (u.attackMove == null) {
                    Location ep = u.path.peekLast();
                    // if the target unit moved since last
                    double dx = ep.x - u.attackUnit.x;
                    double dy = ep.y - u.attackUnit.y;
                    if (Math.hypot(dx, dy) > u.attackUnit.model.maxRange && u.attackUnit.hasPlannedMove()) {
                        movementHandler.clearUnitGoal(u);
                        movementHandler.setMovementGoal(u, u.attackUnit.location());
                    }
                }
            }
        }
    }

    /**
     * Attack the target building.
     * @param u the unit who is attacking
     */
    void attackBuildingEndPhase(GroundwarUnit u) {
        u.cooldown = u.model.delay;
        // for rocket sleds, damage is inflicted by the rocket impact
        if (u.model.type == GroundwarUnitType.KAMIKAZE

        && u.hp * 10 < u.model.hp) {
            special(u);
        } else
        if (u.model.type != GroundwarUnitType.ROCKET_SLED) {
            damageBuilding(u.attackBuilding, u.damage());
        }

        if (u.attackBuilding.isDestroyed()) {
            // TODO demolish animation?
            u.attackBuilding = null;
        }
    }

    /**
     * Attack the target unit.
     * @param u the unit who is attacking
     */
    void attackUnitEndPhase(GroundwarUnit u) {
        u.cooldown = u.model.delay;
        if (u.model.type == GroundwarUnitType.ROCKET_SLED) {
            return;
        }
        if (u.model.type == GroundwarUnitType.ARTILLERY) {
            damageArea(u.attackUnit.x, u.attackUnit.y, u.damage(), u.model.area, u.owner);
        } else if (u.model.type == GroundwarUnitType.KAMIKAZE   && u.hp * 10 < u.model.hp) {
            special(u);
        } else if (unitWithinRange(u, u.attackUnit)) {
            if (!u.attackUnit.isDestroyed()) {
                u.attackUnit.applyDamage(u.damage());
                if (u.attackUnit.isDestroyed()) {
                    effectSound(u.attackUnit.model.destroy);
                    createExplosion(u.attackUnit, ExplosionType.GROUND_RED);
                    // if the unit destroyed was a paralizer, deparalize everyone
                    if (u.attackUnit.model.type == GroundwarUnitType.PARALIZER) {
                        for (GroundwarUnit u2 : units) {
                            if (u2.paralized == u.attackUnit) {
                                u2.paralized = null;
                                u2.paralizedTTL = 0;
                            }
                        }
                    }

                    u.attackUnit.owner.statistics.vehiclesLost.value++;
                    u.attackUnit.owner.statistics.vehiclesLostCost.value += world().researches.get(u.attackUnit.model.id).productionCost;

                    u.owner.statistics.vehiclesDestroyed.value++;
                    u.owner.statistics.vehiclesDestroyedCost.value += world().researches.get(u.attackUnit.model.id).productionCost;

                    u.attackUnit = null;
                }
            } else {
                u.attackUnit = null;
            }
        }
    }
    /**
     * Damage units and structures within the specified cell area.
     * @param cx the cell x
     * @param cy the cell y
     * @param damage the damage to apply
     * @param area the effect area
     * @param owner the units and structures *NOT* to damage
     */
    void damageArea(double cx, double cy, double damage, int area, Player owner) {
        for (GroundwarUnit u : units) {
            if (u.owner != owner) {
                if (cellInRange(cx, cy, u.x, u.y, area)) {
                    if (!u.isDestroyed()) {
                        u.applyDamage((int)(damage * (area - Math.hypot(cx - u.x, cy - u.y)) / area));
                        if (u.isDestroyed()) {
                            createExplosion(u, ExplosionType.GROUND_RED);

                            u.owner.statistics.vehiclesLost.value++;
                            u.owner.statistics.vehiclesLostCost.value += world().researches.get(u.model.id).productionCost;

                            owner.statistics.vehiclesDestroyed.value++;
                            owner.statistics.vehiclesDestroyedCost.value += world().researches.get(u.model.id).productionCost;

                        }
                    }
                }
            }
        }
        if (planet().owner != owner) {
            for (Building b : surface().buildings.list()) {
                Location u = centerCellOf(b);
                if (cellInRange(cx, cy, u.x, u.y, area)) {
                    damageBuilding(b, (int)(damage * (area - Math.hypot(cx - u.x, cy - u.y)) / area));
                }
            }
        }
    }

    /**
     * Update the properties of the given gun.
     * @param g the target gun
     */
    void updateGun(GroundwarGun g) {
        if (!g.building.enabled) {
            return;
        }
        // underpowered guns will not fire
        double powerRate = 1d * g.building.assignedEnergy / g.building.getEnergy();
        double indexRate = (g.index + 0.5) / g.count;
        if (indexRate >= powerRate) {
            g.fireAnimPhase = 0;
            return;
        }

        if (g.fireAnimPhase > 0) {
            g.fireAnimPhase++;
            if (g.fireAnimPhase >= g.maxPhase()) {
                if (g.attack != null && !g.attack.isDestroyed()

                        && unitInRange(g, g.attack)) {
                    if (!g.attack.isDestroyed()) {
                        g.attack.applyDamage(g.damage());
                        if (g.attack.isDestroyed()) {
                            effectSound(g.attack.model.destroy);
                            createExplosion(g.attack, ExplosionType.GROUND_RED);

                            g.attack.owner.statistics.vehiclesLost.value++;
                            g.attack.owner.statistics.vehiclesLostCost.value += world().researches.get(g.attack.model.id).productionCost;

                            g.owner.statistics.vehiclesDestroyed.value++;
                            g.owner.statistics.vehiclesDestroyedCost.value += world().researches.get(g.attack.model.id).productionCost;

                            g.attack = null;
                        }
                    } else {
                        g.attack = null;
                    }
                }
                g.cooldown = g.model.delay;
                g.fireAnimPhase = 0;
            }
        } else {
            if (g.attack != null && !g.attack.isDestroyed()

                    && unitInRange(g, g.attack)) {
                if (rotateGroundwarGunStep(g, centerOf(g.attack))) {
                    if (g.cooldown <= 0) {
                        g.fireAnimPhase++;
                        effectSound(g.model.fire);
                    } else {
                        g.cooldown -= SIMULATION_DELAY;
                    }
                }
            } else {
                g.attack = null;
                // find a new target
                List<GroundwarUnit> targets = unitsInRange(g);
                if (targets.size() > 0) {
                    g.attack = Collections.min(targets, GroundwarUnit.MOST_DAMAGED);
                }
            }
        }
    }
    /**
     * Returns the center cell op the given building.
     * @param b the building
     * @return the location of the center cell
     */
    Location centerCellOf(Building b) {
        return Location.of(b.location.x + b.tileset.normal.width / 2,

                b.location.y - b.tileset.normal.height / 2);
    }
    /**
     * Rotate the structure towards the given target angle by a step.
     * @param gun the gun in question
     * @param target the target point
     * @return rotation done?
     */
    boolean rotateGroundwarGunStep(GroundwarGun gun, Point target) {
        Point pg = centerOf(gun);
        double targetAngle = Math.atan2(target.y - pg.y, target.x - pg.x);

        double currentAngle = U.normalizedAngle(gun.angle);

        double diff = targetAngle - currentAngle;
        if (diff < -Math.PI) {
            diff = 2 * Math.PI - diff;
        } else
        if (diff > Math.PI) {
            diff -= 2 * Math.PI;

        }
        double anglePerStep = 2 * Math.PI * gun.model.rotationTime / gun.model.angles() / SIMULATION_DELAY;
        if (Math.abs(diff) < anglePerStep) {
            gun.angle = targetAngle;
            return true;
        }
        gun.angle += Math.signum(diff) * anglePerStep;
        return false;
    }
    /**
     * Create an explosion animation at the given center location.
     * @param target the target of the explosion
     * @param type the type of the explosion animation
     */
    void createExplosion(GroundwarUnit target, ExplosionType type) {
        GroundwarExplosion exp = new GroundwarExplosion(world().battle.groundExplosions.get(type));
        Point center = centerOf(target);
        exp.x = center.x;
        exp.y = center.y;
        exp.target = target;
        explosions.add(exp);
    }
    /**
     * Create an explosion animation at the given center location.
     * @param x the explosion center in screen coordinates
     * @param y the explosion center in screen coordinates
     * @param type the type of the explosion animation
     */
    void createExplosion(int x, int y, ExplosionType type) {
        GroundwarExplosion exp = new GroundwarExplosion(world().battle.groundExplosions.get(type));
        exp.x = x;
        exp.y = y;
        explosions.add(exp);
    }
    /**
     * Find the units within the range of the gun.
     * @param g the gun
     * @return the units in range
     */
    List<GroundwarUnit> unitsInRange(GroundwarUnit g) {
        List<GroundwarUnit> result = new ArrayList<>();
        for (GroundwarUnit u : units) {
            if (u.owner != g.owner && !u.isDestroyed()

                    && unitInRange(g, u, g.model.maxRange)
                    && !unitInRange(g, u, g.model.minRange)) {
                result.add(u);
            }
        }
        return result;
    }
    /**
     * Find the units within the range of the gun.
     * @param g the gun
     * @return the units in range
     */
    List<Building> buildingsInRange(GroundwarUnit g) {
        List<Building> result = new ArrayList<>();
        for (Building u : surface().buildings.iterable()) {
            if (planet().owner != g.owner && !u.isDestroyed()

                    && unitInRange(g, u, g.model.maxRange)
                    && !unitInRange(g, u, g.model.minRange) && u.type.kind.equals("Defensive")) {
                result.add(u);
            }
        }
        return result;
    }
    /**
     * Find the units within the range of the gun.
     * @param g the gun
     * @return the units in range
     */
    List<GroundwarUnit> unitsInRange(GroundwarGun g) {
        List<GroundwarUnit> result = new ArrayList<>();
        for (GroundwarUnit u : units) {
            if (u.owner != g.owner && !u.isDestroyed()

                    && unitInRange(g, u)) {
                result.add(u);
            }
        }
        return result;
    }
    /**
     * Gives the center point of the gun rectangle in the unscaled pixel space.
     * @param g the gun in question
     * @return the center point
     */
    Point centerOf(GroundwarGun g) {
        Rectangle gr = gunRectangle(g);
        return new Point(gr.x + gr.width / 2, gr.y + gr.height / 2);
    }
    /**
     * Gives the center point of the unit rectangle in the unscaled pixel space.
     * @param g the unit in question
     * @return the center point
     */
    Point centerOf(GroundwarUnit g) {
        Rectangle ur = unitRectangle(g);
        return new Point(ur.x + ur.width / 2, ur.y + ur.height / 2);
    }
    /**
     * Gives the center point of the location cell in the unscaled pixel space.
     * @param g the unit in question
     * @return the center point
     */
    Point centerOf(Location g) {
        return new Point(surface().baseXOffset + Tile.toScreenX(g.x, g.y) + 28,

                surface().baseYOffset + Tile.toScreenY(g.x, g.y) + 14);
    }
    /**
     * Gives the center point of the fractional location cell in the unscaled pixel space.
     * @param x the fractional cell location
     * @param y the fractional cell location
     * @return the center point
     */
    Point centerOf(double x, double y) {
        return new Point((int)(surface().baseXOffset + Tile.toScreenX(x, y) + 28),

                (int)(surface().baseYOffset + Tile.toScreenY(x, y) + 14));
    }
    /**
     * Gives the center point of the unit rectangle in the unscaled pixel space.
     * @param b the building object
     * @return the center point
     */
    Point centerOf(Building b) {
        Rectangle ur = buildingRectangle(b);
        return new Point(ur.x + ur.width / 2, ur.y + ur.height / 2);
    }
    /**
     * Check if the given unit is within the range of the gun.
     * @param g the gun
     * @param u the unit
     * @return true if within range
     */
    boolean unitInRange(GroundwarGun g, GroundwarUnit u /*, double range*/) {
//        Point gp = centerOf(g.building);
//        Point up = centerOf(u);
//

//        double gpx = Tile.toTileX(gp.x, gp.y);
//        double gpy = Tile.toTileY(gp.x, gp.y);
//        double upx = Tile.toTileX(up.x, up.y);
//        double upy = Tile.toTileY(up.x, up.y);
//

//        double ratio = (30 * 30 + 12 * 12) * 1.0 / (28 * 28 + 15 * 15);
//

//        return (gpx - upx) * (gpx - upx) + ratio * (gpy - upy) * (gpy - upy) <= range * range;
        return g.inRange(u);
    }
    /**
     * Check if two cells are within the distance of range.
     * @param cx the first cell X
     * @param cy the first cell Y
     * @param px the second cell X
     * @param py the second cell Y
     * @param range the range in cells
     * @return true if within range
     */
    boolean cellInRange(double cx, double cy, double px, double py, int range) {

        return (cx - px) * (cx - px) + (cy - py) * (cy - py) <= range * range;

    }
    /**
     * Check if the given unit is within the range of the gun.
     * @param g the source unit
     * @param u the unit
     * @param range the maximum range
     * @return true if within range
     */
    boolean unitInRange(GroundwarUnit g, GroundwarUnit u, double range) {
        return Math.hypot(g.x - u.x, g.y - u.y) <= range;

    }
    /**
     * Check if the given unit is within the range of the gun.
     * @param g the source unit
     * @param b the building
     * @param range the maximum range
     * @return true if within range
     */
    boolean unitInRange(GroundwarUnit g, Building b, double range) {
        int bx = b.location.x;
        int bx2 = bx + b.tileset.normal.width - 1;
        int by = b.location.y;
        int by2 = by - b.tileset.normal.height + 1;

        if (Math.hypot(g.x - bx, g.y - by) <= range) {
            return true;
        } else
        if (Math.hypot(g.x - bx2, g.y - by) <= range) {
            return true;
        } else
        if (Math.hypot(g.x - bx, g.y - by2) <= range) {
            return true;
        } else
        if (Math.hypot(g.x - bx2, g.y - by2) <= range) {
            return true;
        } else
        if (g.x >= bx && g.x <= bx2 && (within(g.y - by, 0, range) || within(by2 - g.y, 0, range))) {
            return true;
        } else
        if (g.y <= by && g.y >= by2 && (within(bx - g.x, 0, range) || within(g.x - bx2, 0, range))) {
            return true;
        }
        return false;

    }
    /**
     * Check if the value is within the specified range.
     * @param value the value
     * @param x0 the range
     * @param x1 the range
     * @return true if within
     */
    boolean within(double value, double x0, double x1) {
        if (x0 < x1) {
            return value >= x0 && value <= x1;
        }
        return value >= x1 && value <= x0;
    }

    /**
     * Cancel movements and attacks of the selected units.
     */
    void doStopSelectedUnits() {
        boolean stopped = false;
        for (GroundwarUnit u : units) {
            if (u.selected /* && u.owner == player() */) { // FIXME player only
                stopped = true;
                stop(u);
            }
        }
        for (GroundwarGun g : guns) {
            if (g.selected /* && g.owner == player()*/) { // FIXME player only
                stopped = true;
                g.attack = null;
            }
        }
        if (stopped) {
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
            attackSelect = false;
            moveSelect = false;
        }
        moveUnit.selected = component == moveUnit;
        if (moveUnit.selected) {
            attackSelect = false;
            moveSelect = true;
        }
        attackUnit.selected = component == attackUnit;
        if (attackUnit.selected) {
            attackSelect = true;
            moveSelect = false;
        }
        if (component == null) {
            attackSelect = false;
            moveSelect = false;
        }
    }
    /**
     * Retrieve the building at the given mouse location.
     * @param mx the x coodinate
     * @param my the y coordinate
     * @return the building or null
     */
    Building buildingAt(int mx, int my) {
        return getBuildingAt(render.getLocationAt(mx, my));
    }
    /**
     * Retrieve the unit at the given location.
     * @param mx the mouse X
     * @param my the mouse Y
     * @return the unit or null if empty
     */
    GroundwarUnit unitAt(int mx, int my) {
        Location lm = render.getLocationAt(mx, my);
        for (GroundwarUnit u1 : units) {
            if ((int)u1.x == lm.x && (int)u1.y == lm.y) {
                return u1;
            }
        }
        return null;
    }
    /**
     * Perform an attack move to the designated coordinates.
     * @param mx the mouse X coordinate
     * @param my the mouse Y coordinate
     */
    void doAttackMoveSelectedUnits(final int mx, final int my) {
        boolean attacked = false;
        Location lm = render.getLocationAt(mx, my);
        for (GroundwarUnit u : units) {
            if (u.selected && directAttackUnits.contains(u.model.type)
                    ) {
                move(u, lm.x, lm.y);
                u.attackMove = lm;
                u.guard = true;
                attacked = true;
            }
        }
        if (attacked) {
            effectSound(SoundType.ACKNOWLEDGE_1);
        }
        selectCommand(null);
    }
    /**
     * Attack the object at the given mouse location with the currently selected units.
     * @param mx the mouse X
     * @param my the mouse Y
     */
    void doAttackWithSelectedUnits(final int mx, final int my) {
        boolean attacked = false;
        Location lm = render.getLocationAt(mx, my);
        Building b = getBuildingAt(lm);
        GroundwarUnit gu = null;
        boolean guFound = false;
        for (GroundwarUnit u : units) {
            if (u.selected && directAttackUnits.contains(u.model.type)
                    /* && u.owner == player() */) { // FIXME player only
                if (b != null && planet().owner != u.owner

                        && u.model.type != GroundwarUnitType.PARALIZER) {
                    attack(u, b);
                    u.guard = false;
                    attacked = true;
                } else {

                    if (!guFound) {
                        gu = findNearest(mx, my, u.owner);
                        guFound = true;
                    }
                    if (gu != null) {
                        attack(u, gu);
                        attacked = true;
                        u.guard = false;
                    }
                }

            }
        }
        for (GroundwarGun g : guns) {
            if (g.selected /* && g.owner == player() */) { // FIXME player only
                if (!guFound) {
                    gu = findNearest(mx, my, g.owner);
                    guFound = true;
                }
                if (gu != null) {
                    attack(g, gu);
                    attacked = true;
                }
            }
        }
        if (attacked) {
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
        List<GroundwarUnit> us = new ArrayList<>();
        for (GroundwarUnit u1 : units) {
            if (u1.owner != enemyOf) {
                Rectangle r = unitRectangle(u1);
                scaleToScreen(r);
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
                    Rectangle r1 = unitRectangle(o1);
                    scaleToScreen(r1);

                    double d1 = Math.hypot(mx - r1.x - r1.width / 2d, my - r1.y - r1.height / 2);

                    Rectangle r2 = unitRectangle(o2);
                    scaleToScreen(r2);

                    double d2 = Math.hypot(mx - r2.x - r2.width / 2d, my - r2.y - r2.height / 2);

                    return Double.compare(d1, d2);
                }
            });
        }
        return null;
    }
    /**
     * Create a rocket.
     * @param sender the sender object
     * @param x the target spot
     * @param y the target spot
     */
    void createRocket(GroundwarUnit sender, double x, double y) {
        GroundwarRocket rocket = new GroundwarRocket(world().battle.groundRocket);
        rocket.x = sender.x;
        rocket.lastX = sender.x;
        rocket.y = sender.y;
        rocket.lastY = sender.y;
        rocket.owner = sender.owner;
        rocket.targetX = x;
        rocket.targetY = y;
        rocket.movementSpeed = 25; // FIXME rocket movement speed

        rocket.damage = sender.damage();
        rocket.area = sender.model.area;

        Point pg = centerOf(sender.x, sender.y);
        Point tg = centerOf(x, y);
        rocket.angle = Math.atan2(tg.y - pg.y, tg.x - pg.x);

        rockets.add(rocket);
    }
    /**
     * Update rocket properties.
     * @param rocket the rocket
     */
    void updateRocket(GroundwarRocket rocket) {
        double dv = 1.0 * SIMULATION_DELAY / rocket.movementSpeed / 28;
        double distanceToTarget = Math.hypot(rocket.targetX - rocket.x, rocket.targetY - rocket.y);
        if (distanceToTarget < dv) {
            // target reached, check for enemy rocket jammers
            boolean jammed = isRocketJammed(rocket, 0.5);
            if (!jammed) {
                // if no jammers, affect area
                damageArea(rocket.targetX, rocket.targetY, rocket.damage, rocket.area, rocket.owner);
            }
            Point p = centerOf(rocket.x, rocket.y);
            createExplosion(p.x, p.y, ExplosionType.GROUND_ROCKET_2);
            rockets.remove(rocket);
        } else {
            double angle = Math.atan2(rocket.targetY - rocket.y, rocket.targetX - rocket.x);
            rocket.x += dv * Math.cos(angle);
            rocket.y += dv * Math.sin(angle);
            rocket.fireAnimPhase++;

            if (isRocketJammed(rocket, 0.5)) {
                Point p = centerOf(rocket.x, rocket.y);
                createExplosion(p.x, p.y, ExplosionType.GROUND_ROCKET_2);
                rockets.remove(rocket);
            }
        }

    }
    /**

     * Check if any enemy uint is within jamming range?
     * @param rocket the rocket
     * @param penetrationRatio how far may the rocket travel into the range before it is reported as jammed?
     * @return is jammed
     */
    boolean isRocketJammed(GroundwarRocket rocket, double penetrationRatio) {
        for (GroundwarUnit u : units) {
            if (u.owner != rocket.owner

                    && u.model.type == GroundwarUnitType.ROCKET_JAMMER && u.paralizedTTL == 0) {
                double distance = Math.hypot(u.x - rocket.x, u.y - rocket.y);
                if (distance < u.model.maxRange * penetrationRatio) {
                    if (u.fireAnimPhase == 0) {
                        u.fireAnimPhase++;
                    }
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Initiate a battle with the given settings.
     * @param battle the battle information
     */
    public void initiateBattle(BattleInfo battle) {
        this.battle = battle;

        player().currentPlanet = battle.targetPlanet;

        if (!BattleSimulator.groundBattleNeeded(battle.targetPlanet)) {
            this.battle = null;
            battle.targetPlanet.takeover(battle.attacker.owner);
            BattleSimulator.applyPlanetConquered(battle.targetPlanet, BattleSimulator.PLANET_CONQUER_LOSS);
            battle.groundwarWinner = battle.attacker.owner;
            BattlefinishScreen bfs = (BattlefinishScreen)displaySecondary(Screens.BATTLE_FINISH);
            bfs.displayBattleSummary(battle);
            return;
        }

        setGroundWarTimeControls();

        player().ai.groundBattleInit(this);
        nonPlayer().ai.groundBattleInit(this);

        battlePlacements.clear();
        battlePlacements.addAll(getDeploymentLocations(planet().owner == player(), true)); // skip edge always

        unitsToPlace.clear();
        boolean atBuildings = planet().owner == player();
        InventoryItems iis = (atBuildings ? planet() : battle.attacker).inventory();
        createGroundUnits(atBuildings, iis.iterable(), unitsToPlace);

        preparingGroundBattle = true;

        battle.incrementGroundBattles();

        if (!(player().ai instanceof AIUser)) {
            placeGroundUnits(atBuildings, unitsToPlace);
            doStartBattle();
        }

        if (unitsToPlace.isEmpty()) {
            doStartBattle();
        }
    }
    /** Deploy the non-player vehicles. */
    void deployNonPlayerVehicles() {
        boolean atBuildings = planet().owner != player();

        InventoryItems iis = (atBuildings ? planet() : battle.attacker).inventory();

        LinkedList<GroundwarUnit> gus = new LinkedList<>();
        // create units
        createGroundUnits(atBuildings, iis.iterable(), gus);

        placeGroundUnits(atBuildings, gus);
    }
    /**
     * Record to store the center and radius of the available location set.
     * @author akarnokd, 2011.10.01.
     */
    static class CenterAndRadius {
        /** The center X coordinate. */
        int icx;
        /** The center Y coordinate. */
        int icy;
        /** The maximum radius. */
        int rmax;
    }
    /**
     * Compute the placement circle's center and radius.
     * @param locations the set of locations
     * @return the center and radius
     */
    CenterAndRadius computePlacementCircle(Iterable<Location> locations) {
        // locate geometric center
        double cx = 0;
        double cy = 0;
        boolean first = true;
        int minX = 0;
        int maxX = 0;
        int minY = 0;
        int maxY = 0;
        int count = 0;
        for (Location loc : locations) {
            cx += loc.x;
            cy += loc.y;
            if (first) {
                minX = loc.x;
                maxX = loc.x;
                minY = loc.y;
                maxY = loc.y;
                first = false;
            } else {
                minX = Math.min(minX, loc.x);
                minY = Math.min(minY, loc.y);
                maxX = Math.max(maxX, loc.x);
                maxY = Math.max(maxY, loc.y);
            }
            count++;
        }
        cx /= count;
        cy /= count;
        CenterAndRadius result = new CenterAndRadius();
        result.icx = (int)cx;
        result.icy = (int)cy;
        // expand radially and place vehicles
        result.rmax = Math.max(Math.max(Math.abs(result.icx - minX), Math.abs(result.icx - maxX)),
                Math.max(Math.abs(result.icy - minY), Math.abs(result.icy - maxY))) + 1;
        return result;
    }
    /**
     * Place the non-player units near the geometric center of the deployment location.
     * @param atBuildings place at buildings
     * @param gus the list of units
     */
    void placeGroundUnits(boolean atBuildings, LinkedList<GroundwarUnit> gus) {
        Set<Location> locations = getDeploymentLocations(atBuildings, true);
        for (WarUnit unit : units) {
            locations.remove(unit.location());
        }
        CenterAndRadius car = computePlacementCircle(locations);
        if (atBuildings) {
            placeAroundInCircle(gus, locations, car.icx, car.icy, car.rmax);
        } else {
            Location furthest = ModelUtils.random(locations);
//            // locate geometric center
//            double cx = 0;
//            double cy = 0;
//            for (Building b : surface().buildings) {
//                cx += b.location.x + b.tileset.normal.width / 2.0;
//                cy += b.location.y - b.tileset.normal.height / 2.0;
//            }
//            cx /= surface().buildings.size();
//            cy /= surface().buildings.size();

//            double dist = 0;
//            for (Location loc : locations) {
//                double dist2 = (loc.x - cx) * (loc.x - cx) + (loc.y - cy) * (loc.y * cy);
//                if (dist2 > dist) {
//                    furthest = loc;
//                    dist = dist2;
//                }
//            }
            placeAroundInCircle(gus, locations, furthest.x, furthest.y, car.rmax);
        }
    }

    /**
     * Place units in circular pattern around the center location.
     * @param gus the list of units
     * @param locations the set of locations
     * @param icx the center point
     * @param icy the center point
     * @param rmax the maximum attempt radius
     */
    void placeAroundInCircle(LinkedList<GroundwarUnit> gus,
            Set<Location> locations, int icx, int icy, int rmax) {
        if (!gus.isEmpty() && !locations.isEmpty()) {
            outer:
            for (int r = 0; r < rmax; r++) {
                for (int x = icx - r; x <= icx + r; x++) {
                    if (tryPlaceNonPlayerUnit(x, icy + r, locations, gus)) {
                        break outer;
                    }
                    if (tryPlaceNonPlayerUnit(x, icy - r, locations, gus)) {
                        break outer;
                    }
                }
                for (int y = icy + r; y >= icy - r; y--) {
                    if (tryPlaceNonPlayerUnit(icx - r, y, locations, gus)) {
                        break outer;
                    }
                    if (tryPlaceNonPlayerUnit(icx + r, y, locations, gus)) {
                        break outer;
                    }
                }
            }
        }
    }
    /**
     * Try placing an unit at the specified location and if successful,
     * remove the location and unit from the corresponding collection.
     * @param x the location X
     * @param y the location Y
     * @param locations the set of locations
     * @param units the list of units
     * @return if no more units or locations available
     */
    boolean tryPlaceNonPlayerUnit(int x, int y, Set<Location> locations, LinkedList<GroundwarUnit> units) {
        Location loc = Location.of(x, y);
        if (locations.contains(loc)) {
            locations.remove(loc);
            GroundwarUnit u = units.removeFirst();
            u.x = x;
            u.y = y;
            this.units.add(u);
        }
        return units.isEmpty() || locations.isEmpty();
    }
    /**
     * Create the non-player units.
     * @param atBuildings place them at buildings
     * @param iis the inventory
     * @param gus the unit holder
     */
    void createGroundUnits(boolean atBuildings, Iterable<InventoryItem> iis,
            LinkedList<GroundwarUnit> gus) {
        for (InventoryItem ii : iis) {
            if (ii.type.category == ResearchSubCategory.WEAPONS_TANKS
                    || ii.type.category == ResearchSubCategory.WEAPONS_VEHICLES) {

                BattleGroundVehicle bge = world().battle.groundEntities.get(ii.type.id);

                for (int i = 0; i < ii.count; i++) {
                    GroundwarUnit u = new GroundwarUnit(ii.owner == player()

                            ? bge.normal : bge.alternative);

                    if (atBuildings) {
                        u.owner = planet().owner;
                    } else {
                        u.owner = battle.attacker.owner;
                    }
                    u.item = ii;
                    u.model = bge;
                    u.hp = u.model.hp;

                    gus.add(u);
                }
            }
        }
    }
    /**
     * Start the battle.

     */
    void doStartBattle() {
        unitsToPlace.clear();
        battlePlacements.clear();

        frameTimer = commons.register(SIMULATION_DELAY / 4, new Action0() {
            @Override
            public void invoke() {
                askRepaint();
            }
        });

        doAddGuns();
        deployNonPlayerVehicles();

        preparingGroundBattle = false;
        startBattle.visible(false);
        if (planet().owner != player()) {
            retreat.visible(true);
        }

        world().scripting.onGroundwarStart(this);

        movementHandler = new GroundWarMovementHandler(commons.pool, 28, SIMULATION_DELAY, units, surface().width, surface().height, surface().placement);
        commons.simulation.resume();
    }
    /**
     * Place or remove an unit at the given location if the UI is in deploy mode.
     * @param mx the mouse X coordinate
     * @param my the mouse Y coordinate
     */
    void toggleUnitPlacementAt(int mx, int my) {
        if (preparingGroundBattle) {
            deploySpray = false;
            undeploySpray = false;
            if (canPlaceUnitAt(mx, my)) {
                placeUnitAt(mx, my);
                deploySpray = true;
            } else {
                removeUnitAt(mx, my);
                undeploySpray = true;
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
        return battlePlacements.contains(lm);
    }
    /**
     * Place an unit at the designated cell.
     * @param mx the mouse X coordinate
     * @param my the mouse Y coordinate
     */
    void placeUnitAt(int mx, int my) {
        Location lm = render.getLocationAt(mx, my);
        if (battlePlacements.contains(lm)) {
            if (!unitsToPlace.isEmpty()) {
                GroundwarUnit u = unitsToPlace.removeFirst();
                units.add(u);
                u.x = lm.x;
                u.y = lm.y;
                u.lastX = lm.x;
                u.lastY = lm.y;
                battlePlacements.remove(lm);

                startBattle.visible(unitsToPlace.isEmpty());
            }
        }
    }
    /**
     * Remove an unit from the designated cell.
     * @param mx the mouse X coordinate
     * @param my the mouse Y coordinate
     */
    void removeUnitAt(int mx, int my) {
        Location lm = render.getLocationAt(mx, my);
        for (GroundwarUnit u : new ArrayList<>(units)) {
            if ((int)Math.floor(u.x) == lm.x && (int)Math.floor(u.y) == lm.y) {
                battlePlacements.add(lm);
                units.remove(u);
                unitsToPlace.addFirst(u);
                startBattle.visible(unitsToPlace.isEmpty());
            }
        }
    }
    /**
     * Returns the non-human player of the current battle.
     * @return the player
     */
    Player nonPlayer() {
        if (battle == null) {
            if (planet().owner != world().player) {
                return planet().owner;
            }
            for (GroundwarUnit u : units) {
                if (u.owner != world().player) {
                    return u.owner;
                }
            }
        } else
        if (battle.attacker.owner != player()) {
            return battle.attacker.owner;
        } else
        if (battle.targetFleet != null && battle.targetFleet.owner != player()) {
            return battle.targetFleet.owner;
        } else
        if (battle.targetPlanet != null && battle.targetPlanet.owner != player()) {
            return battle.targetPlanet.owner;
        }
        return null;
    }
    /**
     * Returns the owner of the currently selected units.
     * @return the owner
     */
    Player selectionOwner() {
        for (GroundwarUnit u : units) {
            if (u.selected) {
                return u.owner;
            }
        }
        return null;
    }
    /**
     * Assign the selected units to a group.
     * @param groupNo the group number
     */
    void assignGroup(int groupNo) {
        List<Owned> selected = new ArrayList<>();
        boolean own = false;
        boolean enemy = false;
        for (GroundwarUnit u : units) {
            if (u.selected) {
                own |= u.owner() == player();
                enemy |= u.owner() != player();
                selected.add(u);
            }
        }
        for (GroundwarGun g : guns) {
            if (g.selected) {
                own |= g.owner() == player();
                enemy |= g.owner() != player();
                selected.add(g);
            }
        }

        removeGroup(groupNo);

        if (own && !enemy) {
            for (Object o : selected) {
                groups.put(o, groupNo);
            }
        }
    }
    /**
     * Reselect the units of the saved group.
     * @param groupNo the group number
     */
    void recallGroup(int groupNo) {
        for (GroundwarUnit u : units) {
            Integer gr = groups.get(u);
            u.selected = gr != null && gr == groupNo;
        }
        for (GroundwarGun g : guns) {
            Integer gr = groups.get(g);
            g.selected = gr != null && gr == groupNo;
        }
    }
    @Override
    public BattleInfo battle() {
        return battle;
    }
    @Override
    public Set<Location> placementOptions(Player player) {
        return getDeploymentLocations(player == planet().owner, true);
    }
    @Override
    public void attack(GroundwarGun g, GroundwarUnit target) {
        if (g.owner != target.owner) {
            g.attack = target;
        }
    }
    @Override
    public void attack(GroundwarUnit u, GroundwarUnit target) {
        if (directAttackUnits.contains(u.model.type)
                && u.owner != target.owner
                && u.attackUnit != target) {
            stop(u);
            u.attackBuilding = null;
            u.attackUnit = target;
        }
    }
    @Override
    public void special(GroundwarUnit u) {
        if (u.model.type == GroundwarUnitType.MINELAYER) {
            minelayers.add(u);
        } else
        if (u.model.type == GroundwarUnitType.KAMIKAZE) {
            double x = u.x;
            double y = u.y;
            double m = world().battle.getDoubleProperty(u.model.id, u.owner.id, "self-destruct-multiplier");
            damageArea(x, y, (u.damage() * m), u.model.area, u.owner);
            u.hp = 0; // destroy self
            createExplosion(u, ExplosionType.GROUND_YELLOW);
        }
    }
    @Override
    public void attack(GroundwarUnit u, Building target) {
        if (directAttackUnits.contains(u.model.type)

                && u.owner != planet().owner && u.attackBuilding != target) {
            stop(u);
            u.attackBuilding = target;
            u.attackUnit = null;
        }
    }
    @Override
    public List<GroundwarGun> guns() {
        return guns;
    }
    @Override
    public void move(GroundwarUnit u, int x, int y) {
        u.guard = false;
        Location lm = Location.of(x, y);
        movementHandler.setMovementGoal(u, lm);
        if (!u.inMotion()) {
            u.nextMove = null;
        }
    }
    /**
     * Perform an attack move towards the specified location
     * and ignore enemy ground units in the path.
     * @param u the unit to use for attack move
     * @param x the target X coordinate
     * @param y the target Y coordinate
     */
    public void attackMove(GroundwarUnit u, int x, int y) {
        stop(u);
        u.guard = true;
        Location lm = Location.of(x, y);
        u.attackMove = lm;
        movementHandler.setMovementGoal(u, lm);
    }
    /**
     * Returns the enemy player to the given player.
     * @param p the player
     * @return the enemy player
     */
    protected Player enemy(Player p) {
        // FIXME development patch
        if (battle == null) {
            if (planet().owner != p) {
                return planet().owner;
            }
            for (GroundwarUnit u : units) {
                if (u.owner != p) {
                    return u.owner;
                }
            }
        }
        return battle.enemy(p);
    }
    @Override
    public void stop(GroundwarGun g) {
        g.attack = null;
    }
    @Override
    public void stop(GroundwarUnit u) {
        movementHandler.clearUnitGoal(u);
        u.attackBuilding = null;
        u.attackUnit = null;
        u.attackMove = null;
        u.advanceOnBuilding = null;
        u.advanceOnUnit = null;
        u.guard = true;
        minelayers.remove(u);
    }
    @Override
    public List<GroundwarUnit> units() {
        return units;
    }
    @Override
    public boolean hasMine(int x, int y) {
        return mines.containsKey(Location.of(x, y));
    }
    @Override
    public boolean isPassable(int x, int y) {
        if (!surface().placement.canPlaceBuilding(x, y)) {
            return false;
        }
        Set<WarUnit> units = movementHandler.unitsForPathfinding.get(Location.of(x, y));
        return units == null || units.isEmpty();
    }
    /**
     * Compute the alpha level for the current time of day.
     */
    protected void computeAlpha() {
        if (!config.dayNightCycle) {
            alpha = 1f;
            return;
        }
        int time = world().time.get(GregorianCalendar.HOUR_OF_DAY) * 6
        + world().time.get(GregorianCalendar.MINUTE) / 10;

        if (time < 6 * 4 || time >= 6 * 22) {
            alpha = Tile.MIN_ALPHA;
        } else
        if (time >= 6 * 4 && time < 6 * 10) {
            alpha = (Tile.MIN_ALPHA + (1f - Tile.MIN_ALPHA) * (time - 6 * 4) / 36);
        } else
        if (time >= 6 * 10 && time < 6 * 16) {
            alpha = (1.0f);
        } else

        if (time >= 6 * 16 && time < 6 * 22) {
            alpha = (1f - (1f - Tile.MIN_ALPHA) * (time - 6 * 16) / 36);
        }
        int tcs = config.tileCacheSize > 0 ? config.tileCacheSize : 16;
        if (tcs > 0 && (alpha > Tile.MIN_ALPHA && alpha < 1f)) {
            float step = (1f - Tile.MIN_ALPHA) / tcs;
            float a2 = (alpha - Tile.MIN_ALPHA);
            int n = (int)(a2 / step);
            alpha = step * n + Tile.MIN_ALPHA;
        }
        if (config.allowWeather && planet().weatherTTL > 0) {
            alpha = Math.max(Tile.MIN_ALPHA, alpha - 0.3f);
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
        TankPanel() {
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
                                removeGroup(j);
                            } else {
                                deselectAll();
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
                            recallGroup(j);
                        } else {
                            doSelectAll();
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
            for (GroundwarUnit u2 : units) {
                if (u2.selected) {
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

                        format("spacewar.selection.firepower_dps", (int)(dmg * count),

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

            Set<Integer> selgr = new HashSet<>(groups.values());
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
    /** Deselect all units. */
    void deselectAll() {
        for (GroundwarUnit u : units) {
            u.selected = false;
        }
        for (GroundwarGun g : guns) {
            g.selected = false;
        }
    }
    /** Select all of the player's units. */
    void doSelectAll() {
        for (GroundwarUnit u : units) {
            u.selected = u.owner == player();
        }
        for (GroundwarGun g : guns) {
            g.selected = g.owner == player();
        }
    }
    /**
     * Remove units from group.
     * @param i the group index
     */
    void removeGroup(int i) {
        Iterator<Map.Entry<Object, Integer>> it = groups.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Object, Integer> e = it.next();
            if (e.getValue() == i) {
                it.remove();
            }
        }
    }

    /**
     * Update the various components according to the current game state.
     * @param surface the planet surface
     * @return the planet statistics
     */
    public PlanetStatistics update(PlanetSurface surface) {
        if (lastSurface != surface) {
            buildingBox = null;
            currentBuilding = null;
            lastSurface = surface;
            placementMode = false;
            pavementMode = false;
            buildingsPanel.build.down = false;
            upgradePanel.hideUpgradeSelection();
            if (battle == null) {
                clearGroundBattle();
            }
            setViewLocation();
        }
        // check if the AI has removed any building while we were looking at its planet
        if (currentBuilding != null) {
            if (!planet().surface.buildings.contains(currentBuilding)) {
                buildingBox = null;
                currentBuilding = null;
                // FIXME more actions?
            }
        }

        buildingsPanel.visible(planet().owner == player()
                && showBuildingList && battle == null);
        colonyManagementPanel.visible(buildingsPanel.visible());
        buildingInfoPanel.visible(planet().owner == player() && showBuildingInfo);
        infoPanel.visible(knowledge(planet(), PlanetKnowledge.NAME) >= 0 && showInfo && battle == null);

        boolean showTankPanel = (!units.isEmpty() || !guns.isEmpty()) && !preparingGroundBattle;
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

        starmap.visible(showSidebarButtons && battle == null);
        colonyInfo.visible(showSidebarButtons && battle == null);
        bridge.visible(showSidebarButtons && battle == null);
        planets.visible(showSidebarButtons && battle == null);

        next.visible(battle == null);
        prev.visible(battle == null);

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
    /** Center the view on the planet's middle. */
    void centerScreen() {
        render.offsetX = -(int)((surface().boundingRectangle.width * render.scale - width) / 2);
        render.offsetY = -(int)((surface().boundingRectangle.height * render.scale - height) / 2);
    }

    /** Set the render screen's location, if no previous screen location ratio is available then center the screen. */
    void setViewLocation() {
        if (xViewRatio != 0 && yViewRatio != 0) {
            render.offsetX = (int) (xViewRatio * planet().surface.boundingRectangle.width * render.scale + width / 2);
            render.offsetY = (int) (yViewRatio * planet().surface.boundingRectangle.height * render.scale + height / 2);
        } else {
            centerScreen();
        }
    }
    /**
     * Retreat from the current attack.
     */
    void doRetreat() {
        if (battle != null) {
            battle.groundRetreated = true;
            commons.simulation.resume();
        }
    }
    boolean togglePavementMode() {
        if (planet().owner == player() && !commons.battleMode) {
            placementMode = false;
            buildingsPanel.build.down = false;
            pavementMode = !pavementMode;
            buttonSound(SoundType.CLICK);
            return true;
        }
        return false;
    }

    void calculateObjectPositionInterpolation() {
        double timeSinceLastSimulationStep = System.currentTimeMillis() - currentSimulationTime;
        simFrameRatio = (timeSinceLastSimulationStep) / (currentSimulationTime - previousSimulationTime);

    }

}
