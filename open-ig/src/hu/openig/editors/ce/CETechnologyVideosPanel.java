/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Action0;
import hu.openig.core.Action1;

import java.awt.BorderLayout;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * The technology videos.
 * @author akarnokd, 2012.11.03.
 */
public class CETechnologyVideosPanel extends CESlavePanel {
	/** */
	private static final long serialVersionUID = 5330630746779319543L;
	/** The video field. */
	CEValueBox<JTextField> videoField;
	/** The normal video. */
	CEVideoRef normalVideo;
	/** The wired video. */
	CEVideoRef wiredVideo;
	/**
	 * Constructor. Initializes the GUI.
	 * @param context the context
	 */
	public CETechnologyVideosPanel(CEContext context) {
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
		
		videoField = CEValueBox.of(get("tech.video"), new JTextField());
		
		normalVideo = new CEVideoRef(get("tech.video.normal"));
		wiredVideo = new CEVideoRef(get("tech.video.wired"));
		
		addValidator(videoField, new Action1<Object>() {
			@Override
			public void invoke(Object value) {
				setVideos();
			}
		});
		
		// --------------------------------------------------
		
		int imageSize = 100;
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addComponent(videoField)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(normalVideo.image, imageSize, imageSize, imageSize)
				.addComponent(normalVideo.valid)
				.addComponent(normalVideo.label)
				.addComponent(normalVideo.path)
				.addGap(30)
				.addComponent(wiredVideo.image, imageSize, imageSize, imageSize)
				.addComponent(wiredVideo.valid)
				.addComponent(wiredVideo.label)
				.addComponent(wiredVideo.path)
			)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(videoField)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(normalVideo.image, imageSize, imageSize, imageSize)
				.addComponent(normalVideo.valid)
				.addComponent(normalVideo.label)
				.addComponent(normalVideo.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(wiredVideo.image, imageSize, imageSize, imageSize)
				.addComponent(wiredVideo.valid)
				.addComponent(wiredVideo.label)
				.addComponent(wiredVideo.path, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
		);
		
		JScrollPane sp = new JScrollPane(panel);

		sp.getVerticalScrollBar().setUnitIncrement(30);
		sp.getVerticalScrollBar().setBlockIncrement(90);

		setLayout(new BorderLayout());
		add(sp, BorderLayout.CENTER);
	}
	/** Set the video images. */
	void setVideos() {
		String videoBase = videoField.component.getText();
		if (master != null) {
			master.set("video", videoBase);
		}
		if (videoBase != null && !videoBase.isEmpty()) {
			Action0 act = new Action0() {
				@Override
				public void invoke() {
					validateVideos();
				}
			};
			
			normalVideo.setVideo(videoBase + ".ani.gz", context, act);
			wiredVideo.setVideo(videoBase + "_wired.ani.gz", context, act);
		} else {
			normalVideo.error(errorIcon);
			wiredVideo.error(errorIcon);
			validateVideos();
		}
	}
	/** Validate the video fields. */
	void validateVideos() {
		ImageIcon invalidIcon = videoField.getInvalid();
		invalidIcon = max(invalidIcon, normalVideo.getInvalid());
		
		ImageIcon wi = wiredVideo.getInvalid();
		if ("0".equals(master.get("level", "")) && wi != null) {
			wi = warningIcon;
		}
		invalidIcon = max(invalidIcon, wi);
		onValidate(invalidIcon);
	}
	@Override
	public void onMasterChanged() {
		if (master != null) {
			setTextAndEnabled(videoField, master, "video", true);
		} else {
			setTextAndEnabled(videoField, null, "", false);
			normalVideo.clear();
			wiredVideo.clear();
		}
	}
}
