package de.uni_kassel.vs.cn.planDesigner.alicamodel;

public class AnnotatedPlan extends PlanElement {

    private Plan plan;

    private boolean activated;

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
        this.setName("Annotated" + plan.getName());
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
}