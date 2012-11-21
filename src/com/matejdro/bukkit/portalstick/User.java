package com.matejdro.bukkit.portalstick;

import java.util.HashSet;
import java.util.UUID;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import de.V10lator.PortalStick.V10Location;

public class User {
	public Portal bluePortal;
	public Portal orangePortal;
	private ItemStack[] inventory;
	private ItemStack[] armor;
	public V10Location pointOne;
	public V10Location pointTwo;
	public boolean usingTool = false;
	public int colorPreset = 0;
	public final String name;
	public final UUID uuid;
	public final boolean isPlayer;
	public final HashSet<Item> droppedItems = new HashSet<Item>();
	
	public User(String name)
	{
	  this.name = name;
	  uuid = null;
	  isPlayer = false;
	}
	
	public User(Entity entity)
	{
	  if(entity instanceof Player)
	  {
		name = ((Player)entity).getName();
		isPlayer = true;
	  }
	  else
	  {
		name = null;
		isPlayer = false;
	  }
	  uuid = entity.getUniqueId();
	}
	
	public void recreatePortals()
	{
		if (bluePortal != null) bluePortal.recreate();
		if (orangePortal != null) orangePortal.recreate();
	}
	
	public void revertInventory(InventoryHolder ih) {
		if (inventory == null) return;
		Inventory inv = ih.getInventory();
		inv.clear();
		for (ItemStack old : inventory) {
			if (old != null) {
				ItemStack stack = new ItemStack(old.getType());
				stack.setAmount(old.getAmount());
				stack.setData(old.getData());
				inv.addItem(stack);
			}
		}
		if(inv instanceof PlayerInventory)
		 ((PlayerInventory)inv).setArmorContents(armor);
	}
	
	public void saveInventory(InventoryHolder ih) {
		Inventory inv = ih.getInventory();
		ItemStack[] con = inv.getContents();
		int s = con.length;
		inventory = new ItemStack[s];
		for(int i = 0; i < s; i++)
		  inventory[i] = con[i];
		if(inv instanceof PlayerInventory)
		  armor = ((PlayerInventory)inv).getArmorContents();
	}
}
