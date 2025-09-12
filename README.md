# quat's mods *(Season 2)*

This monorepo contains the source for all of my *Season 2* mods. They can be identified by their datestamped versions (a version number like `2025.08.10` for a mod released on August 10, 2025) and a dependency on `modder_name_lib`. If it has a much smaller version number, it's a *Season 1* mod.

* *Modder Name Lib* (fabric/forge 1.20.1, fabric/neoforge 1.21.1, fabric/neoforge 1.21.5)
  * Contains code shared between all of the *Season 2* mods.
  * You probably don't need to download it separately; on supported platforms it is shipped alongside each mod with the modloader's jar-in-jar system.
* *Rebind Narrator* (fabric/forge 1.20.1, fabric/neoforge 1.21.1, fabric/neoforge 1.21.5)
  * Change the narrator keybind.
  * [Curseforge](https://www.curseforge.com/minecraft/mc-mods/rebind-narrator), [Modrinth](https://modrinth.com/mod/rebind-narrator).
  * *Season 1* codebase: [quat1024/RebindNarrator](https://github.com/quat1024/RebindNarrator)
* *Crowmap* (fabric/forge 1.20.1, fabric/neoforge 1.21.1, fabric/neoforge 1.21.5)
  * Maps update anywhere in your inventory. 
  * [Curseforge](https://www.curseforge.com/minecraft/mc-mods/crowmap), [Modrinth](https://modrinth.com/mod/crowmap) .
  * *Season 1* codebase: [quat1024/Crowmap](https://github.com/quat1024/Crowmap)
* *Packages* (fabric/forge 1.20.1, fabric/neoforge 1.21.1)
  * 512 items is all you need. The underpowered barrels mod.
  * [Curseforge](https://www.curseforge.com/minecraft/mc-mods/packages), [Modrinth](https://modrinth.com/mod/packages)
  * *Season 1* codebase: [quat1024/packages](https://github.com/quat1024/packages)

~~(This is a heinously complicated gradle house-of-cards, so I'm starting with the simple ones.)~~ Packages is pretty big ngl

## Why

I called it ["project sanity"](https://notes.highlysuspect.agency/project-sanity.html). I was getting very annoyed with Minecraft modding largely because of the porting workload, the code duplication, the long wait-times when switching between projects, and the frictions involved in scaffolding a new project (even a small throwaway project). So this repo (hopefully!) allows me to work on mods without the things that burn me out.

## How

It's multi-version development, multi-mod development, *and* [multi-loader](https://github.com/jaredlll08/multiloader-template) development in the same repo.

The list of mods and supported Minecraft versions for each mod is defined in `vanilla/build.gradle`. Some common variables can be set from there too. A version-independent source set is created for each mod (`modidAny`), and for each supported version a version-specific source set is created (`modid1_21_1`). The version-dependent set can compile against the version-independent set, and unless `quatlib = false` in build.gradle, it can additionally compile against the corresponding `modderNameLib___` sets. Minecraft classes are provided with a builtin copy of [`minivan`](https://github.com/CrackedPolishedBlackstoneBricksMC/minivan). See `VanillaSetupPlugin` in `buildSrc`.

Also in the vanilla buildscript, the intersection of all minecraft versions is supplied with [`crossroad`](https://github.com/CrackedPolishedBlackstoneBricksMC/crossroad). This is a little janky. In the future

`:vanilla` emcompasses *all* code which doesn't depend on a modloader in the same Gradle subproject, specifically and only because modloader ecosystem plugins are not as flexible; you can't install two different versions of neoforge in the same gradle project (and I don't want to know what happens if you try to apply Loom as well). Those loader subprojects depend on the artifacts built in `:vanilla` and add more classes of their own. A `modidSplat` configuration contains all items from `:vanilla` which should be copied-and-pasted unchanged into the unmapped jar. Mapping is skipped on Neoforge because it's not needed, everything else feeds the jar through the remapping implementation provided by the ecosystem plugin, although mixin refmaps are manually compiled (again, because loader-specific gradle plugins are not flexible enough and I had to reimplement it). See `AbstractLoaderSetupPlugin` in `buildSrc`.

In the top-level `build.gradle`, the global version number is set based off the current date. Plugin versions are defined in `buildSrc/build.gradle`.

## status

~~contains no actual content mods lol~~ ~~Contains the world's most complicated implementation of "rebind narrator" and crowmap.~~ Contains packages too, and I'm starting to push in 

Wishlist:

* Add more things to the [datagen system](https://notes.highlysuspect.agency/another-datagen.html)
* Make moddernamelib less "special" throughout the ecosystem. E.g. i should be able to make a second quatlib for my mods on very old versions
* Automated publishing

## Release process

1. `./task.sh build` (or just `./gradlew build`)
   * compile everything
2. `./task.sh collect`
   * gets the changelog and gets all built jars out of `whatever-1.21.1/build/libs/`, and puts them in `./collect`, the staging area
3. `./task.sh upload` (or just `./gradlew :uploader:run`)
   * publish the mods to curseforge and modrinth (WIP)
   * requires some publishing secrets in secrets.txt (also WIP, should be thru env variables tbh...)
4`./task.sh mktag`
  * creates a Git tag with the current date
  * remember to upload it with `git push --tags`

## Note

Loom prints 10000 warnings about its inability to find refmap files when building a jar. This is expected; I had to forcibly tear out Loom's built-in mixin handling code, some later part of the code is unable to find those refmaps it's supposed to write.

<details><summary>Why rip out Loom's mixin code?</summary>

[This API](https://github.com/FabricMC/fabric-loom/blob/b37c4d3474fccd30f69beb25a20cc84da94f0574/src/main/java/net/fabricmc/loom/api/MixinExtensionAPI.java#L61) *looks* nice:

```java
for(LoaderMod mod : mods) {
  loom.mixin(mixinApi -> {
    mixinApi.add(mod.set, mod.modid + "." + loader + ".refmap.json");
    mixinApi.add(mod.getPerVersionSourceSet(ver), mod.modid + ".refmap.json");
  });
}
```

but for some reason it wasn't working for me. (the per-verison source set in `:vanilla`, destined for `modid.refmap.json` in the built jar, was just not getting created. Only the loader-specific refmap ended up in the jar.)

</details>