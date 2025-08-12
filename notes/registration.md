"how many layers of abstraction are you on" "you are like a little baby watch this"

## `Latch<T>`

Like `Holder` from vanilla. Binds together a registry, an ID, and the object registered under that ID. At first the object is null. When the object becomes registered under that id, `Reg` "shuts the latch", and now the object can be retrieved with `get()`.

Think forge deferredregister's `RegistryObject<T>` system, but the supplier is kept somewhere else. This is because the suppliers actually live inside the datagen system.

## `Reg<T>`

The important method is `defer(Latch<T>, Supplier<T>)`. This function promises to *eventually* construct the supplier, register its result, and shut the latch with that object. It may be immediate, or it might happen later during a modloader registry event.

## `RegType<T>`

More-or-less `ResourceKey<Registry<T>>` but not Mojang's.

## `RegGetter`

Function from `RegType<T>` to the corresponding `Reg<T>`.

## `RegFacet`

Part of the `Facet` system for declaratively specifying things which should be done to the game. This one declares that an object should be constructed and registered at some point.

## `RegFacet.handle`

Takes a list of `RegFacet`s to register, finds each corresponding `Reg` (with the `RegGetter`) and calls `defer`.

# Shortcomings

* Probably fucked up the generics somewhere. I'm doing a lot of casting
* Annoying: `Latch<PackageMakerBlock>` isn't possible to put in the common source-set because packagemakerblock is from minecraft. Inconvenient to use `Latch<Block>` because then latch#get returns the upcasted type. Could keep two copies of all latches, one downcasted in the version-specific set ?