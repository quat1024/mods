The changelog format is somewhat specific (see ChangelogFragment from the uploader)

* first h1 must be exactly `# Unreleased`
* anything written after that h1 but before the h2 will get included in the changelog of all mods
* h2's must correspond exactly to the name of a mod

# Unreleased

* In-game version number is now zero-padded, just like it is on modrinth. [GH#1](https://github.com/quat1024/mods/issues/1) 

## Modder Name Lib

* (1.20.1) Fix Fabric 1.20.1 mods crashing on startup when using Java 17. Sorry about that. [GH#7](https://github.com/quat1024/mods/issues/7)
* (1.21.1) Include a small amount of code for working with "retexturable models".

## Packages

* (1.20.1) Fix a scenario where if a mod forgets to call `Package#canPlaceItem`, it can lead to a dupe bug. It will now void the offending items instead (there isn't much else I can do). [GH#8](https://github.com/quat1024/mods/issues/8)
* (1.21.1) Restructure the package and package-crafter retexturing logic. It's a lot simpler now (and uses some of the new moddernamelib code).
  * This will come to 1.20.1 at a later time

# v2025.08.21

This is a test release of mods created with my new modding pipeline. Please report any bugs/crashes. 

## Crowmap

* Rewrite the mod.
* There is now a config file for the tooltip behavior.
  * If you don't like the tooltip, you can turn it off. 
  * You can disable the "this tooltip will now hide itself" feature.
* 1.21.5: Remove a horrible mixin used to display the tooltip, in favor of a standard modloader tooltip event.
* *Fabric:* Now depends on Fabric API.

## Rebind Narrator

* Rewrite the mod.
* *Fabric 1.21.1+:* Integrate with "AMECS Reborn".
* *Fabric:* New config file.
  * Enable/disable mod integrations. 
  * If AMECS is not installed, configure whether you're required to hold CTRL to activate the narrator keybind.

This isn't strictly needed on Neoforge, because you've always been able to configure key modifiers through the Neoforge keybindings screen.

## Packages

* Now available for 1.21.1 Fabric as well as 1.20.1.
* Now available for 1.21.1 Neoforge as well as Forge 1.20.1.
* New item: *Sticky Syrup*. Apply it to a package to make it *sticky*. Sticky packages remember their item even when they are empty.
  * This replaces the old system of packages becoming sticky when they are next to a slime/honey block. Not many people knew about it!
  * The old system is available behind a config option.
* Tweak interactions with regard to sticky packages. (For example, you can insert items into an empty, sticky package, even if they aren't items from your hand.)
* Remove the annoying honey particles from sticky packages. Sticky packages can now be distinguied by "sticky" text that appears when shifting.
  * It is behind a client config option, if you want them back for some reason...
* Stickiness might actually work on Forge!!!!! (lmao) it wasn't being enforced through the `IItemHandler`.
* Slightly less willing to be placed vertically. You have to look up/down a bit farther.
* Fix an issue where items which stacked to 16 could be deleted using Packages inventory interactions.
* On Fabric, a nonnull `RenderMaterial` is passed when working with fabric-api, possibly fixing some clientside mod-compat issues.
* On 1.20.1, migrate off of a deprecated `fabric-api` method for registering custom models.
* On 1.21.1, `PackageBlockEntity` no longer implements vanilla `Container`! Instead, modloader-specific inventory mechanisms are used.
  * On NeoForge, the Package exposes an `ItemHandler` capability. On Fabric, the Package exposes an `ItemStorage` API. 
  * This was a rather large change so I'd like to test it before backporting it to 1.20.1.
* On 1.21.1, the Package *item* also exposes these modloader-specific inventories.
  * This means other mods which can, e.g., pull blocks out of shulker boxes in your inventory, may be able to pull items out of Packages too.
  * This feature needs more testing.

On NeoForge, enable the "NeoForge Light Pipeline" in `Mods` -> `NeoForge` -> `Client settings` -> `NeoForge Light Pipeline`. This will improve the apperance of the Package Crafter.

On Fabric 1.20.1, if you have Sodium remember to install Indium. No longer necessary as of Sodium for 1.21.1.
