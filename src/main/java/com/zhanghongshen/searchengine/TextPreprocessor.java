package com.zhanghongshen.searchengine;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.SegToken;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Zhang Hongshen
 * @description TextPreprocess
 * @date 2021/5/3
 */
public class TextPreprocessor {

    public TextPreprocessor(){}

    /**
     * @description delete all html tags in the string
     * @param htmlStr -- input string
     * @return string which all html tags have been deleted
     */
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
                .trim(); //返回文本字符串
    }

    private String deleteChineseSepcialChar(String text){
        String regex = "[_`~!@#$%^&*()+=|{}':;,\\\\[\\\\].<>/?！￥…（）—【】‘；：”“’。，、？]|\\n|\\r|\\t";
        return  text.replaceAll(regex,"")
                .replaceAll(" ","")
                .replaceAll("<a>\\\\s*|\\t|\\r|\\n</a>", "")
                .replaceAll(" ","")
                .trim();
    }

    private String deleteEnglishSepcialChar(String text){
        String regex = "[_`~!@#$%^&*()+=|{}':;,\\\\[\\\\].<>/?！￥…（）—【】‘；：”“’。，、？]|\\n|\\r|\\t";
        return  text.replaceAll(regex," ")
                .trim();
    }

    /**
     * @description process chinese text
     * @param text -- the content to be processed
     * @return the processing result with space as separator
     * @throws IOException
     */
    private String cnProcess(String text) throws IOException {
        //删除html标记
        text = deleteHtmlTag(text);
        //删除中文特殊字符
        text = deleteChineseSepcialChar(text);
        //中文分词
        JiebaSegmenter segmenter = new JiebaSegmenter();
        List<SegToken> tokens = segmenter.process(text, JiebaSegmenter.SegMode.SEARCH);
        List<String> words = new ArrayList<>();
        for(SegToken token : tokens){
            words.add(token.word);
        }
        //过滤空白词
        words.removeIf(e -> e.length() == 0);
        //读取中文停用词
        ArrayList<String> chineseStopwords = new ArrayList<>(Arrays.asList(FileAction.readFile("stopwords/cn_stopwords.txt").split(" ")));
        //删除中文停用词
        words.removeAll(chineseStopwords);
        //以空格符为间隔输出结果
        StringBuffer res = new StringBuffer();
        for(String word : words){
            res.append(word).append(" ");
        }
        return res.toString();
    }

    /**
     * @description process english text
     * @param text -- the content to be processed
     * @return the processing result with space as separator
     * @throws IOException
     */
    private String enProcess(String text) throws IOException {
        //转换为小写字母
        text = text.toLowerCase();
        text = deleteHtmlTag(text);
        //删除英文特殊字符
        text = deleteEnglishSepcialChar(text);
        //英文分词
        List<String> words = new ArrayList<>(Arrays.asList(text.split(" ")));
        //过滤空白词
        words.removeIf(e -> e.length() == 0);
        //读取英文停用词
        List<String> englishStopwords = new ArrayList<>(Arrays.asList(FileAction.readFile("stopwords/en_stopwords.txt").split(" ")));
        //删除英文停用词
        words.removeAll(englishStopwords);
        //Porter Stemming方法提取词干
        Stemmer s = new Stemmer();
        StringBuffer res =  new StringBuffer();
        for(String word : words){
            s.add(word);
            s.stem();
            res.append(s).append(" ");
        }
        //储存Porter-Stemming结果
        String[] stemmingWords = res.toString().split(" ");
        //输出结果
        res =  new StringBuffer();
        for(String stemmingWord : stemmingWords){
            res.append(stemmingWord).append(" ");
        }
        return res.toString();
    }

    /**
     * @description Process the given text.
     *              Support language : chinese, english
     * @param text -- the content to be processed
     * @param language -- the text language type
     * @return the processing result with space as separator
     * @throws Exception
     */
    public String process(String text,String language) throws Exception {
        language = language.toLowerCase();
        HashMap<String, Method> methodMapper = new HashMap<>();
        methodMapper.put("chinese",TextPreprocessor.class.getDeclaredMethod("cnProcess", String.class));
        methodMapper.put("english",TextPreprocessor.class.getDeclaredMethod("enProcess", String.class));
        Method method = methodMapper.get(language);
        return method.invoke(new TextPreprocessor(),text).toString();
    }

    public boolean process(File file,String language) throws Exception {
        //读取文件
        String text = FileAction.readFile(file.getPath());
        return FileAction.writeFile(process(text,language),file.getAbsolutePath()+"_Processed.txt");
    }

    public static void main(String[] args) throws Exception {
        /**
         * 中文文档预处理
         */
        File cnFile = new File("src/test/resources/chinese/1_C_Org.html");
        new TextPreprocessor().process(cnFile,"chinese");
        /**
         * 英文文档预处理
         */
        File enFile = new File("src/test/resources/english/1_E_Org.html");
        new TextPreprocessor().process(enFile,"english");
    }
}

