package net.shadowbeast.arcanemysteries.temprature;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.shadowbeast.arcanemysteries.ArcaneMysteries;
import net.shadowbeast.arcanemysteries.registries.EffectsRegistry;
import net.shadowbeast.arcanemysteries.temprature.util.ClientTick;
import net.shadowbeast.arcanemysteries.temprature.util.EStats;
import net.shadowbeast.arcanemysteries.temprature.util.TUtil;

import java.util.Arrays;

public class TemperatureData extends ClientTick {

    private double temperatureLevel = 0;
    private double displayTemperature = 0;
    private int temperatureTimer;
    private double targetTemperature = 0;
    private int hypTimer = 0;

    public TemperatureData()
    {
        this.hypTimer = 100;
        this.temperatureLevel = ArcaneMysteries.DEF_TEMP;
    }

    public void addHeat(float heatLevel, double max){
        this.temperatureLevel = Math.min(this.temperatureLevel+heatLevel,max);
    }
    public void addCOld(float coldLevel, double max){
        this.temperatureLevel = Math.min(this.temperatureLevel-coldLevel,max);
    }

    private boolean addTemperature(ServerPlayer player, double temperature) {

            if (player.gameMode.isSurvival()) {
                double defaultT = 20;
                double maxHeat1 = TUtil.firstHeat(player);
                double maxHeat2 = TUtil.secondHeat(player);
                double maxHeat3 = TUtil.maxHeat(player);
                double maxCold1 = TUtil.firstCold(player);
                double maxCold2 = TUtil.secondCold(player);
                double maxCold3 = TUtil.maxCold(player);

                if (this.temperatureLevel > defaultT && this.temperatureLevel <= maxHeat1) {
                    if (temperature < 0) this.temperatureLevel = this.temperatureLevel+(temperature*1.5D);
                    else this.temperatureLevel = this.temperatureLevel+(temperature);
                }
                else if (this.temperatureLevel > maxHeat1 && this.temperatureLevel <= maxHeat2) {
                    if (temperature < 0) this.temperatureLevel = this.temperatureLevel+(temperature);
                    else this.temperatureLevel = this.temperatureLevel+(temperature/10.0D);
                }
                else if (this.temperatureLevel > maxHeat2 && this.temperatureLevel <= maxHeat3) {
                    if (temperature < 0) this.temperatureLevel = this.temperatureLevel+(temperature/10.0D);
                    else this.temperatureLevel = this.temperatureLevel+(temperature/100.0D);
                }
                else if (this.temperatureLevel > maxHeat3) {
                    if (temperature < 0) this.temperatureLevel = this.temperatureLevel+(temperature/100.0D);
                    else this.temperatureLevel = maxHeat3;
                }

                else if (this.temperatureLevel < defaultT && this.temperatureLevel >= maxCold1) {
                    if (temperature > 0) this.temperatureLevel = this.temperatureLevel+(temperature*1.5D);
                    else this.temperatureLevel = this.temperatureLevel+(temperature);
                }
                else if (this.temperatureLevel < maxCold1 && this.temperatureLevel >= maxCold2) {
                    if (temperature > 0) this.temperatureLevel = this.temperatureLevel+(temperature);
                    else this.temperatureLevel = this.temperatureLevel+(temperature/10.0D);
                }
                else if (this.temperatureLevel < maxCold2 && this.temperatureLevel >= maxCold3) {
                    if (temperature > 0) this.temperatureLevel = this.temperatureLevel+(temperature/10.0D);
                    else this.temperatureLevel = this.temperatureLevel+(temperature/100.0D);
                }
                else if (this.temperatureLevel < maxCold3) {
                    if (temperature < 0) this.temperatureLevel = this.temperatureLevel+(temperature/100.0D);
                    else this.temperatureLevel = maxCold3;
                }
                else {
                    this.temperatureLevel = this.temperatureLevel+temperature;
                }
                return true;
            }
        return false;
    }
    public void tick(Player player) {
        this.targetTemperature = ArcaneMysteries.DEF_TEMP;
        double mod = (this.targetTemperature - this.temperatureLevel) * 2.0f;
        if (player instanceof ServerPlayer)
            addTemperature((ServerPlayer) player, mod);
        //For Display Purposes
        double tempLocation = this.temperatureLevel - ArcaneMysteries.DEF_TEMP;
        if (tempLocation > 0) {
            double maxTemp = 0.0D;
            double div = tempLocation / maxTemp;
            this.displayTemperature = Mth.clamp(div, 0, 1.0D+(28.0D/63.0D));
        }
        if (tempLocation < 0) {
            double maxTemp = 0.0D;
            double div = tempLocation / maxTemp;
            this.displayTemperature = Mth.clamp(div, -1.0D-(28.0D/63.0D), 0);
        }

        if(!(player.isCreative() || player.isSpectator())) {
            double maxHeat1 = TUtil.firstHeat(player);
            double maxHeat2 = TUtil.secondHeat(player);
            double maxHeat3 = TUtil.maxHeat(player);
            double maxCold1 = TUtil.firstCold(player);
            double maxCold2 = TUtil.secondCold(player);
            double maxCold3 = TUtil.maxCold(player);

            if (this.temperatureLevel > maxHeat1 || this.temperatureLevel < maxCold1) {
                if (this.hypTimer > 0) {
                    this.hypTimer--;
                } else if (this.hypTimer == 0) {
                    if (!player.hasEffect(EffectsRegistry.DEPRECIATED_HYPERTHERMIA.get()) && !player.hasEffect(EffectsRegistry.DEPRECIATED_HYPOTHERMIA.get())) {
                        if (this.temperatureLevel > maxHeat1 && this.temperatureLevel <= maxHeat2) {
                            player.addEffect(new MobEffectInstance(EffectsRegistry.DEPRECIATED_HYPERTHERMIA.get(), 100, 0));
                        }
                        else if (this.temperatureLevel > maxHeat2 && this.temperatureLevel <= maxHeat3) {
                            player.addEffect(new MobEffectInstance(EffectsRegistry.DEPRECIATED_HYPERTHERMIA.get(), 100, 1));
                        }
                        else if (this.temperatureLevel > maxHeat3) {
                            player.addEffect(new MobEffectInstance(EffectsRegistry.DEPRECIATED_HYPERTHERMIA.get(), 100, 2));
                        }

                        if (this.temperatureLevel < maxCold1 && this.temperatureLevel >= maxCold2) {
                            player.addEffect(new MobEffectInstance(EffectsRegistry.DEPRECIATED_HYPOTHERMIA.get(), 100, 0));
                        }
                        else if (this.temperatureLevel < maxCold2 && this.temperatureLevel >= maxCold3) {
                            player.addEffect(new MobEffectInstance(EffectsRegistry.DEPRECIATED_HYPOTHERMIA.get(), 100, 1));
                        }
                        else if (this.temperatureLevel < maxCold3) {
                            player.addEffect(new MobEffectInstance(EffectsRegistry.DEPRECIATED_HYPOTHERMIA.get(), 100, 2));
                        }
                    }
                }
            } else if (this.hypTimer < 20){
                this.hypTimer++;
            }
        }
    }

