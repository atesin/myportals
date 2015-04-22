package cl.netgamer.myportals;

import cl.netgamer.tabtext.TabText;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class MyPortals
  extends JavaPlugin
{
  private static Logger logger;
  private static Map<String, Object> allowedWorlds;
  boolean interWorlds;
  protected int xpCost;
  private Map<Location, Portal> portals;
  protected Data data;
  protected Shape shape;
  private String[] tags;
  
  public static void log(String msg)
  {
    logger.info(msg);
  }
  
  public void onEnable()
  {
    saveDefaultConfig();
    if (getConfig().getBoolean("disabled")) {
      return;
    }
    logger = getLogger();
    
    allowedWorlds = getConfig().getConfigurationSection("allowedWorlds").getValues(false);
    this.interWorlds = getConfig().getBoolean("interWorlds");
    this.xpCost = getConfig().getInt("xpCost");
    
    this.shape = new Shape(readShape("shapeInactive"), readShape("shapeActivate"));
    this.data = new Data(this);
    this.portals = this.data.loadPortals();
    
    new MyListener(this, this.shape.getBaseId(), this.shape.getChargeId(), this.shape.getMaterials(), this.shape.getPortalsBlocks(this.portals));
    getCommand("portal").setExecutor(new MyCmd(this, getConfig().getString("locale"), this.shape.getTransparents()));
    this.tags = MyCmd.tags;
  }
  
  String list(String sender, String owner, int page)
  {
    String data = "";
    for (Portal p : this.portals.values()) {
      if (((owner.equalsIgnoreCase(p.getOwner())) || (owner.equals("*"))) && (
        (p.getPrivacy() <= 1) || (sender.equalsIgnoreCase(p.getOwner())) || (sender.equals("CONSOLE"))))
      {
        data = data + (data.length() < 1 ? "" : "\n");
        

        data = data + (p.getOwner().equalsIgnoreCase(sender) ? "`" : new StringBuilder(String.valueOf(p.getOwner())).append("`").toString());
        

        data = data + p.getName() + "`";
        

        data = data + MyCmd.tags[p.getPrivacy()] + "`";
        

        data = data + getPortalName(p.getDestination(), sender);
      }
    }
    return data;
  }
  
  protected String info(String name, String sender)
  {
    if (name.indexOf(":") < 0) {
      name = sender + ":" + name;
    }
    Location loc = getLocationByFullName(name);
    if (loc == null) {
      return "";
    }
    return info(getPortalByLocation(loc), sender);
  }
  
  protected String info(Portal portal, String sender)
  {
    if ((!portal.getOwner().equalsIgnoreCase(sender)) && (portal.getPrivacy() > 1) && (!sender.equals("CONSOLE"))) {
      return null;
    }
    String ans = "§E" + this.tags[4] + "`: " + locationEncode(portal.getLocation()) + "\n";
    ans = ans + this.tags[5] + "`: " + portal.getOwner() + "\n";
    ans = ans + this.tags[6] + "`: " + portal.getName() + "\n";
    ans = ans + this.tags[7] + "`: " + this.tags[portal.getPrivacy()] + "\n";
    ans = ans + this.tags[8] + "`: " + getPortalName(portal.getDestination(), sender);
    
    TabText t = new TabText(ans);
    t.setTabs(new int[] { 17 });
    return t.getPage(0, false);
  }
  
  protected boolean create(Player owner, Location baseLoc, int facing)
  {
    if (!allowedWorlds.containsKey(baseLoc.getWorld().getName())) {
      return false;
    }
    Portal portal = new Portal(baseLoc, owner, facing);
    this.data.savePortal(portal);
    this.portals.put(baseLoc, portal);
    this.shape.activate(portal);
    return true;
  }
  
  protected void destroy(Portal portal)
  {
    this.shape.deactivate(portal);
    this.data.delPortal(portal);
    this.portals.remove(portal.getLocation());
  }
  
  protected String name(Portal portal, String name, Player player)
  {
    if ((!player.getName().equalsIgnoreCase(portal.getOwner())) && (portal.getPrivacy() > 0)) {
      return "locked";
    }
    if (!name.matches("[a-zA-Z0-9]{1,12}")) {
      return "invalidName";
    }
    if (name.indexOf(":") < 0) {
      name = player.getName() + ":" + name;
    }
    if (getLocationByFullName(name) != null) {
      return "busyName";
    }
    portal.setFullName(name, player.getLocation().getYaw());
    this.data.savePortal(portal);
    return "namedOk";
  }
  
  protected String dest(Portal portal, String destName, Player player)
  {
    if ((!portal.getOwner().equalsIgnoreCase(player.getName())) && (portal.getPrivacy() > 0)) {
      return "locked";
    }
    if (destName.indexOf(":") < 0) {
      destName = player.getName() + ":" + destName;
    }
    Location destLoc = getLocationByFullName(destName);
    if (destLoc == null) {
      return "nameNotFound";
    }
    if ((!this.interWorlds) && (!destLoc.getWorld().equals(portal.getLocation().getWorld()))) {
      return "diffrentWorlds";
    }
    if (!portal.setDest(destLoc, player.getLocation().getYaw())) {
      return "unnamed";
    }
    this.data.savePortal(portal);
    return "destOk";
  }
  
  protected String setPrivacy(Portal portal, int privacy, Player player)
  {
    if ((!portal.getOwner().equalsIgnoreCase(player.getName())) && (portal.getPrivacy() > 0)) {
      return "locked";
    }
    if (!portal.setPrivacy(privacy, player.getLocation().getYaw())) {
      return "unnamed";
    }
    this.data.savePortal(portal);
    return "privacyOk";
  }
  
  protected String give(Portal portal, String recipient, String owner)
  {
    if (!portal.getOwner().equalsIgnoreCase(owner)) {
      return "notYours";
    }
    Player newOwner = Bukkit.getPlayerExact(recipient);
    if (newOwner == null) {
      return "offlinePlayer";
    }
    if (getLocationByFullName(newOwner.getName() + ":" + portal.getName()) != null) {
      return "busyName";
    }
    if (!portal.setOwner(recipient)) {
      return "unnamed";
    }
    this.data.savePortal(portal);
    return "giveOk";
  }
  
  protected void rebuild()
  {
    Portal p;
    for (Iterator localIterator = this.portals.values().iterator(); localIterator.hasNext(); this.shape.rebuild(p)) {
      p = (Portal)localIterator.next();
    }
  }
  
  protected void rebuild(Location loc)
  {
    this.shape.rebuild(getPortalByLocation(loc));
  }
  
  protected Portal getPortalByLocation(Location loc)
  {
    if (!this.portals.containsKey(loc)) {
      return null;
    }
    return (Portal)this.portals.get(loc);
  }
  
  private Location getLocationByFullName(String name)
  {
    if (name == null) {
      return null;
    }
    for (Portal p : this.portals.values()) {
      if (p.getFullName().equalsIgnoreCase(name)) {
        return p.getLocation();
      }
    }
    return null;
  }
  
  private HashMap<String, ArrayList<Number>> readShape(String node)
  {
    HashMap<String, ArrayList<Number>> shape = new HashMap();
    for (Map.Entry<String, Object> col : getConfig().getConfigurationSection(node).getValues(false).entrySet()) {
      shape.put((String)col.getKey(), (ArrayList)col.getValue());
    }
    return shape;
  }
  
  private String getPortalName(Location loc, String sender)
  {
    if ((loc == null) || (!this.portals.containsKey(loc))) {
      return "";
    }
    Portal portal = (Portal)this.portals.get(loc);
    if ((!portal.getOwner().equalsIgnoreCase(sender)) && (portal.getPrivacy() > 1)) {
      return "";
    }
    if (portal.getOwner().equalsIgnoreCase(sender)) {
      return portal.getName();
    }
    return portal.getFullName();
  }
  
  static String locationEncode(Location loc)
  {
    if (loc == null) {
      return null;
    }
    String world = loc.getWorld().getName();
    if (!allowedWorlds.containsKey(world)) {
      return null;
    }
    String ans = (String)allowedWorlds.get(world);
    ans = ans + (loc.getX() < 0.0D ? Integer.valueOf((int)loc.getX()) : new StringBuilder("+").append((int)loc.getX()).toString());
    ans = ans + (loc.getY() < 0.0D ? Integer.valueOf((int)loc.getY()) : new StringBuilder("+").append((int)loc.getY()).toString());
    ans = ans + (loc.getZ() < 0.0D ? Integer.valueOf((int)loc.getZ()) : new StringBuilder("+").append((int)loc.getZ()).toString());
    return ans;
  }
  
  static Location locationDecode(String data)
  {
    if (data == null) {
      return null;
    }
    String[] d = data.split("(?=[+-])", -1);
    if (!allowedWorlds.containsValue(d[0])) {
      return null;
    }
    World w = null;
    for (String s : allowedWorlds.keySet()) {
      if (allowedWorlds.get(s).equals(d[0]))
      {
        w = Bukkit.getWorld(s);
        break;
      }
    }
    return new Location(w, Double.parseDouble(d[1]), Double.parseDouble(d[2]), Double.parseDouble(d[3]));
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.myportals.MyPortals
 * JD-Core Version:    0.7.0.1
 */