package ayral.gml;

import ayral.gml.item.OpenArmorItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = OpenStuffMod.MOD_ID, value = Dist.CLIENT)
public class KeyInputHandler {
    // Crée un keybinding (ex: touche O)
    public static final KeyBinding KEY_OPEN_ARMOR = new KeyBinding("key.openstuff.armor", GLFW.GLFW_KEY_O, "key.categories.openstuff");

    // Méthode appelée à chaque event clavier
    @SubscribeEvent
    public static void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (KEY_OPEN_ARMOR.isDown()) {
            PlayerEntity player = mc.player;
            ItemStack chestStack = player.getItemBySlot(EquipmentSlotType.CHEST);
            if (!chestStack.isEmpty()) {
                // Appelle ta méthode custom avec player et chestStack
                OpenArmorItem.openTabletGuiFromArmor(player);
            }
        }
    }
}
