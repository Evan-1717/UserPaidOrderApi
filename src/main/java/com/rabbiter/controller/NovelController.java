package com.rabbiter.controller;



import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.rabbiter.service.AdvertiserCostService;
import com.rabbiter.service.Impl.NovelServiceImpl;
import com.rabbiter.service.Impl.PlayletServiceImpl;
import com.rabbiter.service.NovelService;
import com.rabbiter.service.PlayletService;
import com.rabbiter.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

/**
 * <p>
 *  前端控制器：仓库管理模块
 * </p>
 *
 * @author rabbiter
 * @since 2023-01-05
 */
@RestController
@RequestMapping("/api/novel")
public class NovelController {
    @Autowired
    private NovelService novelService;

    @Autowired
    private PlayletService playletService;

    @Autowired
    private AdvertiserCostService advertiserCostService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private RestTemplate restTemplate;

    private static final Logger LOGGER = Logger.getLogger(NovelController.class.getName());

    List<List<String>> head = new ArrayList<List<String>>() {
        {
            add(new ArrayList<String>(Arrays.asList(new String[]{"平台"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"素材id"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"小说ID"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"小说名称"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"热度值"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"点赞数"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"评论数"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"分享数"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"收藏数"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"视频ID"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"视频链接"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"标题"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"发布时间"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"创建时间"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"更新时间"})));
        }
    };

    List<List<String>> head1 = new ArrayList<List<String>>() {
        {
            add(new ArrayList<String>(Arrays.asList(new String[]{"短剧ID"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"短剧名称"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"素材ID"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"唯一ID"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"标题"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"宽"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"高"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"时长"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"封面链接"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"视频链接"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"点赞数"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"评论数"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"分享数"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"收藏数"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"热度"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"创建时间"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"更新时间"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"素材类型"})));
            add(new ArrayList<String>(Arrays.asList(new String[]{"MD5"})));
        }
    };
    // [miniseriesId, miniseriesName, materialId, uniqueId, title, width, height, duration, coverUrl, videoUrl, likeCount, commentCount, shareCount, favoriteCount, heat, createTime, updateTime, materialType, videoMd5]
    @Scheduled(cron = "0 0 * * * ?")
    public void dealTopNovel(){
        novelService.dealTopNovel();
        advertiserCostService.dealAdvertiserCost();
        advertiserCostService.dealDailyAdvertiser();
//        promotionService.dealPromotion();
    }

    /*
     * dealTopNovel_test
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping(value = "/dealTopNovel_test")
    @ResponseBody
    public void dealTopNovel_test(){
        novelService.dealTopNovel();
    }

    /*
     * dealTopNovel_test
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping(value = "/dealTopPlaylet_test")
    @ResponseBody
    public void dealTopPlaylet_test(){
        playletService.dealTopPlaylet();
    }

    /*
     * dealAdvertiserCost_test
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping(value = "/dealAdvertiserCost_test")
    @ResponseBody
    public void dealAdvertiserCost_test(){
        advertiserCostService.dealAdvertiserCost();
    }

    /*
     * dealAdvertiserCost_test
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping(value = "/dealDailyAdvertiser_test")
    @ResponseBody
    public void dealDailyAdvertiser_test(){
        advertiserCostService.dealDailyAdvertiser();
    }

    /*
     * dealAllNovel
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping(value = "/dealAllNovel")
    @ResponseBody
    public void dealAllNovel(){
        novelService.dealAllNovel();
    }

    /*
     * exporttopnovel
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping(value = "/exporttopPlaylet")
    @ResponseBody
    public void exporttopPlaylet(@RequestBody HashMap<String, String> param, HttpServletResponse response){
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("content-disposition", "attachment;fileName="+ System.currentTimeMillis() + ".xlsx");// 设定输出文件头，这里fileName=后面跟的就是文件的名称，可以随意更改
        OutputStream os = null;// 取得输出流
        try {
            os = response.getOutputStream();
            ExcelWriter excelWriter = EasyExcel.write(os).build();
            for(int i = 0; i< PlayletServiceImpl.top100_24.size(); i++) {
                WriteSheet writeSheet = EasyExcel.writerSheet(i, PlayletServiceImpl.top100_24_time.get(i)).head(head1).build();
                excelWriter.write(PlayletServiceImpl.top100_24.get(i), writeSheet);
            }
            excelWriter.finish();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * exporttopnovel
     * @author rabbiter
     * @date 2023/1/5 19:42
     */
    @PostMapping(value = "/exporttopnovel")
    @ResponseBody
    public void exportTopNovel(@RequestBody HashMap<String, String> param, HttpServletResponse response){
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("content-disposition", "attachment;fileName="+ System.currentTimeMillis() + ".xlsx");// 设定输出文件头，这里fileName=后面跟的就是文件的名称，可以随意更改
        OutputStream os = null;// 取得输出流
        try {
            os = response.getOutputStream();
            ExcelWriter excelWriter = EasyExcel.write(os).build();
            for(int i = 0; i<NovelServiceImpl.top100_24.size(); i++) {
                WriteSheet writeSheet = EasyExcel.writerSheet(i, NovelServiceImpl.top100_24_time.get(i)).head(head).build();
                excelWriter.write(NovelServiceImpl.top100_24.get(i), writeSheet);
            }
            excelWriter.finish();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @PostMapping("/playleInfo_test")
    public String playleInfo(@RequestBody Map<String, String> params){
        long time = Instant.now().getEpochSecond() * 1000;
        int page = 1;
        String url = "https://dongxiaoju.com/api/v4/data/material?page=0&size=10&sort=update_time,desc";
        Map param = new HashMap() {
            {
                put("startTime", time - 3600000);
                put("endTime",   time);
            }
        };
        HttpHeaders headers = new HttpHeaders();
        headers.add("token",  params.get("token"));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String,String>> entity = new HttpEntity<>(param, headers);
        ResponseEntity<Map> res = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
        List<Map<String,Object>> list = (List<Map<String,Object>>)((Map<String, Object>)res.getBody().get("data")).get("list");

        if (list.size()>0) {
            return "";
        }
        return null;
    }
}
