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
import java.util.List;
import java.util.ArrayList;

import hu.openig.core.Action0;
import hu.openig.model.*;
import hu.openig.render.*;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.*;

/**
 * Screen to specify how to abandon the current planet.
 * @author akarnokd, 2022.01.30.
 */
public class AbandonColonyScreen extends ScreenBase {
    /** The panel base rectangle. */
    final Rectangle base = new Rectangle(0, 0, 500, 420);
    /** The select traits label. */
    UILabel titleLabel;
    /** Cancel selection. */
    UIGenericButton cancel;
    /** Move colonists. */
    UIGenericButton moveColonists;
    /** Kill colonists. */
    UIGenericButton killColonists;
    /** Label. */
    UIColorLabel moveConsequenceLabel;
    /** Label. */
    UILabel moveCostLabel;
    /** Label. */
    UILabel moveCostTarget;
    /** Label. */
    UIColorLabel killConsequenceLabel;
    /** Label. */
    UILabel killCostLabel;
    @Override
    public void onResize() {
        base.width = commons.common().infoEmptyTop.getWidth();
        base.y = 10;

        RenderTools.centerScreen(base, width, height, true);

        titleLabel.location(base.x + 10, base.y + 10);

        int w = cancel.width;
        cancel.location(base.x + (640 - w) / 2, base.y + base.height - 40);
    }
    @Override
    public Screens screen() {
        return Screens.ABANDON_COLONY;
    }

