package com.mygdx.manager;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.badlogic.gdx.Gdx;
import com.mygdx.assets.ItemAssets;
import com.mygdx.currentState.BagInfo;
import com.mygdx.enums.ItemEnum;
import com.mygdx.model.item.Consumables;
import com.mygdx.model.item.Equipment;
import com.mygdx.model.item.Item;

public class BagManager {
	@Autowired
	private ItemAssets itemAssets;
	@Autowired
	private BagInfo bagInfo;

	public void possessItem(ItemEnum itemType, String itemName) {
		Item item;
		switch (itemType) {
			case HANDGRIP :
				item = itemAssets.getHandGrip(itemName);
				addEquipment((Equipment) item);
				break;
			case ACCESSORY :
				item = itemAssets.getAccessory(itemName);
				addEquipment((Equipment) item);
				break;
			case CLOTHES :
				item = itemAssets.getClothes(itemName);
				addEquipment((Equipment) item);
				break;
			case CONSUMABLES :
				item = itemAssets.getConsumables(itemName);
				addConsumables((Consumables) item);
				break;
			case ETC_ITEM :
				item = itemAssets.getEtcItem(itemName);
				addEtcItem(item);
				break;
			default :
				Gdx.app.log("BagManager", "itemType 정보 오류");
				break;
		}
	}

	public void dropItem(Item item) {
		Gdx.app.log("BagManager", item.getName() + "를 버렸다.");
		if (item instanceof Equipment) {
			removeEquipment((Equipment) item);
		} else if (item instanceof Consumables) {
			removeConsumables((Consumables) item);
		} else {
			removeEtcItem(item);
		}
	}

	public void addEquipment(Equipment equipment) {
		getEquipmentList().add(equipment);
	}

	public void removeEquipment(Equipment equipment) {
		getEquipmentList().remove(equipment);
	}

	public List<Equipment> getEquipmentList() {
		return bagInfo.getEquipmentList();
	}

	public List<Consumables> getConsumablesList() {
		return bagInfo.getConsumablesList();
	}

	public void removeConsumables(Consumables consumables) {
		getConsumablesList().remove(consumables);
	}

	public void addConsumables(Consumables consumables) {
		getConsumablesList().add(consumables);
	}

	public List<Item> getEtcItemList() {
		return bagInfo.getEtcItemList();
	}

	public void removeEtcItem(Item item) {
		getEtcItemList().remove(item);
	}

	public void addEtcItem(Item item) {
		getEtcItemList().add(item);
	}
}
