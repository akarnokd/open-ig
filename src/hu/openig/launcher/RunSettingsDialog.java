/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import hu.openig.ui.IGButton;

import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 * The Run Settings dialog.
 * @author akarnokd, 2012.09.20.
 */
public class RunSettingsDialog extends JDialog {
    /** */
    private static final long serialVersionUID = -4663029481753008982L;
    /** Field. */
    private JPanel panel;
    /** Field. */
    private JTextField jvmField;
    /** Field. */
    private JTextField memField;
    /** Field. */
    private JLabel memLabelNow;
    /** Field. */
    private LauncherLabels labelMgr;
    /** Field. */
    private JTextField jvmParams;
    /** Field. */
    private JTextField appParams;
    /** True if the user quit via the OK button. */
    public boolean approved;
    /**
     * Construct the dialog.
     * @param parent the parent frame
     * @param labelMgr the label manager
     * @param styles the launcher styles
     */
    public RunSettingsDialog(JFrame parent,

            LauncherLabels labelMgr,
            LauncherStyles styles) {
        super(parent, labelMgr.label("Run settings"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.labelMgr = labelMgr;

        Container c = getContentPane();

        panel = new JPanel();
        panel.setBackground(styles.backgroundColor());

        c.add(panel);

        GroupLayout gl = new GroupLayout(panel);
        panel.setLayout(gl);
        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);

        jvmField = new JTextField(30);
        IGButton browse = new IGButton(labelMgr.label("Browse..."));
        browse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String path = System.getProperty("java.home");
                if (!jvmField.getText().isEmpty()) {
                    path = jvmField.getText();
                }
                JFileChooser fc = new JFileChooser(path);
                fc.setMultiSelectionEnabled(false);
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                if (fc.showOpenDialog(RunSettingsDialog.this) == JFileChooser.APPROVE_OPTION) {
                    jvmField.setText(fc.getSelectedFile().getAbsolutePath());
                }

            }
        });
        memField = new JTextField(5);

        JLabel jvmLabel = new JLabel(labelMgr.label("Java runtime home:"));
        JLabel jvmLabelNow = new JLabel(labelMgr.format("Default: %s", System.getProperty("java.home")));
        JLabel jvmVersion = new JLabel(System.getProperty("java.version") + ", " + System.getProperty("os.name") + ", " + System.getProperty("os.arch"));

        JLabel memMb = new JLabel(labelMgr.label("MB"));

        JLabel memLabel = new JLabel(labelMgr.label("Memory:"));
        memLabelNow = new JLabel();

        JLabel jvmParamsLabel = new JLabel(labelMgr.format("JVM parameters:"));
        jvmParams = new JTextField();

        JLabel appParamsLabel = new JLabel(labelMgr.format("Game parameters:"));
        appParams = new JTextField();

        IGButton ok = new IGButton(labelMgr.label("OK"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                approved = true;
                dispose();
            }
        });
        IGButton cancel = new IGButton(labelMgr.label("Cancel"));
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        jvmLabel.setFont(styles.fontMedium());
        jvmLabelNow.setFont(styles.fontMedium());
        jvmField.setFont(styles.fontMedium());

        memLabelNow.setFont(styles.fontMedium());
        memLabel.setFont(styles.fontMedium());
        memField.setFont(styles.fontMedium());
        memMb.setFont(styles.fontMedium());

        jvmLabel.setForeground(styles.foreground());
        jvmLabelNow.setForeground(styles.foreground());
        jvmField.setForeground(Color.BLACK);

        memLabelNow.setForeground(styles.foreground());
        memLabel.setForeground(styles.foreground());
        memField.setForeground(Color.BLACK);
        memMb.setForeground(styles.foreground());

        jvmVersion.setForeground(styles.foreground());
        jvmVersion.setFont(styles.fontMedium());

        jvmParamsLabel.setForeground(styles.foreground());
        jvmParamsLabel.setFont(styles.fontMedium());
        jvmParams.setForeground(Color.BLACK);
        jvmParams.setFont(styles.fontMedium());

        appParamsLabel.setForeground(styles.foreground());
        appParamsLabel.setFont(styles.fontMedium());
        appParams.setForeground(Color.BLACK);
        appParams.setFont(styles.fontMedium());

        ok.setFont(styles.fontMedium());
        cancel.setFont(styles.fontMedium());
        browse.setFont(styles.fontMedium());
        ok.setForeground(styles.foreground());
        cancel.setForeground(styles.foreground());
        browse.setForeground(styles.foreground());

        JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);

        gl.setHorizontalGroup(
            gl.createParallelGroup(Alignment.CENTER)
            .addGroup(
                gl.createParallelGroup(Alignment.LEADING)
                .addGroup(
                    gl.createSequentialGroup()
                    .addGroup(
                        gl.createParallelGroup()
                        .addComponent(jvmLabel)
                        .addComponent(memLabel)
                        .addComponent(jvmParamsLabel)
                        .addComponent(appParamsLabel)
                    )
                    .addGroup(
                        gl.createParallelGroup()
                        .addComponent(jvmField)
                        .addGroup(
                            gl.createSequentialGroup()
                            .addComponent(memField, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(memMb)
                        )
                        .addComponent(jvmParams)
                        .addComponent(appParams)
                    )
                    .addComponent(browse)
                )
                .addGroup(
                    gl.createSequentialGroup()
                    .addGap(30)
                    .addComponent(jvmLabelNow)
                )
                .addGroup(
                    gl.createSequentialGroup()
                    .addGap(30)
                    .addComponent(jvmVersion)
                )
                .addGroup(
                    gl.createSequentialGroup()
                    .addGap(30)
                    .addComponent(memLabelNow)
                )
            )
            .addComponent(sep)
            .addGroup(
                gl.createSequentialGroup()
                .addComponent(ok)
                .addComponent(cancel)
            )
        );

        gl.setVerticalGroup(
            gl.createSequentialGroup()
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(jvmLabel)
                .addComponent(jvmField)
                .addComponent(browse)
            )
            .addComponent(jvmLabelNow)
            .addComponent(jvmVersion)
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(memLabel)
                .addComponent(memField)
                .addComponent(memMb)
            )
            .addComponent(memLabelNow)
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(jvmParamsLabel)
                .addComponent(jvmParams)
            )
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(appParamsLabel)
                .addComponent(appParams)
            )
            .addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(ok)
                .addComponent(cancel)
            )
        );

        gl.linkSize(SwingConstants.HORIZONTAL, ok, cancel);

        setResizable(false);
        pack();
        setModal(true);
    }
    /**
     * Set the JVM path.
     * @param jvm the JVM
     */
    public void setJVM(String jvm) {
        jvmField.setText(jvm);
    }
    /**
     * Retrieve the JVM path.
     * @return the JVM path or null if default
     */
    public String getJVM() {
        String jvm = jvmField.getText();
        if (jvm.isEmpty()) {
            return null;
        }
        return jvm;
    }
    /**
     * Set application memory, null for default.
     * @param memory the memory in MB
     */
    public void setMemory(Integer memory) {
        if (memory != null) {
            memField.setText(memory.toString());
        } else {
            memField.setText("");
        }
    }
    /**
     * Returns the application memory, null for default.
     * @return the memory in MB
     */
    public Integer getMemory() {
        String mem = memField.getText();
        if (mem.isEmpty()) {
            return null;
        }
        return Integer.valueOf(mem);
    }
    /**
     * Set the default application memory.
     * @param value the memory in MB
     */
    public void setDefaultMemory(int value) {
        memLabelNow.setText(labelMgr.format("Default: %s MB", value));
    }
    /**
     * Set the JVM parameters.
     * @param params the parameters
     */
    public void setJVMParams(String params) {
        jvmParams.setText(params);
    }
    /**
     * @return the current jvm parameters
     */
    public String getJVMParams() {
        String s = jvmParams.getText();
        if (s.isEmpty()) {
            return null;
        }
        return s;
    }
    /**
     * Set the application parameters.
     * @param params the application parameters
     */
    public void setAppParams(String params) {
        appParams.setText(params);
    }
    /**
     * @return the current application parameters
     */
    public String getAppParams() {
        String s = appParams.getText();
        if (s.isEmpty()) {
            return null;
        }
        return s;
    }
    /**
     * Display the dialog.
     * @param parent the parent frame
     */
    public void display(JFrame parent) {
        pack();
        setLocationRelativeTo(parent);
        setVisible(true);
    }
}
