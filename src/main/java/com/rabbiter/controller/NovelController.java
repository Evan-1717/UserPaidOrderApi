package com.rabbiter.controller;



import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.rabbiter.service.AdvertiserCostService;
import com.rabbiter.service.Impl.NovelServiceImpl;
import com.rabbiter.service.NovelService;
import com.rabbiter.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
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
    private AdvertiserCostService advertiserCostService;

    @Autowired
    private PromotionService promotionService;

    private static final Logger LOGGER = Logger.getLogger(NovelController.class.getName());

    List<List<String>> head = new ArrayList<List<String>>() {
        {
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


}
