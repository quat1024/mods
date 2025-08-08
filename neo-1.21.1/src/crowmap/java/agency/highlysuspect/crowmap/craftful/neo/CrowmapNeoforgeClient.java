package agency.highlysuspect.crowmap.craftful.neo;

import agency.highlysuspect.crowmap.craftful.CrowmapMcClient;
import agency.highlysuspect.crowmap.craftless.CrowmapBase;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@Mod(value = CrowmapBase.MODID, dist = Dist.CLIENT)
public class CrowmapNeoforgeClient extends CrowmapMcClient {
	public CrowmapNeoforgeClient() {
		NeoForge.EVENT_BUS.addListener(ItemTooltipEvent.class, e -> doTooltip(e.getItemStack(), e.getToolTip()::add));
	}
}
