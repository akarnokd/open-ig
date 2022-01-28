/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Action1;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * The technology labels panel.
 * @author akarnokd, 2012.11.03.
 */
public class CETechnologyLabelsPanel extends CESlavePanel {
    /** */
    private static final long serialVersionUID = 8451098727939504438L;
    /** Label. */
    CEValueBox<JTextField> nameField;
    /** Label. */
    CEValueBox<JTextArea> nameLabel;
    /** Label. */
    CEValueBox<JTextField> longNameField;
    /** Label. */
    CEValueBox<JTextArea> longNameLabel;
    /** Label. */
    CEValueBox<JTextField> descField;
    /** Label. */
    CEValueBox<JTextArea> descLabel;
    /**
     * Constructor. Initializes the GUI.
     * @param context the context
     */
    public CETechnologyLabelsPanel(CEContext context) {
        super(context);
        initGUI();
    }
    /** Initializes the GUI. */
    private void initGUI() {
        GroupLayout gl = new GroupLayout(this);
        setLayout(gl);
        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);

        nameField = CEValueBox.of(get("tech.name"), new JTextField());
        longNameField = CEValueBox.of(get("tech.longname"), new JTextField());
        descField = CEValueBox.of(get("tech.desc"), new JTextField());

        nameLabel = CEValueBox.of("", new JTextArea());
        longNameLabel = CEValueBox.of("", new JTextArea());
        descLabel = CEValueBox.of("", new JTextArea());

        addValidator(nameField, new Action1<Object>() {
            @Override
            public void invoke(Object value) {
                if (master != null) {
                    validateLabelRef("name", nameField, nameLabel);
                    validateLabels();
                }
            }
        });
        addValidator(longNameField, new Action1<Object>() {
            @Override
            public void invoke(Object value) {
                if (master != null) {
                    validateLabelRef("long-name", longNameField, longNameLabel);
                    validateLabels();
                }
            }
        });
        addValidator(descField, new Action1<Object>() {
            @Override
            public void invoke(Object value) {
                if (master != null) {
                    validateLabelRef("description", descField, descLabel);
                    validateLabels();
                }
            }
        });

        addValidator(nameLabel, new Action1<Object>() {
            @Override
            public void invoke(Object value) {
                context.dataManager().setLabel(nameField.component.getText(), nameLabel.component.getText());
            }
        });
        addValidator(longNameLabel, new Action1<Object>() {
            @Override
            public void invoke(Object value) {
                context.dataManager().setLabel(longNameField.component.getText(), longNameLabel.component.getText());
            }
        });
        addValidator(descLabel, new Action1<Object>() {
            @Override
            public void invoke(Object value) {
                context.dataManager().setLabel(descField.component.getText(), descLabel.component.getText());
            }
        });

        // --------------------------------------------------------

        gl.setHorizontalGroup(
            gl.createParallelGroup()
            .addComponent(nameField)
            .addComponent(nameLabel)
            .addComponent(longNameField)
            .addComponent(longNameLabel)
            .addComponent(descField)
            .addComponent(descLabel)
        );

        gl.setVerticalGroup(
            gl.createSequentialGroup()
            .addComponent(nameField)
            .addComponent(nameLabel)
            .addComponent(longNameField)
            .addComponent(longNameLabel)
            .addComponent(descField)
            .addComponent(descLabel)
        );
    }
    /**
     * Validate the labels.
     */
    void validateLabels() {
        ImageIcon i = nameField.getInvalid();

        i = max(i, longNameField.getInvalid());
        i = max(i, descField.getInvalid());

        onValidate(i);
    }
    @Override
    public void onMasterChanged() {
        if (master != null) {
            setTextAndEnabled(nameField, master, "name", true);
            setTextAndEnabled(longNameField, master, "long-name", true);
            setTextAndEnabled(descField, master, "description", true);
        } else {
            setTextAndEnabled(nameField, null, "", false);
            setTextAndEnabled(longNameField, null, "", false);
            setTextAndEnabled(descField, null, "", false);
        }
    }
}
