/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.yugong.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.xbib.tools.JDBCImporter;

import com.quancheng.yugong.common.YugongUtils;
import com.quancheng.yugong.dto.SyncTaskDTO;
import com.quancheng.yugong.repository.SyncTaskDao;
import com.quancheng.yugong.repository.SyncTaskStateDao;
import com.quancheng.yugong.repository.entity.SyncTaskDO;
import com.quancheng.yugong.repository.entity.SyncTaskStateDO;
import com.quancheng.yugong.vo.SyncTaskVO;

/**
 * @author shimingliu 2017年2月10日 上午11:37:20
 * @version SyncTaskService.java, v 0.0.1 2017年2月10日 上午11:37:20 shimingliu
 */
@Service
public class TaskBizService {

    private static final Logger             logger = LoggerFactory.getLogger(TaskBizService.class);
    @Autowired
    private SyncTaskStateDao                stateDao;

    @Autowired
    private SyncTaskDao                     taskDao;

    @Autowired
    private TaskLocalStoregeComponent       taskLocalStoreService;

    @Autowired
    private TaskScheduleAndExecuteComponent excutorTaskService;

    private Object                          lock   = new Object();

    public Boolean submitTask(String syncSetting) {
        SyncTaskDTO syncTaskDTO = new SyncTaskDTO(syncSetting, taskDao, stateDao);
        synchronized (lock) {
            String index = syncTaskDTO.getIndex();
            String type = syncTaskDTO.getType();
            final JDBCImporter importer = new JDBCImporter();
            try {
                // step1: saveTask into local
                taskLocalStoreService.save(index, type, importer);
                // step2 : saveTask into DB
                SyncTaskDO taskDo = new SyncTaskDO();
                taskDo.setIndex(syncTaskDTO.getIndex());
                taskDo.setType(syncTaskDTO.getType());
                taskDo.setSetting(syncTaskDTO.getSetting());
                taskDo.setExcuteNode(YugongUtils.getLocalHost());
                taskDao.save(taskDo).getId();
                // step3 : run task
                excutorTaskService.executeTask(syncTaskDTO, importer);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw new RuntimeException(String.format("this index %s type %s is running in other machine", index,
                                                         type));
            }
        }
        return Boolean.TRUE;
    }

    public Boolean cancelTask(Integer taskId) {
        SyncTaskDO taskDo = taskDao.findOne(taskId);
        SyncTaskStateDO taskState = taskDo.getSyncTaskState();
        taskState.setIsCanceled(true);
        stateDao.save(taskState);
        SyncTaskDO taskDO = taskState.getSyncTask();
        String index = taskDO.getIndex();
        String type = taskDO.getType();
        taskLocalStoreService.delete(index, type);
        return true;
    }

    public void deleteTask(Integer taskId) {
        SyncTaskDO taskDo = taskDao.findOne(taskId);
        String index = taskDo.getIndex();
        String type = taskDo.getType();
        taskLocalStoreService.delete(index, type);
        taskDao.delete(taskDo);
    }

    public Page<SyncTaskVO> listAllTask(Pageable pageable) {
        Page<SyncTaskDO> syncTaskDoList = taskDao.findAll(pageable);
        Page<SyncTaskVO> syncTaskVoList = syncTaskDoList.map(new Converter<SyncTaskDO, SyncTaskVO>() {

            @Override
            public SyncTaskVO convert(SyncTaskDO source) {
                SyncTaskVO syncTaskVo = new SyncTaskVO();
                syncTaskVo.setCreateTime(source.getCreateTime());
                syncTaskVo.setId(source.getId());
                syncTaskVo.setIndex(source.getIndex());
                syncTaskVo.setType(source.getType());
                syncTaskVo.setSourceSetting(source.getSetting());
                syncTaskVo.setExcuteNode(source.getExcuteNode());
                SyncTaskStateDO stateDO = source.getSyncTaskState();
                if (stateDO != null) {
                    syncTaskVo.setStateSetting(stateDO.getStateSetting());
                    syncTaskVo.setStateUpdateTime(stateDO.getUpdateTime());
                    syncTaskVo.setIsCanceled(stateDO.getIsCanceled());
                }
                return syncTaskVo;
            }

        });
        return syncTaskVoList;
    }

}
