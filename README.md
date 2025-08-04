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

* The `"refmap"` key isn't automatically added to mixin jsons, would be nice to do. Except sometimes it does work?
* Odd behavior with more than one mixin json (as i found with the test mixin i slapped in rebindnarrator fabric 1.20.1)
* Not doing anything with the extra mappings yet, i didn't know what they were for. One place they might show up is when doing `@Shadow`.
  * also implies refmapping has to happen before remapping