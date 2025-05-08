package net.veskeli.nightrunner.SpellSystem.Attachment;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.veskeli.nightrunner.SpellSystem.Spell;
import org.jetbrains.annotations.UnknownNullability;

public class SpellAttachment implements ISpellAttachment, INBTSerializable<CompoundTag> {

    private Spell[] spells = new Spell[6]; // Array to hold 6 spells
    private Spell selectedSpell;

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return null;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {

    }

    @Override
    public Spell getSelectedSpell() {
        return selectedSpell;
    }

    @Override
    public Spell getSpell(int index) {
        return spells[index];
    }
}
