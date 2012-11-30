package com.matejdro.bukkit.portalstick.listeners;

import java.util.HashSet;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.util.Config.Sound;
import com.matejdro.bukkit.portalstick.util.RegionSetting;

import de.V10lator.PortalStick.V10Location;

public class PortalStickPlayerListener implements Listener {
	private final PortalStick plugin;
	
	public PortalStickPlayerListener(PortalStick plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = false)
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		if(plugin.config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
		  return;
		
		Player player = event.getPlayer();
		User user = plugin.userManager.getUser(player);
	
		//Portal tool
		if (player.getItemInHand().getTypeId() == plugin.config.PortalTool && player.getItemInHand().getDurability() == plugin.config.portalToolData && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
		{
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				Block block = event.getClickedBlock();
				Material mat = block.getType();
				if(mat == Material.STONE_BUTTON || mat == Material.WOOD_BUTTON || mat == Material.LEVER)
					return;
			}
			
			
			event.setCancelled(true);
			Region region = plugin.regionManager.getRegion(new V10Location(player.getLocation()));
			HashSet<Byte> tb = new HashSet<Byte>();
			for (int i : region.getList(RegionSetting.TRANSPARENT_BLOCKS).toArray(new Integer[0]))
				tb.add((byte) i);

			
			if (region.getBoolean(RegionSetting.CHECK_WORLDGUARD) && plugin.worldGuard != null && !plugin.worldGuard.canBuild(player, player.getLocation().getBlock()))
				return;
			if (!region.getBoolean(RegionSetting.ENABLE_PORTALS) || !plugin.hasPermission(player, plugin.PERM_PLACE_PORTAL))
				return;
		
			List<Block> targetBlocks = event.getPlayer().getLineOfSight(tb, 120);
			if (targetBlocks.size() < 1 || !region.getBoolean(RegionSetting.ENABLE_PORTALS))
				return;
			
			V10Location loc;
			if (region.getBoolean(RegionSetting.PREVENT_PORTAL_THROUGH_PORTAL))
			{
				for (Block b : targetBlocks)
				{
					loc = new V10Location(b);
					for (Portal p : plugin.portalManager.portals)
					{
					  for(int i = 0; i < 2; i++)
						if(p.inside[i] != null && p.inside[i].equals(loc))
						{
							plugin.util.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
							plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, loc);
							return;
						}
					}
				}
			}
			
			if (region.getBoolean(RegionSetting.PREVENT_PORTAL_CLOSED_DOOR))
			{
				for (Block b : targetBlocks)
				{
					if ((b.getType() == Material.IRON_DOOR_BLOCK || b.getType() == Material.WOODEN_DOOR) && ((b.getData() & 4) != 4) )
					{
						plugin.util.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
						plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, new V10Location(b));
						return;
					}
					else if (b.getType() == Material.TRAP_DOOR && (b.getData() & 4) == 0)
					{
						plugin.util.sendMessage(player, plugin.i18n.getString("CannotPlacePortal", player.getName()));
						plugin.util.playSound(Sound.PORTAL_CANNOT_CREATE, new V10Location(b));
						return;

					}
				}
			}
			
