package com.runemate.bots.dev.ui.element.query;

import com.runemate.bots.dev.ui.element.query.transform.QueryBuilderTransform;
import java.util.Comparator;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.ComboBox;

public class QueryBuilderComboBox extends ComboBox<QueryBuilderTransform> {

    public QueryBuilderComboBox() {
        setPromptText("Select type...");
        for (Class<?> type : QueryBuilderExtension.NEWQUERY_TEXT.keySet()) {
            getItems().add(new QueryBuilderTransform(type));
        }
        getItems().sort(Comparator.comparing((one) -> one.getType().getSimpleName()));
    }

    public ReadOnlyObjectProperty<QueryBuilderTransform> selectedBuilderProperty() {
        return getSelectionModel().selectedItemProperty();
    }
}
