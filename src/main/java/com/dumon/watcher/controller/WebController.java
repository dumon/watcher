package com.dumon.watcher.controller;

import com.dumon.watcher.entity.Device;
import com.dumon.watcher.repo.DeviceRepository;
import com.dumon.watcher.service.DeviceManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;
import javax.annotation.Resource;

@Controller
public class WebController {

    @Resource
    private DeviceRepository deviceRepository;
    @Resource
    private DeviceManager deviceManager;

    @GetMapping({"/", "/home"})
    public ModelAndView home(final Map<String, Object> model) {
        Iterable<Device> devices = deviceRepository.findAll();
        String currentNetwork = deviceManager.getCurrentNetwork();

        ModelAndView mav = new ModelAndView("home");
        mav.addObject("devices", devices);
        mav.addObject("network", currentNetwork);

        return mav;
    }

    @GetMapping("/login")
    public String login(final Model model) {
        return "login";
    }
}
