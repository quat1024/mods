package agency.highlysuspect.packages.craftful.frg;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.block.PackageBlockEntity;
import agency.highlysuspect.packages.craftful.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.craftful.net.ActionPacket;
import agency.highlysuspect.packages.craftless.PackagesBase;
import agency.highlysuspect.quatlib.craftful.frg.ForgeBackedConfig_V1;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Mod("packages")
public class ForgeInit extends Packages {
	public final SimpleChannel channel = NetworkRegistry.newSimpleChannel(rl("n"), () -> "0", "0"::equals, "0"::equals);
	
	public final ModContainer modContainer;
	public final IEventBus modBus;
	
	public ForgeInit() {
		super();
		
		this.modContainer = ModLoadingContext.get().getActiveContainer();
		this.modBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		//general setup
		earlySetup();
		
		//misc events
		MinecraftForge.EVENT_BUS.addGenericListener(BlockEntity.class, this::attachCaps);
		
		//If i was forge i would simply have client entrypoints
		if(FMLEnvironment.dist == Dist.CLIENT) {
			try {
				Class.forName("agency.highlysuspect.packages.craftful.frg.client.ForgeClientInit").getConstructor().newInstance();
			} catch (ReflectiveOperationException e) {
				throw failures.context()
					.cause(e)
					.detail("Packages had a problem initializing ForgeClientInit")
					.uncheckedReportError();
			}
		}
	}
	
	@Override
	public void registerActionPacketHandler() {
		channel.registerMessage(ActionPacket.SHORT_ID, ActionPacket.class, ActionPacket::write, ActionPacket::read, (action, ctxSupplier) -> {
			NetworkEvent.Context ctx = ctxSupplier.get();
			//Forge uses the same networkstuff for client -> server and server -> client packets.
			//This is a client -> server packet, so of course the sender is a nonnull player, but Forge doesn't statically know that
			ServerPlayer player = ctx.getSender();
			if(player == null) return;
			
			action.handle(player);
			ctx.setPacketHandled(true);
		});
	}
	
	@Override
	public WritableConfig makeConfig(ConfigSection schema) {
		return ForgeBackedConfig_V1.make(failures.context(), schema, modBus, modContainer, ModConfig.Type.COMMON);
	}
	
	//forge doesn't automatically wrap iinventories with item handlers anymore :pensive:
	//not all bad; with the Package i think a custom implementation is beneficial anyway
	private void attachCaps(AttachCapabilitiesEvent<BlockEntity> e) {
		if(e.getObject() instanceof PackageBlockEntity pkg) {
			e.addCapability(Packages.rl("a"), new ICapabilityProvider() {
				@Override
				public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
					return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> new PackageItemHandler(pkg)).cast());
				}
			});
		} else if(e.getObject() instanceof PackageMakerBlockEntity pmbe) {
			e.addCapability(Packages.rl("b"), new ICapabilityProvider() {
				@Override
				public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
					return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> new SidedInvWrapper(pmbe, side)));
				}
			});
		}
	}
	
	public static ForgeInit inst() {
		return (ForgeInit) PackagesBase.INST;
	}
}
