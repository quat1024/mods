# Unreleased

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
* Remove the annoying honey particles from sticky packages. Sticky packages now can be distinguied by "sticky" text that appears when shifting.
  * It is behind a client config option, if you want them back for some reason...
* Stickiness might actually work on Forge!!!!! (lmao) it wasn't being enforced through the `IItemHandler`. Probably needs more testing
* Slightly less willing to be placed vertically. You have to look up/down a bit farther.
* On 1.20.1, migrate off of a deprecated `fabric-api` method for registering custom models.
