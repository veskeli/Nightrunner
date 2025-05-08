package net.veskeli.nightrunner.SpellSystem.Attachment;

import net.veskeli.nightrunner.SpellSystem.Spell;

public interface ISpellAttachment {
    Spell getSelectedSpell();
    Spell getSpell(int index);
    void setSelectedSpell(Spell spell);
    void setSpell(int index, Spell spell);
}
