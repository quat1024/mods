# packages 1.21.1 retexturing engine

`IQuadView`: part of moddernamelib. Abstracts details of working with fabric QuadEmitters vs on forge where I have to manually copy bakedquads and poke at their vertex data

`PackageRetexturizer` encapsulates the retexturing logic. Construct it with a base bakedmodel + some other data it needs, provide it with a `IQuadView` and model parameters, and it'll loop through each quad of the base model (setting them into the quadview with `fromVanilla`), perform the retexturing logic, and output anything it wants to keep to the `Consumer<Q>` you also pass it.

The interface `PackageModelBakery` plugs this into a specific model ecosystem basically. On Fabric we need to make a `Mesh` object to pass to frapi, and on forge we need to make a `List<BakedQuad>`. There are also slighly different ways of reading the particle texture of a block on different modloaders (used to determine what texture to put on the side of a quad). Finally, there is a model caching feature built at this layer (`PackageModelBakery#withCache`). Concrete implementations of this interface extend `PackageRetexturizer`.

Above that is the actual `BakedModel` implementations (an inner class of the corresponding unbaked model) which just read data off the block/itemstack, feed it to the `PackageModelBakery` to get a model, and emit that model.

The unbaked models are responsible for constructing the bakedmodels and the packagemodelbakeries. To perform the retexturing logic `PackageRetexturizer` needs to know the location of the "special_frame" and "special_inner" sprites on the texture atlas, model baking time is the best time to find this information.

## Forge item overrides

This is an annoying system but strictly speaking it's more vanilla...

* return something from `ItemOverrides getOverrides()`
* there is a method `resolve` which i commented as "the magic method":
  * `public BakedModel resolve(BakedModel originalModel, ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity player, int idk) {`
  * that's where you finally get the ItemStack and can return a new BakedModel based on it.
  * I call model bakery code and get a `List<BakedQuad>` which i pass to my `FixedQuadListBakedModel`, another model which wraps the base one
* inside `FixedQuadListBakedModel` since it extends forge's `BakedModelWrapper` class (to inherit, e.g., item rotations in the hand) need to make sure to:
  * return an empty item overrides
  * override `applyTransform` to not return the base model in `applyTransform`
  * override `getRenderPasses` to not return the original model's render passes (etc)

## Old system

was a lot more overcooked and didn't actually reduce code duplication that much anyway. There is a little bit of cut-paste but i think it's better than introducing yet more levels of abstraction at this point lol

## Other design decisions

Forge has some `IQuadTransformer` class. It's useful but there is no way to conditionally accept/reject quads which i need to do for the Package Crafter model
