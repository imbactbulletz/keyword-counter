package web_scanner;

import app.Main;
import job.WebJob;
import misc.ApplicationSettings;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

public class PageScannerWorker implements Callable<Map<String, Integer>> {

    private WebJob webJob;

    public PageScannerWorker(WebJob webJob) {
        this.webJob = webJob;
    }

    @Override
    public Map<String, Integer> call() {
        String pageURL = webJob.getQuery().substring("web|".length());
        Document document = null;


        try {
            document = Jsoup.connect(pageURL).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> foundLinks = getLinksFrom(document);

        if(webJob.getHops() != 0) {
            scheduleNewWebJobs(foundLinks);
        }

        Map<String, Integer> foundKeywords = scanForKeywords(document.body().text());

        return foundKeywords;
    }

    private List<String> getLinksFrom(Document document) {
        List<String> foundLinks = new ArrayList<>();

            Elements links = document.select("a[href]");

            for (Element link : links) {
                foundLinks.add(link.attr("abs:href"));
            }



        return foundLinks;
    }

    private void scheduleNewWebJobs(List<String> links) {
        WebJob tmpJob;
        for(String link : links) {
//            System.out.println("Scheduling new job  (" + link +") with " + (webJob.getHops()-1) + " hops.");
            tmpJob = new WebJob(link, webJob.getHops() -1);
            Main.jobQueue.add(tmpJob);
        }
    }

    private Map<String, Integer> scanForKeywords(String text) {
        Map<String, Integer> resultMap = new HashMap<>();
        List<String> keywordList = ApplicationSettings.keywordsList;


        List<String> words = Arrays.stream(text.split(" "))
                .map(word -> word.replaceAll("\\p{Punct}+$", "")).collect(Collectors.toList());

        for (String word : words) {
            if (keywordList.contains(word)) {
                if(resultMap.containsKey(word)) {
                    resultMap.put(word, resultMap.get(word) + 1);
                } else {
                    resultMap.put(word, 1);
                }
            }
        }

        return resultMap;
    }
}
