package dev.mzcy.server;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.java.Log;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.anvil.AnvilLoader;

import java.net.InetSocketAddress;

@Getter
@Setter
@Log
public class KitPvPServer {

    private final EventNode<Event> EVENT_NODE = EventNode.all("kitpvp");
    private final InstanceContainer MAIN_INSTANCE;

    public KitPvPServer() {
        long startTimestamp = System.currentTimeMillis();
        log.info("Starting KitPvP server...");

        MinecraftServer minecraftServer = MinecraftServer.init();


        //TODO: Add Velocity support
        //VelocityProxy.enable("");
        MojangAuth.init();

        log.info("Starting server at %s:%s".formatted("localhost", 25565));
        minecraftServer.start(new InetSocketAddress("127.0.0.1", 25565));

        long endTimestamp = System.currentTimeMillis();
        long duration = endTimestamp - startTimestamp;

        log.info("KitPvP server started in " + duration + "ms.");

        MAIN_INSTANCE = MinecraftServer.getInstanceManager().createInstanceContainer(new AnvilLoader("worlds/kitpvp/"));

        //TODO: Load premade arenas from file
        //TODO: Load premade kits from file


        MinecraftServer.getGlobalEventHandler().addChild(EVENT_NODE);

        EVENT_NODE.addListener(AsyncPlayerConfigurationEvent.class, (event) -> {
            Player player = event.getPlayer();
            event.setSpawningInstance(MAIN_INSTANCE);
            player.setRespawnPoint(new Pos(-23.5, 53, -7.5, -90.0F, 0.0F));
        });

        MinecraftServer.getSchedulerManager().buildShutdownTask(this::stop);

    }

    public void stop() {
        //TODO
    }

}
