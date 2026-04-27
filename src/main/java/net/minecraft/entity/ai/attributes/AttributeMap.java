package net.minecraft.entity.ai.attributes;

import com.google.common.collect.Sets;
import net.minecraft.util.LowerStringMap;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class AttributeMap extends AbstractAttributeMap {

	protected final Map<String, IAttributeInstance> instancesByName = new LowerStringMap();
	private final Set<IAttributeInstance> dirtyInstances = Sets.newHashSet();

	public ModifiableAttributeInstance getAttributeInstance(IAttribute attribute) {

		return (ModifiableAttributeInstance) super.getAttributeInstance(attribute);
	}

	public ModifiableAttributeInstance getAttributeInstanceByName(String attributeName) {

		IAttributeInstance iattributeinstance = super.getAttributeInstanceByName(attributeName);

		if (iattributeinstance == null) {
			iattributeinstance = instancesByName.get(attributeName);
		}

		return (ModifiableAttributeInstance) iattributeinstance;
	}

	/**
	 * Registers an attribute with this AttributeMap, returns a modifiable AttributeInstance associated with this map
	 */
	public IAttributeInstance registerAttribute(IAttribute attribute) {

		IAttributeInstance iattributeinstance = super.registerAttribute(attribute);

		if (attribute instanceof RangedAttribute && ((RangedAttribute) attribute).getDescription() != null) {
			instancesByName.put(((RangedAttribute) attribute).getDescription(), iattributeinstance);
		}

		return iattributeinstance;
	}

	protected IAttributeInstance createInstance(IAttribute attribute) {

		return new ModifiableAttributeInstance(this, attribute);
	}

	public void onAttributeModified(IAttributeInstance instance) {

		if (instance.getAttribute().getShouldWatch()) {
			dirtyInstances.add(instance);
		}

		for (IAttribute iattribute : descendantsByParent.get(instance.getAttribute())) {
			ModifiableAttributeInstance modifiableattributeinstance = getAttributeInstance(iattribute);

			if (modifiableattributeinstance != null) {
				modifiableattributeinstance.flagForUpdate();
			}
		}
	}

	public Set<IAttributeInstance> getDirtyInstances() {

		return dirtyInstances;
	}

	public Collection<IAttributeInstance> getWatchedAttributes() {

		Set<IAttributeInstance> set = Sets.newHashSet();

		for (IAttributeInstance iattributeinstance : getAllAttributes()) {
			if (iattributeinstance.getAttribute().getShouldWatch()) {
				set.add(iattributeinstance);
			}
		}

		return set;
	}

}
