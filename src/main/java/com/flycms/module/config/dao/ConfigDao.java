package com.flycms.module.config.dao;

import com.flycms.module.config.model.Guide;
import com.flycms.module.config.model.Config;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConfigDao {

    // ///////////////////////////////
    // ///// 增加 ////////
    // ///////////////////////////////

    /**
     * 添加配置信息
     * @param config
     * @return
     */
    public int addConfig(Config config);

    /**
     * 添加设置导航信息
     * @param guide
     * @return
     */
    public int addGuide(Guide guide);
    // ///////////////////////////////
    // ///// 刪除 ////////
    // ///////////////////////////////

    /**
     * 删除配置
     * @param keycode
     * @return
     */
    public int deleteConfig(@Param("keycode") String keycode);

    /**
     * 修改配置
     * @param config
     * @return
     */
    public int updateConfigByKey(Config config);

    // ///////////////////////////////
    // ///// 查詢 ////////
    // ///////////////////////////////

    /**
     * 按key查询配置信息
     * @param keycode
     * @return
     */
    public Config getConfigByKey(@Param("keycode") String keycode);



    public int getConfigCount();

    /**
     * 获取配置列表
     * @param offset
     * @param rows
     * @return
     */
    public List<Config> getConfigList(@Param("offset")int offset, @Param("rows") int rows);


    /**
     * 所有配置列表信息
     * @return
     */
    public List<Config> getConfigAllList();
}
