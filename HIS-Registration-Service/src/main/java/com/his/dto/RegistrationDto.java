package com.his.dto;

import java.time.LocalDate;

public class RegistrationDto {
    private Long id;
    private Long identificationNo;
    private String name;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private String contact;
    private String department;
    private Long departmentId;
    private Long rankId;
    private String level;
    private LocalDate registrationDate;
    private String payment;
    private String session;
    private String doctor;
    private Long doctorId;
    private Double amount;
    private String medicalRecord;
    public Long getId(){return id;} public void setId(Long id){this.id=id;} public Long getIdentificationNo(){return identificationNo;} public void setIdentificationNo(Long identificationNo){this.identificationNo=identificationNo;}
    public String getName(){return name;} public void setName(String name){this.name=name;} public LocalDate getBirthDate(){return birthDate;} public void setBirthDate(LocalDate birthDate){this.birthDate=birthDate;}
    public String getGender(){return gender;} public void setGender(String gender){this.gender=gender;} public String getAddress(){return address;} public void setAddress(String address){this.address=address;}
    public String getContact(){return contact;} public void setContact(String contact){this.contact=contact;} public String getDepartment(){return department;} public void setDepartment(String department){this.department=department;}
    public Long getDepartmentId(){return departmentId;} public void setDepartmentId(Long departmentId){this.departmentId=departmentId;} public Long getRankId(){return rankId;} public void setRankId(Long rankId){this.rankId=rankId;}
    public String getLevel(){return level;} public void setLevel(String level){this.level=level;} public LocalDate getRegistrationDate(){return registrationDate;} public void setRegistrationDate(LocalDate registrationDate){this.registrationDate=registrationDate;}
    public String getPayment(){return payment;} public void setPayment(String payment){this.payment=payment;} public String getSession(){return session;} public void setSession(String session){this.session=session;}
    public String getDoctor(){return doctor;} public void setDoctor(String doctor){this.doctor=doctor;} public Long getDoctorId(){return doctorId;} public void setDoctorId(Long doctorId){this.doctorId=doctorId;}
    public Double getAmount(){return amount;} public void setAmount(Double amount){this.amount=amount;} public String getMedicalRecord(){return medicalRecord;} public void setMedicalRecord(String medicalRecord){this.medicalRecord=medicalRecord;}
}
