package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;

public class ConditionOr implements ICondition {

	final Iterable<ICondition> conditions;

	public ConditionOr(Iterable<ICondition> conditionsIn) {

		conditions = conditionsIn;
	}

	public Predicate<IBlockState> getPredicate(BlockStateContainer blockState) {

		return Predicates.or(Iterables.transform(conditions, condition -> condition == null ? null : condition.getPredicate(blockState)));
	}

}
