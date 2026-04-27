package net.minecraft.advancements.critereon;

import net.minecraft.advancements.ICriterionInstance;
import net.minecraft.util.ResourceLocation;

public class AbstractCriterionInstance implements ICriterionInstance {

	private final ResourceLocation criterion;

	public AbstractCriterionInstance(ResourceLocation criterionIn) {

		criterion = criterionIn;
	}

	public ResourceLocation getId() {

		return criterion;
	}

	public String toString() {

		return "AbstractCriterionInstance{criterion=" + criterion + '}';
	}

}
