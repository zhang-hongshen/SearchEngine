package com.zhanghongshen.searchengine;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zhang Hongshen
 * @description TextPreprocess
 * @date 2021/5/3
 */
public class TextPreprocessor {

    public static String deleteHtmlTag(String htmlStr){
        //style标签的正则表达式
        String styleTagRegex = "<style[^>]*?>[\\s\\S]*?<\\/style>";
        //script标签的正则表达式
        String scriptTagRegex = "<script[^>]*?>[\\s\\S]*?<\\/script>";
        //html标签的正则表达式
        String htmlTagRegex = "<[^>]+>";
        Pattern pattern = Pattern.compile(styleTagRegex,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(htmlStr);
        htmlStr = matcher.replaceAll(" ");
        pattern = Pattern.compile(scriptTagRegex,Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(htmlStr);
        htmlStr = matcher.replaceAll(" ");
        pattern = Pattern.compile(htmlTagRegex,Pattern.CASE_INSENSITIVE);
        matcher = pattern.matcher(htmlStr);
        htmlStr = matcher.replaceAll(" ");
        return htmlStr.replaceAll("&nbsp;"," ")
                .replaceAll("&amp;"," ")
                .trim(); //返回文本字符串
    }

    public String deleteChineseSepcialChar(String text){
        String regex = "[_`~!@#$%^&*()+=|{}':;,\\\\[\\\\].<>/?！￥…（）—【】‘；：”“’。，、？]|\\n|\\r|\\t";
        return  text.replaceAll(regex,"")
                .replaceAll(" ","")
                .replaceAll("<a>\\\\s*|\\t|\\r|\\n</a>", "")
                .replaceAll(" ","")
                .trim();
    }

    public String deleteEnglishSepcialChar(String text){
        String regex = "[_`~!@#$%^&*()+=|{}':;,\\\\[\\\\].<>/?！￥…（）—【】‘；：”“’。，、？]|\\n|\\r|\\t";
        return  text.replaceAll(regex," ")
                .trim();
    }



    public String cnProcess(String text){
        text = deleteHtmlTag(text);
        //删除中文特殊字符
        text = deleteChineseSepcialChar(text);
        //
        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<SegToken> tokens = segmenter.process(text, JiebaSegmenter.SegMode.SEARCH);
        List<String> words = new ArrayList<>();
        for(SegToken token : tokens){
            words.add(token.word);
        }
        //过滤空白词
        ArrayList<String> wordList = new ArrayList<>();
        for(String word : words){
            if(word.length() != 0){ wordList.add(word); }
        }
        //读取中文停用词
        String[] chineseStopwords = new String[0];
        try {
            chineseStopwords = FileAction.readFile("stopwords/cn_stopwords.txt").split(" ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> chineseStopwordList = new ArrayList<>(Arrays.asList(chineseStopwords));
        //删除中文停用词
        wordList.removeAll(chineseStopwordList);
        StringBuffer res = new StringBuffer();
        for(String word : wordList){
            res.append(word).append(" ");
        }
        return res.toString();
    }

    /**
     * @param file file to be processed
     * @return true -- if the process succeed.
     *         false -- if the process failed.
     */

    public boolean cnProcess(File file) {
        //读取文件
        String text = null;
        try {
            text = FileAction.readFile(file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //处理
        String res = cnProcess(text);
        return FileAction.writeFile(res,file.getAbsolutePath()+"_Processed.txt");
    }

    public String enProcess(String text){
        //转换为小写字母
        text = text.toLowerCase();
        text = deleteHtmlTag(text);
        //删除英文特殊字符
        text = deleteEnglishSepcialChar(text);
        //英文分词
        String[] words = text.split(" ");
        //过滤空白词
        ArrayList<String> wordList = new ArrayList<>();
        for(String word : words){
            if(word.length() != 0){ wordList.add(word); }
        }
        //读取英文停用词
        String[] englishStopwords = new String[0];
        try {
            englishStopwords = FileAction.readFile("stopwords/en_stopwords.txt").split(" ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        ArrayList<String> englishStopwordList = new ArrayList<>(Arrays.asList(englishStopwords));
        //删除英文停用词
        wordList.removeAll(englishStopwordList);
        words = wordList.toArray(new String[wordList.size()]);
        //Porter Stemming方法提取词干
        Stemmer s = new Stemmer();
        StringBuffer res =  new StringBuffer();
        for(String word : words){
            s.add(word);
            s.stem();
            res.append(s).append(" ");
        }
        //储存结果
        String[] stemmingWords = res.toString().split(" ");
        //输出结果
        res =  new StringBuffer();
        for(String stemmingWord : stemmingWords){
            res.append(stemmingWord).append(" ");
        }
        return res.toString();
    }

    /**
     * @param file file to be processed
     * @return true -- if the process succeed
     *         false -- if the process failed
     */
    public boolean enProcess(File file){
        //读取文件
        String text = null;
        try {
            text = FileAction.readFile(file.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        //处理
        String res = enProcess(text);
        //输出结果
        return FileAction.writeFile(res,file.getAbsolutePath()+"_Processed.txt");
    }

    public static void main(String[] args){
        /**
         * 中文文档预处理
         */
        String cnFileDirectory = "src/test/resources/chinese/";
        File[] cnFiles = new File[0];
        try {
            cnFiles = FileAction.readDirectory(cnFileDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int cnFileNum = 0;
        System.out.println("处理中...");
        for(File cnFile : cnFiles){
            if(cnFile.isFile()){
                if(!new TextPreprocessor().cnProcess(cnFile)){
                    System.out.println(cnFile.getPath()+" failed.");
                    continue;
                }
                cnFileNum++;
            } else if(cnFile.isDirectory()){
                System.out.println(cnFile.getPath()+" is a directory.");
            }
        }
        System.out.println("处理完成！");
        System.out.println(cnFileNum+"个成功。");

        /**
         * 英文文档预处理
         */
        String enFileDirectory = "src/test/resources/english/";
        File[] enFiles = new File[0];
        try {
            enFiles = FileAction.readDirectory(enFileDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int enFileNum = 0;
        System.out.println("处理中...");
        for(File enFile : enFiles){
            if(enFile.isFile()){
                if(!new TextPreprocessor().enProcess(enFile)){
                    System.out.println(enFile.getPath()+"failed");
                    continue;
                }
                enFileNum++;
            }else if(enFile.isDirectory()){
                System.out.println(enFile.getPath()+" is a directory.");
            }
        }
        System.out.println("处理完成！");
        System.out.println(enFileNum+"个成功。");
    }
}

