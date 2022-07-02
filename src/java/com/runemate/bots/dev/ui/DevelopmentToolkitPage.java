package com.runemate.bots.dev.ui;

import com.runemate.bots.dev.*;
import com.runemate.bots.dev.ui.element.query.*;
import com.runemate.bots.ui.util.*;
import com.runemate.bots.util.*;
import com.runemate.game.api.hybrid.util.*;
import com.runemate.ui.control.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.*;
import javafx.beans.property.*;
import javafx.beans.value.*;
import javafx.fxml.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.*;

public class DevelopmentToolkitPage extends VBox implements Initializable {

    public static final Map<Class<?>, Function<Object, String>> OVERRIDDEN_TO_STRINGS = new HashMap<>(5);
    public static final ObservableValue<String> NULL_STRING_PROPERTY = new SimpleStringProperty("null"),
        EMPTY_STRING_PROPERTY =
            new SimpleStringProperty("empty");
    public static final ObservableValue<Node> NULL_NODE_VALUE = new SimpleObjectProperty<>(new Text("null"));
    private final DevelopmentToolkit bot;
    @FXML
    private VBox entitiesTableViewContainer, eventsTableViewContainer, miscTableViewContainer, databaseTableViewContainer,
        toggleSwitchContainer;
    @FXML
    private TreeTableView<Pair<Method, Object>> entitiesTreeTableView, eventsTreeTableView, miscTreeTableView, databaseTreeTableView;
    @FXML
    private TreeTableColumn<Pair<Method, Object>, String> entityValueTreeTableColumn, entityCommentTreeTableColumn,
        eventValueTreeTableColumn, eventCommentTreeTableColumn, miscValueTreeTableColumn, miscCommentTreeTableColumn,
        databaseValueTreeTableColumn, databaseCommentTreeTableColumn;
    @FXML
    private TreeTableColumn<Pair<Method, Object>, Node> entityObjectTreeTableColumn, eventObjectTreeTableColumn, miscObjectTreeTableColumn,
        databaseObjectTreeTableColumn;
    @FXML
    private TitledPane entitiesTitledPane, eventsTitledPane, miscTitledPane, databaseTitledPane;
    @FXML
    private TextField entitiesSearchTextField, databaseSearchTextField;
    @FXML
    private CheckBox entitiesSearchRegexCheckBox, databaseSearchRegexCheckBox;
    @FXML
    private Button qbButton;

    private ToggleSwitch hoverSwitch, overlaySwitch;

    private QueryBuilderExtension queryBuilderExtension;

    public DevelopmentToolkitPage(DevelopmentToolkit bot) throws IOException {
        InputStream fxmlInputStream = Resources.getAsStream("fxml/DevelopmentToolkitPage.fxml");
        this.bot = bot;
        final FXMLLoader loader = new FXMLLoader();
        loader.setController(this);
        loader.setRoot(this);
        loader.load(fxmlInputStream);
        getStylesheets().add(Resources.getAsURL("css/DevelopmentToolkitPage.css").toExternalForm());
    }

    public static <T> T optionallyThreadedCall(Callable<T> callable) {
        ExecutorService es = QueriableTreeItem.getExecutorService();
        if (es != null && !es.isShutdown()) {
            try {
                return es.submit(callable).get();
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Point D Thread: " + Thread.currentThread().getName());
                e.printStackTrace();
                return null;
            }
        } else {
            try {
                return callable.call();
            } catch (Exception e) {
                System.err.println("Point F Thread: " + Thread.currentThread().getName());
                e.printStackTrace();
                return null;
            }
        }
    }

    public static String cleanToString(Object o) {
        return o != null ? DevelopmentToolkitPage.OVERRIDDEN_TO_STRINGS.getOrDefault(o.getClass(), Object::toString).apply(o) : null;
    }

