package agency.highlysuspect.crowmap.craftful.frg;

import agency.highlysuspect.crowmap.craftful.CrowmapMcClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class CrowmapForgeClient extends CrowmapMcClient {
	public CrowmapForgeClient() {
		MinecraftForge.EVENT_BUS.addListener((ItemTooltipEvent e) -> doTooltip(e.getItemStack(), e.getToolTip()::add));
	}
}
