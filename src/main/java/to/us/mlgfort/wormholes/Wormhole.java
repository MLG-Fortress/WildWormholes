package to.us.mlgfort.wormholes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
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
    private final int size = 3;
    private Wormhole otherSide;
    private Location location;

    private Integer duration;
    private Integer mass;

    public Wormhole(int initialDuration, int initialMass, Location initialLocation, Location destinationLocation)
    {
        this.duration = initialDuration;
        this.mass = initialMass;
        this.location = initialLocation;
        this.otherSide = new Wormhole(this, destinationLocation);
    }

    private Wormhole(Wormhole wormhole, Location location)
    {
        this.location = location;
        this.otherSide = wormhole;
    }

    public Wormhole getOtherSide()
    {
        return otherSide;
    }

    public Location getLocation()
    {
        return location.clone();
    }

    public int getMass()
    {
        if (mass != null)
            return mass;
        return otherSide.mass;
    }

    public void reduceMass(int massToRemove)
    {
        if (this.mass != null)
            this.mass -= massToRemove;
        else
            otherSide.mass -= massToRemove;
    }

    public int getDuration()
    {
        if (this.duration != null)
            return duration;
        return otherSide.duration;
    }

    /**
     * Decreases duration of wormhole
     * @return if wormhole should've expired
     */
    public boolean tick()
    {
        if (duration == null)
            return otherSide.duration < 0;

        duration--;
        return duration < 0;
    }

    public void build()
    {
        //Don't bother building if the chunk isn't loaded
        if (!location.getChunk().isLoaded())
            return;

        int x = location.getBlockX();
        int y = location.getBlockY();
        y++;
        int z = location.getBlockZ();
        World world = location.getWorld();

        //Outer bedrock perimeter
        //Lazy way = set all of it to bedrock
        for (int x1 = x - size; x1 < x + size; x1++)
        {
            for (int z1 = z - size; z1 < z + size; z1++)
                world.getBlockAt(x1, location.getBlockY(), z1).setType(Material.BEDROCK);
        }

        //Clear out blocks above with a TRIPLE FOR-LOOP
        for (int x1 = x - size; x1 < x + size; x1++)
        {
            for (int z1 = z - size; z1 < z + size; z1++)
            {
                for (int y1 = y; y1 < y + 3; y1++)
                    world.getBlockAt(x1, y1, z1).setType(Material.AIR);
            }
        }

        //Inner portal blocks
        for (int x1 = x - (size - 1); x1 < x + (size - 1); x1++)
        {
            for (int z1 = z - (size - 1); z1 < z + (size - 1); z1++)
                world.getBlockAt(x1, location.getBlockY(), z1).setType(Material.ENDER_PORTAL);
        }
    }

    public void destroy()
    {
        int x = location.getBlockX();
        int y = location.getBlockY();
        y++;
        int z = location.getBlockZ();
        World world = location.getWorld();

        for (int x1 = x - size; x1 < x + size; x1++)
        {
            for (int z1 = z - size; z1 < z + size; z1++)
                world.getBlockAt(x1, location.getBlockY(), z1).setType(Material.AIR);
        }
    }

    public boolean isCloseEnough(Location location)
    {
        return this.location.getWorld() == location.getWorld() && this.location.distanceSquared(location) <= size * size;
    }

    public void playSound(String sound, float volume, float pitch)
    {
        location.getWorld().playSound(location, sound, volume, pitch);
    }
}
