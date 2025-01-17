package com.github.lehjr.modularpowerarmor.client.event;

import com.google.common.util.concurrent.AtomicDouble;
import com.github.lehjr.mpalib.capabilities.inventory.modechanging.IModeChangingItem;
import com.github.lehjr.mpalib.capabilities.inventory.modularitem.IModularItem;
import com.github.lehjr.mpalib.client.gui.hud.meters.*;
import com.github.lehjr.mpalib.client.render.Renderer;
import com.github.lehjr.mpalib.energy.ElectricItemUtils;
import com.github.lehjr.mpalib.heat.HeatUtils;
import com.github.lehjr.mpalib.math.MathUtils;
import com.github.lehjr.mpalib.string.StringUtils;
import com.github.lehjr.modularpowerarmor.basemod.MPAObjects;
import com.github.lehjr.modularpowerarmor.basemod.MPARegistryNames;
import com.github.lehjr.modularpowerarmor.basemod.config.ClientConfig;
import com.github.lehjr.modularpowerarmor.client.control.KeybindManager;
import com.github.lehjr.modularpowerarmor.client.gui.clickable.ClickableKeybinding;
import com.github.lehjr.modularpowerarmor.item.module.environmental.AutoFeederModule;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.CapabilityItemHandler;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This handler is called before/after the game processes input events and
 * updates the gui state mainly. *independent of rendering, so don't do rendering here
 * -is also the parent class of KeyBindingHandleryBaseIcon
 *
 * @author MachineMuse
 */
public class ClientTickHandler {
    public ArrayList<String> modules;
    protected HeatMeter heat = null;
    protected HeatMeter energy = null;
    protected WaterMeter water = null;
    protected FluidMeter fluidMeter = null;
    protected PlasmaChargeMeter plasma = null;
    MPAObjects mpsi = MPAObjects.INSTANCE;
    static final ItemStack food = new ItemStack(Items.COOKED_BEEF);
    static final ResourceLocation autoFeederReg = new ResourceLocation(MPARegistryNames.MODULE_AUTO_FEEDER__REGNAME);
    static final ResourceLocation clockReg = new ResourceLocation(MPARegistryNames.MODULE_CLOCK__REGNAME);
    static final ResourceLocation compassReg = new ResourceLocation(MPARegistryNames.MODULE_COMPASS__REGNAME);
    static final ResourceLocation plasmaCannon = new ResourceLocation(MPARegistryNames.MODULE_PLASMA_CANNON__REGNAME);

