# OpenStuff 🧠⚙️

**OpenStuff** is a Minecraft 1.16 mod that extends [OpenComputers (1.16 port)](https://github.com/KosmosPrime/OpenComputers) with a **smart armor** directly programmable in Lua.

***

## ✨ Features

*   🛡️ **Armor** crafted from Netherite armor
*   📟 Access to a graphical interface via the **`O`** key
*   🧩 Custom Lua component (`component.armor`) accessible from OpenOS
*   💡 Armor light dynamically changeable via Lua script

***

## 🔧 Dependencies

*   Minecraft `1.16.x`
*   Forge `36.x` (MC 1.16.5)
*   [OpenComputers (KosmosPrime fork)](https://github.com/KosmosPrime/OpenComputers)

***

## 💻 Lua API

Once inside OpenOS with the armor running, use:

```
local armor = component.armor

-- Example of light control (if exposed by you later)
armor.setLight(0x00FF00) -- Bright green
print(string.format("Current light: %06X", armor.getLight()))
```

***

## 🧪 Compilation

```bash
git clone https://github.com/TON_PSEUDO/OpenStuff.git
cd OpenStuff
./gradlew build
```

---

## 🧠 Notes Techniques
- L’armure embarque une tablette OC configurée lors de la création.
- L’objet ArmorComponent implémente DeviceInfo et s’intègre à l’environnement OC comme composant.
- Le stockage de la tablette est persisté dans le tag NBT Tablet de l’item ItemStack du chestplate.

---

## 👤 Auteur
Mod développé par @Grahmindol
Licence : MIT


