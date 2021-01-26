package com.bdqn.controller;

import cn.itrip.common.*;
import cn.itrip.dao.itripUser.ItripUserMapper;
import cn.itrip.pojo.ItripUser;
import cn.itrip.pojo.ItripUserVO;
import com.alibaba.fastjson.JSONArray;
import cz.mallat.uasparser.UserAgentInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

@Controller
public class UserController {

    @Resource
    ItripUserMapper dao;
    @Resource
    JredisApi jredisApi;
    @Resource
    SMS_Sent sms_sent;
    @Resource
    Email_Sent email_sent;

    /**
     * 检查用户是否已注册
     * @param name
     * @return
     */
    @ApiOperation(value="用户名验证",httpMethod = "GET",
            protocols = "HTTP", produces = "application/json",
            response = Dto.class,notes="验证是否已存在该用户名")
    @RequestMapping(value="/api/ckusr",method=RequestMethod.GET,produces= "application/json")
    public @ResponseBody
    Dto checkUser(
            @ApiParam(name="name",value="被检查的用户名",defaultValue="test@bdqn.cn")
            @RequestParam String name) {
        try {
		/*	if(!validEmail(name))
				return  DtoUtil.returnFail("请使用正确的邮箱地址注册",ErrorCode.AUTH_ILLEGAL_USERCODE);*/
            if (null == dao.getUserCode(name))
            {
                return DtoUtil.returnSuccess("用户名可用");
            }
            else
            {
                return DtoUtil.returnFail("用户已存在，注册失败","404");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return DtoUtil.returnFail(e.getMessage(), "404");
        }
    }

    /***
     * 邮箱激活
     */
    @RequestMapping(value = "/api/activate",method = RequestMethod.PUT,produces="application/json; charset=utf-8")
    @ResponseBody
    public  Dto emailactive(String user,String code,HttpServletRequest request){
        //判断reids中是否有数据
        String oldcode=jredisApi.getRedis(user);
        if (oldcode!=null&&oldcode.equals(code)){
            //如果有数据那么把刚才插入到数据库中的数据激活
            dao.jihuo(user);
            return DtoUtil.returnSuccess("激活成功");
        }
        return  DtoUtil.returnFail("激活失败","404");
    }

    /***
     * 邮箱注册
     */
    @RequestMapping(value = "/api/doregister",method = RequestMethod.POST,produces="application/json; charset=utf-8")
    @ResponseBody
    public Dto email(@RequestBody ItripUserVO vo, HttpServletRequest request)throws Exception{
        try {
            //第一步 插入数据库
            ItripUser itripUser=new ItripUser();
            itripUser.setUserCode(vo.getUserCode());
            itripUser.setUserPassword(MD5.getMd5(vo.getUserPassword(),32));
            itripUser.setUserName(vo.getUserName());
            itripUser.setActivated(0);
            ItripUser user=dao.getUserCode(vo.getUserCode());
            dao.insertItripUser(itripUser);
            //第二部 发送邮箱验证码     把邮箱验证码存入到redis中
            Random random=new Random();
            int mess=random.nextInt(9999);
            jredisApi.SetRedis(vo.getUserCode(),""+mess,120);
            //给邮箱发送短信
            email_sent.SentEmail(vo.getUserCode(),""+mess);
            return  DtoUtil.returnSuccess("注册成功");
        }catch (Exception ex)
        {
            return  DtoUtil.returnFail("注册失败","1000");
        }
    }

    /***
     * 给注册的手机号激活
     */
    @RequestMapping(value = "/api/validatephone",method = RequestMethod.PUT,produces="application/json; charset=utf-8")
    @ResponseBody
    public  Dto phone(String user,String code,HttpServletRequest request){
        //判断reids中是否有数据
        String oldcode=jredisApi.getRedis(user);
        if (oldcode!=null&&oldcode.equals(code)){
            //如果有数据那么把刚才插入到数据库中的数据激活
            dao.jihuo(user);
            return DtoUtil.returnSuccess("激活成功");
        }
        return  DtoUtil.returnFail("激活失败","404");
    }

    /***
     * 手机号注册用户短信验证接口
     */
    @RequestMapping(value = "/api/registerbyphone",method = RequestMethod.POST,produces="application/json; charset=utf-8")
    public @ResponseBody Dto re(@RequestBody ItripUserVO vo, HttpServletRequest request) throws Exception {
        try {
            //第一步 插入数据库
            ItripUser itripUser=new ItripUser();
            itripUser.setUserCode(vo.getUserCode());
            itripUser.setUserPassword(MD5.getMd5(vo.getUserPassword(),32));
            itripUser.setUserName(vo.getUserName());
            itripUser.setActivated(0);
            ItripUser user=dao.getUserCode(vo.getUserCode());
            if (user!=null){
                return  DtoUtil.returnFail("注册失败，手机号已被注册","1000");
            }
            else {
                dao.insertItripUser(itripUser);
                //第二部 发送验证码     把手机号验证码存入到redis中
                Random random=new Random();
                int mess=random.nextInt(9999);
                jredisApi.SetRedis(vo.getUserCode(),""+mess,120);
                //给手机发送短信
                sms_sent.SentSms(vo.getUserCode(),""+mess);
                return  DtoUtil.returnSuccess("注册成功");
            }
        }catch (Exception ex)
        {
            return  DtoUtil.returnFail("登陆失败","1000");
        }
    }

    /***
     * 登录接口
     */
    @RequestMapping(value = "/api/dologin",method = RequestMethod.POST,produces="application/json; charset=utf-8")
    @ResponseBody
    public Dto Dologin(String name, String password, HttpServletRequest request) throws Exception {
        ItripUser user=dao.getlogin(name, MD5.getMd5(password,32));
        //存入redis 中key value 过期时间
        if (user!=null)
        {
            //token= md5 加密 userID+userCode+时间戳
            String agent=request.getHeader("User-Agent");
            String token=generateToken(agent,user);
            jredisApi.SetRedis(token, JSONArray.toJSONString(user),7200);
            ItripTokenVO tokenVO=new ItripTokenVO(token, Calendar.getInstance().getTimeInMillis()+7200,Calendar.getInstance().getTimeInMillis());
            return DtoUtil.returnDataSuccess(tokenVO);
        }
        return  DtoUtil.returnFail("登陆失败","1000");
    }

    /**
     *
     * @param agent 是浏览器上的agent
     * @param user
     * @return
     */
    public String generateToken(String agent, ItripUser user) {
        // TODO Auto-generated method stub
        try {
            UserAgentInfo userAgentInfo = UserAgentUtil.getUasParser().parse(
                    agent);
            StringBuilder sb = new StringBuilder();
            sb.append("token:");//统一前缀
            if (userAgentInfo.getDeviceType().equals(UserAgentInfo.UNKNOWN)) {
                if (UserAgentUtil.CheckAgent(agent)) {
                    sb.append("MOBILE-");
                } else {
                    sb.append("PC-");
                }
            } else if (userAgentInfo.getDeviceType()
                    .equals("Personal computer")) {
                sb.append("PC-");
            } else
                sb.append("MOBILE-");
//			sb.append(user.getUserCode() + "-");
            sb.append(MD5.getMd5(user.getUserCode(),32) + "-");//加密用户名称
            sb.append(user.getId() + "-");
            sb.append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())
                    + "-");
            sb.append(MD5.getMd5(agent, 6));// 识别客户端的简化实现——6位MD5码

            return sb.toString();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
