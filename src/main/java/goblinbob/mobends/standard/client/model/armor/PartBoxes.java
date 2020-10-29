package goblinbob.mobends.standard.client.model.armor;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;

import java.util.*;

public class PartBoxes
{
    protected HashMap<ModelRenderer, List<ModelBox>> modelToBoxesMap = new HashMap<>();

    public void put(ModelRenderer renderer, ModelBox box)
    {
        if (!modelToBoxesMap.containsKey(renderer))
        {
            modelToBoxesMap.put(renderer, new LinkedList<>());
        }

        modelToBoxesMap.get(renderer).add(box);
    }

    public void clear()
    {
        this.modelToBoxesMap.clear();
    }

    public void clearRenderer(ModelRenderer renderer)
    {
        modelToBoxesMap.remove(renderer);
    }

    public Set<Map.Entry<ModelRenderer, List<ModelBox>>> entrySet()
    {
        return modelToBoxesMap.entrySet();
    }

    public Set<ModelRenderer> keySet()
    {
        return modelToBoxesMap.keySet();
    }
}
