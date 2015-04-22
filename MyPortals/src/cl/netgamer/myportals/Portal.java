package cl.netgamer.myportals;

import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Portal
{
  private Location location;
  private String owner = "";
  private String name = "";
  private String fullName = ":";
  private float direction;
  private int facing;
  private int privacy = 0;
  private Location destination = null;
  
  Portal(Location location, Player owner, int facing)
  {
    this.location = location;
    this.owner = owner.getName();
    this.fullName = (owner + ":" + this.name);
    this.facing = facing;
    this.direction = (owner.getLocation().getYaw() + 180.0F);
  }
  
  Portal(Location location)
  {
    this.location = location;
  }
  
  Portal(Location location, List<String> fields)
  {
    this.location = location;
    this.owner = ((String)fields.get(0));
    this.name = ((String)fields.get(1));
    this.fullName = (this.owner + ":" + this.name);
    this.direction = Float.parseFloat((String)fields.get(2));
    this.facing = Integer.parseInt((String)fields.get(3));
    this.privacy = Integer.parseInt((String)fields.get(4));
    if (fields.size() > 5) {
      this.destination = MyPortals.locationDecode((String)fields.get(5));
    }
  }
  
  Location getLocation()
  {
    return this.location;
  }
  
  String getOwner()
  {
    return this.owner;
  }
  
  String getName()
  {
    return this.name;
  }
  
  String getFullName()
  {
    return this.fullName;
  }
  
  int getFacing()
  {
    return this.facing;
  }
  
  int getPrivacy()
  {
    return this.privacy;
  }
  
  float getDirection()
  {
    return this.direction;
  }
  
  Location getDestination()
  {
    return this.destination;
  }
  
  void setName(String name, float yaw)
  {
    this.name = name;
    this.fullName = (this.owner + ":" + name);
    this.direction = (yaw + 180.0F);
  }
  
  void setFullName(String fullName, float yaw)
  {
    this.fullName = fullName;
    this.name = fullName.replaceAll(".*:", "");
    this.direction = (yaw + 180.0F);
  }
  
  String setName(String portalName, Player player)
  {
    if ((!player.getName().equalsIgnoreCase(this.owner)) && (this.privacy > 0)) {
      return "locked";
    }
    this.name = portalName;
    if (this.owner.length() < 1) {
      this.owner = player.getName();
    }
    this.fullName = (this.owner + ":" + this.name);
    this.direction = (player.getLocation().getYaw() + 180.0F);
    return "namedOk";
  }
  
  boolean setOwner(String owner)
  {
    if (this.name.length() == 0) {
      return false;
    }
    this.owner = owner;
    this.fullName = (owner + ":" + this.name);
    return true;
  }
  
  boolean setDest(Location destination, float yaw)
  {
    if (this.name.length() == 0) {
      return false;
    }
    this.destination = destination;
    this.direction = (yaw + 180.0F);
    return true;
  }
  
  String setDestination(Location destination, Player player)
  {
    if (this.name.length() < 1) {
      return "noName";
    }
    if ((!this.owner.equals(player.getName())) && (this.privacy > 0)) {
      return "locked";
    }
    this.destination = destination;
    this.direction = (player.getLocation().getYaw() + 180.0F);
    return "destOk";
  }
  
  boolean setPrivacy(int privacy, float yaw)
  {
    if (this.name.length() == 0) {
      return false;
    }
    this.privacy = privacy;
    this.direction = (yaw + 180.0F);
    return true;
  }
  
  String setPrivacy(int privacy, Player player)
  {
    if (this.name.length() < 1) {
      return "noName";
    }
    if ((!this.owner.equals(player.getName())) && (privacy > 0)) {
      return "locked";
    }
    this.privacy = privacy;
    this.direction = (player.getLocation().getYaw() + 180.0F);
    return "privacyOk";
  }
  
  String give(Player currentOwner, Player recipient)
  {
    if (this.name.length() < 1) {
      return "noName";
    }
    if (!this.owner.equals(currentOwner.getName())) {
      return "notYours";
    }
    this.owner = recipient.getName();
    this.fullName = (this.owner + ":" + this.name);
    return "giveOk";
  }
  
  boolean warp(Player player)
  {
    if ((!player.getName().equalsIgnoreCase(getOwner())) && (this.privacy > 2)) {
      return false;
    }
    player.addPotionEffect(new PotionEffect(PotionEffectType.getById(16), 10, 1));
    if (!this.location.getBlock().getChunk().isLoaded()) {
      this.location.getBlock().getChunk().load();
    }
    Location dest = this.location.clone().add(0.5D, 1.0D, 0.5D);
    dest.setYaw(this.direction);
    player.teleport(dest);
    return true;
  }
  
  String[] encode()
  {
    return new String[] { this.owner, this.name, ""+this.direction, ""+this.facing, ""+this.privacy, MyPortals.locationEncode(this.destination) };
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.myportals.Portal
 * JD-Core Version:    0.7.0.1
 */