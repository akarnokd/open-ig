/*
 * Copyright 2008-2012, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.test;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Utility class to figure out a pathfinding algorithm for the space battles.
 * @author akarnokd, 2011.08.26.
 */
public class SpacePathfinding extends JFrame {
	/** */
	private static final long serialVersionUID = -8497777342276904932L;
	/** The movable entity. */
	class Entity {
		/** The center location. */
		double x;
		/** The center location. */
		double y;
		/** The width. */
		int width;
		/** The height. */
		int height;
		/** Is in movement? */
		boolean move;
		/** The target X. */
		int targetX;
		/** The target Y. */
		int targetY;
		/** The color. */
		Color color;
		/** Is it selected? */
		boolean selected;
		/**
		 * Check if two entities overlap.
		 * @param e the other entity
		 * @return true if overlap
		 */
		boolean overlaps(Entity e) {
			boolean xout = (e.x + e.width / 2 - 1 < x - width / 2) || (e.x - e.width / 2 > x + width / 2 - 1);
			boolean yout = (e.y + e.height / 2 - 1 < y - height / 2) || (e.y - e.height / 2 > y + height / 2 - 1);
			return !xout && !yout;
		}
		/**
		 * Check if completely within the specified enclosing width and height.
		 * @param ewidth the enclosing width
		 * @param eheight the eclosing height
		 * @return true if within
		 */
		boolean within(double ewidth, double eheight) {
			return x >= width / 2 && x + width / 2 < ewidth
					&& y >= height / 2 && y + height / 2 < eheight;
		}
	}
	/** The rendering surface. */
	Surface surface;
	/** The entities. */
	final List<Entity> items = new ArrayList<Entity>();
	/** The random direction. */
	final Random rnd = new Random();
	/** The component drawing the surface. */
	class Surface extends JComponent {
		/** */
		private static final long serialVersionUID = 1L;

