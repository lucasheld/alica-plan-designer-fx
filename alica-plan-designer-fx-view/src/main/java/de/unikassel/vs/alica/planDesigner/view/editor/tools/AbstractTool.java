package de.unikassel.vs.alica.planDesigner.view.editor.tools;

import de.unikassel.vs.alica.planDesigner.view.editor.container.AbstractPlanElementContainer;
import de.unikassel.vs.alica.planDesigner.view.editor.tab.planTab.PlanTab;

import de.unikassel.vs.alica.planDesigner.view.model.ViewModelElement;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TabPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * The {@link AbstractTool} interface provides methods for the tools in the {@link EditorToolBar}.
 * It helps to generalize the usage of these tools for the following workflow:
 * tool is selected (start of the phase) -> Event handlerinterfaces for special actions on the PlanEditorGroup
 * are registered -> The actions are performed. A new alicamodel object is created.
 * Or the actions are aborted. -> The phase ends. The event handlers will be removed. and the editor is usable as before.
 */
public abstract class AbstractTool {
    protected TabPane planEditorTabPane;
    private PlanTab planTab;
    // Contains Icon and Text and triggers the drag events (start and stop).
    private DraggableHBox draggableHBox;
    // Shadow Effect set on draggableHBox when dragged
    private static final DropShadow dropShadowEffect = new DropShadow(10, Color.GREY);
    private HashMap<EventType, EventHandler> defaultHandlerMap;
    protected HashMap<EventType, EventHandler> customHandlerMap;
    protected Cursor previousCursor;


    private boolean recentlyDone;
    private EventHandler<? super ScrollEvent> onScrollInPlanTab;
    private ScrollPane.ScrollBarPolicy vBarPolicy;
    private ScrollPane.ScrollBarPolicy hBarPolicy;
    private double vmax;
    private double hmax;

    public AbstractTool(TabPane planEditorTabPane, PlanTab planTab) {
        this.planEditorTabPane = planEditorTabPane;
        this.planTab = planTab;
        dropShadowEffect.setSpread(0.5);
    }

    protected abstract void initHandlerMap();

    public abstract DraggableHBox createToolUI();

    protected Node getPlanEditorTabPane() {
        return planEditorTabPane;
    }

    protected Map<EventType, EventHandler> defaultHandlers() {
        if (defaultHandlerMap == null) {
            defaultHandlerMap = new HashMap<>();
            // The tool phase is ended, when the courser leaves the scene.
            defaultHandlerMap.put(MouseEvent.MOUSE_DRAGGED, new EventHandler() {
                @Override
                public void handle(Event event) {
                    MouseEvent e = (MouseEvent) event;
                    if (e.getSceneX() + 5 > getPlanEditorTabPane().getScene().getWidth()
                        || e.getSceneY() + 5 > getPlanEditorTabPane().getScene().getHeight()
                        || e.getSceneX() - 5 < 0 || e.getSceneY() - 5 < 0) {
                     endPhase();
                    }
                }
            });

            //Listener, that ends a phase, when the mouse is released
            defaultHandlerMap.put(MouseDragEvent.MOUSE_RELEASED, (event) -> endPhase());
        }
        return defaultHandlerMap;
    }

    protected final  Map<EventType, EventHandler> getCustomHandlerMap() {
        if(customHandlerMap == null){
            customHandlerMap = new HashMap<>();
        }
        if (customHandlerMap.isEmpty()) {
            this.initHandlerMap();
        }
        return customHandlerMap;
    }

    public void startPhase() {
        draggableHBox.setEffect(dropShadowEffect);
        getCustomHandlerMap()
                .entrySet()
                .forEach(entry -> planEditorTabPane.getScene().addEventFilter(entry.getKey(), entry.getValue()));
        defaultHandlers()
                .entrySet()
                .forEach(entry -> planEditorTabPane.getScene().addEventFilter(entry.getKey(), entry.getValue()));

        previousCursor = planEditorTabPane.getScene().getCursor();
        // TODO: should be done in the derived tool classes' start phase methods
        //planEditorTabPane.getScene().setCursor(new ImageCursor(new AlicaIcon("special elementType of abstract tool")));
    }

    public void endPhase() {
        draggableHBox.setEffect(null);
        getCustomHandlerMap()
                .entrySet()
                .forEach(entry -> getPlanEditorTabPane().getScene().removeEventFilter(entry.getKey(), entry.getValue()));
        defaultHandlers()
                .entrySet()
                .forEach(entry -> getPlanEditorTabPane().getScene().removeEventFilter(entry.getKey(), entry.getValue()));

        // TODO: fire event to signal successful termination of event
        //draw();
        planEditorTabPane.getScene().setCursor(previousCursor);
        setRecentlyDone(true);
    }

    public boolean isRecentlyDone() {
        return recentlyDone;
    }

    public void setRecentlyDone(boolean recentlyDone) {
        this.recentlyDone = recentlyDone;
    }

    public void setDraggableHBox(DraggableHBox draggableHBox){
        draggableHBox.setOnDragDetected(event -> {
            draggableHBox.startFullDrag();
            this.startPhase();
            event.consume();
        });

        this.draggableHBox = draggableHBox;
        this.draggableHBox.setOnDragDone(Event::consume);
    }
    public DraggableHBox getDraggableHBox() {
        return draggableHBox;
    }

    public PlanTab getPlanTab() {
        return planTab;
    }

    /**
     * Transform the coordinates contained in a {@link MouseDragEvent} into coordinates relative to the {@link StackPane}
     * that represents the editor.
     *
     * @param event  the {@link MouseDragEvent} containing the base coordinates
     * @return  the relative coordinates or null, if the drag was released outside of the editor
     */
    protected Point2D getLocalCoordinatesFromEvent(MouseDragEvent event){
        //If the events target is the editor, calculate the local coordinates
        if(event.getTarget() != null && isMouseDragEventOnValidTarget(event)){
            return planTab.getPlanEditorGroup().sceneToLocal(event.getX(), event.getY());
        }
        //Otherwise just return null
        return null;
    }

    private boolean isMouseDragEventOnValidTarget(MouseDragEvent event){
        //The target may be the StackPane itself
        return event.getTarget() == planTab.getPlanContent()
                //Or one of the children of its children
                || planTab.getPlanEditorGroup().getChildren()
                .stream().flatMap(container -> ((Pane) container).getChildren().stream())
                .anyMatch(x -> x == event.getTarget());
    }

    protected ViewModelElement getElementFromEvent(Event event){
        return planTab.getPlanEditorGroup().getChildren().stream()
                .filter(container -> ((Pane) container).getChildren().contains(event.getTarget()))
                .findFirst().map(node -> ((AbstractPlanElementContainer)node).getModelElement()).orElse(null);
    }
}
