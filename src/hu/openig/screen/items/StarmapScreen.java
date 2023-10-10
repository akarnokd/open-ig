/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.mechanics.DefaultAIControls;
import hu.openig.model.DiplomaticRelation;
import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.FleetMode;
import hu.openig.model.FleetStatistics;
import hu.openig.model.FleetTask;
import hu.openig.model.Owned;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.PlanetProblems;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Player;
import hu.openig.model.ResearchType;
import hu.openig.model.RotationDirection;
import hu.openig.model.Screens;
import hu.openig.model.SelectionMode;
import hu.openig.model.SoundType;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageTabButton;
import hu.openig.ui.UIImageToggleButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.ui.UIMouse.Type;

import java.awt.Stroke;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The starmap screen.
 * @author akarnokd, 2010.01.11.
 */
public class StarmapScreen extends ScreenBase {
    /** The horizontal/vertical scrollbar painter. */
    public class ScrollBarPainter {
        /** Horizontal scroll rectangle. */
        final Rectangle hscrollRect = new Rectangle();
        /** Horizontal scroll inner rectangle. */
        final Rectangle hscrollInnerRect = new Rectangle();
        /** Horizontal scrollknob rectangle. */
        final Rectangle hscrollKnobRect = new Rectangle();
        /** Vertical scroll rectangle. */
        final Rectangle vscrollRect = new Rectangle();
        /** Vertical scroll inner rectangle. */
        final Rectangle vscrollInnerRect = new Rectangle();
        /** Vertical scrollknob rectangle. */
        final Rectangle vscrollKnobRect = new Rectangle();
        /** Minimum knob height constant. */
        final int minimumKnobHeight = commons.starmap().vKnobTop.getHeight() + commons.starmap().vKnobBottom.getHeight();
        /** Minimum knob width constant. */
        final int minimumKnobWidth = commons.starmap().hKnobLeft.getHeight() + commons.starmap().hKnobRight.getHeight();
        /**
         * Paint the scrollbars to the given graphics.
         * @param g2 the graphics
         */
        public void paint(Graphics2D g2) {
            paintHorizontally(g2, hscrollRect, commons.starmap().hScrollLeft, commons.starmap().hScrollRight, commons.starmap().hScrollFill);
            paintVertically(g2, vscrollRect, commons.starmap().vScrollTop, commons.starmap().vScrollBottom, commons.starmap().vScrollFill);

            paintHorizontally(g2, hscrollKnobRect, commons.starmap().hKnobLeft, commons.starmap().hKnobRight, commons.starmap().hKnobFill);
            paintVertically(g2, vscrollKnobRect, commons.starmap().vKnobTop, commons.starmap().vKnobBottom, commons.starmap().vKnobFill);
        }
        /**
         * Reposition the scrollbar graphics.
         * @param rectangle the window to enclose
         * @param saveX how many pixels to save on the horizontal scrollbar's right side
         * @param saveY how many pixels to save on the vertical scrollbar's bottom side
         */
        public void setBounds(Rectangle rectangle, int saveX, int saveY) {
            hscrollRect.x = rectangle.x;
            hscrollRect.y = rectangle.y + rectangle.height + 1;
            hscrollRect.width = rectangle.width - saveX;
            hscrollRect.height = commons.starmap().hScrollFill.getHeight();

            vscrollRect.x = rectangle.x + rectangle.width + 1;
            vscrollRect.y = rectangle.y;
            vscrollRect.width = commons.starmap().vScrollFill.getWidth();
            vscrollRect.height = rectangle.height - saveY;

            hscrollInnerRect.setLocation(hscrollRect.x + 2, hscrollRect.y + 2);
            hscrollInnerRect.setSize(hscrollRect.width - 4, hscrollRect.height - 4);
            vscrollInnerRect.setLocation(vscrollRect.x + 2, vscrollRect.y + 2);
            vscrollInnerRect.setSize(vscrollRect.width - 4, vscrollRect.height - 4);

            vscrollKnobRect.setLocation(vscrollInnerRect.x + 1, vscrollInnerRect.y + 1);
            vscrollKnobRect.setSize(vscrollInnerRect.width - 2, vscrollInnerRect.height - 2);

            hscrollKnobRect.setLocation(hscrollInnerRect.x + 1, hscrollInnerRect.y + 1);
            hscrollKnobRect.setSize(hscrollInnerRect.width - 2, hscrollInnerRect.height - 2);

            // adjust position and size based on the current zoom and window

            int vPixelsAvailable = Math.max(starmapRect.height - starmapWindow.height, 0);
            int hPixelsAvailable = Math.max(starmapRect.width - starmapWindow.width, 0);

            if (vPixelsAvailable > 0) {
                int maxVScroll = vscrollKnobRect.height - minimumKnobHeight;
                if (vPixelsAvailable <= maxVScroll) {
                    vscrollKnobRect.y += yOffset;
                    vscrollKnobRect.height -= vPixelsAvailable;
                } else {
                    vscrollKnobRect.y += yOffset * maxVScroll / (starmapRect.height - starmapWindow.height) ;
                    vscrollKnobRect.height = minimumKnobHeight;
                }
            }
            if (hPixelsAvailable > 0) {
                int maxHScroll = hscrollKnobRect.width - minimumKnobWidth;
                if (hPixelsAvailable <= maxHScroll) {
                    hscrollKnobRect.x += xOffset;
                    hscrollKnobRect.width -= hPixelsAvailable;
                } else {
                    hscrollKnobRect.x +=  xOffset * maxHScroll / (starmapRect.width - starmapWindow.width);
                    hscrollKnobRect.width = minimumKnobWidth;
                }
            }
        }
        /**
         * @param dx the horizontal knob movement

         * @return computes a one pixel horizontal movement would equal to a X pixels of panning on screen.

         */

        public int getHorizontalPanAmount(int dx) {
            int hPixelsAvailable = Math.max(starmapRect.width - starmapWindow.width, 0);
            if (hPixelsAvailable > 0) {
                int maxHScroll = hscrollInnerRect.width - 2 - minimumKnobWidth;
                if (hPixelsAvailable <= maxHScroll) {
                    return dx;
                }
                return dx * (starmapRect.width - starmapWindow.width) / maxHScroll;
            }
            return 0;
        }
        /**
         * @param dy the vertical knob movement

         * @return computes a one pixel vertical movement would equal to a Y pixels of panning on screen.

         */

