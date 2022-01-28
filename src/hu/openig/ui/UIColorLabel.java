/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.ui;

import hu.openig.model.Configuration;
import hu.openig.render.TextRenderer;
import hu.openig.render.TextRenderer.TextSegment;
import hu.openig.utils.U;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Label with text containing newlines and color settings.
 * The contents are always wrapped to the width.
 * @author akarnokd, 2012.06.24.
 */
public class UIColorLabel extends UIComponent {
    /** The parsed segments. */
    final List<List<TextSegment>> segments = new ArrayList<>();
    /** The default text color. */
    int textColor = TextRenderer.LIGHT_GREEN;
    /** The line spacing. */
    int spacing = 2;
    /** The text size. */
    int size;
    /** The text renderer. */
    TextRenderer tr;
    /** The horizontal alignment. */
    HorizontalAlignment align = HorizontalAlignment.LEFT;
    /** The current line. */
    final List<TextSegment> line = new ArrayList<>();
    /** The raw text. */
    String rawText;
    /**
     * Initialize the component.
     * @param size the text size
     * @param tr the text renderer
     */
    public UIColorLabel(int size, TextRenderer tr) {
        this.size = size;
        this.tr = tr;
    }
    /**
     * Set the text content.
     * <p>Color segments are enclosed by [c=FFFFFFFF][/c]</p>
     * <p>Start square bracket is simply [b]</p>
     * @param text the text
     */
    public void text(String text) {
        this.rawText = text;
        segments.clear();
        line.clear();

        StringBuilder b = new StringBuilder();
        int currentColor = textColor;
        Deque<Integer> colorstack = new LinkedList<>();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '[') {
                if (text.charAt(i + 1) == 'b') {
                    b.append("[");
                    i += 2;
                } else
                if (text.charAt(i + 1) == 'n') {
                    if (b.length() > 0) {
                        addSegment(b.toString(), currentColor);
                        b.setLength(0);
                    }
                    addNewline();
                    i += 2;
                } else
                if (text.charAt(i + 1) == 'c') {
                    String col = text.substring(i + 3, i + 11);
                    if (b.length() > 0) {
                        addSegment(b.toString(), currentColor);
                        b.setLength(0);
                    }
                    colorstack.push(currentColor);
                    currentColor = (int)(Long.parseLong(col, 16));
                    i += 11;
                } else
                if (text.charAt(i + 1) == '/' && text.charAt(i + 2) == 'c') {
                    if (b.length() > 0) {
                        addSegment(b.toString(), currentColor);
                        b.setLength(0);
                    }
                    currentColor = colorstack.pop();
                    i += 3;
                }
            } else
            if (c == '\n') {
                if (b.length() > 0) {
                    addSegment(b.toString(), currentColor);
                    b.setLength(0);
                }
                addNewline();
            } else
            if (c != '\r') {
                b.append(c);
            }
        }
        if (b.length() > 0) {
            addSegment(b.toString(), currentColor);
        }
        addNewline();
        height = Math.max(0, size * segments.size() + (segments.size() - 1) * spacing);
    }
    /**
     * Complete the current line segment and start a new one.
     */
    void addNewline() {
        segments.add(U.newArrayList(line));
        line.clear();
    }
    /**
     * Add a new segment.
     * @param text the text
     * @param color the color
     */
    void addSegment(String text, int color) {
        text = text.replaceAll("\\s{2,}", " ");
        List<String> words = new ArrayList<>();
        int i = 0;
        for (int j = 0; j < text.length(); j++) {
            if (text.charAt(j) == ' ') {
                words.add(text.substring(i, j + 1));
                i = j + 1;
            }
        }
        if (i < text.length()) {
            words.add(text.substring(i));
        }

        // add by words
        for (String w : words) {
            if (w.length() > 0) {
                line.add(new TextSegment(w, color));
            }
        }
        StringBuilder lineText = new StringBuilder();

        outer:
        while (true) {
            int idx = 0;
            for (TextSegment ts : line) {
                lineText.append(ts.text);
                int tw = tr.getTextWidth(size, lineText.toString());
                if (tw > width) {
                    List<TextSegment> sl = line.subList(0, idx);
                    segments.add(U.newArrayList(sl));
                    sl.clear();
                    lineText.setLength(0);
                    continue outer;
                }
                idx++;
            }
            break;
        }
    }
    /**
     * Set the label color.
     * @param textColor the text color to set
     * @return this
     */
    public UIColorLabel color(int textColor) {
        this.textColor = textColor;
        return this;
    }
    /**
     * Set the horizontal alignment.
     * @param a the horizontal alignment constant
     * @return this
     */
    public UIColorLabel horizontally(HorizontalAlignment a) {
        this.align = a;
        return this;
    }
    /**
     * Set the font size in pixels.
     * @param h the font size
     * @return this
     */
    public UIColorLabel size(int h) {
        this.size = h;
        return this;
    }
    /**
     * Set the row spacing for multiline display.
     * @param value the spacing in pixels
     * @return this
     */
    public UIColorLabel spacing(int value) {
        this.spacing = value;
        return this;
    }
    /** @return the text size. */
    public int textSize() {
        return size;
    }
    @Override
    public void draw(Graphics2D g2) {
        int dy = 0;
        for (List<TextSegment> line : segments) {
            int dx = 0;
            if (align != HorizontalAlignment.LEFT) {
                StringBuilder b = new StringBuilder();
                for (TextSegment ts : line) {
                    b.append(ts.text);
                }
                int tw = tr.getTextWidth(size, b.toString());
                dx = (width - tw) / 2;
            }

            tr.paintTo(g2, dx, dy, size, line);

            dy += size + spacing;
        }
    }
    /**
     * Readjust the text based on the current settings.
     */
    public void readjust() {
        text(rawText);
    }
    /**
     * @return The width of the longest line.
     */
    public int maxWidth() {
        int w = 0;
        for (List<TextSegment> line : segments) {
            StringBuilder b = new StringBuilder();
            for (TextSegment ts : line) {
                b.append(ts.text);
            }
            int tw = tr.getTextWidth(size, b.toString());
            w = Math.max(tw, w);
        }

        return w;
    }
    /**
     * Test program.
     * @param args no arguments
     */
    public static void main(String[] args) {
        Configuration config = new Configuration("open-ig-config.xml");
        config.load();
        TextRenderer tr = new TextRenderer(config.newResourceLocator(), false, 0);
        UIColorLabel l = new UIColorLabel(10, tr);
        l.width = 50;

        l.text("Hello [c=FFFFCC00]World[/c]![b]");
        System.out.println(l.segments);
    }
}
