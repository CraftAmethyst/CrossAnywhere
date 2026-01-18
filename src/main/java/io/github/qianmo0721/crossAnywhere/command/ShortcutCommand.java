package io.github.qianmo0721.crossAnywhere.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ShortcutCommand extends Command {
    private final CommandExecutor executor;
    private final TabCompleter completer;
    private final String[] prepend;

    public ShortcutCommand(String name, CommandExecutor executor, TabCompleter completer, String... prepend) {
        super(name);
        this.executor = executor;
        this.completer = completer;
        this.prepend = prepend;
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        String[] merged = mergeArgs(args);
        return executor.onCommand(sender, this, label, merged);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        if (completer == null) {
            return List.of();
        }
        String[] merged = mergeArgs(args);
        List<String> results = completer.onTabComplete(sender, this, alias, merged);
        if (results == null) {
            return List.of();
        }
        if (prepend.length == 0) {
            return results;
        }
        List<String> filtered = new ArrayList<>();
        for (String result : results) {
            if (result == null) {
                continue;
            }
            filtered.add(result);
        }
        return filtered;
    }

    private String[] mergeArgs(String[] args) {
        String[] merged = Arrays.copyOf(prepend, prepend.length + args.length);
        System.arraycopy(args, 0, merged, prepend.length, args.length);
        return merged;
    }
}
