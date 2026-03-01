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
    public static final String VERSION = "1.2";

    public static KeyBinding keyBind;
    private static boolean isActive = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        keyBind = new KeyBinding("key.safewalker.activate", Keyboard.KEY_V, "key.categories.movement");
        ClientRegistry.registerKeyBinding(keyBind);
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("SafeWalker Mod v1.2 загружен!");
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player.world.isRemote && event.player == Minecraft.getMinecraft().player) {
            if (isActive) {
                EntityPlayer player = event.player;
                
                // Работает только когда игрок на земле и не крадётся (чтобы можно было спрыгнуть с Shift)
                if (player.onGround && !player.isSneaking()) {
                    // Получаем скорость движения игрока
                    double motionX = player.motionX;
                    double motionZ = player.motionZ;
                    double speedSq = motionX * motionX + motionZ * motionZ;
                    
                    // Если игрок движется горизонтально
                    if (speedSq > 0.0001) {
                        double dist = 0.3; // дистанция проверки (половина блока)
                        double len = Math.sqrt(speedSq);
                        // Направление движения (нормализованный вектор)
                        double dirX = motionX / len;
                        double dirZ = motionZ / len;
                        
                        // Координаты блока, куда игрок собирается ступить
                        double checkX = player.posX + dirX * dist;
                        double checkY = player.posY - 0.5; // уровень ног
                        double checkZ = player.posZ + dirZ * dist;
                        
                        BlockPos checkPos = new BlockPos(checkX, checkY, checkZ);
                        BlockPos blockBelow = checkPos.down(); // блок под предполагаемой позицией
                        
                        // Если под этим местом пустота (воздух) — это край
                        if (player.world.isAirBlock(blockBelow)) {
                            // Останавливаем игрока
                            player.motionX = 0;
                            player.motionZ = 0;
                        }
                    }
                }
            }
        }
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
