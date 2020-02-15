package com.example.indexing.Services;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.example.indexing.Services.storage.StorageProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Enzo Cotter on 2/8/20.
 */
public class FileServices {
    public FileServices() {
    }

    public static List<File> files = new ArrayList<>();
    public static Map<String, List<String>> termMap;
    public static List<Map.Entry<String, List<String>>> termList;
    public static StorageProperties sp;

    static {

        File dir = new File("./upload-dir");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                files.add(child);
            }
        }
    }


    public static Set<String> stopWords() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File("./stoplist.txt")));
        Set<String> set = new HashSet<>();
        String s = br.readLine();
        while (s != null) {
            set.add(s);
            s = br.readLine();
        }
        return set;
    }


    public static String parseXML(String name, int RecIndex) {
        String s = "";
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = null;
            for (File f : files) {
                if (f.getName().equals(name)) {
                    doc = documentBuilder.parse(f);
                }
            }
            if (doc == null) return "";
            s = getData(null, doc.getElementsByTagName("RECORD").item(RecIndex));
        } catch (Exception exe) {
            exe.printStackTrace();
        }
        return s;
    }


    private static String getData(Node parentNode, Node node) {
        String s = "";
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            if (node.hasChildNodes()) {
                NodeList list = node.getChildNodes();
                int size = list.getLength();
                for (int i = 0; i < size; i++) {
                    s = s + " " + getData(node, list.item(i));
                }
            }
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            String data = node.getNodeValue();

            if (data.trim().length() > 0) {
                return node.getNodeValue();
            }
        }
        return s;


    }


    public static Set<String> uniqueWord(String name, int RecIndex) throws IOException {
        Set<String> words = new HashSet<>();
        Set<String> stopWords = stopWords();
        String s = parseXML(name, RecIndex);

        String stringInfo = s.toLowerCase();
        Pattern p = Pattern.compile("[.,\"\\?!:;{}()@#$%^&+/\\[\\]<>]");
        Matcher m = p.matcher(stringInfo);
        String first = m.replaceAll(" ");
        String[] ws = first.split("\\s+");
        words.add(" ");
        for (String w : ws) {
            if (!stopWords.contains(w)) {
                words.add(w);
            }
        }
        words.remove(" ");
        words.remove("");
        return words;
    }


    public static List<Map.Entry<String, List<String>>> AllFiles() {
        try {

            Map<String, List<String>> map = new HashMap<>();

            for (File child : files) {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc;

                doc = documentBuilder.parse(child);

                NodeList nodes = doc.getElementsByTagName("RECORDNUM");
                for (int i = 0; i < nodes.getLength(); i++) {
                    String recNum = nodes.item(i).getTextContent();
                    Set<String> set = uniqueWord(child.getName(), i);
                    for (String s : set) {
                        if (!map.containsKey(s)) {
                            map.put(s, new LinkedList<>());
                            map.get(s).add(recNum);
                        } else map.get(s).add(recNum);
                    }
                }
            }
            termMap = map;
            List<Map.Entry<String, List<String>>> list = new ArrayList<>(map.entrySet());
            list.sort((a, b) -> a.getValue().size() - b.getValue().size());
            termList = list;
            return list;


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static List<String> writeToFile(List<Map.Entry<String, List<String>>> list) {
        List<String> res = new ArrayList<>();
        File writename = new File("./index/" + "indexes.txt");
        try {
            writename.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            for (Map.Entry<String, List<String>> entry : list) {
                String str = String.format("%-30s", entry.getKey() + ": ");
                List<String> l = entry.getValue();
                for (String i : l) {
                    str = String.format("%-15s", str + i + ", ");
                }
                str = str + "\r\n";
                res.add(str);
                out.write(str);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;

    }
}