			boolean orange = false;
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
				orange = true;
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR || tb.contains((byte) event.getClickedBlock().getTypeId()))
			{
				Block b = targetBlocks.get(targetBlocks.size() - 1);
				loc = new V10Location(b);
		        if (targetBlocks.size() < 2)
		        	plugin.portalManager.placePortal(loc, event.getPlayer(), orange);
		        else
		    	   plugin.portalManager.placePortal(loc, b.getFace(targetBlocks.get(targetBlocks.size() - 2)), event.getPlayer(), orange, true);
			}
			else
				plugin.portalManager.placePortal(new V10Location(event.getClickedBlock()), event.getBlockFace(), event.getPlayer(), orange, true);
		}
		//Region tool
		else if (user.usingTool && player.getItemInHand().getTypeId() == plugin.config.RegionTool)
		{
			switch (event.getAction()) {
				case RIGHT_CLICK_BLOCK:
					user.pointTwo = new V10Location(event.getClickedBlock());
					plugin.util.sendMessage(player, plugin.i18n.getString("RegionPointTwoSet", player.getName()));
					break;
				case LEFT_CLICK_BLOCK:
					user.pointOne = new V10Location(event.getClickedBlock());
					plugin.util.sendMessage(player, plugin.i18n.getString("RegionPointTwoSet", player.getName()));
			}
		}
		//Flint and steel
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getType() == Material.FLINT_AND_STEEL) {
		{
			V10Location loc = new V10Location(event.getClickedBlock());
			if (plugin.grillManager.createGrill(player, loc) || plugin.funnelBridgeManager.placeGlassBridge(player, loc)) 
				event.setCancelled(true);
		}
			
		}
		//Color changing
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getTypeId() == 0 && event.getClickedBlock().getType() == Material.WOOL)
		{
			V10Location loc = new V10Location(event.getClickedBlock());
			Portal portal = plugin.portalManager.borderBlocks.get(loc);
			if (portal == null) portal = plugin.portalManager.insideBlocks.get(loc);
			if (portal == null && plugin.config.CompactPortal) portal = plugin.portalManager.behindBlocks.get(loc);
			if (portal == null) return;
			if (portal.owner.name != player.getName()) return;
		
			
			int preset = user.colorPreset;
			if (preset == plugin.config.ColorPresets.size() - 1)
				preset = 0;
			else
				preset++;
			
			user.colorPreset = preset;
			user.recreatePortals();

			String color1 = DyeColor.values()[plugin.util.getLeftPortalColor(preset)].toString().replace("_", " ");
			String color2 = DyeColor.values()[plugin.util.getRightPortalColor(preset)].toString().replace("_", " ");

			plugin.util.sendMessage(player, plugin.i18n.getString("SwitchedPortalColor", player.getName(), color1, color2));
		}

	}
 	    
	@EventHandler(ignoreCancelled = false)
	public void onPlayerMove(PlayerMoveEvent event)
	{
	  if(plugin.config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
		return;
	  Location to = plugin.entityManager.onEntityMove(event.getPlayer(), event.getFrom(), event.getTo(), false);
	  if(to != null)
		event.setTo(to);
	}
	
	@EventHandler()
	public void noPickup(PlayerPickupItemEvent event)
	{
	  Item item = event.getItem();
	  if(plugin.config.DisabledWorlds.contains(item.getWorld().getName()))
		return;
	  V10Location iloc = new V10Location(item.getLocation());
	  Region region = plugin.regionManager.getRegion(iloc);
	  Player player = event.getPlayer();
	  User user = plugin.userManager.getUser(player);
	  if(!region.getBoolean(RegionSetting.GRILLS_REMOVE_ITEMS) || user.usingTool)
		return;
	  ItemStack is = item.getItemStack();
	  int id;
	  for(Object iss: region.getList(RegionSetting.GRILL_REMOVE_EXCEPTIONS))
	  {
		id = (Integer)iss;
		if(is.getTypeId() == id)
		  return;
	  }
	  V10Location ploc = new V10Location(player.getLocation());
	  int a, b;
	  boolean x;
	  if(ploc.x != iloc.x)
	  {
		a = ploc.x;
		b = iloc.x;
		x = true;
	  }
	  else if(ploc.z != iloc.z)
	  {
		a = ploc.z;
		b = iloc.z;
		x = false;
	  }
	  else
		return;
	  if(a > b)
	  {
		int tmp = a;
		a = b;
		b = tmp;
	  }
	  for(; a < b; a++)
	  {
		if(x)
		  iloc = new V10Location(iloc.world, a, iloc.y, iloc.z);
		else
		  iloc = new V10Location(iloc.world, iloc.x, iloc.y, a);
	    if(plugin.grillManager.insideBlocks.containsKey(iloc))
	    {
	      if(plugin.grillManager.insideBlocks.get(iloc).disabled)
	    	continue;
	      event.setCancelled(true);
	      item.remove();
	      Location el = item.getLocation();
	      if(x)
		    el.setX(a);
	      else
	    	el.setZ(a);
	      plugin.grillManager.playGrillAnimation(el);
	      return;
	    }
	  }
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void trackDrops(PlayerDropItemEvent event)
	{
	  if(plugin.config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
		return;
	  Player player = event.getPlayer();
	  Region region = plugin.regionManager.getRegion(new V10Location(player.getLocation()));
	  
	  if(!region.getBoolean(RegionSetting.GRILLS_CLEAR_ITEM_DROPS))
		return;
	  
	  Item item = event.getItemDrop();
	  ItemStack is = item.getItemStack();
	  
	  int id;
	  for(Object iss: region.getList(RegionSetting.GRILL_REMOVE_EXCEPTIONS))
	  {
		id = (Integer)iss;
		if(is.getTypeId() == id)
		  return;
	  }
	  plugin.userManager.getUser(player).droppedItems.add(item);
	}
}