    @Override
    public void onInitialize() {
        titleLabel = new UILabel(get("abandoncolony.title"), 20, commons.text());

        moveConsequenceLabel = new UIColorLabel(10, commons.text());
        moveCostLabel = new UILabel("", 14, commons.text());
        moveCostTarget = new UILabel("", 14, commons.text());
        killConsequenceLabel = new UIColorLabel(10, commons.text());
        killCostLabel = new UILabel("", 14, commons.text());

        moveColonists = new UIGenericButton(get("abandonplanet.move"), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
        moveColonists.disabledPattern(commons.common().disabledPattern);
        moveColonists.onClick = new Action0() {
            @Override
            public void invoke() {
                if (abandonPlanet(false)) {
                    hideSecondary();
                }
            }
        };
        killColonists = new UIGenericButton(get("abandonplanet.kill"), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
        killColonists.disabledPattern(commons.common().disabledPattern);
        killColonists.onClick = new Action0() {
            @Override
            public void invoke() {
                if (abandonPlanet(true)) {
                    hideSecondary();
                }
            }
        };

        cancel = new UIGenericButton(get("abandoncolony.cancel"), commons.control().fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
        cancel.onClick = new Action0() {
            @Override
            public void invoke() {
                hideSecondary();
            }
        };

        addThis();
    }

    /** Perform a partial repaint. */
    void doRepaint() {
        scaleRepaint(base, base, margin());
    }
    @Override
    public void onEnter(Screens mode) {
        // no action needed
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
    public boolean moveTargetAvailable() {
        if (planet().race.equals(player().race)) {
            return !player().ownPlanets().isEmpty();
        }
        for (Player p : world().players.values()) {
            if (p.race.equals(planet().race)) {
                if (!p.ownPlanets().isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public void draw(Graphics2D g2) {

        if (planet().owner != player()) {
            hideSecondary();
            return;
        }
        moveConsequenceLabel.size(base.width - 40, 50);
        killConsequenceLabel.size(base.width - 40, 50);

        long sp = getSellAmount();
        long costMove = (long)(planet().population() * 5);
        long costKill = (long)(planet().population() * 2);
        long priceMove = Math.max(0, costMove - sp);
        long priceKill = Math.max(0, costKill - sp);

        if (planet().race.equals(planet().owner().race)) {
            moveConsequenceLabel.text(get("abandonplanet.move.own"));
            killConsequenceLabel.text(get("abandonplanet.kill.own"));
        } else {
            moveConsequenceLabel.text(get("abandonplanet.move.alien"));
            killConsequenceLabel.text(get("abandonplanet.kill.alien"));
        }
        moveCostLabel.text(format("abandonplanet.move.cost", priceMove, sp - costMove));
        if (priceMove > player().money()) {
            moveCostLabel.color(TextRenderer.RED);
            moveColonists.enabled(false);
        } else {
            moveCostLabel.color(TextRenderer.GREEN);
            moveColonists.enabled(true);
        }
        if (moveTargetAvailable()) {
            moveCostTarget.text(get("abandonplanet.move.found"));
        } else {
            moveCostTarget.text(get("abandonplanet.move.notfound"));
            moveColonists.enabled(false);
        }
        killCostLabel.text(format("abandonplanet.kill.cost", priceKill, sp - costKill));
        if (priceKill > player().money()) {
            killCostLabel.color(TextRenderer.RED);
            killColonists.enabled(false);
        } else {
            killCostLabel.color(TextRenderer.GREEN);
            killColonists.enabled(true);
        }
        moveCostLabel.sizeToContent();
        killCostLabel.sizeToContent();
        moveCostTarget.sizeToContent();

        moveConsequenceLabel.location(base.x + 20, base.y + 40);
        moveConsequenceLabel.size(base.width - 40, 80);
        moveCostLabel.location(moveConsequenceLabel.x, moveConsequenceLabel.y + moveConsequenceLabel.height + 10);
        moveCostTarget.location(moveCostLabel.x, moveCostLabel.y + 20);
        moveColonists.location(base.x + 20, moveCostTarget.y + 20);
        moveColonists.width = base.width - 40;

        killConsequenceLabel.location(base.x + 20, moveColonists.y + 50);
        killConsequenceLabel.size(base.width - 40, 80);
        killCostLabel.location(killConsequenceLabel.x, killConsequenceLabel.y + killConsequenceLabel.height + 10);
        killColonists.location(base.x + 20, killCostLabel.y + 20);
        killColonists.width = base.width - 40;

        RenderTools.darkenAround(base, width, height, g2, 0.5f, true);

        g2.setColor(Color.BLACK);
        g2.fill(base);
        g2.setColor(Color.GRAY);
        g2.draw(base);

        g2.drawLine(base.x, moveColonists.y + 40, base.x + base.width - 1, moveColonists.y + 40);
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
        return false;
    }

    long getSellAmount() {
        long i = 0;
        for (InventoryItem ii : planet().inventory.findByOwner(player().id)) {
            if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
                i += ii.sellValue();
            }
        }
        for (Building b : planet().surface.buildings.iterable()) {
            i += b.sellPrice();
        }
        return i;
    }

    boolean abandonPlanet(boolean kill) {
        if (planet().owner == player()) {
            long sp = getSellAmount();
            long price;
            if (kill) {
                price = (long)(planet().population() * 2);
            } else {
                price = (long)(planet().population() * 5);
            }
            if (player().money() + sp >= price) {
                if (kill) {
                    if (planet().race.equals(player().race)) {
                        for (Planet p : player().ownPlanets()) {
                            p.morale(p.morale() - 50);
                        }
                        for (DiplomaticRelation dr : world().relations) {
                            if (dr.full && (dr.first.equals(player().id) || dr.second.equals(player().id))) {
                                dr.value = Math.max(0, dr.value - 5);
                            }
                        }
                    } else {
                        for (Planet p : player().ownPlanets()) {
                            p.morale(p.morale() - 20);
                        }
                        for (DiplomaticRelation dr : world().relations) {
                            if (dr.full && (dr.first.equals(player().id) || dr.second.equals(player().id))) {
                                dr.value = Math.max(0, dr.value - 25);
                            }
                        }
                    }
                } else {
                    if (!moveTargetAvailable()) {
                        return false;
                    }
                    if (planet().race.equals(player().race)) {
                        List<Planet> ps = player().ownPlanets();
                        if (ps.size() >= 2) {
                            double remaining = planet().population();
                            double people = 1 + planet().population() / (ps.size() - 1);
                            for (Planet p : ps) {
                                if (p != planet()) {
                                    p.morale(p.morale() - 5);
                                    p.population(p.population() + Math.max(0, Math.min(people, remaining)));
                                    remaining -= people;
                                }
                            }
                        }
                    } else {
                        for (DiplomaticRelation dr : world().relations) {
                            if (dr.first.equals(player().id) || dr.second.equals(player().id)) {
                                dr.value = Math.max(0, dr.value - 5);
                            }
                        }
                        List<Planet> ps = new ArrayList<>();
                        for (Player p : world().players.values()) {
                            if (planet().race.equals(p.race)) {
                                ps.addAll(p.ownPlanets());
                            }
                        }
                        if (!ps.isEmpty()) {
                            double remaining = planet().population();
                            double people = 1 + planet().population() / (ps.size() - 1);
                            for (Planet p : ps) {
                                if (p != planet()) {
                                    p.morale(p.morale() - 5);
                                    p.population(p.population() + Math.max(0, Math.min(people, remaining)));
                                    remaining -= people;
                                }
                            }
                        }
                    }
                }
                List<Building> buildingCopy = new ArrayList<>();
                for (Building b : planet().surface.buildings.iterable()) {
                    buildingCopy.add(b);
                }
                for (Building b : buildingCopy) {
                    planet().demolish(b);
                }

                for (InventoryItem ii : new ArrayList<>(planet().inventory.findByOwner(player().id))) {
                    if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
                        planet().sell(ii, ii.count);
                    } else
                    if (ii.type.category != ResearchSubCategory.SPACESHIPS_SATELLITES) {
                        player().changeInventoryCount(ii.type, ii.count);
                    }
                }

                planet().die();
                player().addMoney(-price);
                player().statistics.moneySpent.value += price;
                world().statistics.moneySpent.value += price;
                return true;
            }
        }
        return false;
    }
}
