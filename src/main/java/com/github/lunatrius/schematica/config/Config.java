package com.github.lunatrius.schematica.config;

import com.github.lunatrius.core.config.Configuration;
import net.minecraft.block.Block;
import net.minecraftforge.common.config.Property;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config extends Configuration {
	public final Property propEnableAlpha;
	public final Property propAlpha;
	public final Property propHighlight;
	public final Property propHighlightAir;
	public final Property propBlockDelta;
	public final Property propPlaceDelay;
	public final Property propPlaceInstantly;
	public final Property propPlaceAdjacent;
	public final Property propDrawQuads;
	public final Property propDrawLines;

	public final Map<String, Property> propIgnoreMetadata = new HashMap<String, Property>();


	public boolean enableAlpha = false;
	public float alpha = 1.0f;
	public boolean highlight = true;
	public boolean highlightAir = true;
	public float blockDelta = 0.005f;
	public int placeDelay = 1;
	public boolean placeInstantly = false;
	public boolean placeAdjacent = true;
	public boolean drawQuads = true;
	public boolean drawLines = true;

	public List<Block> ignoreMetadata = new ArrayList<Block>();


	public Config(File file) {
		super(file);

		this.propEnableAlpha = get("general", "alphaEnabled", this.enableAlpha, "Enable transparent textures.");
		this.propAlpha = get("general", "alpha", this.alpha, 0.0, 1.0, "Alpha value used when rendering the schematic (example: 1.0 = opaque, 0.5 = half transparent, 0.0 = transparent).");
		this.propHighlight = get("general", "highlight", this.highlight, "Highlight invalid placed blocks and to be placed blocks.");
		this.propHighlightAir = get("general", "highlightAir", this.highlightAir, "Highlight invalid placed blocks (where there should be no block).");
		this.propBlockDelta = get("general", "blockDelta", this.blockDelta, 0.0, 0.5, "Delta value used for highlighting (if you're having issue with overlapping textures try setting this value higher).");
		this.propPlaceDelay = get("general", "placeDelay", this.placeDelay, 0, 20, "Delay in ticks between placement attempts.");
		this.propPlaceInstantly = get("general", "placeInstantly", this.placeInstantly, "Place all blocks that can be placed in one tick.");
		this.propPlaceAdjacent = get("general", "placeAdjacent", this.placeAdjacent, "Place blocks only if there is an adjacent block next to it.");
		this.propDrawQuads = get("general", "drawQuads", this.drawQuads, "Draw surface areas.");
		this.propDrawLines = get("general", "drawLines", this.drawLines, "Draw outlines.");

		this.enableAlpha = this.propEnableAlpha.getBoolean(this.enableAlpha);
		this.alpha = (float) this.propAlpha.getDouble(this.alpha);
		this.highlight = this.propHighlight.getBoolean(this.highlight);
		this.highlightAir = this.propHighlightAir.getBoolean(this.highlightAir);
		this.blockDelta = (float) this.propBlockDelta.getDouble(this.blockDelta);
		this.placeDelay = this.propPlaceDelay.getInt(this.placeDelay);
		this.placeInstantly = this.propPlaceInstantly.getBoolean(this.placeInstantly);
		this.placeAdjacent = this.propPlaceAdjacent.getBoolean(this.placeAdjacent);
		this.drawQuads = this.propDrawQuads.getBoolean(this.drawQuads);
		this.drawLines = this.propDrawLines.getBoolean(this.drawLines);
	}
}
