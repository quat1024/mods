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

## Examples of prior art & my criticisms

I think these are great config formats! I just have weird tastes and specific goals (like "whitespace in raw strings")

Here are some reasons my goals are different from other configuration languages:

* My audience is videogame players who are not necessarily software developers.
* It's common for Minecraft mods to manage their own config file. Generally mods parse their config file and then immediately write it back into place, replacing all the comments with developer-supplied ones and redoing the formatting, etc. So features like functions and backreferences will not be used, because the program is in charge of writing the file, not a person.
* I am a "syntax typing" hater. Config files don't need to support numbers and booleans specially. Unquote ALL the strings!!!
* I think unquoted strings should allow whitespace, up until the next delimiter. The TOML files NightConfig writes [make me sad](https://github.com/VazkiiMods/Musketeer/blob/b5d9a76977e4a6cde04b894130db1cefeb6e59f3/config/quark-common.toml#L107-L142).

Some config formats.

* [KDL](https://kdl.dev/)
  * `+` Easy to read
  * `+` Multiline strings with nice syntax. good feature!
  * `+` Slightly whitespace-sensitive, but in a way that makes sense
  * `+` Comments
  * `-` Syntax typing
	* `-` No whitespace in unquoted strings
  * `-` Arrays are fake

KDL has an interesting "node arguments" paradigm where if you write `a b c { ... }`, "b" and "c" become *arguments* to "a". This is cool and would allow for config files like this:

```kdl
module1 enabled {
  option value
}

module2 disabled {
  option2 value
}
```

But this feature precludes whitespace in raw strings, which I care about more for my purposes.

* [scfg](https://git.sr.ht/~emersion/scfg)
  * `+` It's like "KDL if it only had strings", therefore there is no syntax typing, yay
  * `-` No multiline strings?
  * `-` Arrays are still fake

What i mean by "arrays are fake" is that the primary unit in these configuration languages is some kind of multimap from *string* keys to a richer type, so the only place you can put the richer type is in "value position". If you want an array of strings, you can leverage the map structure, use the keys of the map like an array.

```
my-array {
  value1
  value2
  value3
}
```

But if you want arrays-of-more-complicated-things, you need a workaround like ["directives with meaningless names"](https://github.com/kdl-org/kdl/blob/main/JSON-IN-KDL.md). It doesn't speak to me.

For my purposes (minecraft config file, aimed at non-technical users to edit): I *mostly* need maps, but when I want an array I *do* want an array. I think "encoding arrays as directives" is an example of "*fewer* things in the system actually making it *more* complicated".

* [HOCON](https://github.com/lightbend/config/blob/master/HOCON.md)
  * `+` Its goals. It's nice to see this outlined in a document:
    > HOCON is significantly harder to specify and to parse than JSON. Think of it as moving the work from the person maintaining the config file to the computer program
  * `+` Comments
  * `+` Real arrays
  * `+` Comes from the Java ecosystem :sparkles: haha
  * `+` *Interesting thing I will explain*
  * `-` No whitespace in unquoted strings
  * `-` Syntax typing with strange parsing rules (`truefoo` tokenizes as boolean `true` followed by `"foo"`), which is kind of taped around with "value concatenation"
  * `-` Feature creep (includes, selfreferences, an expression langauge, environment variables, etc etc)

quaternioncats pointed out that it seems optimized for creating structured configs via string-templating; you can leverage the value-concatenation to paste arrays together without worrying too much about delimiters and commas. Interesting goal, but not one I particlularly care about.

Includes and selfreferences are fun for people configuring real software. For silly little minecraft mods they are not needed.

The interesting thing: The colon separating a key from a value is optional if the value is an array or object, i.e. `foo {` means `"foo": {` I like this and I actually ran into the same corner of the design space designing my format, but didn't know whether to make the colon required or optional. So I guess it can be optional if HOCON does it... 

* [HashiCorp Configuration Language](https://github.com/hashicorp/hcl/blob/main/hclsyntax/spec.md)
  * `+` Comments 
  * `+` Real arrays
  * `-` Syntax typing
  * `-` Expression language ? Functions????? Chill bro

Also seems optimized for the "expert developer tending to their file" rather than average user `^^`. I can't find too much information about this format because the documentation is dwarfed in size by details of their expression language.

## Details

Still loosely-sketching things in, everything is sort of ad-hoc

### Internal representation

A "half decent config format" file describes an instance of the `Sn` data structure. `Sn` is JSON, except everything except strings and collections has been removed. An `Sn` can be one of three things:

* `String`
* `List<Sn>`
* `Map<String, Sn>`

It stands for "string notation".

### Whitespace

`Character.isWhitespace` denotes whitespace. This includes newlines and such

### Comments

`%` starts a comment. The comment continues to the end of the line.

### Kv

* Skip whitespace and comments
* Parse a **key**
* Skip whitespace and comments
* Look at the next character:
  * if it's an equal sign:
    * skip it,
    * skip *whitespace but not newlines*,
    * if the next character is a newline, the value is `""`,
    * otherwise parse a value
  * if it's an `[`, parse an array
  * if it's an `{`, parse a map
	* anything else is an error
	
`[` and `{` have special handling to implement the "equal signs are optional before arrays and objects" rule.

The weird whitespace shit about equal signs is an attempt to make this

```
{
	foo =
	bar = baz
}
```

parse as `foo = ""`.

### Key

If the next character is `"`, parse a quoted string, otherwise parse a bare string in the following way:

* advance the cursor until seeing a kv-split (`=`), a line-comment start (`%`), the start of an array or object (`{[`), or a newline, whichever comes first
* trim the whitespace from the bit that was advanced over
* this is the key

### Value

Based off the next character:

* `{`, parse an object
* `[`, parse an array
* `"`, parse a quoted string
* anything else, parse a bare string

This time the bare string extends to `%]}` or the end of the line. `%` for a line-comment, `]}` since they may close the structure this keyvalue is embedded in.