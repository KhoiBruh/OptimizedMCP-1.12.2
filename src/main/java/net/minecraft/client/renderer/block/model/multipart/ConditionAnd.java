package net.minecraft.client.renderer.block.model.multipart;

import com.google.common.base.Function;
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

	public Predicate<IBlockState> getPredicate(final BlockStateContainer blockState) {

		return Predicates.and(Iterables.transform(conditions, new Function<ICondition, Predicate<IBlockState>>() {
			
			public Predicate<IBlockState> apply(ICondition p_apply_1_) {

				return p_apply_1_ == null ? null : p_apply_1_.getPredicate(blockState);
			}
		}));
	}

}
