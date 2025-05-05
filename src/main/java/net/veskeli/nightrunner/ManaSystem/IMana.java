package net.veskeli.nightrunner.ManaSystem;

public interface IMana {
    int getMana();
    void setMana(int mana);
    void addMana(int amount);
    void subtractMana(int amount);
    int getMaxMana();
}
