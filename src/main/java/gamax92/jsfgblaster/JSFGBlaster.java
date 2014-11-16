package gamax92.jsfgblaster;

import li.cil.oc.integration.Mods;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import pl.asie.lib.network.PacketHandler;
import gamax92.jsfgblaster.audio.SFXRPlaybackManager;
import gamax92.jsfgblaster.driver.DriverJSFGBlasterCard;
import gamax92.jsfgblaster.item.JSFGBlasterCard;
import gamax92.jsfgblaster.network.NetworkHandlerClient;
import gamax92.jsfgblaster.network.NetworkHandlerServer;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = JSFGBlaster.MODID, name = JSFGBlaster.NAME, version = JSFGBlaster.VERSION, dependencies = "required-after:OpenComputers@[1.4.0,)")
public class JSFGBlaster
{
    public static final String MODID = "jsfgblaster";
    public static final String NAME = "JSFGBlaster";
    public static final String VERSION = "1.0";
    
    @Instance
    public static JSFGBlaster instance;
    
    @SidedProxy(clientSide = "gamax92.jsfgblaster.ClientProxy", serverSide = "gamax92.jsfgblaster.CommonProxy")
	public static CommonProxy proxy;
    
    public static JSFGBlasterCard jsfgblasterCard;
	private Logger log;
	public SFXRPlaybackManager audio;
	public static PacketHandler packet;
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
    	log = LogManager.getLogger(JSFGBlaster.MODID);
    	
		audio = new SFXRPlaybackManager(proxy.isClient());
		packet = new PacketHandler(JSFGBlaster.MODID, new NetworkHandlerClient(), new NetworkHandlerServer());
    	
    	jsfgblasterCard = new JSFGBlasterCard();
    	GameRegistry.registerItem(jsfgblasterCard, "jsfgBlaster");
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {
    	GameRegistry.addRecipe(new ItemStack(jsfgblasterCard),
        	"NM ",
        	" C ",
        	'N', Blocks.noteblock,
        	'M', li.cil.oc.api.Items.get("chip1").createItemStack(1),
        	'C', li.cil.oc.api.Items.get("card").createItemStack(1)
        );
    	
    	li.cil.oc.api.Driver.add(new DriverJSFGBlasterCard());
    }
}
