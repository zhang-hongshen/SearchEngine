package com.zhanghongshen.searchengine;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * @author Zhang Hongshen
 * @description A simple web crawler using BFS strategy
 * @date 2021/5/8
 */
public class WebCrawler {
    /**
     * maxNumOfWebsite : max number of website to be crawled
     * rootUrl : the urls which the crawl begin
     */
    private int maxNumOfWebsite;
    private HashSet<String> rootUrl;
    /**
     * result : save the result
     */
    private HashMap<String,String> result;
    private HashSet<String> contentType;

    private final String govWebsiteRegex = ".gov.cn";
    public WebCrawler(Collection<String> rootUrl){
        try {
            if(rootUrl.size() == 0){
                throw new IllegalArgumentException();
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        maxNumOfWebsite = 100;
        this.rootUrl = new HashSet<>(rootUrl);
        result = new HashMap<>();
        contentType = new HashSet<String>(){{
            add("text/htm");
            add("text/html");
        }};
    }

    public WebCrawler(Collection<String> rootUrl, int maxNumOfWebsite){
        this(rootUrl);
        try {
            if(maxNumOfWebsite < 0){
                throw new IllegalArgumentException();
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
        this.maxNumOfWebsite = maxNumOfWebsite;
    }

    public void setMaxNumOfWebsite(int maxNumOfWebsite) {
        this.maxNumOfWebsite = maxNumOfWebsite;
    }

    public void setRootUrl(Collection<String> rootUrl) {
        this.rootUrl = new HashSet<>(rootUrl);
    }

    public List<String> getResultUrl(){
        return new ArrayList<>(result.keySet());
    }

    public static String parseHtml(String html){
        return html;
    }

    public void start(){
        LinkedList<String> urls = new LinkedList<>(rootUrl);
        long startTime =  System.currentTimeMillis();
        while (result.size() < maxNumOfWebsite) {
            String url = urls.removeFirst();
            try{
                if(isValidUrl(url) && !result.containsKey(url)){
                    System.out.println("???????????? " + url);
                    Connection connection = Jsoup.connect(url);
                    //???????????????
                    connection.header("User-Agent",UserAgent.random());
                    Document doc = connection.get();
                    result.put(url, doc.toString());
                    //??????a?????????href?????????
                    Elements links = doc.select("a[href]");
                    for (Element link : links) {
                        //??????????????????
                        String linkedUrl = link.absUrl("href");
                        urls.add(linkedUrl);
                    }
                }
            }catch (IOException e){
                if(result.containsKey(url)){
                    result.remove(url);
                }
                e.printStackTrace();
            }
        }
        long endTime =  System.currentTimeMillis();
        System.out.println("????????????"+ (endTime - startTime) / 1000 + "s");
    }

    /**
     * @description check whether a url is valid
     * @param url url to be checked
     * @return true --  url is a valid url
     *         false -- url isn't a valid url
     * @throws IOException
     */
    private boolean isValidUrl(String url) throws IOException {
        if(url.isEmpty()){
            return false;
        }
        if(url.contains(govWebsiteRegex)){
            return false;
        }
        HttpURLConnection urlConnection = (HttpURLConnection)new URL(url).openConnection();
        //?????????????????????
        urlConnection.setRequestProperty("User-Agent",UserAgent.random());
        urlConnection.connect();
        //????????????????????????
        int responseCode = urlConnection.getResponseCode();
        if(responseCode != HttpURLConnection.HTTP_OK){
            return false;
        }
        //???????????????htm???html??????
        String contentType = urlConnection.getContentType();
        if(contentType == null){
            return true;
        }
        for (String s : this.contentType) {
            if (contentType.contains(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @description save the result in the specified file directory.
     *              If the directory doesn't exist, then make a directory.
     *              If the directory is a file, then throw a IllegalArgumentException
     * @param fileDirectoryName directory Path
     * @throws IllegalArgumentException
     */
    public void saveAsHtmlPage(String fileDirectoryName){
        File file = new File(fileDirectoryName);
        if(!file.exists()){
            while(!file.mkdir());
        }else if(!file.isDirectory()){
            throw new IllegalArgumentException();
        }
        int count = 0;
        for(Map.Entry<String,String> entry : result.entrySet()){
            count++;
            String filePath = fileDirectoryName + count + "_Org.html";
            FileAction.writeFile(entry.getValue(),filePath);
        }
    }

    public static void main(String[] args){
        List<String> rootUrls = new ArrayList<>();
        rootUrls.add("https://docs.oracle.com/en/java/javase/11/docs/api/");
        //??????????????????
        WebCrawler webCrawler = new WebCrawler(rootUrls,500);
        System.out.println("???????????????...");
        //??????????????????
        webCrawler.start();
        System.out.println("??????????????????");
        System.out.println("???????????????...");
        //???????????????html??????
        webCrawler.saveAsHtmlPage("src/test/resources/english");
        System.out.println("??????????????????");
        System.out.println("?????????????????????" + webCrawler.getResultUrl().size());
    }
}
