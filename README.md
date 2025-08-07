# quat's mods

Monorepo. An attempt at implementing ["project sanity"](https://notes.highlysuspect.agency/project-sanity.html).

## shape

documentation here got outdated. look in buildSrc or ask me

## status

~~contains no actual content mods lol~~ Contains the world's most complicated implementation of "rebind narrator" for 1.21.1 and 1.21.5 and crowmap.

* Need some crossroaded source-sets in :vanilla
* No quatlib jar-in-jar on legacyforge or neoforge. (need to write my own task)
* No datagen or anything (hmm)

Refmap problems:

* Not doing anything with the extra mappings yet, i didn't know what they were for. One place they might show up is when doing `@Shadow`.
  * This means refmapping has to come BEFORE remapping so the extra mapping files can be fed into the process

Loom prints 10000 warnings about its inability to find refmap files when building a jar. This is expected; I had to forcibly tear out Loom's built-in mixin handling code, some later part of the code is unable to find those refmaps it's supposed to write. [This API](https://github.com/FabricMC/fabric-loom/blob/b37c4d3474fccd30f69beb25a20cc84da94f0574/src/main/java/net/fabricmc/loom/api/MixinExtensionAPI.java#L61) *looks* nice:

```java
for(LoaderMod mod : mods) {
  loom.mixin(mixinApi -> {
    mixinApi.add(mod.set, mod.modid + "." + loader + ".refmap.json");
    mixinApi.add(mod.getPerVersionSourceSet(ver), mod.modid + ".refmap.json");
  });
}
```

but for some reason it wasn't working for me. (the per-verison source set in `:vanilla`, destined for `modid.refmap.json` in the built jar, was just not getting created. Only the loader-specific refmap ended up in the jar.)