/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import java.awt.*;
import java.awt.event.KeyEvent;

import hu.openig.core.*;
import hu.openig.model.*;
import hu.openig.model.CustomBalanceSettings.BuildingMoraleCalculationMode;
import hu.openig.render.RenderTools;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.*;

/**
 * Screen for configuring the custom balance options of {@link CustomBalanceSettings}.
 * @author akarnokd, 2022.01.28.
 */
public class CustomBalanceScreen extends ScreenBase {
    /** The panel base rectangle. */
    final Rectangle base = new Rectangle(0, 0, 640, 400);
    /**
     * The record for the values being edited.
     */
    final CustomBalanceSettings settings = new CustomBalanceSettings();
    /**
     * The action to invoke once the user has chosen the traits.
     * Called with null if the user cancelled.
     */
    public Action1<CustomBalanceSettings> onComplete;
    /** The select traits label. */
    UILabel titleLabel;
    /** Accept selection. */
    UIGenericButton ok;
    /** Accept selection. */
    UIGenericButton reset;
    /** Cancel selection. */
    UIGenericButton cancel;
    /** Bonus money label. */
    UILabel bonusMoney;
    /** Bonus production label. */
    UILabel bonusProduction;
    /** Relationship deterioration label. */
    UILabel deterioration;
    /** Morale calculation mode label. */
    UILabel moraleMode;
    /** Bonus money amount. */
    UISpinner bonusMoneySpinner;
    /** Bonus production amount. */
    UISpinner bonusProductionSpinner;
    /** Relationship deterioration amount. */
    UISpinner deteriorationSpinner;
    /** Morale calculation mode value. */
    UISpinner moraleModeSpinner;
    @Override
    public void onResize() {
        base.width = commons.common().infoEmptyTop.getWidth();
        base.y = 10;
        base.height = height - 60;

        RenderTools.centerScreen(base, width, height, true);

        titleLabel.location(base.x + 10, base.y + 10);

        int gap = 30;
        int w = ok.width + cancel.width + gap * 2 + reset.width;
        ok.location(base.x + (640 - w) / 2, base.y + base.height - 40);
        reset.location(ok.x + ok.width + gap, ok.y);
        cancel.location(reset.x + reset.width + gap, ok.y);

        int ddy = 28;
        int dy = 50;

        bonusMoney.location(base.x + 30, base.y + dy + 8);
        dy += ddy;
        bonusMoneySpinner.location(base.x + 60, base.y + dy);
        bonusMoneySpinner.width = 300;
        dy += ddy;

        bonusProduction.location(base.x + 30, base.y + dy + 8);
        dy += ddy;
        bonusProductionSpinner.location(base.x + 60, base.y + dy);
        bonusProductionSpinner.width = 300;
        dy += ddy;

        deterioration.location(base.x + 30, base.y + dy + 8);
        dy += ddy;
        deteriorationSpinner.location(base.x + 60, base.y + dy);
        deteriorationSpinner.width = 300;
        dy += ddy;

        moraleMode.location(base.x + 30, base.y + dy + 8);
        dy += ddy;
        moraleModeSpinner.location(base.x + 60, base.y + dy);
        moraleModeSpinner.width = 350;
        dy += ddy;
    }
    @Override
    public Screens screen() {
        return Screens.CUSTOM_BALANCE;
    }

