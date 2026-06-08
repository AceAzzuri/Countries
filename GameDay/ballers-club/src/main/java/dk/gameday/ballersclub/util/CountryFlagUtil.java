package dk.gameday.ballersclub.util;

import java.util.Map;

public final class CountryFlagUtil {

    private static final Map<String, String> FLAGS = Map.ofEntries(
            Map.entry("argentina", "\uD83C\uDDE6\uD83C\uDDF7"),
            Map.entry("austria", "\uD83C\uDDE6\uD83C\uDDF9"),
            Map.entry("australia", "\uD83C\uDDE6\uD83C\uDDFA"),
            Map.entry("albania", "\uD83C\uDDE6\uD83C\uDDF1"),
            Map.entry("algeria", "\uD83C\uDDE9\uD83C\uDDFF"),
            Map.entry("belgium", "\uD83C\uDDE7\uD83C\uDDEA"),
            Map.entry("bolivia", "\uD83C\uDDE7\uD83C\uDDF4"),
            Map.entry("bosnia", "\uD83C\uDDE7\uD83C\uDDE6"),
            Map.entry("bosnia and herzegovina", "\uD83C\uDDE7\uD83C\uDDE6"),
            Map.entry("brazil", "\uD83C\uDDE7\uD83C\uDDF7"),
            Map.entry("cabo verde", "\uD83C\uDDE8\uD83C\uDDFB"),
            Map.entry("cameroon", "\uD83C\uDDE8\uD83C\uDDF2"),
            Map.entry("canada", "\uD83C\uDDE8\uD83C\uDDE6"),
            Map.entry("chile", "\uD83C\uDDE8\uD83C\uDDF1"),
            Map.entry("colombia", "\uD83C\uDDE8\uD83C\uDDF4"),
            Map.entry("congo dr", "\uD83C\uDDE8\uD83C\uDDE9"),
            Map.entry("costa rica", "\uD83C\uDDE8\uD83C\uDDF7"),
            Map.entry("croatia", "\uD83C\uDDED\uD83C\uDDF7"),
            Map.entry("curacao", "\uD83C\uDDE8\uD83C\uDDFC"),
            Map.entry("curaçao", "\uD83C\uDDE8\uD83C\uDDFC"),
            Map.entry("czech republic", "\uD83C\uDDE8\uD83C\uDDFF"),
            Map.entry("czechia", "\uD83C\uDDE8\uD83C\uDDFF"),
            Map.entry("denmark", "\uD83C\uDDE9\uD83C\uDDF0"),
            Map.entry("ecuador", "\uD83C\uDDEA\uD83C\uDDE8"),
            Map.entry("egypt", "\uD83C\uDDEA\uD83C\uDDEC"),
            Map.entry("england", "\uD83C\uDDEC\uD83C\uDDE7"),
            Map.entry("france", "\uD83C\uDDEB\uD83C\uDDF7"),
            Map.entry("germany", "\uD83C\uDDE9\uD83C\uDDEA"),
            Map.entry("ghana", "\uD83C\uDDEC\uD83C\uDDED"),
            Map.entry("greece", "\uD83C\uDDEC\uD83C\uDDF7"),
            Map.entry("haiti", "\uD83C\uDDED\uD83C\uDDF9"),
            Map.entry("hungary", "\uD83C\uDDED\uD83C\uDDFA"),
            Map.entry("iran", "\uD83C\uDDEE\uD83C\uDDF7"),
            Map.entry("ir iran", "\uD83C\uDDEE\uD83C\uDDF7"),
            Map.entry("iraq", "\uD83C\uDDEE\uD83C\uDDF6"),
            Map.entry("italy", "\uD83C\uDDEE\uD83C\uDDF9"),
            Map.entry("ivory coast", "\uD83C\uDDE8\uD83C\uDDEE"),
            Map.entry("cote d'ivoire", "\uD83C\uDDE8\uD83C\uDDEE"),
            Map.entry("côte d'ivoire", "\uD83C\uDDE8\uD83C\uDDEE"),
            Map.entry("japan", "\uD83C\uDDEF\uD83C\uDDF5"),
            Map.entry("jordan", "\uD83C\uDDEF\uD83C\uDDF4"),
            Map.entry("korea republic", "\uD83C\uDDF0\uD83C\uDDF7"),
            Map.entry("mali", "\uD83C\uDDF2\uD83C\uDDF1"),
            Map.entry("mexico", "\uD83C\uDDF2\uD83C\uDDFD"),
            Map.entry("morocco", "\uD83C\uDDF2\uD83C\uDDE6"),
            Map.entry("netherlands", "\uD83C\uDDF3\uD83C\uDDF1"),
            Map.entry("new zealand", "\uD83C\uDDF3\uD83C\uDDFF"),
            Map.entry("nigeria", "\uD83C\uDDF3\uD83C\uDDEC"),
            Map.entry("norway", "\uD83C\uDDF3\uD83C\uDDF4"),
            Map.entry("panama", "\uD83C\uDDF5\uD83C\uDDE6"),
            Map.entry("paraguay", "\uD83C\uDDF5\uD83C\uDDFE"),
            Map.entry("peru", "\uD83C\uDDF5\uD83C\uDDEA"),
            Map.entry("poland", "\uD83C\uDDF5\uD83C\uDDF1"),
            Map.entry("portugal", "\uD83C\uDDF5\uD83C\uDDF9"),
            Map.entry("qatar", "\uD83C\uDDF6\uD83C\uDDE6"),
            Map.entry("romania", "\uD83C\uDDF7\uD83C\uDDF4"),
            Map.entry("saudi arabia", "\uD83C\uDDF8\uD83C\uDDE6"),
            Map.entry("scotland", "\uD83C\uDDEC\uD83C\uDDE7"),
            Map.entry("senegal", "\uD83C\uDDF8\uD83C\uDDF3"),
            Map.entry("serbia", "\uD83C\uDDF7\uD83C\uDDF8"),
            Map.entry("slovakia", "\uD83C\uDDF8\uD83C\uDDF0"),
            Map.entry("slovenia", "\uD83C\uDDF8\uD83C\uDDEE"),
            Map.entry("south africa", "\uD83C\uDDFF\uD83C\uDDE6"),
            Map.entry("south korea", "\uD83C\uDDF0\uD83C\uDDF7"),
            Map.entry("spain", "\uD83C\uDDEA\uD83C\uDDF8"),
            Map.entry("sweden", "\uD83C\uDDF8\uD83C\uDDEA"),
            Map.entry("switzerland", "\uD83C\uDDE8\uD83C\uDDED"),
            Map.entry("tunisia", "\uD83C\uDDF9\uD83C\uDDF3"),
            Map.entry("turkey", "\uD83C\uDDF9\uD83C\uDDF7"),
            Map.entry("türkiye", "\uD83C\uDDF9\uD83C\uDDF7"),
            Map.entry("ukraine", "\uD83C\uDDFA\uD83C\uDDE6"),
            Map.entry("united states", "\uD83C\uDDFA\uD83C\uDDF8"),
            Map.entry("usa", "\uD83C\uDDFA\uD83C\uDDF8"),
            Map.entry("uruguay", "\uD83C\uDDFA\uD83C\uDDFE"),
            Map.entry("uzbekistan", "\uD83C\uDDFA\uD83C\uDDFF"),
            Map.entry("venezuela", "\uD83C\uDDFB\uD83C\uDDEA"),
            Map.entry("wales", "\uD83C\uDDEC\uD83C\uDDE7")
    );

    private CountryFlagUtil() {
    }

    public static String flagOrFallback(String teamName, String fallback) {
        if (teamName == null || teamName.isBlank()) {
            return fallback == null ? "" : fallback;
        }
        String flag = FLAGS.get(normalize(teamName));
        return flag == null ? fallback : flag;
    }

    private static String normalize(String teamName) {
        return teamName.trim().toLowerCase()
                .replace("  ", " ")
                .replace("u.s.a.", "usa")
                .replace("u.s.a", "usa");
    }
}