    /**
     * Reads the water data for the player.
     */
    public void read(CompoundTag compound) {
        if (compound.contains("temperatureLevel", 99)) {
            this.temperatureLevel = compound.getDouble("temperatureLevel");
            this.targetTemperature = compound.getDouble("targetTemperature");
            this.temperatureTimer = compound.getInt("temperatureTickTimer");
            this.displayTemperature = compound.getDouble("displayTemperature");
            this.hypTimer = compound.getInt("hypTimer");
        }
    }

    /**
     * Writes the water data for the player.
     */
    public void write(CompoundTag compound) {
        compound.putDouble("temperatureLevel", this.temperatureLevel);
        compound.putDouble("targetTemperature", this.targetTemperature);
        compound.putInt("temperatureTickTimer", this.temperatureTimer);
        compound.putDouble("displayTemperature", this.displayTemperature);
        compound.putInt("hypTimer", this.hypTimer);
        ListTag modifiers = new ListTag();
        compound.put("modifiers", modifiers);
    }

    /**
     * Get the player's temperature level.
     */
    public double getTemperatureLevel() {
        return this.temperatureLevel;
    }

    /**
     * Get the player's temperature level rounded to 2 decimal places.
     */
    public float getCelcius() {
        int temp = (int) (this.temperatureLevel*100);
        return ((float)temp) / 100.0F;
    }

    public float getFahrenheit() {
        float rawFTemp = (getCelcius() * (9.0F/5.0F)) + 32.0F;
        int fTemp = (int) (rawFTemp*100);
        return ((float)fTemp) / 100.0F;
    }

    @OnlyIn(Dist.CLIENT)
    public double getDisplayTemperature() {
        return displayTemperature;
    }

    public double getTargetTemperature() {
        return targetTemperature;
    }

    public void setTemperatureLevel(int temperatureLevelIn) {
        this.temperatureLevel = temperatureLevelIn;
    }

    @Override
    public void save(LivingEntity player) {
        EStats.setTemperatureStats(player, this);
    }

    @Override
    public boolean shouldTick() {
        return true;
    }

}