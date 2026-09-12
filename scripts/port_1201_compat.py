from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]


def edit(path: str, transform):
    file = ROOT / path
    text = file.read_text(encoding="utf-8")
    new = transform(text)
    if new != text:
        file.write_text(new, encoding="utf-8")
        print(f"updated {path}")


def common_geckolib(text: str) -> str:
    text = text.replace(
        "software.bernie.geckolib.animatable.instance.AnimatableInstanceCache",
        "software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache",
    )
    text = text.replace(
        "software.bernie.geckolib.animation.",
        "software.bernie.geckolib.core.animation.",
    )
    text = text.replace(
        "software.bernie.geckolib.core.animation.PlayState",
        "software.bernie.geckolib.core.object.PlayState",
    )
    text = text.replace(
        "software.bernie.geckolib.util.RenderUtil.",
        "software.bernie.geckolib.util.RenderUtils.",
    )
    return text


for path in [
    "src/main/java/com/minecartmagic/entity/AdvancedMinecartEntity.java",
    "src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java",
    "src/main/java/com/minecartmagic/client/AdvancedMinecartRenderer.java",
    "src/main/java/com/minecartmagic/client/SelfPropellingBoatModel.java",
    "src/main/java/com/minecartmagic/client/SelfPropellingBoatRenderer.java",
]:
    edit(path, common_geckolib)


# GeckoLib 4.8.4 moved the bone type used by the animation processor.
edit(
    "src/main/java/com/minecartmagic/client/SelfPropellingBoatModel.java",
    lambda t: t
    .replace(
        "import software.bernie.geckolib.cache.object.GeoBone;",
        "import software.bernie.geckolib.core.model.CoreGeoBone;",
    )
    .replace("GeoBone leftPaddle", "CoreGeoBone leftPaddle")
    .replace("GeoBone rightPaddle", "CoreGeoBone rightPaddle"),
)


# GeckoLib 4.8.x renderer methods gained RGBA parameters.
edit(
    "src/main/java/com/minecartmagic/client/SelfPropellingBoatRenderer.java",
    lambda t: t
    .replace(
        "                        packedOverlay,\n                        colour\n                );",
        "                        packedOverlay,\n                        1.0F,\n                        1.0F,\n                        1.0F,\n                        1.0F\n                );",
    )
    .replace(
        "                packedOverlay,\n                colour\n        );",
        "                packedOverlay,\n                1.0F,\n                1.0F,\n                1.0F,\n                1.0F\n        );",
    ),
)


# Fabric 1.20.1 uses the non-generic ExtendedScreenHandlerFactory API.
for path in [
    "src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java",
    "src/main/java/com/minecartmagic/mixin/FurnaceMinecartGuiMixin.java",
]:
    edit(path, lambda t: t.replace("ExtendedScreenHandlerFactory<Integer>", "ExtendedScreenHandlerFactory"))


# 1.20.1 DataTracker API has initDataTracker() + startTracking().
def fix_boat_tracker(t: str) -> str:
    t = t.replace(
        "protected void initDataTracker(\n            DataTracker.Builder builder\n    ) {\n        super.initDataTracker(builder);",
        "protected void initDataTracker() {\n        super.initDataTracker();",
    )
    t = t.replace(
        "        builder.add(\n                BURN_TIME,\n                0\n        );",
        "        getDataTracker().startTracking(BURN_TIME, 0);",
    )
    t = t.replace(
        "        builder.add(\n                FUEL_TIME,\n                0\n        );",
        "        getDataTracker().startTracking(FUEL_TIME, 0);",
    )
    t = t.replace(
        "        builder.add(\n                ENGINE_TAILWIND_LEVEL,\n                0\n        );",
        "        getDataTracker().startTracking(ENGINE_TAILWIND_LEVEL, 0);",
    )
    return t

edit("src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java", fix_boat_tracker)


# Entity registry manager moved behind World in 1.20.1.
for path in [
    "src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java",
    "src/main/java/com/minecartmagic/mixin/BoatTailwindDropMixin.java",
    "src/main/java/com/minecartmagic/mixin/FurnaceMinecartGuiMixin.java",
    "src/main/java/com/minecartmagic/mixin/MinecartDropItemMixin.java",
]:
    edit(path, lambda t: t.replace("getRegistryManager()", "getWorld().getRegistryManager()"))


# RegistryEntry -> Enchantment when adding an enchantment to an ItemStack.
for path in [
    "src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java",
    "src/main/java/com/minecartmagic/mixin/BoatTailwindDropMixin.java",
    "src/main/java/com/minecartmagic/mixin/MinecartDropItemMixin.java",
    "src/main/java/com/minecartmagic/ModItems.java",
]:
    edit(path, lambda t: t.replace(".entryOf(\n                                    ModEnchantments.TAILWIND_KEY\n                            )", ".entryOf(\n                                    ModEnchantments.TAILWIND_KEY\n                            ).value()").replace(".entryOf(\n                        ModEnchantments.TRACTION_KEY\n                )", ".entryOf(\n                        ModEnchantments.TRACTION_KEY\n                ).value()"))


