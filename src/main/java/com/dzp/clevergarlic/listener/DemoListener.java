package com.dzp.clevergarlic.listener;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.dzp.clevergarlic.dto.excel.EasyExportDto;
import com.dzp.clevergarlic.service.DemoService;

import java.util.ArrayList;
import java.util.List;

/**
 * demo监听器
 * @Auther ck
 * @Date 2020/7/3 15:01
 * @Desc
 */
public class DemoListener extends AnalysisEventListener<EasyExportDto> {

    /**
     * 每1000条存储一次数据库
     */
    private static final int BATCH_COUNT = 10;

    /**
     * 接收读取Excel文件得到的数据
     */
    List<EasyExportDto> list = new ArrayList<>();

    /**
     * 导入的模块
     */
    private DemoService service;

    public DemoListener(DemoService service) {
        this.service = service;
    }

    @Override
    public void invoke(EasyExportDto t, AnalysisContext context) {
        list.add(t);
        if (list.size() >= BATCH_COUNT) {
            saveData();
            list.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
    }

    private void saveData() {
        // doSave;
        System.out.println(list);
    }
}