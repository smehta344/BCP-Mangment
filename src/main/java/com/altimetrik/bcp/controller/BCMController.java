package com.altimetrik.bcp.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.altimetrik.bcp.dao.AccountRepo;
import com.altimetrik.bcp.dao.LocationRepo;
import com.altimetrik.bcp.entity.Account;
import com.altimetrik.bcp.entity.Leader;
import com.altimetrik.bcp.entity.Location;
import com.altimetrik.bcp.entity.Project;
import com.altimetrik.bcp.model.PlanDetailFormData;
import com.altimetrik.bcp.service.BCMService;

/**
 * 
 * @author smehta
 *
 */
@RestController
@RequestMapping("/bcm")
public class BCMController {
	
	@Autowired
	BCMService bcmService;
	
	@Autowired
	AccountRepo accountRepo;
		
	@Autowired
	LocationRepo locationRepo;
	
	@Autowired
	AccountRepo acRepo;


	/*
	 * createProjectLocationAssociate
	 */
	@PostMapping(value = "/addDilyStatus")
	public void createDailyStatus(@RequestBody PlanDetailFormData formData) {
		bcmService.createDilyStatus(formData);
		
	}
	
	@GetMapping(value = "/getAllLocations")
	public ResponseEntity<List<Location>> getAllLocation(){
		List<Location> locationList = locationRepo.findAll();
		return ResponseEntity.ok().body(locationList);
	}
	
	@GetMapping(value = "/getAllAccounts")
	public ResponseEntity<List<Account>> getAllAccounts(){
		List<Account> accountList = accountRepo.findAll();
		return ResponseEntity.ok().body(accountList);
	}
	
	@GetMapping(value = "/getLeader/{locationId}/{accountId}")
	public ResponseEntity<Leader> getLeaderById(@PathVariable("locationId") int accountId, 
			@PathVariable("accountId") int locationId){
		Leader leaderData = bcmService.getLeader(locationId, accountId);
		return ResponseEntity.ok().body(leaderData);
		
	}
	
	@GetMapping(value = "/getProject/{accountId}")
	public ResponseEntity<List<Project>> getProject(@PathVariable("accountId") int accountId){
		List<Project> projectList = bcmService.getProjectById(accountId);
		return ResponseEntity.ok().body(projectList);
	}

}
