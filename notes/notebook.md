# notebook

## fabric Packages item models

(also applies to Templates 2)

ModelLoadingPlugin's `modifyModelBeforeBake` event is kind of designed for wrapping existing models, rather than only for replacing them. Wrapping is the more flexible operation. For example Packages and Templates both do dynamic retexturing of an existing model; they *could* be implemented by letting vanilla load the base model and wrapping it in a custom unbakedmodel which performs the retexturing. (They're not implemented like that atm.)

If you want an entirely code-created model for an item, if the corresponding `assets/modid/models/item/xxx.json` file does not exist, vanilla will still print a warning about a missing model. The code-driven model replacement will then get the oppoertunity to wrap or replace the vanilla missing-texture cube.

To avoid the unsightly warning you can just create the file. The contents of that model don't matter if you are replacing the whole model anyway. I created a `modder_name_lib:block/dummy.json` model to use as the parent for all of these dummy models. (Of course `minecraft:block/block` works fine too but i wanted to be specific about my intentions)

## Mojang And The Fucking Letter `S`

* `data/modid/advancementS` -> `data/modid/advancement`
* `data/modid/recipeS` -> `data/modid/recipe`
* `data/modid/loot_tableS/blockS/` -> `data/modid/loot_table/blockS`

Yes you keep the `S` on `blocks` in the loot_table~~s~~ folder. Mojang needs something to do next update, after all.

## recipes

Of course it would take 5 seconds to make it backward compatible. But nah.

```json
"result": {
  "item": "packages:sticky_syrup"
}
```

to

```json
"result": {
  "id": "packages:sticky_syrup"
}
```

## Actually kind of nice things about components (1.21.1)

I still think the `Codec` class itself is tedious, TODO figure out if there's a way to manually write toNbt/fromNbt style functions lol. Part of the appeal is that the same system is used to write NBT and JSON though.

`StreamCodec` is a new class that has nothing to do with `Codec` really. First generic is the type of `ByteBuf` the stream codec uses to de/serialize the second generic. Fortunately you don't have to play codec games and can implement the interface directly.

### Block entity components

Basically an "implicit component" is a component that *you* save to NBT yourself. It doesn't use the component's codec system although of course you can use the codec to save and load. It also lives as a field in your block entity instead of living in the data component map. I feel like it's also kind of good for "mutable" components, e.g. a component representing "the contents of a chest", which can be frozen into an immutable representation when it's time to save the component to disk or to an item. I feel like we're in a `getStateFromMeta/getMetaFromState` period wrt block entity components... maybe "implicit components" are a system mojang cooked up to incrementally make the change without changing NBT for mapmakers.

`collectImplicitComponents` is like `getStateFromMeta`. You enhance the new system (components) using the data gathered from the old system (local block entity fields, your nbt tag). `applyImplicitComponents` does the reverse; you take data from the components system and slap it back onto your local fields.

Also implicit components are important because i'm not sure how to actually *set* a single component from a block entity? I don't see an equiv of `ItemStack#set` for block entities. So implicit components are probably the best way to use components from a block entity

### Components and loot tables

I think block entity components shine in relation to items. Say you want block entity NBT to be saved to the item and restored when the player places the item again. In 1.20.1 usually this is done with the `BlockEntityTag` NBT tag on items, any NBT you carefully copy there with the `copy_nbt` loot function will be automatically copied onto a new block entity when it's placed (..but only on the server, resulting in a clientside flash of the default block entity, unless you fix it). In 1.21 you can copy components directly onto the item with the `copy_components` loot function, and they will be restored in the same way (`BlockItem#updateBlockEntityComponents` (static) -> `BlockEntity#applyComponentsFromItemStack` (final) -> `BlockEntity#applyComponents` (final) -> `BlockEntity#applyImplicitComponents` (yours to override)) on both sides.

### N.b.

