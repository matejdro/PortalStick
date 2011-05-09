package com.matejdro.bukkit.portalstick.util;

import java.util.Arrays;

public enum RegionSetting {
	
	ENABLE_PORTALS("enable-portalstick", true),
	TELEPORT_VEHICLES("teleport-vehicles", true),
	TELEPORT_LIQUIDS("teleport-liquids", true),
	INFINITE_DISPENSERS("infinite-dispensers", true),
	CHECK_WORLDGUARD("obey-worldguard-permissions", false),
	ENABLE_GRILLS("enable-emancipation-grill", true),
	DELETE_ON_EXITENTRANCE("delete-portals-on-exitentrance", true),
	GRILLS_CLEAR_INVENTORY("grills-clear-inventory", true),
	GRILLS_CLEAR_ITEM_DROPS("grills-clear-item-drops", true),
	GRILL_MATERIAL("emancipation-grill-material", "48"),
	TRANSPARENT_BLOCKS("transparent-blocks", Arrays.asList(new Integer[]{0,8,9,10,11,20})),
	PORTAL_BLOCKS("portallable-blocks", Arrays.asList(new Integer[]{1})),
	ALL_BLOCKS_PORTAL("all-blocks-allow-portals", false),
	UNIQUE_INVENTORY("unique-inventory", false),
	ENABLE_FALL_DAMAGE_BOOTS("enable-fall-damage-boots", true),
	FALL_DAMAGE_BOOTS("fall-damage-boots", 313),
	VELOCITY_MULTIPLIER("velocity-multiplier", 1.0),
	ENABLE_GELS("enable-gels", true),
	LOCATION("location", "world:0,0,0:0,0,0");
	
	private String yaml;
	private Object def;
	
	private RegionSetting(String yaml, Object def) {
		this.yaml = yaml;
		this.def = def;
	}
	
	public String getYaml() {
		return yaml;
	}
	
	public Object getDefault() {
		return def;
	}
	
}
