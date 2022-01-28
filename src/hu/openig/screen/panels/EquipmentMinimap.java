/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.model.Fleet;
import hu.openig.model.FleetKnowledge;
import hu.openig.model.Planet;
import hu.openig.model.PlanetKnowledge;
import hu.openig.model.SelectionMode;
import hu.openig.model.SoundTarget;
import hu.openig.model.SoundType;
import hu.openig.render.RenderTools;
import hu.openig.screen.CommonResources;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;

/**
 * A zoomed-in minimap with fleets and planets displayed via name.
 * @author akarnokd, 2011.04.14.
 */
public class EquipmentMinimap extends UIComponent {
    /** The commons. */
    protected final CommonResources commons;
    /** The zoom-in level. */
    public float zoom = 2;
    /** The rectangle of the map. */
    final Rectangle mapRect = new Rectangle();
    /** The last right-down. */
    int lastx;
    /** The last right-down. */
    int lasty;
    /** Are we in pan mode? */
    public boolean panMode;
    /**
     * Construct a minimap.
     * @param commons the commons
     */
    public EquipmentMinimap(CommonResources commons) {
        this.commons = commons;
    }
    @Override
    public void draw(Graphics2D g2) {
        Shape save0 = g2.getClip();
        g2.clipRect(0, 0, width, height);
        BufferedImage map = commons.world().galaxyModel.map;
        mapRect.setSize((int)(map.getWidth() * zoom), (int)(map.getHeight() * zoom));
        g2.translate(-mapRect.x, -mapRect.y);
        g2.drawImage(map, 0, 0, mapRect.width, mapRect.height, null);
        RenderTools.paintGrid(g2, new Rectangle(0, 0, mapRect.width, mapRect.height), commons.starmap().gridColor, commons.text());

        for (Planet p : commons.world().planets.values()) {
            if (p.owner != null && commons.world().player.knowledge(p, PlanetKnowledge.OWNER) >= 0) {
                int cx = (int)(p.x * zoom);
                int cy = (int)(p.y * zoom);

                g2.setColor(new Color(p.owner.color));
                g2.fillRect(cx - 1, cy - 1, 3, 3);

                if (commons.world().player.currentPlanet == p) {
                    if (commons.world().player.selectionMode == SelectionMode.PLANET) {
                        g2.setColor(Color.WHITE);
                    } else {
                        g2.setColor(Color.LIGHT_GRAY);
                    }
                    g2.drawRect(cx - 3, cy - 3, 6, 6);
                }
                int nw = commons.text().getTextWidth(7, p.name());
                commons.text().paintTo(g2, cx - nw / 2, cy + 8, 7, p.owner.color, p.name());
            }
        }
        for (Fleet f : commons.world().player.fleets.keySet()) {
            int cx = (int)(f.x * zoom);
            int cy = (int)(f.y * zoom);

            g2.drawImage(f.owner.fleetIcon, cx - f.owner.fleetIcon.getWidth() / 2, cy - f.owner.fleetIcon.getHeight() / 2, null);

            String fleetName = f.name();
            if (commons.world().player.knowledge(f, FleetKnowledge.VISIBLE) == 0) {
                fleetName = commons.labels().get("fleetinfo.alien_fleet");
            }

            int nw = commons.text().getTextWidth(7, fleetName);
            commons.text().paintTo(g2, cx - nw / 2, cy + f.owner.fleetIcon.getHeight() / 2 + 3, 7,

                    f.owner.color, fleetName);
            if (f == commons.world().player.currentFleet) {
                if (commons.world().player.selectionMode == SelectionMode.FLEET) {
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(Color.LIGHT_GRAY);
                }
                g2.drawRect(cx - f.owner.fleetIcon.getWidth() / 2 - 1, cy - f.owner.fleetIcon.getHeight() / 2 - 1, f.owner.fleetIcon.getWidth() + 2, f.owner.fleetIcon.getHeight() + 2);
            }
        }

        g2.translate(mapRect.x, mapRect.y);
        g2.setClip(save0);
    }
    @Override
    public boolean mouse(UIMouse e) {
        if (e.has(Button.RIGHT)) {
            if (e.has(Type.DOWN)) {
                lastx = e.x;
                lasty = e.y;
                panMode = true;
            } else
            if (e.has(Type.UP)) {
                panMode = false;
            } else
            if (e.has(Type.DRAG) && panMode) {
                pan(lastx - e.x, lasty - e.y);
                lastx = e.x;
                lasty = e.y;
                return true;
            }

        } else
        if (e.has(Type.LEAVE)) {
            panMode = false;
        } else
        if (e.has(Button.LEFT)) {
            doSelect(e.x, e.y);
        }
        return super.mouse(e);
    }
    /**
     * Pan the view by the given amount in each direction.
     * @param dx the delta x to pan
     * @param dy the delta y to pan
     */
    public void pan(int dx, int dy) {
        mapRect.x = Math.max(0, Math.min(mapRect.x + dx, mapRect.width - width));
        mapRect.y = Math.max(0, Math.min(mapRect.y + dy, mapRect.height - height));
    }
    /**
     * Scroll the view to the given map coordinates.
     * @param x the X coordinate
     * @param y the Y coordinate
     */
    public void moveTo(double x, double y) {
        BufferedImage map = commons.world().galaxyModel.map;
        mapRect.setSize((int)(map.getWidth() * zoom), (int)(map.getHeight() * zoom));
        mapRect.x = (int)Math.max(0, Math.min(x * zoom - width / 2d, mapRect.width - width));
        mapRect.y = (int)Math.max(0, Math.min(y * zoom - height / 2d, mapRect.height - height));
    }
    /**

     * Select the entity at the given location.
     * @param x the X coordinate
     * @param y the Y coordinate
     */
    void doSelect(int x, int y) {
        for (Fleet f : commons.world().player.fleets.keySet()) {
            int cx = (int)(f.x * zoom) - mapRect.x;
            int cy = (int)(f.y * zoom) - mapRect.y;
            int w = f.owner.fleetIcon.getWidth();
            int h = f.owner.fleetIcon.getHeight();
            if (cx - w / 2 - 1 <= x && x <= cx + w / 2 + 1 && cy - h / 2 - 1 <= y && y <= cy + h / 2 + 1) {
                commons.playSound(SoundTarget.BUTTON, SoundType.CLICK_HIGH_2, null);
                commons.world().player.currentFleet = f;
                commons.world().player.selectionMode = SelectionMode.FLEET;
                return;
            }
        }
        for (Planet p : commons.world().planets.values()) {
            if (p.owner != null && commons.world().player.knowledge(p, PlanetKnowledge.OWNER) >= 0) {
                int cx = (int)(p.x * zoom) - mapRect.x;
                int cy = (int)(p.y * zoom) - mapRect.y;
                if (cx - 2 <= x && x <= cx + 2 && cy - 2 <= y && y <= cy + 2) {
                    commons.playSound(SoundTarget.BUTTON, SoundType.CLICK_HIGH_2, null);
                    commons.world().player.currentPlanet = p;
                    commons.world().player.selectionMode = SelectionMode.PLANET;
                    return;
                }
            }
        }
    }
}
