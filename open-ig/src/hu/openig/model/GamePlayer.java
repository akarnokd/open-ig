/*
 * Copyright 2008-2009, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/**
 * The players object representing all its knowledge.
 * @author karnokd, 2009.05.11.
 * @version $Revision 1.0$
 */
public class GamePlayer {
	/** The player type. */
	public PlayerType playerType;
	/** The race of the player. */
	public GameRace race;
	/** The player's money amount. */
	public long money;
}
