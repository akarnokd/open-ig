/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.model.BattleProjectile.Mode;
import hu.openig.model.SpacewarStructure;
import hu.openig.model.SpacewarWeaponPort;
import hu.openig.render.TextRenderer;
import hu.openig.render.TextRenderer.TextSegment;
import hu.openig.screen.CommonResources;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Cached display model and painter for one selected spacewar unit
 * in the middle status panel.
 * <p>
 * Layout metrics are measured only when loadout/identity data changes;
 * combat HP/shield values refresh cheaply for visible cells.
 */
public final class SpacewarSelectionCell {
    /** Fixed ship icon column width. */
    public static final int MAX_IMAGE = 50;
    /** Gap between icon column and text. */
    public static final int ICON_TEXT_GAP = 8;
    /** Trailing padding after the text block. */
    public static final int TEXT_RIGHT_PAD = 4;
    /**
     * Opaque-content bounds keyed by angle-frame image.
     * Many selected units share the same {@code angles[0]} instance, so the
     * pixel scan runs once per unique sprite.
     */
    static final Map<BufferedImage, Rectangle> IMAGE_CONTENT_CACHE = new WeakHashMap<>();

    /** Shared resources for text measurement and painting. */
    final CommonResources commons;
    /** Bound structure, or {@code null} when pooled. */
    SpacewarStructure structure;
    /** Display name. */
    String name = "";
    /** Owner name. */
    String owner = "";
    /** Parent fleet/planet name. */
    String parent = "";
    /** Owner color. */
    int color;
    /** Ship/building image. */
    BufferedImage image;
    /** Shield fill ratio, or {@code -1} when none. */
    double shieldRatio;
    /** HP fill ratio. */
    double hpRatio;
    /** Total beam firepower. */
    double firepower;
    /** Damage per second. */
    double dps;
    /** Rocket count. */
    int rockets;
    /** Bomb count. */
    int bombs;
    /** Unit batch count. */
    int count;
    /** Current HP. */
    int hp;
    /** Current shield points, or {@code -1}. */
    int sp;
    /** Last HP baked into defense segments. */
    int defenseHp = Integer.MIN_VALUE;
    /** Last shield baked into defense segments. */
    int defenseSp = Integer.MIN_VALUE;
    /** Name caption (no colon). */
    String nameCaption = "";
    /** Name value. */
    String nameValue = "";
    /** Owner caption (no colon). */
    String ownerCaption = "";
    /** Owner value. */
    String ownerValue = "";
    /** Parent caption (no colon). */
    String parentCaption = "";
    /** Parent value. */
    String parentValue = "";
    /** Firepower caption (no colon). */
    String firepowerCaption = "";
    /** Firepower value. */
    String firepowerValue = "";
    /** Bombs/rockets caption (no colon), or {@code null}. */
    String bombsRocketsCaption;
    /** Bombs/rockets value, or {@code null}. */
    String bombsRocketsValue;
    /** Defense caption (no colon). */
    String defenseCaption = "";
    /** Width of the widest "{@code caption: }" among this cell's lines. */
    int captionColumnWidth;
    /** Measured cell width. */
    int cellWidth;
    /** Measured cell height. */
    int cellHeight;
    /** Text block height. */
    int textHeight;
    /** X within the current layout row. */
    int rowX;
    /** Hit-test bounds in panel coordinates. */
    final Rectangle bounds = new Rectangle();
    /** Opaque pixel bounds of {@link #image} (source coordinates). */
    final Rectangle imageContent = new Rectangle();
    /** Drawn width of the opaque ship art. */
    int iconDrawW;
    /** Drawn height of the opaque ship art. */
    int iconDrawH;
    /** Reused defense-value segments (no caption). */
    final List<TextSegment> defenseSegments = new ArrayList<>(5);

    /**
     * @param commons the common resources
     */
    public SpacewarSelectionCell(CommonResources commons) {
        this.commons = commons;
    }

