/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import hu.openig.ui.IGButton;
import hu.openig.ui.IGCheckBox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Display a message and allow the user to suppress further messages.
 * @author akarnokd, 2012.09.20.
 */
public class MessageDialog extends JDialog {
    /** */
    private static final long serialVersionUID = -3342913595447158669L;
    /** Message. */
    JLabel messageLabel;
    /** Do not show again. */
    IGCheckBox once;
    /** The OK button. */
    IGButton ok;
    /**
     * Construct the dialog.
     * @param parent the parent frame
     * @param lbl the labels
     * @param styles the styles
     */
    public MessageDialog(JFrame parent, LauncherLabels lbl, LauncherStyles styles) {
        super(parent, lbl.label("Java 6 detected"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        messageLabel = new JLabel(lbl.label("Java 6 detected, upgrade needed."));
        messageLabel.setFont(styles.fontMedium());
        messageLabel.setForeground(styles.foreground());
        once = new IGCheckBox(lbl.label("Do not remind me."), styles.fontMedium());
        once.setForeground(styles.foreground());

        ok = new IGButton(lbl.label("OK"));
        ok.setFont(styles.fontMedium());
        ok.setForeground(styles.foreground());
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel p = new JPanel();
        p.setBackground(styles.backgroundColor());

        GroupLayout gl = new GroupLayout(p);
        p.setLayout(gl);
        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);

        gl.setHorizontalGroup(
            gl.createParallelGroup(Alignment.CENTER)
            .addGroup(
                gl.createParallelGroup()
                .addComponent(messageLabel, 450, 450, Short.MAX_VALUE)
                .addComponent(once)
            )
            .addComponent(ok)
        );
        gl.setVerticalGroup(
            gl.createSequentialGroup()
            .addComponent(messageLabel)
            .addGap(20)
            .addComponent(once)
            .addComponent(ok)
        );

        getContentPane().add(p);

        setModal(true);
        pack();
    }
    /**
     * Display the frame.
     * @param parent the parent frame
     */
    public void display(JFrame parent) {
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
    /**
     * @return is once selected?
     */
    public boolean isOnce() {
        return once.isSelected();
    }
}
