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
* todo, just deleted some documentation from this because it was wrong but i'm too tired to write new docs

## status

contains no actual content mods lol

an experimental java-based precompiled script plugin exists in `buildSrc`, which will cut down on the amount of cut-paste to do when i fan this out to more loaders and minecraft versions. Boy it's really slick. I intend to move `vanilla/build.gradle` into there as well, but that's its own thing

The `processResources` situation isn't great. We need dozens of little processresources tasks, one for each source set. Inspiration from multiloader-template: https://github.com/jaredlll08/MultiLoader-Template/blob/2450032d7e14b296df24c519d072310199a23f75/buildSrc/src/main/groovy/multiloader-common.gradle#L85 (includes facilities to subst stuff like the mod name and description, which is clever...)

Neoforge doesn't jarjar `quatlib` atm. It should. They have a weird jarjar system that is less convenient to use outside the established channels, might have to write my own task for it

Currently source-sets like `:neo-1.21.5 modTestmodVersion1_21_5LoaderNeoforge` can see source-sets like `:vanilla modTestmodVersion1_20_1` from the IDE. Compiling seems fine and they can't see each other though. This is clearly bad because they are for different minecraft versions... I've only isolated this as far as "adding `modSplat` to the `implementation` configuration" so maybe `modSplat` has too many artifacts in it?? Or an intellij bug? Weird

no datagen or anything. should be a simple crosscutting concern