package bg.sofia.uni.fmi.mjt.game.recommender;

import java.time.LocalDate;

public record Game(String name, String platform, LocalDate releaseDate, String summary, int metaScore, double userReview) {
}
