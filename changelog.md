# Changelog

## Repository review preparation

- Matched the original project's root naming and version-branch conventions.
- Added upstream provenance/adoption notes, issue forms, and GitHub Actions verification.
- Prepared the source as an explicitly unofficial public review repository.

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
