package com.ayral.openstuff.items;

import li.cil.oc.api.CreativeTab;

import static com.ayral.openstuff.items.OpenStuffItems.setItemName;

public class TurretUpgrade extends ArmorUpgrade{
    public TurretUpgrade(String name) {
        setItemName(this,name);
        setCreativeTab(CreativeTab.instance);
    }

    @Override
    public String getUpgradeName() {
        return "turret";
    }
}
