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

    public void addWormhole(int initialDuration, int initialMass, Location initialLocation, Location destinationLocation)
    {
        Wormhole wormhole = new Wormhole(initialDuration, initialMass, initialLocation, destinationLocation);
        wormholes.add(wormhole);
        wormholes.add(wormhole.getOtherSide());
    }

    private void wormholeMaintenanceTask()
    {
        for (Wormhole wormhole : wormholes)
        {
            if (wormhole.tick())
            {
                wormholes.remove(wormhole.getOtherSide());
                wormholes.remove(wormhole);
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

    public void buildWormholes(Chunk chunk)
    {
        for (Wormhole wormhole : wormholes)
        {
            if (wormhole.getLocation().getChunk() == chunk)
                wormhole.build();
        }
    }
}
