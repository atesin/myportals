package cl.netgamer.myportals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;

class ShapeGetPortalsBlocks
  implements ShapeInterface
{
  Map<Location, ArrayList<Portal>> portalBlocks = new HashMap();
  Map<Location, Portal> portals;
  
  ShapeGetPortalsBlocks(Map<Location, Portal> portals)
  {
    this.portals = portals;
  }
  
  public boolean use(Location loc, Number ref, Location base)
  {
    if (!this.portalBlocks.containsKey(loc)) {
      this.portalBlocks.put(loc, new ArrayList());
    }
    ((ArrayList)this.portalBlocks.get(loc)).add((Portal)this.portals.get(base));
    return true;
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.myportals.ShapeGetPortalsBlocks
 * JD-Core Version:    0.7.0.1
 */