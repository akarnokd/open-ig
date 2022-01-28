/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
/* JOrbis
 * Copyright (C) 2000 ymnk, JCraft,Inc.
 *

 * Written by: 2000 ymnk<ymnk@jcraft.com>
 *

 * Many thanks to

 *   Monty <monty@xiph.org> and

 *   The XIPHOPHORUS Company http://www.xiph.org/ .
 * JOrbis has been based on their awesome works, Vorbis codec.
 *

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package com.jcraft.jorbis;

import com.jcraft.jogg.Buffer;
import com.jcraft.jogg.Packet;
/**
 * The comments are not part of vorbis_info so that vorbis_info can be
 * static storage.
 * Comments and style correction by karnokd.
 * @author ymnk
 *
 */
public class Comment {
    /** The bytes of "vorbis". */
    private static final byte[] VORBIS_BYTES = "vorbis".getBytes();
    /** The bytes of "Xiphophorus libVorbis I 20000508" .*/
    private static final byte[] VENDOR_BYTES = "Xiphophorus libVorbis I 20000508"
            .getBytes();
    /** OV E implementation. */
    private static final int OV_EIMPL = -130;

    /** Unlimited user comment fields. */
    public byte[][] userComments;
    /** User comment lengths. */
    public int[] commentLengths;
    /** Number of comments. */
    public int comments;
    /** Vendor bytes. */
    public byte[] vendor;
    /** Initializer. */
    public void init() {
        userComments = null;
        comments = 0;
        vendor = null;
    }
    /**

     * Add Comment.
     * @param comment the comment text

     */
    public void add(String comment) {
        add(comment.getBytes());
    }
    /**
     * Add comment bytes.
     * @param comment the comment bytes
     */
    private void add(byte[] comment) {
        byte[][] foo = new byte[comments + 2][];
        if (userComments != null) {
            System.arraycopy(userComments, 0, foo, 0, comments);
        }
        userComments = foo;

        int[] goo = new int[comments + 2];
        if (commentLengths != null) {
            System.arraycopy(commentLengths, 0, goo, 0, comments);
        }
        commentLengths = goo;

        byte[] bar = new byte[comment.length + 1];
        System.arraycopy(comment, 0, bar, 0, comment.length);
        userComments[comments] = bar;
        commentLengths[comments] = comment.length;
        comments++;
        userComments[comments] = null;
    }
    /**
     * Adds a tag.
     * @param tag the tag
     * @param contents the contents
     */
    public void addTag(String tag, String contents) {
        if (contents == null) {
            contents = "";
        }
        add(tag + "=" + contents);
    }
    /**
     * Compares two tags.
     * @param s1 the first tag bytes
     * @param s2 the second tag bytes
     * @param n the length to compare
     * @return true if they are equal
     */
    static boolean tagcompare(byte[] s1, byte[] s2, int n) {
        int c = 0;
        byte u1, u2;
        while (c < n) {
            u1 = s1[c];
            u2 = s2[c];
            if ('Z' >= u1 && u1 >= 'A') {
                u1 = (byte) (u1 - 'A' + 'a');
            }
            if ('Z' >= u2 && u2 >= 'A') {
                u2 = (byte) (u2 - 'A' + 'a');
            }
            if (u1 != u2) {
                return false;
            }
            c++;
        }
        return true;
    }
    /**
     * Query for a tag value.
     * @param tag the tag
     * @return the tag value
     */
    public String query(String tag) {
        return query(tag, 0);
    }
    /**
     * Query for a count-th tag value.
     * @param tag the tag to query
     * @param count the count-th tag to return
     * @return the tag value or null if not present
     */
    public String query(String tag, int count) {
        int foo = query(tag.getBytes(), count);
        if (foo == -1) {
            return null;
        }
        byte[] comment = userComments[foo];
        for (int i = 0; i < commentLengths[foo]; i++) {
            if (comment[i] == '=') {
                return new String(comment, i + 1, commentLengths[foo]
                        - (i + 1));
            }
        }
        return null;
    }
    /**
     * Query for the count-th tag value.
     * @param tag the tag bytes
     * @param count the count-th element to return
     * @return the tag value or null if not present
     */
    private int query(byte[] tag, int count) {
        int i = 0;
        int found = 0;
        int fulltaglen = tag.length + 1;
        byte[] fulltag = new byte[fulltaglen];
        System.arraycopy(tag, 0, fulltag, 0, tag.length);
        fulltag[tag.length] = (byte) '=';

        for (i = 0; i < comments; i++) {
            if (tagcompare(userComments[i], fulltag, fulltaglen)) {
                if (count == found) {
                    // We return a pointer to the data, not a copy
                    // return user_comments[i] + taglen + 1;
                    return i;
                }
                found++;
            }
        }
        return -1;
    }
    /**
     * Unpack comments.
     * @param opb the buffer
     * @return success
     */
    int unpack(Buffer opb) {
        int vendorlen = opb.read(32);
        if (vendorlen < 0) {
            clear();
            return (-1);
        }
        vendor = new byte[vendorlen + 1];
        opb.read(vendor, vendorlen);
        comments = opb.read(32);
        if (comments < 0) {
            clear();
            return (-1);
        }
        userComments = new byte[comments + 1][];
        commentLengths = new int[comments + 1];

        for (int i = 0; i < comments; i++) {
            int len = opb.read(32);
            if (len < 0) {
                clear();
                return (-1);
            }
            commentLengths[i] = len;
            userComments[i] = new byte[len + 1];
            opb.read(userComments[i], len);
        }
        if (opb.read(1) != 1) {
            clear();
            return (-1);

        }
        return (0);
    }
    /**
     * Pack comments.
     * @param opb the buffer
     * @return success
     */
    int pack(Buffer opb) {
        // preamble
        opb.write(0x03, 8);
        opb.write(VORBIS_BYTES);

        // vendor
        opb.write(VENDOR_BYTES.length, 32);
        opb.write(VENDOR_BYTES);

        // comments
        opb.write(comments, 32);
        if (comments != 0) {
            for (int i = 0; i < comments; i++) {
                if (userComments[i] != null) {
                    opb.write(commentLengths[i], 32);
                    opb.write(userComments[i]);
                } else {
                    opb.write(0, 32);
                }
            }
        }
        opb.write(1, 1);
        return (0);
    }
    /**
     * Write header out.
     * @param op the packet
     * @return success
     */
    public int headerOut(Packet op) {
        Buffer opb = new Buffer();
        opb.writeinit();

        if (pack(opb) != 0) {
            return OV_EIMPL;
        }

        op.packetBase = new byte[opb.bytes()];
        op.packet = 0;
        op.bytes = opb.bytes();
        System.arraycopy(opb.buffer(), 0, op.packetBase, 0, op.bytes);
        op.beginOfStream = 0;
        op.endOfStream = 0;
        op.granulepos = 0;
        return 0;
    }
    /** Clear comments and other . */
    void clear() {
// unnecessary to null it out
//        for (int i = 0; i < comments; i++) {
//            userComments[i] = null;
//        }
        userComments = null;
        vendor = null;
    }
    /**

     * Returns the vendor.
     * @return the vendor
     */
    public String getVendor() {
        return new String(vendor, 0, vendor.length - 1);
    }
    /**
     * Retuns the ith comment.
     * @param i the comment index to return
     * @return the comment value or null if index is out of range
     */
    public String getComment(int i) {
        if (i < 0 || comments <= i) {
            return null;
        }
        return new String(userComments[i], 0, userComments[i].length - 1);
    }
    /**
     * Returns a convinient string.
     * @return the string
     */
    @Override
    public String toString() {
        StringBuilder foo = new StringBuilder();
        foo.append("Vendor: ");
        foo.append(new String(vendor, 0, vendor.length - 1));
        for (int i = 0; i < comments; i++) {
            foo.append("\nComment: ")
            .append(new String(userComments[i], 0,
                            userComments[i].length - 1));
        }
        foo.append("\n");
        return foo.toString();
    }
}
