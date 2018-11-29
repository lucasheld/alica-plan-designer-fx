package de.unikassel.vs.alica.planDesigner.command.delete;

import de.unikassel.vs.alica.planDesigner.alicamodel.State;
import de.unikassel.vs.alica.planDesigner.alicamodel.Transition;
import de.unikassel.vs.alica.planDesigner.command.AbstractCommand;
import de.unikassel.vs.alica.planDesigner.modelmanagement.ModelManager;
import de.unikassel.vs.alica.planDesigner.uiextensionmodel.PlanModelVisualisationObject;
import de.unikassel.vs.alica.planDesigner.uiextensionmodel.PmlUiExtension;

public class DeleteTransitionInPlan extends AbstractCommand{

    private final PlanModelVisualisationObject parentOfElement;
    private PmlUiExtension pmlUiExtension;
    private State inState;
    private State outState;
    private Transition transition;

    public DeleteTransitionInPlan(ModelManager manager, Transition transition, PlanModelVisualisationObject parentOfElement) {
        super(manager);
        this.parentOfElement = parentOfElement;
        this.transition = transition;
    }

    private void saveForLaterRetrieval() {
        pmlUiExtension = parentOfElement.getPmlUiExtensionMap().getExtension().get(transition);
        outState = transition.getOutState();
        inState = transition.getInState();
    }

    @Override
    public void doCommand() {
        saveForLaterRetrieval();

        parentOfElement.getPlan().getTransitions().remove(transition);
        parentOfElement.getPmlUiExtensionMap().getExtension().remove(transition);
        transition.setInState(null);
        transition.setOutState(null);
    }

    @Override
    public void undoCommand() {
        parentOfElement.getPlan().getTransitions().add(transition);
        parentOfElement.getPmlUiExtensionMap().getExtension().put(transition, pmlUiExtension);
        transition.setInState(inState);
        transition.setOutState(outState);
    }
}