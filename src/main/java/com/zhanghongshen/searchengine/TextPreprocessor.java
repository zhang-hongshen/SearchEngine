package com.zhanghongshen.searchengine;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zhang Hongshen
 * @description TextPreprocess
 * @date 2021/5/3
 */
public class TextPreprocessor {
    private final String anySpaceRegex = "\\s+";
    public TextPreprocessor(){
    }

    private String deleteHtmlTag(String htmlStr){
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
                .replaceAll(anySpaceRegex," ")
                .trim(); //返回文本字符串
    }

    private String deleteChineseSepcialChar(String text){
        String regex = "[_`~!@#$%^&*()+=|{}':;,\\\\[\\\\].<>/?！￥…（）—【】‘；：”“’。，、？]|\\n|\\r|\\t";
        return  text.replaceAll(regex," ")
                .replaceAll("<a>\\\\s*|\\t|\\r|\\n</a>", " ")
                .replaceAll(anySpaceRegex," ")
                .trim();
    }

    private String deleteEnglishSepcialChar(String text){
        String regex = "[_`~!@#$%^&*()+=|{}':;,\\\\[\\\\].<>/?！￥…（）—【】‘；：”“’。，、？]|\\n|\\r|\\t";
        return  text.replaceAll(regex," ")
                .replaceAll(anySpaceRegex," ")
                .trim();
    }

    private void loadStopWord(List<String> stopWords,String filePath){
        File file = new File(filePath);
        try {
            if(!file.exists() || !file.isFile()){
                throw  new IOException();
            }
            FileInputStream fileInputStream = new FileInputStream(filePath);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String s;
            while ((s = bufferedReader.readLine()) != null) {
                stopWords.add(s.trim());
            }
            bufferedReader.close();
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private List<String> cnProcess(String text){
        List<String> words = new ArrayList<>();
        //删除html标记
        text = deleteHtmlTag(text);
        //删除中文特殊字符
        text = deleteChineseSepcialChar(text);
        //中文分词
        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<SegToken> tokens = segmenter.process(text, JiebaSegmenter.SegMode.SEARCH);
        for(SegToken token : tokens){
            words.add(token.word);
        }
        //过滤空白词
        words.removeIf(e -> e.length() == 0 || e.matches(anySpaceRegex));
        //读取中文停用词
        List<String> chineseStopWords = new ArrayList<>();
        loadStopWord(chineseStopWords,"src/main/resources/cn_stop_words.txt");
        //删除中文停用词
        words.removeAll(chineseStopWords);
        return words;
    }

    private List<String> enProcess(String text){
        List<String> words = new ArrayList<>();
        //转换为小写字母
        text = text.toLowerCase();
        //删除html标记
        text = deleteHtmlTag(text);
        //删除英文特殊字符
        text = deleteEnglishSepcialChar(text);
        //英文分词
        words.addAll(Arrays.asList(text.split(" ")));
        //过滤空白词
        words.removeIf(e -> e.length() == 0 || e.matches(anySpaceRegex));
        //读取英文停用词
        List<String> englishStopWords = new ArrayList<>();
        loadStopWord(englishStopWords,"src/main/resources/en_stop_words.txt");
        //删除英文停用词
        words.removeAll(englishStopWords);
        //Porter Stemming方法提取词干
        Stemmer s = new Stemmer();
        List<String> res =  new ArrayList<>();
        for(String word : words){
            s.add(word);
            s.stem();
            //储存Porter-Stemming结果
            res.add(s.toString());
        }
        words.clear();
        words.addAll(res);
        return words;
    }

    public List<String> process(String text,String language){
        List<String> words = new ArrayList<>();
        HashMap<String, Method> methodMapper = new HashMap<>();
        try {
            methodMapper.put("chinese",TextPreprocessor.class.getDeclaredMethod("cnProcess", String.class));
            methodMapper.put("english",TextPreprocessor.class.getDeclaredMethod("enProcess", String.class));
            Method method = methodMapper.get(language.toLowerCase());
            words.addAll((List<String>) method.invoke(new TextPreprocessor(),text));
            return words;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return words;
    }

    public boolean process(File file, String language){
        //读取文件
        String text = FileAction.readFile(file.getPath());
        List<String> words = process(text,language);
        return FileAction.writeFile(words,file.getAbsolutePath()+"_Processed.txt");
    }

    public static void main(String[] args) throws Exception {
        /**
         * 中文文档预处理
         */
        String cnFileDirectory = "src/test/resources/chinese/";
        File[] cnFiles = FileAction.readDirectory(cnFileDirectory);
        int cnFileNum = 0;
        System.out.println("处理中...");
        long startTime =  System.currentTimeMillis();
        for(File cnFile : cnFiles){
            if(cnFile.isFile()){
                if(!new TextPreprocessor().process(cnFile,"chinese")){
                    System.out.println(cnFile.getPath()+" failed.");
                    continue;
                }
                cnFileNum++;
            } else if(cnFile.isDirectory()){
                System.out.println(cnFile.getPath()+" is a directory.");
            }
        }
        long endTime =  System.currentTimeMillis();
        System.out.println("处理完成！");
        System.out.println("总耗时："+ (endTime - startTime) / 1000 + "s");
        System.out.println(cnFileNum+"个成功,"+ (cnFiles.length - cnFileNum) + "失败。");
        /**
         * 英文文档预处理
         */
        String enFileDirectory = "src/test/resources/english/";
        File[] enFiles = FileAction.readDirectory(enFileDirectory);
        int enFileNum = 0;
        System.out.println("处理中...");
        startTime =  System.currentTimeMillis();
        for(File enFile : enFiles){
            if(enFile.isFile()){
                if(!new TextPreprocessor().process(enFile,"english")){
                    System.out.println(enFile.getPath()+"failed");
                    continue;
                }
                enFileNum++;
            }else if(enFile.isDirectory()){
                System.out.println(enFile.getPath()+" is a directory.");
            }
        }
        endTime =  System.currentTimeMillis();
        System.out.println("处理完成！");
        System.out.println("总耗时："+ (endTime - startTime) / 1000 + "s");
        System.out.println(enFileNum+"个成功,"+(enFiles.length - enFileNum) + "失败。");
    }
}

