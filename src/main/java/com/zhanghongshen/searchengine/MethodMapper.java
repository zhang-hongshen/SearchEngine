package com.zhanghongshen.searchengine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhang Hongshen
 * @description x
 * @date 2021/5/12
 */
public class MethodMapper {
    public int plus(int a,int b){
        return a + b;
    }
    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        HashMap<String, Method> map = new HashMap<>();
        Method method = MethodMapper.class.getDeclaredMethod("plus", int.class, int.class);
        map.put("1",method);
        for(Map.Entry<String,Method> entry : map.entrySet()){
            System.out.println(entry.getValue().invoke(new MethodMapper(),1,2));
        }
    }
}
