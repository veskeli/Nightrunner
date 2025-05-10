package net.veskeli.nightrunner.SpellSystem.Spells;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.SpellSystem.Spell;

public class FireballSpell extends Spell {

    private ResourceLocation SPELL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "textures/item/damage_glyph.png");

    public FireballSpell() {
        super();
    }

    @Override
    public boolean castSpell(Level level, Player player, InteractionHand hand) {
        super.castSpell(level, player, hand);

        Vec3 vec3 = player.getLookAngle();

        // Speed it up
        vec3 = vec3.scale(18.0); // Adjust the speed multiplier as needed

        // Summon a fireball entity
        LargeFireball fireball = new LargeFireball(level, player, vec3, 3);
        fireball.setPos(player.getX(), player.getY() + 1.0, player.getZ());
        level.addFreshEntity(fireball);
        return true;
    }

    @Override
    public String getName() {
        return "Fireball";
    }

    @Override
    public ResourceLocation getSpellTexture() {
        return SPELL_TEXTURE;
    }

    @Override
    public int getCost() {
        return 3;
    }
}