Just because your component is a Java record doesn't mean you can forget about `equals` and `hashCode`. My component had an `ItemStack` as one of the fields and it could not stack with other copies of itself because the ItemStacks were different. 

## blockproperties `strength(float, float)`

first param sets `destroyTime`, second sets `explosionResistance`

## blockproperties "full copy" vs "legacy copy"

Looks new in 1.21.1

Legacy copy copies `destroyTime, explosionResistance, hasCollision, isRandomlyTicking, lightEmission, mapColor, soundType, friction, speedFactor, dynamicShape, canOcclude, isAir, ignitedByLava, liquid, forceSolidOff, forceSolidOn, pushReaction, requiresCorrectToolForDrops, offsetFunction, spawnTerrainParticles, requiredFeatures, emissiveRendering, instrument, replaceable`

Full copy additionally gets `jumpFactor, isRedstoneConductor, isValidSpawn, hasPostProcess, isSuffocating, isViewBlocking, drops`

## Block#codec is a footgun (as of 1.21.1)

You'll get inscrutable errors like "`This registry can't create intrusive holders`". If you must, just use `ResourceLocation.CODEC` and `xmap` it to read/write from `BuiltInRegistries`.

## ItemStack#save footgun

`ItemStack#save` crashes when called on `ItemStack.EMPTY`. Use `saveOptional` instead.

# wishlist

things to abstract away...

* "extendo tooltips". like the package maker gui slots.

# tooling

## why didn't `MixinExtension` work on neoforge?

Seems to assume one gradle project maps roughly into one jar. You "add mixin configs" *at the top-level*. This is a problem for me, because i have a `quatlib` source-set and a `rebind_narrator` source-set which both contain different mixin config files. but `MixinExtension` assumes each mixin config exists in each compilation task.

## why is manually configuring mixins not working?

the annotation processor is only configured for some of the compilation tasks in the `forge-1.20.1` subproject, so it's only running when those classes get compiled

the stuff in `:vanilla/modXxxxVersion1_20_1` was compiled without any ap

### this was fixed:

i learned how the mixin annotation procesor works and run it in a dedicated javacompile task, instead of trying to add it as an annotation processor while compiling the rest of the mod

## ConfigState

* what is `refresh` for? copied it from older projects without thinking too hard

I think it's supposed to be for implementing commands like `/auto_third_person refresh` which manually tell the system to go reload the config. But i am planning on using a filewatcher like on forge, then there is no need to refresh, and in fact the notification needs to come the other way

If the config is backed by a that kind of system not super sure it's a necessary method

* Computed values

a common config problem is computing a value based on the contents of the config file. for example taking the pile of `ResourceLocation`s and tag identifiers, and parsing them into a `Set<Block>`, which requires accessing the game in a way that the config-parsing does not

i think configstate is the best place to handle this, because it can cache the results of the computation, and it can dump the cache when the config changes

* the `set` problem

Auto-loading configs with a get/set api have a tricky problem to solve

* you open a config gui and the users click around
* you call `set` a bunch of times to change the config from in-game
* ok... when do you save the file. now? later? will `set` be called again? i don't know

so i think a more robust idea of changing the config has to exist.

```
mutableConfig.change(handle -> {
  for(ConfigChange change : changes) handle.set(change);
});
```

like that? where `changes` is some value managed by the config gui? Basically the only way to change the config is through `change`, so the config knows when it's about to be changed, & it knows when all of the changes have been applied and it's safe to save the file.

## Global validations

for example validating that certain numbers in the config file sum to 100. those types of validation have to go somewhere.

## warnings

I have an error report type inspired by `color-eyre`, where error context is added as the exception unwinds the stack. That's fun. However I think the thing I *actually* want is warnings which don't unwind the stack. I need to pass context *down* into methods so I have the information without needing to unwind. Errors can still make use of this information too, just toss it into whatever gets thrown.

Imagining the config file parser threading this state down into each `parseMap` and `parseList` call. The path inside the config file can be surfaced, not just the line-numbers. Neat.
