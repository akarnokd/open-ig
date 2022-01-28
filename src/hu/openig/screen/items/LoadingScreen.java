/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.model.Screens;
import hu.openig.screen.ScreenBase;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

/**
 * In progress loading screen.
 * @author akarnokd, 2010.01.17.
 */
public class LoadingScreen extends ScreenBase {
    /** The rotating cd icon location. */
    final Rectangle cd = new Rectangle();
    /** The text location. */
    final Rectangle text = new Rectangle();
    @Override
    public void onResize() {
    }
    /** The animation timer. */
    Timer animation;
    @Override
    public void onFinish() {
        animation.stop();
    }

    @Override
    public void onInitialize() {
        animation = new Timer(100, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doAnimate();
            }
        });
    }
    /** The rolling phase. */
    int rollingPhase;
    /** The number of dots. */
    int dots;
    /** Perform the next animation phase. */
    void doAnimate() {
        rollingPhase = (rollingPhase + 1) % commons.research().rolling.length;
        dots = (dots + 1) % 40;
        askRepaint();
    }

    @Override
    public void onEnter(Screens mode) {
        resize();
        animation.start();
    }

    @Override
    public void onLeave() {
        animation.stop();
    }

    @Override
    public void draw(Graphics2D g2) {
        Composite cp = g2.getComposite();
        g2.setComposite(AlphaComposite.SrcOver.derive(0.7f));
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getInnerWidth(), getInnerHeight());
        g2.setComposite(cp);

        cd.height = commons.research().rolling[0].getHeight() * 2;
        cd.width = commons.research().rolling[0].getWidth() * 2;
        cd.y = (getInnerHeight() - cd.height) / 2;
        text.height = 20;
        text.width = commons.text().getTextWidth(text.height, get("loading") + "...");
        text.y = (getInnerHeight() - text.height) / 2;
        int tw = cd.width + 10 + text.width;

        cd.x = (getInnerWidth() - tw) / 2;
        text.x = cd.x + cd.width + 10;

        g2.drawImage(commons.research().rolling[rollingPhase], cd.x, cd.y, cd.width, cd.height, null);

        StringBuilder sb = new StringBuilder(get("loading"));
        for (int i = 0; i < dots / 10; i++) {
            sb.append('.');
        }
        commons.text().paintTo(g2, text.x, text.y, text.height, 0xFFFFFF00, sb.toString());
    }
    @Override
    public Screens screen() {
        return Screens.LOADING;
    }
    @Override
    public void onEndGame() {
        // TODO Auto-generated method stub

    }
}
