package net.minecraft.scoreboard;

public class ScoreCriteria implements IScoreCriteria {

	private final String dummyName;

	public ScoreCriteria(String name) {

		dummyName = name;
		IScoreCriteria.INSTANCES.put(name, this);
	}

	public String getName() {

		return dummyName;
	}

	public boolean isReadOnly() {

		return false;
	}

	public IScoreCriteria.RenderType getRenderType() {

		return IScoreCriteria.RenderType.INTEGER;
	}

}
