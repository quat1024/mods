package agency.highlysuspect.packages.craftful.frg.client;

import agency.highlysuspect.packages.craftful.client.PClientBlockEventHandlers;
import agency.highlysuspect.packages.craftful.client.PackagesClient;
import agency.highlysuspect.packages.craftful.frg.PackagesNeo;
import agency.highlysuspect.packages.craftful.frg.client.model.ForgePackageMakerModel;
import agency.highlysuspect.packages.craftful.frg.client.model.ForgePackageModel;
import agency.highlysuspect.packages.craftful.frg.client.model.NoConfigGeometryLoader;
import agency.highlysuspect.packages.craftful.frg.net.ActionPacketNeo;
import agency.highlysuspect.packages.craftful.net.ActionPacket;
import agency.highlysuspect.packages.craftless.PackagesBase;
import agency.highlysuspect.quatlib.craftful.neo.NeoBackedConfig_V1;
import agency.highlysuspect.quatlib.craftless.config.ConfigSection;
import agency.highlysuspect.quatlib.craftless.config.WritableConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod(value = PackagesBase.MODID, dist = Dist.CLIENT)
public class PackagesNeoClient extends PackagesClient {
	protected final IEventBus modBus;
	protected final ModContainer modContainer;
	
	public PackagesNeoClient() {
		modBus = PackagesNeo.inst().modBus;
		modContainer = PackagesNeo.inst().modContainer;
		
		earlySetup();
		
		//I originally only used PlayerInteractEvent.LeftClickBlock in all situations. However, in Creative mode, this
		//event is fired *after* sending the START_DESTROY_BLOCK action to the server, and there is no way to cancel the
		//event in a way that suppresses the packet. (See Forge's patches to MultiPlayerGameMode, ctrl-f for "LeftClick".)
		//
		//I need to suppress the packet because sending the action seems to tick up the serverside creative mode breaking
		//timer, or something? Basically, after clicking the package for more than ~5ish cumulative ticks, it would break.
		//
		//This isn't an issue on Fabric because cancelling the "i am about to start breaking this block" event on the client
		//suppresses the "i am about to start breaking this block" packet too, which is arguably the correct behavior.
		//I'm not sure in what cases Forge behavior makes sense.
		//
		//See MixinMultiPlayerGameMode.
		//
		//Still, let's try to be good netizens and use the standard LeftClickBlock event in noncreative.
		//In non creative gamemodes it works just fine for my purposes.
		NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.LeftClickBlock.class, (event) -> {
			if(event.isCanceled() || event.getSide() != LogicalSide.CLIENT || event.getEntity().isSpectator()) return;
			if(event.getEntity().isCreative()) return; //Creative mode handled via mixin
			
			InteractionResult r = PClientBlockEventHandlers.onHoldLeftClick(event.getEntity(), event.getLevel(), event.getHand(), event.getPos(), event.getFace());
			if(r.consumesAction()) {
				event.setCanceled(true);
//				event.setCancellationResult(InteractionResult.CONSUME);
				event.setUseBlock(TriState.FALSE);
				event.setUseItem(TriState.FALSE);
			}
		});
		
		//I originally tried PlayerInteractEvent.RightClickBlock. However, there is also no way to cancel this event
		//in a way that suppresses the "i just right clicked this item" packet as well. This is on purpose?
		//There's a line of code in MultiPlayerGameMode#useItemOn that sends a use-item packet when the event *is* cancelled.
		//This honestly just seems like a mistake, or poor api design that I can't wrap my head around.
		
		NeoForge.EVENT_BUS.addListener(InputEvent.InteractionKeyMappingTriggered.class, (event) -> {
			if(event.isCanceled() || !event.isUseItem()) return;
			
			Minecraft minecraft = Minecraft.getInstance();
			Player player = minecraft.player;
			Level level = minecraft.level;
			HitResult hit = minecraft.hitResult;
			if(player == null || level == null || !(hit instanceof BlockHitResult bhr)) return;
			
			//TODO: Since I can't use RightClickBlock, I might have to reimplement hand logic?
			//In practice the mod's "take items from places other than the active hand" system seems to work okay
			InteractionResult r = PClientBlockEventHandlers.onRightClick(player, level, InteractionHand.MAIN_HAND, bhr);
			if(r.consumesAction()) event.setCanceled(true);
		});
	}
	
	@Override
	public void setupCustomModelLoaders() {
		modBus.addListener((ModelEvent.RegisterGeometryLoaders e) -> {
			e.register(PackagesNeo.rl("forge_package_model_loader")      , new NoConfigGeometryLoader<>(ForgePackageModel::new));
			e.register(PackagesNeo.rl("forge_package_maker_model_loader"), new NoConfigGeometryLoader<>(ForgePackageMakerModel::new));
		});
	}
	
	@Override
	public void sendActionPacket(ActionPacket packet) {
		PacketDistributor.sendToServer(new ActionPacketNeo(packet));
	}
	
	//config
	@Override
	public WritableConfig makeClientConfig(ConfigSection schema) {
		return NeoBackedConfig_V1.make(failures.context(), schema, modBus, modContainer, ModConfig.Type.CLIENT)
			.addReloadHook(this::onConfigReload);
	}
}
