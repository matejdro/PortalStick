package com.matejdro.bukkit.portalstick.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.entity.Player;
import org.bukkit.util.config.Configuration;

import com.matejdro.bukkit.portalstick.Bridge;
import com.matejdro.bukkit.portalstick.FunnelBridgeManager;
import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.GrillManager;
import com.matejdro.bukkit.portalstick.PortalManager;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.UserManager;

public class Config {
	
	public static PortalStick plugin;
	private static Configuration mainConfig;
	private static Configuration regionConfig;
	private static Configuration grillConfig;
	private static Configuration bridgeConfig;
	
	public static HashSet<String> DisabledWorlds;
	public static boolean DeleteOnQuit;
	public static int PortalTool;
	public static boolean CompactPortal;
	public static Region GlobalRegion;
	public static int RegionTool;
	public static boolean RestoreInvOnWorldChange;
	public static List<String> ColorPresets;
	public static int FillPortalBack;
	
	public static String MessageCannotPlacePortal;
	
	public static Boolean useBukkitContribSounds;
	public static int soundRange;
	public static String[] soundUrls = new String[Sound.values().length];
	public static String[] soundNotes = new String[Sound.values().length];
	
	public Config (PortalStick instance) {
		
		plugin = instance;
		mainConfig = plugin.getConfiguration();
		regionConfig = getConfigFile("regions.yml");
		grillConfig = getConfigFile("grills.yml");
		bridgeConfig = getConfigFile("bridges.yml");
		
		load();
	}
	
	public static void deleteGrill(String grill) {
		List<String> list = grillConfig.getStringList("grills", null);
		list.remove(grill);
		grillConfig.setProperty("grills", list);
		saveAll();
	}
	
	public static void deleteRegion(String name) {
		regionConfig.removeProperty("regions." + name);
		saveAll();
	}
	
	public static void deleteBridge(String bridge) {
		List<String> list = bridgeConfig.getStringList("bridges", null);
		list.remove(bridge);
		bridgeConfig.setProperty("bridges", list);
		saveAll();
	}

	
	public static void load() {
		
		mainConfig.load();
		regionConfig.load();
		grillConfig.load();
		bridgeConfig.load();
		//Load messages
		MessageCannotPlacePortal = getString("messages.cannot-place-portal", "&cCannot place a portal there!");
        
        //Load main settings
        DisabledWorlds = new HashSet<String>(getStringList("main.disabled-worlds", new ArrayList<String>()));
        DeleteOnQuit = getBoolean("main.delete-on-quit", false);
        PortalTool = getInt("main.portal-tool", 280);
        CompactPortal = getBoolean("main.compact-portal", false);
        RegionTool = getInt("main.region-tool", 268);
        RestoreInvOnWorldChange = getBoolean("main.restore-inventory-on-world-change", true);
        ColorPresets = getStringList("main.portal-color-presets", Arrays.asList(new String[]{"11-1","2-6","9-10","5-13","8-7","15-4"}));
        FillPortalBack = getInt("main.fill-portal-back", -1);
        
        //Load sound settings
        useBukkitContribSounds = getBoolean("sounds.use-bukkitcontrib-sounds", true);
        soundRange = getInt("sounds.sound-range", 20);
        
        soundUrls[Sound.PORTAL_CREATE_BLUE.ordinal()] = getString("sounds.create-blue-portal-url", "");
        soundUrls[Sound.PORTAL_CREATE_ORANGE.ordinal()] = getString("sounds.create-orange-portal-url", "");
        soundUrls[Sound.PORTAL_EXIT_BLUE.ordinal()] = getString("sounds.exit-blue-portal-url", "");
        soundUrls[Sound.PORTAL_EXIT_ORANGE.ordinal()] = getString("sounds.exit-orange-portal-url", "");
        soundUrls[Sound.PORTAL_CANNOT_CREATE.ordinal()] = getString("sounds.cannot-create-portal-url", "");
        soundUrls[Sound.GRILL_EMANCIPATE.ordinal()] = getString("sounds.grill-emancipate-url", "");
        soundUrls[Sound.FAITHPLATE_LAUNCH.ordinal()] = getString("sounds.faith-plate-launch-url", "");
        soundUrls[Sound.GEL_BLUE_BOUNCE.ordinal()] = getString("sounds.blue-gel-bounce-url", "");

        soundNotes[Sound.PORTAL_CREATE_BLUE.ordinal()] = getString("sounds.create-blue-portal-note", "");
        soundNotes[Sound.PORTAL_CREATE_ORANGE.ordinal()] = getString("sounds.create-orange-portal-note", "");
        soundNotes[Sound.PORTAL_EXIT_BLUE.ordinal()] = getString("sounds.exit-blue-portal-note", "");
        soundNotes[Sound.PORTAL_EXIT_ORANGE.ordinal()] = getString("sounds.exit-orange-portal-note", "");
        soundNotes[Sound.PORTAL_CANNOT_CREATE.ordinal()] = getString("sounds.cannot-create-portal-note", "");
        soundNotes[Sound.GRILL_EMANCIPATE.ordinal()] = getString("sounds.grill-emancipate-note", "");
        soundNotes[Sound.FAITHPLATE_LAUNCH.ordinal()] = getString("sounds.faith-plate-launch-note", "4-5");
        soundNotes[Sound.GEL_BLUE_BOUNCE.ordinal()] = getString("sounds.blue-gel-bounce-note", "4-5");

		//Load all current users
		for (Player player : plugin.getServer().getOnlinePlayers())
			UserManager.createUser(player);
		
        //Load all regions
        if (regionConfig.getKeys("regions") != null)
        	for (String regionName : regionConfig.getKeys("regions"))
        		RegionManager.loadRegion(regionName);
        RegionManager.loadRegion("global");
        Util.info(RegionManager.getRegionMap().size() + " region(s) loaded");
        
        //Load grills
        for (String grill : grillConfig.getStringList("grills", null))
        	GrillManager.loadGrill(grill);
        Util.info(GrillManager.grills.size() + " grill(s) loaded");
        //Load bridges
        for (String bridge : bridgeConfig.getStringList("bridges", null))
        	FunnelBridgeManager.loadBridge(bridge);
        Util.info(FunnelBridgeManager.bridges.size() + " bridge(s) loaded");
        
        saveAll();
		
	}
	
