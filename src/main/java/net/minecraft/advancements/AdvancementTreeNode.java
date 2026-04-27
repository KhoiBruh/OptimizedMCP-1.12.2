package net.minecraft.advancements;

import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.List;

public class AdvancementTreeNode {

	private final Advancement advancement;
	private final AdvancementTreeNode parent;
	private final AdvancementTreeNode sibling;
	private final int index;
	private final List<AdvancementTreeNode> children = Lists.newArrayList();
	private AdvancementTreeNode ancestor;
	private AdvancementTreeNode thread;
	private int x;
	private float y;
	private float mod;
	private float change;
	private float shift;

	public AdvancementTreeNode(Advancement advancementIn, @Nullable AdvancementTreeNode parentIn, @Nullable AdvancementTreeNode siblingIn, int indexIn, int xIn) {

		if (advancementIn.getDisplay() == null) {
			throw new IllegalArgumentException("Can't position an invisible advancement!");
		} else {
			advancement = advancementIn;
			parent = parentIn;
			sibling = siblingIn;
			index = indexIn;
			ancestor = this;
			x = xIn;
			y = -1.0F;
			AdvancementTreeNode advancementtreenode = null;

			for (Advancement advancement : advancementIn.getChildren()) {
				advancementtreenode = buildSubTree(advancement, advancementtreenode);
			}
		}
	}

	@Nullable
	private AdvancementTreeNode buildSubTree(Advancement advancementIn, @Nullable AdvancementTreeNode previous) {

		if (advancementIn.getDisplay() != null) {
			previous = new AdvancementTreeNode(advancementIn, this, previous, children.size() + 1, x + 1);
			children.add(previous);
		} else {
			for (Advancement advancement : advancementIn.getChildren()) {
				previous = buildSubTree(advancement, previous);
			}
		}

		return previous;
	}

	private void firstWalk() {

		if (children.isEmpty()) {
			if (sibling != null) {
				y = sibling.y + 1.0F;
			} else {
				y = 0.0F;
			}
		} else {
			AdvancementTreeNode advancementtreenode = null;

			for (AdvancementTreeNode advancementtreenode1 : children) {
				advancementtreenode1.firstWalk();
				advancementtreenode = advancementtreenode1.apportion(advancementtreenode == null ? advancementtreenode1 : advancementtreenode);
			}

			executeShifts();
			float f = ((children.get(0)).y + (children.get(children.size() - 1)).y) / 2.0F;

			if (sibling != null) {
				y = sibling.y + 1.0F;
				mod = y - f;
			} else {
				y = f;
			}
		}
	}

	private float secondWalk(float p_192319_1_, int p_192319_2_, float p_192319_3_) {

		y += p_192319_1_;
		x = p_192319_2_;

		if (y < p_192319_3_) {
			p_192319_3_ = y;
		}

		for (AdvancementTreeNode advancementtreenode : children) {
			p_192319_3_ = advancementtreenode.secondWalk(p_192319_1_ + mod, p_192319_2_ + 1, p_192319_3_);
		}

		return p_192319_3_;
	}

	private void thirdWalk(float yIn) {

		y += yIn;

		for (AdvancementTreeNode advancementtreenode : children) {
			advancementtreenode.thirdWalk(yIn);
		}
	}

	private void executeShifts() {

		float f = 0.0F;
		float f1 = 0.0F;

		for (int i = children.size() - 1; i >= 0; --i) {
			AdvancementTreeNode advancementtreenode = children.get(i);
			advancementtreenode.y += f;
			advancementtreenode.mod += f;
			f1 += advancementtreenode.change;
			f += advancementtreenode.shift + f1;
		}
	}

	@Nullable
	private AdvancementTreeNode getFirstChild() {

		if (thread != null) {
			return thread;
		} else {
			return !children.isEmpty() ? children.get(0) : null;
		}
	}

	@Nullable
	private AdvancementTreeNode getLastChild() {

		if (thread != null) {
			return thread;
		} else {
			return !children.isEmpty() ? children.get(children.size() - 1) : null;
		}
	}

	private AdvancementTreeNode apportion(AdvancementTreeNode nodeIn) {

		if (sibling == null) {
			return nodeIn;
		} else {
			AdvancementTreeNode advancementtreenode = this;
			AdvancementTreeNode advancementtreenode1 = this;
			AdvancementTreeNode advancementtreenode2 = sibling;
			AdvancementTreeNode advancementtreenode3 = parent.children.get(0);
			float f = mod;
			float f1 = mod;
			float f2 = advancementtreenode2.mod;
			float f3;

			for (f3 = advancementtreenode3.mod; advancementtreenode2.getLastChild() != null && advancementtreenode.getFirstChild() != null; f1 += advancementtreenode1.mod) {
				advancementtreenode2 = advancementtreenode2.getLastChild();
				advancementtreenode = advancementtreenode.getFirstChild();
				advancementtreenode3 = advancementtreenode3.getFirstChild();
				advancementtreenode1 = advancementtreenode1.getLastChild();
				advancementtreenode1.ancestor = this;
				float f4 = advancementtreenode2.y + f2 - (advancementtreenode.y + f) + 1.0F;

				if (f4 > 0.0F) {
					advancementtreenode2.getAncestor(this, nodeIn).moveSubtree(this, f4);
					f += f4;
					f1 += f4;
				}

				f2 += advancementtreenode2.mod;
				f += advancementtreenode.mod;
				f3 += advancementtreenode3.mod;
			}

			if (advancementtreenode2.getLastChild() != null && advancementtreenode1.getLastChild() == null) {
				advancementtreenode1.thread = advancementtreenode2.getLastChild();
				advancementtreenode1.mod += f2 - f1;
			} else {
				if (advancementtreenode.getFirstChild() != null && advancementtreenode3.getFirstChild() == null) {
					advancementtreenode3.thread = advancementtreenode.getFirstChild();
					advancementtreenode3.mod += f - f3;
				}

				nodeIn = this;
			}

			return nodeIn;
		}
	}

	private void moveSubtree(AdvancementTreeNode nodeIn, float p_192316_2_) {

		float f = (float) (nodeIn.index - index);

		if (f != 0.0F) {
			nodeIn.change -= p_192316_2_ / f;
			change += p_192316_2_ / f;
		}

		nodeIn.shift += p_192316_2_;
		nodeIn.y += p_192316_2_;
		nodeIn.mod += p_192316_2_;
	}

	private AdvancementTreeNode getAncestor(AdvancementTreeNode p_192326_1_, AdvancementTreeNode p_192326_2_) {

		return ancestor != null && p_192326_1_.parent.children.contains(ancestor) ? ancestor : p_192326_2_;
	}

	private void updatePosition() {

		if (advancement.getDisplay() != null) {
			advancement.getDisplay().setPosition((float) x, y);
		}

		if (!children.isEmpty()) {
			for (AdvancementTreeNode advancementtreenode : children) {
				advancementtreenode.updatePosition();
			}
		}
	}

	public static void layout(Advancement root) {

		if (root.getDisplay() == null) {
			throw new IllegalArgumentException("Can't position children of an invisible root!");
		} else {
			AdvancementTreeNode advancementtreenode = new AdvancementTreeNode(root, null, null, 1, 0);
			advancementtreenode.firstWalk();
			float f = advancementtreenode.secondWalk(0.0F, 0, advancementtreenode.y);

			if (f < 0.0F) {
				advancementtreenode.thirdWalk(-f);
			}

			advancementtreenode.updatePosition();
		}
	}

}
