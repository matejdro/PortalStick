package com.matejdro.bukkit.portalstick;

import java.util.HashMap;

import org.bukkit.Location;

import com.matejdro.bukkit.portalstick.util.Config;

public class RegionManager {
	
	public final HashMap<String, Region> regions = new HashMap<String, Region>();
	
	public Region loadRegion(String name) {
		Region region = getRegion(name);
		if (region == null)
			region = new Region(name);
		Config.loadRegionSettings(region);
		regions.put(name, region);
		return region;
	}
	
	public void deleteRegion(String name) {
		Region region = getRegion(name);
		regions.remove(region.name);
		Config.deleteRegion(name);
	}
	
	public void createRegion(String name, Location one, Location two) {
		Region region = loadRegion(name);
		region.setLocation(one, two);
		Config.saveAll();
	}
	
	public Region getRegion(Location location) {
		for (Region region : regions.values())
			if (region.contains(location.toVector()) && location.getWorld().getName().equalsIgnoreCase(region.world) && !region.name.equalsIgnoreCase("global"))
				return region;
		return getRegion("global");
	}
	
	public Region getRegion(String name) {
		return regions.get(name.toLowerCase());
	}
	
}
