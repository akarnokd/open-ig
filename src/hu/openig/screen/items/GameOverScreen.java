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
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.api.SettingsPage;
import hu.openig.ui.HorizontalAlignment;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UILabel;
import hu.openig.ui.UIMouse;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.Timer;

/**
 * The game over screen.
 * @author akarnokd, 2012.01.14.
 */
public class GameOverScreen extends ScreenBase {
    /** The animation phase. */
    int phase = 0;
    /** The animation timer. */
    Timer anim;
    /** Show the game over image? */
    boolean showImage;
    /** The image alpha level. */
    float imageAlpha;
    /** The animation speed in milliseconds. */
    static final int ANIMATION_SPEED = 50;
    /** The maximum phase. */
    static final int MAX_PHASE = 40;
    /** The player won! */
    public boolean win;
    /** Continue the gameplay. */
    UIGenericButton continueButton;
    /** Load. */
    UIGenericButton loadButton;
    /** Return to main menu. */
    UIGenericButton mainMenuButton;
    /** The win label. */
    UILabel winLabel;
    @Override
    public void onInitialize() {
        anim = new Timer(ANIMATION_SPEED, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doAnimation();
            }
        });
        continueButton = new UIGenericButton(get("win.continue_game"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
        continueButton.onClick = new Action0() {
            @Override
            public void invoke() {
                hideSecondary();
                commons.control().displayStatusbar();
                commons.nongame = false;
                commons.playMusic();
            }
        };
        loadButton = new UIGenericButton(get("win.load"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
        loadButton.onClick = new Action0() {
            @Override
            public void invoke() {
                hideSecondary();
                commons.control().displayStatusbar();
                commons.control().displayOptions(true, SettingsPage.LOAD_SAVE);
            }
        };
        mainMenuButton = new UIGenericButton(get("win.main_menu"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
        mainMenuButton.onClick = new Action0() {
            @Override
            public void invoke() {
                commons.control().endGame();
                displayPrimary(Screens.MAIN);
            }
        };
        winLabel = new UILabel(get("win.win"), 20, commons.text());
        addThis();
    }

    /** Perform an animation step. */
    void doAnimation() {
        phase++;
        StatusbarScreen sts = commons.control().getScreen(Screens.STATUSBAR);
        float len = MAX_PHASE / 2f;
        if (phase * 2 < MAX_PHASE) {
            sts.overlay = new Color(0f, 0f, 0f, phase / len);
        } else
        if (phase * 2 >= MAX_PHASE) {
            sts.overlay = null;
            commons.force = false;
            commons.nongame = true;
            commons.control().hideStatusbar();
            showImage = true;
            imageAlpha = (phase - len) / len;
        }
        if (phase >= MAX_PHASE) {
            imageAlpha = 1f;
            anim.stop();
        }
        askRepaint();
    }

    @Override
    public void onEnter(Screens mode) {
        commons.force = true;
        imageAlpha = 0.0f;
        phase = 0;
        showImage = false;
        anim.start();
    }

    @Override
    public boolean keyboard(KeyEvent e) {
        if (phase < MAX_PHASE) {
            e.consume();
            return true;
        }
        if (!win) {
            commons.control().endGame();
            displayPrimary(Screens.MAIN);
        }
        e.consume();
        return true;
    }
    @Override
    public boolean mouse(UIMouse e) {
        if (phase < MAX_PHASE) {
            return false;
        }
//        if (e.has(Type.DOWN) && !win) {
//            commons.control().endGame();
//            displayPrimary(Screens.MAIN);
//            return true;
//        }
        return super.mouse(e);
    }

    @Override
    public void onLeave() {
        anim.stop();
        win = false;
    }

    @Override
    public void onFinish() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onResize() {
        // TODO Auto-generated method stub

    }

    @Override
    public void draw(Graphics2D g2) {

        continueButton.visible(false);
        mainMenuButton.visible(false);
        winLabel.visible(false);
        if (showImage) {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, width, height);
            BufferedImage gameover = commons.background().gameover;
            Composite save0 = g2.getComposite();
            if (imageAlpha < 1) {
                g2.setComposite(AlphaComposite.SrcOver.derive(imageAlpha));
            }

            int bw = gameover.getWidth();
            int bh = gameover.getHeight();
            int dx = (width - bw) / 2;
            int dy = (height - bh) / 2;

            g2.drawImage(gameover, dx, dy, null);

            loadButton.visible(true);
            mainMenuButton.visible(true);
            continueButton.visible(win);
            winLabel.visible(win);

            winLabel.location(dx, dy + 40);
            winLabel.size(bw, 30);
            winLabel.horizontally(HorizontalAlignment.CENTER);
            winLabel.color(TextRenderer.YELLOW);

            int by = dy + bh - 50;

            placeButtons(dx, by, bw, 20, continueButton, loadButton, mainMenuButton);

            super.draw(g2);

            g2.setComposite(save0);
        }
    }
    /**
     * Place buttons equally in the given width.
     * @param dx the delta x
     * @param dy the delta y
     * @param width the available width
     * @param gap the gap between buttons
     * @param comps the components
     */
    void placeButtons(int dx, int dy, int width, int gap, UIComponent... comps) {
        int cw = 0;
        for (UIComponent c : comps) {
            cw += c.width;
        }
        cw += gap * Math.max(0, comps.length - 1);

        dx += (width - cw) / 2;
        for (UIComponent c : comps) {
            c.x = dx;
            c.y = dy;
            dx += gap + c.width;
        }

    }

    @Override
    public Screens screen() {
        return Screens.GAME_OVER;
    }

    @Override
    public void onEndGame() {
        // TODO Auto-generated method stub

    }

}
