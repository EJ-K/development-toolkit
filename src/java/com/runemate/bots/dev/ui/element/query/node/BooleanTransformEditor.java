package com.runemate.bots.dev.ui.element.query.node;

import com.runemate.bots.dev.ui.element.query.QueryBuilderExtension;
import com.runemate.bots.dev.ui.element.query.transform.QueryParameterTransform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;

public class BooleanTransformEditor extends HBox implements TransformEditor {

    private final QueryParameterTransform transform;
    private final QueryBuilderExtension extension;

    private CheckBox editor;

    public BooleanTransformEditor(QueryParameterTransform t, QueryBuilderExtension ext) {
        setSpacing(6);
        setAlignment(Pos.CENTER_LEFT);
        this.transform = t;
        this.extension = ext;
    }

    @Override
    public void build() {
        setFillHeight(true);
        editor = new CheckBox();

        Button add = new Button("+");
        Button remove = new Button("-");

        add.setOnAction(event -> {
            final Boolean text = editor.isSelected();
            transform.value(text);
            extension.invalidateQuery();
            editor.setSelected(false);
        });

        remove.setOnAction(event -> {
            transform.reset();
            extension.invalidateQuery();
            editor.setSelected(false);
        });

        getChildren().addAll(editor, add, remove);
    }

    public QueryParameterTransform getTransform() {
        return transform;
    }

    public QueryBuilderExtension getExtension() {
        return extension;
    }

}