package com.example.colormons.mixin;

import com.cobblemon.mod.common.client.render.pokemon.PokemonRenderer;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.example.colormons.ColorStore;
import com.example.colormons.Finish;
import com.example.colormons.MonColors;
import com.example.colormons.ShinyHelper;
import com.example.colormons.TintCache;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = PokemonRenderer.class, remap = false)
public abstract class PokemonRendererMixin {

    @Inject(method = "getTextureLocation(Lcom/cobblemon/mod/common/entity/pokemon/PokemonEntity;)Lnet/minecraft/resources/ResourceLocation;",
            at = @At("RETURN"), cancellable = true)
    private void colormons$tint(PokemonEntity entity, CallbackInfoReturnable<ResourceLocation> cir) {
        Pokemon mon = entity.getPokemon();
        MonColors mc = ColorStore.of(mon.getUuid());
        boolean wantsShiny = mc.finish == Finish.SHINY;
        if (mc.isEmpty() && !wantsShiny) return;

        ResourceLocation base = cir.getReturnValue();
        if (wantsShiny) base = ShinyHelper.shinyVariant(base);

        if (!mc.isEmpty()) {
            String species = mon.getSpecies().getResourceIdentifier().getPath();
            base = TintCache.getOrCreate(mon.getUuid(), species, base, mc);
        }
        cir.setReturnValue(base);
    }
}
