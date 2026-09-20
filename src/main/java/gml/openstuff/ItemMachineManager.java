package gml.openstuff;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.ImmutableMap;
import gml.openstuff.item.OpenArmorPiece;
import li.cil.oc.api.network.Node;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@EventBusSubscriber(modid = OpenStuff.MOD_ID)
public class ItemMachineManager {
    public static final Cache SERVER = new Cache();

    public static ItemMachineWrapper get(ItemStack stack, LivingEntity holder) {
        if (holder.level().isClientSide) {
            throw new IllegalStateException("hey I'm trying to build client cache !");
        } else {
            return SERVER.get(stack, holder);
        }
    }

    // -------------------------------------------------------------- //

    public static @Nonnull String getOrCreateId(ItemStack stack) {
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
            SERVER.clear(level);
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre e) {
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
        SERVER.keepAlive();
        SERVER.cleanUp();

        for (ItemMachineWrapper wrapper : SERVER.cache.asMap().values()) {
            wrapper.update();
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        OpenStuff.LOGGER.info("equipment change !");

        if(event.getFrom().is(OpenStuff.OPEN_CHEST.get())){
            String id = getOrCreateId(event.getFrom());
            if(!id.equals(getOrCreateId(event.getTo()))){

                OpenStuff.LOGGER.info("removing a chest !");
                SERVER.cache.invalidate(id);
            }
        }


        ItemStack stack = event.getEntity().getItemBySlot(EquipmentSlot.CHEST);
        if(stack.is(OpenStuff.OPEN_CHEST.get())){
            ItemMachineWrapper wrapper = SERVER.get(stack, event.getEntity());
            wrapper.setHolder(event.getEntity());
            wrapper.connectComponents();
        }
    }


    // -------------------------------------------------------------- //

    public static class Cache implements Callable<ItemMachineWrapper>, RemovalListener<String, ItemMachineWrapper> {
        public final com.google.common.cache.Cache<String, ItemMachineWrapper> cache;

        protected LivingEntity currentHolder;

        public Cache() {
            this.cache = CacheBuilder.newBuilder()
                    .expireAfterAccess(10L, TimeUnit.SECONDS)
                    .removalListener(this)
                    .build();
        }

        public ItemMachineWrapper get(ItemStack stack, LivingEntity holder) {
            String id = getOrCreateId(stack);
            currentHolder = holder;

            ItemMachineWrapper wrapper;
            try {
                wrapper = cache.get(id, this);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load machine wrapper from cache", ex);
            }

            currentHolder = null;

            return wrapper;
        }

        @Override
        public ItemMachineWrapper call() {
            OpenStuff.LOGGER.info("Server init !! {}", currentHolder);
            return new ItemMachineWrapper(currentHolder);
        }

        @Override
        public void onRemoval(RemovalNotification<String, ItemMachineWrapper> notification) {
            ItemMachineWrapper state = notification.getValue();
            if (state != null && state.node() != null) {
                if (state.machine() != null) {
                    state.machine().stop();
                    if (state.machine().node() != null && state.machine().node().network() != null) {
                        for (Node node : state.machine().node().network().nodes()) {
                            node.remove();
                        }
                    }
                }
                state.writeToNBT();
            }
        }

        public void clear(Level level) {

            List<String> keysToRemove = new ArrayList<>();
            for (Map.Entry<String, ItemMachineWrapper> entry : cache.asMap().entrySet()) {
                if (entry.getValue().getEnvironmentLevel() == level) {
                    keysToRemove.add(entry.getKey());
                }
            }
            cache.invalidateAll(keysToRemove);
            cache.cleanUp();
        }

        public void cleanUp() {
            cache.cleanUp();
        }

        public void keepAlive() {
            ImmutableMap.copyOf(cache.getAllPresent(cache.asMap().keySet()));
        }

        public void save(LivingEntity holder) {
            for (ItemMachineWrapper wrapper : cache.asMap().values()) {
                if (wrapper.getHolder() == holder) {
                    wrapper.writeToNBT();
                }
            }
        }

        public void saveAll(Level level) {
            for (ItemMachineWrapper wrapper : cache.asMap().values()) {
                if (wrapper.getEnvironmentLevel() == level && wrapper.getHolder() != null) {
                    wrapper.writeToNBT();
                }
            }
        }
    }
}