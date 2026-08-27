package gml.openstuff;

import com.mojang.blaze3d.platform.InputConstants;
import gml.openstuff.integration.opencomputers.ArmorDriver;
import gml.openstuff.integration.opencomputers.ArmorTemplate;
import gml.openstuff.integration.opencomputers.TrimDriver;
import gml.openstuff.item.OpenBoots;
import gml.openstuff.item.OpenChestplate;
import gml.openstuff.item.OpenHelmet;
import gml.openstuff.item.OpenLeggings;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(OpenStuff.MOD_ID)
public final class OpenStuff {
    public static final String MOD_ID = "openstuff";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final KeyMapping AROMOR_INTERACT_KEY = new KeyMapping(
    "key.opencomputers.armor_gui",
    InputConstants.Type.KEYSYM,
    GLFW.GLFW_KEY_O,
    "key.categories.opencomputers"
    );

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MOD_ID);

    public static final DeferredHolder<Item, ArmorItem> OPEN_HELMET = ITEMS.register("open_helmet",() -> new OpenHelmet(new Item.Properties()));
    public static final DeferredHolder<Item, ArmorItem> OPEN_CHEST = ITEMS.register("open_chest",() -> new OpenChestplate(new Item.Properties()));
    public static final DeferredHolder<Item, ArmorItem> OPEN_LEGS = ITEMS.register("open_legs",() -> new OpenLeggings(new Item.Properties()));
    public static final DeferredHolder<Item, ArmorItem> OPEN_BOOTS = ITEMS.register("open_boots",() -> new OpenBoots(new Item.Properties()));


    public OpenStuff(IEventBus modBus) {
        NeoForge.EVENT_BUS.register(this);
        ITEMS.register(modBus);
        modBus.addListener(OpenStuff::commonSetup);
        modBus.addListener(OpenStuff::onRegisterKeyMappings);
        modBus.addListener(Networking::register);
    }


    private static void commonSetup(FMLCommonSetupEvent event) {
        ArmorTemplate.register();
        event.enqueueWork(() -> {
            li.cil.oc.api.Driver.add(new TrimDriver());
            li.cil.oc.api.Driver.add(new ArmorDriver());
        });
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event){
        event.register(AROMOR_INTERACT_KEY);
    }

    @SubscribeEvent
    private void onClientTick(ClientTickEvent.Pre e){
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            while (AROMOR_INTERACT_KEY.consumeClick()) {
                if (mc.screen == null) {
                    ItemStack stack = mc.player.getItemBySlot(EquipmentSlot.CHEST);
                    if(stack.is(OPEN_CHEST.get())) Networking.askServerInteraction(stack);
                }
            }
        }
    }

    @SubscribeEvent
    private void onEntityTick(EntityTickEvent.Pre e){
        if (e.getEntity() instanceof LivingEntity player){
            ItemStack stack = player.getItemBySlot(EquipmentSlot.CHEST);
            if(stack.is(OPEN_CHEST.get())){
                ItemMachineWrapper wrapper = ItemMachineManager.get(stack, player);
                wrapper.update(player.level(), player);
            }
        }
    }
}