    /**
     * Bind to a structure and fully recompute cached display data.
     * @param s the structure
     */
    public void bind(SpacewarStructure s) {
        this.structure = s;
        image = s.angles[0];
        computeImageContentBounds();
        if (s.item != null) {
            name = s.item.type.name;
        } else if (s.building != null) {
            name = s.building.type.name;
        } else {
            name = "";
        }
        owner = s.owner.name;
        color = s.owner.color;
        if (s.fleet != null) {
            parent = s.fleet.name();
        } else if (s.planet != null) {
            parent = s.planet.name();
        } else {
            parent = "";
        }
        defenseCaption = stripCaption(commons.labels().get("spacewar.selection.defense_values"));
        String[] ownerParts = splitLabeled(commons.labels().format("spacewar.selection.owner", owner));
        ownerCaption = ownerParts[0];
        ownerValue = ownerParts[1];
        String[] parentParts = splitLabeled(commons.labels().format("spacewar.selection.parent", parent));
        parentCaption = parentParts[0];
        parentValue = parentParts[1];
        defenseHp = Integer.MIN_VALUE;
        defenseSp = Integer.MIN_VALUE;
        refreshCombatStats();
        recomputeLoadoutAndSize();
        rebuildDefenseSegments();
    }

    /**
     * Re-read weapon loadout / count and remeasure.
     * @return true if layout size changed
     */
    public boolean recomputeLoadoutAndSize() {
        int prevW = cellWidth;
        int prevH = cellHeight;

        count = structure.count;
        firepower = 0;
        dps = 0;
        rockets = 0;
        bombs = 0;
        for (SpacewarWeaponPort p : structure.ports) {
            if (p.projectile.mode == Mode.BEAM) {
                double dmg = p.damage(structure.owner);
                firepower += p.count * dmg * count;
                dps += p.count * dmg * count * 1000.0 / p.projectile.delay;
            } else if (p.projectile.mode == Mode.ROCKET || p.projectile.mode == Mode.MULTI_ROCKET) {
                rockets += p.count;
            } else if (p.projectile.mode == Mode.BOMB || p.projectile.mode == Mode.VIRUS) {
                bombs += p.count;
            }
        }
        dps = Math.round(dps);

        String[] nameParts;
        if (count > 1) {
            nameParts = splitLabeled(commons.labels().format("spacewar.selection.name_count", name, count));
        } else {
            nameParts = splitLabeled(commons.labels().format("spacewar.selection.name", name));
        }
        nameCaption = nameParts[0];
        nameValue = nameParts[1];

        String[] fpParts = splitLabeled(commons.labels().format(
                "spacewar.selection.firepower_dps", (int)firepower, (int)dps));
        firepowerCaption = fpParts[0];
        firepowerValue = fpParts[1];

        if (rockets > 0 || bombs > 0) {
            String[] brParts = splitLabeled(commons.labels().format(
                    "spacewar.selection.bombs_rockets", bombs, rockets));
            bombsRocketsCaption = brParts[0];
            bombsRocketsValue = brParts[1];
        } else {
            bombsRocketsCaption = null;
            bombsRocketsValue = null;
        }

        int rows = bombsRocketsCaption != null ? 6 : 5;
        textHeight = 7 * rows + (rows - 1) * 2;

        TextRenderer tr = commons.text();
        captionColumnWidth = 0;
        captionColumnWidth = Math.max(captionColumnWidth, captionWidth(tr, nameCaption));
        captionColumnWidth = Math.max(captionColumnWidth, captionWidth(tr, ownerCaption));
        captionColumnWidth = Math.max(captionColumnWidth, captionWidth(tr, parentCaption));
        captionColumnWidth = Math.max(captionColumnWidth, captionWidth(tr, firepowerCaption));
        if (bombsRocketsCaption != null) {
            captionColumnWidth = Math.max(captionColumnWidth, captionWidth(tr, bombsRocketsCaption));
        }
        captionColumnWidth = Math.max(captionColumnWidth, captionWidth(tr, defenseCaption));

        int w = 0;
        w = Math.max(w, valueWidth(tr, nameValue));
        w = Math.max(w, valueWidth(tr, ownerValue));
        w = Math.max(w, valueWidth(tr, parentValue));
        w = Math.max(w, valueWidth(tr, firepowerValue));
        if (bombsRocketsValue != null) {
            w = Math.max(w, valueWidth(tr, bombsRocketsValue));
        }
        // Size defense values for worst-case digits so HP ticks do not force relayout.
        int dv = tr.getTextWidth(7, Integer.toString(Math.max(hp, structure.hpMax)));
        if (structure.shieldMax > 0) {
            dv += tr.getTextWidth(7, " + ");
            dv += tr.getTextWidth(7, Integer.toString(Math.max(sp, (int)structure.shieldMax)));
            dv += tr.getTextWidth(7, " = ");
            dv += tr.getTextWidth(7, Integer.toString(
                    Math.max(hp, structure.hpMax) + Math.max(sp, (int)structure.shieldMax)));
        }
        w = Math.max(w, dv);

        refreshIconDrawSize();
        cellWidth = MAX_IMAGE + ICON_TEXT_GAP + captionColumnWidth + w + TEXT_RIGHT_PAD;
        // Bars share the text top; ship sits in the remaining text height.
        cellHeight = Math.max(10 + iconDrawH, textHeight + 2);
        return prevW != cellWidth || prevH != cellHeight;
    }

