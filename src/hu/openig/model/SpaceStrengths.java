/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.model;

/**
 * The space strength record.
 * @author akarnokd, 2012.04.07.
 */
public class SpaceStrengths {
    /** Attacker properties. */
    public AttackDefense attacker;
    /** Defender properties. */
    public AttackDefense defender;
    /** The other fleet. */
    public Fleet fleet;
    /** The planet. */
    public Planet planet;
    /** The planet strength. */
    public AttackDefense planetStrength;
}
