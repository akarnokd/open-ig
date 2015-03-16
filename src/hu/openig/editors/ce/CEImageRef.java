/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Action0;
import hu.openig.utils.U;

import java.awt.image.BufferedImage;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

/**
 * Container for an image, valid, label and path. 
 * @author akarnokd, 2012.11.02.
 */
public class CEImageRef implements CEInvalid {
	/** The image. */
	public CEImage image = new CEImage();
	/** Is it valid? */
	public JLabel valid = new JLabel();
	/** The label. */
	public JLabel label = new JLabel();
	/** The path. */
	public JTextField path = new JTextField();
	/** The background worker. */
	SwingWorker<BufferedImage, Void> worker;
	/**
	 * Constructor. Sets the label text.
	 * @param displayText the label's text
	 */
	public CEImageRef(String displayText) {
		label.setText(displayText);
		path.setEditable(false);
	}
	/**
	 * Try loading the given resource.
	 * @param resource the resource with extension
	 * @param context the context
	 * @param complete the completion handler
	 */
	public void setImage(final String resource, final CEContext context, final Action0 complete) {
		if (worker != null) {
			worker.cancel(true);
			worker = null;
		}
		clear();
		worker = new SwingWorker<BufferedImage, Void>() {
			@Override
			protected BufferedImage doInBackground() throws Exception {
				return context.dataManager().getImage(resource);
			}
			@Override
			protected void done() {
				path.setText(resource);
				valid.setIcon(null);
				valid.setToolTipText(null);
				worker = null;
				try {
					BufferedImage ico = get();
					if (ico != null) {
						image.setIcon(ico);
					} else {
						valid.setIcon(context.getIcon(CESeverityIndicator.ERROR));
						valid.setToolTipText(context.get("missing_image"));
					}
				} catch (ExecutionException | InterruptedException | CancellationException ex) {
					valid.setIcon(context.getIcon(CESeverityIndicator.ERROR));
					valid.setToolTipText("<html><pre>" + U.stacktrace(ex.getCause()));
				}
				if (complete != null) { 
					complete.invoke();
				}
			}
		};
		worker.execute();
	}
	/**
	 * Clear the fields.
	 */
	public void clear() {
		path.setText("");
		valid.setIcon(null);
		image.setIcon(null);
	}
	/**
	 * Clear the fields.
	 * @param errorIcon the icon
	 */
	public void error(ImageIcon errorIcon) {
		path.setText("");
		valid.setIcon(errorIcon);
		image.setIcon(null);
	}
	@Override
	public ImageIcon getInvalid() {
		return (ImageIcon)valid.getIcon();
	}
	@Override
	public void clearInvalid() {
		valid.setIcon(null);
		valid.setToolTipText(null);
	}
	@Override
	public void setInvalid(ImageIcon icon, String errorText) {
		valid.setIcon(icon);
		valid.setToolTipText(errorText);
	}
}
