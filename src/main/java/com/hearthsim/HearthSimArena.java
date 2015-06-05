package com.hearthsim;

import com.hearthsim.card.Deck;
import com.hearthsim.card.minion.Hero;
import com.hearthsim.exception.HSException;
import com.hearthsim.exception.HSInvalidParamFileException;
import com.hearthsim.exception.HSParamNotFoundException;
import com.hearthsim.io.DeckListFile;
import com.hearthsim.io.ParamFile;
import com.hearthsim.player.playercontroller.ArtificialPlayer;
import com.hearthsim.player.playercontroller.BruteForceSearchAI;
import com.hearthsim.results.GameResult;
import com.hearthsim.results.GameSimpleRecord;
import com.hearthsim.util.ThreadQueue;

import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;

import java.util.concurrent.ConcurrentHashMap;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * A Game setup to play a constructed deck vs constructed deck games.
 *
 * The constructed decks must be specified in a DeckListFile.
 */
public class HearthSimArena extends HearthSimBase {

    private String deckListFilePath_;
    private String arenaResultsPath_;
    private String aiParamFilePath_;

    private int numDecks_;

    private ConcurrentHashMap<Integer, WinLoss> metrics;
    private ArrayList<Integer> available;

    private ThreadQueue tQueue;
    private Writer writer;
    private Integer gameID;

    HearthSimArena(Path setupFilePath) throws IOException, HSInvalidParamFileException, HSParamNotFoundException {
        super();
        ParamFile masterParam = new ParamFile(setupFilePath);

        rootPath_ = setupFilePath.getParent();
        numThreads_ = masterParam.getInt("num_threads", 1);
        gameResultFileName_ = masterParam.getString("output_file", "gameres.txt");


        deckListFilePath_ = masterParam.getString("deckListFilePath");
        aiParamFilePath_ = masterParam.getString("aiParamFilePath");
        arenaResultsPath_ = masterParam.getString("arena_output", "arenares.txt");

        numDecks_ = masterParam.getInt("numDecks");
        metrics = new ConcurrentHashMap(numDecks_, 1.5f, 8);
        available = new ArrayList(numDecks_);

        for (int i = 0; i < numDecks_; i++) {
            metrics.put(i, new WinLoss());
            available.add(i);
        }
    }

    @Override
    public GameResult runSingleGame(int gid) throws IOException, HSException {
        int p0;
        int p1;
        synchronized(available) {
            if (available.size() < 2) {
                return null;
            }
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            p0 = available.remove(rng.nextInt(0, available.size()));
            p1 = available.remove(rng.nextInt(0, available.size()));

        }

        Path path0 = FileSystems.getDefault().getPath(rootPath_.toString(), String.format("%s%d.hsdeck", deckListFilePath_, p0));
        Path path1 = FileSystems.getDefault().getPath(rootPath_.toString(), String.format("%s%d.hsdeck", deckListFilePath_, p1));

        DeckListFile deckList0 = new DeckListFile(path0);
        DeckListFile deckList1 = new DeckListFile(path1);

        Path aiPath0 = FileSystems.getDefault().getPath(rootPath_.toString(), String.format("%s%d.hsai", aiParamFilePath_, p0));
        Path aiPath1 = FileSystems.getDefault().getPath(rootPath_.toString(), String.format("%s%d.hsai", aiParamFilePath_, p1));

        Hero hero0 = deckList0.getHero();
        Hero hero1 = deckList1.getHero();

        Deck deck0 = deckList0.getDeck();
        Deck deck1 = deckList1.getDeck();

        ArtificialPlayer ai0 = new BruteForceSearchAI(aiPath0);
        ArtificialPlayer ai1 = new BruteForceSearchAI(aiPath1);

        GameResult result;
        try {
           result =  super.runSingleGame(ai0, hero0, deck0, ai1, hero1, deck1, gid % 2);
        } catch (Exception e) {
            log.info("Exception was thrown in game, but it was caught");
            ThreadLocalRandom rng = ThreadLocalRandom.current();
            int winner = rng.nextInt(0,2);
            result = new GameResult(gid % 2, winner, 0, new GameSimpleRecord());
        }

        if (result.winnerPlayerIndex_ == 0) {
            metrics.get(p0).wins += 1;
            metrics.get(p1).losses += 1;
        } else {
            metrics.get(p0).losses += 1;
            metrics.get(p1).wins += 1;
        }

        synchronized(available) {
            WinLoss record0 = metrics.get(p0);
            WinLoss record1 = metrics.get(p1);

            if (!record0.isDone()) {
                available.add(p0);
            }

            if (!record1.isDone()) {
                available.add(p1);
            }

            if (available.size() >= 2) {
                synchronized(this.gameID) {
                    GameThread gThread = new GameThread(gameID, writer);
                    tQueue.queue(gThread);
                    gameID += 1;
                }
            }
        }

        return result;
    }

    @Override
    public void run() throws IOException, InterruptedException {
        long simStartTime = System.currentTimeMillis();

        Path outputFilePath = FileSystems.getDefault().getPath(rootPath_.toString(), gameResultFileName_);
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFilePath.toString()), "utf-8"));

        tQueue = new ThreadQueue(numThreads_);
        for (gameID = 0; gameID < numDecks_/2; ++gameID) {
            GameThread gThread = new GameThread(gameID, writer);
            tQueue.queue(gThread);
        }

        tQueue.runQueue();
        writer.close();

        long simEndTime = System.currentTimeMillis();
        double  simDeltaTimeSeconds = (simEndTime - simStartTime) / 1000.0;
        String prettyDeltaTimeSeconds = String.format("%.2f", simDeltaTimeSeconds);
        double secondsPerGame = simDeltaTimeSeconds / gameID;
        String prettySecondsPerGame = String.format("%.2f", secondsPerGame);

        log.info("completed simulation of {} games in {} seconds on {} thread(s)", gameID, prettyDeltaTimeSeconds, numThreads_);
        log.info("average time per game: {} seconds", prettySecondsPerGame);

        Path arenaPath = FileSystems.getDefault().getPath(rootPath_.toString(), arenaResultsPath_);
        BufferedWriter arenaResults =  new BufferedWriter(new OutputStreamWriter(new FileOutputStream(arenaPath.toString()), "utf-8"));

        for (int key : metrics.keySet()) {
            String outStr = String.format("%d,%d,%d\n", key, metrics.get(key).wins, metrics.get(key).losses);
            arenaResults.write(outStr, 0, outStr.length());
        }

        arenaResults.close();
    }

    private class WinLoss {
        public int wins;
        public int losses;

        public boolean isDone() {
            return wins >= 12 || losses >= 3;
        }
    }
}
