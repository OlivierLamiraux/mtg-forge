/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Nate
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
package forge.planarconquest;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;

import forge.card.CardDb;
import forge.card.CardEdition;
import forge.card.CardEdition.CardInSet;
import forge.deck.generation.DeckGenPool;
import forge.item.PaperCard;
import forge.model.FModel;
import forge.properties.ForgeConstants;
import forge.util.FileUtil;
import forge.util.collect.FCollection;
import forge.util.collect.FCollectionView;
import forge.util.storage.StorageBase;
import forge.util.storage.StorageReaderFile;


public class ConquestPlane {
    private final String name;
    private final String directory;
    private FCollection<ConquestRegion> regions;
    private DeckGenPool cardPool;
    private FCollection<PaperCard> planeCards;
    private FCollection<PaperCard> commanders;
    private ConquestAwardPool awardPool;

    private ConquestPlane(String name0) {
        name = name0;
        directory = ForgeConstants.CONQUEST_PLANES_DIR + name + ForgeConstants.PATH_SEPARATOR;
    }

    public String getName() {
        return name;
    }

    public String getDirectory() {
        return directory;
    }

    public FCollectionView<ConquestRegion> getRegions() {
        ensureRegionsLoaded();
        return regions;
    }

    public int getEventCount() {
        ensureRegionsLoaded();
        return regions.size() * ConquestRegion.ROWS_PER_REGION * ConquestRegion.COLS_PER_REGION;
    }

    public DeckGenPool getCardPool() {
        ensureRegionsLoaded();
        return cardPool;
    }

    public FCollectionView<PaperCard> getCommanders() {
        ensureRegionsLoaded();
        return commanders;
    }

    public FCollectionView<PaperCard> getPlaneCards() {
        if (planeCards == null) {
            planeCards = new FCollection<PaperCard>();

            CardDb variantCards = FModel.getMagicDb().getVariantCards();
            List<String> planeCardNames = FileUtil.readFile(directory + "plane_cards.txt");
            for (String name : planeCardNames) {
                PaperCard pc = variantCards.getCard(name);
                if (pc == null) {
                    System.out.println("\"" + name + "\" does not correspond to a valid Plane card");
                    continue;
                }
                planeCards.add(pc);
            }
        }
        return planeCards;
    }

    private void ensureRegionsLoaded() {
        if (regions != null) { return; }

        regions = new FCollection<ConquestRegion>(new StorageBase<ConquestRegion>("Conquest regions", new ConquestRegion.Reader(this)));

        //must initialize card pool when regions loaded
        cardPool = new DeckGenPool();
        commanders = new FCollection<PaperCard>();

        CardDb commonCards = FModel.getMagicDb().getCommonCards();
        List<String> bannedCards = FileUtil.readFile(directory + "banned_cards.txt");
        Set<String> bannedCardSet = bannedCards.isEmpty() ? null : new HashSet<String>(bannedCards);

        List<String> setCodes = FileUtil.readFile(directory + "sets.txt");
        for (String setCode : setCodes) {
            CardEdition edition = FModel.getMagicDb().getEditions().get(setCode);
            if (edition != null) {
                for (CardInSet card : edition.getCards()) {
                    if (bannedCardSet == null || !bannedCardSet.contains(card.name)) {
                        addCard(commonCards.getCard(card.name, setCode));
                    }
                }
            }
        }

        List<String> additionalCards = FileUtil.readFile(directory + "cards.txt");
        for (String cardName : additionalCards) {
            addCard(commonCards.getCard(cardName));
        }

        //sort commanders by name
        Collections.sort(commanders);
    }

    private void addCard(PaperCard pc) {
        if (pc == null) { return; }

        cardPool.add(pc);
        if (pc.getRules().canBeCommander()) {
            commanders.add(pc);
        }
        ConquestRegion.addCard(pc, regions);
    }

    public String toString() {
        return name;
    }

    public static final Function<ConquestPlane, String> FN_GET_NAME = new Function<ConquestPlane, String>() {
        @Override
        public String apply(ConquestPlane plane) {
            return plane.getName();
        }
    };

    public ConquestAwardPool getAwardPool() {
        if (awardPool == null) { //delay initializing until needed
            awardPool = new ConquestAwardPool(cardPool.getAllCards());
        }
        return awardPool;
    }

    public static class Reader extends StorageReaderFile<ConquestPlane> {
        public Reader(String file0) {
            super(file0, ConquestPlane.FN_GET_NAME);
        }

        @Override
        protected ConquestPlane read(String line, int i) {
            return new ConquestPlane(line);
        }
    }

    public static Set<ConquestPlane> getAllPlanesOfCard(PaperCard card) {
        Set<ConquestPlane> planes = new HashSet<ConquestPlane>();
        for (ConquestPlane plane : FModel.getPlanes()) {
            if (plane.cardPool.contains(card)) {
                planes.add(plane);
            }
        }
        return planes;
    }
}
