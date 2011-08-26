/*
 * Copyright 2008-2011, David Karnok 
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
					g2.setColor(Color.YELLOW);
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
				}
				return false;
			}
		});
		
		
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
	}
	/** Remove all entities. */
	void doClear() {
		items.clear();
		repaint();
	}
	/** The mouse controls. */
	class MouseControls extends MouseAdapter {
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
				}
			}
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
	 * @param gamma the angle where to toss away
	 */
	void tossAway(Entity e1, Entity e2, double gamma) {
		double theta = Math.atan2(e1.height, e1.width); // >= 0
		
		if (-theta <= gamma && gamma <= theta) {
			double nx = e1.x + e1.width / 2 + e2.width / 2;
			double mx = nx - e2.x;
			e2.x = nx;
			e2.y += mx * Math.tan(gamma);
		} else
		if (theta < gamma && gamma < Math.PI - theta) {
			double ny = e1.y + e1.height / 2 + e2.height / 2;
			double my = ny - e2.y;
			e2.y = ny;
			e2.x += my / Math.tan(gamma);
		} else
		if (Math.PI - theta <= gamma || gamma <= theta - Math.PI) {
			double nx = e1.x - e1.width / 2 - e2.width / 2;
			double mx = e2.x - nx;
			e2.x = nx;
			e2.y += mx * Math.tan(gamma);
		} else
		if (-theta > gamma && gamma > theta - Math.PI) {
			double ny = e1.y - e1.height / 2 + e2.height / 2;
			double my = e2.y - ny;
			e2.y = ny;
			e2.x += my / Math.tan(gamma);
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
			tossAway(bb, e, angle(bb, e));
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
}
