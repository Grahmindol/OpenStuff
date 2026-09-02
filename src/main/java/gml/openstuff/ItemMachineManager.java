package gml.openstuff;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;
import gml.openstuff.item.OpenArmorPiece;
import li.cil.oc.api.network.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@EventBusSubscriber(modid = OpenStuff.MOD_ID)
public class ItemMachineManager {

    public static final ClientCache CLIENT = new ClientCache();
    public static final ServerCache SERVER = new ServerCache();

    public static ItemMachineWrapper get(ItemStack stack, LivingEntity holder) {
        if (holder.level().isClientSide) {
            return CLIENT.get(stack, holder);
        } else {
            return SERVER.get(stack, holder);
        }
    }

    // -------------------------------------------------------------- //

    private static String getId(ItemStack stack) {
        if (stack.has(DataComponents.CUSTOM_DATA)) {
            CustomData customData = stack.get(DataComponents.CUSTOM_DATA);
            if (customData != null) {
                CompoundTag tag = customData.copyTag();
                if (tag.contains("openstuff_machine_id", Tag.TAG_STRING)) {
                    return tag.getString("openstuff_machine_id");
                }
            }
        }
        return null;
    }

    private static String getOrCreateId(ItemStack stack) {
        // all non-open stuff items are the same for us.
        if(!(stack.getItem() instanceof OpenArmorPiece)) return "none";

        AtomicReference<String> id = new AtomicReference<>();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, data -> {
            if (!data.contains("openstuff_machine_id", Tag.TAG_STRING)) {
                data.putString("openstuff_machine_id", UUID.randomUUID().toString());
            }
            id.set(data.getString("openstuff_machine_id"));
        });
        return id.get();
    }

    private static String getChecksum(LivingEntity _player){
        StringBuilder result = new StringBuilder();
        for(ItemStack stack : _player.getArmorAndBodyArmorSlots()){
            result.append(getOrCreateId(stack));
        }
        return result.toString();
    }

    // -------------------------------------------------------------- //

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save e) {
        if (e.getLevel() instanceof Level level) {
            SERVER.saveAll(level);
        }
    }

    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile e) {
        SERVER.save(e.getEntity());
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload e) {
        if (e.getLevel() instanceof Level level) {
            CLIENT.clear(level);
            SERVER.clear(level);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre e) {
        CLIENT.cleanUp();
        if (ServerLifecycleHooks.getCurrentServer() instanceof IntegratedServer) {
            if (Minecraft.getInstance().isPaused()) {
                CLIENT.keepAlive();
                SERVER.keepAlive();
            }
        }

        //-----------------------------------------------------

        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            while (OpenStuff.AROMOR_INTERACT_KEY.consumeClick()) {
                if (mc.screen == null) {
                    ItemStack stack = mc.player.getItemBySlot(EquipmentSlot.CHEST);
                    if(stack.is(OpenStuff.OPEN_CHEST.get())) Networking.askServerInteraction(stack);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre e) {
        SERVER.cleanUp();
    }

    @SubscribeEvent
    private static void onEntityTick(EntityTickEvent.Pre e){
        if (e.getEntity() instanceof LivingEntity player){
            ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
            if(stack.is(OpenStuff.OPEN_CHEST.get())){
                ItemMachineWrapper wrapper = ItemMachineManager.get(stack, player);
                wrapper.update(player.level(), player);
            }
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if(!Objects.equals(getOrCreateId(event.getTo()), getOrCreateId((event.getFrom())))) return;


        if(event.getSlot() == EquipmentSlot.CHEST){
            if(event.getFrom().is(OpenStuff.OPEN_CHEST)){
                String id = getOrCreateId(event.getFrom());
                ItemMachineWrapper wrapper = SERVER.cache.getIfPresent(id);
                if(wrapper != null){
                    wrapper.machine().stop();
                    wrapper.writeToNBT(event.getEntity().registryAccess());
                    wrapper.autoSave = false;
                    SERVER.cache.invalidate(id);
                    SERVER.cache.cleanUp();
                }
            }
        }

        String id = getOrCreateId(event.getEntity().getItemBySlot(EquipmentSlot.CHEST));
        ItemMachineWrapper wrapper = SERVER.cache.getIfPresent(id);

        if(wrapper != null){
            String newChecksum = getChecksum(event.getEntity());

            // may occur on player world join.
            if(Objects.equals(wrapper.checksum, newChecksum)) return;

            /*wrapper.onItemRemoved(event.getSlot(), event.getFrom());
            wrapper.onItemAdded(event.getSlot(), event.getTo());

            // TODO : What if multiple change at same time ?
            wrapper.checksum = newChecksum;*/
        }

        // TODO use this to add and remove items dynamically
        // we got some issue with loading because all equipment are added for the first time, but the ey already are in the wrapper...

        // so I used checksum to reset cache is needed, this methode will have to update the checksum.
    }


    // -------------------------------------------------------------- //

    public abstract static class Cache implements Callable<ItemMachineWrapper>, RemovalListener<String, ItemMachineWrapper> {

        protected abstract long timeout();

        public final com.google.common.cache.Cache<String, ItemMachineWrapper> cache;

        protected ItemStack currentStack;
        protected LivingEntity currentHolder;

        public Cache() {
            this.cache = CacheBuilder.newBuilder()
                    .expireAfterAccess(timeout(), TimeUnit.SECONDS)
                    .removalListener(this)
                    .build();
        }

        public ItemMachineWrapper getWeak(ItemStack stack) {
            return null; // Overridden in ClientCache
        }

        public ItemMachineWrapper get(ItemStack stack, LivingEntity holder) {
            String id = getOrCreateId(stack);
            synchronized (cache) {
                currentStack = stack;
                currentHolder = holder;

                if (holder.level().isClientSide) {
                    ItemMachineWrapper weak = getWeak(stack);
                    if (weak != null && weak.isInitialized) {
                        // TODO: add LivingEntity inventory tracker.
                        if (holder instanceof Player player) {
                            int timesChanged = player.getInventory().getTimesChanged();
                            if (timesChanged != weak.timesChanged) {
                                if (!weak.isDirty) {
                                    weak.isDirty = true;
                                    gml.openstuff.Networking.askServerState(stack);
                                }
                                weak.timesChanged = timesChanged;
                            }
                        }
                    }
                }

                ItemMachineWrapper wrapper;
                try {
                    wrapper = cache.get(id, this);
                } catch (Exception ex) {
                    throw new RuntimeException("Failed to load machine wrapper from cache", ex);
                }

                if (!getChecksum(holder).equals(wrapper.checksum) || holder.level() != wrapper.getEnvironmentLevel()) {
                    wrapper.writeToNBT(holder.registryAccess());
                    wrapper.autoSave = false;
                    cache.invalidate(id);
                    cache.cleanUp();

                    try {
                        wrapper = cache.get(id, this);
                    } catch (Exception ex) {
                        throw new RuntimeException("Failed to reload machine wrapper", ex);
                    }
                }

                currentStack = null;
                currentHolder = null;

                wrapper.stack = stack;
                wrapper.player = holder;

                return wrapper;
            }
        }

        @Override
        public ItemMachineWrapper call() {
            ItemMachineWrapper wrapper = new ItemMachineWrapper(currentStack, currentHolder);
            wrapper.checksum = getChecksum(currentHolder);
            return wrapper;
        }

        @Override
        public void onRemoval(RemovalNotification<String, ItemMachineWrapper> notification) {
            ItemMachineWrapper state = notification.getValue();
            if (state != null && state.node() != null) {
                if (state.autoSave && state.player != null) {
                    state.writeToNBT(state.player.registryAccess());
                }
                if (state.machine() != null) {
                    state.machine().stop();
                    if (state.machine().node() != null && state.machine().node().network() != null) {
                        for (Node node : state.machine().node().network().nodes()) {
                            node.remove();
                        }
                    }
                }
                state.setChanged();
            }
        }

        public void clear(Level level) {
            synchronized (cache) {
                List<String> keysToRemove = new ArrayList<>();
                for (Map.Entry<String, ItemMachineWrapper> entry : cache.asMap().entrySet()) {
                    if (entry.getValue().getEnvironmentLevel() == level) {
                        keysToRemove.add(entry.getKey());
                    }
                }
                cache.invalidateAll(keysToRemove);
                cache.cleanUp();
            }
        }

        public void cleanUp() {
            synchronized (cache) {
                cache.cleanUp();
            }
        }

        public void keepAlive() {
            synchronized (cache) {
                ImmutableMap.copyOf(cache.getAllPresent(cache.asMap().keySet()));
            }
        }
    }

    // -------------------------------------------------------------- //

    public static class ClientCache extends Cache {

        @Override
        protected long timeout() {
            return 5L;
        }

        @Override
        public ItemMachineWrapper getWeak(ItemStack stack) {
            String id = getId(stack);
            if (id != null && !id.isEmpty()) {
                return cache.getIfPresent(id);
            }
            return null;
        }
    }

    // -------------------------------------------------------------- //

    public static class ServerCache extends Cache {

        @Override
        protected long timeout() {
            return 10L;
        }

        public void save(LivingEntity player) {
            synchronized (cache) {
                for (ItemMachineWrapper wrapper : cache.asMap().values()) {
                    if (wrapper.player == player) {
                        wrapper.writeToNBT(player.registryAccess());
                    }
                }
            }
        }

        public void saveAll(Level level) {
            synchronized (cache) {
                for (ItemMachineWrapper wrapper : cache.asMap().values()) {
                    if (wrapper.getEnvironmentLevel() == level && wrapper.player != null) {
                        wrapper.writeToNBT(wrapper.player.registryAccess());
                    }
                }
            }
        }
    }
}