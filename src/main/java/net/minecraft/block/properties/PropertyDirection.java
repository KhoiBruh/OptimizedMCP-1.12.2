package net.minecraft.block.properties;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import net.minecraft.util.Facing;

import java.util.Collection;

public class PropertyDirection extends PropertyEnum<Facing> {

	protected PropertyDirection(String name, Collection<Facing> values) {

		super(name, Facing.class, values);
	}

	/**
	 * Create a new PropertyDirection with the given name
	 */
	public static PropertyDirection create(String name) {

		return create(name, Predicates.alwaysTrue());
	}

	/**
	 * Create a new PropertyDirection with all directions that match the given Predicate
	 */
	public static PropertyDirection create(String name, Predicate<Facing> filter) {

		return create(name, Collections2.filter(Lists.newArrayList(Facing.values()), filter));
	}

	/**
	 * Create a new PropertyDirection for the given direction values
	 */
	public static PropertyDirection create(String name, Collection<Facing> values) {

		return new PropertyDirection(name, values);
	}

}
