/*
 * Copyright 2008-2017, David Karnok
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig.screen.items;

import hu.openig.GameWindow;
import hu.openig.model.*;
import hu.openig.screen.CommonResources;
import hu.openig.screen.GameControls;

/**
 * Prepare minimal data set for tests.
 *
 * @author rdubisz
 * @since 0.95.210
 */
public class TestData {

    protected CommonResources commonResources;
    protected Configuration configuration;
    protected GameControls gameControls;
    protected World world;
    protected Player player;

    public TestData() {
        initialize();
    }

    public void initialize()
    {
        configuration = new Configuration("open-ig-config.xml");
        commonResources = new CommonResources(configuration, null);
        gameControls = new GameWindow(configuration, commonResources);
        world = new World(commonResources);
        commonResources.world(world);
        player = new Player(world, "me");
        world.player = player;
        Planet planetSmall = planetSmall();
        Planet planetBig = planetBig();
        player.planets.put(planetSmall, null);
        player.planets.put(planetBig, null);
        player.currentPlanet = planetSmall;
    }

    Planet planetSmall(){
        Planet planet = new Planet("planet-small", world);
        planet.surface = new PlanetSurface();
        planet.surface.width = 33;
        planet.surface.height = 45;
        planet.surface.computeRenderingLocations();
        return planet;
    }

    Planet planetBig(){
        Planet planet = new Planet("planet-big", world);
        planet.surface = new PlanetSurface();
        planet.surface.width = 1000;
        planet.surface.height = 2000;
        planet.surface.computeRenderingLocations();
        return planet;
    }

}
