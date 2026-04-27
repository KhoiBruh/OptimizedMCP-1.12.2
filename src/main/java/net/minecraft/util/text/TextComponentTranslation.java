package net.minecraft.util.text;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import net.minecraft.util.text.translation.I18n;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextComponentTranslation extends TextComponentBase {

	public static final Pattern STRING_VARIABLE_PATTERN = Pattern.compile("%(?:(\\d+)\\$)?([A-Za-z%]|$)");
	private final String key;
	private final Object[] formatArgs;
	private final Object syncLock = new Object();

	@VisibleForTesting
	List<ITextComponent> children = Lists.newArrayList();
	private long lastTranslationUpdateTimeInMilliseconds = -1L;

	public TextComponentTranslation(String translationKey, Object... args) {

		key = translationKey;
		formatArgs = args;

		for (Object object : args) {
			if (object instanceof ITextComponent) {
				((ITextComponent) object).getStyle().setParentStyle(getStyle());
			}
		}
	}

	@VisibleForTesting

	/**
	 * Ensures that all of the children are up to date with the most recent translation mapping.
	 */
	synchronized void ensureInitialized() {

		synchronized (syncLock) {
			long i = I18n.getLastTranslationUpdateTimeInMilliseconds();

			if (i == lastTranslationUpdateTimeInMilliseconds) {
				return;
			}

			lastTranslationUpdateTimeInMilliseconds = i;
			children.clear();
		}

		try {
			initializeFromFormat(I18n.translateToLocal(key));
		} catch (TextComponentTranslationFormatException textcomponenttranslationformatexception) {
			children.clear();

			try {
				initializeFromFormat(I18n.translateToFallback(key));
			} catch (TextComponentTranslationFormatException var5) {
				throw textcomponenttranslationformatexception;
			}
		}
	}

	/**
	 * Initializes the content of this component, substituting in variables.
	 */
	protected void initializeFromFormat(String format) {

		boolean flag = false;
		Matcher matcher = STRING_VARIABLE_PATTERN.matcher(format);
		int i = 0;
		int j = 0;

		try {
			int l;

			for (; matcher.find(j); j = l) {
				int k = matcher.start();
				l = matcher.end();

				if (k > j) {
					TextComponentString textcomponentstring = new TextComponentString(String.format(format.substring(j, k)));
					textcomponentstring.getStyle().setParentStyle(getStyle());
					children.add(textcomponentstring);
				}

				String s2 = matcher.group(2);
				String s = format.substring(k, l);

				if ("%".equals(s2) && "%%".equals(s)) {
					TextComponentString textcomponentstring2 = new TextComponentString("%");
					textcomponentstring2.getStyle().setParentStyle(getStyle());
					children.add(textcomponentstring2);
				} else {
					if (!"s".equals(s2)) {
						throw new TextComponentTranslationFormatException(this, "Unsupported format: '" + s + "'");
					}

					String s1 = matcher.group(1);
					int i1 = s1 != null ? Integer.parseInt(s1) - 1 : i++;

					if (i1 < formatArgs.length) {
						children.add(getFormatArgumentAsComponent(i1));
					}
				}
			}

			if (j < format.length()) {
				TextComponentString textcomponentstring1 = new TextComponentString(String.format(format.substring(j)));
				textcomponentstring1.getStyle().setParentStyle(getStyle());
				children.add(textcomponentstring1);
			}
		} catch (IllegalFormatException illegalformatexception) {
			throw new TextComponentTranslationFormatException(this, illegalformatexception);
		}
	}

	private ITextComponent getFormatArgumentAsComponent(int index) {

		if (index >= formatArgs.length) {
			throw new TextComponentTranslationFormatException(this, index);
		} else {
			Object object = formatArgs[index];
			ITextComponent itextcomponent;

			if (object instanceof ITextComponent) {
				itextcomponent = (ITextComponent) object;
			} else {
				itextcomponent = new TextComponentString(object == null ? "null" : object.toString());
				itextcomponent.getStyle().setParentStyle(getStyle());
			}

			return itextcomponent;
		}
	}

	/**
	 * Sets the style of this component and updates the parent style of all of the sibling components.
	 */
	public ITextComponent setStyle(Style style) {

		super.setStyle(style);

		for (Object object : formatArgs) {
			if (object instanceof ITextComponent) {
				((ITextComponent) object).getStyle().setParentStyle(getStyle());
			}
		}

		if (lastTranslationUpdateTimeInMilliseconds > -1L) {
			for (ITextComponent itextcomponent : children) {
				itextcomponent.getStyle().setParentStyle(style);
			}
		}

		return this;
	}

	public Iterator<ITextComponent> iterator() {

		ensureInitialized();
		return Iterators.concat(createDeepCopyIterator(children), createDeepCopyIterator(siblings));
	}

	/**
	 * Gets the raw content of this component (but not its sibling components), without any formatting codes. For
	 * example, this is the raw text in a {@link TextComponentString}, but it's the translated text for a {@link
	 * TextComponentTranslation} and it's the score value for a {@link TextComponentScore}.
	 */
	public String getUnformattedComponentText() {

		ensureInitialized();
		StringBuilder stringbuilder = new StringBuilder();

		for (ITextComponent itextcomponent : children) {
			stringbuilder.append(itextcomponent.getUnformattedComponentText());
		}

		return stringbuilder.toString();
	}

	/**
	 * Creates a copy of this component.  Almost a deep copy, except the style is shallow-copied.
	 */
	public TextComponentTranslation createCopy() {

		Object[] aobject = new Object[formatArgs.length];

		for (int i = 0; i < formatArgs.length; ++i) {
			if (formatArgs[i] instanceof ITextComponent) {
				aobject[i] = ((ITextComponent) formatArgs[i]).createCopy();
			} else {
				aobject[i] = formatArgs[i];
			}
		}

		TextComponentTranslation textcomponenttranslation = new TextComponentTranslation(key, aobject);
		textcomponenttranslation.setStyle(getStyle().createShallowCopy());

		for (ITextComponent itextcomponent : getSiblings()) {
			textcomponenttranslation.appendSibling(itextcomponent.createCopy());
		}

		return textcomponenttranslation;
	}

	public boolean equals(Object p_equals_1_) {

		if (this == p_equals_1_) {
			return true;
		} else if (!(p_equals_1_ instanceof TextComponentTranslation textcomponenttranslation)) {
			return false;
		} else {
			return Arrays.equals(formatArgs, textcomponenttranslation.formatArgs) && key.equals(textcomponenttranslation.key) && super.equals(p_equals_1_);
		}
	}

	public int hashCode() {

		int i = super.hashCode();
		i = 31 * i + key.hashCode();
		i = 31 * i + Arrays.hashCode(formatArgs);
		return i;
	}

	public String toString() {

		return "TranslatableComponent{key='" + key + '\'' + ", args=" + Arrays.toString(formatArgs) + ", siblings=" + siblings + ", style=" + getStyle() + '}';
	}

	/**
	 * Gets the key used to translate this component.
	 */
	public String getKey() {

		return key;
	}

	/**
	 * Gets the object array that is used to translate the key.
	 */
	public Object[] getFormatArgs() {

		return formatArgs;
	}

}
