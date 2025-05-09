package net.veskeli.nightrunner.SpellSystem;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
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
    public boolean castSpell(Level level, Player player, InteractionHand hand) {
        return false;
    }

    @Override
    public void onCast() {

    }

    @Override
    public ResourceLocation getSpellTexture() {
        return SPELL_TEXTURE;
    }

}
