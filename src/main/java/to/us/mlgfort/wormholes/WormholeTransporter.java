package to.us.mlgfort.wormholes;

import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 5/29/2017.
 *
 * @author RoboMWM
 */
public class WormholeTransporter implements Listener
{
    Thera thera;

    public WormholeTransporter(JavaPlugin plugin, Thera thera)
    {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.thera = thera;
    }

    void onEnterWormhole(EntityPortalEnterEvent event)
    {
        Wormhole wormhole = thera.getWormhole(event.getLocation());
        if (wormhole == null)
            return;

        Location location = wormhole.getOtherSide().getLocation();

        int randomX = 3;
        int randomZ = 3;

        if (ThreadLocalRandom.current().nextBoolean())
            randomX = r4nd0m(-3, 3);
        else
            randomZ = r4nd0m(-3, 3);

        if (ThreadLocalRandom.current().nextBoolean())
        {
            randomX = -randomX;
            randomZ = -randomZ;
        }

        location.add(randomX, 1, randomZ);
        event.getEntity().teleport(location);
    }

    public int r4nd0m(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}
