package com.github.lunatrius.schematica.client.renderer.vertex;

import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;

public final class VertexFormats {
    public static final VertexFormat ABSTRACT = new VertexFormat();

    static {
        ABSTRACT.setElement(new VertexFormatElement(0, VertexFormatElement.EnumType.FLOAT, VertexFormatElement.EnumUsage.POSITION, 3));
        ABSTRACT.setElement(new VertexFormatElement(0, VertexFormatElement.EnumType.UBYTE, VertexFormatElement.EnumUsage.COLOR, 4));
    }
}
