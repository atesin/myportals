package cl.netgamer.stepblock;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class StepBlockEvent
  extends Event
{
  private static final HandlerList handlers = new HandlerList();
  private Player player;
  private Location to;
  private Location from;
  
  public StepBlockEvent(Player player, Location to, Location from)
  {
    this.player = player;
    this.to = to;
    this.from = from;
  }
  
  public HandlerList getHandlers()
  {
    return handlers;
  }
  
  public static HandlerList getHandlerList()
  {
    return handlers;
  }
  
  public Player getPlayer()
  {
    return this.player;
  }
  
  public Location getTo()
  {
    return this.to;
  }
  
  public Location getFrom()
  {
    return this.from;
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.stepblock.StepBlockEvent
 * JD-Core Version:    0.7.0.1
 */