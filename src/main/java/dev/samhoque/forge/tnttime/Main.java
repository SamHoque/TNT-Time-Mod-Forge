package dev.samhoque.forge.tnttime;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderTNTPrimed;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.opengl.GL11;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.awt.*;
import java.text.DecimalFormat;

@Mod(modid = "samhoque.forge.tnttime", name = "TNT Time", version = "1.0.0", acceptedMinecraftVersions = "[1.8.9]")
public class Main  {
    @Instance
    public static Main main;
    private final Minecraft mc = Minecraft.getMinecraft();
    private final DecimalFormat timeFormatter = new DecimalFormat("0.0");
    private int checkTimer;
    private boolean playingBedwars;
    private boolean onHypixel;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onLoin(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        this.onHypixel = (!this.mc.isSingleplayer() && (event.manager.getRemoteAddress().toString().toLowerCase().contains("hypixel.net") || FMLClientHandler.instance().getClient().getCurrentServerData().serverName.equalsIgnoreCase("hypixel")));
    }

    @SubscribeEvent
    public void playerLoggedOut(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        this.onHypixel = false;
    }

    @SubscribeEvent
    public void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || checkTimer++ < 250) {
            return;
        }

        if (!onHypixel || 
            mc.theWorld == null || 
            mc.theWorld.getScoreboard() == null || 
            mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1) == null || 
            mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName() == null
        ) this.playingBedwars = false;
            
        else playingBedwars = EnumChatFormatting.getTextWithoutFormattingCodes(mc.theWorld.getScoreboard().getObjectiveInDisplaySlot(1).getDisplayName()).contains("BED WARS");

        checkTimer = 0;
    }

    public void doRender(RenderTNTPrimed tntRenderer, EntityTNTPrimed tntPrimed, double x, double y, double z, float partialTicks) {
        int fuseTimer = this.playingBedwars ? tntPrimed.fuse - 12 : tntPrimed.fuse;
        double distance = tntPrimed.getDistanceSqToEntity(tntRenderer.getRenderManager().livingPlayer);
        if (fuseTimer < 1 || distance > 4096) return;

        String time = this.timeFormatter.format((fuseTimer - partialTicks) / 20);


        GL11.glPushMatrix();
        GL11.glTranslatef((float) (x + 0.0), (float) (y + tntPrimed.height + 0.5), (float) z);
        GL11.glRotatef(-tntRenderer.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(tntRenderer.getRenderManager().playerViewX * (mc.gameSettings.thirdPersonView == 2 ? -1 : 1), 1.0f, 0.0f, 0.0f);
        GL11.glScaled(-0.03, -0.03, 0.03);

        /* Render the text **/
        GlStateManager.disableLighting();
        FontRenderer fontRenderer = tntRenderer.getFontRendererFromRenderManager();
        float green = Math.min(fuseTimer / (this.playingBedwars ? 52 : 80f), 1f);
        Color color = new Color(1f - green, green, 0);
        fontRenderer.drawString(time, -fontRenderer.getStringWidth(time) / 2, 0, color.getRGB());
        GlStateManager.enableLighting();

        GL11.glPopMatrix();
    }
}
