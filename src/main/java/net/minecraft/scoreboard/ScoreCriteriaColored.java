package net.minecraft.scoreboard;

import net.minecraft.util.text.TextFormatting;

public class ScoreCriteriaColored implements IScoreCriteria {

	private final String goalName;

	public ScoreCriteriaColored(String name, TextFormatting format) {

		goalName = name + format.getFriendlyName();
		IScoreCriteria.INSTANCES.put(goalName, this);
	}

	public String getName() {

		return goalName;
	}

	public boolean isReadOnly() {

		return false;
	}

	public IScoreCriteria.RenderType getRenderType() {

		return IScoreCriteria.RenderType.INTEGER;
	}

}
