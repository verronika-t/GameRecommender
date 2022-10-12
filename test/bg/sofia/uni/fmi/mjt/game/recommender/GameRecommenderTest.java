package bg.sofia.uni.fmi.mjt.game.recommender;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class GameRecommenderTest {

    private final String[] platforms = {"Nintendo 64", "PlayStation", "PlayStation 3", "Dreamcast", "Xbox 360", "Wii", "Xbox One"};
    private GameRecommender gameRecommender;
    private List<Game> listForTests;

    @Before
    public void init() {
        try {
            this.gameRecommender = new GameRecommender(new FileReader("test_games.csv", StandardCharsets.UTF_8));
        } catch (IOException e) {
        }
        listForTests = new ArrayList<>();
        openFile("test_games.csv");


    }

    private void openFile(String path) {
        try(BufferedReader reader = new BufferedReader(new FileReader("test_games.csv", StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                Game game = parseGame(line);
                this.listForTests.add(game);
            }
        } catch (IOException e) {

        }
    }

    private Game parseGame(String line) {
        String[] datas = line.split(",");

        if (datas.length != 6) {
            return null;
        }

        String name = datas[0];
        String platform = datas[1];

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
        LocalDate releaseDate;
        try {
            releaseDate = LocalDate.parse(datas[2], formatter);
        } catch (Exception e) {
            throw new IllegalArgumentException("The date is incorrect!");
        }

        String summary = datas[3];
        int metaScore = Integer.parseInt(datas[4]);
        double userReview = Double.parseDouble(datas[5]);

        return new Game(name, platform, releaseDate, summary, metaScore, userReview);

    }

    @Test
    public void testGetAllGames() {
        List<Game> gameList = gameRecommender.getAllGames();
        Assert.assertEquals(10, gameList.size());
        for (int i = 0; i < gameList.size(); i++) {
            Assert.assertEquals(gameList.get(i), listForTests.get(i));
        }
    }

    @Test
    public void testGetAllGamesIfFileIsEmpty() {
        try {
            this.gameRecommender = new GameRecommender(new FileReader("test_empty.csv", StandardCharsets.UTF_8));
            Assert.assertEquals(gameRecommender.getAllGames().size(), 0);
        } catch (IOException e) {

        }
    }

    @Test
    public void testGetGamesReleasedAfter() {
        List<Game> getGames = gameRecommender.getGamesReleasedAfter(LocalDate.of(2014, 11, 10));
        Assert.assertEquals(getGames.size(), 2);
        Assert.assertEquals("Red Dead Redemption 2", getGames.get(0).name());
        Assert.assertEquals("Grand Theft Auto V", getGames.get(1).name());
    }

    @Test
    public void testGetGamesReleasedAfterIsNoneGameCoincides() {
        List<Game> getGames = gameRecommender.getGamesReleasedAfter(LocalDate.now());
        Assert.assertEquals(0, getGames.size());
    }

    @Test
    public void testGetTopNUserRatedGame() {
        List<Game> getGames = gameRecommender.getTopNUserRatedGames(3);
        Assert.assertEquals(3, getGames.size());
        Assert.assertTrue(getGames.get(0).userReview() >= getGames.get(1).userReview());
        Assert.assertTrue(getGames.get(1).userReview() >= getGames.get(2).userReview());
    }

    @Test
    public void testGetTopNUserRatedGameIfNIsBiggerThanSize() {
        List<Game> getGame = gameRecommender.getTopNUserRatedGames(11);
        Assert.assertEquals(10, getGame.size());
    }

    @Test
    public void testGetTopNUserRatedGameIfNIsEqualToSize() {
        List<Game> getGame = gameRecommender.getTopNUserRatedGames(10);
        Assert.assertEquals(10, getGame.size());
    }

    @Test
    public void testGetTopNUserRatedGameIfNIsNegative() {
        Assert.assertThrows(IllegalArgumentException.class, () ->gameRecommender.getTopNUserRatedGames(-3));
    }

    @Test
    public void testGetTopNUserRatedGameIfNIsZero() {
        Assert.assertThrows(IllegalArgumentException.class, () ->gameRecommender.getTopNUserRatedGames(0));
    }

    @Test
    public void testgetYearsWithTopScoringGames() {
        Assert.assertEquals(1, gameRecommender.getYearsWithTopScoringGames(99).size());
    }

    @Test
    public void testgetYearsWithTopScoringGamesIfNoSuchYear() {
        Assert.assertEquals(0, gameRecommender.getYearsWithTopScoringGames(100).size());
    }

    @Test
    public void testgetAllNamesOfGamesReleasedIn() {
        Assert.assertEquals("The Legend of Zelda: Ocarina of Time", gameRecommender.getAllNamesOfGamesReleasedIn(1998));
    }

    @Test
    public void testgetAllNamesOfGamesReleasedInIfNoSuchGame() {
        Assert.assertTrue(gameRecommender.getAllNamesOfGamesReleasedIn(2022).isEmpty() || gameRecommender.getAllNamesOfGamesReleasedIn(2022).isBlank() );
    }

    @Test
    public void testgetHighestUserRatedGameByPlatform() {
        Assert.assertEquals("Grand Theft Auto V", gameRecommender.getHighestUserRatedGameByPlatform("PlayStation 3").name());
    }

    @Test
    public void testgetHighestUserRatedGameByPlatformIfNull() {
       Assert.assertThrows(NoSuchElementException.class, () -> gameRecommender.getHighestUserRatedGameByPlatform(null));
    }

    @Test
    public void testgetHighestUserRatedGameByPlatformIfEmpty() {
        Assert.assertThrows(NoSuchElementException.class, () -> gameRecommender.getHighestUserRatedGameByPlatform(""));
    }

    @Test
    public void testgetHighestUserRatedGameByPlatformNoSuchGame() {
        Assert.assertThrows(NoSuchElementException.class, () -> gameRecommender.getHighestUserRatedGameByPlatform("haha"));
    }

    @Test
    public void testgetAllGamesByPlatform() {
        Map<String, Set<Game>> games = gameRecommender.getAllGamesByPlatform();
        Assert.assertEquals(7, games.size());
        for (String platform : platforms) {
            Assert.assertTrue(games.containsKey(platform));
        }
    }

    @Test
    public void testgetYearsActive() {
        Assert.assertEquals(4, gameRecommender.getYearsActive("Xbox One"));
    }

    @Test
    public void testgetYearsActiveIfPlatformIsNull() {
        Assert.assertEquals(0, gameRecommender.getYearsActive(null));
    }

    @Test
    public void testgetYearsActiveIfPlatformIsEmpty() {
        Assert.assertEquals(0, gameRecommender.getYearsActive(""));
    }

    @Test
    public void testgetYearsActiveIfItsActiveOneYear() {
        Assert.assertEquals(1, gameRecommender.getYearsActive("PlayStation"));
    }

    @Test
    public void testgetGamesSimilarTo() {
        String[] args = {"Gerudo", "boy", "Ganondorf"};
        List<Game> games = gameRecommender.getGamesSimilarTo(args);
        Assert.assertEquals(1, games.size());
       Assert.assertEquals("The Legend of Zelda: Ocarina of Time", games.get(0).name());
    }

    @Test
    public void testgetGamesSimilarToIfNoSuchGame() {
        String[] args = {"hi", "bye"};
        Assert.assertEquals(0, gameRecommender.getGamesSimilarTo(args).size());
    }

    @Test
    public void testgetGamesSimilarToIfThereIsNullInArgs() {
        String[] args = {null, "boy", "Ganondorf"};
        Assert.assertNull(gameRecommender.getGamesSimilarTo(args));
    }

    @Test
    public void testgetGamesSimilarToIfThereIsBlankInArgs() {
        String[] args = {"", "boy", "Ganondorf"};
        Assert.assertNull(gameRecommender.getGamesSimilarTo(args));
    }

}
