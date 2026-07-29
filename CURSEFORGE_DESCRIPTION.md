# Corpse: Unofficial Fabric Port

> This is an unofficial, independently written Fabric port of [Corpse by Max Henkel](https://www.curseforge.com/minecraft/mc-mods/corpse). It is not affiliated with or endorsed by Max Henkel.

When a player dies, this mod creates a corpse at the death location and stores the player's inventory inside it. Return to the corpse and right-click it to recover the items.

## Features

- Keeps the player's inventory inside a persistent corpse after death
- Displays the player's skin, armor, selected item, and offhand item on the corpse
- Protects corpses from fire, lava, the void, and normal distance despawning
- Returns items to their original inventory slots when possible
- Supports owner-only access, operator access, and configurable public access
- Turns old corpses into skeletons after one hour by default
- Provides a persistent death history screen
- Supports Fabric on multiple Minecraft versions


## Skeleton Stage

After 72,000 server ticks—about one hour at the normal 20 TPS—the corpse changes into a skeleton. This is a visual indication of its age and does not change the items stored inside it.

The **Transfer Items** button attempts to return recovered items to their original slots.

## Death History

Press `U` or run `/deathhistory` to view your previous deaths. Each entry records the time, dimension, coordinates, death message, and inventory snapshot.

Operators can view another player's history with:

```text
/deathhistory <player>
```

In Creative Mode, the **Items** button can be used to recover items from a historical inventory snapshot.

The **Location** button displays a teleport command containing the death coordinates and dimension.

## Configuration

The mod creates `config/corpse.json`. Available options include:

- Owner-only corpse access
- Public access after the corpse becomes a skeleton
- Empty and full corpse despawn times
- Skeleton conversion time
- Maximum death-history entries per player
- Face-up or face-down corpse pose

## Requirements

- [Fabric Loader](https://fabricmc.net/use/installer/)
- [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
- The build made for your exact Minecraft version

Install the mod and Fabric API on both the server and every connecting client.

## Links

- [Source code and issue tracker](https://github.com/Fatin-Ishraq/corpse-fabric)
- [Original Corpse project](https://www.curseforge.com/minecraft/mc-mods/corpse)
- [Original Corpse source repository](https://github.com/henkelmax/corpse)

## A Note to Max Henkel

Max, I tried to contact you directly and also reached out through members of your team to discuss this Fabric port, but I have not received a response. I am therefore making this independently written implementation available as an explicitly unofficial port so Fabric players can use it while its origin and ownership remain clear.

If you would prefer this project to be removed, please contact me and I will take it down promptly. My preferred outcome would be for you to accept, merge, or take ownership of the Fabric port so it can be maintained and published as part of the official Corpse project.
