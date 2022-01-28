/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.model.Configuration;
import hu.openig.model.Labels;
import hu.openig.ui.IGButton;
import hu.openig.ui.IGCheckBox;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

/**
 * The other settings dialog.
 * @author akarnokd, 2011.12.30.
 */
public class OtherSettingsDialog extends JDialog {
    /** */
    private static final long serialVersionUID = 5092881498785688796L;
    /** Labels. */
    final Labels labels;
    /** Configuration. */
    final Configuration config;
    /** Settings. */
    IGCheckBox disableD3D;
    /** Settings. */
    IGCheckBox disableDDraw;
    /** Settings. */
    IGCheckBox disableOpenGL;
    /** Settings. */
    IGCheckBox tileCacheEnabled;
    /** Settings. */
    IGCheckBox tileCacheBaseEnabled;
    /** Settings. */
    IGCheckBox tileCacheBuildingEnabled;
    /** Settings. */
    JSpinner tc;
    /** Settings. */
    JSpinner tcBase;
    /** Settings. */
    JSpinner tcBuildings;
    /** Settings. */
    JRadioButton radioBaseAtleast;
    /** Settings. */
    JRadioButton radioBaseAtmost;
    /** Settings. */
    JRadioButton radioBuildingsAtleast;
    /** Settings. */
    JRadioButton radioBuildingAtmost;
    /**
     * Creates the dialog.

     * @param owner the owner of the frame
     * @param labels the labels
     * @param config the configuration
     * @param background the background image to use
     */
    public OtherSettingsDialog(
            JFrame owner,

            Labels labels,

            Configuration config,
            final BufferedImage background) {
        super(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.labels = labels;
        this.config = config;
        setModal(true);
        setTitle(labels.get("othersettings.title"));

        Font f = getFont().deriveFont(16f).deriveFont(Font.BOLD);

        IGButton ok = new IGButton();
        ok.setText(labels.get("othersettings.ok"));
        ok.setFont(f);
        ok.setForeground(Color.WHITE);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doOk();
            }
        });

        IGButton cancel = new IGButton();
        cancel.setText(labels.get("othersettings.cancel"));
        cancel.setFont(f);
        cancel.setForeground(Color.WHITE);
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                doCancel();
            }
        });

        JPanel graphicsPanel = createGraphicsPanel(f);

        JPanel performancePanel = createPerformancePanel(f);
        graphicsPanel.setBackground(new Color(0x8395D3).brighter());
        performancePanel.setBackground(graphicsPanel.getBackground());

        Container cp = getContentPane();

        JPanel c = new JPanel() {
            /** */
            private static final long serialVersionUID = -4015518204196812646L;

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (background != null) {
                    g.drawImage(background, 0, 0, null);
                }
            }
        };
        c.setBackground(Color.BLACK);
        cp.add(c);

        GroupLayout gl = new GroupLayout(c);
        c.setLayout(gl);

        gl.setHorizontalGroup(
            gl.createSequentialGroup()
            .addGap(10)
            .addGroup(
                gl.createParallelGroup(Alignment.CENTER)
                .addComponent(graphicsPanel)
                .addComponent(performancePanel)
                .addGroup(
                    gl.createSequentialGroup()
                    .addComponent(ok)
                    .addGap(20)
                    .addComponent(cancel)
                )
            )
            .addGap(10)
        );

        gl.setVerticalGroup(
            gl.createSequentialGroup()
            .addGap(10)
            .addComponent(graphicsPanel)
            .addGap(10)
            .addComponent(performancePanel)
            .addGap(20)
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(ok)
                .addComponent(cancel)
            )
            .addGap(5)

        );

        prepare();

        pack();
        setResizable(false);
    }
    /**
     * Create the graphics panel.
     * @param font the font
     * @return the panel
     */
    JPanel createGraphicsPanel(Font font) {
        JPanel p = new JPanel();
        TitledBorder tb = BorderFactory.createTitledBorder(labels.get("othersettings.graphics_troubleshoot"));
        tb.setTitleFont(font);
        p.setBorder(tb);

        disableD3D = new IGCheckBox(labels.get("othersettings.disable_d3d"), font);
        disableDDraw = new IGCheckBox(labels.get("othersettings.disable_ddraw"), font);
        disableOpenGL = new IGCheckBox(labels.get("othersettings.disable_opengl"), font);
        JLabel restartProgram = new JLabel(labels.get("othersettings.restart_program"));
        restartProgram.setFont(font);

        GroupLayout gl = new GroupLayout(p);
        p.setLayout(gl);
        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);

        gl.setHorizontalGroup(
            gl.createParallelGroup()
            .addComponent(disableD3D)
            .addComponent(disableDDraw)
            .addComponent(disableOpenGL)
            .addComponent(restartProgram)
        );
        gl.setVerticalGroup(
            gl.createSequentialGroup()
            .addComponent(disableD3D)
            .addComponent(disableDDraw)
            .addComponent(disableOpenGL)
            .addComponent(restartProgram)
        );

        return p;
    }
    /**
     * Create the performance panel.
     * @param font the font
     * @return the panel
     */
    JPanel createPerformancePanel(Font font) {
        JPanel p = new JPanel();
        TitledBorder tb = BorderFactory.createTitledBorder(labels.get("othersettings.performance"));
        tb.setTitleFont(font);
        p.setBorder(tb);

        ActionListener enabler = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                enableControls();
            }
        };

        tileCacheEnabled = new IGCheckBox(labels.get("othersettings.tile_cache_enabled"), font);
        tileCacheEnabled.addActionListener(enabler);
        tileCacheBaseEnabled = new IGCheckBox(labels.get("othersettings.tile_cache_base_enabled"), font);
        tileCacheBaseEnabled.addActionListener(enabler);
        tileCacheBuildingEnabled = new IGCheckBox(labels.get("othersettings.tile_cache_building_enabled"), font);
        tileCacheBuildingEnabled.addActionListener(enabler);

        tc = new JSpinner(new SpinnerNumberModel(1, 1, 128, 1));
        tc.setFont(font);

        tcBase = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1));
        tcBase.setFont(font);
        tcBuildings = new JSpinner(new SpinnerNumberModel(1, 1, 15, 1));
        tcBuildings.setFont(font);

        radioBaseAtleast = new JRadioButton(labels.get("othersettings.tile_cache_atleast"));
        radioBaseAtleast.setFont(font);
        radioBaseAtleast.setOpaque(false);
        radioBaseAtmost = new JRadioButton(labels.get("othersettings.tile_cache_atmost"));
        radioBaseAtmost.setFont(font);
        radioBaseAtmost.setOpaque(false);

        radioBuildingsAtleast = new JRadioButton(labels.get("othersettings.tile_cache_atleast"));
        radioBuildingsAtleast.setFont(font);
        radioBuildingsAtleast.setOpaque(false);
        radioBuildingAtmost = new JRadioButton(labels.get("othersettings.tile_cache_atmost"));
        radioBuildingAtmost.setFont(font);
        radioBuildingAtmost.setOpaque(false);

        ButtonGroup bg1 = new ButtonGroup();
        bg1.add(radioBaseAtleast);
        bg1.add(radioBaseAtmost);

        ButtonGroup bg2 = new ButtonGroup();
        bg2.add(radioBuildingsAtleast);
        bg2.add(radioBuildingAtmost);

        GroupLayout gl = new GroupLayout(p);
        p.setLayout(gl);
        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(
            gl.createParallelGroup()
            .addGroup(
                gl.createSequentialGroup()
                .addComponent(tileCacheEnabled)
                .addGap(30)
                .addComponent(tc, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            )
            .addComponent(tileCacheBaseEnabled)
            .addComponent(tileCacheBuildingEnabled)
            .addGroup(
                gl.createSequentialGroup()
                .addGap(30)
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(radioBaseAtleast)
                    .addComponent(radioBaseAtmost)
                )
                .addComponent(tcBase, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            )
            .addGroup(
                gl.createSequentialGroup()
                .addGap(30)
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(radioBuildingsAtleast)
                    .addComponent(radioBuildingAtmost)
                )
                .addComponent(tcBuildings, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            )
        );

        gl.setVerticalGroup(
            gl.createSequentialGroup()
            .addGroup(
                gl.createParallelGroup(Alignment.BASELINE)
                .addComponent(tileCacheEnabled)
                .addComponent(tc, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            )
            .addComponent(tileCacheBaseEnabled)
            .addGroup(
                gl.createParallelGroup(Alignment.CENTER)
                .addGroup(
                    gl.createSequentialGroup()
                    .addComponent(radioBaseAtleast)
                    .addComponent(radioBaseAtmost)
                )
                .addComponent(tcBase, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            )
            .addComponent(tileCacheBuildingEnabled)
            .addGroup(
                gl.createParallelGroup(Alignment.CENTER)
                .addGroup(
                    gl.createSequentialGroup()
                    .addComponent(radioBuildingsAtleast)
                    .addComponent(radioBuildingAtmost)
                )
                .addComponent(tcBuildings, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            )
        );

        return p;
    }
    /** Load current configuration values. */
    void prepare() {
        disableD3D.setSelected(config.disableD3D);
        disableDDraw.setSelected(config.disableDirectDraw);
        disableOpenGL.setSelected(config.disableOpenGL);

        tileCacheEnabled.setSelected(config.tileCacheSize > 0);
        tileCacheBaseEnabled.setSelected(config.tileCacheBaseLimit != 0);
        tileCacheBuildingEnabled.setSelected(config.tileCacheBuildingLimit != 0);
        tc.setValue(config.tileCacheSize == 0 ? 32 : config.tileCacheSize);
        tcBase.setValue(Math.max(Math.abs(config.tileCacheBaseLimit), 1));
        tcBuildings.setValue(Math.abs(config.tileCacheBuildingLimit));
        radioBaseAtleast.setSelected(config.tileCacheBaseLimit > 0);
        radioBaseAtmost.setSelected(config.tileCacheBaseLimit <= 0);
        radioBuildingsAtleast.setSelected(config.tileCacheBuildingLimit > 0);
        radioBuildingAtmost.setSelected(config.tileCacheBuildingLimit <= 0);
        enableControls();
    }
    /** Enable controls based on checkbox settings. */
    void enableControls() {
        boolean v = tileCacheEnabled.isSelected();

        tileCacheBaseEnabled.setEnabled(v);
        tc.setEnabled(v);
        tcBase.setEnabled(v && tileCacheBaseEnabled.isEnabled());
        radioBaseAtleast.setEnabled(v && tileCacheBaseEnabled.isEnabled());
        radioBaseAtmost.setEnabled(v && tileCacheBaseEnabled.isEnabled());

        tileCacheBuildingEnabled.setEnabled(v);
        radioBuildingsAtleast.setEnabled(v && tileCacheBuildingEnabled.isEnabled());
        radioBuildingAtmost.setEnabled(v && tileCacheBuildingEnabled.isEnabled());
        tcBuildings.setEnabled(v && tileCacheBuildingEnabled.isEnabled());
    }
    /** Apply changes. */
    void doOk() {
        if (tileCacheEnabled.isSelected()) {
            config.tileCacheSize = (Integer)tc.getValue();
            if (tileCacheBaseEnabled.isSelected()) {
                config.tileCacheBaseLimit = (Integer)tcBase.getValue();
                if (radioBaseAtmost.isSelected()) {
                    config.tileCacheBaseLimit *= -1;
                }
            } else {
                config.tileCacheBaseLimit = 0;
            }
            if (tileCacheBuildingEnabled.isSelected()) {
                config.tileCacheBuildingLimit = (Integer)tcBuildings.getValue();
                if (radioBuildingAtmost.isSelected()) {
                    config.tileCacheBuildingLimit *= -1;
                }
            } else {
                config.tileCacheBuildingLimit = 0;
            }
        } else {
            config.tileCacheSize = 0;
        }
        config.save();
        setVisible(false);
    }
    /** Close window. */
    void doCancel() {
        setVisible(false);
    }
}
