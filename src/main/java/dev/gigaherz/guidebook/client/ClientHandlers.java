package dev.gigaherz.guidebook.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.gigaherz.guidebook.GuidebookMod;
import dev.gigaherz.guidebook.guidebook.BookRegistry;
import dev.gigaherz.guidebook.guidebook.client.AnimatedBookBackground;
import dev.gigaherz.guidebook.guidebook.client.BookBakedModel;
import dev.gigaherz.guidebook.guidebook.conditions.AdvancementCondition;
import dev.gigaherz.guidebook.guidebook.conditions.BasicConditions;
import dev.gigaherz.guidebook.guidebook.conditions.CompositeCondition;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.TriState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.resources.VanillaClientListeners;

import java.io.IOException;
import java.util.function.Function;

public class ClientHandlers
{
    public static void clientInit()
    {
        BasicConditions.register();
        CompositeCondition.register();
        AdvancementCondition.register();
        /*if (ModList.get().isLoaded("gamestages"))
            GameStageCondition.register();*/
    }

    @EventBusSubscriber(value = Dist.CLIENT, modid = GuidebookMod.MODID, bus = EventBusSubscriber.Bus.MOD)
    public static class ModClientEvents
    {
        @SubscribeEvent
        public static void construct(FMLConstructModEvent event)
        {
            BookRegistry.injectCustomResourcePack();
        }

        @SubscribeEvent
        public static void clientInit(RegisterParticleProvidersEvent event)
        {
            ClientHandlers.clientInit();
        }

        @SubscribeEvent
        public static void modelRegistry(ModelEvent.RegisterLoaders event)
        {
            event.register(GuidebookMod.location("book_model"), new BookBakedModel.ModelLoader());
        }

        @SubscribeEvent
        public static void additionalModels(ModelEvent.RegisterAdditional event)
        {
            // Ensures that the OBJ models used by the book GUI background, and all referenced textures, are loaded
            event.register(AnimatedBookBackground.BOOK_BACKGROUND0);
            event.register(AnimatedBookBackground.BOOK_BACKGROUND30);
            event.register(AnimatedBookBackground.BOOK_BACKGROUND60);
            event.register(AnimatedBookBackground.BOOK_BACKGROUND90);
            event.register(BookItemRenderer.MODEL_HELPER);
        }

        @SubscribeEvent
        public static void specialModels(RegisterSpecialModelRendererEvent event)
        {
            event.register(GuidebookMod.location("book_item"), BookItemRenderer.Unbaked.CODEC);
        }

        @SubscribeEvent
        public static void shaderRegistry(RegisterShadersEvent event) throws IOException
        {
            event.registerShader(CustomRenderTypes.BRIGHT_SOLID_SHADER);
        }

        @SubscribeEvent
        public static void onRegisterMaterialAtlases(RegisterMaterialAtlasesEvent event) {
            event.register(BookBakedModel.LOCATION_COVERS, ResourceLocation.fromNamespaceAndPath("gbook", "covers"));
        }

        @SubscribeEvent
        static void onRegisterSpriteSourceTypes(RegisterSpriteSourceTypesEvent event) {
            event.register(CoverLister.ID, CoverLister.TYPE);
        }

        @SubscribeEvent
        static void onRegisterReloadListener(AddClientReloadListenersEvent event) {
            ResourceLocation loc = ResourceLocation.fromNamespaceAndPath(GuidebookMod.MODID, "books");
            event.addListener(loc, BookRegistry::onResourceReload);
            event.addDependency(VanillaClientListeners.LANGUAGE, loc);
            event.addDependency(loc, VanillaClientListeners.MODELS);
        }
    }

    public static RenderType brightSolid(ResourceLocation texture)
    {
        return CustomRenderTypes.BRIGHT_SOLID.apply(texture);
    }

    public static RenderType guiMipmapped(ResourceLocation texture)
    {
        return CustomRenderTypes.GUI_MIPMAPPED.apply(texture);
    }

    public static RenderType layeredItemTranslucentMipped(ResourceLocation texture)
    {
        return CustomRenderTypes.LAYERED_ITEM_TRANSLUCENT_MIPPED.apply(texture);
    }

    private static class CustomRenderTypes extends RenderType
    {
        public static final ResourceLocation BRIGHT_SOLID_SHADER_LOCATION = ResourceLocation.fromNamespaceAndPath("gbook", "core/rendertype_bright_solid");

        public static final ShaderProgram BRIGHT_SOLID_SHADER = new ShaderProgram(BRIGHT_SOLID_SHADER_LOCATION, DefaultVertexFormat.NEW_ENTITY, ShaderDefines.EMPTY);

        private static final ShaderStateShard RENDERTYPE_BRIGHT_SOLID_SHADER = new ShaderStateShard(BRIGHT_SOLID_SHADER);

        private CustomRenderTypes(String s, VertexFormat v, VertexFormat.Mode m, int i, boolean b, boolean b2, Runnable r, Runnable r2)
        {
            super(s, v, m, i, b, b2, r, r2);
            throw new IllegalStateException("This class is not meant to be constructed!");
        }

        public static Function<ResourceLocation, RenderType> BRIGHT_SOLID = Util.memoize(CustomRenderTypes::brightSolid);

        private static RenderType brightSolid(ResourceLocation locationIn)
        {
            RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
                    .setShaderState(RENDERTYPE_BRIGHT_SOLID_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, TriState.FALSE, false))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setLightmapState(NO_LIGHTMAP)
                    .setOverlayState(NO_OVERLAY)
                    .createCompositeState(true);
            return create("gbook_bright_solid", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, false, rendertype$state);
        }

        public static Function<ResourceLocation, RenderType> GUI_MIPMAPPED = Util.memoize(CustomRenderTypes::guiMipmapped);

        private static RenderType guiMipmapped(ResourceLocation locationIn)
        {
            RenderType.CompositeState rendertype$state = RenderType.CompositeState.builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, TriState.TRUE, true))
                    .setShaderState(POSITION_TEXTURE_COLOR_SHADER)
                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(LEQUAL_DEPTH_TEST)
                    .createCompositeState(false);
            return create("gbook_gui_mipmapped", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 786432, rendertype$state);
        }

        public static Function<ResourceLocation, RenderType> LAYERED_ITEM_TRANSLUCENT_MIPPED = Util.memoize(CustomRenderTypes::layeredItemTranslucentMipped);

        private static RenderType layeredItemTranslucentMipped(ResourceLocation locationIn) {
            RenderType.CompositeState rendertype$state = CompositeState.builder()
                    .setShaderState(RenderType.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(locationIn, TriState.DEFAULT, true))
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setLightmapState(RenderType.LIGHTMAP)
                    .setOverlayState(RenderType.OVERLAY)
                    .createCompositeState(true);
            return RenderType.create("gbook_item_entity_translucent_mipped", DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS, 256, true, true, rendertype$state);
        }
    }
}
