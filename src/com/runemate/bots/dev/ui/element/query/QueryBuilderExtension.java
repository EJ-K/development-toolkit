package com.runemate.bots.dev.ui.element.query;

import static com.runemate.bots.dev.ui.DevelopmentToolkitPage.cleanToString;
import static com.runemate.bots.dev.ui.DevelopmentToolkitPage.optionallyThreadedCall;

import com.runemate.bots.dev.DevelopmentToolkit;
import com.runemate.bots.dev.ui.DevelopmentToolkitPage;
import com.runemate.bots.dev.ui.QueriableTreeItem;
import com.runemate.bots.dev.ui.ReflectiveTreeItem;
import com.runemate.bots.dev.ui.element.query.transform.QueryBuilderTransform;
import com.runemate.bots.util.ClassUtil;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceComponent;
import com.runemate.game.api.hybrid.local.hud.interfaces.Interfaces;
import com.runemate.game.api.hybrid.queries.GameObjectQueryBuilder;
import com.runemate.game.api.hybrid.queries.GroundItemQueryBuilder;
import com.runemate.game.api.hybrid.queries.InterfaceComponentQueryBuilder;
import com.runemate.game.api.hybrid.queries.NpcQueryBuilder;
import com.runemate.game.api.hybrid.queries.PlayerQueryBuilder;
import com.runemate.game.api.hybrid.queries.ProjectileQueryBuilder;
import com.runemate.game.api.hybrid.queries.QueryBuilder;
import com.runemate.game.api.hybrid.queries.results.LocatableEntityQueryResults;
import com.runemate.game.api.hybrid.queries.results.QueryResults;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.region.GroundItems;
import com.runemate.game.api.hybrid.region.Npcs;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.hybrid.region.Projectiles;
import com.runemate.game.api.hybrid.util.Resources;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.Pair;

public class QueryBuilderExtension extends VBox implements Initializable {

    public static final List<Type> SUPPORTED_PARAMETER_TYPES = Arrays.asList(
        String.class,
        Pattern.class,
        int.class,
        boolean.class,
        GameObject.Type.class,
        InterfaceComponent.Type.class
    );
    public static final Map<Class<?>, String> NEWQUERY_TEXT = new HashMap<>();
    public static final Map<Class<?>, Supplier<QueryBuilder>> NEWQUERY_SUPPLIERS = new HashMap<>();

    static {
        NEWQUERY_TEXT.put(GameObjectQueryBuilder.class, "GameObjects");
        NEWQUERY_TEXT.put(NpcQueryBuilder.class, "Npcs");
        NEWQUERY_TEXT.put(PlayerQueryBuilder.class, "Players");
        NEWQUERY_TEXT.put(ProjectileQueryBuilder.class, "Projectiles");
        NEWQUERY_TEXT.put(GroundItemQueryBuilder.class, "GroundItems");
        NEWQUERY_TEXT.put(InterfaceComponentQueryBuilder.class, "Interfaces");

        NEWQUERY_SUPPLIERS.put(GameObjectQueryBuilder.class, GameObjects::newQuery);
        NEWQUERY_SUPPLIERS.put(NpcQueryBuilder.class, Npcs::newQuery);
        NEWQUERY_SUPPLIERS.put(PlayerQueryBuilder.class, Players::newQuery);
        NEWQUERY_SUPPLIERS.put(ProjectileQueryBuilder.class, Projectiles::newQuery);
        NEWQUERY_SUPPLIERS.put(GroundItemQueryBuilder.class, GroundItems::newQuery);
        NEWQUERY_SUPPLIERS.put(InterfaceComponentQueryBuilder.class, Interfaces::newQuery);
    }

    private final ObjectProperty<QueryBuilderTransform> transformProperty =
        new SimpleObjectProperty<>();
    private final DevelopmentToolkit bot;
    private QueryBuilderComboBox qbSelector;
    @FXML
    private TextField queryText;
    @FXML
    private TreeTableColumn<Pair<Method, Object>, String> queryCommentTreeTableColumn,
        queryValueTreeTableColumn;
    @FXML
    private TreeTableColumn<Pair<Method, Object>, Node> queryObjectTreeTableColumn;

