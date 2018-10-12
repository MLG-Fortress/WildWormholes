package to.us.mlgfort.wormholes;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;

/**
 * Created on 5/29/2017.
 *
 * @author RoboMWM
 */
public class Wormholes extends JavaPlugin
{
    private Thera thera;

    //TODO: Store and cleanup old wormholes (in case of server crash)
    public void onEnable()
    {
        thera = new Thera(this);
        new WormholeSpawner(this, thera, new HashSet<>()); //TODO: add config for world blacklist
        new WormholeTransporter(this, thera);
    }

    public void onDisable()
    {
        getLogger().info("Destroying " + thera.destroyAllWormholes() + " wormholes.");
    }
}
