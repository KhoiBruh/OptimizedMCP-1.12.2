package net.minecraft.scoreboard;

public class ScoreCriteriaHealth extends ScoreCriteria {

	public ScoreCriteriaHealth(String name) {

		super(name);
	}

	public boolean isReadOnly() {

		return true;
	}

	public IScoreCriteria.RenderType getRenderType() {

		return IScoreCriteria.RenderType.HEARTS;
	}

}
