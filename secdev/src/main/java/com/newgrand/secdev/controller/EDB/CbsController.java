package com.newgrand.secdev.controller.EDB;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.newgrand.secdev.config.IJdbcTemplate;
import com.newgrand.secdev.domain.EDB.CbsModel;
import com.newgrand.secdev.domain.EDB.CntModel;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 成本科目
 * @Author ChenXiangLu
 * @Date 2020/11/27 17:46
 * @Version 1.0
 */
@Slf4j
@RestController
@RequestMapping("/edbApi")
@Api("经济数据库接口")
public class CbsController {

    @Autowired
    @Resource(name = "jdbcTemplateOrcle")
    protected IJdbcTemplate jdbcTemplate;
    @Autowired
    private CloseableHttpClient httpClient;

    @Value("${i8.url}")
    private String i8url;
    @Value("${i8.edb.url.cbs}")
    private String cbsUrl;

    @ApiOperation(value="推送CBS数据到经济数据库", notes="推送CBS数据到经济数据库")
    @RequestMapping(value = "/test",method = RequestMethod.GET)
    public String test()
    {
        String jsonString="{\"data\":null,\"error\":\"认证失败\",\"errorCode\":10000401,\"success\":false,\"timestamp\":1608284069963,\"traceId\":\"8672ee91688e578ad9e3f0eb7c1cf00b\"}";
        JSONObject root = JSON.parseObject(jsonString);
        if(root.get("data")==null)
        {
        }
        String dataStr = root.get("data").toString();
        JSONObject data = JSON.parseObject(dataStr);
        System.out.println("i8地址:"+i8url);
        return  "测试成功";
    }

    @ApiOperation(value="推送CBS数据到经济数据库", notes="推送CBS数据到经济数据库", produces="application/json")
    @RequestMapping(value = "/getCbs",method = RequestMethod.GET)
    public String syncCbs()
    {
        ///cbs_status这个是区分cbs清单库和项目cbs的
        // cbs_status='0' 表示清单库，
        // cbs_status='1'表示项目cbs引用清单库，
        // cbs_status=''  表示项目cbs自己新增
        var sql="select t1.cbs_code,t1.cbs_name,t1.note,t1.cbs,t3.cbs_code parent_code,t1.remarks,t2.pc_no\n" +
                "from bd_cbs t1\n" +
                "left join project_table t2 on t2.phid=t1.pcid\n" +
                "left join bd_cbs t3 on t3.phid=t1.parentphid\n" +
                "where t1.cbs_status!='0'";
        RowMapper<CbsModel> rowMapper=new BeanPropertyRowMapper(CbsModel.class);
        List<CbsModel> cbs= jdbcTemplate.query(sql, rowMapper);
        HttpPost httpPost  = new HttpPost(cbsUrl);
        httpPost.addHeader("Content-Type","application/json");
        HttpEntity entity = new StringEntity("{\"cbs_data\":"+JSONObject.toJSONString(cbs)+"}","utf-8");
        httpPost.setEntity(entity);
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(httpPost);
            StatusLine status = response.getStatusLine();
            int state = status.getStatusCode();
            if (state == HttpStatus.SC_OK) {
                HttpEntity responseEntity = response.getEntity();
                String jsonString = EntityUtils.toString(responseEntity);
                JSONObject root = JSON.parseObject(jsonString);
                String form = root.get("code").toString();
            }
            else
                {
                System.out.println("推送失败"+ EntityUtils.toString(response.getEntity()));
            }
        }
        catch (Exception e)
        {
            System.out.println("推送异常"+e.getMessage());
        }
        return  "测试成功";
    }
}
