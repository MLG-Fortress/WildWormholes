package to.us.mlgfort.wormholes;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 5/29/2017.
 *
 * @author RoboMWM
 */
public class WormholeSpawner implements Listener
{
    private JavaPlugin instance;
    private List<World> worlds = new ArrayList<>(4);
    private Thera thera;

    public WormholeSpawner(JavaPlugin plugin, Thera thera)
    {
        this.instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.thera = thera;

        //le custom code
        //worlds wormholes should spawn in
        worlds.add(plugin.getServer().getWorld("world"));
        worlds.add(plugin.getServer().getWorld("world_nether"));
        worlds.add(plugin.getServer().getWorld("world_the_end"));
        worlds.add(plugin.getServer().getWorld("cityworld"));
        worlds.add(plugin.getServer().getWorld("cityworld_nether"));
        worlds.add(plugin.getServer().getWorld("wellworld"));
        worlds.add(plugin.getServer().getWorld("maxiworld"));

        //Spawn wormholes at random time intervals or something
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
//                if (r4nd0m(0, 5) > 4)
//                    return;

                //If nobody's on, there's not many chunks to decide where to spawn.
                if (instance.getServer().getOnlinePlayers().isEmpty())
                    return;

                Chunk[] chunks = worlds.get(r4nd0m(0, worlds.size() - 1)).getLoadedChunks();
                if (chunks.length <= 0) return;
                Chunk chunk = chunks[r4nd0m(0, chunks.length - 1)];

                //Ensure a player is somewhat nearby (within view distance)
                if (!playerNearby(chunk.getBlock(8, 64, 8).getLocation(), instance.getServer().getViewDistance() * 16))
                    return;

                //Don't spawn wormholes on top of players
                //if (playerNearby(chunk.getBlock(8, 64, 8).getLocation(), instance.getServer().getViewDistance() * 16 / 4))
                //    return;

                //Only max of one wormhole in a chunk
                if (thera.getWormhole(chunk) != null)
                    return;

                Location location = randomLocation(chunk, 2);
                Location otherSide = randomLocation(location, 2);
                if (location == null || otherSide == null)
                    return;

                thera.addWormhole(86400, 3000, location, otherSide);

                //Build newly-spawned wormholes
                thera.buildWormholes(chunk);
                thera.buildWormholes(otherSide.getChunk());

                //TODO: debug
                plugin.getLogger().info("Spawned a wormhole at " + location.toString() + "\nWith the other side at " + otherSide.toString());
            }
        }.runTaskTimer(plugin, 1200L, 1200L);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event)
    {
        //Ignore if not a valid world to spawn wormholes in
        if (!worlds.contains(event.getWorld()))
            return;

        //Build any wormholes that should be here
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                thera.buildWormholes(event.getChunk());
            }
        }.runTaskLater(instance, 10L);
    }

    private Location randomLocation(Location initialLocation, int attempts)
    {
        if (initialLocation == null || attempts < 0)
            return null;

        int minY = 10;
        int maxY = 200;
        World world = worlds.get(r4nd0m(0, worlds.size() - 1));
        if (isVanillaNether(world))
            maxY = 126;
        Location borderCenter;
        int borderSize;

        if (world.getWorldBorder() == null || world.getWorldBorder().getCenter() == null) //Apparently this can be null......
        {
            borderCenter = new Location(world, 0, 0, 0);
            borderSize = 60000000;
        }
        else
        {
            borderCenter = world.getWorldBorder().getCenter();
            borderSize = (int)(world.getWorldBorder().getSize() / 2) - 1000; //i.e. world must have a border radius above 1000
        }

        int randomX = r4nd0m(borderCenter.getBlockX() - borderSize, borderCenter.getBlockX() + borderSize);
        int randomZ = r4nd0m(borderCenter.getBlockZ() - borderSize, borderCenter.getBlockZ() + borderSize);

        Location location = new Location(world, randomX, r4nd0m(10, 240), randomZ);

        //Make sure it's not right next to the initial location (rare) and if it's ok to destroy blocks here
        if ((location.getWorld() == initialLocation.getWorld() && location.distanceSquared(initialLocation) < 500)
                || !isOkayToDestroy(location))
            return randomLocation(initialLocation, --attempts); //otherwise try again

        return location;
    }

    private Location randomLocation(Chunk chunk, int attempts)
    {
        if (attempts < 0)
            return null;

        int minY = 10;
        int maxY = 200;
        if (isVanillaNether(chunk.getWorld()))
            maxY = 126;

        Location location = chunk.getBlock(r4nd0m(0, 15), r4nd0m(minY, maxY), r4nd0m(0,15)).getLocation();

        if (!isOkayToDestroy(location))
            return randomLocation(chunk, --attempts);

        return location;
    }

    public int r4nd0m(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    //Ignores y value
    private boolean playerNearby(Location location, int distance)
    {
        for (Player player : location.getWorld().getPlayers())
        {
            if ((Math.abs(player.getLocation().getBlockX() - location.getBlockX()) <= (distance + 8)) && (Math.abs(player.getLocation().getBlockZ() - location.getBlockZ()) <= (distance + 8))) //ergh
            {
                return true;
            }
        }

        return false;
    }

    private boolean isVanillaNether(World world)
    {
        if (world.getEnvironment() != World.Environment.NETHER)
            return false;

        if (world.getLoadedChunks().length == 0)
            world.loadChunk(0, 0);
        return world.getLoadedChunks()[0].getBlock(0, 127, 0).getType() == Material.BEDROCK;
    }

    //Entirely reliant on the wormhole dimensions
    private boolean isOkayToDestroy(Location location)
    {
        location = location.clone();
        for (int i = 0; i >= -3; i--)
        {
            switch(location.add(0, i, 0).getBlock().getType())
            {
                case AIR:
                case NETHERRACK:
                case STONE:
                case DIRT:
                case ENDER_STONE:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }


}
