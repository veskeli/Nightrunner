package net.veskeli.nightrunner.SpellSystem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface ISpell {
    String getName();
    int getCost();
    int getCooldown();
    void castSpell(Level level, Player player, InteractionHand hand);
    void onCast();
    ResourceLocation getSpellTexture();
}
