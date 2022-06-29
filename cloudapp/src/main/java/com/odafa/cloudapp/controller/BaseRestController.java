package com.odafa.cloudapp.controller;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.odafa.cloudapp.dto.DroneInfo;

import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@Component
public class BaseRestController {
    
	@GetMapping("/updateSystemInfo")
	public String updateSystemInfo() {

        final Gson gson = new Gson();
        final List<DroneInfo> drones = new ArrayList<>();
        
        DroneInfo dto1 = new DroneInfo("1", 1.553504, 110.359291, 0.0468, 7.1, 12.3, "ONLINE");
        DroneInfo dto2 = new DroneInfo("2", -6.046810, 107.246430, 0.0468, 3, 21, "ON MISSION");
        drones.add(dto1);
        drones.add(dto2);

        return gson.toJson(drones);
	}

    @PostMapping("/startMission")
	public String startMission( @RequestParam("points") String points, @RequestParam("droneId") String droneId) {
        log.debug("From Drone ID {} Received Points Data: {}", droneId, points);        
        return "ok";
    }

    @PostMapping("/sendCommand")
	public String sendCommand(@RequestParam("droneId") String droneId, @RequestParam("commandCode") String commandCode) {
		log.debug("Received command code {} for Drone ID {}", commandCode, droneId);
        return "ok";
    }
    
}
