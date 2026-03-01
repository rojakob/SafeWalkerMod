package ru.tlauncher.safewalker;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.input.Keyboard;

@Mod(modid = SafeWalkerMod.MODID, name = SafeWalkerMod.NAME, version = SafeWalkerMod.VERSION)
public class SafeWalkerMod {

    public static final String MODID = "safewalker";
    public static final String NAME = "Safe Walker";
    public static final String VERSION = "1.0";

    public static KeyBinding keyBind = new KeyBinding("key.safewalker.activate", Keyboard.KEY_V, "key.categories.movement");
    private static boolean isActive = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientRegistry.registerKeyBinding(keyBind);
        System.out.println("SafeWalker Mod загружен!");
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player.world.isRemote && event.player == Minecraft.getMinecraft().player && isActive) {
            EntityPlayer player = event.player;

            if (player.onGround && !player.isSneaking()) {
                if (isBlockEdgeAhead(player)) {
                    player.motionX = 0;
                    player.motionZ = 0;
                }
            }
        }
    }

    private boolean isBlockEdgeAhead(EntityPlayer player) {
        double yaw = Math.toRadians(player.rotationYaw);
        double dirX = -Math.sin(yaw);
        double dirZ = Math.cos(yaw);

        double checkX = player.posX + dirX * 0.3;
        double checkY = player.posY - 0.5;
        double checkZ = player.posZ + dirZ * 0.3;

        return player.world.isAirBlock(new BlockPos(checkX, checkY, checkZ));
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        if (keyBind.isPressed()) {
            isActive = !isActive;
            if (Minecraft.getMinecraft().player != null) {
                Minecraft.getMinecraft().player.sendChatMessage("SafeWalker: " + (isActive ? "§aВключен" : "§cВыключен"));
            }
        }
    }
}