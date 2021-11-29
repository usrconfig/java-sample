package com.seagame.ext.config.game;

import com.seagame.ext.util.SourceFileHelper;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * @author LamHM
 */
@Getter
public class GameConfig {
    private static final String PLAYER_EXP = "resources/acc_exp.txt";
    private static GameConfig instance;
    private Map<Integer, Integer> accLevels;


    public static GameConfig getInstance() {
        if (instance == null)
            instance = new GameConfig();
        return instance;
    }


    private GameConfig() {
        loadAccLevels();
    }



    private void loadAccLevels() {
        accLevels = new ConcurrentHashMap<>();
        try {
            Stream<String> stream = Files.lines(Paths.get(PLAYER_EXP));
            stream.map(s -> s.split("\t")).forEach(strings -> accLevels.put(Integer.parseInt(strings[0]), Integer.parseInt(strings[1])));
            stream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public int getAccLevelExp(int level) {
        return accLevels.getOrDefault(level, accLevels.get(-1));
    }


    public void writeToJsonFile() {
        try {
            SourceFileHelper.exportJsonFile(
                    accLevels,
                    "acc_level.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException {
        GameConfig.getInstance().writeToJsonFile();
    }
}
