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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * The first-time run dialog.
 * @author akarnokd, 2012.09.20.
 */
public class FirstRunDialog extends JDialog {
    /** */
    private static final long serialVersionUID = 5864369218217854295L;
    /** OK button clicked. */
    public boolean approved;
    /** Field. */
    private JComboBox<String> fullScreen;
    /** Field. */
    private IGCheckBox movie;
    /** Field. */
    private IGCheckBox click;
    /**
     * Construct the dialog.
     * @param parent the parent frame
     * @param lbl the label manager
     * @param styles the styles
     */
    public FirstRunDialog(JFrame parent, LauncherLabels lbl, LauncherStyles styles) {
        super(parent);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JLabel mainLabel = new JLabel();
        mainLabel.setForeground(styles.foreground());
        mainLabel.setFont(styles.fontMedium());

        JLabel fullScreenDesc = new JLabel();
        fullScreenDesc.setForeground(styles.foreground());
        fullScreenDesc.setFont(styles.fontMedium());

        fullScreen = new JComboBox<>();
        fullScreen.setFont(styles.fontMedium());

        JLabel movieDesc = new JLabel();
        movieDesc.setForeground(styles.foreground());
        movieDesc.setFont(styles.fontMedium());
        movie = new IGCheckBox("", styles.fontMedium());
        movie.setForeground(styles.foreground());

        JLabel clickDesc = new JLabel();
        clickDesc.setForeground(styles.foreground());
        clickDesc.setFont(styles.fontMedium());
        click = new IGCheckBox("", styles.fontMedium());
        click.setForeground(styles.foreground());

        IGButton ok = new IGButton();
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                approved = true;
                dispose();
            }
        });
        IGButton cancel = new IGButton();
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        ok.setForeground(styles.foreground());
        ok.setFont(styles.fontMedium());
        cancel.setForeground(styles.foreground());
        cancel.setFont(styles.fontMedium());

        // -----------------------------------------------------------------

        setTitle(lbl.label("First time launch"));
        ok.setText(lbl.label("OK"));
        cancel.setText(lbl.label("Cancel"));

        mainLabel.setText(lbl.label("First-time explanation"));

        fullScreenDesc.setText(lbl.label("Resolution explanation"));

        fullScreen.setModel(new DefaultComboBoxModel<>(new String[] {

                lbl.label("Classic 640 x 480"),
                lbl.label("Maximized"),
                lbl.label("Full screen")
        }));

        movieDesc.setText(lbl.label("Movie explanation"));
        movie.setText(lbl.label("Movie checkbox"));

        clickDesc.setText(lbl.label("Click explanation"));
        click.setText(lbl.label("Click checkbox"));

        // -----------------------------------------------------------------

        JPanel p = new JPanel();
        p.setBackground(styles.backgroundColor());

        GroupLayout gl = new GroupLayout(p);
        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);
        p.setLayout(gl);

        JSeparator sep0 = new JSeparator(JSeparator.HORIZONTAL);
        JSeparator sep3 = new JSeparator(JSeparator.HORIZONTAL);

        gl.setHorizontalGroup(
            gl.createParallelGroup(Alignment.CENTER)
            .addGroup(
                gl.createParallelGroup()
                .addComponent(mainLabel, 450, 450, 450)
                .addComponent(sep0, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                .addComponent(fullScreenDesc, 450, 450, 450)
                .addGroup(
                    gl.createSequentialGroup()
                    .addGap(20)
                    .addComponent(fullScreen)
                )
                .addComponent(movieDesc, 450, 450, 450)
                .addGroup(
                    gl.createSequentialGroup()
                    .addGap(20)
                    .addComponent(movie)
                )
                .addComponent(clickDesc, 450, 450, 450)
                .addGroup(
                    gl.createSequentialGroup()
                    .addGap(20)
                    .addComponent(click)
                )
            )
            .addComponent(sep3, 0, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
            .addGroup(
                gl.createSequentialGroup()
                .addComponent(ok)
                .addComponent(cancel)
            )
        );
        gl.setVerticalGroup(
            gl.createSequentialGroup()
            .addComponent(mainLabel)
            .addComponent(sep0, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(fullScreenDesc)
            .addComponent(fullScreen, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            .addGap(20)
            .addComponent(movieDesc)
            .addComponent(movie)
            .addGap(20)
            .addComponent(clickDesc)
            .addComponent(click)
            .addComponent(sep3, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(ok)
                .addComponent(cancel)
            )
        );

        gl.linkSize(SwingConstants.HORIZONTAL, ok, cancel);

        getContentPane().add(p);
        setModal(true);
    }
    /**
     * Display the dialog.
     * @param parent the parent frame
     * @return true if user clicked on OK.
     */
    public boolean display(final JFrame parent) {
        pack();
        setLocationRelativeTo(parent);
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                pack();
                setLocationRelativeTo(parent);
            }
        });
        setVisible(true);
        return approved;
    }
    /**
     * The screen mode index.
     * @return 1 - maximized, 2 - full screen
     */
    public int screenMode() {
        return fullScreen.getSelectedIndex();
    }
    /** @return true if full window movies enabled. */
    public boolean movieMode() {
        return movie.isSelected();
    }
    /** @return true if click-skip is enabled. */
    public boolean clickMode() {
        return click.isSelected();
    }
}
