/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.Date;

/**
 * A spy.
 * @author akarnokd, 2013.10.02.
 */
public class Spy {
    /** The spy's unique identifier. */
    public final int id;
    /** The spy's name. */
    public String name;
    /** The spy's race. */
    public String race;
    /** The owner, null for unhired spies. */
    public Player owner;
    /** The owner of the turned spies. */
    public Player turnedOwner;
    /** Current experience points. */
    public int xp;
    /** The spy's hitpoints. */
    public double hp;
    /** Skill points available to spend. */
    public int availableSkillPoints;
    /** The current assignment, if not null. */
    public SpyAssignment assignment;
    /** The target empire, if not null, depends on assignment. */
    public Player targetEmpire;
    /** The target planet, null means any planet. */
    public Planet targetPlanet;
    /** The target building kind, null means any building. */
    public String buildingKind;
    /** The target ship type / research type, null means any. */
    public ResearchSubCategory research;
    /** The training level 1-5. */
    public int trainingLevel;
    /** When was the spy captured? */
    public Date captured;
    /** Remaining minutes of the assignment. */
    public int remainingTime;
    /** Infiltration skill points. */
    public int infiltration;
    /** Counter-spy skill points. */
    public int counterSpying;
    /** Combat skill points. */
    public int combat;
    /** Loyalty. */
    public int loyalty;
    /**
     * Constructor, sets the id.
     * @param id the id
     */
    public Spy(int id) {
        this.id = id;
    }
}
