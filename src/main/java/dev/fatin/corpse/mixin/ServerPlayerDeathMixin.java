package dev.fatin.corpse.mixin;

import dev.fatin.corpse.death.CorpseDeathHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDeathMixin {

    @Inject(method = "die", at = @At("HEAD"))
    private void corpse$captureInventory(DamageSource source, CallbackInfo callbackInfo) {
        CorpseDeathHandler.onDeath((ServerPlayer) (Object) this, source);
    }
}
