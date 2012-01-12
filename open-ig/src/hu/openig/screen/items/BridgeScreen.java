/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;


import hu.openig.core.Action0;
import hu.openig.core.Action1;
import hu.openig.core.ResourceLocator.ResourcePlace;
import hu.openig.core.ResourceType;
import hu.openig.core.SwappableRenderer;
import hu.openig.model.Level;
import hu.openig.model.Screens;
import hu.openig.model.SoundType;
import hu.openig.model.VideoMessage;
import hu.openig.model.WalkPosition;
import hu.openig.model.WalkTransition;
import hu.openig.render.TextRenderer;
import hu.openig.screen.MediaPlayer;
import hu.openig.screen.ScreenBase;
import hu.openig.screen.VideoRenderer;
import hu.openig.ui.UIImageButton;
import hu.openig.ui.UIImageToggleButton;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;
import hu.openig.utils.U;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.SwingWorker;
import javax.swing.Timer;

/**
 * The bridge rendering screen.
 * @author akarnokd, 2010.01.11.
 */
public class BridgeScreen extends ScreenBase {
	/** The screen origins. */
	final Rectangle base = new Rectangle();
	/** The message panel open rectangle. */
	final Rectangle messageOpenRect = new Rectangle();
	/** The message list rectangle. */
	final Rectangle messageListRect = new Rectangle();
	/** The projector rectangle. */
	final Rectangle projectorRect = new Rectangle();
	/** The video rectangle. */
	final Rectangle videoRect = new Rectangle();
	/** The message rectangle. */
	final Rectangle messageRect = new Rectangle();
	/** The poligin to click on to close the projector. */
	Polygon closeProjector;
	/** The poligon to click on to close the message panel. */
	Polygon closeMessage;
	/** The message front buffer. */
	BufferedImage messageFront;
	/** The message back buffer. */
	BufferedImage messageBack;
	/** The message lock. */
	final Lock messageLock = new ReentrantLock();
	/** The projector front buffer. */
	BufferedImage projectorFront;
	/** The projector back buffer. */
	BufferedImage projectorBack;
	/** The projector lock. */
	final Lock projectorLock = new ReentrantLock();
	/** The video front buffer. */
	BufferedImage videoFront;
	/** The video back buffer. */
	BufferedImage videoBack;
	/** The video lock. */
	final Lock videoLock = new ReentrantLock();
	/** The video subtitle. */
	String videoSubtitle;
	/** The message panel video animator. */
	volatile MediaPlayer messageAnim;
	/** The projector animator. */
	volatile MediaPlayer projectorAnim;
	/** The video animator. */
	volatile MediaPlayer videoAnim;
	/** Is the message panel open? */
	boolean messageOpen;
	/** The message is closing. */
	boolean messageClosing;
	/** Is the projector open? */
	boolean projectorOpen;
	/** The projector is closing. */
	boolean projectorClosing;
	/** The opening/closing animation is in progress. */
	boolean openCloseAnimating;
	/** The transition the mouse is pointing at. */
	WalkTransition pointerTransition;
	/** The video appear animation (the first frame). */
	BufferedImage videoAppear;
	/** The video appearance timer. */
	Timer videoAppearAnim;
	/** The video appearance percentage. */
	public int videoAppearPercent;
	/** The video appearance increment. */
	public int videoAppearIncrement = 10;
	/** The video to play back. */
	public String video;
	/** The action to invoke when the projector reached its end of animation. */
	Action0 onProjectorComplete;
	/** The action to invoke when the projector reached its end of animation. */
	Action0 onMessageComplete;
	/** The action when the message panel appeared. */
	Action0 onAppearComplete;
	/** Is a video running? */
	boolean videoRunning;
	/** The list up button. */
	UIImageButton listUp;
	/** The list down button. */
	UIImageButton listDown;
	/** The list up button. */
	UIImageToggleButton send;
	/** The list down button. */
	UIImageToggleButton receive;
	/** The current graphical list level. */
	int listOffset;
	/** The list of videos. */
	final List<VideoMessageEntry> videos = U.newArrayList();
	/** The currently selected video. */
	String selectedVideoId;
	/** The message list row height. */
	final int rowHeight = 25;
	/** If the video playback completed and the panel is retracted. */
	Action0 onVideoComplete;
	/**
	 * A video message entry.
	 * @author akarnokd, 2012.01.12.
	 */
	public class VideoMessageEntry {
		/** The video message. */
		public VideoMessage videoMessage;
		/**
		 * Draw this entry.
		 * @param g2 the graphics context
		 * @param x0 the base X
		 * @param y0 the base Y
		 */
		public void draw(Graphics2D g2, int x0, int y0) {
			int colorMain = TextRenderer.RED;
			int colorSub = TextRenderer.GREEN;
			if (videoMessage.seen) {
				colorMain = TextRenderer.GRAY;
				colorSub = TextRenderer.GRAY;
			}
			commons.text().paintTo(g2, x0 + 2, y0 + 2, 10, colorMain, get(videoMessage.title));
			commons.text().paintTo(g2, x0 + 2, y0 + 16, 7, colorSub, get(this.videoMessage.description));
		}
	}
	/** The incoming message list. */
	public final List<VideoMessage> incoming = U.newArrayList();
	/** The outgoing message list. */
	public final List<VideoMessage> outgoing = U.newArrayList();
	@Override
	public void onInitialize() {
		videoAppearAnim = new Timer(50, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doVideoAppear();
			}
		});
	}
	@Override
	public void onFinish() {
		if (messageAnim != null) {
			messageAnim.terminate();
		}
		if (projectorAnim != null) {
			projectorAnim.terminate();
		}
		if (videoAnim != null) {
			videoAnim.terminate();
		}
	}
	/**
	 * Play the video for the. 
	 */
	void playMessageAppear() {
		openCloseAnimating = true;
		messageAnim = new MediaPlayer(commons, commons.world().getCurrentLevel().messageAppear, new SwappableRenderer() {
			@Override
			public BufferedImage getBackbuffer() {
				return messageBack;
			}
			@Override
			public void init(int width, int height) {
				messageFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				messageFront.setAccelerationPriority(0);
				messageBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				messageBack.setAccelerationPriority(0);
			}
			@Override
			public void swap() {
				messageLock.lock();
				try {
					BufferedImage temp = messageFront;
					messageFront = messageBack;
					messageBack = temp;
				} finally {
					messageLock.unlock();
					askRepaint();
				}
			}
		});
		messageAnim.onComplete = new Action0() {
			@Override
			public void invoke() {
				messageOpen = false;
				openCloseAnimating = false;
				if (onAppearComplete != null) {
					onAppearComplete.invoke();
					onAppearComplete = null;
				}
				askRepaint();
			}
		};
		messageAnim.start();
	}
	/**
	 * Play message panel opening.
	 */
	void playMessageOpen() {
		openCloseAnimating = true;
		messageAnim = new MediaPlayer(commons, commons.world().getCurrentLevel().messageOpen, new SwappableRenderer() {
			@Override
			public BufferedImage getBackbuffer() {
				return messageBack;
			}
			@Override
			public void init(int width, int height) {
				messageFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				messageFront.setAccelerationPriority(0);
				messageBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				messageBack.setAccelerationPriority(0);
			}
			@Override
			public void swap() {
				messageLock.lock();
				try {
					BufferedImage temp = messageFront;
					messageFront = messageBack;
					messageBack = temp;
				} finally {
					messageLock.unlock();
					askRepaint();
				}
			}
		});
		messageAnim.onComplete = new Action0() {
			@Override
			public void invoke() {
				messageOpen = true;
				openCloseAnimating = false;
				
				if (onMessageComplete != null) {
					onMessageComplete.invoke();
					onMessageComplete = null;
				}
				askRepaint();
			}
		};
		messageAnim.start();
	}
	/** Play message panel closing. */
	void playMessageClose() {
		openCloseAnimating = true;
		messageClosing = true;
		messageAnim = new MediaPlayer(commons, commons.world().getCurrentLevel().messageClose, new SwappableRenderer() {
			@Override
			public BufferedImage getBackbuffer() {
				return messageBack;
			}
			@Override
			public void init(int width, int height) {
				messageFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				messageFront.setAccelerationPriority(0);
				messageBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				messageBack.setAccelerationPriority(0);
			}
			@Override
			public void swap() {
				messageLock.lock();
				try {
					BufferedImage temp = messageFront;
					messageFront = messageBack;
					messageBack = temp;
				} finally {
					messageLock.unlock();
					askRepaint();
				}
			}
		});
		messageAnim.onComplete = new Action0() {
			@Override
			public void invoke() {
				messageOpen = false;
				messageClosing = false;
				openCloseAnimating = false;
				if (onMessageComplete != null) {
					onMessageComplete.invoke();
					onMessageComplete = null;
				}
				askRepaint();
			}
		};
		messageAnim.start();
	}
	/** Play message panel closing. */
	void playProjectorOpen() {
		openCloseAnimating = true;
		projectorAnim = new MediaPlayer(commons, commons.world().getCurrentLevel().projectorOpen, new SwappableRenderer() {
			@Override
			public BufferedImage getBackbuffer() {
				return projectorBack;
			}
			@Override
			public void init(int width, int height) {
				projectorFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				projectorFront.setAccelerationPriority(0);
				projectorBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				projectorBack.setAccelerationPriority(0);
			}
			@Override
			public void swap() {
				projectorLock.lock();
				try {
					BufferedImage temp = projectorFront;
					projectorFront = projectorBack;
					projectorBack = temp;
				} finally {
					projectorLock.unlock();
					askRepaint();
				}
			}
		});
		projectorAnim.onComplete = new Action0() {
			@Override
			public void invoke() {
				projectorOpen = true;
				openCloseAnimating = false;
				if (onProjectorComplete != null) {
					onProjectorComplete.invoke();
					onProjectorComplete = null;
				}
				askRepaint();
			}
		};
		projectorAnim.start();
	}
	/** Play message panel closing. */
	void playProjectorClose() {
		openCloseAnimating = true;
		projectorClosing = true;
		projectorAnim = new MediaPlayer(commons, commons.world().getCurrentLevel().projectorClose, new SwappableRenderer() {
			@Override
			public BufferedImage getBackbuffer() {
				return projectorBack;
			}
			@Override
			public void init(int width, int height) {
				projectorFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				projectorFront.setAccelerationPriority(0);
				projectorBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				projectorBack.setAccelerationPriority(0);
			}
			@Override
			public void swap() {
				projectorLock.lock();
				try {
					BufferedImage temp = projectorFront;
					projectorFront = projectorBack;
					projectorBack = temp;
				} finally {
					projectorLock.unlock();
					askRepaint();
				}
			}
		});
		projectorAnim.onComplete = new Action0() {
			@Override
			public void invoke() {
				projectorOpen = false;
				openCloseAnimating = false;
				projectorClosing = false;
				if (onProjectorComplete != null) {
					onProjectorComplete.invoke();
					onProjectorComplete = null;
				}
				commons.control().moveMouse();
				askRepaint();
			}
		};
		projectorAnim.start();
	}

	@Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE && videoRunning) {
			videoAnim.stop();
			e.consume();
		}
		return super.keyboard(e);
	}
	
	@Override
	public boolean mouse(UIMouse e) {
		if (messageOpen && !messageClosing) {
			if (listUp.enabled() && listUp.within(e)) {
				return listUp.mouse(e);
			}
			if (listDown.enabled() && listDown.within(e)) {
				return listDown.mouse(e);
			}
			if (send.within(e)) {
				return send.mouse(e);
			}
			if (receive.within(e)) {
				return receive.mouse(e);
			}
			if (e.has(Type.WHEEL)) {
				scrollList(e.z);
				return true;
			}
			if (!videoRunning && !videoAppearAnim.isRunning() && !openCloseAnimating) {
				if (messageListRect.contains(e.x, e.y) && e.has(Type.DOWN)) {
					int idx = (e.y - messageListRect.y) / rowHeight + listOffset;
					if (idx >= 0 && idx < videos.size()) {
						VideoMessageEntry selectedVideo = videos.get(idx);
						selectedVideo.videoMessage.seen = true;
						selectedVideoId = selectedVideo.videoMessage.id;
						playVideo(selectedVideo.videoMessage.media);
					} else {
						selectedVideoId = null;
					}
					return true;
				}
			}
		}
		if (e.type == UIMouse.Type.DOWN) {
			if (!openCloseAnimating) {
				if (videoRunning) {
					videoAnim.stop();
				} else
				if (!messageRect.contains(e.x, e.y) && !videoAppearAnim.isRunning() 
						&& messageOpen
						&& !projectorClosing && !messageClosing) {
					if (projectorOpen) {
						playProjectorClose();
					}
					playMessageClose();
				} else
				if (!messageOpen) {
					if (messageOpenRect.contains(e.x, e.y)) {
						playMessageOpen();
					} else {
						WalkPosition position = ScreenUtils.getWalk("*bridge", world());
						for (WalkTransition wt : position.transitions) {
							if (wt.area.contains(e.x - base.x, e.y - base.y)) {
								ScreenUtils.doTransition(position, wt, commons);
								break;
							}
						}
					}
				}
			}
		} else
		if (e.has(Type.MOVE) || e.has(Type.DRAG) || e.has(Type.ENTER)) {
			WalkTransition prev = pointerTransition;
			pointerTransition = null;
			WalkPosition position = ScreenUtils.getWalk("*bridge", world());
			for (WalkTransition wt : position.transitions) {
				if (wt.area.contains(e.x - base.x, e.y - base.y)) {
					pointerTransition = wt;
					break;
				}
			}
			if (prev != pointerTransition) {
				askRepaint();
			}
		}
		return false;
	}
	/** The level specific background. */
	BufferedImage background;
	@Override
	public void onEnter(Screens mode) {
		background = commons.world().bridge.levels.get(commons.world().level).image;
		
		Level lvl = world().getCurrentLevel();

		listUp = new UIImageButton(lvl.up);
		listUp.setHoldDelay(200);
		listUp.onClick = new Action0() {
			@Override
			public void invoke() {
				scrollList(-1);
				askRepaint();
			}
		};
		listDown = new UIImageButton(lvl.down);
		listDown.setHoldDelay(200);
		listDown.onClick = new Action0() {
			@Override
			public void invoke() {
				scrollList(1);
				askRepaint();
			}
		};
		send = new UIImageToggleButton(lvl.send);
		send.onClick = new Action0() {
			@Override
			public void invoke() {
				send.selected = true;
				receive.selected = false;
				listOffset = 0;
				selectedVideoId = null;
			}
		};
		receive = new UIImageToggleButton(lvl.receive);
		receive.onClick = new Action0() {
			@Override
			public void invoke() {
				send.selected = false;
				receive.selected = true;
				listOffset = 0;
				selectedVideoId = null;
			}
		};
		
		openCloseAnimating = false;
		projectorOpen = false;
		messageOpen = false;
		messageClosing = false;
		projectorClosing = false;
		messageFront = null;
		messageBack = null;
		projectorFront = null;
		projectorBack = null;
		
		send.selected = true;
		receive.selected = false;

		scrollList(0);
		
		onResize();
		
		playMessageAppear();
	}

	@Override
	public void onLeave() {
		if (messageAnim != null) {
			messageAnim.stop();
			messageAnim = null;
			messageFront = null;
			messageBack = null;
		}
		if (projectorAnim != null) {
			projectorAnim.stop();
			projectorAnim = null;
			projectorFront = null;
			projectorBack = null;
		}
		if (videoAnim != null) {
			videoAnim.stop();
			videoAnim = null;
			videoFront = null;
			videoBack = null;
		}
		
		videoAppearAnim.stop();
		videos.clear();
	}
	
	@Override
	public void draw(Graphics2D g2) {
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, getInnerWidth(), getInnerHeight());
		
		g2.drawImage(background, base.x, base.y, null);
		
		
		messageLock.lock();
		try {
			if (messageFront != null) {
				g2.drawImage(messageFront, messageRect.x, messageRect.y, null);
			}
		} finally {
			messageLock.unlock();
		}
		
		projectorLock.lock();
		try {
			if (projectorFront != null) {
				g2.drawImage(projectorFront, projectorRect.x, projectorRect.y, null);
			}
		} finally {
			projectorLock.unlock();
		}

		if (videoAppear != null) {
			if (videoAppearPercent != 200) {
				int p = videoAppearPercent;
				if (videoAppearPercent > 100) {
					p = (200 - videoAppearPercent);
				}
				int h = videoRect.height * p / 100;
				int dy = (videoRect.height - h) / 2;
				g2.drawImage(videoAppear, videoRect.x, videoRect.y + dy, videoRect.width, h, null);
			}
		}
		
		videoLock.lock();
		try {
			if (videoFront != null) {
				g2.drawImage(videoFront, videoRect.x, videoRect.y, videoRect.width, videoRect.height, null);
				if (videoSubtitle != null) {
					paintLabel(g2, base.x, videoRect.y + videoRect.height, base.width);
				}
			}
		} finally {
			videoLock.unlock();
		}
		if (messageOpen && !messageClosing) {
			if (send.selected) {
				prepareSendList();
			} else
			if (receive.selected) {
				prepareReceiveList();
			}
			int rows = messageListRect.height / rowHeight;
			int y = messageListRect.y;
			Shape save0 = g2.getClip();
			g2.clipRect(messageListRect.x, messageListRect.y, messageListRect.width, messageListRect.height);
			
			int maxOffset = Math.max(0, videos.size() - rows);
			listOffset = Math.max(0, Math.min(listOffset, maxOffset));
			
			for (int i = listOffset; i < videos.size(); i++) {
				VideoMessageEntry e = videos.get(i);
				e.draw(g2, messageListRect.x, y);
				if (e.videoMessage.id.equals(selectedVideoId)) {
					g2.setColor(Color.WHITE);
					g2.drawRect(messageListRect.x, y, messageListRect.width - 1, rowHeight);
				}
				y += rowHeight;
			}
			g2.setClip(save0);
			
			if (listOffset > 0) {
				drawComponent(g2, listUp);
			} else {
				g2.drawImage(world().getCurrentLevel().upEmpty, listUp.x, listUp.y, null);
			}
			if (listOffset < maxOffset) {
				drawComponent(g2, listDown);
			} else {
				g2.drawImage(world().getCurrentLevel().downEmpty, listDown.x, listDown.y, null);
			}
			drawComponent(g2, send);
			drawComponent(g2, receive);

		}
		if (!projectorOpen && !messageOpen && pointerTransition != null && !openCloseAnimating) {
			ScreenUtils.drawTransitionLabel(g2, pointerTransition, base, commons);
		}
	}
	/**
	 * Scroll the list by the given amount.
	 * @param delta the delta
	 */
	void scrollList(int delta) {
		int rows = messageListRect.height / rowHeight;
		int maxOffset = Math.max(0, videos.size() - rows);
		listOffset = Math.max(0, Math.min(listOffset + delta, maxOffset));
		listUp.enabled(listOffset > 0);
		listDown.enabled(listOffset < maxOffset);
	}
	@Override
	public void onResize() {
		base.setBounds((getInnerWidth() - 640) / 2, 20 + (getInnerHeight() - 38 - 442) / 2, 640, 442);
		messageOpenRect.setBounds(base.x + 572, base.y + 292, 68, 170);
		projectorRect.setBounds(base.x + (base.width - 524) / 2 - 4, base.y, 524, 258);
		videoRect.setBounds(projectorRect.x + 103, projectorRect.y + 9, 320, 240);
		messageRect.setBounds(base.x + base.width - 298, base.y + base.height - 182, 298, 182);

		messageListRect.setBounds(messageRect.x + 13, messageRect.y + 27, 180, 138);
		
		if (listUp != null) {
			listUp.location(messageRect.x + 231, messageRect.y + 106);
			listDown.location(messageRect.x + 231, messageRect.y + 142);
			send.location(messageRect.x + 220, messageRect.y + 67);
			receive.location(messageRect.x + 220, messageRect.y + 29);
		}
		
		closeProjector = new Polygon(
			new int[] { 
					base.x, projectorRect.x, 
					projectorRect.x, projectorRect.x + projectorRect.width, 
					projectorRect.x + projectorRect.width, base.x + base.width - 1, 
					base.x + base.width - 1, messageRect.x, 
					messageRect.x, base.x
				},
			new int[] { 
					base.y, base.y, 
					projectorRect.y + projectorRect.height, projectorRect.y + projectorRect.height, 
					base.y, base.y, 
					messageRect.y, messageRect.y, 
					base.y + base.height - 1, base.y + base.height - 1
					
				},
			10
		);
		closeMessage = new Polygon(
			new int[] {
				base.x, base.x + base.width - 1, base.x + base.width - 1, messageRect.x,
				messageRect.x, base.x
			},
			new int[] {
				base.y, base.y, messageRect.y, messageRect.y, base.y + base.height - 1, 
				base.y + base.height - 1
			},
			6
		);
	}
	/**
	 * Paint a word-wrapped label.
	 * @param g2 the graphics context.
	 * @param x0 the X coordinate
	 * @param y0 the Y coordinate
	 * @param width the draw width
	 */
	public void paintLabel(Graphics2D g2, int x0, int y0, int width) {
		List<String> lines = new ArrayList<String>();
		int maxWidth = commons.text().wrapText(videoSubtitle, width, 14, lines);
		int y = y0 + 6;
		Composite cp = g2.getComposite();
		g2.setColor(Color.BLACK);
		g2.setComposite(AlphaComposite.SrcOver.derive(0.8f));
		int x1 = x0 + (width - maxWidth) / 2 - 3;
		int y1 = y0 + 3;
		int w1 = maxWidth + 6;
		int h1 = lines.size() * 17 + 3;
		g2.fillRect(x1, y1, w1, h1);
		g2.setComposite(cp);
		for (String s : lines) {
			int tw = commons.text().getTextWidth(14, s);
			int x = (width - tw) / 2;
			commons.text().paintTo(g2, x0 + x, y, 14, TextRenderer.WHITE, s);
			y += 17;
		}
	}
	@Override
	public Screens screen() {
		return Screens.BRIDGE;
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
	}
	/**
	 * Play the specific video.
	 * @param video the video
	 */
	public void playVideo(final String video) {
		final boolean paused = commons.simulation.paused();
		commons.simulation.pause();
		onVideoComplete = new Action0() {
			@Override
			public void invoke() {
				if (!paused) {
					commons.simulation.resume();
				}
			}
		};
		this.video = video;
		final SwingWorker<BufferedImage, Void> sw = new SwingWorker<BufferedImage, Void>() {
			@Override
			protected BufferedImage doInBackground() throws Exception {
				ResourcePlace rp = rl.get(video, ResourceType.VIDEO);
				return VideoRenderer.firstFrame(rp);
			}
		};
		sw.execute();
		if (!projectorOpen) {
			onProjectorComplete = new Action0() {
				@Override
				public void invoke() {
					playVideoAppear(sw);
				}
			};
			playProjectorOpen();
		} else {
			playVideoAppear(sw);
		}
	}
	/**
	 * Start playing the video.
	 * @param sw the worker that will return the first frame.
	 */
	void playVideoAppear(final SwingWorker<BufferedImage, Void> sw) {
		videoAppearPercent = 0;
		try {
			videoAppear = sw.get();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (ExecutionException ex) {
			ex.printStackTrace();
		}
		sound(SoundType.ACKNOWLEDGE_2);
		videoAppearAnim.start();
	}
	/**
	 * Animate the the appearance of the video.
	 */
	void doVideoAppear() {
		final String fVideo = video;
		videoAppearPercent += videoAppearIncrement;
		if (videoAppearPercent == 100) {
			videoAppearAnim.stop();
			videoAnim = new MediaPlayer(commons, video, new SwappableRenderer() {
				@Override
				public BufferedImage getBackbuffer() {
					return videoBack;
				}
				@Override
				public void init(int width, int height) {
					videoFront = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					videoFront.setAccelerationPriority(0);
					videoBack = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					videoBack.setAccelerationPriority(0);
				}
				@Override
				public void swap() {
					videoLock.lock();
					try {
						BufferedImage temp = videoFront;
						videoFront = videoBack;
						videoBack = temp;
					} finally {
						videoLock.unlock();
						askRepaint();
					}
				}
			});
			videoAnim.onLabel = new Action1<String>() {
				@Override
				public void invoke(String value) {
					videoSubtitle = value;
				}
			};
			videoAnim.onComplete = new Action0() {
				@Override
				public void invoke() {
					videoRunning = false;
					videoAppear = videoFront;
					videoFront = null;
					videoAppearAnim.start();
					sound(SoundType.ACKNOWLEDGE_2);
					videoSubtitle = null;
				}
			};
			videoRunning = true;
			videoAnim.start();
		} else
		if (videoAppearPercent == 200) {
			videoAppearAnim.stop();
			videoAppear = null;
			if (onVideoComplete != null) {
				onVideoComplete.invoke();
				onVideoComplete = null;
			}
			world().scripting.onMessageSeen(fVideo);
		}
		askRepaint();
	}
	/**
	 * Prepare the send list.
	 */
	void prepareSendList() {
		// TODO implement
		prepareList(world().scripting.getSendMessages());
	}
	/** Prepare the receive list. */
	void prepareReceiveList() {
		// TODO implement
		prepareList(world().scripting.getReceiveMessages());
	}
	/**
	 * Prepare the video listings.
	 * @param available the available messages
	 */
	void prepareList(List<VideoMessage> available) {
		videos.clear();
		for (VideoMessage msg : available) {
			VideoMessageEntry e = new VideoMessageEntry();
			e.videoMessage = msg;
			videos.add(e);
		}
	}
}
