package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;

public class LanguageCommand extends BaseCommand
{
	public LanguageCommand(PortalStick plugin)
	{
		super(plugin);
		name = "language";
		argLength = 1;
		usage = "<- switches the language";
	}
	
	public boolean execute() {
		if(plugin.i18n.setLang(args.get(0)))
		  plugin.util.sendMessage(player, plugin.i18n.getString("LanguageChanged", player.getName(), args.get(0)));
		else
		  plugin.util.sendMessage(player, plugin.i18n.getString("LanguageNotChanged", player.getName(), args.get(0)));
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_LANGUAGE);
	}
}
