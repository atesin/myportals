package cl.netgamer.stepblock;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

public class StepBlock
{
  private Map<String, Location> steps = new HashMap();
  
  public void clean(Player player)
  {
    if (this.steps.containsKey(player.getName())) {
      this.steps.remove(player.getName());
    }
  }
  
  public void check(Player player)
  {
    if (!player.isOnGround()) {
      return;
    }
    String name = player.getName();
    Location loc = player.getLocation();
    Location to = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ());
    
    Location from = (Location)this.steps.get(name);
    if (from == null)
    {
      this.steps.put(name, to);
      return;
    }
    if (from.equals(to)) {
      return;
    }
    this.steps.put(name, to);
    StepBlockEvent e = new StepBlockEvent(player, to, from);
    Bukkit.getServer().getPluginManager().callEvent(e);
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.stepblock.StepBlock
 * JD-Core Version:    0.7.0.1
 */