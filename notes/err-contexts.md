# Error and warning contexts

This comes up when working with config files; you want to warn that something has gone wrong, but you're able to recover. Like a config option is incorrect and you want to warn about it, but it's possible to load the default value so the game doesn't crash

TODO, those.

# A `color-eyre` knockoff

The fundamental exception type is `Report`, which holds a *list* of messages as well as a pile of "Section"s.

The rethrow pattern is like this:

```
try {
	doSomething();
} catch (Throwable e) {
	throw Report.modify(e, it -> it
		.addMessage("While reticulating " + count + " splines")
	);
}
```

If `e` is a Report, the message is added to its existing message stack, and if `e` is not a report, one is freshly created. This can then be nicely formatted to the console like `color-eyre` with `ConsoleReportFormatter`. (I could also format it nicely using `printStackTrace`, but there are potential problems (what if the error formatter crashes, hmm). Instead, if you use `printStackTrace` the message is "all of the messages in the message stack joined with commas", so there is at least *some* data.)

The `Report.modify` function is used to catch errors related to adding context to the report. The lambda gets is wrapped in a trycatch at its call site. So adding context inside `modify` can never get in the way of the report displaying.

# Context chains?

`Report`s are cute but they deal with exceptions. Sometimes i just want a warning and don't need to unwind the stack. So this cute `Report.modify` trick won't work, since the only way to get context is to actually unwind the stack.

Instead there is `ContextChain` which is a way to proactively pass information *down* into functions, instead of passively collecting it while unwinding the stack. Each one is attached to a `FailureBin` which contains all the warnings and at any time you can add a new warning

# Types of error

* *Recoverable warnings*. Log a warning and move on.
  * If I'm parsing a quoted string and the line ends unexpectedly, I can pretty safely assume the user just forgot the closing quote
  * If an integer value is above the maximum permitted value, I can just clamp it (the "correction" mechanism)
* *Unrecoverable errors*. These typically throw an exception.
  * Parsing state gets FUBARd and I know it'd just confuse things even further if I tried to guess what the user meant.
  * Stuff like `SnView.asMap` on not-maps. I can't return anything.
* *Recoverable errors*? Catching exceptions.
  * E.g. if there is a problem while parsing a config option to an integer. I can't return an integer, but I would still like to try and parse the rest of the options in the file, and maybe set this config value to its default
  * usually the recoverable-ness is decided by the call site, not by the nature of the error. I don't care *why* it wasn't possible to parse the config option to an integer I already know i need to move on.

Observations:

* The context mechanism seems good for both recoverable and unrecoverable errors. In both cases I have a nice amount of context to display.
* If reporting an error through the context mechanism is the only way to throw the exception used for unrecoverable-errors, then the exception itself doesn't need any information. It's already been reported.