package net.veskeli.nightrunner.ManaSystem;

public interface IMana {
    int getMana();
    void setMana(int mana);
    void setMaxMana(int maxMana);
    void addMana(int amount);
    void subtractMana(int amount);
    int getMaxMana();
    int getRegenCooldown();
    void setRegenCooldown(int regenCooldown);
    int getCurrentPenalty();
    void subtractPenalty(int amount);
    int getCurrentRecharge();

    // spell slots
    int getSpellAmount();
    void regenSpellSlots(int amount);
    void subtractSpellSlots(int amount);
    void setSpellLevel(int level);
    int getMaxSpellAmount();
}
