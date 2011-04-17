/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.screen.items;

import hu.openig.core.Act;
import hu.openig.core.Difficulty;
import hu.openig.model.Screens;
import hu.openig.render.RenderTools;
import hu.openig.render.TextRenderer;
import hu.openig.screen.ScreenBase;
import hu.openig.ui.UIContainer;
import hu.openig.ui.UIGenericButton;
import hu.openig.ui.UIMouse;
import hu.openig.ui.UIMouse.Type;
import hu.openig.utils.XElement;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.CancellationException;

import javax.swing.SwingWorker;



/**
 * A load and save game screen.
 * @author akarnokd, 2010.01.11.
 */
public class LoadSaveScreen extends ScreenBase {
	/** The panel base rectangle. */
	final Rectangle base = new Rectangle(0, 0, 640, 442);
	/** The random background. */
	final Random rnd = new Random();
	/** The displayed background. */
	BufferedImage background;
	/** The screen to restore when exiting. */
	Screens restore;
	/** Show the settings page? */
	public boolean settingsMode;
	/** Allow saving? */
	public boolean maySave;
	/** Resume the simulation after exiting? */
	boolean resume;
	/** The save button. */
	UIGenericButton save;
	/** The load button. */
	UIGenericButton load;
	/** The settings button. */
	UIGenericButton settings;
	/** The main menu button. */
	UIGenericButton mainmenu;
	/** Delete. */
	UIGenericButton delete;
	/** The file listing worker. */
	SwingWorker<Void, Void> listWorker;
	/** The file list. */
	FileList list;
	@Override
	public void onInitialize() {
		save = new UIGenericButton(get("save"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		save.disabledPattern(commons.common().disabledPattern);
		
		save.onClick = new Act() {
			@Override
			public void act() {
				doSave();
			}
		};
		
		load = new UIGenericButton(get("load"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		load.disabledPattern(commons.common().disabledPattern);
		load.onClick = new Act() {
			@Override
			public void act() {
				doLoad();
			}
		};
		settings = new UIGenericButton(get("settings"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		settings.disabledPattern(commons.common().disabledPattern);
		settings.onClick = new Act() {
			@Override
			public void act() {
				doSettings();
			}
		};
		
		mainmenu = new UIGenericButton(get("mainmenu"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		mainmenu.disabledPattern(commons.common().disabledPattern);
		mainmenu.onClick = new Act() {
			@Override
			public void act() {
				doMainMenu();
			}
		};
		
		
		delete = new UIGenericButton(get("delete"), fontMetrics(16), commons.common().mediumButton, commons.common().mediumButtonPressed);
		delete.disabledPattern(commons.common().disabledPattern);
		delete.onClick = new Act() {
			@Override
			public void act() {
				doDelete();
			}
		};
		
		list = new FileList();

		addThis();
	}

	@Override
	public void onEnter(Screens mode) {
		restore = mode;
		background = commons.background().options[rnd.nextInt(commons.background().options.length)];
		settingsMode = false;
		resume = commons.world() != null && !commons.paused();
		commons.pause();
		commons.nongame = true;
		commons.worldLoading = true;
		list.items.clear();
		list.selected = null;
		startWorker();
	}

	@Override
	public void onLeave() {
		commons.nongame = false;
		commons.worldLoading = false;
		if (listWorker != null) {
			listWorker.cancel(true);
			listWorker = null;
		}
	}

	@Override
	public void onFinish() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResize() {
		RenderTools.centerScreen(base, getInnerWidth(), getInnerHeight(), true);
		
		load.location(base.x + 10, base.y + 10);
		save.location(load.x + load.width + 10, load.y);
		delete.location(save.x + save.width + 30, save.y);
		settings.location(delete.x + delete.width + 30, delete.y);
		mainmenu.location(settings.x + settings.width + 30, settings.y);
		
		list.location(base.x + 10, load.y + load.height + 10);
		list.size(base.width - 20, base.height - list.y + base.y - 10);
	}
	@Override
	public Screens screen() {
		return Screens.LOAD_SAVE;
	}
	@Override
	public void onEndGame() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void draw(Graphics2D g2) {
		RenderTools.darkenAround(base, width, height, g2, 0.5f, true);

		if (settingsMode) {
			g2.drawImage(commons.background().setup, base.x, base.y, null);
			list.visible(false);
			save.visible(false);
			load.visible(false);
			delete.visible(false);
			settings.text(get("load_save"));
		} else {
			g2.drawImage(background, base.x, base.y, null);
			list.visible(true);
			save.visible(true);
			load.visible(true);
			delete.visible(true);
			settings.text(get("settings"));
		}
		
		save.enabled(maySave);
		
		load.enabled(list.selected != null);
		delete.enabled(list.selected != null);
		
		super.draw(g2);
	}
	@Override
	public boolean mouse(UIMouse e) {
		if (e.has(Type.DOWN) && !e.within(base.x, base.y, base.width, base.height)) {
			doBack();
		}
		return super.mouse(e);
	}
	@Override
	public boolean keyboard(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			doBack();
			e.consume();
		} else
		if (e.getKeyChar() == ' ' 
			|| e.getKeyChar() == '1'
			|| e.getKeyChar() == '2'
			|| e.getKeyChar() == '3'
		) {
			e.consume();
		}
		return super.keyboard(e);
	}
	/** Start the list population worker. */
	void startWorker() {
		listWorker = new SwingWorker<Void, Void>() {
			final List<FileItem> flist = new ArrayList<FileItem>();
			@Override
			protected Void doInBackground() throws Exception {
				try {
					File dir = new File("save/" + commons.profile.name);
					if (!dir.exists()) {
						dir.mkdirs();
					}
					File[] files = dir.listFiles(new FilenameFilter() {
						@Override
						public boolean accept(File dir, String name) {
							if (isCancelled()) {
								throw new CancellationException();
							}
							return name.startsWith("save-") && name.endsWith(".xml");
						}
					});
					
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
					dateFormat.setCalendar(new GregorianCalendar(TimeZone.getTimeZone("GMT")));
					
					if (files != null) {
						for (File f : files) {
							if (isCancelled()) {
								break;
							}
							final FileItem fi = new FileItem();
							
							fi.name = f.getName();
							fi.saveDate = new Date(f.lastModified());
							
							XElement w = XElement.parseXML(f.getAbsolutePath());
							
							if (isCancelled()) {
								break;
							}
	
							fi.gameDate = dateFormat.parse(w.get("time"));
							fi.difficulty = Difficulty.valueOf(w.get("difficulty"));
							fi.level = w.getInt("level");
							
							String pid = w.get("player");
							for (XElement pl : w.childrenWithName("player")) {
								if (pid.equals(pl.get("id"))) {
									fi.money = pl.getLong("money");
									break;
								}
							}
							flist.add(fi);
						}
						Collections.sort(flist);
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return null;
			}
			@Override
			protected void done() {
				listWorker = null;
				list.items.addAll(flist);
				askRepaint();
			};
		};
		listWorker.execute();
	}
	/** The file item. */
	class FileItem implements Comparable<FileItem> {
		/** The save name. */
		public String name;
		/** The level. */
		public int level;
		/** The difficulty. */
		public Difficulty difficulty;
		/** The ingame date. */
		public Date gameDate;
		/** The save date. */
		public Date saveDate;
		/** The player money. */
		public long money;
		@Override
		public int compareTo(FileItem o) {
			return o.saveDate.compareTo(saveDate);
		}
	}
	/** The file list. */
	class FileList extends UIContainer {
		/** The scroll top. */
		int top;
		/** The selected row. */
		FileItem selected;
		/** The file items. */
		final List<FileItem> items = new ArrayList<FileItem>();
		/** The row height. */
		int rowHeight = 18;
		/** The text height. */
		int textHeight = 10;
		@Override
		public void draw(Graphics2D g2) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			dateFormat2.setCalendar(new GregorianCalendar(TimeZone.getTimeZone("GMT")));
			
			if (listWorker != null && !listWorker.isDone()) {
				commons.text().paintTo(g2, 0, 0, 14, TextRenderer.GRAY, get("loading") + "...");
			} else {
				Composite save0 = g2.getComposite();
				g2.setComposite(AlphaComposite.SrcOver.derive(0.75f));
				g2.setColor(Color.BLACK);
				g2.fillRect(0, 0, width, height);
				g2.setComposite(save0);

				int rows = height / rowHeight;
				if (top < 0 || items.size() == 0) {
					top = 0;
				}
				if (items.size() == 0) {
					commons.text().paintTo(g2, 0, 0, 14, TextRenderer.GRAY, get("no_saves"));
				} else {
					int y = 0;
					for (int i = top; i < top + rows && i < items.size(); i++) {
						FileItem fi = items.get(i);
						
						int c = TextRenderer.GREEN;
						if (fi == selected) {
							c = TextRenderer.YELLOW;
						}
						
						int dh = (rowHeight - textHeight) / 2;
						
						commons.text().paintTo(g2, 5, y + dh, textHeight, c, dateFormat.format(fi.saveDate));
						commons.text().paintTo(g2, 5 + 200, y + dh, textHeight, c, get(fi.difficulty.label));
						commons.text().paintTo(g2, 5 + 300, y + dh, textHeight, c, "" + fi.level);
						commons.text().paintTo(g2, 5 + 330, y + dh, textHeight, c, dateFormat2.format(fi.gameDate));
						String m = fi.money + " cr";
						commons.text().paintTo(g2, width - 5 - commons.text().getTextWidth(10, m), y + dh, textHeight, c, m);
						y += rowHeight;
					}
				}
			}
		}
		@Override
		public boolean mouse(UIMouse e) {
			if (e.has(Type.DOWN)) {
				int idx = top + e.y / rowHeight;
				if (idx >= 0 && idx < items.size()) {
					selected = items.get(idx);
					return true;
				}
			} else
			if (e.has(Type.DOUBLE_CLICK)) {
				int idx = top + e.y / rowHeight;
				if (idx >= 0 && idx < items.size()) {
					selected = items.get(idx);
					doLoad();
					return true;
				}
			} else
			if (e.has(Type.WHEEL)) {
				if (e.z < 0) {
					doScrollUp();
					doScrollUp();
					doScrollUp();
				} else {
					doScrollDown();
					doScrollDown();
					doScrollDown();
				}
				return true;
			}
			return super.mouse(e);
		}
		/** Scroll up. */
		void doScrollUp() {
			top = Math.max(0, top - 1);
		}
		/** Scroll down. */
		void doScrollDown() {
			int rows = height / rowHeight;
			top = Math.max(0, Math.min(top + 1, items.size() - rows));
		}
	}
	/** Go back to main menu. */
	void doMainMenu() {
		displayPrimary(Screens.MAIN);
		if (commons.world() != null) {
			commons.control().endGame();
		}
	}
	/** Delete the current selected item. */
	void doDelete() {
		int idx = list.items.indexOf(list.selected);
		if (idx >= 0) {
			
			File f = new File("save/" + commons.profile.name + "/" + list.selected.name);
			if (f.delete()) {
				list.items.remove(idx);
				
				idx = Math.min(idx, list.items.size() - 1);
				if (idx >= 0) {
					list.selected = list.items.get(idx);
				} else {
					list.selected = null;
				}
			} else {
				System.err.println("Could not delete " + f);
			}
		}
	}
	/** Load the selected item. */
	void doLoad() {
		doBack();
		load("save/" + commons.profile.name + "/" + list.selected.name);
	}
	/** Create a save. */
	void doSave() {
		save();
		doBack();
	}
	/** Return to the previous screen. */
	void doBack() {
		if (restore != null) {
			displaySecondary(restore);
		} else {
			hideSecondary();
		}
		if (resume) {
			commons.resume();
		}
	}
	/** Toggle the settings screen. */
	void doSettings() {
		settingsMode = !settingsMode;
	}
}
