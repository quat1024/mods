"how many layers of abstraction are you on" "you are like a little baby watch this"

## `Latch<T>`

Binds together:

* a registry
* an ID
* the object registered in that registry under that ID.

At first the object is null. When the object becomes registered under that id, `Reg` "shuts the latch"; now the object can be retrieved with `get()`.

Analogies: It's very close to `Holder` from vanilla. It's also like forge deferredregister's `RegistryObject<T>`, but the supplier is kept somewhere else (the objects are actually created inside the datagen system)

## `Reg<T>`

The important method is `defer(Latch<T>, Supplier<T>)`. This function promises to *eventually* construct the supplier, register its result, and shut the latch with that object.

It may be immediate, or it might happen later during a modloader registry event. For example on Forge it is not safe to construct `Block`s before the block registry event, so forge only shuts block latches after that event occurs.

## `RegType<T>`

More-or-less `ResourceKey<Registry<T>>` but not Mojang's.

## `RegistryGetter`

Function from `RegType<T>` to the corresponding `Reg<T>`.

## `RegFacet`

Part of the `Facet` system (for declaratively specifying things which should be done to the game).

This one declares that an object should be constructed and registered at some point. And yeah, handling the facet calls `Reg#defer`.

# Shortcomings

* Probably fucked up the generics somewhere. I'm doing a lot of casting
  * Addressed this by just type-erasing (RegFacet in particular is now fully erased). Janky.
* Annoying: `Latch<PackageMakerBlock>` isn't possible to put in the common source-set because packagemakerblock is from minecraft. Inconvenient to use `Latch<Block>` because then latch#get returns the upcasted type.
  * Could keep two copies of all latches, one downcasted in the version-specific set ?

## a global latch registry? (2025.09.12)

I want to solve the "hard to refer to latches from vanilla areas of the game" problem.

Part of the problem is that only one latch can exist for any particular registee?  There needs to be a "canonical" latch just due to how `Reg` is designed. more often than not that latch exists in version-specific code just because it's easier to use the latch from business logic.

Two ways around it

* redesign `Reg` to accept lists of latches. Then i'm worried about situations where an open and shut latch exist for the same object (definitely not good)
* Global state? If the only way to construct latches canonicalizes them somehow, then only one `Latch` object exists for everything. Multiple latches can be constructed for the same object

Also adding a `Latch#downcast` function which is just an unchecked `return this` cast
