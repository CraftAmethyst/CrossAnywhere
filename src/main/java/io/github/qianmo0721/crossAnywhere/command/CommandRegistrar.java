package io.github.qianmo0721.crossAnywhere.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.logging.Logger;

public final class CommandRegistrar {
    private final Plugin plugin;
    private final Logger logger;
    private final CommandMap commandMap;

    public CommandRegistrar(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.commandMap = resolveCommandMap();
    }

    public void registerShortcut(String name, CommandExecutor executor, TabCompleter completer, String... prepend) {
        if (commandMap == null) {
            logger.warning("CommandMap unavailable; skipping shortcut registration for /" + name);
            return;
        }
        if (isTaken(name)) {
            logger.warning("Command /" + name + " is already registered; shortcut disabled.");
            return;
        }
        ShortcutCommand command = new ShortcutCommand(name, executor, completer, prepend);
        commandMap.register(plugin.getName().toLowerCase(Locale.ROOT), command);
    }

    private boolean isTaken(String name) {
        Command existing = commandMap.getCommand(name);
        if (existing == null) {
            return false;
        }
        if (existing instanceof PluginIdentifiableCommand identifiable
                && identifiable.getPlugin().equals(plugin)) {
            return false;
        }
        return true;
    }

    private CommandMap resolveCommandMap() {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            return (CommandMap) field.get(Bukkit.getServer());
        } catch (ReflectiveOperationException ex) {
            logger.warning("Failed to access commandMap: " + ex.getMessage());
            return null;
        }
    }
}
