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


# GeckoLib 4.8.4 moved the animation-processor bone type.
def fix_model_bone_type(t: str) -> str:
    t = t.replace(
        "import software.bernie.geckolib.cache.object.GeoBone;",
        "import software.bernie.geckolib.core.animatable.model.CoreGeoBone;",
    )
    t = t.replace(
        "import software.bernie.geckolib.core.model.CoreGeoBone;",
        "import software.bernie.geckolib.core.animatable.model.CoreGeoBone;",
    )
    t = t.replace("CoreCoreGeoBone", "CoreGeoBone")
    t = t.replace("GeoBone leftPaddle", "CoreGeoBone leftPaddle")
    t = t.replace("GeoBone rightPaddle", "CoreGeoBone rightPaddle")
    return t


edit(
    "src/main/java/com/minecartmagic/client/SelfPropellingBoatModel.java",
    fix_model_bone_type,
)


# GeckoLib 4.8.x renderer hooks use RGBA floats instead of the old colour int.
def fix_boat_renderer_hooks(t: str) -> str:
    t = t.replace(
        "            int packedOverlay,\n            int colour\n    ) {",
        "            int packedOverlay,\n            float red,\n            float green,\n            float blue,\n            float alpha\n    ) {",
    )
    t = t.replace(
        "    @Override\n    protected void applyRotations(",
        "    protected void applyRotations(",
    )
    return t


edit(
    "src/main/java/com/minecartmagic/client/SelfPropellingBoatRenderer.java",
    fix_boat_renderer_hooks,
)

# The custom minecart renderer used the same helper name, but that hook is not
# present in the GeckoLib 4.8.4 renderer hierarchy.
edit(
    "src/main/java/com/minecartmagic/client/AdvancedMinecartRenderer.java",
    lambda t: t.replace(
        "    @Override\n    protected void applyRotations(",
        "    protected void applyRotations(",
    ),
)


# Fabric 1.20.1 uses the non-generic ExtendedScreenHandlerFactory API.
for path in [
    "src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java",
    "src/main/java/com/minecartmagic/mixin/FurnaceMinecartGuiMixin.java",
]:
    edit(
        path,
        lambda t: t.replace(
            "ExtendedScreenHandlerFactory<Integer>",
            "ExtendedScreenHandlerFactory",
        ),
    )


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


edit(
    "src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java",
    fix_boat_tracker,
)


# Normalize all registry-manager chains. This also makes the pass idempotent.
def fix_registry_manager(t: str) -> str:
    return re.sub(
        r"(?:getWorld\(\)\.)+getRegistryManager\(\)",
        "getWorld().getRegistryManager()",
        t,
    )


for path in [
    "src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java",
    "src/main/java/com/minecartmagic/mixin/BoatTailwindDropMixin.java",
    "src/main/java/com/minecartmagic/mixin/FurnaceMinecartGuiMixin.java",
    "src/main/java/com/minecartmagic/mixin/MinecartDropItemMixin.java",
]:
    edit(path, fix_registry_manager)


# The old transform may already have expanded a correct chain once. Normalize
# it before doing any further compatibility edits.
for path in [
    "src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java",
    "src/main/java/com/minecartmagic/mixin/BoatTailwindDropMixin.java",
    "src/main/java/com/minecartmagic/mixin/FurnaceMinecartGuiMixin.java",
    "src/main/java/com/minecartmagic/mixin/MinecartDropItemMixin.java",
]:
    edit(
        path,
        lambda t: t.replace(
            "getRegistryManager()",
            "getRegistryManager()",
        ),
    )


# RegistryEntry -> Enchantment for ItemStack.addEnchantment in 1.20.1.
def fix_enchantment_values(t: str) -> str:
    t = t.replace(
        "                tailwind,\n                level\n        );",
        "                tailwind.value(),\n                level\n        );",
    )
    t = t.replace(
        "                traction,\n                level\n        );",
        "                traction.value(),\n                level\n        );",
    )
    t = t.replace(
        "                enchantment,\n                level\n        );",
        "                enchantment.value(),\n                level\n        );",
    )
    return t


for path in [
    "src/main/java/com/minecartmagic/mixin/BoatTailwindDropMixin.java",
    "src/main/java/com/minecartmagic/mixin/MinecartDropItemMixin.java",
    "src/main/java/com/minecartmagic/ModItems.java",
]:
    edit(path, fix_enchantment_values)


