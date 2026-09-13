package gml.openstuff;

import io.netty.buffer.ByteBuf;
import net.minecraft.data.registries.VanillaRegistries;
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
            REQUEST_STATE,
            REQUEST_INTERACTION,
            SET_STATE, // <--- New state for setting state directly
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
                    case REQUEST_STATE -> sendServerState(player, stack, wrapper.machine().isRunning());
                    case SET_STATE -> {
                        // 1. Update server state
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
                        wrapper.data.saveData(stack, VanillaRegistries.createLookup());
                        sendServerState((ServerPlayer) player, stack, wrapper.data.isRunning);
                    }
                    default -> {}
                }
            }
        } else {
            // ----------------------------------------------------------------------- //

            Player player = context.player();
            ItemStack stack = payload.stack();
            ItemMachineWrapper wrapper = ItemMachineManager.get(stack, player);

            switch (payload.state()) {
                case REQUEST_INTERACTION -> wrapper.interact(player.level(), player);
                case RESPONSE_RUNNING, RESPONSE_STOPPED -> {
                    wrapper.data.isRunning = (payload.state() == MachineStatePayload.State.RESPONSE_RUNNING);
                    wrapper.saveData(stack);
                }
                default -> {}
            }
        }
    }

    //----------------------------------------------------------------------//

    /**
     * Sends a request from the Client to the Server to retrieve the current running state of a machine.
     *
     * @param stack the {@link ItemStack} representing the machine to query
     */
    public static void askServerState(ItemStack stack) {
        PacketDistributor.sendToServer(new MachineStatePayload(stack, MachineStatePayload.State.REQUEST_STATE));
    }

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

    /**
     * Sends the current machine state from the Server back to a specific client player.
     *
     * @param player    the target {@link ServerPlayer} receiving the state update
     * @param stack     the {@link ItemStack} representing the machine
     * @param isRunning the current operational state of the machine on the server
     */
    public static void sendServerState(ServerPlayer player, ItemStack stack, boolean isRunning) {
        MachineStatePayload.State responseState = isRunning
                ? MachineStatePayload.State.RESPONSE_RUNNING
                : MachineStatePayload.State.RESPONSE_STOPPED;
        PacketDistributor.sendToPlayer(player, new MachineStatePayload(stack, responseState));
    }
}