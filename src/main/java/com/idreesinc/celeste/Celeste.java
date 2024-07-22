package com.idreesinc.celeste;

import com.idreesinc.celeste.utilities.Metrics;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.idreesinc.celeste.commands.CommandCeleste;
import com.idreesinc.celeste.commands.CommandFallingStar;
import com.idreesinc.celeste.commands.CommandShootingStar;
import com.idreesinc.celeste.utilities.UpdateChecker;
import com.idreesinc.celeste.config.CelesteConfigManager;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Celeste extends JavaPlugin {

    public CelesteConfigManager configManager = new CelesteConfigManager(this);

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        // bStats metrics
        Metrics metrics = new Metrics(this, 8292);

        this.getCommand("celeste").setExecutor(new CommandCeleste(this));
        this.getCommand("shootingstar").setExecutor(new CommandShootingStar(this));
        this.getCommand("fallingstar").setExecutor(new CommandFallingStar(this));
        configManager.processConfigs();

        Consumer<ScheduledTask> stargazingTask = task -> new Astronomer(this).run();
        Bukkit.getAsyncScheduler().runAtFixedRate(this, stargazingTask, 0, 10 * 50, TimeUnit.MILLISECONDS);

        checkForUpdates();
    }

    public void reload() {
        reloadConfig();
        configManager.processConfigs();
        checkForUpdates();
    }

    public void checkForUpdates() {
        if (this.getConfig().getBoolean("check-for-updates")) {
            new UpdateChecker(this, 81862).getVersion(version -> {
                try {
                    double current = Double.parseDouble(this.getDescription().getVersion());
                    double api = Double.parseDouble(version);
                    if (current < api) {
                        this.getLogger().info("There is an update available for Celeste (" + current + " -> " + api + ")");
                    }
                } catch (NumberFormatException e) {
                    if (this.getConfig().getBoolean("debug")) {
                        this.getLogger().severe("Unable to process remote plugin version number '" + version + "'");
                    }
                }
            });
        }
    }
}
