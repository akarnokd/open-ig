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
import java.util.*;
import java.util.List;

import hu.openig.core.*;
import hu.openig.model.*;
import hu.openig.render.*;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.*;

/**
 * Screen to specify how many colonists to move in or out and from/to what other planet.
 * @author akarnokd, 2022.01.30.
 */
public class MoveColonistsScreen extends ScreenBase {
    /** The panel base rectangle. */
    final Rectangle base = new Rectangle(0, 0, 500, 410);
    /** The select traits label. */
    UILabel titleLabel;
    /** Perform movement. */
    UIGenericButton move;
    /** Perform movement. */
    UIGenericButton moveAndClose;
    /** Cancel selection. */
    UIGenericButton cancel;
    /** True if the colonists will move in, false if they move out. */
    Screens mode;
    /** Planet selection. */
    UISpinner planetSpinner;
    /** Bonus money amount. */
    UISpinner percentageSpinner;
    /** Label. */
    UILabel planetLabel;
    /** Label. */
    UILabel percentageLabel;
    /** Label. */
    UILabel planetPopulationLabel;
    /** Label. */
    UILabel planetLivingspaceLabel;
    /** Label. */
    UILabel planetWorkerneedsLabel;
    /** Label. */
    UILabel currentPopulationLabel;
    /** Label. */
    UILabel currentLivingspaceLabel;
    /** Label. */
    UILabel currentWorkerneedsLabel;
    /** Label. */
    UILabel toBeMovedLabel;
    /** Label. */
    UILabel movementCostLabel;
    /** The list of planets to chose from. */
    final List<Planet> planetList = new ArrayList<>();
    /** Stores the planet id selected in the spinner. */
    String currentPlanetId;
    /** The current percentage value of the spinner. */
    int percentage;
    @Override
    public void onResize() {
        base.width = commons.common().infoEmptyTop.getWidth();
        base.y = 10;

        RenderTools.centerScreen(base, width, height, true);

        if (mode == Screens.MOVE_COLONISTS_IN) {
            titleLabel.text(get("movecolonists.in.title"));
        } else {
            titleLabel.text(get("movecolonists.out.title"));
        }

        titleLabel.location(base.x + 10, base.y + 10);

        planetLabel.location(base.x + 20, base.y + 50);
        planetSpinner.location(base.x + 30, base.y + 70);
        planetSpinner.width = base.width - 40;
        planetPopulationLabel.location(base.x + 40, base.y + 110);
        planetLivingspaceLabel.location(base.x + 40, base.y + 130);
        planetWorkerneedsLabel.location(base.x + 40, base.y + 150);

        percentageLabel.location(base.x + 20, base.y + 180);
        percentageSpinner.location(base.x + 30, base.y + 200);
        percentageSpinner.width = base.width - 40;
        currentPopulationLabel.location(base.x + 40, base.y + 240);
        currentLivingspaceLabel.location(base.x + 40, base.y + 260);
        currentWorkerneedsLabel.location(base.x + 40, base.y + 280);
        toBeMovedLabel.location(base.x + 40, base.y + 300);
        movementCostLabel.location(base.x + 20, base.y + 330);

        int w = move.width + moveAndClose.width + cancel.width + 40;
        int center = base.x + (base.width - w) / 2;
        move.location(center, base.y + base.height - 40);
        moveAndClose.location(center + 20 + move.width, base.y + base.height - 40);
        cancel.location(center + 40 + move.width + moveAndClose.width, base.y + base.height - 40);
    }
    @Override
    public Screens screen() {
        return mode;
    }

