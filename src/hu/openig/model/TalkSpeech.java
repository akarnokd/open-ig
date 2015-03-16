/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import java.util.Objects;

/**
 * The individual media and state for a conversation.
 * @author akarnokd, 2009.10.10.
 */
public class TalkSpeech {
	/** The speech ID. */
	public final String id;
	/** The speech was already taken. */
	public boolean spoken;
	/** The speech media. */
	public String media;
	/** The target state. */
	public String to;
	/** The speech option text. */
	public String text;
    /**
     * Constructor, sets the speech identifier.
     * @param id the fleet identifier
     */
    public TalkSpeech(String id) {
        this.id = Objects.requireNonNull(id);
    }
}
