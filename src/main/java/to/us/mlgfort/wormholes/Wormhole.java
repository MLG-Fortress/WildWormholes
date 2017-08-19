package to.us.mlgfort.wormholes;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
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
    //private final int size = 3;
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
        //y++;
        int z = location.getBlockZ();
        World world = location.getWorld();

        setBlock(location.getBlock(), Material.END_GATEWAY);
        setBlock(world.getBlockAt(x, location.getBlockY() - 1, z), Material.AIR);
        setBlock(world.getBlockAt(x, location.getBlockY() - 2, z), Material.AIR);
        setBlock(world.getBlockAt(x, location.getBlockY() - 3, z), Material.BEDROCK);

        //The following builds a 4x4 portal, perimeter made of bedrock, inside made of end portal
        //This is also provided that int size = 3;
        /*
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
        */
    }

    public void setBlock(Block block, Material material)
    {
        if (block.getType() != material)
            block.setType(material);
    }

    public void destroy()
    {
        int x = location.getBlockX();
        int y = location.getBlockY();
        //y++;
        int z = location.getBlockZ();
        World world = location.getWorld();

        location.getBlock().setType(Material.AIR);
        world.getBlockAt(x, location.getBlockY() - 3, z).setType(Material.AIR);

        /*
        for (int x1 = x - size; x1 < x + size; x1++)
        {
            for (int z1 = z - size; z1 < z + size; z1++)
                world.getBlockAt(x1, location.getBlockY(), z1).setType(Material.AIR);
        }
        */
    }

    public boolean isCloseEnough(Location location)
    {
        //return this.location.getWorld() == location.getWorld() && this.location.distanceSquared(location) <= size * size;

        //Since the actual portal block is a single block, a distance of 2 should be sufficient to lazily determine if an entity is within it.
        return this.location.getWorld() == location.getWorld() && this.location.distanceSquared(location) <= 4;
    }

    public void playSound(String sound, float volume, float pitch)
    {
        location.getWorld().playSound(location, sound, volume, pitch);
    }
}
