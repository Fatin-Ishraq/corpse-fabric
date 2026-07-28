# Corpse for Fabric

A clean Fabric implementation of the corpse-on-death gameplay concept for Minecraft 1.20.1.
When a player dies, their non-vanishing inventory is moved into a persistent corpse instead of being dropped as item entities.

> This is a private test build. Public distribution, the final project name/branding, and licensing remain pending written permission from the original Corpse author. No upstream textures or other assets are included.

## Requirements

- Minecraft 1.20.1
- Fabric Loader 0.15.11 or newer
- Fabric API for Minecraft 1.20.1
- Java 17 or newer

Install Fabric API and the release JAR in the `mods` folder. On multiplayer servers, install both on the server and every connecting client.

## Implemented features

- Captures all 41 vanilla player inventory slots: hotbar, main inventory, armor, and offhand.
- Honors `keepInventory`; items with Curse of Vanishing are not stored.
- Prevents duplicate vanilla item drops after a corpse is created.
- Persistent corpse entity with owner UUID, player name, inventory, orientation, age, and skeleton state saved to world data.
- Player-skin corpse renderer with a vanilla skeleton stage; no copied upstream art.
- Owner-protected access by default, with operator bypass and optional public skeleton access.
- Right-click inventory with shift-click retrieval and a one-button transfer back to original slots where possible.
- Persistent death history with time, dimension, coordinates, death message, and inventory snapshot.
- `U` opens your death history. `/deathhistory` does the same; operators can use `/deathhistory <player>`.
- Creative players can recover a copy from a historical inventory record.
- Fire immunity, invulnerability, fixed position, no distance despawn, and minimum-world-height protection.
- Dedicated-server-safe client separation.

## Configuration

The first launch creates `config/corpse.json`:

```json
{
  "ownerOnlyAccess": true,
  "skeletonAccessibleByEveryone": true,
  "emptyDespawnTicks": 600,
  "fullDespawnTicks": -1,
  "skeletonTicks": 72000,
  "maxHistoryEntriesPerPlayer": 50,
  "spawnFaceDown": false
}
```

A despawn value of `-1` disables that despawn rule. Values are read at startup, so restart after editing the file.

## Build and verification

Use a Java 21 Gradle runtime; the produced mod targets Java 17 bytecode:

```powershell
.\gradlew.bat build
```

`build` also launches Fabric's GameTest server. The current suite verifies real `ServerPlayer.die` mixin capture, no duplicate drops, `keepInventory`, original-slot transfer, corpse NBT persistence, and skeleton timing. See [TESTING.md](TESTING.md) for the in-game checklist.

## Permission boundary

This repository is intentionally not assigned an open-source license yet. See [PERMISSION.md](PERMISSION.md). Do not publish or monetize this test build until the permission terms are recorded and the metadata is updated.
