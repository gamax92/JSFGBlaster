package gamax92.jsfgblaster.item;

import gamax92.jsfgblaster.JSFGBlaster;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

public class JSFGBlasterCard extends Item
{
	public JSFGBlasterCard()
	{
		super();
		setUnlocalizedName("jsfgBlaster");
		setCreativeTab(li.cil.oc.api.CreativeTab.instance);
	}
	
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
	{
		this.itemIcon = par1IconRegister.registerIcon(JSFGBlaster.MODID + ":" + this.getUnlocalizedName().substring(5));
	}
}
