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
import hu.openig.model.ResearchType;
import hu.openig.model.SpacewarStructure;
import hu.openig.render.TextRenderer;
import hu.openig.screen.CommonResources;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Button;
import hu.openig.ui.UIMouse.Modifier;
import hu.openig.ui.UIMouse.Type;
import hu.openig.ui.UIPanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Spacewar middle status panel: group buttons + scrollable selection grid.
 * <p>
 * Structure:
 * <ul>
 * <li>{@link #groupBar} — fixed top strip of group recall buttons</li>
 * <li>{@link #content} — layout, caching, and atomic offscreen painting of cells</li>
 * </ul>
 * Layout runs only when the selection set, loadout sizes, or panel width change.
 * The visible page is composed into a buffer then blitted once so icons and
 * labels never update out of sync while scrolling.
 */
public class SpacewarSelectionPanel extends UIPanel {
    /** Top of the scrollable cell area (below group buttons). */
    public static final int CONTENT_TOP = 22;
    /** Horizontal inset for the cell grid (matches group-button start). */
    public static final int CONTENT_PAD_X = 5;
    /** Vertical inset inside the scrollable cell area. */
    public static final int CONTENT_PAD_Y = 3;
    /** Gap between cells in the selection grid. */
    public static final int CELL_GAP = 4;
    /** Overlay scrollbar width. */
    static final int SCROLL_BAR_WIDTH = 8;
    /** Minimum scrollbar thumb height. */
    static final int SCROLL_MIN_THUMB = 12;
    /** Scrollbar track color. */
    static final Color SCROLL_TRACK = new Color(0x40, 0x40, 0x40);
    /** Scrollbar thumb color. */
    static final Color SCROLL_THUMB = new Color(0xA0, 0xA0, 0xA0);

    /** Common resources. */
    final CommonResources commons;
    /** Group button strip. */
    final UIPanel groupBar;
    /** Scrollable / virtualized cell viewport. */
    final ContentPanel content;
    /** Group buttons; index 0 is select-all (*), 1..10 are groups 0..9. */
    final List<UIImageButton> groupButtons = new ArrayList<>();
    /** Reusable group-presence flags. */
    final boolean[] groupPresent = new boolean[11];

    /** Live battle structures (owner keeps the list). */
    public List<SpacewarStructure> structures;
    /** Control-group map (owner keeps the map). */
    public Map<SpacewarStructure, Integer> groups;

    /** Select-all action. */
    public Action0 onSelectAll;
    /** Deselect-all action (right-click *). */
    public Action0 onDeselectAll;
    /** Recall control group. */
    public Action1<Integer> onRecallGroup;
    /** Remove control group. */
    public Action1<Integer> onRemoveGroup;
    /** Called after local selection edits (right-click / double-click filters). */
    public Action0 onSelectionModified;

    /**
     * @param commons the common resources
     */
    public SpacewarSelectionPanel(CommonResources commons) {
        this.commons = commons;
        backgroundColor(0xFF000000);

        groupBar = new UIPanel();
        groupBar.location(0, 0);
        groupBar.height = CONTENT_TOP;

        content = new ContentPanel();
        content.location(0, CONTENT_TOP);

        buildGroupButtons();

        add(groupBar, content);
    }

    /** Create the * / 0..9 group buttons. */
    void buildGroupButtons() {
        int x = 5;
        for (int i = -1; i < 10; i++) {
            final int j = i;
            UIImageButton ib = new UIImageButton(commons.common().shield) {
                @Override
                public void draw(Graphics2D g2) {
                    super.draw(g2);
                    String s = j < 0 ? "*" : Integer.toString(j);
                    commons.text().paintTo(g2, 7, 3, 10, TextRenderer.WHITE, s);
                }
                @Override
                public boolean mouse(UIMouse e) {
                    if (e.has(Button.RIGHT) && e.has(Type.DOWN)) {
                        if (j >= 0) {
                            if (onRemoveGroup != null) {
                                onRemoveGroup.invoke(j);
                            }
                        } else if (onDeselectAll != null) {
                            onDeselectAll.invoke();
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
                        if (onRecallGroup != null) {
                            onRecallGroup.invoke(j);
                        }
                    } else if (onSelectAll != null) {
                        onSelectAll.invoke();
                    }
                }
            };
            if (i == -1) {
                ib.tooltip(commons.labels().get("battle.selectall.tooltip"));
            } else {
                ib.tooltip(commons.labels().format("battle.selectgroup.tooltip", i));
            }
            ib.x = x;
            x += 25;
            groupButtons.add(ib);
            groupBar.add(ib);
        }
    }

    @Override
    public void size(int width, int height) {
        super.size(width, height);
        groupBar.size(width, CONTENT_TOP);
        content.size(Math.max(0, width), Math.max(0, height - CONTENT_TOP));
        content.invalidateLayout();
    }

    @Override
    public void draw(Graphics2D g2) {
        updateGroupButtons();
        content.syncAndLayout();
        super.draw(g2);
    }

    @Override
    public boolean mouse(UIMouse e) {
        // Wheel works over the whole panel (including the group strip).
        if (e.has(Type.WHEEL)) {
            content.syncFromSelection();
            int prev = content.offset;
            if (e.z < 0) {
                content.offset--;
            } else {
                content.offset++;
            }
            content.clampOffset();
            return content.offset != prev;
        }
        return super.mouse(e);
    }

    /** Update group-button visibility from the current group map. */
    void updateGroupButtons() {
        Arrays.fill(groupPresent, false);
        groupPresent[0] = true;
        if (groups != null) {
            for (Integer g : groups.values()) {
                if (g != null && g >= 0 && g < 10) {
                    groupPresent[g + 1] = true;
                }
            }
        }
        for (int i = 0; i < 11; i++) {
            boolean v0 = groupButtons.get(i).visible();
            groupButtons.get(i).visible(groupPresent[i]);
            if (v0 != groupPresent[i]) {
                commons.control().moveMouse();
            }
        }
    }

    /**
     * Clear selection view state (keeps group buttons).
     * Does not remove child components.
     */
    public void clear() {
        content.clearContents();
    }

    /** One wrap-row of selection cells. */
    static final class SelectionRow {
        /** Cells in left-to-right order. */
        final List<SpacewarSelectionCell> cells = new ArrayList<>();
        /** Row height in pixels. */
        int height;
    }

    /**
     * Scrollable viewport: owns cell pool, row layout, and atomic buffer paint.
     */
    final class ContentPanel extends UIPanel {
        /** Scroll offset in layout rows. */
        int offset;
        /** Max scroll offset. */
        int maxOffset;
        /** Width used by the last layout. */
        int layoutWidth = -1;
        /** True when rows must be recomputed. */
        boolean layoutDirty = true;
        /** Offscreen buffer for the visible page. */
        BufferedImage contentBuffer;
        /** Scratch list of the current selection. */
        final List<SpacewarStructure> selectionScratch = new ArrayList<>();
        /** Bound cells in display order. */
        final List<SpacewarSelectionCell> cells = new ArrayList<>();
        /** Recycled cells. */
        final List<SpacewarSelectionCell> cellPool = new ArrayList<>();
        /** Laid-out rows. */
        final List<SelectionRow> rows = new ArrayList<>();
        /** Recycled rows. */
        final List<SelectionRow> rowPool = new ArrayList<>();
        /** Scrollbar track (content-local). */
        final Rectangle scrollTrack = new Rectangle();
        /** Scrollbar thumb (content-local). */
        final Rectangle scrollThumb = new Rectangle();
        /** True while dragging the scrollbar thumb. */
        boolean scrollDragging;
        /** Grab offset within the thumb when drag started. */
        int scrollGrabY;
        ContentPanel() {
            backgroundColor(0xFF000000);
        }

        /** Mark layout dirty (e.g. after resize). */
        void invalidateLayout() {
            layoutDirty = true;
        }

        /** Release cell/row state. */
        void clearContents() {
            selectionScratch.clear();
            for (SpacewarSelectionCell c : cells) {
                c.structure = null;
                cellPool.add(c);
            }
            cells.clear();
            for (SelectionRow r : rows) {
                r.cells.clear();
                rowPool.add(r);
            }
            rows.clear();
            offset = 0;
            maxOffset = 0;
            layoutWidth = -1;
            layoutDirty = true;
            contentBuffer = null;
            scrollDragging = false;
        }

        /** Sync selection, layout if needed, then paint the buffer. */
        void syncAndLayout() {
            syncFromSelection();
            renderContentBuffer();
        }

        void collectSelection() {
            selectionScratch.clear();
            if (structures == null) {
                return;
            }
            for (SpacewarStructure s : structures) {
                if (s.selected) {
                    selectionScratch.add(s);
                }
            }
        }

        boolean selectionMatchesCells() {
            if (selectionScratch.size() != cells.size()) {
                return false;
            }
            for (int i = 0; i < cells.size(); i++) {
                if (cells.get(i).structure != selectionScratch.get(i)) {
                    return false;
                }
            }
            return true;
        }

        void rebuildCells() {
            for (SpacewarSelectionCell c : cells) {
                c.structure = null;
                cellPool.add(c);
            }
            cells.clear();
            for (SpacewarStructure s : selectionScratch) {
                SpacewarSelectionCell c;
                if (!cellPool.isEmpty()) {
                    c = cellPool.remove(cellPool.size() - 1);
                } else {
                    c = new SpacewarSelectionCell(commons);
                }
                c.bind(s);
                cells.add(c);
            }
            layoutDirty = true;
        }

        void syncFromSelection() {
            collectSelection();
            if (!selectionMatchesCells()) {
                rebuildCells();
            } else {
                forEachVisibleCell((c, y0) -> {
                    if (c.syncLoadout()) {
                        layoutDirty = true;
                    }
                });
            }
            if (layoutWidth != width) {
                layoutDirty = true;
            }
            if (layoutDirty) {
                layoutRows();
            }
        }

        /** Flow cells into wrap-rows for the current width. */
        void layoutRows() {
            for (SelectionRow r : rows) {
                r.cells.clear();
                r.height = 0;
                rowPool.add(r);
            }
            rows.clear();

            int innerWidth = Math.max(0, width - 2 * CONTENT_PAD_X);
            SelectionRow current = null;
            int x0 = 0;
            for (SpacewarSelectionCell c : cells) {
                if (current != null && x0 > 0 && x0 + c.cellWidth >= innerWidth) {
                    rows.add(current);
                    current = null;
                    x0 = 0;
                }
                if (current == null) {
                    if (!rowPool.isEmpty()) {
                        current = rowPool.remove(rowPool.size() - 1);
                    } else {
                        current = new SelectionRow();
                    }
                }
                current.cells.add(c);
                c.rowX = CONTENT_PAD_X + x0;
                current.height = Math.max(current.height, c.cellHeight);
                x0 += c.cellWidth + CELL_GAP;
            }
            if (current != null && !current.cells.isEmpty()) {
                rows.add(current);
            }

            layoutWidth = width;
            layoutDirty = false;
            clampOffset();
        }

        void clampOffset() {
            if (rows.isEmpty()) {
                offset = 0;
                maxOffset = 0;
                return;
            }

            maxOffset = Math.max(0, rows.size() - 1);
            if (offset < 0) {
                offset = 0;
            } else if (offset > maxOffset) {
                offset = maxOffset;
            }
        }

        /**
         * Invoke {@code action} for each cell on the visible page.
         * @param action receives the cell and its content-local top Y
         */
        void forEachVisibleCell(BiConsumer<SpacewarSelectionCell, Integer> action) {
            int y0 = CONTENT_PAD_Y;
            int viewBottom = height - CONTENT_PAD_Y;
            for (int r = offset; r < rows.size() && y0 < viewBottom; r++) {
                SelectionRow row = rows.get(r);
                for (SpacewarSelectionCell c : row.cells) {
                    action.accept(c, y0);
                }
                y0 += row.height + CELL_GAP;
            }
        }

        int ensureContentBuffer() {
            int bw = Math.max(1, width);
            int bh = Math.max(1, height);
            if (contentBuffer == null
                    || contentBuffer.getWidth() != bw
                    || contentBuffer.getHeight() != bh) {
                contentBuffer = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_ARGB);
            }
            return bh;
        }

        /**
         * Compose the visible page into {@link #contentBuffer}.
         * Icons, bars and labels for every visible cell are painted here
         * before a single blit in {@link #draw(Graphics2D)}.
         */
        void renderContentBuffer() {
            int viewH = ensureContentBuffer();
            Graphics2D bg = contentBuffer.createGraphics();
            try {
                bg.setColor(Color.BLACK);
                bg.fillRect(0, 0, contentBuffer.getWidth(), viewH);

                forEachVisibleCell((c, y0) -> {
                    c.refreshCombatStats();
                    c.rebuildDefenseSegments();
                    // Bounds in parent (SpacewarSelectionPanel) coordinates.
                    c.bounds.setBounds(c.rowX, CONTENT_TOP + y0, c.cellWidth, c.cellHeight);
                    c.paint(bg, c.rowX, y0);
                });
            } finally {
                bg.dispose();
            }
        }

        @Override
        public void draw(Graphics2D g2) {
            // Buffer already prepared in syncAndLayout() during parent draw.
            Shape save = g2.getClip();
            g2.clipRect(0, 0, width, height);
            g2.drawImage(contentBuffer, 0, 0, null);
            drawScrollbar(g2);
            g2.setClip(save);
        }

        /** @return true if the list can scroll */
        boolean scrollable() {
            return maxOffset > 0;
        }

        /** Recompute track/thumb rectangles from {@link #offset} / {@link #maxOffset}. */
        void updateScrollGeometry() {
            if (!scrollable()) {
                scrollTrack.setBounds(0, 0, 0, 0);
                scrollThumb.setBounds(0, 0, 0, 0);
                return;
            }
            // Full content height so top/bottom margins stay equal.
            int x = width - SCROLL_BAR_WIDTH;
            scrollTrack.setBounds(x, 0, SCROLL_BAR_WIDTH, height);

            int thumbH = Math.max(SCROLL_MIN_THUMB, height / (maxOffset + 1));
            if (thumbH > height) {
                thumbH = height;
            }
            int travel = height - thumbH;
            int thumbY = 0;
            if (travel > 0 && maxOffset > 0) {
                thumbY = (int)Math.round(1.0 * offset * travel / maxOffset);
            }
            scrollThumb.setBounds(x, thumbY, SCROLL_BAR_WIDTH, thumbH);
        }

        void drawScrollbar(Graphics2D g2) {
            updateScrollGeometry();
            if (!scrollable()) {
                return;
            }
            g2.setColor(SCROLL_TRACK);
            g2.fillRect(scrollTrack.x, scrollTrack.y, scrollTrack.width, scrollTrack.height);
            g2.setColor(SCROLL_THUMB);
            g2.fillRect(scrollThumb.x, scrollThumb.y, scrollThumb.width, scrollThumb.height);
        }

        /**
         * Place the thumb so its top is {@code thumbTop} and sync {@link #offset}.
         * @param thumbTop desired thumb top in content-local Y
         */
        void setOffsetFromThumbTop(int thumbTop) {
            updateScrollGeometry();
            int travel = scrollTrack.height - scrollThumb.height;
            if (travel <= 0) {
                offset = 0;
            } else {
                int ty = Math.max(scrollTrack.y, Math.min(thumbTop, scrollTrack.y + travel));
                offset = (int)Math.round(1.0 * (ty - scrollTrack.y) * maxOffset / travel);
            }
            clampOffset();
            updateScrollGeometry();
        }

        @Override
        public boolean mouse(UIMouse e) {
            if (scrollDragging) {
                if (e.has(Type.DRAG) || e.has(Type.MOVE)) {
                    setOffsetFromThumbTop(e.y - scrollGrabY);
                    return true;
                }
                if (e.has(Type.UP) || e.has(Type.LEAVE)) {
                    scrollDragging = false;
                    return true;
                }
            }
            if (scrollable() && e.has(Type.DOWN) && e.has(Button.LEFT)) {
                updateScrollGeometry();
                if (scrollThumb.contains(e.x, e.y)) {
                    scrollDragging = true;
                    scrollGrabY = e.y - scrollThumb.y;
                    return true;
                }
                if (scrollTrack.contains(e.x, e.y)) {
                    setOffsetFromThumbTop(e.y - scrollThumb.height / 2);
                    scrollDragging = true;
                    scrollGrabY = scrollThumb.height / 2;
                    return true;
                }
            }
            if (e.has(Type.UP) || e.has(Type.LEAVE)) {
                scrollDragging = false;
            }

            // Coordinates are content-local; convert to panel-local for bounds.
            int mx = e.x;
            int my = e.y + CONTENT_TOP;
            if (e.has(Type.DOWN) && e.has(Button.RIGHT)) {
                removeFromSelection(mx, my);
                fireSelectionModified();
                return true;
            }
            if (e.has(Type.DOUBLE_CLICK) && e.has(Button.LEFT)) {
                if (e.has(Modifier.SHIFT)) {
                    addTypeToSelection(mx, my, e.z > 2);
                } else if (e.has(Modifier.CTRL)) {
                    removeTypeFromSelection(mx, my, e.z > 2);
                } else {
                    retainTypeFromSelection(mx, my, e.z > 2);
                }
                fireSelectionModified();
                return true;
            }
            return super.mouse(e);
        }

        void fireSelectionModified() {
            if (onSelectionModified != null) {
                onSelectionModified.invoke();
            }
        }

        SpacewarSelectionCell cellAt(int mx, int my) {
            SpacewarSelectionCell[] hit = { null };
            forEachVisibleCell((c, y0) -> {
                if (hit[0] == null && c.bounds.contains(mx, my)) {
                    hit[0] = c;
                }
            });
            return hit[0];
        }

        void addTypeToSelection(int mx, int my, boolean category) {
            SpacewarSelectionCell c = cellAt(mx, my);
            if (c == null || structures == null) {
                return;
            }
            for (SpacewarStructure s : structures) {
                if (s.owner == c.structure.owner) {
                    if (category) {
                        ResearchType rt0 = commons.world().researches.get(c.structure.techId);
                        ResearchType rt1 = commons.world().researches.get(s.techId);
                        s.selected = rt0.category == rt1.category;
                    } else {
                        s.selected = s.techId.equals(c.structure.techId);
                    }
                }
            }
        }

        void retainTypeFromSelection(int mx, int my, boolean category) {
            SpacewarSelectionCell c = cellAt(mx, my);
            if (c == null) {
                return;
            }
            for (SpacewarSelectionCell c1 : cells) {
                if (c1.structure.owner == c.structure.owner) {
                    if (category) {
                        ResearchType rt0 = commons.world().researches.get(c.structure.techId);
                        ResearchType rt1 = commons.world().researches.get(c1.structure.techId);
                        c1.structure.selected = rt0.category == rt1.category;
                    } else {
                        c1.structure.selected = c1.structure.techId.equals(c.structure.techId);
                    }
                }
            }
        }

        void removeTypeFromSelection(int mx, int my, boolean category) {
            SpacewarSelectionCell c = cellAt(mx, my);
            if (c == null) {
                return;
            }
            for (SpacewarSelectionCell c1 : cells) {
                if (c1.structure.owner == c.structure.owner) {
                    if (category) {
                        ResearchType rt0 = commons.world().researches.get(c.structure.techId);
                        ResearchType rt1 = commons.world().researches.get(c1.structure.techId);
                        c1.structure.selected = rt0.category != rt1.category;
                    } else {
                        c1.structure.selected = !c1.structure.techId.equals(c.structure.techId);
                    }
                }
            }
        }

        void removeFromSelection(int mx, int my) {
            SpacewarSelectionCell c = cellAt(mx, my);
            if (c != null) {
                c.structure.selected = false;
            }
        }
    }
}
