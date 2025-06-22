# quat's mods

Monorepo. An attempt at implementing ["project sanity"](https://notes.highlysuspect.agency/project-sanity.html).

## shape

the guiding principle is "can we do more things with Gradle source-sets, rather than going straight to subprojects?"

* project `:vanilla`, source-set `modAnyVersionAny`, contains code included in every mod
  * it can see no libraries (but i might add an old version of GSON or something, since it's handy and every minecraft version uses it)
* project `:vanilla`, source-set `modAnyVersion1_21_1`, contains code written against vanilla minecraft 1.21.1 and included in all minecraft 1.21.1 mods
  * it can see `modAnyVersionAny`
* project `:vanilla`, source-set `modTestmod1VersionAny`, contains code shared across all minecraft versions of `testmod1`
  * it can see `modAnyVersionAny` 
* project `:vanilla`, source-set `modTestmod1Version1_21_1` contains code for `testmod1` against vanilla minecraft 1.21.1
  * it can see `modAnyVersionAny`, `modTestmod1VersionAny`, and `modAnyVersion1_21_1` 
* each of these `modXxxxVersionYyyy` projects exposes a consumable configuration `modXxxxVersionYyyyElements`, which contains only the classes in that specific project

then there are "loader/version pair"-specific projects. in these:

* project `:fabric-1.21.1`, source-set `main`, contains code for all of the fabric 1.21 mods
  * it can see `:vanilla` `modAnyVersionAny` and `modAnyVersion1_21_1`
* project `:fabric-1.21.1`, source-set `testmod1`, contains code for specifically `testmod1` on fabric 1.21. the lowest layer!
  * it can see `:vanilla` `modTestmod1VersionAny` and `modTestmod1Version1_21_1`, as well as everything `:fabric-1.21.1` `main` can see
* two types of fatjar are produced:
  * one builds the contents of `:vanilla` `modAnyVersionAny` + `modAnyVersion1_21_1` and `:fabric-1.21.1` `main`, this is called "quatlib"
  * the other builds a specific mod: containing `:vanilla` `modTestmod1VersionAny` + `modTestmod1Version1_21_1` and `:fabric-1.21.1` `testmod`. this is distributed as the final jar
  * each mod jar-in-jars the corresponding quatlib

`:floader-only` depends on Fabric Loader and nothing else. All fabric `main` source-sets can see it and it gets included in the final fatjar. This isn't possible on neoforge since the modloader and mod runtime both differ between minecraft versions. Rip FML.

to add a dependency to a specific version of a specific mod in a loader-version-pair subproject, something like `testmod1Implementation` should work ok

Everything is scaffolded with a handful of small spaghetti plugins in `buildSrc`.

also "quatlib" is renamed and published as "ModderNameLib" as a #funny and #ironic nod to moddernamelibs everywhere.

## status

~~contains no actual content mods lol~~ Contains the world's most complicated implementation of "rebind narrator" for 1.21.1 and 1.21.5. Doesn't really use the code-sharing system yet, this mod is very small.

Neoforge doesn't jarjar `quatlib` atm. It should. They have a weird jarjar system that is less convenient to use outside the established channels, might have to write my own task for it

Currently source-sets like `:neo-1.21.5 testmod1` *can* see source-sets like `:vanilla modTestmod1Version1_20_1` from the IDE. Compiling seems fine; they *correctly* aren't on each other's classpaths, though. This is clearly bad because they are for different minecraft versions... I've only isolated this as far as "adding `modSplat` to the `implementation` configuration" so maybe `modSplat` has too many artifacts in it?? Or an intellij bug? Weird

no datagen or anything. should be a "simple" crosscutting concern

I would like a "no quatlib" option i can flick on per-mod. Rebind narrator is small enough to not need quatlib, especially on neoforge (on fabric i might want a config file to toggle on and off the AMECS integration, or to fiddle with modifier stuff, since i don't think that is available through the vanilla gui)

Todo: needs ForgeGradle support for 1.20.1 and maybe below.