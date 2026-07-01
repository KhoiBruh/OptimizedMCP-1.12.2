package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class ConditionAnd implements ICondition {

	private final Iterable<ICondition> conditions;

	public ConditionAnd(Iterable<ICondition> conditionsIn) {

		conditions = conditionsIn;
	}

	public Predicate<IBlockState> getPredicate(BlockStateContainer blockState) {

		return Predicates.and(Iterables.transform(conditions, condition -> condition == null ? null : condition.getPredicate(blockState)));
	}

}
