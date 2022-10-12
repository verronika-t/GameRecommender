package bg.sofia.uni.fmi.mjt.game.recommender;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toSet;

public class GameRecommender {

    private List<Game> games;

    /**
     * Loads the dataset from the given {@code dataInput} stream.
     *
     * @param dataInput java.io.Reader input stream from which the dataset can be read
     */
    public GameRecommender(Reader dataInput) {
        this.games = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(dataInput)) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                Game game = parseGame(line);
                games.add(game);
            }
        } catch (IOException e) {

        }
    }

    /**
     * @return All games from the dataset as an unmodifiable copy.
     * If the dataset is empty, return an empty collection
     */
    public List<Game> getAllGames() {
        return Collections.unmodifiableList(games);
    }

    /**
     * Returns all games in the dataset released after the specified {@code date} as an unmodifiable list.
     * If no games have been released after the given date, returns an empty list.
     *
     * @param date
     * @return a list of all games released after {@code date}, in an undefined order.
     */
    public List<Game> getGamesReleasedAfter(LocalDate date) {
         return this.games
                .stream()
                .filter(g -> g.releaseDate().isAfter(date))
                .collect(Collectors.toUnmodifiableList());
    }

    /**
     * Returns the top {@code n} games by user review score.
     *
     * @param n maximum number of games to return
     *          If {@code n} exceeds the total number of games in the dataset, return all games.
     * @return unmodifiable list of the games sorted by user review score in descending order
     * @throws IllegalArgumentException in case {@code n} is a negative number.
     */
    public List<Game> getTopNUserRatedGames(int n) {
        if (n <= 0) {
            throw new IllegalArgumentException("N cannot be negative!");
        }
        if (n >= games.size()) {
            return games.stream()
                    .sorted((g1, g2) -> Double.compare(g2.userReview(), g1.userReview()))
                    .toList();
        }
        return games.stream()
                .sorted( (g1, g2) -> Double.compare(g2.userReview(), g1.userReview()))
                .limit(n)
                .toList();
    }

    /**
     * Returns a list (without repetitions) of all years in which at least one game with meta score
     * {@code minimalScore} or higher has been released. The order of the years in the result is undefined.
     * If there are no such years, return an empty list.
     *
     * @param minimalScore
     * @return the years when a game with at least {@code minimalScore} meta score has been released
     */
    public List<Integer> getYearsWithTopScoringGames(int minimalScore) {
         return games.stream()
                .filter(g -> g.metaScore() >= minimalScore)
                .map(Game::releaseDate)
                .map(LocalDate::getYear)
                .distinct()
                .toList();
    }

    /**
     * Returns the names of all games in the dataset released in {@code year} as a comma-separated String.
     * Each comma in the result must be followed by a space. The order of the game names in the result is undefined.
     * If no games have been released in the given year, returns an empty String.
     *
     * @param year
     * @return a comma-separated String containing all game names released in {@code year}
     */
    public String getAllNamesOfGamesReleasedIn(int year) {
        return games.stream()
                .filter(g -> g.releaseDate().getYear() == year)
                .map(Game::name)
                .collect(Collectors.joining(", "));
    }

    /**
     * Returns the game for the specified {@code platform} with the highest user review score.
     *
     * @param platform the name of the platform
     * @return the game for the specified {@code platform} with the highest review score
     * @throws NoSuchElementException if there is no game at all released for the specified platform,
     *                                or if {@code platform} is null or an empty String.
     */
    public Game getHighestUserRatedGameByPlatform(String platform) {
        if (platform == null || platform.isEmpty()) {
            throw new NoSuchElementException("Platform is invalid!");
        }

        return games.stream()
                .filter(g -> g.platform().equals(platform))
                .sorted((g1, g2) -> Double.compare(g2.userReview(), g1.userReview()))
                .findFirst()
                .orElseThrow(NoSuchElementException::new);
    }

    /**
     * Returns all games by platform. The result should map a platform name to the set of all games
     * released for this platform.
     *
     * @return all games by platform
     */
    public Map<String, Set<Game>> getAllGamesByPlatform() {
        return games.stream()
                .collect(Collectors.groupingBy(Game::platform,toSet()));
    }

    /**
     * Returns the number of years a game platform has been live.
     * The lifecycle of a platform is assumed to start and end with the release year of the oldest and newest game
     * released for this platform (the exact date is not significant).
     * In case all games for a given platform have been released in a single year, return 1.
     * In case {@code platform} is null, blank or unknown in the dataset, return 0.
     *
     * @return the number of years a game platform has been live
     */
    public int getYearsActive(String platform) {
        if (platform == null || platform.isBlank()) {
            return 0;
        }

        List<Game> platformGames = games.stream()
                .filter(g -> g.platform().equals(platform))
                .sorted(Comparator.comparing(Game::releaseDate))
                .toList();

        int startYear = platformGames.get(0).releaseDate().getYear();
        int endYear = platformGames.get(platformGames.size() - 1).releaseDate().getYear();

        if (startYear == endYear) {
            return 1;
        } else {
            return endYear - startYear;
        }
    }

    /**
     * Returns the games whose summary contains all {@code keywords} specified, as an unmodifiable list.
     * <p>
     * If there are no such games, return an empty list.
     * In case no keywords are specified, or any of the keywords is null or blank, the result is undefined.
     *
     * @param keywords the keywords to search for in the game summary
     * @return the games whose summary contains the specified keywords
     */
    public List<Game> getGamesSimilarTo(String... keywords) {

        if (Arrays.stream(keywords).allMatch(k -> k != null && !k.isBlank())) {

            return games.stream()
                    .filter(g -> containsAllWords(g.summary(), keywords))
                    .toList();
        } else {
            return null;
        }
    }

    private static boolean containsAllWords(String word, String ...keywords) {
        for (String k : keywords)
            if (!word.contains(k)) return false;
        return true;
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
}