package io.github.qianmo0721.crossAnywhere.listener;

import io.github.qianmo0721.crossAnywhere.config.PluginConfig;
import io.github.qianmo0721.crossAnywhere.i18n.MessageService;
import io.github.qianmo0721.crossAnywhere.manager.BackManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public final class PlayerDeathListener implements Listener {
    private PluginConfig config;
    private final BackManager backManager;
    private MessageService messages;

    public PlayerDeathListener(PluginConfig config, BackManager backManager, MessageService messages) {
        this.config = config;
        this.backManager = backManager;
        this.messages = messages;
    }

    public void update(PluginConfig config, MessageService messages) {
        this.config = config;
        this.messages = messages;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!config.backOnDeath) {
            return;
        }
        backManager.setBack(event.getEntity().getUniqueId(), event.getEntity().getLocation());
        Component backButton = messages.component(event.getEntity(), "back.button");
        messages.send(event.getEntity(), "back.saved", TagResolver.resolver(messages.placeholder("button", backButton)));
    }
}
