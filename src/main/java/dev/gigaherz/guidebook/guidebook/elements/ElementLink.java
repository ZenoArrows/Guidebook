package dev.gigaherz.guidebook.guidebook.elements;

import dev.gigaherz.guidebook.guidebook.IBookGraphics;
import dev.gigaherz.guidebook.guidebook.ParsingContext;
import dev.gigaherz.guidebook.guidebook.SectionRef;
import dev.gigaherz.guidebook.guidebook.drawing.VisualElement;
import dev.gigaherz.guidebook.guidebook.util.AttributeGetter;
import dev.gigaherz.guidebook.guidebook.util.LinkHelper;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.util.List;
import java.util.stream.Collectors;

public class ElementLink extends ElementSpan
{
    public LinkContext ctx = new LinkContext();

    public ElementLink(boolean isFirstElement, boolean isLastElement, ElementInline... addRuns)
    {
        super(isFirstElement, isLastElement, addRuns);
    }

    @Override
    public List<VisualElement> measure(IBookGraphics nav, int width, int firstLineWidth)
    {
        List<VisualElement> texts = super.measure(nav, width, firstLineWidth);
        texts.forEach(e -> {
            if (e instanceof LinkHelper.ILinkable)
            {
                LinkHelper.ILinkable linkable = (LinkHelper.ILinkable) e;
                linkable.setLinkContext(ctx);
            }
        });
        return texts;
    }

    @Override
    public void parse(ParsingContext context, AttributeGetter attributes)
    {
        super.parse(context, attributes);

        String attr = attributes.getAttribute("ref");
        if (attr != null)
        {
            String ref = attr;
            ctx.target = SectionRef.fromString(ref);
        }

        attr = attributes.getAttribute("href");
        if (attr != null)
        {
            ctx.textTarget = attr;
            ctx.textAction = "openUrl";
        }

        attr = attributes.getAttribute("text");
        if (attr != null)
        {
            ctx.textTarget = attr;
        }

        attr = attributes.getAttribute("action");
        if (attr != null)
        {
            ctx.textAction = attr;
        }
    }

    @Override
    public ElementInline copy()
    {
        ElementLink link = super.copy(new ElementLink(isFirstElement, isLastElement));
        for (ElementInline run : inlines)
        {
            link.inlines.add(run.copy());
        }

        link.ctx = ctx.copy();

        return link;
    }

    @Override
    public String toString(boolean complete)
    {
        // TODO: Complete mode
        return String.format("<link %s ...>%s</link>", ctx.toString(), inlines.stream().map(Object::toString).collect(Collectors.joining()));
    }
}
