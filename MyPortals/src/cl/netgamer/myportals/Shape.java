package cl.netgamer.myportals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.Location;

public class Shape
{
  HashMap<String, ArrayList<Number>> active;
  HashMap<String, ArrayList<Number>> inactive;
  HashMap<String, ArrayList<Number>> activate;
  HashMap<String, ArrayList<Number>> deactivate;
  
  public Shape(HashMap<String, ArrayList<Number>> inactive, HashMap<String, ArrayList<Number>> activate)
  {
    this.inactive = inactive;
    this.activate = activate;
    

    this.active = ((HashMap)inactive.clone());
    this.deactivate = ((HashMap)activate.clone());
    for (String key : activate.keySet())
    {
      this.active.put(key, (ArrayList)((ArrayList)activate.get(key)).clone());
      this.deactivate.put(key, (ArrayList)((ArrayList)inactive.get(key)).clone());
    }
    ((ArrayList)this.deactivate.get("center")).set(1, Integer.valueOf(0));
  }
  
  int getBaseId()
  {
    return ((Number)((ArrayList)this.inactive.get("center")).get(0)).intValue();
  }
  
  int getChargeId()
  {
    return ((Number)((ArrayList)this.inactive.get("center")).get(1)).intValue();
  }
  
  List<Integer> getMaterials(){
		List<Integer> materials = new ArrayList<Integer>();
		for (ArrayList<Number> col: active.values()){
			for (Number id: col) if (id != null && !materials.contains(id)) materials.add(id.intValue());
		}
		return materials;
	}  
  
	HashSet<Byte> getTransparents(){
		HashSet<Byte> transparents = new HashSet<Byte>(){{add((byte)0);}};
		for (ArrayList<Number> col: active.values()) for (Number id: col) if (id != null && !transparents.contains(id)) transparents.add(id.byteValue());
		transparents.remove(active.get("center").get(0).byteValue());
		return transparents;
	}
  
  int getFacing(Location loc)
  {
    return loop(loc, -1, this.inactive, new ShapeCompare());
  }
  
  void activate(Portal portal)
  {
    loop(portal.getLocation(), portal.getFacing(), this.activate, new ShapeReplace());
  }
  
  void deactivate(Portal portal)
  {
    loop(portal.getLocation(), portal.getFacing(), this.deactivate, new ShapeReplace());
  }
  
  void rebuild(Portal portal)
  {
    loop(portal.getLocation(), portal.getFacing(), this.active, new ShapeReplace());
  }
  
  ArrayList<Location> getPortalBlocks(Location loc, int facing)
  {
    ShapeGetPortalBlocks inter = new ShapeGetPortalBlocks();
    loop(loc, facing, this.active, inter);
    return inter.portalBlocks;
  }
  
  Map<Location, ArrayList<Portal>> getPortalsBlocks(Map<Location, Portal> portals)
  {
    ShapeGetPortalsBlocks blocks = new ShapeGetPortalsBlocks(portals);
    for (Map.Entry<Location, Portal> p : portals.entrySet()) {
      loop((Location)p.getKey(), ((Portal)p.getValue()).getFacing(), this.active, blocks);
    }
    return blocks.portalBlocks;
  }
  
  private int loop(Location loc, int facing, Map<String, ArrayList<Number>> ref, ShapeInterface inter)
  {
    if ((ref.containsKey("center")) && (!loopColumn(loc, (List)ref.get("center"), loc, inter))) {
      return -1;
    }
    if (ref.containsKey("corners"))
    {
      List<Number> corner = (List)ref.get("corners");
      Location loc2 = loc.clone().add(1.0D, 0.0D, 1.0D);
      if (!loopColumn(loc2, corner, loc, inter)) {
        return -1;
      }
      loc2.add(-2.0D, 0.0D, 0.0D);
      if (!loopColumn(loc2, corner, loc, inter)) {
        return -1;
      }
      loc2.add(0.0D, 0.0D, -2.0D);
      if (!loopColumn(loc2, corner, loc, inter)) {
        return -1;
      }
      loc2.add(2.0D, 0.0D, 0.0D);
      if (!loopColumn(loc2, corner, loc, inter)) {
        return -1;
      }
    }
    return loopEdges(loc, facing, ref, inter);
  }
  
  private int loopEdges(Location loc, int facing, Map<String, ArrayList<Number>> ref, ShapeInterface inter)
  {
    List<Location> faces = new ArrayList();
    


    faces.add(loc.clone().add(1.0D, 0.0D, 0.0D));
    faces.add(loc.clone().add(0.0D, 0.0D, -1.0D));
    faces.add(loc.clone().add(-1.0D, 0.0D, 0.0D));
    faces.add(loc.clone().add(0.0D, 0.0D, 1.0D));
    for (int facePos = 0; facePos < 4; facePos++)
    {
      Collections.rotate(faces, 1);
      if ((facing < 0) || (facing == facePos)) {
        if (((!ref.containsKey("front")) || (loopColumn((Location)faces.get(0), (List)ref.get("front"), loc, inter))) && 
          ((!ref.containsKey("back")) || (loopColumn((Location)faces.get(2), (List)ref.get("back"), loc, inter))) && 
          ((!ref.containsKey("sides")) || (loopColumn((Location)faces.get(1), (List)ref.get("sides"), loc, inter))) && (
          (!ref.containsKey("sides")) || (loopColumn((Location)faces.get(3), (List)ref.get("sides"), loc, inter)))) {
          return facePos;
        }
      }
    }
    return -1;
  }
  
  private boolean loopColumn(Location loc, List<Number> ref, Location base, ShapeInterface inter)
  {
    Location loc2 = loc.clone();
    for (int i = 0; i < ref.size(); i++)
    {
      if (ref.get(i) != null) {
        try
        {
          if (!inter.use(loc2.clone(), (Number)ref.get(i), base)) {
            return false;
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
      loc2.add(0.0D, 1.0D, 0.0D);
    }
    return true;
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.myportals.Shape
 * JD-Core Version:    0.7.0.1
 */