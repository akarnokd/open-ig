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

import hu.openig.core.Action0;
import hu.openig.model.Screens;
import hu.openig.render.RenderTools;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.*;

/**
 * Screen to specify how to abandon the current planet.
 * @author akarnokd, 2022.01.30.
 */
public class AbandonColonyScreen extends ScreenBase {
    /** The panel base rectangle. */
    final Rectangle base = new Rectangle(0, 0, 500, 350);
    /** The select traits label. */
    UILabel titleLabel;
    /** Cancel selection. */
    UIGenericButton cancel;
    @Override
    public void onResize() {
        base.width = commons.common().infoEmptyTop.getWidth();
        base.y = 10;

        RenderTools.centerScreen(base, width, height, true);

        titleLabel.location(base.x + 10, base.y + 10);

        int w = cancel.width;
        cancel.location(base.x + (640 - w) / 2, base.y + base.height - 40);
    }
    @Override
    public Screens screen() {
        return Screens.ABANDON_COLONY;
    }

    @Override
    public void onInitialize() {
        titleLabel = new UILabel(get("abandoncolony.title"), 20, commons.text());

        cancel = new UIGenericButton(get("abandoncolony.cancel"), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
        cancel.onClick = new Action0() {
            @Override
            public void invoke() {
                hideSecondary();
            }
        };

        addThis();
    }

    /** Perform a partial repaint. */
    void doRepaint() {
        scaleRepaint(base, base, margin());
    }
    @Override
    public void onEnter(Screens mode) {
        // no action needed
    }

    @Override
    public void onLeave() {
        // no cleanup needed
    }

    @Override
    public void onFinish() {
        // no cleanup needed
    }

    @Override
    public void onEndGame() {
        // not an ingame screen
    }
    @Override
    public void draw(Graphics2D g2) {
        RenderTools.darkenAround(base, width, height, g2, 0.5f, true);

        g2.setColor(Color.BLACK);
        g2.fill(base);
        g2.setColor(Color.GRAY);
        g2.draw(base);

        g2.drawLine(base.x, cancel.y - 5, base.x + base.width - 1, cancel.y - 5);
        g2.drawLine(base.x, cancel.y + cancel.height + 5, base.x + base.width - 1, cancel.y + cancel.height + 5);

        super.draw(g2);
    }
    @Override
    public boolean keyboard(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            e.consume();
            cancel.onClick.invoke();
            return true;
        }
        return false;
    }
}
