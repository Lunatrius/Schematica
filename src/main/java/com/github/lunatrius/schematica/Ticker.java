package com.github.lunatrius.schematica;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

import java.util.EnumSet;

public class Ticker implements ITickHandler {
	private EnumSet<TickType> ticks = EnumSet.noneOf(TickType.class);

	public Ticker(EnumSet<TickType> tickTypes) {
		this.ticks = tickTypes;
	}

	@Override
	public void tickStart(EnumSet<TickType> tickTypes, Object... tickData) {
		tick(tickTypes, true);
	}

	@Override
	public void tickEnd(EnumSet<TickType> tickTypes, Object... tickData) {
		tick(tickTypes, false);
	}

	private void tick(EnumSet<TickType> tickTypes, boolean start) {
		for (TickType tick : tickTypes) {
			if (!Schematica.instance.onTick(tick, start)) {
				this.ticks.remove(tick);
				this.ticks.removeAll(tick.partnerTicks());
			}
		}
	}

	@Override
	public EnumSet<TickType> ticks() {
		return this.ticks;
	}

	@Override
	public String getLabel() {
		return "SchematicaTicker";
	}
}
