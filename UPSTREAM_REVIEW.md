# Upstream review notes

This document is intended to make review and possible adoption by the original Corpse maintainer straightforward.

## Provenance

- The port was implemented independently for Minecraft 1.20.1 and 1.21.1 against Fabric Loader and Fabric API.
- No Java source, textures, icons, translations, or binary artifacts were copied from `henkelmax/corpse`.
- Behavior was matched from the public feature description and normal in-game expectations.
- The official Fabric example-mod Gradle layout was used as build scaffolding.

## Repository layout

The root follows the original project's familiar Gradle structure:

- `build.gradle`, `gradle.properties`, `settings.gradle`, and the Gradle wrapper at the root;
- gameplay code and common resources under `src/main`;
- Fabric-only client entrypoints, screens, and rendering under `src/client` so dedicated servers do not load client classes;
- automated Minecraft tests under `src/gametest`;
- version branches such as `1.20.1` and `1.21.1`, with `master` representing the current review line.

## Implementation map

- `ServerPlayerDeathMixin` intercepts `ServerPlayer.die` before vanilla inventory drops.
- `CorpseDeathHandler` captures eligible inventory slots, records history, spawns the corpse, and clears vanilla inventory.
- `CorpseEntity` owns persistent storage, permissions, skeleton timing, and despawn/safety behavior.
- `CorpseMenu` and the client screens implement inventory recovery and death-history browsing.
- `DeathHistoryState` stores per-player history using overworld `SavedData`.
- `CorpseNetworking` carries history requests and item-view requests with server-side ownership checks.

## Validation evidence

`./gradlew build` runs seven required Fabric GameTests. They cover:

1. the real `ServerPlayer.die` mixin path;
2. capture without duplicate item entities;
3. the `keepInventory` rule;
4. transfer back to original inventory/armor slots;
5. owner, inventory, selected-slot, and equipment NBT serialization;
6. armor, hand, and offhand slot mapping/removal;
7. configured skeleton conversion.

Minecraft 1.20.1 completed client and dedicated-server startup smoke tests. Minecraft 1.21.1 passed eight automated GameTests and user-confirmed in-game testing. Manual coverage is tracked in `TESTING.md`.

## Adoption options

The code can remain as a separate Fabric repository, move into an official Fabric branch, or be adapted into a shared multi-loader architecture. Namespace, metadata, branding, license headers, release coordinates, and author attribution should be changed to whatever the original maintainer approves before an official release.
