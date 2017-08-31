package to.us.mlgfort.wormholes;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on 5/29/2017.
 *
 * @author RoboMWM
 */
public class Thera
{
    private Set<Wormhole> wormholes = new HashSet<>();
    boolean building = false; //Used to prevent CME's cuz rite now we use chunkloadevent to add new wormholes... and building wormholes can cause more chunk load events.

    public Thera(JavaPlugin plugin)
    {
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                wormholeMaintenanceTask();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public Wormhole addWormhole(int initialDuration, int initialMass, Location initialLocation, Location destinationLocation)
    {
        if (building)
            return null;
        Wormhole wormhole = new Wormhole(initialDuration, initialMass, initialLocation, destinationLocation);
        wormholes.add(wormhole);
        wormholes.add(wormhole.getOtherSide());
        return wormhole;
    }

    private void wormholeMaintenanceTask()
    {
        for (Wormhole wormhole : wormholes)
        {
            if (wormhole.tick())
            {
                wormhole.destroy();
                wormhole.getOtherSide().destroy();

                //TODO: sounds of destruction

                wormholes.remove(wormhole.getOtherSide());
                wormholes.remove(wormhole);
                return;
            }
        }
    }

    public Wormhole getWormhole(Location location)
    {
        for (Wormhole wormhole : wormholes)
        {
            if (wormhole.isCloseEnough(location))
            {
                return wormhole;
            }
        }
        return null;
    }

    /**
     * For now, only returns a single Wormhole object, since there should only be one wormhole in a chunk
     * @param chunk
     * @return
     */
    public Wormhole getWormhole(Chunk chunk)
    {
        for (Wormhole wormhole : wormholes)
        {
            if (wormhole.getLocation().getChunk() == chunk)
            {
                return wormhole;
            }
        }
        return null;
    }

    public void buildWormholes(Chunk chunk)
    {
        if (building) //poor man's "thread lock"
            return;

        building = true;

        for (Wormhole wormhole : wormholes)
        {
            if (wormhole.getLocation().getChunk() == chunk)
                wormhole.build();
        }

        building = false;
    }

    public int destroyAllWormholes()
    {
        int size = wormholes.size();
        for (Wormhole wormhole : wormholes)
            wormhole.destroy();
        return size;
    }

}