# Legacy Fabric networking used by 1.20.1.
edit(
    "src/main/java/com/minecartmagic/network/BoatScreenPayload.java",
    lambda _t: '''package com.minecartmagic.network;\n\nimport net.fabricmc.fabric.api.networking.v1.FabricPacket;\nimport net.fabricmc.fabric.api.networking.v1.PacketType;\nimport net.minecraft.network.PacketByteBuf;\nimport net.minecraft.util.Identifier;\n\npublic record BoatScreenPayload() implements FabricPacket {\n\n    public static final Identifier ID =\n            new Identifier(\n                    "minecartmagic",\n                    "open_self_propelling_boat"\n            );\n\n    public static final PacketType<BoatScreenPayload> TYPE =\n            PacketType.create(\n                    ID,\n                    BoatScreenPayload::new\n            );\n\n    public static final BoatScreenPayload INSTANCE =\n            new BoatScreenPayload();\n\n    public BoatScreenPayload(PacketByteBuf buf) {\n        this();\n    }\n\n    @Override\n    public PacketType<?> getType() {\n        return TYPE;\n    }\n\n    @Override\n    public void write(PacketByteBuf buf) {\n    }\n}\n''',
)


edit(
    "src/main/java/com/minecartmagic/network/ModNetworking.java",
    lambda _t: '''package com.minecartmagic.network;\n\nimport com.minecartmagic.entity.SelfPropellingBoatEntity;\nimport net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;\n\npublic final class ModNetworking {\n\n    private ModNetworking() {\n    }\n\n    public static void init() {\n        ServerPlayNetworking.registerGlobalReceiver(\n                BoatScreenPayload.TYPE,\n                (server, player, handler, buf, responseSender) ->\n                        server.execute(() -> {\n                            if (!(player.getVehicle() instanceof SelfPropellingBoatEntity boat)) {\n                                return;\n                            }\n\n                            if (boat.isRemoved()) {\n                                return;\n                            }\n\n                            player.openHandledScreen(boat);\n                        })\n        );\n    }\n}\n''',
)


# 1.20.1 ExtendedScreenHandlerType receives a PacketByteBuf directly.
edit(
    "src/main/java/com/minecartmagic/screen/ModScreenHandlers.java",
    lambda _t: '''package com.minecartmagic.screen;\n\nimport com.minecartmagic.MinecartMagicMod;\nimport net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;\nimport net.minecraft.registry.Registries;\nimport net.minecraft.registry.Registry;\n\npublic final class ModScreenHandlers {\n\n    public static final ExtendedScreenHandlerType<SelfPropellingBoatScreenHandler>\n            SELF_PROPELLING_BOAT =\n            new ExtendedScreenHandlerType<>(\n                    (syncId, playerInventory, buf) ->\n                            new SelfPropellingBoatScreenHandler(\n                                    syncId,\n                                    playerInventory,\n                                    buf.readInt()\n                            )\n            );\n\n    public static final ExtendedScreenHandlerType<SelfPropellingMinecartScreenHandler>\n            SELF_PROPELLING_MINECART =\n            new ExtendedScreenHandlerType<>(\n                    (syncId, playerInventory, buf) ->\n                            new SelfPropellingMinecartScreenHandler(\n                                    syncId,\n                                    playerInventory,\n                                    buf.readInt()\n                            )\n            );\n\n    static {\n        Registry.register(\n                Registries.SCREEN_HANDLER,\n                MinecartMagicMod.id("self_propelling_boat"),\n                SELF_PROPELLING_BOAT\n        );\n\n        Registry.register(\n                Registries.SCREEN_HANDLER,\n                MinecartMagicMod.id("self_propelling_minecart"),\n                SELF_PROPELLING_MINECART\n        );\n    }\n\n    private ModScreenHandlers() {\n    }\n\n    public static void init() {\n    }\n}\n''',
)


# 1.20.1 ExtendedScreenHandlerFactory opening data.
edit(
    "src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java",
    lambda t: re.sub(
        r"    @Override\n    public Integer getScreenOpeningData\(\n            ServerPlayerEntity player\n    \) \{.*?    \}\n",
        "    @Override\n    public void writeScreenOpeningData(\n            ServerPlayerEntity player,\n            net.minecraft.network.PacketByteBuf buf\n    ) {\n        buf.writeInt(getId());\n    }\n",
        t,
        flags=re.S,
    ),
)


