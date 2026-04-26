package net.veskeli.nightrunner.commands.subcommands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.Vec3;
import net.veskeli.nightrunner.entity.modifiers.MobModifierRegistry;
import net.veskeli.nightrunner.entity.modifiers.MobPreset;
import net.veskeli.nightrunner.entity.modifiers.MobPresetTable;
import net.veskeli.nightrunner.entity.modifiers.MobSpawnEvents;

import java.util.List;
import java.util.Optional;

public class SummonCommand {
    private static final SimpleCommandExceptionType INVALID_MOB_ID = new SimpleCommandExceptionType(
            Component.literal("Unknown mob ID. Use 'minecraft:' namespace for vanilla mobs")
    );
    private static final SimpleCommandExceptionType INVALID_PRESET = new SimpleCommandExceptionType(
            Component.literal("Invalid preset for this mob type")
    );
    private static final SimpleCommandExceptionType SUMMON_FAILED = new SimpleCommandExceptionType(
            Component.literal("Failed to summon mob")
    );
    private static final SimpleCommandExceptionType NOT_MOB_ENTITY = new SimpleCommandExceptionType(
            Component.literal("Entity ID is valid but is not a mob")
    );
    private static final SimpleCommandExceptionType NO_PRESET_TABLE = new SimpleCommandExceptionType(
            Component.literal("This mob type has no preset modifiers")
    );

    /** Suggestions provider for mob IDs */
    public static final SuggestionProvider<CommandSourceStack> MOB_ID_SUGGESTIONS =
            (context, builder) -> {
                return SharedSuggestionProvider.suggestResource(
                        BuiltInRegistries.ENTITY_TYPE.stream()
                                .map(type -> BuiltInRegistries.ENTITY_TYPE.getKey(type))
                                .toList(),
                        builder
                );
            };

    public static int execute(CommandContext<CommandSourceStack> context,
                              ResourceLocation mobId,
                              Vec3 position,
                              String presetId) throws CommandSyntaxException {
        ServerLevel level = context.getSource().getLevel();

        // Resolve entity type from registry
        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(mobId).orElse(null);

        if (entityType == null) {
            throw INVALID_MOB_ID.create();
        }

        // Create entity
        Entity entity = entityType.create(level);
        if (entity == null) {
                throw SUMMON_FAILED.create();
        }

        if (!(entity instanceof Mob mob)) {
            throw NOT_MOB_ENTITY.create();
        }

        // Resolve table from the actual mob instance so family tables (e.g. illagers) are supported.
        MobPresetTable presetTable = MobModifierRegistry.getPresetTable(mob);
        if (presetTable == null) {
            throw NO_PRESET_TABLE.create();
        }

        // Position the mob
        mob.moveTo(position.x, position.y, position.z, mob.getYRot(), mob.getXRot());

        // Let vanilla initialize default equipment/AI data (e.g., skeleton bow), while preventing random NR preset roll.
        mob.addTag(MobSpawnEvents.NIGHTRUNNER_SKIP_MODIFIERS_TAG);
        mob.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(mob.blockPosition()),
                MobSpawnType.COMMAND,
                null
        );
        mob.removeTag(MobSpawnEvents.NIGHTRUNNER_SKIP_MODIFIERS_TAG);

        MobPreset preset = resolvePreset(presetTable, presetId).orElseThrow(INVALID_PRESET::create);

        // Apply explicit preset selected by command.
        presetTable.applyAlways(mob);
        for (var modifier : preset.modifiers()) {
            modifier.apply(mob);
        }

        mob.addTag(MobSpawnEvents.NIGHTRUNNER_PRESET_PREFIX + preset.id());
        mob.addTag(MobSpawnEvents.NIGHTRUNNER_TAG);
        mob.setCustomName(Component.nullToEmpty(preset.id()));

        // Spawn the entity
        level.addFreshEntity(mob);

        context.getSource().sendSuccess(
                () -> Component.literal("Summoned ")
                        .withStyle(ChatFormatting.GREEN)
                        .append(Component.literal(mobId.toString()).withStyle(ChatFormatting.AQUA))
                        .append(" with preset ")
                        .append(Component.literal(preset.id()).withStyle(ChatFormatting.GOLD))
                        .append(Component.literal(" at [" + (int)position.x + ", " + (int)position.y + ", " + (int)position.z + "]")
                                .withStyle(ChatFormatting.GRAY)),
                true
        );

        return 1;
    }

    /**
     * Provides preset ID suggestions for a given mob
     */
    public static List<String> getPresetsForMob(CommandSourceStack source, ResourceLocation mobId) {
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getOptional(mobId).orElse(null);
        if (type == null) {
            return List.of();
        }

        if (!(type.create(source.getLevel()) instanceof Mob mob)) {
            return List.of();
        }

        MobPresetTable table = MobModifierRegistry.getPresetTable(mob);
        if (table == null) {
            return List.of();
        }

        return table.presetIds();
    }

    private static Optional<MobPreset> resolvePreset(MobPresetTable table, String presetId) {
        var exact = table.findPreset(presetId);
        if (exact.isPresent()) {
            return exact;
        }

        return table.presetIds().stream()
                .filter(id -> id.equalsIgnoreCase(presetId))
                .findFirst()
                .flatMap(table::findPreset);
    }
}






