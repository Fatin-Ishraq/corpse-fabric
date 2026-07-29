# Changelog

## Repository review preparation

- Matched the original project's root naming and version-branch conventions.
- Added upstream provenance/adoption notes, issue forms, and GitHub Actions verification.
- Prepared the source as an explicitly unofficial public review repository.

## 0.1.1+1.21.1 - death history interface parity

- Restyle death history with the original mod's light-gray Minecraft interface palette.
- Align the navigation row with consistent panel padding and separate the Done button.
- Render information text without shadows for sharper readability.
- Keep panel contents out of Minecraft's blur pass so the interface remains sharp.
- Recreate the palette programmatically without bundling upstream textures or assets.

## 0.1.0+1.21.1 - Fabric port

- Port the approved 1.20.1 gameplay, face-up corpse pose, equipment rendering, screens, and configuration to Minecraft 1.21.1 and Java 21.
- Migrate networking and extended screen handlers to typed 1.21.1 payload and opening-data codecs.
- Migrate entity synchronization, SavedData, and item serialization to registry-aware 1.21.1 APIs.
- Preserve Curse of Vanishing behavior through enchantment effect components and add dedicated GameTest coverage.
- Pass all eight required GameTests and user-confirmed in-game testing.

## 0.1.2+1.20.1 - corpse pose correction

- Rotate corpses around the X axis so they rest on their back or face instead of a shoulder.
- Spawn corpses face-up by default while preserving the optional face-down configuration.
- Apply a small vertical clearance offset to prevent the body from clipping into the ground.

## 0.1.1+1.20.1 - armor rendering fix

- Render stored helmet, chestplate, leggings, and boots on the corpse.
- Render the item from the player's selected hotbar slot and the offhand item.
- Synchronize equipment appearance to clients and clear visuals as items are recovered.
- Use matching vanilla armor geometry during both player-skin and skeleton stages.
- Persist the selected hotbar slot and verify equipment mapping with a seventh GameTest.

## 0.1.0+1.20.1 — private test build

- Initial Fabric implementation for Minecraft 1.20.1.
- Persistent corpse storage, player/skeleton renderer, access control, transfer UI, death history, command/key access, configuration, and dedicated-server separation.
- Six required Fabric GameTests, including the real `ServerPlayer.die` mixin path.
- Written permission and official binary-release licensing remain pending.
