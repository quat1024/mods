package agency.highlysuspect.crowmap.craftful.fab;

import agency.highlysuspect.crowmap.craftful.CrowmapMcClient;
import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibClientInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;

public class CrowmapFabricClient extends CrowmapMcClient implements AfterQuatlibClientInitializer {
	@Override
	public void onInitializeClient() {
		ItemTooltipCallback.EVENT.register((stack, ctx, flag, components) -> doTooltip(stack, components::add));
	}
}
