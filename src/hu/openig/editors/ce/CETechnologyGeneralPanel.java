/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Action1;
import hu.openig.model.ResearchSubCategory;
import hu.openig.utils.U;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

/**
 * The general attributes of a technology.
 * @author akarnokd, 2012.11.03.
 */
public class CETechnologyGeneralPanel extends CESlavePanel {
	/** */
	private static final long serialVersionUID = 8942741206244193522L;
	/** The ID. */
	CEValueBox<JTextField> idField;
	/** The category field. */
	CEValueBox<JComboBox<String>> categoryField;
	/** The factory field. */
	CEValueBox<JComboBox<String>> factoryField;
	/** The index field. */
	CEValueBox<JTextField> indexField;
	/** The races. */
	CEValueBox<JTextField> raceField;
	/** Lab number. */
	CEValueBox<JTextField> civilField;
	/** Lab number. */
	CEValueBox<JTextField> mechField;
	/** Lab number. */
	CEValueBox<JTextField> compField;
	/** Lab number. */
	CEValueBox<JTextField> aiField;
	/** Lab number. */
	CEValueBox<JTextField> milField;
	/** Production cost. */
	CEValueBox<JTextField> productionField;
	/** Research cost. */
	CEValueBox<JTextField> researchField;
	/** Level. */
	CEValueBox<JComboBox<String>> levelField;
	/** The total lab count. */
	CEValueBox<JTextField> sumLabs;
	/** The factory enums. */
	static final String[] FACTORIES = { "spaceship", "equipment", "weapon", "building" };
	/** The levels enums. */
	static final String[] LEVELS = { "0", "1", "2", "3", "4", "5", "6" };
	/**
	 * Constructor. Initializes the GUI.
	 * @param context the context
	 */
	public CETechnologyGeneralPanel(CEContext context) {
		super(context);
		initGUI();
	}
	/** Initializes the GUI. */
	private void initGUI() {
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		idField = new CEValueBox<>(get("tech.id"), new JTextField());

		JComboBox<String> category = new JComboBox<>();
		for (ResearchSubCategory cat : ResearchSubCategory.values()) {
			category.addItem(get(cat.toString()));
		}
		category.setSelectedIndex(-1);
		categoryField = new CEValueBox<>(get("tech.category"), category); 
		
		JComboBox<String> factory = new JComboBox<>(new String[] {
				get("SPACESHIP"), get("EQUIPMENT"), get("WEAPON"), get("BUILDING")
		});
		factory.setSelectedIndex(-1);
		factoryField = new CEValueBox<>(get("tech.factory"), factory);

		indexField = new CEValueBox<>(get("tech.index"), numberField());
		
		productionField = CEValueBox.of(get("tech.production_cost"), numberField());
		researchField = CEValueBox.of(get("tech.research_cost"), numberField());
		
		JComboBox<String> level = new JComboBox<>(new String[] {
				get("tech.level.0"), get("tech.level.1"), get("tech.level.2"),
				get("tech.level.3"), get("tech.level.4"), get("tech.level.5"),
				get("tech.level.6")
		});
		level.setSelectedIndex(-1);
		levelField = CEValueBox.of(get("tech.level"), level);
		
		raceField = CEValueBox.of(get("tech.race"), new JTextField());
		
		JLabel labsLabel = new JLabel(get("tech.labs"));
		
		civilField = CEValueBox.of(get("tech.lab.civil"), numberField());
		mechField = CEValueBox.of(get("tech.lab.mech"), numberField());
		compField = CEValueBox.of(get("tech.lab.comp"), numberField());
		aiField = CEValueBox.of(get("tech.lab.ai"), numberField());
		milField = CEValueBox.of(get("tech.lab.mil"), numberField());
		
		JTextField sumLabsTF = numberField();
		sumLabsTF.setEditable(false);
		sumLabs = CEValueBox.of(get("tech.lab.sum"), sumLabsTF);
		
		Action1<Object> labValid = new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				setSumLab();
			}
		};
		
		addValidator(civilField, labValid);
		addValidator(mechField, labValid);
		addValidator(compField, labValid);
		addValidator(aiField, labValid);
		addValidator(milField, labValid);
		
		addValidator2(categoryField, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				if (master != null) {
					validateChoice("category", categoryField, ResearchSubCategory.class);
					validateGeneral();
				}
			}
		});
		addValidator2(factoryField, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				if (master != null) {
					validateChoice("factory", factoryField, FACTORIES);
					validateGeneral();
				}
			}
		});
		addValidator2(levelField, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				if (master != null) {
					validateChoice("level", levelField, LEVELS);
					validateGeneral();
				}
			}
		});
		
		// ------------------------------------------------------

		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addComponent(idField)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(categoryField)
				.addComponent(factoryField)
				.addComponent(indexField)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(researchField)
				.addComponent(productionField)
				.addComponent(levelField)
			)
			.addComponent(raceField)
			.addComponent(labsLabel)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(civilField)
				.addComponent(mechField)
				.addComponent(compField)
				.addComponent(aiField)
				.addComponent(milField)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(sumLabs)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(idField)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(categoryField)
				.addComponent(factoryField)
				.addComponent(indexField)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(researchField)
				.addComponent(productionField)
				.addComponent(levelField)
			)
			.addComponent(raceField)
			.addComponent(labsLabel)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(civilField)
				.addComponent(mechField)
				.addComponent(compField)
				.addComponent(aiField)
				.addComponent(milField)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(sumLabs)
			)
		);
	}
	/**
	 * Set the sum labs.
	 */
	void setSumLab() {
		String l1 = civilField.component.getText();
		String l2 = mechField.component.getText();
		String l3 = compField.component.getText();
		String l4 = aiField.component.getText();
		String l5 = milField.component.getText();
		
		int sumLabCount = 0;
		try {
			sumLabCount += l1.isEmpty() ? 0 : Integer.parseInt(l1);
			sumLabCount += l2.isEmpty() ? 0 : Integer.parseInt(l2);
			sumLabCount += l3.isEmpty() ? 0 : Integer.parseInt(l3);
			sumLabCount += l4.isEmpty() ? 0 : Integer.parseInt(l4);
			sumLabCount += l5.isEmpty() ? 0 : Integer.parseInt(l5);
			sumLabs.component.setText(String.valueOf(sumLabCount));
		} catch (NumberFormatException ex) {
			sumLabs.component.setText("");
		}
	}
	/** Validate the general fields. */
	void validateGeneral() {
		ImageIcon i = null;

		for (CEInvalid iv : U.fieldsOf(this, CEInvalid.class)) {
			i = max(i, iv.getInvalid());
		}
		
		onValidate(i);
	}

	@Override
	public void onMasterChanged() {
		if (master != null) {
			setTextAndEnabled(idField, master, "id", true);
			setTextAndEnabled(raceField, master, "race", true);
			setTextAndEnabled(indexField, master, "index", true);
			setTextAndEnabled(civilField, master, "civil", true);
			setTextAndEnabled(mechField, master, "mech", true);
			setTextAndEnabled(compField, master, "comp", true);
			setTextAndEnabled(aiField, master, "ai", true);
			setTextAndEnabled(milField, master, "mil", true);
			setTextAndEnabled(productionField, master, "production-cost", true);
			setTextAndEnabled(researchField, master, "research-cost", true);
			setChoiceAndEnabled(categoryField, master, "category", true, ResearchSubCategory.class);
			setChoiceAndEnabled(factoryField, master, "factory", true, FACTORIES);
			setChoiceAndEnabled(levelField, master, "level", true, LEVELS);
		} else {
			setTextAndEnabled(idField, null, "", false);
			setTextAndEnabled(raceField, null, "", false);
			setTextAndEnabled(indexField, null, "", false);
			setTextAndEnabled(civilField, null, "", false);
			setTextAndEnabled(mechField, null, "", false);
			setTextAndEnabled(compField, null, "", false);
			setTextAndEnabled(aiField, null, "", false);
			setTextAndEnabled(milField, null, "", false);
			setTextAndEnabled(productionField, null, "", false);
			setTextAndEnabled(researchField, null, "", false);
			setChoiceAndEnabled(categoryField, null, null, false, ResearchSubCategory.class);
			setChoiceAndEnabled(factoryField, null, null, false, FACTORIES);
			setChoiceAndEnabled(levelField, null, null, false, LEVELS);
		}
	}
}
