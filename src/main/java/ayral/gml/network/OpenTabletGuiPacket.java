package ayral.gml.network;

import ayral.gml.NetworkHandler;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;

public class OpenTabletGuiPacket {
    public static final ResourceLocation ID = new ResourceLocation("openstuff", "open_tablet_gui");

    public static void register() {
        NetworkHandler.INSTANCE.registerMessage(0, OpenTabletGuiPacket.class,
                (pkt, buf) -> {}, // encode (vide car aucun data)
                buf -> new OpenTabletGuiPacket(), // decode
                (pkt, ctx) -> ctx.get().enqueueWork(() -> {
                    ServerPlayerEntity sender = ctx.get().getSender();
                    if (sender != null) {
                        OpenStuffPacketHandler.handleOpenTabletGui(sender);
                    }
                })
        );
    }
}

