package com.zhanghongshen.searchengine;

import java.util.Random;

/**
 * @author Zhang Hongshen
 * @description Create random User-Agent
 * @date 2021/5/13
 */
public class UserAgent {
    public static final String CHROME = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36";
    public static final String EDGE = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.93 Safari/537.36 Edg/90.0.818.56";
    public static final String FIREFOX = "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:88.0) Gecko/20100101 Firefox/88.0";
    public static final String[] BROWSER = {CHROME,EDGE,FIREFOX};
    public static String random(){
        Random rand = new Random();
        return BROWSER[rand.nextInt(3)];
    }

    public static void main(String[] args) {
        System.out.println(UserAgent.random());
    }
}
