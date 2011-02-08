/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.xold.core;

/**
 * The game player type.
 * @author karnokd, 2009.05.11.
 * @version $Revision 1.0$
 */
public enum PlayerType {
	/** Player is on the local machine. */
	LOCAL_HUMAN,
	/** Player is an AI character. */
	LOCAL_AI,
	/** Player is a remote person. */
	REMOTE_HUMAN,
	/** Player is a remote AI. */
	REMOTE_AI
}
