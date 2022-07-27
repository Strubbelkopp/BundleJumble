package dev.strubbelkopp.bundle_jumble.mixin;

import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(AnimalMateGoal.class)
public abstract class AnimalMateGoalMixin extends Goal {

    @Shadow @Final protected AnimalEntity animal;

    @Shadow @Final protected World world;

    @Shadow @Nullable protected AnimalEntity mate;

    @Inject(method = "breed", at = @At("HEAD"))
    private void breedRabbits(CallbackInfo ci) {
        if (this.animal instanceof RabbitEntity) {
            Random random = new Random();
            for (int i = 0; i < random.nextInt(3); i++) {
                this.animal.breed((ServerWorld) this.world, this.mate);
            }
        }
    }
}