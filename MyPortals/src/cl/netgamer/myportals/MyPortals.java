package cl.netgamer.myportals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import cl.netgamer.tabtext.TabText;

public final class MyPortals extends JavaPlugin{
	
	// FOR DEBUGGING
	
	private static Logger logger;
	// get logger on enable
	public static void log(String msg){
		logger.info(msg);
	}
		
	public static int waitTime;
	// PROPERTIES
	
	static boolean debug;
	private static Map<String, Object> allowedWorlds;
	boolean interWorlds;
	protected int xpCost;
	private Map<Location, Portal> portals;
	protected Data data;
	protected Shape shape;
	MyCmd cmd;
	private String[] tags;
	
	public boolean blockplacecooldown = false;
	
	private static Lang lang;
	public static String msg(String key){
		if (key.length() < 20 && lang.msg.containsKey(key)) return lang.msg.get(key);
		else return key;
	}
	
	// ENABLE PLUGIN
	
	public void onEnable(){
		this.saveDefaultConfig();
		
		if (getConfig().getBoolean("disabled")){
			getLogger().info("MyPortals disabled (does nothing)");
			return;
		}
		
		lang = new Lang(this, getConfig().getString("locale"));
		
		waitTime = getConfig().getInt("waitTime");
		debug = getConfig().getBoolean("debug");
		logger = getLogger();

		allowedWorlds = getConfig().getConfigurationSection("allowedWorlds").getValues(false);
		interWorlds = getConfig().getBoolean("interWorlds");
		xpCost = getConfig().getInt("xpCost");
		
		shape = new Shape(readShape("shapeInactive"), readShape("shapeActivate"));
		data = new Data(this);
		portals = data.loadPortals();
		
		cmd = new MyCmd(this, shape.getTransparents());
		getCommand("portal").setExecutor(cmd);
		tags = cmd.tags;
		new MyListener(this, shape.getBaseId(), shape.getChargeId(), shape.getMaterials(), shape.getPortalsBlocks(portals));
		
		logger.log(Level.INFO, Bukkit.getServer().getWorlds().toString());
	}
	
	// PORTAL INFORMATION
	
	String list(String sender, String owner, int page){
		// owner(10), name(12), privacy(8), destination fullname(23)
		String data = "";
		
		// find portals
		for (Portal p: portals.values()){
			
			// what portals do not list?
			if (!owner.equalsIgnoreCase(p.getOwner()) && !owner.equals("*")) continue;
			if (p.getPrivacy() > 1 && !sender.equalsIgnoreCase(p.getOwner()) && !sender.equals("CONSOLE")) continue;
			
			// add a line feed in next lines
			data += (data.length() < 1)? "": "\n";
		
			// add portal owner
			data += (p.getOwner().equalsIgnoreCase(sender))? "`": p.getOwner()+"`";
			
			// add portal name
			data += p.getName()+"`";
			
			// add privacy status
			data += tags[p.getPrivacy()]+"`";
			
			// add destination portal name
			data += getPortalName(p.getDestination(), sender);
			
		}
		return data;
	}
	
	protected String info(String name, String sender){
		// if not fullname add owner
		if (name.indexOf(":") < 0) name = sender+":"+name;
		
		// get portal location, empty if not found
		Location loc = getLocationByFullName(name);
		if (loc == null) return "";
		
		return info(getPortalByLocation(loc), sender);
	}
	
	protected String info(Portal portal, String sender){
		// hidden?
		if (!portal.getOwner().equalsIgnoreCase(sender) && portal.getPrivacy() > 1 && !sender.equals("CONSOLE")) return null;
		String ans = "�E"+tags[4]+"`: "+MyPortals.locationEncode(portal.getLocation())+"\n";
		ans += tags[5]+"`: "+portal.getOwner()+"\n";
		ans += tags[6]+"`: "+portal.getName()+"\n";
		ans += tags[7]+"`: "+tags[portal.getPrivacy()]+"\n";
		ans += tags[8]+"`: "+getPortalName(portal.getDestination(), sender);
		
		TabText t = new TabText(ans);
		t.setTabs(17);
		return t.getPage(0, false);
	}
	
	// PORTAL EVENTS
	
	protected boolean create(Player owner, Location baseLoc, int facing){
		//MyPortals.log("Is this world (" + baseLoc.getWorld().getName() + ") Authorized?");
		//MyPortals.log("Well, the list of autorized world is " + allowedWorlds.toString());
		if (!allowedWorlds.containsKey(baseLoc.getWorld().getName())) {
			//MyPortals.log("So, no.");
			return false;
		}
		//MyPortals.log("So, yes.");
		//Portal portal = new Portal(baseLoc, owner, facing);
		Portal portal = new Portal(baseLoc, facing);
		data.savePortal(portal);
		portals.put(baseLoc, portal);
		shape.activate(portal);
		owner.sendMessage(cmd.msg("activatedOk"));
		return true;
	}
	
