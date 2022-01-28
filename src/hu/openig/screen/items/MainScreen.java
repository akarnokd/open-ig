/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.Pair;
import hu.openig.model.Configuration;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.api.SettingsPage;
import hu.openig.ui.UIComponent;
import hu.openig.ui.UIImage;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;
import hu.openig.ui.UIPanel;
import hu.openig.utils.Exceptions;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

/**
 * The main menu rendering and actions.
 * @author akarnokd, 2009.12.25.
 */
public class MainScreen extends ScreenBase {
    /**
     * The click label.
     * @author akarnokd, 2009.12.26.
     */
    class ClickLabel extends UIComponent {
        /** The action to invoke. */
        public Action0 action;
        /** The text size. */
        public int size;
        /** The text label. */
        public String label;
        /** The selected state. */
        public boolean selected;
        /** The pressed state. */
        public boolean pressed;
        /**
         * Constructor.
         * @param size the text size
         * @param label the label
         */
        public ClickLabel(int size, String label) {
            this.height = size + 10;
            this.size = size;
            this.label = label;
            this.width = preferredWidth();
        }
        /** @return the preferred width. */
        public int preferredWidth() {
            return commons.text().getTextWidth(size, get(label)) + 20;
        }
        /** Sizes the label to the preferred text width. */
        public void sizeToContent() {
            this.width = preferredWidth();
        }
        /** Invoke the associated action. */
        public void invoke() {
            if (action != null) {
                action.invoke();
            }
        }
        @Override
        public void draw(Graphics2D g2) {
            int color = 0xFFFFCC00;
            if (!enabled) {
                color = 0xFFC0C0C0;
            } else
            if (pressed) {
                color = 0xFFFF0000;
            } else
            if (selected) {
                color = 0xFFFFFFFF;
            }
            Composite save0 = g2.getComposite();
            g2.setComposite(AlphaComposite.SrcOver.derive(0.5f));
            if (selected) {
                g2.setColor(new Color(224, 0, 0));
                g2.fillRoundRect(0, 0, width, height, 10, 10);
            } else {
                g2.setColor(Color.BLACK);
                g2.fillRoundRect(0, 0, width, height, 10, 10);
            }
            g2.setComposite(save0);

            String s = get(label);

            int dx = (width - commons.text().getTextWidth(size, s)) / 2;

            commons.text().paintTo(g2, dx + 2, 7, size, 0xFF000000, s);
            commons.text().paintTo(g2, dx + 1, 6, size, 0xFF000000, s);
            commons.text().paintTo(g2, dx + 0, 5, size, color, s);
        }
        @Override
        public boolean mouse(UIMouse e) {
            if (e.has(Type.ENTER)) {
                selected = true;
                return true;
            } else
            if (e.has(Type.LEAVE)) {
                selected = false;
                pressed = false;
                return true;
            } else
            if (e.has(Type.DOWN)) {
                pressed = true;
                return true;
            } else
            if (e.has(Type.UP)) {
                if (pressed) {
                    pressed = false;
                    action.invoke();
                    return true;
                }
            }
            return false;
        }
    }
    /** The screen base. */
    Rectangle base = new Rectangle(0, 0, 640, 442);
    /** The background image. */
    BufferedImage background;
    /** The random used for background selection. */
    Random rnd = new Random();
    /** The continue label. */
    ClickLabel continueLabel;
    /** The achievements screen. */
    ClickLabel achievements;
    /** The campaign click label. */
    ClickLabel campaign;
    /** The skirmish click label. */
    ClickLabel skirmish;
    /** Label button. */
    ClickLabel load;
    /** Label button. */
    ClickLabel multiplayer;
    /** Label button. */
    ClickLabel settings;
    /** Label button. */
    ClickLabel videosLabel;
    /** Label button. */
    ClickLabel introLabel;
    /** Label button. */
    ClickLabel titleLabel;
    /** Label button. */
    ClickLabel exit;
    /** Label button. */
    ClickLabel creditsLabel;
    /** Label button. */
    ClickLabel profileLabel;
    /** The language selector panel. */
    UIPanel languagePanel;
    /** The language changer. */
    UIImage changeLanguage;
    /** Resume the last gameplay. */
    void doContinue() {
        continueLabel.enabled(isSaveAvailable());
        if (continueLabel.enabled()) {
            load(null);
        }
    }
    /** Perform the exit. */
    void doExit() {
        effectSound(SoundType.GOOD_BYE);
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            // ignored
        }
        exit();
    }
    /**
     * Play the intro videos.
     */
    protected void doPlayIntro() {
        commons.control().playVideos("intro/intro_1", "intro/intro_2", "intro/intro_3");
    }
    /**
     * Play the title video.
     */
    protected void doPlayTitle() {
        commons.control().playVideos("intro/gt_interactive_intro");
    }
    /** Display the settings video. */
    void doSettings() {
        // reload global configuration
        config.load();
        commons.control().displayOptions();
        LoadSaveScreen scr = commons.control().getScreen(Screens.LOAD_SAVE);
        scr.maySave(false);
        scr.displayPage(SettingsPage.AUDIO);
    }

    @Override
    public void draw(Graphics2D g2) {
        onResize(); // repaint might come before an onResize

        int w = background.getWidth();
        g2.setColor(Color.BLACK);
        g2.fillRect(0, 0, getInnerWidth(), getInnerHeight());

        AffineTransform save0 = scaleDraw(g2, base, margin());

        g2.translate(base.x, base.y);
        g2.drawImage(background, 0, 0, null);

        RenderTools.setInterpolation(g2, true);
        BufferedImage tli = commons.background().openigTextLogo;
        int tliw = tli.getWidth() * 3 / 4;
        int tlih = tli.getHeight() * 3 / 4;
        int tlw = (w - tliw) / 2;
        g2.drawImage(tli, tlw, 8, tliw, tlih, null);
        RenderTools.setInterpolation(g2, false);

        int vx = 70;
        int vy = 428;
        int vs = 10;
        String vstr = "v" + Configuration.VERSION + " - " + config.language;
        int w1 = commons.text().getTextWidth(vs, vstr);
        g2.setColor(new Color(0, 0, 0, 128));
        g2.fillRect(vx - 3, vy - 3, w1 + 6, vs + 6);
        commons.text().paintTo(g2, vx + 1, vy + 1, vs, 0xFF000000, vstr);
        commons.text().paintTo(g2, vx, vy, vs, 0xFFFF0000, vstr);

        g2.translate(-base.x, -base.y);

        // draw profile
        String pn = commons.profile.name;
        int pnw = commons.text().getTextWidth(14, pn);

        int profw = pnw + profileLabel.width + 40 + achievements.width;

        int dx = (w - profw) / 2;
        profileLabel.x = base.x + dx;
        profileLabel.enabled(true);

        achievements.x = base.x + dx + 40 + pnw + profileLabel.width;

        g2.setColor(new Color(0, 0, 0, 128));
        g2.fillRect(base.x + dx + profileLabel.width + 15, profileLabel.y + 2, pnw + 10, 20);
        commons.text().paintTo(g2, base.x + dx + profileLabel.width + 20, profileLabel.y + 5, 14, TextRenderer.GREEN, pn);

        Paint savep = g2.getPaint();

        int gx1 = profileLabel.x;
        int gx2 = profileLabel.x + profw / 2;
        int gx3 = achievements.x + achievements.width;
        int gy = profileLabel.y + profileLabel.size + 13;

        g2.setPaint(new GradientPaint(gx1, gy, new Color(255, 255, 255, 192), gx2, gy, new Color(255, 255, 0, 255)));
        g2.fillRect(gx1, gy, gx2 - gx1, 2);
        g2.setPaint(new GradientPaint(gx3, gy, new Color(255, 255, 255, 192), gx2, gy, new Color(255, 255, 0, 255)));
        g2.fillRect(gx2, gy, gx3 - gx2, 2);
//        g2.setColor(Color.YELLOW);
//        g2.drawLine(gx1, gy, gx3, gy);

        g2.setPaint(savep);
        // draw other labels

        super.draw(g2);

        g2.setTransform(save0);

    }
    @Override
    public boolean mouse(UIMouse e) {
        scaleMouse(e, base, margin());
        if (e.has(Type.DOWN) && languagePanel.visible()
                && !languagePanel.within(e)) {
            languagePanel.visible(false);
            if (changeLanguage.within(e)) {
                return true;
            }
        }
        return super.mouse(e);
    }
    @Override
    public void onEndGame() {
        // TODO Auto-generated method stub

    }
    @Override
    public void onEnter(Screens mode) {
        // if returned to main menu, allow new aggregation of exceptions
        Exceptions.clear();

        selectRandomBackground();
        onResize();

        checkExistingSave();
    }
    /**
     * Search for previous saves to continue.
     */
    public void checkExistingSave() {
        continueLabel.enabled(false);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean found = isSaveAvailable();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        continueLabel.enabled(found);
                        askRepaint();
                        if (found) {
                            if (config.continueLastGame) {
                                config.continueLastGame = false;
                                continueLabel.action.invoke();
                            }
                        }
                    }
                });
            }
        }, "Save-Lookup");
        t.start();
    }
    /**
     * @return Check if save file is available.
     */
    boolean isSaveAvailable() {
        File dir = new File("save/" + commons.profile.name);
        if (dir.exists()) {
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.startsWith("save-") && name.endsWith(".xml.gz");
                }
            });
            return files != null && files.length > 0;
        }
        return false;
    }
    @Override
    public void onFinish() {
    }

    @Override
    public void onInitialize() {
        campaign = new ClickLabel(20, "mainmenu.campaign");
        campaign.action = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.UI_ACKNOWLEDGE_2);
                displayPrimary(Screens.SINGLEPLAYER);
            }
        };

        skirmish = new ClickLabel(20, "mainmenu.skirmish");
        skirmish.action = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.UI_ACKNOWLEDGE_2);
                displayPrimary(Screens.SKIRMISH);
            }
        };

        continueLabel = new ClickLabel(14, "mainmenu.continue");
        continueLabel.action = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.UI_ACKNOWLEDGE_2);
                doContinue();
            }
        };
        continueLabel.enabled(false);

        load = new ClickLabel(14, "mainmenu.load");
        load.action = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.UI_ACKNOWLEDGE_2);
                commons.control().displayOptions();
                LoadSaveScreen scr = commons.control().getScreen(Screens.LOAD_SAVE);
                scr.displayPage(SettingsPage.LOAD_SAVE);
                scr.maySave(false);
            }
        };

        multiplayer = new ClickLabel(20 , "mainmenu.multiplayer");
        multiplayer.action = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.UI_ACKNOWLEDGE_2);
                // TODO
            }
        };
        multiplayer.enabled(false);

        settings = new ClickLabel(20, "mainmenu.settings");
        settings.action = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.UI_ACKNOWLEDGE_2);
                doSettings();
            }
        };

        videosLabel = new ClickLabel(20, "mainmenu.videos");
        videosLabel.action = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.UI_ACKNOWLEDGE_2);
                displaySecondary(Screens.VIDEOS);
            }
        };

        introLabel = new ClickLabel(14, "mainmenu.videos.intro");
        introLabel.action = new Action0() {
            @Override
            public void invoke() {
                doPlayIntro();
            }
        };
        titleLabel = new ClickLabel(14, "mainmenu.videos.title");
        titleLabel.action = new Action0() {
            @Override
            public void invoke() {
                doPlayTitle();
            }
        };

        creditsLabel = new ClickLabel(14, "credits");
        creditsLabel.action = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.UI_ACKNOWLEDGE_2);
                doPlayCredits();
            }
        };

        exit = new ClickLabel(20, "mainmenu.exit");
        exit.action = new Action0() { @Override public void invoke() {

            doExit();

        } };

        achievements = new ClickLabel(14, "achievements");
        achievements.action = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.UI_ACKNOWLEDGE_2);
                displaySecondary(Screens.ACHIEVEMENTS);
            }
        };

        profileLabel = new ClickLabel(14, "profile");
        profileLabel.action = new Action0() {
            @Override
            public void invoke() {
                buttonSound(SoundType.UI_ACKNOWLEDGE_2);
                displaySecondary(Screens.PROFILE);
            }
        };

        languagePanel = new UIPanel();
        languagePanel.visible(false);
        languagePanel.size(500, 300);
        languagePanel.backgroundColor(0xC0000000);
        languagePanel.borderColor(0xFFE0E0E0);
        languagePanel.z = 10;

        URL flagsURL = MainScreen.class.getResource("/hu/openig/gfx/flags.png");
        if (flagsURL != null) {
            try {
                BufferedImage bimg = ImageIO.read(flagsURL);
                changeLanguage = new UIImage(bimg);
                changeLanguage.onClick = new Action0() {
                    @Override
                    public void invoke() {
                        languagePanel.visible(true);
                    }
                };
                changeLanguage.tooltip(get("languages.change.tooltip"));
            } catch (IOException ex) {
                Exceptions.add(ex);
            }
        }

        int fy = 10;
        for (int i = 0; i < config.languageSupport.size(); i += 2) {
            final String code = config.languageSupport.get(i);
            String file = config.languageSupport.get(i + 1);
            String resName = "/hu/openig/gfx/" + file + ".png";
            URL imgURL = MainScreen.class.getResource(resName);
            if (imgURL != null) {
                try {
                    BufferedImage bimg = ImageIO.read(imgURL);
                    UIImage img = new UIImage(bimg);

                    languagePanel.add(img);

                    ClickLabel lbl = new ClickLabel(14, "languages." + code);

                    img.x = 10;
                    img.y = fy;

                    lbl.x = img.width + 20;
                    lbl.y = fy + 5;

                    languagePanel.add(lbl);

                    img.onClick = new Action0() {
                        @Override
                        public void invoke() {
                            languagePanel.visible(false);
                            switchTo(code);
                        }
                    };

                    lbl.action = img.onClick;
                } catch (IOException ex) {
                    Exceptions.add(ex);
                }
            } else {
                Exceptions.add(new RuntimeException("Missing: " + resName));
            }
            fy += 40;
        }

        languagePanel.pack();
        languagePanel.width += 10;
        languagePanel.height += 10;

        addThis();
    }

    @Override
    public void onLeave() {
        // TODO Auto-generated method stub

    }
    @Override
    public void onResize() {
        if (background == null) {
            selectRandomBackground();
        }
        scaleResize(base, margin());

        campaign.location(0, base.y + 135);
        skirmish.location(0, base.y + 135);

        continueLabel.location(0, base.y + 168);
        load.location(0, base.y + 195);
        multiplayer.location(0, base.y + 225);
        settings.location(0, base.y + 258);
        videosLabel.location(0, base.y + 293);
        creditsLabel.location(0, base.y + 350);
        exit.location(0, base.y + 385);

        for (ClickLabel cl : Arrays.asList(continueLabel, load,

                multiplayer, settings, videosLabel, exit, creditsLabel)) {
            cl.x = base.x + base.width / 2 - cl.width / 2;
        }
        introLabel.x = base.x + base.width / 3 - introLabel.width / 2;
        titleLabel.x = base.x + base.width * 2 / 3 - titleLabel.width / 2;

        introLabel.y = base.y + 325;
        titleLabel.y = base.y + 325;

        profileLabel.location(base.x + 20, base.y + 100);
        achievements.location(base.x + 120, base.y + 100);

        int w12 = Math.max(campaign.preferredWidth(), skirmish.preferredWidth());
        campaign.width = w12;
        skirmish.width = w12;

        campaign.x = base.x + base.width / 4 - campaign.width / 2;
        skirmish.x = base.x + base.width * 3 / 4 - skirmish.width / 2;

        languagePanel.x = base.x + (base.width - languagePanel.width) / 2;
        languagePanel.y = base.y + (base.height - languagePanel.height) / 2;

        changeLanguage.x = base.x + 10;
        changeLanguage.y = base.y + 405;
    }
    @Override
    public Screens screen() {
        return Screens.MAIN;
    }
    /**
     * Set the background randomly.
     */
    protected void selectRandomBackground() {
        background = commons.background().start[rnd.nextInt(commons.background().start.length)];
    }
    /**
     * Use the given background for the main menu.
     * @param newBackground the new background
     */
    public void useBackground(BufferedImage newBackground) {
        if (newBackground == null) {
            throw new IllegalArgumentException("newBackground is null");
        }
        background = newBackground;
        askRepaint();
    }
    @Override
    public void load(String name) {
        commons.control().load(name);
    }
    /** Play the credits. */
    void doPlayCredits() {
        displayPrimary(Screens.CREDITS);
    }
    /**

     * Switch to the given language.
     * @param newLang the new language
     */
    void switchTo(String newLang) {
        commons.control().switchLanguage(newLang);
        selectRandomBackground();
        checkExistingSave();

        askRepaint();
    }
    @Override
    public boolean keyboard(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            if (languagePanel.visible()) {
                languagePanel.visible(false);
                return true;
            }
        } else
        if (e.getKeyChar() == 'l' || e.getKeyChar() == 'L') {
            if (!languagePanel.visible()) {
                languagePanel.visible(true);
                return true;
            }
        } else
        if (e.getKeyCode() == KeyEvent.VK_O) {
            doSettings();
        }
        return super.keyboard(e);
    }
    @Override
    protected Point scaleBase(int mx, int my) {
        UIMouse m = new UIMouse();
        m.x = mx;
        m.y = my;
        scaleMouse(m, base, margin());

        return new Point(m.x, m.y);
    }
    @Override
    protected Pair<Point, Double> scale() {
        Pair<Point, Double> s = scale(base, margin());
        return Pair.of(new Point(base.x, base.y), s.second);
    }
}
