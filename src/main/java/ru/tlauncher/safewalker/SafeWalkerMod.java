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
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.lwjgl.input.Keyboard;

@Mod(modid = SafeWalkerMod.MODID, name = SafeWalkerMod.NAME, version = SafeWalkerMod.VERSION)
public class SafeWalkerMod {

    public static final String MODID = "safewalker";
    public static final String NAME = "Safe Walker";
    public static final String VERSION = "1.4";

    public static KeyBinding keyBind;
    private static boolean isActive = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        keyBind = new KeyBinding("key.safewalker.activate", Keyboard.KEY_V, "key.categories.movement");
        ClientRegistry.registerKeyBinding(keyBind);
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("SafeWalker Mod v1.4 загружен!");
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Проверяем, что это клиентский тик и игрок — наш
        if (event.phase == TickEvent.Phase.END && event.player.world.isRemote && event.player == Minecraft.getMinecraft().player) {
            if (isActive) {
                EntityPlayer player = event.player;
                // Приводим к клиентскому игроку, чтобы получить movementInput
                if (!(player instanceof EntityPlayerSP)) return;
                EntityPlayerSP playerSP = (EntityPlayerSP) player;

                // Работает только когда игрок на земле и не крадётся (чтобы можно было спрыгнуть с Shift)
                if (player.onGround && !player.isSneaking()) {
                    // Получаем направление взгляда игрока
                    float yaw = player.rotationYaw;

                    // Получаем нажатия клавиш (WASD) через movementInput клиентского игрока
                    float forward = playerSP.movementInput.moveForward;
                    float strafe = playerSP.movementInput.moveStrafe;

                    // Если игрок не нажимает клавиши движения — пропускаем
                    if (forward == 0 && strafe == 0) return;

                    // Вычисляем вектор направления движения на основе угла обзора и нажатий
                    float sin = (float) Math.sin(Math.toRadians(yaw));
                    float cos = (float) Math.cos(Math.toRadians(yaw));

                    double dirX = strafe * cos - forward * sin;
                    double dirZ = forward * cos + strafe * sin;

                    // Нормализуем вектор (для диагоналей)
                    double length = Math.sqrt(dirX * dirX + dirZ * dirZ);
                    if (length < 0.001) return;

                    dirX /= length;
                    dirZ /= length;

                    // Координаты блока, в который игрок собирается шагнуть (на уровне ног)
                    double checkX = player.posX + dirX * 0.3;
                    double checkY = player.posY - 0.5; // уровень ног
                    double checkZ = player.posZ + dirZ * 0.3;

                    BlockPos checkPos = new BlockPos(checkX, checkY, checkZ);

                    // Если в этом месте воздух — это край, останавливаем игрока
                    if (player.world.isAirBlock(checkPos)) {
                        player.motionX = 0;
                        player.motionZ = 0;
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
