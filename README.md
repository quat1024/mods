# quat's mods

Monorepo. An attempt at implementing ["project sanity"](https://notes.highlysuspect.agency/project-sanity.html).

## shape

the guiding principle is "can we do more things with Gradle source-sets rather than going straight to subprojects".

* project `:vanilla`, source-set `main`, contains code included in every mod.
* project `:vanilla`, source-set `minecraft1_21_1`, contains code written against minecraft 1.21.1 and included in all minecraft 1.21.1 mods
* project `:vanilla` exposes a consumable configuration `minecraft1_21_1ApiElements` which contains the contents of `main` plus the contents of `minecraft1_21_1`

then there are "loader/version pair"-specific projects. in these, source-set `main` contains code included in every mod for that loader/version, and each additional source-set represents one mod; these are what ultimately get published to curseforge or whatever

todo: push things down. `:vanilla` should also contain mod source-sets, basically each *source-set* should contain the same things you'd put in the `:Xplat` *subproject* in the multiloader world. then focus on exposing `packages1_20_1ApiElements` from `:vanilla` which would have vanilla main, vanilla 1.20.1, and packages 1.20.1 vanilla

## status

contains no actual mods lol, just running short on time today.

Neoforge run configs proly broken, i doubt the :vanilla project is getting on the classpath