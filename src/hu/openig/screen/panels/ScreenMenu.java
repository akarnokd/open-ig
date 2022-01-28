/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.model.Screens;
import hu.openig.model.WalkPosition;
import hu.openig.render.TextRenderer;
import hu.openig.screen.CommonResources;
import hu.openig.screen.api.LoadSaveScreenAPI;
import hu.openig.screen.api.SettingsPage;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Map;

/**
 * A popup menu to switch to arbitrary screen by the mouse.
 * @author akarnokd, 2011.04.20.
 */
public class ScreenMenu extends UIContainer {
    /** The common resources. */
    final CommonResources commons;
    /** The current highlight index. */
    public int highlight = -1;
    /** The screen name labels. */
    final String[] labels = {
        "screens.bridge",
        "screens.starmap",
        "screens.colony",
        "screens.equipment",
        "screens.production",
        "screens.research",
        "screens.information",
        "screens.database",
        "screens.bar",
        "screens.diplomacy",
        "screens.spying",
        "screens.trade",
        "screens.statistics",
        "screens.achievements",
        "screens.loadsave",
        "screens.options",
    };
    /**

     * Constructor. Sets the dimensions.
     * @param commons the common resources

     */
    public ScreenMenu(CommonResources commons) {
        this.commons = commons;
        height = 18 * labels.length + 10;
        width = 0;
        for (String s : labels) {
            width = Math.max(width, commons.text().getTextWidth(14, commons.get(s)));
        }
        width += 10;
    }
    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 224));
        g2.fillRect(0, 0, width, height);
        g2.setColor(Color.GRAY);
        g2.drawRect(0, 0, width - 1, height - 1);
        int y = 7;
        int idx = 0;
        for (String s0 : labels) {
            String s = commons.get(s0);
            int c = idx == highlight ? TextRenderer.WHITE : TextRenderer.ORANGE;
            if (isScreenDisabled(idx)) {
                c = TextRenderer.GRAY;
            }
            commons.text().paintTo(g2, 5, y, 14, c, s);
            y += 18;
            idx++;
        }
    }
    /**
     * Check if the given screen index is disabled in the current state.
     * @param idx the screen index
     * @return true if disabled
     */
    boolean isScreenDisabled(int idx) {
        Map<String, WalkPosition> positions = commons.world().getShip().positions;
        return (idx == 4 && commons.world().level < 2)

                || (idx == 5 && commons.world().level < 3)
                || (idx == 8 && !positions.containsKey("*bar"))
                || (idx == 9 && !positions.containsKey("*diplomacy"))
                || (idx < 14 && commons.battleMode);
    }
    @Override
    public boolean mouse(UIMouse e) {
        int idx = (e.y - 7) / 18;
        if (idx >= 0 && idx < labels.length) {
            highlight = idx;
        } else {
            highlight = -1;
        }
        if (!isScreenDisabled(highlight)) {
            if (e.has(Type.UP)) {
                switchScreen();
            }
        } else {
            if (e.has(Type.UP)) {
                visible(false);
            }
        }
        super.mouse(e);
        return true;
    }
    /**
     * Switch to the highlighted screen or hide the menu.
     */
    void switchScreen() {
        switch (highlight) {
        case 0:
            commons.control().displayPrimary(Screens.BRIDGE);
            break;
        case 1:
            commons.control().displayPrimary(Screens.STARMAP);
            break;
        case 2:
            commons.control().displayPrimary(Screens.COLONY);
            break;
        case 3:
            commons.control().displaySecondary(Screens.EQUIPMENT);
            break;
        case 4:
            if (commons.world().level >= 2) {
                commons.control().displaySecondary(Screens.PRODUCTION);
            }
            break;
        case 5:
            if (commons.world().level >= 3) {
                commons.control().displaySecondary(Screens.RESEARCH);
            }
            break;
        case 6:
            commons.control().displaySecondary(Screens.INFORMATION_PLANETS);
            break;
        case 7:
            commons.control().displaySecondary(Screens.DATABASE);
            break;
        case 8:
            if (commons.world().getShip().positions.containsKey("*bar")) {
                commons.control().displaySecondary(Screens.BAR);
            }
            break;
        case 9:
            if (commons.world().getShip().positions.containsKey("*diplomacy")) {
                commons.control().displaySecondary(Screens.DIPLOMACY);
            }
            break;
        case 10:
            commons.control().displaySecondary(Screens.SPYING);
            break;
        case 11:
            commons.control().displaySecondary(Screens.TRADE);
            break;
        case 12:
            commons.control().displaySecondary(Screens.STATISTICS);
            break;
        case 13:
            commons.control().displaySecondary(Screens.ACHIEVEMENTS);
            break;
        case 14:
            commons.control().displayOptions();
            LoadSaveScreenAPI scr = (LoadSaveScreenAPI)commons.control().getScreen(Screens.LOAD_SAVE);
            scr.maySave(!commons.battleMode);
            scr.displayPage(SettingsPage.LOAD_SAVE);
            break;
        case 15:
            commons.control().displayOptions();
            scr = (LoadSaveScreenAPI)commons.control().getScreen(Screens.LOAD_SAVE);
            scr.maySave(!commons.battleMode);
            scr.displayPage(SettingsPage.AUDIO);
            break;
        default:
        }
        visible(false);
    }
}
