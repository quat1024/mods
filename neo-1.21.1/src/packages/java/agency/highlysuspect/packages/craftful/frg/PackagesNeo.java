package agency.highlysuspect.packages.craftful.frg;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.content.PLatches;
import agency.highlysuspect.packages.craftful.frg.net.ActionPacketNeo;
import agency.highlysuspect.packages.craftful.junk.ItemStackPackageContainer2;
import agency.highlysuspect.packages.craftful.junk.PackageRules;
import agency.highlysuspect.packages.craftless.PackagesBase;
import agency.highlysuspect.quatlib.craftful.neo.NeoBackedConfig_V1;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;

@Mod("packages")
public class PackagesNeo extends Packages {
	public final ModContainer modContainer;
	public final IEventBus modBus;
	
	public PackagesNeo(IEventBus modBus, ModContainer modContainer) {
		super();
		
		this.modBus = modBus;
		this.modContainer = modContainer;
		
		//general setup
		earlySetup();
		
		//misc events
		modBus.addListener(RegisterCapabilitiesEvent.class, this::registerCaps);
	}
	
	@Override
	public void registerActionPacketHandler() {
		modBus.addListener(RegisterPayloadHandlersEvent.class, e -> {
			//Executing on the network thread simply because my ActionPacket#handle already runs enqueueWork internally.
			//The neoforge executesOn helper basically just runs enqueueWork for you.
			e.registrar("1").executesOn(HandlerThread.NETWORK).playToServer(ActionPacketNeo.TYPE, ActionPacketNeo.STREAM_CODEC,
				(pk, ctx) -> {
					if(ctx.player() instanceof ServerPlayer sp) pk.handle(sp);
				});
		});
	}
	
	@Override
	public WritableConfig makeConfig(ConfigSection schema) {
		return NeoBackedConfig_V1.make(failures.context(), schema, modBus, modContainer, ModConfig.Type.COMMON);
	}
	
	private void registerCaps(RegisterCapabilitiesEvent e) {
		e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
			PLatches.BlockEntityTypes.PACKAGE.get(),
			(be, side) -> new PackageItemHandler(be));
		
		//TODO: some way to test this
		e.registerItem(Capabilities.ItemHandler.ITEM,
			(stack, __) -> new PackageItemHandler(new ItemStackPackageContainer2(stack, PackageRules.DEFAULT)),
			PLatches.Items.PACKAGE.get());
		
		//Neo doesn't automatically wrap vanilla inventories in IItemHandler. But SidedInvWrapper does.
		e.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
			PLatches.BlockEntityTypes.PACKAGE_MAKER.get(),
			SidedInvWrapper::new);
	}
	
	public static PackagesNeo inst() {
		return (PackagesNeo) PackagesBase.INST;
	}
}
