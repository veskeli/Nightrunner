package net.veskeli.nightrunner.entity.modifiers;
import java.util.List;
/**
 * One weighted stat preset for a mob type.
 */
public record MobPreset(String id, int weight, List<IMobModifier> modifiers) {
}