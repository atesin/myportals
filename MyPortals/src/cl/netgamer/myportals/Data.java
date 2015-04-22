package cl.netgamer.myportals;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

class Data
{
  private ConfigAccessor conf;
  
  Data(MyPortals plugin)
  {
    this.conf = new ConfigAccessor(plugin, "data.yml");
    this.conf.saveDefaultConfig();
  }
  
  void savePortal(Portal portal)
  {
    this.conf.getConfig().set(MyPortals.locationEncode(portal.getLocation()), portal.encode());
    this.conf.saveConfig();
  }
  
  void delPortal(Portal portal)
  {
    this.conf.getConfig().set(MyPortals.locationEncode(portal.getLocation()), null);
    this.conf.saveConfig();
  }
  
  Map<Location, Portal> loadPortals()
  {
    Map<Location, Portal> portals = new HashMap();
    for (String key : this.conf.getConfig().getKeys(false))
    {
      Location loc = MyPortals.locationDecode(key);
      portals.put(loc, new Portal(loc, this.conf.getConfig().getStringList(key)));
    }
    return portals;
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.myportals.Data
 * JD-Core Version:    0.7.0.1
 */