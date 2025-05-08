package net.veskeli.nightrunner.SpellSystem;

import net.minecraft.resources.ResourceLocation;
import net.veskeli.nightrunner.Nightrunner;

public class Spell implements ISpell{

    //private final ResourceLocation id;
    private ResourceLocation SPELL_TEXTURE = ResourceLocation.fromNamespaceAndPath(Nightrunner.MODID, "textures/item/diamond_wand.png");

    public Spell() {
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public int getCost() {
        return 0;
    }

    @Override
    public int getCooldown() {
        return 0;
    }

    @Override
    public void castSpell() {

    }

    @Override
    public void onCast() {

    }

    @Override
    public ResourceLocation getSpellTexture() {
        return SPELL_TEXTURE;
    }

}
