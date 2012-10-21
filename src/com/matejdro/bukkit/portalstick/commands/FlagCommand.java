package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class FlagCommand extends BaseCommand {

	public FlagCommand(PortalStick plugin) {
		super(plugin);
		name = "flag";
		argLength = 3;
		usage = "<region> <flag> <value> <- flag a region";
	}
	
	public boolean execute() {
		
		Region editRegion = plugin.regionManager.getRegion(args.get(0));
		if (editRegion == null) {
			Util.sendMessage(player, "&cInvalid region name, please supply an existing region!");
			return true;
		}
		
		for (RegionSetting setting : RegionSetting.values()) {
			if (setting.getYaml().equalsIgnoreCase(args.get(1)) && setting.getEditable()) {
				Object old = editRegion.settings.remove(setting);
				try {
					
					if (setting.getDefault() instanceof Integer)
						editRegion.settings.put(setting, Integer.parseInt(args.get(2)));
					else if (setting.getDefault() instanceof Double)
						editRegion.settings.put(setting, Double.parseDouble(args.get(2)));
					else if (setting.getDefault() instanceof Boolean)
						editRegion.settings.put(setting, Boolean.parseBoolean(args.get(2)));
					else
						editRegion.settings.put(setting, args.get(2));
					
					Util.sendMessage(player, "&aRegion &7" + editRegion.name + " &aupdated");
					Config.saveAll();
					Config.reLoad();
					return true;
				} catch (Throwable t) {
					Util.sendMessage(player, "&cInvalid value supplied for flag &7" + setting.getYaml());
					editRegion.settings.put(setting, old);
					return true;
				}
			}
		}
		Util.sendMessage(player, "&cInvalid flag, please choose from one of the following:");
		String flags = "";
		for (RegionSetting setting : RegionSetting.values())
			if (setting.getEditable()) flags += "&c" + setting.getYaml() + "&7, ";
		Util.sendMessage(player, "&c" + flags.substring(0, flags.length() - 2));
		return true;
	}
	
	public boolean permission(Player player) {
		return Permission.adminRegions(player);
	}

}
