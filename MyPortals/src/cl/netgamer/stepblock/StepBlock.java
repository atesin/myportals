package cl.netgamer.stepblock;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * class to check when a player step another block
 * 
 * // in your listener, instantiate this class and listen PlayerMoveEvent like this:
 * private StepBlock step = new StepBlock();
 * @EventHandler
 * public void onPlayerMove(PlayerMoveEvent event){
 *   step.check(event.getPlayer());
 * }
 * // this will generate a StepBlockEvent if the player steps onto another block
 * 
 * // in your listener do this again
 * @EventHandler
 * public void onStepBlock(StepBlockEvent event){
 *   // whatever you want with the event
 * }
 * 
 * // to keep clean your memory also in your listener do this
 * 	@EventHandler
 * public void onPlayerQuit(PlayerQuitEvent event){
 *   step.clean(event.getPlayer());
 * }
 * 
 * @author atesin#gmail.com
 */
public class StepBlock {
	
	private Map<String, Location> steps = new HashMap<String, Location>();
	
	public void clean(Player player){
		if (steps.containsKey(player.getName())) steps.remove(player.getName());
	}
	
	public void check(Player player){
		// only when stepping the ground
		if (!((Entity) player).isOnGround()) return;
		
		String name = player.getName();
		Location loc = player.getLocation();
		Location to = new Location(loc.getWorld(), loc.getBlockX(), loc.getBlockY()-1, loc.getBlockZ());
		
		Location from = steps.get(name);
		// last step not registered yet?
		if (from == null){
			steps.put(name, to);
			return;
		}
		
		// stepping over the same block?
		if (from.equals(to)) return;
		
		// stepped into another block, store it and 
		steps.put(name, to);
		new StepBlockEvent(player, to, from);
	}
}
