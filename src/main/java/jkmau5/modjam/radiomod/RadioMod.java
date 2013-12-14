package jkmau5.modjam.radiomod;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStoppedEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;
import jkmau5.modjam.radiomod.block.BlockAntenna;
import jkmau5.modjam.radiomod.block.BlockRadio;
import jkmau5.modjam.radiomod.item.ItemMediaPlayer;
import jkmau5.modjam.radiomod.network.PacketHandler;
import jkmau5.modjam.radiomod.network.RadioWorldHandler;
import jkmau5.modjam.radiomod.server.ProxyCommon;
import jkmau5.modjam.radiomod.tile.TileEntityRadio;
import net.minecraft.creativetab.CreativeTabs;

import java.util.Random;

@Mod(modid = Constants.MODID)
@NetworkMod(clientSideRequired = true, channels = {Constants.MODID}, packetHandler = PacketHandler.class)
public class RadioMod {

    public BlockRadio blockRadio;
    public BlockAntenna blockAntenna;
    public ItemMediaPlayer itemMediaPlayer;

    public static RadioWorldHandler radioWorldHandler;

    public static final CreativeTabs tabRadioMod = new CreativeTabs("RadioMod");

    @SidedProxy(modId = Constants.MODID, clientSide = "jkmau5.modjam.radiomod.client.ProxyClient", serverSide = "jkmau5.modjam.radiomod.server.ProxyCommon")
    public static ProxyCommon proxy;

    public static String getUniqueRadioID() {
        Random random = new Random();
        return "Radio-" + (random.nextInt(8999999) + 1000000);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        blockRadio = new BlockRadio(2500);
        blockAntenna = new BlockAntenna(2501);
        GameRegistry.registerBlock(blockRadio, "BlockRadio");
        GameRegistry.registerBlock(blockAntenna, "BlockAntenna");

        GameRegistry.registerTileEntity(TileEntityRadio.class, "TileRadio");

        proxy.preInit();

        //--------

        itemMediaPlayer = new ItemMediaPlayer(5000);
        GameRegistry.registerItem(itemMediaPlayer, "ItemMediaPlayer");
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        LanguageRegistry.addName(blockRadio, "Radio Block");
        LanguageRegistry.addName(itemMediaPlayer, "Media Player");

        proxy.init();

        TickRegistry.registerTickHandler(new RadioTickHandler(), Side.SERVER);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartedEvent event) {
        radioWorldHandler = new RadioWorldHandler();
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppedEvent event) {
        radioWorldHandler = null;
    }
}
