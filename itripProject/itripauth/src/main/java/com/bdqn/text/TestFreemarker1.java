package com.bdqn.text;

import com.bdqn.entity.People;
import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestFreemarker1 {
    public static void main(String[] args) throws Exception {
        Map map=new HashMap();
        List list=new ArrayList();
        for (int i = 0; i < 10; i++) {
            People people=new People("name"+i,i);
            list.add(people);
        }
        map.put("a",list);
        Configuration config=new Configuration();
        config.setDefaultEncoding("utf-8");
        config.setDirectoryForTemplateLoading(new File("E:\\Maven\\ItripProject\\itripProject\\itripauth\\src\\main\\resources"));
        Template template=config.getTemplate("2.flt");
        template.process(map,new FileWriter("E:\\2.html"));
    }
}
