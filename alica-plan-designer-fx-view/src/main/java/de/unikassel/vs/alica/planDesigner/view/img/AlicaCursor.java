package de.unikassel.vs.alica.planDesigner.view.img;

import javafx.scene.ImageCursor;

public class AlicaCursor extends ImageCursor {

    public enum Type {
        //transitions
        transition,
        forbidden_transition,
        add_transition,
        // state
        state,
        forbidden_state,
        add_state,
        //successstate
        successstate,
        forbidden_successstate,
        add_failure_state,
        //failurestate
        failurestate,
        forbidden_failurestate,
        add_failurestate,
        //entrypoint
        entrypoint,
        forbidden_entrypoint,
        add_entrypoint,
        //behaviour
        behaviour,
        forbidden_behaviour,
        add_behaviour,
        //synchronization
        synchronization,
        forbidden_synchronization,
        add_synchronization,
        //synctransition
        synctransition,
        forbidden_synctransition,
        add_synctransition,
        //postcondition
        postcondition,
        forbidden_postcondition,
        add_postcondition,

        //plantypes
        tasks,
        plantype,
        masterplan,
        plan,

        //common
        add,
        forbidden
    }

    public AlicaCursor(Type type) {
        super(new AlicaIcon(type.name(), AlicaIcon.Size.SMALL));
    }

    public AlicaCursor(Type type, int x, int y) {
        super(new AlicaIcon(type.name(), AlicaIcon.Size.SMALL), x, y);
    }
}