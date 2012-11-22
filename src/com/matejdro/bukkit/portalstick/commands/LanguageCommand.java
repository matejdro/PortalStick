package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;

import com.matejdro.bukkit.portalstick.PortalStick;

public class LanguageCommand extends BaseCommand
{
	public LanguageCommand(PortalStick plugin)
	{
		super(plugin, "language", 1, "<- switches the language", false);
	}
	
	public boolean execute() {
		if(plugin.i18n.setLang(args[0]))
		{
		  plugin.util.sendMessage(sender, plugin.i18n.getString("LanguageChanged", playerName, args[0]));
		  plugin.config.lang = args[0];
		  plugin.config.saveAll();
		}
		else
		  plugin.util.sendMessage(sender, plugin.i18n.getString("LanguageNotChanged", playerName, args[0]));
		return true;
	}
	
	public boolean permission(Player player) {
		return plugin.hasPermission(player, plugin.PERM_LANGUAGE);
	}
}
