package agency.highlysuspect.quatlib.craftful.frg.client;

import agency.highlysuspect.quatlib.craftful.client.QuatlibClientMc;
import agency.highlysuspect.quatlib.craftful.frg.QuatlibForge;
import agency.highlysuspect.quatlib.craftless.client.MyBlockEntityRendererProvider;
import agency.highlysuspect.quatlib.craftless.client.MyScreenConstructor;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import net.minecraft.client.gui.screens.MenuScreens;
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
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuatlibClientForge extends QuatlibClientMc {
	public QuatlibClientForge() {
		super();
		
		this.modBus = QuatlibForge.inst().modBus;
		modBus.addListener(this::actuallyRegisterMenuScreens);
		modBus.addListener(this::actuallySetBlockEntityRenderers);
		modBus.addListener(this::actuallySetRenderTypes);
	}
	
	protected final IEventBus modBus;
	
	private final List<MenuScreenEntry<?, ?>> menuScreensToRegister = new ArrayList<>();
	private final List<BlockEntityRendererEntry<?>> blockEntityRenderersToRegister = new ArrayList<>();
	private final Map<Latch<? extends Block>, RenderType> renderTypesToRegister = new HashMap<>();
	
	private record MenuScreenEntry<T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>>(Latch<MenuType<T>> type, MyScreenConstructor<T, U> cons) {
		private void register() { MenuScreens.register(type.get(), cons::create); } //generics moment
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
	
	private void actuallyRegisterMenuScreens(FMLClientSetupEvent e) {
		menuScreensToRegister.forEach(MenuScreenEntry::register);
	}
	
	private void actuallySetBlockEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		blockEntityRenderersToRegister.forEach(entry -> entry.register(event));
	}
	
	@SuppressWarnings("deprecation")
	private void actuallySetRenderTypes(FMLClientSetupEvent e) {
		renderTypesToRegister.forEach((handle, layer) -> ItemBlockRenderTypes.setRenderLayer(handle.get(), layer));
	}
	
	public static QuatlibClientForge inst() {
		return (QuatlibClientForge) INST;
	}
}
