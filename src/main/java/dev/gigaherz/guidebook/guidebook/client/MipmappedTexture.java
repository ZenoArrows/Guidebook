package dev.gigaherz.guidebook.guidebook.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;

public class MipmappedTexture extends SimpleTexture
{
    public MipmappedTexture(ResourceLocation pLocation)
    {
        super(pLocation);
    }

    @Override
    public void apply(TextureContents contents)
    {
        boolean clamp = contents.clamp();
        boolean blur = contents.blur();
        this.defaultBlur = blur;

        Integer levels = Minecraft.getInstance().options.mipmapLevels().get();
        NativeImage[] mips = MipmapGenerator.generateMipLevels(new NativeImage[]{ contents.image() }, levels);

        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> {
                this.doLoad(mips, blur, clamp);
            });
        } else {
            this.doLoad(mips, blur, clamp);
        }
    }

    private void doLoad(NativeImage[] images, boolean blur, boolean clamp)
    {
        TextureUtil.prepareImage(this.getId(), images.length - 1, images[0].getWidth(), images[0].getHeight());
        this.setFilter(blur, true);
        this.setClamp(clamp);
        for(int i = 0; i < images.length; ++i)
            images[i].upload(i, 0, 0, 0, 0, images[i].getWidth(), images[i].getHeight(), true);
    }
}
