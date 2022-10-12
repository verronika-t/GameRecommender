package bg.sofia.uni.fmi.mjt.game.recommender;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        try (Reader reader = new FileReader("all_games.csv", StandardCharsets.UTF_8)) {
            GameRecommender gameRecommender = new GameRecommender(reader);
            List<Game> g1 = gameRecommender.getAllGames();
            Map<String, Set<Game>> g2 = gameRecommender.getAllGamesByPlatform();
            String g3 = gameRecommender.getAllNamesOfGamesReleasedIn(2000);
            Game g4 = gameRecommender.getHighestUserRatedGameByPlatform("Nintendo 64");
            int g5 = gameRecommender.getYearsActive("Nintendo");

        } catch (IOException e) {

        }
    }
}
