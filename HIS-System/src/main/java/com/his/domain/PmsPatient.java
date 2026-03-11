package com.his.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

@Data
public class PmsPatient {

    private Long id;

    private String name;

    private LocalDate dateOfBirth;

    private Long identificationNo;

    private String homeAddress;

    private String phoneNo;

    private Integer gender;

    private String medicalRecordNo;

}
