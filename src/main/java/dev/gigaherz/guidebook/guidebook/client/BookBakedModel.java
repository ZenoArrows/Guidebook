package dev.gigaherz.guidebook.guidebook.client;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import dev.gigaherz.guidebook.GuidebookMod;
import dev.gigaherz.guidebook.guidebook.BookRegistry;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.util.context.ContextMap;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.StandardModelParameters;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.obj.ObjLoader;
import net.neoforged.neoforge.client.model.obj.ObjModel;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BookBakedModel implements BakedModel
{
    public static final ResourceLocation LOCATION_COVERS = ResourceLocation.fromNamespaceAndPath(GuidebookMod.MODID, "textures/atlas/covers.png");

    private final boolean isSideLit;
    private final ItemTransforms cameraTransforms;
    private final BakedModel baseModel;
    private final Map<ResourceLocation, BakedModel> bookModels;
    private final TextureAtlasSprite particle;

    public BakedModel getActualModel(@Nullable ResourceLocation book)
    {
        if (book != null)
        {
            BakedModel bakedModel = bookModels.get(book);
            if (bakedModel != null)
                return bakedModel;
        }
        return baseModel;
    }

    public BookBakedModel(BakedModel baseModel,
                          boolean isSideLit, ItemTransforms cameraTransforms,
                          Map<ResourceLocation, BakedModel> models,
                          @Nullable TextureAtlasSprite particle)
    {
        this.baseModel = baseModel;
        this.bookModels = models;
        this.particle = particle;
        this.isSideLit = isSideLit;
        this.cameraTransforms = cameraTransforms;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand)
    {
        return Collections.emptyList();
    }

    @Override
    public boolean useAmbientOcclusion()
    {
        return true;
    }

    @Override
    public boolean isGui3d()
    {
        return true;
    }

    @Override
    public boolean usesBlockLight()
    {
        return isSideLit;
    }

    @Override
    public TextureAtlasSprite getParticleIcon()
    {
        return particle;
    }

    @Deprecated
    @Override
    public ItemTransforms getTransforms()
    {
        return cameraTransforms;
    }

    public static class Model implements UnbakedModel
    {
        private final UnbakedModel baseModel;
        private final Map<ResourceLocation, UnbakedModel> bookModels = Maps.newHashMap();

        public Model(UnbakedModel baseModel)
        {
            this.baseModel = baseModel;
        }

        @Override
        public BakedModel bake(TextureSlots textureSlots, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion, boolean usesBlockLight, ItemTransforms itemTransforms, ContextMap additionalProperties)
        {
            var part = baker.findSprite(textureSlots, TextureSlot.PARTICLE.getId());

            Map<ResourceLocation, BakedModel> bakedBookModels = ImmutableMap.copyOf(Maps.transformValues(bookModels,
                    v -> UnbakedModel.bakeWithTopModelValues(v, baker, modelState)));

            var baseModel = UnbakedModel.bakeWithTopModelValues(this.baseModel, baker, modelState);

            return new BookBakedModel(
                    baseModel,
                    usesBlockLight, itemTransforms, bakedBookModels, part);
        }

        @Override
        public BakedModel bake(TextureSlots textureSlots, ModelBaker baker, ModelState modelState, boolean useAmbientOcclusion, boolean usesBlockLight, ItemTransforms itemTransforms)
        {
            return this.bake(textureSlots, baker, modelState, useAmbientOcclusion, usesBlockLight, itemTransforms, ContextMap.EMPTY);
        }

        @Override
        public void resolveDependencies(Resolver resolver)
        {
            baseModel.resolveDependencies(resolver);

            ResourceLocation paper = ResourceLocation.fromNamespaceAndPath(GuidebookMod.MODID, "misc/paper");
            ResourceLocation coverBase = ResourceLocation.fromNamespaceAndPath(GuidebookMod.MODID, "misc/cover");
            BookRegistry.getLoadedBooks().forEach((resourceLocation, bookDocument) -> {
                bookModels.computeIfAbsent(resourceLocation, (loc) -> {
                    ResourceLocation bookModel = bookDocument.getModelStandalone();
                    UnbakedModel parent = bookModel == null ? baseModel : resolver.resolve(bookModel);
                    if (!bookDocument.hasCover())
                        return parent;

                    TextureSlots.Data.Builder slots = new TextureSlots.Data.Builder();
                    slots.addTexture("cover_base", new Material(LOCATION_COVERS, coverBase));
                    slots.addTexture("paper", new Material(LOCATION_COVERS, paper));
                    if (bookDocument.hasCover())
                        bookDocument.applyCover(LOCATION_COVERS, slots);

                    BlockModel mdl = new BlockModel(
                            ResourceLocation.fromNamespaceAndPath(loc.getNamespace(), "generated/book_models/" + loc.getPath()),
                            List.of(), slots.build(), null, null, null);
                    //mdl.parentLocation = null;
                    mdl.parent = parent;
                    //mdl.resolveDependencies(resolver);
                    return mdl;
                });
            });
        }
    }

    public static class ModelLoader implements UnbakedModelLoader<Model>
    {
        @Override
        public Model read(JsonObject modelContents, JsonDeserializationContext deserializationContext)
        {
            var baseModel = readBaseModel(GsonHelper.getAsJsonObject(modelContents,"base_model"), deserializationContext);
            return new Model(baseModel);
        }

        private static UnbakedModel readBaseModel(
                JsonObject jsonObject,
                JsonDeserializationContext context) {
            String modelLocation = jsonObject.get("model").getAsString();

            boolean automaticCulling = GsonHelper.getAsBoolean(jsonObject, "automatic_culling", true);
            boolean shadeQuads = GsonHelper.getAsBoolean(jsonObject, "shade_quads", true);
            boolean flipV = GsonHelper.getAsBoolean(jsonObject, "flip_v", false);
            boolean emissiveAmbient = GsonHelper.getAsBoolean(jsonObject, "emissive_ambient", true);
            String mtlOverride = GsonHelper.getAsString(jsonObject, "mtl_override", null);
            StandardModelParameters params = StandardModelParameters.parse(jsonObject, context);
            if (jsonObject.has("textures")) {
                JsonObject jsonobject = GsonHelper.getAsJsonObject(jsonObject, "textures");
                params = new StandardModelParameters(params.parent(), TextureSlots.parseTextureMap(jsonobject, LOCATION_COVERS), params.itemTransforms(), params.ambientOcclusion(), params.guiLight(), params.rootTransform(), params.renderTypeGroup(), params.partVisibility());
            }
            return ObjLoader.INSTANCE.loadModel(new ObjModel.ModelSettings(ResourceLocation.parse(modelLocation), automaticCulling, shadeQuads, flipV, emissiveAmbient, mtlOverride, params));
        }
    }
}
