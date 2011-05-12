package com.matejdro.bukkit.portalstick.util;

import java.util.Arrays;

public enum RegionSetting {
	
	ENABLE_PORTALS("enable-portals", true, true),
	TELEPORT_VEHICLES("teleport-vehicles", true, true),
	TELEPORT_LIQUIDS("teleport-liquids", true, true),
	INFINITE_DISPENSERS("infinite-dispensers", true, true),
	CHECK_WORLDGUARD("obey-worldguard-permissions", false, true),
	ENABLE_GRILLS("enable-emancipation-grill", true, true),
	DELETE_ON_EXITENTRANCE("delete-portals-on-exitentrance", true, true),
	GRILLS_CLEAR_INVENTORY("grills-clear-inventory", true, true),
	GRILLS_CLEAR_ITEM_DROPS("grills-clear-item-drops", true, true),
	GRILL_MATERIAL("emancipation-grill-material", "48", true),
	TRANSPARENT_BLOCKS("transparent-blocks", Arrays.asList(new Integer[]{0,8,9,10,11,20})),
	PORTAL_BLOCKS("portallable-blocks", Arrays.asList(new Integer[]{1})),
	ALL_BLOCKS_PORTAL("all-blocks-allow-portals", false, true),
	UNIQUE_INVENTORY("unique-inventory", false, true),
	ENABLE_FALL_DAMAGE_BOOTS("enable-fall-damage-boots", true, true),
	FALL_DAMAGE_BOOTS("fall-damage-boots", 313, true),
	VELOCITY_MULTIPLIER("velocity-multiplier", 1.0, true),
	PREVENT_PORTAL_THROUGH_PORTAL("prevent-creating-portal-through-portal", false, true),
	LOCATION("location", "world:0,0,0:0,0,0");
	
	private String yaml;
	private Object def;
	private boolean editable;
	
	private RegionSetting(String yaml, Object def) {
		this.yaml = yaml;
		this.def = def;
		this.editable = false;
	}
	
	private RegionSetting(String yaml, Object def, boolean editable) {
		this.yaml = yaml;
		this.def = def;
		this.editable = editable;
	}
	
	public String getYaml() {
		return yaml;
	}
	
	public Object getDefault() {
		return def;
	}
	
	public boolean getEditable() {
		return editable;
	}
	
}
