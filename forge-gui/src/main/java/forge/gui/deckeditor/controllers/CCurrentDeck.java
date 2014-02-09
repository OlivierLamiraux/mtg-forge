package forge.gui.deckeditor.controllers;

import forge.Command;
import forge.Singletons;
import forge.deck.Deck;
import forge.deck.DeckBase;
import forge.deck.io.DeckHtmlSerializer;
import forge.deck.io.DeckSerializer;
import forge.deck.io.DeckStorage;
import forge.error.BugReporter;
import forge.gui.deckeditor.CDeckEditorUI;
import forge.gui.deckeditor.DeckImport;
import forge.gui.deckeditor.SEditorIO;
import forge.gui.deckeditor.views.VCurrentDeck;
import forge.gui.framework.ICDoc;
import forge.item.InventoryItem;
import forge.properties.NewConstants;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.Dialog.ModalityType;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

/** 
 * Controls the "current deck" panel in the deck editor UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CCurrentDeck implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    private static File previousDirectory = null;

    private JFileChooser fileChooser = new JFileChooser(NewConstants.DECK_BASE_DIR);

    //========== Overridden methods

    private CCurrentDeck() {
        FileFilter[] defaultFilters = fileChooser.getChoosableFileFilters();
        for(FileFilter defFilter : defaultFilters)
        {
            fileChooser.removeChoosableFileFilter(defFilter);
        }

        FileFilter DCK_FILTER = new FileFilter() {
            @Override
            public boolean accept(final File f) {
                return f.getName().endsWith(DeckStorage.FILE_EXTENSION) || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return "Simple Deck File .dck";
            }
        };

        fileChooser.addChoosableFileFilter(DCK_FILTER);
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public Command getCommandOnSelect() {
        return null;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#initialize()
     */
    @Override
    @SuppressWarnings("serial")
    public void initialize() {
        VCurrentDeck.SINGLETON_INSTANCE.getBtnSave().setCommand(new Command() {
            @Override
            public void run() {
                SEditorIO.saveDeck();
            }
        });
        VCurrentDeck.SINGLETON_INSTANCE.getBtnSaveAs().setCommand(new Command() {
            @Override
            public void run() {
                exportDeck();
            }
        });
        VCurrentDeck.SINGLETON_INSTANCE.getBtnPrintProxies().setCommand(new Command() {
            @Override
            public void run() {
                printProxies();
            }
        });
        VCurrentDeck.SINGLETON_INSTANCE.getBtnOpen().setCommand(new Command() {
            @Override
            public void run() {
                openDeck();
            }
        });

        VCurrentDeck.SINGLETON_INSTANCE.getBtnNew().setCommand(new Command() {
            @Override
            public void run() {
                newDeck();
            }
        });
        VCurrentDeck.SINGLETON_INSTANCE.getBtnImport().setCommand(new Command() {
            @Override
            public void run() {
                importDeck();
            }
        });
        VCurrentDeck.SINGLETON_INSTANCE.getTxfTitle().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (Character.isLetterOrDigit(e.getKeyChar())) {
                    CDeckEditorUI.SINGLETON_INSTANCE.getCurrentEditorController().getDeckController().notifyModelChanged();
                }
            }
        });
    }

    /**
     * Opens dialog for importing a deck from a different MTG software.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private <TItem extends InventoryItem, TModel extends DeckBase> void importDeck() {
        final ACEditorBase<TItem, TModel> ed = (ACEditorBase<TItem, TModel>)
                CDeckEditorUI.SINGLETON_INSTANCE.getCurrentEditorController();

        final DeckImport dImport = new DeckImport(ed);
        dImport.setModalityType(ModalityType.APPLICATION_MODAL);
        dImport.setVisible(true);
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
    }

    @SuppressWarnings("unchecked")
    private void newDeck() {
        if (!SEditorIO.confirmSaveChanges(Singletons.getControl().getCurrentScreen(), true)) { return; }

        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ((DeckController<DeckBase>) CDeckEditorUI.SINGLETON_INSTANCE.getCurrentEditorController().getDeckController()).newModel();
                    VCurrentDeck.SINGLETON_INSTANCE.getTxfTitle().requestFocusInWindow();
                }
            });
        } catch (final Exception ex) {
            BugReporter.reportException(ex);
            throw new RuntimeException("Error creating new deck. " + ex);
        }
    }

    /** */
    @SuppressWarnings("unchecked")
    private void openDeck() {
        if (!SEditorIO.confirmSaveChanges(Singletons.getControl().getCurrentScreen(), true)) { return; }

        final File file = this.getImportFilename();

        if (file != null) {
            try {
                ((DeckController<DeckBase>) CDeckEditorUI.SINGLETON_INSTANCE
                        .getCurrentEditorController().getDeckController())
                        .setModel(DeckSerializer.fromFile(file));

            } catch (final Exception ex) {
                BugReporter.reportException(ex);
                throw new RuntimeException("Error importing deck." + ex);
            }
        }
    }

    /** */
    private File getImportFilename() {
        fileChooser.setDialogTitle("Import Deck");

        final int returnVal = fileChooser.showOpenDialog(null);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            previousDirectory = file.getParentFile();
            return file;
        }
        return null;
    }

    /** */
    @SuppressWarnings("unchecked")
    private void exportDeck() {
        final File filename = this.getExportFilename();
        if (filename == null) {
            return;
        }

        try {
            DeckSerializer.writeDeck(
                ((DeckController<Deck>) CDeckEditorUI.SINGLETON_INSTANCE
                .getCurrentEditorController().getDeckController()).getModel(), filename);
        } catch (final Exception ex) {
            BugReporter.reportException(ex);
            throw new RuntimeException("Error exporting deck." + ex);
        }
    }

    /** */
    @SuppressWarnings("unchecked")
    private void printProxies() {
        final File filename = this.getPrintProxiesFilename();
        if (filename == null) {
            return;
        }

        try {
            DeckHtmlSerializer.writeDeckHtml(
                ((DeckController<Deck>) CDeckEditorUI.SINGLETON_INSTANCE
                .getCurrentEditorController().getDeckController()).getModel(), filename);
        } catch (final Exception ex) {
            BugReporter.reportException(ex);
            throw new RuntimeException("Error exporting deck." + ex);
        }
    }

    private File getExportFilename() {
        fileChooser.setDialogTitle("Export Deck");
        fileChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        fileChooser.setCurrentDirectory(previousDirectory);

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            final File file = fileChooser.getSelectedFile();
            final String check = file.getAbsolutePath();

            previousDirectory = file.getParentFile();

            return check.endsWith(".dck") ? file : new File(check + ".dck");
        }
        return null;
    }

    private File getPrintProxiesFilename() {
        final JFileChooser save = new JFileChooser(previousDirectory);
        save.setDialogTitle("Print Proxies");
        save.setDialogType(JFileChooser.SAVE_DIALOG);
        save.setFileFilter(HTML_FILTER);

        if (save.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            final File file = save.getSelectedFile();
            final String check = file.getAbsolutePath();

            previousDirectory = file.getParentFile();

            return check.endsWith(".html") ? file : new File(check + ".html");
        }
        return null;
    }

    /** The Constant HTML_FILTER. */
    public static final FileFilter HTML_FILTER = new FileFilter() {
        @Override
        public boolean accept(final File f) {
            return f.getName().endsWith(".html") || f.isDirectory();
        }

        @Override
        public String getDescription() {
            return "Proxy File .html";
        }
    };
}
