package net.minecraft.scoreboard;

import com.google.common.collect.Sets;
import net.minecraft.util.text.TextFormatting;
import java.util.Collection;
import java.util.Set;

public class ScorePlayerTeam extends Team {

	private final Scoreboard scoreboard;
	private final String name;
	private final Set<String> membershipSet = Sets.newHashSet();
	private String displayName;
	private String prefix = "";
	private String suffix = "";
	private boolean allowFriendlyFire = true;
	private boolean canSeeFriendlyInvisibles = true;
	private Team.Visible nameTagVisibility = Team.Visible.ALWAYS;
	private Team.Visible deathMessageVisibility = Team.Visible.ALWAYS;
	private TextFormatting color = TextFormatting.RESET;
	private Team.CollisionRule collisionRule = Team.CollisionRule.ALWAYS;

	public ScorePlayerTeam(Scoreboard scoreboardIn, String name) {

		scoreboard = scoreboardIn;
		this.name = name;
		displayName = name;
	}

	/**
	 * Formats the given text as a member of the given team, using the team's prefix and suffix.
	 */
	public static String formatPlayerName(Team teamIn, String string) {

		return teamIn == null ? string : teamIn.formatString(string);
	}

	/**
	 * Retrieve the name by which this team is registered in the scoreboard
	 */
	public String getName() {

		return name;
	}

	/**
	 * Gets the display name for this team.
	 */
	public String getDisplayName() {

		return displayName;
	}

	/**
	 * Sets the display name for this team.
	 */
	public void setDisplayName(String name) {

		if (name == null) {
			throw new IllegalArgumentException("Name cannot be null");
		} else {
			displayName = name;
			scoreboard.broadcastTeamInfoUpdate(this);
		}
	}

	public Collection<String> getMembershipCollection() {

		return membershipSet;
	}

	/**
	 * Gets the prefix applied before the names of members of this team. Usually a single format code, but may be any
	 * text.
	 * <p>
	 * Note that the prefix is also used to determine the color for the "glowing" effect - see {@link
	 * net.minecraft.client.renderer.entity.Renderer#getTeamColor Renderer.getTeamColor}.
	 */
	public String getPrefix() {

		return prefix;
	}

	/**
	 * Sets the prefix applied before the names of members of this team.
	 */
	public void setPrefix(String prefix) {

		if (prefix == null) {
			throw new IllegalArgumentException("Prefix cannot be null");
		} else {
			this.prefix = prefix;
			scoreboard.broadcastTeamInfoUpdate(this);
		}
	}

	/**
	 * Gets the suffix applied after the names of members of this team. Usually a single reset format code, but may be
	 * any text.
	 */
	public String getSuffix() {

		return suffix;
	}

	/**
	 * Sets the suffix applied after the names of members of this team.
	 */
	public void setSuffix(String suffix) {

		this.suffix = suffix;
		scoreboard.broadcastTeamInfoUpdate(this);
	}

	/**
	 * Formats the given text as a member of this team, using the prefix and suffix.
	 */
	public String formatString(String input) {

		return getPrefix() + input + getSuffix();
	}

	/**
	 * Checks whether friendly fire (PVP between members of the team) is allowed.
	 */
	public boolean getAllowFriendlyFire() {

		return allowFriendlyFire;
	}

	/**
	 * Sets whether friendly fire (PVP between members of the team) is allowed.
	 */
	public void setAllowFriendlyFire(boolean friendlyFire) {

		allowFriendlyFire = friendlyFire;
		scoreboard.broadcastTeamInfoUpdate(this);
	}

	/**
	 * Checks whether members of this team can see other members that are invisible.
	 */
	public boolean getSeeFriendlyInvisiblesEnabled() {

		return canSeeFriendlyInvisibles;
	}

	/**
	 * Sets whether members of this team can see other members that are invisible.
	 */
	public void setSeeFriendlyInvisiblesEnabled(boolean friendlyInvisibles) {

		canSeeFriendlyInvisibles = friendlyInvisibles;
		scoreboard.broadcastTeamInfoUpdate(this);
	}

	/**
	 * Gets the visibility flags for player name tags.
	 */
	public Team.Visible getNameTagVisibility() {

		return nameTagVisibility;
	}

	/**
	 * Sets the visibility flags for player name tags.
	 */
	public void setNameTagVisibility(Team.Visible visibility) {

		nameTagVisibility = visibility;
		scoreboard.broadcastTeamInfoUpdate(this);
	}

	/**
	 * Gets the visibility flags for player death messages.
	 */
	public Team.Visible getDeathMessageVisibility() {

		return deathMessageVisibility;
	}

	/**
	 * Sets the visibility flags for player death messages.
	 */
	public void setDeathMessageVisibility(Team.Visible visibility) {

		deathMessageVisibility = visibility;
		scoreboard.broadcastTeamInfoUpdate(this);
	}

	/**
	 * Gets the rule to be used for handling collisions with members of this team.
	 */
	public Team.CollisionRule getCollisionRule() {

		return collisionRule;
	}

	/**
	 * Sets the rule to be used for handling collisions with members of this team.
	 */
	public void setCollisionRule(Team.CollisionRule rule) {

		collisionRule = rule;
		scoreboard.broadcastTeamInfoUpdate(this);
	}

	/**
	 * Gets a bitmask containing the friendly fire and invisibles flags.
	 */
	public int getFriendlyFlags() {

		int i = 0;

		if (getAllowFriendlyFire()) {
			i |= 1;
		}

		if (getSeeFriendlyInvisiblesEnabled()) {
			i |= 2;
		}

		return i;
	}

	/**
	 * Sets friendly fire and invisibles flags based off of the given bitmask.
	 */
	public void setFriendlyFlags(int flags) {

		setAllowFriendlyFire((flags & 1) > 0);
		setSeeFriendlyInvisiblesEnabled((flags & 2) > 0);
	}

	/**
	 * Gets the color for this team. The team color is used mainly for team kill objectives and team-specific setDisplay
	 * usage; it does _not_ affect all situations (for instance, the prefix is used for the glowing effect).
	 */
	public TextFormatting getColor() {

		return color;
	}

	/**
	 * Sets the color for this team. The team color is used mainly for team kill objectives and team-specific setDisplay
	 * usage; it does _not_ affect all situations (for instance, the prefix is used for the glowing effect).
	 */
	public void setColor(TextFormatting color) {

		this.color = color;
	}

}
