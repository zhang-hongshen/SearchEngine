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
     * depth : the Crawl depth
     * maxNumOfWebsite : max number of website to be crawled
     * rootUrl : the urls which the crawl begin
     */
    private int depth;
    private int maxNumOfWebsite;
    private HashSet<String> rootUrl;
    /**
     * result : save the result
     */
    private HashMap<String,String> result;
    private HashSet<String> contentType;
    private final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36";

    public WebCrawler(Collection<String> rootUrl){
        depth = 1;
        maxNumOfWebsite = 100;
        this.rootUrl = new HashSet<>(rootUrl);
        result = new HashMap<>();
        contentType = new HashSet<String>(){{
            add("text/htm");
            add("text/html");
        }};
    }

    public WebCrawler(Collection<String> rootUrl, int depth, int maxNumOfWebsite) throws IllegalArgumentException{
        this(rootUrl);
        if(depth < 0 || maxNumOfWebsite < 0){
            throw new IllegalArgumentException();
        }
        this.depth = depth;
        this.maxNumOfWebsite = maxNumOfWebsite;
    }

    public void setDepth(int depth) {
        this.depth = depth;
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

    public void start(){
        try{
            if(depth == 0 && maxNumOfWebsite > 0){
                startWithoutDeepthLimit();
            }else if(depth > 0 && maxNumOfWebsite == 0){
                startWithoutMaxNumLimit();
            }else{
                startWithDeepthAndMaxNumLimit();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    private void startWithDeepthAndMaxNumLimit() throws IOException {
        HashSet<String> pre = new HashSet<>(rootUrl);
        HashSet<String> res = new HashSet<>();
        int deepth = 0;
        while(deepth <= this.depth && result.size() <= this.maxNumOfWebsite){
            for(String url : pre){
                Connection connection = Jsoup.connect(url);
                //模拟浏览器访问
                connection.userAgent(USER_AGENT);
                Document doc = connection.get();
                //抓取a标签的href属性值
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    if(result.size() >= maxNumOfWebsite){
                        return;
                    }
                    String websiteUrl = link.absUrl("href");
                    if(!websiteUrl.isEmpty() && isValidUrl(url)){
                        res.add(websiteUrl);
                        result.put(websiteUrl,doc.toString());
                    }
                }
            }
            pre.clear();
            pre.addAll(res);
            res.clear();
            deepth++;
        }
    }

    private void startWithoutDeepthLimit() throws IOException {
        HashSet<String> pre = new HashSet<>(rootUrl);
        HashSet<String> res = new HashSet<>();
        while(result.size() <= maxNumOfWebsite){
            for(String url : pre){
                Connection connection = Jsoup.connect(url);
                //模拟浏览器访问
                connection.userAgent(USER_AGENT);
                Document doc = connection.get();
                //抓取a标签的href属性值
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    if(res.size() >= maxNumOfWebsite){
                        return;
                    }
                    String websiteUrl = link.attr("abs:href");
                    if(!websiteUrl.isEmpty() && isValidUrl(url)){
                        res.add(websiteUrl);
                        result.put(websiteUrl,doc.toString());
                    }
                }
            }
            pre.clear();
            pre.addAll(res);
            res.clear();
        }
    }

    private void startWithoutMaxNumLimit() throws IOException{
        HashSet<String> pre = new HashSet<>(rootUrl);
        HashSet<String> res = new HashSet<>();
        int deepth = 0;
        while(deepth <= this.depth){
            for(String url : pre){
                //建立连接
                Connection connection = Jsoup.connect(url);
                //模拟浏览器访问
                connection.userAgent(USER_AGENT);
                Document doc = connection.get();
                //抓取a标签的href属性值
                Elements links = doc.select("a[href]");
                for (Element link : links) {
                    String websiteUrl = link.attr("abs:href");
                    if(!websiteUrl.isEmpty() && isValidUrl(url)){
                        res.add(websiteUrl);
                        result.put(websiteUrl,doc.toString());
                    }
                }
            }
            pre.clear();
            pre.addAll(res);
            res.clear();
            deepth++;
        }
    }

    /**
     * @description check whether a url is valid
     * @param url -- url to be checked
     * @return true --  url is a valid url
     *         false -- url isn't a valid url
     * @throws IOException
     */
    private boolean isValidUrl(String url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
        //模拟浏览器访问
        urlConnection.setRequestProperty("User-Agent",USER_AGENT);
        String contentType = urlConnection.getContentType();
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
     * @param fileDirectoryName -- directory Path
     * @throws IllegalArgumentException
     */
    public void saveAsHtmlPage(String fileDirectoryName) throws IllegalArgumentException{
        File file = new File(fileDirectoryName);
        if(!file.exists()){
            file.mkdir();
        }else if(!file.isDirectory()){
            throw new IllegalArgumentException();
        }
        int count = 0;
        for(Map.Entry<String,String> entry : result.entrySet()){
            count++;
            String filePath = fileDirectoryName + count + "_E_Org.html";
            FileAction.writeFile(entry.getValue(),filePath);
        }
    }

    public static void main(String[] args){
        //初始化根网页，这里以java 11 API文档为例子
        List<String> rootUrls = new ArrayList<>();
        rootUrls.add("https://www.baidu.com/");
        //初始化爬取器
        WebCrawler webCrawler = new WebCrawler(rootUrls,2,500);
        System.out.println("网页爬取中...");
        //开始爬取网页
        webCrawler.start();
        System.out.println("网页爬取完毕");
        System.out.println("结果保存中...");
        System.out.println(webCrawler.getResultUrl());
        //结果保存为html文件
        webCrawler.saveAsHtmlPage("src/test/resources/english/");
        System.out.println("保存结果完毕");
        System.out.println("爬取网页数量：" + webCrawler.getResultUrl().size());

    }
}
