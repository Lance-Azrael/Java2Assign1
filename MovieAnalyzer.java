import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class MovieAnalyzer {
    //    Stream<Movie> data;
    Supplier<Stream<Movie>> streamSupplier;

    public MovieAnalyzer(String dataset_path) throws IOException {
//        data = Files.lines(Paths.get(dataset_path)).skip(1)
//                .map(l -> l.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1))
//                .map(a -> new Movie(a[1], Integer.parseInt(a[2]), a[3].isEmpty() ? "" : a[3], a[4], a[5], Double.parseDouble(a[6]), a[7], a[8].isEmpty() ? -1 : Integer.parseInt(a[8]), a[9], a[10], a[11], a[12], a[13], Integer.parseInt(a[14]), a[15].isEmpty() ? "" : a[15]));
//        data.forEach(System.out::println);
        streamSupplier = () -> {
            try {
                return Files.lines(Paths.get(dataset_path)).skip(1)
                        .map(l -> l.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1))
                        .map(a -> new Movie(a[1], Integer.parseInt(a[2]), a[3].isEmpty() ? "" : a[3], a[4], a[5], Float.parseFloat(a[6]), a[7], a[8].isEmpty() ? -1 : Integer.parseInt(a[8]), a[9], a[10], a[11], a[12], a[13], Integer.parseInt(a[14]), a[15].isEmpty() ? "" : a[15]));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };
    }

    public Map<Integer, Integer> getMovieCountByYear() {
        Map<Integer, Integer> map = new TreeMap<>(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o2 - o1;
            }
        });
        streamSupplier.get().forEach(m -> {
            if (map.containsKey(m.getReleased_Year())) {
                map.put(m.getReleased_Year(), map.get(m.getReleased_Year()) + 1);
            } else {
                map.put(m.getReleased_Year(), 1);
            }
        });
        return map;
    }

    public Map<String, Integer> getMovieCountByGenre() {
        Map<String, Integer> map = new TreeMap<>();
        streamSupplier.get().forEach(m -> {
            String[] genres = m.getGenre().replace("\"", "").split(", ");
            for (String genre : genres) {
                if (map.containsKey(genre)) {
                    map.put(genre, map.get(genre) + 1);
                } else {
                    map.put(genre, 1);
                }
            }
        });
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });
        Map<String, Integer> map2 = list.stream().collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
        return map2;
    }

    public Map<List<String>, Integer> getCoStarCount() {
        Map<List<String>, Integer> map = new LinkedHashMap<>(); //
        streamSupplier.get().forEach(m -> {
            String[] stars = {m.getStar1(), m.getStar2(), m.getStar3(), m.getStar4()};
            Arrays.sort(stars);
            for (int i = 0; i < stars.length; i++) {
                for (int j = i + 1; j < stars.length; j++) {
                    List<String> list = new ArrayList<>();
                    list.add(stars[i]);
                    list.add(stars[j]);
                    if (map.containsKey(list)) {
                        map.put(list, map.get(list) + 1);
                    } else {
                        map.put(list, 1);
                    }
                }
            }
        });
        List<Map.Entry<List<String>, Integer>> list = new ArrayList<>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<List<String>, Integer>>() {
            @Override
            public int compare(Map.Entry<List<String>, Integer> o1, Map.Entry<List<String>, Integer> o2) {
                return o2.getValue() - o1.getValue();
            }
        });
        Map<List<String>, Integer> map2 = list.stream().collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), Map::putAll);
        return map2;
    }

    public List<String> getTopMovies(int top_k, String by) {
        List<String> list = new ArrayList<>();
        if (by.equals("runtime")) {
            streamSupplier.get().sorted(new Comparator<Movie>() {
                @Override
                public int compare(Movie o1, Movie o2) {
                    if (o1.getRuntime().equals(o2.getRuntime())) {
                        return (o1.getSeries_Title().charAt(0) == '"' ? o1.getSeries_Title().substring(1, o1.getSeries_Title().length() - 1) : o1.getSeries_Title()).compareTo((o2.getSeries_Title().charAt(0) == '"' ? o2.getSeries_Title().substring(1, o2.getSeries_Title().length() - 1) : o2.getSeries_Title()));
                    }
                    return Integer.parseInt(o2.getRuntime().split(" ")[0]) - Integer.parseInt(o1.getRuntime().split(" ")[0]);
                }
            }).limit(top_k).forEach(m -> list.add(m.getSeries_Title().charAt(0) == '"' ? m.getSeries_Title().substring(1, m.getSeries_Title().length() - 1) : m.getSeries_Title()));
        } else if (by.equals("overview")) {
            streamSupplier.get().sorted(new Comparator<Movie>() {
                @Override
                public int compare(Movie o1, Movie o2) {
                    if ((o2.getOverview().charAt(0) == '"' ? o2.getOverview().substring(1, o2.getOverview().length() - 1) : o2.getOverview()).length() == (o1.getOverview().charAt(0) == '"' ? o1.getOverview().substring(1, o1.getOverview().length() - 1) : o1.getOverview()).length()) {
                        return (o1.getSeries_Title().charAt(0) == '"' ? o1.getSeries_Title().substring(1, o1.getSeries_Title().length() - 1) : o1.getSeries_Title()).compareTo((o2.getSeries_Title().charAt(0) == '"' ? o2.getSeries_Title().substring(1, o2.getSeries_Title().length() - 1) : o2.getSeries_Title()));
                    }
                    return (o2.getOverview().charAt(0) == '"' ? o2.getOverview().substring(1, o2.getOverview().length() - 1) : o2.getOverview()).length() - (o1.getOverview().charAt(0) == '"' ? o1.getOverview().substring(1, o1.getOverview().length() - 1) : o1.getOverview()).length();
                }
            }).limit(top_k).forEach(m -> list.add(m.getSeries_Title().charAt(0) == '"' ? m.getSeries_Title().substring(1, m.getSeries_Title().length() - 1) : m.getSeries_Title()));
        }
        return list;
    }

    public List<String> getTopStars(int top_k, String by) {
        List<String> list = new ArrayList<>();
        Map<String, Double> map = new TreeMap<>(); // gross
        Map<String, Double> map2 = new TreeMap<>(); // rating
        Map<String, Double> rating = new TreeMap<>();
        Map<String, Double> rating2 = new TreeMap<>();
        Map<String, Double> gross = new TreeMap<>();
        streamSupplier.get().forEach(m -> {
            String[] stars = {m.getStar1(), m.getStar2(), m.getStar3(), m.getStar4()};
            for (int i = 0; i < stars.length; i++) {
                if (map.containsKey(stars[i])) {
                    if (!Objects.equals(m.getGross(), "")) {
                        map.put(stars[i], map.get(stars[i]) + 1);
                        double temp = Double.parseDouble(m.getGross().substring(1, m.getGross().length() - 1).replace(",", ""));
                        gross.put(stars[i], gross.get(stars[i]) + temp);
                    }
                } else {
                    if (!Objects.equals(m.getGross(), "")) {
                        map.put(stars[i], 1.0);
                        double temp = Double.parseDouble(m.getGross().substring(1, m.getGross().length() - 1).replace(",", ""));
                        gross.put(stars[i], temp);
                    }
                }
                if (map2.containsKey(stars[i])) {
                    map2.put(stars[i], map2.get(stars[i]) + 1);
                    rating.put(stars[i], rating.get(stars[i]) + m.getIMDB_Rating());

                } else {
                    map2.put(stars[i], 1.0);
                    rating.put(stars[i], (double) m.getIMDB_Rating());
                }
            }
        });
        rating.forEach((k, v) -> rating2.put(k, (double) v / map2.get(k)));
        gross.forEach((k, v) -> gross.put(k, v / map.get(k)));
        if (by.equals("rating")) {
            List<Map.Entry<String, Double>> list2 = new ArrayList<>(rating2.entrySet());
            Collections.sort(list2, new Comparator<Map.Entry<String, Double>>() {
                @Override
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    if (Objects.equals(o2.getValue(), o1.getValue())) {
                        return o1.getKey().compareTo(o2.getKey());
                    }
                    return o2.getValue() - o1.getValue() > 0 ? 1 : -1;
                }
            });
            for (int i = 0; i < top_k; i++) {
                list.add(list2.get(i).getKey());
            }
        } else if (by.equals("gross")) {
            List<Map.Entry<String, Double>> list2 = new ArrayList<>(gross.entrySet());
            Collections.sort(list2, new Comparator<Map.Entry<String, Double>>() {
                @Override
                public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                    if (Objects.equals(o2.getValue(), o1.getValue())) {
                        return o1.getKey().compareTo(o2.getKey());
                    }
                    return o2.getValue() - o1.getValue() > 0 ? 1 : -1;
                }
            });
            for (int i = 0; i < top_k; i++) {
                list.add(list2.get(i).getKey());
                System.out.println(list2.get(i).getValue());
            }
        }
        return list;
    }

    public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
        List<String> list = new ArrayList<>();
        streamSupplier.get().filter(m -> m.getGenre().contains(genre) && m.getIMDB_Rating() >= min_rating && Integer.parseInt(m.getRuntime().split(" ")[0]) <= max_runtime).forEach(m -> list.add(m.getSeries_Title().charAt(0) == '"' ? m.getSeries_Title().substring(1, m.getSeries_Title().length() - 1) : m.getSeries_Title()));
        list.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        return list;
    }

    public static void main(String[] args) throws IOException {
        MovieAnalyzer analyzer = new MovieAnalyzer("resources/imdb_top_500.csv");
//        System.out.println(analyzer.getMovieCountByYear());
//        System.out.println(analyzer.getMovieCountByGenre());
//        System.out.println(analyzer.getCoStarCount());
    }

}

