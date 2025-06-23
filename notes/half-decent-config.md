# Half Decent Config

Yet another ModderNameConfigFormat. Planning to use this on Fabric until they develop a config api (ha, ha).

This time i'm moving on up from crummyconfig!

## Northstar

```
# This is my cool config file
coolmod {
	# Yuck stinky mammals
	mammals {
		# How many rabbits?
		# Must be at least 0.
		# Default: 5
		rabbits: 5
	}

	# Wooo lets go
	# I love lizards
	reptiles {
		# How many lizards?
		# Must be at least 0.
		# Default: 5
		lizards: 5

		# How many dragons?
		# Must be at least 0.
		# Default: 5
		dragons: 999
	}
}
```

## Goals

Basically see my essay ["Towards a good config system"](https://notes.highlysuspect.agency/config.html)

For users:

* A simple textual format that's approachable and hard to break
* Decent error-reporting and decent error-recovery where appropriate

For developers:

* Separate "parsing config into a bag of strings", "mapping that onto a config schema", and "parsing the strings in the config according to the schema" into three separate phases
  1. Stuff like config migrations. Need to be able to handle "config values that don't exist anymore in my schema" in ad-hoc ways
  2. Hopefully this leads to a holistic approach to error-reporting
* Tidy config option api
* Lots of control over how types are represented as strings
  * A color looks best as an `#AARRGGBB` string, even though it's just an `int` in-game. Therefore type cannot completely drive the serialization and deserialization process

## Shortcomings of the simple line-by-line split parser

Here is "the simple `split` parser":

```java
private void parseToStringsImpl(Lineserator iter, SnocList<String> fullName, Map<SnocList<String>, String> result) {
	while(iter.hasNext()) {
		String line = iter.next().trim();
		if(line.isEmpty() || line.startsWith("#")) continue;
		
		String[] split = line.split(":", 2);
		if(split.length == 2) {
			//if it has a colon, it's a value
			String key = split[0].trim();
			String value = split[1].trim();
			result.put(fullName.snoc(key), value);
		} else if(line.startsWith("}")) {
			//closing a section
			return;
		} else if(line.endsWith("{")) {
			//opening a section: recur into the section
			String sectionName = line.substring(0, line.length() - 1).trim();
			parseToStringsImpl(iter, fullName.snoc(sectionName), result);
		}
	}
}
```

where `Lineserator` is an `Iterator<String>` and `SnocList` is an cheap-to-append-to immutable linked list.

This outlines some basics of the format:

* unquoted keys, which run up until the first `:` or `{`, are allowed and permitted
  * if it runs into a `:`, it starts a kv pair
  * otherwise if it runs into a `{`, it starts a section
* unquoted values, which run up until the end of the line, are allowed and permitted
* both unquoted keys and values may contain spaces, numbers, and other fun stuff
* line comments start with `#` (and may only begin at the start of a line?)

but has significant shortcomings:

* no facility for keys starting with `#` or containing `:`,
* no facility for values spanning multiple lines,
* lines that don't contain `:`, `{`, or `}` get *silently* dropped (very bad!)

## better parser

So I think the appropriate way to parse this format is with a recursive parser. I would like to add (optional!!) quoted strings, multiline strings, etc so blunt `lines` and `split`-based approaches will not work anymore.  It's a little bit tricky to parse this format because of all the unquoted strings.

to parse an "item" (either a section or an option, where you're not sure which one you're going to get until you try)

* skip whitespace
* parse a string key:
  * peek the next character. if it is `"`, parse a quoted string
  * otherwise parse an unquoted string which ends at the first `:`, `{` or `\n` and trim the whitespace
* skip whitespace
* eat the next character
	* if it is `:`, you have an option. skip whitespace and parse the next string.
    * if it is quoted, same quoted-string routine
    * if it is unquoted, this time the string doesn't end at `:` or `{` and always continues to the end of the line
  * if it is `{`, you have a section. skip whitespace and peek the next character
    * if it is `}`, end the section
    * otherwise parse an item and add it to the section
  * otherwise we have a problem, a key without a corresponding value or section.
    * report a *warning*, drop the key on the floor, and continue to the next thing?
    * (only if it does not cause a mess, or situations where one typo causes a lot of the config file to reset)

This has been implemented in `HalfDecentConfigParser`. Yay.

# dev api

This is separate from the Half Decent Config textual format but I'm also thinking about a nice API I can use

Actually I went and moved this to the bottom of ["Towards a good config system"](https://notes.highlysuspect.agency/config.html) (section "Programmer API styles")