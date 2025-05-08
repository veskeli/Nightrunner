package net.veskeli.nightrunner.SpellSystem;

import net.minecraft.resources.ResourceLocation;

public interface ISpell {
    String getName();
    int getCost();
    int getCooldown();
    void castSpell();
    void onCast();
    ResourceLocation getSpellTexture();
}