class Movie {
    private String Series_Title;
    private int Released_Year;
    private String Certificate; // null
    private String Runtime;
    private String Genre;
    private float IMDB_Rating;
    private String Overview;
    private int Meta_score; // null
    private String Director;
    private String Star1;
    private String Star2;
    private String Star3;
    private String Star4;
    private int No_of_Votes;
    private String Gross; // null

    public String getSeries_Title() {
        return Series_Title;
    }

    public void setSeries_Title(String series_Title) {
        Series_Title = series_Title;
    }

    public int getReleased_Year() {
        return Released_Year;
    }

    public void setReleased_Year(int released_Year) {
        Released_Year = released_Year;
    }

    public String getCertificate() {
        return Certificate;
    }

    public void setCertificate(String certificate) {
        Certificate = certificate;
    }

    public String getRuntime() {
        return Runtime;
    }

    public void setRuntime(String runtime) {
        Runtime = runtime;
    }

    public String getGenre() {
        return Genre;
    }

    public void setGenre(String genre) {
        Genre = genre;
    }

    public float getIMDB_Rating() {
        return IMDB_Rating;
    }

    public void setIMDB_Rating(float IMDB_Rating) {
        this.IMDB_Rating = IMDB_Rating;
    }

    public String getOverview() {
        return Overview;
    }

