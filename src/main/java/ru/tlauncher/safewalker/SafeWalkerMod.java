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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Keyboard;

@Mod(modid = SafeWalkerMod.MODID, name = SafeWalkerMod.NAME, version = SafeWalkerMod.VERSION)
public class SafeWalkerMod {

    public static final String MODID = "safewalker";
    public static final String NAME = "Safe Walker";
    public static final String VERSION = "1.1"; // Повысил версию

    public static KeyBinding keyBind;
    private static boolean isActive = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        keyBind = new KeyBinding("key.safewalker.activate", Keyboard.KEY_V, "key.categories.movement");
        ClientRegistry.registerKeyBinding(keyBind);
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("SafeWalker Mod v1.1 загружен!");
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player.world.isRemote && event.player == Minecraft.getMinecraft().player) {
            if (isActive) {
                EntityPlayer player = event.player;
                
                // Проверяем, стоит ли игрок на земле
                if (player.onGround) {
                    // Получаем позицию блока ПОД ногами игрока
                    BlockPos playerPos = new BlockPos(player.posX, player.posY - 0.1, player.posZ);
                    
                    // Проверяем края ВОКРУГ игрока (со всех 4 сторон)
                    boolean dangerNorth = isBlockEdge(player, playerPos.north());
                    boolean dangerSouth = isBlockEdge(player, playerPos.south());
                    boolean dangerEast = isBlockEdge(player, playerPos.east());
                    boolean dangerWest = isBlockEdge(player, playerPos.west());
                    
                    // Проверяем диагональные направления (для полной блокировки)
                    boolean dangerNorthWest = isBlockEdge(player, playerPos.north().west());
                    boolean dangerNorthEast = isBlockEdge(player, playerPos.north().east());
                    boolean dangerSouthWest = isBlockEdge(player, playerPos.south().west());
                    boolean dangerSouthEast = isBlockEdge(player, playerPos.south().east());
                    
                    // Если есть опасность с любой стороны - блокируем движение
                    if (dangerNorth || dangerSouth || dangerEast || dangerWest ||
                        dangerNorthWest || dangerNorthEast || dangerSouthWest || dangerSouthEast) {
                        
                        // Полная остановка движения
                        player.motionX = 0;
                        player.motionZ = 0;
                        
                        // НЕбольшая коррекция позиции, чтобы игрок не "проваливался"
                        if (dangerNorth && player.motionZ < 0) player.motionZ = 0;
                        if (dangerSouth && player.motionZ > 0) player.motionZ = 0;
                        if (dangerWest && player.motionX < 0) player.motionX = 0;
                        if (dangerEast && player.motionX > 0) player.motionX = 0;
                    }
                }
            }
        }
    }

    private boolean isBlockEdge(EntityPlayer player, BlockPos checkPos) {
        // Проверяем, есть ли блок под проверяемой позицией
        BlockPos blockBelow = checkPos.down();
        
        // Если блок под проверяемой позицией - воздух, значит это край
        if (player.world.isAirBlock(blockBelow)) {
            return true;
        }
        
        return false;
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
