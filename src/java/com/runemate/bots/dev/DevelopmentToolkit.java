package com.runemate.bots.dev;

import com.runemate.bots.dev.ui.DevelopmentToolkitPage;
import com.runemate.bots.dev.ui.QueriableTreeItem;
import com.runemate.bots.dev.ui.ReflectiveTreeItem;
import com.runemate.bots.dev.ui.element.adapter.VarbitEventWrapper;
import com.runemate.bots.dev.ui.overlay.DevelopmentToolkitOverlay;
import com.runemate.game.api.client.embeddable.EmbeddableUI;
import com.runemate.game.api.hybrid.Environment;
import com.runemate.game.api.hybrid.GameEvents;
import com.runemate.game.api.hybrid.RuneScape;
import com.runemate.game.api.hybrid.cache.configs.EnumDefinitions;
import com.runemate.game.api.hybrid.cache.configs.IdentityKits;
import com.runemate.game.api.hybrid.cache.configs.OverlayDefinitions;
import com.runemate.game.api.hybrid.cache.configs.SpotAnimationDefinitions;
import com.runemate.game.api.hybrid.cache.configs.UnderlayDefinitions;
import com.runemate.game.api.hybrid.cache.materials.Materials;
import com.runemate.game.api.hybrid.entities.Actor;
import com.runemate.game.api.hybrid.entities.Entity;
import com.runemate.game.api.hybrid.entities.GameObject;
import com.runemate.game.api.hybrid.entities.LocatableEntity;
import com.runemate.game.api.hybrid.entities.Npc;
import com.runemate.game.api.hybrid.entities.Player;
import com.runemate.game.api.hybrid.entities.definitions.GameObjectDefinition;
import com.runemate.game.api.hybrid.entities.definitions.ItemDefinition;
import com.runemate.game.api.hybrid.entities.definitions.NpcDefinition;
import com.runemate.game.api.hybrid.entities.details.Interactable;
import com.runemate.game.api.hybrid.entities.details.Renderable;
import com.runemate.game.api.hybrid.entities.details.Rotatable;
import com.runemate.game.api.hybrid.input.Keyboard;
import com.runemate.game.api.hybrid.input.Mouse;
import com.runemate.game.api.hybrid.local.AccountInfo;
import com.runemate.game.api.hybrid.local.Camera;
import com.runemate.game.api.hybrid.local.House;
import com.runemate.game.api.hybrid.local.Quests;
import com.runemate.game.api.hybrid.local.Screen;
import com.runemate.game.api.hybrid.local.Skill;
import com.runemate.game.api.hybrid.local.Skills;
import com.runemate.game.api.hybrid.local.Varbits;
import com.runemate.game.api.hybrid.local.Varps;
import com.runemate.game.api.hybrid.local.Wilderness;
import com.runemate.game.api.hybrid.local.Worlds;
import com.runemate.game.api.hybrid.local.hud.GraphicsConfiguration;
import com.runemate.game.api.hybrid.local.hud.HintArrows;
import com.runemate.game.api.hybrid.local.hud.Menu;
import com.runemate.game.api.hybrid.local.hud.Model;
import com.runemate.game.api.hybrid.local.hud.interfaces.Bank;
import com.runemate.game.api.hybrid.local.hud.interfaces.ChatDialog;
import com.runemate.game.api.hybrid.local.hud.interfaces.Chatbox;
import com.runemate.game.api.hybrid.local.hud.interfaces.DepositBox;
import com.runemate.game.api.hybrid.local.hud.interfaces.EnterAmountDialog;
import com.runemate.game.api.hybrid.local.hud.interfaces.Equipment;
import com.runemate.game.api.hybrid.local.hud.interfaces.Health;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceComponent;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceContainer;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceContainers;
import com.runemate.game.api.hybrid.local.hud.interfaces.InterfaceWindows;
import com.runemate.game.api.hybrid.local.hud.interfaces.Interfaces;
import com.runemate.game.api.hybrid.local.hud.interfaces.Inventory;
import com.runemate.game.api.hybrid.local.hud.interfaces.NpcContact;
import com.runemate.game.api.hybrid.local.hud.interfaces.Shop;
import com.runemate.game.api.hybrid.local.hud.interfaces.Trade;
import com.runemate.game.api.hybrid.local.hud.interfaces.WorldHop;
import com.runemate.game.api.hybrid.local.sound.SoundEffects;
import com.runemate.game.api.hybrid.location.Area;
import com.runemate.game.api.hybrid.location.Coordinate;
import com.runemate.game.api.hybrid.location.navigation.Traversal;
import com.runemate.game.api.hybrid.net.GrandExchange;
import com.runemate.game.api.hybrid.region.GameObjects;
import com.runemate.game.api.hybrid.region.GroundItems;
import com.runemate.game.api.hybrid.region.Npcs;
import com.runemate.game.api.hybrid.region.Players;
import com.runemate.game.api.hybrid.region.Projectiles;
import com.runemate.game.api.hybrid.region.Region;
import com.runemate.game.api.hybrid.region.SpotAnimations;
import com.runemate.game.api.hybrid.util.Validatable;
import com.runemate.game.api.osrs.local.AchievementDiary;
import com.runemate.game.api.osrs.local.KourendHouseFavour;
import com.runemate.game.api.osrs.local.hud.interfaces.ControlPanelTab;
import com.runemate.game.api.osrs.local.hud.interfaces.LootingBag;
import com.runemate.game.api.osrs.local.hud.interfaces.Magic;
import com.runemate.game.api.osrs.local.hud.interfaces.MakeAllInterface;
import com.runemate.game.api.osrs.local.hud.interfaces.OptionsTab;
import com.runemate.game.api.osrs.local.hud.interfaces.Prayer;
import com.runemate.game.api.rs3.local.CombatMode;
import com.runemate.game.api.rs3.local.InterfaceMode;
import com.runemate.game.api.rs3.local.hud.Powers;
import com.runemate.game.api.rs3.local.hud.interfaces.BeastOfBurden;
import com.runemate.game.api.rs3.local.hud.interfaces.Lodestone;
import com.runemate.game.api.rs3.local.hud.interfaces.LootInventory;
import com.runemate.game.api.rs3.local.hud.interfaces.MakeXInterface;
import com.runemate.game.api.rs3.local.hud.interfaces.MoneyPouch;
import com.runemate.game.api.rs3.local.hud.interfaces.Summoning;
import com.runemate.game.api.rs3.local.hud.interfaces.eoc.ActionBar;
import com.runemate.game.api.rs3.local.hud.interfaces.eoc.ActionWindow;
import com.runemate.game.api.rs3.local.hud.interfaces.legacy.LegacyTab;
import com.runemate.game.api.rs3.region.Familiars;
import com.runemate.game.api.script.Execution;
import com.runemate.game.api.script.framework.LoopingBot;
import com.runemate.game.api.script.framework.listeners.AnimationListener;
import com.runemate.game.api.script.framework.listeners.ChatboxListener;
import com.runemate.game.api.script.framework.listeners.DeathListener;
import com.runemate.game.api.script.framework.listeners.EngineListener;
import com.runemate.game.api.script.framework.listeners.EquipmentListener;
import com.runemate.game.api.script.framework.listeners.GrandExchangeListener;
import com.runemate.game.api.script.framework.listeners.HitsplatListener;
import com.runemate.game.api.script.framework.listeners.InventoryListener;
import com.runemate.game.api.script.framework.listeners.MenuInteractionListener;
import com.runemate.game.api.script.framework.listeners.MoneyPouchListener;
import com.runemate.game.api.script.framework.listeners.PlayerMovementListener;
import com.runemate.game.api.script.framework.listeners.ProjectileLaunchListener;
import com.runemate.game.api.script.framework.listeners.SkillListener;
import com.runemate.game.api.script.framework.listeners.TargetListener;
import com.runemate.game.api.script.framework.listeners.VarbitListener;
import com.runemate.game.api.script.framework.listeners.VarpListener;
import com.runemate.game.api.script.framework.listeners.events.AnimationEvent;
import com.runemate.game.api.script.framework.listeners.events.DeathEvent;
import com.runemate.game.api.script.framework.listeners.events.GrandExchangeEvent;
import com.runemate.game.api.script.framework.listeners.events.HitsplatEvent;
import com.runemate.game.api.script.framework.listeners.events.ItemEvent;
import com.runemate.game.api.script.framework.listeners.events.MenuInteractionEvent;
import com.runemate.game.api.script.framework.listeners.events.MessageEvent;
import com.runemate.game.api.script.framework.listeners.events.MoneyPouchEvent;
import com.runemate.game.api.script.framework.listeners.events.PlayerMovementEvent;
import com.runemate.game.api.script.framework.listeners.events.ProjectileLaunchEvent;
import com.runemate.game.api.script.framework.listeners.events.SkillEvent;
import com.runemate.game.api.script.framework.listeners.events.TargetEvent;
import com.runemate.game.api.script.framework.listeners.events.VarbitEvent;
import com.runemate.game.api.script.framework.listeners.events.VarpEvent;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.util.Pair;