    @Override
    public void onInitialize() {
        titleLabel = new UILabel(get("custombalance.title"), 20, commons.text());

        ok = new UIGenericButton(get("custombalance.ok"), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
        ok.onClick = new Action0() {
            @Override
            public void invoke() {
                if (onComplete != null) {
                    try {
                        onComplete.invoke(settings);
                    } finally {
                        onComplete = null;
                    }
                }
                hideSecondary();
            }
        };
        reset = new UIGenericButton(get("custombalance.reset"), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
        reset.onClick = new Action0() {
            @Override
            public void invoke() {
                doReset();
            }
        };
        cancel = new UIGenericButton(get("custombalance.cancel"), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
        cancel.onClick = new Action0() {
            @Override
            public void invoke() {
                if (onComplete != null) {
                    try {
                        onComplete.invoke(null);
                    } finally {
                        onComplete = null;
                    }
                }
                hideSecondary();
            }
        };

        createBonusMoney();
        createBonusProduction();
        createDeterioration();
        createMoraleMode();

        addThis();
    }

    void createBonusMoney() {
        final UIImageButton prev = new UIImageButton(commons.common().moveLeft);
        prev.setDisabledPattern(commons.common().disabledPattern);
        prev.setHoldDelay(250);
        final UIImageButton next = new UIImageButton(commons.common().moveRight);
        next.setDisabledPattern(commons.common().disabledPattern);
        next.setHoldDelay(250);

        prev.onClick = new Action0() {
            @Override
            public void invoke() {
                int increment = 25000;
                if (prev.lastEvent != null && prev.lastEvent.modifiers.contains(UIMouse.Modifier.SHIFT)) {
                    increment *= 10;
                }
                // // buttonSound(SoundType.CLICK_MEDIUM_2);
                settings.nonPlayerBonusMoneyPerDay = Math.max(0, settings.nonPlayerBonusMoneyPerDay - increment);
                doRepaint();
            }
        };
        next.onClick = new Action0() {
            @Override
            public void invoke() {
                int increment = 25000;
                if (next.lastEvent != null && next.lastEvent.modifiers.contains(UIMouse.Modifier.SHIFT)) {
                    increment *= 10;
                }
                // // buttonSound(SoundType.CLICK_MEDIUM_2);
                settings.nonPlayerBonusMoneyPerDay = Math.min(2000000, settings.nonPlayerBonusMoneyPerDay + increment);
                doRepaint();
            }
        };

        bonusMoneySpinner = new UISpinner(14, prev, next, commons.text());
        bonusMoneySpinner.getValue = new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                StringBuilder b = new StringBuilder();
                b.append(settings.nonPlayerBonusMoneyPerDay);
                b.append(" cr/").append(get("custombalance.day"));
                return b.toString();
            }
        };
        bonusMoney = new UILabel(get("custombalance.bonusmoney"), 14, commons.text());
    }
    void createBonusProduction() {
        final UIImageButton prev = new UIImageButton(commons.common().moveLeft);
        prev.setDisabledPattern(commons.common().disabledPattern);
        prev.setHoldDelay(250);
        final UIImageButton next = new UIImageButton(commons.common().moveRight);
        next.setDisabledPattern(commons.common().disabledPattern);
        next.setHoldDelay(250);

        prev.onClick = new Action0() {
            @Override
            public void invoke() {
                // buttonSound(SoundType.CLICK_MEDIUM_2);
                int increment = 10;
                if (prev.lastEvent != null && prev.lastEvent.modifiers.contains(UIMouse.Modifier.SHIFT)) {
                    increment *= 10;
                }
                settings.nonPlayerProductionBoostPercent = Math.max(0, settings.nonPlayerProductionBoostPercent - increment);
                doRepaint();
            }
        };
        next.onClick = new Action0() {
            @Override
            public void invoke() {
                // buttonSound(SoundType.CLICK_MEDIUM_2);
                int increment = 10;
                if (next.lastEvent != null && next.lastEvent.modifiers.contains(UIMouse.Modifier.SHIFT)) {
                    increment *= 10;
                }
                settings.nonPlayerProductionBoostPercent = Math.min(500, settings.nonPlayerProductionBoostPercent + increment);
                doRepaint();
            }
        };

        bonusProductionSpinner = new UISpinner(14, prev, next, commons.text());
        bonusProductionSpinner.getValue = new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                StringBuilder b = new StringBuilder();
                b.append(settings.nonPlayerProductionBoostPercent);
                b.append(" %");
                return b.toString();
            }
        };
        bonusProduction = new UILabel(get("custombalance.bonusproduction"), 14, commons.text());
    }

    void createDeterioration() {
        final UIImageButton prev = new UIImageButton(commons.common().moveLeft);
        prev.setDisabledPattern(commons.common().disabledPattern);
        prev.setHoldDelay(250);
        final UIImageButton next = new UIImageButton(commons.common().moveRight);
        next.setDisabledPattern(commons.common().disabledPattern);
        next.setHoldDelay(250);

        prev.onClick = new Action0() {
            @Override
            public void invoke() {
                // buttonSound(SoundType.CLICK_MEDIUM_2);
                int increment = 5;
                if (prev.lastEvent != null && prev.lastEvent.modifiers.contains(UIMouse.Modifier.SHIFT)) {
                    increment *= 10;
                }
                settings.nonPlayerRelationshipDeterioration = Math.max(0, settings.nonPlayerRelationshipDeterioration - increment);
                doRepaint();
            }
        };
        next.onClick = new Action0() {
            @Override
            public void invoke() {
                // buttonSound(SoundType.CLICK_MEDIUM_2);
                int increment = 5;
                if (next.lastEvent != null && next.lastEvent.modifiers.contains(UIMouse.Modifier.SHIFT)) {
                    increment *= 10;
                }
                settings.nonPlayerRelationshipDeterioration = Math.min(200, settings.nonPlayerRelationshipDeterioration + increment);
                doRepaint();
            }
        };

        deteriorationSpinner = new UISpinner(14, prev, next, commons.text());
        deteriorationSpinner.getValue = new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                StringBuilder b = new StringBuilder();
                b.append(String.format("%.2f", settings.nonPlayerRelationshipDeterioration / 100d));
                b.append(" ").append(get("custombalance.pointsperday"));
                return b.toString();
            }
        };
        deterioration = new UILabel(get("custombalance.deterioration"), 14, commons.text());
    }

    void createMoraleMode() {
        final UIImageButton prev = new UIImageButton(commons.common().moveLeft);
        prev.setDisabledPattern(commons.common().disabledPattern);
        prev.setHoldDelay(250);
        final UIImageButton next = new UIImageButton(commons.common().moveRight);
        next.setDisabledPattern(commons.common().disabledPattern);
        next.setHoldDelay(250);

        prev.onClick = new Action0() {
            @Override
            public void invoke() {
                // buttonSound(SoundType.CLICK_MEDIUM_2);
                settings.buildingMoraleCalculationMode = BuildingMoraleCalculationMode.values()[Math.max(0, settings.buildingMoraleCalculationMode.ordinal() - 1)];
                setTooltipText(moraleModeSpinner, get("custombalance.moralemode." + settings.buildingMoraleCalculationMode + ".desc"));
                doRepaint();
            }
        };
        next.onClick = new Action0() {
            @Override
            public void invoke() {
                // buttonSound(SoundType.CLICK_MEDIUM_2);
                BuildingMoraleCalculationMode[] vs = BuildingMoraleCalculationMode.values();
                settings.buildingMoraleCalculationMode = vs[Math.min(vs.length - 1, settings.buildingMoraleCalculationMode.ordinal() + 1)];
                setTooltipText(moraleModeSpinner, get("custombalance.moralemode." + settings.buildingMoraleCalculationMode + ".desc"));
                doRepaint();
            }
        };

        moraleModeSpinner = new UISpinner(14, prev, next, commons.text());
        moraleModeSpinner.getValue = new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                return get("custombalance.moralemode." + settings.buildingMoraleCalculationMode);
            }
        };
        moraleMode = new UILabel(get("custombalance.moralemode"), 14, commons.text());
        setTooltipText(moraleModeSpinner, get("custombalance.moralemode." + settings.buildingMoraleCalculationMode + ".desc"));
    }

    /** Perform a partial repaint. */
    void doRepaint() {
        scaleRepaint(base, base, margin());
    }
    @Override
    public void onEnter(Screens mode) {
    }
    /**
     * Update the settings controls.
     * @param settings the new settings
     */
    public void setCustomBalance(CustomBalanceSettings settings) {
        this.settings.copyFrom(settings);
    }

    void doReset() {
        this.settings.clear();
        doRepaint();
    }

    @Override
    public void onLeave() {
        // no cleanup needed
    }

    @Override
    public void onFinish() {
        // no cleanup needed
    }

    @Override
    public void onEndGame() {
        // not an ingame screen
    }
    @Override
    public void draw(Graphics2D g2) {
        RenderTools.darkenAround(base, width, height, g2, 0.5f, true);

        g2.setColor(Color.BLACK);
        g2.fill(base);
        g2.setColor(Color.GRAY);
        g2.draw(base);

        g2.drawLine(base.x, ok.y - 5, base.x + base.width - 1, ok.y - 5);
        g2.drawLine(base.x, ok.y + ok.height + 5, base.x + base.width - 1, ok.y + ok.height + 5);

        super.draw(g2);
    }
    @Override
    public boolean keyboard(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            e.consume();
            cancel.onClick.invoke();
            return true;
        }
        return false;
    }
}
