package com.runemate.bots.dev.ui.element.query.node;

import com.runemate.bots.dev.ui.element.query.QueryBuilderExtension;
import com.runemate.bots.dev.ui.element.query.transform.QueryParameterTransform;
import java.util.regex.Pattern;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class PatternTransformEditor extends HBox implements TransformEditor {

    private final QueryParameterTransform transform;
    private final QueryBuilderExtension extension;

    private TextField editor;

    public PatternTransformEditor(QueryParameterTransform t, QueryBuilderExtension ext) {
        setSpacing(6);
        setAlignment(Pos.CENTER_LEFT);
        this.transform = t;
        this.extension = ext;
    }

    @Override
    public void build() {
        setFillHeight(true);
        editor = new TextField();
        editor.setPromptText("Enter pattern");
        editor.prefHeightProperty().bind(heightProperty());

        Button add = new Button("+");
        Button remove = new Button("-");

        add.setOnAction(event -> {
            final String text = editor.getText();
            if (text != null && !text.isEmpty()) {
                transform.value(Pattern.compile(text));
                extension.invalidateQuery();
                editor.setText(null);
            }
        });

        remove.setOnAction(event -> {
            transform.reset();
            extension.invalidateQuery();
            editor.setText(null);
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
