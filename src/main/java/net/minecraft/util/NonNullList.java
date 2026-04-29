package net.minecraft.util;

import org.apache.commons.lang3.Validate;

import java.util.*;

public class NonNullList<E> extends AbstractList<E> {

	private final List<E> delegate;
	private final E defaultElement;

	protected NonNullList() {

		this(new ArrayList<>(), null);
	}

	protected NonNullList(List<E> delegateIn, E listType) {

		delegate = delegateIn;
		defaultElement = listType;
	}

	public static <E> NonNullList<E> create() {

		return new NonNullList<>();
	}

	public static <E> NonNullList<E> withSize(int size, E fill) {

		Validate.notNull(fill);
		Object[] aobject = new Object[size];
		Arrays.fill(aobject, fill);
		return new NonNullList<>(Arrays.asList((E[]) aobject), fill);
	}

	@SafeVarargs
	public static <E> NonNullList<E> from(E defaultElementIn, E... elements) {

		return new NonNullList<>(Arrays.asList(elements), defaultElementIn);
	}
	public E get(int p_get_1_) {

		return delegate.get(p_get_1_);
	}

	public E set(int p_set_1_, E p_set_2_) {

		Validate.notNull(p_set_2_);
		return delegate.set(p_set_1_, p_set_2_);
	}

	public void add(int p_add_1_, E p_add_2_) {

		Validate.notNull(p_add_2_);
		delegate.add(p_add_1_, p_add_2_);
	}

	public E remove(int p_remove_1_) {

		return delegate.remove(p_remove_1_);
	}

	public int size() {

		return delegate.size();
	}

	public void clear() {

		if (defaultElement == null) {
			super.clear();
		} else {
			Collections.fill(this, defaultElement);
		}
	}

}
