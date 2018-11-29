package de.unikassel.vs.alica.planDesigner.command.delete;

import de.unikassel.vs.alica.planDesigner.alicamodel.Task;
import de.unikassel.vs.alica.planDesigner.command.AbstractCommand;
import de.unikassel.vs.alica.planDesigner.modelmanagement.ModelManager;
import de.unikassel.vs.alica.planDesigner.modelmanagement.ModelModificationQuery;
import de.unikassel.vs.alica.planDesigner.modelmanagement.Types;

public class DeleteTaskFromRepository extends AbstractCommand {

    ModelModificationQuery mmq;
    protected Task taskToDelete;

    public DeleteTaskFromRepository(ModelManager modelManager, ModelModificationQuery mmq) {
        super(modelManager);
        this.mmq = mmq;
    }

    @Override
    public void doCommand() {
        for (Task task : modelManager.getTaskRepository().getTasks()) {
            if (task.getId() == mmq.getElementId()) {
                taskToDelete = task;
                break;
            }
        }

        // put outside the loop, in order to avoid concurrent modification exception
        if (taskToDelete != null) {
            modelManager.removePlanElement(Types.TASK, taskToDelete, null, false);
        }
    }

    @Override
    public void undoCommand() {
        modelManager.addPlanElement(Types.TASK, taskToDelete, null, false);
    }
}