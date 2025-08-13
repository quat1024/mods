package agency.highlysuspect.packages.craftful.client;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.block.PBlockEntityTypes;
import agency.highlysuspect.packages.craftful.block.PBlocks;
import agency.highlysuspect.packages.craftful.menu.PMenuTypes;
import agency.highlysuspect.packages.craftful.net.ActionPacket;
import agency.highlysuspect.packages.craftful.net.PackageAction;
import agency.highlysuspect.packages.craftless.client.PackagesBaseClient;
import agency.highlysuspect.quatlib.craftless.client.QuatlibClientBase;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.ReadableConfig;
import net.minecraft.client.renderer.RenderType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class PackagesClient extends PackagesBaseClient {
	//Bindings, parsed from the config, and sorted such that the more specific ones are at the front of the list
	//(so the game checks ctrl-shift-alt, before ctrl-alt, before alt, etc.)
	//Kind of a clumsy spot to put this
	//TODO(season2): Clean up this mess lol
	public List<PackageActionBinding> sortedBindings = new ArrayList<>();
	
	public void earlySetup() {
		super.earlySetup();
		
		Packages.inst().proxy = new ClientProxy();
		
		//TODO: Facet system for these too.
		
		setupCustomModelLoaders();
		
		QuatlibClientBase.inst().registerMenuScreen(PMenuTypes.PACKAGE_MAKER, PackageMakerScreen::new);
		QuatlibClientBase.inst().setBlockEntityRenderer(PBlockEntityTypes.PACKAGE, PackageRenderer::new);
		QuatlibClientBase.inst().setRenderType(PBlocks.PACKAGE_MAKER, RenderType.cutoutMipped());
	}
	
	public void onConfigReload(ReadableConfig config) {
		//TODO(season2): Debug print
		Packages.LOG.info("Packages onConfigReload called with {}!!", config);
		
		sortedBindings = new ArrayList<>(Arrays.asList(
			PackageActionBinding.fromString(PackageAction.INSERT_ONE, config.get(PropsClient.INSERT_ONE_BINDING_UNPARSED)),
			PackageActionBinding.fromString(PackageAction.INSERT_STACK, config.get(PropsClient.INSERT_STACK_BINDING_UNPARSED)),
			PackageActionBinding.fromString(PackageAction.INSERT_ALL, config.get(PropsClient.INSERT_ALL_BINDING_UNPARSED)),
			PackageActionBinding.fromString(PackageAction.TAKE_ONE, config.get(PropsClient.TAKE_ONE_BINDING_UNPARSED)),
			PackageActionBinding.fromString(PackageAction.TAKE_STACK, config.get(PropsClient.TAKE_STACK_BINDING_UNPARSED)),
			PackageActionBinding.fromString(PackageAction.TAKE_ALL, config.get(PropsClient.TAKE_ALL_BINDING_UNPARSED))
		));
		Collections.sort(sortedBindings);
	}
	
	public abstract void setupCustomModelLoaders();
	public abstract void sendActionPacket(ActionPacket packet);
	
	@Override
	public ConfigSection visitClientConfigSchema(ConfigSection root) {
		return PropsClient.visit(root);
	}
	
	public static PackagesClient inst() {
		return (PackagesClient) PackagesBaseClient.INST;
	}
}
