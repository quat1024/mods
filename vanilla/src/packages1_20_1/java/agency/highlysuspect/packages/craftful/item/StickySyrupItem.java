package agency.highlysuspect.packages.craftful.item;

import agency.highlysuspect.packages.craftful.Packages;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StickySyrupItem extends Item {
	public StickySyrupItem(Properties props) {
		super(props);
	}
	
	private static final Component HOLD_FOR_INFO = Component.translatable("packages.sticky_syrup_tooltip.hold_for_info", Component.translatable("packages.style_tooltip.shift").withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY);
	private static final List<Component> THE_INFO_IN_QUESTION = List.of(
		Component.translatable("packages.sticky_syrup_tooltip.1").withStyle(ChatFormatting.GRAY),
		Component.translatable("packages.sticky_syrup_tooltip.2").withStyle(ChatFormatting.GRAY),
		Component.translatable("packages.sticky_syrup_tooltip.3").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)
	);
	
	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag bwop) {
		super.appendHoverText(stack, level, tooltip, bwop);
		if(Packages.inst().proxy.hasShiftDownForTooltip()) tooltip.addAll(THE_INFO_IN_QUESTION);
		else tooltip.add(HOLD_FOR_INFO);
	}
}