	protected void destroy(Portal portal){
		shape.deactivate(portal);
		data.delPortal(portal);
		portals.remove(portal.getLocation());
	}
	
	// PORTAL MODIFICATION
	
	protected String name(Portal portal, String name, Player player){
		// already used name?
		if (getLocationByFullName(name) != null) return "busyName";
		// set name
		String status = portal.setName(name, player);
		if (status.equals("namedOk")) data.savePortal(portal);
		return status;
	}
	
	protected String dest(Portal portal, String destName, Player player){
		// if not fullname add owner
		if (destName.indexOf(":") < 0) destName = player.getName()+":"+destName;
		
		// get dest portal location
		Location destLoc = getLocationByFullName(destName);
		if (destLoc == null) return "nameNotFound";
		
		// can dest to diffrent world?
		if (!interWorlds && !destLoc.getWorld().equals(portal.getLocation().getWorld())) return "diffrentWorlds";
		
		// try to set destination
		String status = portal.setDestination(destLoc, player);
		if (status.equals("destOk")) data.savePortal(portal);
		return status;
	}
	
	protected String setPrivacy(Portal portal, int privacy, Player player){
		// try to change privacy
		String status = portal.setPrivacy(privacy, player);
		if (status.equals("privacyOk")) data.savePortal(portal);
		return status;
	}
	
	protected String give(Portal portal, Player currentOwner, String recipient){
		// recipient has a portal with same name?
		if (getLocationByFullName(recipient+":"+portal.getName()) != null) return "busyName";
		
		// try to change owner
		String status = portal.give(currentOwner, recipient);
		if (status.equals("giveOk")){
			data.savePortal(portal);
			Bukkit.getPlayerExact(recipient).sendMessage(String.format(cmd.msg("recieveOk"), currentOwner.getName(), portal.getName()));
		}
		return status;
	}
	
	protected void rebuild(){
		for (Portal p: portals.values()) shape.rebuild(p);
	}
	
	// UTILITY METHODS
	
	// external use
	protected Portal getPortalByLocation(Location loc){
		if (!portals.containsKey(loc)) return null;
		return portals.get(loc);
	}
	
	/**
	 * @param name portal search criteria
	 * @param owner possible player who owns the portal
	 * @return 
	 */
	private Location getLocationByFullName(String name){
		if (name == null) return null;
		for (Portal p: portals.values()) if (p.getFullName().equalsIgnoreCase(name)) return p.getLocation();
		return null;
	}
	
	// on enable, to read shape configuration
	private HashMap<String, ArrayList<Number>> readShape(String node){
		HashMap<String, ArrayList<Number>> shape = new HashMap<String, ArrayList<Number>>();
		for (Entry<String, Object> col: getConfig().getConfigurationSection(node).getValues(false).entrySet()){
			shape.put(col.getKey(), (ArrayList<Number>) col.getValue());
		}
		return shape;
	}
	
	/**
	 * @param loc supposed portal location
	 * @param sender who asks
	 * @return name if yours, full name if not yours, empty string if null, not found or hidden
	 */
	private String getPortalName(Location loc, String sender){
		if (loc == null || !portals.containsKey(loc)) return "";
		Portal portal = portals.get(loc);
		if (!portal.getOwner().equalsIgnoreCase(sender) && portal.getPrivacy() > 1) return "";
		if (portal.getOwner().equalsIgnoreCase(sender)) return portal.getName();
		return portal.getFullName();
	}
	
	// utility methods

	static String locationEncode(Location loc){
		// nothing passed
		if (loc == null) return null;
		
		// unknown world
		String world = loc.getWorld().getName();
		if (!allowedWorlds.containsKey(world)) return null;
		
		// format: W+35-46+2354
		String ans = (String) allowedWorlds.get(world);
		ans += loc.getX() < 0? (int)loc.getX(): "+"+(int)loc.getX();
		ans += loc.getY() < 0? (int)loc.getY(): "+"+(int)loc.getY();
		ans += loc.getZ() < 0? (int)loc.getZ(): "+"+(int)loc.getZ();
		return ans;
	}

	static Location locationDecode(String data){
		// nothing passed
		if (data == null) return null;
		
		// split including regex
		String[] d = data.split("(?=[+-])", -1);
		
		// unknown world
		if (!allowedWorlds.containsValue(d[0])) return null;
		
		World w = null;
		for (String s: allowedWorlds.keySet()){
			if (allowedWorlds.get(s).equals(d[0])){
				w = Bukkit.getServer().getWorld(s);
				break;
			}
		}
		return new Location(w, Double.parseDouble(d[1]), Double.parseDouble(d[2]), Double.parseDouble(d[3]));
	}
}
