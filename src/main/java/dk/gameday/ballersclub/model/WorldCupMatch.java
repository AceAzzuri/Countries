package dk.gameday.ballersclub.model;

import dk.gameday.ballersclub.util.CountryFlagUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Pattern;

@Entity
@Table(name = "matches")
public class WorldCupMatch {

    private static final DateTimeFormatter KICKOFF_FORMAT =
            DateTimeFormatter.ofPattern("d. MMM yyyy HH:mm", Locale.ENGLISH);
    private static final ZoneId DISPLAY_ZONE = ZoneId.of("Europe/Copenhagen");
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[\\s\\-/]+");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String roundLabel;

    @Column(nullable = false)
    private String homeTeam;

    @Column(nullable = false)
    private String awayTeam;

    @Column(nullable = false)
    private LocalDateTime kickoffAt;

    private String venue;
    private Integer homeScore;
    private Integer awayScore;
    private String advancingTeam;

    protected WorldCupMatch() {
    }

    public WorldCupMatch(String roundLabel, String homeTeam, String awayTeam, LocalDateTime kickoffAt, String venue) {
        this.roundLabel = roundLabel;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.kickoffAt = kickoffAt;
        this.venue = venue;
    }

    public boolean isPredictionOpen() {
        return !hasResult() && LocalDateTime.now(DISPLAY_ZONE).isBefore(kickoffAt);
    }

    public boolean isGroupMatch() {
        return roundLabel != null && roundLabel.startsWith("Group ");
    }

    public boolean isKnockoutMatch() {
        return !isGroupMatch();
    }

    public boolean isQuarterFinalOrLater() {
        return roundLabel != null && (
                roundLabel.equals("Quarter-final")
                        || roundLabel.equals("Semi-final")
                        || roundLabel.equals("Play-off for third place")
                        || roundLabel.equals("Final")
        );
    }

    public boolean hasResult() {
        return homeScore != null && awayScore != null;
    }

    public boolean isDraw() {
        return hasResult() && homeScore.equals(awayScore);
    }

    public String getKickoffLabel() {
        return kickoffAt.format(KICKOFF_FORMAT);
    }

    public String getStatusLabel() {
        if (hasResult()) {
            return "Færdig";
        }
        if (advancingTeam != null && !advancingTeam.isBlank()) {
            return "Afgjort";
        }
        return isPredictionOpen() ? "Åben" : "Låst";
    }

    public void updateFixture(String roundLabel, String homeTeam, String awayTeam, LocalDateTime kickoffAt, String venue) {
        this.roundLabel = roundLabel;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.kickoffAt = kickoffAt;
        this.venue = venue;
    }

    public void updateResult(Integer homeScore, Integer awayScore) {
        updateResult(homeScore, awayScore, null);
    }

    public void updateResult(Integer homeScore, Integer awayScore, String advancingTeam) {
        this.homeScore = homeScore;
        this.awayScore = awayScore;
        String trimmedAdvancingTeam = advancingTeam == null ? null : advancingTeam.trim();
        if (isGroupMatch()) {
            this.advancingTeam = null;
            return;
        }
        if (homeScore == null && awayScore == null) {
            this.advancingTeam = trimmedAdvancingTeam;
            return;
        }
        if (homeScore != null && awayScore != null && homeScore.equals(awayScore)) {
            this.advancingTeam = trimmedAdvancingTeam;
            return;
        }
        if (homeScore != null && awayScore != null) {
            this.advancingTeam = homeScore > awayScore ? homeTeam : awayTeam;
            return;
        }
        this.advancingTeam = null;
    }

    public void clearResult() {
        this.homeScore = null;
        this.awayScore = null;
        this.advancingTeam = null;
    }

    public String getHomeBadge() {
        return buildBadge(homeTeam);
    }

    public String getAwayBadge() {
        return buildBadge(awayTeam);
    }

    public String getHomeFlag() {
        return CountryFlagUtil.flagOrFallback(homeTeam, getHomeBadge());
    }

    public String getAwayFlag() {
        return CountryFlagUtil.flagOrFallback(awayTeam, getAwayBadge());
    }

    private static String buildBadge(String teamName) {
        String[] parts = SPLIT_PATTERN.split(teamName.trim());
        StringBuilder badge = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank() && Character.isLetter(part.charAt(0))) {
                badge.append(Character.toUpperCase(part.charAt(0)));
            }
            if (badge.length() == 2) {
                break;
            }
        }
        return badge.isEmpty() ? teamName.substring(0, Math.min(2, teamName.length())).toUpperCase(Locale.ROOT) : badge.toString();
    }

    public Long getId() {
        return id;
    }

    public String getRoundLabel() {
        return roundLabel;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public LocalDateTime getKickoffAt() {
        return kickoffAt;
    }

    public String getVenue() {
        return venue;
    }

    public Integer getHomeScore() {
        return homeScore;
    }

    public Integer getAwayScore() {
        return awayScore;
    }

    public String getAdvancingTeam() {
        return advancingTeam;
    }

    public boolean hasAdvancingTeam() {
        return advancingTeam != null && !advancingTeam.isBlank();
    }
}
