package de.uni_kassel.vs.cn.planDesigner.events;

public interface IModelEventHandler {
    public abstract void handleModelEvent(ModelEvent event);
    public abstract void handleCloseTab(long id);
}
