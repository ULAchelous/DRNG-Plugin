package io.ula.drng.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import net.kyori.adventure.key.Key;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConfigManager {
    class AutoSave implements Runnable{
        private volatile Boolean pluginStopped = false;
        Logger LOGGER = LogManager.getLogger("dr-ng/ConfigManager:autoSave");
        public void stop(){this.pluginStopped = true;}
        @Override
        public void run() {
            while(true){
                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(period));
                } catch (InterruptedException e) {
                    this.LOGGER.error(e.getMessage());
                }
                if(pluginStopped == true){
                    saveAll();
                    return;
                }
                saveAll();
            }
        }
    }


    private Map<Key,ConfigFile> configs = new HashMap<>();
    private ArrayList<ConfigFile> autoRemove = new ArrayList<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private Thread thread;
    private AutoSave autoSave = new AutoSave();
    private long period = 60;
    private Logger LOGGER = LogManager.getLogger("dr-ng/ConfigManager");;
    public ConfigManager(){
        thread = new Thread(autoSave,"save");
        thread.start();;
    }
    public void register(Key key,ConfigFile configFile){
        init();
        if(!(configFile instanceof InlineConfigFile)){
            configFile.createDir();
            configFile.createFile();
        }
        configs.put(key, configFile);
    }
    public void setAutoRemove(Key key){
        autoRemove.add(configs.get(key));
    }
    public void setAutoSave(long period){
        this.period = period;
    }
    public ConfigFile getConfig(Key key){
        return configs.get(key);
    }
    public void saveAll(){
        lock.readLock().lock();
        for (Map.Entry<Key, ConfigFile> entry : configs.entrySet()) {
            entry.getValue().write();
            LOGGER.info(String.format("Saved Change to \"%s\"",entry.getValue().getName()));
        }
        lock.readLock().unlock();
    }
    public void reloadAll(){
        lock.writeLock().lock();
        for (Map.Entry<Key, ConfigFile> entry : configs.entrySet()) {
            entry.getValue().reload();
            LOGGER.info(String.format("Saved Change to \"%s\"",entry.getValue().getName()));
        }
        lock.writeLock().unlock();
    }

    public void init(){
        File serverRoot = Bukkit.getServer().getWorldContainer();
        if(!Files.exists(Path.of(new File(serverRoot.getPath()+"/config/dr-ng").toURI()))){
            try{
                Files.createDirectory(Path.of(new File(serverRoot.getPath()+"/config/dr-ng").toURI()));
            }catch(IOException e){
                LOGGER.error("Failed to create config directory :" + e.getMessage());
                return;
            }
        }
    }

    public void onDisabled(){
        autoSave.stop();
        for(ConfigFile configFile : autoRemove){
            configFile.removeFile();
        }
    }
}
