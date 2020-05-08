package myCrawler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.common.io.Files;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class MyCrawler extends WebCrawler{
	private final static Pattern FILTERS = Pattern.compile(".*(\\.(css|js|mp3|zip|gz))$");
	
	private HashSet<String> urls  = new  HashSet<>();
	
	private CrawlState crawlState;
	private static File storageFolder;
	
	public MyCrawler(){
		crawlState = new CrawlState();
	}
	
	
	/**
	 * This method receives two parameters. The first parameter is the page
	 * in which we have discovered this new url and the second parameter is
	 * the new url. You should implement this function to specify whether
	 * the given url should be crawled or not (based on your crawling logic).
	 * In this example, we are instructing the crawler to ignore urls that
	 * have css, js, git, ... extensions and to only accept urls that start
	 * with "http://www.viterbi.usc.edu/". In this case, we didn't need the
	 * referringPage parameter to make the decision.
	 */
	public boolean shouldVisit(Page referringPage, WebURL url) {
		String href = url.getURL().toLowerCase();
		String type= "";
		if(href.startsWith("https://www.foxnews.com/") || href.startsWith("https://foxnews.com/") || href.startsWith("http://foxnews.com/") || href.startsWith("http://www.foxnews.com/"))
		{
			type="OK";
		}
		else
		{
			type="N_OK";
		}

		crawlState.discoveredUrls.add(new UrlInfo(href, type));
		return !FILTERS.matcher(href).matches() && (href.startsWith("https://www.foxnews.com/") || href.startsWith("https://foxnews.com/") || href.startsWith("http://foxnews.com/") || href.startsWith("http://www.foxnews.com/"));
	}

	/**
	 * This function is called when a page is fetched and ready
	 * to be processed by your program.
	 */
	 @Override
	 public void visit(Page page) {
		 
		int docid = page.getWebURL().getDocid();
		String url = page.getWebURL().getURL();
		String domain = page.getWebURL().getDomain();
		String path = page.getWebURL().getPath();
		String subDomain = page.getWebURL().getSubDomain();
		String parentUrl = page.getWebURL().getParentUrl();
		String anchor = page.getWebURL().getAnchor();
 
		String contentType = page.getContentType().split(";")[0];
        ArrayList<String> outgoingUrls = new ArrayList<String>();

        logger.debug("Docid: {}", docid);
        logger.info("URL: {}", url);
        logger.debug("Domain: '{}'", domain);
        logger.debug("Sub-domain: '{}'", subDomain);
        logger.debug("Path: '{}'", path);
        logger.debug("Parent page: {}", parentUrl);
        logger.debug("Anchor text: {}", anchor);
		
        UrlInfo urlInfo;
        if (contentType.equals("text/html")) { // html
            if (page.getParseData() instanceof HtmlParseData) {
                HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
                Set<WebURL> links = htmlParseData.getOutgoingUrls();
                for (WebURL link : links) {
                    outgoingUrls.add(link.getURL());
                }
                urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "text/html", ".html");
                crawlState.visitedUrls.add(urlInfo);
            }
            
            else {
                urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "text/html", ".html");
                crawlState.visitedUrls.add(urlInfo);
            }
        } else if (contentType.equals("application/msword")) { // doc
            urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "application/msword", ".doc");
            crawlState.visitedUrls.add(urlInfo);
        } else if (contentType.equals("application/pdf")) { // pdf
            urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "application/pdf", ".pdf");
            crawlState.visitedUrls.add(urlInfo);
        }
        else if (contentType.equals("application/vnd.openxmlformats-officedocument.wordprocessingml.document")) {
            urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx");
            crawlState.visitedUrls.add(urlInfo);
        } 
        else if(contentType.contains("image/jpg"))
        {
        	
        	urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "image/jpg", ".jpg");
        	 crawlState.visitedUrls.add(urlInfo);
        }
        else if(contentType.contains("image/png"))
        {
        	
        	urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "image/png", ".png");
        	 crawlState.visitedUrls.add(urlInfo);
        }
        else if(contentType.contains("image/jpeg"))
        {
        	
        	urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "image/jpeg", ".jpeg");
        	 crawlState.visitedUrls.add(urlInfo);
        }
        else if(contentType.contains("image/gif"))
        {
        	
        	urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "image/gif", ".gif");
        	 crawlState.visitedUrls.add(urlInfo);
        }
        else if(contentType.contains("image/x-icon"))
        {
        	
        	urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "image/x-icon", ".x-icon");
        	 crawlState.visitedUrls.add(urlInfo);
        }
        else {
            urlInfo = new UrlInfo(url, page.getContentData().length, outgoingUrls, "unknown", "");
            crawlState.visitedUrls.add(urlInfo);
        }
        if (!urlInfo.extension.equals("")) {
            String filename = storageFolder.getAbsolutePath() + "/" + urlInfo.hash + urlInfo.extension;
            try {
                Files.write(page.getContentData(), new File(filename));
            } catch (IOException iox) {
                System.out.println("Failed to write file: " + filename);
            }
        }
		
	 }
	 
     @Override
     protected void handlePageStatusCode(WebURL webUrl, int statusCode, String statusDescription) {
         crawlState.attemptUrls.add(new UrlInfo(webUrl.getURL(), statusCode));
     }

     @Override
     public Object getMyLocalData() {
         return crawlState;
     }
     
     public void WriteResultToFile(HtmlParseData htmlParseData) {
//        try {
//            File file = new File("output.txt");
//            PrintStream ps = new PrintStream(new FileOutputStream(file));
            
            
            String text = htmlParseData.getText();
			String html = htmlParseData.getHtml();
			 
			Set<WebURL> links = htmlParseData.getOutgoingUrls();
			System.out.println("Text length: " + text.length());
			System.out.println("text: " + text);
			System.out.println("Html length: " + html.length());
			System.out.println("html: " + html);
			System.out.println("Number of outgoing links: " + links.size());
            
			System.out.println();
			System.out.println(urls.size());
//            
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }


}

