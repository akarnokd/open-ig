/*
 * Copyright 2008-2013, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.multiplayer;

import hu.openig.model.Configuration;
import hu.openig.model.ResourceLocator;
import hu.openig.screen.CommonResources;
import hu.openig.ui.IGButton;
import hu.openig.utils.Exceptions;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

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
	private JTabbedPane tabs;
	/** UI component. */
	private JPanel hostGamePanel;
	/** UI component. */
	private JPanel joinGamePanel;
	/** UI component. */
	private JTabbedPane gameOptions;
	/** UI component. */
	private JPanel galaxyPanel;
	/** UI component. */
	private JPanel economyPanel;
	/** UI component. */
	private JPanel playersPlanel;
	/** UI component. */
	private JPanel victoryPanel;
	/** UI component. */
	private JPanel generalPanel;
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
	 * Initialize the GUI elements.
	 */
	private void init() {
		setTitle(get("multiplayer.settings.title"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel basePanel = new JPanel();
		
		tabs = new JTabbedPane();
		
		hostGamePanel = new JPanel();
		joinGamePanel = new JPanel();
		
		tabs.addTab(get("multiplayer.settings.host_game"), hostGamePanel);
		tabs.addTab(get("multiplayer.settings.join_game"), joinGamePanel);
		
		gameOptions = new JTabbedPane();
		
		generalPanel = new JPanel();
		
		galaxyPanel = new JPanel();
		economyPanel = new JPanel();
		playersPlanel = new JPanel();
		victoryPanel = new JPanel();
		
		gameOptions.addTab(get("multiplayer.settings.general"), generalPanel);
		gameOptions.addTab(get("multiplayer.settings.galaxy"), galaxyPanel);
		gameOptions.addTab(get("multiplayer.settings.economy"), economyPanel);
		gameOptions.addTab(get("multiplayer.settings.players"), playersPlanel);
		gameOptions.addTab(get("multiplayer.settings.victory"), victoryPanel);
		
		IGButton cancel = new IGButton(get("multiplayer.settings.back"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doCancel();
			}
		});
		
		initHost();
		initJoin();
		
		initGeneral();
		
		GroupLayout gl = new GroupLayout(basePanel);
		gl.setAutoCreateGaps(true);
		basePanel.setLayout(gl);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(tabs)
			.addComponent(gameOptions)
			.addComponent(cancel)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(tabs, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(gameOptions)
			.addComponent(cancel)
		);
		
		basePanel.setPreferredSize(new Dimension(640, 480));
		setLayout(new BorderLayout());
		add(basePanel, BorderLayout.CENTER);
		
		pack();
	}
	/**
	 * Initialize the host panel.
	 */
	private void initHost() {
		JLabel localAddress = new JLabel(get("multiplayer.settings.local_address"));
		JComboBox<String> localAddressBox = new JComboBox<String>();
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
		
		JLabel localPort = new JLabel(get("multiplayer.settings.local_port"));
		JComboBox<String> localPortBox = new JComboBox<String>();
		localPortBox.setEditable(true);
		for (String port : config.serverPorts) {
			localPortBox.addItem(port);
		}
		if (config.lastServerPort != null && !config.lastServerPort.isEmpty()) {
			localPortBox.setSelectedItem(config.lastServerPort);
		}
		
		JCheckBox upnp = new JCheckBox(get("multiplayer.settings.upnp"));
		upnp.setSelected(config.upnp);
		
		IGButton upnpSettings = new IGButton(get("multiplayer.settings.upnp_settings"));
		upnpSettings.setEnabled(false);
		
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
				.addComponent(upnp)
				.addComponent(upnpSettings)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(localAddress)
				.addComponent(localAddressBox)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
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
		JLabel remoteAddress = new JLabel(get("multiplayer.settings.remote_address"));
		JComboBox<String> remoteAddressBox = new JComboBox<String>();
		remoteAddressBox.setEditable(true);
		for (String s : config.clientAddresses) {
			remoteAddressBox.addItem(s);
		}
		if (config.lastClientAddress != null && !config.lastClientAddress.isEmpty()) {
			remoteAddressBox.setSelectedItem(config.lastClientAddress);
		}
		
		JLabel remotePort = new JLabel(get("multiplayer.settings.remote_port"));
		JComboBox<String> remotePortBox = new JComboBox<String>();
		remotePortBox.setEditable(true);
		for (String s : config.clientPorts) {
			remotePortBox.addItem(s);
		}
		if (config.lastClientPort != null && !config.lastClientPort.isEmpty()) {
			remotePortBox.setSelectedItem(config.lastClientPort);
		}

		IGButton connect = new IGButton(get("multiplayer.settings.connect"));
		
		GroupLayout gl = new GroupLayout(joinGamePanel);
		joinGamePanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(remoteAddress)
			.addComponent(remoteAddressBox)
			.addComponent(remotePort)
			.addComponent(remotePortBox, 100, 100, 100)
			.addComponent(connect)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup(Alignment.BASELINE)
			.addComponent(remoteAddress)
			.addComponent(remoteAddressBox)
			.addComponent(remotePort)
			.addComponent(remotePortBox)
			.addComponent(connect)
		);
	}
	/**
	 * Initialize the general panel.
	 */
	private void initGeneral() {
		
	}
	/** Close the panel. */
	void doCancel() {
		dispose();
	}
}
