package com.matejdro.bukkit.portalstick;

import java.util.HashSet;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class User {
	private Portal bluePortal;
	private Portal orangePortal;
	private ItemStack[] inventory;
	private ItemStack boots;
	private ItemStack chest;
	private ItemStack legs;
	private ItemStack helmet;
	private Location pointOne;
	private Location pointTwo;
	private Boolean usingTool = false;
	private int colorPreset = 0;
	private HashSet<Item> droppedItems = new HashSet<Item>();
	
	public Portal getBluePortal() {
		return bluePortal;
	}
	
	public void setBluePortal(Portal portal) {
		bluePortal = portal;
	}
	
	public Portal getOrangePortal() {
		return orangePortal;
	}
	
	public void setOrangePortal(Portal portal) {
		orangePortal = portal;
	}
	
	public int getColorPreset()
	{
		return colorPreset;
	}
	
	public void setColorPreset(int input)
	{
		colorPreset = input;
	}
	
	public void recreatePortals()
	{
		if (bluePortal != null) bluePortal.recreate();
		if (orangePortal != null) orangePortal.recreate();
	}
	
	public void revertInventory(Player player) {
		if (inventory == null) return;
		PlayerInventory inv = player.getInventory();
		inv.clear();
		for (ItemStack old : inventory) {
			if (old != null) {
				ItemStack stack = new ItemStack(old.getType());
				stack.setAmount(old.getAmount());
				stack.setData(old.getData());
				inv.addItem(stack);
			}
		}
		
		if (boots != null && boots.getTypeId() != 0)
			inv.setBoots(new ItemStack(boots.getType()));
		if (chest != null && chest.getTypeId() != 0)
			inv.setChestplate(new ItemStack(chest.getType()));
		if (legs != null && legs.getTypeId() != 0)
			inv.setLeggings(new ItemStack(legs.getType()));
		if (helmet != null && helmet.getTypeId() != 0)
			inv.setHelmet(new ItemStack(helmet.getType()));
	}
	
	public void saveInventory(Player player) {
		PlayerInventory inv = player.getInventory();
		ItemStack[] con = inv.getContents();
		inventory = new ItemStack[player.getInventory().getContents().length];
		int i = 0;
		for (ItemStack old : con) {
			if (old != null) {
				ItemStack stack = new ItemStack(old.getType());
				stack.setData(old.getData());
				stack.setAmount(old.getAmount());
				inventory[i] = stack;
			}
			i++;
		}
		boots = new ItemStack(inv.getBoots().getType());
		chest = new ItemStack(inv.getChestplate().getType());
		legs = new ItemStack(inv.getLeggings().getType());
		helmet = new ItemStack(inv.getHelmet().getType());
	}

	public void setPointTwo(Location positiontwo) {
		this.pointTwo = positiontwo;
	}

	public Location getPointTwo() {
		return pointTwo;
	}

	public void setPointOne(Location positionone) {
		this.pointOne = positionone;
	}

	public Location getPointOne() {
		return pointOne;
	}

	public void setUsingTool(Boolean usingTool) {
		this.usingTool = usingTool;
	}

	public Boolean getUsingTool() {
		return usingTool;
	}
	
	public void addDroppedItem(Item item) {
		droppedItems.add(item);
	}
	
	public HashSet<Item> getDroppedItems() {
		return droppedItems;
	}
	
	public void resetItems() {
		droppedItems = new HashSet<Item>();
	}
}