    /** Update HP / shield display values from the live structure. */
    public void refreshCombatStats() {
        hp = (int)structure.hp;
        hpRatio = structure.hpMax > 0 ? structure.hp / structure.hpMax : 0;
        if (structure.shieldMax > 0) {
            shieldRatio = structure.shield / structure.shieldMax;
            sp = (int)structure.shield;
        } else {
            shieldRatio = -1;
            sp = -1;
        }
    }

    /**
     * Sync loadout that affects labels.
     * @return true if cell size changed and layout must run
     */
    public boolean syncLoadout() {
        int newRockets = 0;
        int newBombs = 0;
        for (SpacewarWeaponPort p : structure.ports) {
            if (p.projectile.mode == Mode.ROCKET || p.projectile.mode == Mode.MULTI_ROCKET) {
                newRockets += p.count;
            } else if (p.projectile.mode == Mode.BOMB || p.projectile.mode == Mode.VIRUS) {
                newBombs += p.count;
            }
        }
        if (structure.count != count || newRockets != rockets || newBombs != bombs) {
            return recomputeLoadoutAndSize();
        }
        return false;
    }

    /** Rebuild colored defense-value segments when HP / shield text changed. */
    public void rebuildDefenseSegments() {
        if (defenseHp == hp && defenseSp == sp) {
            return;
        }
        defenseHp = hp;
        defenseSp = sp;
        defenseSegments.clear();
        defenseSegments.add(new TextSegment(Integer.toString(hp), Color.GREEN.getRGB()));
        if (sp >= 0) {
            defenseSegments.add(new TextSegment(" + ", color));
            defenseSegments.add(new TextSegment(Integer.toString(sp), Color.ORANGE.getRGB()));
            defenseSegments.add(new TextSegment(" = ", color));
            defenseSegments.add(new TextSegment(Integer.toString(hp + sp), TextRenderer.YELLOW));
        }
    }

    /**
     * Paint the cell at the given origin.
     * @param g2 the graphics context
     * @param x0 origin X
     * @param y0 origin Y
     */
    public void paint(Graphics2D g2, int x0, int y0) {
        g2.translate(x0, y0);

        // Bars share the first text line; both start at the top of the cell.
        int ty = 0;
        int barY = 0;

        g2.setColor(Color.GREEN);
        int iw2 = (int)(MAX_IMAGE * hpRatio);
        g2.drawRect(0, barY, MAX_IMAGE, 4);
        g2.fillRect(0, barY, iw2, 4);

        if (shieldRatio >= 0) {
            g2.setColor(Color.ORANGE);
            iw2 = (int)(MAX_IMAGE * shieldRatio);
            g2.drawRect(0, barY + 6, MAX_IMAGE, 4);
            g2.fillRect(0, barY + 6, iw2, 4);
        }

        // Ship centered in the space below the bars.
        int belowTop = barY + 10;
        int belowH = Math.max(0, cellHeight - belowTop);
        int iy = belowTop + Math.max(0, (belowH - iconDrawH) / 2);
        int ix = (MAX_IMAGE - iconDrawW) / 2;
        g2.drawImage(
                image,
                ix, iy, ix + iconDrawW, iy + iconDrawH,
                imageContent.x, imageContent.y,
                imageContent.x + imageContent.width, imageContent.y + imageContent.height,
                null);

        int labelX = MAX_IMAGE + ICON_TEXT_GAP;
        int valueX = labelX + captionColumnWidth;

        TextRenderer tr = commons.text();
        paintLabeled(tr, g2, labelX, valueX, ty, nameCaption, nameValue);
        paintLabeled(tr, g2, labelX, valueX, ty + 9, ownerCaption, ownerValue);
        paintLabeled(tr, g2, labelX, valueX, ty + 18, parentCaption, parentValue);
        paintLabeled(tr, g2, labelX, valueX, ty + 27, firepowerCaption, firepowerValue);
        int y2 = ty + 36;
        if (bombsRocketsCaption != null) {
            paintLabeled(tr, g2, labelX, valueX, y2, bombsRocketsCaption, bombsRocketsValue);
            y2 += 9;
        }
        tr.paintTo(g2, labelX, y2, 7, color, defenseCaption + ": ");
        tr.paintTo(g2, valueX, y2, 7, defenseSegments);

        g2.translate(-x0, -y0);
    }

