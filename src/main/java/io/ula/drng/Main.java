package io.ula.drng;

import io.ula.drng.commands.*;


import io.ula.drng.config.ConfigManager;
import io.ula.drng.config.Configs;
import io.ula.drng.utils.PlayerUtils;
import io.ula.drng.utils.TBUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.CompletableFuture;


public final class Main extends JavaPlugin {
    private ConfigManager configManager = DrngBootstrap.configManager;
    @Override
    public void onEnable() {
        ControlCmd.plugin = this;
        ConfigCmd.ownerPlugin = this;
        BindCmd.ownerPlugin = this;
        HomeCmd.ownerPlugin = this;
        NoticeCmd.ownerPlugin = this;
        PlayerUtils.ownerPlugin = this;
        TBUtils.ownerPlugin = this;
        //为命令类的plugin对象传值
        ScoreBoardHelper.init(this);
        Configs.init(this);
        eventRegister();
        startTasks();
        //主插件
    }

    @Override
    public void onDisable(){
        CompletableFuture<Void> saveTask = CompletableFuture.runAsync(() -> this.configManager.onDisabled());
        saveTask.join();
    }
    private void eventRegister(){
        this.getServer().getPluginManager().registerEvents(new PlayerBehaviourListener(this),this);
        this.getServer().getPluginManager().registerEvents(new ServerJoinListener(this),this);
        //主插件
    }


    public ConfigManager getConfigManager(){
        return this.configManager;
    }

    private void startTasks(){
        BukkitScheduler scheduler = this.getServer().getScheduler();
        scheduler.runTaskTimer(this, TBUtils::tipsBehaviour,1800*20,1800*20);//定时发送提示
        scheduler.runTaskTimer(this, TBUtils::aliceBehaviour,20,20);
        scheduler.runTaskTimer(this, () -> PlayerUtils.playerUpdateOnlineTime(this) ,60*20,60*20);
    }
}