/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.core.Difficulty;
import hu.openig.core.ResourceType;
import hu.openig.editors.ce.GenericTableModel;
import hu.openig.model.Configuration;
import hu.openig.model.GameDefinition;
import hu.openig.model.ResourceLocator;
import hu.openig.model.ResourceLocator.ResourcePlace;
import hu.openig.model.SkirmishAIMode;
import hu.openig.model.SkirmishDiplomaticRelation;
import hu.openig.model.SkirmishPlayer;
import hu.openig.screen.CommonResources;
import hu.openig.screen.items.SkirmishScreen;
import hu.openig.ui.IGButton;
import hu.openig.ui.IGCheckBox;
import hu.openig.utils.Exceptions;
import hu.openig.utils.U;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * The multiplayer setup screen.
 * @author akarnokd, 2013.04.23.
 */
public class MultiplayerScreen extends JFrame {
	/** */
	private static final long serialVersionUID = 3550620753942266321L;
	/** The resource locator. */
	protected ResourceLocator rl;
	/** The configuration. */
	protected Configuration config;
	/** The common resources. */
	protected CommonResources commons;
	/** UI component. */
	private JPanel hostGamePanel;
	/** UI component. */
	private JPanel joinGamePanel;
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
	private JComboBox<String> localAddressBox;
	/** UI component. */
	private JComboBox<String> localPortBox;
	/** UI component. */
	private JCheckBox upnp;
	/** UI component. */
	private JComboBox<String> remoteAddressBox;
	/** UI component. */
	private JComboBox<String> remotePortBox;
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
	final List<GameDefinition> campaigns = new ArrayList<GameDefinition>();
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
	/**
	 * Constructor. Initializes the sceen.
	 * @param commons the commons object
	 */
	public MultiplayerScreen(CommonResources commons) {
		super();
		this.commons = commons;
		this.config = commons.config;
		this.rl = commons.rl;
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
		
		hostGamePanel = new JPanel();
		joinGamePanel = new JPanel();
		joinGamePanel.setVisible(false);

		final IGButton hostButton = new IGButton(get("multiplayer.settings.host_game"));
		final IGButton joinButton = new IGButton(get("multiplayer.settings.join_game"));
		publishGame = new IGButton(get("multiplayer.settings.publish"));
		publishGame.setFont(fontLarge);
		publishGame.setForeground(Color.WHITE);
		joinGame = new IGButton(get("multiplayer.settings.join"));
		joinGame.setFont(fontLarge);
		joinGame.setForeground(Color.WHITE);
		
		publishGame.setEnabled(false);
		joinGame.setEnabled(false);
		joinGame.setVisible(false);


		hostButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hostGamePanel.setVisible(true);
				joinGamePanel.setVisible(false);
				hostButton.setForeground(Color.WHITE);
				joinButton.setForeground(Color.BLACK);
				publishGame.setVisible(true);
				joinGame.setVisible(false);
			}
		});
		
		joinButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				hostGamePanel.setVisible(false);
				joinGamePanel.setVisible(true);
				hostButton.setForeground(Color.BLACK);
				joinButton.setForeground(Color.WHITE);
				publishGame.setVisible(false);
				joinGame.setVisible(true);
			}
		});
		
		hostButton.setFont(fontLarge);
		joinButton.setFont(fontLarge);
		hostButton.setForeground(Color.WHITE);
		joinButton.setForeground(Color.BLACK);
		
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
		
		initHost();
		initJoin();
		
		initGeneral();
		initGalaxy();
		initEconomy();
		initPlayers();
		initVictory();
		
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		JSeparator sep1 = new JSeparator(JSeparator.HORIZONTAL);

		gameSettingsPanel = new JPanel();
		initGameSettingsPanel();
		
		
		GroupLayout gl = new GroupLayout(basePanel);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		basePanel.setLayout(gl);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(hostButton)
				.addComponent(joinButton)
			)
			.addComponent(hostGamePanel)
			.addComponent(joinGamePanel)
			.addComponent(sep)
			.addComponent(gameSettingsPanel)
			.addComponent(sep1)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(publishGame)
				.addComponent(joinGame)
				.addComponent(cancel)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(hostButton)
				.addComponent(joinButton)
			)
			.addComponent(hostGamePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(joinGamePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(sep, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(gameSettingsPanel)
			.addComponent(sep1, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(publishGame)
				.addComponent(joinGame)
				.addComponent(cancel)
			)
		);
		
		setLayout(new BorderLayout());
		add(basePanel, BorderLayout.CENTER);
		
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
	/**
	 * Initialize the host panel.
	 */
	private void initHost() {
		JLabel localAddress = createLabel("multiplayer.settings.local_address");
		localAddress.setFont(fontMedium);
		localAddressBox = new JComboBox<String>();
		localAddressBox.setFont(fontMedium);
		try {
			boolean oneLoopback = false;
			Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
			while (networkInterfaces.hasMoreElements()) {
				NetworkInterface ni = networkInterfaces.nextElement();
				if (ni.isUp() && (!ni.isLoopback() || !oneLoopback)) {
					oneLoopback |= ni.isLoopback();
					Enumeration<InetAddress> addrs = ni.getInetAddresses();
					while (addrs.hasMoreElements()) {
						InetAddress ia = addrs.nextElement();
						String ha = ia.getHostAddress();
						localAddressBox.addItem(ha);
					}
				}
			}
		} catch (SocketException e) {
			Exceptions.add(e);
		}
		if (config.lastServerAddress != null && !config.lastServerAddress.isEmpty()) {
			localAddressBox.setSelectedItem(config.lastServerAddress);
		}
		
		JLabel localPort = createLabel("multiplayer.settings.local_port");
		localPort.setFont(fontMedium);
		localPortBox = new JComboBox<String>();
		localPortBox.setEditable(true);
		localPortBox.setFont(fontMedium);
		for (String port : config.serverPorts) {
			localPortBox.addItem(port);
		}
		if (config.lastServerPort != null && !config.lastServerPort.isEmpty()) {
			localPortBox.setSelectedItem(config.lastServerPort);
		}
		
		upnp = createCheckBox("multiplayer.settings.upnp");
		upnp.setSelected(config.upnp);
		
		IGButton upnpSettings = new IGButton(get("multiplayer.settings.upnp_settings"));
		upnpSettings.setEnabled(false);
		upnpSettings.setFont(fontMedium);
		upnpSettings.setForeground(Color.WHITE);
		
		GroupLayout gl = new GroupLayout(hostGamePanel);
		hostGamePanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(localAddress)
				.addComponent(localAddressBox)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(localPort)
				.addComponent(localPortBox, 100, 100, 100)
				.addGap(30)
				.addComponent(upnp)
				.addComponent(upnpSettings)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(localAddress)
				.addComponent(localAddressBox)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(localPort)
				.addComponent(localPortBox)
				.addComponent(upnp)
				.addComponent(upnpSettings)
			)
		);
	}
	/**
	 * Initialize the join panel.
	 */
	private void initJoin() {
		JLabel remoteAddress = createLabel("multiplayer.settings.remote_address");
		remoteAddress.setFont(fontMedium);
		remoteAddressBox = new JComboBox<String>();
		remoteAddressBox.setEditable(true);
		remoteAddressBox.setFont(fontMedium);
		for (String s : config.clientAddresses) {
			remoteAddressBox.addItem(s);
		}
		if (config.lastClientAddress != null && !config.lastClientAddress.isEmpty()) {
			remoteAddressBox.setSelectedItem(config.lastClientAddress);
		}
		
		JLabel remotePort = createLabel("multiplayer.settings.remote_port");
		remotePort.setFont(fontMedium);
		remotePortBox = new JComboBox<String>();
		remotePortBox.setEditable(true);
		remotePortBox.setFont(fontMedium);
		for (String s : config.clientPorts) {
			remotePortBox.addItem(s);
		}
		if (config.lastClientPort != null && !config.lastClientPort.isEmpty()) {
			remotePortBox.setSelectedItem(config.lastClientPort);
		}

		IGButton connect = new IGButton(get("multiplayer.settings.connect"));
		connect.setFont(fontMedium);
		connect.setForeground(Color.WHITE);
		
		GroupLayout gl = new GroupLayout(joinGamePanel);
		joinGamePanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(remoteAddress)
				.addComponent(remoteAddressBox)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(remotePort)
				.addComponent(remotePortBox, 100, 100, 100)
				.addGap(30)
				.addComponent(connect)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(remoteAddress)
				.addComponent(remoteAddressBox)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(remotePort)
				.addComponent(remotePortBox)
				.addComponent(connect)
			)
		);
	}
	/** Close the panel. */
	void doCancel() {
		dispose();
	}
	/**
	 * Initialize the general panel.
	 */
	private void initGeneral() {
		allowQuicksave = createCheckBox("multiplayer.settings.quicksave");
		allowAutosave = createCheckBox("multiplayer.settings.autosave");
		allowPause = createCheckBox("multiplayer.settings.pause");
		
		JLabel simulationSpeed = createLabel("multiplayer.settings.simulation_speed");
		simulationSpeed.setFont(fontMedium);
		simulationSpeedBox = new JComboBox<String>(new String[] {
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
			.addComponent(allowQuicksave)
			.addComponent(allowAutosave)
			.addComponent(allowPause)
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
			.addComponent(allowQuicksave)
			.addComponent(allowAutosave)
			.addComponent(allowPause)
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
				info.setToolTipText(gd.getDescription(rl.language));
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
		galaxyBox = new JComboBox<String>();
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
		galaxyRacesBox = new JComboBox<String>();
		galaxyRacesBox.setFont(fontMedium);
		galaxyRacesInfo = new InfoLabel(commons.common().infoIcon);
		
		JLabel technologyDef = createLabel("skirmish.tech_template");
		technologyDefBox = new JComboBox<String>();
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
		initialRelation = new JComboBox<String>();
		
		for (SkirmishDiplomaticRelation dr : SkirmishDiplomaticRelation.values()) {
			initialRelation.addItem(get("skirmish.relation." + dr));
		}
		initialRelation.setSelectedIndex(SkirmishDiplomaticRelation.DEFAULT.ordinal());
		initialRelation.setFont(fontMedium);
		
		JLabel initialDifficultyLabel = createLabel("skirmish.initial_difficulty");
		initialDifficulty = new JComboBox<String>();
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
			c.setToolTipText(get(tipKey));
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
	 * The record type of the user.
	 * @author akarnokd, 2013.04.24.
	 */
	public static class MultiplayerUser {
		// TODO fields
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
			// TODO Auto-generated method stub
			return null;
		}
	}
	/** Initialize players panel. */
	void initPlayers() {
		
		playerModel = new PlayerModel();
		playerModel.setColumnTypes(
				String.class,
				ImageIcon.class,
				String.class,
				Boolean.class,
				Integer.class
		);
		playerModel.setColumnNames(
				get("multiplayer.settings.user"),
				get("multiplayer.settings.race"),
				get("multiplayer.settings.icon"),
				get("multiplayer.settings.traits"),
				get("multiplayer.settings.group")
		);
		
		playerTable = new JTable(playerModel);
		playerTable.setFont(fontMedium);
		playerTable.getTableHeader().setFont(fontMedium);
		JScrollPane sp = new JScrollPane(playerTable);
		
		final IGButton addPlayer = new IGButton(get("multiplayer.settings.add_player"));
		addPlayer.setFont(fontMedium);
		addPlayer.setForeground(Color.WHITE);
		addPlayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GameDefinition def = campaigns.get(galaxyRacesBox.getSelectedIndex());
				EditMultiplayerUser dialog = new EditMultiplayerUser(null, def);
				dialog.setLocationRelativeTo(MultiplayerScreen.this);
				MultiplayerUser mu = dialog.showDialog();
				if (mu != null) {
					playerModel.add(mu);
				}
			}
		});
		
		
		final IGButton editPlayer = new IGButton(get("multiplayer.settings.edit_player"));
		editPlayer.setFont(fontMedium);
		editPlayer.setForeground(Color.WHITE);
		editPlayer.setEnabled(false);
		editPlayer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int idx = playerTable.getSelectedRow();
				if (idx >= 0) {
					idx = playerTable.convertRowIndexToModel(idx);

					MultiplayerUser mu = playerModel.get(idx);
					
					GameDefinition def = campaigns.get(galaxyRacesBox.getSelectedIndex());
					
					EditMultiplayerUser dialog = new EditMultiplayerUser(mu, def);
					dialog.setLocationRelativeTo(MultiplayerScreen.this);
					mu = dialog.showDialog();
					if (mu != null) {
						playerModel.update(idx);
					}
				}
			}
		});
		
		
		final IGButton removePlayer = new IGButton(get("multiplayer.settings.remove_player"));
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
			}
		});
		
		playerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				boolean isSelected = playerTable.getSelectedRow() >= 0;
				editPlayer.setEnabled(isSelected);
				removePlayer.setEnabled(isSelected);
			}
		});
		
		GroupLayout gl = new GroupLayout(playersPanel);
		playersPanel.setLayout(gl);
		gl.setAutoCreateGaps(true);

		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(sp)
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
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(addPlayer)
				.addComponent(editPlayer)
				.addComponent(removePlayer)
			)
		);
	}
	/**
	 * Edit an user. 
	 * @author akarnokd, 2013.04.24.
	 */
	class EditMultiplayerUser extends JDialog {
		/** */
		private static final long serialVersionUID = 5328342764334198386L;
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
		private IGCheckBox changeIcon;
		/** UI component. */
		private IGCheckBox changeTraits;
		/** UI component. */
		private JSpinner group;
		/** UI component. */
		private IGCheckBox changeRace;
		/**
		 * Constructor.
		 * @param user The user to edit or null to create a new
		 * @param def The current player game definition
		 */
		public EditMultiplayerUser(MultiplayerUser user, GameDefinition def) {
			super();
			this.def = def;
			setTitle(user == null ? get("multiplayer.settings.add_user") : get("multiplayer.settings.edit_user"));
			initDialog();
			if (user != null) {
				this.user = user;
				loadValues();
			} else {
				this.user = new MultiplayerUser();
			}
		}
		/** Initialize the dialog. */
		private void initDialog() {
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			setModal(true);
			
			final JLabel userType = createLabel("multiplayer.settings.user_type");
			userTypeBox = new JComboBox<String>();
			userTypeBox.setFont(fontMedium);
			userTypeBox.addItem(get("multiplayer.settings.client_user"));
			for (SkirmishAIMode m : SkirmishAIMode.values()) {
				userTypeBox.addItem(get("skirmish.ai." + m + ".tooltip"));
			}

			final JLabel userNameLabel = createLabel("multiplayer.settings.client_name");
			
			userName = new JTextField();
			
			final JLabel userPassphraseLabel = createLabel("multiplayer.settings.client_passphrase");
			
			userPassphrase = new JTextField();
			
			JLabel empireRaceLabel = createLabel("multiplayer.settings.empire_race");
			empireRace = new JComboBox<String>();
			empireRace.setFont(fontMedium);
			
			templatePlayers = U.newArrayList(SkirmishScreen.getPlayersFrom(rl, def));
//			Collections.sort(templatePlayers, new Comparator<SkirmishPlayer>() {
//				@Override
//				public int compare(SkirmishPlayer o1, SkirmishPlayer o2) {
//					return o1.description.compareToIgnoreCase(o2.description);
//				}
//			});
			for (SkirmishPlayer p : templatePlayers) {
				empireRace.addItem(p.description + " = " + p.race);
			}
			changeRace = createCheckBox("multiplayer.settings.client_race"); 

			JLabel iconsLabel = createLabel("multiplayer.settings.icons");
			icons = new JComboBox<ImageIcon>();
			icons.setBackground(Color.BLACK);
			iconNames = new ArrayList<String>();
//			icons.addItem(null);
//			iconNames.add(null);
			
			for (ResourcePlace rp : rl.list(rl.language, "starmap/fleets")) {
				if (rp.type() == ResourceType.IMAGE) {
					String imgRef = rp.getName();
					iconNames.add(imgRef);
					icons.addItem(new ImageIcon(rl.getImage(imgRef)));
				}
			}
			
			changeIcon = createCheckBox("multiplayer.settings.client_icons"); 
			
			IGButton okayPlayer = new IGButton(get("multiplayer.settings.user_edit_ok"));
			okayPlayer.setFont(fontMedium);
			okayPlayer.setForeground(Color.WHITE);
			IGButton cancelPlayer = new IGButton(get("multiplayer.settings.user_edit_cancel"));
			cancelPlayer.setFont(fontMedium);
			cancelPlayer.setForeground(Color.WHITE);
			
			JLabel traitsLabel = createLabel("multiplayer.settings.traits_long");
			JPanel traitsPanel = new JPanel();
			JScrollPane traitsScroll = new JScrollPane(traitsPanel);
			traitsScroll.setPreferredSize(new Dimension(400, 100));
			
			changeTraits = createCheckBox("multiplayer.settings.client_traits");
			
			JLabel groupLabel = createLabel("multiplayer.settings.group");
			group = createSpinner(1, 1, 100, 1);
			
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
					} else {
						userNameLabel.setEnabled(false);
						userName.setEnabled(false);
						userPassphraseLabel.setEnabled(false);
						userPassphrase.setEnabled(false);
						changeIcon.setEnabled(false);
						changeRace.setEnabled(false);
						changeTraits.setEnabled(false);
					}
				}
			});
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
							.addComponent(userName)
							.addComponent(userPassphrase)
							.addComponent(empireRace, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(icons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(traitsScroll)
							.addComponent(group, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
						)
					)
					.addComponent(changeRace)
					.addComponent(changeIcon)
					.addComponent(changeTraits)
				)
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
				)
				.addComponent(changeRace)
				.addGroup(
					gl.createParallelGroup(Alignment.CENTER)
					.addComponent(iconsLabel)
					.addComponent(icons, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addComponent(changeIcon)
				.addGroup(
					gl.createParallelGroup(Alignment.CENTER)
					.addComponent(traitsLabel)
					.addComponent(traitsScroll)
				)
				.addComponent(changeTraits)
				.addGroup(
					gl.createParallelGroup(Alignment.CENTER)
					.addComponent(groupLabel)
					.addComponent(group)
				)
				.addGroup(
					gl.createParallelGroup(Alignment.CENTER)
					.addComponent(okayPlayer)
					.addComponent(cancelPlayer)
				)
			);
			pack();
		}
		/**
		 * Show the dialog and return the user object.
		 * @return the the added/modified
		 */
		public MultiplayerUser showDialog() {
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
			return true;
		}
		/**
		 * Load values from the user object.
		 */
		void loadValues() {
			
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
	}
}
