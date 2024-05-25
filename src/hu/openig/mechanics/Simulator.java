/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import java.awt.geom.Point2D;
import java.util.*;

import hu.openig.core.Difficulty;
import hu.openig.model.*;
import hu.openig.model.CustomBalanceSettings.BuildingMoraleCalculationMode;
import hu.openig.utils.U;

/**
 * Collection of algorithms which update the world
 * state as time progresses: progress on buildings, repair
 * research, production, fleet movement, etc.
 * @author akarnokd, Apr 5, 2011
 */
public final class Simulator {
    /** Utility class. */
    private Simulator() {
    }
    /**
     * The main computation.
     * @param world the world to compute
     * @return true if the a quicksave is needed
     */
    public static boolean compute(final World world) {
        int day0 = world.time.get(GregorianCalendar.DATE);
        world.time.add(GregorianCalendar.MINUTE, world.params().speed());
        int day1 = world.time.get(GregorianCalendar.DATE);

        boolean result = false;

        // Prepare global statistics
        // -------------------------
        prepareGlobalStatistics(world);

        // -------------------------

        for (Player player : world.players.values()) {
            Map<Planet, PlanetStatistics> planetStats = new HashMap<>();

            PlanetStatistics all = player.getPlanetStatistics(planetStats);

            // -------------------------
            // Prepare player statistics

            preparePlayerStatistics(world, player, all);

            // -------------------------

            if (day0 != day1) {
                player.yesterday.clear();
                player.yesterday.assign(player.today);
                player.today.clear();
            }
            // result |= player == world.player
            if (!player.pauseResearch) {
                progressResearch(world, player, all);
            }
            // result |= player == world.player
            if (!player.pauseProduction) {
                progressProduction(world, player, all);
            }

            if (!player.id.equals("Traders")) {

                double taxCompensation = 1;
                if (day0 != day1) {
                    taxCompensation = taxCompensation(player, all.planetCount);
                }

                // progress player's planets
                for (Map.Entry<Planet, PlanetStatistics> pps : planetStats.entrySet()) {
                    Planet p = pps.getKey();
                    PlanetStatistics ps = pps.getValue();
                    ps.planetCount = all.planetCount;
                    progressPlanet(world, p, day0 != day1, ps, taxCompensation);
                }

                handleBlackMarket(player);
            }
        }

        if (day0 != day1) {

            for (Player p : world.players.values()) {

                if (!p.isMainPlayer) {
                    p.addMoney(world.customBalanceSettings.nonPlayerBonusMoneyPerDay);

                    if (world.level >= 4) {
                        for (DiplomaticRelation dr : world.relations) {
                            if (dr != null && dr.full && !dr.strongAlliance && dr.alliancesAgainst.isEmpty()) {
                                Player p1 = dr.first;
                                Player p2 = dr.second;
                                if  ((p1.isMainPlayer && !p2.isMainPlayer) || (!p1.isMainPlayer && p2.isMainPlayer)) {
                                    dr.updateDrValue(dr.value - world.customBalanceSettings.nonPlayerRelationshipDeterioration / 100d);
                                }
                            }
                        }
                    }
                }

                p.ai.onNewDay();
            }

            result = true;
        }

        checkAchievements(world, day0 != day1);

        world.scripting.onTime();
        return result;
    }
    /**
     * Prepare global statistics.
     * <p>Typically accumulative values.</p>
     * @param world the world object
     */
    static void prepareGlobalStatistics(World world) {
        world.statistics.totalPopulation.value = 0;
        world.statistics.totalBuilding.value = 0;
        world.statistics.totalAvailableBuilding.value = 0;

        world.statistics.totalWorkerDemand.value = 0;
        world.statistics.totalEnergyDemand.value = 0;
        world.statistics.totalAvailableEnergy.value = 0;
        world.statistics.totalAvailableHouse.value = 0;
        world.statistics.totalAvailableFood.value = 0;
        world.statistics.totalAvailableHospital.value = 0;
        world.statistics.totalAvailablePolice.value = 0;
    }
    /**
     * Prepare the world and player statistics.
     * @param world the world
     * @param player the player
     * @param all the all planet statistics of the player
     */
    static void preparePlayerStatistics(World world, Player player,
            PlanetStatistics all) {
        player.statistics.totalPopulation.value = 0;
        player.statistics.totalBuilding.value = 0;
        player.statistics.totalAvailableBuilding.value = 0;

        player.statistics.totalWorkerDemand.value = all.workerDemand;
        player.statistics.totalEnergyDemand.value = all.energyDemand;
        player.statistics.totalAvailableEnergy.value = all.energyAvailable;
        player.statistics.totalAvailableHouse.value = all.houseAvailable;
        player.statistics.totalAvailableFood.value = all.foodAvailable;
        player.statistics.totalAvailableHospital.value = all.hospitalAvailable;
        player.statistics.totalAvailablePolice.value = all.policeAvailable;

        world.statistics.totalWorkerDemand.value += all.workerDemand;
        world.statistics.totalEnergyDemand.value += all.energyDemand;
        world.statistics.totalAvailableEnergy.value += all.energyAvailable;
        world.statistics.totalAvailableHouse.value += all.houseAvailable;
        world.statistics.totalAvailableFood.value += all.foodAvailable;
        world.statistics.totalAvailableHospital.value += all.hospitalAvailable;
        world.statistics.totalAvailablePolice.value += all.policeAvailable;

        player.statistics.planetsOwned.value = 0;
    }
    /**
     * Make progress on the buildings of the planet.
     * @param world the world
     * @param planet the planet
     * @param dayChange consider day change
     * @param ps the planet statistics
     * @param taxCompensation the compensation multiplier for planet count
     * @return true if repaint will be needed
     */
    static boolean progressPlanet(World world, Planet planet, boolean dayChange,
            PlanetStatistics ps, double taxCompensation) {
        boolean result = false;
        int tradeIncome = 0;
        double multiply = 1.0f;
        double moraleBoost = 0;
        Map<String, List<Double>> moraleBoostMap = null;
        BuildingMoraleCalculationMode moraleCalcMode = world.customBalanceSettings.buildingMoraleCalculationMode;
        if (moraleCalcMode != BuildingMoraleCalculationMode.LINEAR) {
            moraleBoostMap = new HashMap<>();
        }
        int radar = 0;

        long eqPlaytime = 20L * 60 * 60;
        int eqDelta = 365 * 24 * 60;
        if (world.difficulty == Difficulty.NORMAL) {
            eqPlaytime /= 2;
            eqDelta /= 2;
        } else
        if (world.difficulty == Difficulty.HARD) {
            eqPlaytime /= 4;
            eqDelta /= 4;
        }
        double populationGrowthModifier = ps.populationGrowthModifier;
        double planetTypeModifier = world.galaxyModel.getGrowth(planet.type.type, planet.race);
        // apply fertility trait
        Trait t = planet.owner.traits.trait(TraitKind.FERTILE);
        if (t != null) {
            planetTypeModifier *= 1 + t.value / 100;
        }

        final int repairCost = world.params().repairCost();
        final int repairAmount = world.params().repairSpeed();
//        final int buildCost = world.params().constructionCost();
        final int buildAmount = world.params().constructionSpeed();

        int speed = world.params().speed();

        if (world.statistics.simulationTime.value >= eqPlaytime
                && (planet.type.type.equals("earth") || planet.type.type.equals("rocky"))) {
            if (planet.earthQuakeTTL <= 0) {
                // cause earthquake once in every 12, 6 or 3 months
                if (ModelUtils.randomInt(eqDelta) < speed) {
                    planet.earthQuakeTTL = 60; // in minutes

                    Message msg = world.newMessage("message.earthquake");
                    msg.priority = 25;
                    msg.targetPlanet = planet;

                    planet.owner.addMessage(msg);
                }
            } else {
                planet.earthQuakeTTL -= speed;
            }
        }

        // rain calculation
        if (planet.type.weatherDrop != null) {
            if (planet.weatherTTL <= 0) {
                if (ModelUtils.randomInt(planet.type.weatherFrequency) < speed) {
                    planet.weatherTTL = planet.type.weatherDuration + ModelUtils.randomInt(120) - 60;
                }
            } else {
                planet.weatherTTL -= speed;
            }
        } else {
            planet.weatherTTL = -1;
        }

        // progress quarantine if any
        if (planet.quarantineTTL > 0) {
            planet.quarantineTTL -= speed;
            if (planet.quarantineTTL <= 0) {
                planet.quarantineTTL = 0;

                world.cureFleets(planet);
                world.scripting.onPlanetCured(planet);
            }
        }
        boolean rebuildroads = false;
        boolean runAllocator = false;

        for (Building b : planet.surface.buildings.list()) {

            planet.owner.statistics.totalBuilding.value++;
            world.statistics.totalBuilding.value++;

            double eff = b.getEfficiency();

            if (Building.isOperational(eff)) {
                planet.owner.statistics.totalAvailableBuilding.value++;
                world.statistics.totalAvailableBuilding.value++;
            }

            if (b.isConstructing()) {
                b.buildProgress += buildAmount;
                b.buildProgress = Math.min(b.type.hitpoints, b.buildProgress);
                b.hitpoints += buildAmount;
                b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);

                if (b.buildProgress >= b.type.hitpoints) {
                    planet.owner.ai.onBuildingComplete(planet, b);
                    world.scripting.onBuildingComplete(planet, b);

                    runAllocator = true;
                }

                result = true;
            } else
            // repair an unit if autorepair or explicitly requested
            if (b.repairing || (planet.owner == world.player && world.config.autoRepair && b.isDamaged())) {
                if (b.hitpoints * 100d / b.type.hitpoints < ps.freeRepair) {
                    b.hitpoints += repairAmount * ps.freeRepairEff;
                    b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
                    result = true;
                } else {
                    if (planet.owner.money() >= repairCost
                            && (b.repairing || planet.owner.money() >= world.config.autoRepairLimit)) {
                        planet.owner.addMoney(-repairCost); // FIXME repair cost per unit?
                        planet.owner.today.repairCost += repairCost;
                        b.hitpoints += repairAmount;
                        b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
                        result = true;

                        planet.owner.statistics.moneyRepair.value += repairCost;
                        planet.owner.statistics.moneySpent.value += repairCost;

                        world.statistics.moneyRepair.value += repairCost;
                        world.statistics.moneySpent.value += repairCost;
                    }
                }
            } else
            // free repair buildings
            if (b.isDamaged() && (b.hitpoints * 100d / b.type.hitpoints < ps.freeRepair)) {
                b.hitpoints += repairAmount * ps.freeRepairEff;
                b.hitpoints = Math.min(b.type.hitpoints, b.hitpoints);
                result = true;
            }
            // turn of repairing when hitpoints are reached
            if (b.repairing && b.hitpoints == b.type.hitpoints) {
                // FIXME planet.owner.ai.onRepairComplete(planet, b);
                world.scripting.onRepairComplete(planet, b);
                b.repairing = false;
                result = true;
            }
            if (Building.isOperational(eff)) {
                if (b.hasResource(BuildingType.RESOURCE_CREDIT)) {
                    tradeIncome += b.getResource(BuildingType.RESOURCE_CREDIT) * eff;
                }
                if (b.hasResource(BuildingType.RESOURCE_MULTIPLY)) {
                    multiply = b.getResource(BuildingType.RESOURCE_MULTIPLY) * eff;
                }
                if (b.hasResource(BuildingType.RESOURCE_MORALE)) {
                    double v = b.getResource(BuildingType.RESOURCE_MORALE) * eff;
                    moraleBoost += v;
                    if (moraleBoostMap != null) {
                        List<Double> perType = moraleBoostMap.get(b.type.id);
                        if (perType == null) {
                            perType = new ArrayList<>();
                            moraleBoostMap.put(b.type.id, perType);
                        }
                        perType.add(v);
                    }
                }
                if (b.hasResource(BuildingType.RESOURCE_RADAR)) {
                    radar = Math.max(radar, (int)b.getResource(BuildingType.RESOURCE_RADAR));
                }
            }
            if (planet.earthQuakeTTL > 0) {
                if (b.type.kind.equals(BuildingType.KIND_FACTORY)) {
                    b.hitpoints -= Math.max(b.type.hitpoints * 15L * speed / 6000, 1);
                } else
                if (b.getEnergy() <= 0) {
                    // reduce regular building health by 25% of its total hitpoints during the earthquake duration
                    b.hitpoints -= Math.max(b.type.hitpoints * 10L * speed / 6000, 1);
                } else {
                    // reduce energy building health by 55% of its total hitpoints during the earthquake duration
                    b.hitpoints -= Math.max(b.type.hitpoints * 20L * speed / 6000, 1);
                }
                if (b.hitpoints <= 0) {
                    planet.surface.removeBuilding(b);
                    rebuildroads = true;
                }
            }
        }
        if (rebuildroads) {
            planet.rebuildRoads();
        }
        if (runAllocator) {
            Allocator.computeNow(planet);
        }
        if (moraleBoostMap != null) {
            moraleBoost = 0d;
            for (List<Double> boosts : moraleBoostMap.values()) {
                Collections.sort(boosts, REVERSE_DOUBLE_COMPARATOR);
                int boostIdx = 0;
                switch (moraleCalcMode) {
                case ONE_PER_TYPE:
                case BEST_PER_TYPE:
                    moraleBoost += boosts.get(0); // best building
                    break;
                case DEGRESSIVE:
                    for (Double v : boosts) {
                        moraleBoost += v / (1 + boostIdx);
                        boostIdx++;
                    }
                    break;
                case ASYMPTOTIC:
                    for (Double v : boosts) {
                        moraleBoost += v / Math.pow(2, boostIdx);
                        boostIdx++;
                    }
                    break;
                default:
                    break;
                }

            }
        }
        // search for radar capable inventory
        for (InventoryItem pii : planet.inventory.findByOwner(planet.owner.id)) {
            radar = Math.max(radar, pii.type.getInt("radar", 0));
        }
        if (radar != 0) {
            for (Map.Entry<InventoryItem, Integer> ittl : new ArrayList<>(planet.timeToLive.entrySet())) {
                if (ittl.getKey().owner != planet.owner) {
                    Integer cttl = ittl.getValue();
                    int cttl2 = cttl - radar;
                    if (cttl2 <= 0) {
                        planet.timeToLive.remove(ittl.getKey());
                        planet.inventory.remove(ittl.getKey());

                        ittl.getKey().owner.ai.onSatelliteDestroyed(planet, ittl.getKey());
                        world.scripting.onInventoryRemove(planet, ittl.getKey());
                    } else {
                        ittl.setValue(cttl2);
                    }
                }
            }
        }
        if (planet.autoBuild != AutoBuild.OFF
                && (planet.owner == world.player && planet.owner.money() >= world.config.autoBuildLimit)) {
            AutoBuilder.performAutoBuild(world, planet, ps);
        }

