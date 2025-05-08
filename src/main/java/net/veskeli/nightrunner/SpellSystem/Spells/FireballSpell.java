package net.veskeli.nightrunner.SpellSystem.Spells;

import net.minecraft.resources.ResourceLocation;
import net.veskeli.nightrunner.Nightrunner;
import net.veskeli.nightrunner.SpellSystem.Spell;

public class FireballSpell extends Spell {

    private ResourceLocation SPELL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "textures/item/damage_glyph.png");

    public FireballSpell() {
        super();
    }

    @Override
    public String getName() {
        return "Fireball";
    }

    @Override
    public ResourceLocation getSpellTexture() {
        return SPELL_TEXTURE;
    }
}
