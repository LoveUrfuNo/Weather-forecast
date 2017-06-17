package com.testlinenergo.controller;

import com.testlinenergo.service.ExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * заппппооооооолнить!!!!!!!!!!!!
 */
@Controller
public class MainController {
    @Autowired
    private ExcelService excelService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String start() {
        return "start-page";
    }

    @RequestMapping(value = "/download", method = RequestMethod.GET)
    public String download() {
        if(excelService.createFile()) {
            System.out.println("File was successfully created");
        } else {
            System.out.println("File was not created");

            return "download-error";
        }

        return "redirect:/";
    }
}
