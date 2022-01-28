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
import hu.openig.model.InventoryItem;
import hu.openig.model.InventoryItemGroup;
import hu.openig.model.ResearchType;
import hu.openig.render.TextRenderer;
import hu.openig.screen.CommonResources;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** List vehicles in a scrollable region. */
public class VehicleList extends UIContainer {
    /** The scroll-up indicator. */
    UIImageButton scrollUp;
    /** The scroll down indicator. */
    UIImageButton scrollDown;
    /** The scroll amount. */
    public int yOffset;
    /** The maximum scroll height. */
    int maxHeight;
    /** The maximum width of the images. */
    int maxWidth;
    /** The selection action. */
    public Action1<InventoryItem> onSelect;
    /** Group types? */
    public boolean group;
    /** The currently selected inventory item. */
    public InventoryItem selectedItem;
    /** The items. */
    public final List<InventoryItem> items = new ArrayList<>();
    /** The grouping map. */
    public final Map<ResearchType, InventoryItemGroup> map = new LinkedHashMap<>();
    /** The common resources. */
    final CommonResources commons;
    /**

     * Construct.

     * @param commons the common resources
     */
    public VehicleList(CommonResources commons) {
        this.commons = commons;
        scrollUp = new UIImageButton(commons.common().moveUp);
        scrollUp.setHoldDelay(100);
        scrollUp.onClick = new Action0() {
            @Override
            public void invoke() {
                doScrollUp();
            }
        };
        scrollDown = new UIImageButton(commons.common().moveDown);
        scrollDown.setHoldDelay(100);
        scrollDown.onClick = new Action0() {
            @Override
            public void invoke() {
                doScrollDown();
            }
        };

        addThis();
    }
    /** Sort the items by category ascending (battleships first) and version descending. */
    void sort() {
        Collections.sort(items, new Comparator<InventoryItem>() {
            @Override
            public int compare(InventoryItem o1,
                    InventoryItem o2) {
                int c = o2.type.category.ordinal() - o1.type.category.ordinal();
                if (c == 0) {
                    c = o2.type.index - o1.type.index;

                }
                return c;
            }
        });
    }
    /** Compute the scrollable region height and its items. */
    public void compute() {
        sort();
        int row = 0;
        maxHeight = 0;
        maxWidth = 0;
        if (group) {
            map.clear();
            for (InventoryItem pii : items) {
                InventoryItemGroup list = map.get(pii.type);
                if (list == null) {
                    list = new InventoryItemGroup(pii.type);
                    map.put(pii.type, list);
                }
                maxWidth = Math.max(maxWidth, pii.type.equipmentImage.getWidth());
                if (pii == selectedItem) {
                    list.index = list.items.size();
                }
                list.add(pii);
            }

            for (ResearchType rt : map.keySet()) {
                if (row++ > 0) {
                    maxHeight += 5;
                }
                int equipmentHeight = rt.equipmentImage.getHeight();
                maxHeight += Math.max(equipmentHeight, 27);
            }
        } else {
            for (InventoryItem pii : items) {
                if (row++ > 0) {
                    maxHeight += 5;
                }
                int equipmentHeight = pii.type.equipmentImage.getHeight();

                maxHeight += Math.max(equipmentHeight, 27);
                maxWidth = Math.max(maxWidth, pii.type.equipmentImage.getWidth());
            }
        }
        yOffset = Math.max(0, Math.min(yOffset, maxHeight - height));
    }
    /**
     * Return the inventory item at the specified Y coordinates.
     * @param y the coordinate
     * @return the item or null
     */
    public InventoryItem getItemAt(final int y) {
        int y0 = -yOffset;
        for (InventoryItem pii : items) {
            int equipmentHeight = pii.type.equipmentImage.getHeight();
            int rowHeight = Math.max(equipmentHeight, 27);
            if (y >= y0 && y < y0 + rowHeight) {
                return pii;
            }
            y0 += rowHeight + 5;
        }
        return null;
    }
    /**
     * Return the inventory item at the specified Y coordinates.
     * @param y the coordinate
     * @return the item or null
     */
    public InventoryItemGroup getGroupAt(final int y) {
        int y0 = -yOffset;
        for (Map.Entry<ResearchType, InventoryItemGroup> e : map.entrySet()) {
            int eh = Math.max(e.getKey().equipmentImage.getHeight(), 27);
            if (y >= y0 && y < y0 + eh) {
                return e.getValue();
            }
            y0 += eh + 5;
        }
        return null;
    }
    @Override
    public boolean mouse(UIMouse e) {
        if (e.has(Type.WHEEL)) {
            if (e.z < 0) {
                doScrollUp();
            } else {
                doScrollDown();
            }
            return true;
        } else
        if (e.has(Type.DOWN) && e.x < width - scrollDown.width && onSelect != null) {
            if (group) {
                InventoryItemGroup ig = getGroupAt(e.y);
                if (ig != null) {
                    if (selectedItem != null && ig.type == selectedItem.type) {
                        if (e.has(Button.LEFT)) {
                            ig.index = (ig.index + 1) % ig.items.size();
                        } else

                        if (e.has(Button.RIGHT)) {
                            ig.index--;
                            if (ig.index < 0) {
                                ig.index = ig.items.size() - 1;
                            }
                        }
                    }
                    onSelect.invoke(ig.items.get(ig.index));
                }
            } else {
                InventoryItem pii = getItemAt(e.y);
                if (pii != null) {
                    selectedItem = pii;
                    onSelect.invoke(pii);
                }
            }
            return true;
        }
        return super.mouse(e);
    }
    /**
     * Select the next item from the current group.
     */
    public void nextGroupItem() {
        if (group) {
            for (InventoryItemGroup ig : map.values()) {
                if (selectedItem != null && ig.type == selectedItem.type) {
                    ig.index = (ig.index + 1) % ig.items.size();
                    onSelect.invoke(ig.items.get(ig.index));
                    break;
                }
            }
        }

    }
    /**
     * Select the previous item from the current group.
     */
    public void previousGroupItem() {
        if (group) {
            for (InventoryItemGroup ig : map.values()) {
                if (selectedItem != null && ig.type == selectedItem.type) {
                    ig.index--;
                    if (ig.index < 0) {
                        ig.index = ig.items.size() - 1;
                    }
                    onSelect.invoke(ig.items.get(ig.index));
                    break;
                }
            }
        }
    }
    /** Scroll up. */
    void doScrollUp() {
        yOffset = Math.max(0, Math.min(yOffset - 16, maxHeight - height));
    }
    /** Scroll down. */
    void doScrollDown() {
        yOffset = Math.max(0, Math.min(yOffset + 16, maxHeight - height));
    }
    /** Adjust scroll level if the height changes. */
    public void adjustScroll() {
        yOffset = Math.max(0, Math.min(yOffset, maxHeight - height));
    }
    @Override
    public void draw(Graphics2D g2) {
        scrollUp.location(width - scrollUp.width, 0);
        scrollDown.location(width - scrollDown.width, height - scrollDown.height);

        Shape save0 = g2.getClip();

        int availableWidth = width - scrollUp.width - 2;
        g2.clipRect(0, 0, availableWidth, height);
        g2.translate(0, -yOffset);

        int row = 0;
        int y = 0;
        if (group) {
            for (Map.Entry<ResearchType, InventoryItemGroup> e : map.entrySet()) {
                if (e.getValue().items.size() == 0) {
                    continue;
                }
                if (row++ > 0) {
                    y += 5;
                }
                g2.drawImage(e.getKey().equipmentImage, 0, y, null);
                if (selectedItem != null && selectedItem.type == e.getKey()) {
                    g2.setColor(Color.ORANGE);
                    g2.drawRect(0, y, e.getKey().equipmentImage.getWidth() - 1, e.getKey().equipmentImage.getHeight() - 1);
                }
                // damage and shield indicators

                InventoryItem ii = e.getValue().items.get(e.getValue().index);

                int hpx = (int)((1d * (availableWidth - maxWidth) * ii.hp / commons.world().getHitpoints(ii.type, ii.owner)));

                g2.setColor(Color.GREEN);
                g2.fillRect(maxWidth + 5, y, hpx, 4);
                g2.setColor(Color.RED);
                g2.fillRect(maxWidth + 5 + hpx, y, (availableWidth - hpx - 5 - maxWidth), 4);

                long sMax = ii.shieldMax();
                if (sMax > 0) {
                    double s0 = ii.shield;
                    int shx = (int)((availableWidth - maxWidth) * s0 / sMax);
                    g2.setColor(Color.ORANGE);
                    g2.drawRect(maxWidth + 5, y + 5, (availableWidth - 5 - maxWidth) - 1, 4 - 1);
                    g2.fillRect(maxWidth + 5, y + 5, shx, 4);
                }

                // name

                commons.text().paintTo(g2,

                        maxWidth + 5, y + 10,

                        7, TextRenderer.GREEN, e.getKey().name);

                commons.text().paintTo(g2,

                        maxWidth + 5, y + 20,

                        7, TextRenderer.GREEN,

                        String.format("%d / %d", e.getValue().index + 1, e.getValue().items.size())
                );

                y += Math.max(e.getKey().equipmentImage.getHeight(), 27);
            }
        } else {
            for (InventoryItem pii : items) {
                if (row++ > 0) {
                    y += 5;
                }
                g2.drawImage(pii.type.equipmentImage, 0, y, null);
                if (selectedItem == pii) {
                    g2.setColor(Color.ORANGE);
                    g2.drawRect(0, y, pii.type.equipmentImage.getWidth() - 1, pii.type.equipmentImage.getHeight() - 1);
                }

                // damage and shield indicators

                long hpMax = commons.world().getHitpoints(pii.type, pii.owner);
                int hpx = (int)((availableWidth - maxWidth) * pii.hp / hpMax);

                g2.setColor(Color.GREEN);
                g2.fillRect(maxWidth + 5, y, hpx, 4);
                g2.setColor(Color.RED);
                g2.fillRect(maxWidth + 5 + hpx, y, (availableWidth - hpx - 5 - maxWidth), 4);

                long sm = pii.shieldMax();
                if (sm > 0) {
                    int shx = (int)((availableWidth - maxWidth) * pii.shield / sm);
                    g2.setColor(Color.ORANGE);
                    g2.drawRect(maxWidth + 5, y + 5, (availableWidth - 5 - maxWidth) - 1, 4 - 1);
                    g2.fillRect(maxWidth + 5, y + 5, shx, 4);
                }

                // name

                commons.text().paintTo(g2,

                        maxWidth + 5, y + 10,

                        7, TextRenderer.GREEN, pii.type.name);

                y += Math.max(pii.type.equipmentImage.getHeight(), 27);
            }
        }

        g2.translate(0, yOffset);
        g2.setClip(save0);

        scrollUp.visible(yOffset > 0);
        scrollDown.visible(yOffset < maxHeight - height);

        super.draw(g2);
    }
    /** Clear the items. */
    @Override
    public void clear() {
        map.clear();
        items.clear();
    }
    /**
     * Return the current group index of the given research type or the default value.
     * @param rt the research
     * @param def the default index
     * @return the index
     */
    public int groupIndex(ResearchType rt, int def) {
        InventoryItemGroup ig = map.get(rt);
        if (ig != null) {
            return ig.index;
        }
        return def;
    }
}
