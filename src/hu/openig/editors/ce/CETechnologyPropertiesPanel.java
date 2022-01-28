/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.utils.XElement;

import javax.swing.GroupLayout;

/**
 * The properties panel.
 * @author akarnokd, 2012.11.03.
 */
public class CETechnologyPropertiesPanel extends CEBasePanel {
    /** */
    private static final long serialVersionUID = -6719463759393555509L;
    /** The current tech item under editing. */
    XElement selected;
    /**
     * Constructor. Sets the context.
     * @param context the context
     */
    public CETechnologyPropertiesPanel(CEContext context) {
        super(context);
        initGUI();
    }
    /** Initialize the GUI. */
    private void initGUI() {
        GroupLayout gl = new GroupLayout(this);
        this.setLayout(gl);
        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);

    }
    /**
     * Set the current tech item.
     * @param item the item or null to clear the panel.
     */
    public void setTechItem(XElement item) {
        this.selected = item;

    }
}
