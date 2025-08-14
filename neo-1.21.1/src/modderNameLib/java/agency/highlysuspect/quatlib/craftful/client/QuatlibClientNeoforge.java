package agency.highlysuspect.quatlib.craftful.client;

import agency.highlysuspect.quatlib.craftful.neo.QuatlibNeoforge;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.client.MyBlockEntityRendererProvider;
import agency.highlysuspect.quatlib.craftless.client.MyScreenConstructor;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mod(value = QuatlibBase.MODID, dist = Dist.CLIENT)
public class QuatlibClientNeoforge extends QuatlibClientMc {
	public QuatlibClientNeoforge() {
		super();
		
		this.modBus = QuatlibNeoforge.inst().modBus;
		modBus.addListener(RegisterMenuScreensEvent.class, this::actuallyRegisterMenuScreens);
		modBus.addListener(EntityRenderersEvent.RegisterRenderers.class, this::actuallySetBlockEntityRenderers);
		modBus.addListener(FMLClientSetupEvent.class, this::actuallySetRenderTypes);
	}
	
	protected final IEventBus modBus;
	
	private final List<MenuScreenEntry<?, ?>> menuScreensToRegister = new ArrayList<>();
	private final List<BlockEntityRendererEntry<?>> blockEntityRenderersToRegister = new ArrayList<>();
	private final Map<Latch<? extends Block>, RenderType> renderTypesToRegister = new HashMap<>();
	
	private record MenuScreenEntry<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>>(Latch<MenuType<T>> type, MyScreenConstructor<T, U> cons) {
		void doIt(RegisterMenuScreensEvent e) {
			e.register(type.get(), cons::create);
		}
	}
	
	private record BlockEntityRendererEntry<T extends BlockEntity>(Latch<? extends BlockEntityType<T>> type, BlockEntityRendererProvider<? super T> renderer) {
		private void register(EntityRenderersEvent.RegisterRenderers e) { e.registerBlockEntityRenderer(type.get(), renderer); } //generics moment
	}
	
	@Override
	public <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerMenuScreen(Latch<MenuType<T>> type, MyScreenConstructor<T, U> cons) {
		menuScreensToRegister.add(new MenuScreenEntry<>(type, cons));
	}
	
	@Override
	public <T extends BlockEntity> void setBlockEntityRenderer(Latch<? extends BlockEntityType<T>> type, MyBlockEntityRendererProvider<? super T> renderer) {
		blockEntityRenderersToRegister.add(new BlockEntityRendererEntry<>(type, adaptRendererProvider(renderer)));
	}
	
	@Override
	public void setRenderType(Latch<? extends Block> block, RenderType type) {
		renderTypesToRegister.put(block, type);
	}
	
	public void actuallyRegisterMenuScreens(RegisterMenuScreensEvent e) {
		menuScreensToRegister.forEach(it -> it.doIt(e));
	}
	
	private void actuallySetBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		blockEntityRenderersToRegister.forEach(entry -> entry.register(event));
	}
	
	@SuppressWarnings("deprecation")
	private void actuallySetRenderTypes(FMLClientSetupEvent e) {
		e.enqueueWork(() ->
			renderTypesToRegister.forEach((handle, layer) -> ItemBlockRenderTypes.setRenderLayer(handle.get(), layer)));
		
	}
	
	public static QuatlibClientNeoforge inst() {
		return (QuatlibClientNeoforge) INST;
	}
}
