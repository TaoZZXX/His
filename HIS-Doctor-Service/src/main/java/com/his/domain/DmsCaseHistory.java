package com.his.domain;

import java.time.LocalDateTime;

public class DmsCaseHistory {
    private Long id; private String chiefComplaint; private String historyOfPresentIllness; private String historyOfTreatment; private String pastHistory; private String allergies; private String healthCheckup;
    private Long registrationId; private String priliminaryDiseStrList; private LocalDateTime startDate; private String name; private Integer gender; private String ageStr; private String checkStrList;
    private String dispositionStrList; private String herbalPrescriptionStrList; private LocalDateTime createTime; private String definiteDiseStrList; private Long patientId; private String testStrList;
    private String medicinePrescriptionStrList; private Integer status; private String priliminaryDiseIdList; private String checkResult; private String testResult;
    public Long getId(){return id;} public void setId(Long id){this.id=id;} public String getChiefComplaint(){return chiefComplaint;} public void setChiefComplaint(String v){this.chiefComplaint=v;}
    public String getHistoryOfPresentIllness(){return historyOfPresentIllness;} public void setHistoryOfPresentIllness(String v){this.historyOfPresentIllness=v;} public String getHistoryOfTreatment(){return historyOfTreatment;} public void setHistoryOfTreatment(String v){this.historyOfTreatment=v;}
    public String getPastHistory(){return pastHistory;} public void setPastHistory(String v){this.pastHistory=v;} public String getAllergies(){return allergies;} public void setAllergies(String v){this.allergies=v;} public String getHealthCheckup(){return healthCheckup;} public void setHealthCheckup(String v){this.healthCheckup=v;}
    public Long getRegistrationId(){return registrationId;} public void setRegistrationId(Long v){this.registrationId=v;} public String getPriliminaryDiseStrList(){return priliminaryDiseStrList;} public void setPriliminaryDiseStrList(String v){this.priliminaryDiseStrList=v;}
    public LocalDateTime getStartDate(){return startDate;} public void setStartDate(LocalDateTime v){this.startDate=v;} public String getName(){return name;} public void setName(String v){this.name=v;} public Integer getGender(){return gender;} public void setGender(Integer v){this.gender=v;}
    public String getAgeStr(){return ageStr;} public void setAgeStr(String v){this.ageStr=v;} public String getCheckStrList(){return checkStrList;} public void setCheckStrList(String v){this.checkStrList=v;}
    public String getDispositionStrList(){return dispositionStrList;} public void setDispositionStrList(String v){this.dispositionStrList=v;} public String getHerbalPrescriptionStrList(){return herbalPrescriptionStrList;} public void setHerbalPrescriptionStrList(String v){this.herbalPrescriptionStrList=v;}
    public LocalDateTime getCreateTime(){return createTime;} public void setCreateTime(LocalDateTime v){this.createTime=v;} public String getDefiniteDiseStrList(){return definiteDiseStrList;} public void setDefiniteDiseStrList(String v){this.definiteDiseStrList=v;}
    public Long getPatientId(){return patientId;} public void setPatientId(Long v){this.patientId=v;} public String getTestStrList(){return testStrList;} public void setTestStrList(String v){this.testStrList=v;}
    public String getMedicinePrescriptionStrList(){return medicinePrescriptionStrList;} public void setMedicinePrescriptionStrList(String v){this.medicinePrescriptionStrList=v;} public Integer getStatus(){return status;} public void setStatus(Integer v){this.status=v;}
    public String getPriliminaryDiseIdList(){return priliminaryDiseIdList;} public void setPriliminaryDiseIdList(String v){this.priliminaryDiseIdList=v;} public String getCheckResult(){return checkResult;} public void setCheckResult(String v){this.checkResult=v;}
    public String getTestResult(){return testResult;} public void setTestResult(String v){this.testResult=v;}
}
