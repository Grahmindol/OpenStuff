package gml.openstuff;

import com.mojang.blaze3d.platform.InputConstants;
import gml.openstuff.client.renderer.ArmorComponentLayer;
import gml.openstuff.data.MachineData;
import gml.openstuff.data.PieceData;
import gml.openstuff.integration.opencomputers.ArmorDriver;
import gml.openstuff.integration.opencomputers.ArmorTemplate;
import gml.openstuff.integration.opencomputers.TrimDriver;
import gml.openstuff.integration.openstuff.ProcessorDriverRenderer;
import gml.openstuff.integration.openstuff.TrimDriverRenderer;
import gml.openstuff.item.OpenBoots;
import gml.openstuff.item.OpenChestplate;
import gml.openstuff.item.OpenHelmet;
import gml.openstuff.item.OpenLeggings;
import li.cil.oc.Constants;
import li.cil.oc.common.Loot;
import li.cil.oc.common.init.OCBlocks;
import li.cil.oc.common.init.OCBlocks$;
import li.cil.oc.common.init.OCItems;
import li.cil.oc.common.init.OCItems$;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.model.ArmorStandArmorModel;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

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
        ITEMS.register(modBus);
        modBus.addListener(OpenStuff::onCommonSetup);
        modBus.addListener(OpenStuff::onLoadComplete);
        modBus.addListener(OpenStuff::onRegisterKeyMappings);
        modBus.addListener(Networking::register);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(this::onAddLayers);
        }
    }


    private static void onCommonSetup(FMLCommonSetupEvent event) {
        ArmorTemplate.register();
        event.enqueueWork(() -> {
            li.cil.oc.api.Driver.add(new TrimDriver());
            li.cil.oc.api.Driver.add(new ArmorDriver());
        });

        gml.openstuff.client.renderer.ArmorComponentLayer.add(new TrimDriverRenderer());
        gml.openstuff.client.renderer.ArmorComponentLayer.add(new ProcessorDriverRenderer());


    }

    private static void onLoadComplete(BuildCreativeModeTabContentsEvent event){
        // just to be executed once
        if (event.getTabKey() != CreativeModeTabs.INGREDIENTS) return;

        ItemStack helmet = new ItemStack(OPEN_HELMET);
        PieceData helmet_data = new PieceData();
        ItemStack[] helmet_items = new ItemStack[] {
                OCBlocks$.MODULE$.ScreenTier1().toStack(),
                OCBlocks$.MODULE$.Keyboard().toStack(),
        };

        helmet_data.items = Arrays.copyOf(helmet_items, 32);
        Arrays.fill(helmet_data.items, helmet_items.length, 32, ItemStack.EMPTY);
        helmet_data.saveData(helmet);

        li.cil.oc.api.Items.registerStack(helmet, "Creative Helmet", null);

        ItemStack chest = new ItemStack(OPEN_CHEST);
        PieceData data = new PieceData();
        ItemStack[] items = new ItemStack[] {
                OCItems$.MODULE$.GraphicsCardTier3().toStack(),
                OCItems$.MODULE$.WirelessNetworkCardTier2().toStack(),

                OCItems$.MODULE$.CPUTier3().toStack(),
                OCItems$.MODULE$.RAMTier6().toStack(),
                OCItems$.MODULE$.RAMTier6().toStack(),

                Loot.defaultEEPROM().copy(),
                OCItems$.MODULE$.HDDTier3().toStack(),
        };
        data.items = Arrays.copyOf(items, 32);
        Arrays.fill(data.items, items.length, 32, ItemStack.EMPTY);
        data.saveData(chest);

        MachineData machine_data = new MachineData();

        machine_data.container = OCBlocks$.MODULE$.DiskDrive().toStack();
        machine_data.isRunning = false;

        HolderLookup.Provider registries = VanillaRegistries.createLookup();
        machine_data.saveData(chest, registries);

        li.cil.oc.api.Items.registerStack(chest, "Creative Chest", null);

        ItemStack legs = new ItemStack(OPEN_LEGS);
        li.cil.oc.api.Items.registerStack(legs, "Creative Legs", null);

        ItemStack boots = new ItemStack(OPEN_BOOTS);
        li.cil.oc.api.Items.registerStack(boots, "Creative Boots", null);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event){
        event.register(AROMOR_INTERACT_KEY);
    }

    public void onAddLayers(EntityRenderersEvent.AddLayers event) {
        EntityModelSet models = event.getEntityModels();

        PlayerRenderer wideRenderer = event.getSkin(PlayerSkin.Model.WIDE);
        if (wideRenderer != null) {
            HumanoidArmorModel<AbstractClientPlayer> innerModel = new HumanoidArmorModel<>(models.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
            HumanoidArmorModel<AbstractClientPlayer> outerModel = new HumanoidArmorModel<>(models.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));

            wideRenderer.addLayer(new ArmorComponentLayer<>(wideRenderer, innerModel, outerModel));
        }

        PlayerRenderer slimRenderer = event.getSkin(PlayerSkin.Model.SLIM);
        if (slimRenderer != null) {
            HumanoidArmorModel<AbstractClientPlayer> slimInnerModel = new HumanoidArmorModel<>(models.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR));
            HumanoidArmorModel<AbstractClientPlayer> slimOuterModel = new HumanoidArmorModel<>(models.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR));

            slimRenderer.addLayer(new ArmorComponentLayer<>(slimRenderer, slimInnerModel, slimOuterModel));
        }

        ArmorStandRenderer armorStandRenderer = event.getRenderer(EntityType.ARMOR_STAND);
        if (armorStandRenderer != null) {
            ArmorStandArmorModel innerArmorStand = new ArmorStandArmorModel(models.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR));
            ArmorStandArmorModel outerArmorStand = new ArmorStandArmorModel(models.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR));
            armorStandRenderer.addLayer(new ArmorComponentLayer<>(armorStandRenderer, innerArmorStand, outerArmorStand));
        }
    }
}
