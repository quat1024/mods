package agency.highlysuspect.quatlib.craftful.fab.client;

import agency.highlysuspect.quatlib.craftful.client.QuatlibClientMc;
import agency.highlysuspect.quatlib.craftless.client.MyBlockEntityRendererProvider;
import agency.highlysuspect.quatlib.craftless.client.MyScreenConstructor;
import agency.highlysuspect.quatlib.craftless.fab.AfterQuatlibClientInitializer;
import agency.highlysuspect.quatlib.craftless.facet.Latch;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class QuatlibClientFabric extends QuatlibClientMc implements ClientModInitializer {
	public QuatlibClientFabric() {
		super();
	}
	
	@Override
	public void onInitializeClient() {
		FabricLoader.getInstance().invokeEntrypoints("modder_name_lib:after_client", AfterQuatlibClientInitializer.class, AfterQuatlibClientInitializer::onInitializeClient);
	}
	
	//only possible to do this stuff immediately because fabric doesn't have reg events
	
	@Override
	public <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerMenuScreen(Latch<MenuType<T>> type, MyScreenConstructor<T, U> cons) {
		MenuScreens.register(type.get(), cons::create);
	}
	
	@Override
	public <T extends BlockEntity> void setBlockEntityRenderer(Latch<? extends BlockEntityType<T>> type, MyBlockEntityRendererProvider<? super T> renderer) {
		BlockEntityRenderers.register(type.get(), QuatlibClientMc.inst().adaptRendererProvider(renderer)); //fabric-transitive-access-wideners
	}
	
	@Override
	public void setRenderType(Latch<? extends Block> block, RenderType type) {
		BlockRenderLayerMap.INSTANCE.putBlock(block.get(), type);
	}
	
	//
	
	public static QuatlibClientFabric inst() {
		return (QuatlibClientFabric) INST;
	}
}
