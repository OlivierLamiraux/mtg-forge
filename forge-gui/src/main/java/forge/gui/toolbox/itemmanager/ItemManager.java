/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.gui.toolbox.itemmanager;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import forge.UiCommand;
import forge.gui.GuiUtils;
import forge.gui.toolbox.*;
import forge.gui.toolbox.FSkin.Colors;
import forge.gui.toolbox.FSkin.SkinIcon;
import forge.gui.toolbox.FSkin.SkinnedCheckBox;
import forge.gui.toolbox.FSkin.SkinnedPanel;
import forge.gui.toolbox.itemmanager.filters.ItemFilter;
import forge.gui.toolbox.itemmanager.views.*;
import forge.item.InventoryItem;
import forge.util.Aggregates;
import forge.util.ItemPool;
import forge.util.ReflectionUtil;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;


/**
 * ItemManager.
 * 
 * @param <T>
 *            the generic type
 */
@SuppressWarnings("serial")
public abstract class ItemManager<T extends InventoryItem> extends JPanel {
    private ItemPool<T> pool;
    private final ItemManagerModel<T> model;
    private Predicate<? super T> filterPredicate = null;
    private final Map<Class<? extends ItemFilter<? extends T>>, List<ItemFilter<? extends T>>> filters =
            new HashMap<Class<? extends ItemFilter<? extends T>>, List<ItemFilter<? extends T>>>();
    private final List<ItemFilter<? extends T>> orderedFilters = new ArrayList<ItemFilter<? extends T>>();
    private boolean wantUnique = false;
    private boolean alwaysNonUnique = false;
    private boolean allowMultipleSelections = false;
    private boolean hideFilters = false;
    private UiCommand itemActivateCommand;
    private ContextMenuBuilder contextMenuBuilder;
    private final Class<T> genericType;
    private ItemManagerConfig config;
    private final ArrayList<ListSelectionListener> selectionListeners = new ArrayList<ListSelectionListener>();

    private final SkinnedCheckBox chkEnableFilters = new SkinnedCheckBox();

    private final FTextField txtFilterLogic = new FTextField.Builder()
        .tooltip("Use '&','|','!' symbols (AND,OR,NOT) in combination with filter numbers and optional grouping \"()\" to build Boolean expression evaluated when applying filters")
        .readonly() //TODO: Support editing filter logic
        .build();

    private final ItemFilter<? extends T> mainSearchFilter;
    private final SkinnedPanel pnlButtons = new SkinnedPanel(new MigLayout("insets 0, gap 0, ax center, hidemode 3"));

    private final FLabel btnFilters = new FLabel.ButtonBuilder()
        .text("Filters")
        .tooltip("Click to configure filters")
        .reactOnMouseDown()
        .build();

    private final FLabel lblCaption = new FLabel.Builder()
        .fontAlign(SwingConstants.LEFT)
        .fontSize(12)
        .build();

    private final FLabel lblRatio = new FLabel.Builder()
        .tooltip("Number of cards shown / Total available cards")
        .fontAlign(SwingConstants.LEFT)
        .fontSize(12)
        .build();

    private static final SkinIcon VIEW_OPTIONS_ICON = FSkin.getIcon(FSkin.DockIcons.ICO_SETTINGS).resize(20, 20);
    private final FLabel btnViewOptions = new FLabel.Builder()
        .hoverable().selectable(true)
        .icon(VIEW_OPTIONS_ICON).iconScaleAuto(false)
        .tooltip("Toggle to show/hide options for current view")
        .build();

    private final List<ItemView<T>> views = new ArrayList<ItemView<T>>();
    private final ItemListView<T> listView;
    private final ImageView<T> imageView;
    private ItemView<T> currentView;
    private boolean initialized;
    protected boolean lockFiltering;

    /**
     * ItemManager Constructor.
     * 
     * @param genericType0 the class of item that this table will contain
     * @param statLabels0 stat labels for this item manager
     * @param wantUnique0 whether this table should display only one item with the same name
     */
    protected ItemManager(final Class<T> genericType0, final boolean wantUnique0) {
        this.genericType = genericType0;
        this.wantUnique = wantUnique0;
        this.model = new ItemManagerModel<T>(genericType0);

        this.mainSearchFilter = createSearchFilter();

        this.listView = new ItemListView<T>(this, this.model);
        this.imageView = createImageView(this.model);

        this.views.add(this.listView);
        this.views.add(this.imageView);
        this.currentView = this.listView;
    }

