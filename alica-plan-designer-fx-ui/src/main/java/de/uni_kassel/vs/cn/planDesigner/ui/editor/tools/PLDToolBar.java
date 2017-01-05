package de.uni_kassel.vs.cn.planDesigner.ui.editor.tools;

import javafx.geometry.Orientation;
import javafx.scene.control.ToolBar;

/**
 * Created by marci on 23.11.16.
 */
public class PLDToolBar extends ToolBar {
    public PLDToolBar() {
        super();
        setOrientation(Orientation.VERTICAL);
        getItems().add(new StateTool().createToolUI());
        /*getItems().addAll(
                Stream.of(
                        DragableAlicaType.values())
                        .map(DragableHBox::new)
                        .collect(Collectors.toList())
        );*/
    }
}