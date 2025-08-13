package agency.highlysuspect.quatlib.craftless.client;

import agency.highlysuspect.quatlib.craftless.facet.Latch;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public abstract class QuatlibClientBase {
	public QuatlibClientBase() {
		INST = this;
	}
	
	public abstract <T extends AbstractContainerMenu, U extends Screen & MenuAccess<T>> void registerMenuScreen(Latch<MenuType<T>> type, MyScreenConstructor<T, U> cons);
	public abstract <T extends BlockEntity> void setBlockEntityRenderer(Latch<? extends BlockEntityType<T>> type, MyBlockEntityRendererProvider<? super T> renderer);
	public abstract void setRenderType(Latch<? extends Block> block, RenderType type);
	
	protected static QuatlibClientBase INST;
	
	public static QuatlibClientBase inst() {
		return INST;
	}
}
