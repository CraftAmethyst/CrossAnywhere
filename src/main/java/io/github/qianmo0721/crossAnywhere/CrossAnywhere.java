package io.github.qianmo0721.crossAnywhere;

import io.github.qianmo0721.crossAnywhere.command.CaCommand;
import io.github.qianmo0721.crossAnywhere.command.CommandRegistrar;
import io.github.qianmo0721.crossAnywhere.config.PluginConfig;
import io.github.qianmo0721.crossAnywhere.i18n.MessageService;
import io.github.qianmo0721.crossAnywhere.listener.PlayerDeathListener;
import io.github.qianmo0721.crossAnywhere.manager.*;
import io.github.qianmo0721.crossAnywhere.repository.TpaAllowlistRepository;
import io.github.qianmo0721.crossAnywhere.repository.WaypointRepository;
import io.github.qianmo0721.crossAnywhere.safety.NearbySafeFinder;
import io.github.qianmo0721.crossAnywhere.safety.SafetyChecker;
import io.github.qianmo0721.crossAnywhere.teleport.TeleportService;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CrossAnywhere extends JavaPlugin {
    private static final Logger log = LoggerFactory.getLogger(CrossAnywhere.class);
    private WaypointRepository repository;
    private TpaAllowlistRepository tpaAllowlist;
    private PluginConfig pluginConfig;
    private MessageService messages;
    private CooldownManager cooldownManager;
    private CostManager costManager;
    private SafetyChecker safetyChecker;
    private NearbySafeFinder safeFinder;
    private BackManager backManager;
    private ConfirmManager confirmManager;
    private TpaManager tpaManager;
    private TeleportService teleportService;
    private CaCommand commandHandler;
    private PlayerDeathListener deathListener;

    @Override
    public void onEnable() {
        getLogger().info("Loading...");

        saveDefaultConfig();
        reloadConfigs();

        repository = new WaypointRepository(getDataFolder().toPath());
        repository.load();
        tpaAllowlist = new TpaAllowlistRepository(getDataFolder().toPath());
        tpaAllowlist.load();

        messages = new MessageService(this, pluginConfig.defaultLocale);
        messages.load();

        cooldownManager = new CooldownManager();
        safeFinder = new NearbySafeFinder();
        backManager = new BackManager();
        confirmManager = new ConfirmManager(this, messages);
        tpaManager = new TpaManager(this, messages);
        rebuildTeleportPipeline();

        commandHandler = new CaCommand(this, pluginConfig, messages, repository, tpaAllowlist,
                teleportService, tpaManager, confirmManager, backManager);

        PluginCommand command = getCommand("ca");
        if (command != null) {
            command.setExecutor(commandHandler);
            command.setTabCompleter(commandHandler);
        }

        registerShortcuts(commandHandler);
        deathListener = new PlayerDeathListener(pluginConfig, backManager, messages);
        getServer().getPluginManager().registerEvents(deathListener, this);

        if (pluginConfig.saveIntervalSeconds > 0) {
            long interval = pluginConfig.saveIntervalSeconds * 20L;
            getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
                repository.saveIfDirty();
                tpaAllowlist.saveIfDirty();
            }, interval, interval);
        }

        getLogger().info("CrossAnywhere v" + getDescription().getVersion() + " by " + getDescription().getAuthors() + " enabled.");
    }

    @Override
    public void onDisable() {
        if (repository != null) {
            repository.save();
        }
        if (tpaAllowlist != null) {
            tpaAllowlist.save();
        }
    }

    private void reloadConfigs() {
        reloadConfig();
        pluginConfig = PluginConfig.load(getConfig());
    }

    public void reloadAll() {
        reloadConfigs();
        messages = new MessageService(this, pluginConfig.defaultLocale);
        messages.load();
        confirmManager.setMessages(messages);
        tpaManager.setMessages(messages);
        rebuildTeleportPipeline();
        if (commandHandler != null) {
            commandHandler.updateContext(pluginConfig, messages, teleportService);
        }
        if (deathListener != null) {
            deathListener.update(pluginConfig, messages);
        }
    }

    private void rebuildTeleportPipeline() {
        costManager = new CostManager(pluginConfig.cost);
        safetyChecker = new SafetyChecker(pluginConfig.safety);
        teleportService = new TeleportService(pluginConfig, messages, cooldownManager,
                costManager, safetyChecker, safeFinder, confirmManager, backManager);
    }

    private void registerShortcuts(CaCommand commandHandler) {
        CommandRegistrar registrar = new CommandRegistrar(this);
        registrar.registerShortcut("setp", commandHandler, commandHandler, "setp");
        registrar.registerShortcut("tpp", commandHandler, commandHandler, "tpp");
        registrar.registerShortcut("delp", commandHandler, commandHandler, "delp");
        registrar.registerShortcut("listp", commandHandler, commandHandler, "listp");
        registrar.registerShortcut("descp", commandHandler, commandHandler, "descp");

        registrar.registerShortcut("setg", commandHandler, commandHandler, "setg");
        registrar.registerShortcut("tpg", commandHandler, commandHandler, "tpg");
        registrar.registerShortcut("delg", commandHandler, commandHandler, "delg");
        registrar.registerShortcut("listg", commandHandler, commandHandler, "listg");
        registrar.registerShortcut("descg", commandHandler, commandHandler, "descg");

        registrar.registerShortcut("tplist", commandHandler, commandHandler, "list");
        registrar.registerShortcut("back", commandHandler, commandHandler, "back");

        registrar.registerShortcut("tpa", commandHandler, commandHandler, "tpa");
        registrar.registerShortcut("tpahere", commandHandler, commandHandler, "tpahere");
        registrar.registerShortcut("tphere", commandHandler, commandHandler, "tphere");
        registrar.registerShortcut("tpaccept", commandHandler, commandHandler, "accept");
        registrar.registerShortcut("tpdeny", commandHandler, commandHandler, "deny");
        registrar.registerShortcut("tpcancel", commandHandler, commandHandler, "cancel");
        registrar.registerShortcut("tpconfirm", commandHandler, commandHandler, "confirm");
        registrar.registerShortcut("tpcancelconfirm", commandHandler, commandHandler, "cancelconfirm");

        if (pluginConfig.commands.overrideTp) {
            registrar.registerShortcut("tp", commandHandler, commandHandler, "tp");
        }
    }
}
