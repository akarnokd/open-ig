/*
 * Copyright 2008-2014, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */
package hu.openig;

import hu.openig.screen.items.AchievementsScreen;
import hu.openig.screen.items.BarScreen;
import hu.openig.screen.items.BattlefinishScreen;
import hu.openig.screen.items.BridgeScreen;
import hu.openig.screen.items.CreditsScreen;
import hu.openig.screen.items.DatabaseScreen;
import hu.openig.screen.items.DiplomacyScreen;
import hu.openig.screen.items.EquipmentScreen;
import hu.openig.screen.items.GameOverScreen;
import hu.openig.screen.items.InfoScreen;
import hu.openig.screen.items.LoadSaveScreen;
import hu.openig.screen.items.LoadingScreen;
import hu.openig.screen.items.MainScreen;
import hu.openig.screen.items.MovieScreen;
import hu.openig.screen.items.PlanetScreen;
import hu.openig.screen.items.ProfileScreen;
import hu.openig.screen.items.ResearchProductionScreen;
import hu.openig.screen.items.ShipwalkScreen;
import hu.openig.screen.items.SingleplayerScreen;
import hu.openig.screen.items.SkirmishScreen;
import hu.openig.screen.items.SpacewarScreen;
import hu.openig.screen.items.SpyScreen;
import hu.openig.screen.items.StarmapScreen;
import hu.openig.screen.items.StatusbarScreen;
import hu.openig.screen.items.TestScreen;
import hu.openig.screen.items.TradeScreen;
import hu.openig.screen.items.TraitScreen;
import hu.openig.screen.items.VideoScreen;

/** The record of screens. */
public final class GameScreens {
    /** Main menu. */
    public MainScreen main;
    /** Videos. */
    public VideoScreen videos;
    /** Bridge. */
    public BridgeScreen bridge;
    /** Starmap. */
    public StarmapScreen starmap;
    /** Colony. */
    public PlanetScreen colony;
    /** Equipment. */
    public EquipmentScreen equipment;
    /** Research and production. */
    public ResearchProductionScreen researchProduction;
    /** Information. */
    public InfoScreen info;
    /** Diplomacy. */
    public DiplomacyScreen diplomacy;
    /** Database. */
    public DatabaseScreen database;
    /** Bar. */
    public BarScreen bar;
    /** Statistics and achievements. */
    public AchievementsScreen statisticsAchievements;
    /** Spacewar. */
    public SpacewarScreen spacewar;
    /** Single player. */
    public SingleplayerScreen singleplayer;
    /** Load and save. */
    public LoadSaveScreen loadSave;
    /** Battle finish screen. */
    public BattlefinishScreen battleFinish;
    /** The movie screens. */
    public MovieScreen movie;
    /** The loading in progress screen. */
    public LoadingScreen loading;
    /** The ship walk screen. */
    public ShipwalkScreen shipwalk;
    /** The status bar screen. */
    public StatusbarScreen statusbar;
    /** The phsychologist test. */
    public TestScreen test;
    /** The credits. */
    public CreditsScreen credits;
    /** The game over screen. */
    public GameOverScreen gameOver;
    /** The skirmish screen. */
    public SkirmishScreen skirmish;
    /** The traits screen. */
    public TraitScreen traits;
    /** The profile screen. */
    public ProfileScreen profile;
    /** The spy screen. */
    public SpyScreen spying;
    /** The trade screen. */
    public TradeScreen trade;
}