    public void setOverview(String overview) {
        Overview = overview;
    }

    public int getMeta_score() {
        return Meta_score;
    }

    public void setMeta_score(int meta_score) {
        Meta_score = meta_score;
    }

    public String getDirector() {
        return Director;
    }

    public void setDirector(String director) {
        Director = director;
    }

    public String getStar1() {
        return Star1;
    }

    public void setStar1(String star1) {
        Star1 = star1;
    }

    public String getStar2() {
        return Star2;
    }

    public void setStar2(String star2) {
        Star2 = star2;
    }

    public String getStar3() {
        return Star3;
    }

    public void setStar3(String star3) {
        Star3 = star3;
    }

    public String getStar4() {
        return Star4;
    }

    public void setStar4(String star4) {
        Star4 = star4;
    }

    public int getNo_of_Votes() {
        return No_of_Votes;
    }

    public void setNo_of_Votes(int no_of_Votes) {
        No_of_Votes = no_of_Votes;
    }

    public String getGross() {
        return Gross;
    }

    public void setGross(String gross) {
        Gross = gross;
    }

    public Movie(String series_Title, int released_Year, String certificate, String runtime, String genre, float IMDB_Rating, String overview, int meta_score, String director, String star1, String star2, String star3, String star4, int no_of_Votes, String gross) {
        Series_Title = series_Title;
        Released_Year = released_Year;
        Certificate = certificate;
        Runtime = runtime;
        Genre = genre;
        this.IMDB_Rating = IMDB_Rating;
        Overview = overview;
        Meta_score = meta_score;
        Director = director;
        Star1 = star1;
        Star2 = star2;
        Star3 = star3;
        Star4 = star4;
        No_of_Votes = no_of_Votes;
        Gross = gross;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "Series_Title='" + Series_Title + '\'' +
                ", Released_Year=" + Released_Year +
                ", Certificate='" + Certificate + '\'' +
                ", Runtime='" + Runtime + '\'' +
                ", Genre='" + Genre + '\'' +
                ", IMDB_Rating=" + IMDB_Rating +
                ", Overview='" + Overview + '\'' +
                ", Meta_score=" + Meta_score +
                ", Director='" + Director + '\'' +
                ", Star1='" + Star1 + '\'' +
                ", Star2='" + Star2 + '\'' +
                ", Star3='" + Star3 + '\'' +
                ", Star4='" + Star4 + '\'' +
                ", No_of_Votes=" + No_of_Votes +
                ", Gross='" + Gross + '\'' +
                '}';
    }
}
