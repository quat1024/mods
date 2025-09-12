# "Gen system"

A leaning tower of abstraction, encompassing "registering things to the game" and "performing datagen"

From bottom-to-top:

## Facet

Represents an atom of work.

* `RegFacet(Latch latch, Supplier<T> thing)` means "register this object under this registry" (`Latch` is a pair of registry and id, see `registration.md`)
* `LangFacet(file, key, value)` means "add this lang key/value pair to that language file"
* `DispenserBehaviorFacet(Latch<? extends Item>, DispenseItemBehavior)` means "register this dispenser behavior for that item"
* `TagFacet(TagType, tag, value, optional)` means "add this thing into that tag"

and so on.

### Handling a facet

Each facet (by convention) has a `handle` method, which takes a list of that facet + anything else it needs to carry out the described action

## FacetBuilder

A nicer domain-specific language to build one or more `Facet`s.

* Some, like `RegFacetBuilder` and `DispenserBehaviorFacetBuilder`, are basically just a wrapper around the corresponding facet's constructor with a fluent API and nullchecks
* `LangFacet` has convenience methods to set the lang key to a block's, item's, or subtitle's untranslated name
* `TagFacet` has a bunch of convenience methods too for commonly-used tags (eg. mineability tags)

`SoundEventFacetBuilder` in particular fans out in a lot of different directions:

* it builds a `RegFacet` to register the sound event with the game
* it builds a `LangFacet` to add the subtitle language keys
* it builds a `SoundEventFacet` to add the `sounds.json` entry (with a nice domain-specific language for building that out)

Every time you add a sound you need to add a sounds.json, and add a subtitle, and register the sound event. Makes sense to configure it all in one place.

## Gen

Represents an even higher level of abstraction, on the scale of "everything related to a particular piece of content" and up. The `PackageMakerGen` creates facetbuilders for everything related to the package crafter: register the block/item/menu, add relevant language keys and sound events, and so on.

Every time you want to make a certain piece of content you'll need to add a block, item, langauge key, whatever, so the philosophy is that it makes sense to co-locate all of that instead of spreading it across `MyModBlocks` `MyModItems` `MyModLanguageGenerator` etc

Gets `Gen.Ctx` as an argument, which keeps a list of `FacetBuilder`s and has a nice dsl to create them

It also accepts `Consumer<Gen>`, for fanning out into more gens; the very top-level is `PackagesGen` which fans out into `PackageGen`, `PackageMakerGen`, and `StickySyrupGen` to register each piece of content. I don't have any of these at the moment, but another use of gen fanout is stamping out gobs of prefabricated content (sixteen-color variants, stairs slabs walls, woody blocks)
