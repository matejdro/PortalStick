package com.matejdro.bukkit.portalstick;

import java.util.HashMap;

import org.bukkit.Location;

import com.matejdro.bukkit.portalstick.util.Config;

public class RegionManager {
	
	private static HashMap<String, Region> regions = new HashMap<String, Region>();
	
	public static Region loadRegion(String name) {
		Region region = getRegion(name);
		if (region == null)
			region = new Region(name);
		Config.loadRegionSettings(region);
		regions.put(name, region);
		return region;
	}
	
	public static void deleteRegion(String name) {
		Region region = getRegion(name);
		Config.deleteRegion(name);
		regions.remove(region);
	}
	
	public static void createRegion(String name, Location one, Location two) {
		Region region = loadRegion(name);
		region.setLocation(one, two);
		Config.saveAll();
	}
	
	public static Region getRegion(Location location) {
		for (Region region : regions.values())
			if (region.contains(location.toVector()) && location.getWorld().getName().equalsIgnoreCase(region.World) && !region.Name.equalsIgnoreCase("global"))
				return region;
		return getRegion("global");
	}
	
	public static Region getRegion(String name) {
		return regions.get(name.toLowerCase());
	}
	
	public static HashMap<String, Region> getRegionMap() {
		return regions;
	}

}
