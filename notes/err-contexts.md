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

