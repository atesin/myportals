package cl.netgamer.myportals;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;

public class Lang
{
  private ConfigAccessor conf = null;
  Map<String, String> msg = new HashMap();
  
  public Lang(MyPortals plugin, String locale)
  {
    this.conf = new ConfigAccessor(plugin, "locale-" + locale + ".yml");
    this.conf.saveDefaultConfig();
    


    this.msg.put("help1", "§E");
    this.msg.put("help2", "§E");
    this.msg.put("help3", "§E");
    this.msg.put("help4", "§E");
    this.msg.put("tags", "");
    this.msg.put("activatedOk", "§B");
    this.msg.put("bePlayer", "§D");
    this.msg.put("argsNotMatch", "§D");
    this.msg.put("unknownCmd", "§D");
    this.msg.put("listHead", "§E");
    this.msg.put("tooFar", "§D");
    this.msg.put("lookNotPortal", "§D");
    this.msg.put("hidden", "§D");
    this.msg.put("locked", "§D");
    this.msg.put("invalidName", "§D");
    this.msg.put("busyName", "§D");
    this.msg.put("namedOk", "§B");
    this.msg.put("nameNotFound", "§D");
    this.msg.put("diffrentWorlds", "§D");
    this.msg.put("noName", "§D");
    this.msg.put("destOk", "§B");
    this.msg.put("privacyOk", "§B");
    this.msg.put("notYours", "§D");
    this.msg.put("offlinePlayer", "§D");
    this.msg.put("giveOk", "§B");
    this.msg.put("beConsole", "§D");
    this.msg.put("mustConfirm", "§D");
    this.msg.put("rebuilding", "§E");
    this.msg.put("rebuilded", "§B");
    String key;
    for (Iterator localIterator = this.msg.keySet().iterator(); localIterator.hasNext(); this.msg.put(key, (String)this.msg.get(key) + this.conf.getConfig().getString(key))) {
      key = (String)localIterator.next();
    }
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.myportals.Lang
 * JD-Core Version:    0.7.0.1
 */