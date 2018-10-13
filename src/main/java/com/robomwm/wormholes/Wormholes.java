package com.robomwm.wormholes;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
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
        getConfig().addDefault("blacklistedWorlds", Collections.singletonList("spawn"));
        saveConfig();
        thera = new Thera(this);
        new WormholeSpawner(this, thera, new HashSet<>(getConfig().getStringList("blacklistedWorlds")));
        new WormholeTransporter(this, thera);
        try
        {
            //new Metrics(this);
        }
        catch (Throwable ignored){}
    }

    public void onDisable()
    {
        getLogger().info("Destroying " + thera.destroyAllWormholes() + " wormholes.");
    }
}
