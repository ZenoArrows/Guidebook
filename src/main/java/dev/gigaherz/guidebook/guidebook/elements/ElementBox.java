package dev.gigaherz.guidebook.guidebook.elements;

import dev.gigaherz.guidebook.guidebook.IBookGraphics;
import dev.gigaherz.guidebook.guidebook.ParsingContext;
import dev.gigaherz.guidebook.guidebook.drawing.VisualBox;
import dev.gigaherz.guidebook.guidebook.drawing.VisualElement;
import dev.gigaherz.guidebook.guidebook.util.AttributeGetter;
import dev.gigaherz.guidebook.guidebook.util.Rect;
import dev.gigaherz.guidebook.guidebook.util.Size;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ElementBox extends ElementInline
{
    public ElementBox(boolean isFirstElement, boolean isLastElement)
    {
        super(isFirstElement, isLastElement);
    }

    private VisualBox getVisual()
    {
        return new VisualBox(new Size(w, h), position, baseline, verticalAlignment);
    }

    @Override
    public List<VisualElement> measure(IBookGraphics nav, int width, int firstLineWidth)
    {
        return Collections.singletonList(getVisual());
    }

    @Override
    public int reflow(List<VisualElement> paragraph, IBookGraphics nav, Rect bounds, Rect page)
    {
        VisualBox element = getVisual();
        element.position = applyPosition(bounds.position, bounds.position);
        paragraph.add(element);
        if (position != POS_RELATIVE)
            return bounds.position.y();
        return bounds.position.y() + element.size.height();
    }

    @Override
    public void findTextures(Map<ResourceLocation, ReloadableTexture> textures)
    {
        // No need to require them, since they are used dynamically and not stitched.
        //textures.add(textureLocation);
    }

    @Override
    public void parse(ParsingContext context, AttributeGetter attributes)
    {
        super.parse(context, attributes);
    }

    @Override
    public String toString(boolean complete)
    {
        // TODO: Complete mode
        return "<box />";
    }

    @Override
    public ElementInline copy()
    {
        return super.copy(new ElementBox(isFirstElement, isLastElement));
    }

    @Override
    public boolean supportsPageLevel()
    {
        return true;
    }
}
