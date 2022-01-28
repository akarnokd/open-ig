/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

/** The message description. */
public class VideoMessage {
    /** The identifier. */
    public String id;
    /** The media. */
    public String media;
    /** The title. */
    public String title;
    /** The description. */
    public String description;
    /** Indicate that this message was viewed. */
    public boolean seen;
    /** Display in the message list? */
    public boolean visible;
    /**
     * Create a copy of this message.
     * @return the copy
     */
    public VideoMessage copy() {
        VideoMessage result = new VideoMessage();

        result.id = id;
        result.media = media;
        result.title = title;
        result.description = description;
        result.seen = seen;
        result.visible = visible;

        return result;
    }
}