        public int getVerticalPanAmount(int dy) {
            int vPixelsAvailable = Math.max(starmapRect.height - starmapWindow.height, 0);
            if (vPixelsAvailable > 0) {
                int maxVScroll = vscrollInnerRect.height - 2 - minimumKnobHeight;
                if (vPixelsAvailable <= maxVScroll) {
                    return dy;
                }
                return dy * (starmapRect.height - starmapWindow.height) / maxVScroll;
            }
            return 0;
        }
    }
    /**
     * Paint a horizontal resizable area from images.
     * @param g2 the graphics
     * @param origin the bounding rectangle
     * @param left the left image
     * @param right the right image
     * @param fill the filler between the two images
     */
    static void paintHorizontally(Graphics2D g2, Rectangle origin, BufferedImage left, BufferedImage right, BufferedImage fill) {
        g2.drawImage(left, origin.x, origin.y, null);
        g2.drawImage(right, origin.x + origin.width - right.getWidth(), origin.y, null);
        Paint pt = g2.getPaint();
        g2.setPaint(new TexturePaint(fill,

                new Rectangle(origin.x + left.getWidth(), origin.y
                        , fill.getWidth(), fill.getHeight())));
        g2.fillRect(origin.x + left.getWidth(), origin.y,

                origin.width - left.getWidth() - right.getWidth(), fill.getHeight());
        g2.setPaint(pt);
    }
    /**
     * Paint a vertically resizable area from images.
     * @param g2 the graphics
     * @param origin the bounding rectangle
     * @param top the left image
     * @param bottom the right image
     * @param fill the filler between the two images
     */
    static void paintVertically(Graphics2D g2, Rectangle origin, BufferedImage top, BufferedImage bottom, BufferedImage fill) {
        g2.drawImage(top, origin.x, origin.y, null);
        g2.drawImage(bottom, origin.x, origin.y + origin.height - bottom.getHeight(), null);
        Paint pt = g2.getPaint();

        g2.setPaint(new TexturePaint(fill,

                new Rectangle(origin.x, origin.y  + top.getHeight()
                        , fill.getWidth(), fill.getHeight())));

        g2.fillRect(origin.x, origin.y + top.getHeight(),

                origin.width, origin.height - top.getHeight() - bottom.getHeight());

        g2.setPaint(pt);
    }
    /** The minimum zoom level. */
    private int minimumZoom = 2;
    /** The maximum zoom level. */
    private int zoomLevelCount = 14;
    /** The zoom step. */
    /** The current zoom index. The actual zoom is (minimumZoom + zoomIndex) / 4.0 . */
    private int zoomIndex = 0;
    /** The current horizontal pixel offset for the starmap contents. */
    private int xOffset;
    /** The current vertical pixel offset for the starmap contents. */
    private int yOffset;
    /** The main starmap window's coordinates. (E.g., the rectangle where the map is drawn.) */
    final Rectangle starmapWindow = new Rectangle();
    /** The rendering rectangle of the starmap. (E.g., the entire, scaled rectangle of the map.) */
    final Rectangle starmapRect = new Rectangle();
    /** The minimap rectangle. */
    final Rectangle minimapRect = new Rectangle();
    /** The minimap's inner rectangle. */
    final Rectangle minimapInnerRect = new Rectangle();
    /** The current minimap viewport rectangle. */
    final Rectangle minimapViewportRect = new Rectangle();
    /** The current radar dot. */
    private BufferedImage radarDot;
    /** The rotation animation timer. */
    Closeable rotationTimer;
    /** The starmap clipping rectangle. */
    Rectangle starmapClip;
    /** The scrollbar painter. */
    ScrollBarPainter scrollbarPainter;
    /** The right panel rectangle. */
    UIContainer rightPanel;
    /** The bottom panel rectangle. */
    UIContainer bottomPanel;
    /** To blink the currently selected planet on the minimap. */
    boolean minimapPlanetBlink;
    /** The blink counter. */
    int blinkCounter;
    /** The divident ratio between the planet listing and the fleet listing. */
    public double planetFleetSplitter = 0.5;
    /** The planets listing entire subpanel. */
    final Rectangle planetsListPanel = new Rectangle();
    /** The fleets listing entire subpanel. */
    final Rectangle fleetsListPanel = new Rectangle();
    /** The zooming entire subpanel. */
    final Rectangle zoomingPanel = new Rectangle();
    /** The screen selection buttons subpanel. */
    final Rectangle buttonsPanel = new Rectangle();
    /** Planet fleet splitter. */
    final Rectangle planetFleetSplitterRect = new Rectangle();
    /** The planets/fleets splitter movement range. */
    final Rectangle planetFleetSplitterRange = new Rectangle();
    /** The planets listing. */
    final Rectangle planetsList = new Rectangle();
    /** The fleets listings. */
    final Rectangle fleetsList = new Rectangle();
    /** The planet scrolled index. */
    int planetsOffset;
    /** The fleets scrolled index. */
    int fleetsOffset;
    /** Button. */
    UIImageButton prevPlanet;
    /** Button. */
    UIImageButton nextPlanet;
    /** Button. */
    UIImageButton colony;
    /** Button. */
    UIImageButton prevFleet;
    /** Button. */
    UIImageButton nextFleet;
    /** Button. */
    UIImageButton equipment;
    /** Button. */
    UIImageButton info;
    /** Button. */
    UIImageButton bridge;
    /** The zoom button. */
    UIImageButton zoom;
    /** The zoom direction. */
    boolean zoomDirection;
    /** In panning mode? */
    boolean panning;
    /** Scrolling vertically. */
    boolean scrollX;
    /** Scrolling horizontally. */
    boolean scrollY;
    /** Mouse down. */
    boolean mouseDown;
    /** Moving the planets fleets splitter. */
    boolean pfSplitter;
    /** Last X. */
    int lastX;
    /** Last Y. */
    int lastY;
    /** The colony name. */
    UILabel colonyName;
    /** The colony owner. */
    UILabel colonyOwner;
    /** The colony race and surface type. */
    UILabel colonySurface;
    /** The colony population and tax. */
    UILabel colonyPopulationTax;
    /** The other properties. */
    UILabel colonyOther;
    /** Problem indicator icon. */
    UIImage problemsHouse;
    /** Problem indicator icon. */
    UIImage problemsEnergy;
    /** Problem indicator icon. */
    UIImage problemsFood;
    /** Problem indicator icon. */
    UIImage problemsHospital;
    /** Problem indicator icon. */
    UIImage problemsWorker;
    /** Problem indicator icon. */
    UIImage problemsVirus;
    /** Problem indicator icon. */
    UIImage problemsStadium;
    /** Problem indicator icon. */
    UIImage problemsRepair;
    /** Problem indicator icon. */
    UIImage problemsColonyHub;
    /** Problem indicator icon. */
    UIImage problemsPolice;
    /** Problem indicator icon. */
    UIImage problemsFireBrigade;
    /** Military spaceport on the planet. */
    UIImage indicatorMilitarySpaceport;
    /** Deploy satellite button. */
    UIImageButton surveySatellite;
    /** Deploy spy satellite 1 button. */
    UIImageButton spySatellite1;
    /** Deploy spy satellite 2 button. */
    UIImageButton spySatellite2;
    /** Deploy hubble. */
    UIImageButton hubble2;
    /** Show radar. */
    public UIImageToggleButton showRadarButton;
    /** Show fleet. */
    public UIImageToggleButton showFleetButton;
    /** Show stars. */
    public UIImageToggleButton showStarsButton;
    /** Show grid. */
    public UIImageToggleButton showGridButton;
    /** Show name. */
    UIImageButton showNamesNone;
    /** Show name. */
    UIImageButton showNamesPlanet;
    /** Show name. */
    UIImageButton showNamesFleet;
    /** Show name. */
    UIImageButton showNamesBoth;
    /** The fleet name. */
    UILabel fleetName;
    /** The fleet owner. */
    UILabel fleetOwner;
    /** The fleet status. */
    UILabel fleetStatus;
    /** The fleet composition. */
    UILabel fleetComposition;
    /** The fleet nearby planet. */
    UILabel fleetPlanet;
    /** The fleet speed. */
    UILabel fleetSpeed;
    /** The fleet firepower. */
    UILabel fleetFirepower;
    /** Move fleet. */
    UIImageTabButton fleetMove;
    /** Attack with the fleet. */
    UIImageTabButton fleetAttack;
    /** Stop the fleet. */
    UIImageTabButton fleetStop;
    /** Colonize with the fleet. */
    UIImageButton fleetColonize;
    /** Colonize with the fleet. */
    UIImageButton fleetColonizeCancel;
    /** The fleet separator image. */
    UIImage fleetSeparator;
    /** The current fleet control mode. */
    FleetMode fleetMode;
    /** Show the planet names. */
    boolean showPlanetNames = true;
    /** Show the fleet names. */
    boolean showFleetNames = true;
    /** The statistics cache delayed by ~450ms. */
    final Map<Planet, PlanetStatistics> cache = new HashMap<>();
    /** The statistics button. */
    UIImageButton statistics;
    /** The achievements button. */
    UIImageButton achievements;
    /** Flag to indicate this is the first time the starmap is displayed in a new game. */
    public boolean newGameStarted;
    /** Debug: show all planets and fleets. */
    boolean showAll;
    /** The extra fleet icon 2. */
    BufferedImage extraFleet2;
    /** Indicate if the current planet/fleet's name is in edit mode. */
    boolean editNameMode;
    /** Given the current panel visibility settings, set the map rendering coordinates. */
    void computeRectangles() {
        starmapWindow.x = 0;
        starmapWindow.y = 20;
        starmapWindow.width = width;
        starmapWindow.height = height - 37;
        if (config.showStarmapScroll) {
            starmapWindow.width -= commons.starmap().vScrollFill.getWidth();
            starmapWindow.height -= commons.starmap().hScrollFill.getHeight();
        }
        if (config.showStarmapLists) {
            starmapWindow.width -= commons.starmap().panelVerticalFill.getWidth();
            if (config.showStarmapScroll) {
                starmapWindow.width -= 3;
            }
        } else {
            if (config.showStarmapScroll) {
                starmapWindow.width -= 1;
            }
        }
        if (config.showStarmapInfo) {
            starmapWindow.height -= commons.starmap().infoFill.getHeight();
            if (config.showStarmapScroll) {
                starmapWindow.height -= 3;
            }
        } else {
            if (config.showStarmapScroll) {
                starmapWindow.height -= 1;
            }
        }

        minimapRect.x = width - commons.starmap().minimap.getWidth();
        minimapRect.y = height - commons.starmap().minimap.getHeight() - 17;
        minimapRect.width = commons.starmap().minimap.getWidth();
        minimapRect.height = commons.starmap().minimap.getHeight();

        int saveX = 0;
        int saveY = 0;
        if (config.showStarmapMinimap) {
            if (!config.showStarmapLists) {
                saveX += minimapRect.width + 1;
                if (config.showStarmapScroll) {
                    saveX -= commons.starmap().vScrollFill.getWidth() + 1;
                }
            } else {
                if (!config.showStarmapScroll) {
                    saveX += commons.starmap().vScrollFill.getWidth() + 3;
                }
            }
            if (!config.showStarmapInfo) {
                saveY += minimapRect.height + 1;
                if (config.showStarmapScroll) {
                    saveY -= commons.starmap().hScrollFill.getHeight() + 1;
                }
            } else {
                if (!config.showStarmapScroll) {
                    saveY += commons.starmap().hScrollFill.getHeight() + 3;
                }
            }
        }
        rightPanel.x = getInnerWidth() - commons.starmap().panelVerticalFill.getWidth();
        rightPanel.y = starmapWindow.y;
        rightPanel.width = commons.starmap().panelVerticalFill.getWidth();
        rightPanel.height = starmapWindow.height - saveY;

        bottomPanel.x = starmapWindow.x;
        bottomPanel.y = height - 18 - commons.starmap().infoFill.getHeight();
        bottomPanel.width = starmapWindow.width - saveX;
        bottomPanel.height = commons.starmap().infoFill.getHeight();

        scrollbarPainter.setBounds(starmapWindow, saveX, saveY);

        Rectangle bottomPanel = new Rectangle(0, 0, this.bottomPanel.width, this.bottomPanel.height);
        Rectangle rightPanel = new Rectangle(0, 0, this.rightPanel.width, this.rightPanel.height);

        // ..............................................................
        // the right subpanels
        buttonsPanel.width = commons.starmap().panelVerticalFill.getWidth() - 4;
        buttonsPanel.height = commons.common().infoButton[0].getHeight() + commons.common().bridgeButton[0].getHeight() + 2;
        buttonsPanel.x = rightPanel.x + 2;
        buttonsPanel.y = rightPanel.y + rightPanel.height - buttonsPanel.height;

        zoomingPanel.width = buttonsPanel.width;
        zoomingPanel.height = commons.starmap().zoom[0].getHeight() + 2;
        zoomingPanel.x = buttonsPanel.x;
        zoomingPanel.y = buttonsPanel.y - zoomingPanel.height - 2;

        zoom.location(zoomingPanel.x + zoomingPanel.width - zoom.width - 2, zoomingPanel.y + 1);

        statistics.location(zoomingPanel.x + 3, zoomingPanel.y + 1);
        achievements.location(zoomingPanel.x + 3, zoomingPanel.y + 34);

        planetFleetSplitterRange.x = zoomingPanel.x;
        planetFleetSplitterRange.width = zoomingPanel.width;
        planetFleetSplitterRange.y = rightPanel.y + 16 + commons.starmap().backwards[0].getHeight()
            + commons.starmap().colony[0].getHeight();
        planetFleetSplitterRange.height = zoomingPanel.y - planetFleetSplitterRange.y
            - 16 - commons.starmap().backwards[0].getHeight() - commons.starmap().equipment[0].getHeight();

        planetFleetSplitterRect.x = planetFleetSplitterRange.x;
        planetFleetSplitterRect.width = planetFleetSplitterRange.width;
        planetFleetSplitterRect.height = 4;
        planetFleetSplitterRect.y = (int)(planetFleetSplitterRange.y + (planetFleetSplitter * planetFleetSplitterRange.height));

        planetsListPanel.x = zoomingPanel.x;
        planetsListPanel.y = rightPanel.y + 2;
        planetsListPanel.width = zoomingPanel.width;
        planetsListPanel.height = planetFleetSplitterRect.y - planetsListPanel.y;

        fleetsListPanel.x = planetsListPanel.x;
        fleetsListPanel.y = planetsListPanel.y + planetsListPanel.height + 2;
        fleetsListPanel.width = planetsListPanel.width;
        fleetsListPanel.height = zoomingPanel.y - planetFleetSplitterRect.y - 4;

        prevPlanet.x = planetsListPanel.x + 2;
        prevPlanet.y = planetsListPanel.y + 1;
        nextPlanet.x = planetsListPanel.x + prevPlanet.width + 4;
        nextPlanet.y = planetsListPanel.y + 1;

        prevFleet.x = fleetsListPanel.x + 2;
        prevFleet.y = fleetsListPanel.y + 1;
        nextFleet.x = fleetsListPanel.x + prevFleet.width + 4;
        nextFleet.y = fleetsListPanel.y + 1;

        colony.x = planetsListPanel.x + 1;
        colony.y = planetsListPanel.y + planetsListPanel.height - colony.height - 1;

        equipment.x = fleetsListPanel.x + 1;
        equipment.y = fleetsListPanel.y + fleetsListPanel.height - equipment.height - 1;

        info.x = buttonsPanel.x + 1;
        info.y = buttonsPanel.y + 1;
        bridge.x = buttonsPanel.x + 1;
        bridge.y = info.y + info.height + 1;

        List<Planet> planets = planets();
        if (planets.size() > 0) {
            int idx = planets.indexOf(planet());
            prevPlanet.enabled(idx > 0);
            nextPlanet.enabled(idx + 1 < planets.size());
        } else {
            prevPlanet.enabled(false);
            nextPlanet.enabled(false);
        }

        List<Fleet> fleets = player().ownFleets();
        if (fleets.size() > 0) {
            int idx = fleets.indexOf(fleet());
            prevFleet.enabled(idx > 0);
            nextFleet.enabled(idx + 1 < fleets.size());
        } else {
            prevFleet.enabled(false);
            nextFleet.enabled(false);
        }

        // ..............................................................

        planetsList.x = planetsListPanel.x;
        planetsList.y = planetsListPanel.y + prevPlanet.height + 1;
        planetsList.width = planetsListPanel.width;
        planetsList.height = colony.y - planetsList.y;

        fleetsList.x = fleetsListPanel.x;
        fleetsList.y = fleetsListPanel.y + prevFleet.height + 1;
        fleetsList.width = fleetsListPanel.width;
        fleetsList.height = equipment.y - fleetsList.y;

        // ..............................................................
        minimapRect.x = width - commons.starmap().minimap.getWidth();
        minimapRect.y = height - commons.starmap().minimap.getHeight() - 17;
        minimapRect.width = commons.starmap().minimap.getWidth();
        minimapRect.height = commons.starmap().minimap.getHeight();

        minimapInnerRect.setBounds(minimapRect);
        minimapInnerRect.x += 2;
        minimapInnerRect.y += 2;
        minimapInnerRect.width -= 4;
        minimapInnerRect.height -= 3;
        // ..............................................................

        colonyName.location(bottomPanel.x + 10, bottomPanel.y + 6);
        colonyOwner.location(bottomPanel.x + 10, bottomPanel.y + 26);
        colonySurface.location(bottomPanel.x + 10, bottomPanel.y + 40);
        colonyPopulationTax.location(bottomPanel.x + 10, bottomPanel.y + 54);
        colonyOther.location(bottomPanel.x + 10, bottomPanel.y + 70);

        int probcnt = 11;
        indicatorMilitarySpaceport.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt + 1), bottomPanel.y + 70);

        problemsHouse.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
        problemsEnergy.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
        problemsWorker.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
        problemsFood.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
        problemsHospital.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
        problemsVirus.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
        problemsStadium.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
        problemsRepair.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
        problemsColonyHub.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
        problemsPolice.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt--), bottomPanel.y + 70);
        problemsFireBrigade.location(bottomPanel.x + bottomPanel.width - 215 - 11 * (probcnt), bottomPanel.y + 70);

        surveySatellite.location(bottomPanel.x + bottomPanel.width - 199, bottomPanel.y + 4);
        spySatellite1.location(bottomPanel.x + bottomPanel.width - 199, bottomPanel.y + 24);
        spySatellite2.location(bottomPanel.x + bottomPanel.width - 199, bottomPanel.y + 44);
        hubble2.location(bottomPanel.x + bottomPanel.width - 199, bottomPanel.y + 64);

        showRadarButton.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 25);
        showFleetButton.location(bottomPanel.x + bottomPanel.width - 55, bottomPanel.y + 25);
        showStarsButton.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 45);
        showGridButton.location(bottomPanel.x + bottomPanel.width - 55, bottomPanel.y + 45);

        showNamesNone.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 65);
        showNamesPlanet.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 65);
        showNamesFleet.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 65);
        showNamesBoth.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 65);

        fleetName.location(colonyName.location());
        fleetOwner.location(colonyOwner.location());
        fleetStatus.location(colonySurface.location());
        fleetPlanet.location(colonyPopulationTax.location());

        fleetComposition.location(fleetPlanet.x, fleetPlanet.y + 16);
        int ff = Math.max(fleetSpeed.width, fleetFirepower.width);

        int fleetcmd = 214;

        fleetSpeed.location(bottomPanel.x + bottomPanel.width - fleetcmd - ff - 10, fleetOwner.y);
        fleetFirepower.location(bottomPanel.x + bottomPanel.width - fleetcmd - ff - 10, fleetSpeed.y + 10);

        fleetMove.location(bottomPanel.x + bottomPanel.width - fleetcmd, bottomPanel.y + 5);
        fleetAttack.location(bottomPanel.x + bottomPanel.width - fleetcmd, bottomPanel.y + 32);
        fleetStop.location(bottomPanel.x + bottomPanel.width - fleetcmd, bottomPanel.y + 59);

        fleetSeparator.location(bottomPanel.x + bottomPanel.width - fleetcmd - 4, bottomPanel.y + 2);

        fleetColonize.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 4);
        fleetColonizeCancel.location(bottomPanel.x + bottomPanel.width - 110, bottomPanel.y + 4);

        computeViewport();

        setTooltip(statistics, "starmap.statistics.tooltip");
        setTooltip(achievements, "starmap.achievements.tooltip");
        setTooltip(zoom, "starmap.zoom.tooltip");
    }

    /**
     * Compute the current viewport's coordinates.
     */
    private void computeViewport() {
        double zoom = getZoom();

        starmapRect.width = (int)(commons.starmap().background.getWidth() * zoom);
        starmapRect.height = (int)(commons.starmap().background.getHeight() * zoom);

        if (starmapRect.width < starmapWindow.width) {
            xOffset = -(starmapWindow.width - starmapRect.width) / 2;
        }
        if (starmapRect.height < starmapWindow.height) {
            yOffset = -(starmapWindow.height - starmapRect.height) / 2;
        }
        starmapRect.x = starmapWindow.x - xOffset;
        starmapRect.y = starmapWindow.y - yOffset;

        // center if smaller
        limitOffsets();
        // ..............................................................
        int miniw = minimapInnerRect.width;
        int minih = minimapInnerRect.height;

        int x2;
        if (xOffset < 0) {
            minimapViewportRect.x = 0;
            x2 = miniw;
        }  else {
            minimapViewportRect.x = miniw * xOffset / starmapRect.width;
            x2 = miniw * (xOffset + starmapWindow.width) / starmapRect.width;
        }
        int y2;
        if (yOffset < 0) {
            minimapViewportRect.y = 0;
            y2 = minih;
        } else {
            minimapViewportRect.y = minih * yOffset / starmapRect.height;
            y2 = minih * (yOffset + starmapWindow.height) / starmapRect.height;
        }

        if (x2 > miniw) {
            x2 = miniw;
        }
        if (y2 > minih) {
            y2 = minih;
        }

        minimapViewportRect.width = x2 - minimapViewportRect.x;
        minimapViewportRect.height = y2 - minimapViewportRect.y;
        minimapViewportRect.x += minimapRect.x + 2;
        minimapViewportRect.y += minimapRect.y + 2;

        starmapClip = starmapWindow.intersection(starmapRect);
    }

    /**
     * Display the colony problem icons for the own planets.
     * @param p the planet
     */
    void displayColonyProblems(Planet p) {
        problemsHouse.visible(false);
        problemsEnergy.visible(false);
        problemsWorker.visible(false);
        problemsFood.visible(false);
        problemsHospital.visible(false);
        problemsVirus.visible(false);
        problemsStadium.visible(false);
        problemsRepair.visible(false);
        problemsColonyHub.visible(false);
        problemsPolice.visible(false);
        problemsFireBrigade.visible(false);
        indicatorMilitarySpaceport.visible(false);
        if (p.owner == player()) {
            PlanetStatistics ps = getStatistics(p);
            if (ps.hasProblem(PlanetProblems.HOUSING)) {
                problemsHouse.image(commons.common().houseIcon).visible(true);
                setTooltip(problemsHouse, "info.problems.house.tooltip");
            } else
            if (ps.hasWarning(PlanetProblems.HOUSING)) {
                problemsHouse.image(commons.common().houseIconDark).visible(true);
                setTooltip(problemsHouse, "info.warnings.house.tooltip");
            }

            if (ps.hasProblem(PlanetProblems.ENERGY)) {
                problemsEnergy.image(commons.common().energyIcon).visible(true);
                setTooltip(problemsEnergy, "info.problems.energy.tooltip");
            } else
            if (ps.hasWarning(PlanetProblems.ENERGY)) {
                problemsEnergy.image(commons.common().energyIconDark).visible(true);
                setTooltip(problemsEnergy, "info.warnings.energy.tooltip");
            }

            if (ps.hasProblem(PlanetProblems.WORKFORCE)) {
                problemsWorker.image(commons.common().workerIcon).visible(true);
                setTooltip(problemsWorker, "info.problems.worker.tooltip");
            } else
            if (ps.hasWarning(PlanetProblems.WORKFORCE)) {
                problemsWorker.image(commons.common().workerIconDark).visible(true);
                setTooltip(problemsWorker, "info.warnings.worker.tooltip");
            }

            if (ps.hasProblem(PlanetProblems.FOOD)) {
                problemsFood.image(commons.common().foodIcon).visible(true);
                setTooltip(problemsFood, "info.problems.food.tooltip");
            } else
            if (ps.hasWarning(PlanetProblems.FOOD)) {
                problemsFood.image(commons.common().foodIconDark).visible(true);
                setTooltip(problemsFood, "info.warnings.food.tooltip");
            }

            if (ps.hasProblem(PlanetProblems.HOSPITAL)) {
                problemsHospital.image(commons.common().hospitalIcon).visible(true);
                setTooltip(problemsHospital, "info.problems.hospital.tooltip");
            } else
            if (ps.hasWarning(PlanetProblems.HOSPITAL)) {
                problemsHospital.image(commons.common().hospitalIconDark).visible(true);
                setTooltip(problemsHospital, "info.warnings.hospital.tooltip");
            }

            if (ps.hasProblem(PlanetProblems.VIRUS)) {
                problemsVirus.image(commons.common().virusIcon).visible(true);
                setTooltip(problemsVirus, "info.problems.virus.tooltip");
            } else
            if (ps.hasWarning(PlanetProblems.VIRUS)) {
                problemsVirus.image(commons.common().virusIconDark).visible(true);
                setTooltip(problemsVirus, "info.warnings.virus.tooltip");
            }

            if (ps.hasProblem(PlanetProblems.STADIUM)) {
                problemsStadium.image(commons.common().stadiumIcon).visible(true);
                setTooltip(problemsStadium, "info.problems.stadium.tooltip");
            } else
            if (ps.hasWarning(PlanetProblems.STADIUM)) {
                problemsStadium.image(commons.common().stadiumIconDark).visible(true);
                setTooltip(problemsStadium, "info.warnings.stadium.tooltip");
            }

            if (ps.hasProblem(PlanetProblems.REPAIR)) {
                problemsRepair.image(commons.common().repairIcon).visible(true);
                setTooltip(problemsRepair, "info.problems.damage.tooltip");
            } else
            if (ps.hasWarning(PlanetProblems.REPAIR)) {
                problemsRepair.image(commons.common().repairIconDark).visible(true);
                setTooltip(problemsRepair, "info.warnings.damage.tooltip");
            }

            if (ps.hasProblem(PlanetProblems.COLONY_HUB)) {
                problemsColonyHub.image(commons.common().colonyHubIcon).visible(true);
                setTooltip(problemsColonyHub, "info.problems.hq.tooltip");
            } else
            if (ps.hasWarning(PlanetProblems.COLONY_HUB)) {
                problemsColonyHub.image(commons.common().colonyHubIconDark).visible(true);
                setTooltip(problemsColonyHub, "info.warnings.hq.tooltip");
            }
            if (ps.hasProblem(PlanetProblems.POLICE)) {
                problemsPolice.image(commons.common().policeIcon).visible(true);
                setTooltip(problemsPolice, "info.problems.police.tooltip");
            } else
            if (ps.hasWarning(PlanetProblems.POLICE)) {
                problemsPolice.image(commons.common().policeIconDark).visible(true);
                setTooltip(problemsPolice, "info.warnings.police.tooltip");
            }
            if (ps.hasProblem(PlanetProblems.FIRE_BRIGADE)) {
                problemsFireBrigade.image(commons.common().fireBrigadeIcon).visible(true);
                setTooltip(problemsFireBrigade, "info.problems.fire.tooltip");
            }
            indicatorMilitarySpaceport.visible(ps.hasMilitarySpaceport);
            setTooltip(indicatorMilitarySpaceport, "info.military_spaceport.tooltip");
        }
    }
    /** Display the current fleet info. */
    void displayFleetInfo() {

        Fleet f = fleet();
        if (!player().fleets.containsKey(f)) {
            f = null;
            player().selectionMode = SelectionMode.PLANET;
            player().currentFleet = null;
            fleetMode = null;
        }
        boolean fleetMode = f != null && player().selectionMode == SelectionMode.FLEET;

        fleetName.visible(fleetMode);
        fleetOwner.visible(fleetMode);
        fleetStatus.visible(fleetMode);
        fleetPlanet.visible(fleetMode);
        fleetComposition.visible(fleetMode && knowledge(f, FleetKnowledge.COMPOSITION) >= 0);

        boolean showFleetControls = fleetMode && f.owner == player() && world().scripting.mayControlFleet(f);

        fleetAttack.visible(showFleetControls);
        fleetStop.visible(showFleetControls);
        fleetMove.visible(showFleetControls);
        fleetSeparator.visible(showFleetControls);

        fleetFirepower.visible(fleetMode && knowledge(f, FleetKnowledge.FULL) >= 0);
        fleetSpeed.visible(fleetMode);

        if (!fleetMode) {
            fleetColonize.visible((player().selectionMode == SelectionMode.PLANET

                    && planet().owner == null && knowledge(planet(), PlanetKnowledge.OWNER) >= 0
                    && (player().isAvailable("ColonyShip") || hasColonyShip())
                    && !player().colonizationTargets.contains(planet().id)
            ));
            fleetColonizeCancel.visible(player().colonizationTargets.contains(planet().id));
            return;
        }
        fleetName.color(f.owner.color);

        if (this.fleetMode == null) {
            fleetAttack.down = f.mode == FleetMode.ATTACK && minimapPlanetBlink;
            fleetMove.down = f.mode == FleetMode.MOVE && minimapPlanetBlink;
            fleetStop.down = f.mode == null;
        }

        FleetStatistics fs = f.getStatistics();

        fleetColonize.visible(
                (
                    fleetMode && f.owner == player()
                    && fs.planet != null

                    && fs.planet.owner == null && knowledge(fs.planet, PlanetKnowledge.OWNER) >= 0
                    && f.inventoryCount(world().researches.get("ColonyShip")) > 0
                )
        );
        fleetColonizeCancel.visible(false);

        if (knowledge(f, FleetKnowledge.VISIBLE) > 0) {
            fleetOwner.text(f.owner.name, true);
            String fn = f.name();
            if (editNameMode && minimapPlanetBlink) {
                fn += "-";
            }
            if (fn.isEmpty()) {
                fn = "        "; // allow changing a fleet name if it was completely erased
            }
            fleetName.text(fn, true);
        } else {
            fleetOwner.text("");
            if (f.owner != player()) {
                fleetName.text(get("fleetinfo.alien_fleet"), true);
            }
        }

        if (knowledge(f, FleetKnowledge.FULL) >= 0) {
            fleetFirepower.text(format("fleetstatus.firepower", fs.firepower), true).visible(true);
            fleetComposition.text(format("fleetstatus.composition",
                    zeroDash(fs.battleshipCount),
                    zeroDash(fs.cruiserCount),
                    zeroDash(fs.fighterCount),
                    zeroDash(fs.vehicleCount)
            ), true).visible(true);
        } else

        if (knowledge(f, FleetKnowledge.COMPOSITION) >= 0) {
            fleetFirepower.visible(false);
            int sc = fs.battleshipCount + fs.cruiserCount;
            fleetComposition.text(format("fleetstatus.composition.alien",
                    sc,
                    ((fs.fighterCount / 10) * 10) + ".." + ((fs.fighterCount  / 10 + 1) * 10),
                    "?"
            ), true).visible(true);
        } else {
            fleetFirepower.visible(false);
            fleetComposition.visible(false);
        }

//        fleetComposition.text(format("fleetstatus.composition", fs.battleshipCount, fs.cruiserCount, fs.fighterCount, fs.vehicleCount), true);
        fleetSpeed.text(format("fleetstatus.speed", fs.speed), true);
        if (fs.planet != null) {
            fleetPlanet.text(format("fleetstatus.nearby", fs.planet.name()), true);
        } else {
            fleetPlanet.text(format("fleetstatus.nearby", "----"), true);
        }
        if ((f.targetFleet == null && f.targetPlanet() == null) || f.owner != player()) {
            if (f.waypoints.size() > 0 || f.targetFleet != null || f.targetPlanet() != null) {
                fleetStatus.text(format("fleetstatus.moving"), true);
            } else {
                fleetStatus.text(format("fleetstatus.stopped"), true);
            }
        } else {
            if (f.mode == FleetMode.ATTACK) {
                if (f.targetFleet != null) {
                    fleetStatus.text(format("fleetstatus.attack", f.targetFleet.name()), true);
                } else {
                    fleetStatus.text(format("fleetstatus.attack", f.targetPlanet().name()), true);
                }
            } else {
                if (f.targetFleet != null) {
                    fleetStatus.text(format("fleetstatus.moving.after", f.targetFleet.name()), true);
                } else {
                    fleetStatus.text(format("fleetstatus.moving.to", f.targetPlanet().name()), true);
                }
            }
        }
    }
    /**
     * Replace the value with a dash if the value is zero.
     * @param i the value
     * @return a dash or the value as string
     */
    String zeroDash(int i) {
        if (i == 0) {
            return "-";
        }
        return Integer.toString(i);
    }
    /**
     * Check if the fleet contains any colony ships.
     * @return true if colony ships are in the fleet
     */
    boolean hasColonyShip() {
        for (Fleet f : player().ownFleets()) {
            if (!f.inventory.findByType("ColonyShip").isEmpty()) {
                return true;
            }
        }
        return false;
    }
    /** Display the right panel's planet info on the current selected planet. */
    void displayPlanetInfo() {
        Planet p = planet();

        if (p == null || player().selectionMode != SelectionMode.PLANET) {
            colonyName.visible(false);
            colonyOwner.visible(false);
            colonySurface.visible(false);
            colonyPopulationTax.visible(false);
            colonyOther.visible(false);

            problemsHouse.visible(false);
            problemsEnergy.visible(false);
            problemsWorker.visible(false);
            problemsFood.visible(false);
            problemsHospital.visible(false);
            problemsVirus.visible(false);
            problemsStadium.visible(false);
            problemsRepair.visible(false);
            problemsColonyHub.visible(false);
            problemsPolice.visible(false);
            problemsFireBrigade.visible(false);
            indicatorMilitarySpaceport.visible(false);

            surveySatellite.visible(false);
            spySatellite1.visible(false);
            spySatellite2.visible(false);
            hubble2.visible(false);

            return;
        }

        surveySatellite.visible(
                p.owner != player()
                && (knowledge(p, PlanetKnowledge.VISIBLE) >= 0)
                && player().inventoryCount(world().researches.get("Satellite")) > 0
                && !p.hasInventory(world().researches.get("Satellite"), player())
                && !p.hasInventory(world().researches.get("SpySatellite1"), player())
                && !p.hasInventory(world().researches.get("SpySatellite2"), player())
        );
        spySatellite1.visible(
                p.owner != player()

                && knowledge(p, PlanetKnowledge.VISIBLE) >= 0
                && player().inventoryCount(world().researches.get("SpySatellite1")) > 0
                && !p.hasInventory(world().researches.get("SpySatellite1"), player())
                && !p.hasInventory(world().researches.get("SpySatellite2"), player())
        );
        spySatellite2.visible(
                p.owner != player()
                && knowledge(p, PlanetKnowledge.VISIBLE) >= 0
                && player().inventoryCount(world().researches.get("SpySatellite2")) > 0
                && !p.hasInventory(world().researches.get("SpySatellite2"), player())
        );
        hubble2.visible(
                p.owner == player()
                && player().inventoryCount(world().researches.get("Hubble2")) > 0
                && !p.hasInventory(world().researches.get("Hubble2"), player())
        );

        if (p.owner == player() || knowledge(p, PlanetKnowledge.NAME) >= 0) {
            String pn = p.name();
            if (editNameMode && minimapPlanetBlink) {
                pn += "-";
            }
            if (pn.isEmpty()) {
                pn = "        "; // allow changing a planet name if it was completely erased
            }
            colonyName.text(pn, true);
            int c = TextRenderer.GRAY;
            if (p.owner == player() || knowledge(p, PlanetKnowledge.OWNER) >= 0 && p.isPopulated()) {
                c = p.owner.color;
            }
            colonyName.color(c);
            colonyName.visible(true);
        } else {
            colonyName.visible(false);
        }

        if (p.owner == player() || (knowledge(p, PlanetKnowledge.OWNER) >= 0)) {
            colonyOwner.text(p.owner != null ? p.owner.name : get("planet.colonizable"), true);
            colonyOwner.visible(true);
        } else {
            colonyOwner.visible(false);
        }
        if (p.owner == player() || (knowledge(p, PlanetKnowledge.OWNER) >= 0) && p.isPopulated()) {
            colonySurface.text(
                    get(p.getRaceLabel()) + ", "
                    + format("buildinginfo.planet.surface", get(p.type.label))
            , true).visible(true);
        } else {
            colonySurface.text(
                    format("buildinginfo.planet.surface", get(p.type.label))
            , true).visible(true);
        }

        if (p.owner == player()) {
            String pn = p.name();
            if (editNameMode && minimapPlanetBlink) {
                pn += "-";
            }
            if (pn.isEmpty()) {
                pn = "        "; // allow changing a planet name if it was completely erased
            }
            colonyName.text(pn, true);

            colonyPopulationTax.text(
                    format("colonyinfo.population.own",

                            (int)p.population(), get(p.getRaceLabel()), get(p.getMoraleLabel())
                    ) + " (" + withSign((int)(p.population() - p.lastPopulation())) + ")  "
                    + format("colonyinfo.tax_short", get(p.getTaxLabel()))
            , true).visible(true);
        } else {
            if (knowledge(p, PlanetKnowledge.BUILDING) >= 0) {
                if (p.isPopulated()) {
                    colonyPopulationTax.text(format("colonyinfo.population.short.alien",

                            (int)p.population()
                    ), true).visible(true);
                } else {
                    colonyPopulationTax.visible(false);
                }
            } else {
                colonyPopulationTax.visible(false);
            }
        }
        colonyOther.text(world().getOtherItems(), true).visible(true);

        displayColonyProblems(p);
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
    /**
     * Zoom in and keep try to keep the given pixel under the mouse.
     * @param x the X coordinate within the starmapWindow
     * @param y the Y coordinate within the starmapWindow
     */
    public void doZoomIn(int x, int y) {
        if (zoomIndex < zoomLevelCount) {
            readjustZoom(x, y, zoomIndex + 1);
        }
    }

    /**
     * Zoom out and keep try to keep the given pixel under the mouse.
     * @param x the X coordinate within the starmapWindow
     * @param y the Y coordinate within the starmapWindow
     */
    public void doZoomOut(int x, int y) {
        if (zoomIndex > 0) {
            readjustZoom(x, y, zoomIndex - 1);
        }
    }
    /**
     * The parameters of a radar circle.

     * @author akarnokd, 2011.11.03.
     */
    static final class RadarCircle {
        /** Center X. */
        final int x;
        /** Center Y. */
        final int y;
        /** Radius. */
        final int r;
        /**
         * Construct a circle object.
         * @param x the center X
         * @param y the center Y
         * @param r the radius
         */
        RadarCircle(int x, int y, int r) {
            this.x = x;
            this.y = y;
            this.r = r;
        }
        /**
         * Test if a point is within the circle.
         * @param x the point
         * @param y the point
         * @return boolean if within
         */
        public boolean in(double x, double y) {
            return (this.x - x) * (this.x - x) + (this.y - y) * (this.y - y) < r * r;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof RadarCircle) {
                RadarCircle that = (RadarCircle) obj;
                return this.x == that.x && this.y == that.y && this.r == that.r;
            }
            return false;
        }
        @Override
        public int hashCode() {
            return (((17 + x) * 31 + y) * 31 + r) * 31;
        }
    }
    @Override
    public void draw(Graphics2D g2) {
        if (blinkCounter == 0) {
            cache.clear();
        }
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, width, height);

        if (!showAll) {
            if (fleet() != null && player().knowledge(fleet(), FleetKnowledge.VISIBLE) < 0) {
                player().currentFleet = null;
            }
            if (planet() != null && player().knowledge(planet(), PlanetKnowledge.VISIBLE) < 0) {
                player().currentPlanet = null;
            }
            if (player().currentPlanet == null) {
                player().moveNextPlanet();
            }
        }

        displayPlanetInfo();
        displayFleetInfo();

        computeRectangles();

        Shape save0 = g2.getClip();

        g2.clipRect(starmapClip.x, starmapClip.y, starmapClip.width, starmapClip.height);
        g2.drawImage(commons.starmap().background, starmapRect.x, starmapRect.y, starmapRect.width, starmapRect.height, null);

        double zoom = getZoom();

        if (showStarsButton.selected) {
            RenderTools.paintStars(g2, starmapRect, starmapClip, starmapClip, zoomIndex, zoomLevelCount);
        }

        if (showGridButton.selected) {
            RenderTools.paintGrid(g2, starmapRect, commons.starmap().gridColor, commons.text());
        }

        Collection<Fleet> fleets = player().visibleFleets();
        if (showAll) {
            fleets = new ArrayList<>();
            addAllFleets(fleets);
        }

        // render exploration outer and inner limits
        if (showGridButton.selected) {
            renderExplorationLimits(g2);
        }

        // render radar circles
        if (showRadarButton.selected) {
            List<RadarCircle> radarCircles = new ArrayList<>();
            for (Planet p : world().planets.values()) {
                if (p.owner == player() || sharedRadar(p)) {
                    if (p.radar > 0) {
                        if (config.radarUnion) {
                            radarCircles.add(new RadarCircle(p.x, p.y, p.radar));
                        } else {
                            paintRadar(g2, p.x, p.y, p.radar, zoom);
                        }
                    }
                }
            }
            if (config.radarUnion) {
                paintRadar(g2, radarCircles, zoom);
            }
            if (showFleetButton.selected) {
                for (Fleet f : fleets) {
                    if (f.owner == player() || sharedRadar(f)) {
                        f.getStatistics();
                        paintRadar(g2, f.x, f.y, f.radar, zoom);
                    }
                }
            }
        }
        // Enlarge planet name if zoomed in
        int nameFontSize = 5;
        if (zoomIndex >= 12) {
            nameFontSize = 10;
        } else
        if (zoomIndex >= 9) {
            nameFontSize = 7;
        }

        for (Planet p : commons.world().planets.values()) {
            drawPlanet(g2, p, nameFontSize, zoom);
        }
        if (showFleetButton.selected) {
            for (Fleet f : fleets) {
                if (f.owner == player()) {
                    Stroke defaultStroke = g2.getStroke();
                    g2.setStroke(new BasicStroke(1.5f));
                    if (f.mode == FleetMode.ATTACK) {
                        if (f.targetFleet != null) {
                            g2.setColor(new Color(255, 0, 0, 128));
                            drawArrow(
                                    g2,
                                    (int)(starmapRect.x + f.x * zoom),
                                    (int)(starmapRect.y + f.y * zoom),
                                    (int)(starmapRect.x + f.targetFleet.x * zoom),
                                    (int)(starmapRect.y + f.targetFleet.y * zoom));
                        } else
                        if (f.targetPlanet() != null) {
                            g2.setColor(new Color(255, 0, 0, 128));
                            drawArrow(
                                    g2,
                                    (int)(starmapRect.x + f.x * zoom),
                                    (int)(starmapRect.y + f.y * zoom),
                                    (int)(starmapRect.x + f.targetPlanet().x * zoom),
                                    (int)(starmapRect.y + f.targetPlanet().y * zoom));
                        }
                    } else {
                        double lastx = f.x;
                        double lasty = f.y;
                        if (f.targetFleet != null) {
                            g2.setColor(new Color(124, 124, 180, 128));
                            drawArrow(
                                    g2,
                                    (int)(starmapRect.x + f.x * zoom),
                                    (int)(starmapRect.y + f.y * zoom),
                                    (int)(starmapRect.x + f.targetFleet.x * zoom),
                                    (int)(starmapRect.y + f.targetFleet.y * zoom));
                        } else
                        if (f.targetPlanet() != null) {
                            g2.setColor(new Color(124, 124, 180, 128));
                            drawArrow(
                                    g2,
                                    (int)(starmapRect.x + f.x * zoom),
                                    (int)(starmapRect.y + f.y * zoom),
                                    (int)(starmapRect.x + f.targetPlanet().x * zoom),
                                    (int)(starmapRect.y + f.targetPlanet().y * zoom));
                        } else

                        if (f.waypoints.size() > 0) {
                            g2.setColor(new Color(124, 124, 180, 128));
                            int i = 0;
                            for (Point2D.Double pt : f.waypoints) {
                                if(i++ == f.waypoints.size() - 1) {
                                    drawArrow(
                                            g2,
                                            (int)(starmapRect.x + lastx * zoom),
                                            (int)(starmapRect.y + lasty * zoom),
                                            (int)(starmapRect.x + pt.x * zoom),
                                            (int)(starmapRect.y + pt.y * zoom));
                                } else {
                                    g2.drawLine(
                                            (int) (starmapRect.x + lastx * zoom),
                                            (int) (starmapRect.y + lasty * zoom),
                                            (int) (starmapRect.x + pt.x * zoom),
                                            (int) (starmapRect.y + pt.y * zoom));
                                }
                                lastx = pt.x;
                                lasty = pt.y;
                            }
                        }
                    }
                    g2.setStroke(defaultStroke);
                }

                BufferedImage icon = f.owner.fleetIcon;

                if (f.owner == player() && !world().scripting.mayControlFleet(f)) {
                    icon = extraFleet2;
                }

                int x0 = (int)(starmapRect.x + f.x * zoom - icon.getWidth() / 2d);
                int y0 = (int)(starmapRect.y + f.y * zoom - icon.getHeight() / 2d);

                g2.drawImage(icon, x0, y0, null);

                String fleetName = f.name();
                if (knowledge(f, FleetKnowledge.VISIBLE) == 0) {
                    fleetName = get("fleetinfo.alien_fleet");
                }
                int tw = commons.text().getTextWidth(nameFontSize, fleetName);
                int xt = (int)(starmapRect.x + f.x * zoom - tw / 2d);
                int yt = (int)(starmapRect.y + f.y * zoom + f.owner.fleetIcon.getHeight() / 2d) + 3;
                if (showFleetNames) {
                    commons.text().paintTo(g2, xt, yt, nameFontSize, f.owner.color, fleetName);
                }
                if (f == fleet()) {
                    if (player().selectionMode == SelectionMode.FLEET) {
                        g2.setColor(Color.WHITE);
                    } else {
                        g2.setColor(Color.GRAY);
                    }
                    g2.drawRect(x0 - 1, y0 - 1, f.owner.fleetIcon.getWidth() + 2, f.owner.fleetIcon.getHeight() + 2);
                }
                // indicate interesting fleets
                if (minimapPlanetBlink) {
                    if (world().infectedFleets.containsKey(f.id)) {
                        g2.setColor(Color.RED);
                        g2.drawRect(x0 - 2, y0 - 2, f.owner.fleetIcon.getWidth() + 4, f.owner.fleetIcon.getHeight() + 4);
                    }
                    if (world().scripting.fleetBlink(f)) {
                        g2.setColor(Color.RED);
                        g2.drawRect(x0 - 3, y0 - 3, f.owner.fleetIcon.getWidth() + 6, f.owner.fleetIcon.getHeight() + 6);
                    }
                }
            }
        }

        g2.setClip(save0);

        // TODO panel rendering

        if (config.showStarmapLists) {
            paintVertically(g2, rightPanel.bounds(), commons.starmap().panelVerticalTop, commons.starmap().panelVerticalFill, commons.starmap().panelVerticalFill);

            AffineTransform save1 = g2.getTransform();
            g2.translate(rightPanel.x, rightPanel.y);

            g2.drawImage(commons.starmap().panelVerticalSeparator, planetFleetSplitterRect.x, planetFleetSplitterRect.y, null);
            g2.drawImage(commons.starmap().panelVerticalSeparator, zoomingPanel.x, zoomingPanel.y - 2, null);
            g2.drawImage(commons.starmap().panelVerticalSeparator, buttonsPanel.x, buttonsPanel.y - 2, null);

            Shape save2 = g2.getClip();
            g2.clipRect(planetsList.x, planetsList.y, planetsList.width, planetsList.height);
            List<Planet> planets = planets();
            for (int i = planetsOffset; i < planets.size(); i++) {
                Planet p = planets.get(i);
                int color = TextRenderer.GREEN;
                if (p == planet()) {
                    color = TextRenderer.RED;
                }
                commons.text().paintTo(g2, planetsList.x + 3, planetsList.y + (i - planetsOffset) * 10 + 2, 7, color, p.name());
            }
            g2.setClip(save2);
            g2.clipRect(fleetsList.x, fleetsList.y, fleetsList.width, fleetsList.height);
            List<Fleet> playersFleet = player().ownFleets();
            for (int i = fleetsOffset; i < playersFleet.size(); i++) {
                Fleet f = playersFleet.get(i);
                int color = TextRenderer.GREEN;
                if (f == fleet()) {
                    color = TextRenderer.RED;
                }
                commons.text().paintTo(g2, fleetsList.x + 3, fleetsList.y + (i - fleetsOffset) * 10 + 2, 7, color, f.name());
            }

            g2.setClip(save2);
            g2.setTransform(save1);
        }
        if (config.showStarmapInfo) {
            paintHorizontally(g2, bottomPanel.bounds(), commons.starmap().infoLeft, commons.starmap().infoRight, commons.starmap().infoFill);
        }

        if (config.showStarmapScroll) {
            scrollbarPainter.paint(g2);
        }

        if (config.showStarmapMinimap) {
            g2.drawImage(commons.starmap().minimap, minimapRect.x, minimapRect.y, null);
            g2.setColor(Color.WHITE);
            g2.drawImage(world().galaxyModel.map, minimapInnerRect.x, minimapInnerRect.y, minimapInnerRect.width, minimapInnerRect.height, null);
            g2.drawRect(minimapViewportRect.x, minimapViewportRect.y, minimapViewportRect.width - 1, minimapViewportRect.height - 1);
            g2.setClip(save0);
            g2.clipRect(minimapInnerRect.x, minimapInnerRect.y, minimapInnerRect.width, minimapInnerRect.height);
            // render planets
            for (Planet p : commons.world().planets.values()) {
                if (knowledge(p, PlanetKnowledge.VISIBLE) < 0) {
                    continue;
                }
                if (p != planet() || minimapPlanetBlink) {
                    int x0 = minimapInnerRect.x + (p.x * minimapInnerRect.width / commons.starmap().background.getWidth());
                    int y0 = minimapInnerRect.y + (p.y * minimapInnerRect.height / commons.starmap().background.getHeight());
                    int labelColor = TextRenderer.GRAY;
                    if (p.owner != null && knowledge(p, PlanetKnowledge.OWNER) >= 0) {
                        labelColor = p.owner.color;
                    }
                    g2.setColor(new Color(labelColor));
                    g2.fillRect(x0 - 1, y0 - 1, 3, 3);
                }
            }
        }
        g2.setClip(save0);

        rightPanel.visible(config.showStarmapLists);
        bottomPanel.visible(config.showStarmapInfo);

        super.draw(g2);
    }
    /**
     * Renders a planet onto the starmap.
     * @param g2 the graphics context
     * @param p the planet
     * @param nameFontSize the size of the name tag
     * @param zoom the zoom level
     */
    void drawPlanet(Graphics2D g2, Planet p, int nameFontSize, double zoom) {

        if (knowledge(p, PlanetKnowledge.VISIBLE) < 0 && !showAll) {
            return;
        }
        BufferedImage phase = p.type.body[p.rotationPhase];
        double d = p.diameter * zoom / 4;
        int di = (int)d;
        int x0 = (int)(starmapRect.x + p.x * zoom - d / 2);
        int y0 = (int)(starmapRect.y + p.y * zoom - d / 2);
        g2.drawImage(phase, x0, y0, (int)d, (int)d, null);

        int tw = commons.text().getTextWidth(nameFontSize, p.name());
        int xt = (int)(starmapRect.x + p.x * zoom - tw / 2d);
        int yt = (int)(starmapRect.y + p.y * zoom + d / 2) + 4;
        int labelColor = TextRenderer.GRAY;
        if (p.owner != null && (showAll || knowledge(p, PlanetKnowledge.OWNER) >= 0)) {
            labelColor = p.owner.color;
        }
        if (showPlanetNames && (showAll || knowledge(p, PlanetKnowledge.NAME) >= 0)) {
            commons.text().paintTo(g2, xt, yt, nameFontSize, labelColor, p.name());
        }
        if (p == planet()) {
            if (player().selectionMode == SelectionMode.PLANET) {
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(Color.GRAY);
            }
            g2.drawLine(x0 - 1, y0 - 1, x0 + 2, y0 - 1);
            g2.drawLine(x0 - 1, y0 + di + 1, x0 + 2, y0 + di + 1);
            g2.drawLine(x0 + di - 2, y0 - 1, x0 + di + 1, y0 - 1);
            g2.drawLine(x0 + di - 2, y0 + di + 1, x0 + di + 1, y0 + di + 1);

            g2.drawLine(x0 - 1, y0 - 1, x0 - 1, y0 + 2);
            g2.drawLine(x0 + di + 1, y0 - 1, x0 + di + 1, y0 + 2);
            g2.drawLine(x0 - 1, y0 + di - 2, x0 - 1, y0 + di + 1);
            g2.drawLine(x0 + di + 1, y0 + di - 2, x0 + di + 1, y0 + di + 1);
        }
        if (p.quarantineTTL > 0 && minimapPlanetBlink) {
            g2.setColor(Color.RED);
            g2.drawRect(x0 - 1, y0 - 1, 2 + (int)d, 2 + (int)d);
        }
        if (p.owner == player()) {
            PlanetStatistics ps = getStatistics(p);

            // paint military spaceport icon if any
            if (ps.hasMilitarySpaceport) {
                BufferedImage msi = commons.starmap().militarySpaceportIcon;
                int msix = (int)(x0 + d / 2d - msi.getWidth() / 2d); // xt - msi.getWidth() - 1;
                int msiy = yt + nameFontSize + 2;
                g2.drawImage(msi, msix , msiy, null);
            }

            Set<PlanetProblems> combined = new HashSet<>();
            combined.addAll(ps.problems);
            combined.addAll(ps.warnings);

            if (combined.size() > 0) {
                int w = combined.size() * 11 - 1;
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
                        g2.drawImage(icon, (int)(starmapRect.x + p.x * zoom - w / 2d + i * 11), y0 - 13, null);
                    } else
                    if (ps.hasWarning(pp)) {
                        g2.drawImage(iconDark, (int)(starmapRect.x + p.x * zoom - w / 2d + i * 11), y0 - 13, null);

                    }
                    i++;
                }
            }
        }
    }
    /**
     * Checks if the radar line should be shared with the object?
     * @param o the other object
     * @return true if should be shared
     */
    boolean sharedRadar(Owned o) {
        if (o.owner() != null) {
            DiplomaticRelation dr = world().getRelation(player(), o.owner());
            return dr != null && dr.value >= world().params().radarShareLimit() && !dr.alliancesAgainst.isEmpty();
        }
        return false;
    }
    /**
     * Draw the exploration limit rectangle borders.
     * @param g2 the graphics context
     */
    void renderExplorationLimits(Graphics2D g2) {
        if (player().explorationOuterLimit != null) {

            Area a = new Area(new Rectangle(0, 0, world().galaxyModel.map.getWidth(), world().galaxyModel.map.getHeight()));
            a.subtract(new Area(player().explorationOuterLimit));

            g2.setColor(new Color(0x80000000, true));

            drawShape(g2, a, true);

            g2.setColor(Color.RED);
            drawRect(g2, player().explorationOuterLimit, false);
        } else
        if (player().explorationInnerLimit != null) {
            g2.setColor(new Color(0x80000000, true));
            drawRect(g2, player().explorationInnerLimit, true);
        }
    }
    /**
     * Draw the given rectangle onto the starmap.
     * @param g2 the graphics context
     * @param rect the rectangle
     * @param fill fill it?
     */
    void drawRect(Graphics2D g2, Rectangle rect, boolean fill) {
        double zoom = getZoom();
        int x0 = (int)(starmapRect.x + zoom * rect.x);
        int y0 = (int)(starmapRect.y + zoom * rect.y);
        int w0 = (int)(zoom * rect.width);
        int h0 = (int)(zoom * rect.height);
        if (fill) {
            g2.fillRect(x0, y0, w0, h0);
        } else {
            g2.drawRect(x0, y0, w0, h0);
        }
    }
    /**
     * Draw the given rectangle onto the starmap.
     * @param g2 the graphics context
     * @param shape the shape
     * @param fill fill it?
     */
    void drawShape(Graphics2D g2, Shape shape, boolean fill) {
        double zoom = getZoom();
        AffineTransform at = g2.getTransform();
        g2.translate(starmapRect.x, starmapRect.y);
        g2.scale(zoom, zoom);
        if (fill) {
            g2.fill(shape);
        } else {
            g2.draw(shape);
        }
        g2.setTransform(at);
    }
    /**
     * Add all fleets to the collection.
     * @param fleets the output collection
     */
    void addAllFleets(Collection<Fleet> fleets) {
        for (Player py : world().players.values()) {
            for (Fleet pf : py.fleets.keySet()) {
                if (pf.owner == py) {
                    fleets.add(pf);
                }
            }
        }
    }
    /**

     * Get a planet at the given absolute location.

     * @param x the absolute x
     * @param y the absolute y
     * @param owner the owner
     * @param enemyOnly consider only enemy fleets?
     * @param except the fleet to ignore (usually self)
     * @return a planet or null if not found
     */
    public Fleet getFleetAt(Player owner, int x, int y, boolean enemyOnly, Fleet except) {
        double zoom = getZoom();
        Collection<Fleet> vf = player().visibleFleets();
        if (showAll) {
            vf = new ArrayList<>();
            addAllFleets(vf);
        }
        for (Fleet f : vf) {
            if ((!enemyOnly || f.owner != owner) && (f != except)) {
                int w = f.owner.fleetIcon.getWidth();
                int h = f.owner.fleetIcon.getHeight();
                int x0 = (int)(starmapRect.x + f.x * zoom - w * 0.5);
                int y0 = (int)(starmapRect.y + f.y * zoom - h * 0.5);
                if (x0 <= x && x <= x0 + w && y0 <= y && y <= y0 + h) {
                    DiplomaticRelation dr = world().getRelation(player(), f.owner);
                    boolean strong = dr != null && dr.strongAlliance;
                    if (enemyOnly && f.owner != player()) {
                        if (!strong) {
                            return f;
                        }
                    } else {
                        return f;
                    }
                }
            }
        }
        return null;
    }
    /**

     * Get a planet at the given absolute location.
     * @param owner the owner

     * @param x the absolute x
     * @param y the absolute y
     * @param enemyOnly consider only enemy planets?
     * @return a planet or null if not found
     */
    public Planet getPlanetAt(Player owner, int x, int y, boolean enemyOnly) {
        double zoom = getZoom();
        for (Planet p : commons.world().planets.values()) {
            if (enemyOnly && p.owner == null) {
                continue;
            }
            if ((!enemyOnly || p.owner != owner)

                    && (owner.knowledge(p, PlanetKnowledge.VISIBLE) >= 0 || showAll)) {
                double d = p.diameter * zoom / 4;
                int di = (int)d;
                int x0 = (int)(starmapRect.x + p.x * zoom - d / 2);
                int y0 = (int)(starmapRect.y + p.y * zoom - d / 2);

                if (x0 <= x && x <= x0 + di && y0 <= y && y <= y0 + di) {
                    DiplomaticRelation dr = world().getRelation(player(), p.owner);
                    boolean strong = dr != null && dr.strongAlliance;

                    if (enemyOnly && p.owner != player()) {
                        if (!strong) {
                            return p;
                        }
                    } else {
                        return p;
                    }
                }
            }
        }
        return null;
    }
    /**
     * Convert the screen coordinates to map coordinates.
     * @param x the screen X coordinate
     * @param y the screen Y coordinate
     * @return the map point
     */
    public Point2D.Double toMapCoordinates(int x, int y) {
        double zoom = getZoom();
        double nx = (x - starmapRect.x) / zoom;
        double ny = (y - starmapRect.y) / zoom;
        return new Point2D.Double(nx, ny);
    }
    /**
     * @return computes the current zoom factor.
     */
    public double getZoom() {
        return (minimumZoom + zoomIndex) / 4.0;
    }
    @Override
    public boolean keyboard(KeyEvent e) {
        boolean rep = false;

        if (editNameMode) {
            if (e.getKeyCode() == KeyEvent.VK_PAGE_UP || e.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
                editNameMode = false;
                return false;
            } else
            if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                if (player().selectionMode == SelectionMode.FLEET && fleet() != null) {
                    String fn0 = fleet().name();
                    if (fn0.length() > 0) {
                        fn0 = fn0.substring(0, fn0.length() - 1);
                        fleet().name(fn0);
                    }
                    e.consume();
                    return true;
                } else
                if (player().selectionMode == SelectionMode.PLANET && planet() != null) {
                    String pn0 = planet().customName;
                    if (pn0 == null) {
                        pn0 = planet().name0;
                    }
                    if (pn0.length() > 0) {
                        pn0 = pn0.substring(0, pn0.length() - 1);
                        planet().customName = pn0;
                    } else {
                        planet().customName = null;
                    }
                    e.consume();
                    return true;
                }
                e.consume();
                return true;
            } else
            if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                editNameMode = false;
                e.consume();
                return true;
            } else
            if (commons.text().isSupported(e.getKeyChar())) {
                if (player().selectionMode == SelectionMode.FLEET && fleet() != null) {
                    String fn = fleet().name();
                    fn += e.getKeyChar();
                    fleet().name(fn);
                    e.consume();
                    return true;
                }
                if (player().selectionMode == SelectionMode.PLANET && planet() != null) {
                    String pn0 = planet().customName;
                    if (pn0 == null) {
                        pn0 = planet().name0;
                    }
                    pn0 += e.getKeyChar();
                    planet().customName = pn0;
                    e.consume();
                    return true;
                }
            }
        }

        switch (e.getKeyCode()) {
        case KeyEvent.VK_UP:
            pan(0, 30);
            rep = true;
            e.consume();
            break;
        case KeyEvent.VK_DOWN:
            pan(0, -30);
            rep = true;
            e.consume();
            break;
        case KeyEvent.VK_LEFT:
            pan(30, 0);
            rep = true;
            e.consume();
            break;
        case KeyEvent.VK_RIGHT:
            pan(-30, 0);
            rep = true;
            e.consume();
            break;
        case KeyEvent.VK_V:
            if (e.isControlDown()) {
                showAll = !showAll;
                e.consume();
                rep = true;
            }
            break;
        default:
            char c = Character.toUpperCase(e.getKeyChar());
            if (c == 'M') {
                if (player().selectionMode == SelectionMode.FLEET

                        && fleet() != null
                        && world().scripting.mayControlFleet(fleet())
                        && fleet().owner == player()) {
                    fleetMove.down = true;
                    fleetMove.onPress.invoke();
                    rep = true;
                    e.consume();
                }
            } else
            if (c == 'A') {
                if (player().selectionMode == SelectionMode.FLEET

                        && fleet() != null

                        && world().scripting.mayControlFleet(fleet())
                        && fleet().owner == player()) {
                    fleetAttack.down = true;
                    fleetAttack.onPress.invoke();
                    rep = true;
                    e.consume();
                }
            } else
            if (c == 'S') {
                if (player().selectionMode == SelectionMode.FLEET
                        && fleet() != null
                        && world().scripting.mayControlFleet(fleet())
                        && fleet().owner == player()) {
                    fleetStop.down = true;
                    fleetStop.onPress.invoke();
                    rep = true;
                    e.consume();
                }
            }
        }
        return rep;
    }
    /** Limit the offsets how far they can go. */
    public void limitOffsets() {
        int maxX = starmapRect.width - starmapWindow.width;
        int maxY = starmapRect.height - starmapWindow.height;
        if (xOffset > maxX) {
            xOffset = maxX;
        }
        if (xOffset < 0) {
            xOffset = 0;
        }
        if (yOffset > maxY) {
            yOffset = maxY;
        }
        if (yOffset < 0) {
            yOffset = 0;
        }
    }
    /**
     * Limit the scroll box offset value based on the size of the box and rowheight.
     * @param offset the current offset
     * @param count the total item count
     * @param height the box height
     * @param rowHeight the rowheight
     * @return the corrected offset
     */
    int limitScrollBox(int offset, int count, int height, int rowHeight) {
        int visibleRows = height / rowHeight;
        if (count <= visibleRows || offset <= 0) {
            return 0;
        }
        if (offset > count - visibleRows) {
            return count - visibleRows;
        }
        return offset;

    }
    @Override
    public boolean mouse(UIMouse e) {
        Rectangle fleetsList = new Rectangle(this.fleetsList);
        fleetsList.translate(rightPanel.x, rightPanel.y);
        Rectangle planetsList = new Rectangle(this.planetsList);
        planetsList.translate(rightPanel.x, rightPanel.y);
        Rectangle planetFleetSplitterRange = new Rectangle(this.planetFleetSplitterRange);

        planetFleetSplitterRange.translate(rightPanel.x, rightPanel.y);

        boolean rep = false;
        switch (e.type) {
        case MOVE:
        case DRAG:
            if (panning || commons.isPanningEvent(e)) {
                if (starmapWindow.contains(e.x, e.y)) {
                    if (!panning) {
                        lastX = e.x;
                        lastY = e.y;
                        panning = true;
                    }
                    int dx = e.x - lastX;
                    int dy = e.y - lastY;

                    pan(dx, dy);
                    lastX = e.x;
                    lastY = e.y;
                    rep = true;
                }
            }
            if (scrollX || (e.has(Button.LEFT) && e.has(Type.DRAG) && scrollbarPainter.hscrollKnobRect.contains(e.x, e.y))) {
                if (!scrollX) {
                    lastX = e.x;
                    lastY = e.y;
                    scrollX = true;
                }
                int dx = scrollbarPainter.getHorizontalPanAmount(lastX - e.x);

                pan(dx, 0);
                lastX = e.x;
                lastY = e.y;
                rep = true;
            }
            if (scrollY || (e.has(Button.LEFT) && e.has(Type.DRAG) && scrollbarPainter.vscrollKnobRect.contains(e.x, e.y))) {
                if (!scrollY) {
                    lastX = e.x;
                    lastY = e.y;
                    scrollY = true;
                }
                int dy = scrollbarPainter.getVerticalPanAmount(lastY - e.y);

                pan(0, dy);
                lastX = e.x;
                lastY = e.y;
                rep = true;
            }
            if (mouseDown && !scrollX && !scrollY && !panning) {
                if (e.has(Button.LEFT) && config.showStarmapMinimap && minimapInnerRect.contains(e.x, e.y)) {
                    scrollMinimapTo(e.x - minimapInnerRect.x, e.y - minimapInnerRect.y);
                    rep = true;
                }
                if (e.has(Button.LEFT) && pfSplitter

                        && planetFleetSplitterRange.contains(e.x, e.y)) {
                    planetFleetSplitter = 1.0 * (e.y - planetFleetSplitterRange.y) / (planetFleetSplitterRange.height);
                    fleetsOffset = limitScrollBox(fleetsOffset, player().ownFleets().size(), fleetsList.height, 10);
                    planetsOffset = limitScrollBox(planetsOffset, planets().size(), planetsList.height, 10);
                    rep = true;
                }
            }
            break;
        case DOWN:
//            Point2D.Double d1 = toMapCoordinates(lastX, lastY);
            lastX = e.x;
            lastY = e.y;
            rep |= onMouseDown(e);
//            Point2D.Double d = toMapCoordinates(e.x, e.y);
//            System.out.printf("%s, %s, %s, %s%n", d1.x, d1.y, Math.abs(d.x - d1.x), Math.abs(d.y - d1.y));
            break;
        case DOUBLE_CLICK:
            if (starmapWindow.contains(e.x, e.y)) {
                Planet p = getPlanetAt(player(), e.x, e.y, false);
                Fleet f = getFleetAt(player(), e.x, e.y, false, null);
                if (f != null) {
                    player().currentFleet = f;
                    player().selectionMode = SelectionMode.FLEET;
                    displaySecondary(Screens.EQUIPMENT);
                    editNameMode = false;
                    rep = true;
                } else
                if (p != null) {
                    player().currentPlanet = p;
                    player().selectionMode = SelectionMode.PLANET;
                    displayPrimary(Screens.COLONY);
                    editNameMode = false;
                    rep = true;
                }
            } else

            if (config.showStarmapLists && planetsList.contains(e.x, e.y)) {
                int idx = planetsOffset + (e.y - planetsList.y) / 10;
                List<Planet> planets = planets();
                if (idx < planets.size()) {
                    player().currentPlanet = planets.get(idx);
                    displayPrimary(Screens.COLONY);
                    editNameMode = false;
                }
            } else
            if (config.showStarmapLists && fleetsList.contains(e.x, e.y)) {
                int idx = fleetsOffset + (e.y - fleetsList.y) / 10;
                List<Fleet> fleets = player().ownFleets();
                if (idx < fleets.size()) {
                    player().currentFleet = fleets.get(idx);
                    displaySecondary(Screens.EQUIPMENT);
                    editNameMode = false;
                }
            }
            break;
        case UP:
        case LEAVE:
            panning = false;
            scrollX = false;
            scrollY = false;
            mouseDown = false;
            pfSplitter = false;
            break;
        case WHEEL:
            if (starmapWindow.contains(e.x, e.y)) {
                if (e.has(Modifier.CTRL)) {
                    if (e.z < 0) {
                        doZoomIn(e.x, e.y);
                        rep = true;
                    } else {
                        doZoomOut(e.x, e.y);
                        rep = true;
                    }
                } else
                if (e.has(Modifier.SHIFT)) {
                    if (e.z < 0) {
                        pan(+30, 0);
                    } else {
                        pan(-30, 0);
                    }
                } else {
                    if (e.z < 0) {
                        pan(0, +30);
                    } else {
                        pan(0, -30);
                    }
                }
            } else
            if (config.showStarmapLists && fleetsList.contains(e.x, e.y)) {
                if (e.z < 0) {
                    fleetsOffset--;
                } else {
                    fleetsOffset++;
                }
                List<Fleet> fleets = player().ownFleets();
                fleetsOffset = limitScrollBox(fleetsOffset, fleets.size(), fleetsList.height, 10);
            } else
            if (config.showStarmapLists && planetsList.contains(e.x, e.y)) {
                if (e.z < 0) {
                    planetsOffset--;
                } else {
                    planetsOffset++;
                }
                planetsOffset = limitScrollBox(planetsOffset, planets().size(), planetsList.height, 10);
            }
            break;
        default:
        }
        if (!rep) {
            rep = super.mouse(e);
        }
        return rep;
    }
    /**
     * Check if the mouse coordinate is within the exploration limits of the current user.
     * @param mx the mouse X
     * @param my the mouse Y
     * @return true if within limits
     */
    boolean checkExplorationLimits(int mx, int my) {
        if (!showAll) {
            Point2D.Double pt = toMapCoordinates(mx, my);
            if (player().explorationInnerLimit != null) {
                return player().explorationInnerLimit.contains(pt);
            }
            if (player().explorationOuterLimit != null) {
                return !player().explorationOuterLimit.contains(pt);
            }
        }
        return false;
    }
    /**
     * Mouse down event handler.
     * @param e the mouse event
     * @return true if repaint needed
     */
    boolean onMouseDown(UIMouse e) {
        // compensate for lousy composition
        Rectangle fleetsList = new Rectangle(this.fleetsList);
        fleetsList.translate(rightPanel.x, rightPanel.y);
        Rectangle planetsList = new Rectangle(this.planetsList);
        planetsList.translate(rightPanel.x, rightPanel.y);
        Rectangle planetFleetSplitterRange = new Rectangle(this.planetFleetSplitterRange);

        planetFleetSplitterRange.translate(rightPanel.x, rightPanel.y);
        Rectangle planetFleetSplitterRect = new Rectangle(this.planetFleetSplitterRect);
        planetFleetSplitterRect.translate(rightPanel.x, rightPanel.y);

        boolean rep = false;
        if (((config.classicControls && e.has(Button.MIDDLE))

                || (!config.classicControls && e.has(Button.RIGHT)))
                && starmapWindow.contains(e.x, e.y)) {
            panning = true;
        }
        if (e.has(Button.LEFT) && scrollbarPainter.vscrollKnobRect.contains(e.x, e.y)) {
            scrollY = true;
        }
        if (e.has(Button.LEFT) && scrollbarPainter.hscrollKnobRect.contains(e.x, e.y)) {
            scrollX = true;
        }
        mouseDown = true;
        if (e.has(Button.LEFT) && config.showStarmapMinimap && minimapInnerRect.contains(e.x, e.y)) {
            scrollMinimapTo(e.x - minimapInnerRect.x, e.y - minimapInnerRect.y);
            rep = true;
        }
        if (starmapWindow.contains(e.x, e.y)) {
            if (e.has(Button.MIDDLE)) {
                if (config.classicControls == e.has(Modifier.CTRL)) {
                    zoomToFit();
                }
            } else
            if (player().selectionMode == SelectionMode.FLEET

            && fleet() != null

            && fleet().owner == player()
            && world().scripting.mayControlFleet(fleet())) {
                if (e.has(Button.RIGHT)) {
                    if (checkExplorationLimits(e.x, e.y)) {
                        return false;
                    }

                    if (e.has(Modifier.SHIFT)) {
                        fleet().moveNext(toMapCoordinates(e.x, e.y));

                        fleetMode = null;
                        panning = false;
                    } else

                    if (config.classicControls || e.has(Modifier.CTRL)) {
                        boolean forceAttack = e.has(Modifier.CTRL) || !config.classicControls;

                        // what was the target?
                        Planet p = getPlanetAt(player(), e.x, e.y, true);
                        Fleet f = getFleetAt(player(), e.x, e.y, true, fleet());
                        if (f != null) {
                            if (f.owner == fleet().owner
                                    || (!forceAttack && shouldOnlyFollow(f))) {
                                fleet().follow(f);
                            } else {
                                fleet().attack(f);
                            }
                            fleetMode = null;
                            editNameMode = false;
                        } else
                        if (p != null) {
                            fleetMode = null;
                            if (p.owner == fleet().owner) {

                                fleet().moveTo(p);
                            } else {
                                if (knowledge(p, PlanetKnowledge.OWNER) < 0) {
                                    if (forceAttack) {
                                        effectSound(SoundType.NOT_AVAILABLE);
                                        commons.control().displayError(format("message.cant_attack_that_planet", p.name()));
                                    } else {
                                        fleet().moveTo(p);
                                    }
                                } else {
                                    fleet().attack(p);
                                }
                            }
                            editNameMode = false;
                        } else {
                            // try any fleet
                            f = getFleetAt(player(), e.x, e.y, false, fleet());
                            if (f != null) {
                                if (f.owner == fleet().owner
                                        || (!forceAttack && shouldOnlyFollow(f))) {
                                    fleet().follow(f);
                                } else {
                                    fleet().attack(f);
                                }
                            } else {
                                fleet().moveTo(toMapCoordinates(e.x, e.y));
                            }
                            fleetMode = null;
                            editNameMode = false;
                        }
                        panning = false;
                    }
                }
            }
        }
        if (e.has(Button.LEFT)) {
            if (starmapWindow.contains(e.x, e.y)) {
                if (checkExplorationLimits(e.x, e.y)) {
                    return false;
                }
                if (fleetMode == FleetMode.ATTACK && world().scripting.mayControlFleet(fleet())) {
                    Fleet f = getFleetAt(fleet().owner, e.x, e.y, true, fleet());
                    Planet p = getPlanetAt(fleet().owner, e.x, e.y, true);
                    if (f != null) {
                        fleetMode = null;
                        fleet().attack(f);
                        editNameMode = false;
                    } else
                    if (p != null) {
                        if (knowledge(p, PlanetKnowledge.OWNER) >= 0) {
                            fleetMode = null;
                            fleet().attack(p);
                        } else {
                            effectSound(SoundType.NOT_AVAILABLE);
                            commons.control().displayError(format("message.cant_attack_that_planet", p.name()));
                        }
                        editNameMode = false;
                    }
                } else
                if (fleetMode == FleetMode.MOVE && world().scripting.mayControlFleet(fleet())) {
                    Planet p = getPlanetAt(fleet().owner, e.x, e.y, false);
                    Fleet f = getFleetAt(fleet().owner, e.x, e.y, false, fleet());
                    if (p != null) {
                        fleet().targetPlanet(p);
                        fleet().targetFleet = null;
                        fleet().mode = FleetMode.MOVE;
                        fleet().task = FleetTask.MOVE;
                    } else

                    if (f != null) {
                        fleet().targetPlanet(null);
                        fleet().targetFleet = f;
                        fleet().mode = FleetMode.MOVE;
                        fleet().task = FleetTask.MOVE;
                    } else {
                        fleet().targetPlanet(null);
                        fleet().targetFleet = null;
                        fleet().mode = FleetMode.MOVE;
                        fleet().task = FleetTask.MOVE;
                        fleet().waypoints.clear();
                        fleet().waypoints.add(toMapCoordinates(e.x, e.y));
                    }
                    fleetMode = null;
                    editNameMode = false;
                } else
                if (!e.has(Modifier.CTRL) && !e.has(Modifier.SHIFT)) {
                    selectPlanetOrFleetAt(e);
                }
                rep = true;
            } else
            if (planetFleetSplitterRect.contains(e.x, e.y)

                    && planetFleetSplitterRange.height > 0) {
                pfSplitter = true;
            }
            if (config.showStarmapLists) {
                if (planetsList.contains(e.x, e.y)) {
                    int idx = planetsOffset + (e.y - planetsList.y) / 10;
                    List<Planet> planets = planets();
                    if (idx < planets.size()) {
                        buttonSound(SoundType.CLICK_MEDIUM_2);
                        player().currentPlanet = planets.get(idx);
                        player().selectionMode = SelectionMode.PLANET;
                        editNameMode = false;
                        rep = true;
                    }
                }
                if (fleetsList.contains(e.x, e.y)) {
                    int idx = fleetsOffset + (e.y - fleetsList.y) / 10;
                    List<Fleet> fleets = player().ownFleets();
                    if (idx < fleets.size()) {
                        buttonSound(SoundType.CLICK_MEDIUM_2);
                        player().currentFleet = fleets.get(idx);
                        player().selectionMode = SelectionMode.FLEET;
                        editNameMode = false;
                        rep = true;
                    }
                }
            }
        }
        if (e.has(Button.RIGHT)) {
            if (config.showStarmapLists) {
                if (planetsList.contains(e.x, e.y)) {
                    int idx = planetsOffset + (e.y - planetsList.y) / 10;
                    List<Planet> planets = planets();
                    if (idx < planets.size()) {
                        buttonSound(SoundType.CLICK_MEDIUM_2);
                        player().currentPlanet = planets.get(idx);
                        player().selectionMode = SelectionMode.PLANET;
                        editNameMode = false;

                        double zoom = getZoom();
                        int px = (int)(player().currentPlanet.x * zoom);
                        int py = (int)(player().currentPlanet.y * zoom);

                        pan(xOffset + starmapWindow.width / 2 - px, yOffset + starmapWindow.height / 2 - py);

                        rep = true;
                    }
                }
                if (fleetsList.contains(e.x, e.y)) {
                    int idx = fleetsOffset + (e.y - fleetsList.y) / 10;
                    List<Fleet> fleets = player().ownFleets();
                    if (idx < fleets.size()) {
                        buttonSound(SoundType.CLICK_MEDIUM_2);
                        player().currentFleet = fleets.get(idx);
                        player().selectionMode = SelectionMode.FLEET;
                        editNameMode = false;

                        double zoom = getZoom();
                        int px = (int)(player().currentFleet.x * zoom);
                        int py = (int)(player().currentFleet.y * zoom);

                        pan(xOffset + starmapWindow.width / 2 - px, yOffset + starmapWindow.height / 2 - py);

                        rep = true;
                    }
                }
            }
        }
        return rep;
    }
    /**
     * Decide if the right click should do a follow on the fleet or
     * attack?
     * @param f the target fleet
     * @return true if should follow
     */
    boolean shouldOnlyFollow(Fleet f) {
        // follow same owner
        if (f.owner == player()) {
            return true;
        }
        // follow non-infected traders
        return f.owner.id.equals("Traders")
                && !world().infectedFleets.containsKey(f.id);
    }
    /**
     * Select a planet or fleet at the specified location.
     * @param e the mouse coordinate
     */
    void selectPlanetOrFleetAt(UIMouse e) {
        Planet p = getPlanetAt(player(), e.x, e.y, false);
        Fleet f = getFleetAt(player(), e.x, e.y, false, null);

        if (f != null) {
            buttonSound(SoundType.CLICK_HIGH_2);
            player().currentFleet = f;
            player().selectionMode = SelectionMode.FLEET;
            editNameMode = false;
        } else
        if (p != null) {
            buttonSound(SoundType.CLICK_HIGH_2);
            player().currentPlanet = p;
            player().selectionMode = SelectionMode.PLANET;
            editNameMode = false;
        }
    }
    @Override
    public void onEndGame() {
        cache.clear();
        radarCache.clear();
        newGameStarted = true;
    }
    @Override
    public void onEnter(Screens mode) {
        rotationTimer = commons.register(75, new Action0() {
            @Override
            public void invoke() {
                rotatePlanets();
                askRepaint();
            }
        });
        cache.clear();
        if (newGameStarted) {
            newGameStarted = false;
            zoomToFit();
        }
        editNameMode = false;
    }
    @Override
    public void onFinish() {
    }
    @Override
    public void onInitialize() {
        newGameStarted = true;
        scrollbarPainter = new ScrollBarPainter();
        prevPlanet = new UIImageButton(commons.starmap().backwards);
        prevPlanet.setDisabledPattern(commons.common().disabledPattern);
        nextPlanet = new UIImageButton(commons.starmap().forwards);
        nextPlanet.setDisabledPattern(commons.common().disabledPattern);
        prevFleet = new UIImageButton(commons.starmap().backwards);
        prevFleet.setDisabledPattern(commons.common().disabledPattern);
        nextFleet = new UIImageButton(commons.starmap().forwards);
        nextFleet.setDisabledPattern(commons.common().disabledPattern);
        colony = new UIImageButton(commons.starmap().colony);
        equipment = new UIImageButton(commons.starmap().equipment);
        info = new UIImageButton(commons.common().infoButton);
        bridge = new UIImageButton(commons.common().bridgeButton);

        bottomPanel = new UIContainer();
        rightPanel = new UIContainer();

        prevPlanet.onClick = new Action0() {
            @Override

            public void invoke() {
                buttonSound(SoundType.CLICK_HIGH_2);
                List<Planet> planets = planets();
                int idx = planets.indexOf(planet());
                if (idx > 0 && planets.size() > 0) {
                    player().currentPlanet = planets.get(idx - 1);
                    planetsOffset = limitScrollBox(idx - 1, planets.size(), planetsList.height, 10);
                }
            }
        };
        nextPlanet.onClick = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.CLICK_HIGH_2);
                List<Planet> planets = planets();
                int idx = planets.indexOf(planet());
                if (idx + 1 < planets.size()) {
                    player().currentPlanet = planets.get(idx + 1);
                    planetsOffset = limitScrollBox(idx + 1, planets.size(), planetsList.height, 10);
                }
            }
        };
        prevFleet.onClick = new Action0() {
            @Override

            public void invoke() {
                buttonSound(SoundType.CLICK_HIGH_2);
                List<Fleet> fleets = player().ownFleets();
                int idx = fleets.indexOf(fleet());
                if (idx > 0 && fleets.size() > 0) {
                    player().currentFleet = fleets.get(idx - 1);
                    fleetsOffset = limitScrollBox(idx - 1, fleets.size(), fleetsList.height, 10);
                }
            }
        };
        nextFleet.onClick = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.CLICK_HIGH_2);
                List<Fleet> fleets = player().ownFleets();
                int idx = fleets.indexOf(fleet());
                if (idx + 1 < fleets.size()) {
                    player().currentFleet = fleets.get(idx + 1);
                    fleetsOffset = limitScrollBox(idx + 1, fleets.size(), fleetsList.height, 10);
                }
            }
        };

        colony.onClick = new Action0() {
            @Override
            public void invoke() {
                displayPrimary(Screens.COLONY);
            }

        };
        equipment.onClick = new Action0() {
            @Override
            public void invoke() {
                displaySecondary(Screens.EQUIPMENT);
            }
        };
        info.onClick = new Action0() {
            @Override
            public void invoke() {
                displaySecondary(Screens.INFORMATION_COLONY);
            }
        };
        bridge.onClick = new Action0() {
            @Override
            public void invoke() {
                displayPrimary(Screens.BRIDGE);
            }
        };

        zoom = new UIImageButton(commons.starmap().zoom) {
            @Override
            public boolean mouse(UIMouse e) {
                zoomDirection = (e.has(Button.LEFT));
                return super.mouse(e);
            }
        };
        zoom.setHoldDelay(100);
        zoom.onClick = new Action0() {
            @Override
            public void invoke() {
                if (zoomDirection) {
                    doZoomIn(starmapWindow.width / 2, starmapWindow.height / 2);
                } else {
                    doZoomOut(starmapWindow.width / 2, starmapWindow.height / 2);
                }
            }
        };
        achievements = new UIImageButton(commons.starmap().achievements);
        achievements.onClick = new Action0() {
            @Override
            public void invoke() {
                displaySecondary(Screens.ACHIEVEMENTS);
            }
        };
        statistics = new UIImageButton(commons.starmap().statistics);
        statistics.onClick = new Action0() {
            @Override
            public void invoke() {
                displaySecondary(Screens.STATISTICS);
            }
        };

        colonyName = new UILabel("", 14, commons.text());
        colonyName.onDoubleClick = new Action0() {
            @Override
            public void invoke() {
                editNameMode = true;
            }
        };
        colonyName.tooltip(get("starmap.renameplanet.tooltip"));

        colonyOwner = new UILabel("", 10, commons.text());
        colonySurface = new UILabel("", 10, commons.text());
        colonyPopulationTax = new UILabel("", 10, commons.text());
        colonyOther = new UILabel("", 7, commons.text());

        problemsHouse = new UIImage(commons.common().houseIcon);
        problemsEnergy = new UIImage(commons.common().energyIcon);
        problemsWorker = new UIImage(commons.common().workerIcon);
        problemsFood = new UIImage(commons.common().foodIcon);
        problemsHospital = new UIImage(commons.common().hospitalIcon);
        problemsVirus = new UIImage(commons.common().virusIcon);
        problemsStadium = new UIImage(commons.common().stadiumIcon);
        problemsRepair = new UIImage(commons.common().repairIcon);
        problemsColonyHub = new UIImage(commons.common().colonyHubIcon);
        problemsPolice = new UIImage(commons.common().policeIcon);
        problemsFireBrigade = new UIImage(commons.common().fireBrigadeIcon);
        indicatorMilitarySpaceport = new UIImage(commons.starmap().militarySpaceportIcon);

        surveySatellite = new UIImageButton(commons.starmap().deploySatellite);
        surveySatellite.onClick = new Action0() {
            @Override
            public void invoke() {
                surveySatellite.visible(false);
                deploySatellite("Satellite", "interlude/deploy_satellite");
            }
        };
        spySatellite1 = new UIImageButton(commons.starmap().deploySpySat1);
        spySatellite1.onClick = new Action0() {
            @Override
            public void invoke() {
                spySatellite1.visible(false);
                deploySatellite("SpySatellite1", "interlude/deploy_spy_satellite_1");
            }
        };
        spySatellite2 = new UIImageButton(commons.starmap().deploySpySat2);
        spySatellite2.onClick = new Action0() {
            @Override
            public void invoke() {
                spySatellite2.visible(false);
                deploySatellite("SpySatellite2", "interlude/deploy_spy_satellite_2");
            }
        };
        hubble2 = new UIImageButton(commons.starmap().deployHubble);
        hubble2.onClick = new Action0() {
            @Override
            public void invoke() {
                hubble2.visible(false);
                deploySatellite("Hubble2", "interlude/deploy_hubble");
            }
        };

        showRadarButton = new UIImageToggleButton(commons.starmap().viewRadar);
        showRadarButton.selected = true;
        showRadarButton.onClick = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.CLICK_MEDIUM_2);
            }
        };
        showFleetButton = new UIImageToggleButton(commons.starmap().viewFleet);
        showFleetButton.selected = true;
        showFleetButton.onClick = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.CLICK_MEDIUM_2);
            }
        };
        showStarsButton = new UIImageToggleButton(commons.starmap().viewStar);
        showStarsButton.selected = true;
        showStarsButton.onClick = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.CLICK_MEDIUM_2);
            }
        };
        showGridButton = new UIImageToggleButton(commons.starmap().viewSector);
        showGridButton.selected = true;
        showGridButton.onClick = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.CLICK_MEDIUM_2);
            }
        };

        showNamesNone = new UIImageButton(commons.starmap().namesNone);
        showNamesNone.visible(!showFleetNames && !showPlanetNames);
        showNamesNone.onClick = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.CLICK_MEDIUM_2);
                showNamesNone.visible(false);
                showNamesPlanet.visible(true);
                showFleetNames = false;
                showPlanetNames = true;
            }
        };

        showNamesPlanet = new UIImageButton(commons.starmap().namesPlanets);
        showNamesPlanet.visible(!showFleetNames && showPlanetNames);
        showNamesPlanet.onClick = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.CLICK_MEDIUM_2);
                showNamesPlanet.visible(false);
                showNamesFleet.visible(true);
                showFleetNames = true;
                showPlanetNames = false;
            }
        };

        showNamesFleet = new UIImageButton(commons.starmap().namesFleets);
        showNamesFleet.visible(showFleetNames && !showPlanetNames);
        showNamesFleet.onClick = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.CLICK_MEDIUM_2);
                showNamesFleet.visible(false);
                showNamesBoth.visible(true);
                showFleetNames = true;
                showPlanetNames = true;
            }
        };

        showNamesBoth = new UIImageButton(commons.starmap().namesBoth);
        showNamesBoth.visible(showFleetNames && showPlanetNames);
        showNamesBoth.onClick = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.CLICK_MEDIUM_2);
                showNamesBoth.visible(false);
                showNamesNone.visible(true);
                showFleetNames = false;
                showPlanetNames = false;
            }
        };

        fleetName = new UILabel("", 14, commons.text());
        fleetName.onDoubleClick = new Action0() {
            @Override
            public void invoke() {
                editNameMode = true;
            }
        };
        fleetName.tooltip(get("starmap.renamefleet.tooltip"));
        fleetOwner = new UILabel("", 10, commons.text());
        fleetStatus = new UILabel("", 10, commons.text());
        fleetPlanet = new UILabel("", 10, commons.text());
        fleetComposition = new UILabel("", 7, commons.text());
        fleetSpeed = new UILabel("", 7, commons.text());
        fleetFirepower = new UILabel("", 7, commons.text());
        fleetMove = new UIImageTabButton(commons.starmap().move);
        fleetAttack = new UIImageTabButton(commons.starmap().attack);
        fleetStop = new UIImageTabButton(commons.starmap().stop);
        fleetColonize = new UIImageButton(commons.starmap().colonize);
        fleetColonize.onClick = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.CLICK_MEDIUM_2);
                doColonize();
            }
        };
        fleetColonizeCancel = new UIImageButton(commons.starmap().colonizeCancel);
        fleetColonizeCancel.onClick = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.NOT_AVAILABLE);
                player().colonizationTargets.remove(planet().id);
            }
        };
        setTooltip(fleetColonizeCancel, "starmap.colonize_cancel.tip");

        fleetSeparator = new UIImage(commons.starmap().commandSeparator);

        fleetMove.onPress = new Action0() {
            @Override
            public void invoke() {
                editNameMode = false;
                buttonSound(SoundType.UI_ACKNOWLEDGE_2);
                doFleetMove();
            }
        };
        fleetAttack.onPress = new Action0() {
            @Override
            public void invoke() {
                editNameMode = false;
                buttonSound(SoundType.UI_ACKNOWLEDGE_2);
                doFleetAttack();
            }
        };
        fleetStop.onPress = new Action0() {
            @Override
            public void invoke() {
                editNameMode = false;
                buttonSound(SoundType.NOT_AVAILABLE);
                doFleetStop();
            }
        };

        extraFleet2 = rl.getImage("starmap/fleets/extra_2_fleet");

