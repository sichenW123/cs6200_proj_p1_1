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
    private List<File> files = new ArrayList<>();
    private Map<String, List<String>> termMap;
    public FileServices() {
        File dir = new File("./upload-dir");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                files.add(child);
            }
        }
    }

    public void addFile(){
        File dir = new File("./upload-dir");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                files.add(child);
            }
        }
    }





    public List<File> getFiles(){

        return this.files;
    }

    public Map<String, List<String>> getMap(){
        return this.termMap;
    }





    public Set<String> stopWords() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File("./stoplist.txt")));
        Set<String> set = new HashSet<>();
        String s = br.readLine();
        while (s != null) {
            set.add(s);
            s = br.readLine();
        }
        return set;
    }


    public  String parseXML(String name, int RecIndex) {
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


    private  String getData(Node parentNode, Node node) {
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


    public  Set<String> wordSet(String name, int RecIndex) throws IOException {
        Set<String> words = new HashSet<>();
        Set<String> stopWords = stopWords();
        String s = parseXML(name, RecIndex);

        String stringInfo = s.toLowerCase();
        Pattern p = Pattern.compile("[.,\"\\?!:;{}()@#$%^&+/\\[\\]<>]|[^\\s\\p{L}\\p{N}']|(?<=(^|\\s))'|'(?=($|\\s))");
        Matcher m = p.matcher(stringInfo);
        String first = m.replaceAll(" ");
        String[] ws = first.split("\\s+");
        for (String w : ws) {
            if (!stopWords.contains(w)) {
                words.add(w);
            }
        }
        words.remove("");
        return words;
    }


    public Map<String, List<String>> indexMap() {
        try {

            Map<String, List<String>> map = new HashMap<>();

            for (File child : files) {
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc;

                doc = documentBuilder.parse(child);

                NodeList nodes = doc.getElementsByTagName("RECORDNUM");
                for (int i = 0; i < nodes.getLength(); i++) {
                    String recNum = nodes.item(i).getTextContent();
                    Set<String> set = wordSet(child.getName(), i);
                    for (String s : set) {
                        if (!map.containsKey(s)) {
                            map.put(s, new LinkedList<>());
                            map.get(s).add(recNum);
                        } else map.get(s).add(recNum);
                    }
                }
            }
            termMap = map;
            return map;



        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public List<String> writeToFile() {
        List<String> res = new ArrayList<>();
        long startTime=System.currentTimeMillis();
        Map<String, List<String>> map=new HashMap<>(indexMap());
        long endTime=System.currentTimeMillis();
        List<Map.Entry<String, List<String>>> sortedList=new ArrayList<>(map.entrySet());
        sortedList.sort((a, b)->a.getKey().compareTo(b.getKey()));
        File writename = new File("./index/" + "indexes.txt");
        try {
            writename.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            out.write("                    \r\n");
            out.write("run time： "+(endTime-startTime)+"ms"+"\r\n");
            for (Map.Entry<String, List<String>> entry : sortedList) {
                String str = String.format("%-40s", entry.getKey() + ": ");
                List<String> l = entry.getValue();
                for (String i : l) {
                    str = String.format("%-15s", str + i + ", ");
                }
                str = str + "\r\n";
                res.add(str);
                out.write(str);
            }
            String size="file size: "+writename.length()/1024+"kB\r\n";
            RandomAccessFile rf=new RandomAccessFile(writename, "rw");
            rf.seek(0);
            rf.write(size.getBytes());
            rf.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;

    }
}

