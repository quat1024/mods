# Naming conventions

## package structure

Split-package rules are a thing in neoforge, and also there's just dozens of source sets flying around that should be organized

* `agency`
  * `highlysuspect`
    * (modid)
      * `craftless`
        * Code for any minecraft version (in `:vanilla modidAny`)
        * `fab`
          * Code only touching fabric-loader (in `:floader-only`)
      * `craftful`
        * Code for the specific minecraft version but not touching any loader (in `:vanilla modid1_xx_x`)
        * `fab`/`fge`/`neo`
          * Mod and loader-specific code for the specific version (in the loader subproject). This is the only package loader projects should put code in

I don't have to worry about quatlib code ending up in the same package as non-quatlib code b/c the modid is `modder_name_lib` (idk why i as worried about that)

## Mod ids

Should use underscores, not hyphens, if possible (forge likes it better)

## Mixin jsons

Should be called `${modid}.mixins.json` if they are loader-independent and `${modid}.${loader}.mixins.json` if they are not.

I assume all mixin jsons are version-specific. Cross-version mixins should just be cut and pasted across versions. (I should also try to keep mixins smaller)

## Icons

Should live at the root of the jar (forge requires that I think?). Mod Menu-compatible icons (128 x 128) should be named `${modid}_modmenu.png`. Forge-style (480 x 120) should be named `${modid}_banner.png`

Use this icon generator: https://notes.highlysuspect.agency/icon-maker.html
