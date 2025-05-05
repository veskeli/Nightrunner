package net.veskeli.nightrunner.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.veskeli.nightrunner.ManaSystem.Mana;
import net.veskeli.nightrunner.ModAttachments;
import net.veskeli.nightrunner.entity.ModEntities;
import net.veskeli.nightrunner.entity.projectile.WandProjectile;
import net.veskeli.nightrunner.item.properties.WandItemProperties;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class WandItem extends Item{

    private float power = 4.0f;
    private float aoeRadius = 1.0f;

    public WandItem(WandItemProperties properties) {
        super(properties);
        this.power = properties.getPower();
        this.aoeRadius = properties.getAoeRadius();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand)
    {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            // Summon the WandProjectile
            WandProjectile projectile = new WandProjectile(ModEntities.WAND_PROJECTILE.get(), level);
            projectile.setCustomProperties(power, aoeRadius);
            projectile.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
            projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 3.0F); // same speed as arrow
            level.addFreshEntity(projectile);

            // Play sound effect
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.PLAYERS, 0.5f, 1.0f);

            // Damage the item
            itemStack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

            // Apply use time
            player.getCooldowns().addCooldown(this, 20);
        }

        // reduce mana
        Mana mana = player.getData(ModAttachments.PLAYER_MANA);
        mana.subtractMana(1);
        player.setData(ModAttachments.PLAYER_MANA, mana);

        // print mana to action bar
        player.displayClientMessage(Component.literal("Mana: " + mana.getMana()), true);

        return InteractionResultHolder.success(itemStack);
    }
}
