package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.block.PBlocks;
import agency.highlysuspect.packages.craftful.item.PItems;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import agency.highlysuspect.quatlib.craftless.facet.Id;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import agency.highlysuspect.quatlib.craftless.facet.RegType;
import agency.highlysuspect.quatlib.craftless.facet.facets.RegFacet;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class PackagesGen implements Gen {
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		more.accept(new PackageGen());
		more.accept(new PackageMakerGen());
		
		ctx.add(new RegFacet<>().latch(Latch.open(RegType.CREATIVE_TABS, new Id(Packages.MODID, "group"))).sup(() ->
			Packages.inst().creativeModeTabBuilder()
				.title(Component.translatable("itemGroup.packages.group"))
				.icon(() -> {
					try {
						List<ItemStack> stacks = PBlocks.PACKAGE.get().lotsOfPackages();
						return stacks.get(new Random(System.currentTimeMillis()).nextInt(stacks.size()));
					} catch (Exception e) { //trust no one not even yourself
						return new ItemStack(PItems.PACKAGE_MAKER.get());
					}
				})
				.displayItems((params, out) -> {
					out.accept(PItems.PACKAGE_MAKER.get());
					PBlocks.PACKAGE.get().lotsOfPackages().forEach(out::accept);
				})
				.build()
		));
	}
}
