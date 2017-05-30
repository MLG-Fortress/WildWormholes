package to.us.mlgfort.wormholes;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 5/29/2017.
 *
 * @author RoboMWM
 */
public class WormholeSpawner implements Listener
{
    Wormholes instance;
    List<World> worlds = new ArrayList<>(4);
    Thera thera;

    public WormholeSpawner(Wormholes instance, Thera thera)
    {
        this.instance = instance;
        this.thera = thera;

        //le custom code
        //worlds wormholes should spawn in
        worlds.add(instance.getServer().getWorld("world"));
        worlds.add(instance.getServer().getWorld("world_nether"));
        worlds.add(instance.getServer().getWorld("world_the_end"));
        worlds.add(instance.getServer().getWorld("cityworld"));
        //worlds.add(instance.getServer().getWorld("cityworld_nether"));
    }

    public void onChunkLoad(ChunkLoadEvent event)
    {
        //Build any wormholes that should be here or something
        thera.buildWormholes(event.getChunk());

        //Spawn a new wormhole?
        //TODO: change
        if (r4nd0m(0, 20) < 18)
            return;

        Location location = event.getChunk().getBlock(ThreadLocalRandom.current().nextInt(16), 255, ThreadLocalRandom.current().nextInt(16)).getLocation();

        location.setY(location.getWorld().getHighestBlockYAt(location));

        //TODO: nether roof support (or just not even worry about getting a clear block. Might just do that instead.)

        thera.addWormhole(86400, 3000, location, randomLocation(location));
    }

    private Location randomLocation(Location initialLocation)
    {
        World world = worlds.get(r4nd0m(0, worlds.size()));
        Location borderCenter = world.getWorldBorder().getCenter();
        int borderSize = (int)world.getWorldBorder().getSize() - 1000; //i.e. world must have a border size far above 1000

        int randomX = r4nd0m(borderCenter.getBlockX() + borderSize, borderCenter.getBlockX() - borderSize);
        int randomZ = r4nd0m(borderCenter.getBlockZ() + borderSize, borderCenter.getBlockZ() - borderSize);

        Location location = new Location(world, randomX, r4nd0m(10, 200), randomZ);

        //Make sure it's not right next to the initial location (rare)
        if (location.getWorld() != initialLocation.getWorld() && location.distanceSquared(initialLocation) > 500)
            return location;
        //otherwise try again
        return randomLocation(initialLocation);
    }

    public int r4nd0m(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }


}
