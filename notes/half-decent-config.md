# Half Decent Config

Yet another ModderNameConfigFormat. Planning to use this on Fabric until they develop a config api (ha, ha).

This time i'm moving on up from crummyconfig!

## Initial sketch

<details><summary>click to expand the initial sketch</summary>

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

</details>

## better way

Shortcomings of the second parser:

* `#` character for comments clashes with `#minecraft:foo` used to denote tags in the game
  * Changed to `%`
* `:` character to split keys from values clashes with `minecraft:stone` used to split `ResourceLocation`s
  * Not actually ambiguous parse due to the raw-string rules: `foo:bar:baz` parses as `"foo": "bar:baz"` unambiguously
  * Still, it's confusing for users especially if there is not enough whitespace, so changed to `=`
* Multiline strings are too easy to make on accident if you omit the closing quote, and cause the rest of the file to parse like a string
  * Forbid literal-newlines from appearing in strings.
  * Literal newlines can be escaped with a backslash (line continuation character)
* It can't parse toplevel keyvalues
  * Fixed with revised parser that munches the entire file

Also, I was wrong in "Towards a good config system", and we actually do need arrays! Arrays can't be represented with multiline strings because the config formatter can't see into them, and because multiline strings are now even more annoying than they used to be, requiring line-continuation characters.

If I'm going to add arrays-as-first-class-values I might as well add objects-as-first-class-values, and then remove the distinction between "config categories" and "objects".

After lots of fruitful discussion with arty i've landed on the following format & internal representation. The format is temporarily (lol) called `SN` for "string notation".

## Revised northstar

```
% This is my cool config file

% Yuck stinky mammals
mammals = {
	% How many rabbits?
	% Must be at least 0.
	% Default: 5
	rabbits = 5
}

% Wooo lets go
% I love lizards
reptiles = {
	% How many lizards?
	% Must be at least 0.
	% Default: 5
	lizards = 5

	% How many dragons?
	% Must be at least 0.
	% Default: 5
	dragons = 999
}

% These are a few of my favorite things
favorites = [
	apple
	banana
	gator
]
```

## Details

Still loosely-sketching things in, everything is sort of ad-hoc

### Internal representation

`Sn` is JSON except everything except strings and collections has been removed. An `Sn` can be one of three things:

* `String`
* `List<Sn>`
* `Map<String, Sn>`

### Parsing

#### Whitespace

`Character.isWhitespace` denotes whitespace. This includes newlines and such

#### Comments

`%` starts a comment. The comment continues to the end of the line.

#### Kv

* Skip whitespace and comments
* Parse a **key**
* Skip whitespace and comments
* Eat an equal sign (?)
* Skip whitespace and comments
* Parse a **value**

The (?) is because of an error-recovery mechanism I'm thinking about? If the next character is not `=`, report a warning but parse the value anyway?

#### Key

If the next character is `"`, parse a quoted string

Otherwise, parse a bare string in the following way:

* advance the cursor until `{`, `[`, a newline, a line-comment start (`%`), or a kv-split (`=`); whichever comes first
* trim the whitespace from the bit that was advanced over
* this is the key

#### Value

Based off the next character:

``