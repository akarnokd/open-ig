/*
 * Copyright 2008-2011, David Karnok 
 * The file is part of the Open Imperium Galactica project.
 * 
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.mechanics;

import hu.openig.model.AIManager;
import hu.openig.model.BattleInfo;
import hu.openig.model.DiplomaticInteraction;
import hu.openig.model.Player;
import hu.openig.model.ResponseMode;
import hu.openig.model.World;
import hu.openig.utils.XElement;

/**
 * AI for pirate attacks.
 * @author akarnokd, 2011.12.08.
 */
public class AIPirate implements AIManager {

	@Override
	public void prepare(World w, Player p) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void manage() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ResponseMode diplomacy(World world, Player we, Player other,
			DiplomaticInteraction offer) {
		// No diplomatic options
		return ResponseMode.NO;
	}

	@Override
	public void spaceBattle(World world, Player we, BattleInfo battle) {
		// TODO Auto-generated method stub

	}

	@Override
	public void groundBattle(World world, Player we, BattleInfo battle) {
		// No ground battle
	}

	@Override
	public void save(XElement out) {
		// TODO Auto-generated method stub

	}

	@Override
	public void load(XElement in) {
		// TODO Auto-generated method stub

	}

}
