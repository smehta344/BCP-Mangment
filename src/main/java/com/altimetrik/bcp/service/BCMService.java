package com.altimetrik.bcp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.altimetrik.bcp.dao.DailyStatusRepo;
import com.altimetrik.bcp.dao.LeaderRepo;
import com.altimetrik.bcp.dao.LocationRepo;
import com.altimetrik.bcp.dao.ProjectRepo;
import com.altimetrik.bcp.entity.DailyStatus;
import com.altimetrik.bcp.model.PlanDetailFormData;

import org.springframework.beans.factory.annotation.Autowired;

@Service
public class BCMService {
	@Autowired
	private DailyStatusRepo statusRepo;
	
	@Autowired
	private LocationRepo locationRepo;

	@Autowired
	private ProjectRepo projectRepo;
	
	public void createDilyStatus(PlanDetailFormData formaData){
		DailyStatus statusObj = createStatusObj(formaData);
		statusRepo.save(statusObj);
	}
	
	public DailyStatus createStatusObj(PlanDetailFormData formData){
		DailyStatus statusObject = new DailyStatus();
		statusObject.setDate(formData.getDate());
		statusObject.setLocation(locationRepo.findById(formData.getLocationId()).get());
		statusObject.setProject(projectRepo.findById(formData.getProjectId()).get());
		return statusObject;
	}
}