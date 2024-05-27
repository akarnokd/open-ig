/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Contains the diplomatic relation status between two players.

 * The status is defined pairwise between players (unordered).
 * @author akarnokd, 2012.03.19.
 */
public class DiplomaticRelation {
    /** The first player ID who made the contact. */
    public Player first;
    /** The second player ID who made the contact. */
    public Player second;
    /** Indicate that the secondary side has also discovered the first. */
    public boolean full;
    /** The relation value, 0..100. */
    public double value;
    /** When was the last contact. */
    public Date lastContact;
    /** Indicator that the parties won't talk for longer periods of time. */
    private boolean wontTalk;
    /** The parties have trade agreement. */
    public boolean tradeAgreement;
    /** The alliance can't be broken and parties can't attack each other. */
    public boolean strongAlliance;
    /**
     * The set of common enemies.
     */
    public final Set<String> alliancesAgainst = new HashSet<>();
    /** @return a copy of this record. */
    public DiplomaticRelation copy() {
        DiplomaticRelation dr = new DiplomaticRelation();
        dr.assign(this);
        return dr;
    }
    /**
     * Update the diplomatic relations value with the given amount.
     * Apply any changes that might result from the new relations value.
     * @param drValue the amount to current value to
     */
    public void updateDrValue(double drValue) {
        double oldValue = this.value;
        this.value = Math.min(100, Math.max(0, drValue));
        // Relations improved
        if (oldValue < this.value) {
            int endHostilitiesThreshold = Math.max(first.endHostilitiesThreshold, second.endHostilitiesThreshold);
            if (this.value > endHostilitiesThreshold) {
                if (first != first.world.player) {
                    for (Fleet fleet : first.ownFleets()) {
                        if ((fleet.mode == FleetMode.ATTACK || fleet.task == FleetTask.ATTACK)
                                && (second.ownPlanets().contains(fleet.targetPlanet()) || second.ownFleets().contains(fleet.targetFleet))) {
                            fleet.stop();
                        }
                    }
                }
                if (second != first.world.player) {
                    for (Fleet fleet : second.ownFleets()) {
                        if ((fleet.mode == FleetMode.ATTACK || fleet.task == FleetTask.ATTACK)
                                && (first.ownPlanets().contains(fleet.targetPlanet()) || first.ownFleets().contains(fleet.targetFleet))) {
                            fleet.stop();
                        }
                    }
                }
            }
        }
    }
    /**
     * Assign the values from the other diplomatic relation.
     * @param other the other relation
     */
    public void assign(DiplomaticRelation other) {
        first = other.first;
        second = other.second;
        full = other.full;
        value = other.value;
        lastContact = other.lastContact;
        wontTalk = other.wontTalk;
        tradeAgreement = other.tradeAgreement;
        strongAlliance = other.strongAlliance;
        alliancesAgainst.addAll(other.alliancesAgainst);
    }
    /**
     * Check if the given player knows about this relation.
     * @param playerId the player identifier
     * @return true if the player is the establisher of this relation
     * or the relation is full.
     */
    public boolean knows(String playerId) {
        return first.id.equals(playerId) || (second.id.equals(playerId) && full);
    }
    /**
     * Set the new won't talk value.
     * @param value the value
     */
    public void wontTalk(boolean value) {
        this.wontTalk = value;
    }
    /**
     * @return the current won't talk status
     */
    public boolean wontTalk() {
        return wontTalk;
    }
}
