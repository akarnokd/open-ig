/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/** The attack/defense record. */
public class AttackDefense {
    /** Attack value. */
    public double attack;
    /** Defense value. */
    public double defense;
    /** Damage of all rockets/bombs which will be applied once. */
    public double onetimeAttack;
    /** The sum levels of the ECM capabilities. */
    public int ecmSum;
    /** The number of ECM capabilities. */
    public int ecmCount;
    /** The sum levels of the Anti-ECM capabilities. */
    public int antiEcmSum;
    /** The number of Anti-ECM capabilities. */
    public int antiEcmCount;
    /** The number of structures. */
    public int structures;
    /**
     * Add another record.
     * @param d the other defense
     */
    public void add(AttackDefense d) {
        this.attack += d.attack;
        this.defense += d.defense;
        this.ecmSum += d.ecmSum;
        this.ecmCount += d.ecmCount;
        this.antiEcmSum += d.antiEcmSum;
        this.antiEcmCount += d.antiEcmCount;
        this.structures += d.structures;
    }
    @Override
    public String toString() {
        return "AttackDefense [attack=" + attack + ", defense=" + defense
                + ", onetimeAttack=" + onetimeAttack + ", ecmSum=" + ecmSum
                + ", ecmCount=" + ecmCount + ", antiEcmSum=" + antiEcmSum
                + ", antiEcmCount=" + antiEcmCount + ", structures="
                + structures + "]";
    }

}
