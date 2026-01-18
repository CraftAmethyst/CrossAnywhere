package io.github.qianmo0721.crossAnywhere.i18n;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class MessageService {
    private final Plugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, String> zh = new HashMap<>();
    private final Map<String, String> en = new HashMap<>();
    private final String defaultLocale;

    public MessageService(Plugin plugin, String defaultLocale) {
        this.plugin = plugin;
        this.defaultLocale = defaultLocale == null ? "en_US" : defaultLocale;
    }

    public void load() {
        zh.clear();
        en.clear();
        loadInto("messages_zh_CN.yml", zh);
        loadInto("messages_en_US.yml", en);
    }

    private void loadInto(String fileName, Map<String, String> target) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                target.put(key, config.getString(key, ""));
            }
        }
    }

    public void send(CommandSender sender, String key, TagResolver... resolvers) {
        sender.sendMessage(component(sender, key, resolvers));
    }

    public Component component(CommandSender sender, String key, TagResolver... resolvers) {
        String message = resolveMessage(sender, key);
        if (message == null || message.isEmpty()) {
            return Component.text("[" + key + "]");
        }
        if ("prefix".equalsIgnoreCase(key)) {
            TagResolver resolver = TagResolver.resolver(resolvers);
            return miniMessage.deserialize(message, resolver);
        }
        boolean hasPrefixTag = message.contains("<prefix>");
        if (message.startsWith("<noprefix>")) {
            message = message.substring("<noprefix>".length()).trim();
            hasPrefixTag = true;
        }
        String prefixRaw = resolveMessage(sender, "prefix");
        Component prefixComponent = (prefixRaw == null || prefixRaw.startsWith("["))
                ? Component.empty()
                : miniMessage.deserialize(prefixRaw);
        TagResolver[] combined = new TagResolver[resolvers.length + 1];
        combined[0] = Placeholder.component("prefix", prefixComponent);
        System.arraycopy(resolvers, 0, combined, 1, resolvers.length);
        TagResolver resolver = TagResolver.resolver(combined);
        Component parsed = miniMessage.deserialize(message, resolver);
        if (hasPrefixTag || prefixComponent.equals(Component.empty())) {
            return parsed;
        }
        return prefixComponent.append(parsed);
    }

    public TagResolver placeholder(String key, String value) {
        return Placeholder.unparsed(key, value == null ? "" : value);
    }

    public TagResolver placeholder(String key, Component component) {
        return Placeholder.component(key, component == null ? Component.empty() : component);
    }

    public String resolveMessage(CommandSender sender, String key) {
        String locale = defaultLocale;
        if (sender instanceof Player player) {
            Locale playerLocale = player.locale();
            if (playerLocale != null && "zh".equalsIgnoreCase(playerLocale.getLanguage())) {
                locale = "zh_CN";
            } else if (playerLocale != null && "en".equalsIgnoreCase(playerLocale.getLanguage())) {
                locale = "en_US";
            }
        }
        Map<String, String> bundle = "zh_CN".equalsIgnoreCase(locale) ? zh : en;
        String message = bundle.get(key);
        if (message != null) {
            return message;
        }
        Map<String, String> fallback = "zh_CN".equalsIgnoreCase(locale) ? en : zh;
        return fallback.getOrDefault(key, "[" + key + "]");
    }
}
