package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.item.StickySyrupItem;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

public class StickySyrupGen implements PGen {
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		if(ctx.dgen != null) {
			ctx.add(enUs()).item(PLatches.Items.STICKY_SYRUP).value("Sticky Syrup");
		}
		
		ctx.reg(PLatches.Items.STICKY_SYRUP, () -> new StickySyrupItem(new Item.Properties().stacksTo(1).durability(64)));
	}
}
