/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.panels;

import hu.openig.core.ResourceType;
import hu.openig.editors.ce.GenericTableModel;
import hu.openig.model.GameDefinition;
import hu.openig.model.MultiplayerUser;
import hu.openig.model.ResourceLocator.ResourcePlace;
import hu.openig.model.SkirmishAIMode;
import hu.openig.model.SkirmishPlayer;
import hu.openig.model.Trait;
import hu.openig.model.TraitKind;
import hu.openig.screen.CommonResources;
import hu.openig.ui.IGButton;
import hu.openig.ui.IGCheckBox;
import hu.openig.utils.U;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

/**
 * Edit an user. 
 * @author akarnokd, 2013.04.24.
 */
public class MultiplayerEditUserDialog extends JDialog {
	/** */
	private static final long serialVersionUID = 5328342764334198386L;
	/** The common resources. */
	protected CommonResources commons;
	/** Large font. */
	private Font fontLarge;
	/** Medium font. */
	private Font fontMedium;
	/** Was the dialog closed with OK? */
	protected boolean accept;
	/** The user to edit. */
	protected MultiplayerUser user;
	/** The current player game definition. */
	private GameDefinition def;
	/** The templates. */
	private List<SkirmishPlayer> templatePlayers;
	/** The icon names. */
	private List<String> iconNames;
	/** UI component. */
	private JComboBox<String> userTypeBox;
	/** UI component. */
	private JTextField userName;
	/** UI component. */
	private JTextField userPassphrase;
	/** UI component. */
	private JComboBox<String> empireRace;
	/** UI component. */
	private JComboBox<ImageIcon> icons;
	/** UI component. */
	private JSpinner group;
	/** UI component. */
	private IGCheckBox changeIcon;
	/** UI component. */
	private IGCheckBox changeTraits;
	/** UI component. */
	private IGCheckBox changeRace;
	/** Allow changing the group? */
	private IGCheckBox changeGroup;
	/** The trait checkboxes. */
	private final List<IGCheckBox2> traitCheckboxes = new ArrayList<>();
	/** The traits for each checkbox. */
	private final List<Trait> traitList = new ArrayList<>();
	/** The list of icon images. */
	private final List<BufferedImage> iconImages = new ArrayList<>();
	/** UI component. */
	private JLabel traitPoints;
	/** Indicate if the editor dialog is for the join mode. */
	private boolean joinMode;
	/** The group number in join mode. */
	private JLabel groupStatic;
	/** UI component. */
	private JLabel userNameLabel;
	/** UI component. */
	private JLabel userPassphraseLabel;
	/** UI component. */
	private JLabel empireRaceStatic;
	/** UI component. */
	private JLabel iconStatic;
	/** UI component. */
	private JLabel userTypeStatic;
	/** The global player model. */
	private GenericTableModel<MultiplayerUser> playerModel;
	/**
	 * Constructor.
	 * @param playerModel the player model
	 * @param commons the common resources
	 * @param user The user to edit or null to create a new
	 * @param def The current player game definition
	 * @param joinMode if viewing a game join.
	 */
	public MultiplayerEditUserDialog(
			GenericTableModel<MultiplayerUser> playerModel,
			CommonResources commons,
			MultiplayerUser user, 
			GameDefinition def, 
			boolean joinMode) {
		super();
		this.playerModel = playerModel;
		this.commons = commons;
		this.def = def;
		this.joinMode = joinMode;
		setTitle(user == null ? get("multiplayer.settings.add_user") : get("multiplayer.settings.edit_user"));
		initDialog();
		if (user != null) {
			this.user = user;
			loadValues();
		} else {
			this.user = new MultiplayerUser();
		}
	}
	/**
	 * Returns a concrete label.
	 * @param label the label id
	 * @return the translation
	 */
	protected String get(String label) {
		return commons.labels().get(label);
	}
	/**
	 * Format a label.
	 * @param label the label id
	 * @param params the parameters
	 * @return the translation
	 */
	protected String format(String label, Object... params) {
		return commons.labels().format(label, params);
	}
	/**
	 * Creates a label with the given label and medium font size.
	 * @param key the label key
	 * @return the label
	 */
	JLabel createLabel(String key) {
		JLabel r = new JLabel(get(key));
		r.setFont(fontMedium);
		return r;
	}
	/**
	 * Create a spinner with preset font.
	 * @param value the current value
	 * @param min the minimum value
	 * @param max the maximum value
	 * @param step the step
	 * @return the spinner
	 */
	JSpinner createSpinner(int value, int min, int max, int step) {
		JSpinner r = new JSpinner(new SpinnerNumberModel(value, min, max, step));
		r.setFont(fontMedium);
		return r;
	}
	/**
	 * Create a checkbox with preset font.
	 * @param key the label key
	 * @return the checkbox
	 */
	IGCheckBox createCheckBox(String key) {
		IGCheckBox r = new IGCheckBox(get(key), fontMedium);
		return r;
	}
	/** Initialize the dialog. */
	private void initDialog() {
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setModal(true);

		fontLarge = new Font(Font.SANS_SERIF, Font.BOLD, 18);
		fontMedium = new Font(Font.SANS_SERIF, Font.BOLD, 14);

		final JLabel userType = createLabel("multiplayer.settings.user_type");
		userTypeBox = new JComboBox<>();
		userTypeBox.setFont(fontMedium);
		userTypeBox.addItem(get("multiplayer.settings.client_user"));
		for (SkirmishAIMode m : SkirmishAIMode.values()) {
			userTypeBox.addItem(get("skirmish.ai." + m + ".tooltip"));
		}
		userTypeStatic = new JLabel();
		userTypeStatic.setFont(fontMedium);
		userTypeStatic.setVisible(false);

		userNameLabel = createLabel("multiplayer.settings.client_name");
		
		userName = new JTextField();
		userName.setFont(fontMedium);
		
		userPassphraseLabel = createLabel("multiplayer.settings.client_passphrase");
		
		userPassphrase = new JTextField();
		userPassphrase.setFont(fontMedium);
		
		JLabel empireRaceLabel = createLabel("multiplayer.settings.empire_race");
		empireRace = new JComboBox<>();
		empireRace.setFont(fontMedium);
		
		empireRaceStatic = new JLabel();
		empireRaceStatic.setFont(fontMedium);
		empireRaceStatic.setVisible(false);
		
		templatePlayers = U.newArrayList(commons.getPlayersFrom(def));
		for (SkirmishPlayer p : templatePlayers) {
			empireRace.addItem(p.description + " = " + p.race);
		}

		changeRace = createCheckBox("multiplayer.settings.client_race"); 
		changeIcon = createCheckBox("multiplayer.settings.client_icons"); 
		changeTraits = createCheckBox("multiplayer.settings.client_traits");
		changeGroup = createCheckBox("multiplayer.settings.change_groups");

		JLabel iconsLabel = createLabel("multiplayer.settings.icons");
		icons = new JComboBox<>();
		icons.setBackground(Color.BLACK);
		iconNames = new ArrayList<>();

		iconStatic = new JLabel();
		iconStatic.setFont(fontMedium);
		iconStatic.setVisible(false);
		iconStatic.setBackground(Color.BLACK);
		iconStatic.setOpaque(true);
		iconStatic.setHorizontalAlignment(JLabel.CENTER);

		for (ResourcePlace rp : commons.rl.list(commons.rl.language, "starmap/fleets")) {
			if (rp.type() == ResourceType.IMAGE) {
				String imgRef = rp.getName();
				iconNames.add(imgRef);
				BufferedImage image = commons.rl.getImage(imgRef);
				iconImages.add(image);
				icons.addItem(new ImageIcon(image));
			}
		}
		
		IGButton okayPlayer = new IGButton(get("multiplayer.settings.user_edit_ok"));
		okayPlayer.setFont(fontMedium);
		okayPlayer.setForeground(Color.WHITE);
		IGButton cancelPlayer = new IGButton(get("multiplayer.settings.user_edit_cancel"));
		cancelPlayer.setFont(fontMedium);
		cancelPlayer.setForeground(Color.WHITE);
		
		JLabel traitsLabel = createLabel("multiplayer.settings.traits_long");
		JPanel traitsPanel = new JPanel();
		JScrollPane traitsScroll = new JScrollPane(traitsPanel);
		traitsScroll.getVerticalScrollBar().setUnitIncrement(20);
		traitsScroll.getVerticalScrollBar().setBlockIncrement(60);
		traitsScroll.setPreferredSize(new Dimension(400, 100));
		
		GroupLayout gl2 = new GroupLayout(traitsPanel);
		gl2.setAutoCreateGaps(true);
		gl2.setAutoCreateContainerGaps(true);
		traitsPanel.setLayout(gl2);
		
		ParallelGroup pg1 = gl2.createParallelGroup();
		ParallelGroup pg2 = gl2.createParallelGroup();
		SequentialGroup sg = gl2.createSequentialGroup();
		
		for (Trait t : commons.traits()) {
			IGCheckBox2 tcb = new IGCheckBox2(get(t.label), fontMedium, t);
			tcb.setToolTipText("<html><div style='width: 400px;'>" + format(t.description, t.value));
			traitCheckboxes.add(tcb);
			traitList.add(t);
			pg1.addComponent(tcb);
			JLabel pointLabel = new JLabel((t.cost > 0 ? "+" : "") + t.cost);
			pointLabel.setFont(fontMedium);
			pg2.addComponent(pointLabel);
			
			tcb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					doTraitChanged();
				}
			});
			
			sg.addGroup(
				gl2.createParallelGroup(Alignment.CENTER)
				.addComponent(tcb)
				.addComponent(pointLabel)
			);
		}
		gl2.setHorizontalGroup(
			gl2.createSequentialGroup()
			.addGroup(pg1)
			.addGroup(pg2)
		);
		gl2.setVerticalGroup(sg);
		
		
		JLabel groupLabel = createLabel("multiplayer.settings.group");
		group = createSpinner(1, 1, 100, 1);
		
		
		groupStatic = new JLabel();
		groupStatic.setFont(fontMedium);
		groupStatic.setVisible(false);
		
		okayPlayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doPlayerOkay();
			}
		});
		cancelPlayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doPlayerCancel();
			}
		});
		
		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		
		JRootPane rp = getRootPane();
		rp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, "close-player-edit");
		rp.getActionMap().put("close-player-edit", new AbstractAction() {
			/** */
			private static final long serialVersionUID = -6075726698236355846L;

			@Override
			public void actionPerformed(ActionEvent e) {
				doPlayerCancel();
			}
		});
		
		userTypeBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (userTypeBox.getSelectedIndex() == 0) {
					userNameLabel.setEnabled(true);
					userName.setEnabled(true);
					userPassphraseLabel.setEnabled(true);
					userPassphrase.setEnabled(true);
					changeIcon.setEnabled(true);
					changeRace.setEnabled(true);
					changeTraits.setEnabled(true);
					changeGroup.setEnabled(true);
				} else {
					userNameLabel.setEnabled(false);
					userName.setEnabled(false);
					userPassphraseLabel.setEnabled(false);
					userPassphrase.setEnabled(false);
					changeIcon.setEnabled(false);
					changeRace.setEnabled(false);
					changeTraits.setEnabled(false);
					changeGroup.setEnabled(false);
				}
			}
		});
		
		traitPoints = new JLabel();
		traitPoints.setFont(fontLarge);
		
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				userName.requestFocusInWindow();
			}
		});
		
		doTraitChanged();

		if (joinMode) {
			userTypeBox.setEditable(false);
			userName.setEditable(false);
			userPassphrase.setEnabled(false);
			userPassphraseLabel.setEnabled(false);
			changeIcon.setVisible(false);
			changeRace.setVisible(false);
			changeTraits.setVisible(false);
			changeGroup.setVisible(false);
		}
		
		// ---------------
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createParallelGroup()
				.addGroup(
					gl.createSequentialGroup()
					.addGroup(
						gl.createParallelGroup()
						.addComponent(userType)
						.addComponent(userNameLabel)
						.addComponent(userPassphraseLabel)
						.addComponent(empireRaceLabel)
						.addComponent(iconsLabel)
						.addComponent(traitsLabel)
						.addComponent(groupLabel)
					)
					.addGroup(
						gl.createParallelGroup()
						.addComponent(userTypeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(userTypeStatic)
						.addComponent(userName)
						.addComponent(userPassphrase)
						.addComponent(empireRace, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(empireRaceStatic)
						.addComponent(icons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(iconStatic, 30, 30, 30)
						.addComponent(traitsScroll)
						.addComponent(traitPoints)
						.addComponent(group, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(groupStatic)
					)
				)
				.addComponent(changeRace)
				.addComponent(changeIcon)
				.addComponent(changeTraits)
				.addComponent(changeGroup)
			)
			.addComponent(sep)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(okayPlayer)
				.addComponent(cancelPlayer)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(userType)
				.addComponent(userTypeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(userTypeStatic)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(userNameLabel)
				.addComponent(userName, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(userPassphraseLabel)
				.addComponent(userPassphrase, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(empireRaceLabel)
				.addComponent(empireRace, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(empireRaceStatic)
			)
			.addComponent(changeRace)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(iconsLabel)
				.addComponent(icons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(iconStatic, 20, 20, 20)
			)
			.addComponent(changeIcon)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(traitsLabel)
				.addComponent(traitsScroll)
			)
			.addComponent(traitPoints)
			.addComponent(changeTraits)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(groupLabel)
				.addComponent(group, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(groupStatic)
			)
			.addComponent(changeGroup)
			.addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(okayPlayer)
				.addComponent(cancelPlayer)
			)
		);
		pack();
	}
	/**
	 * Prepare the controls if a new user is created.
	 * @param defaultChangeIcon the default change icon state
	 * @param defaultChangeRace the default change race state
	 * @param defaultChangeTraits the default change traits state
	 * @param defaultChangeGroup the default change group state
	 */
	public void prepareNewUser(
			boolean defaultChangeIcon,
			boolean defaultChangeRace,
			boolean defaultChangeTraits,
			boolean defaultChangeGroup) {
		this.user.group = 1 + playerModel.getRowCount();
		group.setValue(this.user.group);
		
		if (this.user.group == 1) {
			userTypeBox.setSelectedIndex(1);
		}
		
		Set<String> usedIcons = new HashSet<>();
		for (MultiplayerUser mu : playerModel) {
			usedIcons.add(mu.iconRef);
		}
		for (int i = 0; i < iconNames.size(); i++) {
			if (!usedIcons.contains(iconNames.get(i))) {
				icons.setSelectedIndex(i);
				break;
			}
		}
		
		changeIcon.setSelected(defaultChangeIcon);
		changeRace.setSelected(defaultChangeRace);
		changeTraits.setSelected(defaultChangeTraits);
		changeGroup.setSelected(defaultChangeGroup);
	}
	/**
	 * Show the dialog and return the user object.
	 * @return the the added/modified
	 */
	public MultiplayerUser showDialog() {
		pack();
		setVisible(true);
		if (accept) {
			return user;
		}
		return null;
	}
	/**
	 * Save values into the user object.
	 * @return true if the input data is valid
	 */
	boolean saveValues() {
		if (userTypeBox.getSelectedIndex() == 0) {
			String un = userName.getText();
			if (un.isEmpty()) {
				userName.requestFocusInWindow();
				JOptionPane.showMessageDialog(this, get("multiplayer.settings.error_enter_user_name"), getTitle(), JOptionPane.ERROR_MESSAGE);
				return false;
			}
			for (MultiplayerUser mu : playerModel) {
				if (mu != user) { 
					if (Objects.equals(mu.userName, un)) {
						userName.requestFocusInWindow();
						JOptionPane.showMessageDialog(this, get("multiplayer.settings.error_enter_unique_user_name"), getTitle(), JOptionPane.ERROR_MESSAGE);
						return false;
					} else
					if (Objects.equals(mu.iconRef, iconNames.get(icons.getSelectedIndex()))) {
						icons.requestFocusInWindow();
						JOptionPane.showMessageDialog(this, get("multiplayer.settings.error_enter_unique_icon"), getTitle(), JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
			}
			
			user.ai = null;
			user.userName = un;
			user.passphrase = userPassphrase.getText();
		} else {
			user.ai = SkirmishAIMode.values()[userTypeBox.getSelectedIndex() - 1];
			user.userName = null;
			user.passphrase = null;
			for (MultiplayerUser mu : playerModel) {
				if (mu != user) { 
					if (Objects.equals(mu.iconRef, iconNames.get(icons.getSelectedIndex()))) {
						icons.requestFocusInWindow();
						JOptionPane.showMessageDialog(this, get("multiplayer.settings.error_enter_unique_icon"), getTitle(), JOptionPane.ERROR_MESSAGE);
						return false;
					}
				}
			}
		}
		SkirmishPlayer sp = templatePlayers.get(empireRace.getSelectedIndex());
		
		user.description = sp.description;
		user.diplomacyHead = sp.diplomacyHead;
		user.originalId = sp.originalId;
		user.race = sp.race;
		user.nodatabase = sp.nodatabase;
		user.nodiplomacy = sp.nodiplomacy;
		user.picture = sp.picture;
		user.color = sp.color;
		user.iconRef = iconNames.get(icons.getSelectedIndex());
		user.icon = iconImages.get(icons.getSelectedIndex());

		user.changeIcon = changeIcon.isSelected();
		user.changeRace = changeRace.isSelected();
		user.changeTraits = changeTraits.isSelected();
		user.changeGroup = changeGroup.isSelected();
		
		user.traits.clear();
		int i = 0;
		for (Trait t : traitList) {
			if (traitCheckboxes.get(i).isSelected()) {
				user.traits.add(t.id);
			}
			i++;
		}
		
		user.group = (Integer)group.getValue();
		
		return true;
	}
	/**
	 * Load values from the user object.
	 */
	void loadValues() {
		if (user.ai == null) {
			userTypeBox.setSelectedIndex(0);
			userName.setText(user.userName);
			userPassphrase.setText(user.passphrase);
		} else {
			userTypeBox.setSelectedIndex(1 + user.ai.ordinal());
			userName.setText("");
			userPassphrase.setText("");
		}
		userTypeStatic.setText((String)userTypeBox.getSelectedItem());
		int i = 0;
		for (SkirmishPlayer sp : templatePlayers) {
			if (Objects.equals(sp.originalId, user.originalId)) {
				empireRace.setSelectedIndex(i);
				empireRaceStatic.setText((String)empireRace.getSelectedItem());
				break;
			}
			i++;
		}
		changeRace.setSelected(user.changeRace);
		icons.setSelectedIndex(iconNames.indexOf(user.iconRef));
		
		iconStatic.setIcon(new ImageIcon(iconImages.get(icons.getSelectedIndex())));
		
		changeIcon.setSelected(user.changeIcon);
		i = 0;
		for (Trait t : traitList) {
			traitCheckboxes.get(i).setSelected(user.traits.contains(t.id));
			i++;
		}
		changeTraits.setSelected(user.changeTraits);
		
		group.setValue(user.group);
		groupStatic.setText("" + user.group);

		doTraitChanged();
		
		if (joinMode) {
			userTypeBox.setVisible(false);
			userTypeStatic.setVisible(true);
			userPassphrase.setVisible(false);
			userPassphraseLabel.setVisible(false);
			group.setVisible(user.changeGroup);
			groupStatic.setVisible(!user.changeGroup);
			icons.setVisible(user.changeIcon);
			iconStatic.setVisible(!user.changeIcon);
			empireRace.setVisible(user.changeRace);
			empireRaceStatic.setVisible(!user.changeRace);
			
			for (IGCheckBox2 tcb : traitCheckboxes) {
				tcb.setEditable(!tcb.isEnabled());
				tcb.setEnabled(user.changeTraits);
			}
		} else {
			userTypeBox.setVisible(true);
			userTypeStatic.setVisible(false);
			userPassphrase.setVisible(true);
			userPassphraseLabel.setVisible(true);
			group.setVisible(true);
			groupStatic.setVisible(false);
			icons.setVisible(true);
			empireRace.setVisible(true);
			iconStatic.setVisible(false);
			empireRaceStatic.setVisible(false);
			for (IGCheckBox2 tcb : traitCheckboxes) {
				tcb.setEditable(true);
			}
		}
		
	}
	/** Accept changes. */
	void doPlayerOkay() {
		if (saveValues()) {
			accept = true;
			dispose();
		}
	}
	/** Cancel changes. */
	void doPlayerCancel() {
		accept = false;
		dispose();
	}
	/**
	 * Update trait counts and controls.
	 */
	void doTraitChanged() {
		int points = 0;
		loop:
		while (!Thread.currentThread().isInterrupted()) {
			Set<String> excludeIds = new HashSet<>();
			Set<TraitKind> excludeKinds = new HashSet<>();
			
			// collect exclusion settings
			for (IGCheckBox2 tcb : traitCheckboxes) {
				if (tcb.isSelected()) {
					excludeIds.addAll(tcb.trait.excludeIds);
					excludeKinds.addAll(tcb.trait.excludeKinds);
				}
			}
			points = commons.traits().initialPoints;
			for (IGCheckBox2 tcb : traitCheckboxes) {
				boolean enabled = !excludeIds.contains(tcb.trait.id) && !excludeKinds.contains(tcb.trait.kind);
				
				tcb.setSelected(tcb.isSelected() & enabled);
				tcb.setEnabled(enabled);
				
				if (tcb.isSelected()) {
					points -= tcb.trait.cost;
				}
			}
			if (points < 0) {
				for (IGCheckBox2 tcb : traitCheckboxes) {
					if (tcb.isSelected() && tcb.trait.cost > 0) {
						tcb.setSelected(false);
						continue loop;
					}
				}
				throw new AssertionError("Points remained negative?!");
			}
			for (IGCheckBox2 tcb : traitCheckboxes) {
				if (tcb.trait.cost > points && !tcb.isSelected()) {
					tcb.setEnabled(false);
				}
			}
			break;
		}
		
		traitPoints.setText(get("traits.available_points") + " " + points);			
	}
}