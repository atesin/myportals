package cl.netgamer.myportals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import cl.netgamer.stepblock.StepBlock;
import cl.netgamer.stepblock.StepBlockEvent;

public final class MyListener implements Listener{
	
	// PROPERTIES
	
	//private List<Integer> redBlocks = Arrays.asList(new Integer[]{27, 28, 36, 55, 69, 70, 72, 75, 76, 77, 93, 94, 94, 131, 132, 143, 147, 148, 149, 150, 152});;
	protected MyPortals plugin;
	private int baseId;
	private int chargeId;
	private List<Integer> materials;
	private Map<Location, ArrayList<Portal>> portalBlocks;
	//private Map<Location, ArrayList<Portal>> portalBlocks;
	public Map<String, Integer> warps = new HashMap<String, Integer>();
	private StepBlock step = new StepBlock();
	
	// CONSTRUCTORS	
	
	public MyListener(MyPortals plugin, int baseId, int chargeId, List<Integer> materials, Map<Location, ArrayList<Portal>> portalBlocks){
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.plugin = plugin;
		this.baseId = baseId;
		this.chargeId = chargeId;
		this.materials = materials;
		this.portalBlocks = portalBlocks;
	}
	
	// REGULAR METHODS
	
	/** displays a welcome/help message when player joins server */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event){
		event.getPlayer().sendMessage(plugin.cmd.msg("welcome"));
	}
	
	/** cleans blocks step array when player leaves to prevent growing */
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		step.clean(event.getPlayer());
	}
	
	/** stay lamps turned on if (not actioned by redstone) belongs a portal, requires static world while placing */
	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event){
		//if (event.getBlock().getTypeId() == 124 && !redBlocks.contains(event.getChangedTypeId())) event.setCancelled(true);
		if (portalBlocks.containsKey(event.getBlock().getLocation())) event.setCancelled(true);
	}
	
	/** it seems when place fire on redlamp it generates a pulse and lamp powers off */
	@EventHandler
	public void onBlockRedstone(BlockRedstoneEvent event){
		if (portalBlocks.containsKey(event.getBlock().getLocation())) event.setNewCurrent(event.getOldCurrent());
	}

	/** self contained water portal blocks */
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event){
		if (portalBlocks.containsKey(event.getBlock().getLocation())) event.setCancelled(true);
	}
	
	// cancel try grab water portal with bucket
	/* @EventHandler
	public void onPlayerBucket(PlayerBucketEvent event){
		if (portalBlocks.containsKey(event.getBlockClicked().getLocation())) event.setCancelled(true);
	} */
	
	// cancel try grab water portal with bucket
	/* @EventHandler
	public void onPlayerInteract(PlayerInteractEvent event){
		if (portalBlocks.containsKey(event.getClickedBlock().getLocation())) event.setCancelled(true);
	} */	
	

	// BLOCK PLACING = POSSIBLE PORTAL ACTIVATION
	
	// on player placing a possible portal warp
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		// check materials, from simple to complex, step by step
		if(plugin.blockplacecooldown) return;
		plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			public void run() {
			    plugin.blockplacecooldown = false;
			}
		}, 2L);
		
		//MyPortals.log("Placed Block: " + event.getBlock().getType().getId());
		
		// check material of block placed, check only integer part is faster
		if (event.getBlock().getTypeId() != ((Number) chargeId).intValue()){
			return;
		}
		plugin.blockplacecooldown = true;
		
		//MyPortals.log("Block is charge");
		
		// check material of block below
		if (event.getBlock().getLocation().clone().add(0, -1, 0).getBlock().getTypeId() != ((Number) baseId).intValue()){
		//if (event.getBlock().getLocation().add(0, -1, 0).getBlock().getTypeId() != ((Number) plugin.portalId).intValue()){
			return;
		}
		
		//MyPortals.log("Block is on base");
		
		// possible portal, get location we will use a lot
		Location loc = event.getBlock().getLocation().clone().add(0, -1, 0);
		int facing = plugin.shape.getFacing(loc);
		if (facing < 0) return;
		
		//MyPortals.log("Facing = " + facing);
		
		// looks like a portal, try to create
		if (!plugin.create(event.getPlayer(), loc, facing)) return;
		
		//MyPortals.log("Looks like a portal");
		
		// add portal blocks, block -> base, base...
		for (Location l: plugin.shape.getPortalBlocks(loc, facing)){
			if (!portalBlocks.containsKey(l)) portalBlocks.put(l, new ArrayList<Portal>());
			portalBlocks.get(l).add(plugin.getPortalByLocation(loc));
		}
	}
	
	// BLOCK DELETED SOME WAY = POSSIBLE PORTAL DESTROY
	
	// on player breaks a block
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event){
		checkPortalDestroyed(event.getBlock());
	}

	// on explosion, it handles a list of destroyed blocks
	@EventHandler
	public void onEntityExplode(EntityExplodeEvent event){
		// loop destroyed blocks
		for (Block block: event.blockList()){
			checkPortalDestroyed(block);
		}
	}
	
	// on block burn, if portal made of wood
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event){
		checkPortalDestroyed(event.getBlock());
	}
	
	// on water block update (lava)?
	/* @EventHandler
	public void blockPhysics(BlockPhysicsEvent event){
		if ( (event.getChangedTypeId() != 8 || event.getChangedTypeId() != 9) &&
			(event.getBlock().getTypeId() != 8 || event.getBlock().getTypeId() != 9) ){
			checkPortalDestroyed(event.getBlock(), "blockPhysics");
		}
	} */
	
	// on water block update (solid)?
	@EventHandler
	public void onBlockCanBuild(BlockCanBuildEvent event){
		if (event.getMaterialId() != 8 || event.getMaterialId() != 9){
			checkPortalDestroyed(event.getBlock());
		}
	}
	
	// on grab water portal with bucket
	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event){
		if(isPartOfPortal(event.getBlockClicked())) {
			event.setCancelled(true);
			//checkPortalDestroyed(event.getBlockClicked());
		}
	}

	
	// POSSIBLE PORTAL BLOCK DELETED
	
	private boolean isPartOfPortal(Block block) {
		// was portal material?
		if (materials.contains(block.getTypeId())){
			return true;
		}

		// belonged some portal?
		if (portalBlocks.containsKey(block.getLocation())){
			return true;
		}
		
		return false;
	}
	
	// on block event, check portal shape
	private void checkPortalDestroyed(Block block){
		
		if(!isPartOfPortal(block)) return;
		
		// what portals belonged to? (clone to avoid removing while iterating problems)
		ArrayList<Portal> ofPortals = (ArrayList<Portal>)(portalBlocks.get(block.getLocation())).clone();
		
		// delete portal blocks and portal
		for (Portal p: ofPortals){
			delPortalBlocks(p);
			plugin.destroy(p);
		}
	}
	
	// STEPS, DONT KNOW IS BETTER WITH EVENTS OR POLLING
	
	// maybe is better with polling
	
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event){
		step.check(event.getPlayer());
	}
	
	@EventHandler
	public void onStepBlock(StepBlockEvent event){
		// player move?, cancel warp task if exists
		//MyPortals.log("Step");
		if (warps.containsKey(event.getPlayer().getName())){
			warps.remove(event.getPlayer().getName());
			event.getPlayer().removePotionEffect(PotionEffectType.CONFUSION);
		}

		// portal enter?: create warp task, schedule, store id and play nausea effect
		if (plugin.getPortalByLocation(event.getTo()) == null) return;
		
		WarpTask warp = new WarpTask(this, event.getPlayer(), event.getTo());
		warp.runTaskLater(plugin, MyPortals.waitTime);
		warps.put(event.getPlayer().getName(), warp.getTaskId());
		event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 160, 1));
		event.getPlayer().playEffect(event.getPlayer().getEyeLocation(), Effect.ENDER_SIGNAL, 10);
	}
	
	@EventHandler
	public void onPistonExtend(BlockPistonExtendEvent event) {
		for(Block block : event.getBlocks()) {
			checkPortalDestroyed(block);
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Portal p = plugin.getPortalByLocation(event.getClickedBlock().getLocation());
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getItem().getType() == Material.COMPASS) {
			if (p != null) {
				if (p.canWarp(event.getPlayer())) return;
				Location dest = p.getDestination();
				if (dest != null) {
					event.getPlayer().setCompassTarget(dest);
				}
			}
			if (event.getPlayer().isSneaking()) {
				Location spawn = event.getPlayer().getWorld().getSpawnLocation();
				event.getPlayer().setCompassTarget(spawn);
			}
		}
	}
	
	// UTILITY
	
	/** with iterator to avoid remove while iterating problems */
	private void delPortalBlocks(Portal portal){
		Iterator<Map.Entry<Location, ArrayList<Portal>>> i = portalBlocks.entrySet().iterator();
		while (i.hasNext()){
			Entry<Location, ArrayList<Portal>> entry = i.next();
			if (entry.getValue().contains(portal)){
				if (entry.getValue().size() > 1) entry.getValue().remove(portal);
				else i.remove();
			}
		}
	}

	
}
