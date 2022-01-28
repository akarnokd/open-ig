/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.model.Screens;
import hu.openig.render.RenderTools;
import hu.openig.screen.ScreenBase;
import hu.openig.utils.Exceptions;
import hu.openig.utils.XElement;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * The credits.
 * @author akarnokd, 2011.04.20.
 */
public class CreditsScreen extends ScreenBase {
    /** The animation timer. */
    Closeable animation;
    /** The rendering offset relative to the bottom of the screen. */
    int offset;
    /** The credits XML. */
    XElement credits;
    /** The image cache. */
    final Map<String, BufferedImage> imageCache = new HashMap<>();
    /** Indicator that it reached the end. */
    boolean reachedEnd;
    @Override
    public void onInitialize() {
    }

    @Override
    public void onEnter(Screens mode) {
        reachedEnd = false;
        imageCache.clear();
        offset = 0;
        animation = commons.register(25, new Action0() {
            @Override
            public void invoke() {
                doAnimation();
            }
        });
        credits = rl.getXML("credits");
    }

    @Override
    public void onLeave() {
        if (animation != null) {
            try {

                animation.close();

            } catch (IOException ex) {

                Exceptions.add(ex);

            }
            animation = null;
        }
        imageCache.clear();
    }

    @Override
    public void onFinish() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResize() {
    }

    @Override
    public Screens screen() {
        return Screens.CREDITS;
    }

    @Override
    public void onEndGame() {
        // TODO Auto-generated method stub

    }
    /** Perform the animation. */
    void doAnimation() {
        offset += 1;
        askRepaint();
        if (reachedEnd) {
            displayPrimary(Screens.MAIN);
        }
    }
    @Override
    public void draw(Graphics2D g2) {
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, width, height);
        RenderTools.setInterpolation(g2, true);
        RenderTools.setAntiailas(g2, true);
        int y = height - offset;
        for (XElement e : credits.children()) {
            switch (e.name) {
                case "h1": {
                    y += 5;
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
                    g2.setColor(Color.WHITE);

                    int w = g2.getFontMetrics().stringWidth(nvl(e.content));
                    g2.drawString(nvl(e.content), (width - w) / 2, y + g2.getFontMetrics().getAscent());

                    y += 15 + g2.getFontMetrics().getHeight();
                    break;
                }
                case "h2": {
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
                    g2.setColor(Color.YELLOW);

                    int w = g2.getFontMetrics().stringWidth(nvl(e.content));
                    g2.drawString(nvl(e.content), (width - w) / 2, y + g2.getFontMetrics().getAscent());

                    y += 10 + g2.getFontMetrics().getHeight();
                    break;
                }
                case "h3": {
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
                    g2.setColor(new Color(0xFFFFCC00));

                    int w = g2.getFontMetrics().stringWidth(nvl(e.content));
                    g2.drawString(nvl(e.content), (width - w) / 2, y + g2.getFontMetrics().getAscent());

                    y += 10 + g2.getFontMetrics().getHeight();
                    break;
                }
                case "p": {
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                    g2.setColor(Color.LIGHT_GRAY);

                    int w = g2.getFontMetrics().stringWidth(nvl(e.content));
                    g2.drawString(nvl(e.content), (width - w) / 2, y + g2.getFontMetrics().getAscent());

                    y += 10 + g2.getFontMetrics().getHeight();
                    break;
                }
                case "img":
                    BufferedImage img = imageCache.get(e.content);
                    if (img == null) {
                        img = rl.getImage(e.content);
                        imageCache.put(e.content, img);
                    }
                    g2.drawImage(img, (width - img.getWidth()) / 2, y, null);
                    y += 10 + img.getHeight();
                    break;
                default:
                    y += 20;
                    break;
            }
            if (y > height + 20) {
                break;
            }
        }
        if (y < 0) {
            reachedEnd = true;
        }
        RenderTools.setAntiailas(g2, false);
        RenderTools.setInterpolation(g2, false);
    }
    /**
     * If the parameter is null, an empty string is returned.
     * @param s the string
     * @return the s or empty string
     */
    String nvl(String s) {
        return s != null ? s : "";
    }
    @Override
    public boolean keyboard(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            displayPrimary(Screens.MAIN);
            e.consume();
            return true;
        }
        return false;
    }
}
