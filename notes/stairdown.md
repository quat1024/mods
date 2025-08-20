## Northstar

Ideally the config file would look something like

```
variants = [
  {
    craftedFrom = somemod:some_thing
    stairs = true
    slab = true
    wall = {
      recipe = false
    }
  }
]
```

basically

* leave off a variant to not generate it
* set a variant to `true` to generate it with the default options
* set a variant to an sn object to configure it further

I'm thinking top-level keys that are *not* variant names (keys other than `stairs`, `slab`, `wall` etc) also set some default properties copied to all variants in the `{ }` block...? 

## minimum viable

forget the nesting and cascading stuff, i actually don't think it will be that helpful anyway. (this mod is for quick and dirty blocks anyhoo. if you want something fancier there's always cube js)

```
variants = [
  {
    craftedFrom = somemod:some_thing
    idPrefix = some_thing_
    stairs = true
    slab = true
    wall = true
  }
]
```