package net.veskeli.nightrunner.item.custom;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WandItem extends Item{
    public WandItem(Properties properties) {
        super(properties);
    }

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();

        // Log the block clicked
        System.out.println("Clicked block: " + context.getClickedPos());

        Block block = level.getBlockState(context.getClickedPos()).getBlock();
        if(block == Blocks.AIR)
        {
            return InteractionResult.FAIL;
        }

        if(!level.isClientSide())
        {
            // Replace with air
            level.setBlockAndUpdate(context.getClickedPos(), Blocks.AIR.defaultBlockState());

            context.getItemInHand().hurtAndBreak(1, ((ServerLevel) level), context.getPlayer(),
                    item -> Objects.requireNonNull(context.getPlayer()).onEquippedItemBroken(item, EquipmentSlot.MAINHAND));

            level.playSound(null, context.getClickedPos(), SoundEvents.GRINDSTONE_USE, SoundSource.PLAYERS);
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }
}
