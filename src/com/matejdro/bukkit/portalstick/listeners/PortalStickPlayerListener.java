package com.matejdro.bukkit.portalstick.listeners;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

import com.matejdro.bukkit.portalstick.Grill;
import com.matejdro.bukkit.portalstick.GrillManager;
import com.matejdro.bukkit.portalstick.Portal;
import com.matejdro.bukkit.portalstick.PortalManager;
import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.Region;
import com.matejdro.bukkit.portalstick.RegionManager;
import com.matejdro.bukkit.portalstick.User;
import com.matejdro.bukkit.portalstick.UserManager;
import com.matejdro.bukkit.portalstick.util.Config;
import com.matejdro.bukkit.portalstick.util.Permission;
import com.matejdro.bukkit.portalstick.util.RegionSetting;
import com.matejdro.bukkit.portalstick.util.Util;

public class PortalStickPlayerListener extends PlayerListener {
	
	private PortalStick plugin;

	public PortalStickPlayerListener(PortalStick instance)
	{
		plugin = instance;
	}	
	
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		
		Player player = event.getPlayer();
		User user = UserManager.getUser(player);
		Region region = RegionManager.getRegion(player.getLocation());
		HashSet<Byte> tb = new HashSet<Byte>();
		for (int i : region.getList(RegionSetting.TRANSPARENT_BLOCKS).toArray(new Integer[0]))
			tb.add((byte) i);
		
		//Portal tool
		if (player.getItemInHand().getTypeId() == Config.PortalTool && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
		{
			
			if (region.getBoolean(RegionSetting.CHECK_WORLDGUARD) && PortalStick.worldGuard != null && !PortalStick.worldGuard.canBuild(player, player.getLocation().getBlock()))
				return;
			if (!region.getBoolean(RegionSetting.ENABLE_PORTALS))
				return;
			if (!Permission.placePortal(player))
				return;
		
			List<Block> targetBlocks = event.getPlayer().getLineOfSight(tb, 20);
			if (targetBlocks.size() < 1) return;
			
			if (Config.DisabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
			{
				player.sendMessage(Config.MessageRestrictedWorld);
				return;
			}
			
			if (!region.getBoolean(RegionSetting.ENABLE_PORTALS)) return;
			
			if (region.getBoolean(RegionSetting.PREVENT_PORTAL_THROUGH_PORTAL))
			{
				for (Block b : targetBlocks)
				{
					for (Portal p : PortalManager.portals)
					{
						if (p.getInside().contains(b))
						{
							Util.sendMessage(player, Config.MessageCannotPlacePortal);
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
							Util.sendMessage(player, Config.MessageCannotPlacePortal);
							return;
					}
				}
			}
			
			Boolean orange = false;
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
				orange = true;
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR ||  tb.contains((byte) event.getClickedBlock().getTypeId()))
			{
				Block b = targetBlocks.get(targetBlocks.size() - 1);
				PortalManager.placePortal(b, event.getPlayer(), orange);
			}
			else
			{
				PortalManager.placePortal(event.getClickedBlock(), event.getBlockFace(), event.getPlayer(), orange, true);
			}
			//float dir = (float)Math.toDegrees(Math.atan2(player.getLocation().getBlockX() - b.getX(), b.getZ() - player.getLocation().getBlockZ()));
		}
		//Region tool
		else if (user.getUsingTool() && player.getItemInHand().getTypeId() == Config.RegionTool)
		{
			switch (event.getAction()) {
				case RIGHT_CLICK_BLOCK:
					user.setPointTwo(event.getClickedBlock().getLocation());
					Util.sendMessage(player, "&aRegion point two set`nType /portal setregion to save the region");
					break;
				case LEFT_CLICK_BLOCK:
					user.setPointOne(event.getClickedBlock().getLocation());
					Util.sendMessage(player, "&aRegion point one set`nType /portal setregion to save the region");
					break;
			}
		}
		//Flint and steel
		else if (event.getAction() == Action.RIGHT_CLICK_BLOCK && player.getItemInHand().getType() == Material.FLINT_AND_STEEL) {
			GrillManager.createGrill(player, event.getClickedBlock());
		}

	}
 	    
	public void onPlayerMove(PlayerMoveEvent event)
	{
		
		Player player = event.getPlayer();
		Vector vector = player.getVelocity();
		Block blockTo = event.getTo().getBlock();
		Region regionTo = RegionManager.getRegion(event.getTo());
		Region regionFrom = RegionManager.getRegion(event.getFrom());
		
		//Check for changing regions
		PortalManager.checkPlayerMove(player, regionFrom, regionTo);
		
		//Emancipation grill
		if (blockTo.getType() == Material.SUGAR_CANE_BLOCK && regionTo.getBoolean(RegionSetting.ENABLE_GRILLS))
		{
			for (Grill grill: GrillManager.grills)
			{
				if (grill.getInside().contains(blockTo))
				{
					GrillManager.emancipate(player);
				}
			}
		}
			
		//Portals
		if (player.isInsideVehicle()) return;
		if (!Permission.teleport(player)) return;
			 
		//Aiming assistant: 
		double addX = 0.0;
		double addY = 0.0;
		double addZ = 0.0;
		if (Math.abs(vector.getX()) > 1)
		{
			addX += 3.0;
			addY += 2.0;
			addZ += 2.0;
		}
		if (Math.abs(vector.getZ()) > 1)
		{
			addX += 2.0;
			addY += 2.0;
			addZ += 3.0;
		}
		if (Math.abs(vector.getY()) > 1)
		{
			addX += 2.0;
			addY += 3.0;
			addZ += 2.0;
		}
				 
		Portal portal = null;
		for (Portal p : PortalManager.portals)
		{
			for (Block b : p.getInside())
			{
				if (offsetequals(b.getLocation().getX(), blockTo.getX(),addX) && offsetequals(b.getLocation().getZ(), blockTo.getZ(), addZ) && offsetequals(b.getLocation().getY(), blockTo.getY(),addY))
				{
					portal = p;
					break;
				}
			}	 
		}
			 
		if (portal != null)
		{
			if (!portal.isOpen() || portal.isDisabled()) return;
			if (Math.abs(vector.getY()) > 1 && !portal.isVertical()) return;
			User owner = portal.getOwner();
				 
			Location teleport;
			Portal destination;
			if (portal.isOrange())
				destination = owner.getBluePortal();
			else
				destination = owner.getOrangePortal();
				 				 
			teleport = destination.getTeleportLocation().clone();
								 
			float yaw = 0;
			float pitch = 0;
				
			//Read input velocity
			Double momentum = 0.0;
			switch(portal.getTeleportFace())
	        {
	        	case NORTH:
	        	case SOUTH:
	        		momentum = vector.getX();
	        		break;
	        	case EAST:
	        	case WEST:
	        		momentum = vector.getZ();
	        		break;
	        	case UP:
	        	case DOWN:
	        		momentum = vector.getY();
	        		break;
	        }
				
			momentum = Math.abs(momentum);
			momentum = momentum * regionTo.getDouble(RegionSetting.VELOCITY_MULTIPLIER);

			//reposition velocity to match output portal's orientation
			Vector outvector = player.getVelocity().zero();
			switch(destination.getTeleportFace())
	        {
	        	case NORTH:
	        		yaw = 270;
	        		outvector = outvector.setX(momentum);
	        		break;
	        	case EAST:
	        		yaw = 0;
	        		outvector = outvector.setZ(momentum);
	        		break;
	        	case SOUTH:
	        		yaw = 90;
	        		outvector = outvector.setX(-momentum);
	        		break;
	        	case WEST:
	        		yaw = 180;
	        		outvector = outvector.setZ(-momentum);
	        		break;
	        	case UP:
	        		pitch = 90;
	        		outvector = outvector.setY(momentum);
	        		break;
	        	case DOWN:
	        		pitch = -90;
	        		outvector = outvector.setY(-momentum);
	        		break;
	        }
				 				
			player.setFallDistance(0);	
			player.setVelocity(player.getVelocity().zero());
				 
			teleport.setPitch(pitch);
			teleport.setYaw(yaw);
				 
			event.setTo(teleport);
			player.teleport(teleport);
				 				 
			player.setVelocity(outvector);
				 
			destination.setDisabled(true);
			plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new EnablePortal(destination), 10L);
		}
	}
		 
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (event.isCancelled()) return;
		
