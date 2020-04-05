/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.joestr.tachyon.bungeecord_plugin;

import io.undertow.Handlers;
import io.undertow.Undertow;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import xyz.joestr.tachyon.api.TachyonAPI;
import xyz.joestr.tachyon.api.classes.ChatFilter;
import static xyz.joestr.tachyon.bungeecord_plugin.TachyonBungeeCordPlugin.configuration;
import xyz.joestr.tachyon.bungeecord_plugin.chatfilters.AnnotatedPlayerChatFilter;
import xyz.joestr.tachyon.bungeecord_plugin.chatfilters.ColorCodeFilter;
import xyz.joestr.tachyon.bungeecord_plugin.chatfilters.RawChatFilter;
import xyz.joestr.tachyon.bungeecord_plugin.commands.CommandGList;
import xyz.joestr.tachyon.bungeecord_plugin.commands.CommandGStaffChat;
import xyz.joestr.tachyon.bungeecord_plugin.commands.CommandGTpa;
import xyz.joestr.tachyon.bungeecord_plugin.commands.CommandTList;
import xyz.joestr.tachyon.bungeecord_plugin.listeners.ChatFilterListener;
import xyz.joestr.tachyon.bungeecord_plugin.listeners.StaffChatMessageListener;
import xyz.joestr.tachyon.bungeecord_plugin.rest.EndPointPlayers;
import xyz.joestr.tachyon.bungeecord_plugin.rest.EndPointPlayersPosition;

/**
 * The Tachyon-BungeeCord-Plugin itself.
 *
 * @author Joel
 */
public class TachyonBungeeCordPlugin extends Plugin {

  private static TachyonBungeeCordPlugin INSTANCE;

  /**
   * A {@link Set} of {@link ChatFilter}s
   */
  public static Set<ChatFilter> chatFilters;

  /**
   * The loaded {@link net.md_5.bungee.config.Configuration}.
   */
  public static net.md_5.bungee.config.Configuration configuration;

  /**
   * The configuration file itself
   */
  public static File configurationFile;

  // Hold the teleports which have to be accepted or cancelled.
  public static Map<Entry<UUID, UUID>, LocalDate> ongoingTeleports = new HashMap<>();

  private Undertow httpServer = null;

  /**
   * Called when the plugin starts
   */
  @Override
  public void onEnable() {

    INSTANCE = this;

    // The chatfilters
    chatFilters = new HashSet<>();

    // Add the chat filters we provide
    chatFilters.add(new RawChatFilter());
    chatFilters.add(new ColorCodeFilter());
    chatFilters.add(new AnnotatedPlayerChatFilter());

    // Success status
    boolean successfullyLoaded = false;

    // Set the filepath fot the configuration file
    configurationFile = new File(this.getDataFolder(), "config.yml");

    // Check if the configuration file exists
    if (configurationFile.exists()) {

      try {
        configuration
          = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configurationFile);
        successfullyLoaded = true;
      } catch (IOException ex) {
        Logger.getLogger(TachyonBungeeCordPlugin.class.getName()).log(Level.SEVERE, null, ex);
      }
    } else {

      // If the data folder for the plugin does not exist
      if (!this.getDataFolder().exists()) {
        // create it
        this.getDataFolder().mkdir();
      }

      File file = new File(this.getDataFolder(), "config.yml");

      // If the configuration file does not exist
      if (!file.exists()) {
        // Copy the local ressource
        try (InputStream in = getResourceAsStream("config.yml")) {
          Files.copy(in, file.toPath());
        } catch (IOException ex) {
          Logger.getLogger(TachyonBungeeCordPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
      }

      // Finally, load the configuration
      try {
        configuration
          = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configurationFile);
        successfullyLoaded = true;
      } catch (IOException ex) {
        Logger.getLogger(TachyonBungeeCordPlugin.class.getName()).log(Level.SEVERE, null, ex);
      }
    }

    // If the plugin was not loaded successfully ...
    if (!successfullyLoaded) {
      // ... quit here.
      return;
    }

    // Get the BungeeCord plugin manager here
    PluginManager pluginManager = this.getProxy().getPluginManager();

    // Register the '/tpa' command here
    // pluginManager.registerCommand(
    //    this,
    //    new TPACommand()
    // );
    // Register the '/tlist'
    pluginManager.registerCommand(this, new CommandTList("tlist", "tachyon.command.tlist"));

    // Register the '/staffchat'
    pluginManager.registerCommand(this, new CommandGStaffChat());

    // Register the listener for the staff chat
    pluginManager.registerListener(this, new StaffChatMessageListener());

    // Register the chat filter listener
    pluginManager.registerListener(this, new ChatFilterListener());

    this.httpServer
      = Undertow.builder()
        .addHttpListener(
          configuration.getInt("listenport"), configuration.getString("listenaddress"))
        .setHandler(
          Handlers.path()
            .addPrefixPath(
              "/players",
              Handlers.routing()
                .get("/", new EndPointPlayers())
                .get(
                  "/{uuid}",
                  Handlers.routing().get("/position", new EndPointPlayersPosition()))
                .put("/{uuid}", Handlers.routing().get("/position", null))
                .setFallbackHandler(null)))
        .build();

    httpServer.start();
  }

  /**
   * Called when the plugin will be disabled.
   */
  @Override
  public void onDisable() {

    this.httpServer.stop();
  }

  public static TachyonBungeeCordPlugin getInstance() {
    return INSTANCE;
  }
}
