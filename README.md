# OpenStuff 🧠⚙️

**OpenStuff** est un mod Minecraft 1.16 qui étend [OpenComputers (port 1.16)](https://github.com/KosmosPrime/OpenComputers) avec des objets intelligents comme une **armure-tablette** directement programmable via Lua.

---

## ✨ Fonctionnalités

- 🛡️ **Armure** intégrée à la pièce de torse (slot `CHEST`).
- 📟 Accès à une interface graphic via la touche **`O`** du clavier.
- 🧩 Composant Lua personnalisé (`component.armor`) accessible depuis OpenOS.
- 💡 Lumière d’armure modifiable dynamiquement via le script Lua.
- 🔋 La tablette démarre uniquement si l'armure complète est portée.

---

## 🔧 Dépendances

- Minecraft `1.16.x`
- Forge `36.x` (MC 1.16.5)
- [OpenComputers (KosmosPrime fork)](https://github.com/KosmosPrime/OpenComputers)

---

## 💻 API Lua

Une fois dans OpenOS avec la tablette démarrée, utilisez :

```lua
local armor = component.armor

-- Exemple de gestion de lumière (si exposé par toi ensuite)
armor.setLight(0x00FF00) -- Vert fluo
print(string.format("Current light: %06X", armor.getLight()))

```

---

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


