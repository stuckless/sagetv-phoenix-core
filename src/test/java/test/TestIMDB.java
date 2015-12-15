package test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.BasicConfigurator;
import org.xml.sax.SAXException;

public class TestIMDB {
    public static void main(String[] args) throws FileNotFoundException, IOException, SAXException {
        BasicConfigurator.configure();
        InitPhoenix.init(true, true);

        File pageIn = new File("NoCommit/imdb/imdb-detail.html");
        String page = IOUtils.toString(new FileInputStream(pageIn), "UTF-8");

        Pattern title = Pattern.compile("<h1 class=\"header\">([^<]+).*?>([0-9]+)<", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                | Pattern.DOTALL);
        Matcher m = title.matcher(page);
        if (m.find()) {
            System.out.println("Title: " + m.group(1).trim());
            System.out.println("Year: " + m.group(2).trim());
        }
        Pattern infobar = Pattern.compile("<div class=\"infobar\">.*?</div>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                | Pattern.DOTALL);
        Pattern pgrating = Pattern.compile("<img .*?title=\"([^\"]+)\"", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                | Pattern.DOTALL);
        Pattern runtime = Pattern.compile("([0-9]+)\\s+min", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Pattern genres = Pattern.compile("<a.*?href=\"/genre/[^>]+>([^<]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                | Pattern.DOTALL);
        m = infobar.matcher(page);
        if (m.find()) {
            String data = m.group(0);
            m = pgrating.matcher(data);
            if (m.find()) {
                System.out.println("pg: " + m.group(1));
            }
            m = runtime.matcher(data);
            if (m.find()) {
                System.out.println("runtime: " + m.group(1));
            }
            m = genres.matcher(data);
            while (m.find()) {
                System.out.println("Genre: " + m.group(1));
            }
        }

        Pattern userRating = Pattern.compile("<span class=\"rating-rating\">([^<]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                | Pattern.DOTALL);
        m = userRating.matcher(page);
        if (m.find()) {
            System.out.println("UserRating: " + m.group(1).trim());
        }

        Pattern director = Pattern.compile("<h4 class=\"inline\">\\s+Director:\\s+</h4>.*?</div>", Pattern.CASE_INSENSITIVE
                | Pattern.MULTILINE | Pattern.DOTALL);
        Pattern prodCast = Pattern.compile("<a.*?href=\"/name/[^>]+>([^<]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                | Pattern.DOTALL);
        m = director.matcher(page);
        if (m.find()) {
            String data = m.group(0);
            m = prodCast.matcher(data);
            while (m.find()) {
                System.out.println("Director: " + m.group(1).trim());
            }
        }

        Pattern writers = Pattern.compile("<h4 class=\"inline\">\\s+Writers:\\s+</h4>.*?</div>", Pattern.CASE_INSENSITIVE
                | Pattern.MULTILINE | Pattern.DOTALL);
        m = writers.matcher(page);
        if (m.find()) {
            // System.out.println(m.group(0));
            String data = m.group(0);
            m = prodCast.matcher(data);
            while (m.find()) {
                System.out.println("Writer: " + m.group(1).trim());
            }
        }

        Pattern trailer = Pattern.compile("id=\"overview-bottom\".*?<a.*?href=\"(/video/[^\"]+)", Pattern.CASE_INSENSITIVE
                | Pattern.MULTILINE | Pattern.DOTALL);
        m = trailer.matcher(page);
        if (m.find()) {
            System.out.println("Trailer: " + m.group(1));
        }

        Pattern cast = Pattern.compile("<table class=\"cast_list\">.+?</table>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                | Pattern.DOTALL);
        Pattern castItem = Pattern
                .compile(
                        "<tr[^>]*>.*?<td class=\"primary_photo\">.*?<img.*?src=\"([^\"]+)\".*?</td>.*?<td class=\"name\">(.*?)</td>.*?<td class=\"character\">(.*?)</td>.*?</tr>",
                        Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        Pattern replaceTags = Pattern.compile("<[^>]+>", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        m = cast.matcher(page);
        if (m.find()) {
            String data = m.group(0);
            m = castItem.matcher(data);
            while (m.find()) {
                String img = m.group(1);
                String name = m.group(2);
                String character = m.group(3);

                Matcher m2 = replaceTags.matcher(name);
                name = m2.replaceAll("").trim();

                m2 = replaceTags.matcher(character);
                character = m2.replaceAll("").trim();

                System.out.println("----");
                System.out.println("Image: " + img);
                System.out.println("Name: " + name);
                System.out.println("Char: " + character);
            }
        }

        Pattern plot = Pattern.compile("<div\\s+class=\"article\"\\s*>\\s+<h2>\\s*Storyline\\s*</h2>\\s*<p>(.*?)</p>",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        m = plot.matcher(page);
        if (m.find()) {
            Matcher m2 = replaceTags.matcher(m.group(1));
            String desc = m2.replaceAll("");
            System.out.println("Desc: " + desc);
        }

        Pattern mpaaDesc = Pattern.compile("<h4>Motion Picture Rating.*?</h4>([^<]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                | Pattern.DOTALL);
        m = mpaaDesc.matcher(page);
        if (m.find()) {
            System.out.println("MPAA: " + m.group(1));
        }

        Pattern releaseDate = Pattern.compile("<h4[^>]*>Release Date:</h4>([^<]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE
                | Pattern.DOTALL);
        m = releaseDate.matcher(page);
        if (m.find()) {
            System.out.println("Release: " + m.group(1));
        }

        Pattern poster = Pattern.compile("id=\"title-overview-widget-layout\".*?id=\"img_primary\".*?<img.*?src=\"([^\"]+)",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
        m = poster.matcher(page);
        if (m.find()) {
            System.out.println("Poster: " + m.group(1));
        }

        System.out.println("Done");
    }
}
