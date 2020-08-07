package com.dzp.clevergarlic.service.admin.impl;

import com.dzp.clevergarlic.dto.admin.budgetPlanDTO.request.*;
import com.dzp.clevergarlic.dto.admin.budgetPlanDTO.response.PlanInfoResponse;
import com.dzp.clevergarlic.dto.admin.budgetPlanDTO.response.PlanListResponse;
import com.dzp.clevergarlic.dto.admin.budgetPlanDTO.response.ReadyCommitResponse;
import com.dzp.clevergarlic.dto.admin.calculateDTO.BeforeCalculate;
import com.dzp.clevergarlic.dto.admin.calculateDTO.BuildingInfo;
import com.dzp.clevergarlic.dto.admin.calculateDTO.VersionInfo;
import com.dzp.clevergarlic.entity.PlanBuildingEntity;
import com.dzp.clevergarlic.enums.CodeNumberEnum;
import com.dzp.clevergarlic.enums.CommonStatusEnum;
import com.dzp.clevergarlic.enums.ExceptionMsg;
import com.dzp.clevergarlic.mapper.admin.BudgetPlanMapper;
import com.dzp.clevergarlic.result.Result;
import com.dzp.clevergarlic.result.ResultVo;
import com.dzp.clevergarlic.service.admin.BudgetPlanService;
import com.dzp.clevergarlic.util.CodeUtil;
import com.dzp.clevergarlic.util.DateUtil;
import com.dzp.clevergarlic.util.IdUtil.Sid;
import com.dzp.clevergarlic.util.PageUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 预算计划
 * @Auther ck
 * @Date 2020/7/22 13:54
 * @Desc
 */

@Service
public class BudgetPlanServiceImpl implements BudgetPlanService {

    @Autowired
    Sid sid;

    @Autowired
    BudgetPlanMapper budgetPlanMapper;

    @Autowired
    private ApplicationEventPublisher publisher;

    /**
     * 计划列表
     * @param request
     * @return
     */
    @Override
    public PageUtil<PlanListResponse> getPlanList(GetPlanListRequest request) {

        Map<String, Object> map = new HashMap<>();
        if (ObjectUtils.isNotEmpty(request.getPlanName())) {
            map.put("planName", request.getPlanName());
        }
        if (ObjectUtils.isNotEmpty(request.getStatus())) {
            map.put("status",request.getStatus());
        }
        if (ObjectUtils.isNotEmpty(request.getStartDate())) {
            map.put("startDate", DateUtil.getDateToString("yyyy-MM-dd",request.getStartDate()));
        }
        if (ObjectUtils.isNotEmpty(request.getEndDate())) {
            map.put("endDate", DateUtil.getDateToString("yyyy-MM-dd",request.getEndDate()));
        }
        if (ObjectUtils.isNotEmpty(request.getAdminName())) {

            // TODO: 2020/7/24 根据adminName获取adminId
            Long adminId = 10000001L;
            map.put("adminId",adminId);
        }
        if (ObjectUtils.isNotEmpty(request.getPlanCode())) {
            map.put("planCode",request.getPlanCode());
        }
        // 分页查询
        PageInfo<PlanListResponse> infoList = PageHelper.startPage(request.getPage(), request.getPageSize()).doSelectPageInfo(() -> budgetPlanMapper.getPlanList(map));

        infoList.getList().forEach(res -> {
            if (res.getStatus() != null) {
                res.setStatusName(CommonStatusEnum.getMsgByCode(res.getStatus()));
            }
            if (res.getAdminId() != null) {
                res.setAdminName("name");
            }
        });
        return new PageUtil<>(infoList);
    }

    /**
     * 保存计划
     * @param request
     */
    @Override
    public ResultVo savePlan(SavePlanRequest request, String type) {

        AtomicInteger sucNum = new AtomicInteger();
        request.setStatus(CommonStatusEnum.REVIEW_XJ.getCode());
        request.setAdminId(10000001L);

        if (ObjectUtils.isEmpty(request.getPlanId())) {// 新增

            request.setPlanId(sid.nextShort());

            // TODO: 2020/7/23 公司编号获取
            String companyCode = "";
            request.setPlanCode(CodeUtil.getCodeNumber(CodeNumberEnum.CODE_YCJH.getPrefix(), CodeNumberEnum.CODE_YCJH.getLength(),companyCode));
            request.setPlanVersion("v" + DateUtil.getDateTime());
            budgetPlanMapper.insertToPlan(request);

        } else {// 编辑

            budgetPlanMapper.updatePlan(request);
            budgetPlanMapper.deleteBuildingByPlanId(request.getPlanId());
        }

        // 存入计划子表-楼宇信息
        request.getPlanBuildingList().forEach(res -> {
            PlanBuildingEntity req = new PlanBuildingEntity();
            req.setPlanBuildingId(sid.nextShort());
            req.setPlanId(request.getPlanId());
            BeanUtils.copyProperties(res, req);
            budgetPlanMapper.insertToBuilding(req);
            sucNum.getAndIncrement();
        });

        if (sucNum.get() == request.getPlanBuildingList().size()) {
            return Result.success(type);
        } else {
            budgetPlanMapper.deleteBuildingByPlanId(request.getPlanId());
            return Result.error(ExceptionMsg.FAILED,"保存楼宇信息失败", type);
        }
    }

    /**
     * 计划详情
     * @param planId
     * @return
     */
    @Override
    public PlanInfoResponse getPlanInfo(String planId) {

        PlanInfoResponse info = budgetPlanMapper.getPlanInfo(planId);
        if (info.getAdminId() != null) {
            info.setAdminName("name");
        }
        return info;
    }

