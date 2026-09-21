package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;

@FunctionalInterface
public interface IDriverFactory<T extends DriverItemTemplate>
{

    IPoseItem create(IKumoInstancingContext context, Skeleton skeleton, T template) throws MalformedKumoTemplateException;

}
