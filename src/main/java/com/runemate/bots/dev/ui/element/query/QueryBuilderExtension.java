package com.runemate.bots.dev.ui.element.query;

import static com.runemate.bots.dev.ui.DevelopmentToolkitPage.*;

import com.runemate.bots.dev.*;
import com.runemate.bots.dev.ui.*;
import com.runemate.bots.dev.ui.element.query.transform.*;
import com.runemate.bots.util.*;
import com.runemate.game.api.hybrid.entities.*;
import com.runemate.game.api.hybrid.local.hud.interfaces.*;
import com.runemate.game.api.hybrid.queries.*;
import com.runemate.game.api.hybrid.queries.results.*;
import com.runemate.game.api.hybrid.region.*;
import com.runemate.game.api.hybrid.util.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import java.util.regex.*;
import java.util.stream.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.util.*;

public class QueryBuilderExtension extends VBox implements Initializable {

    public static final List<Type>
        SUPPORTED_PARAMETER_TYPES =
        Arrays.asList(String.class, Pattern.class, int.class, boolean.class, GameObject.Type.class, InterfaceComponent.Type.class);
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

    private final ObjectProperty<QueryBuilderTransform> transformProperty = new SimpleObjectProperty<>();
    private final DevelopmentToolkit bot;
    private QueryBuilderComboBox qbSelector;
    @FXML
    private TextField queryText;
    @FXML
    private TreeTableColumn<Pair<Method, Object>, String> queryCommentTreeTableColumn, queryValueTreeTableColumn;
    @FXML
    private TreeTableColumn<Pair<Method, Object>, Node> queryObjectTreeTableColumn;

    @FXML
    private HBox filterContent, queryContainer;

    @FXML
    private TreeTableView<Pair<Method, Object>> queryTreeTableView;

    public QueryBuilderExtension(final DevelopmentToolkit bot) throws IOException {
        this.bot = bot;
        InputStream fxmlInputStream = Resources.getAsStream(bot, "fxml/QueryBuilderPage.fxml");
        final FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        loader.setRoot(this);
        loader.load(fxmlInputStream);
        try {
            final URL url = bot.getPlatform().invokeAndWait(() -> Resources.getAsURL("css/DevelopmentToolkitPage.css"));
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
        qbSelector.selectedBuilderProperty().addListener((
            (observable, oldValue, newValue) -> {
                invalidateQuery();
                if (newValue == null) {
                    return;
                }
                final QueryParameterContainer container = new QueryParameterContainer(newValue, this);
                queryContainer.getChildren().setAll(qbSelector, container);
            }
        ));


        final Callback<TreeTableColumn.CellDataFeatures<Pair<Method, Object>, Node>, ObservableValue<Node>> objectCallback = param -> {
            Pair<Method, Object> vv = param.getValue().getValue();
            if (vv.getKey() == null) {
                if (vv.getValue() == null) {
                    return DevelopmentToolkitPage.NULL_NODE_VALUE;
                }
                if (Map.Entry.class.isAssignableFrom(vv.getValue().getClass())) {
                    return new SimpleObjectProperty<>(new Text(optionallyThreadedCall(() -> cleanToString(((Map.Entry<?, ?>) vv.getValue()).getKey()))));
                }
                if (vv.getValue() instanceof Class) {
                    return new SimpleObjectProperty<>(new Text(((Class) vv.getValue()).getSimpleName()));
                }
                return new SimpleObjectProperty<>(new Text(optionallyThreadedCall(() -> cleanToString(vv.getValue()))));
            }
            final Text typeText = new Text(vv.getKey().getReturnType().getSimpleName());
            typeText.getStyleClass().addAll("type", "type-" + (
                ClassUtil.isPrimitiveOrWrapper(vv.getKey().getReturnType())
                    ? "primitive"
                    : Integer.toString(vv.getKey().getReturnType().getSimpleName().charAt(0) % 16)
            ));
            final Text spaceText = new Text(" ");
            final Text nameText = new Text(vv.getKey().getName());
            // tried TextFlow but couldn't use it due to internal bug with TableViews and TextFlows :(
            final HBox hbox = new HBox(typeText, spaceText, nameText);
            hbox.setAlignment(Pos.CENTER_LEFT);
            return new SimpleObjectProperty<>(hbox);
            //return new SimpleObjectProperty<>(new Text(vv.getKey().getReturnType().getSimpleName() + " " + vv.getKey().getName()));
        };
        final Callback<TreeTableColumn.CellDataFeatures<Pair<Method, Object>, String>, ObservableValue<String>> valueCallback = param -> {
            Pair<Method, Object> vv = param.getValue().getValue();
            if (vv.getKey() == null) {
                if (vv.getValue() != null && Map.Entry.class.isAssignableFrom(vv.getValue().getClass())) {
                    return new SimpleStringProperty(optionallyThreadedCall(() -> cleanToString(((Map.Entry<?, ?>) vv.getValue()).getValue())));
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
            return new SimpleStringProperty(optionallyThreadedCall(() -> cleanToString(vv.getValue())));
        };
        final Callback<TreeTableColumn.CellDataFeatures<Pair<Method, Object>, String>, ObservableValue<String>> commentCallback = param -> {
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
                return res.stream().map(i -> new ReflectiveTreeItem(null, i)).collect(Collectors.toList());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return Collections.emptyList();
        }
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        invalidateQuery();
    }
}
