package net.veskeli.nightrunner.SpellSystem.Attachment;

import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.veskeli.nightrunner.SpellSystem.ModSpells;
import net.veskeli.nightrunner.SpellSystem.Spell;
import org.jetbrains.annotations.UnknownNullability;

public class SpellAttachment implements ISpellAttachment, INBTSerializable<CompoundTag> {

    private Spell[] spells = new Spell[6]; // Array to hold 6 spells
    private Spell selectedSpell;

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();

        Registry<Spell> spellRegistry = Minecraft.getInstance().level
                .registryAccess()
                .registry(ModSpells.SPELL_REGISTRY_KEY)
                .orElseThrow();

        // Serialize selected spell
        if (selectedSpell != null) {
            tag.putInt("SelectedSpell", spellRegistry.getId(selectedSpell));
        } else {
            tag.putInt("SelectedSpell", -1);
        }

        // Serialize spell array
        int[] spellIds = new int[spells.length];
        for (int i = 0; i < spells.length; i++) {
            spellIds[i] = spells[i] != null ? spellRegistry.getId(spells[i]) : -1;
        }
        tag.putIntArray("Spells", spellIds);

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag compoundTag) {
        Registry<Spell> spellRegistry = Minecraft.getInstance().level
                .registryAccess()
                .registry(ModSpells.SPELL_REGISTRY_KEY)
                .orElseThrow();

        // Deserialize selected spell
        if (compoundTag.contains("SelectedSpell")) {
            int spellId = compoundTag.getInt("SelectedSpell");
            selectedSpell = spellRegistry.byId(spellId);
        }

        // Deserialize spell array
        if (compoundTag.contains("Spells")) {
            int[] spellIds = compoundTag.getIntArray("Spells");
            for (int i = 0; i < spellIds.length && i < spells.length; i++) {
                spells[i] = spellIds[i] != -1 ? spellRegistry.byId(spellIds[i]) : null;
            }
        }
    }

    @Override
    public Spell getSelectedSpell() {
        return selectedSpell;
    }

    @Override
    public Spell getSpell(int index) {
        return spells[index];
    }

    @Override
    public void setSelectedSpell(Spell spell) {
        if (spell != null){
            this.selectedSpell = spell;
        }
        else
        {
            this.selectedSpell = null;
        }
    }

    @Override
    public void setSpell(int index, Spell spell) {
        spells[index] = spell;
    }
}
