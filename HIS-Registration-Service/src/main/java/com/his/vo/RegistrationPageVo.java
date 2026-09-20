package com.his.vo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RegistrationPageVo {
    private Long id;
    private Long patientId;
    private LocalDateTime createTime;
    private Integer endAttendance;
    private Integer status;
    private Long skdId;
    private Integer needBook;
    private Integer bindStatus;
    private Long deptId;
    private LocalDate attendanceDate;
    private String patientAgeStr;
    private String patientName;
    private String medicalRecordNo;
    private LocalDate dateOfBirth;
    private String genderStr;
    private String deptName;
    private Long doctorId;
    private String doctorName;
    private Integer noon;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getPatientId(){return patientId;} public void setPatientId(Long patientId){this.patientId=patientId;}
    public LocalDateTime getCreateTime(){return createTime;} public void setCreateTime(LocalDateTime createTime){this.createTime=createTime;}
    public Integer getEndAttendance(){return endAttendance;} public void setEndAttendance(Integer endAttendance){this.endAttendance=endAttendance;}
    public Integer getStatus(){return status;} public void setStatus(Integer status){this.status=status;}
    public Long getSkdId(){return skdId;} public void setSkdId(Long skdId){this.skdId=skdId;}
    public Integer getNeedBook(){return needBook;} public void setNeedBook(Integer needBook){this.needBook=needBook;}
    public Integer getBindStatus(){return bindStatus;} public void setBindStatus(Integer bindStatus){this.bindStatus=bindStatus;}
    public Long getDeptId(){return deptId;} public void setDeptId(Long deptId){this.deptId=deptId;}
    public LocalDate getAttendanceDate(){return attendanceDate;} public void setAttendanceDate(LocalDate attendanceDate){this.attendanceDate=attendanceDate;}
    public String getPatientAgeStr(){return patientAgeStr;} public void setPatientAgeStr(String patientAgeStr){this.patientAgeStr=patientAgeStr;}
    public String getPatientName(){return patientName;} public void setPatientName(String patientName){this.patientName=patientName;}
    public String getMedicalRecordNo(){return medicalRecordNo;} public void setMedicalRecordNo(String medicalRecordNo){this.medicalRecordNo=medicalRecordNo;}
    public LocalDate getDateOfBirth(){return dateOfBirth;} public void setDateOfBirth(LocalDate dateOfBirth){this.dateOfBirth=dateOfBirth;}
    public String getGenderStr(){return genderStr;} public void setGenderStr(String genderStr){this.genderStr=genderStr;}
    public String getDeptName(){return deptName;} public void setDeptName(String deptName){this.deptName=deptName;}
    public Long getDoctorId(){return doctorId;} public void setDoctorId(Long doctorId){this.doctorId=doctorId;}
    public String getDoctorName(){return doctorName;} public void setDoctorName(String doctorName){this.doctorName=doctorName;}
    public Integer getNoon(){return noon;} public void setNoon(Integer noon){this.noon=noon;}
}
