package com.odafa.cloudapp.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.odafa.cloudapp.configuration.ConfigReader;
import com.odafa.cloudapp.dto.DroneInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class BaseController {
    
    private final ConfigReader configurations;

    @ResponseBody
    @GetMapping("/updateSystemInfo")
    public String updateSysteminfo () {

        final Gson gson = new Gson();
        final List<DroneInfo> drones = new ArrayList<>();
        
        DroneInfo dto1 = new DroneInfo("1", 1.553504, 110.359291, 0.0468, 7.1, 12.3, "ONLINE");
        DroneInfo dto2 = new DroneInfo("2", -6.046810, 107.246430, 0.0468, 3, 21, "ONLINE");
        drones.add(dto1);
        drones.add(dto2);

        return gson.toJson(drones);
    }

    @GetMapping("/")
    public String indexPage(Model model){

        model.addAttribute("publicIP", getPublicIpAddress());
        model.addAttribute("defaultSpeed", configurations.getDefaultSpeed());
        model.addAttribute("defaultAltitude", configurations.getDefaultAltitude());
        model.addAttribute("videoEndpoint", configurations.getVideoWsEndpoint());
        
        //log.debug("Index Page Opened");
        
        return "index";
    }

    @GetMapping("/v/{droneId}")
    public String getVideoFeed(Model model, @PathVariable("droneId") String droneId){

        model.addAttribute("publicIp", getPublicIpAddress());
        model.addAttribute("droneId", droneId);
        model.addAttribute("videoEndpoint", configurations.getVideoWsEndpoint());

        return "video";
    }

    
	private String getPublicIpAddress() {
		// String ip = "";
		// try {
		// 	final URL whatismyip = new URL("http://checkip.amazonaws.com");

		// 	try(final BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()))){
		// 		ip = in.readLine();
		// 	}

		// } catch (Exception e) {
		// 	log.error(e.getMessage());
		// }
        // log.debug(ip);
        String ip = "10.60.215.193";
        //String ip = "192.168.8.115";

		return ip;
	}
}
