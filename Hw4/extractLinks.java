import java.io.*;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;
import sun.util.resources.cldr.zh.CalendarData_zh_Hans_HK;


public class extractLinks {

    public static void main(String[] args) throws Exception {
        String csvPath= "/Users/frank/Desktop/572/hw4/dataset/URLtoHTML_fox_news.csv";
        Map<String, String> fileUrlMap = new HashMap<>();
        Map<String, String> urlFileMap = new HashMap<>();
        readFile(csvPath, fileUrlMap , urlFileMap);

        File newsPages = new File("/Users/frank/Desktop/572/hw4/dataset/foxnews");
        Set<String> res = new HashSet<>();


        for (File file : newsPages.listFiles()) {
            Document doc = Jsoup.parse(file, "UTF-8", fileUrlMap.get(file.getName()));
            Elements links = doc.select("a[href]");

            for (Element src : links) {
                String url = src.attr("abs:href").trim();
                if (urlFileMap.containsKey(url)) {
                    res.add(file.getName() + " " + urlFileMap.get(url));
                }

            }

        }
//            System.out.println("links size: " + res.size());
//
//        System.out.println(res);
        writeToFile(res,"/Users/frank/Desktop/572/hw4/" );
    }

    private static void readFile(String path, Map<String, String>  fileUrl, Map<String, String> urlFile) throws IOException {
        File read = new File(path);
        Scanner myReader = new Scanner(read);
        myReader.nextLine();
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            String []record = data.split(",");
            fileUrl.put(record[0], record[1]);
            urlFile.put(record[1], record[0]);
        }
        myReader.close();
    }

    private static void writeToFile(Set<String> res, String path) throws IOException{

        FileWriter writer = new FileWriter(path+"edgeList.txt");
        BufferedWriter wr = new BufferedWriter(writer);

        for (String str : res){
            wr.write(str + "\n");
        }
        wr.close();
    }
}
