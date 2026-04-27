package net.minecraft.util.text;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

import java.util.Iterator;
import java.util.List;

public abstract class TextComponentBase implements ITextComponent {

	protected List<ITextComponent> siblings = Lists.newArrayList();
	private Style style;

	public static Iterator<ITextComponent> createDeepCopyIterator(Iterable<ITextComponent> components) {

		Iterator<ITextComponent> iterator = Iterators.concat(Iterators.transform(components.iterator(), p_apply_1_ -> p_apply_1_.iterator()));
		iterator = Iterators.transform(iterator, p_apply_1_ -> {

			ITextComponent itextcomponent = p_apply_1_.createCopy();
			itextcomponent.setStyle(itextcomponent.getStyle().createDeepCopy());
			return itextcomponent;
		});
		return iterator;
	}

	/**
	 * Adds a new component to the end of the sibling list, setting that component's style's parent style to this
	 * component's style.
	 *
	 * @return This component, for chaining (and not the newly added component)
	 */
	public ITextComponent appendSibling(ITextComponent component) {

		component.getStyle().setParentStyle(getStyle());
		siblings.add(component);
		return this;
	}

	public List<ITextComponent> getSiblings() {

		return siblings;
	}

	/**
	 * Adds a new component to the end of the sibling list, with the specified text. Same as calling {@link
	 * #appendSibling(ITextComponent)} with a new {@link TextComponentString}.
	 *
	 * @return This component, for chaining (and not the newly added component)
	 */
	public ITextComponent appendText(String text) {

		return appendSibling(new TextComponentString(text));
	}

	/**
	 * Sets the style of this component and updates the parent style of all of the sibling components.
	 */
	public ITextComponent setStyle(Style style) {

		this.style = style;

		for (ITextComponent itextcomponent : siblings) {
			itextcomponent.getStyle().setParentStyle(getStyle());
		}

		return this;
	}

	/**
	 * Gets the style of this component. Returns a direct reference; changes to this style will modify the style of this
	 * component (IE, there is no need to call {@link #setStyle(Style)} again after modifying it).
	 * <p>
	 * If this component's style is currently <code>null</code>, it will be initialized to the default style, and the
	 * parent style of all sibling components will be set to that style. (IE, changes to this style will also be
	 * reflected in sibling components.)
	 * <p>
	 * This method never returns <code>null</code>.
	 */
	public Style getStyle() {

		if (style == null) {
			style = new Style();

			for (ITextComponent itextcomponent : siblings) {
				itextcomponent.getStyle().setParentStyle(style);
			}
		}

		return style;
	}

	public Iterator<ITextComponent> iterator() {

		return Iterators.concat(Iterators.forArray(this), createDeepCopyIterator(siblings));
	}

	/**
	 * Gets the text of this component <em>and all sibling components</em>, without any formatting codes.
	 */
	public final String getUnformattedText() {

		StringBuilder stringbuilder = new StringBuilder();

		for (ITextComponent itextcomponent : this) {
			stringbuilder.append(itextcomponent.getUnformattedComponentText());
		}

		return stringbuilder.toString();
	}

	/**
	 * Gets the text of this component <em>and all sibling components</em>, with formatting codes added for rendering.
	 */
	public final String getFormattedText() {

		StringBuilder stringbuilder = new StringBuilder();

		for (ITextComponent itextcomponent : this) {
			String s = itextcomponent.getUnformattedComponentText();

			if (!s.isEmpty()) {
				stringbuilder.append(itextcomponent.getStyle().getFormattingCode());
				stringbuilder.append(s);
				stringbuilder.append(TextFormatting.RESET);
			}
		}

		return stringbuilder.toString();
	}

	public boolean equals(Object p_equals_1_) {

		if (this == p_equals_1_) {
			return true;
		} else if (!(p_equals_1_ instanceof TextComponentBase textcomponentbase)) {
			return false;
		} else {
			return siblings.equals(textcomponentbase.siblings) && getStyle().equals(textcomponentbase.getStyle());
		}
	}

	public int hashCode() {

		return 31 * style.hashCode() + siblings.hashCode();
	}

	public String toString() {

		return "BaseComponent{style=" + style + ", siblings=" + siblings + '}';
	}

}
