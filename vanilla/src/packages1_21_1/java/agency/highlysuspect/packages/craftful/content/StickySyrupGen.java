package agency.highlysuspect.packages.craftful.content;

import agency.highlysuspect.packages.craftful.item.StickySyrupItem;
import agency.highlysuspect.quatlib.craftless.facet.Gen;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;

public class StickySyrupGen implements PGen {
	@Override
	public void gen(Ctx ctx, Consumer<Gen> more) {
		ctx.lang(EN_US).item(PLatches.Items.STICKY_SYRUP, "Sticky Syrup");
		
		ctx.sound(PLatches.SoundEvents.STICKY_SYRUP_APPLY)
			.effect("block.honey_block.step")
			.subtitle(EN_US, "Sticky Syrup applied");
		
		ctx.sound(PLatches.SoundEvents.STICKY_SYRUP_CLEAR)
			.effect("block.honey_block.break")
			.subtitle(EN_US, "Sticky Syrup wiped off");
		
		ctx.reg(PLatches.Items.STICKY_SYRUP, this::constructItem);
	}
	
	protected Item constructItem() {
		return new StickySyrupItem(new Item.Properties().stacksTo(1).durability(64));
	}
}
