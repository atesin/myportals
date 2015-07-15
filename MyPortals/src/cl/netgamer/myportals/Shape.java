package cl.netgamer.myportals;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

public class Shape {
	
	// PROPERTIES
	
	HashMap<String, ArrayList<Number>> active;
	HashMap<String, ArrayList<Number>> inactive;
	HashMap<String, ArrayList<Number>> activate;
	HashMap<String, ArrayList<Number>> deactivate;
	
	// CONSTRUCTOR
	
	public Shape(HashMap<String, ArrayList<Number>> inactive, HashMap<String, ArrayList<Number>> activate){
		this.inactive = inactive;
		this.activate = activate;
		
		// get these as base and correct them, notice it replaces full columns!
		active = (HashMap<String, ArrayList<Number>>)inactive.clone();
		deactivate = (HashMap<String, ArrayList<Number>>)activate.clone();
		for (String key: activate.keySet()){
			active.put(key, (ArrayList<Number>)activate.get(key).clone());
			deactivate.put(key, (ArrayList<Number>)inactive.get(key).clone());
		}
		// replace charge id by air to switch off portal
		deactivate.get("center").set(1, 0);
	}
	
	// SETTER AND GETTER METHODS
	
	int getBaseId(){
		return inactive.get("center").get(0).intValue();
	}
	
	int getChargeId(){
		return inactive.get("center").get(1).intValue();
	}
	
	List<Integer> getMaterials(){
		List<Integer> materials = new ArrayList<Integer>();
		for (ArrayList<Number> col: active.values()){
			for (Number id: col) if (id != null && !materials.contains(id)) materials.add(id.intValue());
		}
		return materials;
	}
	
	HashSet<Byte> getTransparents(){
		HashSet<Byte> transparents = new HashSet<Byte>();
		transparents.add((byte)0);
		for (ArrayList<Number> col: active.values()) for (Number id: col) if (id != null && !transparents.contains(id)) transparents.add(id.byteValue());
		transparents.remove(active.get("center").get(0).byteValue());
		return transparents;
	}
	
	// REGULAR METHODS
	
	/**
	 * used when try to activating a portal to check shape and get facing
	 * @param loc location of supossed portal
	 * @return 0, 1, 2, 3, -1: south, west, north, east, none
	 */
	int getFacing(Location loc){
		return loop(loc, -1, inactive, new ShapeCompare());
	}
	
	/**
	 * used when destroy a portal block to replace shape to look deactivated
	 * @param portal
	 */
	void activate(Portal portal){
		loop(portal.getLocation(), portal.getFacing(), activate, new ShapeReplace());
	}
	
	void deactivate(Portal portal){
		loop(portal.getLocation(), portal.getFacing(), deactivate, new ShapeReplace());
	}
	
	void rebuild(Portal portal){
		loop(portal.getLocation(), portal.getFacing(), active, new ShapeReplace());
	}
	
	// get portal blocks on run time
	ArrayList<Location> getPortalBlocks(Location loc, int facing){
		ShapeGetPortalBlocks inter = new ShapeGetPortalBlocks();
		loop(loc, facing, active, inter);
		return inter.portalBlocks;
	}
	
	/**
	 * used at plugin load to get a map of portal blocks, for future destroy check
	 * @param portals
	 * @return
	 */
	Map<Location, ArrayList<Portal>> getPortalsBlocks(Map<Location, Portal> portals){
		ShapeGetPortalsBlocks blocks = new ShapeGetPortalsBlocks(portals);
		for (Entry<Location, Portal> p: portals.entrySet()){
			loop(p.getKey(), p.getValue().getFacing(), active, blocks);
		}
		return blocks.portalBlocks;	
	}
	
	// INTERNAL METHODS
	
	/**
	 * internal use, iterates (posible) blocks to do things with shape
	 * @param loc location of supposed portal
	 * @param facing shape orientation of the supposed portal (-1 for guess)
	 * @param inter the interface to use the supposed portal
	 * @return facing (0, 1, 2, 3, -1 = south, west, north, east, none)
	 */
	private int loop(Location loc, int facing, Map<String, ArrayList<Number>> ref, ShapeInterface inter){
		
		if (ref.containsKey("center") && !loopColumn(loc, ref.get("center"), loc, inter)) return -1;
		
		if (ref.containsKey("corners")){
			List<Number> corner = ref.get("corners");
			Location loc2 = loc.clone().add(1, 0, 1);
			if (!loopColumn(loc2, corner, loc, inter)) return -1;
			loc2.add(-2, 0, 0);
			if (!loopColumn(loc2, corner, loc, inter)) return -1;
			loc2.add(0, 0, -2);
			if (!loopColumn(loc2, corner, loc, inter)) return -1;
			loc2.add(2, 0, 0);
			if (!loopColumn(loc2, corner, loc, inter)) return -1;
		}
		return loopEdges(loc, facing, ref, inter);
	}
	
