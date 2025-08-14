package agency.highlysuspect.packages.craftful.junk;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

//TODO(season2, 1.21.1) make this into a proper data component too?
// It's only used on the block entity, for the 3d model
public record PackageMakerStyle(@Nullable Block frameBlock, @Nullable Block innerBlock, @Nullable DyeColor color) {
	public static final PackageMakerStyle NIL = new PackageMakerStyle(null, null, null);
}
