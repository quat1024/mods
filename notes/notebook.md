# notebook

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
