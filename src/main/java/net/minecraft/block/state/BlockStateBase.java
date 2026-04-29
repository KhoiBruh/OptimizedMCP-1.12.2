package net.minecraft.block.state;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map.Entry;

public abstract class BlockStateBase implements IBlockState {

	private static final Joiner COMMA_JOINER = Joiner.on(',');
	private static final Function<Entry<IProperty<?>, Comparable<?>>, String> MAP_ENTRY_TO_STRING = new Function<>() {
		
		public String apply(Entry<IProperty<?>, Comparable<?>> p_apply_1_) {

			if (p_apply_1_ == null) {
				return "<NULL>";
			} else {
				IProperty<?> iproperty = p_apply_1_.getKey();
				return iproperty.getName() + "=" + getPropertyName(iproperty, p_apply_1_.getValue());
			}
		}

		private <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> entry) {

			return property.getName((T) entry);
		}
	};

	protected static <T> T cyclePropertyValue(Collection<T> values, T currentValue) {

		Iterator<T> iterator = values.iterator();

		while (iterator.hasNext()) {
			if (iterator.next().equals(currentValue)) {
				if (iterator.hasNext()) {
					return iterator.next();
				}

				return values.iterator().next();
			}
		}

		return iterator.next();
	}

	public <T extends Comparable<T>> IBlockState cycleProperty(IProperty<T> property) {

		return withProperty(property, cyclePropertyValue(property.getAllowedValues(), getValue(property)));
	}

	public String toString() {

		StringBuilder stringbuilder = new StringBuilder();
		stringbuilder.append(Block.REGISTRY.getNameForObject(getBlock()));

		if (!getProperties().isEmpty()) {
			stringbuilder.append("[");
			COMMA_JOINER.appendTo(stringbuilder, Iterables.transform(getProperties().entrySet(), MAP_ENTRY_TO_STRING));
			stringbuilder.append("]");
		}

		return stringbuilder.toString();
	}

}
