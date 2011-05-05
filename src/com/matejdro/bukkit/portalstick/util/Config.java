package com.matejdro.bukkit.portalstick.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.util.config.Configuration;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;

public class Config {
	
	public static PortalStick plugin;
	private static Configuration config;
	private static List<Region> regions = new ArrayList<Region>();
	
	public static HashSet<String> EnabledWorlds;
	public static boolean DeleteOnQuit;
	public static int PortalTool;
	public static boolean CompactPortal;
	public static Region GlobalRegion;
	public static int RegionTool;
	public static String MessageCannotPlacePortal;
	public static String MessagePortalStickEnabled;
	public static String MessagePortalStickDisabled;
	public static String MessageRestrictedWorld;

	public Config (PortalStick instance) {
		
		plugin = instance;
		config = plugin.getConfiguration();
		config.load();
		
		//Check main settings
		List<String> keys = config.getKeys("main");
		if (!keys.contains("enabled-worlds"))
			config.setProperty("main.enabled-worlds", plugin.getServer().getWorlds().get(0));
		if (!keys.contains("compact-portal"))
			config.setProperty("main.compact-portal", false);
		if (!keys.contains("delete-on-quit"))
			config.setProperty("main.delete-on-quit", false);
		if (!keys.contains("portal-tool"))
			config.setProperty("main.portal-tool", 280);
		if (!keys.contains("region-tool"))
			config.setProperty("main.region-tool", 268);
		
		//Check messages
		keys = config.getKeys("messages");
		if (!keys.contains("cannot-place-portal"))
			config.setProperty("messages.cannot-place-portal", "Cannot place a portal there!");
		if (!keys.contains("portal-stick-enabled"));
			config.setProperty("messages.portal-stick-enabled", "You have just turned your crappy piece of wood into Aperture Science Handheld Portal Stick!");
		if (!keys.contains("portal-stick-disabled"));
			config.setProperty("messages.portal-stick-enabled", "You have just reverted your Aperture Science Handheld Portal Stick back into crappy piece of wood!");
		if (!keys.contains("restricted-world"))
			config.setProperty("messages.restricted-world", "You cannot do that in this world!");

		//Load messages
		MessageCannotPlacePortal = config.getString("messages.cannot-place-portal");
		MessagePortalStickEnabled = config.getString("messages.portal-stick-enabled");
		MessagePortalStickDisabled = config.getString("messages.portal-stick-disabled");
		MessageRestrictedWorld = config.getString("messages.restricted-world");
		
		//Load global region
		GlobalRegion = loadRegion("global");
        
        //Load main settings
        EnabledWorlds = new HashSet<String>(config.getStringList("main.enabled-worlds", null));
        DeleteOnQuit = config.getBoolean("main.delete-on-quit", false);
        PortalTool = config.getInt("main.portal-tool", 280);
        CompactPortal = config.getBoolean("main.compact-portal", false);
        RegionTool = config.getInt("main.region-tool", 268);
        
        //Load all regions
        for (String regionName : config.getKeys("regions"))
        	regions.add(loadRegion(regionName));
		
		//Attempt save
		if (!config.save())
			Util.severe("Error while writing to config.yml");

	}

	public static Region getRegion(Location location) {
		for (Region region : regions.toArray(new Region[0]))
			if (region.contains(location.toVector()) && location.getWorld().getName().equalsIgnoreCase(region.World))
				return region;
		return GlobalRegion;
	}
	
	public static void createRegion(Location one, Location two, String name) {
		Region region = loadRegion(name);
		region.settings.remove(Setting.LOCATION);
		region.settings.put(Setting.LOCATION, one.getWorld().getName() + ":" + one.toVector().toString() + ":" + two.toVector().toString());
		region.updateLocation();
	}
	
	private static Region loadRegion(String regionName) {
		Region region = new Region(regionName);
		for (Setting setting : Setting.values()) {
			Object prop = config.getProperty(mkNode(region.Name, setting.getYaml()));
    		if (prop == null)
    			region.settings.put(setting, setting.getDefault());
    		else
    			region.settings.put(setting, prop);
    		config.setProperty(mkNode(region.Name, setting.getYaml()), region.settings.get(setting));
    	}
		region.updateLocation();
		return region;
	}
	
	private static String mkNode(String node, String region) {
		if (region.equalsIgnoreCase("global"))
			return "global." + node;
		return "regions." + region + "." + node;
	}
	
}