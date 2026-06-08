package dk.gameday.ballersclub.util;

import java.util.Map;

public final class CountryPlayerUtil {

    private static final String FALLBACK = "Trupforslag kommer snart | Officiel trup kan ændre sig op til kampstart";

    private static final Map<String, String> PLAYERS = Map.ofEntries(
            Map.entry("argentina", "Lionel Messi | Lautaro Martinez | Julian Alvarez | Alexis Mac Allister | Enzo Fernandez | Emiliano Martinez"),
            Map.entry("algeria", "Riyad Mahrez | Ismael Bennacer | Houssem Aouar | Amine Gouiri | Ramy Bensebaini | Anthony Mandrea"),
            Map.entry("australia", "Mathew Ryan | Harry Souttar | Jackson Irvine | Riley McGree | Craig Goodwin | Mitchell Duke"),
            Map.entry("austria", "David Alaba | Marcel Sabitzer | Christoph Baumgartner | Konrad Laimer | Marko Arnautovic | Michael Gregoritsch"),
            Map.entry("belgium", "Kevin De Bruyne | Romelu Lukaku | Jeremy Doku | Youri Tielemans | Amadou Onana | Thibaut Courtois"),
            Map.entry("bosnia and herzegovina", "Edin Dzeko | Miralem Pjanic | Sead Kolasinac | Ermedin Demirovic | Rade Krunic | Anel Ahmedhodzic"),
            Map.entry("brazil", "Vinicius Junior | Rodrygo | Neymar | Bruno Guimaraes | Marquinhos | Alisson"),
            Map.entry("cabo verde", "Ryan Mendes | Garry Rodrigues | Jamiro Monteiro | Kevin Pina | Logan Costa | Vozinha"),
            Map.entry("canada", "Alphonso Davies | Jonathan David | Tajon Buchanan | Stephen Eustaquio | Ismael Kone | Cyle Larin"),
            Map.entry("colombia", "Luis Diaz | James Rodriguez | Jhon Duran | Daniel Munoz | Jefferson Lerma | Davinson Sanchez"),
            Map.entry("congo dr", "Yoane Wissa | Cedric Bakambu | Chancel Mbemba | Arthur Masuaku | Samuel Moutoussamy | Lionel Mpasi"),
            Map.entry("croatia", "Luka Modric | Mateo Kovacic | Josko Gvardiol | Marcelo Brozovic | Andrej Kramaric | Dominik Livakovic"),
            Map.entry("côte d'ivoire", "Sebastien Haller | Simon Adingra | Franck Kessie | Ibrahim Sangare | Oumar Diakite | Yahia Fofana"),
            Map.entry("curacao", "Leandro Bacuna | Juninho Bacuna | Rangelo Janga | Vurnon Anita | Eloy Room | Kenji Gorre"),
            Map.entry("curaçao", "Leandro Bacuna | Juninho Bacuna | Rangelo Janga | Vurnon Anita | Eloy Room | Kenji Gorre"),
            Map.entry("czechia", "Patrik Schick | Tomas Soucek | Antonin Barak | Adam Hlozek | David Jurasek | Jindrich Stanek"),
            Map.entry("ecuador", "Moises Caicedo | Piero Hincapie | Pervis Estupinan | Kendry Paez | Enner Valencia | William Pacho"),
            Map.entry("egypt", "Mohamed Salah | Omar Marmoush | Mostafa Mohamed | Trezeguet | Mohamed Elneny | Mohamed Abdelmonem"),
            Map.entry("england", "Harry Kane | Jude Bellingham | Bukayo Saka | Phil Foden | Declan Rice | Jordan Pickford"),
            Map.entry("france", "Kylian Mbappe | Antoine Griezmann | Ousmane Dembele | Aurelien Tchouameni | William Saliba | Mike Maignan"),
            Map.entry("germany", "Jamal Musiala | Florian Wirtz | Kai Havertz | Joshua Kimmich | Antonio Rudiger | Marc-Andre ter Stegen"),
            Map.entry("ghana", "Mohammed Kudus | Thomas Partey | Inaki Williams | Jordan Ayew | Antoine Semenyo | Alexander Djiku"),
            Map.entry("haiti", "Duckens Nazon | Frantzdy Pierrot | Danley Jean Jacques | Fafa Picault | Carlens Arcus | Johny Placide"),
            Map.entry("ir iran", "Mehdi Taremi | Sardar Azmoun | Alireza Jahanbakhsh | Saman Ghoddos | Saeid Ezatolahi | Hossein Hosseini"),
            Map.entry("iraq", "Aymen Hussein | Ali Jasim | Zidane Iqbal | Osama Rashid | Amir Al-Ammari | Jalal Hassan"),
            Map.entry("japan", "Kaoru Mitoma | Takefusa Kubo | Wataru Endo | Ritsu Doan | Daichi Kamada | Zion Suzuki"),
            Map.entry("jordan", "Mousa Al-Taamari | Yazan Al-Naimat | Nizar Al-Rashdan | Noor Al-Rawabdeh | Ali Olwan | Yazeed Abulaila"),
            Map.entry("korea republic", "Son Heung-min | Kim Min-jae | Lee Kang-in | Hwang Hee-chan | Cho Gue-sung | Jo Hyeon-woo"),
            Map.entry("mexico", "Santiago Gimenez | Edson Alvarez | Hirving Lozano | Luis Chavez | Cesar Montes | Guillermo Ochoa"),
            Map.entry("morocco", "Achraf Hakimi | Hakim Ziyech | Sofyan Amrabat | Youssef En-Nesyri | Brahim Diaz | Yassine Bounou"),
            Map.entry("netherlands", "Virgil van Dijk | Xavi Simons | Cody Gakpo | Frenkie de Jong | Matthijs de Ligt | Bart Verbruggen"),
            Map.entry("new zealand", "Chris Wood | Liberato Cacace | Joe Bell | Sarpreet Singh | Marko Stamenic | Michael Boxall"),
            Map.entry("norway", "Erling Haaland | Martin Odegaard | Alexander Sorloth | Antonio Nusa | Sander Berge | Orjan Nyland"),
            Map.entry("panama", "Adalberto Carrasquilla | Jose Fajardo | Michael Murillo | Anibal Godoy | Eric Davis | Orlando Mosquera"),
            Map.entry("paraguay", "Miguel Almiron | Julio Enciso | Ramon Sosa | Diego Gomez | Gustavo Gomez | Carlos Coronel"),
            Map.entry("portugal", "Cristiano Ronaldo | Bruno Fernandes | Bernardo Silva | Rafael Leao | Joao Felix | Diogo Costa"),
            Map.entry("qatar", "Akram Afif | Almoez Ali | Hassan Al-Haydos | Boualem Khoukhi | Assim Madibo | Meshaal Barsham"),
            Map.entry("saudi arabia", "Salem Al-Dawsari | Firas Al-Buraikan | Saleh Al-Shehri | Saud Abdulhamid | Mohammed Kanno | Mohammed Al-Owais"),
            Map.entry("scotland", "Scott McTominay | Andy Robertson | John McGinn | Billy Gilmour | Kieran Tierney | Angus Gunn"),
            Map.entry("senegal", "Sadio Mane | Nicolas Jackson | Ismaila Sarr | Pape Matar Sarr | Kalidou Koulibaly | Edouard Mendy"),
            Map.entry("south africa", "Percy Tau | Teboho Mokoena | Themba Zwane | Ronwen Williams | Mothobi Mvala | Lyle Foster"),
            Map.entry("spain", "Lamine Yamal | Pedri | Nico Williams | Rodri | Dani Olmo | Unai Simon"),
            Map.entry("sweden", "Alexander Isak | Dejan Kulusevski | Viktor Gyokeres | Emil Forsberg | Anthony Elanga | Robin Olsen"),
            Map.entry("switzerland", "Granit Xhaka | Xherdan Shaqiri | Manuel Akanji | Breel Embolo | Noah Okafor | Yann Sommer"),
            Map.entry("tunisia", "Ellyes Skhiri | Hannibal Mejbri | Youssef Msakni | Ali Abdi | Aissa Laidouni | Bechir Ben Said"),
            Map.entry("türkiye", "Hakan Calhanoglu | Arda Guler | Kenan Yildiz | Orkun Kokcu | Ferdi Kadioglu | Ugurcan Cakir"),
            Map.entry("uruguay", "Federico Valverde | Darwin Nunez | Ronald Araujo | Manuel Ugarte | Rodrigo Bentancur | Sergio Rochet"),
            Map.entry("united states", "Christian Pulisic | Weston McKennie | Tyler Adams | Gio Reyna | Folarin Balogun | Matt Turner"),
            Map.entry("uzbekistan", "Eldor Shomurodov | Abbosbek Fayzullaev | Oston Urunov | Odiljon Hamrobekov | Abdukodir Khusanov | Utkir Yusupov")
    );

