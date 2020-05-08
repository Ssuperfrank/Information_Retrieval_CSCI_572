import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

public class extractBig {

    public static void writeToFile(ArrayList<String> wordList) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(new FileWriter("/Users/frank/Desktop/572/hw5/big1.txt"));
        for(String x: wordList)
        {
            writer.write(x+"\n");
        }
        writer.close();
    }


    public static ArrayList<String> parseFile(File myFile) throws FileNotFoundException, IOException, SAXException, TikaException
    {
        BodyContentHandler handler = new BodyContentHandler(-1);
        Metadata metadata = new Metadata();
        FileInputStream inputstream = new FileInputStream(myFile);
        ParseContext context = new ParseContext();

        HtmlParser htmlparser = new HtmlParser();
        htmlparser.parse(inputstream, handler, metadata, context);

        String title = metadata.get("title");

//      String temp = handler.toString();
//      temp =temp.replace("\\[.,\\/#!$%\\^&\\*;:{}=\\-_`~()\\]","");
        String myString = handler.toString();//.toLowerCase();

        ArrayList bigList = new ArrayList(Arrays.asList(myString.replaceAll("\t", " ").replaceAll("\n", " ").trim().split("\\W+")));
        System.out.println(title);
        System.out.println(bigList);

        return bigList;
    }

    public static void main(String args[]) throws FileNotFoundException, IOException, SAXException, TikaException
    {   File file = new File("/Users/frank/Desktop/572/hw4/dataset/foxnews");


        ArrayList<String> fullList = new ArrayList();
        for(File f:  file.listFiles())
        {
            fullList.addAll(parseFile(f));
        }
        writeToFile(fullList);
    }

}

