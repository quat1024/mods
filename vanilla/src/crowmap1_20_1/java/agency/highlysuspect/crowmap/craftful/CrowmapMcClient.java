package agency.highlysuspect.crowmap.craftful;

import agency.highlysuspect.crowmap.craftless.CrowmapBase;
import agency.highlysuspect.crowmap.craftless.CrowmapBaseClient;
import agency.highlysuspect.crowmap.craftless.CrowmapBaseOpts;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;

import java.util.function.Consumer;

public class CrowmapMcClient extends CrowmapBaseClient {
	protected static final Component HOLD_SHIFT =
		Component.translatable("crowmap.tooltip.hide").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
	protected static final Component INFO1 =
		Component.translatable("crowmap.tooltip.hello").withStyle(ChatFormatting.DARK_AQUA);
	protected static final Component INFO2 =
		Component.translatable("crowmap.tooltip.info").withStyle(ChatFormatting.DARK_AQUA);
	protected static final Component BYE =
		Component.translatable("crowmap.tooltip.bye").withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC);
	
	protected void doTooltip(ItemStack stack, Consumer<Component> list) {
		if(!CrowmapBase.inst().config.get(CrowmapBaseOpts.SHOW_TOOLTIP)) return;
		
		if(stack.getItem() instanceof MapItem) {
			switch(tooltipStateMachine.tick(Screen.hasShiftDown())) {
				case SHOW_HOLD_SHIFT_PROMPT -> {
					list.accept(HOLD_SHIFT);
				}
				case SHOW_INFO -> {
					list.accept(INFO1);
					list.accept(INFO2);
				}
				case SHOW_SELF_DESTRUCT_MESSAGE -> {
					list.accept(BYE);
				}
			}
		}
	}
	
	public static CrowmapMcClient inst() {
		return (CrowmapMcClient) INST;
	}
}
