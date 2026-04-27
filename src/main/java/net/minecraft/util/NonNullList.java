package net.minecraft.util;

import org.apache.commons.lang3.Validate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NonNullList<E> extends AbstractList<E> {

	private final List<E> delegate;
	private final E defaultElement;

	public static <E> NonNullList<E> create() {

		return new NonNullList<E>();
	}

	public static <E> NonNullList<E> withSize(int size, E fill) {

		Validate.notNull(fill);
		Object[] aobject = new Object[size];
		Arrays.fill(aobject, fill);
		return new NonNullList<E>(Arrays.asList((E[]) aobject), fill);
	}

	public static <E> NonNullList<E> from(E defaultElementIn, E... elements) {

		return new NonNullList<E>(Arrays.asList(elements), defaultElementIn);
	}

	protected NonNullList() {

		this(new ArrayList(), null);
	}

	protected NonNullList(List<E> delegateIn, @Nullable E listType) {

		delegate = delegateIn;
		defaultElement = listType;
	}

	@Nonnull
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
			for (int i = 0; i < size(); ++i) {
				set(i, defaultElement);
			}
		}
	}

}
