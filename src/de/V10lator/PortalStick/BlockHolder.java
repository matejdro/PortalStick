package de.V10lator.PortalStick;

import org.bukkit.block.Block;

public class BlockHolder
{
  private final V10Location loc;
  public int id;
  public byte data;
  
  public BlockHolder(Block block)
  {
	loc = new V10Location(block);
	id = block.getTypeId();
	data = block.getData();
  }
  
  public void reset()
  {
	Block b = loc.getHandle().getBlock();
	b.setTypeIdAndData(id, data, true);
  }
  
  @Override
  public int hashCode()
  {
	return loc.hashCode();
  }
  
  @Override
  public boolean equals(Object obj)
  {
	if(obj == null || !(obj instanceof BlockHolder))
	  return false;
	return loc.equals(((BlockHolder)obj).loc);
  }
}
