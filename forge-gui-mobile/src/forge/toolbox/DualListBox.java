package forge.toolbox;

import forge.screens.match.views.VPrompt;
import forge.toolbox.FEvent.FEventHandler;
import forge.toolbox.FEvent.FEventType;
import forge.util.Callback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;

// An input box for handling the order of choices.
// Left box has the original choices
// Right box has the final order
// Top string will be like Top of the Stack or Top of the Library
// Bottom string will be like Bottom of the Stack or Bottom of the Library
// Single Arrows in between left box and right box for ordering
// Multi Arrows for moving everything in order
// Up/down arrows on the right of the right box for swapping
// Single ok button, disabled until left box has specified number of items remaining
public class DualListBox<T> extends FDialog {
    private final ChoiceList sourceList;
    private final ChoiceList destList;

    private final FButton addButton;
    private final FButton addAllButton;
    private final FButton removeButton;
    private final FButton removeAllButton;
    private final FButton okButton;
    private final FButton autoButton;

    private final FLabel orderedLabel;
    private final FLabel selectOrder;

    private final int targetRemainingSourcesMin;
    private final int targetRemainingSourcesMax;

    public DualListBox(String title, int remainingSources, List<T> sourceElements, List<T> destElements, final Callback<List<T>> callback) {
        this(title, remainingSources, remainingSources, sourceElements, destElements, callback);
    }
    
