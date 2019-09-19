/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xyz.joestr.tachyon.bukkit_plugin;

import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Command;
import org.bukkit.plugin.java.annotation.dependency.Dependency;
//import org.bukkit.plugin.java.annotation.dependency.SoftDependency;
import org.bukkit.plugin.java.annotation.permission.ChildPermission;
import org.bukkit.plugin.java.annotation.permission.Permission;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
//import org.bukkit.plugin.java.annotation.dependency.LoadBefore;
import org.bukkit.plugin.java.annotation.plugin.Description;
import org.bukkit.plugin.java.annotation.plugin.LoadOrder;
import org.bukkit.plugin.java.annotation.plugin.LogPrefix;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.plugin.java.annotation.plugin.Website;
import org.bukkit.plugin.java.annotation.plugin.author.Author;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.server.ResourceConfig;
import xyz.joestr.tachyon.api.TachyonAPI;
import xyz.joestr.tachyon.api.request.RequestManager;
import xyz.joestr.tachyon.api.utils.Updater;
import xyz.joestr.tachyon.bukkit_plugin.api.TachyonAPIBukkit;
import xyz.joestr.tachyon.bukkit_plugin.commands.TBukkitCommand;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

/**
 *
 * @author Joel
 */
@Plugin(name="Tachyon-Bukkit-Plugin", version="0.1.6")
@Description(value="The Tachyon unit for servers which implement the Bukkit API.")
@Website(value="https://git.joestr.xyz/joestr/Tachyon/Bukkit-Plugin")
@Author(value="joestr")
@LoadOrder(value=PluginLoadOrder.POSTWORLD)
@LogPrefix(value="ddd")
@Dependency(value="LuckPerms")
@ApiVersion(value=ApiVersion.Target.v1_13)
//@LoadBefore("")
//@SoftDependency("")
//@Command(name = "foo", desc = "Foo command", aliases = {"foobar", "fubar"}, permission = "test.foo", permissionMessage = "You do not have permission!", usage = "/<command> [test|stop]")
//@Permission(name = "test.foo", desc = "Allows foo command", defaultValue = PermissionDefault.OP)
//@Permission(name = "test.*", desc = "Wildcard foo permission", defaultValue = PermissionDefault.OP)
//@Permission(name = "test.*", desc = "Wildcard permission", children = {@ChildPermission(name ="test.foo")})
public class TachyonBukkitPlugin extends JavaPlugin {
    
    private Updater updater;
    private HttpServer httpServer = null;
    
    @Override
    public void onEnable() {
        
        this.updater =
            new Updater(
                "https://mvn-repo.joestr.xyz/repository/repository/",
                Bukkit.getServer().getUpdateFolderFile(),
                86400,
                new File(Bukkit.getServer().getUpdateFolderFile(), "${project.name}.${project.packaging}")
            );
        
        TachyonAPI.setInstance(new TachyonAPIBukkit());
        
        this.saveDefaultConfig();
        
        try {
            TachyonAPI.getInstance().setRequestManager(
                new RequestManager(
                    this.getConfig().getString("mqtt.broker.address"),
                    this.getConfig().getInt("mqtt.broker.port"),
                    this.getConfig().getString("mqtt.topic"),
                    this.getConfig().getInt("mqtt.qos"),
                    this.getConfig().getString("mqtt.client-id"),
                    new MemoryPersistence()
                )
            );
        } catch (MqttException ex) {
            Logger
                .getLogger(TachyonBukkitPlugin.class.getName())
                .log(Level.SEVERE, null, ex);
        }
        
        TBukkitCommand tBukkitCommand = new TBukkitCommand(this, this.updater); 
        PluginCommand tBukkitPluginCommand = this.getServer().getPluginCommand("tbukkit");
        tBukkitPluginCommand.setExecutor(tBukkitCommand);
        tBukkitPluginCommand.setTabCompleter(tBukkitCommand);
        
        final ResourceConfig rc = new ResourceConfig().packages("com.example.rest");
        this.httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:8080/rest/"), rc);
    }

    @Override
    public void onDisable() {

        this.httpServer.shutdownNow();
    }
}
