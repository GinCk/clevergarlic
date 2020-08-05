package com.dzp.clevergarlic.dto.admin.budgetPlanDTO.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Auther ck
 * @Date 2020/8/5 10:50
 * @Desc
 */

@Data
public class BuildingListResponse {

    @ApiModelProperty("楼宇id")
    private Integer buildingId;

    @ApiModelProperty("楼宇名称")
    private String buildingName;
}
