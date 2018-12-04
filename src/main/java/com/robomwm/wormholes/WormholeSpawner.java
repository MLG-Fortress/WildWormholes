package com.robomwm.wormholes;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 5/29/2017.
 *
 * @author RoboMWM
 */
public class WormholeSpawner implements Listener
{
    private JavaPlugin instance;
    private Set<String> blacklistedWorlds = new HashSet<>();
    private Thera thera;
    private int maxRadius;
    private Map<String, Integer> customMaxRadius;

    public WormholeSpawner(JavaPlugin plugin, Thera thera, Set<String> worldBlacklist, int maxRadius, Map<String, Integer> customMaxRadius)
    {
        this.instance = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.thera = thera;
        this.blacklistedWorlds = worldBlacklist;
        this.maxRadius = maxRadius;
        this.customMaxRadius = customMaxRadius;

        //Spawn wormholes at random time intervals or something
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                //TODO: think of good randomization to determine if we should spawn a portal (perhaps based on amount of players/unique players and currently-existing wormholes?)
//                if (r4nd0m(0, 5) > 4)
//                    return;

                //If nobody's on, there are not many chunks to decide where to spawn.
                if (instance.getServer().getOnlinePlayers().isEmpty())
                    return;

                //A more efficient way would be to maintain collection state on world loads/unloads
                //But it's not like we're spawning wormholes multiple times a second.
                List<World> worlds = new ArrayList<>();
                for (World world : instance.getServer().getWorlds())
                    if (!blacklistedWorlds.contains(world.getName()))
                        worlds.add(world);

                Chunk[] chunks = worlds.get(r4nd0m(0, worlds.size() - 1)).getLoadedChunks();
                if (chunks.length <= 0) return;
                Chunk chunk = chunks[r4nd0m(0, chunks.length - 1)];

                Location chunkLocation = chunk.getBlock(8, 64, 8).getLocation();

                //Ensure a player is somewhat nearby (within view distance)
                if (!playerNearby(chunkLocation, instance.getServer().getViewDistance() * 16))
                    return;

                //Don't spawn wormholes on top of players
                //if (playerNearby(chunk.getBlock(8, 64, 8).getLocation(), instance.getServer().getViewDistance() * 16 / 4))
                //    return;

                //Only max of one wormhole in a chunk
                if (thera.getWormhole(chunk) != null)
                    return;

                //Too far away from spawn
                if (!chunk.getWorld().getWorldBorder().isInside(chunkLocation))
                    return;
                int borderSizeRadius = maxRadius;
                if (customMaxRadius.containsKey(chunkLocation.getWorld().getName()))
                    borderSizeRadius = customMaxRadius.get(chunkLocation.getWorld().getName());
                if (borderSizeRadius > -1 && chunkLocation.distanceSquared(chunkLocation.getWorld().getSpawnLocation()) > borderSizeRadius * borderSizeRadius)
                    return;

                Location location = randomLocation(chunk, 2);
                Location otherSide = randomLocation(location, 2, worlds);
                if (location == null || otherSide == null)
                    return;

                thera.addWormhole(86400, 3000, location, otherSide);

                //Build newly-spawned wormholes
                thera.buildWormholes(chunk);
                thera.buildWormholes(otherSide.getChunk());

                //TODO: debug
                plugin.getLogger().info("Spawned a wormhole at " + location.toString() + "\nWith the other side at " + otherSide.toString());
            }
        }.runTaskTimer(plugin, 20L, 400L);
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event)
    {
        //Ignore if not a valid world to spawn wormholes in
        if (blacklistedWorlds.contains(event.getWorld().getName()))
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

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event)
    {
        if (blacklistedWorlds.contains(event.getWorld().getName()))
            return;
        thera.destroyAllWormholes(event.getWorld());
    }

    /**
     * Returns a random location <b>not nearby</b> the provided initialLocation
     * @param initialLocation the location to avoid being nearby
     * @param attempts
     * @param worlds
     * @return
     */
    private Location randomLocation(Location initialLocation, int attempts, List<World> worlds)
    {
        if (initialLocation == null || attempts < 0)
            return null;

        int maxY = 200;
        World world = worlds.get(r4nd0m(0, worlds.size() - 1));
        if (isVanillaNether(world))
            maxY = 126;
        Location borderCenter = world.getSpawnLocation();
        int borderSizeRadius = maxRadius;
        if (customMaxRadius.containsKey(world.getName()))
            borderSizeRadius = customMaxRadius.get(world.getName());

        if (borderSizeRadius < 0)
        {
            if (world.getWorldBorder() == null || world.getWorldBorder().getCenter() == null) //Apparently this can be null......
            {
                borderSizeRadius = 60000000 / 2;
            }
            else
            {
                borderCenter = world.getWorldBorder().getCenter();
                borderSizeRadius = (int)(world.getWorldBorder().getSize() / 2) - 20;
            }
        }


        if (borderSizeRadius <= 0)
            return randomLocation(initialLocation, --attempts, worlds);

        int randomX = r4nd0m(borderCenter.getBlockX() - borderSizeRadius, borderCenter.getBlockX() + borderSizeRadius);
        int randomZ = r4nd0m(borderCenter.getBlockZ() - borderSizeRadius, borderCenter.getBlockZ() + borderSizeRadius);

        Location location = new Location(world, randomX, r4nd0m(7, maxY), randomZ);

        //Make sure it's not right next to the initial location (rare) and if it's ok to destroy blocks here
        if ((location.getWorld() == initialLocation.getWorld() && location.distanceSquared(initialLocation) < 500)
                || !isOkayToDestroy(location))
            return randomLocation(initialLocation, --attempts, worlds); //otherwise try again

        return location;
    }

    /**
     * Returns a random location within a given chunk
     * @param chunk
     * @param attempts
     * @return
     */
    private Location randomLocation(Chunk chunk, int attempts)
    {
        if (attempts < 0)
            return null;

        int maxY = 200;
        if (isVanillaNether(chunk.getWorld()))
            maxY = 126;

        Location location = chunk.getBlock(r4nd0m(0, 15), r4nd0m(7, maxY), r4nd0m(0,15)).getLocation();

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
                case END_STONE:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }


}
