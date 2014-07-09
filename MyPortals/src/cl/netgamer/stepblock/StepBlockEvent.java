package cl.netgamer.stepblock;


import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public final class StepBlockEvent extends Event{

	// PROPERTIES
	
	private static final HandlerList handlers = new HandlerList();
	private Player player;
	private Location to;
	private Location from;
	
	// CONSTRUCTOR
	
	public StepBlockEvent(Player player, Location to, Location from){
		this.player = player;
		this.to = to;
		this.from = from;
	}
	
	// INTERNAL METHODS USED BY BUKKIT
	
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public static HandlerList getHandlerList() {
	    return handlers;
	}
	
	// GETTER METHODS
	public Player getPlayer(){
		return player;
	}

	public Location getTo(){
		return to;
	}

	public Location getFrom(){
		return from;
	}

}
