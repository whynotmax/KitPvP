package dev.mzcy.server.entity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoRemovePacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnstableApiUsage")
public class PlaybackEntity extends Entity {
    private final String username;
    private final EntityType entityType;

    private final String skinTexture;
    private final String skinSignature;

    //Holo stuff
    private Entity textDisplayEntity;

    public PlaybackEntity(@NotNull String username, @Nullable String skinTexture, @Nullable String skinSignature) {
        super(EntityType.PLAYER);
        this.entityType = EntityType.PLAYER;

        this.username = username;

        this.skinTexture = skinTexture;
        this.skinSignature = skinSignature;

        setNoGravity(true);
    }

    public PlaybackEntity(@NotNull EntityType entityType) {
        super(entityType);
        this.entityType = entityType;

        this.username = null;

        this.skinTexture = null;
        this.skinSignature = null;

        setNoGravity(true);
    }

    public void setHologram(String text, float scale, int color) {
        if (textDisplayEntity != null) {
            textDisplayEntity.remove();
        }

        textDisplayEntity = new Entity(EntityType.TEXT_DISPLAY);
        textDisplayEntity.setInstance(getInstance(), getPosition().add(0, 2, 0));

        textDisplayEntity.editEntityMeta(TextDisplayMeta.class, (textDisplayMeta -> {
            textDisplayMeta = (TextDisplayMeta) textDisplayEntity.getEntityMeta();
            textDisplayMeta.setCustomNameVisible(true);

            Component textLines = Arrays.stream(text.split("\n")).map(
                            line -> MiniMessage.miniMessage().deserialize(line))
                    .reduce(Component.empty(), Component::append);
            textDisplayMeta.setText(textLines);

            textDisplayMeta.setScale(new Vec(scale, scale, scale));
            textDisplayMeta.setBackgroundColor(color);
        }));

        getViewers().forEach(textDisplayEntity::addViewer);
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        var properties = new ArrayList<PlayerInfoUpdatePacket.Property>();
        if (skinTexture != null && skinSignature != null) {
            properties.add(new PlayerInfoUpdatePacket.Property("textures", skinTexture, skinSignature));
        }
        var entry = new PlayerInfoUpdatePacket.Entry(getUuid(), username, properties, false,
                0, GameMode.SURVIVAL, null, null);
        player.sendPacket(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER, entry));

        super.updateNewViewer(player);

        player.sendPackets(new EntityMetaDataPacket(getEntityId(), Map.of(17, Metadata.Byte((byte) 127))));

        textDisplayEntity.addViewer(player);
    }



    @Override
    public void updateOldViewer(@NotNull Player player) {
        super.updateOldViewer(player);

        player.sendPacket(new PlayerInfoRemovePacket(getUuid()));

        textDisplayEntity.removeViewer(player);
    }
}