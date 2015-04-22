package cl.netgamer.myportals;

import org.bukkit.Location;
import org.bukkit.block.Block;

class ShapeCompare
  implements ShapeInterface
{
  public boolean use(Location loc, Number ref, Location base)
  {
    if ((ref instanceof Integer)) {
      return ref.equals(Integer.valueOf(loc.getBlock().getTypeId()));
    }
    return ref.equals(Double.valueOf(0.01D * loc.getBlock().getData() + loc.getBlock().getTypeId()));
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.myportals.ShapeCompare
 * JD-Core Version:    0.7.0.1
 */