//        addThis();
        // bottom panel elements

        bottomPanel.add(
            colonyName, colonyOwner, colonySurface, colonyPopulationTax,
            colonyOther,
            problemsHouse, problemsEnergy, problemsWorker, problemsFood,
            problemsHospital, problemsVirus, problemsStadium,
            problemsRepair, problemsColonyHub, problemsPolice, problemsFireBrigade,
            surveySatellite, spySatellite1, spySatellite2, hubble2,
            showRadarButton, showFleetButton, showStarsButton, showGridButton,
            showNamesNone, showNamesFleet, showNamesPlanet, showNamesBoth,
            fleetName, fleetOwner, fleetStatus, fleetPlanet, fleetComposition, fleetSpeed,
            fleetFirepower, fleetMove, fleetStop, fleetColonize, fleetColonizeCancel, fleetAttack,
            fleetSeparator, indicatorMilitarySpaceport
        );

        // right panel elements

        rightPanel.add(
            prevPlanet, nextPlanet,
            prevFleet, nextFleet,
            colony, equipment, info, bridge,
            zoom, achievements, statistics
        );

        add(bottomPanel, rightPanel);
    }
    /**
     * Deploy a satellite with an animation.
     * @param typeId the satellite id
     * @param media the media to play
     */
    void deploySatellite(final String typeId, String media) {
        final Planet p = planet();
        final boolean isPaused = commons.simulation.paused();
        if (!isPaused) {
            commons.simulation.pause();
        }
        if (config.satelliteDeploy) {

            final int volume = config.musicVolume;
            commons.music.setVolume(0);
            commons.control().playVideos(new Action0() {
                @Override
                public void invoke() {
                    placeSatellite(typeId, p, isPaused);
                    commons.music.setVolume(volume);
                }
            }, media);
        } else {
            placeSatellite(typeId, p, isPaused);
        }
    }
    @Override
    public void onLeave() {
        close0(rotationTimer);
        rotationTimer = null;
        editNameMode = false;
    }
    @Override
    public void onResize() {
        computeRectangles();
    }
    /**
     * Paint the radar circle.
     * @param g2 the graphics object.
     * @param x the center coordinate
     * @param y the center coordinate
     * @param radius the radius
     * @param zoom the zoom factor
     */
    void paintRadar(Graphics2D g2, double x, double y, float radius, double zoom) {
        if (radius <= 0) {
            radius = 0.3f * 35;
        }
        double angle = 0;
        int n = (int)(2 * radius * Math.PI * zoom / 10);
        double dangle = Math.PI * 2 / n;
        while (angle < 2 * Math.PI) {

            double rx = (x + Math.cos(angle) * radius) * zoom + starmapRect.x;
            double ry = (y + Math.sin(angle) * radius) * zoom + starmapRect.y;

            g2.drawImage(radarDot, (int)rx, (int)ry, null);

            angle += dangle;
        }
    }
    /** Caches the radar settings. */
    static class RadarCache {
        /** The zoom value associated. */
        double zoom;
        /** The area to fill in. */
        Area radarArea;
        /** The radar dots. */
        final List<Point> dots = new ArrayList<>();
        /** The old radar circle. */
        final Set<RadarCircle> old = new HashSet<>();
        /**

         * Test if the old circles are still the same as the new circles.
         * @param that the another list
         * @return true if same
         */
        boolean changed(List<RadarCircle> that) {
            if (old.size() != that.size()) {
                return true;
            }
            return !old.containsAll(that);
        }
        /** Clear current values. */
        public void clear() {
            zoom = -1;
            old.clear();
            dots.clear();
            radarArea = null;
        }
    }
    /** The radar cache. */
    final RadarCache radarCache = new RadarCache();
    /**
     * Render a set of radar circles but only those points which are at the external polygons
     * (e.g., not within another circle).
     * @param g2 the graphics context
     * @param circles the circles
     * @param zoom the zoom level
     */
    void paintRadar(Graphics2D g2, List<RadarCircle> circles, double zoom) {
        // check cache changes
        if (Math.abs(zoom - radarCache.zoom) > 0.001 || radarCache.changed(circles)) {
            radarCache.zoom = zoom;
            radarCache.radarArea = null;
            radarCache.dots.clear();
            radarCache.old.clear();
            radarCache.old.addAll(circles);
            for (RadarCircle c : circles) {
                double rx = c.x * zoom;
                double ry = c.y * zoom;
                double rr = c.r * zoom * 2;

                Ellipse2D.Double e = new Ellipse2D.Double(rx - rr / 2, ry - rr / 2, rr, rr);
                if (radarCache.radarArea != null) {
                    radarCache.radarArea.add(new Area(e));
                } else {
                    radarCache.radarArea = new Area(e);
                }

                double angle = 0;
                int n = (int)(2 * c.r * Math.PI * zoom / 10);
                double dangle = Math.PI * 2 / n;
                while (angle < 2 * Math.PI) {

                    double ix = c.x + Math.cos(angle) * c.r;
                    double iy = c.y + Math.sin(angle) * c.r;

                    boolean in = false;
                    for (RadarCircle c2 : circles) {
                        if (c2 != c && c2.in(ix, iy)) {
                            in = true;
                            break;
                        }
                    }
                    if (!in) {
                        double rx0 = ix * zoom;
                        double ry0 = iy * zoom;

                        radarCache.dots.add(new Point((int)rx0, (int)ry0));
                    }

                    angle += dangle;
                }

            }
        }
        g2.translate(starmapRect.x, starmapRect.y);
        if (radarCache.radarArea != null) {
            g2.setColor(new Color(128, 0, 0, 32));
            g2.fill(radarCache.radarArea);
        }
        for (Point p : radarCache.dots) {
            g2.drawImage(radarDot, p.x, p.y, null);
        }
        g2.translate(-starmapRect.x, -starmapRect.y);
    }
    /**
     * Pan the starmap with the given pixels.
     * @param dx the horizontal difference
     * @param dy the vertical difference
     */
    public void pan(int dx, int dy) {
        xOffset -= dx;
        yOffset -= dy;
        // limit the offset
        limitOffsets();
    }
    /** @return the player's own planets. */
    public List<Planet> planets() {
        List<Planet> pls = player().ownPlanets();
        Collections.sort(pls, Planet.NAME_ORDER);
        return pls;
    }
    /**
     * @param x the mouse coordinate
     * @param y the mouse coordinate
     * @param newIndex the new zoom index
     */
    private void readjustZoom(int x, int y, int newIndex) {
        double pre = getZoom();

        double vx = (x - starmapRect.x) / pre;
        double vy = (y - starmapRect.y) / pre;

        zoomIndex = newIndex;

        double post = getZoom();

        selectRadarDot();
        limitOffsets();
        computeViewport();

        double vx2 = (x - starmapRect.x) / post;
        double vy2 = (y - starmapRect.y) / post;

        xOffset -= (vx2 - vx) * post;
        yOffset -= (vy2 - vy) * post;

        limitOffsets();
        computeViewport();

    }
    /**
     * Rotate the planets on screen.
     */
    protected void rotatePlanets() {
        for (Planet p : commons.world().planets.values()) {
            if (p.rotationDirection == RotationDirection.LR) {
                p.rotationPhase = (p.rotationPhase + 1) % p.type.body.length;
            } else {
                if (p.rotationPhase > 0) {
                    p.rotationPhase = (p.rotationPhase - 1) % p.type.body.length;
                } else {
                    p.rotationPhase = p.type.body.length - 1;
                }
            }
        }
        minimapPlanetBlink = blinkCounter < 6;
        blinkCounter = (blinkCounter + 1) % 12;
    }
    @Override
    public Screens screen() {
        return Screens.STARMAP;
    }
    /**
     * Scroll to the given minimap relative coordinate.
     * @param x the click on the minimap
     * @param y the click on the minimap
     */
    public void scrollMinimapTo(int x, int y) {
        xOffset = x * starmapRect.width / minimapInnerRect.width - starmapWindow.width / 2;
        yOffset = y * starmapRect.height / minimapInnerRect.height - starmapWindow.height / 2;
        limitOffsets();
        computeViewport();
    }
    /**
     * Select the radar dot image for the current zoom level.
     */
    void selectRadarDot() {
        radarDot = commons.starmap().radarDots[commons.starmap().radarDots.length * zoomIndex / (zoomLevelCount + 1)];
    }
    /** Move the fleet (turns on movement selection mode. */
    void doFleetMove() {
        fleetMove.down = true;
        fleetAttack.down = false;
        fleetStop.down = false;
        fleetMode = FleetMode.MOVE;
    }
    /** Attack with the fleet (turns on attach selection mode. */
    void doFleetAttack() {
        fleetMove.down = false;
        fleetAttack.down = true;
        fleetStop.down = false;
        fleetMode = FleetMode.ATTACK;
    }
    /** Stop the current fleet. */
    void doFleetStop() {
        fleetMove.down = false;
        fleetAttack.down = false;
        fleetStop.down = true;
        fleet().stop();
        fleetMode = null;
    }
    /** Colonize the fleet nearby planet. */
    void doColonize() {
        if (player().selectionMode == SelectionMode.FLEET) {
            Fleet f = fleet();
            if (f == null) {
                return;
            }
            Planet p = f.getStatistics().planet;
            if (p == null || p.owner != null) {
                commons.control().displayError(get("starmap.colony_taken"));
                effectSound(SoundType.NOT_AVAILABLE);
                return;
            }
            if (f.colonize(p)) {
                displayPrimary(Screens.COLONY);
                return;
            }
            commons.control().displayError(get("starmap.colonization_failed"));
        } else {
            Planet p = planet();
            if (p == null || p.owner != null) {
                commons.control().displayError(get("starmap.colony_taken"));
                effectSound(SoundType.NOT_AVAILABLE);
                return;
            }
            player().colonizationTargets.add(p.id);
        }
    }
    /** @return the current X offset. */
    public int getXOffset() {
        return xOffset;
    }
    /** @return the current Y offset. */
    public int getYOffset() {
        return yOffset;
    }
    /** @return the current zoom index. */
    public int getZoomIndex() {
        return zoomIndex;
    }
    /**
     * Set the X offset.
     * @param x the X offset
     */
    public void setXOffset(int x) {
        xOffset = x;
        pan(0, 0);
    }
    /**
     * Set the Y offset.
     * @param y the Y offset
     */
    public void setYOffset(int y) {
        yOffset = y;
        pan(0, 0);
    }
    /**
     * Set the zoom index.
     * @param z the zoom index
     */
    public void setZoomIndex(int z) {
        zoomIndex = Math.max(0, Math.min(z, zoomLevelCount));
        selectRadarDot();
        limitOffsets();
        computeViewport();
    }
    /**
     * Place satellite around the given planet and resume simulation if needed.
     * @param typeId the satellite id
     * @param p the target planet
     * @param isPaused was the game paused
     */
    void placeSatellite(final String typeId, final Planet p,
            final boolean isPaused) {
        ResearchType rt = world().researches.get(typeId);

        DefaultAIControls.actionDeploySatellite(player(), p, rt);

        if (!isPaused) {
            commons.simulation.resume();
        }
    }
    /**
     * Zoom in to fit the available window.
     */
    public void zoomToFit() {
        int mw = starmapWindow.width;
        int mh = starmapWindow.height;
        int w = world().galaxyModel.map.getWidth();
        int h = world().galaxyModel.map.getHeight();

        if (player().explorationOuterLimit != null) {
            w = player().explorationOuterLimit.width;
            h = player().explorationOuterLimit.height;
        }

        double s1 = 1.0 * mw / w;
        double s2 = 1.0 * mh / h;
        double s = Math.min(s1, s2);
        double s0 = Math.min(4, Math.max(0.5, s));

        int zi = (int)(s0 * 4 - minimumZoom);

        readjustZoom(mw / 2, mh / 2, zi);
        if (player().explorationOuterLimit != null) {
            xOffset = 0;
            yOffset = 0;
            double zoom = getZoom();

            double dw = player().explorationOuterLimit.width * zoom;
            double dh = player().explorationOuterLimit.height * zoom;

            pan(-(int)(player().explorationOuterLimit.x * zoom - (mw - dw) / 2),

                    -(int)(player().explorationOuterLimit.y * zoom - (mh - dh) / 2));
        }
    }
    /** Show names mode. */
    public enum ShowNamesMode {
        /** Show both fleets and planets. */
        BOTH,
        /** Show fleets only. */
        FLEETS,
        /** Show planets only. */
        PLANETS,
        /** Show none. */
        NONE
    }
    /**
     * Returns the current show-names mode.
     * @return the current show-names mode
     */
    public ShowNamesMode showNames() {
        if (showNamesBoth.visible()) {
            return ShowNamesMode.BOTH;
        } else
        if (showNamesFleet.visible()) {
            return ShowNamesMode.FLEETS;
        } else
        if (showNamesPlanet.visible()) {
            return ShowNamesMode.PLANETS;
        }
        return ShowNamesMode.NONE;
    }
    /**
     * Sets the show-names mode.
     * @param mode the mode
     */
    public void showNames(ShowNamesMode mode) {
        showNamesBoth.visible(mode == ShowNamesMode.BOTH);
        showNamesFleet.visible(mode == ShowNamesMode.FLEETS);
        showNamesPlanet.visible(mode == ShowNamesMode.PLANETS);
        showNamesNone.visible(mode == ShowNamesMode.NONE);

        showFleetNames = mode == ShowNamesMode.BOTH || mode == ShowNamesMode.FLEETS;
        showPlanetNames = mode == ShowNamesMode.BOTH || mode == ShowNamesMode.PLANETS;
    }
    /**
     * Retrieves and caches the planet statistics.
     * @param p the planet
     * @return the statistics
     */
    public PlanetStatistics getStatistics(Planet p) {
        PlanetStatistics ps = cache.get(p);
        if (ps == null) {
            ps = p.getStatistics();
            cache.put(p, ps);
        }
        return ps;
    }
    /**
     * Draw a simple arrow.
     * @param g2 the graphics
     * @param x0 the X coordinate of the start point
     * @param y0 the Y coordinate of the start point
     * @param x1 the X coordinate of the end point
     * @param x1 the X coordinate of the end point
     */
    public void drawArrow(Graphics2D g2, int x0, int y0, int x1, int y1) {
        int headAngle = 30;
        int headLength = 10;
        double offs = headAngle * Math.PI / 180.0;
        double angle = Math.atan2(y0 - y1, x0 - x1);
        int[] xs = { x1 + (int) (headLength * Math.cos(angle + offs)), x1,
                x1 + (int) (headLength * Math.cos(angle - offs)) };
        int[] ys = { y1 + (int) (headLength * Math.sin(angle + offs)), y1,
                y1 + (int) (headLength * Math.sin(angle - offs)) };
        g2.drawLine(x0, y0, x1, y1);
        g2.drawPolyline(xs, ys, 3);
    }
}
