/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.yugong.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author shimingliu 2017年2月13日 下午4:01:25
 * @version JobsController.java, v 0.0.1 2017年2月13日 下午4:01:25 shimingliu
 */
@Controller
public class JobsController {

    @RequestMapping(value = "/jobs", method = RequestMethod.GET)
    public ModelAndView index() {
        return new ModelAndView("/task/tasks");
    }
}
