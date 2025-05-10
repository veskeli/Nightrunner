package net.veskeli.nightrunner.SpellSystem.Spells;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.SpellSystem.Spell;

import java.util.List;

public class Longstrider extends Spell {

    public Longstrider() {
        super();
    }

    @Override
    public String getName() {
        return "Longstrider";
    }

    @Override
    public int getCost() {
        return 1;
    }

    @Override
    public ResourceLocation getSpellTexture() {
        return ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "textures/item/runestone.png");
    }

    @Override
    public boolean castSpell(Level level, Player player, InteractionHand hand) {

        float areaSize = 4.0f; // The size of the area to check
        AABB area = new AABB(
            player.getX() - areaSize, player.getY() - areaSize, player.getZ() - areaSize,
            player.getX() + areaSize, player.getY() + areaSize, player.getZ() + areaSize
        );

        List<Player> playersInArea = level.getEntitiesOfClass(Player.class, area, Player::isAlive);

        for (Player target : playersInArea) {
            // Apply the Longstrider effect to each player in the area
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1));
        }

        return true;
    }
}
