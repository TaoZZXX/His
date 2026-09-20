package com.his.vo;

import java.time.LocalDate;

public class ExamLabItemRowVo {
    private Long id;
    private Long registrationId;
    private Integer type;
    private Integer status;
    private String demand;
    private Long noDrugId;
    private String checkResult;
    private String clinicalImpression;
    private String clinicalDiagnosis;
    private String patientName;
    private String medicalRecordNo;
    private Integer age;
    private String genderStr;
    private String projectName;
    private LocalDate attendanceDate;
    private String deptName;
    private String orderDoctorName;
    private String resultImgUrlList;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getRegistrationId() { return registrationId; } public void setRegistrationId(Long registrationId) { this.registrationId = registrationId; }
    public Integer getType() { return type; } public void setType(Integer type) { this.type = type; }
    public Integer getStatus() { return status; } public void setStatus(Integer status) { this.status = status; }
    public String getDemand() { return demand; } public void setDemand(String demand) { this.demand = demand; }
    public Long getNoDrugId() { return noDrugId; } public void setNoDrugId(Long noDrugId) { this.noDrugId = noDrugId; }
    public String getCheckResult() { return checkResult; } public void setCheckResult(String checkResult) { this.checkResult = checkResult; }
    public String getClinicalImpression() { return clinicalImpression; } public void setClinicalImpression(String clinicalImpression) { this.clinicalImpression = clinicalImpression; }
    public String getClinicalDiagnosis() { return clinicalDiagnosis; } public void setClinicalDiagnosis(String clinicalDiagnosis) { this.clinicalDiagnosis = clinicalDiagnosis; }
    public String getPatientName() { return patientName; } public void setPatientName(String patientName) { this.patientName = patientName; }
    public String getMedicalRecordNo() { return medicalRecordNo; } public void setMedicalRecordNo(String medicalRecordNo) { this.medicalRecordNo = medicalRecordNo; }
    public Integer getAge() { return age; } public void setAge(Integer age) { this.age = age; }
    public String getGenderStr() { return genderStr; } public void setGenderStr(String genderStr) { this.genderStr = genderStr; }
    public String getProjectName() { return projectName; } public void setProjectName(String projectName) { this.projectName = projectName; }
    public LocalDate getAttendanceDate() { return attendanceDate; } public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    public String getDeptName() { return deptName; } public void setDeptName(String deptName) { this.deptName = deptName; }
    public String getOrderDoctorName() { return orderDoctorName; } public void setOrderDoctorName(String orderDoctorName) { this.orderDoctorName = orderDoctorName; }
    public String getResultImgUrlList() { return resultImgUrlList; } public void setResultImgUrlList(String resultImgUrlList) { this.resultImgUrlList = resultImgUrlList; }
}
