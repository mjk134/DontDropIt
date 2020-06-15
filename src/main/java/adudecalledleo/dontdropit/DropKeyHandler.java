package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.api.ContainerScreenDropHandlerInterface;
import adudecalledleo.dontdropit.api.ContainerScreenExtensions;
import adudecalledleo.dontdropit.api.DefaultDropHandlerInterface;
import adudecalledleo.dontdropit.api.DropHandlerInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;

public class DropKeyHandler {
    private static DropKeyHandler instance;

    private DropKeyHandler() { }

    public static void onClientTick(MinecraftClient mc, DropHandlerInterface dhi) {
        if (instance == null)
            instance = new DropKeyHandler();
        instance.tick(mc, dhi);
    }

    private static ContainerScreenDropHandlerInterface csdhi;

    public static void onClientTick(MinecraftClient mc) {
        if (mc.currentScreen == null)
            onClientTick(mc, DefaultDropHandlerInterface.INSTANCE);
        else if (mc.currentScreen instanceof ContainerScreenExtensions) {
            if (csdhi == null || csdhi.getCse() != mc.currentScreen)
                csdhi = new ContainerScreenDropHandlerInterface((ContainerScreenExtensions) mc.currentScreen);
            onClientTick(mc, csdhi);
        }
    }

    public static int getDropDelayTicks() {
        return DROP_DELAY_TICKS;
    }

    public static int getTickCounter() {
        if (instance == null)
            return 0;
        return instance.dropDelayCounter;
    }

    public static boolean isDroppingEntireStack() {
        if (instance == null)
            return false;
        return instance.controlWasDown;
    }

    private static final int DROP_DELAY_TICKS = 10;
    private int dropDelayCounter = 0;
    private ItemStack currentStack = ItemStack.EMPTY;
    private boolean controlWasDown = false;

    public void tick(MinecraftClient mc, DropHandlerInterface dhi) {
        if (dhi.isDropKeyDown(mc)) {
            if (dropDelayCounter < getDropDelayTicks()) {
                ItemStack stack = dhi.getCurrentStack(mc);
                if (stack.isEmpty() || (dropDelayCounter > 0 && stack != currentStack)) {
                    dropDelayCounter = 0;
                    return;
                }
                if (dropDelayCounter == 0)
                    controlWasDown = Screen.hasControlDown();
                else
                    if (controlWasDown != Screen.hasControlDown()) {
                        dropDelayCounter = 0;
                        return;
                    }
                currentStack = stack;
                dropDelayCounter++;
            } else {
                dropDelayCounter = 0;
                dhi.drop(controlWasDown, mc);
            }
        } else
            dropDelayCounter = 0;
    }
}
