/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.launcher;

import hu.openig.ui.IGButton;
import hu.openig.utils.Exceptions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

/**
 * The language packs manager dialog.
 * @author akarnokd, 2012.09.20.
 */
public class LanguagePacksDialog extends JDialog {
	/** */
	private static final long serialVersionUID = 5480442874979125136L;
	/** User pressed OK? */
	public boolean approved;
	/** Uninstall checkbox. */
	private final JCheckBox cbUninstall;
	/** Install checkbox. */
	private final JCheckBox cbInstall;
	/** Language checkboxes. */
	private final List<JCheckBox> checkBoxes;
	/** Language text per checkbox. */
	private final List<String> languages;
	/**
	 * Constructs the dialog.
	 * @param parent the parent frame
	 * @param lbl the label manager
	 * @param styles the styles
	 * @param flags the flag map
	 * @param files the game files
	 * @param installDir the installation directory
	 * @param isInstall called from install?
	 */
	public LanguagePacksDialog(JFrame parent, 
			LauncherLabels lbl, 
			LauncherStyles styles,
			Map<String, BufferedImage> flags, 
			List<LFile> files,
			File installDir,
			boolean isInstall) {
		super(parent);
		setTitle(lbl.label("Select language packs to install"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		IGButton ok = new IGButton(lbl.label("OK"));
		ok.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				approved = true;
				dispose();
			}
		});
		IGButton cancel = new IGButton(lbl.label("Cancel"));
		cancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		JPanel p = new JPanel();
		GroupLayout gl = new GroupLayout(p);
		p.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		// create language lines
		SequentialGroup vert = gl.createSequentialGroup();
		
		ParallelGroup pg1 = gl.createParallelGroup();
		ParallelGroup pg2 = gl.createParallelGroup();
		ParallelGroup pg3 = gl.createParallelGroup();

		final JLabel sum = new JLabel();
		sum.setHorizontalAlignment(JLabel.RIGHT);
		
		checkBoxes = new ArrayList<>();
		final List<Long> szs = new ArrayList<>();
		languages = new ArrayList<>();
		long sz0 = 0;
		for (Map.Entry<String, BufferedImage> lng : flags.entrySet()) {
			
			ParallelGroup pg0 = gl.createParallelGroup(Alignment.CENTER);
			vert.addGroup(pg0);
			
			JLabel  flag = new JLabel(new ImageIcon(lng.getValue()));
			
			JCheckBox cb = new JCheckBox(lbl.label("language-" + lng.getKey()));
			cb.setOpaque(false);
			
			JLabel sizes = new JLabel();
			sizes.setHorizontalAlignment(JLabel.RIGHT);
			
			pg0.addComponent(flag).addComponent(cb).addComponent(sizes);
			
			pg1.addComponent(flag);
			pg2.addComponent(cb, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE);
			pg3.addComponent(sizes);
			
			long sz = 0;
			for (LFile f : files) {
				if (f.language.equals(lng.getKey())) {
					sz += f.size;
					try {
						File f2 = new File(installDir, f.name());
						if (f2.canRead()) {
							cb.setSelected(true);
							sz0 += f.size;
						}
					} catch (MalformedURLException ex) {
						Exceptions.add(ex);
					}
				}
			}
			sizes.setText(String.format("%,.1f MB", sz / 1024d / 1024d));
			
			checkBoxes.add(cb);
			szs.add(sz);
			languages.add(lng.getKey());
		}
		
		for (JCheckBox cb : checkBoxes) {
			cb.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					long sz = 0;
					int i = 0;
					for (JCheckBox cb2 : checkBoxes) {
						if (cb2.isSelected()) {
							sz += szs.get(i);
						}
						i++;
					}
					
					sum.setText(String.format("%,.1f MB", sz / 1024d / 1024d));
				}
			});
		}

		sum.setText(String.format("%,.1f MB", sz0 / 1024d / 1024d));
		
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		JSeparator sep2 = new JSeparator(JSeparator.HORIZONTAL);
		JSeparator sep3 = new JSeparator(JSeparator.HORIZONTAL);
		
		cbUninstall = new JCheckBox(lbl.label("Remove files of unselected packages"));
		cbInstall = new JCheckBox(lbl.label("Install new packages now"));
		
		cbUninstall.setVisible(!isInstall);
		cbUninstall.setOpaque(false);
		cbInstall.setVisible(!isInstall);
		cbInstall.setOpaque(false);
		sep3.setVisible(!isInstall);
		// ------------------------------------------
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createParallelGroup(Alignment.LEADING)
				.addGroup(
					gl.createParallelGroup(Alignment.TRAILING)
					.addGroup(
						gl.createSequentialGroup()
						.addGroup(pg1)
						.addGroup(pg2)
						.addGroup(pg3)
					)
					.addComponent(sep)
					.addComponent(sum, 200, 200, 200)
				)
				.addComponent(sep2)
				.addComponent(cbInstall)
				.addComponent(cbUninstall)
			)
			.addComponent(sep3)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(ok)
				.addComponent(cancel)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(vert)
			.addComponent(sep)
			.addComponent(sum)
			.addComponent(sep2)
			.addComponent(cbInstall)
			.addComponent(cbUninstall)
			.addComponent(sep3)
			.addGap(5)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(ok)
				.addComponent(cancel)
			)
		);

		gl.linkSize(SwingConstants.HORIZONTAL, ok, cancel);

		for (Component cmp : p.getComponents()) {
			if ((cmp instanceof IGButton) 
					|| (cmp instanceof JLabel)
					|| (cmp instanceof JCheckBox)) {
				cmp.setFont(styles.fontMedium());
				cmp.setForeground(styles.foreground());
			}
		}
		p.setBackground(styles.backgroundColor());
		
		Container c = getContentPane();
		c.add(p, BorderLayout.CENTER);

		setModal(true);
		setResizable(false);
	}
	/**
	 * Display the dialog.
	 * @param parent the parent
	 * @return true if user clicked ok
	 */
	public boolean display(JFrame parent) {
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
		return approved;
	}
	/** @return install checkbox checked? */
	public boolean doInstall() {
		return cbInstall.isSelected();
	}
	/** @return uninstall checkbox checked? */
	public boolean doUninstall() {
		return cbUninstall.isSelected();
	}
	/**
	 * @param selected the selected or unselected languages
	 * @return The selected language codes.
	 */
	public Collection<String> getLanguages(boolean selected) {
		List<String> result = new ArrayList<>();
		int i = 0;
		for (JCheckBox cb : checkBoxes) {
			if (cb.isSelected() == selected) {
				result.add(languages.get(i));
			}
			i++;
		}
		return result;
	}
}
