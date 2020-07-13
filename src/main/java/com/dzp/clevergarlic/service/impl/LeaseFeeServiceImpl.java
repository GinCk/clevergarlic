package com.dzp.clevergarlic.service.impl;

import com.dzp.clevergarlic.dto.admin.leaseFeeDto.EditLeaseFeeRequest;
import com.dzp.clevergarlic.enums.CommonStatusEnum;
import com.dzp.clevergarlic.mapper.admin.LeaseFeeMapper;
import com.dzp.clevergarlic.service.LeaseFeeService;
import com.dzp.clevergarlic.util.IdUtil.Sid;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 租金预测参数
 * @Auther ck
 * @Date 2020/7/13 13:13
 * @Desc
 */
@Service
public class LeaseFeeServiceImpl implements LeaseFeeService {

    @Autowired
    Sid sid;

    @Autowired
    LeaseFeeMapper leaseFeeMapper;

    @Override
    public void editLeaseFee(EditLeaseFeeRequest request) {

        if (request.getOperation() == null) {
            throw new RuntimeException("操作类型必传");
        }
        if (request.getOperation() == 1) {
            request.setStatus(CommonStatusEnum.REVIEW_SHZ.getCode());
        } else {
            request.setStatus(CommonStatusEnum.REVIEW_CGX.getCode());
        }

        // 数据校验
        checkParameters(request);

        if (ObjectUtils.isEmpty(request.getLeaseFeeId())) {// 新增
            // request.setLeaseFeeId(sid.nextShort());
            // do insert
            leaseFeeMapper.insertLeaseFee(request);

        } else {// 编辑
            // do update
            leaseFeeMapper.updateLeaseFee(request);
        }

    }

    private void checkParameters(EditLeaseFeeRequest request) {

        if (request.getOperation() == 1) {}
    }
}