package dev.gigaherz.guidebook.guidebook.drawing;

import dev.gigaherz.guidebook.guidebook.HoverContext;
import dev.gigaherz.guidebook.guidebook.IBookGraphics;
import dev.gigaherz.guidebook.guidebook.elements.LinkContext;
import dev.gigaherz.guidebook.guidebook.util.LinkHelper;
import dev.gigaherz.guidebook.guidebook.util.Size;
import net.minecraft.client.gui.GuiGraphics;

public class VisualBox extends VisualElement implements LinkHelper.ILinkable
{
    public LinkContext linkContext = null;

    public VisualBox(Size size, int positionMode, float baseline, int verticalAlign)
    {
        super(size, positionMode, baseline, verticalAlign);
    }

    @Override
    public void draw(IBookGraphics nav, GuiGraphics graphics)
    {
        super.draw(nav, graphics);
        if (linkContext != null && linkContext.isHovering)
            graphics.fill(position.x(), position.y(), position.x() + size.width(), position.y() + size.height(), colorHover);
    }

    public int colorHover = 0x3f77cc66;

    @Override
    public boolean wantsHover()
    {
        return linkContext != null;
    }

    @Override
    public void mouseOver(IBookGraphics nav, HoverContext hoverContext, GuiGraphics graphics)
    {
        if (linkContext != null ) {
            linkContext.isHovering = true;
            //Mouse.setNativeCursor(Cursor.)
        }
    }

    @Override
    public void mouseOut(IBookGraphics nav, HoverContext hoverContext)
    {
        if (linkContext != null ) {
            linkContext.isHovering = false;
        }
    }

    @Override
    public void click(IBookGraphics nav)
    {
        if (linkContext != null)
            LinkHelper.click(nav, linkContext);
        //return linkContext != null;
    }

    @Override
    public void setLinkContext(LinkContext ctx)
    {
        linkContext = ctx;
    }
}