	private static int getInt(String path, int def)
	{
		if (mainConfig.getProperty(path) == null)
			mainConfig.setProperty(path, def);
		
		return mainConfig.getInt(path, def);
	}
	
	private static String getString(String path, String def)
	{
		if (mainConfig.getProperty(path) == null)
			mainConfig.setProperty(path, def);
		
		return mainConfig.getString(path, def);
	}
	
	private static List<String> getStringList(String path, List<String> def)
	{
		if (mainConfig.getProperty(path) == null)
			mainConfig.setProperty(path, def);
		
		return mainConfig.getStringList(path, def);
	}
	
	private static Boolean getBoolean(String path, Boolean def)
	{
		if (mainConfig.getProperty(path) == null)
			mainConfig.setProperty(path, def);
		
		return mainConfig.getBoolean(path, def);
	}
	
	public static void reLoad() {
		unLoad();
		load();
	}
	
	public static void unLoad() {
		
		FunnelBridgeManager.deleteAll();
		PortalManager.deleteAll();
		GrillManager.deleteAll();
		for (Map.Entry<String, User> entry : UserManager.getUserList().entrySet()) {
			User user = entry.getValue();
			Player player = plugin.getServer().getPlayer(entry.getKey());
			if (player != null) {
				if (!RegionManager.getRegion(player.getLocation()).Name.equalsIgnoreCase("global"))
					user.revertInventory(player);
			}
			UserManager.deleteUser(user);
		}
		
	}
	
	public static void loadRegionSettings(Region region) {
		for (RegionSetting setting : RegionSetting.values()) {
			Object prop = regionConfig.getProperty("regions." + region.Name + "." + setting.getYaml());
    		if (prop == null)
    			region.settings.put(setting, setting.getDefault());
    		else
    			region.settings.put(setting, prop);
    		regionConfig.setProperty("regions." + region.Name + "." + setting.getYaml(), region.settings.get(setting));
    	}
		region.updateLocation();
	}
	
	private static Configuration getConfigFile(String filename) {
		Configuration configfile = null;
		File file = new File(plugin.getDataFolder(), filename);
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
	
	public static void saveAll() {
		
		//Save regions
		for (Map.Entry<String, Region> entry : RegionManager.getRegionMap().entrySet()) {
			Region region = entry.getValue();
			for (Entry<RegionSetting, Object> setting : region.settings.entrySet())
				regionConfig.setProperty("regions." + region.Name + "." + setting.getKey().getYaml(), setting.getValue());
		}
		if (!regionConfig.save())
			Util.severe("Error while writing to regions.yml");
		
		//Save grills
		grillConfig.removeProperty("grills");
		List<String> list = new ArrayList<String>();
		for (Grill grill : GrillManager.getGrillList())
			list.add(grill.getStringLocation());
		grillConfig.setProperty("grills", list);
		if (!grillConfig.save())
			Util.severe("Error while writing to grills.yml");
		
		//Save bridges
		bridgeConfig.removeProperty("bridges");
		list = new ArrayList<String>();
		for (Bridge bridge : FunnelBridgeManager.bridges)
			list.add(bridge.getStringLocation());
		bridgeConfig.setProperty("bridges", list);
		if (!bridgeConfig.save())
			Util.severe("Error while writing to bridges.yml");
		
		//Save main
		if (!mainConfig.save())
			Util.severe("Error while writing to config.yml");
			
	}
	
	public static enum Sound {
		PORTAL_CREATE_BLUE,
		PORTAL_CREATE_ORANGE,
		PORTAL_EXIT_BLUE,
		PORTAL_EXIT_ORANGE,
		PORTAL_CANNOT_CREATE,
		GRILL_EMANCIPATE,
		FAITHPLATE_LAUNCH,
		GEL_BLUE_BOUNCE
	}
	
}