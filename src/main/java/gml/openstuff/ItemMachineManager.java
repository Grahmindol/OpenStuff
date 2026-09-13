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

    public static ItemMachineWrapper getWeak(ItemStack stack, Level level) {
        if (level.isClientSide) {
            return CLIENT.getWeak(stack);
        } else {
            return SERVER.getWeak(stack);
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

        for (Map.Entry<String, ItemMachineWrapper> entry : SERVER.cache.asMap().entrySet()) {
            ItemMachineWrapper wrapper = entry.getValue();
            if(!getChecksum(wrapper.holder).equals(wrapper.checksum)){
                wrapper.writeToNBT(wrapper.holder.registryAccess());
                wrapper.autoSave = false;
                CLIENT.cache.invalidate(entry.getKey());
            }
        }

        CLIENT.cleanUp();
        if (ServerLifecycleHooks.getCurrentServer() instanceof IntegratedServer) {
            if (Minecraft.getInstance().isPaused()) {
                CLIENT.keepAlive();
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


    /**
     * called on server each tick, we update each cached network.
     */
    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Pre e) {
        SERVER.cleanUp();

        for (ItemMachineWrapper wrapper : SERVER.cache.asMap().values()) {
            wrapper.update();
        }
    }

    /**
     * Update holders on both sides
     */
    @SubscribeEvent
    private static void onEntityTick(EntityTickEvent.Pre e) {
        if (e.getEntity() instanceof LivingEntity holder) {
            ItemStack stack = holder.getItemBySlot(EquipmentSlot.CHEST);

            if (stack.is(OpenStuff.OPEN_CHEST.get())) {
                String id = getId(stack);

                if (id != null) {
                    Cache targetCache = holder.level().isClientSide() ? CLIENT : SERVER;
                    ItemMachineWrapper wrapper = targetCache.cache.getIfPresent(id);

                    if (wrapper != null) {
                        wrapper.holder = holder;
                    }
                }
            }
        }
    }

    /**
     * when an openChest is equiped on server side, we create the wrapper.
     */
    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        OpenStuff.LOGGER.info("equipment change !");

        ItemStack stack = event.getEntity().getItemBySlot(EquipmentSlot.CHEST);
        if(stack.is(OpenStuff.OPEN_CHEST.get())){
            ItemMachineWrapper wrapper = SERVER.get(stack, event.getEntity());

            wrapper.connectComponents();
            wrapper.checksum = getChecksum(event.getEntity());
        }

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
            String id = getId(stack);
            if (id != null && !id.isEmpty()) {
                return cache.getIfPresent(id);
            }
            return null;
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

                currentStack = null;
                currentHolder = null;

                wrapper.stack = stack;
                wrapper.holder = holder;

                return wrapper;
            }
        }

        @Override
        public ItemMachineWrapper call() {
            if(currentHolder.level().isClientSide){
                OpenStuff.LOGGER.info("Client init !! {} {}", currentHolder, currentStack);
            }else {
                OpenStuff.LOGGER.info("Server init !! {} {}", currentHolder, currentStack);
            }
            ItemMachineWrapper wrapper = new ItemMachineWrapper(currentStack, currentHolder);
            wrapper.checksum = getChecksum(currentHolder);
            return wrapper;
        }

        @Override
        public void onRemoval(RemovalNotification<String, ItemMachineWrapper> notification) {
            ItemMachineWrapper state = notification.getValue();
            if (state != null && state.node() != null) {
                if (state.autoSave && state.holder != null) {
                    state.writeToNBT(state.holder.registryAccess());
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
    }

    // -------------------------------------------------------------- //

    public static class ServerCache extends Cache {

        @Override
        protected long timeout() {
            return 10L;
        }

        public void save(LivingEntity holder) {
            synchronized (cache) {
                for (ItemMachineWrapper wrapper : cache.asMap().values()) {
                    if (wrapper.holder == holder) {
                        wrapper.writeToNBT(holder.registryAccess());
                    }
                }
            }
        }

        public void saveAll(Level level) {
            synchronized (cache) {
                for (ItemMachineWrapper wrapper : cache.asMap().values()) {
                    if (wrapper.getEnvironmentLevel() == level && wrapper.holder != null) {
                        wrapper.writeToNBT(wrapper.holder.registryAccess());
                    }
                }
            }
        }
    }
}