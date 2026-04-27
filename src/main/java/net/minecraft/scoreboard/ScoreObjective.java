package net.minecraft.scoreboard;

public class ScoreObjective {

	private final Scoreboard scoreboard;
	private final String name;

	/**
	 * The ScoreObjectiveCriteria for this objetive
	 */
	private final IScoreCriteria objectiveCriteria;
	private IScoreCriteria.EnumRenderType renderType;
	private String displayName;

	public ScoreObjective(Scoreboard scoreboard, String nameIn, IScoreCriteria objectiveCriteriaIn) {

		this.scoreboard = scoreboard;
		name = nameIn;
		objectiveCriteria = objectiveCriteriaIn;
		displayName = nameIn;
		renderType = objectiveCriteriaIn.getRenderType();
	}

	public Scoreboard getScoreboard() {

		return scoreboard;
	}

	public String getName() {

		return name;
	}

	public IScoreCriteria getCriteria() {

		return objectiveCriteria;
	}

	public String getDisplayName() {

		return displayName;
	}

	public void setDisplayName(String nameIn) {

		displayName = nameIn;
		scoreboard.onObjectiveDisplayNameChanged(this);
	}

	public IScoreCriteria.EnumRenderType getRenderType() {

		return renderType;
	}

	public void setRenderType(IScoreCriteria.EnumRenderType type) {

		renderType = type;
		scoreboard.onObjectiveDisplayNameChanged(this);
	}

}
