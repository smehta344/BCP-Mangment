package com.altimetrik.bcp.controller;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.altimetrik.bcp.dao.AccountRepo;
import com.altimetrik.bcp.dao.LocationRepo;
import com.altimetrik.bcp.entity.Account;
import com.altimetrik.bcp.entity.Leader;
import com.altimetrik.bcp.entity.Location;
import com.altimetrik.bcp.entity.Project;
import com.altimetrik.bcp.model.DeliveryInput;
import com.altimetrik.bcp.model.PlanDetailFormData;
import com.altimetrik.bcp.service.BCMService;

/**
 * 
 * @author smehta
 *
 */
@RestController
@CrossOrigin
@RequestMapping("/bcm")
public class BCMController {

	@Autowired
	private BCMService bcmService;

	@Autowired
	private AccountRepo accountRepo;

	@Autowired
	private LocationRepo locationRepo;

	@Autowired
	AccountRepo acRepo;

	/*
	 * createProjectLocationAssociate
	 */
	@PostMapping(value = "/addDilyStatus")
	public ResponseEntity<String> createDailyStatus(@RequestBody PlanDetailFormData formData) {
		bcmService.createDilyStatus(formData);
		return ResponseEntity.ok().body("success");
	}

	@GetMapping(value = "/getAllLocations")
	public ResponseEntity<List<Location>> getAllLocation() {
		List<Location> locationList = locationRepo.findAll();
		return ResponseEntity.ok().body(locationList);
	}

	@GetMapping(value = "/getAllAccounts")
	public ResponseEntity<List<Account>> getAllAccounts() {
		List<Account> accountList = accountRepo.findAll();
		return ResponseEntity.ok().body(accountList);
	}

	@GetMapping(value = "/getLocationAndLeader/{projectId}/{accountId}")
	public ResponseEntity<DeliveryInput> getLeaderById(@PathVariable("projectId") int projectId,
			@PathVariable("accountId") int accountId) {
		DeliveryInput deliveryData = bcmService.getLocationAndLeader(projectId, accountId);
		return ResponseEntity.ok().body(deliveryData);

	}

	@GetMapping(value = "/getProject/{accountId}")
	public ResponseEntity<List<Project>> getProject(@PathVariable("accountId") int accountId) {
		List<Project> projectList = bcmService.getProjectById(accountId);
		return ResponseEntity.ok().body(projectList);
	}

	@GetMapping(value = "/getHistoryData")
	public ResponseEntity<PlanDetailFormData> getHistory(@RequestParam("projectId") int projectId, 
			@RequestParam("fromDate") @DateTimeFormat(pattern="yyyy/MM/dd") Date fromDate) {
		PlanDetailFormData planDetail = bcmService.getHistoryData(fromDate, projectId);
		return ResponseEntity.ok().body(planDetail);
	}
	
}
