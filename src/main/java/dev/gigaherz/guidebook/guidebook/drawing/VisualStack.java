package dev.gigaherz.guidebook.guidebook.drawing;

import dev.gigaherz.guidebook.guidebook.HoverContext;
import dev.gigaherz.guidebook.guidebook.IBookGraphics;
import dev.gigaherz.guidebook.guidebook.SectionRef;
import dev.gigaherz.guidebook.guidebook.elements.LabelPosition;
import dev.gigaherz.guidebook.guidebook.util.Size;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class VisualStack extends VisualElement
{
    public static final int CYCLE_TIME = 1000;//=1s

    private final Size iconSize;
    private final LabelPosition labelPosition;
    private final int labelColor;
    public ItemStack[] stacks;
    public float scale = 1.0f;
    public int z;

    public VisualStack(NonNullList<ItemStack> stacks, Size size, int positionMode, float baseline, int verticalAlign, float scale, int z, Size iconSize, LabelPosition labelPosition, int labelColor)
    {
        super(size, positionMode, baseline, verticalAlign);
        this.stacks = stacks.toArray(new ItemStack[0]);
        this.scale = scale;
        this.z = z;
        this.iconSize = iconSize;
        this.labelPosition = labelPosition;
        this.labelColor = labelColor;
    }

    public ItemStack getCurrentStack()
    {
        if (stacks == null || stacks.length == 0)
            return ItemStack.EMPTY;
        long time = System.currentTimeMillis();
        return stacks[(int) ((time / CYCLE_TIME) % stacks.length)];
    }

    @Override
    public void draw(IBookGraphics nav, GuiGraphics graphics)
    {
        super.draw(nav, graphics);
        ItemStack stack = getCurrentStack();
        if (stack.getCount() > 0)
        {
            if (labelPosition == LabelPosition.NONE)
            {
                nav.drawItemStack(graphics, position.x(), position.y(), z, stack, 0xFFFFFFFF, scale);
            }
            else            
            {
                var labelText = stack.getHoverName().copy().withStyle(style -> style.withColor(labelColor));
                var labelSize = nav.measure(labelText);

                switch (labelPosition)
                {
                    case LEFT -> {
                        nav.addString(graphics,
                                position.x(),
                                position.y() + (size.height() - labelSize.height())/2,
                                labelText, 0xFFFFFFFF, scale);
                        nav.drawItemStack(graphics,
                                position.x() + labelSize.width(),
                                position.y() + (size.height() - iconSize.height())/2,
                                z, stack, 0xFFFFFFFF, scale);
                    }
                    case RIGHT -> {
                        nav.drawItemStack(graphics,
                                position.x(),
                                position.y() + (size.height() - iconSize.height())/2,
                                z, stack, 0xFFFFFFFF, scale);
                        nav.addString(graphics,
                                position.x() + iconSize.width(),
                                position.y() + (size.height() - labelSize.height())/2,
                                labelText, 0xFFFFFFFF, scale);
                    }
                    case ABOVE -> {
                        nav.addString(graphics,
                                position.x() + (size.width() - labelSize.width())/2,
                                position.y(),
                                labelText, 0xFFFFFFFF, scale);
                        nav.drawItemStack(graphics,
                                position.x() + (size.width() - iconSize.width())/2,
                                position.y() + labelSize.height(),
                                z, stack, 0xFFFFFFFF, scale);
                    }
                    case BELOW -> {
                        nav.drawItemStack(graphics,
                                position.x() + (size.width() - iconSize.width())/2,
                                position.y(),
                                z, stack, 0xFFFFFFFF, scale);
                        nav.addString(graphics,
                                position.x() + (size.width() - labelSize.width())/2,
                                position.y() + iconSize.height(),
                                labelText, 0xFFFFFFFF, scale);
                    }
                }
            }
            
        }
    }

    @Override
    public boolean wantsHover()
    {
        return true;
    }

    @Override
    public void mouseOver(IBookGraphics nav, HoverContext hoverContext, GuiGraphics graphics)
    {
        ItemStack stack = getCurrentStack();
        if (stack.getCount() > 0)
        {
            graphics.renderTooltip(nav.getFont(), stack, hoverContext.mouseX, hoverContext.mouseY);
        }
    }

    @Override
    public boolean click(IBookGraphics nav)
    {
        SectionRef ref = nav.getBook().getStackLink(getCurrentStack());
        if (ref != null)
            nav.navigateTo(ref);
        return ref != null;
    }
}
