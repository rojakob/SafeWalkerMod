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
import net.minecraft.init.Blocks;
import org.lwjgl.input.Keyboard;

@Mod(modid = SafeWalkerMod.MODID, name = SafeWalkerMod.NAME, version = SafeWalkerMod.VERSION)
public class SafeWalkerMod {

    public static final String MODID = "safewalker";
    public static final String NAME = "Safe Walker";
    public static final String VERSION = "2.1"; // небольшая версия

    public static KeyBinding keyBind;
    private static boolean isActive = false;
    private static boolean safewalkSneaking = false;
    private static boolean userSneaking = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        keyBind = new KeyBinding("key.safewalker.activate", Keyboard.KEY_V, "key.categories.movement");
        ClientRegistry.registerKeyBinding(keyBind);
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("SafeWalker Mod v2.1 загружен!");
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        if (keyBind.isPressed()) {
            isActive = !isActive;
            String message = "SafeWalker: " + (isActive ? "§aВключен" : "§cВыключен");
            mc.player.sendMessage(new TextComponentString(message));
        }

        userSneaking = Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player.world.isRemote && event.player == Minecraft.getMinecraft().player) {
            EntityPlayer player = event.player;
            Minecraft mc = Minecraft.getMinecraft();

            if (!isActive) return;

            // Автоотключение при падении (можно оставить или убрать позже)
            if (player.fallDistance > 0.4f) {
                isActive = false;
                mc.player.sendMessage(new TextComponentString("SafeWalker: §cВыключен (падение)"));
                return;
            }

            BlockPos posBelow = new BlockPos(player.posX, player.posY - 1.0, player.posZ);
            boolean isAirBelow = player.world.isAirBlock(posBelow);

            safewalkSneaking = isAirBelow && player.onGround;

            applySneakState(mc);
        }
    }

    private void applySneakState(Minecraft mc) {
        if (mc.player == null) return;

        if (userSneaking) {
            return;
        }

        int sneakKeyCode = mc.gameSettings.keyBindSneak.getKeyCode();
        KeyBinding.setKeyBindState(sneakKeyCode, safewalkSneaking);
    }
}
