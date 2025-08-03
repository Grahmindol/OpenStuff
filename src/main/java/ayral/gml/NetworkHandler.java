package ayral.gml;

import ayral.gml.network.OpenStuffPacketHandler;
import ayral.gml.network.OpenTabletGuiPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("openstuff", "main"), // 💡 change "openstuff" si besoin
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(id++, OpenTabletGuiPacket.class,
                (msg, buf) -> {}, // encode
                buf -> new OpenTabletGuiPacket(), // decode
                (msg, ctx) -> ctx.get().enqueueWork(() -> {
                    if (ctx.get().getSender() != null) {
                        OpenStuffPacketHandler.handleOpenTabletGui(ctx.get().getSender());
                    }
                })
        );
    }
}