    @FXML
    private HBox filterContent, queryContainer;

    @FXML
    private TreeTableView<Pair<Method, Object>> queryTreeTableView;

    public QueryBuilderExtension(final DevelopmentToolkit bot) throws IOException {
        this.bot = bot;
        InputStream fxmlInputStream = Resources.getAsStream(
            bot,
            "com/runemate/bots/dev/ui/element/query/QueryBuilderPage.fxml"
        );
        final FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        loader.setRoot(this);
        loader.load(fxmlInputStream);
        try {
            final URL url = bot.getPlatform().invokeAndWait(
                () -> Resources.getAsURL("com/runemate/bots/dev/ui/DevelopmentToolkitPage.css"));
            getStylesheets().add(url.toExternalForm());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {

        //Add the dropdown to choose what type of QueryBuilder
        qbSelector = new QueryBuilderComboBox();
        queryText.setFont(Font.font("Consolas", 12));

        queryContainer.getChildren().add(qbSelector);

        //transformProperty will always be synced with the dropdown
        transformProperty.bind(qbSelector.selectedBuilderProperty());

        //When it changes we want to build a new query
        qbSelector.selectedBuilderProperty().addListener(((observable, oldValue, newValue) -> {
            invalidateQuery();
            if (newValue == null) {
                return;
            }
            final QueryParameterContainer container = new QueryParameterContainer(newValue, this);
            queryContainer.getChildren().setAll(qbSelector, container);
        }));


        final Callback<TreeTableColumn.CellDataFeatures<Pair<Method, Object>, Node>, ObservableValue<Node>>
            objectCallback = param -> {
            Pair<Method, Object> vv = param.getValue().getValue();
            if (vv.getKey() == null) {
                if (vv.getValue() == null) {
                    return DevelopmentToolkitPage.NULL_NODE_VALUE;
                }
                if (Map.Entry.class.isAssignableFrom(vv.getValue().getClass())) {
                    return new SimpleObjectProperty<>(new Text(optionallyThreadedCall(
                        () -> cleanToString(((Map.Entry<?, ?>) vv.getValue()).getKey()))));
                }
                if (vv.getValue() instanceof Class) {
                    return new SimpleObjectProperty<>(
                        new Text(((Class) vv.getValue()).getSimpleName()));
                }
                return new SimpleObjectProperty<>(
                    new Text(optionallyThreadedCall(() -> cleanToString(vv.getValue()))));
            }
            final Text typeText = new Text(vv.getKey().getReturnType().getSimpleName());
            typeText.getStyleClass().addAll("type", "type-" + (
                ClassUtil.isPrimitiveOrWrapper(vv.getKey().getReturnType()) ? "primitive" :
                    Integer.toString(vv.getKey().getReturnType().getSimpleName().charAt(0) % 16)));
            final Text spaceText = new Text(" ");
            final Text nameText = new Text(vv.getKey().getName());
            // tried TextFlow but couldn't use it due to internal bug with TableViews and TextFlows :(
            final HBox hbox = new HBox(typeText, spaceText, nameText);
            hbox.setAlignment(Pos.CENTER_LEFT);
            return new SimpleObjectProperty<>(hbox);
            //return new SimpleObjectProperty<>(new Text(vv.getKey().getReturnType().getSimpleName() + " " + vv.getKey().getName()));
        };
        final Callback<TreeTableColumn.CellDataFeatures<Pair<Method, Object>, String>, ObservableValue<String>>
            valueCallback = param -> {
            Pair<Method, Object> vv = param.getValue().getValue();
            if (vv.getKey() == null) {
                if (vv.getValue() != null && Map.Entry.class.isAssignableFrom(
                    vv.getValue().getClass())) {
                    return new SimpleStringProperty(optionallyThreadedCall(
                        () -> cleanToString(((Map.Entry<?, ?>) vv.getValue()).getValue())));
                }
                return null;
            }
            if (vv.getValue() == null) {
                return DevelopmentToolkitPage.NULL_STRING_PROPERTY;
            }
            if (Iterable.class.isAssignableFrom(vv.getValue().getClass())) {
                if (!((Iterable) vv.getValue()).iterator().hasNext()) {
                    return DevelopmentToolkitPage.EMPTY_STRING_PROPERTY;
                } else if (Collection.class.isAssignableFrom(vv.getValue().getClass())) {
                    return new SimpleStringProperty("size: " + ((Collection) vv.getValue()).size());
                }
                return null;
            }
            if (vv.getValue().getClass().isArray()) {
                if (Array.getLength(vv.getValue()) == 0) {
                    return DevelopmentToolkitPage.EMPTY_STRING_PROPERTY;
                }
                return null;
            }
            if (Map.class.isAssignableFrom(vv.getValue().getClass())) {
                if (((Map) vv.getValue()).isEmpty()) {
                    return DevelopmentToolkitPage.EMPTY_STRING_PROPERTY;
                }
                return null;
            }
            return new SimpleStringProperty(
                optionallyThreadedCall(() -> cleanToString(vv.getValue())));
        };
        final Callback<TreeTableColumn.CellDataFeatures<Pair<Method, Object>, String>, ObservableValue<String>>
            commentCallback = param -> {
            Object object = param.getValue().getValue().getValue();
            if (object == null) {
                return null;
            }
            //TODO display distance or something
            return new SimpleStringProperty("");
        };

        queryTreeTableView.prefWidthProperty().bind(widthProperty().subtract(20));
        queryTreeTableView.prefHeight(400);
        queryTreeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        queryObjectTreeTableColumn.setCellValueFactory(objectCallback);
        queryValueTreeTableColumn.setCellValueFactory(valueCallback);
        queryCommentTreeTableColumn.setCellValueFactory(commentCallback);

        queryTreeTableView.setRoot(new PseudoRootItem("Results", transformProperty));
    }

    public HBox getFilterContent() {
        return filterContent;
    }

    public void invalidateQuery() {
        queryTextProperty().set(null);
        queryTreeTableView.setRoot(null);

        final QueryBuilderTransform transform = transformProperty.get();
        if (transform == null) {
            return;
        }

        queryTreeTableView.setRoot(new PseudoRootItem("Results", transformProperty));
        queryTextProperty().set(transform.getQueryText());
    }

    public TreeTableView<Pair<Method, Object>> getQueryTreeTableView() {
        return queryTreeTableView;
    }

    public StringProperty queryTextProperty() {
        return queryText.textProperty();
    }

    private static class PseudoRootItem extends QueriableTreeItem<Pair<Method, Object>> {

        private final ObjectProperty<QueryBuilderTransform> transform;

        public PseudoRootItem(String name, ObjectProperty<QueryBuilderTransform> transform) {
            super(new Pair<>(null, name));
            this.transform = transform;
        }

        @Override
        public List<TreeItem<Pair<Method, Object>>> query() {
            final QueryBuilderTransform t = transform.get();
            if (t == null) {
                return Collections.emptyList();
            }
            try {
                final Class<?> builderType = t.getType();
                final QueryBuilder<?, ?, ?> builder = NEWQUERY_SUPPLIERS.get(builderType).get();

                long time = System.currentTimeMillis();
                final Collection<?> res = optionallyThreadedCall(() -> {
                    final QueryResults<?, ?> qr = t.execute(builder);
                    if (qr instanceof LocatableEntityQueryResults) {
                        return ((LocatableEntityQueryResults<?>) qr).sortByDistance();
                    }
                    return qr;
                });
                time = System.currentTimeMillis() - time;

                if (!this.isLeaf()) {
                    this.getChildren().clear();
                }

                if (res == null) {
                    return Collections.emptyList();
                }

                this.setValue(new Pair<>(null, "Queried in " + time + "ms"));
                return res.stream()
                    .map(i -> new ReflectiveTreeItem(null, i))
                    .collect(Collectors.toList());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }
    }
}