    public ReadOnlyBooleanProperty hoverMouseOverProperty() {
        return hoverSwitch == null ? new SimpleBooleanProperty(false) : hoverSwitch.selectedProperty();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {


        qbButton.setOnAction(event -> {
            try {
                queryBuilderExtension = new QueryBuilderExtension(bot);
                final Stage stage = new Stage(StageStyle.DECORATED);
                stage.setScene(new Scene(queryBuilderExtension, 800, 400));
                stage.setTitle("Query Builder Kit");
                stage.setAlwaysOnTop(true);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        hoverSwitch = new ToggleSwitch(false);
        final Label hoverLabel = new Label("Enable Mouseover Highlight", hoverSwitch);
        hoverLabel.setContentDisplay(ContentDisplay.LEFT);
        toggleSwitchContainer.getChildren().addAll(hoverLabel);

        overlaySwitch = new ToggleSwitch(false);
        final Label overlayLabel = new Label("Enable Overlay", overlaySwitch);
        overlayLabel.setContentDisplay(ContentDisplay.LEFT);
        toggleSwitchContainer.getChildren().addAll(overlayLabel);

        overlaySwitch.selectedProperty().bindBidirectional(bot.getOverlay().showingProperty());
        overlaySwitch.setSelected(true);

        final Callback<TreeTableColumn.CellDataFeatures<Pair<Method, Object>, Node>, ObservableValue<Node>> objectCallback = param -> {
            Pair<Method, Object> vv = param.getValue().getValue();
            if (vv.getKey() == null) {
                if (vv.getValue() == null) {
                    return DevelopmentToolkitPage.NULL_NODE_VALUE;
                }
                if (Map.Entry.class.isAssignableFrom(vv.getValue().getClass())) {
                    return new SimpleObjectProperty<>(new Text(DevelopmentToolkitPage.optionallyThreadedCall(() -> DevelopmentToolkitPage.cleanToString(
                        ((Map.Entry<?, ?>) vv.getValue()).getKey()))));
                }
                if (vv.getValue() instanceof Class) {
                    return new SimpleObjectProperty<>(new Text(((Class) vv.getValue()).getSimpleName()));
                }
                return new SimpleObjectProperty<>(new Text(DevelopmentToolkitPage.optionallyThreadedCall(() -> DevelopmentToolkitPage.cleanToString(
                    vv.getValue()))));
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
                    return new SimpleStringProperty(DevelopmentToolkitPage.optionallyThreadedCall(() -> DevelopmentToolkitPage.cleanToString(
                        ((Map.Entry<?, ?>) vv.getValue()).getValue())));
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
            return new SimpleStringProperty(DevelopmentToolkitPage.optionallyThreadedCall(() -> DevelopmentToolkitPage.cleanToString(vv.getValue())));
        };
        final Callback<TreeTableColumn.CellDataFeatures<Pair<Method, Object>, String>, ObservableValue<String>> commentCallback = param -> {
            Object object = param.getValue().getValue().getValue();
            if (object == null) {
                return null;
            }
            //TODO display distance or something
            return new SimpleStringProperty("");
        };

        // ENTITIES

        VerticalDragResizerUtil.makeResizable(entitiesTableViewContainer, 6);
        entitiesTableViewContainer.maxHeightProperty().bind(entitiesTableViewContainer.minHeightProperty());
        entitiesTreeTableView.setRoot(new TreeItem<>(new Pair<>(null, null)));
        entitiesTreeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        entityObjectTreeTableColumn.setCellValueFactory(objectCallback);
        entityValueTreeTableColumn.setCellValueFactory(valueCallback);
        entityCommentTreeTableColumn.setCellValueFactory(commentCallback);

        entitiesTitledPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && entitiesTreeTableView.getRoot() != null) {
                entitiesTreeTableView.getRoot().getChildren().forEach(ti -> ti.setExpanded(false));
            }
        });

        // EVENTS

        VerticalDragResizerUtil.makeResizable(eventsTableViewContainer, 6);
        eventsTableViewContainer.maxHeightProperty().bindBidirectional(eventsTableViewContainer.minHeightProperty());
        eventsTreeTableView.setRoot(new TreeItem<>(new Pair<>(null, null)));

        eventObjectTreeTableColumn.setCellValueFactory(objectCallback);
        eventValueTreeTableColumn.setCellValueFactory(valueCallback);
        eventCommentTreeTableColumn.setCellValueFactory(commentCallback);

        eventsTitledPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && eventsTreeTableView.getRoot() != null) {
                eventsTreeTableView.getRoot().getChildren().forEach(ti -> ti.getChildren().clear());
            }
        });

        // MISC

        VerticalDragResizerUtil.makeResizable(miscTableViewContainer, 6);
        miscTableViewContainer.maxHeightProperty().bindBidirectional(miscTableViewContainer.minHeightProperty());
        miscTreeTableView.setRoot(new TreeItem<>(new Pair<>(null, null)));

        miscObjectTreeTableColumn.setCellValueFactory(objectCallback);
        miscValueTreeTableColumn.setCellValueFactory(valueCallback);
        miscCommentTreeTableColumn.setCellValueFactory(commentCallback);
        miscTreeTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        miscTitledPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && miscTreeTableView.getRoot() != null) {
                miscTreeTableView.getRoot().getChildren().forEach(ti -> ti.setExpanded(false));
            }
        });

        // DATABASE

        VerticalDragResizerUtil.makeResizable(databaseTableViewContainer, 6);
        databaseTableViewContainer.maxHeightProperty().bindBidirectional(databaseTableViewContainer.minHeightProperty());
        databaseTreeTableView.setRoot(new TreeItem<>(new Pair<>(null, null)));

        databaseObjectTreeTableColumn.setCellValueFactory(objectCallback);
        databaseValueTreeTableColumn.setCellValueFactory(valueCallback);
        databaseCommentTreeTableColumn.setCellValueFactory(commentCallback);

        databaseTitledPane.expandedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && databaseTreeTableView.getRoot() != null) {
                databaseTreeTableView.getRoot().getChildren().forEach(ti -> ti.setExpanded(false));
            }
        });
    }

    public TreeTableView<Pair<Method, Object>> getEntitiesTreeTableView() {
        return entitiesTreeTableView;
    }

    public TreeTableView<Pair<Method, Object>> getEventsTreeTableView() {
        return eventsTreeTableView;
    }

    public TreeTableView<Pair<Method, Object>> getMiscTreeTableView() {
        return miscTreeTableView;
    }

    public TreeTableView<Pair<Method, Object>> getDatabaseTreeTableView() {
        return databaseTreeTableView;
    }

    public TitledPane getEventsTitledPane() {
        return eventsTitledPane;
    }

    public TextField getEntitiesSearchTextField() {
        return entitiesSearchTextField;
    }

    public TextField getDatabaseSearchTextField() {
        return databaseSearchTextField;
    }

    public CheckBox getEntitiesSearchRegexCheckBox() {
        return entitiesSearchRegexCheckBox;
    }

    public CheckBox getDatabaseSearchRegexCheckBox() {
        return databaseSearchRegexCheckBox;
    }

    public TreeTableView<Pair<Method, Object>> getQueryTreeView() {
        final QueryBuilderExtension extension = this.queryBuilderExtension;
        if (extension != null) {
            return extension.getQueryTreeTableView();
        }
        return null;
    }
}
