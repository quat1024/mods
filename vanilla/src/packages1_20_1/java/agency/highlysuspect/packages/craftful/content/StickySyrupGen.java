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
			
			ctx.soundsJson(PLatches.SoundEvents.STICKY_SYRUP_APPLY).effect("block.honey_block.step");
			ctx.add(enUs()).sound(PLatches.SoundEvents.STICKY_SYRUP_APPLY).value("Sticky Syrup applied");
			
			ctx.soundsJson(PLatches.SoundEvents.STICKY_SYRUP_CLEAR).effect("block.honey_block.break");
			ctx.add(enUs()).sound(PLatches.SoundEvents.STICKY_SYRUP_CLEAR).value("Sticky Syrup wiped off");
		}
		
		ctx.reg(PLatches.Items.STICKY_SYRUP, () -> new StickySyrupItem(new Item.Properties().stacksTo(1).durability(64)));
		
		simpleSoundEvent(ctx, PLatches.SoundEvents.STICKY_SYRUP_APPLY);
		simpleSoundEvent(ctx, PLatches.SoundEvents.STICKY_SYRUP_CLEAR);
	}
}
