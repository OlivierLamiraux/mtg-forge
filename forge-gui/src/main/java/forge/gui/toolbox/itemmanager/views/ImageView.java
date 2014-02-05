package forge.gui.toolbox.itemmanager.views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;

import forge.ImageCache;
import forge.game.card.Card;
import forge.gui.deckeditor.DeckProxy;
import forge.gui.framework.ILocalRepaint;
import forge.gui.match.controllers.CDetail;
import forge.gui.match.controllers.CPicture;
import forge.gui.toolbox.FMouseAdapter;
import forge.gui.toolbox.FScrollPane;
import forge.gui.toolbox.FSkin;
import forge.gui.toolbox.FSkin.SkinColor;
import forge.gui.toolbox.FSkin.SkinFont;
import forge.gui.toolbox.FSkin.SkinImage;
import forge.gui.toolbox.itemmanager.ItemManager;
import forge.gui.toolbox.itemmanager.ItemManagerModel;
import forge.gui.toolbox.special.CardZoomer;
import forge.item.IPaperCard;
import forge.item.InventoryItem;
import forge.view.arcane.CardPanel;

public class ImageView<T extends InventoryItem> extends ItemView<T> {
    private static final int PADDING = 5;
    private static final float GAP_SCALE_FACTOR = 0.04f;
    private static final float PILE_SPACING_Y = 0.1f;
    private static final SkinColor GROUP_HEADER_FORE_COLOR = FSkin.getColor(FSkin.Colors.CLR_TEXT);
    private static final SkinColor GROUP_HEADER_LINE_COLOR = GROUP_HEADER_FORE_COLOR.alphaColor(120);
    private static final SkinFont GROUP_HEADER_FONT = FSkin.getFont(12);
    private static final int GROUP_HEADER_HEIGHT = 19;
    private static final int GROUP_HEADER_GLYPH_WIDTH = 6;

    private final CardViewDisplay display;
    private List<Integer> selectedIndices = new ArrayList<Integer>();
    private int imageScaleFactor = 3;
    private boolean allowMultipleSelections;
    private ColumnDef pileBy = null;
    private GroupDef groupBy = null;
    private boolean lockHoveredItem = false;
    private Point hoverPoint;
    private Point hoverScrollPos;
    private ItemInfo hoveredItem;
    private ArrayList<ItemInfo> orderedItems = new ArrayList<ItemInfo>();
    private ArrayList<Group> groups = new ArrayList<Group>();

