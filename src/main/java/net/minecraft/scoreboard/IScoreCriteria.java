package net.minecraft.scoreboard;

import com.google.common.collect.Maps;
import net.minecraft.util.text.TextFormat;

import java.util.Map;

public interface IScoreCriteria {

	Map<String, IScoreCriteria> INSTANCES = Maps.newHashMap();
	IScoreCriteria DUMMY = new ScoreCriteria("dummy");
	IScoreCriteria TRIGGER = new ScoreCriteria("trigger");
	IScoreCriteria DEATH_COUNT = new ScoreCriteria("deathCount");
	IScoreCriteria PLAYER_KILL_COUNT = new ScoreCriteria("playerKillCount");
	IScoreCriteria TOTAL_KILL_COUNT = new ScoreCriteria("totalKillCount");
	IScoreCriteria HEALTH = new ScoreCriteriaHealth("health");
	IScoreCriteria FOOD = new ScoreCriteriaReadOnly("food");
	IScoreCriteria AIR = new ScoreCriteriaReadOnly("air");
	IScoreCriteria ARMOR = new ScoreCriteriaReadOnly("armor");
	IScoreCriteria XP = new ScoreCriteriaReadOnly("xp");
	IScoreCriteria LEVEL = new ScoreCriteriaReadOnly("level");
	IScoreCriteria[] TEAM_KILL = new IScoreCriteria[]{new ScoreCriteriaColored("teamkill.", TextFormat.BLACK), new ScoreCriteriaColored("teamkill.", TextFormat.DARK_BLUE), new ScoreCriteriaColored("teamkill.", TextFormat.DARK_GREEN), new ScoreCriteriaColored("teamkill.", TextFormat.DARK_AQUA), new ScoreCriteriaColored("teamkill.", TextFormat.DARK_RED), new ScoreCriteriaColored("teamkill.", TextFormat.DARK_PURPLE), new ScoreCriteriaColored("teamkill.", TextFormat.GOLD), new ScoreCriteriaColored("teamkill.", TextFormat.GRAY), new ScoreCriteriaColored("teamkill.", TextFormat.DARK_GRAY), new ScoreCriteriaColored("teamkill.", TextFormat.BLUE), new ScoreCriteriaColored("teamkill.", TextFormat.GREEN), new ScoreCriteriaColored("teamkill.", TextFormat.AQUA), new ScoreCriteriaColored("teamkill.", TextFormat.RED), new ScoreCriteriaColored("teamkill.", TextFormat.LIGHT_PURPLE), new ScoreCriteriaColored("teamkill.", TextFormat.YELLOW), new ScoreCriteriaColored("teamkill.", TextFormat.WHITE)};
	IScoreCriteria[] KILLED_BY_TEAM = new IScoreCriteria[]{new ScoreCriteriaColored("killedByTeam.", TextFormat.BLACK), new ScoreCriteriaColored("killedByTeam.", TextFormat.DARK_BLUE), new ScoreCriteriaColored("killedByTeam.", TextFormat.DARK_GREEN), new ScoreCriteriaColored("killedByTeam.", TextFormat.DARK_AQUA), new ScoreCriteriaColored("killedByTeam.", TextFormat.DARK_RED), new ScoreCriteriaColored("killedByTeam.", TextFormat.DARK_PURPLE), new ScoreCriteriaColored("killedByTeam.", TextFormat.GOLD), new ScoreCriteriaColored("killedByTeam.", TextFormat.GRAY), new ScoreCriteriaColored("killedByTeam.", TextFormat.DARK_GRAY), new ScoreCriteriaColored("killedByTeam.", TextFormat.BLUE), new ScoreCriteriaColored("killedByTeam.", TextFormat.GREEN), new ScoreCriteriaColored("killedByTeam.", TextFormat.AQUA), new ScoreCriteriaColored("killedByTeam.", TextFormat.RED), new ScoreCriteriaColored("killedByTeam.", TextFormat.LIGHT_PURPLE), new ScoreCriteriaColored("killedByTeam.", TextFormat.YELLOW), new ScoreCriteriaColored("killedByTeam.", TextFormat.WHITE)};

	String getName();

	boolean isReadOnly();

	IScoreCriteria.RenderType getRenderType();

	enum RenderType {
		INTEGER("integer"),
		HEARTS("hearts");

		private static final Map<String, IScoreCriteria.RenderType> BY_NAME = Maps.newHashMap();

		static {
			for (IScoreCriteria.RenderType iscorecriteria$enumrendertype : values()) {
				BY_NAME.put(iscorecriteria$enumrendertype.getRenderType(), iscorecriteria$enumrendertype);
			}
		}

		private final String renderType;

		RenderType(String renderTypeIn) {
			renderType = renderTypeIn;
		}

		public static IScoreCriteria.RenderType getByName(String name) {

			IScoreCriteria.RenderType iscorecriteria$enumrendertype = BY_NAME.get(name);
			return iscorecriteria$enumrendertype == null ? INTEGER : iscorecriteria$enumrendertype;
		}

		public String getRenderType() {
			return renderType;
		}
	}

}
