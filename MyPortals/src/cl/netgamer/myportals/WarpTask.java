package cl.netgamer.myportals;

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

public class WarpTask
  extends BukkitRunnable
{
  private MyListener parent;
  private Player player;
  private Location location;
  
  public WarpTask(MyListener parent, Player player, Location location)
  {
    this.parent = parent;
    this.player = player;
    this.location = location;
  }
  
  public void run()
  {
    if (!this.parent.warps.containsValue(Integer.valueOf(getTaskId()))) {
      return;
    }
    this.player.removePotionEffect(PotionEffectType.getById(9));
    

    int xp = this.player.getTotalExperience();
    if (xp < this.parent.plugin.xpCost) {
      return;
    }
    Portal portal = this.parent.plugin.getPortalByLocation(this.location);
    if (portal == null) {
      return;
    }
    portal = this.parent.plugin.getPortalByLocation(portal.getDestination());
    if (portal == null) {
      return;
    }
    if (!portal.warp(this.player)) {
      return;
    }
    this.player.setTotalExperience(0);
    this.player.setLevel(0);
    this.player.setExp(0.0F);
    this.player.giveExp(xp - this.parent.plugin.xpCost);
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.myportals.WarpTask
 * JD-Core Version:    0.7.0.1
 */