		@Override
		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D)g;
			
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, getWidth(), getHeight());
			
			for (Entity e : items) {
				g2.setColor(e.color);
				g2.fillRect(
						(int)(e.x - e.width / 2), 
						(int)(e.y - e.height / 2), 
						(e.width), 
						(e.height));
				if (e.selected) {
					g2.setColor(Color.BLACK);
					g2.drawRect(
							(int)(e.x - e.width / 2), 
							(int)(e.y - e.height / 2), 
							(e.width - 1), 
							(e.height - 1));
				}
			}
			g2.setColor(Color.RED);
			for (Entity e1 : items) {
				for (Entity e2 : items) {
					if (e1 != e2 && e1.overlaps(e2)) {
						int ox1 = (int)Math.max(e1.x - e1.width / 2, e2.x - e2.width / 2);
						int ox2 = (int)Math.min(e1.x + e1.width / 2, e2.x + e2.width / 2) - 1;
						
						int oy1 = (int)Math.max(e1.y - e1.height / 2, e2.y - e2.height / 2);
						int oy2 = (int)Math.min(e1.y + e1.height / 2, e2.y + e2.height / 2) - 1;
						
						g2.fillRect(ox1, oy1, ox2 - ox1 + 1, oy2 - oy1 + 1);
						
					}
				}
			}
		}
	}
	/** Initialize the screen. */
	public SpacePathfinding() {
		super("Space pathfinding");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		surface = new Surface();
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(surface, 640, 640, 640)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(surface, 480, 480, 480)
		);
		
		MouseControls mc = new MouseControls();
		
		surface.addMouseListener(mc);
		surface.addMouseMotionListener(mc);
		surface.addMouseWheelListener(mc);

		KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		kfm.addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (e.getID() == KeyEvent.KEY_TYPED) {
					if (e.getKeyChar() == 'c') {
						doClear();
					}
					if (e.getKeyChar() == 'f') {
						fixOverlappings();
						repaint();
					}
					if (e.getKeyChar() == 'r') {
						doAddRandom();
						repaint();
					}
					if (e.getKeyChar() == 'a') {
						centerAll();
						repaint();
					} 
				} else
				if (e.getID() == KeyEvent.KEY_PRESSED) {
					if (e.getKeyCode() == KeyEvent.VK_LEFT) {
						moveSelected(-4, -4);
						repaint();
					}
					if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
						moveSelected(4, 4);
						repaint();
					}
					if (e.getKeyCode() == KeyEvent.VK_UP) {
						moveSelected(4, -4);
						repaint();
					}
					if (e.getKeyCode() == KeyEvent.VK_DOWN) {
						moveSelected(-4, 4);
						repaint();
					}
				}
				return false;
			}
		});
		
		
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
	}
	/**
	 * Move the selected entities with the given amount.
	 * @param dx the delta-x
	 * @param dy the delta-y
	 */
	void moveSelected(int dx, int dy) {
		for (Entity e : items) {
			if (e.selected) {
				e.x += dx;
				e.y += dy;
			}
		}
		tossEverythingElseAside(Math.atan2(dy, dx));
	}
	/** Remove all entities. */
	void doClear() {
		items.clear();
		repaint();
	}
	/** The mouse controls. */
	class MouseControls extends MouseAdapter {
		/** Last mouse x. */
		int lastx;
		/** Last mouse y. */
		int lasty;
		/** Entity moving. */
		Entity moving;
		@Override
		public void mousePressed(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				if (e.isShiftDown() && !e.isControlDown()) {
					// add small
					Entity entity = new Entity();
					entity.x = e.getX();
					entity.y = e.getY();
					entity.width = 20;
					entity.height = 20;
					entity.color = Color.GREEN;
					items.add(entity);
//					fixOverlappings();
					repaint();
				} else
				if (e.isControlDown() && !e.isShiftDown()) {
					// add medium
					Entity entity = new Entity();
					entity.x = e.getX();
					entity.y = e.getY();
					entity.width = 40;
					entity.height = 40;
					entity.color = Color.BLUE;
					items.add(entity);
//					fixOverlappings();
					repaint();
				} else
				if (e.isControlDown() && e.isShiftDown()) {
					// add large
					Entity entity = new Entity();
					entity.x = e.getX();
					entity.y = e.getY();
					entity.width = 60;
					entity.height = 60;
					entity.color = Color.PINK;
					items.add(entity);
//					fixOverlappings();
					repaint();
				} else {
					lastx = e.getX();
					lasty = e.getY();
					moving = find(lastx, lasty);
					select(moving);
					repaint();
				}
			}
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			if (SwingUtilities.isLeftMouseButton(e)) {
				moving = null;
			}
		}
		@Override
		public void mouseDragged(MouseEvent e) {
			if (moving != null) {
				double dx = e.getX() - lastx;
				double dy = e.getY() - lasty;
				
				lastx = e.getX();
				lasty = e.getY();
				
				// move small distance per loop

				double a = Math.atan2(dy, dx);
				double len = Math.sqrt(dx * dx + dy * dy);
				
				while (len > 0.01) {
					double v = len >= 1 ? 1 : len;
					moving.x += v * Math.cos(a);
					moving.y += v * Math.sin(a);
					tossEverythingElseAside(moving, a);
					len -= 1.0;
				}

				
				
				repaint();
			}
		}
	}
	/**
	 * Find an entity with the given location.
	 * @param x the mouse X
	 * @param y the mouse Y
	 * @return the entity or null
	 */
	Entity find(int x, int y) {
		for (Entity e : items) {
			if (e.x - e.width / 2 <= x && e.y - e.height / 2 <= y
					&& e.x + e.width / 2 > x && e.y + e.height / 2 > y) {
				return e;
			}
		}
		return null;
	}
	/**
	 * Select the given entity.
	 * @param e the entity
	 */
	void select(Entity e) {
		for (Entity e0 : items) {
			e0.selected = e0 == e;
		}
	}
	/**
	 * @param args no arguments.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SpacePathfinding pf = new SpacePathfinding();
				pf.setVisible(true);
			}
		});
	}
	/**
	 * Compute the distance between two entities.
	 * @param e1 the first instance
	 * @param e2 the second instance
	 * @return the distance
	 */
	double distance(Entity e1, Entity e2) {
		return Math.sqrt((e1.x - e2.x) * (e1.x - e2.x) + (e1.y - e2.y) * (e1.y * e2.y));
	}
	/**
	 * Compute the distance square between two entities.
	 * @param e1 the first instance
	 * @param e2 the second instance
	 * @return the distance square
	 */
	double distance2(Entity e1, Entity e2) {
		return (e1.x - e2.x) * (e1.x - e2.x) + (e1.y - e2.y) * (e1.y * e2.y);
	}
	/**
	 * Compute the angle of the second entity in respect to the first entity's center.
	 * @param e1 the first entity
	 * @param e2 the second entity
	 * @return the angle in radians
	 */
	double angle(Entity e1, Entity e2) {
		double dx = e2.x - e1.x;
		double dy = e2.y - e1.y;
		
		double gamma = 0;
		if (dx == 0 && dy == 0) {
			// random direction
			gamma = Math.PI * (1 - rnd.nextDouble() * 2);
		} else {
			gamma = Math.atan2(dy, dx);
		}
		return gamma;
	}
	/**
	 * Moves the {@code e2} entity away from {@code e1} entity along a
	 * vector crossing both of their centers. 
	 * @param e1 the first entity
	 * @param e2 the second entity
	 */
	void tossAway(Entity e1, Entity e2) {
		double theta = Math.atan2(e1.height, e1.width); // >= 0
		
		// the current angle relative to e1
		double gamma = angle(e1, e2);
		
		if (-theta <= gamma && gamma <= theta) {
			double nx = e1.x + e1.width / 2 + e2.width / 2;
			double mx = nx - e2.x;
			e2.x = nx;
			double dy = mx * Math.tan(gamma);
			e2.y += dy;
		} else
		if (theta < gamma && gamma < Math.PI - theta) {
			double ny = e1.y + e1.height / 2 + e2.height / 2;
			double my = ny - e2.y;
			e2.y = ny;
			double dx = my / Math.tan(gamma);
			e2.x += dx;

		} else
		if (Math.PI - theta <= gamma || gamma <= theta - Math.PI) {
			double nx = e1.x - e1.width / 2 - e2.width / 2;
			double mx = e2.x - nx;
			e2.x = nx;
			double dy = -mx * Math.tan(gamma);
			e2.y += dy;
		} else
		if (-theta > gamma && gamma > theta - Math.PI) {
			double ny = e1.y - e1.height / 2 - e2.height / 2;
			double my = e2.y - ny;
			e2.y = ny;
			e2.x += -my / Math.tan(gamma);
		}
	}
	/** Add 10 random elements. */
	void doAddRandom() {
		for (int i = 0; i < 10; i++) {
			Entity e = new Entity();
			e.x = surface.getWidth() / 2;
			e.y = surface.getHeight() / 2;
			switch (rnd.nextInt(3)) {
			case 0:
				e.width = 20;
				e.height = 20;
				e.color = Color.GREEN;
				break;
			case 1:
				e.width = 40;
				e.height = 40;
				e.color = Color.BLUE;
				break;
			default:
				e.width = 60;
				e.height = 60;
				e.color = Color.PINK;
			}
			items.add(e);
		}
	}
	/** Center all entities. */
	void centerAll() {
		for (Entity e : items) {
			e.x = surface.getWidth() / 2;
			e.y = surface.getHeight() / 2;
		}
	}
	/** Fix overlappings of various entities. */
	void fixOverlappings() {
		// move back inside
		for (Entity e : items) {
			e.x = Math.max(Math.min(e.x, surface.getWidth() - e.width / 2), e.width / 2);
			e.y = Math.max(Math.min(e.y, surface.getHeight() - e.height / 2), e.height / 2);
		}

		Map<Entity, Integer> intersections = new HashMap<Entity, Integer>();
		
		Deque<Entity> overlappings = new LinkedList<Entity>();
		
		int totalOverlaps = 0;
		Set<Entity> entities = new HashSet<Entity>(items);
		do {
			totalOverlaps = 0;
			intersections.clear();
			for (Entity e1 : entities) {
				int count = 0;
				for (Entity e2 : entities) {
					if (e1 != e2 && e1.overlaps(e2)) {
						count++;
						totalOverlaps++;
					}
				}
				if (count > 0) {
					intersections.put(e1, count);
				}
			}
			int max = 0;
			Entity maxE = null;
			for (Map.Entry<Entity, Integer> me : intersections.entrySet()) {
				if (me.getValue() > max) {
					max = me.getValue();
					maxE = me.getKey();
				}
			}
			if (maxE != null) {
				overlappings.push(maxE);
				entities.remove(maxE);
			}
		} while (totalOverlaps > 0);
		
		elimination:
		while (!overlappings.isEmpty()) {
			Entity e = overlappings.pop();
			
			// center coordinates
			double cx = 0;
			double cy = 0;
			// bounding box of the current list
			double minx = Double.MAX_VALUE;
			double miny = Double.MAX_VALUE;
			double maxx = 0;
			double maxy = 0;
			
			for (Entity e1 : entities) {
				cx += e1.x;
				cy += e1.y;
				
				minx = Math.min(minx, e1.x - e1.width / 2);
				miny = Math.min(miny, e1.y - e1.height / 2);
				maxx = Math.max(maxx, e1.x + e1.width / 2);
				maxy = Math.max(maxy, e1.y + e1.height / 2);
				
				
			}
			cx /= entities.size();
			cy /= entities.size();
			
			
			double dx = cx - e.x;
			double dy = cy - e.y;
			double angle = Math.atan2(dy, dx);
			
			if (dx == 0 && dy == 0) {
				angle = Math.PI * (1 - rnd.nextDouble() * 2);
			}
			// try four directions
			double cx2 = e.x;
			double cy2 = e.y;
			
			double mindist = Double.MAX_VALUE;
			double ex0 = e.x;
			double ey0 = e.y;
			boolean found = false;
			angleTest:
			for (double gamma : new double[] { angle, angle + Math.PI / 2, angle + Math.PI, angle - Math.PI / 2 }) {
				e.x = cx2;
				e.y = cy2;
				while (!outside(minx, miny, maxx, maxy, cx2, cy2)) {
					if (!anyIntersection(e, entities)) {
						double dq = dist(e.x, e.y, cx, cy);
						if (dq < mindist) {
							mindist = dq;
							ex0 = e.x;
							ey0 = e.y;
							found = true;
						}
						continue angleTest;
					}
					e.x += Math.cos(gamma);
					e.y += Math.sin(gamma);
				}
			}
			if (found) {
				e.x = ex0;
				e.y = ey0;
				entities.add(e);
				continue elimination;
			}
			// no angle was good enough, toss it outside of the bonding box
			Entity bb = new Entity();
			bb.width = (int)(maxx - minx + 1);
			bb.height = (int)(maxy - miny + 1);
			bb.x = minx + bb.width / 2;
			bb.y = miny + bb.height / 2;
			tossAway(bb, e);
		}
	}
	/**
	 * Compute the distance square of two points.
	 * @param cx the first X
	 * @param cy the first Y
	 * @param px the second X
	 * @param py the second Y
	 * @return the distance square
	 */
	double dist(double cx, double cy, double px, double py) {
		return (cx - px) * (cx - px) + (cy - py) * (cy - py);
	}
	/**
	 * Is the point outside of the rectangle?
	 * @param x1 the bounding rectangle minimum x
	 * @param y1 the bounding rectangle minimum y
	 * @param x2 the bounding rectangle maximum x
	 * @param y2 the bounding rectangle maximum y
	 * @param px the parget point X
	 * @param py the target point Y
	 * @return true if outside
	 */
	boolean outside(double x1, double y1, double x2, double y2, double px, double py) {
		return px < x1 || px > x2 || py < y1 || py > y2;
	}
	/**
	 * Check if the given entity overlaps with any other entity.
	 * @param e the target entity
	 * @param others the other entitites
	 * @return true if intersecting
	 */
	boolean anyIntersection(Entity e, Iterable<Entity> others) {
		for (Entity e0 : others) {
			if (e.overlaps(e0)) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Toss everyone else aside.
	 * @param moving the moving object
	 * @param baseAngle the general direction
	 */
	void tossEverythingElseAside(Entity moving, double baseAngle) {
		Set<Entity> fixed = new HashSet<Entity>();
		fixed.add(moving);
		tossEverythingElseAside(sortByDistance(items, moving), fixed);
	}
	/**
	 * Toss everyone else aside.
	 * @param list the list to work with
	 * @param fixed the set of already fixed items
	 */
	void tossEverythingElseAside(List<Entity> list, Set<Entity> fixed) {
		for (Entity e : list) {
			for (Entity f : fixed) {
				if (e.overlaps(f)) {
					tossAway(f, e);
				}
			}
		}
	}
	/**
	 * @param source the source of the movement
	 * @param target the center entity
	 * @return sort the entities by distance from the given entity
	 */
	List<Entity> sortByDistance(final List<Entity> source, final Entity target) {
		List<Entity> result = new ArrayList<Entity>();
		for (Entity e : source) {
			if (e != target) {
				result.add(e);
			}
		}
		Collections.sort(items, new Comparator<Entity>() {
			@Override
			public int compare(Entity o1, Entity o2) {
				double d1 = distance2(target, o1);
				double d2 = distance2(target, o2);
				return d1 < d2 ? -1 : (d1 > d2 ? 1 : 0);
			}
		});
		return result;
	}
	/**
	 * Toss everything aside from the selected groups.
	 * @param baseAngle the base angle
	 */
	void tossEverythingElseAside(double baseAngle) {
		
	}
}
