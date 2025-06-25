# notebook

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
