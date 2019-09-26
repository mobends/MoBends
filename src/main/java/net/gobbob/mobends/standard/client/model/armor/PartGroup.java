package net.gobbob.mobends.standard.client.model.armor;

import net.gobbob.mobends.core.client.model.ModelPartContainer;
import net.gobbob.mobends.core.client.model.ModelPartTransform;
import net.gobbob.mobends.core.data.EntityData;
import net.minecraft.client.model.ModelRenderer;

import java.util.List;

public class PartGroup<T extends EntityData>
{

    private List<ModelPartContainer> parts;
    private DataPartSelector<T> dataPartSelector;
    private ModelPartSelector modelPartSelector;

    public PartGroup(List<ModelPartContainer> parts, DataPartSelector<T> dataPartSelector, ModelPartSelector modelPartSelector)
    {
        this.parts = parts;
        this.dataPartSelector = dataPartSelector;
        this.modelPartSelector = modelPartSelector;
    }

    public void syncUp(T data)
    {
        parts.forEach(part -> part.syncUp(dataPartSelector.selectPart(data)));
    }

    public void updateVisibility(MutatedArmorModel model)
    {
        parts.forEach(part -> part.setVisible(modelPartSelector.selectPart(model).showModel));
    }

    public void add(ModelPartContainer part)
    {
        parts.add(part);
    }

    public void clear()
    {
        parts.clear();
    }

    public List<ModelPartContainer> getParts()
    {
        return parts;
    }

    @FunctionalInterface
    public interface DataPartSelector<T extends EntityData>
    {

        ModelPartTransform selectPart(T data);

    }

    @FunctionalInterface
    public interface ModelPartSelector<T extends EntityData>
    {

        ModelRenderer selectPart(MutatedArmorModel model);

    }

}