    public ImageView(ItemManager<T> itemManager0, ItemManagerModel<T> model0) {
        super(itemManager0, model0);
        display = new CardViewDisplay();
        display.addMouseListener(new FMouseAdapter() {
            @Override
            public void onLeftMouseDown(MouseEvent e) {
                if (!selectItem(e)) {
                    //if didn't click on item, see if clicked on group header
                    if (groupBy != null) {
                        Point point = e.getPoint();
                        for (Group group : groups) {
                            if (group.getBounds().contains(point)) {
                                if (!group.items.isEmpty() && point.y < group.getTop() + GROUP_HEADER_HEIGHT) {
                                    group.isCollapsed = !group.isCollapsed;
                                    clearSelection(); //must clear selection since indices and visible items will be changing
                                    updateLayout(false);
                                }
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onLeftDoubleClick(MouseEvent e) {
                ItemInfo item = getItemAtPoint(e.getPoint());
                if (item != null && item.selected) {
                    itemManager.activateSelectedItems();
                }
            }

            @Override
            public void onMiddleMouseDown(MouseEvent e) {
                ItemInfo item = getItemAtPoint(e.getPoint());
                if (item != null && item.item instanceof IPaperCard) {
                    setLockHoveredItem(true); //lock hoveredItem while zoomer open
                    Card card = Card.getCardForUi((IPaperCard) item.item);
                    CardZoomer.SINGLETON_INSTANCE.doMouseButtonZoom(card);
                }
            }

            @Override
            public void onMiddleMouseUp(MouseEvent e) {
                CardZoomer.SINGLETON_INSTANCE.closeZoomer();
                setLockHoveredItem(false);
            }

            @Override
            public void onRightClick(MouseEvent e) {
                if (selectItem(e)) {
                    setLockHoveredItem(true); //lock hoveredItem while context menu open
                    itemManager.showContextMenu(e, new Runnable() {
                        @Override
                        public void run() {
                            setLockHoveredItem(false);
                        }
                    });
                }
            }

            private boolean selectItem(MouseEvent e) {
                focus();

                ItemInfo item = getItemAtPoint(e.getPoint());
                if (item == null) {
                    if (!e.isControlDown() && !e.isShiftDown()) {
                        clearSelection();
                        onSelectionChange();
                    }
                    return false;
                }

                if (item.selected) {
                    //toggle selection off item if Control down and left mouse down, otherwise do nothing
                    if (e.getButton() != 1) {
                        return true;
                    }
                    if (e.isControlDown()) {
                        item.selected = false;
                        selectedIndices.remove((Object)item.index);
                        onSelectionChange();
                        item.scrollIntoView();
                        return true;
                    }
                }
                if (!allowMultipleSelections || (!e.isControlDown() && !e.isShiftDown())) {
                    clearSelection();
                }
                selectedIndices.add(0, item.index);
                item.selected = true;
                onSelectionChange();
                item.scrollIntoView();
                return true;
            }

            @Override
            public void onMouseExit(MouseEvent e) {
                if (updateHoveredItem(null, null)) {
                    display.repaintSelf();
                }
            }
        });
        display.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                FScrollPane scroller = getScroller();
                Point hoverScrollPos = new Point(scroller.getHorizontalScrollBar().getValue(), scroller.getVerticalScrollBar().getValue());
                if (updateHoveredItem(e.getPoint(), hoverScrollPos)) {
                    display.repaintSelf();
                }
            }
        });
        groups.add(new Group("")); //add default group
        getScroller().setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }

    public GroupDef getGroupBy() {
        return groupBy;
    }
    public void setGroupBy(GroupDef groupBy0) {
        setGroupBy(groupBy, false);
    }
    public void setGroupBy(GroupDef groupBy0, boolean forSetup) {
        if (groupBy == groupBy0) { return; }
        groupBy = groupBy0;

        groups.clear();

        if (groupBy == null) {
            groups.add(new Group(""));
        }
        else {
            for (String groupName : groupBy.getGroups()) {
                groups.add(new Group(groupName));
            }
        }

        if (!forSetup) {
            refresh(null, -1, 0);
        }
    }

    public ColumnDef getPileBy() {
        return pileBy;
    }
    public void setPileBy(ColumnDef pileBy0) {
        setPileBy(pileBy0, false);
    }
    public void setPileBy(ColumnDef pileBy0, boolean forSetup) {
        if (pileBy == pileBy0) { return; }
        pileBy = pileBy0;

        if (!forSetup) {
            refresh(null, -1, 0);
        }
    }

    @Override
    protected void fixSelection(final Iterable<T> itemsToSelect, final int backupIndexToSelect, final int scrollValueToRestore) {
        if (itemsToSelect == null) {
            clearSelection(); //just clear selection if no items to select
            setScrollValue(scrollValueToRestore); //ensure scroll value restored
            onSelectionChange();
        }
        else {
            if (!setSelectedItems(itemsToSelect)) {
                setSelectedIndex(backupIndexToSelect);
            }
        }
    }

    @Override
    protected void onResize() {
        updateLayout(false); //need to update layout to adjust wrapping of items
    }

    @Override
    protected void onRefresh() {
        Group otherItems = groupBy == null ? groups.get(0) : null;

        for (Group group : groups) {
            group.items.clear();
        }
        clearSelection();

        for (Entry<T, Integer> itemEntry : model.getOrderedList()) {
            T item = itemEntry.getKey();
            int qty = itemEntry.getValue();
            int groupIndex = groupBy == null ? -1 : groupBy.getItemGroupIndex(item);

            for (int i = 0; i < qty; i++) {
                if (groupIndex >= 0) {
                    groups.get(groupIndex).add(new ItemInfo(item));
                }
                else {
                    if (otherItems == null) {
                        //reuse existing Other group if possible
                        if (groups.size() > groupBy.getGroups().length) {
                            otherItems = groups.get(groups.size() - 1);
                        }
                        else {
                            otherItems = new Group("Other");
                            groups.add(otherItems);
                        }
                    }
                    otherItems.add(new ItemInfo(item));
                }
            }
        }

        if (otherItems == null && groups.size() > groupBy.getGroups().length) {
            groups.remove(groups.size() - 1); //remove Other group if empty
        }

        updateLayout(true);
    }

    private void updateLayout(boolean forRefresh) {
        int x, groupY, pileY, pileHeight, maxPileHeight;
        int y = PADDING;
        int groupX = PADDING;
        int itemAreaWidth = getVisibleSize().width;
        int groupWidth = itemAreaWidth - 2 * groupX;
        int pileX = PADDING;
        int pileWidth = itemAreaWidth - 2 * pileX;

        int itemWidth = 50 * imageScaleFactor;
        int gap = Math.round(itemWidth * GAP_SCALE_FACTOR);
        int dx = itemWidth + gap;
        int itemsPerRow = (pileWidth + gap) / dx;
        if (itemsPerRow == 0) {
            itemsPerRow = 1;
            itemWidth = pileWidth;
        }
        int itemHeight = Math.round(itemWidth * CardPanel.ASPECT_RATIO);
        int dy = pileBy == null ? itemHeight + gap : Math.round(itemHeight * PILE_SPACING_Y);

        for (int i = 0; i < groups.size(); i++) {
            Group group = groups.get(i);

            if (forRefresh && pileBy != null) { //refresh piles if needed
                //use TreeMap to build pile set so iterating below sorts on key
                ColumnDef groupPileBy = groupBy == null ? pileBy : groupBy.getGroupPileBy(i, pileBy);
                TreeMap<Comparable<?>, Pile> piles = new TreeMap<Comparable<?>, Pile>();
                for (ItemInfo itemInfo : group.items) {
                    Comparable<?> key = groupPileBy.fnSort.apply(itemInfo);
                    if (!piles.containsKey(key)) {
                        piles.put(key, new Pile());
                    }
                    piles.get(key).items.add(itemInfo);
                }
                group.piles.clear();
                for (Pile pile : piles.values()) {
                    group.piles.add(pile);
                }
            }

            groupY = y;
            if (groupBy != null) {
                y += GROUP_HEADER_HEIGHT + PADDING; //leave room for group header
                if (group.isCollapsed || group.items.isEmpty()) {
                    group.setBounds(groupX, groupY, groupWidth, GROUP_HEADER_HEIGHT);
                    continue;
                }
            }
            else if (group.items.isEmpty()) {
                group.setBounds(groupX, groupY, groupWidth, 0);
                continue;
            }

            if (pileBy == null) {
                //if not piling by anything, wrap items using a pile for each row
                group.piles.clear();
                Pile pile = new Pile();
                x = pileX;

                for (ItemInfo itemInfo : group.items) {
                    if (pile.items.size() == itemsPerRow) {
                        pile = new Pile();
                        x = pileX;
                        y += dy;
                    }

                    itemInfo.setBounds(x, y, itemWidth, itemHeight);

                    if (pile.items.size() == 0) {
                        pile.setBounds(pileX, y, pileWidth, itemHeight);
                        group.piles.add(pile);
                    }
                    pile.items.add(itemInfo);
                    x += dx;
                }
                y += itemHeight;
            }
            else {
                x = pileX;
                pileY = y;
                maxPileHeight = 0;
                for (int j = 0; j < group.piles.size(); j++) {
                    if (j > 0 && j % itemsPerRow == 0) {
                        //start new row if needed
                        y = pileY + maxPileHeight + gap;
                        x = pileX;
                        pileY = y;
                        maxPileHeight = 0;
                    }
                    Pile pile = group.piles.get(j);
                    y = pileY;
                    for (ItemInfo itemInfo : pile.items) {
                        itemInfo.setBounds(x, y, itemWidth, itemHeight);
                        y += dy;
                    }
                    pileHeight = y + itemHeight - dy - pileY;
                    if (pileHeight > maxPileHeight) {
                        maxPileHeight = pileHeight;
                    }
                    pile.setBounds(x, pileY, itemWidth, pileHeight);
                    x += dx;
                }
                y = pileY + maxPileHeight; //update y for setting group height below
            }

            group.setBounds(groupX, groupY, groupWidth, y - groupY);
            y += PADDING;
        }

        if (forRefresh) { //refresh ordered items if needed
            int index = 0;
            orderedItems.clear();
            for (Group group : groups) {
                if (group.isCollapsed || group.items.isEmpty()) { continue; }

                for (Pile pile : group.piles) {
                    for (ItemInfo itemInfo : pile.items) {
                        itemInfo.index = index++;
                        orderedItems.add(itemInfo);
                    }
                }
            }
        }

        display.setPreferredSize(new Dimension(itemAreaWidth, y));
        display.revalidate();
        display.repaintSelf();
    }

    private ItemInfo getItemAtPoint(Point p) {
        for (int i = groups.size() - 1; i >= 0; i--) {
            Group group = groups.get(i);
            if (!group.isCollapsed && group.getBounds().contains(p)) {
                for (int j = group.piles.size() - 1; j >= 0; j--) {
                    Pile pile = group.piles.get(j);
                    if (pile.getBounds().contains(p)) {
                        for (int k = pile.items.size() - 1; k >= 0; k--) {
                            ItemInfo item = pile.items.get(k);
                            if (item.getBounds().contains(p)) {
                                return item;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private Dimension getVisibleSize() {
        FScrollPane scroller = getScroller();
        Dimension size = getScroller().getSize();
        Insets insets = getScroller().getInsets();
        size =  new Dimension(size.width - insets.left - insets.right,
                size.height - insets.top - insets.bottom);
        size.width -= scroller.getVerticalScrollBar().getPreferredSize().width;
        return size;
    }

    private void setLockHoveredItem(boolean lockHoveredItem0) {
        if (lockHoveredItem == lockHoveredItem0) { return; }
        lockHoveredItem = lockHoveredItem0;
        if (!lockHoveredItem && updateHoveredItem(hoverPoint, hoverScrollPos)) {
            display.repaintSelf(); //redraw hover effect immediately if needed
        }
    }

    private boolean updateHoveredItem(Point hoverPoint0, Point hoverScrollPos0) {
        hoverPoint = hoverPoint0;
        hoverScrollPos = hoverScrollPos0;

        if (lockHoveredItem) { return false; }

        ItemInfo item = null;
        FScrollPane scroller = getScroller();
        if (hoverPoint0 != null) {
            Point displayPoint = new Point(hoverPoint0);
            //account for change in scroll positions since mouse last moved
            displayPoint.x += scroller.getHorizontalScrollBar().getValue() - hoverScrollPos0.x;
            displayPoint.y += scroller.getVerticalScrollBar().getValue() - hoverScrollPos0.y;
            item = getItemAtPoint(displayPoint);
        }

        if (hoveredItem == item) { return false; }
        hoveredItem = item;
        if (item != null) {
            showHoveredItem(item.item);
        }
        return true;
    }

    protected void showHoveredItem(T item) {
        CDetail.SINGLETON_INSTANCE.showCard(item);
        CPicture.SINGLETON_INSTANCE.showImage(item);
    }

    @Override
    public JComponent getComponent() {
        return display;
    }

    @Override
    public void setAllowMultipleSelections(boolean allowMultipleSelections0) {
        allowMultipleSelections = allowMultipleSelections0;
    }

    @Override
    public T getItemAtIndex(int index) {
        if (index >= 0 && index < getCount()) {
            return orderedItems.get(index).item;
        }
        return null;
    }

    @Override
    public int getIndexOfItem(T item) {
        for (Group group : groups) {
            for (ItemInfo itemInfo : group.items) {
                if (itemInfo.item == item) {
                    //if group containing item is collapsed, expand it so the item can be selected and has a valid index
                    if (group.isCollapsed) {
                        group.isCollapsed = false;
                        clearSelection(); //must clear selection since indices and visible items will be changing
                        updateLayout(false);
                    }
                    return itemInfo.index;
                }
            }
        }
        return -1;
    }

    @Override
    public int getSelectedIndex() {
        return selectedIndices.isEmpty() ? -1 : selectedIndices.get(0);
    }

    @Override
    public Iterable<Integer> getSelectedIndices() {
        return selectedIndices;
    }

    @Override
    public int getCount() {
        return orderedItems.size();
    }

    @Override
    public int getSelectionCount() {
        return selectedIndices.size();
    }

    @Override
    public int getIndexAtPoint(Point p) {
        ItemInfo item = getItemAtPoint(p);
        if (item != null) {
            return item.index;
        }
        return -1;
    }

    @Override
    protected SkinImage getIcon() {
        if (itemManager.getGenericType().equals(DeckProxy.class)) {
            return FSkin.getImage(FSkin.EditorImages.IMG_PACK).resize(18, 18);
        }
        return FSkin.getIcon(FSkin.InterfaceIcons.ICO_CARD_IMAGE);
    }

    @Override
    protected String getCaption() {
        return "Image View";
    }

    @Override
    public void selectAll() {
        clearSelection();
        for (Integer i = 0; i < getCount(); i++) {
            selectedIndices.add(i);
        }
        updateSelection();
    }

    @Override
    protected void onSetSelectedIndex(int index) {
        clearSelection();
        selectedIndices.add(index);
        updateSelection();
    }

    @Override
    protected void onSetSelectedIndices(Iterable<Integer> indices) {
        clearSelection();
        for (Integer index : indices) {
            selectedIndices.add(index);
        }
        updateSelection();
    }

    private void clearSelection() {
        int count = getCount();
        for (Integer i : selectedIndices) {
            if (i < count) {
                orderedItems.get(i).selected = false;
            }
        }
        selectedIndices.clear();
    }

    private void updateSelection() {
        for (Integer i : selectedIndices) {
            orderedItems.get(i).selected = true;
        }
        onSelectionChange();
    }

    @Override
    protected void onSelectionChange() {
        super.onSelectionChange();
        display.repaintSelf();
    }

    @Override
    protected void onScrollSelectionIntoView(JViewport viewport) {
        if (selectedIndices.isEmpty()) { return; }

        ItemInfo itemInfo = orderedItems.get(selectedIndices.get(0));
        itemInfo.scrollIntoView();
    }

    private class DisplayArea {
        private final Rectangle bounds = new Rectangle();

        public Rectangle getBounds() {
            return bounds;
        }
        public void setBounds(int x, int y, int width, int height) {
            bounds.x = x;
            bounds.y = y;
            bounds.width = width;
            bounds.height = height;
        }
        public int getTop() {
            return bounds.y;
        }
        public int getBottom() {
            return bounds.y + bounds.height;
        }
        public void scrollIntoView() {
            int x = bounds.x - PADDING;
            int y = bounds.y - PADDING;
            int width = bounds.width + 2 * PADDING;
            int height = bounds.height + 2 * PADDING;
            display.scrollRectToVisible(new Rectangle(x, y, width, height));
        }
    }
    private class Group extends DisplayArea {
        private final List<ItemInfo> items = new ArrayList<ItemInfo>();
        private final List<Pile> piles = new ArrayList<Pile>();
        private final String name;
        private boolean isCollapsed;

        public Group(String name0) {
            name = name0;
        }

        public void add(ItemInfo item) {
            items.add(item);
        }

        @Override
        public String toString() {
            return name;
        }
    }
    private class Pile extends DisplayArea {
        private final List<ItemInfo> items = new ArrayList<ItemInfo>();
    }
    private class ItemInfo extends DisplayArea implements Entry<InventoryItem, Integer> {
        private final T item;
        private int index;
        private boolean selected;

        private ItemInfo(T item0) {
            item = item0;
        }

        @Override
        public String toString() {
            return item.toString();
        }

        @Override
        public InventoryItem getKey() {
            return item;
        }

        @Override
        public Integer getValue() {
            return 1;
        }

        @Override
        public Integer setValue(Integer value) {
            return 1;
        }
    }

    @SuppressWarnings("serial")
    private class CardViewDisplay extends JPanel implements ILocalRepaint {
        private CardViewDisplay() {
            setOpaque(false);
            setFocusable(true);
        }

        @Override
        public void repaintSelf() {
            repaint(getVisibleRect());
        }

        @Override
        public final void paintComponent(final Graphics g) {
            updateHoveredItem(hoverPoint, hoverScrollPos); //ensure hovered item up to date

            final Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            final Dimension visibleSize = getVisibleSize();
            final int visibleTop = getScroller().getVerticalScrollBar().getValue();
            final int visibleBottom = visibleTop + visibleSize.height;

            FSkin.setGraphicsFont(g2d, GROUP_HEADER_FONT);
            FontMetrics fm = g2d.getFontMetrics();
            int fontOffsetY = (GROUP_HEADER_HEIGHT - fm.getHeight()) / 2 + fm.getAscent();

            for (Group group : groups) {
                if (group.getBottom() < visibleTop) {
                    continue;
                }
                if (group.getTop() >= visibleBottom) {
                    break;
                }
                if (groupBy != null) {
                    Rectangle bounds = group.getBounds();

                    //draw header background and border if hovered
                    //TODO: Uncomment
                    //FSkin.setGraphicsColor(g2d, ItemListView.HEADER_BACK_COLOR);
                    //g2d.fillRect(bounds.x, bounds.y, bounds.width, GROUP_HEADER_HEIGHT - 1);
                    //FSkin.setGraphicsColor(g2d, ItemListView.GRID_COLOR);
                    //g2d.drawRect(bounds.x, bounds.y, bounds.width - 1, GROUP_HEADER_HEIGHT - 1);

                    //draw group name and horizontal line
                    int x = bounds.x + GROUP_HEADER_GLYPH_WIDTH + PADDING + 1;
                    int y = bounds.y + fontOffsetY;
                    FSkin.setGraphicsColor(g2d, GROUP_HEADER_FORE_COLOR);
                    String caption = group.name + " (" + group.items.size() + ")";
                    g2d.drawString(caption, x, y);
                    x += fm.stringWidth(caption) + PADDING;
                    y = bounds.y + GROUP_HEADER_HEIGHT / 2;
                    FSkin.setGraphicsColor(g2d, GROUP_HEADER_LINE_COLOR);
                    g2d.drawLine(x, y, bounds.x + bounds.width - 1, y);

                    if (!group.items.isEmpty()) { //draw expand/collapse glyph as long as group isn't empty
                        Polygon glyph = new Polygon();
                        int offset = GROUP_HEADER_GLYPH_WIDTH / 2 + 1;
                        x = bounds.x + offset;
                        if (group.isCollapsed) {
                            y++;
                            glyph.addPoint(x, y - offset);
                            glyph.addPoint(x + offset, y);
                            glyph.addPoint(x, y + offset);
                        }
                        else {
                            glyph.addPoint(x - offset + 2, y + offset - 1);
                            glyph.addPoint(x + offset, y + offset - 1);
                            glyph.addPoint(x + offset, y - offset + 1);
                        }
                        g2d.fill(glyph);
                    }
                    if (group.isCollapsed || group.items.isEmpty()) {
                        continue;
                    }
                }
                else if (group.items.isEmpty()) {
                    continue;
                }

                ItemInfo skippedItem = null;
                for (Pile pile : group.piles) {
                    if (pile.getBottom() < visibleTop) {
                        continue;
                    }
                    if (pile.getTop() >= visibleBottom) {
                        break;
                    }
                    for (ItemInfo itemInfo : pile.items) {
                        if (itemInfo.getBottom() < visibleTop) {
                            continue;
                        }
                        if (itemInfo.getTop() >= visibleBottom) {
                            break;
                        }
                        if (itemInfo != hoveredItem) { //save hovered item for last
                            drawItemImage(g2d, itemInfo);
                        }
                        else {
                            skippedItem = itemInfo;
                        }
                    }
                }
                if (skippedItem != null) { //draw hovered item on top
                    drawItemImage(g2d, skippedItem);
                }
            }
        }

        private void drawItemImage(Graphics2D g, ItemInfo itemInfo) {
            Rectangle bounds = itemInfo.getBounds();
            final int itemWidth = bounds.width;
            final int selBorderSize = 1;
            final int borderSize = Math.round(itemWidth * CardPanel.BLACK_BORDER_SIZE);
            final int cornerSize = Math.max(4, Math.round(itemWidth * CardPanel.ROUNDED_CORNER_SIZE));

            if (itemInfo.selected || itemInfo == hoveredItem) {
                g.setColor(Color.green);
                g.fillRoundRect(bounds.x - selBorderSize, bounds.y - selBorderSize,
                        bounds.width + 2 * selBorderSize, bounds.height + 2 * selBorderSize,
                        cornerSize + selBorderSize, cornerSize + selBorderSize);
            }

            g.setColor(Color.black);
            g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, cornerSize, cornerSize);

            BufferedImage img = ImageCache.getImage(itemInfo.item, bounds.width - 2 * borderSize, bounds.height - 2 * borderSize);
            if (img != null) {
                g.drawImage(img, null, bounds.x + borderSize, bounds.y + borderSize);
            }
            else {
                g.setColor(Color.white);
                Shape clip = g.getClip();
                g.setClip(bounds);
                g.drawString(itemInfo.item.getName(), bounds.x + 10, bounds.y + 20);
                g.setClip(clip);
            }
        }
    }
}