        // reequip station bombs and rockets
        for (InventoryItem ii : planet.inventory.findByOwner(planet.owner.id)) {
            if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
                regenerateInventory(true, ii);
            }
        }

        if (dayChange) {
            planet.lastMorale(planet.morale());
            planet.lastPopulation(planet.population());
        }
        double dayPercent = world.params().speed() / 24d / 60d;
        if (!world.config.continuousMoney) {
            dayPercent = 1d;
        }

        if (world.config.continuousMoney || dayChange) {
            // FIXME morale computation
            double newMorale = 50 + moraleBoost;
            if (planet.tax == TaxLevel.NONE) {
                newMorale += 5;
            } else
            if (planet.tax.ordinal() <= TaxLevel.MODERATE.ordinal()) {
                if (!planet.owner.race.equals(planet.race)) {
                    newMorale -= planet.tax.percent / 4f;
                } else {
                    newMorale -= planet.tax.percent / 6f;
                }
            } else {
                if (!planet.owner.race.equals(planet.race)) {
                    newMorale -= planet.tax.percent / 2.5f;
                } else {
                    newMorale -= planet.tax.percent / 3f;
                }
            }
            if (ps.houseAvailable < planet.population()) {
                newMorale += (ps.houseAvailable - planet.population()) * 75f / planet.population();
            } else {
                newMorale += (ps.houseAvailable - planet.population()) * 2f / planet.population();
            }
            if (ps.hospitalAvailable < planet.population()) {
                newMorale += (ps.hospitalAvailable - planet.population()) * 50f / planet.population();
            }
            if (ps.foodAvailable < planet.population()) {
                newMorale += (ps.foodAvailable - planet.population()) * 75f / planet.population();
            }
            if (planet.quarantineTTL > 0) {
                newMorale = Math.max(0, newMorale - 5);
            }
            if (ps.policeAvailable < planet.population()) {
                newMorale += (ps.policeAvailable - planet.population()) * 50f / planet.population();
            } else {
                newMorale += Math.min(25, (ps.policeAvailable - planet.population()) * planet.owner.policeRatio / planet.population());
            }

            double moraleMax = planet.owner.race.equals(planet.race) ? 100 : 75;

            newMorale = Math.max(0, Math.min(moraleMax, newMorale));

            double nextMorale = (planet.morale() * 0.8d + 0.2d * newMorale);
            if (planet.morale() >= 95 && newMorale > planet.morale()) {
                nextMorale = planet.morale() + (newMorale - 95) / 10;
            }

            double morale0 = planet.morale();
            double morale1 = Math.max(0, Math.min(100, /* (int) */nextMorale));

            planet.morale(morale0 + (morale1 - morale0) * dayPercent);

            // ----------

            double populationDelta;
            double nonOwnerRace = 1d;
            if (!planet.race.equals(planet.owner.race)) {
                nonOwnerRace = 3d;
            }
            if (planet.morale() < 20) {
                populationDelta = 1000 * (planet.morale() - 50) / 100 * nonOwnerRace;
            } else
            if (planet.morale() < 30) {
                populationDelta = 1000 * (planet.morale() - 50) / 150 * nonOwnerRace;
            } else
            if (planet.morale() < 40) {
                populationDelta = 1000 * (planet.morale() - 50) / 200 * nonOwnerRace;
            } else
            if (planet.morale() < 50) {
                populationDelta = 1000 * (planet.morale() - 50) / 250 * nonOwnerRace;
            } else {
                populationDelta = 1000 * (planet.morale() - 50) / 500 * populationGrowthModifier * planetTypeModifier;
            }

            double population0 = planet.population();
            double population1 = world.scripting.playerPopulationGrowthOverride(planet, planet.population() + populationDelta);

            planet.population(population0 +  (population1 - population0) * dayPercent);

            // tax calculation

            double moneyModifier = 1;
            t = planet.owner.traits.trait(TraitKind.TAX);
            if (t != null) {
                moneyModifier = 1 + t.value / 100;
            }

            double trade1 = (int)(tradeIncome * multiply * moneyModifier);

            double taxIncomeSim = (
                     taxCompensation * moneyModifier * planet.population() * planet.morale() * planet.tax.percent / 10000);

            double tax1 = world.scripting.playerTaxIncomeOverride(planet, taxIncomeSim);

            double tradeDelta = (trade1 * dayPercent);
            double taxDelta = (tax1 * dayPercent);

            if (!world.config.continuousMoney) {
                planet.taxIncome(0);
                planet.tradeIncome(0);
            }

            // apply base and scale taxing
            taxDelta = planet.owner.taxBase * dayPercent / ps.planetCount + taxDelta * planet.owner.taxScale;

            planet.addTradeIncome(tradeDelta);
            planet.addTaxIncome(taxDelta);

            // update statistics
            planet.owner.addMoney(tradeDelta + taxDelta);

            planet.owner.statistics.moneyIncome.value += tradeDelta + taxDelta;
            planet.owner.statistics.moneyTaxIncome.value += taxDelta;
            planet.owner.statistics.moneyTradeIncome.value += tradeDelta;

            world.statistics.moneyIncome.value += tradeDelta + taxDelta;
            world.statistics.moneyTaxIncome.value += taxDelta;
            world.statistics.moneyTradeIncome.value += tradeDelta;
        }

        if (dayChange) {
            // handle revolt/death
            planet.owner.yesterday.taxIncome += planet.taxIncome();
            planet.owner.yesterday.tradeIncome += planet.tradeIncome();
            planet.owner.yesterday.taxMorale += planet.morale();
            planet.owner.yesterday.taxMoraleCount++;

            if (world.config.continuousMoney) {
                planet.taxIncome(0);
                planet.tradeIncome(0);
            }

            if (planet.population() < 1) {
                planet.owner.ai.onPlanetDied(planet);
                world.scripting.onLost(planet);
                planet.owner.statistics.planetsDied.value++;
                planet.die();
            } else {
                if (planet.morale() <= 15) {
                    planet.owner.ai.onPlanetRevolt(planet);
                    if (planet.lastMorale() > 15) {
                        planet.owner.statistics.planetsRevolted.value++;
                    }
                    if (planet.morale() < 10 && planet.lastMorale() < 10) {
                        revoltPlanet(world, planet);
                    }
                }
            }
        }

        if (planet.owner != null) {
            planet.owner.statistics.totalPopulation.value += planet.population();
            world.statistics.totalPopulation.value += planet.population();
            planet.owner.statistics.planetsOwned.value++;
        }

        return result;
    }
    /**
     * Switch sides for the revolting planet.
     * @param world the world
     * @param planet the planet
     */
    static void revoltPlanet(World world, Planet planet) {
        // find the closest known alien planet
        double d = -1;
        Player newOwner = null;
        for (Planet p : world.planets.values()) {
            if (p.owner != null && p.owner != planet.owner && planet.owner.knowledge(p, PlanetKnowledge.OWNER) >= 0) {
                double d2 = Math.hypot(planet.x - p.x, planet.y - p.y);
                if (d2 < 100) {
                    if (d < 0 || d > d2) {
                        d = d2;
                        newOwner = p.owner;
                    }
                }
            }
        }
        if (d < 0) {
            newOwner = world.players.get("Pirates");
        }

        if (newOwner != null) {
            planet.takeover(newOwner);
            planet.autoBuild = AutoBuild.OFF;
            planet.morale(50);
        } else {
            planet.owner.ai.onPlanetDied(planet);
            world.scripting.onLost(planet);
            planet.owner.statistics.planetsDied.value++;
            planet.die();
        }
    }
    /**
     * Make progress on the active research if any.
     * @param world the world
     * @param player the player
     * @param all the total planet statistics of the player
     * @return true if repaint will be needed
     */
    static boolean progressResearch(World world, Player player, PlanetStatistics all) {
        if (player.runningResearch() != null) {
            Research rs = player.runningResearchProgress();
            if (rs != null) {
                ResearchState last = rs.state;
                int maxpc = rs.getResearchMaxPercent(all);
                // test for max percentage
                if (rs.remainingMoney > 0) {
                    if (rs.getPercent(player.traits) < maxpc) {
                        float rel = 1.0f * rs.assignedMoney / rs.remainingMoney;
                        int dmoney = (int)(rel * world.params().researchSpeed());
                        if (dmoney < player.money()) {
                            rs.remainingMoney = Math.max(0, rs.remainingMoney - dmoney);
                            rs.assignedMoney = Math.min((int)(rs.remainingMoney * rel) + 1, rs.remainingMoney);
                            player.today.researchCost += dmoney;
                            player.addMoney(-dmoney);

                            player.statistics.moneyResearch.value += dmoney;
                            player.statistics.moneySpent.value += dmoney;

                            world.statistics.moneyResearch.value += dmoney;
                            world.statistics.moneySpent.value += dmoney;

                            rs.state = ResearchState.RUNNING;
                            if (last != rs.state) {
                                player.ai.onResearchStateChange(rs.type, rs.state);
                            }
                        } else {
                            rs.state = ResearchState.MONEY;
                            if (last != rs.state) {
                                player.ai.onResearchStateChange(rs.type, rs.state);
                            }
                        }
                    } else {
                        rs.state = ResearchState.LAB;
                        if (last != rs.state) {
                            player.ai.onResearchStateChange(rs.type, rs.state);
                        }
                    }
                }
                // test for completedness
                if (rs.remainingMoney == 0) {
                    player.completeResearch(rs.type);

                    player.ai.onResearchStateChange(rs.type, rs.state);
                    world.scripting.onResearched(player, rs.type);
                }
                return true;
            }
            player.runningResearch(null);
        }
        return false;
    }
    /**
     * Perform the next step of the production process.
     * @param world the world
     * @param player the player
     * @param all the all planet statistics of the player
     * @return need for repaint?
     */
    static boolean progressProduction(World world, Player player, PlanetStatistics all) {
        boolean result = false;
        for (ResearchMainCategory mcat : World.PRODUCTION_CATEGORIES) {
            int capacity;
            switch (mcat) {
            case SPACESHIPS:
                capacity = all.activeProduction.spaceship;
                break;
            case WEAPONS:
                capacity = all.activeProduction.weapons;
                break;
            case EQUIPMENT:
                capacity = all.activeProduction.equipment;
                break;
            default:
                throw new AssertionError("Production of category not supported: " + mcat);
            }
            Collection<Production> prods = player.productionLines(mcat);
            // sum priorities
            int prioritySum = 0;
            for (Production pr : prods) {
                if (pr.type.has(ResearchType.PARAMETER_NEEDS_ORBITAL_FACTORY)
                        && all.orbitalFactory == 0) {
                    continue;
                }
                if (pr.count > 0) {
                    prioritySum += pr.priority;
                }
            }
            if (prioritySum > 0) {
                for (Production pr : new ArrayList<>(prods)) {
                    if (pr.type.has(ResearchType.PARAMETER_NEEDS_ORBITAL_FACTORY)
                            && all.orbitalFactory == 0) {
                        continue;
                    }
                    int targetCap = (int)(1d * capacity * pr.priority * world.params().productionUnit() / prioritySum);
                    if (pr.count == 0) {
                        targetCap = 0;
                    }
                    int currentCap = (int)Math.min(Math.min(
                            player.money(),
                            targetCap
                            ),
                            pr.count * pr.type.productionCost - pr.progress
                    );
                    if (currentCap > 0) {
                        player.addMoney(-currentCap);
                        player.today.productionCost += currentCap;

                        int progress = pr.progress + currentCap;

                        int countDelta = progress / pr.type.productionCost;
                        int progressDelta = progress % pr.type.productionCost;

                        int count0 = pr.count;
                        pr.count = Math.max(0, pr.count - countDelta);
                        pr.progress = progressDelta;

                        int intoInventory = count0 - pr.count;
                        Integer invCount = player.inventory.get(pr.type);
                        player.inventory.put(pr.type, invCount != null ? invCount + intoInventory : intoInventory);

                        player.statistics.moneyProduction.value += currentCap;
                        player.statistics.moneySpent.value += currentCap;

                        world.statistics.moneyProduction.value += currentCap;
                        world.statistics.moneySpent.value += currentCap;

                        player.statistics.productionCount.value += intoInventory;
                        world.statistics.productionCount.value += intoInventory;

                        result = true;

                        if (pr.count == 0) {
                            player.ai.onProductionComplete(pr.type);
                            world.scripting.onProduced(player, pr.type);
                        }
                    }
                }
            }
        }

        return result;
    }
    /**
     * Run the tests for achievements.
     * @param world the world to test for achievements
     * @param dayChange was there a day change?
     */
    static void checkAchievements(World world, boolean dayChange) {
        Profile p = world.env.profile();

        for (String a : AchievementManager.achievements()) {
            if (!p.hasAchievement(a)) {
                AchievementProgress ap = p.getOrCreateProgress(a);
                AchievementManager.get(a).invoke(world, world.player, ap);
                if (ap.isComplete()) {
                    world.env.achievementQueue().add(a);
                    p.save();
                } else
                if (dayChange) {
                    p.save();
                }
            }
        }
        if (dayChange) {
            AchievementProgress ap = p.getOrCreateProgress("achievements.decade");
            ap.displayProgress = true;
            ap.max = 10;
            ap.progress += 1 / 365.2425;

            ap = p.getOrCreateProgress("achievements.oldest_man");
            ap.displayProgress = true;
            ap.max = 100;
            ap.progress += 1 / 365.2425;

            p.save();
        }
    }
    /**
     * Move all fleets.
     * @param world the world
     * @return true if repaint will be needed
     */
    public static boolean moveFleets(World world) {
        boolean radar = false;
        List<Player> ps = U.newArrayList(world.players.values());
        ModelUtils.shuffle(ps);
        for (Player p : ps) {
            radar |= moveFleets(p.ownFleets(), world);
        }
        if (radar) {
            Radar.compute(world);
            world.scripting.onFleetsMoved();
        }
        return radar;
    }
    /**
     * Move fleets.
     * @param playerFleets the list of fleets
     * @param world the world object to indicate battle scenarios
     * @return true if a fleet was moved and the radar needs to be recalculated
     */
    static boolean moveFleets(List<Fleet> playerFleets, World world) {
        boolean invokeRadar = false;
        double stepMultiplier = world.params().fleetSpeed();

        for (Fleet f : playerFleets) {
            // regenerate shields
            regenerateFleet(f);

            // move fleet
            Point2D.Double target = null;
            double targetSpeed = 0.0;
            boolean removeWp = false;
            if (f.targetFleet != null) {
                target = new Point2D.Double(f.targetFleet.x, f.targetFleet.y);
                f.waypoints.clear();
                // if not in radar range any more just move to its last position and stop
                if (!f.owner.fleets.containsKey(f.targetFleet)
                        || !f.owner.withinLimits(f.targetFleet.x, f.targetFleet.y, 1)) {

                    f.owner.ai.onLostTarget(f, f.targetFleet);

                    f.stop();
                } else {
                    targetSpeed = getSpeed(f.targetFleet) * stepMultiplier;
                }
            } else
            if (f.targetPlanet() != null) {
                target = new Point2D.Double(f.targetPlanet().x, f.targetPlanet().y);
                f.waypoints.clear();
            } else
            if (f.waypoints.size() > 0) {
                target = f.waypoints.get(0);
                removeWp = true;
            }
            if (target != null) {
                double dist = Math.sqrt((f.x - target.x) * (f.x - target.x) + (f.y - target.y) * (f.y - target.y));
                double dx = getSpeed(f) * stepMultiplier;
                // if the target has roughly the same speed as our fleet, give a small boost
                if (targetSpeed > 0 && Math.abs(dx - targetSpeed) < 0.5) {
                    dx += 0.5 / 4 * stepMultiplier;
                }

                if (dx >= dist) {
                    f.x = target.x;
                    f.y = target.y;
                    if (removeWp) {
                        f.waypoints.remove(0);
                    }
                    if (f.waypoints.size() == 0) {
                        boolean clearPlanet = false;
                        boolean clearFleet = false;
                        if (f.targetPlanet() != null) {
                            f.owner.ai.onFleetArrivedAtPlanet(f, f.targetPlanet());
                            world.scripting.onFleetAt(f, f.targetPlanet());
                            clearPlanet = true;
                        } else
                        if (f.targetFleet != null) {
                            f.owner.ai.onFleetArrivedAtFleet(f, f.targetFleet);
                            world.scripting.onFleetAt(f, f.targetFleet);
                            clearFleet = f.mode == FleetMode.ATTACK;
                        } else {
                            f.owner.ai.onFleetArrivedAtPoint(f, f.x, f.y);
                            world.scripting.onFleetAt(f, f.x, f.y);
                        }
                        if (f.mode == FleetMode.ATTACK) {
                            handleAttack(world, f);
                        }
                        f.mode = null;
                        if (clearFleet) {
                            f.targetFleet = null;
                        }
                        if (clearPlanet) {
                            f.setTargetPlanet(null);
                        }
                        if (f.task == FleetTask.MOVE) {
                            f.task = FleetTask.IDLE;
                        }
                    }
                } else {
                    double angle = Math.atan2(target.y - f.y, target.x - f.x);
                    f.x = (float)(f.x + Math.cos(angle) * dx);
                    f.y = (float)(f.y + Math.sin(angle) * dx);
                }
                invokeRadar = true;
            } else {
                f.mode = null;
            }
        }

        return invokeRadar;
    }
    /**
     * Handle the case when the fleet reached the target planet.
     * @param world the world
     * @param fleet the fleet
     */
    static void handleAttack(World world, Fleet fleet) {
        Planet targetPlanet = fleet.targetPlanet();
        Fleet targetFleet = fleet.targetFleet;
        Player targetPlayer = targetPlanet != null ? targetPlanet.owner : (targetFleet != null ? targetFleet.owner : null);
        fleet.task = FleetTask.IDLE;
        if (targetPlayer == null) {
            return;
        }
        DiplomaticRelation dr = world.getRelation(fleet.owner, targetPlayer);
        if (dr != null) {
            // do not attack a strong ally
            if (dr.strongAlliance) {
                return;
            }
            // cancel attack if relations improved
            int endHostilitiesThreshold = Math.max(dr.first.endHostilitiesThreshold, dr.second.endHostilitiesThreshold);
            if (dr.value > endHostilitiesThreshold) {
                return;
            }
        }

        BattleInfo bi = new BattleInfo();
        bi.attacker = fleet;
        bi.targetFleet = targetFleet;
        bi.targetPlanet = targetPlanet;
        world.pendingBattles.add(bi);
    }
    /**
     * Regenerate the shields and/or health.
     * @param f the fleet
     */
    static void regenerateFleet(Fleet f) {
        Planet np = f.nearbyPlanet();
        boolean spaceport = f.task != FleetTask.SCRIPT
                && ((np != null
                && np.owner == f.owner
                && np.hasMilitarySpaceport()));
        for (InventoryItem ii : f.inventory.list()) {
            regenerateInventory(spaceport, ii);
        }
        if (spaceport && f.refillOnce) {
            f.refillEquipment();
            f.refillOnce = false;
        }
    }
    /**
     * Regenerate inventory health.
     * @param spaceport has spaceport
     * @param ii the inventory item
     */
    static void regenerateInventory(boolean spaceport,
            InventoryItem ii) {
        boolean engineers = ii.owner.traits.has(TraitKind.ENGINEERS);
        double spd = ii.owner.world.params().speed();
        if (ii.type.category == ResearchSubCategory.SPACESHIPS_STATIONS) {
            spd *= 5;
        } else
        if (engineers) {
            if (!spaceport) {
                spd /= 5;
            } else {
                spd *= 1.2;
            }
        }
        int hpMax = ii.owner.world.getHitpoints(ii.type, ii.owner);
        if (spaceport || engineers || ii.type.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
            if (ii.hp < hpMax) {
                double delta0 = spd;
                ii.hp = Math.min(hpMax, ii.hp + delta0);
            }
            // regenerate slots
            for (InventorySlot is : ii.slots.values()) {
                int m = is.hpMax(ii.owner);
                is.hp = Math.min(m, is.hp + spd / 4);
            }
        }
        int sm = ii.shieldMax();
        if (sm > 0 && ii.shield < sm) {
            double delta = engineers ? spd : 2 * spd;
            ii.shield = (int)Math.min(sm, ii.shield + delta);
        }
    }
    /**
     * Calculates the fleet speed per simulation step.
     * @param f the fleet
     * @return the speed
     */
    static double getSpeed(Fleet f) {
        int fsp = f.getSpeed();
        int fspo = f.owner.world.scripting.fleetSpeedOverride(f, fsp);
        return fspo / 4d;
    }
    /**
     * Compute the tax compensation for the given player and planet numbers.
     * @param p the player
     * @param n the number
     * @return the compensation value
     */
    static double taxCompensation(Player p, int n) {
        if (n < 3) {
            n = 3;
        }
        double k;
        if (p == p.world.player) {
            k = 2.5 / Math.sqrt(n - 2 + p.world.difficulty.ordinal());
        } else {
            k = 2.5 / Math.sqrt(n - p.world.difficulty.ordinal());
        }
        return k;
    }
    /**
     * Handle the restock of the black market.
     * @param player the player object
     */
    static void handleBlackMarket(Player player) {
        int restockTime = player.world.params().blackMarketRestockTime();
        long nowMinutes = player.world.time.getTimeInMillis() / 60000;
        if (player.blackMarketRestock == null || nowMinutes - player.blackMarketRestock.getTime() / 60000 >= restockTime) {
            player.blackMarketRestock = new Date(player.world.time.getTimeInMillis() + restockTime * 60000);
            player.blackMarket.clear();

            List<ResearchType> list = new ArrayList<>(player.availableSet());
            if (list.isEmpty()) {
                return;
            }
            Random rnd = ModelUtils.RANDOM.get();
            Collections.shuffle(list, rnd);
            int max = Math.min(list.size(), list.size() * 3 / 4 + 1);
            list = list.subList(0, max);

            for (ResearchType rt : list) {
                if (rt.category.main == ResearchMainCategory.BUILDINGS) {
                    continue;
                }
                if (rt.level > player.world.level) {
                    continue;
                }
                int count = 0;
                if (rt.category == ResearchSubCategory.SPACESHIPS_FIGHTERS) {
                    count = rnd.nextInt(5) + 1;
                } else
                if (rt.category.main == ResearchMainCategory.EQUIPMENT) {
                    count = rnd.nextInt(6) + 1;
                } else
                if (rt.category == ResearchSubCategory.WEAPONS_TANKS
                        ) {
                    count = rnd.nextInt(3) + 1;
                } else
                if (rt.category == ResearchSubCategory.WEAPONS_VEHICLES
                        ) {
                    count = rnd.nextInt(2) + 1;
                } else
                if (rt.category.main == ResearchMainCategory.WEAPONS) {
                    count = rnd.nextInt(7) + 1;
                } else {
                    count = rnd.nextInt(2) + 1;
                }

                InventoryItem ii = new InventoryItem(player.world.newId(), player, rt);
                ii.count = count;
                ii.init();
                for (InventorySlot is : ii.slots.values()) {
                    if (!is.slot.fixed) {
                        is.count = 0;
                        is.type = null;
                        if (rnd.nextDouble() * 3 >= 1) {
                            List<ResearchType> options =  new ArrayList<>();
                            for (ResearchType rt0 : is.slot.items) {
                                if (player.isAvailable(rt0)) {
                                    options.add(rt0);
                                }
                            }
                            if (!options.isEmpty()) {
                                is.type = ModelUtils.random(options);
                                is.count = rnd.nextInt(is.slot.max) + 1;
                            }
                        }
                    }
                }
                player.blackMarket.add(ii);
            }
        }
    }

    /**
     * Basic reverse comparator to stay within JDK 7.
     */
    static final Comparator<Double> REVERSE_DOUBLE_COMPARATOR = new Comparator<Double>() {
        @Override
        public int compare(Double o1, Double o2) {
            return o2.compareTo(o1);
        }
    };
}
