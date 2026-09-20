package com.his.vo;

import com.his.enums.EndAttendanceCode;
import com.his.enums.RegistrationStatusCode;

public class OutpatientPatientVo {
    private Long id; private String medicalNo; private String name; private String gender; private Integer age;
    private RegistrationStatusCode status; private EndAttendanceCode endAttendance; private Integer noon;
    public Long getId(){return id;} public void setId(Long id){this.id=id;} public String getMedicalNo(){return medicalNo;} public void setMedicalNo(String medicalNo){this.medicalNo=medicalNo;}
    public String getName(){return name;} public void setName(String name){this.name=name;} public String getGender(){return gender;} public void setGender(String gender){this.gender=gender;}
    public Integer getAge(){return age;} public void setAge(Integer age){this.age=age;} public RegistrationStatusCode getStatus(){return status;} public void setStatus(RegistrationStatusCode status){this.status=status;}
    public EndAttendanceCode getEndAttendance(){return endAttendance;} public void setEndAttendance(EndAttendanceCode endAttendance){this.endAttendance=endAttendance;} public Integer getNoon(){return noon;} public void setNoon(Integer noon){this.noon=noon;}
}
