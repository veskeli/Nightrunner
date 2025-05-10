package net.veskeli.nightrunner.SpellSystem.Spells;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.SpellSystem.Spell;
import net.veskeli.nightrunner.entity.ModEntities;
import net.veskeli.nightrunner.entity.projectile.IceKnifeProjectile;

public class IceKnifeSpell extends Spell {
    private ResourceLocation SPELL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "textures/item/poison_glyph.png");

    @Override
    public String getName() {
        return "Ice Knife";
    }

    @Override
    public ResourceLocation getSpellTexture() {
        return SPELL_TEXTURE;
    }

    @Override
    public int getCost() {
        return 2;
    }

    @Override
    public boolean castSpell(Level level, Player player, InteractionHand hand) {
        IceKnifeProjectile iceKnife = new IceKnifeProjectile(ModEntities.ICE_KNIFE_PROJECTILE.get(), level);

        iceKnife.setCustomProperties((ServerPlayer) player, 4.f);
        iceKnife.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        iceKnife.setOwner(player);
        iceKnife.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 2.0F); // same speed as arrow

        level.addFreshEntity(iceKnife);
        return true;
    }
}
