package com.example.indexing.Services;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.example.indexing.Models.BTree;
import com.example.indexing.Models.Doc;
import com.example.indexing.Models.Index;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Sichen Wang on 2/8/20.
 */
@Service
public class FileServices {
    //list of files
    private List<File> files = new ArrayList<>();
    //index dictionary
    private BTree<String, Index> dict=new BTree<>();
    //total number of documents
    private int docNum;
    //map of documents
    private Map<Integer,Doc> docMap=new HashMap<>();

    //constructor of fileservices adding files to the list
    @Autowired
    public FileServices() {
        File dir = new File("./upload-dir");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                files.add(child);
            }
        }
    }

    //get index dictionary
    public BTree<String, Index> getDict(){
        return dict;
    }

    //add file to file list
    public void addFile(){
        File dir = new File("./upload-dir");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                files.add(child);
            }
        }
    }

    //get all files
    public List<File> getFiles(){
        return this.files;
    }

    //initialize set of stop words
    public Set<String> stopWords() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File("./stoplist.txt")));
        Set<String> set = new HashSet<>();
        String s = br.readLine();
        while (s != null) {
            set.add(s);
            s = br.readLine();
        }
        set.add("");
        return set;
    }


    //get content of a .xml file
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

    //parse content to an array of words
    public String[] tokenization(String content){

        String stringInfo = content.toLowerCase();
        Pattern p = Pattern.compile("[.,\"\\?!:;{}()@#$%^&+/\\[\\]<>]|[^\\s\\p{L}\\p{N}']|(?<=(^|\\s))'|'(?=($|\\s))");
        Matcher m = p.matcher(stringInfo);
        String first = m.replaceAll(" ");
        String[] ws = first.split("\\s+");
        return ws;
    }

    //construct indexes to a dictionary(B-tree)
    public void constructDict() {
        try{
            Set<String> stopWords = stopWords();
            for (File child : files){
                DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document doc = null;
                doc=documentBuilder.parse(child);
                NodeList nl=doc.getElementsByTagName("RECORD");
                docNum+=nl.getLength();
                for(int j=0; j<nl.getLength(); j++){
                    Element el=(Element)nl.item(j);
                    int id=Integer.parseInt(el.getElementsByTagName("RECORDNUM").item(0).getTextContent());
                    String title=el.getElementsByTagName("TITLE").item(0).getTextContent();
                    String content=parseXML(child.getName(), j);
                    String[] ws =tokenization(content);
                    Doc rec=new Doc(id, ws.length, title);
                    docMap.put(id, rec);
                    for (String w : ws) {
                        if (!stopWords.contains(w)) {
                            Index ind=dict.get(w);
                            if(ind!=null){
                                int size=ind.getMatrix().size();
                                int last=ind.getMatrix().get(size-1)[0];
                                if(id>last){
                                    dict.get(w).getMatrix().add(new int[]{id, 1});
                                }else{
                                    dict.get(w).getMatrix().get(size-1)[1]++;
                                }
                            }else{
                                int[] pair = new int[]{j+1, 1};
                                List<int[]> list = new LinkedList<>();
                                list.add(pair);
                                dict.put(w, new Index(w, list));
                            }
                        }
                    }
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //get inverted document frequency
    public double getIdf(String t){
        double res=0;
        double total=dict.get(t).getMatrix().size()*1.0;
        res=Math.log10(docNum/total);
        return res;
    }

    //get term frequency
    public double getTf(int raw){
        return 1+Math.log10(raw*1.0);
    }

    //get tf-idf weight
    public double getWeight(double tf, double idf){
        return tf*idf;
    }


    //get heap of rankings
    public PriorityQueue<double[]> getRanking(String[] terms){
        int[] len=new int[docNum];
        double[][] scores=new double[docNum][2];
        double[] wd=new double[docNum];

        for(int i=0; i<scores.length; i++){
            scores[i][0]=i+1;
        }
        for(String t:terms){
            Index index=dict.get(t);
            if(index!=null){
                double idf=getIdf(t);
                for(int[] l:index.getMatrix()){

                    double wq=getWeight(1,idf);
                    len[l[0]-1]=docMap.get(l[0]).getDocLength();

                    scores[l[0]-1][1]+=wq;
                    wd[l[0]-1]=getTf(l[1]);


                }
            }
        }
        double normal=0;
        for(int i=0; i<wd.length; i++){
            normal+=wd[i]*wd[i];
        }
        normal=Math.sqrt(normal);
        PriorityQueue<double[]> heap=new PriorityQueue<>((a,b)->{
            if(a[1]<b[1]) return 1;
            else if(a[1]>b[1]) return -1;
            else return 0;
        });

        for(int i=0; i<wd.length; i++){
            wd[i]=wd[i]/normal;
            scores[i][1]*=wd[i];

            heap.add(scores[i]);

        }
        return heap;
    }

    //get top k rankings
    public int[] rankingTopK( int k, PriorityQueue<double[]> heap) {
        int[] res=new int[k];
        for(int i=0; i<res.length; i++){
            double[] pair=heap.poll();
            res[i]=(int)pair[0];
        }
        return res;
    }


    //write dictionary to a file
    public List<String> writeToFile() {
        List<String> res = new ArrayList<>();
        constructDict();
        File writename = new File("./index/" + "indexes.txt");
        try {
            writename.createNewFile();
            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
            out.write("                    \r\n");
            out.write(dict.toString());
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

