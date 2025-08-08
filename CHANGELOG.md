# Unreleased

This is a test release of mods created with my new modding pipeline. Please report any bugs/crashes. 

## Crowmap

* Rewrite the mod.
* There is now a config file for the tooltip behavior.
  * You can disable the "this tooltip will now hide itself" feature, or remove the tooltip entirely.
* 1.21.5: Remove a horrible mixin used to display the tooltip, in favor of a standard modloader tooltip event.
* *Fabric:* Now depends on Fabric API.

## Rebind Narrator

* Rewrite the mod.
* *Fabric 1.21.1+:* Integrate with "AMECS Reborn".
* *Fabric:* New config file.
  * Enable/disable mod integrations. 
  * If AMECS is not installed, configure whether you're required to hold CTRL to activate the narrator keybind.

This isn't strictly needed on Neoforge, because you've always been able to configure key modifiers through the Neoforge keybindings screen. 