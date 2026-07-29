# Corpse - Fabric port proposal

[![Build](https://github.com/Fatin-Ishraq/corpse-fabric/actions/workflows/build.yml/badge.svg)](https://github.com/Fatin-Ishraq/corpse-fabric/actions/workflows/build.yml)

This repository contains an independent Fabric implementation of the gameplay provided by [henkelmax/corpse](https://github.com/henkelmax/corpse). It is being prepared for review by the original creator, Max Henkel, with the hope that the Fabric build can be accepted, merged, or published through the official project.

> **Upstream review status:** This repository is an unofficial technical preview. It is not endorsed by, maintained by, or published on behalf of Max Henkel. Marketplace publication, final branding, and licensing remain pending the original creator's written approval.

No upstream Java source, textures, icons, or other assets are bundled. The implementation and interface assets in this repository were written specifically for Fabric.

## Links

- [Original Corpse repository](https://github.com/henkelmax/corpse)
- [Official CurseForge project](https://www.curseforge.com/minecraft/mc-mods/corpse)
- [Permission and publication status](PERMISSION.md)
- [Upstream review notes](UPSTREAM_REVIEW.md)
- [In-game test checklist](TESTING.md)

## Supported versions

| Branch | Minecraft | Loader | Status |
| --- | --- | --- | --- |
| `master` | 1.21.1 | Fabric | Current review build |
| `1.21.1` | 1.21.1 | Fabric | Preserved version branch |
| `1.20.1` | 1.20.1 | Fabric | Preserved version branch |

Future Minecraft versions will use version-named branches, following the structure of the original project. Superseded branches can later move under `outdated/`.

## Requirements

- Minecraft 1.21.1
- Fabric Loader 0.15.11 or newer
- Fabric API for Minecraft 1.21.1
- Java 21 or newer

Install Fabric API and the built JAR in the `mods` folder. On multiplayer servers, install both on the server and every connecting client.

## Features

- Captures all 41 vanilla player inventory slots: hotbar, main inventory, armor, and offhand.
- Honors `keepInventory`; items with Curse of Vanishing are not stored.
- Prevents duplicate vanilla item drops after a corpse is created.
- Persists corpse ownership, inventory, orientation, age, and skeleton state in world data.
- Uses the player's skin and a vanilla skeleton model without copied project artwork.
- Renders synchronized armor, the selected hotbar item, and offhand equipment; visuals update as items are recovered.
- Owner-protected access, operator bypass, and configurable public skeleton access.
- Right-click inventory, shift-click retrieval, and one-button transfer to original slots where possible.
- Persistent death history with time, dimension, coordinates, death message, and inventory snapshot.
- `U` or `/deathhistory` opens the player's history; operators can use `/deathhistory <player>`.
- Creative-mode recovery copies from historical inventory records.
- Fire immunity, invulnerability, no distance despawn, and minimum-world-height protection.
- Client/server source separation for dedicated-server compatibility.

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

A despawn value of `-1` disables that rule. Restart the game or server after editing the file.

## Building

Use a Java 21 Gradle runtime; the produced mod targets Java 21 bytecode:

```powershell
.\gradlew.bat build
```

The distributable JAR is written to `build/libs`. The build also launches Fabric's GameTest server. Eight required tests cover the real `ServerPlayer.die` mixin path, duplicate-drop prevention, Curse of Vanishing, `keepInventory`, original-slot transfer, corpse NBT persistence, equipment-slot synchronization, and skeleton timing.

For a development client:

```powershell
.\gradlew.bat runClient
```

## License and contributions

No open-source license has been assigned while upstream permission is pending. The public source is available for review, but publication here does not grant permission to redistribute, relicense, monetize, or claim this work as an official Corpse release. See [PERMISSION.md](PERMISSION.md).
