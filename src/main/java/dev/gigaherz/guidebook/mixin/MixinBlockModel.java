package dev.gigaherz.guidebook.mixin;

import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@OnlyIn(Dist.CLIENT)
@Mixin(BlockModel.Deserializer.class)
public class MixinBlockModel
{
    private static FileToIdConverter ATLAS_ID_CONVERTER = new FileToIdConverter("textures/atlas", ".png");

    @Inject(at = @At("HEAD"), method = "getTextureMap", cancellable = true)
    private void onGetTextureMap(JsonObject json, CallbackInfoReturnable<TextureSlots.Data> ci) {
        if (json.has("atlas") && json.has("textures")) {
            String atlasName = GsonHelper.getAsString(json, "atlas", "");
            ResourceLocation atlas = atlasName.isEmpty() ? TextureAtlas.LOCATION_BLOCKS :
                    ATLAS_ID_CONVERTER.idToFile(ResourceLocation.parse(atlasName));
            JsonObject jsonobject = GsonHelper.getAsJsonObject(json, "textures");
            ci.setReturnValue(TextureSlots.parseTextureMap(jsonobject, atlas));
            ci.cancel();
        }
    }
}