    @SubscribeEvent
    public void onPreClientTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getInstance().player == null)
            return;

        if (event.phase == TickEvent.Phase.START) {
            for (ClickableKeybinding kb : KeybindManager.INSTANCE.getKeybindings()) {
                kb.doToggleTick();
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onRenderTickEvent(TickEvent.RenderTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null)
            return;


        int yOffsetString = 18;
        double yOffsetIcon = 16.0;
        double yBaseIcon;
        int yBaseString;
        if (ClientConfig.HUD_USE_GRAPHICAL_METERS.get()) {
            yBaseIcon = 150.0;
            yBaseString = 155;
        } else {
            yBaseIcon = 26.0;
            yBaseString = 32;
        }

        if (event.phase == TickEvent.Phase.END) {
            PlayerEntity player = minecraft.player;
            modules = new ArrayList<>();
            if (player != null && minecraft.isGuiEnabled() && minecraft.currentScreen == null) {
                Minecraft mc = minecraft;
                MainWindow screen = mc.mainWindow;

                // Misc Overlay Items ---------------------------------------------------------------------------------
                AtomicInteger index = new AtomicInteger(0);

                // Helmet modules with overlay
                player.getItemStackFromSlot(EquipmentSlotType.HEAD).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(h -> {
                    if (!(h instanceof IModularItem))
                        return;

                    // AutoFeeder
                    ItemStack autoFeeder = ((IModularItem) h).getOnlineModuleOrEmpty(autoFeederReg);
                    if (!autoFeeder.isEmpty()) {
                        int foodLevel = (int) ((AutoFeederModule) autoFeeder.getItem()).getFoodLevel(autoFeeder);
                        String num = StringUtils.formatNumberShort(foodLevel);
                        Renderer.drawString(num, 17, yBaseString + (yOffsetString * index.get()));
                        Renderer.drawItemAt(-1.0, yBaseIcon + (yOffsetIcon * index.get()), food);
                        index.addAndGet(1);
                    }

                    // Clock
                    ItemStack clock = ((IModularItem) h).getOnlineModuleOrEmpty(clockReg);
                    if (!clock.isEmpty()) {
                        String ampm;
                        long time = player.world.getGameTime();
                        long hour = ((time % 24000) / 1000);
                        if (ClientConfig.HUD_USE_24_HOUR_CLOCK.get()) {
                            if (hour < 19) {
                                hour += 6;
                            } else {
                                hour -= 18;
                            }
                            ampm = "h";
                        } else {
                            if (hour < 6) {
                                hour += 6;
                                ampm = " AM";
                            } else if (hour == 6) {
                                hour = 12;
                                ampm = " PM";
                            } else if (hour > 6 && hour < 18) {
                                hour -= 6;
                                ampm = " PM";
                            } else if (hour == 18) {
                                hour = 12;
                                ampm = " AM";
                            } else {
                                hour -= 18;
                                ampm = " AM";
                            }

                            Renderer.drawString(hour + ampm, 17, yBaseString + (yOffsetString * index.get()));
                            Renderer.drawItemAt(-1.0, yBaseIcon + (yOffsetIcon * index.get()), clock);

                            index.addAndGet(1);
                        }
                    }

                    // Compass
                    ItemStack compass = ((IModularItem) h).getOnlineModuleOrEmpty(compassReg);
                    if (!compass.isEmpty()) {

                        Renderer.drawItemAt(-1.0, yBaseIcon + (yOffsetIcon * index.get()), compass);
                        index.addAndGet(1);
                    }
                });

                // Meters ---------------------------------------------------------------------------------------------
                double top = (double) screen.getScaledHeight() / 2.0 - (double) 16;
//    	double left = screen.getScaledWidth() - 2;
                double left = screen.getScaledWidth() - 34;

                // energy
                double maxEnergy = ElectricItemUtils.getMaxPlayerEnergy(player);
                double currEnergy = ElectricItemUtils.getPlayerEnergy(player);
                String currEnergyStr = StringUtils.formatNumberShort(currEnergy) + "RF";
                String maxEnergyStr = StringUtils.formatNumberShort(maxEnergy);

                // heat
                double maxHeat = HeatUtils.getPlayerMaxHeat(player);
                double currHeat = HeatUtils.getPlayerHeat(player);
                String currHeatStr = StringUtils.formatNumberShort(currHeat);
                String maxHeatStr = StringUtils.formatNumberShort(maxHeat);

                // Fluid
                AtomicDouble currFluid = new AtomicDouble(0);
                AtomicDouble maxFluid = new AtomicDouble(0);
                String currFluidStr = "";
                String maxFluidStr = "";

//                player.getItemStackFromSlot(EquipmentSlotType.CHEST).getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY).ifPresent(fh -> {
//                              for (IFluidTankProperties prop : fh.getTankProperties()) {
//                                  FluidStack stack = prop.getContents();
//                                  if (stack!= null) {
//                                      fluidMeter = new FluidMeter(stack.getFluid());
//                                      maxFluid.getAndAdd(prop.getCapacity());
//                                      currFluid.addAndGet(stack.amount);
//                                  }
//                              }
//                });

                // Plasma
                AtomicDouble currentPlasma = new AtomicDouble(0);
                AtomicDouble maxPlasma = new AtomicDouble(0);
                if (player.isHandActive())
                    player.getHeldItem(player.getActiveHand()).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).ifPresent(modechanging -> {
                        if (!(modechanging instanceof IModeChangingItem))
                            return;

                        ItemStack module = ((IModeChangingItem) modechanging).getActiveModule();
                        int actualCount = 0;

                        int maxDuration = ((IModeChangingItem) modechanging).getModularItemStack().getUseDuration();
                        if (!module.isEmpty()) {
                            // Plasma Cannon
                            if (module.getItem().getRegistryName().equals(plasmaCannon)) {
                                actualCount = (maxDuration - player.getItemInUseCount());
                                currentPlasma.getAndAdd((actualCount > 50 ? 50 : actualCount) * 2);
                                maxPlasma.getAndAdd(100);

                            // Ore Scanner or whatever
                            } else {
                                actualCount = (maxDuration - player.getItemInUseCount());
                                currentPlasma.getAndAdd((actualCount > 40 ? 40 : actualCount) * 2.5);
                                maxPlasma.getAndAdd(100);
                            }
                        }
                    });
                String currPlasmaStr = StringUtils.formatNumberShort(currentPlasma.get());
                String maxPlasmaStr = StringUtils.formatNumberShort(maxPlasma.get());


                if (ClientConfig.HUD_USE_GRAPHICAL_METERS.get()) {
                    int numMeters = 0;

                    if (maxEnergy > 0) {
                        numMeters++;
                        if (energy == null) {
                            energy = new EnergyMeter();
                        }
                    } else energy = null;

                    if (maxHeat > 0) {
                        numMeters++;
                        if (heat == null)
                            heat = new HeatMeter();
                    } else heat = null;

                    if (maxFluid.get() > 0 && fluidMeter != null) {
                        numMeters++;
                     }

                    if (maxPlasma.get() > 0 /* && drawPlasmaMeter */) {
                        numMeters++;
                        if (plasma == null) {
                            plasma = new PlasmaChargeMeter();
                        }
                    } else plasma = null;

                    double stringX = left - 2;
                    final int totalMeters = numMeters;
                    //"(totalMeters-numMeters) * 8" = 0 for whichever of these is first,
                    //but including it won't hurt and this makes it easier to swap them around.

                    if (energy != null) {
                        energy.draw(left, top + (totalMeters - numMeters) * 8, currEnergy / maxEnergy);
                        Renderer.drawRightAlignedString(currEnergyStr, stringX, top);
                        numMeters--;
                    }

                    if (heat != null) {
                        heat.draw(left, top + (totalMeters - numMeters) * 8, MathUtils.clampDouble(currHeat, 0, maxHeat) / maxHeat);
                        Renderer.drawRightAlignedString(currHeatStr, stringX, top + (totalMeters - numMeters) * 8);
                        numMeters--;
                    }

                    if (fluidMeter != null) {
                        fluidMeter.draw(left, top + (totalMeters - numMeters) * 8, currFluid.get() / maxFluid.get());
                        Renderer.drawRightAlignedString(currFluidStr, stringX, top + (totalMeters - numMeters) * 8);
                        numMeters--;
                    }

                    if (plasma != null) {
                        plasma.draw(left, top + (totalMeters - numMeters) * 8, currentPlasma.get() / maxPlasma.get());
                        Renderer.drawRightAlignedString(currPlasmaStr, stringX, top + (totalMeters - numMeters) * 8);
                    }

                } else {
                    int numReadouts = 0;
                    if (maxEnergy > 0) {
                        Renderer.drawString(currEnergyStr + '/' + maxEnergyStr + " \u1D60", 2, 2);
                        numReadouts += 1;
                    }

                    Renderer.drawString(currHeatStr + '/' + maxHeatStr + " C", 2, 2 + (numReadouts * 9));
                    numReadouts += 1;

                    if (maxFluid.get() > 0) {
                        Renderer.drawString(currFluidStr + '/' + maxFluidStr + " buckets", 2, 2 + (numReadouts * 9));
                        numReadouts += 1;
                    }

                    if (maxPlasma.get() > 0 /* && drawPlasmaMeter */) {
                        Renderer.drawString(currPlasmaStr + '/' + maxPlasmaStr + "%", 2, 2 + (numReadouts * 9));
                    }
                }
            }
        }
    }
}
