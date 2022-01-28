/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.model;

import hu.openig.core.SimulationSpeed;
import hu.openig.utils.XElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Multiplayer definition record.

 * @author akarnokd, 2013.04.25.
 */
public class MultiplayerDefinition extends CustomGameDefinition {
    /** Allow server quicksaving. */
    public boolean allowQuickSave;
    /** Allow server autosave. */
    public boolean allowAutoSave;
    /** Allow server pause. */
    public boolean allowPause;
    /** Allow cheats. */
    public boolean allowCheat;
    /** Base simulation speed. */
    public SimulationSpeed speed;
    /** Base timestep. */
    public int timestep;
    /** The list of multiplayer users. */
    public final List<MultiplayerUser> players = new ArrayList<>();
    /**
     * Save the multiplayer definition.
     * @param xout the output
     */
    public void save(XElement xout) {
        xout.saveFields(this);
        XElement xplayers = xout.add("players");
        for (MultiplayerUser p : players) {
            XElement xplayer = xplayers.add("player");
            xplayer.saveFields(p);
            XElement xtraits = xplayer.add("traits");
            for (String t : p.traits) {
                xtraits.add("trait").set("id", t);
            }
        }
    }
    /**
     * Load the mutliplayer definition.
     * @param xin the input
     */
    public void load(XElement xin) {
        players.clear();
        xin.loadFields(this);
        for (XElement xplayers : xin.childrenWithName("players")) {
            for (XElement xplayer : xplayers.childrenWithName("player")) {
                MultiplayerUser sp = new MultiplayerUser();
                xplayer.loadFields(sp);
                for (XElement xtraits : xplayer.childrenWithName("traits")) {
                    for (XElement xtrait : xtraits.childrenWithName("trait")) {
                        sp.traits.add(xtrait.get("id"));
                    }
                }
                players.add(sp);
            }
        }
    }
}