    @Override
    public void onInitialize() {
        titleLabel = new UILabel(get("movecolonists.in.title"), 20, commons.text());

        move = new UIGenericButton(get("movecolonists.move"), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
        move.disabledPattern(commons.common().disabledPattern);
        move.onClick = new Action0() {
            @Override
            public void invoke() {
                movePopulation();
                doRepaint();
            }
        };
        moveAndClose = new UIGenericButton(get("movecolonists.moveandclose"), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
        moveAndClose.disabledPattern(commons.common().disabledPattern);
        moveAndClose.onClick = new Action0() {
            @Override
            public void invoke() {
                movePopulation();
                hideSecondary();
            }
        };

        cancel = new UIGenericButton(get("movecolonists.cancel"), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
        cancel.onClick = new Action0() {
            @Override
            public void invoke() {
                hideSecondary();
            }
        };

        planetLabel = new UILabel(get("movecolonists.planet"), 14, commons.text());
        percentageLabel = new UILabel(get("movecolonists.percentage"), 14, commons.text());
        planetPopulationLabel = new UILabel("", 14, commons.text());
        planetLivingspaceLabel = new UILabel("", 14, commons.text());
        planetWorkerneedsLabel = new UILabel("", 14, commons.text());
        currentPopulationLabel = new UILabel("", 14, commons.text());
        currentLivingspaceLabel = new UILabel("", 14, commons.text());
        currentWorkerneedsLabel = new UILabel("", 14, commons.text());
        toBeMovedLabel = new UILabel("", 14, commons.text());
        movementCostLabel = new UILabel("", 14, commons.text());

        createPlanetSpinner();
        createPercentageSpinner();

        addThis();
    }

    void generatePlanetList() {
        planetList.clear();

        boolean currentSelectionFound = false;
        for (Planet p : planet().owner.ownPlanets()) {
            if (p != planet()) {
                if (p.race.equals(planet().race)) {
                    if (mode == Screens.MOVE_COLONISTS_IN) {
                        if (p.population() > 0) {
                            planetList.add(p);
                            currentSelectionFound |= p.id.equals(currentPlanetId);
                        }
                    }
                    if (mode == Screens.MOVE_COLONISTS_OUT) {
                        planetList.add(p);
                        currentSelectionFound |= p.id.equals(currentPlanetId);
                    }
                }
            }
        }
        Collections.sort(planetList, Planet.NAME_ORDER);
        if (!currentSelectionFound) {
            if (planetList.isEmpty()) {
                currentPlanetId = null;
            } else {
                currentPlanetId = planetList.get(0).id;
            }
        }
    }

    void updateLabels() {
        Planet p = world().planet(currentPlanetId);
        if (p != null) {
            PlanetStatistics ps = p.getStatistics();
            planetPopulationLabel.text(format("movecolonists.population", (int)p.population()));
            planetLivingspaceLabel.text(format("movecolonists.livingspace", ps.houseAvailable));
            planetWorkerneedsLabel.text(format("movecolonists.workerneeds", ps.workerDemand));
        } else {
            planetPopulationLabel.text("");
            planetLivingspaceLabel.text("");
            planetWorkerneedsLabel.text("");
        }
        double pop = 0;
        if (p != null) {
            if (mode == Screens.MOVE_COLONISTS_IN) {
                pop = p.population();
            } else {
                pop = planet().population();
            }
        }

        p = planet();
        PlanetStatistics ps = p.getStatistics();
        currentPopulationLabel.text(format("movecolonists.population", (int)p.population()));
        currentLivingspaceLabel.text(format("movecolonists.livingspace", ps.houseAvailable));
        currentWorkerneedsLabel.text(format("movecolonists.workerneeds", ps.workerDemand));

        int price = (int)(5 * pop * percentage / 100);
        toBeMovedLabel.text(format("movecolonists.tobemoved", (int)(pop * percentage / 100)));
        movementCostLabel.text(format("movecolonists.cost", price));

        planetPopulationLabel.sizeToContent();
        planetLivingspaceLabel.sizeToContent();
        planetWorkerneedsLabel.sizeToContent();
        currentPopulationLabel.sizeToContent();
        currentLivingspaceLabel.sizeToContent();
        currentWorkerneedsLabel.sizeToContent();
        toBeMovedLabel.sizeToContent();
        movementCostLabel.sizeToContent();

        if (price > planet().owner.money()) {
            movementCostLabel.color(TextRenderer.RED);
            move.enabled(false);
            moveAndClose.enabled(false);
        } else {
            movementCostLabel.color(TextRenderer.GREEN);
            move.enabled(currentPlanetId != null && percentage > 0);
            moveAndClose.enabled(currentPlanetId != null && percentage > 0);
        }
    }

    void movePopulation() {
        Planet p = world().planet(currentPlanetId);
        if (planet().owner == player() && p.owner == player()) {
            int price = 0;
            if (mode == Screens.MOVE_COLONISTS_IN) {
                price = (int)(5 * p.population() * percentage / 100);
            } else {
                price = (int)(5 * planet().population() * percentage / 100);
            }
            if (price <= p.owner.money()) {
                if (mode == Screens.MOVE_COLONISTS_IN) {
                    world().resettleColonists(p, planet(), percentage / 100d);
                } else {
                    world().resettleColonists(planet(), p, percentage / 100d);
                }
                p.owner.addMoney(-price);
                p.owner.statistics.moneySpent.value += price;
                world().statistics.moneySpent.value += price;
            }
        }
    }

    int findCurrentPlanetInList() {
        int i = 0;
        for (Planet p : planetList) {
            if (p.id.equals(currentPlanetId)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    void createPlanetSpinner() {
        final UIImageButton prev = new UIImageButton(commons.common().moveLeft);
        prev.setDisabledPattern(commons.common().disabledPattern);
        prev.setHoldDelay(250);
        final UIImageButton next = new UIImageButton(commons.common().moveRight);
        next.setDisabledPattern(commons.common().disabledPattern);
        next.setHoldDelay(250);

        prev.onClick = new Action0() {
            @Override
            public void invoke() {
                int i = findCurrentPlanetInList();
                if (i > 0) {
                    currentPlanetId = planetList.get(i - 1).id;
                }
                askRepaint();
            }
        };
        next.onClick = new Action0() {
            @Override
            public void invoke() {
                int i = findCurrentPlanetInList();
                if (i >= 0 && i < planetList.size() - 1) {
                    currentPlanetId = planetList.get(i + 1).id;
                }
                askRepaint();
            }
        };

        planetSpinner = new UISpinner(14, prev, next, commons.text());
        planetSpinner.getValue = new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                StringBuilder b = new StringBuilder();
                if (currentPlanetId == null) {
                    b.append(get("movecolonists.noplanet"));
                } else {
                    b.append(world().planet(currentPlanetId).name());
                }
                return b.toString();
            }
        };
    }

    void createPercentageSpinner() {
        final UIImageButton prev = new UIImageButton(commons.common().moveLeft);
        prev.setDisabledPattern(commons.common().disabledPattern);
        prev.setHoldDelay(250);
        final UIImageButton next = new UIImageButton(commons.common().moveRight);
        next.setDisabledPattern(commons.common().disabledPattern);
        next.setHoldDelay(250);

        prev.onClick = new Action0() {
            @Override
            public void invoke() {
                int increment = 1;
                if (prev.lastEvent != null && prev.lastEvent.modifiers.contains(UIMouse.Modifier.SHIFT)) {
                    increment *= 10;
                }
                percentage = Math.min(100, Math.max(0, percentage - increment));
                askRepaint();
            }
        };
        next.onClick = new Action0() {
            @Override
            public void invoke() {
                int increment = 1;
                if (next.lastEvent != null && next.lastEvent.modifiers.contains(UIMouse.Modifier.SHIFT)) {
                    increment *= 10;
                }
                percentage = Math.min(100, Math.max(0, percentage + increment));
                askRepaint();
            }
        };

        percentageSpinner = new UISpinner(14, prev, next, commons.text());
        percentageSpinner.getValue = new Func1<Void, String>() {
            @Override
            public String invoke(Void value) {
                StringBuilder b = new StringBuilder();
                b.append(percentage).append(" %");
                return b.toString();
            }
        };
    }

    /** Perform a partial repaint. */
    void doRepaint() {
        scaleRepaint(base, base, margin());
    }
    @Override
    public void onEnter(Screens mode) {
        this.mode = mode;
        onResize();
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
        if (planet().owner != player()) {
            hideSecondary();
            return;
        }
        generatePlanetList();
        updateLabels();

        RenderTools.darkenAround(base, width, height, g2, 0.5f, true);

        g2.setColor(Color.BLACK);
        g2.fill(base);
        g2.setColor(Color.GRAY);
        g2.draw(base);

        g2.drawLine(base.x, cancel.y - 5, base.x + base.width - 1, cancel.y - 5);
        g2.drawLine(base.x, cancel.y + cancel.height + 5, base.x + base.width - 1, cancel.y + cancel.height + 5);

        super.draw(g2);
    }
    @Override
    public boolean keyboard(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
            e.consume();
            cancel.onClick.invoke();
            return true;
        }
        // prevent switching planet while showing the dialog
        if (e.getKeyCode() == KeyEvent.VK_PLUS) {
            e.consume();
            return false;
        }
        if (e.getKeyCode() == KeyEvent.VK_MINUS) {
            e.consume();
            return false;
        }
        return false;
    }
}
