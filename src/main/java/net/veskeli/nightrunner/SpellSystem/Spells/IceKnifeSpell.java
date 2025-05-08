package net.veskeli.nightrunner.SpellSystem.Spells;

import net.minecraft.resources.ResourceLocation;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.SpellSystem.Spell;

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
}
