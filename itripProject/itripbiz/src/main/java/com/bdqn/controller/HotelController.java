package com.bdqn.controller;

import cn.itrip.common.Dto;
import cn.itrip.common.DtoUtil;
import cn.itrip.dao.itripAreaDic.ItripAreaDicMapper;
import cn.itrip.dao.itripLabelDic.ItripLabelDicMapper;
import cn.itrip.pojo.ItripAreaDic;
import cn.itrip.pojo.ItripLabelDic;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class HotelController {
    @Resource
    ItripAreaDicMapper dao;
    @Resource
    ItripLabelDicMapper dic;

    @RequestMapping(value = "/api/hotel/queryhotcity/{id}",method = RequestMethod.GET,produces="application/json; charset=utf-8")
    @ResponseBody
    public Dto hotel(@PathVariable("id")int id) throws Exception {
        List<ItripAreaDic> list= dao.ishot(id);
        return DtoUtil.returnDataSuccess(list);
    }
    @RequestMapping(value = "/api/hotel/queryhotelfeature",method = RequestMethod.GET,produces="application/json; charset=utf-8")
    @ResponseBody
    public Dto hotel() throws Exception {
        List<ItripLabelDic> list= dic.list();
        return DtoUtil.returnDataSuccess(list);
    }
}
