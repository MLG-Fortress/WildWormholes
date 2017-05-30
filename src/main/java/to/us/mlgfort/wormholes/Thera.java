package to.us.mlgfort.wormholes;

import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;

/**
 * Created on 5/29/2017.
 *
 * @author RoboMWM
 */
public class Thera
{
    Set<Wormhole> wormholes = new HashSet<>();

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

    }

    public void buildWormholes(Chunk chunk)
    {
        for (Wormhole wormhole : wormholes)
        {
            if (wormhole.getLocation().getChunk() == chunk)
                wormhole.build();
        }
    }

    public boolean isNearPortal(Location location)
    {

    }
}
