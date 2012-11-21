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
	GRILLS_REMOVE_ITEMS("grills-clear-inventory", true, true),
	GRILL_REMOVE_EXCEPTIONS("grill-inventory-clear-exceptions", Arrays.asList(new String[]{"280,313"}), false),
	GRILL_MATERIAL("emancipation-grill-material", "48", true),
	GRILLS_CLEAR_ITEM_DROPS("grills-clear-item-drops", true, true),
	ENABLE_GRILL_REDSTONE_DISABLING("enable-disabling-grill-with-redstone", true, true),
	ENABLE_HARD_GLASS_BRIDGES("enable-hard-glass-bridges", true, true),
	HARD_GLASS_BRIDGE_BASE_MATERIAL("hard-light-bridge-base-block", "42", true),
	FUNNEL_BASE_MATERIAL("excursion-funnel-base-block", "41", true),
	ENABLE_BRIDGE_REDSTONE_DISABLING("enable-disabling-glass-bridge-with-redstone", true, true),
	TRANSPARENT_BLOCKS("transparent-blocks", Arrays.asList(new Integer[]{0,8,9,10,11,20,64,71,96,65,102,132}), false),
	PORTAL_BLOCKS("portallable-blocks", Arrays.asList(new Integer[]{42}), false),
	ALL_BLOCKS_PORTAL("all-blocks-allow-portals", false, true),
	UNIQUE_INVENTORY("unique-inventory", false, true),
	UNIQUE_INVENTORY_ITEMS("unique-inventory-items", Arrays.asList(new String[]{"280,1"}), false),
	ENABLE_FALL_DAMAGE_BOOTS("enable-fall-damage-boots", true, true),
	FALL_DAMAGE_BOOTS("fall-damage-boots", 313, true),
	VELOCITY_MULTIPLIER("velocity-multiplier", 1.0, true),
	PREVENT_PORTAL_THROUGH_PORTAL("prevent-creating-portal-through-portal", false, true),
	PREVENT_PORTAL_CLOSED_DOOR("prevent-creating-portal-through-closed-door", true, true),
	ENABLE_AERIAL_FAITH_PLATES("enable-aerial-faith-plates", true, true),
	ENABLE_BLUE_GEL_BLOCKS("enable-blue-gel-blocks", true, true),
	ENABLE_RED_GEL_BLOCKS("enable-red-gel-blocks", true, true),
	BLUE_GEL_BLOCK("blue-gel-block", "35:3", true),
	RED_GEL_BLOCK("red-gel-block", "35:1", true),
//	BLUE_GEL_VERTICAL_VELOCITY_MULTIPLIER("blue-gel-horizontal-velocity-multiplier", 1.5),
//	BLUE_GEL_HORIZONTAL_VELOCITY_MULTIPLIER("blue-gel-horizontal-velocity-multiplier", 1.5),
//	BLUE_GEL_VERTICAL_BOUNCE_VELOCITY("blue-gel-vertical-bounce-velocity", 1.0),
//	BLUE_GEL_HORIZONTAL_BOUNCE_VELOCITY("blue-gel-horizontal-bounce-velocity", 1.0),
	RED_GEL_VELOCITY_MULTIPLIER("red-gel-velocity-multiplier", 1.5, true),
	FAITH_PLATE_BLOCK("aerial-faith-plate-block", "57", true),
	FAITH_PLATE_POWER("aerial-faith-plate-power", "6-2", true),
	ENABLE_SOUNDS("enable-sounds", true, true),
	PROTECT_PORTALS_FROM_TNT("protect-portals-from-tnt", false, true),
	ENABLE_REDSTONE_TRANSFER("enable-transferring-redstone-current", true, true),
	ENABLE_PISTON_BLOCK_TELEPORT("enable-teleporting-blocks-moved-by-pistons", true, true),
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
