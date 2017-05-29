package to.us.mlgfort.wormholes;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Created on 5/29/2017.
 *
 * Packages? What are those?
 *
 * @author RoboMWM
 */
public class Wormhole
{
    private Wormhole otherSide;
    private Location location;

    private int duration;
    private int mass;

    public Wormhole(int initialDuration, int initialMass, JavaPlugin instance)
    {
        this.duration = initialDuration;
        this.mass = initialMass;
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                duration--;
            }
        }.runTask(instance);
    }

    public int getMass()
    {
        return mass;
    }

    public void setMass(int mass)
    {
        this.mass = mass;
    }

    public int getDuration()
    {
        return duration;
    }

    public boolean buildWormhole()
    {

    }

    public boolean isWithin()
    {

    }

    /**
     *
     * @return
     */
    public boolean isClear()
    {

    }
}
