package to.us.mlgfort.wormholes;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created on 5/29/2017.
 *
 * @author RoboMWM
 */
public class Wormholes extends JavaPlugin
{
    public void onEnable()
    {
        Thera thera = new Thera(this);
        new WormholeSpawner(this, thera);
        new WormholeTransporter(this, thera);
    }
}
