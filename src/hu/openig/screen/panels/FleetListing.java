/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.model.Fleet;
import hu.openig.render.TextRenderer;
import hu.openig.screen.CommonResources;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

/**
 * List fleets of the player.
 * @author akarnokd, 2011.04.14.
 */
public class FleetListing extends UIContainer {
    /** The common resources. */
    final CommonResources commons;
    /** Scroll up button. */
    final UIImageButton scrollUp;
    /** Scroll down button. */
    final UIImageButton scrollDown;
    /** If a fleet is selected. */
    public Action1<Fleet> onSelect;
    /** List only nearby fleets? */
    public boolean nearby;
    /** The top index. */
    int top;
    /** The text height. */
    int textHeight = 10;
    /** The row height. */
    int rowHeight = 13;
    /**
     * Construct the listing.
     * @param commons the common resources
     */
    public FleetListing(CommonResources commons) {
        this.commons = commons;
        scrollUp = new UIImageButton(commons.common().moveUp);
        scrollUp.onClick = new Action0() {
            @Override
            public void invoke() {
                doScrollUp();
            }
        };
        scrollUp.setHoldDelay(100);
        scrollDown = new UIImageButton(commons.common().moveDown);
        scrollDown.onClick = new Action0() {
            @Override
            public void invoke() {
                doScrollDown();
            }
        };
        scrollDown.setHoldDelay(100);

        addThis();
    }
    /**
     * Lists the fleets within 20 distance.
     * @return the list of fleets
     */
    List<Fleet> nearbyFleets() {
        Fleet cf = commons.world().player.currentFleet;
        List<Fleet> fleets;
        if (nearby && cf != null) {
            fleets = cf.fleetsInRange(20);
//            cf = selected;
        } else {
            fleets = commons.world().player.ownFleets();
        }
        for (int i = fleets.size() - 1; i >= 0; i--) {
            if (!commons.world().scripting.mayControlFleet(fleets.get(i))) {
                fleets.remove(i);
            }
        }
        return fleets;
    }
    @Override
    public void draw(Graphics2D g2) {
        int rows = height / rowHeight;
        List<Fleet> fleets = nearbyFleets();

        scrollUp.location(width - scrollUp.width, 0);
        scrollUp.visible(top > 0);
        scrollDown.location(width - scrollUp.width, height - scrollDown.height);
        scrollDown.visible(top + rows < fleets.size());

        if (top < 0) {
            top = 0;
        }

        g2.setColor(new Color(0, 0, 0, 192));
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.LIGHT_GRAY);
        g2.drawRect(0, 0, width - 1, height - 1);

        for (int i = top; i < fleets.size() && i < top + rows; i++) {
            Fleet f = fleets.get(i);
            int c = TextRenderer.GREEN;
            if (f == commons.world().player.currentFleet) {
                c = TextRenderer.RED;
            }
            commons.text().paintTo(g2, 4, i * rowHeight + 2, textHeight, c, f.name());
        }

        super.draw(g2);
    }
    @Override
    public boolean mouse(UIMouse e) {
        if (e.has(Button.LEFT) && e.has(Type.DOWN) && onSelect != null && e.x < width - scrollUp.width - 2) {
            int row = (e.y - top) / rowHeight;
            List<Fleet> fleet = nearbyFleets();
            if (row >= 0 && row < fleet.size()) {
                onSelect.invoke(fleet.get(row));
                return true;
            }
        } else
        if (e.has(Type.WHEEL)) {
            if (e.z < 0) {
                doScrollUp();
            } else {
                doScrollDown();
            }
            return true;
        }
        return super.mouse(e);
    }
    /** Scroll up. */
    void doScrollUp() {
        top = Math.max(0, top - 1);
    }
    /** Scroll down. */
    void doScrollDown() {
        int rows = height / rowHeight;

        List<Fleet> fleet;
        Fleet cf = commons.world().player.currentFleet;
        if (nearby && cf != null) {
            fleet = cf.fleetsInRange(20);
        } else {
            fleet = commons.world().player.ownFleets();
        }

        top = Math.max(0, Math.min(top + 1, fleet.size() - rows));
    }
    /**
     * Scroll to the point where the given fleet is visible.
     * @param f the fleet
     */
    public void show(Fleet f) {
        List<Fleet> fleet;
        Fleet cf = commons.world().player.currentFleet;
        if (nearby && cf != null) {
            fleet = cf.fleetsInRange(20);
        } else {
            fleet = commons.world().player.ownFleets();
        }

        int idx = fleet.indexOf(f);
        if (idx >= 0) {
            if (idx < top) {
                top = idx;
            } else {
                int rows = height / rowHeight;
                if (idx >= top + rows) {
                    top = idx - rows;
                }
            }
        }
    }
}