		Player player = event.getPlayer();
		User user = UserManager.getUser(player);
		Region region = RegionManager.getRegion(player.getLocation());
		if (region.getBoolean(RegionSetting.GRILLS_CLEAR_ITEM_DROPS))
			user.addDroppedItem(event.getItemDrop());
		
	}
	
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		Region regionFrom = RegionManager.getRegion(event.getFrom());
		Region regionTo = RegionManager.getRegion(event.getTo());
		if (!Config.RestoreInvOnWorldChange && !event.getFrom().getWorld().getName().equalsIgnoreCase(event.getTo().getWorld().getName()))
			return;
		PortalManager.checkPlayerMove(event.getPlayer(), regionFrom, regionTo);
	}
		 
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		User user = UserManager.getUser(player);
		
		Region region = RegionManager.getRegion(player.getLocation());
		if (region.Name != "global" && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
			user.revertInventory(player);
		if (Config.DeleteOnQuit) {
			PortalManager.deletePortals(user);
			UserManager.deleteUser(player);
		}
		UserManager.deleteDroppedItems(player);
	}
		
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		User user = new User();
		UserManager.createUser(player);
		Region region = RegionManager.getRegion(player.getLocation());
		if (!region.Name.equals("global") && region.getBoolean(RegionSetting.UNIQUE_INVENTORY))
			user.saveInventory(player);
	}
		 
	private Boolean offsetequals(double x, double y, double difference)
	{
		return (x + difference >= y && y + difference >= x );
	}		
		 
	public class EnablePortal implements Runnable
	{
		Portal portal = null;
		public EnablePortal(Portal p){
			portal = p;
		}
		@Override
		public void run() {
			// this part knows plugin
			if (portal != null) portal.setDisabled(false);
		}
	}
}
