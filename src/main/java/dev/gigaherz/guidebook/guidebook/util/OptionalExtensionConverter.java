package dev.gigaherz.guidebook.guidebook.util;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.ResourceLocation;

public class OptionalExtensionConverter extends FileToIdConverter
{
    private String prefix;

    public OptionalExtensionConverter(String prefix, String extension) {
        super(prefix, extension);
        this.prefix = prefix;
    }

    @Override
    public ResourceLocation idToFile(ResourceLocation id)
    {
        if (id.getPath().indexOf('.') > 0)
            return id.withPath(prefix + "/" + id.getPath());
        else
            return super.idToFile(id);
    }
}
