/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Action0;
import hu.openig.core.ResourceType;
import hu.openig.model.ResourceLocator.ResourcePlace;
import hu.openig.model.Screens;
import hu.openig.render.RenderTools;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The video listing screen.
 * @author akarnokd, 2010.01.15.
 */
public class VideoScreen extends ScreenBase {
	/** The video entry. */
	static class VideoEntry {
		/** The video name. */
		String name;
		/** The video path. */
		String path;
		/** The full video path and name. */
		String fullName;

        @Override
        public String toString() {
            return path + "/" + name;
        }
        
	}
	/** The screen origin. */
	Rectangle origin;
	/** The listing rectangle. */
	final Rectangle listRect = new Rectangle();
	/** Scroll up button. */
	UIImageButton scrollUpButton;
	/** Scroll down button. */
	UIImageButton scrollDownButton;
	/** Statistics label. */
	UIGenericButton playLabel;
	/** Achievements label. */
	UIGenericButton backLabel;
	/** The top index. */
	int videoIndex;
	/** The view count. */
	int videoCount;
	/** The list of videos. */
	final List<VideoEntry> videos = new ArrayList<>();
	/** The selected video. */
	VideoEntry selectedVideo;
	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#doResize()
	 */
	@Override
	public void onResize() {
		RenderTools.centerScreen(origin, getInnerWidth(), getInnerHeight(), true);
		listRect.setBounds(origin.x + 10, origin.y + 20, origin.width - 50, 360);
		videoCount = listRect.height / 20;
		
		scrollUpButton.x = origin.x + listRect.width + 12;
		scrollUpButton.y = origin.y + 10 + (listRect.height / 2 - scrollUpButton.height) / 2;
		
		scrollDownButton.x = scrollUpButton.x;
		scrollDownButton.y = origin.y + 10 + listRect.height / 2 + (listRect.height / 2 - scrollDownButton.height) / 2;

		int w = origin.width / 2;
		
		backLabel.x = origin.x + (w - backLabel.width) / 2;
		backLabel.y = origin.y + origin.height - backLabel.height - 5;
		
		playLabel.x = origin.x + w + (w - playLabel.width) / 2;
		playLabel.y = origin.y + origin.height - playLabel.height - 5;
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#finish()
	 */
	@Override
	public void onFinish() {
	}

	/* (non-Javadoc)
	 * @see hu.openig.v1.ScreenBase#initialize()
	 */
	@Override
	public void onInitialize() {
		origin = new Rectangle(0, 0, 
				commons.common().infoEmptyTop.getWidth(), 
				commons.common().infoEmptyLeft.getHeight());
		 
		scrollUpButton = new UIImageButton(commons.database().arrowUp);
		scrollUpButton.setHoldDelay(100);
		scrollUpButton.onClick = new Action0() {
			@Override
			public void invoke() {
				doScrollUp(1);
			}
		};
		
		scrollDownButton = new UIImageButton(commons.database().arrowDown);
		scrollDownButton.setHoldDelay(100);
		scrollDownButton.onClick = new Action0() {
			@Override
			public void invoke() {
				doScrollDown(1);
			}
		};
		
		playLabel = new UIGenericButton(
				get("videos.play"),
				fontMetrics(16),
				commons.common().mediumButton,
				commons.common().mediumButtonPressed
				);
		playLabel.onClick = new Action0() {
			@Override
			public void invoke() {
				if (selectedVideo != null) {
					commons.control().playVideos(selectedVideo.fullName);
				}
			}
		};
		
		backLabel = new UIGenericButton(
			get("videos.back"),
			fontMetrics(16),
			commons.common().mediumButton,
			commons.common().mediumButtonPressed
		);
		backLabel.onClick = new Action0() {
			@Override
			public void invoke() {
				hideSecondary();
			}
		};

		addThis();
	}

	@Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			hideSecondary();
		}
		return false;
	}

	@Override
	public boolean mouse(UIMouse e) {
		boolean rep = false;
		switch (e.type) {
		case DOWN:
		case DOUBLE_CLICK:
			if (listRect.contains(e.x, e.y)) {
				int idx = (e.y - listRect.y) / 20 + videoIndex;
				if (idx < videos.size()) {
					selectedVideo = videos.get(idx);
					if (e.type == Type.DOUBLE_CLICK) {
						commons.control().playVideos(selectedVideo.fullName);
					}
					rep = true;
				}
			} else {
				rep = super.mouse(e);
			}
			break;
		case WHEEL:
			if (listRect.contains(e.x, e.y)) {
				if (e.z < 0) {
					doScrollUp(3);
				} else {
					doScrollDown(3);
				}
			}
			break;
		default:
			rep = super.mouse(e);
		}
		return rep;
	}

	/** 
	 * Scroll the list up.
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
		scrollUpButton.visible(videoIndex > 0);
		scrollDownButton.visible(videoIndex < videos.size() - videoCount);
		askRepaint();
	}
	@Override
	public void onEnter(Screens mode) {
		videos.clear();
		Map<String, VideoEntry> veMap = new HashMap<>();
		for (String lang : Arrays.asList("generic", commons.config.language)) {
			Map<String, Map<String, ResourcePlace>> map1 = rl.resourceMap.get(ResourceType.VIDEO);
			if (map1 == null) {
				continue;
			}
			Map<String, ResourcePlace> map2 = map1.get(lang);
			if (map2 == null) {
				continue;
			}
			for (ResourcePlace rp : map2.values()) {
				VideoEntry ve = new VideoEntry();
				ve.fullName = rp.getName();
				int idx = rp.getName().lastIndexOf('/');
				ve.name = rp.getName();
				ve.path = "";
				if (idx < 0) {
					ve.name = rp.getName().substring(idx + 1);
					ve.path = rp.getName().substring(idx);
				}
				
				veMap.put(ve.fullName, ve);
			}
		}
		for (String s : commons.profile.unlockedVideos()) {
			VideoEntry ve = veMap.get(s);
			if (ve != null) {
				videos.add(ve);
			}
		}
		onResize();
		adjustScrollButtons();
	}

	@Override
	public void onLeave() {
		// TODO Auto-generated method stub

	}

	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(origin, getInnerWidth(), getInnerHeight(), g2, 0.5f, true);
		
		commons.common().drawInfoEmpty(g2, origin.x, origin.y);
		
		String s = get("videos.unlocked_videos");
		int w = commons.text().getTextWidth(14, s) + 10;
		g2.setColor(Color.BLACK);
		g2.fillRect(origin.x + (origin.width - w) / 2, origin.y - 9, w, 18);
		commons.text().paintTo(g2, origin.x + (origin.width - w) / 2 + 5, origin.y - 7, 14, 0xFFFFCC00, s);
		
		Shape save0 = g2.getClip();
		g2.clipRect(listRect.x, listRect.y, listRect.width, listRect.height);
		
		int y = listRect.y;
		int h = 10;
		for (int i = videoIndex; i < videos.size() && i < videoIndex + videoCount; i++) {
			VideoEntry ve = videos.get(i);
			int color = ve == selectedVideo ? 0xFFFFCC00 : 0xFF80FFFF;
			commons.text().paintTo(g2, listRect.x + 10, y + (20 - h) / 2, h, color, ve.fullName);
			y += 20;
		}
		
		g2.setClip(save0);
		
		super.draw(g2);
	}
	@Override
	public Screens screen() {
		return Screens.VIDEOS;
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
		
	}
}
