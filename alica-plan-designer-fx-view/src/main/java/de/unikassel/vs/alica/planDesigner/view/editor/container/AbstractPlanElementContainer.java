package de.unikassel.vs.alica.planDesigner.view.editor.container;


import de.unikassel.vs.alica.planDesigner.controller.MainWindowController;
import de.unikassel.vs.alica.planDesigner.handlerinterfaces.IShowGeneratedSourcesEventHandler;
import de.unikassel.vs.alica.planDesigner.view.editor.tab.planTab.PlanEditorGroup;
import de.unikassel.vs.alica.planDesigner.view.editor.tab.planTab.PlanTab;
import de.unikassel.vs.alica.planDesigner.view.editor.tools.AbstractTool;
import de.unikassel.vs.alica.planDesigner.view.menu.ShowGeneratedSourcesMenuItem;
import de.unikassel.vs.alica.planDesigner.view.model.*;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.ArrayList;

/**
 * The {@link AbstractPlanElementContainer} is a base class for visual representations, with a alicamodel object to hold changes from the visualisation
 * that will be written back to resource later.
 */
public abstract class AbstractPlanElementContainer extends Pane implements DraggableEditorElement {

    private ViewModelElement viewModelElement;
    private IShowGeneratedSourcesEventHandler showGeneratedSourcesEventHandler;
    protected Node visualRepresentation;
    private PlanTab planTab;

    /**
     * @param viewModelElement
     * @param planTab
     */
    public AbstractPlanElementContainer(ViewModelElement viewModelElement, IShowGeneratedSourcesEventHandler showGeneratedSourcesEventHandler, PlanTab planTab) {
        this.viewModelElement = viewModelElement;
        this.showGeneratedSourcesEventHandler = showGeneratedSourcesEventHandler;
        this.planTab = planTab;
        setBackground(Background.EMPTY);
        setPickOnBounds(false);
        addEventFilter(MouseEvent.MOUSE_CLICKED, getMouseClickedEventHandler(viewModelElement));
        setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent e) {
                ContextMenu contextMenu = new ContextMenu(new ShowGeneratedSourcesMenuItem(AbstractPlanElementContainer.this.viewModelElement.getId(), AbstractPlanElementContainer.this.showGeneratedSourcesEventHandler));
                contextMenu.show(AbstractPlanElementContainer.this, e.getScreenX(), e.getScreenY());
            }
        });
        // prohibit containers from growing indefinitely (especially transition containers)
        setMaxSize(1, 1);
    }

    public PlanEditorGroup getPlanEditorGroup() {
        return planTab.getPlanEditorGroup();
    }

    /**
     * Sets the selection flag for the editor when modelElementId is clicked.
     * Unless the last click was performed as part of a tool phase.
     *
     * @param modelElement
     * @return
     */
    private EventHandler<MouseEvent> getMouseClickedEventHandler(ViewModelElement modelElement) {
        return new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                PlanTab planTab = ((PlanTab) MainWindowController.getInstance().getEditorTabPane().getSelectionModel().getSelectedItem());
                // Was the last click performed in the context of a tool?
                AbstractTool recentlyDoneTool = planTab.getEditorToolBar().getRecentlyDoneTool();
                if (recentlyDoneTool != null) {
                    recentlyDoneTool.setRecentlyDone(false);
                } else {
                    ArrayList<Pair<ViewModelElement, AbstractPlanElementContainer>> selectedElements = new ArrayList<>();
                    selectedElements.add(new Pair<>(modelElement, AbstractPlanElementContainer.this));
                    planTab.getSelectedPlanElements().setValue(selectedElements);
                }
            }
        };
    }


    public Node getVisualRepresentation() {
        return visualRepresentation;
    }

    @Override
    public ViewModelElement getViewModelElement() {
        return viewModelElement;
    }

    public abstract void setupContainer();

    @Override
    public void makeDraggable(Node node) {
        final DragContext dragContext = new DragContext();

        // disable mouse events for all children
        node.addEventHandler(MouseEvent.ANY, Event::consume);

        node.addEventHandler(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                AbstractPlanElementContainer.this.setDragged(false);
                // remember initial mouse cursor coordinates
                // and node position
                dragContext.mouseAnchorX = mouseEvent.getSceneX();
                dragContext.mouseAnchorY = mouseEvent.getSceneY();
                dragContext.initialLayoutX = node.getLayoutX();
                dragContext.initialLayoutY = node.getLayoutY();
            }
        });

        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                // shift node from its initial position by delta
                // calculated from mouse cursor movement
                AbstractPlanElementContainer.this.setDragged(true);

                // set temporary translation
                node.setTranslateX(mouseEvent.getSceneX() - dragContext.mouseAnchorX);
                node.setTranslateY(mouseEvent.getSceneY() - dragContext.mouseAnchorY);
                //System.out.println("X: " + mouseEvent.getX() + " Y:" + mouseEvent.getY());
            }
        });

        node.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            // save final position in actual bendpoint
            if (wasDragged()) {
                // reset translation and set layout to actual position
                node.setTranslateX(0);
                node.setTranslateY(0);
                node.setLayoutX(dragContext.initialLayoutX + mouseEvent.getSceneX() - dragContext.mouseAnchorX);
                node.setLayoutY(dragContext.initialLayoutY + mouseEvent.getSceneY() - dragContext.mouseAnchorY);

                planTab.fireChangePositionEvent(this, viewModelElement.getType(), node.getLayoutX(), node.getLayoutY());
                mouseEvent.consume();
                redrawElement();
            }
        });
    }

    /**
     * Making the {@link AbstractPlanElementContainer} update its position, whenever the {@link PlanElementViewModel}
     * changes its coordinates.
     *
     * Method also sets the current position according to the {@link PlanElementViewModel} on call.
     *
     * @param node  the Node to change the position of
     * @param planElementViewModel  the element, that containsPlan the coordinates to listen to
     */
    public void createPositionListeners(Node node, PlanElementViewModel planElementViewModel){
        //Set to initial Position
        node.setLayoutX(planElementViewModel.getXPosition());
        node.setLayoutY(planElementViewModel.getYPosition());

        //Create Listeners
        planElementViewModel.xPositionProperty().addListener((observable, oldValue, newValue) -> {
            node.setLayoutX(newValue.doubleValue());
            Platform.runLater(this::redrawElement);
        });

        planElementViewModel.yPositionProperty().addListener((observable, oldValue, newValue) -> {
            node.setLayoutY(newValue.doubleValue());
            Platform.runLater(this::redrawElement);
        });
    }

    public void createAbstractPlanToStateListeners(Node node, StateViewModel state) {
        state.getPlanElements().addListener(new ListChangeListener<PlanElementViewModel>() {
            @Override
            public void onChanged(Change<? extends PlanElementViewModel> c) {
                Platform.runLater(AbstractPlanElementContainer.this::redrawElement);
            }
        });
    }

    public void createTaskToEntryPointListeners(Node node, EntryPointViewModel entryPoint){

        entryPoint.taskProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(this::redrawElement);
        });
    }

    /**
     * Sets the standard effect for the {@link AbstractPlanElementContainer}.
     * This should be overwritten by a child class for individual styling. By default no effect is set.
     */
    public void setEffectToStandard() {
        setEffect(null);
    }

    public abstract Color getVisualisationColor();

    @Override
    public void redrawElement() {}

    @Override
    public void setDragged(boolean dragged) {}

    @Override
    public boolean wasDragged() {
        return false;
    }
}
