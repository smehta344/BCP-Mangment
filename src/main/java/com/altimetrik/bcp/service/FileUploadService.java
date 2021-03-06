package com.altimetrik.bcp.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.altimetrik.bcp.config.BcpPropertyConfig;
import com.altimetrik.bcp.dao.AccountRepo;
import com.altimetrik.bcp.dao.AttendanceRepo;
import com.altimetrik.bcp.dao.DailyStatusRepo;
import com.altimetrik.bcp.dao.ProjectRepo;
import com.altimetrik.bcp.entity.Account;
import com.altimetrik.bcp.entity.AttendanceStatus;
import com.altimetrik.bcp.entity.DailyStatus;
import com.altimetrik.bcp.entity.Project;
import com.altimetrik.bcp.exception.FileStorageException;
import com.altimetrik.bcp.util.BcpUtils;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@Service
public class FileUploadService {
	
	@Autowired
	AttendanceRepo attendenceRepo;
	
	BcpPropertyConfig itsConfig;
	
	@Autowired
	ProjectRepo projectRepo;
	
	@Autowired
	AccountRepo accountRepo;
	
	@Autowired
	DailyStatusRepo dailyStatusRepo;

	private  Path attendanceFileStoragePath;
	
	public FileUploadService(BcpPropertyConfig itsConfig ) {
		this.itsConfig = itsConfig;
		this.attendanceFileStoragePath = Paths.get(itsConfig.getAttendanceFilePath())
                .toAbsolutePath().normalize();
		try {
            Files.createDirectories(this.attendanceFileStoragePath);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
	}

	public String storeFile(MultipartFile uploadfile,String uploadFileType,String uploadedBy) throws Exception{
		String fileName = "";
		 try{
			 fileName = StringUtils.cleanPath(uploadfile.getOriginalFilename());
			 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss") ;
			 Path targetLocation = null;
			 if(uploadFileType.equals("Attendance")){
				 File file = new File("Attendance_"+uploadedBy+"_"+dateFormat.format(new Date()) + BcpUtils.getFileExtension(fileName)) ;
				 targetLocation = this.attendanceFileStoragePath.resolve(file.getPath());
			 } else if(uploadFileType.equals("Delivery")) {
				 File file = new File("Delivery_"+dateFormat.format(new Date()) + BcpUtils.getFileExtension(fileName)) ;
				 targetLocation = this.attendanceFileStoragePath.resolve(file.getPath());
			 }
			 Files.copy(uploadfile.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
			 return targetLocation.toString();
		 }catch(IOException ex){
			 throw new FileStorageException("Could not upload file " + fileName + ". Please try again!", ex);
		 }
		
	}

	public void readAttendanceFromExcel(String file) throws FileStorageException, IOException{
		FileInputStream excelFile = null;
		Workbook workbook = null;
		File excel = null;
		try {
			excel = new File(file);
			excelFile = new FileInputStream(excel);
			workbook = new XSSFWorkbook(excelFile);
			Sheet firstSheet = workbook.getSheetAt(0);
			Iterator<Row> iterator = firstSheet.iterator();
			System.out.println("Read excel Start time:"+new Date());
			List<AttendanceStatus> attendanceList = new ArrayList<>();
			while (iterator.hasNext()) {
				Row nextRow = iterator.next();
				if (nextRow.getRowNum() == 0) {
					continue;
				}
				List<String> list = new ArrayList<>();
				for (int colIndex = 0; colIndex < nextRow.getLastCellNum(); colIndex++) {
					Cell cell = nextRow.getCell(colIndex);
					if (nextRow.getLastCellNum() >= colIndex && cell == null) {
						list.add("");
					} else {
						cell.setCellType(cell.CELL_TYPE_STRING);
						list.add(cell.getStringCellValue());
					}
				}
				if(list.size() != 24){
					throw new FileStorageException("Uploaded file contains invalid column counts.<br>Expected column count is 24.But actual is "+list.size());
				}
				if (BcpUtils.attendanceValidation(list, itsConfig)) {
					AttendanceStatus attendance = new AttendanceStatus();
					attendance.setEmployeeId(list.get(1));
					attendance.setEmpployeeName(list.get(2));
					attendance.setGeography(list.get(3));
					attendance.setAccountName(list.get(4));
					attendance.setCountry(list.get(5));
					attendance.setClinetLocation(list.get(6));
					attendance.setTotalHc(Double.parseDouble(list.get(7)));
					attendance.setProject(list.get(8));
					attendance.setBaseCategory(list.get(9));
					attendance.setCapabilityCenter( list.get(10));
					if (list.get(11) != null && !list.get(11).toString().equals("")) {
						attendance.setBenchWebDate(DateUtil.getJavaDate(Double.parseDouble(list.get(11))));
					}
					attendance.setAssignmentStatus(list.get(12));
					attendance.setCategory(list.get(13));
					if (list.get(14) != null && !list.get(14).toString().equals("")) {
						attendance.setDateOfJoining(DateUtil.getJavaDate(Double.parseDouble(list.get(14))));
					}
					if (list.get(15) != null && !list.get(15).toString().equals("")) {
						attendance.setBenchWebAging(Integer.parseInt(list.get(15)));
					}
					attendance.setPrimarySkill(list.get(16));
					attendance.setSecondarySkill(list.get(17));
					attendance.setTotalExperience(list.get(18));
					attendance.setReportingManager(list.get(19));
					attendance.setBaseLocation( list.get(20));
					attendance.setEmailId(list.get(21));
					if(list.get(22).equalsIgnoreCase("Unmarked") || list.get(22).equalsIgnoreCase("#N/A") || list.get(22).equalsIgnoreCase("Not Marked")){
						attendance.setAttendanceStatus("Not Marked");
					} else if(list.get(22).equalsIgnoreCase("Leave - Approval Pending") || list.get(22).equalsIgnoreCase("Leave") || list.get(22).equalsIgnoreCase("Floater Holiday")){
						attendance.setAttendanceStatus("Leave");
					} else {
						attendance.setAttendanceStatus(list.get(22));
					}
					if (list.get(23) != null && !list.get(23).toString().equals("")) {
						attendance.setAttendanceDate(DateUtil.getJavaDate(Double.parseDouble(list.get(23))));
					}
					attendanceList.add(attendance);
				}
			}
			System.out.println("Read excel end time:"+new Date());
			System.out.println("Total records="+attendanceList.size());
			
			saveAttendance(attendanceList);
		} catch (Exception e) { 
			boolean isfileDeleted = Files.deleteIfExists(excel.toPath());
			System.out.println("Error occured, so deleted the uploaded excel file :"+isfileDeleted);
			throw new FileStorageException(e.getLocalizedMessage());
		} finally {
			try {
				workbook.close();
				excelFile.close();
			} catch (IOException e) {
				boolean isfileDeleted = Files.deleteIfExists(excel.toPath());
				System.out.println("Error occured, so deleted the uploaded excel file :"+isfileDeleted);
				throw new FileStorageException(e.getLocalizedMessage());
			}
		}
	}
	
	private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
	    Set<Object> seen = ConcurrentHashMap.newKeySet();
	    return t -> seen.add(keyExtractor.apply(t));
	}
	
	private List<Date> getUniqueDateList(List<AttendanceStatus> attendanceList){
		List<Date> listDate = new ArrayList<>();
		Stream<AttendanceStatus> uniqueDateList = attendanceList.stream().filter(distinctByKey(AttendanceStatus::getAttendanceDate));
		uniqueDateList.forEach(f -> {listDate.add(f.getAttendanceDate());});
		return listDate;
	}
	
	private List<Date> getUniqueDateListByDelivery(List<DailyStatus> dailyStatusList){
		List<Date> listDate = new ArrayList<>();
		Stream<DailyStatus> uniqueDateList = dailyStatusList.stream().filter(distinctByKey(DailyStatus::getDate));
		uniqueDateList.forEach(f -> {listDate.add(f.getDate());});
		return listDate;
	}
	
	@Transactional
	public void saveAttendance(List<AttendanceStatus> attendanceList){
		
		List<Date> dateList = getUniqueDateList(attendanceList);
		System.out.println("DateList:"+dateList);
		if(null != dateList && !dateList.isEmpty()){
			System.out.println("Delete records Start time:"+new Date());
			attendenceRepo.deleteAttendanceStatusByAttendanceDateIn(dateList);
			System.out.println("Delete records End time:"+new Date());
			
		}
		System.out.println("Save batch start time:"+new Date());
		attendenceRepo.saveAll(attendanceList);
		System.out.println("Save batch end time:"+new Date());
	}
	
	@Transactional
	public void readDailyStatusFromExcel(String uploadedFile) throws IOException, ParseException, InvalidFormatException {
		Workbook workbook = null;
		Project project = null;
		File file = new File(uploadedFile);
		FileInputStream excelFile = new FileInputStream(file);
		workbook = new XSSFWorkbook(excelFile);
		List<DailyStatus> statusList = new ArrayList<DailyStatus>();
		try{
		for(int i=0; i < workbook.getNumberOfSheets();i++){
			Sheet excelSheet = workbook.getSheetAt(i);
			List<DailyStatus> tmpList= new ArrayList<DailyStatus>();
			Iterator<Row> rowInterator = excelSheet.iterator();
			while(rowInterator.hasNext()){
				Row rowVal = rowInterator.next();
				if(rowVal.getRowNum() == 0){
					continue;
				}
				project = null;
				DailyStatus statusObj = new DailyStatus();
				if(!excelSheet.getSheetName().equals("DropValue")){
				for(int k=1;k<22;k++){
					Row rowData = excelSheet.getRow(0);
					String header = rowData.getCell(k).getStringCellValue();
					Cell cellVal = rowVal.getCell(k);
					if(project != null){
						statusObj.setProjectId(project);
					}
					if(rowVal.getCell(26) != null){
						statusObj.setMilestone(rowVal.getCell(26).getStringCellValue());
					}
					
					if(rowVal.getCell(27) != null){
						Cell cellData = rowVal.getCell(27);
						cellData.setCellType(cellVal.CELL_TYPE_STRING);
						if(!cellData.getStringCellValue().equals("")){
							statusObj.setTeamSize(Float.parseFloat(cellData.getStringCellValue()));
							}
					}
					
					if(rowVal.getCell(32) != null){
						statusObj.setDeliverableOfDay(rowVal.getCell(32).getStringCellValue());
					}
					
					if(rowVal.getCell(33) != null){
						statusObj.setChallenges(rowVal.getCell(33).getStringCellValue());
					}

					if(rowVal.getCell(34) != null){
						statusObj.setWfhChallenge(rowVal.getCell(34).getStringCellValue());
					}
					
					if(header.contains("Project Name")){
						if((cellVal == null) || (cellVal.getStringCellValue().equals("") )){
							break;
						}
						else{
							List<Project> projectList = new ArrayList<Project>();
							projectList = projectRepo.findAllByName(cellVal.getStringCellValue());
							if(projectList.size() > 1){
								Account account = accountRepo.findByName(excelSheet.getSheetName());
								for(Project proj : projectList){
									if(proj.getAccount().getId() == account.getId()){
										project = proj;
										statusObj.setProjectId(project);
										break;
									}

								}
							}
							else{
								project = projectList.get(0);
								statusObj.setProjectId(project);
							}
						}
					}
					
					else if(header.contains("Logged")){
						if((cellVal != null)){
							cellVal.setCellType(cellVal.CELL_TYPE_STRING);
							if(!cellVal.getStringCellValue().equals("")){
							statusObj.setTeamLogged(Float.parseFloat(cellVal.getStringCellValue()));
							}
							else
								statusObj.setTeamLogged(0.0f);
						}  
						else{		
							statusObj.setTeamLogged(0.0f);
						}
					}
					
					else if(header.contains("Status")){
						String dateValue = header.substring(header.indexOf(")")+5);
						SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yy");
						SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
						String dateString = formatter.format(parser.parse(dateValue));
						statusObj.setDate(formatter.parse(dateString));
						if(cellVal.getStringCellValue().equals("")){
							statusObj.setStatus("none");
						}
						else{
							statusObj.setStatus(cellVal.getStringCellValue());
						}
					}
					
					else if(header.contains("Remarks")&&(cellVal != null)){
						statusObj.setRemarks(cellVal.getStringCellValue());
					}
					else if(header.contains("Hiring")){
						if((cellVal != null) && !(cellVal.getStringCellValue().equals(""))){
							statusObj.setHiringUpdate(cellVal.getStringCellValue());
						}
						else{
							statusObj.setHiringUpdate("");
							
						}
					}
					
					if(isAllFieldsFilled(statusObj)){
						statusList.add(statusObj);
						statusObj = new DailyStatus();
					}
				}
			}
			}
		}
		}
		catch(Exception exception){
			boolean isfileDeleted = Files.deleteIfExists(file.toPath());
			System.out.println("Error occured, so deleted the uploaded excel file :"+isfileDeleted);
			throw new FileStorageException(exception.getLocalizedMessage());
		}finally {
			try {
				workbook.close();
				excelFile.close();
			} catch (IOException e) {
				boolean isfileDeleted = Files.deleteIfExists(file.toPath());
				System.out.println("Error occured, so deleted the uploaded excel file :"+isfileDeleted);
				throw new FileStorageException(e.getLocalizedMessage());
			}
		}
		List<Date> dateList = getUniqueDateListByDelivery(statusList);
		System.out.println("date size" + dateList.get(0).getDate());
		dailyStatusRepo.deleteDailyStatusByDateIn(dateList);
		dailyStatusRepo.saveAll(statusList);
	}
	
	boolean isAllFieldsFilled(DailyStatus dailyStatus){
		System.out.println("data"+dailyStatus.toString());
		if((dailyStatus.getStatus() != null) && (dailyStatus.getProject() != null) && (dailyStatus.getRemarks() != null) &&
				(dailyStatus.getTeamLogged() != null) && (dailyStatus.getHiringUpdate() != null)){
			return true;
		}
		return false;
	}
}
