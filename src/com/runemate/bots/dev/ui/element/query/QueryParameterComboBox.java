package com.runemate.bots.dev.ui.element.query;

import com.runemate.bots.dev.ui.element.query.transform.QueryBuilderTransform;
import com.runemate.bots.dev.ui.element.query.transform.QueryParameterTransform;
import java.util.Comparator;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.ComboBox;

public class QueryParameterComboBox extends ComboBox<QueryParameterTransform> {

    private final QueryBuilderTransform transform;
    private final QueryBuilderExtension extension;

    public QueryParameterComboBox(QueryBuilderTransform transform, QueryBuilderExtension extension) {
        setPromptText("Select filter...");
        this.transform = transform;
        this.extension = extension;
        build();
    }

    private void build() {
        getItems().addAll(transform.getTransforms());
        getItems().sort(Comparator.comparing(QueryParameterTransform::getName));
    }

    public ReadOnlyObjectProperty<QueryParameterTransform> selectedTransformProperty() {
        return getSelectionModel().selectedItemProperty();
    }

    public QueryBuilderExtension getExtension() {
        return extension;
    }

    public QueryBuilderTransform getTransform() {
        return transform;
    }
}
