package gml.openstuff;
import gml.openstuff.integration.opencomputers.ArmorDriver;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;


public class Networking {

    public record MachineStatePayload(ItemStack stack, State state) implements CustomPacketPayload {

        public enum State {
            REQUEST_STATE,
            REQUEST_INTERACTION,
            RESPONSE_STOPPED,
            RESPONSE_RUNNING;

            public static final StreamCodec<ByteBuf, State> STREAM_CODEC =
                    ByteBufCodecs.idMapper(id -> values()[id], State::ordinal);

        }

        public static final CustomPacketPayload.Type<MachineStatePayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("openstuff", "machine_state"));

        public static final StreamCodec<RegistryFriendlyByteBuf, MachineStatePayload> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC, MachineStatePayload::stack,
                State.STREAM_CODEC, MachineStatePayload::state,
                MachineStatePayload::new
        );

        @Override
        public CustomPacketPayload.@NotNull Type<MachineStatePayload> type() {
            return TYPE;
        }
    }

    // ----------------------------------------------------------------------- //

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0.0");

        // Registers packet for both Client -> Server and Server -> Client channels
        registrar.playBidirectional(
                MachineStatePayload.TYPE,
                MachineStatePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> onReceiveMachineStatePayload(payload, context))
        );
    }

    // ----------------------------------------------------------------------- //

    public static void onReceiveMachineStatePayload(MachineStatePayload payload, IPayloadContext context) {
        if (context.flow().isServerbound()) {
            if (context.player() instanceof ServerPlayer player) {
                ItemStack stack = payload.stack();
                ItemMachineWrapper wrapper = ItemMachineManager.get(stack, player);

                switch (payload.state()) {
                    case REQUEST_INTERACTION -> {
                        // Trigger interaction on server
                        wrapper.interact(player.level(), player);
                        // Send interaction request back to the client player
                        PacketDistributor.sendToPlayer(player, new MachineStatePayload(stack, MachineStatePayload.State.REQUEST_INTERACTION));
                    }
                    case REQUEST_STATE -> sendServerState(player, stack, wrapper.machine().isRunning());
                    default -> {}
                }
            }
        } else {
            // ----------------------------------------------------------------------- //

            Player player = context.player();
            ItemStack stack = payload.stack();
            ItemMachineWrapper wrapper = ItemMachineManager.get(stack, player);

            switch (payload.state()) {
                case REQUEST_INTERACTION -> {
                    wrapper.update(player.level(), player);
                    // Execute client-side interaction
                    wrapper.interact(player.level(), player);
                }
                case RESPONSE_RUNNING, RESPONSE_STOPPED -> {
                    wrapper.data.isRunning = (payload.state() == MachineStatePayload.State.RESPONSE_RUNNING);

                    if(!wrapper.isInitialized){
                        OpenStuff.LOGGER.info("Client wrapper init !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

                        wrapper.connectComponents();

                        for (var slot : wrapper.componentSlots()) {
                            if (slot != null && slot.isDefined() && slot.get() instanceof ArmorDriver.Armor piece) {
                                piece.connectComponents();
                            }
                        }
                        wrapper.isInitialized = true;
                    }
                }
                default -> {}
            }
        }
    }

    //----------------------------------------------------------------------//

    public static void askServerState(ItemStack stack){
        PacketDistributor.sendToServer(new MachineStatePayload(stack, MachineStatePayload.State.REQUEST_STATE));
    }

    public static void askServerInteraction(ItemStack stack){
        PacketDistributor.sendToServer(new MachineStatePayload(stack, MachineStatePayload.State.REQUEST_INTERACTION));
    }

    public static void sendServerState(ServerPlayer player, ItemStack stack, boolean isRunning){
        MachineStatePayload.State responseState = isRunning
                ? MachineStatePayload.State.RESPONSE_RUNNING
                : MachineStatePayload.State.RESPONSE_STOPPED;
        PacketDistributor.sendToPlayer(player, new MachineStatePayload(stack, responseState));
    }
}