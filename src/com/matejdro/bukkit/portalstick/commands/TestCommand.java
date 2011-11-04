package com.matejdro.bukkit.portalstick.commands;

import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;

import com.matejdro.bukkit.portalstick.PortalStick;
import com.matejdro.bukkit.portalstick.blocks.PortalBlockDown;
import com.matejdro.bukkit.portalstick.blocks.PortalBlockUp;
import com.matejdro.bukkit.portalstick.util.Permission;

public class TestCommand extends BaseCommand {

	public TestCommand() {
		name = "test";
		argLength = 0;
		usage = "<- tests";
	}
	
	public boolean execute() {
		
		
        PortalBlockDown portalBlockDown = new PortalBlockDown(PortalStick.instance);
        PortalBlockUp portalBlockUp = new PortalBlockUp(PortalStick.instance);
		
        player.getInventory().addItem(SpoutManager.getMaterialManager().getCustomItemStack(portalBlockUp, 1));
        player.getInventory().addItem(SpoutManager.getMaterialManager().getCustomItemStack(portalBlockDown, 1));
		//Viewing through other portal
//		User user = UserManager.getUser(player);
//		Portal p1 = user.getBluePortal();
//		Portal p2 = user.getOrangePortal();
//		
//		Block bs1 = p1.getCenterBlock().getRelative(p1.getTeleportFace().getOppositeFace(), 1);
//		Block bs2 = p2.getCenterBlock().getRelative(p2.getTeleportFace(), 1);
//
//		for (int depth = 0; depth < 20; depth++)
//		{
//			for (int height = -5; height < 5; height++)
//			{
//				for (int width = -5; width < 5; width++)
//				{					
//					Block b1, b2;
//					BlockFace heightf = null, widthf = null;
//					
//					switch (p1.getTeleportFace())
//					{
//						case NORTH:
//							heightf = BlockFace.UP;
//							widthf = BlockFace.EAST;
//							break;
//						case SOUTH:
//							heightf = BlockFace.UP;
//							widthf = BlockFace.WEST;
//							break;		
//						case UP:
//							heightf = BlockFace.NORTH;
//							widthf = BlockFace.WEST;
//							break;
//						case DOWN:
//							heightf = BlockFace.NORTH;
//							widthf = BlockFace.EAST;
//							break;
//						case EAST:
//							heightf = BlockFace.UP;
//							widthf = BlockFace.NORTH;
//							break;
//						case WEST:
//							heightf = BlockFace.UP;
//							widthf = BlockFace.SOUTH;
//							break;
//					}
//					b1 = bs1.getRelative(p1.getTeleportFace().getOppositeFace(), depth).getRelative(heightf, height).getRelative(widthf, width);
//					
//					BlockFace oldWidthFace = widthf;
//					
//					switch (p2.getTeleportFace())
//					{
//						case NORTH:
//							heightf = BlockFace.UP;
//							widthf = BlockFace.EAST;
//							break;
//						case SOUTH:
//							heightf = BlockFace.UP;
//							widthf = BlockFace.WEST;
//							break;		
//						case UP:
//							heightf = BlockFace.NORTH;
//							widthf = BlockFace.WEST;
//							break;
//						case DOWN:
//							heightf = BlockFace.NORTH;
//							widthf = BlockFace.EAST;
//							break;
//						case EAST:
//							heightf = BlockFace.UP;
//							widthf = BlockFace.NORTH;
//							break;
//						case WEST:
//							heightf = BlockFace.UP;
//							widthf = BlockFace.SOUTH;
//							break;
//					}
//
//					b2 = bs2.getRelative(p2.getTeleportFace(), depth).getRelative(heightf, height).getRelative(widthf, width);
//					player.sendBlockChange(b2.getLocation(), b1.getTypeId(), BlockUtil.rotateBlock(b1.getType(), b1.getData(), oldWidthFace, widthf));
//				}
//			}
//		}
		return true;
		
	}
	
	public boolean permission(Player player) {
		return Permission.adminRegions(player);
	}
	

}
