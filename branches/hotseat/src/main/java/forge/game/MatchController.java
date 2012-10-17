package forge.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import forge.AllZone;
import forge.Singletons;
import forge.control.FControl;
import forge.control.input.InputMulligan;
import forge.deck.Deck;
import forge.error.ErrorViewer;
import forge.game.player.ComputerAIGeneral;
import forge.game.player.ComputerAIInput;
import forge.game.player.LobbyPlayer;
import forge.game.player.Player;
import forge.game.player.PlayerType;
import forge.game.zone.ZoneType;
import forge.gui.framework.EDocID;
import forge.gui.framework.SDisplayUtil;
import forge.gui.match.CMatchUI;
import forge.gui.match.controllers.CLog;
import forge.gui.match.controllers.CMessage;
import forge.gui.match.controllers.CStack;
import forge.util.Aggregates;

/**
 * TODO: Write javadoc for this type.
 * 
 */

public class MatchController {

    private final Map<LobbyPlayer, PlayerStartConditions> players = new HashMap<LobbyPlayer, PlayerStartConditions>();
    private GameType gameType = GameType.Constructed;

    private int gamesPerMatch = 3;
    private int gamesToWinMatch = 2;

    private GameState currentGame = null;

    private final List<GameOutcome> gamesPlayed = new ArrayList<GameOutcome>();
    private final List<GameOutcome> gamesPlayedRo;

    public MatchController() {
        gamesPlayedRo = Collections.unmodifiableList(gamesPlayed);
    }

    /**
     * Gets the games played.
     * 
     * @return the games played
     */
    public final List<GameOutcome> getPlayedGames() {
        return this.gamesPlayedRo;
    }

    /** @return int */
    public int getGamesPerMatch() {
        return gamesPerMatch;
    }

    /** @return int */
    public int getGamesToWinMatch() {
        return gamesToWinMatch;
    }

    /**
     * TODO: Write javadoc for this method.
     * 
     * @param game
     */
    public void addGamePlayed(GameState game) {
        game.setGameOver();

        // game.end(GameEndReason.AllOpponentsLost, computer.getName(), null);
    }

    /**
     * TODO: Write javadoc for this method.
     */
    public void startRound() {

        // Will this lose all the ordering?
        currentGame = Singletons.getModel().newGame(players.keySet());

        Map<Player, PlayerStartConditions> startConditions = new HashMap<Player, PlayerStartConditions>();
        for (Player p : currentGame.getPlayers())
            startConditions.put(p, players.get(p.getLobbyPlayer()));

        try {

            CMessage.SINGLETON_INSTANCE.updateGameInfo(this);
            GameNew.newGame(startConditions);

            Player computerPlayer = Aggregates.firstFieldEquals(currentGame.getPlayers(), Player.Accessors.FN_GET_TYPE,
                    PlayerType.COMPUTER);
            AllZone.getInputControl().setComputer(new ComputerAIInput(new ComputerAIGeneral(computerPlayer)));

            CMatchUI.SINGLETON_INSTANCE.initMatch(players.size(), 1);
            Singletons.getModel().getPreferences().actuateMatchPreferences();
            Singletons.getControl().changeState(FControl.MATCH_SCREEN);
            SDisplayUtil.showTab(EDocID.REPORT_LOG.getDoc());

            // set all observers
            CMessage.SINGLETON_INSTANCE.subscribe(currentGame);
            CLog.SINGLETON_INSTANCE.subscribe(currentGame);
            CStack.SINGLETON_INSTANCE.subscribe(currentGame);
            // per player observers?

            // Update observers
            AllZone.getStack().updateObservers();
            AllZone.getInputControl().updateObservers();
            AllZone.getGameLog().updateObservers();

            
            for( Player p : currentGame.getPlayers() ) {
                p.updateObservers();
                p.getZone(ZoneType.Hand).updateObservers();
            }

            CMatchUI.SINGLETON_INSTANCE.setCard(Singletons.getControl().getPlayer().getCardsIn(ZoneType.Hand).get(0));
            AllZone.getInputControl().setInput(new InputMulligan());            
            
        } catch (Exception e) {
            ErrorViewer.showError(e);
        }
        // bf.updateObservers();
        // player.updateObservers();
        // player.getZone(ZoneType.Hand).updateObservers();

    }

    /**
     * TODO: Write javadoc for this method.
     */
    public void initMatch(GameType type, Map<LobbyPlayer, PlayerStartConditions> map) {
        gamesPlayed.clear();
        players.clear();
        players.putAll(map);
        gameType = type;
    }

    /**
     * TODO: Write javadoc for this method.
     */
    public void replayRound() {
        gamesPlayed.remove(gamesPlayed.size() - 1);
    }

    /**
     * TODO: Write javadoc for this method.
     * 
     * @return
     */
    public GameType getGameType() {
        return gameType;
    }

    /**
     * TODO: Write javadoc for this method.
     * 
     * @return
     */
    public GameOutcome getLastGameOutcome() {
        return gamesPlayed.isEmpty() ? null : gamesPlayed.get(gamesPlayed.size() - 1);
    }

    public GameState getCurrentGame() {
        return currentGame;
    }

    /**
     * TODO: Write javadoc for this method.
     * 
     * @return
     */
    public boolean isMatchOver() {
        int[] victories = new int[players.size()];
        for (GameOutcome go : gamesPlayed) {
            LobbyPlayer winner = go.getWinner();
            int i = 0;
            for (LobbyPlayer p : players.keySet()) {
                if (p.equals(winner)) {
                    victories[i]++;
                    break; // can't have 2 winners per game
                }
                i++;
            }
        }

        for (int score : victories) {
            if (score >= gamesToWinMatch)
                return true;
        }
        return gamesPlayed.size() >= gamesPerMatch;
    }

    /**
     * TODO: Write javadoc for this method.
     * 
     * @param questPlayer
     * @return
     */
    public int getGamesWonBy(LobbyPlayer questPlayer) {
        int sum = 0;
        for (GameOutcome go : gamesPlayed) {
            if (go.getWinner().equals(questPlayer)) {
                sum++;
            }
        }
        return sum;
    }

    /**
     * TODO: Write javadoc for this method.
     * 
     * @param questPlayer
     * @return
     */
    public boolean isWonBy(LobbyPlayer questPlayer) {
        return getGamesWonBy(questPlayer) >= gamesToWinMatch;
    }

    /**
     * TODO: Write javadoc for this method.
     * 
     * @param lobbyPlayer
     * @return
     */
    public Deck getPlayersDeck(LobbyPlayer lobbyPlayer) {
        PlayerStartConditions cond = players.get(lobbyPlayer);
        return cond == null ? null : cond.getDeck();
    }
}
