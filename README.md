# quat's mods

Monorepo. An attempt at implementing ["project sanity"](https://notes.highlysuspect.agency/project-sanity.html).

## shape

the guiding principle is "can we do more things with Gradle source-sets, rather than going straight to subprojects?"

* project `:vanilla`, source-set `modAnyVersionAny`, contains code included in every mod
* project `:vanilla`, source-set `modAnyVersion1_21_1`, contains code written against vanilla minecraft 1.21.1 and included in all minecraft 1.21.1 mods
* project `:vanilla`, source-set `modTestmodVersionAny`, contains code shared across all minecraft versions of `testmod`
* project `:vanilla`, source-set `modTestmodVersion1_21_1` contains code for `testmod` against vanilla minecraft 1.21.1
* each of these expose a consumable configuration `xxxxElements` which contains those contents packaged as a thinjar

everything is created with `setupMc` and `setupMod` calls in `:vanilla/build.gradle`

then there are "loader/version pair"-specific projects. in these:

* the `main` source-set contains code included in every mod for that loader/version
* the source-sets named after mods contain code for specifically that mod/loader/version triple (the lowest layer!)
* we grab all four of the `modXxxVersionY_yy_y` source-sets from `:vanilla` + the `main` source-set and *shade* all five source-sets into the published jar 
  * todo: currently i just splat them in with `zipTree`, but shading is the goal though
  * why shade? because otherwise the five `main` + `modXxxVersionY_yy_y` source sets need to get packaged as a `quatlib-(version)-(loader).jar` to download. i don't wanna manage that, you don't wanna manage that. shading them in ensures the mod contains a snapshot of all my helper code, and you won't run into a library going out-of-sync with the rest of the mod
  * but it does mean that dev-time and runtime handle these files differently and it could be a problem... i'll try to ban `static`. or maybe i will need a quatlib after all

because shading doesn't happen at dev-time and neoforge is picky about split packages, a `quatlib` actually *does* exist at development-time, just to hold all the classes which don't change across separate mods

## status

contains no actual content mods lol