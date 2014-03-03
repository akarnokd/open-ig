/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action1E;
import hu.openig.core.Difficulty;
import hu.openig.core.SimulationSpeed;
import hu.openig.editors.ce.GenericTableModel;
import hu.openig.model.APIResult;
import hu.openig.model.Configuration;
import hu.openig.model.GameDefinition;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.MultiplayerUser;
import hu.openig.model.ResourceLocator;
import hu.openig.model.SkirmishAIMode;
import hu.openig.model.SkirmishDiplomaticRelation;
import hu.openig.net.ErrorResponse;
import hu.openig.net.ErrorType;
import hu.openig.screen.CommonResources;
import hu.openig.screen.panels.MultiplayerEditUserDialog;
import hu.openig.ui.IGButton;
import hu.openig.ui.IGCheckBox;
import hu.openig.utils.Exceptions;
import hu.openig.utils.GUIUtils;
import hu.openig.utils.XElement;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.xml.stream.XMLStreamException;

/**
 * The multiplayer setup screen.
 * @author akarnokd, 2013.04.23.
 */
public class MultiplayerSetupScreen extends JFrame {
	/** */
	private static final long serialVersionUID = 3550620753942266321L;
	/** The resource locator. */
	protected ResourceLocator rl;
	/** The configuration. */
	protected Configuration config;
	/** The common resources. */
	protected CommonResources commons;
	/** UI component. */
	private JPanel galaxyPanel;
	/** UI component. */
	private JPanel economyPanel;
	/** UI component. */
	private JPanel playersPanel;
	/** UI component. */
	private JPanel victoryPanel;
	/** UI component. */
	private JPanel generalPanel;
	/** Large font. */
	private Font fontLarge;
	/** Medium font. */
	private Font fontMedium;
	/** UI component. */
	private JCheckBox allowQuicksave;
	/** UI component. */
	private JCheckBox allowAutosave;
	/** UI component. */
	private JCheckBox allowPause;
	/** UI component. */
	private JComboBox<String> simulationSpeedBox;
	/** UI component. */
	private JSpinner timestepSpin;
	/** The list of campaigns. */
	final List<GameDefinition> campaigns = new ArrayList<>();
	/** UI component. */
	private JComboBox<String> galaxyBox;
	/** UI component. */
	private IGCheckBox galaxyRandomSurface;
	/** UI component. */
	private IGCheckBox galaxyRandomLayout;
	/** UI component. */
	private IGCheckBox galaxyCustomPlanets;
	/** UI component. */
	private JSpinner galaxyPlanetCount;
	/** UI component. */
	private JComboBox<String> galaxyRacesBox;
	/** UI component. */
	private JComboBox<String> technologyDefBox;
	/** UI component. */
	private JLabel galaxyInfo;
	/** UI component. */
	private JLabel galaxyRacesInfo;
	/** UI component. */
	private JLabel technologyDefInfo;
	/** UI component. */
	private JSpinner technologyLevelStartSpin;
	/** UI component. */
	private JSpinner technologyLevelMaxSpin;
	/** UI component. */
	private JComboBox<String> initialRelation;
	/** UI component. */
	private JComboBox<String> initialDifficulty;
	/** UI component. */
	private IGButton publishGame;
	/** UI component. */
	private IGButton joinGame;
	/** UI component. */
	private JPanel gameSettingsPanel;
	/** UI component. */
	private JSpinner initialMoney;
	/** UI component. */
	private JSpinner initialPlanets;
	/** UI component. */
	private JSpinner initialPopulation;
	/** UI component. */
	private IGCheckBox placeColonyHub;
	/** UI component. */
	private IGCheckBox grantColonyShip;
	/** UI component. */
	private IGCheckBox grantOrbitalFactory;
	/** UI component. */
	private JSpinner colonyShips;
	/** UI component. */
	private JSpinner orbitalFactories;
	/** UI component. */
	private IGCheckBox winConquest;
	/** UI component. */
	private IGCheckBox winOccupation;
	/** UI component. */
	private IGCheckBox winEconomic;
	/** UI component. */
	private IGCheckBox winTechnology;
	/** UI component. */
	private IGCheckBox winSocial;
	/** UI component. */
	private JSpinner winOccupationPercent;
	/** UI component. */
	private JSpinner winOccupationTime;
	/** UI component. */
	private JSpinner winEconomicMoney;
	/** UI component. */
	private JSpinner winSocialMorale;
	/** UI component. */
	private JSpinner winSocialPlanets;
	/** The player model. */
	private PlayerModel playerModel;
	/** The player table. */
	private JTable playerTable;
	/** UI component. */
	private IGButton addPlayer;
	/** UI component. */
	private IGButton editPlayer;
	/** UI component. */
	private IGButton removePlayer;
	/** UI component. */
	private JLabel playerState;
	/** UI component. */
	private IGCheckBox defaultChangeIcon;
	/** UI component. */
	private IGCheckBox defaultChangeTraits;
	/** UI component. */
	private IGCheckBox defaultChangeRace;
	/** Allow changing the group? */
	private IGCheckBox defaultChangeGroup;
	/** Allow cheating? */
	private IGCheckBox allowCheat;
	/** UI component. */
	private IGButton openButton;
	/** UI component. */
	private IGButton saveButton;
	/**  The last open/save directory. */
	private File lastDir = new File(".");
	/** Is the dialog in join mode? */
	private boolean joinMode;
	/** The local server's address. */
	private InetAddress serverAddress;
	/** The local server's port. */
	private int serverPort;
	/** The local server's UPNP usage status. */
	private boolean serverUPnP;
	/**
	 * Constructor. Initializes the sceen.
	 * @param commons the commons object
	 */
	public MultiplayerSetupScreen(CommonResources commons) {
		super();
		this.commons = commons;
		this.config = commons.config;
		this.rl = commons.rl;
		this.joinMode = true;
		init();
	}
	/**
	 * Constructor. Initializes the sceen.
	 * @param commons the commons object
	 * @param serverAddress the local server address
	 * @param serverPort the local server port
	 * @param serverUPnP the local server UPnP usage.
	 */
	public MultiplayerSetupScreen(CommonResources commons,
			InetAddress serverAddress, int serverPort, boolean serverUPnP) {
		super();
		this.commons = commons;
		this.serverAddress = serverAddress;
		this.serverPort = serverPort;
		this.serverUPnP = serverUPnP;
		this.config = commons.config;
		this.rl = commons.rl;
		this.joinMode = false;
		init();
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
	 * Initialize the GUI elements.
	 */
	private void init() {
		setTitle(get("multiplayer.settings.title"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel basePanel = new JPanel();
		fontLarge = new Font(Font.SANS_SERIF, Font.BOLD, 18);
		fontMedium = new Font(Font.SANS_SERIF, Font.BOLD, 14);
		
		publishGame = new IGButton(get("multiplayer.settings.publish"));
		publishGame.setFont(fontLarge);
		publishGame.setForeground(Color.WHITE);
		joinGame = new IGButton(get("multiplayer.settings.join"));
		joinGame.setFont(fontLarge);
		joinGame.setForeground(Color.WHITE);
		
		publishGame.setEnabled(false);
		publishGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doPublishGame();
			}
		});
		joinGame.setEnabled(false);
		joinGame.setVisible(false);
		joinGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO
			}
		});

		generalPanel = new JPanel();
		galaxyPanel = new JPanel();
		economyPanel = new JPanel();
		playersPanel = new JPanel();
		victoryPanel = new JPanel();
		galaxyPanel.setVisible(false);
		economyPanel.setVisible(false);
		playersPanel.setVisible(false);
		victoryPanel.setVisible(false);
		

		IGButton cancel = new IGButton(get("multiplayer.settings.back"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doCancel();
			}
		});
		cancel.setFont(fontLarge);
		cancel.setForeground(Color.WHITE);
		
		openButton = new IGButton();
		openButton.setIcon(new ImageIcon(MultiplayerSetupScreen.class.getResource("/hu/openig/editors/res/Open24.gif")));
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doOpen();
			}
		});
		saveButton = new IGButton();
		saveButton.setIcon(new ImageIcon(MultiplayerSetupScreen.class.getResource("/hu/openig/editors/res/Save24.gif")));
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSave();
			}
		});
		
		openButton.setVisible(!joinMode);
		saveButton.setVisible(!joinMode);
		
		initGeneral();
		initGalaxy();
		initEconomy();
		initPlayers();
		initVictory();
		
		JSeparator sep1 = new JSeparator(JSeparator.HORIZONTAL);

		gameSettingsPanel = new JPanel();
		initGameSettingsPanel();
		
		
		GroupLayout gl = new GroupLayout(basePanel);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		basePanel.setLayout(gl);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(gameSettingsPanel)
			.addComponent(sep1)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(openButton)
				.addComponent(saveButton)
				.addGap(30)
				.addComponent(publishGame)
				.addComponent(joinGame)
				.addComponent(cancel)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(gameSettingsPanel)
			.addComponent(sep1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(openButton)
				.addComponent(saveButton)
				.addComponent(publishGame)
				.addComponent(joinGame)
				.addComponent(cancel)
			)
		);
		
		setLayout(new BorderLayout());
		add(basePanel, BorderLayout.CENTER);

		checkEnablePublish();
		
		pack();
		setMinimumSize(getSize());
	}
	/** Create the settings panels. */
	void initGameSettingsPanel() {
		final IGButton generalButton = new IGButton(get("multiplayer.settings.general"));
		final IGButton galaxyButton = new IGButton(get("multiplayer.settings.galaxy"));
		final IGButton economyButton = new IGButton(get("multiplayer.settings.economy"));
		final IGButton playersButton = new IGButton(get("multiplayer.settings.players"));
		final IGButton victoryButton = new IGButton(get("multiplayer.settings.victory"));

		final JPanel[] panels = { generalPanel, galaxyPanel, economyPanel, playersPanel, victoryPanel };
		final IGButton[] buttons = { generalButton, galaxyButton, economyButton, playersButton, victoryButton };

		ActionListener tabSwitch = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < panels.length; i++) {
					if (buttons[i] == e.getSource()) {
						buttons[i].setForeground(Color.WHITE);
						panels[i].setVisible(true);
					} else {
						buttons[i].setForeground(Color.BLACK);
						panels[i].setVisible(false);
					}
				}
			}
		};
		
		generalButton.addActionListener(tabSwitch);
		galaxyButton.addActionListener(tabSwitch);
		economyButton.addActionListener(tabSwitch);
		playersButton.addActionListener(tabSwitch);
		victoryButton.addActionListener(tabSwitch);
		
		
		generalButton.setFont(fontLarge);
		galaxyButton.setFont(fontLarge);
		economyButton.setFont(fontLarge);
		playersButton.setFont(fontLarge);
		victoryButton.setFont(fontLarge);

		generalButton.setForeground(Color.WHITE);
		galaxyButton.setForeground(Color.BLACK);
		economyButton.setForeground(Color.BLACK);
		playersButton.setForeground(Color.BLACK);
		victoryButton.setForeground(Color.BLACK);

		GroupLayout gl = new GroupLayout(gameSettingsPanel);
		gl.setAutoCreateGaps(true);
		gameSettingsPanel.setLayout(gl);

		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(generalButton)
				.addComponent(galaxyButton)
				.addComponent(economyButton)
				.addComponent(playersButton)
				.addComponent(victoryButton)
			)
			.addComponent(generalPanel)
			.addComponent(galaxyPanel)
			.addComponent(economyPanel)
			.addComponent(playersPanel)
			.addComponent(victoryPanel)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(generalButton)
				.addComponent(galaxyButton)
				.addComponent(economyButton)
				.addComponent(playersButton)
				.addComponent(victoryButton)
			)
			.addComponent(generalPanel, 300, 300, Short.MAX_VALUE)
			.addComponent(galaxyPanel, 300, 300, Short.MAX_VALUE)
			.addComponent(economyPanel, 300, 300, Short.MAX_VALUE)
			.addComponent(playersPanel, 300, 300, Short.MAX_VALUE)
			.addComponent(victoryPanel, 300, 300, Short.MAX_VALUE)
		);
	}
	/** Close the panel. */
	void doCancel() {
		commons.joinCallback = null;
		commons.multiplayer.stopServer();
		dispose();
	}
	/**
	 * Initialize the general panel.
	 */
	private void initGeneral() {
		allowQuicksave = createCheckBox("multiplayer.settings.quicksave");
		allowAutosave = createCheckBox("multiplayer.settings.autosave");
		allowPause = createCheckBox("multiplayer.settings.pause");
		allowCheat = createCheckBox("multiplayer.settings.cheat");
		
		defaultChangeRace = createCheckBox("multiplayer.settings.client_race"); 
		defaultChangeIcon = createCheckBox("multiplayer.settings.client_icons"); 
		defaultChangeTraits = createCheckBox("multiplayer.settings.client_traits");
		defaultChangeGroup = createCheckBox("multiplayer.settings.change_groups");
		
		JLabel simulationSpeed = createLabel("multiplayer.settings.simulation_speed");
		simulationSpeed.setFont(fontMedium);
		simulationSpeedBox = new JComboBox<>(new String[] {
			get("multiplayer.settings.speed_normal"),	
			get("multiplayer.settings.speed_double"),	
			get("multiplayer.settings.speed_quadruple")	
		});
		simulationSpeedBox.setFont(fontMedium);
		
		JLabel timestep = createLabel("multiplayer.settings.timestep");
		timestep.setFont(fontMedium);
		timestepSpin = createSpinner(config.timestep, 1, 60, 1);
		timestepSpin.setFont(fontMedium);
		
		GroupLayout gl = new GroupLayout(generalPanel);
		generalPanel.setLayout(gl);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addGroup(
					gl.createParallelGroup()
					.addComponent(allowQuicksave)
					.addComponent(allowAutosave)
					.addComponent(allowPause)
					.addComponent(allowCheat)
				)
				.addGap(50)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(defaultChangeRace)
					.addComponent(defaultChangeIcon)
					.addComponent(defaultChangeTraits)
					.addComponent(defaultChangeGroup)
				)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(simulationSpeed)
				.addComponent(simulationSpeedBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(timestep)
				.addComponent(timestepSpin, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup()
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(allowQuicksave)
					.addComponent(allowAutosave)
					.addComponent(allowPause)
					.addComponent(allowCheat)
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(defaultChangeRace)
					.addComponent(defaultChangeIcon)
					.addComponent(defaultChangeTraits)
					.addComponent(defaultChangeGroup)
				)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(simulationSpeed)
				.addComponent(simulationSpeedBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(timestep)
				.addComponent(timestepSpin, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
	}
	/**
	 * Locate the campaings.
	 */
	private void findCampaigns() {
		campaigns.clear();
		for (String name : commons.rl.listDirectories(commons.config.language, "campaign/")) {
			GameDefinition gd = GameDefinition.parse(commons.rl, "campaign/" + name);
			campaigns.add(gd);
		}
		for (String name : commons.rl.listDirectories(commons.config.language, "skirmish/")) {
			GameDefinition gd = GameDefinition.parse(commons.rl, "skirmish/" + name);
			campaigns.add(gd);
		}
		Collections.sort(campaigns, new Comparator<GameDefinition>() {
			@Override
			public int compare(GameDefinition o1, GameDefinition o2) {
				return o1.name.compareToIgnoreCase(o2.name);
			}
		});
	}
	/**
	 * Sets the tooltip of a label based on the current item in a combobox.
	 * @author akarnokd, 2013.04.24.
	 */
	class CampaignChangeAction implements ActionListener {
		/** The box. */
		JComboBox<?> box;
		/** The label. */
		JLabel info;
		/**
		 * Constructor.
		 * @param box the box
		 * @param info the info
		 */
		public CampaignChangeAction(JComboBox<?> box, JLabel info) {
			this.box = box;
			this.info = info;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			int index = box.getSelectedIndex();
			if (index >= 0) {
				GameDefinition gd = campaigns.get(index);
				info.setToolTipText("<html><div style='width: 400px;'>" + gd.getDescription(rl.language));
			} else {
				info.setToolTipText(null);
			}
		}
	}
	/**
	 * Label with custom tooltip settings.
	 * @author akarnokd, 2013.04.24.
	 *
	 */
	class InfoLabel extends JLabel {
		/** */
		private static final long serialVersionUID = 8414905686873484094L;
		/**
		 * Constructor.
		 * @param image the image
		 */
		public InfoLabel(Image image) {
			super(new ImageIcon(image));
		}
		@Override
		public JToolTip createToolTip() {
			JToolTip tip = new JToolTip();
			tip.setForeground(Color.BLACK);
			tip.setBackground(Color.YELLOW);
			tip.setFont(fontMedium);
			return tip;
		}
	}
	/**
	 * Initialize the galaxy panel.
	 */
	void initGalaxy() {
		findCampaigns();
		JLabel galaxy = createLabel("skirmish.galaxy_template");
		galaxyBox = new JComboBox<>();
		galaxyBox.setFont(fontMedium);
		galaxyInfo = new InfoLabel(commons.common().infoIcon);
		
		galaxyRandomSurface = createCheckBox("skirmish.random_surface");
		galaxyRandomLayout = createCheckBox("skirmish.random_layout");
		galaxyRandomLayout.setEnabled(false);
		galaxyCustomPlanets = createCheckBox("skirmish.custom_planets");
		galaxyCustomPlanets.setEnabled(false);
		
		galaxyPlanetCount = createSpinner(100, 0, 500, 1);
		galaxyPlanetCount.setEnabled(false);
		galaxyPlanetCount.setFont(fontMedium);
		
		JLabel galaxyRaces = createLabel("skirmish.race_template");
		galaxyRacesBox = new JComboBox<>();
		galaxyRacesBox.setFont(fontMedium);
		galaxyRacesInfo = new InfoLabel(commons.common().infoIcon);
		
		JLabel technologyDef = createLabel("skirmish.tech_template");
		technologyDefBox = new JComboBox<>();
		technologyDefBox.setFont(fontMedium);
		technologyDefInfo = new InfoLabel(commons.common().infoIcon);
		
		galaxyBox.addActionListener(new CampaignChangeAction(galaxyBox, galaxyInfo));
		galaxyRacesBox.addActionListener(new CampaignChangeAction(galaxyRacesBox, galaxyRacesInfo));
		technologyDefBox.addActionListener(new CampaignChangeAction(technologyDefBox, technologyDefInfo));
		
		for (GameDefinition gd : campaigns) {
			galaxyBox.addItem(gd.getTitle(rl.language));
			galaxyRacesBox.addItem(gd.getTitle(rl.language));
			technologyDefBox.addItem(gd.getTitle(rl.language));
		}

		JLabel technologyLevel = createLabel("skirmish.tech_level");
		JLabel technologyLevelMax = createLabel("skirmish.tech_level_max");
		
		technologyLevelStartSpin = createSpinner(0, 0, 6, 1);
		technologyLevelStartSpin.setFont(fontMedium);

		technologyLevelMaxSpin = createSpinner(5, 1, 5, 1);
		technologyLevelMaxSpin.setFont(fontMedium);

		JLabel initialRelationLabel = createLabel("skirmish.initial_relation");
		initialRelation = new JComboBox<>();
		
		for (SkirmishDiplomaticRelation dr : SkirmishDiplomaticRelation.values()) {
			initialRelation.addItem(get("skirmish.relation." + dr));
		}
		initialRelation.setSelectedIndex(SkirmishDiplomaticRelation.DEFAULT.ordinal());
		initialRelation.setFont(fontMedium);
		
		JLabel initialDifficultyLabel = createLabel("skirmish.initial_difficulty");
		initialDifficulty = new JComboBox<>();
		for (Difficulty value : Difficulty.values()) {
			initialDifficulty.addItem(get("difficulty." + value));
		}
		initialDifficulty.setSelectedIndex(Difficulty.NORMAL.ordinal());
		initialDifficulty.setFont(fontMedium);

		
		GroupLayout gl = new GroupLayout(galaxyPanel);
		galaxyPanel.setLayout(gl);
		gl.setAutoCreateGaps(true);

		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(galaxy)
				.addComponent(galaxyBox)
				.addComponent(galaxyInfo)
			)
			.addComponent(galaxyRandomSurface)
			.addComponent(galaxyRandomLayout)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(galaxyCustomPlanets)
				.addComponent(galaxyPlanetCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(galaxyRaces)
				.addComponent(galaxyRacesBox)
				.addComponent(galaxyRacesInfo)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(technologyDef)
				.addComponent(technologyDefBox)
				.addComponent(technologyDefInfo)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(technologyLevel)
				.addComponent(technologyLevelStartSpin, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(technologyLevelMax)
				.addComponent(technologyLevelMaxSpin, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(initialRelationLabel)
				.addComponent(initialRelation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(initialDifficultyLabel)
				.addComponent(initialDifficulty, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(galaxy)
				.addComponent(galaxyBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(galaxyInfo)
			)
			.addComponent(galaxyRandomSurface)
			.addComponent(galaxyRandomLayout)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(galaxyCustomPlanets)
				.addComponent(galaxyPlanetCount, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(galaxyRaces)
				.addComponent(galaxyRacesBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(galaxyRacesInfo)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(technologyDef)
				.addComponent(technologyDefBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(technologyDefInfo)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(technologyLevel)
				.addComponent(technologyLevelStartSpin, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(technologyLevelMax)
				.addComponent(technologyLevelMaxSpin, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(initialRelationLabel)
				.addComponent(initialRelation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(initialDifficultyLabel)
				.addComponent(initialDifficulty, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
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
	/** Create the economy panel. */
	void initEconomy() {
		JLabel initialMoneyLabel = createLabel("skirmish.initial_money");
		initialMoney = createSpinner(200000, 0, 2000000000, 10000); 
		
		JLabel initialPlanetsLabel = createLabel("skirmish.initial_planets");
		initialPlanets = createSpinner(3, 1, 500, 1); 
		
		JLabel initialPopulationLabel = createLabel("skirmish.initial_population");
		initialPopulation = createSpinner(5000, 0, 1000000, 100); 
		
		placeColonyHub = createCheckBox("skirmish.place_colony_hub");
		placeColonyHub.setSelected(true);
		grantColonyShip = createCheckBox("skirmish.grant_colonyship");
		grantOrbitalFactory = createCheckBox("skirmish.grant_orbital_factory");
		
		JLabel colonyShipLabel = createLabel("skirmish.colony_ships");
		colonyShips = createSpinner(1, 0, 1000, 1); 
		
		JLabel orbitalFactoryLabel = createLabel("skirmish.orbital_factories");
		orbitalFactories = createSpinner(0, 0, 1000, 1); 

		
		GroupLayout gl = new GroupLayout(economyPanel);
		economyPanel.setLayout(gl);
		gl.setAutoCreateGaps(true);

		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(initialMoneyLabel)
				.addComponent(initialMoney, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(initialPlanetsLabel)
				.addComponent(initialPlanets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(initialPopulationLabel)
				.addComponent(initialPopulation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(placeColonyHub)
			.addComponent(grantColonyShip)
			.addGroup(
				gl.createSequentialGroup()
				.addGap(30)
				.addComponent(colonyShipLabel)
				.addComponent(colonyShips, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(grantOrbitalFactory)
			.addGroup(
				gl.createSequentialGroup()
				.addGap(30)
				.addComponent(orbitalFactoryLabel)
				.addComponent(orbitalFactories, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(initialMoneyLabel)
				.addComponent(initialMoney, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(initialPlanetsLabel)
				.addComponent(initialPlanets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(initialPopulationLabel)
				.addComponent(initialPopulation, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(placeColonyHub)
			.addComponent(grantColonyShip)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(colonyShipLabel)
				.addComponent(colonyShips, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(grantOrbitalFactory)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(orbitalFactoryLabel)
				.addComponent(orbitalFactories, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
	}
	/**
	 * Set a tooltip on a component.
	 * @param c the component
	 * @param tipKey the tip key
	 */
	void setTooltip(JComponent c, String tipKey) {
		if (tipKey != null) {
			c.setToolTipText("<html><div style='width: 400px;'>" + get(tipKey));
		} else {
			c.setToolTipText(null);
		}
	}
	/** Initialize victory panel. */
	void initVictory() {
		winConquest = createCheckBox("skirmish.conquest");
		setTooltip(winConquest, "skirmish.conquest.tooltip");
		winOccupation = createCheckBox("skirmish.occupation");
		setTooltip(winOccupation, "skirmish.occupation.tooltip");
		winEconomic = createCheckBox("skirmish.economic");
		setTooltip(winEconomic, "skirmish.economic.tooltip");
		winTechnology = createCheckBox("skirmish.technology");
		setTooltip(winTechnology, "skirmish.technology.tooltip");
		winSocial = createCheckBox("skirmish.social");
		setTooltip(winSocial, "skirmish.social.tooltip");
		
		winOccupationPercent = createSpinner(66, 0, 100, 1);
		
		winOccupationTime = createSpinner(30, 0, 1000, 1);
		
		winEconomicMoney = createSpinner(10000000, 0, 2000000000, 1000000);
		
		winSocialMorale = createSpinner(95, 0, 100, 1);
		
		winSocialPlanets = createSpinner(30, 0, 500, 1);

		JLabel winOccupationPercentLabel = createLabel("skirmish.occupation_percent");
		JLabel winOccupationTimeLabel = createLabel("skirmish.occupation_time");
		JLabel winEconomicMoneyLabel = createLabel("skirmish.economic_money");
		JLabel winSocialMoraleLabel = createLabel("skirmish.social_morale");
		JLabel winSocialPlanetsLabel = createLabel("skirmish.social_planets");

		GroupLayout gl = new GroupLayout(victoryPanel);
		victoryPanel.setLayout(gl);
		gl.setAutoCreateGaps(true);

		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addComponent(winConquest)
			.addComponent(winOccupation)
			.addGroup(
				gl.createSequentialGroup()
				.addGap(30)
				.addComponent(winOccupationPercentLabel)
				.addComponent(winOccupationPercent, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addGap(30)
				.addComponent(winOccupationTimeLabel)
				.addComponent(winOccupationTime, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(winEconomic)
			.addGroup(
				gl.createSequentialGroup()
				.addGap(30)
				.addComponent(winEconomicMoneyLabel)
				.addComponent(winEconomicMoney, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(winTechnology)
			.addComponent(winSocial)
			.addGroup(
				gl.createSequentialGroup()
				.addGap(30)
				.addComponent(winSocialMoraleLabel)
				.addComponent(winSocialMorale, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addGap(30)
				.addComponent(winSocialPlanetsLabel)
				.addComponent(winSocialPlanets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(winConquest)
			.addComponent(winOccupation)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(winOccupationPercentLabel)
				.addComponent(winOccupationPercent, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(winOccupationTimeLabel)
				.addComponent(winOccupationTime, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(winEconomic)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(winEconomicMoneyLabel)
				.addComponent(winEconomicMoney, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(winTechnology)
			.addComponent(winSocial)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(winSocialMoraleLabel)
				.addComponent(winSocialMorale, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(winSocialPlanetsLabel)
				.addComponent(winSocialPlanets, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
	}
	/**
	 * The players table model.
	 * @author akarnokd, 2013.04.24.
	 *
	 */
	class PlayerModel extends GenericTableModel<MultiplayerUser> {
		/** */
		private static final long serialVersionUID = -1832103927527003705L;
		@Override
		public Object getValueFor(MultiplayerUser item, int rowIndex,
				int columnIndex) {
			switch (columnIndex) {
			case 0:
				if (item.ai == null) {
					if (joinMode) {
						return MultiplayerSetupScreen.this.get("multiplayer.settings.host_player");
					}
					return item.userName;
				}
				return MultiplayerSetupScreen.this.get("skirmish.ai." + item.ai + ".tooltip");
			case 1:
				return item.description + " - " + item.race;
			case 2:
				if (item.icon != null) {
					return new ImageIcon(item.icon);
				}
				return null;
			case 3:
				return !item.traits.isEmpty();
			case 4:
				return item.group;
			case 5:
				return item.joined();
			default:
				return null;
			}
		}
	}
	/** Initialize players panel. */
	void initPlayers() {
		playerModel = new PlayerModel();
		playerModel.setColumnTypes(
				String.class,
				String.class,
				ImageIcon.class,
				Boolean.class,
				Integer.class,
				Boolean.class
		);
		playerModel.setColumnNames(
				get("multiplayer.settings.user"),
				get("multiplayer.settings.race"),
				get("multiplayer.settings.icon"),
				get("multiplayer.settings.traits"),
				get("multiplayer.settings.group"),
				get("multiplayer.settings.joined")
		);
		
		playerTable = new JTable(playerModel);
		playerTable.setFont(fontMedium);
		playerTable.getTableHeader().setFont(fontMedium);
		playerTable.setRowHeight(20);
		
		playerTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
			/** */
			private static final long serialVersionUID = 7042968471226335390L;

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setOpaque(true);
				if (!isSelected) {
					label.setBackground(Color.BLACK);
				} else {
					label.setBackground(Color.DARK_GRAY);
				}
				label.setText(null);
				label.setIcon((Icon)value);
				label.setHorizontalAlignment(JLabel.CENTER);
				return label;
			}
		});
		
		JScrollPane sp = new JScrollPane(playerTable);
		
		addPlayer = new IGButton(get("multiplayer.settings.add_player"));
		addPlayer.setFont(fontMedium);
		addPlayer.setForeground(Color.WHITE);
		addPlayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GameDefinition def = campaigns.get(galaxyRacesBox.getSelectedIndex());
				MultiplayerEditUserDialog dialog = new MultiplayerEditUserDialog(playerModel, commons, null, def, isJoinMode());
				dialog.prepareNewUser(
						defaultChangeIcon.isSelected(),
						defaultChangeRace.isSelected(),
						defaultChangeTraits.isSelected(),
						defaultChangeGroup.isSelected()
				);
				dialog.setLocationRelativeTo(MultiplayerSetupScreen.this);
				MultiplayerUser mu = dialog.showDialog();
				if (mu != null) {
					playerModel.add(mu);
					GUIUtils.autoResizeColWidth(playerTable, playerModel);
					checkEnablePublish();
				}
			}
		});
		
		
		editPlayer = new IGButton(get("multiplayer.settings.edit_player"));
		editPlayer.setFont(fontMedium);
		editPlayer.setForeground(Color.WHITE);
		editPlayer.setEnabled(false);
		editPlayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doEditPlayer();
			}
		});
		playerTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					doEditPlayer();
				}
			}
		});
		
		
		removePlayer = new IGButton(get("multiplayer.settings.remove_player"));
		removePlayer.setFont(fontMedium);
		removePlayer.setForeground(Color.WHITE);
		removePlayer.setEnabled(false);
		removePlayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int[] idxs = playerTable.getSelectedRows();
				for (int i = 0; i < idxs.length; i++) {
					idxs[i] = playerTable.convertRowIndexToModel(idxs[i]);
				}
				playerModel.delete(idxs);
				checkEnablePublish();
			}
		});
		
		playerState = new JLabel("");
		playerState.setFont(fontMedium);
		
		playerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updatePlayerButtons();
			}

		});
		
		GroupLayout gl = new GroupLayout(playersPanel);
		playersPanel.setLayout(gl);
		gl.setAutoCreateGaps(true);

		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(sp)
			.addComponent(playerState)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(addPlayer)
				.addComponent(editPlayer)
				.addComponent(removePlayer)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(sp)
			.addComponent(playerState)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(addPlayer)
				.addComponent(editPlayer)
				.addComponent(removePlayer)
			)
		);
	}
	/**
	 * Update the player button states.
	 */
	void updatePlayerButtons() {
		boolean isSelected = playerTable.getSelectedRow() >= 0;
		editPlayer.setEnabled(isSelected);
		removePlayer.setEnabled(isSelected && !isJoinMode());
	}
	/** @return Are we in join game mode? */
	boolean isJoinMode() {
		return joinMode;
	}
	/**
	 * Edit the currently selected player. 
	 */
	void doEditPlayer() {
		int idx = playerTable.getSelectedRow();
		if (idx >= 0) {
			idx = playerTable.convertRowIndexToModel(idx);

			MultiplayerUser mu = playerModel.get(idx);
			
			GameDefinition def = campaigns.get(galaxyRacesBox.getSelectedIndex());
			
			MultiplayerEditUserDialog dialog = new MultiplayerEditUserDialog(playerModel, commons, mu, def, isJoinMode());
			dialog.setLocationRelativeTo(MultiplayerSetupScreen.this);
			mu = dialog.showDialog();
			if (mu != null) {
				playerModel.update(idx);
				GUIUtils.autoResizeColWidth(playerTable, playerModel);
				checkEnablePublish();
			}
		}
	}
	/**
	 * Check the conditions to enable game publishing.
	 */
	void checkEnablePublish() {
		if (playerModel.getRowCount() >= 2) {
			int you = 0;
			boolean remote = false;
			for (MultiplayerUser mu : playerModel) {
				if (mu.ai == null) {
					remote |= true;
				} else
				if (mu.ai == SkirmishAIMode.USER) {
					you++;
				}
			}
			publishGame.setEnabled(you == 1 && remote);
			if (you == 0) {
				playerState.setText(get("multiplayer.settings.you_missing"));
				playerState.setIcon(new ImageIcon(commons.common().warningIcon));
			} else
			if (you > 1) {
				playerState.setText(get("multiplayer.settings.too_many_you"));
				playerState.setIcon(new ImageIcon(commons.common().warningIcon));
			} else
			if (!remote) {
				playerState.setText(get("multiplayer.settings.no_remote_user"));
				playerState.setIcon(new ImageIcon(commons.common().warningIcon));
			} else {
				playerState.setText("");
				playerState.setIcon(null);
			}
		} else {
			publishGame.setEnabled(false);
			playerState.setText(get("multiplayer.settings.not_enough_players"));
			playerState.setIcon(new ImageIcon(commons.common().warningIcon));
		}
	}
	/**
	 * Create the multiplayer definition from the GUI state.
	 * @return the multiplayer definition
	 */
	public MultiplayerDefinition createDefinition() {
		MultiplayerDefinition r = new MultiplayerDefinition();
		
		// global definitions
		
		r.allowQuickSave = this.allowQuicksave.isSelected();
		r.allowAutoSave = this.allowAutosave.isSelected();
		r.allowPause = this.allowPause.isSelected();
		r.allowCheat = this.allowCheat.isSelected();
		
		r.speed = SimulationSpeed.values()[this.simulationSpeedBox.getSelectedIndex()];
		r.timestep = ((Number)this.timestepSpin.getValue()).intValue();
		
		// add galaxy mode
		
		r.galaxy = campaigns.get(galaxyBox.getSelectedIndex()).name;
		r.race = campaigns.get(galaxyRacesBox.getSelectedIndex()).name;
		r.tech = campaigns.get(technologyDefBox.getSelectedIndex()).name;
		r.galaxyRandomSurface = galaxyRandomSurface.isSelected();
		r.galaxyRandomLayout = galaxyRandomLayout.isSelected();
		r.galaxyCustomPlanets = galaxyCustomPlanets.isSelected();
		r.galaxyPlanetCount = ((Number)galaxyPlanetCount.getValue()).intValue();
		
		r.startLevel = ((Number)technologyLevelStartSpin.getValue()).intValue();
		r.maxLevel = ((Number)technologyLevelMaxSpin.getValue()).intValue();
		r.initialDiplomaticRelation = SkirmishDiplomaticRelation.values()[initialRelation.getSelectedIndex()];
		r.initialDifficulty = Difficulty.values()[initialDifficulty.getSelectedIndex()];
		
		// add economy
		
		r.initialMoney = ((Number)initialMoney.getValue()).intValue();
		r.initialPlanets = ((Number)initialPlanets.getValue()).intValue();
		r.initialPopulation = ((Number)initialPopulation.getValue()).intValue();
		r.placeColonyHubs = placeColonyHub.isSelected();
		r.grantColonyShip = grantColonyShip.isSelected();
		r.grantOrbitalFactory = grantOrbitalFactory.isSelected();
		r.initialColonyShips = ((Number)colonyShips.getValue()).intValue();
		r.initialOrbitalFactories = ((Number)orbitalFactories.getValue()).intValue();
		
		// add players
		for (MultiplayerUser mu : playerModel) {
			r.players.add(mu.copy());
		}
		// add victory conditions
		
		r.victoryConquest = winConquest.isSelected();
		r.victoryOccupation = winOccupation.isSelected();
		r.victoryEconomic = winEconomic.isSelected();
		r.victoryTechnology = winTechnology.isSelected();
		r.victorySocial = winSocial.isSelected();
		
		r.victoryOccupationPercent = ((Number)winOccupationPercent.getValue()).intValue();
		r.victoryOccupationTime = ((Number)winOccupationTime.getValue()).intValue();
		r.victoryEconomicMoney = ((Number)winEconomicMoney.getValue()).intValue();
		r.victorySocialMorale = ((Number)winSocialMorale.getValue()).intValue();
		r.victorySocialPlanets = ((Number)winSocialPlanets.getValue()).intValue();
		
		return r;
	}
	/**
	 * @return the file filter for multiplayer file
	 */
	private FileFilter multiplayerFileFilter() {
		return new javax.swing.filechooser.FileFilter() {
			
			@Override
			public String getDescription() {
				return get("multiplayer.file_type");
			}
			
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".mp.xml");
			}
		};
	}
	/**
	 * Open an existing multiplayer definition.
	 * @return true if the open was approved
	 */
	boolean doOpen() {
		JFileChooser fc = new JFileChooser(lastDir);
		fc.setDialogTitle(get("multiplayer.load_file"));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(true);
		fc.setFileFilter(multiplayerFileFilter());
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			lastDir = f.getParentFile();
			doOpenFile(f);
			return true;
		}
		return false;
	}
	/**
	 * Save the current multiplayer definition.
	 */
	void doSave() {
		JFileChooser fc = new JFileChooser(lastDir);
		fc.setDialogTitle(get("multiplayer.save_file"));
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setAcceptAllFileFilterUsed(true);
		fc.setFileFilter(multiplayerFileFilter());
		
		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			File f = fc.getSelectedFile();
			
			lastDir = f.getParentFile();
			doSaveFile(f);
		}
	}
	/**
	 * Sets a boolean value on the checkbox.
	 * @param value the source value
	 * @param cb the target checkbox
	 */
	void set(boolean value, JCheckBox cb) {
		cb.setSelected(value);
	}
	/**
	 * Set an integer value on the spinner.
	 * @param value the source value
	 * @param sp the target spinner
	 */
	void set(int value, JSpinner sp) {
		sp.setValue(value);
	}
	/**
	 * Set a string on the combobox based on its index value.
	 * @param value the source value
	 * @param cb the target combobox
	 * @param options the options
	 */
	void set(String value, JComboBox<?> cb, List<String> options) {
		cb.setSelectedIndex(options.indexOf(value));
	}
	/**
	 * Set an enum on the combobox value based on its ordinal value.
	 * @param <E> the enum type
	 * @param value the source value
	 * @param cb the combobox
	 */
	<E extends Enum<E>> void set(E value, JComboBox<?> cb) {
		cb.setSelectedIndex(value.ordinal());
	}
	/**
	 * Load GUI from the definition record.
	 * @param r the definition record
	 */
	void loadFromDefinition(MultiplayerDefinition r) {
		set(r.allowQuickSave, this.allowQuicksave);
		set(r.allowAutoSave, this.allowAutosave);
		set(r.allowPause, this.allowPause);
		set(r.allowCheat, this.allowCheat);
		
		set(r.speed, this.simulationSpeedBox);
		set(r.timestep, this.timestepSpin);
		
		List<String> campaignNames = new ArrayList<>();
		for (GameDefinition  gd : campaigns) {
			campaignNames.add(gd.name);
		}
		
		// add galaxy mode
		
		set(r.galaxy, galaxyBox, campaignNames);
		set(r.race, galaxyRacesBox, campaignNames);
		set(r.tech, technologyDefBox, campaignNames);
		set(r.galaxyRandomSurface, galaxyRandomSurface);
		set(r.galaxyRandomLayout, galaxyRandomLayout);
		set(r.galaxyCustomPlanets, galaxyCustomPlanets);
		set(r.galaxyPlanetCount, galaxyPlanetCount);
		
		set(r.startLevel, technologyLevelStartSpin);
		set(r.maxLevel, technologyLevelMaxSpin);
		set(r.initialDiplomaticRelation, initialRelation);
		set(r.initialDifficulty, initialDifficulty);
		
		// add economy
		
		set(r.initialMoney, initialMoney);
		set(r.initialPlanets, initialPlanets);
		set(r.initialPopulation, initialPopulation);
		set(r.placeColonyHubs, placeColonyHub);
		set(r.grantColonyShip, grantColonyShip);
		set(r.grantOrbitalFactory, grantOrbitalFactory);
		set(r.initialColonyShips, colonyShips);
		set(r.initialOrbitalFactories, orbitalFactories);
		
		// add players
		playerModel.clear();
		for (MultiplayerUser mu : r.players) {
			mu.icon = rl.getImage(mu.iconRef, true);
			playerModel.add(mu);
		}
		// add victory conditions
		
		set(r.victoryConquest, winConquest);
		set(r.victoryOccupation, winOccupation);
		set(r.victoryEconomic, winEconomic);
		set(r.victoryTechnology, winTechnology);
		set(r.victorySocial, winSocial);
		
		set(r.victoryOccupationPercent, winOccupationPercent);
		set(r.victoryOccupationTime, winOccupationTime);
		set(r.victoryEconomicMoney, winEconomicMoney);
		set(r.victorySocialMorale, winSocialMorale);
		set(r.victorySocialPlanets, winSocialPlanets);
		
		GUIUtils.autoResizeColWidth(playerTable, playerModel);
		checkEnablePublish();		
	}
	/**
	 * Load a multiplayer definition and setup the GUI accordingly.
	 * @param f the file to load
	 */
	void doOpenFile(File f) {
		try {
			XElement xmp = XElement.parseXML(f);
			MultiplayerDefinition def = new MultiplayerDefinition();
			def.load(xmp);
			loadFromDefinition(def);
		} catch (XMLStreamException ex) {
			Exceptions.add(ex);
		}
	}
	/**
	 * Save the current definition into the file.
	 * @param f the target file
	 */
	void doSaveFile(File f) {
		try {
			MultiplayerDefinition def = createDefinition();
			XElement xmp = new XElement("multiplayer-definition");
			def.save(xmp);

			if (!f.getName().contains(".")) {
				f = new File(f.getParentFile(), f.getName() + ".mp.xml");
			}
			
			xmp.save(f);
		} catch (IOException ex) {
			Exceptions.add(ex);
		}
	}
	/**
	 * Setup the join callback.
	 */
	void setupJoinCallback() {
		commons.joinCallback = new Action1E<MultiplayerUser, IOException>() {
			@Override
			public void invoke(MultiplayerUser param1) throws IOException {
				doHandleJoin(param1);
			}
		};
	}
	/**
	 * Handle the join of the user.
	 * @param mu the multiplayer user
	 * @throws IOException to indicate error
	 */
	void doHandleJoin(MultiplayerUser mu) throws IOException {
		boolean allJoined = false;
		for (MultiplayerUser mu0 : playerModel) {
			if (Objects.equals(mu0.userName, mu.userName)
					&& Objects.equals(mu0.passphrase, mu.passphrase)) {
				
				if (!mu0.changeGroup && mu0.group != mu.group) {
					ErrorType.JOIN_REJECTED.raise("group changed");
				}
				if (!mu0.changeIcon && !Objects.equals(mu0.iconRef, mu.iconRef)) {
					ErrorType.JOIN_REJECTED.raise("icon changed");
				}
				if (!mu0.changeRace && (!Objects.equals(mu0.race, mu.race) || !Objects.equals(mu0.originalId, mu.originalId))) {
					ErrorType.JOIN_REJECTED.raise("icon changed");
				}
				if (!mu0.changeTraits && !Objects.equals(mu0.traits, mu.traits)) {
					ErrorType.JOIN_REJECTED.raise("icon changed");
				}
				
				mu0.group = mu.group;
				mu0.iconRef = mu.iconRef;
				mu0.icon = rl.getImage(mu0.iconRef);
				mu0.race = mu.race;
				mu0.originalId = mu.originalId;
				mu0.joined(true);
				
				playerModel.update(mu0);
			}
			allJoined |= mu0.ai == SkirmishAIMode.USER | mu0.joined();
		}
		if (allJoined) {
			// start loading the multiplayer map
			dispose();
			commons.startGame(createDefinition());
		}
	}
	/**
	 * Take the current settings and start a server.
	 */
	void doPublishGame() {
		commons.multiplayer.definition = createDefinition();
		commons.multiplayer.startServer(serverAddress, serverPort, serverUPnP);
	}
	/**
	 * Show an error dialog with the given concrete message.
	 * @param messageText the message to show
	 */
	void errorDialog(String messageText) {
		JOptionPane.showMessageDialog(this, messageText, 
				get("multiplayer.error"), JOptionPane.ERROR_MESSAGE);
	}
	/**
	 * Ask for the game settings.
	 */
	void doGetGameSettings() {
		commons.multiplayer.remoteAsyncAPI.getGameDefinition(new APIResult<MultiplayerDefinition>() {
			@Override
			public void success(MultiplayerDefinition value) {
				loadFromDefinition(value);
			}
			@Override
			public void error(IOException ex) {
				if (ex instanceof ErrorResponse) {
					errorDialog(((ErrorResponse)ex).getText());
				} else {
					Exceptions.add(ex);
				}
			}
		});
	}
}