    public DualListBox(String title, int remainingSourcesMin, int remainingSourcesMax, List<T> sourceElements, List<T> destElements, final Callback<List<T>> callback) {
        super(title);
        targetRemainingSourcesMin = remainingSourcesMin;
        targetRemainingSourcesMax = remainingSourcesMax;

        final FEventHandler onAdd = new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                if (!addButton.isEnabled()) { return; }

                List<T> selected = sourceList.getSelectedItems();
                destList.clearSelection();
                for (T item : selected) {
                    sourceList.removeItem(item);
                    destList.addSelectedIndex(destList.getCount());
                    destList.addItem(item);
                }
                sourceList.cleanUpSelections();
                setButtonState();
            }
        };

        final FEventHandler onRemove = new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                if (!removeButton.isEnabled()) { return; }

                List<T> selected = destList.getSelectedItems();
                sourceList.clearSelection();
                for (T item : selected) {
                    destList.removeItem(item);
                    sourceList.addSelectedIndex(sourceList.getCount());
                    sourceList.addItem(item);
                }
                destList.cleanUpSelections();
                setButtonState();
            }
        };

        T typeItem = null; //pass typeItem so both lists use same renderer
        if (sourceElements != null && sourceElements.size() > 0) {
            typeItem = sourceElements.get(0);
        }
        else if (destElements != null && destElements.size() > 0) {
            typeItem = destElements.get(0);
        }
        sourceList = add(new ChoiceList(sourceElements, typeItem, onAdd));
        destList = add(new ChoiceList(destElements, typeItem, onRemove));

        // Dual List control buttons
        addButton = add(new FButton(">", onAdd));
        addAllButton = add(new FButton(">>", new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                addAll();
            }
        }));
        removeButton = add(new FButton("<", onRemove));
        removeAllButton = add(new FButton("<<", new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                removeAll();
            }
        }));
        
        final FEventHandler onAccept = new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                hide();
                callback.run(destList.extractListData());
            }
        };

        // Dual List Complete Buttons
        okButton = add(new FButton("OK", onAccept));
        autoButton = add(new FButton("Auto", new FEventHandler() {
            @Override
            public void handleEvent(FEvent e) {
                addAll();
                onAccept.handleEvent(e);
            }
        }));

        selectOrder = add(new FLabel.Builder().align(HAlignment.CENTER).text("Select Order").build());
        orderedLabel = add(new FLabel.Builder().align(HAlignment.CENTER).build());

        setButtonState();
    }

    @Override
    protected float layoutAndGetHeight(float width, float maxHeight) {
        float x = FOptionPane.PADDING;
        float y = FOptionPane.PADDING / 2;
        width -= 2 * x;
        maxHeight -= 2 * (VPrompt.HEIGHT - FDialog.INSETS);

        float gapBetweenButtons = FOptionPane.PADDING / 2;
        float buttonHeight = FOptionPane.BUTTON_HEIGHT;
        float labelHeight = selectOrder.getAutoSizeBounds().height;
        float listHeight = (maxHeight - 2 * labelHeight - buttonHeight - FOptionPane.PADDING - 2 * FDialog.INSETS) / 2;
        float addButtonWidth = addAllButton.getAutoSizeBounds().width * 1.2f;
        float addButtonHeight = listHeight / 2 - gapBetweenButtons;
        float listWidth = width - addButtonWidth - gapBetweenButtons;

        selectOrder.setBounds(x, y, width, labelHeight);
        y += labelHeight;
        sourceList.setBounds(x, y, listWidth, listHeight);
        x += width - addButtonWidth;
        addButton.setBounds(x, y, addButtonWidth, addButtonHeight);
        addAllButton.setBounds(x, y + addButtonHeight + gapBetweenButtons, addButtonWidth, addButtonHeight);
        y += listHeight + FOptionPane.PADDING / 2;

        x = FOptionPane.PADDING;
        orderedLabel.setBounds(x, y, width, labelHeight);
        y += labelHeight;
        removeButton.setBounds(x, y, addButtonWidth, addButtonHeight);
        removeAllButton.setBounds(x, y + addButtonHeight + gapBetweenButtons, addButtonWidth, addButtonHeight);
        destList.setBounds(x + width - listWidth, y, listWidth, listHeight);
        y += listHeight + FOptionPane.PADDING;

        float buttonWidth = (width - gapBetweenButtons) / 2;
        okButton.setBounds(x, y, buttonWidth, buttonHeight);
        x += buttonWidth + gapBetweenButtons;
        autoButton.setBounds(x, y, buttonWidth, buttonHeight);

        return maxHeight;
    }

    public void setSecondColumnLabelText(String label) {
        orderedLabel.setText(label);
    }

    public List<T> getRemainingSourceList() {
        return sourceList.extractListData();
    }

    private void addAll() {
        destList.clearSelection();
        for (T item : sourceList) {
            destList.addSelectedIndex(destList.getCount());
            destList.addItem(item);
        }
        sourceList.clear();
        setButtonState();
    }

    private void removeAll() {
        sourceList.clearSelection();
        for (T item : destList) {
            sourceList.addSelectedIndex(sourceList.getCount());
            sourceList.addItem(item);
        }
        destList.clear();
        setButtonState();
    }

    private void setButtonState() {
        boolean anySize = targetRemainingSourcesMax < 0;
        boolean canAdd = sourceList.getCount() != 0 && (anySize || targetRemainingSourcesMin <= sourceList.getCount());
        boolean canRemove = destList.getCount() != 0;
        boolean targetReached = anySize || targetRemainingSourcesMin <= sourceList.getCount() && targetRemainingSourcesMax >= sourceList.getCount();

        autoButton.setEnabled(targetRemainingSourcesMax == 0 && !targetReached);

        addButton.setEnabled(canAdd);
        addAllButton.setEnabled(canAdd);
        removeButton.setEnabled(canRemove);
        removeAllButton.setEnabled(canRemove);
        okButton.setEnabled(targetReached);
    }

    private class ChoiceList extends FChoiceList<T> {
        private final FEventHandler onActivate;

        private ChoiceList(Collection<T> items, final T typeItem, final FEventHandler onActivate0) {
            super(items != null ? items : new ArrayList<T>(), typeItem); //handle null without crashing
            onActivate = onActivate0;
        }

        protected void onItemActivate(Integer index, T value) {
            onActivate.handleEvent(new FEvent(this, FEventType.ACTIVATE, value));
        }
    }

    @Override
    public boolean keyDown(int keyCode) {
        switch (keyCode) {
        case Keys.ENTER:
        case Keys.ESCAPE: //Enter and Escape should trigger either OK or Auto based on which is enabled
            if (okButton.trigger()) {
                return true;
            }
            return autoButton.trigger();
        case Keys.SPACE: //Space should trigger OK button if enabled,
            //otherwise it should trigger first enabled button (default container behavior)
            if (okButton.trigger()) {
                return true;
            }
            break;
        }
        return super.keyDown(keyCode);
    }
}
