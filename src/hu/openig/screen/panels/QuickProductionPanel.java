/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.core.Action0;
import hu.openig.mechanics.DefaultAIControls;
import hu.openig.model.Planet;
import hu.openig.model.PlanetStatistics;
import hu.openig.model.Production;
import hu.openig.model.ResearchMainCategory;
import hu.openig.model.ResearchType;
import hu.openig.model.SoundType;
import hu.openig.render.TextRenderer;
import hu.openig.screen.CommonResources;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * The quick production panel.
 * @author akarnokd, 2012.08.13.
 */
public class QuickProductionPanel extends UIContainer {
    /** The common resources. */
    final CommonResources commons;
    /** Label. */
    UILabel shipTitle;
    /** Label. */
    UILabel shipAvailable;
    /** Label. */
    UILabel shipTotal;
    /** Label. */
    UILabel weaponTitle;
    /** Label. */
    UILabel weaponAvailable;
    /** Label. */
    UILabel weaponTotal;
    /** Label. */
    UILabel equipmentTitle;
    /** Label. */
    UILabel equipmentAvailable;
    /** Label. */
    UILabel equipmentTotal;
    /** Pause the production. */
    UIImageButton pause;
    /** Resume the production. */
    UIImageButton resume;
    /** The margin inside the panel. */
    static final int MARGIN = 6;
    /** The column separator. */
    static final int COLUMN_SEPARATOR = 15;
    /** The gap between production lines. */
    static final int LINE_GAP = 3;
    /** The top divider y. */
    int topDivider;
    /** The middle divider. */
    int middleDivider;
    /** The middle divider. */
    int bottomDivider;
    /** Description of the currently hovered research. */
    UILabel hoverResearchCost;
    /** Description of the currently hovered research. */
    UILabel hoverResearchDescription;
    /** Description of the currently hovered research. */
    UILabel hoverResearchTitle;
    /** The production simple lines. */
    final List<List<ProductionSimpleLine>> lines = new ArrayList<>();
    /** The production simple lines. */
    final List<List<ProductionHistoryLine>> historyLines = new ArrayList<>();
    /** Column widths. */
    private int col1Width;
    /** Column widths. */
    private int col2Width;
    /** Column widths. */
    private int col3Width;
    /** The research type currently hovered. */
    ResearchType hoverType;
    /**
     * Constructor. Initializes the inner controls.
     * @param commons the common resources
     */
    public QuickProductionPanel(final CommonResources commons) {
        this.commons = commons;

        shipTitle = new UILabel("", 10, commons.text());
        shipTitle.horizontally(HorizontalAlignment.CENTER);
        weaponTitle = new UILabel("", 10, commons.text());
        weaponTitle.horizontally(HorizontalAlignment.CENTER);
        equipmentTitle = new UILabel("", 10, commons.text());
        equipmentTitle.horizontally(HorizontalAlignment.CENTER);

        shipAvailable = new UILabel("", 10, commons.text());
        weaponAvailable = new UILabel("", 10, commons.text());
        equipmentAvailable = new UILabel("", 10, commons.text());

        shipTotal = new UILabel("", 10, commons.text());
        weaponTotal = new UILabel("", 10, commons.text());
        equipmentTotal = new UILabel("", 10, commons.text());

        resume = new UIImageButton(commons.common().moveRight);
        resume.onClick = new Action0() {
            @Override
            public void invoke() {
                commons.player().pauseProduction = false;
            }
        };
        resume.tooltip(commons.get("production.resume"));

        pause = new UIImageButton(commons.common().pauseAll);
        pause.onClick = new Action0() {
            @Override
            public void invoke() {
                commons.player().pauseProduction = true;
            }
        };
        pause.tooltip(commons.get("production.pause"));

        hoverResearchDescription = new UILabel("", 10, commons.text());
        hoverResearchDescription.wrap(true);

        hoverResearchTitle = new UILabel("", 10, commons.text());
        hoverResearchTitle.color(TextRenderer.RED);
        hoverResearchTitle.horizontally(HorizontalAlignment.CENTER);

        hoverResearchCost = new UILabel("", 10, commons.text());
        hoverResearchCost.color(TextRenderer.YELLOW);

        addThis();
    }
    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 192));
        g2.fillRect(0, 0, width, height);
        g2.setColor(new Color(192, 192, 192));
        g2.drawRect(0, 0, width - 1, height - 1);

        g2.drawLine(0, topDivider, width - 1, topDivider);
        g2.drawLine(0, middleDivider, width - 1, middleDivider);
        g2.drawLine(0, bottomDivider, width - 1, bottomDivider);

        int cx = MARGIN + col1Width + COLUMN_SEPARATOR / 2;
        g2.drawLine(cx, 0, cx, middleDivider);
        cx += col2Width + COLUMN_SEPARATOR;
        g2.drawLine(cx, 0, cx, middleDivider);

        super.draw(g2);
    }
    /**
     * Update the display contents.

     */
    public void update() {
        PlanetStatistics ps = computeProductionInfo();

        shipTitle.text(commons.get("quickproduction.ship_title"), true);
        weaponTitle.text(commons.get("quickproduction.weapon_title"), true);
        equipmentTitle.text(commons.get("quickproduction.equipment_title"), true);

        shipAvailable.text(String.valueOf(ps.activeProduction.spaceship), true);
        shipAvailable.color(ps.activeProduction.spaceship < ps.production.spaceship ? TextRenderer.YELLOW : TextRenderer.GREEN);
        shipTotal.text(" / " + ps.production.spaceship, true);

        weaponAvailable.text(String.valueOf(ps.activeProduction.weapons), true);
        weaponAvailable.color(ps.activeProduction.weapons < ps.production.weapons ? TextRenderer.YELLOW : TextRenderer.GREEN);
        weaponTotal.text(" / " + ps.production.weapons, true);

        equipmentAvailable.text(String.valueOf(ps.activeProduction.equipment), true);
        equipmentAvailable.color(ps.activeProduction.equipment < ps.production.equipment ? TextRenderer.YELLOW : TextRenderer.GREEN);
        equipmentTotal.text(" / " + ps.production.equipment, true);

        col1Width = Math.max(shipTitle.width, shipAvailable.width + shipTotal.width);
        col2Width = Math.max(equipmentTitle.width, equipmentAvailable.width + equipmentTotal.width);
        col3Width = Math.max(weaponTitle.width, weaponAvailable.width + weaponTotal.width);
        int titlesHeight = Math.max(shipTitle.height + shipAvailable.height, resume.height) + COLUMN_SEPARATOR;

        topDivider = MARGIN + titlesHeight - COLUMN_SEPARATOR / 2;

        // listing of production lines

        col1Width = Math.max(col1Width, layoutCategory(ResearchMainCategory.SPACESHIPS, 0));
        col2Width = Math.max(col2Width, layoutCategory(ResearchMainCategory.EQUIPMENT, 1));
        col3Width = Math.max(col3Width, layoutCategory(ResearchMainCategory.WEAPONS, 2));

        // history

        col1Width = Math.max(col1Width, layoutHistoryCategory(ResearchMainCategory.SPACESHIPS, 0));
        col2Width = Math.max(col2Width, layoutHistoryCategory(ResearchMainCategory.EQUIPMENT, 1));
        col3Width = Math.max(col3Width, layoutHistoryCategory(ResearchMainCategory.WEAPONS, 2));

        // hover info

        // layout

        int colMax = Math.min(Math.max(col1Width, Math.max(col2Width, col3Width)), 400);
        col1Width = colMax;
        col2Width = colMax;
        col3Width = colMax;

        // TODO

        shipTitle.location(MARGIN, MARGIN);
        shipTitle.width = col1Width;
        shipAvailable.y = shipTitle.y + shipTitle.height + MARGIN;
        shipTotal.y = shipTitle.y + shipTitle.height + MARGIN;
        centerInto(shipTitle.x, shipTitle.width, 0, shipAvailable, shipTotal);

        equipmentTitle.location(MARGIN + col1Width + COLUMN_SEPARATOR, MARGIN);
        equipmentTitle.width = col2Width;
        equipmentAvailable.y = equipmentTitle.y + equipmentTitle.height + MARGIN;
        equipmentTotal.y = equipmentTitle.y + equipmentTitle.height + MARGIN;
        centerInto(equipmentTitle.x, equipmentTitle.width, 0, equipmentAvailable, equipmentTotal);

        weaponTitle.location(MARGIN + col1Width + 2 * COLUMN_SEPARATOR + col2Width, MARGIN);
        weaponTitle.width = col3Width;
        weaponAvailable.y = weaponTitle.y + weaponTitle.height + MARGIN;
        weaponTotal.y = weaponTitle.y + weaponTitle.height + MARGIN;
        centerInto(weaponTitle.x, weaponTitle.width, 0, weaponAvailable, weaponTotal);

        pause.location(weaponTitle.x + weaponTitle.width + COLUMN_SEPARATOR, MARGIN);
        pause.visible(!commons.player().pauseProduction);
        resume.location(pause.location());
        resume.visible(commons.player().pauseProduction);

        // layout production columns
        int lineHeights = 0;
        int colX = MARGIN;
        for (List<ProductionSimpleLine> psls : lines) {
            int lineHeightCol = -LINE_GAP;
            int colY = topDivider + (MARGIN - MARGIN / 2);
            for (ProductionSimpleLine psl : psls) {
                psl.x = colX;
                psl.y = colY;
                psl.width = colMax;
                psl.layout();
                lineHeightCol += psl.height + LINE_GAP;
                colY += psl.height + LINE_GAP;
            }
            lineHeights = Math.max(lineHeights, lineHeightCol);
            colX += COLUMN_SEPARATOR + colMax;
        }

        middleDivider = topDivider + MARGIN + lineHeights;

        // layout history

        lineHeights = 0;
        colX = MARGIN;
        for (List<ProductionHistoryLine> psls : historyLines) {
            int lineHeightCol = -LINE_GAP;
            int colY = middleDivider + (MARGIN - MARGIN / 2);
            for (ProductionHistoryLine psl : psls) {
                psl.x = colX;
                psl.y = colY;
                psl.width = colMax;
                psl.layout();
                lineHeightCol += psl.height + LINE_GAP;
                colY += psl.height + LINE_GAP;
            }
            lineHeights = Math.max(lineHeights, lineHeightCol);
            colX += COLUMN_SEPARATOR + colMax;
        }

        bottomDivider = middleDivider + MARGIN + lineHeights;

        int innerWidth = col1Width + col2Width + col3Width + 3 * COLUMN_SEPARATOR + pause.width;

        int bottomPart = 0;
        hoverResearchTitle.location(MARGIN, bottomDivider + (MARGIN - MARGIN / 2));
        hoverResearchTitle.width = innerWidth;
        hoverResearchDescription.width = innerWidth;
        hoverResearchDescription.location(hoverResearchTitle.x, hoverResearchTitle.y + hoverResearchTitle.height + 3);

        if (hoverType != null) {
            hoverResearchTitle.text(hoverType.longName);

            hoverResearchDescription.text(hoverType.description);
            hoverResearchDescription.height = hoverResearchDescription.getWrappedHeight();

            hoverResearchCost.text(commons.format("quickproduction.cost_inventory", hoverType.productionCost, commons.player().inventoryCount(hoverType)), true);
            hoverResearchCost.height = hoverResearchCost.textSize();

            bottomPart += hoverResearchTitle.height;
            bottomPart += hoverResearchDescription.height;
            bottomPart += hoverResearchCost.height;
            bottomPart += 6;
        } else {
            hoverResearchTitle.text(commons.get("quickproduction.no_active"));
            hoverResearchDescription.text("");
            hoverResearchDescription.height = 0;
            hoverResearchCost.text("");
            hoverResearchCost.height = 0;

            bottomPart += hoverResearchTitle.height;
        }
        hoverResearchCost.location(hoverResearchDescription.x, hoverResearchDescription.y + hoverResearchDescription.height + 3);
        hoverResearchCost.width = innerWidth;

        width = innerWidth + 2 * MARGIN;

        height = bottomDivider + 2 * MARGIN + bottomPart;
    }
    @Override
    public boolean mouse(UIMouse e) {
        ResearchType crt = hoverType;
        ResearchType rt0 = commons.player().currentResearch();
        for (UIComponent c : components) {
            if (c instanceof ProductionSimpleLine) {
                if (c.within(e)) {
                    rt0 = ((ProductionSimpleLine)c).rt.type;
                }
            } else
            if (c instanceof ProductionHistoryLine) {
                if (c.within(e)) {
                    rt0 = ((ProductionHistoryLine)c).rt;
                }
            }
        }
        hoverType = rt0;
        return super.mouse(e) || rt0 != crt;
    }
    @Override
    public UIComponent visible(boolean state) {
        if (state && !this.visible) {
            hoverType = commons.player().currentResearch();
        }
        return super.visible(state);
    }
    /**
     * Add/remove lines of process.
     * @param mcat the main category
     * @param column the column
     * @return the maximum width of the column based on the contents
     */
    int layoutCategory(ResearchMainCategory mcat, int column) {
        Collection<Production> prods = commons.player().productionLines(mcat);
        if (prods == null) {
            prods = Collections.emptyList();
        }
        while (lines.size() <= column) {
            lines.add(new ArrayList<ProductionSimpleLine>());
        }
        boolean changed = false;
        int w = 0;
        List<ProductionSimpleLine> list = lines.get(column);
        int i = 0;
        for (Production prod : prods) {
            ProductionSimpleLine psl;
            if (list.size() > i) {
                psl = list.get(i);
            } else {
                // add necessary new lines
                psl = new ProductionSimpleLine();
                list.add(psl);
                add(psl);
                changed = true;
            }
            psl.rt = prod;

            w = Math.max(w, psl.update());

            i++;
        }
        // remove unnecessary lines
        for (int j = list.size() - 1; j >= i; j--) {
            ProductionSimpleLine psl = list.remove(j);
            this.components.remove(psl);
            changed = true;
        }
        if (changed) {
            commons.control().moveMouse();
        }
        return w;
    }
    /**
     * Add/remove lines of process.
     * @param mcat the main category
     * @param column the column
     * @return the maximum width of the column based on the contents
     */
    int layoutHistoryCategory(ResearchMainCategory mcat, int column) {
        List<ResearchType> prods = commons.player().productionHistory.get(mcat);
        if (prods == null) {
            prods = Collections.emptyList();
        }
        Set<ResearchType> currProds = commons.player().productionLineTypes(mcat);
        if (currProds == null) {
            currProds = Collections.emptySet();
        }
        while (historyLines.size() <= column) {
            historyLines.add(new ArrayList<ProductionHistoryLine>());
        }
        boolean changed = false;
        int w = 0;
        List<ProductionHistoryLine> list = historyLines.get(column);
        int i = 0;
        for (ResearchType prod : prods) {
            // skip running production
            if (currProds != null && currProds.contains(prod)) {
                continue;
            }
            if (prod.nobuild) {
                continue;
            }

            ProductionHistoryLine psl;
            if (list.size() > i) {
                psl = list.get(i);
            } else {
                // add necessary new lines
                psl = new ProductionHistoryLine();
                list.add(psl);
                add(psl);
                changed = true;
            }
            psl.rt = prod;

            w = Math.max(w, psl.update());

            i++;
        }
        // remove unnecessary lines
        for (int j = list.size() - 1; j >= i; j--) {
            ProductionHistoryLine psl = list.remove(j);
            this.components.remove(psl);
            changed = true;
        }
        if (changed) {
            commons.control().moveMouse();
        }
        return w;
    }
    /**
     * Center the combination of components.
     * @param x the left coordinate
     * @param w the width
     * @param gap the gap between components
     * @param c1 the first component
     * @param c2 the second component
     */
    void centerInto(int x, int w, int gap, UIComponent c1, UIComponent c2) {
        int w1 = c1.width + c2.width + gap;
        c1.x = x + (w - w1) / 2;
        c2.x = c1.x + c1.width + gap;
    }
    /**
     * @return Compute the production statistics of the planets.

     */
    PlanetStatistics computeProductionInfo() {
        PlanetStatistics result = new PlanetStatistics();
        for (Planet p : commons.player().ownPlanets()) {
            result.add(p.getProductionStatistics());
        }

        return result;
    }
    /**
     * Clear memorized references and values.
     */
    @Override
    public void clear() {
        lines.clear();
        historyLines.clear();
        for (int i = components.size() - 1; i >= 0; i--) {
            UIComponent c = components.get(i);
            if (c instanceof ProductionSimpleLine) {
                components.remove(i);
            }
            if (c instanceof ProductionHistoryLine) {
                components.remove(i);
            }
        }
    }
    /**
     * The running production line with a few controls.

     * @author akarnokd, 2012.08.14.
     */
    public class ProductionSimpleLine extends UIContainer {
        /** The research name. */
        UILabel name;
        /** Remove one unit. */
        UIImageButton oneLess;
        /** The remaining prod count. */
        UILabel count;
        /** Add one unit. */
        UIImageButton oneMore;
        /** Current progress. */
        UILabel progress;
        /** Remove from production. */
        UIImageButton remove;
        /** The assigned research. */
        Production rt;
        /** Gap between components. */
        static final int GAP = 10;
        /** Is the shift. */
        boolean countShift;
        /**
         * Constructor. Initializes the fields.
         */
        public ProductionSimpleLine() {
            name = new UILabel("", 10, commons.text());
            oneLess = new UIImageButton(commons.research().less) {
                @Override
                public boolean mouse(UIMouse e) {
                    countShift = e.has(Modifier.SHIFT);
                    return super.mouse(e);
                }
            };
            oneLess.setHoldDelay(200);
            oneLess.onClick = new Action0() {
                @Override
                public void invoke() {
                    int cnt = -1;
                    if (countShift) {
                        cnt *= 10;
                    }
                    DefaultAIControls.actionStartProduction(commons.player(), rt.type, cnt, rt.priority);
                }
            };

            count = new UILabel("", 10, commons.text());
            count.horizontally(HorizontalAlignment.CENTER);
            oneMore = new UIImageButton(commons.research().more) {
                @Override
                public boolean mouse(UIMouse e) {
                    countShift = e.has(Modifier.SHIFT);
                    return super.mouse(e);
                }
            };
            oneMore.setHoldDelay(200);
            oneMore.onClick = new Action0() {
                @Override
                public void invoke() {
                    int cnt = 1;
                    if (countShift) {
                        cnt *= 10;
                    }
                    DefaultAIControls.actionStartProduction(commons.player(), rt.type, cnt, rt.priority);
                }
            };

            progress = new UILabel("", 10, commons.text());
            progress.horizontally(HorizontalAlignment.CENTER);
            remove = new UIImageButton(commons.research().removeX);
            remove.onClick = new Action0() {
                @Override
                public void invoke() {
                    DefaultAIControls.actionRemoveProduction(commons.player(), rt.type);
                    commons.computerSound(SoundType.DEL_PRODUCTION);
                    commons.control().moveMouse();
                }
            };
            height = remove.height;

            oneLess.tooltip(commons.get("production.one_less.tooltip"));
            oneMore.tooltip(commons.get("production.one_more.tooltip"));
            remove.tooltip(commons.get("quickproduction.remove.tooltip"));

            addThis();
        }
        /**
         * Update contents.
         * @return the minimum width
         */
        public int update() {
            name.text(rt.type.name, true);
            count.text(String.valueOf(rt.count));
            progress.text((rt.progress * 100 / rt.type.productionCost) + "%");
            count.width = commons.text().getTextWidth(count.textSize(), "0000");
            progress.width = commons.text().getTextWidth(progress.textSize(), "0000");

            int c = TextRenderer.GREEN;
            if (over) {
                c = TextRenderer.YELLOW;
            } else
            if (commons.player().currentResearch() == rt.type) {
                c = TextRenderer.RED;
            }
            name.color(c);
            count.color(c);
            progress.color(c);

            oneLess.visible(rt.count > 0);

            return name.width + count.width + progress.width
                    + oneLess.width
                    + oneMore.width
                    + remove.width + GAP + 5;
        }
        /**
         * Apply the layout based on the current component width.
         */
        public void layout() {
            name.location(0, 1);

            // right align rest
            remove.location(width - remove.width, 0);
            progress.location(remove.x - 1 - progress.width, 1);
            oneMore.location(progress.x - 1 - oneMore.width, 0);
            count.location(oneMore.x - 1 - count.width, 1);
            oneLess.location(count.x - 1 - oneLess.width, 0);
        }
        @Override
        public boolean mouse(UIMouse e) {
            if (e.has(Type.DOWN)) {
                ResearchType crt = commons.player().currentResearch();
                if (crt != rt.type) {
                    commons.player().currentResearch(rt.type);
                    commons.researchChanged(rt.type);
                }
            }
            return super.mouse(e);
        }
    }
    /**
     * A production history line.

     * @author akarnokd, 2012.08.14.
     */
    public class ProductionHistoryLine extends UIContainer {
        /** The research. */
        public ResearchType rt;
        /** The research name. */
        UILabel name;
        /** The research name. */
        UILabel cost;
        /** The research name. */
        UILabel inventory;
        /** Gap between components. */
        static final int GAP = 10;
        /**
         * Constructor. Prepares the sub-components.
         */
        public ProductionHistoryLine() {
            name = new UILabel("", 10, commons.text());
            cost = new UILabel("", 10, commons.text());
            inventory = new UILabel("", 10, commons.text());

            addThis();
        }
        /**
         * Update contents.
         * @return the minimum width
         */
        public int update() {
            name.text(rt.name, true);
            cost.text(rt.productionCost + " cr", true);
//            inventory.text(Integer.toString(commons.player().inventoryCount(rt)), true);
            inventory.text("  ", true);

            if (over) {
                name.color(TextRenderer.YELLOW);
                cost.color(TextRenderer.YELLOW);
                inventory.color(TextRenderer.YELLOW);
            } else
            if (commons.player().currentResearch() == rt) {
                name.color(TextRenderer.RED);
                cost.color(TextRenderer.RED);
                inventory.color(TextRenderer.RED);
            } else {
                name.color(TextRenderer.GREEN);
                cost.color(TextRenderer.GREEN);
                inventory.color(TextRenderer.GREEN);
            }

            height = name.height;

            int w = name.width + cost.width + inventory.width + 2 * GAP;
            return w;
        }
        /**
         * Apply the layout based on the current component width.
         */
        public void layout() {
            name.location(0, 0);
            cost.location(width - cost.width, 0);
            inventory.location(name.x + name.width + GAP, 0);
        }
        @Override
        public boolean mouse(UIMouse e) {
            if (e.has(Type.DOWN)) {
                int cnt = 0;
                if (e.has(Modifier.SHIFT)) {
                    cnt = 10;
                }
                if (!DefaultAIControls.actionStartProduction(commons.player(), rt, cnt, 50)) {
                    commons.computerSound(SoundType.NOT_AVAILABLE);
                } else {
                    commons.computerSound(SoundType.ADD_PRODUCTION);
                }
            }
            return false;
        }
    }
}
