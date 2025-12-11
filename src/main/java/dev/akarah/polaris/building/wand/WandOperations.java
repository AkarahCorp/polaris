package dev.akarah.polaris.building.wand;

import dev.akarah.polaris.building.palette.MultiPalette;
import dev.akarah.polaris.building.palette.NoopPalette;
import dev.akarah.polaris.building.palette.Palette;
import dev.akarah.polaris.building.region.MultiRegion;
import dev.akarah.polaris.building.region.NoopRegion;
import dev.akarah.polaris.building.region.Region;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

public class WandOperations {
    public static void applyToWand(Entity entity, Consumer<ItemStack> editFunction) {
        if(entity instanceof ServerPlayer player) {
            var item = player.getItemBySlot(EquipmentSlot.MAINHAND);
            editFunction.accept(item);
            updateDesc(item);
            player.setItemInHand(InteractionHand.MAIN_HAND, item);
        }
    }

    public static void useWandLeftClick(ServerPlayer player) {
        try {
            var target = player.getEyePosition();
            int times = 0;
            while(player.level().getBlockState(new BlockPos((int) target.x, (int) target.y, (int) target.z)).is(Blocks.AIR) && times < 500) {
                times++;
                target = target.add(player.getLookAngle().multiply(0.33, 0.33, 0.33));
            }
            if(times > 499) {
                return;
            }
            var pos = new BlockPos((int) target.x, (int) target.y, (int) target.z);
            if(player.getItemBySlot(EquipmentSlot.MAINHAND).getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().contains("wand/is")) {
                var palette = getPalette(player.getItemBySlot(EquipmentSlot.MAINHAND));
                var region = getRegion(player.getItemBySlot(EquipmentSlot.MAINHAND));
                Thread.ofVirtual().start(() -> {
                    region.forEach(player.level(), pos, palette::apply);
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ItemStack emptyWand() {
        var is = new ItemStack(Items.NETHERITE_SPEAR);
        setRegion(is, new NoopRegion());
        setPalette(is, new NoopPalette());
        updateDesc(is);
        return is;
    }

    public static void updateDesc(ItemStack stack) {
        var lore = new ArrayList<Component>();
        lore.add(Component.literal("Regions:").withColor(ARGB.color(200, 200, 200)));
        lore.addAll(getRegion(stack).description());
        lore.add(Component.literal(""));
        lore.add(Component.literal("Palettes:").withColor(ARGB.color(200, 200, 200)));
        lore.addAll(getPalette(stack).description());
        lore.add(Component.literal(""));
        lore.add(Component.literal("Left-click to apply at target!").withColor(ARGB.color(200, 200, 200)));
        lore.add(Component.literal("Right-click to apply at your position!").withColor(ARGB.color(200, 200, 200)));

        stack.set(DataComponents.LORE, new ItemLore(lore.stream().map(x -> {
            var newStyle = x.getStyle().withItalic(false);
            return (Component) x.copy().withStyle(newStyle);
        }).toList()));

        stack.set(
                DataComponents.TOOLTIP_DISPLAY,
                TooltipDisplay.DEFAULT
                        .withHidden(
                                DataComponents.ATTRIBUTE_MODIFIERS,
                                true
                        )
        );

        stack.set(DataComponents.ITEM_NAME, Component.literal("Polariscope Wand"));
        stack.set(DataComponents.RARITY, Rarity.EPIC);
    }

    public static Consumer<ItemStack> resetRegions() {
        return stack -> {
            setRegion(stack, new NoopRegion());
            updateDesc(stack);
        };
    }

    public static Consumer<ItemStack> addRegion(Region region) {
        return stack -> {
            setRegion(stack, new MultiRegion(getRegion(stack), region));
            updateDesc(stack);
        };
    }

    public static Consumer<ItemStack> resetPalettes() {
        return stack -> {
            setPalette(stack, new NoopPalette());
            updateDesc(stack);
        };
    }

    public static Consumer<ItemStack> addPalette(Palette palette) {
        return stack -> {
            setPalette(stack, new MultiPalette(getPalette(stack), palette));
            updateDesc(stack);
        };
    }

    public static Region getRegion(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.CUSTOM_DATA))
                .map(i -> i.copyTag().get("wand/region"))
                .map(tag -> Region.CODEC.decode(NbtOps.INSTANCE, tag))
                .map(decoded -> decoded.getOrThrow().getFirst())
                .orElse(new NoopRegion());
    }

    public static Palette getPalette(ItemStack stack) {
        return Optional.ofNullable(stack.get(DataComponents.CUSTOM_DATA))
                .map(i -> i.copyTag().get("wand/palette"))
                .map(tag -> Palette.CODEC.decode(NbtOps.INSTANCE, tag))
                .map(decoded -> decoded.getOrThrow().getFirst())
                .orElse(new NoopPalette());
    }

    public static void setRegion(ItemStack stack, Region region) {
        var encoded = Region.CODEC.encodeStart(NbtOps.INSTANCE, region).getOrThrow();
        stack.update(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()), data -> {
            data = data.update(tag -> tag.put("wand/region", encoded));
            data = data.update(tag -> tag.put("wand/is", IntTag.valueOf(1)));
            return data;
        });
    }

    public static void setPalette(ItemStack stack, Palette palette) {
        var encoded = Palette.CODEC.encodeStart(NbtOps.INSTANCE, palette).getOrThrow();
        stack.update(DataComponents.CUSTOM_DATA, CustomData.of(new CompoundTag()), data -> {
            data = data.update(tag -> tag.put("wand/palette", encoded));
            data = data.update(tag -> tag.put("wand/is", IntTag.valueOf(1)));
            return data;
        });
    }
}
