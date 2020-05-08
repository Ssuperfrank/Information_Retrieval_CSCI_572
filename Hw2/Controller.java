package myCrawler;

import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller {

	private static final String crawlStorageFolder = "/data/crawl";
	private static final int numberOfCrawlers = 10;
	private static final int maxPageToFetch = 20000;
	private static final int maxDepthOfCrawl = 16;
	private final static String crawlSeed = "https://www.foxnews.com/";
	
	
	public static void main(String[] args) throws Exception {
		CrawlConfig config = new CrawlConfig();
		config.setCrawlStorageFolder(crawlStorageFolder);
		config.setMaxDepthOfCrawling(maxDepthOfCrawl);
		config.setMaxPagesToFetch(maxPageToFetch);
		config.setIncludeBinaryContentInCrawling(true);
		
		/*
		* Instantiate the controller for this crawl.
		*/
		PageFetcher pageFetcher = new PageFetcher(config);
		RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
		RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		/*
		* For each crawl, you need to add some seed urls. These are the first
		* URLs that are fetched and then the crawler starts following links
		* which are found in these pages
		*/
		controller.addSeed(crawlSeed);
		/*
		* Start the crawl. This is a blocking operation, meaning that your code
		* will reach the line after this only when crawling is finished.
		* */ 
		
		controller.start(MyCrawler.class, numberOfCrawlers);
		
		//CrawlConfig config = new CrawlConfig();
    	CrawlState sumState = new CrawlState();
        List<Object> crawlersLocalData = controller.getCrawlersLocalData();
        for (Object localData : crawlersLocalData) {
            CrawlState state = (CrawlState) localData;
            sumState.attemptUrls.addAll(state.attemptUrls);
            sumState.visitedUrls.addAll(state.visitedUrls);
            sumState.discoveredUrls.addAll(state.discoveredUrls);
        }
        

		saveFetchCsv(sumState);
		saveVisitCsv(sumState);
		saveUrlsCsv(sumState);
		saveStatistics(sumState);
	}
	
	
	
	  //the URLs it attempts to fetch
    public static void saveFetchCsv(CrawlState sumState) throws Exception {
        String fileName =  "fetch_foxnews.csv";
        FileWriter writer = new FileWriter(fileName);
        writer.append("URL,Status\n");
        for (UrlInfo info : sumState.attemptUrls) {
            writer.append(info.url + "," + info.statusCode + "\n");
        }
        writer.flush();
        writer.close();
    }


    public static void saveVisitCsv(CrawlState sumState) throws Exception {
        String fileName = "visit_foxnews.csv";
        FileWriter writer = new FileWriter(fileName);
        writer.append("URL,Size,OutLinks,ContentType\n");
        for (UrlInfo info : sumState.visitedUrls) {
            if (info.type != "unknown") {
                writer.append(info.url + "," + info.size + "," + info.outgoingUrls.size() + "," + info.type + "\n");
            }
        }
        writer.flush();
        writer.close();
    }
    
    public static void saveUrlsCsv(CrawlState sumState) throws Exception {
        String fileName = "urls_foxnews.csv";
        FileWriter writer = new FileWriter(fileName);
        writer.append("URL,Type\n");
        for (UrlInfo info : sumState.discoveredUrls) {
            writer.append(info.url + "," + info.type + "\n");
        }
        writer.flush();
        writer.close();
    }
    
    public static void saveStatistics(CrawlState sumState) throws Exception {
        String fileName = "CrawlReport.txt";
        FileWriter writer = new FileWriter(fileName);

        // Personal Info
        writer.append("Name: Jiaqi Fan \n");
        writer.append("USC ID: 7045273947 \n");
        writer.append("News site crawled: foxnews.com \n");
        writer.append("Number of threads used: " + numberOfCrawlers + "\n");
        writer.append("\n");

        // Fetch Statistics
        writer.append("Fetch Statistics:\n=====================\n");
        writer.append("# fetches attempted: " + sumState.attemptUrls.size() + "\n");
        // get failed url and aborted urls
        int succeedUrlsCount = 0;
        int failedOrAbortedUrlsCount = 0;
        for (UrlInfo info : sumState.attemptUrls) {
            if (info.statusCode >= 200 && info.statusCode < 300) {
            	succeedUrlsCount++;
            } else {
            	failedOrAbortedUrlsCount++;
            }
        }
        writer.append("# fetches succeeded: " + succeedUrlsCount + "\n");
        writer.append("# fetches failed or aborted : " + failedOrAbortedUrlsCount + "\n");
        writer.append("\n");

        // Outgoing URLS
        HashSet<String> hashSet = new HashSet<String>();
        int uniqueUrls = 0;
        int withinUrls = 0;
        int outUrls = 0;
        
        writer.append("Outgoing URLs:\n=====================\n");
        writer.append("Total URLS extracted: " + sumState.discoveredUrls.size() + "\n");
        for (UrlInfo info : sumState.discoveredUrls) {
            if (!hashSet.contains(info.url)) {
                hashSet.add(info.url);
                uniqueUrls++;
                if (info.type.equals("OK")) {
                	withinUrls++;
                } else {
                    outUrls++;
                }
            }
        }
        writer.append("# unique URLs extracted: " + uniqueUrls + "\n");
        writer.append("# unique URLs within news site: " + withinUrls + "\n");
        writer.append("# unique URLs outside news site: " + outUrls + "\n");
        writer.append("\n");

        // Status Code
        HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
        for (UrlInfo info : sumState.attemptUrls) {
            if (hashMap.containsKey(info.statusCode)) {
                hashMap.put(info.statusCode, hashMap.get(info.statusCode) + 1);
            } else {
                hashMap.put(info.statusCode, 1);
            }
        }
        HashMap<Integer, String> statusCodeMapping = new HashMap<Integer, String>();
        statusCodeMapping.put(200, "OK");
        statusCodeMapping.put(301, "Moved Permanently");
        statusCodeMapping.put(302, "Found");
        statusCodeMapping.put(401, "Unauthorized");
        statusCodeMapping.put(403, "Forbidden");
        statusCodeMapping.put(404, "Not Found");
        statusCodeMapping.put(405, "Method Not Allowed");
        statusCodeMapping.put(500, "Internal Server Error");
        
        writer.append("Status Codes:\n=====================\n");
        for (Integer key : hashMap.keySet()) {
            writer.append("" + key + " " + statusCodeMapping.get(key) + ": " + hashMap.get(key) + "\n");
        }
        writer.append("\n");

        // File Size
        int oneK = 0;
        int tenK = 0;
        int hundredK = 0;
        int oneM = 0;
        int other = 0;
        for (UrlInfo info : sumState.visitedUrls) {
            if (info.size < 1024) {
                oneK++;
            } else if (info.size < 10240) {
                tenK++;
            } else if (info.size < 102400) {
                hundredK++;
            } else if (info.size < 1024 * 1024) {
                oneM++;
            } else {
                other++;
            }
        }
        writer.append("File Sizes:\n=====================\n");
        writer.append("< 1KB: " + oneK + "\n");
        writer.append("1KB ~ <10KB: " + tenK + "\n");
        writer.append("10KB ~ <100KB: " + hundredK + "\n");
        writer.append("100KB ~ <1MB: " + oneM + "\n");
        writer.append(">= 1MB: " + other + "\n");
        writer.append("\n");

        // Content Types
        HashMap<String, Integer> hashMap1 = new HashMap<String, Integer>();
        for (UrlInfo info : sumState.visitedUrls) {
            if (info.type.equals("unknown")) {
                continue;
            }
            if (hashMap1.containsKey(info.type)) {
                hashMap1.put(info.type, hashMap1.get(info.type) + 1);
            } else {
                hashMap1.put(info.type, 1);
            }
        }

        writer.append("Content Types:\n=====================\n");
        for (String key : hashMap1.keySet()) {
            writer.append("" + key + ": " + hashMap1.get(key) + "\n");
        }
        writer.append("\n");

        writer.flush();
        writer.close();
    }
	

}