    /**
     * 计划删除
     * @param request
     * @return
     */
    @Override
    public ResultVo deletePlan(DeletePlanRequest request, String type) {

        for (String id : request.getIds()) {
            PlanInfoResponse info = budgetPlanMapper.getPlanInfo(id);
            if (info == null) {
                return Result.error(ExceptionMsg.FAILED,"选中的计划不存在", type);
            }

            budgetPlanMapper.deleteBuildingByPlanId(id);
            budgetPlanMapper.deletePlan(id);
        }
        return Result.success(type);
    }

    /**
     * 计划确认
     * @param request
     * @return
     */
    @Override
    public ResultVo reviewPlan(ReviewPlanRequest request, String type) {

        if (request.getOperation() == 1) {

            PlanInfoResponse info = budgetPlanMapper.getPlanInfo(request.getPlanId());
            if (!CommonStatusEnum.REVIEW_XJ.getCode().equals(info.getStatus())) {
                return Result.error(ExceptionMsg.FAILED,"状态错误，不可确认！", type);
            }

            Long adminId = 10000001L;
            Map<String, Object> map = new HashMap<>();
            map.put("planId",request.getPlanId());
            map.put("status",CommonStatusEnum.REVIEW_YQR.getCode());
            map.put("adminId",adminId);
            budgetPlanMapper.reviewPlan(map);

            // publisher.publishEvent(new ReadyCommitEvent("计划确认，生成代办信息",request.getPlanId(),request.getOperation(),adminId));
        }
        return Result.success(type);
    }

    /**
     * 预测参数列表
     * @param request
     * @return
     */
    @Override
    public PageUtil<ReadyCommitResponse> readyCommitList(ReviewPlanListRequest request) {

        Map<String, Object> map = new HashMap<>();
        if (ObjectUtils.isNotEmpty(request.getPlanName())) {
            map.put("planName", request.getPlanName());
        }
        if (ObjectUtils.isNotEmpty(request.getStatus())) {
            map.put("status",request.getStatus());
        }
        if (ObjectUtils.isNotEmpty(request.getStartDate())) {
            map.put("startDate", DateUtil.getDateToString("yyyy-MM-dd",request.getStartDate()));
        }
        if (ObjectUtils.isNotEmpty(request.getEndDate())) {
            map.put("endDate", DateUtil.getDateToString("yyyy-MM-dd",request.getEndDate()));
        }
        if (ObjectUtils.isNotEmpty(request.getPlanCode())) {
            map.put("planCode",request.getPlanCode());
        }
        if (ObjectUtils.isNotEmpty(request.getParamType())) {
            map.put("paramType",request.getParamType());
        }
        if (ObjectUtils.isNotEmpty(request.getBuildingName())) {
            map.put("buildingName",request.getBuildingName());
        }
        // 分页查询
        PageInfo<ReadyCommitResponse> infoList = PageHelper.startPage(request.getPage(), request.getPageSize()).doSelectPageInfo(() -> budgetPlanMapper.readyCommitList(map));
        return new PageUtil<>(infoList);
    }

    /**
     * 计算
     * @describe 计算接口：将计划/参数的版本号统一成唯一的版本号作为键传给BI，最终会通过此版本号和计划id拿到结果
     * @param request
     */
    @Override
    public ResultVo calculate(CalculateRequest request, String type) {

        BeforeCalculate beforeCalculate = budgetPlanMapper.getBeforeCalculate(request.getPlanId());
        for (BuildingInfo building : beforeCalculate.getBuildingParamData()) {

            // TODO: 2020/8/5 写入租金/招商参数

        }

        // 版本号记录
        VersionInfo versionInfo = budgetPlanMapper.getVersionInfo(request.getPlanId());
        versionInfo.setColumnId(sid.nextShort());
        Integer res = budgetPlanMapper.insertVersion(versionInfo);
        if (res <= 0) {
            return Result.error(ExceptionMsg.FAILED,"版本记录失败！", type);
        }
        beforeCalculate.setVersion(versionInfo.getColumnId());

        // 状态级联更新（已参与计算）
        budgetPlanMapper.updateStatusById(request.getPlanId(), CommonStatusEnum.REVIEW_YJS.getCode());

        // 调外部接口参与计算


        return Result.success(type);
    }

    /**
     * 取消计算
     * @param request
     * @return
     */
    @Override
    public ResultVo cancelCalculate(CalculateRequest request, String type) {

        PlanInfoResponse info = budgetPlanMapper.getPlanInfo(request.getPlanId());
        Assert.notNull(info);
        if (!CommonStatusEnum.REVIEW_YJG.getCode().equals(info.getStatus())) {
            return Result.error(ExceptionMsg.FAILED,"该计划上次计算未生成计算结果，不能取消计算！", type);
        }
        budgetPlanMapper.updateStatusById(request.getPlanId(), CommonStatusEnum.REVIEW_YQX.getCode());
        return Result.success(type);
    }

    /**
     * 计划预览
     * @param planId
     * @return
     */
    @Override
    public ResultVo planPreview(String planId, String type) {

        PlanInfoResponse info = budgetPlanMapper.getPlanInfo(planId);
        Assert.notNull(info);
        if (!CommonStatusEnum.REVIEW_YTW.getCode().equals(info.getStatus())) {
            return Result.error(ExceptionMsg.FAILED,"该计划有参数未填写完成！", type);
        }
        return Result.success(type);
    }
}
