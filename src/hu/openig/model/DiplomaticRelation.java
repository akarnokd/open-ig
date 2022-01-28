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
    public String first;
    /** The second player ID who made the contact. */
    public String second;
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
        return first.equals(playerId) || (second.equals(playerId) && full);
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
