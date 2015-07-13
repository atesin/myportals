package cl.netgamer.myportals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.block.Block;
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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import cl.netgamer.stepblock.StepBlock;
import cl.netgamer.stepblock.StepBlockEvent;

public final class MyListener implements Listener{
	
	// PROPERTIES
	
	private List<Integer> redBlocks = Arrays.asList(new Integer[]{27, 28, 36, 55, 69, 70, 72, 75, 76, 77, 93, 94, 94, 131, 132, 143, 147, 148, 149, 150, 152});;
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
	
	// pa cachar que pasa
	// no es un evento :(
	/*
	@EventHandler
	public void onBlockEvent(BlockEvent e){
		if (portalBlocks.containsKey(e.getBlock().getLocation())) MyPortals.log("event = "+e.getEventName());
	}
	*/
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event){
		step.clean(event.getPlayer());
	}
	
	/** stay lamps turned on if not actioned by redstone, requires static world while placing */
	@EventHandler
	public void onBlockPhysics(BlockPhysicsEvent event){
		if (event.getBlock().getTypeId() == 124 && !redBlocks.contains(event.getChangedTypeId())) event.setCancelled(true);
	}
	
	/** it seems when place fire on redlamp it generates a pulse and lamp powers off */
	@EventHandler
	public void onBlockRedstone(BlockRedstoneEvent event){
		if (portalBlocks.containsKey(event.getBlock().getLocation())) event.setNewCurrent(event.getOldCurrent());
	}
	
	/** it seems when place fire on redlamp it generates multi placement and powers off the lamp */
	/* @EventHandler
	public void onBlockMultiPlaceEvent(BlockMultiPlaceEvent event){
		if (portalBlocks.containsKey(event.getBlock().getLocation())){
			//event.setCancelled(true);
			//plugin.rebuild(event.getBlock().getLocation().clone().add(0, -1, 0));
			//MyPortals.log("loc = "+event.getBlock().getLocation());
			for (org.bukkit.block.BlockState bs: event.getReplacedBlockStates()){
				//MyPortals.log("bs pos = "+bs.getLocation());
				//MyPortals.log("bs mat = "+bs.getTypeId());
				//MyPortals.log("changed? = "+bs.setTypeId(124));
				
				
				
			}
		}
	} */
	
	
	// hay un evento que apaga la lampara, no es corriente de redstone
	// parece un evento de player porque el rebuild funciona bien
	// parece algo que tiene que ver con el encendedor o el fuego
	// (podias agregar sonidos de status a los teleport)
	// PUEDE SER BlockMultiPlaceEvent !!!
	
	
	
	/** self contained water portal blocks */
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event){
		if (portalBlocks.containsKey(event.getBlock().getLocation())) event.setCancelled(true);
	}

	// BLOCK PLACING = POSSIBLE PORTAL ACTIVATION
	
	// on player placing a possible portal warp
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event){
		// check materials, from simple to complex, step by step
		
		// check material of block placed, check only integer part is faster
		if (event.getBlock().getTypeId() != ((Number) chargeId).intValue()){
			return;
		}
		
		// check material of block below
		if (event.getBlock().getLocation().clone().add(0, -1, 0).getBlock().getTypeId() != ((Number) baseId).intValue()){
		//if (event.getBlock().getLocation().add(0, -1, 0).getBlock().getTypeId() != ((Number) plugin.portalId).intValue()){
			return;
		}
		
		// possible portal, get location we will use a lot
		Location loc = event.getBlock().getLocation().clone().add(0, -1, 0);
		int facing = plugin.shape.getFacing(loc);
		if (facing < 0) return;
		
		// looks like a portal, try to create
		if (!plugin.create(event.getPlayer(), loc, facing)) return;
		
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

	// on grab water portal with bucket
	@EventHandler
	public void onPlayerBucketFill(PlayerBucketFillEvent event){
		checkPortalDestroyed(event.getBlockClicked());
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
	/*@EventHandler
	public void blockPhysics(BlockPhysicsEvent event){
		if ( (event.getChangedTypeId() != 8 || event.getChangedTypeId() != 9) &&
			(event.getBlock().getTypeId() != 8 || event.getBlock().getTypeId() != 9) ){
			checkPortalDestroyed(event.getBlock(), "blockPhysics");
		}
	}*/
	
	// on water block update (solid)?
	@EventHandler
	public void onBlockCanBuild(BlockCanBuildEvent event){
		if (event.getMaterialId() != 8 || event.getMaterialId() != 9){
			checkPortalDestroyed(event.getBlock());
		}
	}
	
	// POSSIBLE PORTAL BLOCK DELETED
	
	// on block event, check portal shape
	private void checkPortalDestroyed(Block block){
		 // was portal material?
		if (!(materials.contains(block.getTypeId()))){
			return;
		}

		// belonged some portal?
		if (!(portalBlocks.containsKey(block.getLocation()))){
			return;
		}
		
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
		if (warps.containsKey(event.getPlayer().getName())){
			warps.remove(event.getPlayer().getName());
			event.getPlayer().removePotionEffect(PotionEffectType.getById(9));
		}

		// portal enter?: create warp task, schedule, store id and play nausea effect
		if (plugin.getPortalByLocation(event.getTo()) == null) return;
		
		WarpTask warp = new WarpTask(this, event.getPlayer(), event.getTo());
		warp.runTaskLater(plugin, 80);
		warps.put(event.getPlayer().getName(), warp.getTaskId());
		event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.getById(9), 160, 1));
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
