/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.model.APIResult;
import hu.openig.model.Configuration;
import hu.openig.model.MultiplayerDefinition;
import hu.openig.model.ResourceLocator;
import hu.openig.model.WelcomeResponse;
import hu.openig.net.ErrorResponse;
import hu.openig.screen.CommonResources;
import hu.openig.ui.IGButton;
import hu.openig.ui.IGCheckBox;
import hu.openig.utils.Exceptions;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

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
	/** Large font. */
	private Font fontLarge;
	/** Medium font. */
	private Font fontMedium;
	/** UI component. */
	private JComboBox<String> localAddressBox;
	/** The addresses. */
	private final List<InetAddress> localAddresses = new ArrayList<>();
	/** UI component. */
	private JComboBox<String> localPortBox;
	/** UI component. */
	private JCheckBox upnp;
	/** UI component. */
	private JComboBox<String> remoteAddressBox;
	/** UI component. */
	private JComboBox<String> remotePortBox;
	/** UI component. */
	private JTextField joinUserName;
	/** UI component. */
	private JTextField joinPassphrase;
	/** Setup game button. */
	private IGButton setupGame;
	/** Connect to game button. */
	private IGButton connectGame;
	/** UI component. */
	private IGButton openButton;
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
	 * Initialize the GUI elements.
	 */
	private void init() {
		setTitle(get("multiplayer.settings.title"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JPanel basePanel = new JPanel();
		fontLarge = new Font(Font.SANS_SERIF, Font.BOLD, 18);
		fontMedium = new Font(Font.SANS_SERIF, Font.BOLD, 14);
		
		IGButton cancel = new IGButton(get("multiplayer.settings.back"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doCancel();
			}
		});
		cancel.setFont(fontMedium);
		cancel.setForeground(Color.WHITE);
		
		initHost();
		initJoin();
		
		GroupLayout gl = new GroupLayout(basePanel);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		basePanel.setLayout(gl);

		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(hostGamePanel)
			.addComponent(joinGamePanel)
			.addComponent(cancel)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(hostGamePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(joinGamePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(cancel)
		);
		
		setLayout(new BorderLayout());
		add(basePanel, BorderLayout.CENTER);

		pack();
		setMinimumSize(getSize());
	}
	/**
	 * Initialize the host panel.
	 */
	private void initHost() {
		hostGamePanel = new JPanel();
		
		TitledBorder border = new TitledBorder(get("multiplayer.host_game"));
		border.setTitleFont(fontMedium);
		hostGamePanel.setBorder(border);
		
		JLabel localAddress = createLabel("multiplayer.settings.local_address");
		localAddress.setFont(fontMedium);
		localAddressBox = new JComboBox<>();
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
						localAddresses.add(ia);
					}
				}
			}
		} catch (SocketException e) {
			Exceptions.add(e);
		}
		if (config.hostAddress != null && !config.hostAddress.isEmpty()) {
			localAddressBox.setSelectedItem(config.hostAddress);
		}
		
		JLabel localPort = createLabel("multiplayer.settings.local_port");
		localPort.setFont(fontMedium);
		localPortBox = new JComboBox<>();
		localPortBox.setEditable(true);
		localPortBox.setFont(fontMedium);
		for (String port : config.hostPorts) {
			localPortBox.addItem(port);
		}
		if (config.hostPort != null && !config.hostPort.isEmpty()) {
			localPortBox.setSelectedItem(config.hostPort);
		}
		
		upnp = createCheckBox("multiplayer.settings.upnp");
		upnp.setSelected(config.hostUPnP);
		
		IGButton upnpSettings = new IGButton(get("multiplayer.settings.upnp_settings"));
		upnpSettings.setEnabled(false);
		upnpSettings.setFont(fontMedium);
		upnpSettings.setForeground(Color.WHITE);
		
		setupGame = new IGButton(get("multiplayer.settings.setup"));
		setupGame.setFont(fontLarge);
		setupGame.setForeground(Color.WHITE);
		setupGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doSetupGame();
			}
		});

		openButton = new IGButton();
		openButton.setIcon(new ImageIcon(MultiplayerScreen.class.getResource("/hu/openig/editors/res/Open24.gif")));
		openButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doOpen();
			}
		});

		ActionListener localEditAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableSetupGame();
				if (setupGame.isEnabled()) {
					doSetupGame();
				}
			}
		};
		
		DocumentListener localEditListener = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				enableSetupGame();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				enableSetupGame();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				enableSetupGame();
			}
		};

		localAddressBox.addActionListener(localEditAction);
		localPortBox.addActionListener(localEditAction);
		
		((JTextComponent)localAddressBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(localEditListener);
		((JTextComponent)localPortBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(localEditListener);
		
		
		GroupLayout gl = new GroupLayout(hostGamePanel);
		hostGamePanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
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
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(setupGame)
				.addComponent(openButton)
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
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(setupGame)
				.addComponent(openButton)
			)
		);
		enableSetupGame();
	}
	/**
	 * Enable the setup game button if the fields are correctly filled in.
	 */
	void enableSetupGame() {
		boolean e = true;
		e &= localAddressBox.getSelectedIndex() >= 0;
		
		try {
			Integer.parseInt((String)localPortBox.getSelectedItem());
			e &= true;
		} catch (NumberFormatException ex) {
			e &= false;
		}
		setupGame.setEnabled(e);
	}
	/**
	 * Initialize the join panel.
	 */
	private void initJoin() {
		joinGamePanel = new JPanel();
		
		TitledBorder border = new TitledBorder(get("multiplayer.join_game"));
		border.setTitleFont(fontMedium);
		joinGamePanel.setBorder(border);

		JLabel remoteAddress = createLabel("multiplayer.settings.remote_address");
		remoteAddress.setFont(fontMedium);
		remoteAddressBox = new JComboBox<>();
		remoteAddressBox.setEditable(true);
		remoteAddressBox.setFont(fontMedium);
		for (String s : config.joinAddresses) {
			remoteAddressBox.addItem(s);
		}
		if (config.joinAddress != null && !config.joinAddress.isEmpty()) {
			remoteAddressBox.setSelectedItem(config.joinAddress);
		}
		
		JLabel remotePort = createLabel("multiplayer.settings.remote_port");
		remotePort.setFont(fontMedium);
		remotePortBox = new JComboBox<>();
		remotePortBox.setEditable(true);
		remotePortBox.setFont(fontMedium);
		for (String s : config.joinPorts) {
			remotePortBox.addItem(s);
		}
		if (config.joinPort != null && !config.joinPort.isEmpty()) {
			remotePortBox.setSelectedItem(config.joinPort);
		}

		JLabel joinUserNameLabel = createLabel("multiplayer.settings.join_user_name");
		joinUserName = new JTextField(config.joinUserName);
		joinUserName.setFont(fontMedium);
		JLabel joinPassphraseLabel = createLabel("multiplayer.settings.join_passphrase");
		joinPassphrase = new JTextField(config.joinPassphrase);
		joinPassphrase.setFont(fontMedium);
		
		connectGame = new IGButton(get("multiplayer.settings.connect"));
		connectGame.setFont(fontLarge);
		connectGame.setForeground(Color.WHITE);
		connectGame.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doConnectGame();
			}
		});
		
		ActionListener remoteEditAction = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				enableConnectGame();
				if (connectGame.isEnabled()) {
					doConnectGame();
				}
			}
		};
		
		DocumentListener remoteEditListener = new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				enableConnectGame();
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) {
				enableConnectGame();
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) {
				enableConnectGame();
			}
		};
		
		remoteAddressBox.addActionListener(remoteEditAction);
		remotePortBox.addActionListener(remoteEditAction);
		
		((JTextComponent)remoteAddressBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(remoteEditListener);
		((JTextComponent)remotePortBox.getEditor().getEditorComponent()).getDocument().addDocumentListener(remoteEditListener);

		joinUserName.addActionListener(remoteEditAction);
		joinPassphrase.addActionListener(remoteEditAction);
		joinUserName.getDocument().addDocumentListener(remoteEditListener);
		joinPassphrase.getDocument().addDocumentListener(remoteEditListener);
		
		connectGame.setEnabled(false);
		
		GroupLayout gl = new GroupLayout(joinGamePanel);
		joinGamePanel.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
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
				)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(joinUserNameLabel)
					.addComponent(joinUserName)
					.addComponent(joinPassphraseLabel)
					.addComponent(joinPassphrase)
				)
			)
			.addComponent(connectGame)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(remoteAddress)
				.addComponent(remoteAddressBox)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(remotePort)
				.addComponent(remotePortBox)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(joinUserNameLabel)
				.addComponent(joinUserName)
				.addComponent(joinPassphraseLabel)
				.addComponent(joinPassphrase)
			)
			.addComponent(connectGame)
		);
		
		enableConnectGame();
	}
	/**
	 * Check if the fields are setup propely to join a game.
	 */
	void enableConnectGame() {
		boolean e = true;
		e &= remoteAddressBox.getSelectedItem() != null;
		
		if (remotePortBox.getSelectedItem() != null) {
			try {
				Integer.parseInt((String)remotePortBox.getSelectedItem());
				e &= true;
			} catch (NumberFormatException ex) {
				e = false;
			}
		}
		e &= !joinUserName.getText().isEmpty();
		e &= !joinPassphrase.getText().isEmpty();
		
		connectGame.setEnabled(e);
	}
	/** Close the panel. */
	void doCancel() {
		dispose();
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
	/**
	 * Connect to an existing game.
	 */
	void doConnectGame() {
		String ra = (String)remoteAddressBox.getSelectedItem();
		String ps = (String)remotePortBox.getSelectedItem();
		int p = Integer.parseInt(ps);
		
		config.joinAddress = ra;
		config.joinPort = ps;
		
		if (!config.joinAddresses.contains(ra)) {
			config.joinAddresses.add(ra);
		}
		if (!config.joinPorts.contains(ps)) {
			config.joinPorts.add(ps);
		}
		
		config.joinUserName = joinUserName.getText();
		config.joinPassphrase = joinPassphrase.getText();
		
		try {
			commons.multiplayer.startClient(
					InetAddress.getByName(ra),
					p
			);
			commons.multiplayer.remoteAsyncAPI.login(
					joinUserName.getText(),
					joinPassphrase.getText(),
					Configuration.VERSION,
					new APIResult<WelcomeResponse>() {
						@Override
						public void success(WelcomeResponse value) {
							commons.multiplayer.remoteSessionId = value.sessionId;
							doGetGameSettings();
						}
						@Override
						public void error(IOException ex) {
							if (ex instanceof ErrorResponse) {
								errorDialog(((ErrorResponse)ex).getText());
								Exceptions.add(ex);
							} else {
								Exceptions.add(ex);
							}
						}
					}
			);
		} catch (UnknownHostException ex) {
			errorDialog(get("multiplayer.error.unknown_host"));
		} catch (IOException ex) {
			errorDialog(format("multiplayer.error.connect_game", ex.toString()));
		}
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
				MultiplayerSetupScreen mpss = new MultiplayerSetupScreen(commons);
				mpss.loadFromDefinition(value);
				mpss.setLocationRelativeTo(MultiplayerScreen.this);
				dispose();
				mpss.setVisible(true);
			}
			@Override
			public void error(IOException ex) {
				if (ex instanceof ErrorResponse) {
					errorDialog(((ErrorResponse)ex).getText());
					Exceptions.add(ex);
				} else {
					Exceptions.add(ex);
				}
			}
		});
	}
	/**
	 * Setup the game.
	 */
	void doSetupGame() {
		MultiplayerSetupScreen mpss = prepareSetupScreen();
		dispose();
		mpss.setVisible(true);
	}
	/**
	 * @return the prepared but invisible setup screen
	 */
	MultiplayerSetupScreen prepareSetupScreen() {
		InetAddress serverAddress = localAddresses.get(localAddressBox.getSelectedIndex());
		String sp = (String)localPortBox.getSelectedItem();
		int serverPort = Integer.parseInt(sp);
		boolean serverUPnP = upnp.isSelected();

		config.hostAddress = serverAddress.getHostAddress();
		config.hostPort = sp;
		config.hostUPnP = serverUPnP;
		
		if (!config.hostPorts.contains(sp)) {
			config.hostPorts.add(sp);
		}
		
		MultiplayerSetupScreen mpss = new MultiplayerSetupScreen(commons, 
				serverAddress, serverPort, serverUPnP);
		mpss.setLocationRelativeTo(MultiplayerScreen.this);
		return mpss;
	}
	/**
	 * Open an existing multiplayer definition.
	 */
	void doOpen() {
		MultiplayerSetupScreen mpss = prepareSetupScreen();
		if (mpss.doOpen()) {
			dispose();
			mpss.setVisible(true);
		}
	}
}