    private static final Map<String, String> TEAM_NOTES = Map.ofEntries(
            Map.entry("algeria", "FIFA: Gruppe J | Verdensrangliste 28 | VM-deltagelser 4"),
            Map.entry("argentina", "FIFA: Gruppe J | Verdensrangliste 3 | VM-deltagelser 18"),
            Map.entry("australia", "FIFA: Gruppe D | Verdensrangliste 27 | VM-deltagelser 6"),
            Map.entry("austria", "FIFA: Gruppe J | Verdensrangliste 24 | VM-deltagelser 7"),
            Map.entry("belgium", "FIFA: Gruppe G | Verdensrangliste 9 | VM-deltagelser 13"),
            Map.entry("bosnia and herzegovina", "FIFA: Gruppe B | Verdensrangliste 65 | VM-deltagelser 1"),
            Map.entry("brazil", "FIFA: Gruppe C | Verdensrangliste 6 | VM-deltagelser 22"),
            Map.entry("cabo verde", "FIFA: Gruppe H | Verdensrangliste 69 | VM-deltagelser 0"),
            Map.entry("canada", "FIFA: Gruppe B | Verdensrangliste 30 | VM-deltagelser 2"),
            Map.entry("colombia", "FIFA: Gruppe K | Verdensrangliste 13 | VM-deltagelser 6"),
            Map.entry("congo dr", "FIFA: Gruppe K | Verdensrangliste 46 | VM-deltagelser 1"),
            Map.entry("croatia", "FIFA: Gruppe L | Verdensrangliste 11 | VM-deltagelser 6"),
            Map.entry("côte d'ivoire", "FIFA: Gruppe E | Verdensrangliste 34 | VM-deltagelser 3"),
            Map.entry("curacao", "FIFA: Gruppe E | Verdensrangliste 82 | VM-deltagelser 0"),
            Map.entry("curaçao", "FIFA: Gruppe E | Verdensrangliste 82 | VM-deltagelser 0"),
            Map.entry("czechia", "FIFA: Gruppe A | Verdensrangliste 41 | VM-deltagelser 9"),
            Map.entry("ecuador", "FIFA: Gruppe E | Verdensrangliste 23 | VM-deltagelser 4"),
            Map.entry("egypt", "FIFA: Gruppe G | Verdensrangliste 29 | VM-deltagelser 3"),
            Map.entry("england", "FIFA: Gruppe L | Verdensrangliste 4 | VM-deltagelser 16"),
            Map.entry("france", "FIFA: Gruppe I | Verdensrangliste 1 | VM-deltagelser 16"),
            Map.entry("germany", "FIFA: Gruppe E | Verdensrangliste 10 | VM-deltagelser 20"),
            Map.entry("ghana", "FIFA: Gruppe L | Verdensrangliste 74 | VM-deltagelser 4"),
            Map.entry("haiti", "FIFA: Gruppe C | Verdensrangliste 83 | VM-deltagelser 1"),
            Map.entry("ir iran", "FIFA: Gruppe G | Verdensrangliste 21 | VM-deltagelser 6"),
            Map.entry("iraq", "FIFA: Gruppe I | Verdensrangliste 57 | VM-deltagelser 1"),
            Map.entry("japan", "FIFA: Gruppe F | Verdensrangliste 18 | VM-deltagelser 7"),
            Map.entry("jordan", "FIFA: Gruppe J | Verdensrangliste 63 | VM-deltagelser 0"),
            Map.entry("korea republic", "FIFA: Gruppe A | Verdensrangliste 25 | VM-deltagelser 10"),
            Map.entry("mexico", "FIFA: Gruppe A | Verdensrangliste 15 | VM-deltagelser 17"),
            Map.entry("morocco", "FIFA: Gruppe C | Verdensrangliste 8 | VM-deltagelser 6"),
            Map.entry("netherlands", "FIFA: Gruppe F | Verdensrangliste 7 | VM-deltagelser 10"),
            Map.entry("new zealand", "FIFA: Gruppe G | Verdensrangliste 85 | VM-deltagelser 2"),
            Map.entry("norway", "FIFA: Gruppe I | Verdensrangliste 31 | VM-deltagelser 3"),
            Map.entry("panama", "FIFA: Gruppe L | Verdensrangliste 33 | VM-deltagelser 1"),
            Map.entry("paraguay", "FIFA: Gruppe D | Verdensrangliste 40 | VM-deltagelser 8"),
            Map.entry("portugal", "FIFA: Gruppe K | Verdensrangliste 5 | VM-deltagelser 8"),
            Map.entry("qatar", "FIFA: Gruppe B | Verdensrangliste 55 | VM-deltagelser 1"),
            Map.entry("saudi arabia", "FIFA: Gruppe H | Verdensrangliste 61 | VM-deltagelser 6"),
            Map.entry("scotland", "FIFA: Gruppe C | Verdensrangliste 43 | VM-deltagelser 7"),
            Map.entry("senegal", "FIFA: Gruppe I | Verdensrangliste 14 | VM-deltagelser 3"),
            Map.entry("south africa", "FIFA: Gruppe A | Verdensrangliste 60 | VM-deltagelser 3"),
            Map.entry("spain", "FIFA: Gruppe H | Verdensrangliste 2 | VM-deltagelser 16"),
            Map.entry("sweden", "FIFA: Gruppe F | Verdensrangliste 38 | VM-deltagelser 12"),
            Map.entry("switzerland", "FIFA: Gruppe B | Verdensrangliste 19 | VM-deltagelser 12"),
            Map.entry("tunisia", "FIFA: Gruppe F | Verdensrangliste 44 | VM-deltagelser 6"),
            Map.entry("türkiye", "FIFA: Gruppe D | Verdensrangliste 22 | VM-deltagelser 2"),
            Map.entry("uruguay", "FIFA: Gruppe H | Verdensrangliste 17 | VM-deltagelser 14"),
            Map.entry("united states", "FIFA: Gruppe D | Verdensrangliste 16 | VM-deltagelser 11"),
            Map.entry("uzbekistan", "FIFA: Gruppe K | Verdensrangliste 50 | VM-deltagelser 0")
    );

    private CountryPlayerUtil() {
    }

    public static Map<String, String> playerSuggestions() {
        return PLAYERS;
    }

    public static Map<String, String> teamNotes() {
        return TEAM_NOTES;
    }

    public static String playersFor(String country) {
        if (country == null || country.isBlank()) {
            return FALLBACK;
        }
        return PLAYERS.getOrDefault(normalize(country), FALLBACK);
    }

    private static String normalize(String country) {
        return country.trim().toLowerCase()
                .replace("  ", " ")
                .replace("curacao", "curaçao");
    }
}