public class DevelopmentToolkit extends LoopingBot implements EmbeddableUI,
    GrandExchangeListener,
    ChatboxListener,
    InventoryListener,
    EquipmentListener,
    AnimationListener,
    MoneyPouchListener,
    SkillListener,
    VarpListener,
    VarbitListener,
    HitsplatListener,
    PlayerMovementListener,
    ProjectileLaunchListener,
    DeathListener,
    MenuInteractionListener,
    TargetListener,
    EngineListener/*,
        VarcListener*/ {
    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    static {
        ReflectiveTreeItem.BLACKLISTED_METHODS.clear();
        //noinspection RedundantTypeArguments (explicit type arguments speedup compilation and analysis time)
        ReflectiveTreeItem.BLACKLISTED_METHODS.addAll(Arrays.<Pair<Class<?>, String>>asList(
            new Pair<>(Object.class, "getClass"),
            new Pair<>(Object.class, "hashCode"),
            new Pair<>(Enum.class, "getDeclaringClass"),
            new Pair<>(Skill.class, "getExperienceToNextLevel"),
            new Pair<>(Skill.class, "getExperienceAsPercent"),
            new Pair<>(Skill.class, "getExperienceToNextLevelAsPercent"),
            new Pair<>(LocatableEntity.class, "getHighPrecisionPosition"),
            new Pair<>(Interactable.class, "isVisible"),
            new Pair<>(Validatable.class, "isValid"),
            new Pair<>(Rotatable.class, "getHighPrecisionOrientation"),
            new Pair<>(Model.class, "getBoundingModel"),
            new Pair<>(Coordinate.class, "getPosition"),
            new Pair<>(Coordinate.class, "getHighPrecisionPosition"),
            new Pair<>(Coordinate.class, "getBounds"),
            new Pair<>(Coordinate.class, "isLoaded"),
            new Pair<>(Area.class, "getRandomCoordinate"),
            new Pair<>(Area.Rectangular.class, "getArea"),
            new Pair<>(LocatableEntity.class, "getPlane"),
            new Pair<>(Actor.class, "getHeight"),
            new Pair<>(GameObject.class, "getCollisionType"),
            new Pair<>(GameObject.class, "getOrientation"),
            new Pair<>(Player.class, "getFamiliar"),
            new Pair<>(NpcDefinition.class, "getModelOffsets"),
            new Pair<>(ItemDefinition.class, "getInventoryActionArray"),
            new Pair<>(ItemDefinition.class, "getGroundActionArray"),
            new Pair<>(GameObjectDefinition.class, "getRawAppearance"),
            new Pair<>(GameObjectDefinition.class, "getModelTypes"),
            new Pair<>(GameObjectDefinition.class, "getModelXScale"),
            new Pair<>(GameObjectDefinition.class, "getModelYScale"),
            new Pair<>(GameObjectDefinition.class, "getModelZScale"),
            new Pair<>(GameObjectDefinition.class, "getModelHeight"),
            new Pair<>(NpcDefinition.class, "getModelXZScale"),
            new Pair<>(NpcDefinition.class, "getModelYScale"),
            new Pair<>(NpcDefinition.class, "getXYZModelTranslations"),
            new Pair<>(ItemDefinition.class, "getModelXScale"),
            new Pair<>(ItemDefinition.class, "getModelYScale"),
            new Pair<>(ItemDefinition.class, "getModelZScale"),
            new Pair<>(InterfaceContainer.class, "getId"),
            new Pair<>(InterfaceComponent.class, "getContainer"),
            new Pair<>(InterfaceComponent.class, "getParentComponent"),
            new Pair<>(InterfaceComponent.class, "getContentType"),
            new Pair<>(Traversal.class, "getDefaultWeb"),
            new Pair<>(Environment.class, "getLogger"),
            new Pair<>(Environment.class, "getSharedLogger"),
            new Pair<>(Environment.class, "isOSRS"),
            new Pair<>(Environment.class, "isRS3"),
            new Pair<>(Environment.class, "getBot"),
            new Pair<>(Mouse.class, "getPathGenerator"),
            new Pair<>(Region.class, "getHighPrecisionBase"),
            new Pair<>(Bank.class, "getItems"),
            new Pair<>(Interfaces.class, "getLoaded"),
            new Pair<>(Interfaces.class, "getVisiblePredicate")
        ));
        ReflectiveTreeItem.ALIASED_METHODS.addAll(Arrays.asList(
            new Pair<>(
                GameObject.class, new Pair<>(
                DevelopmentToolkit.createSpoofedMethod(GameObject.class, "getName", String.class),
                o -> {
                    GameObjectDefinition god = null;
                    String name = o instanceof GameObject
                        && (god = ((GameObject) o).getDefinition()) != null ? god.getName() : null;
                    if (Objects.equals("null", name)) {
                        name = null;
                    }
                    if (name == null && god != null && (god = god.getLocalState()) != null) {
                        name = god.getName();
                    }
                    if (Objects.equals("null", name)) {
                        name = null;
                    }
                    return name;
                }
            )),
            new Pair<>(
                Npc.class, new Pair<>(
                DevelopmentToolkit.createSpoofedMethod(Npc.class, "getName", String.class), npc -> {
                NpcDefinition nd = null;
                String name = npc instanceof Npc && (nd = ((Npc) npc).getDefinition()) != null ?
                    nd.getName() : null;
                if (Objects.equals("null", name)) {
                    name = null;
                }
                if (name == null && nd != null && (nd = nd.getLocalState()) != null) {
                    name = nd.getName();
                }
                if (Objects.equals("null", name)) {
                    name = null;
                }
                return name;
            }))
        ));

        DevelopmentToolkitPage.OVERRIDDEN_TO_STRINGS.put(Point.class, o -> {
            Point p = (Point) o;
            return "Point(" + p.x + ", " + p.y + ')';
        });
        DevelopmentToolkitPage.OVERRIDDEN_TO_STRINGS.put(Color.class, o -> {
            Color c = (Color) o;
            int alpha = c.getAlpha();
            if (alpha != 0xFF) {
                return "Rgba(" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ", "
                    + alpha + ')';
            } else {
                return "Rgb(" + c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ')';
            }
        });
        DevelopmentToolkitPage.OVERRIDDEN_TO_STRINGS.put(Dimension.class, o -> {
            Dimension d = (Dimension) o;
            return "Dimension(" + d.width + " x " + d.height + ')';
        });
        DevelopmentToolkitPage.OVERRIDDEN_TO_STRINGS.put(Rectangle.class, o -> {
            Rectangle r = (Rectangle) o;
            return "Rectangle(" + r.x + ", " + r.y + ", " + r.width + ", " + r.height + ')';
        });
        DevelopmentToolkitPage.OVERRIDDEN_TO_STRINGS.put(Rectangle2D.Double.class, o -> {
            Rectangle2D.Double rd = (Rectangle2D.Double) o;
            return "Rectangle2D(" + rd.x + ", " + rd.y + ", " + rd.width + ", " + rd.height + ')';
        });
    }

    private final DevelopmentToolkitOverlay overlay = new DevelopmentToolkitOverlay(this);
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final List<Renderable> renderables = Collections.synchronizedList(new ArrayList<>());
    private DevelopmentToolkitPage developmentToolkitPage;
    private TreeItem<Pair<Method, Object>> grandExchangeTreeItem, chatboxTreeItem,
        inventoryTreeItem,
        moneyPouchTreeItem, skillTreeItem, varpTreeItem, animationTreeItem, hitsplatTreeItem,
        equipmentTreeItem,
        varbitTreeItem, playerMovementTreeItem, deathTreeItem, menuInteractionTreeItem,
        targetTreeItem, projectileTreeItem;
    private ObjectProperty<DevelopmentToolkitPage> botInterfaceProperty;

    public DevelopmentToolkit() {
        setEmbeddableUI(this);
    }

    /*
          Class<?> declaringClass,
          String name,
          Class<?>[] parameterTypes,
          Class<?> returnType,
          Class<?>[] checkedExceptions,
          int modifiers,
          int slot,
          String signature,
          byte[] annotations,
          byte[] parameterAnnotations,
          byte[] annotationDefault)
     */
    public static Method createSpoofedMethod(Class<?> declaringClass, String name,
        Class<?> returnType) {
        try {
            Constructor<Method> methodConstructor =
                Method.class.getDeclaredConstructor(Class.class, String.class, Class[].class,
                    Class.class, Class[].class, int.class, int.class, String.class, byte[].class,
                    byte[].class, byte[].class
                );
            if (methodConstructor != null) {
                boolean isAccessible = methodConstructor.isAccessible();
                if (!isAccessible) {
                    methodConstructor.setAccessible(true);
                }
                Method method = methodConstructor.newInstance(declaringClass, name,
                    DevelopmentToolkit.EMPTY_CLASS_ARRAY, returnType,
                    DevelopmentToolkit.EMPTY_CLASS_ARRAY, Member.PUBLIC, -1, null,
                    DevelopmentToolkit.EMPTY_BYTE_ARRAY, DevelopmentToolkit.EMPTY_BYTE_ARRAY,
                    DevelopmentToolkit.EMPTY_BYTE_ARRAY
                );
                if (!isAccessible) {
                    methodConstructor.setAccessible(false);
                }
                return method;
            }
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
            System.err.println("Reflective Error On Thread: " + Thread.currentThread().getName());
        }
        return null;
    }

    @Override
    public void onStart(String... arguments) {
        if (Environment.isRS3()) {
            GameEvents.Universal.LOGIN_HANDLER.disable();
        } else {
            GameEvents.OSRS.NPC_DISMISSER.disable();
        }
        GameEvents.Universal.LOBBY_HANDLER.disable();
        GameEvents.Universal.BANK_PIN.disable();
        GameEvents.Universal.INTERFACE_CLOSER.disable();
        GameEvents.Universal.UNEXPECTED_ITEM_HANDLER.disable();
        while (botInterfaceProperty == null) {
            Execution.delay(100);
        }
        QueriableTreeItem.setExecutorService(executorService);
        StringProperty entitiesSearchTextProperty =
            botInterfaceProperty.get().getEntitiesSearchTextField().textProperty();
        BooleanProperty entitiesSearchRegexProperty =
            botInterfaceProperty.get().getEntitiesSearchRegexCheckBox().selectedProperty();
        botInterfaceProperty().get().getEntitiesTreeTableView().getRoot().getChildren().setAll(
            buildPseudoRootTreeItem(
                Players.class.getSimpleName(), () -> Players.getLoaded().sortByDistance(),
                entitiesSearchTextProperty, entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                Npcs.class.getSimpleName(), () -> Npcs.getLoaded().sortByDistance(),
                entitiesSearchTextProperty, entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                GameObjects.class.getSimpleName(), () -> GameObjects.getLoaded().sortByDistance(),
                entitiesSearchTextProperty, entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                SpotAnimations.class.getSimpleName(),
                () -> SpotAnimations.getLoaded().sortByDistance(), entitiesSearchTextProperty,
                entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                GroundItems.class.getSimpleName(), () -> GroundItems.getLoaded().sortByDistance(),
                entitiesSearchTextProperty, entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                Projectiles.class.getSimpleName(), () -> Projectiles.getLoaded().sortByDistance(),
                entitiesSearchTextProperty, entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                HintArrows.class.getSimpleName(), () -> HintArrows.getLoaded().sortByDistance(),
                entitiesSearchTextProperty, entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                InterfaceContainers.class.getSimpleName(), InterfaceContainers::getLoaded,
                entitiesSearchTextProperty, entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                InterfaceComponent.class.getSimpleName() + 's',
                () -> Interfaces.newQuery().results(), entitiesSearchTextProperty,
                entitiesSearchRegexProperty
            ),

            buildPseudoRootTreeItem(
                Inventory.class.getSimpleName(), () -> Inventory.getItems().sortByIndex(),
                entitiesSearchTextProperty, entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                Equipment.class.getSimpleName(), Equipment::getItems, entitiesSearchTextProperty,
                entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                "Equipment Slots", () -> Arrays.asList(Equipment.Slot.values()),
                entitiesSearchTextProperty, entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                Bank.class.getSimpleName(), () -> Bank.getItems().sortByIndex(),
                entitiesSearchTextProperty, entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                Shop.class.getSimpleName(), () -> Shop.getItems().sortByIndex(),
                entitiesSearchTextProperty, entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                "Incoming Trade", Trade.Incoming::getItems, entitiesSearchTextProperty,
                entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                "Outgoing Trade", Trade.Outgoing::getItems, entitiesSearchTextProperty,
                entitiesSearchRegexProperty
            ),

            buildPseudoRootTreeItem(
                Varps.class.getSimpleName(), Varps::getLoaded, entitiesSearchTextProperty,
                entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                Quests.class.getSimpleName(), Quests::getAll, entitiesSearchTextProperty,
                entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                SoundEffects.class.getSimpleName(), SoundEffects::getEmittingSoundEffects,
                entitiesSearchTextProperty, entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                Skills.class.getSimpleName(), () -> Arrays.asList(Skill.values()),
                entitiesSearchTextProperty, entitiesSearchRegexProperty
            ),
            buildPseudoRootTreeItem(
                Worlds.class.getSimpleName(), Worlds::getLoaded, entitiesSearchTextProperty,
                entitiesSearchRegexProperty
            )
        );
        botInterfaceProperty().get().getEventsTreeTableView().getRoot().getChildren().setAll(
            animationTreeItem =
                new TreeItem<>(new Pair<>(null, AnimationListener.class.getSimpleName())),
            grandExchangeTreeItem =
                new TreeItem<>(new Pair<>(null, GrandExchangeListener.class.getSimpleName())),
            chatboxTreeItem =
                new TreeItem<>(new Pair<>(null, ChatboxListener.class.getSimpleName())),
            inventoryTreeItem =
                new TreeItem<>(new Pair<>(null, InventoryListener.class.getSimpleName())),
            equipmentTreeItem =
                new TreeItem<>(new Pair<>(null, EquipmentListener.class.getSimpleName())),
            skillTreeItem = new TreeItem<>(new Pair<>(null, SkillListener.class.getSimpleName())),
            varpTreeItem = new TreeItem<>(new Pair<>(null, VarpListener.class.getSimpleName())),
            varbitTreeItem = new TreeItem<>(new Pair<>(null, VarbitListener.class.getSimpleName())),
            //varcTreeItem = new TreeItem<>(new Pair<>(null, VarcListener.class.getSimpleName())),
            hitsplatTreeItem =
                new TreeItem<>(new Pair<>(null, HitsplatListener.class.getSimpleName())),
            playerMovementTreeItem =
                new TreeItem<>(new Pair<>(null, PlayerMovementListener.class.getSimpleName())),
            deathTreeItem = new TreeItem<>(new Pair<>(null, DeathListener.class.getSimpleName())),
            menuInteractionTreeItem =
                new TreeItem<>(new Pair<>(null, MenuInteractionListener.class.getSimpleName())),
            targetTreeItem = new TreeItem<>(new Pair<>(null, TargetListener.class.getSimpleName()))
        );
        botInterfaceProperty().get().getMiscTreeTableView().getRoot().getChildren().setAll(
            new ReflectiveTreeItem.StaticReflectiveTreeItem(AccountInfo.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Bank.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Camera.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Chatbox.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(ChatDialog.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(DepositBox.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(EnterAmountDialog.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Environment.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Equipment.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(GrandExchange.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Health.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(House.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Interfaces.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(InterfaceWindows.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Inventory.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Keyboard.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Menu.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Mouse.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(NpcContact.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Region.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(RuneScape.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Screen.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Trade.class) {
                @Override
                public List<TreeItem<Pair<Method, Object>>> query() {
                    final List<TreeItem<Pair<Method, Object>>> results =
                        new ArrayList<>(Arrays.asList(
                            new StaticReflectiveTreeItem(Trade.Outgoing.class),
                            new StaticReflectiveTreeItem(Trade.Incoming.class)
                        ));
                    results.addAll(super.query());
                    return results;
                }
            },
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Shop.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Traversal.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(Wilderness.class),
            new ReflectiveTreeItem.StaticReflectiveTreeItem(WorldHop.class)
        );
        if (Environment.isOSRS()) {
            botInterfaceProperty().get().getMiscTreeTableView().getRoot().getChildren().addAll(
                new ReflectiveTreeItem.StaticReflectiveTreeItem(Prayer.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(Magic.class) {
                    @Override
                    public List<TreeItem<Pair<Method, Object>>> query() {
                        final List<TreeItem<Pair<Method, Object>>> results =
                            new ArrayList<>(Arrays.asList(
                                new StaticReflectiveTreeItem(Magic.Ancient.class),
                                new StaticReflectiveTreeItem(Magic.Lunar.class),
                                new StaticReflectiveTreeItem(Magic.Book.class)
                            ));
                        results.addAll(super.query());
                        return results;
                    }
                },
                new ReflectiveTreeItem.StaticReflectiveTreeItem(LootingBag.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(ControlPanelTab.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(OptionsTab.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(KourendHouseFavour.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(AchievementDiary.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(MakeAllInterface.class)
            );
        }
        if (Environment.isRS3()) {
            botInterfaceProperty().get().getEntitiesTreeTableView().getRoot().getChildren().add(
                buildPseudoRootTreeItem(
                    ActionBar.class.getSimpleName(),
                    () -> ActionBar.newQuery().filled(true).results(), entitiesSearchTextProperty,
                    entitiesSearchRegexProperty
                ));
            botInterfaceProperty().get().getEntitiesTreeTableView().getRoot().getChildren().add(
                buildPseudoRootTreeItem(
                    Familiars.class.getSimpleName(), Familiars::getLoaded,
                    entitiesSearchTextProperty, entitiesSearchRegexProperty
                ));
            botInterfaceProperty().get().getEventsTreeTableView().getRoot().getChildren().add(
                moneyPouchTreeItem =
                    new TreeItem<>(new Pair<>(null, MoneyPouchListener.class.getSimpleName())));
            botInterfaceProperty().get().getMiscTreeTableView().getRoot().getChildren().addAll(

                new ReflectiveTreeItem.StaticReflectiveTreeItem(ActionBar.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(ActionWindow.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(BeastOfBurden.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(CombatMode.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(InterfaceMode.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(GraphicsConfiguration.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(LegacyTab.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(Lodestone.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(LootInventory.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(MakeXInterface.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(MoneyPouch.class),
                new ReflectiveTreeItem.StaticReflectiveTreeItem(Powers.class) {
                    @Override
                    public List<TreeItem<Pair<Method, Object>>> query() {
                        final List<TreeItem<Pair<Method, Object>>> results =
                            new ArrayList<>(Arrays.asList(
                                new StaticReflectiveTreeItem(Powers.Magic.class) {
                                    @Override
                                    public List<TreeItem<Pair<Method, Object>>> query() {
                                        final List<TreeItem<Pair<Method, Object>>> results =
                                            new ArrayList<>(Arrays.asList(
                                                new StaticReflectiveTreeItem(
                                                    Powers.Magic.Lunar.class),
                                                new StaticReflectiveTreeItem(
                                                    Powers.Magic.Ancient.class),
                                                new StaticReflectiveTreeItem(
                                                    Powers.Magic.Dungeoneering.class),
                                                new StaticReflectiveTreeItem(
                                                    Powers.Magic.Book.class),
                                                new StaticReflectiveTreeItem(
                                                    Powers.Magic.Category.class)
                                            ));
                                        results.addAll(super.query());
                                        return results;
                                    }
                                },
                                new StaticReflectiveTreeItem(Powers.Prayer.class) {
                                    @Override
                                    public List<TreeItem<Pair<Method, Object>>> query() {
                                        final List<TreeItem<Pair<Method, Object>>> results =
                                            new ArrayList<>(Arrays.asList(
                                                new StaticReflectiveTreeItem(
                                                    Powers.Prayer.Curse.class),
                                                new StaticReflectiveTreeItem(
                                                    Powers.Prayer.Book.class)
                                            ));
                                        results.addAll(super.query());
                                        return results;
                                    }
                                }
                            ));
                        results.addAll(super.query());
                        return results;
                    }
                }, new ReflectiveTreeItem.StaticReflectiveTreeItem(Summoning.class)
            );
        }
        botInterfaceProperty().get().getMiscTreeTableView().getRoot().getChildren().sort(
            Comparator.comparing(
                o -> ((o.getValue().getValue() instanceof Class) ? (Class) o.getValue().getValue() :
                    o.getValue().getValue().getClass()).getSimpleName()));

        StringProperty databaseSearchTextProperty =
            botInterfaceProperty.get().getDatabaseSearchTextField().textProperty();
        BooleanProperty databaseSearchRegexProperty =
            botInterfaceProperty.get().getDatabaseSearchRegexCheckBox().selectedProperty();
        botInterfaceProperty().get().getDatabaseTreeTableView().getRoot().getChildren()
            .setAll(Arrays.asList(
                buildPseudoRootTreeItem(
                    GameObjectDefinition.class.getSimpleName() + 's', GameObjectDefinition::loadAll,
                    databaseSearchTextProperty, databaseSearchRegexProperty
                ),
                buildPseudoRootTreeItem(
                    ItemDefinition.class.getSimpleName() + 's',
                    () -> ItemDefinition.get(0, 100_000), databaseSearchTextProperty,
                    databaseSearchRegexProperty
                ),
                buildPseudoRootTreeItem(
                    NpcDefinition.class.getSimpleName() + 's', () -> NpcDefinition.get(0, 50000),
                    databaseSearchTextProperty, databaseSearchRegexProperty
                ),
                buildPseudoRootTreeItem(
                    SpotAnimationDefinitions.class.getSimpleName(),
                    SpotAnimationDefinitions::loadAll, databaseSearchTextProperty,
                    databaseSearchRegexProperty
                ),
                buildPseudoRootTreeItem(
                    EnumDefinitions.class.getSimpleName(), EnumDefinitions::loadAll,
                    databaseSearchTextProperty, databaseSearchRegexProperty
                ),
                buildPseudoRootTreeItem(
                    OverlayDefinitions.class.getSimpleName(), OverlayDefinitions::loadAll,
                    databaseSearchTextProperty, databaseSearchRegexProperty
                ),
                buildPseudoRootTreeItem(
                    UnderlayDefinitions.class.getSimpleName(), UnderlayDefinitions::loadAll,
                    databaseSearchTextProperty, databaseSearchRegexProperty
                ),
                buildPseudoRootTreeItem(
                    Materials.class.getSimpleName(), Materials::loadAll, databaseSearchTextProperty,
                    databaseSearchRegexProperty
                ),
                buildPseudoRootTreeItem(
                    IdentityKits.class.getSimpleName(), IdentityKits::loadAll,
                    databaseSearchTextProperty, databaseSearchRegexProperty
                ),
                buildPseudoRootTreeItem(
                    Varbits.class.getSimpleName(), Varbits::loadAll, databaseSearchTextProperty,
                    databaseSearchRegexProperty
                )
            ));
        setLoopDelay(2000);
        getEventDispatcher().addListener(this);

        overlay.start();
    }

    @Override
    public void onStop() {
        overlay.hide();
        Platform.runLater(() -> {
            botInterfaceProperty().get().getEntitiesTreeTableView().setRoot(null);
            botInterfaceProperty().get().getEventsTreeTableView().setRoot(null);
            botInterfaceProperty().get().getMiscTreeTableView().setRoot(null);
            botInterfaceProperty().get().getDatabaseTreeTableView().setRoot(null);
            botInterfaceProperty().get().setDisable(true);
            executorService.shutdown();
        });
    }

    @Override
    public ObjectProperty<DevelopmentToolkitPage> botInterfaceProperty() {
        if (botInterfaceProperty == null) {
            try {
                developmentToolkitPage = new DevelopmentToolkitPage(this);
            } catch (IOException ioe) {
                System.err.println("Failed to load Development Toolkit UI.");
                ioe.printStackTrace();
            }
            botInterfaceProperty = new SimpleObjectProperty<>(developmentToolkitPage);
        }
        return botInterfaceProperty;
    }

    public DevelopmentToolkitOverlay getOverlay() {
        return overlay;
    }

    private TreeItem<Pair<Method, Object>> buildPseudoRootTreeItem(final String name,
        final Callable<Collection<?>> query, final StringProperty searchTextProperty,
        final BooleanProperty searchRegexProperty) {
        return new QueriableTreeItem<Pair<Method, Object>>(new Pair<>(null, name)) {
            @Override
            public List<TreeItem<Pair<Method, Object>>> query() {
                String searchText = searchTextProperty != null ? searchTextProperty.get() : null;
                Predicate<Pair<Method, Object>> filter =
                    searchText != null && !searchText.isEmpty() ?
                        new RegexSearchPredicate(searchRegexProperty, searchText) : null;
                Predicate<ReflectiveTreeItem> treeItemPredicate = parent -> {
                    if (parent.getValue() == null) {
                        return false;
                    }
                    if (filter == null || filter.test(parent.getValue())) {
                        return true;
                    }
                    List<TreeItem<Pair<Method, Object>>> children = parent.query();
                    return children != null && children.stream().anyMatch(
                        child -> child.getValue() != null && filter.test(child.getValue()));
                };
                try {
                    return query.call().stream().map(i -> new ReflectiveTreeItem(null, i))
                        .filter(treeItemPredicate).collect(Collectors.toList());
                } catch (Exception e) {
                    System.err.println("Point B Thread: " + Thread.currentThread().getName());
                    e.printStackTrace();
                }
                return Collections.emptyList();
            }

            @Override
            public boolean isLeaf() {
                return false;
            }
        };
    }

    @Override
    public void onLoop() {

    }

    public List<Renderable> getRenderables() {
        return renderables;
    }

    @Override
    public void onMessageReceived(MessageEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> chatboxTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onContentsChanged(MoneyPouchEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> moneyPouchTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onItemAdded(ItemEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> inventoryTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onItemRemoved(ItemEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> inventoryTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onItemEquipped(ItemEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> equipmentTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onItemUnequipped(ItemEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> equipmentTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onExperienceGained(SkillEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> skillTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onLevelUp(SkillEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> skillTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onValueChanged(VarpEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> varpTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onValueChanged(VarbitEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(() -> varbitTreeItem.getChildren()
            .add(new ReflectiveTreeItem(null, new VarbitEventWrapper(event))));
    }

    @Override
    public void onSlotUpdated(GrandExchangeEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> grandExchangeTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onAnimationChanged(AnimationEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> animationTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onHitsplatAdded(HitsplatEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> hitsplatTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onDeath(DeathEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> deathTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onInteraction(MenuInteractionEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> menuInteractionTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onPlayerMoved(PlayerMovementEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> playerMovementTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onTargetChanged(TargetEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> targetTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onProjectileLaunched(ProjectileLaunchEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(
            () -> projectileTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onCycleStart() {
        try {
            final Function<TreeTablePosition<Pair<Method, Object>, ?>, Object> mapper = it -> {
                final TreeItem<Pair<Method, Object>> item = it.getTreeItem();
                final Pair<Method, Object> pair;
                return item == null ? null : (pair = item.getValue()) == null ? null : pair.getValue();
            };
            Stream<Object> stream = developmentToolkitPage.getEntitiesTreeTableView()
                .getSelectionModel()
                .getSelectedCells().stream()
                .map(mapper);

            stream = Stream.concat(
                stream,
                developmentToolkitPage.getMiscTreeTableView()
                    .getSelectionModel()
                    .getSelectedCells().stream()
                    .map(mapper)
            );

            final TreeTableView<Pair<Method, Object>> query = developmentToolkitPage.getQueryTreeView();
            if (query != null) {
                stream = Stream.concat(
                    stream,
                    developmentToolkitPage.getQueryTreeView()
                        .getSelectionModel()
                        .getSelectedCells().stream()
                        .map(mapper)
                );
            }

            if (developmentToolkitPage.hoverMouseOveroverProperty().get()) {
                final List<Entity> hovered = Region.getHoveredEntities();
                stream = hovered == null ? stream : Stream.concat(stream, hovered.stream());
            }

            renderables.clear();
            stream.filter(it -> Objects.nonNull(it) && it instanceof Renderable)
                .map(it -> (Renderable) it)
                .collect(Collectors.toCollection(() -> renderables));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*@Override
    public void onIntChanged(VarcEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(() -> varcTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }

    @Override
    public void onStringChanged(VarcEvent event) {
        if (!developmentToolkitPage.getEventsTitledPane().isExpanded()) {
            return;
        }
        Platform.runLater(() -> varcTreeItem.getChildren().add(new ReflectiveTreeItem(null, event)));
    }*/

    private static final class RegexSearchPredicate implements Predicate<Pair<Method, Object>> {
        private final Pattern pattern;
        private final String methodName;

        private RegexSearchPredicate(BooleanProperty searchRegexProperty, String searchText) {
            boolean searchHasMethodName = searchText.indexOf('=') != -1;
            methodName =
                searchHasMethodName ? searchText.substring(0, searchText.indexOf('=')) : null;
            pattern = Pattern.compile(
                searchRegexProperty != null && searchRegexProperty.get() ? searchText :
                    ".*" + searchText + ".*");
        }

        @Override
        public boolean test(Pair<Method, Object> pair) {
            Method method = pair.getKey();
            String thisMethodName = "";
            if (method != null) {
                thisMethodName = method.getName();
                if (methodName != null && !methodName.equals(thisMethodName)) {
                    return false;
                }
            }
            return pattern.matcher(
                (!Objects.requireNonNull(this.methodName).isEmpty() ? methodName + '=' : "")
                    + DevelopmentToolkitPage.cleanToString(pair.getValue())).matches();
        }
    }
}