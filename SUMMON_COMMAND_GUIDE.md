# Mob Summon Command with Presets

## Usage

Spawn a mob with a specific preset using the `/nr summon` command:

```
/nr summon <mob_id> <x> <y> <z> <preset_id>
```

### Command Syntax

- **`mob_id`**: The Minecraft namespace ID of the mob (e.g., `minecraft:creeper`, `minecraft:spider`)
  - Supports tab-completion for all available mob types
- **`x y z`**: Coordinate position where the mob will spawn
  - Supports relative coordinates (e.g., `~ ~1 ~`)
- **`preset_id`**: The preset configuration to apply to the mob
  - Supports tab-completion based on the selected mob type

### Available Presets

#### Zombies
- `normal` - Standard zombie
- `tanky` - Increased health, reduced damage, slower movement
- `berserker` - Reduced health, faster movement, scaled smaller

#### Skeletons
- `default` - Standard skeleton

#### Wither Skeletons
- `brute` - Stronger melee variant
- `juggernaut` - Heavily armored, large variant

#### Creepers
- `Default` - Standard creeper
- `Lite` - Small, weak explosion
- `Nuke` - Large explosion, slow movement
- `splitter` - Splits into smaller creeperswhen approaching players

#### Spiders
- `default` - Standard spider
- `broodmother` - Large spider that spawns baby spiders when killed

#### Illagers (Pillager, Vindicator, Evoker)
- `standard` - Standard variant
- `agile` - Fast, smaller variant
- `bruiser` - Strong, larger variant

## Examples

### Spawn a normal zombie at current position
```
/nr summon minecraft:zombie ~ ~ ~ normal
```

### Spawn a splitter creeper nearby
```
/nr summon minecraft:creeper ~5 ~2 ~5 splitter
```

### Spawn a broodmother spider
```
/nr summon minecraft:spider ~ ~ ~ broodmother
```

### Spawn a nuke creeper at specific coordinates
```
/nr summon minecraft:creeper 100 64 200 Nuke
```

## Requirements

- Requires operator/permission level 2
- Must be run by a player on the server
- Only works with mob types that have preset configurations

