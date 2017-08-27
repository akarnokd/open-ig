/*
 * Copyright 2008-2017, David Karnok
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.screen.items;

import org.junit.Before;
import org.junit.Test;

import java.awt.Rectangle;

import static org.junit.Assert.assertEquals;

/**
 * Planet screen unit test.
 *
 * @author rdubisz
 * @since 0.95.210
 */
public class PlanetScreenTest {

    private PlanetScreen scr = new PlanetScreen();
    private TestData data = new TestData();

    @Before
    public void setUp() throws Exception {
        scr = new PlanetScreen();
        scr.initialize(data.commonResources);
    }

    @Test
    public void centerScreen() throws Exception {
        centerScreenTester(1.0, 2035, 1744, 717, 555, -659, -594);
        centerScreenTester(1.0, 2045, 1879, 717, 555, -664, -662);
        centerScreenTester(1.0, 1953, 1417, 717, 555, -618, -431);
        centerScreenTester(1.0, 1953, 1417, 717, 555, -618, -431);
        centerScreenTester(1.0, 1907, 1576, 717, 555, -595, -510);
        centerScreenTester(1.0, 1775, 1354, 717, 555, -529, -399);
        centerScreenTester(1.0, 1907, 1576, 916, 640, -495, -468);
        centerScreenTester(1.0, 1775, 1354, 916, 640, -429, -357);
        centerScreenTester(1.0, 2035, 1744, 916, 640, -559, -552);
        centerScreenTester(1.0, 2045, 1879, 916, 640, -564, -619);
        centerScreenTester(1.0, 1953, 1417, 916, 640, -518, -388);
        centerScreenTester(1.0, 1953, 1417, 799, 757, -577, -330);
        centerScreenTester(1.0, 1907, 1576, 799, 757, -554, -409);
        centerScreenTester(1.0, 1775, 1354, 799, 757, -488, -298);
        centerScreenTester(1.0, 2035, 1744, 1489, 934, -273, -405);
        centerScreenTester(1.0, 2045, 1879, 1489, 934, -278, -472);
        centerScreenTester(1.0, 1953, 1417, 1489, 934, -232, -241);

        centerScreenTester(1.5, 2035, 1744, 1489, 934, -781, -841);
        centerScreenTester(1.5, 2045, 1879, 1489, 934, -789, -942);
        centerScreenTester(1.5, 1953, 1417, 1489, 934, -720, -595);

        centerScreenTester(2.5, 2035, 1744, 1489, 934, -1799, -1713);
        centerScreenTester(2.5, 2045, 1879, 1489, 934, -1811, -1881);
        centerScreenTester(2.5, 1953, 1417, 1489, 934, -1696, -1304);

        centerScreenTester(0.5, 2035, 1744, 1489, 934, 235, 31);
        centerScreenTester(0.5, 2045, 1879, 1489, 934, 233, -2);
        centerScreenTester(0.5, 1953, 1417, 1489, 934, 256, 112);

        centerScreenTester(0.2, 2035, 1744, 1489, 934, 541, 292);
        centerScreenTester(0.2, 2045, 1879, 1489, 934, 540, 279);
        centerScreenTester(0.2, 1953, 1417, 1489, 934, 549, 325);
    }

    private void centerScreenTester(double scale, int rectWidth, int rectHeight, int scrWidth, int scrHeight, int expectedX, int expectedY) {
        scr.width = scrWidth;
        scr.height = scrHeight;
        scr.render.scale = scale;
        scr.surface().boundingRectangle = new Rectangle(0, 0, rectWidth, rectHeight);
        scr.centerScreen();
        assertEquals(expectedX, scr.render.offsetX);
        assertEquals(expectedY, scr.render.offsetY);
    }

}
