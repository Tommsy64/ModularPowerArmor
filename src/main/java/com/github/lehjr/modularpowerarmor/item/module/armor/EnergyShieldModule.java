package com.github.lehjr.modularpowerarmor.item.module.armor;

import com.github.lehjr.modularpowerarmor.basemod.MPAConstants;
import com.github.lehjr.modularpowerarmor.basemod.config.CommonConfig;
import com.github.lehjr.modularpowerarmor.item.module.AbstractPowerModule;
import com.github.lehjr.mpalib.basemod.MPALIbConstants;
import com.github.lehjr.mpalib.capabilities.IConfig;
import com.github.lehjr.mpalib.capabilities.module.powermodule.EnumModuleCategory;
import com.github.lehjr.mpalib.capabilities.module.powermodule.EnumModuleTarget;
import com.github.lehjr.mpalib.capabilities.module.powermodule.PowerModuleCapability;
import com.github.lehjr.mpalib.capabilities.module.tickable.IPlayerTickModule;
import com.github.lehjr.mpalib.capabilities.module.tickable.PlayerTickModule;
import com.github.lehjr.mpalib.capabilities.module.toggleable.IToggleableModule;
import com.github.lehjr.mpalib.energy.ElectricItemUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EnergyShieldModule extends AbstractPowerModule {
    public EnergyShieldModule(String regName) {
        super(regName);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundNBT nbt) {
        return new CapProvider(stack);
    }

    public class CapProvider implements ICapabilityProvider {
        ItemStack module;
        IPlayerTickModule ticker;

        public CapProvider(@Nonnull ItemStack module) {
            this.module = module;
            if (CommonConfig.moduleConfig != null) {
                ticker = new Ticker(module, EnumModuleCategory.ARMOR, EnumModuleTarget.ARMORONLY, CommonConfig.moduleConfig, true);
                ticker.addTradeoffPropertyDouble(MPAConstants.MODULE_FIELD_STRENGTH, MPAConstants.ARMOR_VALUE_ENERGY, 6, MPALIbConstants.MODULE_TRADEOFF_PREFIX + MPAConstants.ARMOR_POINTS);
                ticker.addTradeoffPropertyDouble(MPAConstants.MODULE_FIELD_STRENGTH, MPAConstants.ARMOR_ENERGY_CONSUMPTION, 5000, "RF");
                ticker.addTradeoffPropertyDouble(MPAConstants.MODULE_FIELD_STRENGTH, MPAConstants.MAXIMUM_HEAT, 500, "");
                ticker.addBasePropertyDouble(MPAConstants.KNOCKBACK_RESISTANCE, 0.25, "");
            }
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            if (cap instanceof IToggleableModule) {
                ((IToggleableModule) cap).updateFromNBT();
            }
            if (ticker == null) {
                return LazyOptional.empty();
            }
            return PowerModuleCapability.POWER_MODULE.orEmpty(cap, LazyOptional.of(()-> ticker));
        }

        class Ticker extends PlayerTickModule {
            public Ticker(@Nonnull ItemStack module, EnumModuleCategory category, EnumModuleTarget target, IConfig config, boolean defBool) {
                super(module, category, target, config, defBool);
            }

            @Override
            public void onPlayerTickActive(PlayerEntity player, @Nonnull ItemStack item) {
                int energy = ElectricItemUtils.getPlayerEnergy(player);
                int energyUsage = (int) applyPropertyModifiers(MPAConstants.ARMOR_ENERGY_CONSUMPTION);

                // turn off module if energy is too low. This will fire on both sides so no need to sync
                if (energy < energyUsage) {
                    this.toggleModule(false);
                }
            }
        }
    }
}