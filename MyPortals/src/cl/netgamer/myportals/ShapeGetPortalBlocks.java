package cl.netgamer.myportals;

import java.util.ArrayList;
import org.bukkit.Location;

class ShapeGetPortalBlocks
  implements ShapeInterface
{
  ArrayList<Location> portalBlocks = new ArrayList();
  
  public boolean use(Location loc, Number ref, Location base)
  {
    this.portalBlocks.add(loc);
    return true;
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.myportals.ShapeGetPortalBlocks
 * JD-Core Version:    0.7.0.1
 */