    protected ImageView<T> createImageView(final ItemManagerModel<T> model0) {
        return new ImageView<T>(this, model0);
    }

    /**
     * Initialize item manager if needed
     */
    public void initialize() {
        if (this.initialized) { return; } //avoid initializing more than once

        //initialize views
        for (int i = 0; i < this.views.size(); i++) {
            this.views.get(i).initialize(i);
        }

        //build enable filters checkbox
        ItemFilter.layoutCheckbox(this.chkEnableFilters);
        this.chkEnableFilters.setText("(*)");
        this.chkEnableFilters.setSelected(true);
        this.chkEnableFilters.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent arg0) {
                lockFiltering = true;
                boolean enabled = chkEnableFilters.isSelected();
                for (ItemFilter<? extends T> filter : orderedFilters) {
                    filter.setEnabled(enabled);
                }
                txtFilterLogic.setEnabled(enabled);
                mainSearchFilter.setEnabled(enabled);
                mainSearchFilter.updateEnabled(); //need to call updateEnabled since no listener for filter checkbox
                lockFiltering = false;
                applyFilters();
            }
        });

        //build display
        this.setOpaque(false);
        this.setLayout(null);
        this.add(this.chkEnableFilters);
        this.add(this.txtFilterLogic);
        this.add(mainSearchFilter.getWidget());
        this.pnlButtons.setOpaque(false);
        this.pnlButtons.setBorder(new FSkin.MatteSkinBorder(1, 0, 1, 0, FSkin.getColor(Colors.CLR_TEXT)));
        this.add(this.pnlButtons);
        this.add(this.btnFilters);
        this.add(this.lblCaption);
        this.add(this.lblRatio);
        for (ItemView<T> view : this.views) {
            this.add(view.getButton());
            view.getButton().setSelected(view == this.currentView);
        }
        this.add(this.btnViewOptions);
        this.btnViewOptions.setSelected(this.currentView.getPnlOptions().isVisible());
        this.add(this.currentView.getPnlOptions());
        this.add(this.currentView.getScroller());

        final Runnable cmdAddCurrentSearch = new Runnable() {
            @Override
            public void run() {
                ItemFilter<? extends T> searchFilter = mainSearchFilter.createCopy();
                if (searchFilter != null) {
                    lockFiltering = true; //prevent updating filtering from this change
                    addFilter(searchFilter);
                    mainSearchFilter.reset();
                    lockFiltering = false;
                }
            }
        };
        final Runnable cmdResetFilters = new Runnable() {
            @Override
            public void run() {
                resetFilters();
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        focus();
                    }
                });
            }
        };
        final Runnable cmdHideFilters = new Runnable() {
            @Override
            public void run() {
                setHideFilters(!getHideFilters());
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        focus();
                    }
                });
            }
        };

        this.mainSearchFilter.getMainComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == 10) {
                    if (e.isControlDown() || e.isMetaDown()) {
                        cmdAddCurrentSearch.run();
                    }
                }
            }
        });

        //setup command for btnFilters
        final UiCommand cmdBuildFilterMenu = new UiCommand() {
            @Override
            public void run() {
                JPopupMenu menu = new JPopupMenu("FilterMenu");
                if (hideFilters) {
                    GuiUtils.addMenuItem(menu, "Show Filters", null, cmdHideFilters);
                }
                else {
                    JMenu addMenu = GuiUtils.createMenu("Add");
                    if (mainSearchFilter.isEnabled()) {
                        GuiUtils.addMenuItem(addMenu, "Current text search",
                                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                                cmdAddCurrentSearch, !mainSearchFilter.isEmpty());
                        if (config != ItemManagerConfig.STRING_ONLY) {
                            buildAddFilterMenu(addMenu);
                        }
                    }
                    else {
                        addMenu.setEnabled(false);
                    }
                    menu.add(addMenu);
                    GuiUtils.addSeparator(menu);
                    GuiUtils.addMenuItem(menu, "Reset Filters", null, cmdResetFilters);
                    GuiUtils.addMenuItem(menu, "Hide Filters", null, cmdHideFilters);
                }
                menu.show(btnFilters, 0, btnFilters.getHeight());
            }
        };
        this.btnFilters.setCommand(cmdBuildFilterMenu);
        this.btnFilters.setRightClickCommand(cmdBuildFilterMenu); //show menu on right-click too

        this.btnViewOptions.setCommand(new Runnable() {
            @Override
            public void run() {
                currentView.getPnlOptions().setVisible(!currentView.getPnlOptions().isVisible());
                revalidate();
            }
        });

        //setup initial filters
        addDefaultFilters();

        this.initialized = true; //must set flag just before applying filters
        if (!applyFilters()) {
            if (this.pool != null) { //ensure view updated even if filter predicate didn't change
                this.updateView(true, null);
            }
        }
    }

    public ItemManagerConfig getConfig() {
        return this.config;
    }

    public void setup(ItemManagerConfig config0) {
        this.setup(config0, null);
    }
    public void setup(ItemManagerConfig config0, Map<ColumnDef, ItemColumn> colOverrides) {
        this.config = config0;
        this.setWantUnique(config0.getUniqueCardsOnly());
        for (ItemView<T> view : this.views) {
            view.setup(config0, colOverrides);
        }
        this.setViewIndex(config0.getViewIndex());
        this.setHideFilters(config0.getHideFilters());
    }

    public void setViewIndex(int viewIndex) {
        if (viewIndex < 0 || viewIndex >= this.views.size()) { return; }
        ItemView<T> view = this.views.get(viewIndex);
        if (this.currentView == view) { return; }

        if (this.config != null) {
            this.config.setViewIndex(viewIndex);
        }

        final int backupIndexToSelect = this.currentView.getSelectedIndex();
        final Iterable<T> itemsToSelect; //only retain selected items if not single selection of first item
        if (backupIndexToSelect > 0 || this.getSelectionCount() > 1) {
            itemsToSelect = this.currentView.getSelectedItems();
        }
        else {
            itemsToSelect = null;
        }

        this.currentView.getButton().setSelected(false);
        this.remove(this.currentView.getPnlOptions());
        this.remove(this.currentView.getScroller());

        this.currentView = view;

        this.btnViewOptions.setSelected(view.getPnlOptions().isVisible());
        view.getButton().setSelected(true);
        view.refresh(itemsToSelect, backupIndexToSelect, 0);

        this.add(view.getPnlOptions());
        this.add(view.getScroller());
        this.revalidate();
        this.repaint();
        this.focus();
    }

    public void setHideViewOptions(int viewIndex, boolean hideViewOptions) {
        if (viewIndex < 0 || viewIndex >= this.views.size()) { return; }
        this.views.get(viewIndex).getPnlOptions().setVisible(!hideViewOptions);
    }

    @Override
    public void doLayout() {
        int buttonPanelHeight = 32;
        LayoutHelper helper = new LayoutHelper(this);

        boolean showButtonPanel = false;
        if (this.pnlButtons.isVisible()) {
            for (Component comp : this.pnlButtons.getComponents()) {
                if (comp.isVisible()) {
                    showButtonPanel = true;
                    break;
                }
            }
        }

        if (this.hideFilters) {
            if (showButtonPanel) {
                helper.offset(0, -4);
                helper.fillLine(this.pnlButtons, buttonPanelHeight);
            }
            else {
                this.pnlButtons.setBounds(0, 0, 0, 0); //prevent horizontal line appearing
            }
        }
        else {
            int number = 0;
            StringBuilder logicBuilder = new StringBuilder();
            for (ItemFilter<? extends T> filter : this.orderedFilters) {
                filter.setNumber(++number);
                logicBuilder.append(number + "&");
                helper.fillLine(filter.getPanel(), ItemFilter.PANEL_HEIGHT);
            }
            this.txtFilterLogic.setText(logicBuilder.toString());
            helper.newLine();
            helper.include(this.chkEnableFilters, 41, FTextField.HEIGHT);
            helper.offset(-1, 0); //ensure widgets line up
            helper.include(this.txtFilterLogic, this.txtFilterLogic.getAutoSizeWidth(), FTextField.HEIGHT);
            helper.fillLine(this.mainSearchFilter.getWidget(), ItemFilter.PANEL_HEIGHT);
            helper.newLine(-3);
            helper.fillLine(this.pnlButtons, showButtonPanel ? buttonPanelHeight : 1); //just show border if no buttons
        }
        helper.newLine();
        helper.offset(1, 0); //align filters button with expand/collapse all button
        helper.include(this.btnFilters, 61, FTextField.HEIGHT);
        int captionWidth = this.lblCaption.getAutoSizeWidth();
        int ratioWidth = this.lblRatio.getAutoSizeWidth();
        int viewButtonWidth = FTextField.HEIGHT;
        int viewButtonCount = this.views.size() + 1;
        int availableCaptionWidth = helper.getParentWidth() - viewButtonWidth * viewButtonCount - ratioWidth - helper.getX() - (viewButtonCount + 2) * helper.getGapX();
        if (captionWidth > availableCaptionWidth) { //truncate caption if not enough room for it
            this.lblCaption.setToolTipText(this.lblCaption.getText());
            captionWidth = availableCaptionWidth;
        }
        else {
            this.lblCaption.setToolTipText(null);
        }
        helper.include(this.lblCaption, captionWidth, FTextField.HEIGHT);
        helper.fillLine(this.lblRatio, FTextField.HEIGHT, (viewButtonWidth + helper.getGapX()) * viewButtonCount - viewButtonCount + 1); //leave room for view buttons
        for (ItemView<T> view : this.views) {
            helper.include(view.getButton(), viewButtonWidth, FTextField.HEIGHT);
            helper.offset(-1, 0);
        }
        helper.include(this.btnViewOptions, viewButtonWidth, FTextField.HEIGHT);
        helper.newLine(-1);
        if (this.currentView.getPnlOptions().isVisible()) {
            helper.fillLine(this.currentView.getPnlOptions(), FTextField.HEIGHT + 4);
        }
        helper.fill(this.currentView.getScroller());
    }

    /**
     * 
     * getGenericType.
     * 
     * @return generic type of items
     */
    public Class<T> getGenericType() {
        return this.genericType;
    }

    /**
     * 
     * getCaption.
     * 
     * @return caption to display before ratio
     */
    public String getCaption() {
        return this.lblCaption.getText();
    }

    /**
     * 
     * setCaption.
     * 
     * @param caption - caption to display before ratio
     */
    public void setCaption(String caption) {
        this.lblCaption.setText(caption);
    }

    /**
     * 
     * Gets the item pool.
     * 
     * @return ItemPoolView
     */
    public ItemPool<T> getPool() {
        return this.pool;
    }

    /**
     * 
     * Sets the item pool.
     * 
     * @param items
     */
    public void setPool(final Iterable<T> items) {
        this.setPool(ItemPool.createFrom(items, this.genericType), false);
    }

    /**
     * 
     * Sets the item pool.
     * 
     * @param poolView
     * @param infinite
     */
    public void setPool(final ItemPool<T> poolView, boolean infinite) {
        this.setPoolImpl(ItemPool.createFrom(poolView, this.genericType), infinite);
    }

    public void setPool(final ItemPool<T> pool0) {
        this.setPoolImpl(pool0, false);
    }

    /**
     * 
     * Sets the item pool.
     * 
     * @param pool0
     * @param infinite
     */
    private void setPoolImpl(final ItemPool<T> pool0, boolean infinite) {
        this.model.clear();
        this.pool = pool0;
        this.model.addItems(this.pool);
        this.model.setInfinite(infinite);
        this.updateView(true, null);
    }

    public ItemView<T> getCurrentView() {
        return this.currentView;
    }

    /**
     * 
     * getItemCount.
     * 
     * @return int
     */
    public int getItemCount() {
        return this.currentView.getCount();
    }

    /**
     * 
     * getSelectionCount.
     * 
     * @return int
     */
    public int getSelectionCount() {
        return this.currentView.getSelectionCount();
    }

    /**
     * 
     * getSelectedItem.
     * 
     * @return T
     */
    public T getSelectedItem() {
        return this.currentView.getSelectedItem();
    }

    /**
     * 
     * getSelectedItems.
     * 
     * @return Iterable<T>
     */
    public Collection<T> getSelectedItems() {
        return this.currentView.getSelectedItems();
    }

    /**
     * 
     * getSelectedItems.
     * 
     * @return ItemPool<T>
     */
    public ItemPool<T> getSelectedItemPool() {
        ItemPool<T> selectedItemPool = new ItemPool<T>(this.genericType);
        for (T item : getSelectedItems()) {
            selectedItemPool.add(item, getItemCount(item));
        }
        return selectedItemPool;
    }

    /**
     * 
     * setSelectedItem.
     * 
     * @param item - Item to select
     */
    public boolean setSelectedItem(T item) {
    	return this.currentView.setSelectedItem(item);
    }

    /**
     * 
     * setSelectedItems.
     * 
     * @param items - Items to select
     */
    public boolean setSelectedItems(Iterable<T> items) {
        return this.currentView.setSelectedItems(items);
    }

    /**
     * 
     * stringToItem.
     * 
     * @param str - String to get item corresponding to
     */
    public T stringToItem(String str) {
        for (Entry<T, Integer> itemEntry : this.pool) {
            if (itemEntry.getKey().toString().equals(str)) {
                return itemEntry.getKey();
            }
        }
        return null;
    }

    /**
     * 
     * setSelectedString.
     * 
     * @param str - String to select
     */
    public boolean setSelectedString(String str) {
        T item = stringToItem(str);
        if (item != null) {
            return this.setSelectedItem(item);
        }
        return false;
    }

    /**
     * 
     * setSelectedStrings.
     * 
     * @param strings - Strings to select
     */
    public boolean setSelectedStrings(Iterable<String> strings) {
        List<T> items = new ArrayList<T>();
        for (String str : strings) {
            T item = stringToItem(str);
            if (item != null) {
                items.add(item);
            }
        }
        return this.setSelectedItems(items);
    }

    /**
     * 
     * selectItemEntrys.
     * 
     * @param itemEntrys - Item entrys to select
     */
    public boolean selectItemEntrys(Iterable<Entry<T, Integer>> itemEntrys) {
        List<T> items = new ArrayList<T>();
        for (Entry<T, Integer> itemEntry : itemEntrys) {
            items.add(itemEntry.getKey());
        }
        return this.setSelectedItems(items);
    }

    /**
     * 
     * selectAll.
     * 
     */
    public void selectAll() {
        this.currentView.selectAll();
    }

    /**
     * 
     * getSelectedItem.
     * 
     * @return T
     */
    public int getSelectedIndex() {
        return this.currentView.getSelectedIndex();
    }

    /**
     * 
     * getSelectedItems.
     * 
     * @return Iterable<Integer>
     */
    public Iterable<Integer> getSelectedIndices() {
        return this.currentView.getSelectedIndices();
    }

    /**
     * 
     * setSelectedIndex.
     * 
     * @param index - Index to select
     */
    public void setSelectedIndex(int index) {
        this.currentView.setSelectedIndex(index);
    }

    /**
     * 
     * setSelectedIndices.
     * 
     * @param indices - Indices to select
     */
    public void setSelectedIndices(Integer[] indices) {
        this.currentView.setSelectedIndices(Arrays.asList(indices));
    }
    public void setSelectedIndices(Iterable<Integer> indices) {
        this.currentView.setSelectedIndices(indices);
    }

    /**
     * 
     * addItem.
     * 
     * @param item
     * @param qty
     */
    public void addItem(final T item, int qty) {
        this.pool.add(item, qty);
        if (this.isUnfiltered()) {
            this.model.addItem(item, qty);
        }
        List<T> items = new ArrayList<T>();
        items.add(item);
        this.updateView(false, items);
    }

    /**
     * 
     * addItems.
     * 
     * @param itemsToAdd
     */
    public void addItems(Iterable<Entry<T, Integer>> itemsToAdd) {
        this.pool.addAll(itemsToAdd);
        if (this.isUnfiltered()) {
            this.model.addItems(itemsToAdd);
        }

        List<T> items = new ArrayList<T>();
        for (Map.Entry<T, Integer> item : itemsToAdd) {
            items.add(item.getKey());
        }
        this.updateView(false, items);
    }

    /**
     * 
     * removeItem.
     * 
     * @param item
     * @param qty
     */
    public void removeItem(final T item, int qty) {
        final Iterable<T> itemsToSelect = this.currentView == this.listView ? this.getSelectedItems() : null;

        this.pool.remove(item, qty);
        if (this.isUnfiltered()) {
            this.model.removeItem(item, qty);
        }
        this.updateView(false, itemsToSelect);
    }

    /**
     * 
     * removeItems.
     * 
     * @param itemsToRemove
     */
    public void removeItems(Iterable<Map.Entry<T, Integer>> itemsToRemove) {
        final Iterable<T> itemsToSelect = this.currentView == this.listView ? this.getSelectedItems() : null;

        for (Map.Entry<T, Integer> item : itemsToRemove) {
            this.pool.remove(item.getKey(), item.getValue());
            if (this.isUnfiltered()) {
                this.model.removeItem(item.getKey(), item.getValue());
            }
        }
        this.updateView(false, itemsToSelect);
    }

    /**
     * 
     * removeAllItems.
     * 
     */
    public void removeAllItems() {
        this.pool.clear();
        this.model.clear();
        this.updateView(false, null);
    }

    /**
     * 
     * scrollSelectionIntoView.
     * 
     */
    public void scrollSelectionIntoView() {
        this.currentView.scrollSelectionIntoView();
    }

    /**
     * 
     * getItemCount.
     * 
     * @param item
     */
    public int getItemCount(final T item) {
        return this.model.isInfinite() ? Integer.MAX_VALUE : this.pool.count(item);
    }

    /**
     * Gets all filtered items in the model.
     * 
     * @return ItemPoolView<T>
     */
    public ItemPool<T> getFilteredItems() {
        return this.model.getItems();
    }

    protected abstract void addDefaultFilters();
    protected abstract ItemFilter<? extends T> createSearchFilter();
    protected abstract void buildAddFilterMenu(JMenu menu);

    protected <F extends ItemFilter<? extends T>> F getFilter(Class<F> filterClass) {
        return ReflectionUtil.safeCast(this.filters.get(filterClass), filterClass);
    }

    @SuppressWarnings("unchecked")
    public void addFilter(final ItemFilter<? extends T> filter) {
        final Class<? extends ItemFilter<? extends T>> filterClass = (Class<? extends ItemFilter<? extends T>>) filter.getClass();
        List<ItemFilter<? extends T>> classFilters = this.filters.get(filterClass);
        if (classFilters == null) {
            classFilters = new ArrayList<ItemFilter<? extends T>>();
            this.filters.put(filterClass, classFilters);
        }
        if (classFilters.size() > 0) {
            //if filter with the same class already exists, try to merge if allowed
            //NOTE: can always use first filter for these checks since if
            //merge is supported, only one will ever exist
            final ItemFilter<? extends T> existingFilter = classFilters.get(0);
            if (existingFilter.merge(filter)) {
                //if new filter merged with existing filter, just refresh the widget
                existingFilter.refreshWidget();
                this.applyNewOrModifiedFilter(existingFilter);
                return;
            }
        }
        classFilters.add(filter);
        orderedFilters.add(filter);
        this.add(filter.getPanel());

        boolean visible = !this.hideFilters;
        filter.getPanel().setVisible(visible);
        if (visible && this.initialized) {
            this.revalidate();
            this.applyNewOrModifiedFilter(filter);
        }
    }

    //apply filters and focus existing filter's main component if filtering not locked
    private void applyNewOrModifiedFilter(final ItemFilter<? extends T> filter) {
        if (this.lockFiltering) {
            filter.afterFiltersApplied(); //ensure this called even if filters currently locked
            return;
        }

        if (!applyFilters()) {
            filter.afterFiltersApplied(); //ensure this called even if filters didn't need to be updated
        }
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                filter.getMainComponent().requestFocusInWindow();
            }
        });
    }

    public void restoreDefaultFilters() {
        lockFiltering = true;
        for (ItemFilter<? extends T> filter : this.orderedFilters) {
            this.remove(filter.getPanel());
        }
        this.filters.clear();
        this.orderedFilters.clear();
        addDefaultFilters();
        lockFiltering = false;
        this.revalidate();
        this.applyFilters();
    }

    @SuppressWarnings("unchecked")
    public void removeFilter(ItemFilter<? extends T> filter) {
        final Class<? extends ItemFilter<? extends T>> filterClass = (Class<? extends ItemFilter<? extends T>>) filter.getClass();
        final List<ItemFilter<? extends T>> classFilters = this.filters.get(filterClass);
        if (classFilters != null && classFilters.remove(filter)) {
            if (classFilters.size() == 0) {
                this.filters.remove(filterClass);
            }
            this.orderedFilters.remove(filter);
            this.remove(filter.getPanel());
            this.revalidate();
            applyFilters();
        }
    }

    public boolean applyFilters() {
        if (this.lockFiltering || !this.initialized) { return false; }

        List<Predicate<? super T>> predicates = new ArrayList<Predicate<? super T>>();
        for (ItemFilter<? extends T> filter : this.orderedFilters) { //TODO: Support custom filter logic
            if (filter.isEnabled() && !filter.isEmpty()) {
                predicates.add(filter.buildPredicate(this.genericType));
            }
        }
        if (!this.mainSearchFilter.isEmpty()) {
            predicates.add(mainSearchFilter.buildPredicate(this.genericType));
        }

        Predicate<? super T> newFilterPredicate = predicates.size() == 0 ? null : Predicates.and(predicates);
        if (this.filterPredicate == newFilterPredicate) { return false; }

        this.filterPredicate = newFilterPredicate;
        if (this.pool != null) {
            this.updateView(true, this.getSelectedItems());
        }
        return true;
    }

    /**
     * 
     * isUnfiltered.
     * 
     */
    private boolean isUnfiltered() {
        return this.filterPredicate == null;
    }

    /**
     * 
     * getHideFilters.
     * 
     * @return true if filters are hidden, false otherwise
     */
    public boolean getHideFilters() {
        return this.hideFilters;
    }

    /**
     * 
     * setHideFilters.
     * 
     * @param hideFilters0 - if true, hide the filters, otherwise show them
     */
    public void setHideFilters(boolean hideFilters0) {
        if (this.hideFilters == hideFilters0) { return; }
        this.hideFilters = hideFilters0;

        boolean visible = !hideFilters0;
        for (ItemFilter<? extends T> filter : this.orderedFilters) {
            filter.getPanel().setVisible(visible);
        }
        this.chkEnableFilters.setVisible(visible);
        this.txtFilterLogic.setVisible(visible);
        this.mainSearchFilter.getWidget().setVisible(visible);

        if (this.initialized) {
            this.revalidate();
    
            if (hideFilters0) {
                this.resetFilters(); //reset filters when they're hidden
            }
            else {
                this.applyFilters();
            }

            if (this.config != null) {
                this.config.setHideFilters(hideFilters0);
            }
        }
    }

    /**
     * 
     * resetFilters.
     * 
     */
    public void resetFilters() {
        lockFiltering = true; //prevent updating filtering from this change until all filters reset
        for (ItemFilter<? extends T> filter : orderedFilters) {
            filter.setEnabled(true);
            filter.reset();
        }
        mainSearchFilter.reset();
        lockFiltering = false;

        if (mainSearchFilter.isEnabled()) {
            applyFilters();
        }
        else {
            chkEnableFilters.setSelected(true); //this will apply filters in itemStateChanged handler
        }
    }

    /**
     * Refresh displayed items
     */
    public void refresh() {
        this.updateView(true, this.getSelectedItems());
    }

    /**
     * 
     * updateView.
     * 
     * @param bForceFilter
     */
    public void updateView(final boolean forceFilter, final Iterable<T> itemsToSelect) {
        final boolean useFilter = (forceFilter && (this.filterPredicate != null)) || !isUnfiltered();

        if (useFilter || this.wantUnique || forceFilter) {
            this.model.clear();
        }

        if (useFilter && this.wantUnique) {
            Predicate<Entry<T, Integer>> filterForPool = Predicates.compose(this.filterPredicate, this.pool.FN_GET_KEY);
            Iterable<Entry<T, Integer>> items = Aggregates.uniqueByLast(Iterables.filter(this.pool, filterForPool), this.pool.FN_GET_NAME);
            this.model.addItems(items);
        }
        else if (useFilter) {
            Predicate<Entry<T, Integer>> pred = Predicates.compose(this.filterPredicate, this.pool.FN_GET_KEY);
            this.model.addItems(Iterables.filter(this.pool, pred));
        }
        else if (this.wantUnique) {
            Iterable<Entry<T, Integer>> items = Aggregates.uniqueByLast(this.pool, this.pool.FN_GET_NAME);
            this.model.addItems(items);
        }
        else if (!useFilter && forceFilter) {
            this.model.addItems(this.pool);
        }

        this.currentView.refresh(itemsToSelect, this.getSelectedIndex(), forceFilter ? 0 : this.currentView.getScrollValue());

        for (ItemFilter<? extends T> filter : this.orderedFilters) {
            filter.afterFiltersApplied();
        }

        //update ratio of # in filtered pool / # in total pool
        int total;
        if (!useFilter) {
            total = this.getFilteredItems().countAll();
        }
        else if (this.wantUnique) {
            total = 0;
            Iterable<Entry<T, Integer>> items = Aggregates.uniqueByLast(this.pool, this.pool.FN_GET_NAME);
            for (Entry<T, Integer> entry : items) {
                total += entry.getValue();
            }
        }
        else {
            total = this.pool.countAll();
        }
        this.lblRatio.setText("(" + this.getFilteredItems().countAll() + " / " + total + ")");
    }

    /**
     * 
     * getPnlButtons.
     * 
     * @return panel to put any custom buttons on
     */
    public JPanel getPnlButtons() {
        return this.pnlButtons;
    }

    /**
     * 
     * isIncrementalSearchActive.
     * 
     * @return true if an incremental search is currently active
     */
    public boolean isIncrementalSearchActive() {
        return this.currentView.isIncrementalSearchActive();
    }

    /**
     * 
     * getWantUnique.
     * 
     * @return true if the editor is in "unique item names only" mode.
     */
    public boolean getWantUnique() {
        return this.wantUnique;
    }

    /**
     * 
     * setWantUnique.
     * 
     * @param unique - if true, the editor will be set to the "unique item names only" mode.
     */
    public void setWantUnique(boolean unique) {
        this.wantUnique = this.alwaysNonUnique ? false : unique;
    }

    /**
     * 
     * getAlwaysNonUnique.
     * 
     * @return if true, this editor must always show non-unique items (e.g. quest editor).
     */
    public boolean getAlwaysNonUnique() {
        return this.alwaysNonUnique;
    }

    /**
     * 
     * setAlwaysNonUnique.
     * 
     * @param nonUniqueOnly - if true, this editor must always show non-unique items (e.g. quest editor).
     */
    public void setAlwaysNonUnique(boolean nonUniqueOnly) {
        this.alwaysNonUnique = nonUniqueOnly;
    }

    /**
     * 
     * getAllowMultipleSelections.
     * 
     * @return if true, multiple items can be selected at once
     */
    public boolean getAllowMultipleSelections() {
    	return this.allowMultipleSelections;
    }

    /**
     * 
     * setAllowMultipleSelections.
     * 
     * @return allowMultipleSelections0 - if true, multiple items can be selected at once
     */
    public void setAllowMultipleSelections(boolean allowMultipleSelections0) {
    	if (this.allowMultipleSelections == allowMultipleSelections0) { return; }
    	this.allowMultipleSelections = allowMultipleSelections0;
    	for (ItemView<T> view : views) {
    	    view.setAllowMultipleSelections(allowMultipleSelections0);
    	}
    }

    /**
     * 
     * isInfinite.
     * 
     * @return whether item manager's pool of items is in infinite supply
     */
    public boolean isInfinite() {
        return this.model.isInfinite();
    }

    /**
     * 
     * focus.
     * 
     */
    public void focus() {
        this.currentView.focus();
    }

    /**
     * 
     * focusSearch.
     * 
     */
    public void focusSearch() {
        this.setHideFilters(false); //ensure filters shown
        this.mainSearchFilter.getMainComponent().requestFocusInWindow();
    }

    public void addSelectionListener(ListSelectionListener listener) {
    	selectionListeners.remove(listener); //ensure listener not added multiple times
    	selectionListeners.add(listener);
    }

    public void removeSelectionListener(ListSelectionListener listener) {
    	selectionListeners.remove(listener);
    }

    public Iterable<ListSelectionListener> getSelectionListeners() {
    	return selectionListeners;
    }

    public void setItemActivateCommand(UiCommand itemActivateCommand0) {
        this.itemActivateCommand = itemActivateCommand0;
    }

    public void activateSelectedItems() {
        if (this.itemActivateCommand != null) {
            this.itemActivateCommand.run();
        }
    }

    public void setContextMenuBuilder(ContextMenuBuilder contextMenuBuilder0) {
        this.contextMenuBuilder = contextMenuBuilder0;
    }

    public void showContextMenu(MouseEvent e) {
        showContextMenu(e, null);
    }
    public void showContextMenu(MouseEvent e, final Runnable onClose) {
        //ensure the item manager has focus
        this.focus();

        //if item under the cursor is not selected, select it
        int index = this.currentView.getIndexAtPoint(e.getPoint());
        boolean needSelection = true;
        for (Integer selectedIndex : this.getSelectedIndices()) {
            if (selectedIndex == index) {
                needSelection = false;
                break;
            }
        }
        if (needSelection) {
            this.setSelectedIndex(index);
        }

        if (this.contextMenuBuilder == null) {
            if (onClose != null) {
                onClose.run(); //run onClose immediately even if no context menu shown
            }
            return;
        }

        JPopupMenu menu = new JPopupMenu("ItemManagerContextMenu");
        this.contextMenuBuilder.buildContextMenu(menu);

        if (onClose != null) {
            menu.addPopupMenuListener(new PopupMenuListener() {
                @Override
                public void popupMenuCanceled(PopupMenuEvent arg0) {
                    onClose.run();
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
                    onClose.run();
                }

                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
                }
            });
        }

        menu.show(e.getComponent(), e.getX(), e.getY());
    }
}
