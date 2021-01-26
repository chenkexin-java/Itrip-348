package com.bdqn.text;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

public class TestFreemarker {
    public static void main(String[] args) throws Exception {
        Map map=new HashMap();
        map.put("a","你好java");
        Configuration config=new Configuration();
        config.setDefaultEncoding("utf-8");
        config.setDirectoryForTemplateLoading(new File("E:\\Maven\\ItripProject\\itripProject\\itripauth\\src\\main\\resources"));
        Template template=config.getTemplate("1.flt");
        template.process(map,new FileWriter("E:\\1.html"));
    }
}
