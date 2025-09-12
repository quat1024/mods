package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftless.PackagesBase;
import agency.highlysuspect.quatlib.craftful.QuatlibMc;
import agency.highlysuspect.quatlib.craftless.facet.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class PackagesGen implements PGen {
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		more.accept(new PackageGen());
		more.accept(new PackageMakerGen());
		more.accept(new StickySyrupGen());
		
		ctx.lang(EN_US).key("itemGroup.packages.group").value(PackagesBase.NAME);
		
		ctx.reg(LatchPool.INST.get(RegType.CREATIVE_TABS, new Id(Packages.MODID, "group")), () ->
			QuatlibMc.inst().makeCreativeModeTabBuilder()
				.title(Component.translatable("itemGroup.packages.group"))
				.icon(() -> {
					try {
						List<ItemStack> stacks = PLatches.Blocks.PACKAGE.get().lotsOfPackages();
						return stacks.get(new Random(System.currentTimeMillis()).nextInt(stacks.size()));
					} catch (Exception e) { //trust no one not even yourself
						return new ItemStack(PLatches.Items.PACKAGE_MAKER.get());
					}
				})
				.displayItems((params, out) -> {
					out.accept(PLatches.Items.PACKAGE_MAKER.get());
					out.accept(PLatches.Items.STICKY_SYRUP.get());
					PLatches.Blocks.PACKAGE.get().lotsOfPackages().forEach(out::accept);
				})
				.build()
		);
	}
}
