### 项目介绍

该项目是使用Java语言开发的简易搜索引擎，其中内置了一个网络爬虫(Web Crawler)。

### 项目环境搭建

| 名称                                  | 功能描述           | 版本号    |
| ------------------------------------- | ------------------ | --------- |
| Windows 10 Professional               | 操作系统           | 20H2      |
| Intellij IDEA                         | 开发IDE            | 2021.1.1  |
| JDK                                   | Java语言开发核心包 | 1.8.0_291 |
| [Jsoup](https://github.com/jhy/jsoup) | 第三方jar包        | 1.13.1    |

### 项目使用

#### Web Crawler

- 支持自定义搜索深度和最大搜索网页数
- 支持多个根网页

```java
//初始化根网页，这里以https://docs.oracle.com/en/java/javase/11/docs/api为跟网页
List<String> rootUrls = new ArrayList<>();
rootUrls.add("https://docs.oracle.com/en/java/javase/11/docs/api");
//4种初始化网络爬虫方式
WebCrawler webCrawler = new WebCrawler(rootUrls);//默认搜索深度为1，最大抓取网页100个
WebCrawler webCrawler = new WebCrawler(rootUrls,0,500);//搜索深度无限制，最大抓取网页500个
WebCrawler webCrawler = new WebCrawler(rootUrls,2,0);//搜索深度为2，最大抓取网页无限制
WebCrawler webCrawler = new WebCrawler(rootUrls,2,500);//搜索深度为2，最大抓取网页500个
//开始爬取网页
webCrawler.start();
//结果保存为html文件
webCrawler.saveAsHtmlPage("src/test/resources/english/");
```



### 文本预处理

本文预处理使用结巴分词实现中文分词，使用Porter Stemming算法实现英文单词词干提取。

#### 中文文档处理

```java
TextPreprocessor processor = new TextPreprocess();
String text = "哈哈哈哈，我是个人才";
String result = processor.process(text,"chinese");
```

#### 英文文档处理

```java
TextPreprocessor processor = new TextPreprocess();
String text = "Hello, this is a simple Search Engine!";
String result = processor.process(text,"english");
```

