package ru.tlauncher.safewalker;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Keyboard;

@Mod(modid = SafeWalkerMod.MODID, name = SafeWalkerMod.NAME, version = SafeWalkerMod.VERSION)
public class SafeWalkerMod {

    public static final String MODID = "safewalker";
    public static final String NAME = "Safe Walker";
    public static final String VERSION = "1.0";

    public static KeyBinding keyBind;
    private static boolean isActive = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Создаем и регистрируем клавишу
        keyBind = new KeyBinding("key.safewalker.activate", Keyboard.KEY_V, "key.categories.movement");
        ClientRegistry.registerKeyBinding(keyBind);
        
        // ВАЖНО: Регистрируем этот класс для получения событий
        MinecraftForge.EVENT_BUS.register(this);
        
        System.out.println("SafeWalker Mod загружен и зарегистрирован!");
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Проверяем, что это клиент и наш игрок
        if (event.phase == TickEvent.Phase.END && event.player.world.isRemote && event.player == Minecraft.getMinecraft().player) {
            if (isActive) {
                EntityPlayer player = event.player;

                if (player.onGround && !player.isSneaking()) {
                    if (isBlockEdgeAhead(player)) {
                        player.motionX = 0;
                        player.motionZ = 0;
                    }
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
                String message = "SafeWalker: " + (isActive ? "§aВключен" : "§cВыключен");
                Minecraft.getMinecraft().player.sendMessage(new TextComponentString(message));
            }
        }
    }
}
