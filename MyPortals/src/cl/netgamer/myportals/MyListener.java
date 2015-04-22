package cl.netgamer.myportals;

import cl.netgamer.stepblock.StepBlock;
import cl.netgamer.stepblock.StepBlockEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockMultiPlaceEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class MyListener
  implements Listener
{
  private List<Integer> redBlocks = Arrays.asList(new Integer[] { Integer.valueOf(27), Integer.valueOf(28), Integer.valueOf(36), Integer.valueOf(55), Integer.valueOf(69), Integer.valueOf(70), Integer.valueOf(72), Integer.valueOf(75), Integer.valueOf(76), Integer.valueOf(77), Integer.valueOf(93), Integer.valueOf(94), Integer.valueOf(94), Integer.valueOf(131), Integer.valueOf(132), Integer.valueOf(143), Integer.valueOf(147), Integer.valueOf(148), Integer.valueOf(149), Integer.valueOf(150), Integer.valueOf(152) });
  protected MyPortals plugin;
  private int baseId;
  private int chargeId;
  private List<Integer> materials;
  private Map<Location, ArrayList<Portal>> portalBlocks;
  public Map<String, Integer> warps = new HashMap();
  private StepBlock step = new StepBlock();
  
  public MyListener(MyPortals plugin, int baseId, int chargeId, List<Integer> materials, Map<Location, ArrayList<Portal>> portalBlocks)
  {
    plugin.getServer().getPluginManager().registerEvents(this, plugin);
    this.plugin = plugin;
    this.baseId = baseId;
    this.chargeId = chargeId;
    this.materials = materials;
    this.portalBlocks = portalBlocks;
  }
  
  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event)
  {
    this.step.clean(event.getPlayer());
  }
  
  @EventHandler
  public void onBlockPhysics(BlockPhysicsEvent event)
  {
    if ((event.getBlock().getTypeId() == 124) && (!this.redBlocks.contains(Integer.valueOf(event.getChangedTypeId())))) {
      event.setCancelled(true);
    }
  }
  
  @EventHandler
  public void onBlockRedstone(BlockRedstoneEvent event)
  {
    if (this.portalBlocks.containsKey(event.getBlock().getLocation())) {
      event.setNewCurrent(event.getOldCurrent());
    }
  }
  
  @EventHandler
  public void onBlockMultiPlaceEvent(BlockMultiPlaceEvent event)
  {
    if (this.portalBlocks.containsKey(event.getBlock().getLocation())) {
      for (BlockState bs : event.getReplacedBlockStates())
      {
        MyPortals.log("bs pos = " + bs.getLocation());
        MyPortals.log("bs mat = " + bs.getTypeId());
        MyPortals.log("changed? = " + bs.setTypeId(124));
      }
    }
  }
  
  @EventHandler
  public void onBlockFromTo(BlockFromToEvent event)
  {
    if (this.portalBlocks.containsKey(event.getBlock().getLocation())) {
      event.setCancelled(true);
    }
  }
  
  @EventHandler
  public void onBlockPlace(BlockPlaceEvent event)
  {
    if (event.getBlock().getTypeId() != Integer.valueOf(this.chargeId).intValue()) {
      return;
    }
    if (event.getBlock().getLocation().clone().add(0.0D, -1.0D, 0.0D).getBlock().getTypeId() != Integer.valueOf(this.baseId).intValue()) {
      return;
    }
    Location loc = event.getBlock().getLocation().clone().add(0.0D, -1.0D, 0.0D);
    int facing = this.plugin.shape.getFacing(loc);
    if (facing < 0) {
      return;
    }
    if (!this.plugin.create(event.getPlayer(), loc, facing)) {
      return;
    }
    for (Location l : this.plugin.shape.getPortalBlocks(loc, facing))
    {
      if (!this.portalBlocks.containsKey(l)) {
        this.portalBlocks.put(l, new ArrayList());
      }
      ((ArrayList)this.portalBlocks.get(l)).add(this.plugin.getPortalByLocation(loc));
    }
  }
  
  @EventHandler
  public void onBlockBreak(BlockBreakEvent event)
  {
    checkPortalDestroyed(event.getBlock());
  }
  
  @EventHandler
  public void onPlayerBucketFill(PlayerBucketFillEvent event)
  {
    checkPortalDestroyed(event.getBlockClicked());
  }
  
  @EventHandler
  public void onEntityExplode(EntityExplodeEvent event)
  {
    for (Block block : event.blockList()) {
      checkPortalDestroyed(block);
    }
  }
  
  @EventHandler
  public void onBlockBurn(BlockBurnEvent event)
  {
    checkPortalDestroyed(event.getBlock());
  }
  
  @EventHandler
  public void onBlockCanBuild(BlockCanBuildEvent event)
  {
    if ((event.getMaterialId() != 8) || (event.getMaterialId() != 9)) {
      checkPortalDestroyed(event.getBlock());
    }
  }
  
  private void checkPortalDestroyed(Block block)
  {
    if (!this.materials.contains(Integer.valueOf(block.getTypeId()))) {
      return;
    }
    if (!this.portalBlocks.containsKey(block.getLocation())) {
      return;
    }
    ArrayList<Portal> ofPortals = (ArrayList)((ArrayList)this.portalBlocks.get(block.getLocation())).clone();
    for (Portal p : ofPortals)
    {
      delPortalBlocks(p);
      this.plugin.destroy(p);
    }
  }
  
  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event)
  {
    this.step.check(event.getPlayer());
  }
  
  @EventHandler
  public void onStepBlock(StepBlockEvent event)
  {
    if (this.warps.containsKey(event.getPlayer().getName()))
    {
      this.warps.remove(event.getPlayer().getName());
      event.getPlayer().removePotionEffect(PotionEffectType.getById(9));
    }
    if (this.plugin.getPortalByLocation(event.getTo()) == null) {
      return;
    }
    WarpTask warp = new WarpTask(this, event.getPlayer(), event.getTo());
    warp.runTaskLater(this.plugin, 80L);
    this.warps.put(event.getPlayer().getName(), Integer.valueOf(warp.getTaskId()));
    event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.getById(9), 160, 1));
  }
  
  private void delPortalBlocks(Portal portal)
  {
    Iterator<Map.Entry<Location, ArrayList<Portal>>> i = this.portalBlocks.entrySet().iterator();
    while (i.hasNext())
    {
      Map.Entry<Location, ArrayList<Portal>> entry = (Map.Entry)i.next();
      if (((ArrayList)entry.getValue()).contains(portal)) {
        if (((ArrayList)entry.getValue()).size() > 1) {
          ((ArrayList)entry.getValue()).remove(portal);
        } else {
          i.remove();
        }
      }
    }
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.myportals.MyListener
 * JD-Core Version:    0.7.0.1
 */