package net.veskeli.nightrunner.entity.modifiers;

import net.minecraft.world.entity.Mob;

/**
 * Implement this interface for each stat tweak you want to apply to a mob on spawn.
 */
public interface IMobModifier {
    void apply(Mob mob);
}
