from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]


def edit(path: str, transform):
    file = ROOT / path
    text = file.read_text(encoding="utf-8")
    new = transform(text)
    if new != text:
        file.write_text(new, encoding="utf-8")
        print(f"finalized {path}")


# SimpleInventory in Minecraft 1.20.1 serializes without a registry manager.
edit(
    "src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java",
    lambda t: re.sub(
        r"fuelInventory\.toNbtList\(\s*getWorld\(\)\.getRegistryManager\(\)\s*\)",
        "fuelInventory.toNbtList()",
        re.sub(
            r"fuelInventory\.readNbtList\(\s*(nbt\.getList\(.*?\))\s*,\s*getWorld\(\)\.getRegistryManager\(\)\s*\)",
            r"fuelInventory.readNbtList(\n                    \1\n            )",
            t,
            flags=re.S,
        ),
    ).replace(
        "boatStack.addEnchantment(\n                    tailwind,",
        "boatStack.addEnchantment(\n                    tailwind.value(),",
    ),
)


edit(
    "src/main/java/com/minecartmagic/mixin/FurnaceMinecartGuiMixin.java",
    lambda t: re.sub(
        r"minecartmagic\$fuelInventory\.toNbtList\(\s*minecart\.getWorld\(\)\.getRegistryManager\(\)\s*\)",
        "minecartmagic$fuelInventory.toNbtList()",
        re.sub(
            r"minecartmagic\$fuelInventory\.readNbtList\(\s*(nbt\.getList\(.*?\))\s*,\s*minecart\.getWorld\(\)\.getRegistryManager\(\)\s*\)",
            r"minecartmagic$fuelInventory.readNbtList(\n                    \1\n            )",
            t,
            flags=re.S,
        ),
    ),
)


# Fabric API 0.92.11 provides the legacy Identifier receiver overload used here.
edit(
    "src/main/java/com/minecartmagic/network/ModNetworking.java",
    lambda t: t.replace(
        "BoatScreenPayload.TYPE,",
        "BoatScreenPayload.ID,",
    ),
)