edit(
    "src/main/java/com/minecartmagic/mixin/FurnaceMinecartGuiMixin.java",
    lambda t: re.sub(
        r"    @Override\n    public Integer getScreenOpeningData\(\n            ServerPlayerEntity player\n    \) \{.*?    \}\n",
        "    @Override\n    public void writeScreenOpeningData(\n            ServerPlayerEntity player,\n            net.minecraft.network.PacketByteBuf buf\n    ) {\n        buf.writeInt(((FurnaceMinecartEntity) (Object) this).getId());\n    }\n",
        t,
        flags=re.S,
    ),
)


# 1.20.1 identifiers don't have ofVanilla().
for path in [
    "src/main/java/com/minecartmagic/client/SelfPropellingBoatScreen.java",
    "src/main/java/com/minecartmagic/client/SelfPropellingMinecartScreen.java",
]:
    edit(
        path,
        lambda t: t.replace(
            "Identifier.ofVanilla(\"",
            "new Identifier(\"minecraft\", \"",
        ),
    )


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


# AdvancedMinecartEntity has no asItem override in 1.20.1.
edit(
    "src/main/java/com/minecartmagic/entity/AdvancedMinecartEntity.java",
    lambda t: t.replace(
        "    @Override\n    public Item asItem()",
        "    public Item asItem()",
    ),
)


# 1.20.1 BoatEntity uses updatePassengerPosition(Entity, PositionUpdater).
def fix_passenger_position(t: str) -> str:
    pattern = re.compile(
        r"    @Override\n    protected Vec3d getPassengerAttachmentPos\(.*?    \}\n\n    @Override\n    protected void initDataTracker\(\) \{",
        re.S,
    )
    replacement = '''    @Override\n    protected void updatePassengerPosition(\n            Entity passenger,\n            Entity.PositionUpdater positionUpdater\n    ) {\n        super.updatePassengerPosition(\n                passenger,\n                (entity, x, y, z) -> {\n                    Vec3d forward =\n                            getRotationVec(1.0F);\n\n                    forward =\n                            new Vec3d(\n                                    forward.x,\n                                    0.0D,\n                                    forward.z\n                            );\n\n                    if (forward.lengthSquared() < 1.0E-8D) {\n                        positionUpdater.accept(\n                                entity,\n                                x,\n                                y,\n                                z\n                        );\n                        return;\n                    }\n\n                    forward = forward.normalize();\n\n                    Vec3d offset =\n                            forward.multiply(\n                                    PASSENGER_FORWARD_OFFSET\n                            );\n\n                    positionUpdater.accept(\n                            entity,\n                            x + offset.x,\n                            y + offset.y,\n                            z + offset.z\n                    );\n                }\n        );\n    }\n\n    @Override\n    protected void initDataTracker() {'''
    return pattern.sub(replacement, t, count=1)


edit(
    "src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java",
    fix_passenger_position,
)


# Final idempotent cleanups. These are deliberately at the end because the
# compatibility pass itself runs on every CI build.
def final_cleanup(t: str) -> str:
    t = t.replace("CoreCoreGeoBone", "CoreGeoBone")
    t = re.sub(
        r"(?:getWorld\(\)\.)+getRegistryManager\(\)",
        "getWorld().getRegistryManager()",
        t,
    )
    return t


for path in [
    "src/main/java/com/minecartmagic/entity/SelfPropellingBoatEntity.java",
    "src/main/java/com/minecartmagic/entity/AdvancedMinecartEntity.java",
    "src/main/java/com/minecartmagic/client/AdvancedMinecartRenderer.java",
    "src/main/java/com/minecartmagic/client/SelfPropellingBoatModel.java",
    "src/main/java/com/minecartmagic/client/SelfPropellingBoatRenderer.java",
    "src/main/java/com/minecartmagic/mixin/BoatTailwindDropMixin.java",
    "src/main/java/com/minecartmagic/mixin/FurnaceMinecartGuiMixin.java",
    "src/main/java/com/minecartmagic/mixin/MinecartDropItemMixin.java",
    "src/main/java/com/minecartmagic/ModItems.java",
]:
    edit(path, final_cleanup)
