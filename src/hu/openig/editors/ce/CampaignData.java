/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.model.GameDefinition;
import hu.openig.utils.XElement;

import java.io.File;

/**
 * The container for all campaign related data.
 * @author akarnokd, 2012.12.12.
 */
public class CampaignData {
    /** The campaign's directory. */
    public File directory;
    /** The project's language. */
    public String projectLanguage;
    /** The main definition. */
    public GameDefinition definition;
    /** The label data per language + label file reference. */
    public CampaignLabels labels;
    /** The definition in XML. */
    public XElement def;
    /** Data. */
    public XElement galaxy;
    /** Data. */
    public XElement players;
    /** Data. */
    public XElement planets;
    /** Data. */
    public XElement technology;
    /** Data. */
    public XElement buildings;
    /** Data. */
    public XElement battle;
    /** Data. */
    public XElement diplomacy;
    /** Data. */
    public XElement bridge;
    /** Data. */
    public XElement talks;
    /** Data. */
    public XElement walks;
    /** Data. */
    public XElement chats;
    /** Data. */
    public XElement test;
    /** Data. */
    public XElement spies;
    /** Data. */
    public XElement scripting;
}
