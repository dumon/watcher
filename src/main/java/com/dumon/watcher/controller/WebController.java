package com.dumon.watcher.controller;

import com.dumon.watcher.entity.Device;
import com.dumon.watcher.repo.DeviceRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import javax.annotation.Resource;

@Controller
public class WebController {

    @Resource
    private DeviceRepository deviceRepository;

    @GetMapping({"/", "/home"})
    public ModelAndView home(final Map<String, Object> model) {
        List<Device> devices = deviceRepository.findDeviceByActiveTrue();

        model.put("devices", devices);

        ModelAndView mav = new ModelAndView("home");
        mav.addObject("devices", devices);

        return mav;
    }

    @GetMapping("/login")
    public String login(final Model model) {
        return "login";
    }
}
