package com.matejdro.bukkit.portalstick;

import java.util.HashSet;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

public class PortalStickPlayerListener extends PlayerListener {
	private PortalStick plugin;

	public PortalStickPlayerListener(PortalStick instance)
	{
		plugin = instance;
	}		
	
	public void onPlayerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		Region region = Config.getRegion(player.getLocation());
		HashSet<Byte> tb = new HashSet<Byte>();
		for (int i : region.getList(Setting.TRANSPARENT_BLOCKS).toArray(new Integer[0]))
			tb.add((byte) i);
		
		if (player.getItemInHand().getTypeId() == Config.PortalTool && (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK))
		{		
			if (!Config.EnabledWorlds.contains(event.getPlayer().getLocation().getWorld().getName()))
			{
				player.sendMessage(Config.MessageRestrictedWorld);
				return;
			}
			
			Boolean orange = false;
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
				orange = true;
			if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_AIR ||  tb.contains((byte) event.getClickedBlock().getTypeId()))
			{
				List<Block> targetBlocks = event.getPlayer().getLastTwoTargetBlocks(tb, 120);
				if (targetBlocks.size() != 2) return;
				Block b = targetBlocks.get(1);
				plugin.PlacePortal(b, event.getPlayer(), orange);

			}
			else
			{
				plugin.PlacePortal(event.getClickedBlock(), event.getBlockFace(), event.getPlayer(), orange, true);
			}
			//float dir = (float)Math.toDegrees(Math.atan2(player.getLocation().getBlockX() - b.getX(), b.getZ() - player.getLocation().getBlockZ()));
		}

	}
	
 	    
		 public void onPlayerMove(PlayerMoveEvent event) {
			 
			 Player player = event.getPlayer();
			 Vector vector = player.getVelocity();
			 Block loc = event.getTo().getBlock();
			 Region region = Config.getRegion(loc.getLocation());
			 
			 if (loc.getType() == Material.SUGAR_CANE_BLOCK)
			 {
				 for (Grill grill: PortalStick.grills)
				 {
					 if (grill.getInside().contains(loc))
					 {
						 User user = PortalStick.players.get(player.getName());
						 if (user != null)
						 {
								if (user.getBluePortal() != null) user.getBluePortal().delete();
								if (user.getOrangePortal() != null) user.getOrangePortal().delete();

						 }
						 PlayerInventory inv = player.getInventory();
						 for (ItemStack i : inv.getContents().clone())
						 {
							 if (i != null && i.getTypeId() != Config.PortalTool)
							 {
								 inv.remove(i);
							 }
						 }
					}
				 }

			 }
			
			 if (player.isInsideVehicle()) return;
			 if (!PortalStick.permission(event.getPlayer(), "portalstick.teleport", true)) return;
			 
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
			 for (Portal p : PortalStick.portals)
			 {
				 for (Block b : p.getInside())
				 {
					 if (offsetequals(b.getLocation().getX(), loc.getX(),addX) && offsetequals(b.getLocation().getZ(), loc.getZ(), addZ) && offsetequals(b.getLocation().getY(), loc.getY(),addY))
					 {
						 portal = p;
						 break;
					 }
				 }
				 
			 }
			 
			 if (portal != null)
			 {
				 if (!portal.isOpen() || portal.isDisabled()) return;
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
				momentum = momentum * region.getInt(Setting.VELOCITY_MULTIPLIER);

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
			 Block b = event.getPlayer().getLocation().getBlock();
			 for (Grill grill: PortalStick.grills)
			 {
				 for (Block i: grill.getInside())
				 {
					 double distance = Math.sqrt(Math.pow(i.getLocation().getX() - b.getLocation().getX(),2) + Math.pow(i.getLocation().getY() - b.getLocation().getY(),2) + Math.pow(i.getLocation().getZ() - b.getLocation().getZ(),2));
					 if (distance < 3)
					 {
						 event.getPlayer().sendMessage("Don't drop items so close to Material Emancipation Grill!");
						 event.setCancelled(true);
						 return;
					 }
				 }
				 
			 }
		 }
		 
		 public void onPlayerQuit(PlayerQuitEvent event) {
			 Player player = event.getPlayer();
			 if (PortalStick.players.containsKey(player.getName())) {
				 User user = PortalStick.players.get(player.getName());
					if (user.getBluePortal() != null) user.getBluePortal().delete();
					if (user.getOrangePortal() != null) user.getOrangePortal().delete();
					PortalStick.players.remove(player.getName());
			 }
		 }
		 
	private Boolean offsetequals(double x, double y, double difference)
	{
			 return (x + difference >= y && y + difference >= x );
	}		
		 
	public class EnablePortal implements Runnable {
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