	private int loopEdges(Location loc, int facing, Map<String, ArrayList<Number>> ref, ShapeInterface inter){
		
		// create an array to store reference locations
		List<Location> faces = new ArrayList<Location>();
		
		// facings = south, west, north, east = 0, 1, 2, 3 = z+, x-, z-, x+
		// list will rotate from the end, must be reverse rotated 1 pos
		faces.add(loc.clone().add(1, 0, 0));
		faces.add(loc.clone().add(0, 0, -1));
		faces.add(loc.clone().add(-1, 0, 0));
		faces.add(loc.clone().add(0, 0, 1));
		
		// facing locations loop
		for (int facePos = 0;facePos < 4;++facePos){
			
			// rotate reference locations (clockwise), restoring size to avoid infinite grow
			Collections.rotate(faces, 1);
			
			// to match rotation with passed facing value
			if (facing >= 0 && facing != facePos) continue;
			
			if (ref.containsKey("front") && !loopColumn(faces.get(0), ref.get("front"), loc, inter)) continue;
			if (ref.containsKey("back") && !loopColumn(faces.get(2), ref.get("back"), loc, inter)) continue;
			if (ref.containsKey("sides") && !loopColumn(faces.get(1), ref.get("sides"), loc, inter)) continue;
			if (ref.containsKey("sides") && !loopColumn(faces.get(3), ref.get("sides"), loc, inter)) continue;
			
			// it passed all edges
			return facePos;
		}
		return -1;
	}
	
	private boolean loopColumn(Location loc, List<Number> ref, Location base, ShapeInterface inter){
		Location loc2 = loc.clone();
		for (int i = 0; i < ref.size(); ++i){
			// if ref is null then continue
			if (ref.get(i) != null){
				try {
					if (!inter.use(loc2.clone(), ref.get(i), base)) return false;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			loc2.add(0, 1, 0);
		}
		return true;
	}
}


// OTHER CLASSES


/**
 * interface to be passed to loop shape method, the methods that implements does the job really
 * @author atesin#gmail.com
 */
interface ShapeInterface{
	
	/**
	 * common method that all implementing classes have, but all do diffrent things with it
	 * @param loc location to examine
	 * @param id material to be compared or replaced
	 * @param base portal locaction related with this
	 * @return true or false on id comparison, always true in other cases
	 * @throws Exception 
	 */
	boolean use(Location loc, Number id, Location base) throws Exception;
}


/**
 * implements a method that compare portal shapes
 * @author atesin#gmail.com
 */
class ShapeCompare implements ShapeInterface{

	public boolean use(Location loc, Number ref, Location base){
		if (ref instanceof Integer) return ref.equals(loc.getBlock().getTypeId());
		else return ref.equals(0.01d * loc.getBlock().getData() + loc.getBlock().getTypeId());
	}
}


/**
 * implements a method that replace portal shape on portal creation/destroying
 * @author atesin#gmail.com
 */
class ShapeReplace implements ShapeInterface{
	
	public boolean use(Location loc, Number ref, Location base) throws Exception{
		loc.getBlock().setTypeId(ref.intValue());
		return true;
	}
	
	
	
}


/**
 * implements a method that returns block location
 * @author atesin#gmail.com
 */
class ShapeGetPortalBlocks implements ShapeInterface{
	
	ArrayList<Location> portalBlocks = new ArrayList<Location>();

	public boolean use(Location loc, Number ref, Location base){
		portalBlocks.add(loc);
		return true;
	}
}


class ShapeGetPortalsBlocks implements ShapeInterface{
	
	Map<Location, ArrayList<Portal>> portalBlocks = new HashMap<Location, ArrayList<Portal>>();
	Map<Location, Portal> portals;
	
	ShapeGetPortalsBlocks(Map<Location, Portal> portals){
		this.portals = portals;
	}

	public boolean use(Location loc, Number ref, Location base){
		if (!portalBlocks.containsKey(loc)) portalBlocks.put(loc, new ArrayList<Portal>());
		portalBlocks.get(loc).add(portals.get(base));
		return true;
	}
}
