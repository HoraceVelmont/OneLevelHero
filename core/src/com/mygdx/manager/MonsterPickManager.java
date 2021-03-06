package com.mygdx.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.mygdx.assets.NodeAssets;
import com.mygdx.assets.UnitAssets;
import com.mygdx.enums.FieldTypeEnum;
import com.mygdx.model.unit.Monster;

/**
 * WorldMap에서 MovingInfoManager를 통해 설정된 MovingInfo를 받아와서 Monster를 만들어낸다.
 * 
 * @author Velmont
 * 
 */

public class MonsterPickManager {
	@Autowired
	private UnitAssets unitAssets;
	@Autowired
	private NodeAssets nodeAssets;
	@Autowired
	private PositionManager positionManager;
	@Autowired
	private BattleManager battleManager;
	@Autowired
	private FieldManager fieldManager;
	@Autowired
	private UnitManager unitManager;

	public Monster createMonster() {
		Monster monster = unitAssets.getMonster(selectMonster());
		return monster;
	}

	public Monster createMonster(String monsterName) {
		Monster monster = unitAssets.getMonster(monsterName);
		unitManager.initiateMonster(monster);
		return monster;
	}

	private String selectMonster() {
		FieldTypeEnum fieldType = fieldManager.getFieldType();
		List<String> monsterStrings = nodeAssets.getMonsterFieldListByFieldType(fieldType);
		// FIXME : 랜덤로직
		int randomInt = (int) (Math.random() * monsterStrings.size());
		String selectedMonsterString = monsterStrings.get(randomInt);
		return selectedMonsterString;
	}
}
