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
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = SafeWalkerMod.MODID, name = SafeWalkerMod.NAME, version = SafeWalkerMod.VERSION)
public class SafeWalkerMod {

    public static final String MODID = "safewalker";
    public static final String NAME = "Safe Walker";
    public static final String VERSION = "2.0"; // Новая версия

    public static KeyBinding keyBind;
    private static boolean isActive = false;
    private static boolean safewalkSneaking = false; // должен ли мод зажимать шифт
    private static boolean userSneaking = false;     // зажат ли шифт игроком вручную

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        keyBind = new KeyBinding("key.safewalker.activate", Keyboard.KEY_V, "key.categories.movement");
        ClientRegistry.registerKeyBinding(keyBind);
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("SafeWalker Mod v2.0 загружен!");
    }

    // ===== Обработка нажатия клавиш =====
    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        // Клавиша V - включение/выключение мода
        if (keyBind.isPressed()) {
            isActive = !isActive;
            String message = "SafeWalker: " + (isActive ? "§aВключен" : "§cВыключен");
            mc.player.sendMessage(new TextComponentString(message));
        }

        // Отслеживаем ручное нажатие Shift
        userSneaking = Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode());
    }

    // ===== Основная логика на каждом тике =====
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.player.world.isRemote && event.player == Minecraft.getMinecraft().player) {
            EntityPlayer player = event.player;
            Minecraft mc = Minecraft.getMinecraft();

            // Если мод выключен — ничего не делаем
            if (!isActive) return;

            // Автоотключение при падении (как в оригинале)
            if (player.fallDistance > 0.4f) {
                isActive = false;
                mc.player.sendMessage(new TextComponentString("SafeWalker: §cВыключен (падение)"));
                return;
            }

            // Получаем блок под игроком (на уровне ног)
            BlockPos posBelow = new BlockPos(player.posX, player.posY - 1.0, player.posZ);
            boolean isAirBelow = player.world.isAirBlock(posBelow);

            // Устанавливаем флаг подкрадывания: true если под игроком пустота и он на земле
            safewalkSneaking = isAirBelow && player.onGround;

            // Применяем состояние клавиши Shift
            applySneakState(mc);
        }
    }

    // Принудительно зажимаем/отпускаем Shift в зависимости от safewalkSneaking и userSneaking
    private void applySneakState(Minecraft mc) {
        if (mc.player == null) return;

        // Если игрок сам зажал Shift — ничего не меняем (приоритет ручного управления)
        if (userSneaking) {
            // Можно ничего не делать, клавиша уже зажата игроком
            return;
        }

        // Иначе устанавливаем состояние клавиши sneak в соответствии с safewalkSneaking
        int sneakKeyCode = mc.gameSettings.keyBindSneak.getKeyCode();
        KeyBinding.setKeyBindState(sneakKeyCode, safewalkSneaking);
    }
