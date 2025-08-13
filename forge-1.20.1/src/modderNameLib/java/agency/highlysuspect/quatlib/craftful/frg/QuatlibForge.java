package agency.highlysuspect.quatlib.craftful.frg;

import agency.highlysuspect.quatlib.craftful.QuatlibMc;
import agency.highlysuspect.quatlib.craftful.frg.client.QuatlibClientForge;
import agency.highlysuspect.quatlib.craftless.QuatlibBase;
import agency.highlysuspect.quatlib.craftless.util.BlockEntityFactory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Set;

@Mod(QuatlibBase.MODID)
public class QuatlibForge extends QuatlibMc {
	public QuatlibForge() {
		super();
		this.modBus = FMLJavaModLoadingContext.get().getModEventBus();
		
		//if i was forge i would simply have client entrypoints
		DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> QuatlibClientForge::new);
	}
	
	public final IEventBus modBus;
	
	@SuppressWarnings("DataFlowIssue") //dfu param
	@Override
	public <T extends BlockEntity> BlockEntityType<T> makeBlockEntityType(BlockEntityFactory<T> factory, Block... blocks) {
		return new BlockEntityType<>(factory::create, Set.of(blocks), null); //Access widened by forge
	}
	
	@Override
	public CreativeModeTab.Builder makeCreativeModeTabBuilder() {
		return CreativeModeTab.builder(); //forge-added zero arg method
	}
	
	public static QuatlibForge inst() {
		return (QuatlibForge) QuatlibBase.INST;
	}
}
