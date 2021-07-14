/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The taxation level.
 * @author akarnokd, Mar 29, 2011
 */
public enum TaxLevel {
	/** None. */
	NONE,
	/** Very low. */
	VERY_LOW,
	/** Low. */
	LOW,
	/** Moderate. */
	MODERATE,
	/** Above moderate. */
	ABOVE_MODERATE,
	/** High.*/
	HIGH,
	/** Very high. */
	VERY_HIGH,
	/** Oppressive. */
	OPPRESSIVE,
	/** Exploiter. */
	EXPLOITER,
	/** Slavery. */
	SLAVERY
	;
	
        private static final float NO_TAX_MORALE = 5f;
        private static final int MAX_LEVEL = 9;
        private static final int MAX_TAX = 100;
        private static final float MAX_TAX_MORALE_SAME = -100f / 3f;
        private static final float MAX_TAX_MORALE_OTHER = -40f;
        private static final int BASE_LEVEL = 3;
        private static final int BASE_TAX = MAX_TAX * BASE_LEVEL / MAX_LEVEL;
        private static final float BASE_TAX_MORALE_SAME = -5.5f;
        private static final float BASE_TAX_MORALE_OTHER = -8.25f;

        /**
         * The taxation percent.
         */
        public final int percent;
        /**
         * Same race morale change.
         */
        private final float sameMorale;
        /**
         * Different race morale change.
         */
        private final float otherMorale;

        /**
         * Constructor.
         */
        TaxLevel() {
                this.percent = MAX_TAX * ordinal() / MAX_LEVEL;

                if (this.percent == 0) {
                        this.sameMorale = NO_TAX_MORALE;
                        this.otherMorale = NO_TAX_MORALE;
                } else if (this.percent <= BASE_TAX) {
                        this.sameMorale = BASE_TAX_MORALE_SAME * this.percent / BASE_TAX;
                        this.otherMorale = BASE_TAX_MORALE_OTHER * this.percent / BASE_TAX;
                } else {
                        this.sameMorale = BASE_TAX_MORALE_SAME
                                + (MAX_TAX_MORALE_SAME - BASE_TAX_MORALE_SAME)
                                * (this.percent - BASE_TAX) / (MAX_TAX - BASE_TAX);
                        this.otherMorale = BASE_TAX_MORALE_OTHER
                                + (MAX_TAX_MORALE_OTHER - BASE_TAX_MORALE_OTHER)
                                * (this.percent - BASE_TAX) / (MAX_TAX - BASE_TAX);
                }
        }

        /**
         * @param sameRace If planet race is Empire's main race.
         * @return Morale change for this tax level and selected race.
         */
        public float getMoraleChange(boolean sameRace) {
                if (sameRace) {
                        return sameMorale;
                } else {
                        return otherMorale;
                }
        }
}
