package me.bramar.task.entity.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 检查代理返回对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckAgentDO {
    /**
     * ip地址
     */
    private String ip;
    /**
     * 国家
     */
    private String country;

    /**
     * 国家代码
     */
    private String country_code;

    /**
     * 省份缩写编码
     */
    private String region_code;

    /**
     * 地区(州)中文
     */
    private String region_cn;

    /**
     * 拼音
     */
    private String region;
    /**
     * 城市
     */
    private String city;
    /**
     * 时区
     */
    private String timezone;

    /**
     * 邮编
     */
    private String postal;
    /**
     * 是否已使用
     */
    private Boolean used;
}
