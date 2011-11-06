package com.matejdro.bukkit.portalstick.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

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
	private static FileConfiguration mainConfig;
	private static FileConfiguration regionConfig;
	private static FileConfiguration grillConfig;
	
	private static File mainConfigFile;
	private static File regionConfigFile;
	private static File grillConfigFile;
	
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

	public Config (PortalStick instance) {
		
		plugin = instance;
		
		mainConfigFile = getConfigFile("config.yml");
		regionConfigFile = getConfigFile("regions.yml");
		grillConfigFile = getConfigFile("grills.yml");
		
		mainConfig = getConfig(mainConfigFile);
		regionConfig = getConfig(regionConfigFile);
		grillConfig = getConfig(grillConfigFile);
		
		load();

	}
	
	public static void deleteGrill(String grill) {
		List<String> list = (List<String>) grillConfig.getList("grills", null);
		list.remove(grill);
		grillConfig.set("grills", list);
		saveAll();
	}
	
	public static void deleteRegion(String name) {
		regionConfig.set("regions." + name, null);
		saveAll();
	}
	
	public static void load() {
		
		try {
			mainConfig.load(mainConfigFile);
			regionConfig.load(regionConfigFile);

			grillConfig.load(grillConfigFile);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (InvalidConfigurationException e) {
			Util.severe("Unable to load config!");
			e.printStackTrace();
		}
		
		//Load messages
		MessageCannotPlacePortal = mainConfig.getString("messages.cannot-place-portal", "&cCannot place a portal there!");
        
        //Load main settings
        DisabledWorlds = new HashSet<String>(getStringList("main.disabled-worlds", new ArrayList<String>()));
        DeleteOnQuit = getBoolean("main.delete-on-quit", false);
        PortalTool = getInt("main.portal-tool", 280);
        CompactPortal = getBoolean("main.compact-portal", false);
        RegionTool = getInt("main.region-tool", 268);
        RestoreInvOnWorldChange = getBoolean("main.restore-inventory-on-world-change", true);
        ColorPresets =  getStringList("main.portal-color-presets", Arrays.asList(new String[]{"11-1","2-6","9-10","5-13","8-7","15-4"}));
        FillPortalBack = mainConfig.getInt("main.fill-portal-back", -1);
		
		//Load all current users
		for (Player player : plugin.getServer().getOnlinePlayers())
			UserManager.createUser(player);
		
        //Load all regions
        if (regionConfig.getConfigurationSection("regions") != null)
        	for (String regionName : regionConfig.getConfigurationSection("regions").getKeys(false))
        		RegionManager.loadRegion(regionName);
        RegionManager.loadRegion("global");
        Util.info(RegionManager.getRegionMap().size() + " region(s) loaded");
        
        //Load grills
        for (String grill : ((List<String>) grillConfig.getList("grills", new ArrayList<String>())).toArray(new String[0]))
        	GrillManager.loadGrill(grill);
        Util.info(GrillManager.grills.size() + " grill(s) loaded");
        
        saveAll();
		
	}
	
	private static int getInt(String path, int def)
	{
		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);
	
		return mainConfig.getInt(path, def);
	}

	private static String getString(String path, String def)
	{
		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);

		return mainConfig.getString(path, def);
	}

	private static List<String> getStringList(String path, List<String> def)
	{
		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);

	return (List<String>) mainConfig.getList(path, def);
	}

	private static Boolean getBoolean(String path, Boolean def)
	{
		if (mainConfig.get(path) == null)
			mainConfig.set(path, def);

		return mainConfig.getBoolean(path, def);
	}
	
	public static void reLoad() {
		unLoad();
		load();
	}
	
	public static void unLoad() {
		
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
			Object prop = regionConfig.get("regions." + region.Name + "." + setting.getYaml());
    		if (prop == null)
    			region.settings.put(setting, setting.getDefault());
    		else
    			region.settings.put(setting, prop);
    		regionConfig.set("regions." + region.Name + "." + setting.getYaml(), region.settings.get(setting));
    	}
		region.updateLocation();
	}
	
	private static File getConfigFile(String filename)
	{
		if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdir();
		
		File file = new File(plugin.getDataFolder(), filename);
		return file;
	}
	private static FileConfiguration getConfig(File file) {
		FileConfiguration config = null;
		try {
			config = new YamlConfiguration();
			if (file.exists())
			{
				config.load(file);
				config.set("setup", null);
			}
			config.save(file);
			
			return config;
		} catch (Exception e) {
			Util.severe("Unable to load YAML file " + file.getAbsolutePath());
		}
		return null;
	}
	
	public static void saveAll() {
		
		//Save regions
		for (Map.Entry<String, Region> entry : RegionManager.getRegionMap().entrySet()) {
			Region region = entry.getValue();
			for (Entry<RegionSetting, Object> setting : region.settings.entrySet())
				regionConfig.set("regions." + region.Name + "." + setting.getKey().getYaml(), setting.getValue());
		}
		try
		{
			regionConfig.save(regionConfigFile);
		}
		catch (Exception ex)
		{
			Util.severe("Error while writing to regions.yml");
		}
		
		//Save grills
		grillConfig.set("grills", null);
		List<String> list = new ArrayList<String>();
		for (Grill grill : GrillManager.getGrillList())
			list.add(grill.getStringLocation());
		grillConfig.set("grills", list);
		try
		{
			grillConfig.save(grillConfigFile);
		}
		catch (Exception ex)
		{
			Util.severe("Error while writing to grills.yml");
		}
		
		//Save main
		try
		{
			mainConfig.save(mainConfigFile);
		}
		catch (Exception ex)
		{
			Util.severe("Error while writing to config.yml");
		}
			
	}
	
}