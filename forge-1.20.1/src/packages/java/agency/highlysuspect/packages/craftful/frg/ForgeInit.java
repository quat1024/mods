package agency.highlysuspect.packages.craftful.frg;

import agency.highlysuspect.packages.craftful.Packages;
import agency.highlysuspect.packages.craftful.block.PackageBlockEntity;
import agency.highlysuspect.packages.craftful.block.PackageMakerBlockEntity;
import agency.highlysuspect.packages.craftful.net.ActionPacket;
import agency.highlysuspect.packages.craftful.platform.BlockEntityFactory;
import agency.highlysuspect.packages.craftful.platform.MyMenuSupplier;
import agency.highlysuspect.packages.craftful.platform.RegistryHandle;
import agency.highlysuspect.packages.craftless.PackagesBase;
import agency.highlysuspect.quatlib.craftful.frg.ForgeBackedConfig_V1;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Mod("packages")
public class ForgeInit extends Packages {
	public final SimpleChannel channel = NetworkRegistry.newSimpleChannel(id("n"), () -> "0", "0"::equals, "0"::equals);
	
	private final Map<Registry<?>, DeferredRegister<?>> deferredRegistries = new HashMap<>();
	private final Map<RegistryHandle<? extends ItemLike>, DispenseItemBehavior> dispenseBehaviorsToRegister = new HashMap<>();
	
	public final ModContainer modContainer;
	public final IEventBus modBus;
	
	public ForgeInit() {
		super();
		
		this.modContainer = ModLoadingContext.get().getActiveContainer();
		this.modBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		//general setup
		earlySetup();
		
		//misc events
		modBus.addListener(this::actuallyRegisterDispenserBehaviors);
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
	public boolean isForge() {
		return true;
	}
	
	@SuppressWarnings("unchecked") //Go directly to generics hell. Do not pass Go or collect $200.
	private <T> DeferredRegister<T> getDeferredRegister(Registry<?> reg) {
		return (DeferredRegister<T>) deferredRegistries.computeIfAbsent(reg, __ -> {
			DeferredRegister<T> deferred = (DeferredRegister<T>) DeferredRegister.create(reg.key(), Packages.MODID);
			deferred.register(FMLJavaModLoadingContext.get().getModEventBus());
			return deferred;
		});
	}
	
	@Override
	public <T> RegistryHandle<T> register(Registry<? super T> registry, ResourceLocation id, Supplier<T> thingMaker) {
		if(!id.getNamespace().equals(Packages.MODID)) throw new IllegalArgumentException("Forge enforces one modid per DeferredRegister");
		
		RegistryObject<T> obj = getDeferredRegister(registry).register(id.getPath(), thingMaker);
		return new RegistryObjectRegistryHandle<>(obj);
	}
	
	private record RegistryObjectRegistryHandle<T>(RegistryObject<T> obj) implements RegistryHandle<T> {
		@Override
		public T get() {
			return obj.get();
		}
		
		@Override
		public ResourceLocation getId() {
			return obj.getId();
		}
	}
	
	@SuppressWarnings("ConstantConditions") //null DFU type
	@Override
	public <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks) {
		return new BlockEntityType<>(factory::create, Set.of(blocks), null); //Access widened by forge
	}
	
	@Override
	public CreativeModeTab.Builder creativeModeTabBuilder() {
		return CreativeModeTab.builder(); //Forge-added zero arg constructor
	}
	
	@Override
	public void registerDispenserBehavior(RegistryHandle<? extends ItemLike> item, DispenseItemBehavior behavior) {
		dispenseBehaviorsToRegister.put(item, behavior);
	}
	
	@Override
	public <T extends AbstractContainerMenu> MenuType<T> makeMenuType(MyMenuSupplier<T> supplier) {
		//Looks the same as on FabricPlatformSupport but it's private in mojang source so i can't use it there without access widening
		return new MenuType<>(supplier::create, FeatureFlagSet.of());
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
	
	private void actuallyRegisterDispenserBehaviors(FMLCommonSetupEvent e) {
		dispenseBehaviorsToRegister.forEach((handle, behavior) -> DispenserBlock.registerBehavior(handle.get(), behavior));
	}
	
	//forge doesn't automatically wrap iinventories with item handlers anymore :pensive:
	//not all bad; with the Package i think a custom implementation is beneficial anyway
	private void attachCaps(AttachCapabilitiesEvent<BlockEntity> e) {
		if(e.getObject() instanceof PackageBlockEntity pkg) {
			e.addCapability(Packages.id("a"), new ICapabilityProvider() {
				@Override
				public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
					return ForgeCapabilities.ITEM_HANDLER.orEmpty(cap, LazyOptional.of(() -> new PackageItemHandler(pkg.getContainer())).cast());
				}
			});
		} else if(e.getObject() instanceof PackageMakerBlockEntity pmbe) {
			e.addCapability(Packages.id("b"), new ICapabilityProvider() {
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
