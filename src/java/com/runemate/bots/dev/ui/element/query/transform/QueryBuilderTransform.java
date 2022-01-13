package com.runemate.bots.dev.ui.element.query.transform;

import com.runemate.bots.dev.ui.element.query.QueryBuilderExtension;
import com.runemate.game.api.hybrid.queries.QueryBuilder;
import com.runemate.game.api.hybrid.queries.results.QueryResults;
import java.util.List;

public class QueryBuilderTransform {

    private final Class<?> type;
    private final List<QueryParameterTransform> transforms;

    public QueryBuilderTransform(final Class<?> type) {
        this.type = type;
        this.transforms = QueryParameterTransform.reflect(type);
    }

    public Class<?> getType() {
        return type;
    }

    public List<QueryParameterTransform> getTransforms() {
        System.out.println(transforms);
        return transforms;
    }

    public QueryResults execute(QueryBuilder builder) {
        for (QueryParameterTransform transform : transforms) {
            if (transform.hasValue()) {
                transform.apply(builder);
            }
        }
        return builder.results();
    }

    public String getQueryText() {
        final StringBuilder builder = new StringBuilder();
        builder.append(QueryBuilderExtension.NEWQUERY_TEXT.get(type)).append(".newQuery()");
        for (QueryParameterTransform transform : transforms) {
            builder.append(transform.getBuilderChainLink());
        }
        builder.append(".results()");
        return builder.toString();
    }

    @Override
    public String toString() {
        return type.getSimpleName();
    }
}