# Legacy Fabric networking used by 1.20.1.
edit(
    "src/main/java/com/minecartmagic/network/BoatScreenPayload.java",
    lambda _t: '''package com.minecartmagic.network;\n\nimport net.fabricmc.fabric.api.networking.v1.FabricPacket;\nimport net.minecraft.network.PacketByteBuf;\nimport net.minecraft.util.Identifier;\n\npublic record BoatScreenPayload() implements FabricPacket {\n\n    public static final BoatScreenPayload INSTANCE = new BoatScreenPayload();\n\n    public static final Identifier ID =\n            new Identifier(\n                    "minecartmagic",\n                    "open_self_propelling_boat"\n            );\n\n    @Override\n    public Identifier getId() {\n        return ID;\n    }\n\n    @Override\n    public void write(PacketByteBuf buf) {\n    }\n}\n''',
)

edit(
    "src/main/java/com/minecartmagic/network/ModNetworking.java",
    lambda _t: '''package com.minecartmagic.network;\n\nimport com.minecartmagic.entity.SelfPropellingBoatEntity;\nimport net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;\nimport net.minecraft.entity.player.PlayerEntity;\n\npublic final class ModNetworking {\n\n    private ModNetworking() {\n    }\n\n    public static void init() {\n        ServerPlayNetworking.registerGlobalReceiver(\n                BoatScreenPayload.ID,\n                (server, player, handler, buf, responseSender) ->\n                        server.execute(() -> {\n                            if (!(player.getVehicle() instanceof SelfPropellingBoatEntity boat)) {\n                                return;\n                            }\n\n                            if (boat.isRemoved()) {\n                                return;\n                            }\n\n                            player.openHandledScreen(boat);\n                        })\n        );\n    }\n}\n''',
)


edit(
    "src/main/java/com/minecartmagic/mixin/SelfPropellingBoatInventoryMixin.java",
    lambda t: t.replace(
        "ClientPlayNetworking.send(\n                BoatScreenPayload.INSTANCE\n        );",
        "ClientPlayNetworking.send(\n                BoatScreenPayload.INSTANCE\n        );",
    ),
)


# 1.20.1 ExtendedScreenHandlerType receives a PacketByteBuf directly.
edit(
    "src/main/java/com/minecartmagic/screen/ModScreenHandlers.java",
    lambda _t: '''package com.minecartmagic.screen;\n\nimport com.minecartmagic.MinecartMagicMod;\nimport net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;\nimport net.minecraft.network.PacketByteBuf;\nimport net.minecraft.registry.Registries;\nimport net.minecraft.registry.Registry;\n\npublic final class ModScreenHandlers {\n\n    public static final ExtendedScreenHandlerType<SelfPropellingBoatScreenHandler>\n            SELF_PROPELLING_BOAT =\n            new ExtendedScreenHandlerType<>(\n                    (syncId, playerInventory, buf) ->\n                            new SelfPropellingBoatScreenHandler(\n                                    syncId,\n                                    playerInventory,\n                                    buf.readInt()\n                            )\n            );\n\n    public static final ExtendedScreenHandlerType<SelfPropellingMinecartScreenHandler>\n            SELF_PROPELLING_MINECART =\n            new ExtendedScreenHandlerType<>(\n                    (syncId, playerInventory, buf) ->\n                            new SelfPropellingMinecartScreenHandler(\n                                    syncId,\n                                    playerInventory,\n                                    buf.readInt()\n                            )\n            );\n\n    static {\n        Registry.register(\n                Registries.SCREEN_HANDLER,\n                MinecartMagicMod.id("self_propelling_boat"),\n                SELF_PROPELLING_BOAT\n        );\n\n        Registry.register(\n                Registries.SCREEN_HANDLER,\n                MinecartMagicMod.id("self_propelling_minecart"),\n                SELF_PROPELLING_MINECART\n        );\n    }\n\n    private ModScreenHandlers() {\n    }\n\n    public static void init() {\n    }\n}\n''',
)


# Legacy 1.20.1 screen opening data.
edit(
    "src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java",
    lambda t: re.sub(
        r"    @Override\n    public Integer getScreenOpeningData\(\n            ServerPlayerEntity player\n    \) \{.*?    \}\n",
        "    @Override\n    public void writeScreenOpeningData(\n            ServerPlayerEntity player,\n            net.minecraft.network.PacketByteBuf buf\n    ) {\n        buf.writeInt(getId());\n    }\n",
        t,
        flags=re.S,
    ),
)


# 1.20.1 identifiers don't have ofVanilla().
for path in [
    "src/main/java/com/minecartmagic/client/SelfPropellingBoatScreen.java",
    "src/main/java/com/minecartmagic/client/SelfPropellingMinecartScreen.java",
]:
    edit(path, lambda t: t.replace("Identifier.ofVanilla(", "new Identifier(\"minecraft\", ").replace(")", ")", 0))


# VehicleEntity doesn't exist in 1.20.1; target the concrete vehicle classes.
edit(
    "src/main/java/com/minecartmagic/mixin/BoatTailwindDropMixin.java",
    lambda t: t.replace(
        "import net.minecraft.entity.vehicle.VehicleEntity;",
        "import net.minecraft.entity.vehicle.BoatEntity;",
    ).replace("@Mixin(VehicleEntity.class)", "@Mixin(BoatEntity.class)"),
)
edit(
    "src/main/java/com/minecartmagic/mixin/MinecartDropItemMixin.java",
    lambda t: t.replace(
        "import net.minecraft.entity.vehicle.VehicleEntity;",
        "import net.minecraft.entity.vehicle.AbstractMinecartEntity;",
    ).replace("@Mixin(VehicleEntity.class)", "@Mixin(AbstractMinecartEntity.class)"),
)