    /** Resolve opaque bounds for {@link #image}, using a shared sprite cache. */
    void computeImageContentBounds() {
        Rectangle cached = IMAGE_CONTENT_CACHE.get(image);
        if (cached != null) {
            imageContent.setBounds(cached);
            return;
        }
        Rectangle scanned = scanOpaqueBounds(image);
        IMAGE_CONTENT_CACHE.put(image, scanned);
        imageContent.setBounds(scanned);
    }

    /**
     * Bulk-scan non-transparent pixels of an angle frame.
     * @param img the sprite frame
     * @return opaque content bounds in image coordinates
     */
    static Rectangle scanOpaqueBounds(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] pixels = new int[w * h];
        img.getRGB(0, 0, w, h, pixels, 0, w);

        int top = h;
        int bottom = -1;
        int left = w;
        int right = -1;
        for (int y = 0; y < h; y++) {
            int row = y * w;
            for (int x = 0; x < w; x++) {
                if ((pixels[row + x] >>> 24) != 0) {
                    if (y < top) {
                        top = y;
                    }
                    if (y > bottom) {
                        bottom = y;
                    }
                    if (x < left) {
                        left = x;
                    }
                    if (x > right) {
                        right = x;
                    }
                }
            }
        }
        if (bottom < 0) {
            return new Rectangle(0, 0, w, h);
        }
        return new Rectangle(left, top, right - left + 1, bottom - top + 1);
    }

    /**
     * Scale the opaque ship art into {@link #MAX_IMAGE}.
     */
    void refreshIconDrawSize() {
        double scale = Math.min(
                1.0 * MAX_IMAGE / imageContent.width,
                1.0 * MAX_IMAGE / imageContent.height);
        if (scale > 1) {
            scale = 1;
        }
        iconDrawW = Math.max(1, (int)(imageContent.width * scale));
        iconDrawH = Math.max(1, (int)(imageContent.height * scale));
    }

    /**
     * Split a {@code "Caption : value"} label into caption and value.
     * @param formatted the localized formatted line
     * @return {@code [caption, value]}
     */
    static String[] splitLabeled(String formatted) {
        int i = formatted.indexOf(':');
        if (i < 0) {
            return new String[] { formatted.trim(), "" };
        }
        return new String[] {
            formatted.substring(0, i).trim(),
            formatted.substring(i + 1).trim()
        };
    }

    /**
     * Strip trailing colon / spaces from a caption-only label.
     * @param caption the localized caption
     * @return caption without trailing {@code :}
     */
    static String stripCaption(String caption) {
        String s = caption.trim();
        if (s.endsWith(":")) {
            s = s.substring(0, s.length() - 1).trim();
        }
        return s;
    }

    /**
     * @param tr text renderer
     * @param caption label caption
     * @return width of {@code caption + ": "}
     */
    static int captionWidth(TextRenderer tr, String caption) {
        return tr.getTextWidth(7, caption + ": ");
    }

    /**
     * @param tr text renderer
     * @param value label value
     * @return value text width
     */
    static int valueWidth(TextRenderer tr, String value) {
        return tr.getTextWidth(7, value);
    }

    /**
     * Paint a caption/value pair with a shared value column.
     * @param tr text renderer
     * @param g2 graphics
     * @param labelX caption origin X
     * @param valueX value origin X
     * @param y baseline Y
     * @param caption caption without colon
     * @param value value text
     */
    void paintLabeled(TextRenderer tr, Graphics2D g2, int labelX, int valueX, int y,
            String caption, String value) {
        tr.paintTo(g2, labelX, y, 7, color, caption + ": ");
        tr.paintTo(g2, valueX, y, 7, color, value);
    }
}
