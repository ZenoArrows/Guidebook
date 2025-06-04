package dev.gigaherz.guidebook.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.gigaherz.guidebook.ConfigValues;
import dev.gigaherz.guidebook.GuidebookMod;
import dev.gigaherz.guidebook.guidebook.BookRegistry;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.atlas.SpriteSource;
import net.minecraft.client.renderer.texture.atlas.SpriteSourceType;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceMetadata;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public record CoverLister(Optional<String> idPrefix) implements SpriteSource {

    private static final MapCodec<CoverLister> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Codec.STRING.optionalFieldOf("prefix").forGetter(lister -> lister.idPrefix)).apply(inst, CoverLister::new));
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(GuidebookMod.MODID, "covers");
    public static final SpriteSourceType TYPE = new SpriteSourceType(CODEC);

    @Override
    public void run(ResourceManager resourceManager, Output output) {
        BookRegistry.gatherBookCovers().forEach((cover) -> {
            ResourceLocation id = this.idPrefix.isPresent() ? cover.withPrefix(this.idPrefix.get()) : cover;
            Optional<Resource> resource = resourceManager.getResource(TEXTURE_ID_CONVERTER.idToFile(cover));
            if (resource.isPresent())
                output.add(id, (loader) -> loader.loadSprite(id, resource.get(), CoverLister::create));
        });
    }

    public static SpriteContents create(ResourceLocation location, FrameSize framesize, NativeImage image, ResourceMetadata metadata)
    {
        if (framesize.width() > ConfigValues.maxCoverRes || framesize.height() > ConfigValues.maxCoverRes)
        {
            NativeImage[] mips = new NativeImage[]{image};
            int level = Math.max(Mth.log2(framesize.width()), Mth.log2(framesize.height())) - Mth.log2(ConfigValues.maxCoverRes);
            image = MipmapGenerator.generateMipLevels(mips, level)[level];
            framesize = new FrameSize(framesize.width() >> level, framesize.height() >> level);
        }
        return new SpriteContents(location, framesize, image, metadata);
    }

    @Override
    public SpriteSourceType type() {
        return TYPE;
    }
}
