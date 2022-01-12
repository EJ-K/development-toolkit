package com.runemate.bots.dev.ui.element.query.node;

import com.runemate.bots.dev.ui.element.query.QueryBuilderExtension;
import com.runemate.bots.dev.ui.element.query.transform.QueryParameterTransform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

public class EnumTransformEditor extends HBox implements TransformEditor {

    private final QueryParameterTransform transform;
    private final QueryBuilderExtension extension;

    private ComboBox<Enum<?>> editor;

    public EnumTransformEditor(QueryParameterTransform t, QueryBuilderExtension ext) {
        setSpacing(6);
        setAlignment(Pos.CENTER_LEFT);
        this.transform = t;
        this.extension = ext;
    }

    @Override
    public void build() {
        setFillHeight(true);
        editor = new ComboBox<>();
        editor.setPromptText("Select value");
        editor.setConverter(new StringConverter<Enum<?>>() {
            @Override
            public String toString(final Enum<?> object) {
                return object == null ? "null" : object.name();
            }

            @Override
            public Enum<?> fromString(final String string) {
                return null;
            }
        });

        editor.setItems(FXCollections.observableArrayList(
            (Enum<?>[]) transform.getParameterType().getEnumConstants()
        ));
        editor.prefHeightProperty().bind(heightProperty());

        Button add = new Button("+");
        Button remove = new Button("-");

        add.setOnAction(event -> {
            final Enum<?> text = editor.getSelectionModel().getSelectedItem();
            if (text != null) {
                transform.value(text);
                extension.invalidateQuery();
                editor.getSelectionModel().select(null);
            }
        });

        remove.setOnAction(event -> {
            transform.reset();
            extension.invalidateQuery();
            editor.getSelectionModel().select(null);
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
