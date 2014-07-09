package cl.netgamer.myportals;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Portal {
	
	// PROPERTIES
	
	/** where the portal base is located */
	private Location location;
	/** who this portal belongs */
	private String owner;
	/** the name of this portal */
	private String name = "";
	/** to do fast searches, "owner:name" concatenated */
	private String fullName;
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
	
	/** you cannot set any value if portal is unnamed yet */
	void setName(String name, float yaw){
		this.name = name;
		this.fullName = owner+":"+name;
		this.direction = yaw + 180;
	}
	
	boolean setOwner(String owner){
		if (name.length() == 0) return false;
		this.owner = owner;
		fullName = owner+":"+name;
		return true;
	}
	
	boolean setDest(Location destination, float yaw){
		if (name.length() == 0) return false;
		this.destination = destination;
		this.direction = yaw + 180;
		return true;
	}
	
	boolean setPrivacy(int privacy, float yaw){
		if (name.length() == 0) return false;
		this.privacy = privacy;
		this.direction = yaw + 180;
		return true;
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
