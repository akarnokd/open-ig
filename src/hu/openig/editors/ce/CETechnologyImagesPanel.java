/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.model.ResearchSubCategory;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * The technology images.
 * @author akarnokd, 2012.11.03.
 */
public class CETechnologyImagesPanel extends CESlavePanel {
    /** */
    private static final long serialVersionUID = 6356090108602851297L;
    /** Image base. */
    CEValueBox<JTextField> imageField;
    /** Image. */
    CEImageRef imageNormal;
    /** Image. */
    CEImageRef imageInfoAvail;
    /** Image. */
    CEImageRef imageInfoWired;
    /** Image. */
    CEImageRef imageSpacewar;
    /** Image. */
    CEImageRef imageEquipDetails;
    /** Image. */
    CEImageRef imageEquipFleet;
    /**
     * Constructor. Initializes the GUI.
     * @param context the context
     */
    public CETechnologyImagesPanel(CEContext context) {
        super(context);
        initGUI();
    }
    /** Initializes the GUI. */
    private void initGUI() {
        JPanel panel = new JPanel();
        GroupLayout gl = new GroupLayout(panel);
        panel.setLayout(gl);
        gl.setAutoCreateContainerGaps(true);
        gl.setAutoCreateGaps(true);

        imageField = CEValueBox.of(get("tech.image"), new JTextField());

        imageNormal = new CEImageRef(get("tech.image.normal"));
        imageInfoAvail = new CEImageRef(get("tech.image.info_available"));
        imageInfoWired = new CEImageRef(get("tech.image.info_wired"));
        imageSpacewar = new CEImageRef(get("tech.image.spacewar"));
        imageEquipDetails = new CEImageRef(get("tech.image.equipment_details"));
        imageEquipFleet = new CEImageRef(get("tech.image.equipment_fleet"));

        JButton browse = new JButton(get("browse"));
        browse.setVisible(false);

        addValidator(imageField, new Action1<Object>() {
            @Override
            public void invoke(Object value) {
                setImages();
            }
        });

        // -----------------------------------------------

        int imageSize = 75;

        gl.setHorizontalGroup(
            gl.createParallelGroup()
            .addGroup(
                gl.createSequentialGroup()
                .addComponent(imageField)
                .addComponent(browse)
            )
            .addGroup(
                gl.createSequentialGroup()
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(imageNormal.image, imageSize, imageSize, imageSize)
                    .addComponent(imageInfoAvail.image, imageSize, imageSize, imageSize)
                    .addComponent(imageInfoWired.image, imageSize, imageSize, imageSize)
                )
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(imageNormal.valid, 20, 20, 20)
                    .addComponent(imageInfoAvail.valid, 20, 20, 20)
                    .addComponent(imageInfoWired.valid, 20, 20, 20)
                )
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(imageNormal.label)
                    .addComponent(imageInfoAvail.label)
                    .addComponent(imageInfoWired.label)
                )
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(imageNormal.path)
                    .addComponent(imageInfoAvail.path)
                    .addComponent(imageInfoWired.path)
                )
                .addGap(30)
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(imageSpacewar.image, imageSize, imageSize, imageSize)
                    .addComponent(imageEquipDetails.image, imageSize, imageSize, imageSize)
                    .addComponent(imageEquipFleet.image, imageSize, imageSize, imageSize)
                )
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(imageSpacewar.valid, 20, 20, 20)
                    .addComponent(imageEquipDetails.valid, 20, 20, 20)
                    .addComponent(imageEquipFleet.valid, 20, 20, 20)
                )
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(imageSpacewar.label)
                    .addComponent(imageEquipDetails.label)
                    .addComponent(imageEquipFleet.label)
                )
                .addGroup(
                    gl.createParallelGroup()
                    .addComponent(imageSpacewar.path)
                    .addComponent(imageEquipDetails.path)
                    .addComponent(imageEquipFleet.path)
                )
            )
        );

        gl.setVerticalGroup(
            gl.createSequentialGroup()
            .addGroup(
                gl.createParallelGroup(Alignment.CENTER)
                .addComponent(imageField)
                .addComponent(browse)
            )
            .addGroup(
                gl.createParallelGroup(Alignment.CENTER)
                .addComponent(imageNormal.image, imageSize, imageSize, imageSize)
                .addComponent(imageNormal.valid)
                .addComponent(imageNormal.label)
                .addComponent(imageNormal.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(imageSpacewar.image, imageSize, imageSize, imageSize)
                .addComponent(imageSpacewar.valid)
                .addComponent(imageSpacewar.label)
                .addComponent(imageSpacewar.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            )
            .addGroup(
                gl.createParallelGroup(Alignment.CENTER)
                .addComponent(imageInfoAvail.image, imageSize, imageSize, imageSize)
                .addComponent(imageInfoAvail.valid)
                .addComponent(imageInfoAvail.label)
                .addComponent(imageInfoAvail.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(imageEquipDetails.image, imageSize, imageSize, imageSize)
                .addComponent(imageEquipDetails.valid)
                .addComponent(imageEquipDetails.label)
                .addComponent(imageEquipDetails.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            )
            .addGroup(
                gl.createParallelGroup(Alignment.CENTER)
                .addComponent(imageInfoWired.image, imageSize, imageSize, imageSize)
                .addComponent(imageInfoWired.valid)
                .addComponent(imageInfoWired.label)
                .addComponent(imageInfoWired.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addComponent(imageEquipFleet.image, imageSize, imageSize, imageSize)
                .addComponent(imageEquipFleet.valid)
                .addComponent(imageEquipFleet.label)
                .addComponent(imageEquipFleet.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
            )
        );

        JScrollPane sp = new JScrollPane(panel);

        sp.getVerticalScrollBar().setUnitIncrement(30);
        sp.getVerticalScrollBar().setBlockIncrement(90);

        setLayout(new BorderLayout());
        add(sp, BorderLayout.CENTER);
    }
    /**
     * Set the images.
     */
    void setImages() {
        String imageBase = imageField.component.getText();
        if (master != null) {
            master.set("image", imageBase);
        }
        if (imageBase != null && !imageBase.isEmpty()) {
            Action0 act = new Action0() {
                @Override
                public void invoke() {
                    validateImages();
                }
            };
            imageNormal.setImage(imageBase + ".png", context, act);
            imageInfoAvail.setImage(imageBase + "_large.png", context, act);
            imageInfoWired.setImage(imageBase + "_wired_large.png", context, act);
            imageSpacewar.setImage(imageBase + "_huge.png", context, act);
            imageEquipDetails.setImage(imageBase + "_small.png", context, act);
            imageEquipFleet.setImage(imageBase + "_tiny.png", context, act);

        } else {
            imageNormal.error(errorIcon);
            imageInfoAvail.error(errorIcon);
            imageInfoWired.error(errorIcon);
            imageSpacewar.error(errorIcon);
            imageEquipDetails.error(errorIcon);
            imageEquipFleet.error(errorIcon);
            validateImages();
        }
    }
    /**
     * Validate the image fields.
     */
    void validateImages() {
        ImageIcon i = imageField.getInvalid();

        i = max(i, imageNormal.getInvalid());
        i = max(i, imageInfoAvail.getInvalid());
        i = max(i, imageInfoWired.getInvalid());

        String cat = master.get("category");

        Set<String> checkSpaceCategory = new HashSet<>();
        checkSpaceCategory.add(ResearchSubCategory.SPACESHIPS_BATTLESHIPS.toString());
        checkSpaceCategory.add(ResearchSubCategory.SPACESHIPS_STATIONS.toString());
        checkSpaceCategory.add(ResearchSubCategory.SPACESHIPS_CRUISERS.toString());
        checkSpaceCategory.add(ResearchSubCategory.SPACESHIPS_FIGHTERS.toString());

        if (checkSpaceCategory.contains(cat)) {
            i = max(i, imageSpacewar.getInvalid());
            i = max(i, imageEquipDetails.getInvalid());
            i = max(i, imageEquipFleet.getInvalid());
        }

        Set<String> checkVehicleCategory = new HashSet<>();
        checkVehicleCategory.add(ResearchSubCategory.WEAPONS_TANKS.toString());
        checkVehicleCategory.add(ResearchSubCategory.WEAPONS_VEHICLES.toString());

        if (checkVehicleCategory.contains(cat)) {
            i = max(i, imageEquipDetails.getInvalid());
            i = max(i, imageEquipFleet.getInvalid());
        }

        onValidate(i);
    }
    @Override
    public void onMasterChanged() {
        if (master != null) {
            setTextAndEnabled(imageField, master, "image", true);
        } else {
            setTextAndEnabled(imageField, null, "", false);
            imageNormal.clear();
            imageInfoAvail.clear();
            imageInfoWired.clear();
            imageSpacewar.clear();
            imageEquipDetails.clear();
            imageEquipFleet.clear();
        }
    }
}
