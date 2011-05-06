package com.matejdro.bukkit.portalstick.util;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;

import org.bukkit.util.config.Configuration;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;

public class Config {
	
	public static PortalStick plugin;
	private static Configuration mainConfig;
	private static Configuration regionConfig;
	private static Configuration grillConfig;
	
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
		mainConfig = plugin.getConfiguration();
		mainConfig.load();
		regionConfig = getConfigFile("regions.yml");
		grillConfig = getConfigFile("grills.yml");
		
		//Check main settings
		List<String> keys = mainConfig.getKeys("main");
		if (!keys.contains("enabled-worlds"))
			mainConfig.setProperty("main.enabled-worlds", plugin.getServer().getWorlds().get(0));
		if (!keys.contains("compact-portal"))
			mainConfig.setProperty("main.compact-portal", false);
		if (!keys.contains("delete-on-quit"))
			mainConfig.setProperty("main.delete-on-quit", false);
		if (!keys.contains("portal-tool"))
			mainConfig.setProperty("main.portal-tool", 280);
		if (!keys.contains("region-tool"))
			mainConfig.setProperty("main.region-tool", 268);
		
		//Check messages
		keys = mainConfig.getKeys("messages");
		if (!keys.contains("cannot-place-portal"))
			mainConfig.setProperty("messages.cannot-place-portal", "Cannot place a portal there!");
		if (!keys.contains("portal-stick-enabled"));
			mainConfig.setProperty("messages.portal-stick-enabled", "You have just turned your crappy piece of wood into Aperture Science Handheld Portal Stick!");
		if (!keys.contains("portal-stick-disabled"));
			mainConfig.setProperty("messages.portal-stick-enabled", "You have just reverted your Aperture Science Handheld Portal Stick back into crappy piece of wood!");
		if (!keys.contains("restricted-world"))
			mainConfig.setProperty("messages.restricted-world", "You cannot do that in this world!");

		//Load messages
		MessageCannotPlacePortal = mainConfig.getString("messages.cannot-place-portal");
		MessagePortalStickEnabled = mainConfig.getString("messages.portal-stick-enabled");
		MessagePortalStickDisabled = mainConfig.getString("messages.portal-stick-disabled");
		MessageRestrictedWorld = mainConfig.getString("messages.restricted-world");
        
        //Load main settings
        EnabledWorlds = new HashSet<String>(mainConfig.getStringList("main.enabled-worlds", null));
        DeleteOnQuit = mainConfig.getBoolean("main.delete-on-quit", false);
        PortalTool = mainConfig.getInt("main.portal-tool", 280);
        CompactPortal = mainConfig.getBoolean("main.compact-portal", false);
        RegionTool = mainConfig.getInt("main.region-tool", 268);
        
        //Load all regions
        for (String regionName : regionConfig.getKeys("regions"))
        	RegionManager.loadRegion(regionName);
        
        save();

	}
	
	public static void loadRegionSettings(Region region) {
		for (RegionSetting setting : RegionSetting.values()) {
			Object prop = regionConfig.getProperty(mkNode(region.Name, setting.getYaml()));
    		if (prop == null)
    			region.settings.put(setting, setting.getDefault());
    		else
    			region.settings.put(setting, prop);
    		regionConfig.setProperty(mkNode(region.Name, setting.getYaml()), region.settings.get(setting));
    	}
		region.updateLocation();
	}
	
	private static Configuration getConfigFile(String filename) {
		Configuration configfile = null;
		File file = new File(plugin.getDataFolder(), filename);
		if (!file.exists()) {
			try {
				file.createNewFile();
				PrintWriter output = new PrintWriter(file);
				output.println("setup: false");
				output.close();
			} catch (Exception e) {
				Util.severe("Unable to create file " + filename);
			}
		}
		try {
			configfile = new Configuration(file);
			configfile.load();
			configfile.removeProperty("setup");
			configfile.save();
		} catch (Exception e) {
			Util.severe("Unable to load YAML file " + filename);
		}
		return configfile;
	}
	
	private static String mkNode(String node, String region) {
		if (region.equalsIgnoreCase("global"))
			return "global." + node;
		return "regions." + region + "." + node;
	}
	
	private static void save() {
		if (!mainConfig.save())
			Util.severe("Error while writing to config.yml");
	}
	
}