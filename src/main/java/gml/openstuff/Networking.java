package gml.openstuff;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
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

    public record MachineStatePayload(ItemStack stack, State state, boolean targetState) implements CustomPacketPayload {

        public enum State {
            REQUEST_INTERACTION,
            SET_STATE;

            public static final StreamCodec<ByteBuf, State> STREAM_CODEC =
                    ByteBufCodecs.idMapper(id -> values()[id], State::ordinal);
        }

        public static final CustomPacketPayload.Type<MachineStatePayload> TYPE =
                new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("openstuff", "machine_state"));

        public static final StreamCodec<RegistryFriendlyByteBuf, MachineStatePayload> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC, MachineStatePayload::stack,
                State.STREAM_CODEC, MachineStatePayload::state,
                ByteBufCodecs.BOOL, MachineStatePayload::targetState, // <--- Encodes/decodes boolean state
                MachineStatePayload::new
        );

        // Convenience constructor for existing states that don't need a targetState boolean
        public MachineStatePayload(ItemStack stack, State state) {
            this(stack, state, false);
        }

        @Override
        public CustomPacketPayload.@NotNull Type<MachineStatePayload> type() {
            return TYPE;
        }
    }

    // ----------------------------------------------------------------------- //

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0.0");

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
                        wrapper.interact(player.level(), player);
                        PacketDistributor.sendToPlayer(player, new MachineStatePayload(stack, MachineStatePayload.State.REQUEST_INTERACTION));
                    }
                    case SET_STATE -> {
                        if(payload.targetState()){
                            wrapper.machine().start();
                        }else {
                            wrapper.machine().stop();
                        }

                        String msg = wrapper.machine().lastError();
                        if(msg != null) {
                            // TODO: fix translation.
                            //player.sendSystemMessage(Component.translatable("gui.Analyzer.LastError", Component.translatable(msg)));
                            player.sendSystemMessage(Component.translatable(msg));
                        }
                        wrapper.data.isRunning = wrapper.machine().isRunning();
                        wrapper.setChanged();
                    }
                    default -> {}
                }
            }
        } else {
            // ----------------------------------------------------------------------- //
            Player player = context.player();
            ItemStack stack = payload.stack();

            switch (payload.state()) {
                case REQUEST_INTERACTION -> {
                    ItemMachineWrapper wrapper = new ItemMachineWrapper(stack, player);
                    wrapper.interact(player.level(), player);
                }
                default -> {}
            }
        }
    }

    //----------------------------------------------------------------------//

    /**
     * Sends an interaction request from the Client to the Server to trigger the machine's primary action.
     *
     * @param stack the {@link ItemStack} representing the machine to interact with
     */
    public static void askServerInteraction(ItemStack stack) {
        PacketDistributor.sendToServer(new MachineStatePayload(stack, MachineStatePayload.State.REQUEST_INTERACTION));
    }

    /**
     * Sends a command from the Client to the Server forcing the target machine to update its running state.
     *
     * @param stack     the {@link ItemStack} representing the machine to update
     * @param isRunning {@code true} to enable/run the machine, {@code false} to stop it
     */
    public static void setServerState(ItemStack stack, boolean isRunning) {
        PacketDistributor.sendToServer(new MachineStatePayload(stack, MachineStatePayload.State.SET_STATE, isRunning));
    }
}