package com.runemate.bots.dev.ui.element.query;

import com.runemate.bots.dev.ui.element.query.node.BooleanTransformEditor;
import com.runemate.bots.dev.ui.element.query.node.EnumTransformEditor;
import com.runemate.bots.dev.ui.element.query.node.IntegerTransformEditor;
import com.runemate.bots.dev.ui.element.query.node.PatternTransformEditor;
import com.runemate.bots.dev.ui.element.query.node.StringTransformEditor;
import com.runemate.bots.dev.ui.element.query.node.TransformEditor;
import com.runemate.bots.dev.ui.element.query.transform.QueryBuilderTransform;
import java.util.regex.Pattern;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

public class QueryParameterContainer extends QueryParameterComboBox {

    private final QueryBuilderTransform transform;
    private final QueryBuilderExtension extension;

    public QueryParameterContainer(QueryBuilderTransform trans, QueryBuilderExtension ext) {
        super(trans, ext);
        this.transform = trans;
        this.extension = ext;
        build();
    }

    private void build() {
        selectedTransformProperty()
            .addListener(((observable, oldValue, newValue) -> {
                if (newValue == null) {
                    return;
                }
                final Class<?> type = newValue.getParameterType();
                final TransformEditor editor;
                if (type.equals(String.class)) {
                    editor = new StringTransformEditor(newValue, extension);
                } else if (type.equals(Pattern.class)) {
                    editor = new PatternTransformEditor(newValue, extension);
                } else if (type.equals(Integer.TYPE)) {
                    editor = new IntegerTransformEditor(newValue, extension);
                } else if (newValue.isEnum()) {
                    editor = new EnumTransformEditor(newValue, extension);
                } else if (type.equals(Boolean.TYPE)) {
                    editor = new BooleanTransformEditor(newValue, extension);
                } else {
                    editor = null;
                }
                extension.getFilterContent().getChildren().clear();
                if (editor != null) {
                    editor.build();
                    extension.getFilterContent().getChildren().add((Node) editor);
                }
            }));
    }

    public QueryBuilderTransform getTransform() {
        return transform;
    }

    public QueryBuilderExtension getExtension() {
        return extension;
    }

    private StringProperty queryTextProperty() {
        return getExtension().queryTextProperty();
    }
}
