package net.veskeli.nightrunner.entity.modifiers;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Holds permanent modifiers and weighted presets for one mob type.
 */
public final class MobPresetTable {
    private final List<IMobModifier> always;
    private final List<MobPreset> weightedPresets;
    private final int totalWeight;

    private MobPresetTable(List<IMobModifier> always, List<MobPreset> weightedPresets) {
        this.always = always;
        this.weightedPresets = weightedPresets;
        this.totalWeight = weightedPresets.stream().mapToInt(MobPreset::weight).sum();
    }

    public void applyAlways(Mob mob) {
        for (IMobModifier modifier : always) {
            modifier.apply(mob);
        }
    }

    public Optional<MobPreset> pickPreset(RandomSource random) {
        if (weightedPresets.isEmpty() || totalWeight <= 0) {
            return Optional.empty();
        }

        int roll = random.nextInt(totalWeight);
        int cumulative = 0;

        for (MobPreset preset : weightedPresets) {
            cumulative += preset.weight();
            if (roll < cumulative) {
                return Optional.of(preset);
            }
        }

        return Optional.empty();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final List<IMobModifier> always = new ArrayList<>();
        private final List<MobPreset> presets = new ArrayList<>();

        public Builder always(IMobModifier... modifiers) {
            always.addAll(Arrays.asList(modifiers));
            return this;
        }

        public Builder preset(String id, int weight, IMobModifier... modifiers) {
            if (weight <= 0) {
                return this;
            }

            presets.add(new MobPreset(id, weight, List.of(modifiers)));
            return this;
        }

        public MobPresetTable build() {
            return new MobPresetTable(
                    Collections.unmodifiableList(new ArrayList<>(always)),
                    Collections.unmodifiableList(new ArrayList<>(presets))
            );
        }
    }
}

