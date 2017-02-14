/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.yugong.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.quancheng.yugong.service.TaskBizService;

/**
 * @author shimingliu 2017年2月13日 下午3:47:09
 * @version SyncTaskController.java, v 0.0.1 2017年2月13日 下午3:47:09 shimingliu
 */
@Controller
public class SubmitJobController {

    @Autowired
    private TaskBizService syncTaskService;

    @RequestMapping(value = "/addJob", method = RequestMethod.GET)
    public ModelAndView index() {
        return new ModelAndView("/task/task");
    }

    @RequestMapping(value = "/submitJob", method = RequestMethod.POST)
    public String submitJob(@ModelAttribute("setting") String setting) {
        try {
            JsonObject settingJson = new JsonParser().parse(setting).getAsJsonObject();
            String settingCopy = settingJson.toString();
            Boolean success = syncTaskService.submitSyncTask(settingCopy);
            if (success) return "redirect:/jobs ";
            else throw new IllegalArgumentException("save config to data base failed");
        } catch (JsonSyntaxException e) {
            throw e;
        }

    }
}
