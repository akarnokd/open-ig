/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a state in the conversation graph with the
 * list of available topics, including a static
 * picture to show when the state is reached.
 * @author akarnokd, 2009.10.10.
 */
public class TalkState {
	/** The start state. */
	public static final String START = "start";
	/** The end state. */
	public static final String END = "end";
	/** The state id. */
	public String id;
	/** The resource name of the picture. */
	public String pictureName;
	/** The available talk speeches. */
	public final List<TalkSpeech> speeches = new ArrayList<>();
}
