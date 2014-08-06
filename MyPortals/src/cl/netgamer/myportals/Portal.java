package cl.netgamer.myportals;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Portal {
	
	// PROPERTIES
	
	/** where the portal base is located */
	private Location location;
	/** who this portal belongs */
	private String owner = "";
	/** the name of this portal */
	private String name = "";
	/** to do fast searches, "owner:name" concatenated */
	private String fullName =":";
	/** where player points when bring teleported (yaw) */
	private float direction;
	/** where the portal "door" points, compatible with F3 screen */
	private int facing;
	/**	0=public, 1=locked, 2=hidden, 3=private */ 
	private int privacy = 0;
	/** location of the supposed destination portal, check first */
	private Location destination = null;
	
	// CONSTRUCTORS
	
	/** for event created portals */
	Portal(Location location, Player owner, int facing){
		this.location = location;
		this.owner = owner.getName();
		this.fullName = owner+":"+name;
		this.facing = facing;
		this.direction = owner.getLocation().getYaw() + 180f;
	}
	
	/** for event created portals */
	Portal(Location location, int facing){
		this.location = location;
		this.facing = facing;
	}
	
	/** for importing */
	Portal(Location location, List<String> fields){
		this.location = location;
		owner = fields.get(0);
		name = fields.get(1);
		fullName = owner+":"+name;
		direction = Float.parseFloat(fields.get(2)); 
		facing = Integer.parseInt(fields.get(3));
		privacy = Integer.parseInt(fields.get(4));
		if (fields.size() > 5) destination = MyPortals.locationDecode(fields.get(5));
	}
	
	// GETTER METHODS
	
	Location getLocation(){
		return location;
	}
	String getOwner(){
		return owner;
	}
	String getName(){
		return name;
	}
	String getFullName(){
		return fullName;
	}
	int getFacing(){
		return facing;
	}
	int getPrivacy(){
		return privacy;
	}
	float getDirection(){
		return direction;
	}
	Location getDestination(){
		return destination;
	}
	
	// SETTER METHODS
	
	// name required for all other commands
	// check repeated name before
	String setName(String portalName, Player player){
		// can change name? 
		if (!player.getName().equalsIgnoreCase(owner) && privacy > 0) return "locked";
		// check name syntax
		if (!name.matches("[a-zA-Z0-9]{1,12}")) return "invalidName";
		name = portalName;
		// on first naming give ownership
		if (owner.length() < 1) owner = player.getName();
		// set portal
		fullName = owner+":"+name;
		direction = player.getLocation().getYaw() + 180;
		return "namedOk";
	}
	
	// check if destination portal exists before
	String setDestination(Location destination, Player player){
		if (name.length() < 1) return "noName";
		if (!owner.equals(player.getName()) && privacy > 0) return "locked";
		this.destination = destination;
		direction = player.getLocation().getYaw() + 180;
		return "destOk";
	}
	
	String setPrivacy(int privacy, Player player){
		if (name.length() < 1) return "noName";
		if (!owner.equals(player.getName()) && privacy > 0) return "locked";
		this.privacy = privacy;
		direction = player.getLocation().getYaw() + 180;
		return "privacyOk";
	}
	
	// REGULAR METHODS
	
	// must check if recipient has a portal with same name before
	String give(Player currentOwner, String recipient){
		if (name.length() < 1) return "noName";
		if (!owner.equals(currentOwner.getName())) return "notYours";
		Player newOwner = Bukkit.getPlayerExact(recipient);
		if (newOwner == null) return "offlinePlayer";
		owner = newOwner.getName();
		fullName = owner+":"+name;
		return "giveOk";
	}
	
	boolean warp(Player player){
		// allow teleport?
		//if (!canWarp(player)) return false;
		if (!player.getName().equalsIgnoreCase(getOwner()) && privacy > 2) return false;
		
		// use night vision effect to do a flash
		player.addPotionEffect(new PotionEffect(PotionEffectType.getById(16), 10, 1));
		
		// bring player over here, load chunk before
		// add a while or a for?
		if (!location.getBlock().getChunk().isLoaded()) location.getBlock().getChunk().load();
		Location dest = location.clone().add(0.5, 1, 0.5);
		dest.setYaw(direction);
		player.teleport(dest);
		return true;
	}
	
	// CHECK PLAYER PERMISSIONS
	
	/* boolean canWarp(Player player){
		if (player.getName().equalsIgnoreCase(getOwner()) || privacy < 3) return true;
		return false;
	} */
	
	
	// REGULAR METHODS
	
	String[] encode(){
		// no location, portals array index is used instead for compatibility and avoid redundancy
		return new String[]{owner, name, ""+direction, ""+facing, ""+privacy, MyPortals.locationEncode(destination)};
	}
	
	
}
