package com.zhanghongshen.searchengine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

/**
 * @author Zhang Hongshen
 * @description Some file actions
 * @date 2021/5/8
 */
public class FileAction {
    /**
     * @description Read all files from the existing directory,including the subdirectory.
     *              If the directory path doesn't exist or the file is not a directory,
     *              then throw a IOException
     * @param directoryPath the file directory path
     * @return file list reading from the directory
     * @throws IOException
     */
    public static File[] readDirectory(String directoryPath){
        File file = new File(directoryPath);
        try {
            if(!file.exists() || !file.isDirectory()){
                throw  new IOException();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return file.listFiles();
    }

    /**
     * @description Read all files from the existing file.
     *              If the file path doesn't exist or the file is not a file,
     *              then throw a IOException
     * @param filePath the file path
     * @return content reading from the file
     * @throws IOException
     */
    public static String readFile(String filePath){
        StringBuffer res = new StringBuffer();
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
                res.append(s.trim());
            }
            bufferedReader.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return res.toString();
    }

    /**
     * @description Write content into a file.
     *              If the file has existed, then it will overwrite the existing file.
     * @param content content to be writed into file
     * @param filePath file path
     * @return true  -- if writing file succeed
     *         false -- if writing file failed
     */
    public static boolean writeFile(String content,String filePath){
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.write(content);
            bufferedWriter.flush();
            bufferedWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean writeFile(Collection<String> strs, String filePath){
        try {
            FileWriter fileWriter = new FileWriter(filePath);
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for(String str : strs){
                bufferedWriter.write(str);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
            bufferedWriter.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
