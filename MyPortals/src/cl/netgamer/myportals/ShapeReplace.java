package cl.netgamer.myportals;

import java.lang.reflect.Field;
import net.minecraft.server.v1_8_R2.World;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R2.CraftWorld;

class ShapeReplace
  implements ShapeInterface
{
  public boolean use(Location loc, Number ref, Location base)
    throws Exception
  {
    if (ref.intValue() == 124)
    {
      World w = ((CraftWorld)loc.getBlock().getWorld()).getHandle();
      setWorldStatic(w, true);
      
      loc.getBlock().setTypeId(124);
      
      setWorldStatic(w, false);
      








































      return true;
    }
    loc.getBlock().setTypeId(ref.intValue());
    return true;
  }
  
  private static void setWorldStatic(World world, boolean static_boolean)
    throws Exception
  {
    Field static_field = World.class.getDeclaredField("isClientSide");
    
    static_field.setAccessible(true);
    static_field.set(world, Boolean.valueOf(static_boolean));
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.myportals.ShapeReplace
 * JD-Core Version:    0.7.0.1
 */