# In-game test checklist - Fabric 26.2

Use a separate backup world for this test build. Install Fabric API and this mod on both client and server when testing multiplayer.

1. Die with items in the hotbar, main inventory, armor, and offhand. Confirm no duplicate item entities appear and the corpse contains every non-vanishing item.
2. Wear a helmet, chestplate, leggings, and boots; hold an item in the selected hotbar slot and another in the offhand. Confirm the corpse visibly renders all six pieces.
3. Remove the visible equipment one piece at a time. Confirm each armor/hand visual disappears immediately while untouched equipment remains visible.
4. Right-click the corpse as its owner. Try individual retrieval, shift-click, and **Transfer Items**. Confirm armor/offhand return to their original slots when those slots are free.
5. Put an item with Curse of Vanishing in the inventory, die, and confirm it is absent from the corpse.
6. Enable `keepInventory`, die, and confirm no corpse is created and the inventory stays on the player.
7. Restart the world/server while a filled, equipped corpse exists. Confirm its owner, appearance, selected held item, location, and inventory survive.
8. Test a second non-operator player. Before skeleton conversion they should see "This corpse is protected." Operators should still be able to open it.
9. Temporarily set `skeletonTicks` to a small value, restart, and confirm the corpse changes to a vanilla skeleton while still rendering its armor and following `skeletonAccessibleByEveryone`.
10. Empty a corpse and confirm it disappears after `emptyDespawnTicks` (default 600 ticks / 30 seconds).
11. Press `U` and run `/deathhistory`. Check the time, dimension, coordinates, death message, Items view, and copied Location command.
12. In survival, historical items must be read-only. In creative, verify **Recover Copy** adds copies without changing the existing live corpse.
13. Test deaths in water, lava, the Nether, the End, and near the bottom of the world. The corpse should remain present and accessible.
14. Test once in single-player and once on a dedicated Fabric server; watch `latest.log` for errors mentioning `corpse`.

For armor coverage, try normal iron armor, dyed leather armor, enchanted armor, armor trims, a carved pumpkin, and any modded armor you expect to support.

If something fails, keep `logs/latest.log` plus the generated `config/corpse.json`, and note the exact death location/dimension and whether other inventory/death mods were installed.
