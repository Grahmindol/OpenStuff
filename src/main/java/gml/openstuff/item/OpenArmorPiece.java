package gml.openstuff.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;

abstract class OpenArmorPiece extends ArmorItem {
    public OpenArmorPiece(Type type, Properties properties) {
        super(ArmorMaterials.NETHERITE,
                type,
                properties.fireResistant()
                        .stacksTo(1)
                        .durability(type.getDurability(37))
        );
    }
}
