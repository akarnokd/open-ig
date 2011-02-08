/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screens;

import hu.openig.core.Act;
import hu.openig.core.Button;
import hu.openig.core.ResourceType;
import hu.openig.core.ResourceLocator.ResourcePlace;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Timer;

/**
 * The video listing screen.
 * @author karnok, 2010.01.15.
 * @version $Revision 1.0$
 */
public class VideoScreen extends ScreenBase {
	/** The video entry. */
	class VideoEntry {
		/** The video name. */
		String name;
		/** The video path. */
		String path;
		/** The full video path and name. */
		String fullName;
	}
	/** The screen origin. */
	final Rectangle origin = new Rectangle();
	/** The listing rectangle. */
	final Rectangle listRect = new Rectangle();
	/** Scroll up button. */
	ImageButton scrollUpButton;
	/** Scroll down button. */
	ImageButton scrollDownButton;
	/** Scroll buttons. */
	final List<Button> buttons = new ArrayList<Button>();
	/** The timer for the continuous scroll down. */
	Timer scrollDownTimer;
	/** The timer for the continuous scroll up. */
	Timer scrollUpTimer;
	/** Statistics label. */
	ClickLabel playLabel;
	/** Achievements label. */
	ClickLabel backLabel;
	/** The top index. */
	int videoIndex;
	/** The view count. */
	int videoCount;
	/** The list of videos. */
	final List<VideoEntry> videos = new ArrayList<VideoEntry>();
	/** The selected video. */
	VideoEntry selectedVideo;
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#doResize()
	 */
	@Override
	public void doResize() {
		origin.setBounds(
			(parent.getWidth() - commons.infoEmpty.getWidth()) / 2,
			20 + (parent.getHeight() - commons.infoEmpty.getHeight() - 38) / 2,
			commons.infoEmpty.getWidth(),
			commons.infoEmpty.getHeight()
		);
		listRect.setBounds(origin.x + 10, origin.y + 20, origin.width - 50, 360);
		videoCount = listRect.height / 20;
		
		scrollUpButton.x = listRect.width + 12;
		scrollUpButton.y = 10 + (listRect.height / 2 - scrollUpButton.normalImage.getHeight()) / 2;
		
		scrollDownButton.x = scrollUpButton.x;
		scrollDownButton.y = 10 + listRect.height / 2 + (listRect.height / 2 - scrollDownButton.normalImage.getHeight()) / 2;

		int w = origin.width / 2;
		
		playLabel.x = (w - playLabel.getWidth()) / 2;
		playLabel.y = origin.height - 30;
		
		backLabel.x = w + (w - backLabel.getWidth()) / 2;
		backLabel.y = origin.height - 30;
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#finish()
	 */
	@Override
	public void finish() {
		scrollDownTimer.stop();
		scrollUpTimer.stop();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#initialize()
	 */
	@Override
	public void initialize() {
		buttons.clear();
		
		scrollDownTimer = new Timer(500, new Act() {
			@Override
			public void act() {
				doScrollDown(1);
			}
		});
		scrollDownTimer.setDelay(50);
		scrollUpTimer = new Timer(500, new Act() {
			@Override
			public void act() {
				doScrollUp(1);
			}
		});
		scrollUpTimer.setDelay(50);
		
		scrollUpButton = new ImageButton();
		scrollUpButton.normalImage = commons.database.arrowUp[0];
		scrollUpButton.selectedImage = commons.database.arrowUp[1];
		scrollUpButton.pressedImage = commons.database.arrowUp[2];
		
		scrollUpButton.onPress = new Act() {
			@Override
			public void act() {
				doScrollUp(1);
				scrollUpTimer.start();
			}
		};
		scrollUpButton.onLeave = new Act() {
			@Override
			public void act() {
				scrollUpTimer.stop();
			}
		};
		scrollUpButton.onRelease = new Act() {
			@Override
			public void act() {
				scrollUpTimer.stop();
			}
		};
		
		scrollDownButton = new ImageButton();
		scrollDownButton.normalImage = commons.database.arrowDown[0];
		scrollDownButton.selectedImage = commons.database.arrowDown[1];
		scrollDownButton.pressedImage = commons.database.arrowDown[2];
		scrollDownButton.onPress = new Act() {
			@Override
			public void act() {
				doScrollDown(1);
				scrollDownTimer.start();
			}
		};
		scrollDownButton.onLeave = new Act() {
			@Override
			public void act() {
				scrollDownTimer.stop();
			}
		};
		scrollDownButton.onRelease = new Act() {
			@Override
			public void act() {
				scrollDownTimer.stop();
			}
		};
		
		playLabel = new ClickLabel();
		playLabel.visible = true;
		playLabel.commons = commons;
		playLabel.size = 20;
		playLabel.label = "videos.play";
		playLabel.onLeave = new Act() { @Override public void act() { playLabel.selected = false; } };
		playLabel.onReleased = new Act() {
			@Override
			public void act() {
				playLabel.selected = false;
				if (selectedVideo != null) {
					commons.control.playVideos(selectedVideo.fullName);
				}
			}
		};
		playLabel.onPressed = new Act() {
			@Override
			public void act() {
				playLabel.selected = true;
			}
		};
		
		backLabel = new ClickLabel();
		backLabel.visible = true;
		backLabel.commons = commons;
		backLabel.size = 20;
		backLabel.label = "videos.back";
		backLabel.onLeave = new Act() { @Override public void act() { backLabel.selected = false; } };
		backLabel.onReleased = new Act() {
			@Override
			public void act() {
				backLabel.selected = false;
				commons.control.hideSecondary();
			}
		};
		backLabel.onPressed = new Act() {
			@Override
			public void act() {
				backLabel.selected = true;
			}
		};
		
		buttons.add(scrollUpButton);
		buttons.add(scrollDownButton);
		buttons.add(playLabel);
		buttons.add(backLabel);

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#keyTyped(int, int)
	 */
	@Override
	public void keyTyped(int key, int modifiers) {
		if (key == KeyEvent.VK_ESCAPE) {
			playLabel.selected = false;
			backLabel.selected = false;
			commons.control.hideSecondary();
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseMoved(int, int, int, int)
	 */
	@Override
	public void mouseMoved(int button, int x, int y, int modifiers) {
		for (Button btn : buttons) {
			if (btn.test(x, y, origin.x, origin.y)) {
				if (!btn.mouseOver) {
					btn.mouseOver = true;
					btn.onEnter();
					requestRepaint();
				}
			} else
			if (btn.mouseOver || btn.pressed) {
				btn.mouseOver = false;
				btn.pressed = false;
				btn.onLeave();
				requestRepaint();
			}
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mousePressed(int, int, int, int)
	 */
	@Override
	public void mousePressed(int button, int x, int y, int modifiers) {
		for (Button btn : buttons) {
			if (btn.test(x, y, origin.x, origin.y)) {
				btn.pressed = true;
				btn.onPressed();
				requestRepaint();
			}
		}
		if (listRect.contains(x, y)) {
			int idx = (y - listRect.y) / 20 + videoIndex;
			if (idx < videos.size()) {
				selectedVideo = videos.get(idx);
				if (button == 3) {
					commons.control.playVideos(selectedVideo.fullName);
				}
				requestRepaint();
			}
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseReleased(int, int, int, int)
	 */
	@Override
	public void mouseReleased(int button, int x, int y, int modifiers) {
		for (Button btn : buttons) {
			if (btn.pressed) {
				btn.pressed = false;
				if (btn.test(x, y, origin.x, origin.y)) {
					btn.onReleased();
				}
				requestRepaint();
			}
		}
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#mouseScrolled(int, int, int, int)
	 */
	@Override
	public void mouseScrolled(int direction, int x, int y, int modifiers) {
		if (listRect.contains(x, y)) {
			if (direction < 0) {
				doScrollUp(3);
			} else {
				doScrollDown(3);
			}
		}
	}
	/** 
	 * Scoll the list up. 
	 * @param amount the scroll amount
	 */
	void doScrollUp(int amount) {
		int oldIndex = videoIndex;
		videoIndex = Math.max(0, videoIndex - amount);
		if (oldIndex != videoIndex) {
			adjustScrollButtons();
		}
	}
	/** 
	 * Scroll the list down. 
	 * @param amount the scroll amount
	 */
	void doScrollDown(int amount) {
		if (videos.size() > videoCount) {
			int oldIndex = videoIndex;
			videoIndex = Math.min(videoIndex + amount, videos.size() - videoCount);
			if (oldIndex != videoIndex) {
				adjustScrollButtons();
			}
		}
	}
	/** Adjust the visibility of the scroll buttons. */
	void adjustScrollButtons() {
		scrollUpButton.visible = videoIndex > 0;
		scrollDownButton.visible = videoIndex < videos.size() - videoCount;
		if (!scrollUpButton.visible) {
			scrollUpTimer.stop();
		}
		if (!scrollDownButton.visible) {
			scrollDownTimer.stop();
		}
		repaint();
	}
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onEnter()
	 */
	@Override
	public void onEnter() {
		videos.clear();
		for (String lang : Arrays.asList("generic", commons.config.language)) {
			for (ResourcePlace rp : rl.resourceMap.get(ResourceType.VIDEO).get(lang).values()) {
				VideoEntry ve = new VideoEntry();
				ve.fullName = rp.getName();
				int idx = rp.getName().lastIndexOf('/');
				ve.name = rp.getName();
				ve.path = "";
				if (idx < 0) {
					ve.name = rp.getName().substring(idx + 1);
					ve.path = rp.getName().substring(idx);
				}
				videos.add(ve);
			}
		}
		Collections.sort(videos, new Comparator<VideoEntry>() {
			@Override
			public int compare(VideoEntry o1, VideoEntry o2) {
				return o1.fullName.compareTo(o2.fullName);
			}
		});
		onResize();
		adjustScrollButtons();
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#onLeave()
	 */
	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#paintTo(java.awt.Graphics2D)
	 */
	@Override
	public void paintTo(Graphics2D g2) {
		Composite cp = g2.getComposite();
		g2.setComposite(AlphaComposite.SrcOver.derive(0.8f));
		g2.drawImage(commons.infoEmpty, origin.x, origin.y, null);
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, parent.getWidth(), origin.y);
		g2.fillRect(0, origin.y + origin.height, parent.getWidth(), parent.getHeight() - origin.y - origin.height);
		g2.fillRect(0, origin.y, origin.x, origin.height);
		g2.fillRect(origin.x + origin.width, origin.y, parent.getWidth() - origin.width - origin.x, origin.height);
		g2.setComposite(cp);
		
		
		String s = commons.labels.get("videos.all_videos");
		int w = commons.text.getTextWidth(14, s) + 10;
		g2.setColor(Color.BLACK);
		g2.fillRect(origin.x + (origin.width - w) / 2, origin.y - 9, w, 18);
		commons.text.paintTo(g2, origin.x + (origin.width - w) / 2 + 5, origin.y - 7, 14, 0xFFFFCC00, s);
		
		Shape sp = g2.getClip();
		g2.setClip(listRect);
		
		int y = listRect.y;
		int h = 10;
		for (int i = videoIndex; i < videos.size() && i < videoIndex + videoCount; i++) {
			VideoEntry ve = videos.get(i);
			int color = ve == selectedVideo ? 0xFFFFCC00 : 0xFF80FFFF;
			commons.text.paintTo(g2, listRect.x + 10, y + (20 - h) / 2, h, color, ve.fullName);
			y += 20;
		}
		
		g2.setClip(sp);
		
		for (Button btn : buttons) {
			btn.paintTo(g2, origin.x, origin.y);
		}

	}

	@Override
	public void mouseDoubleClicked(int button, int x, int y, int modifiers) {
		// TODO Auto-generated method stub
		
	}
}
