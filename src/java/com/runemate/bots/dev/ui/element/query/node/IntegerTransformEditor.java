package com.runemate.bots.dev.ui.element.query.node;

import com.runemate.bots.dev.ui.element.query.QueryBuilderExtension;
import com.runemate.bots.dev.ui.element.query.transform.QueryParameterTransform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;

public class IntegerTransformEditor extends HBox implements TransformEditor {

    private final QueryParameterTransform transform;
    private final QueryBuilderExtension extension;

    private Spinner<Integer> editor;

    public IntegerTransformEditor(QueryParameterTransform t, QueryBuilderExtension ext) {
        setSpacing(6);
        setAlignment(Pos.CENTER_LEFT);
        this.transform = t;
        this.extension = ext;
    }

    @Override
    public void build() {
        setFillHeight(true);
        editor = new Spinner<>();
        editor.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(Integer.MIN_VALUE,
            Integer.MAX_VALUE
        ));
        editor.getValueFactory().setValue(0);
        editor.setEditable(true);
        editor.prefHeightProperty().bind(heightProperty());

        Button add = new Button("+");
        Button remove = new Button("-");

        add.setOnAction(event -> {
            final Integer text = editor.getValueFactory().getValue();
            if (text != null) {
                transform.value(text);
                extension.invalidateQuery();
                editor.getValueFactory().setValue(0);
            }
        });

        remove.setOnAction(event -> {
            transform.reset();
            extension.invalidateQuery();
            editor.getValueFactory().setValue(0);
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
