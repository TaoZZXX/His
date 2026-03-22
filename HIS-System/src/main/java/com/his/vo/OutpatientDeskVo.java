package com.his.vo;

import lombok.Data;

import java.util.List;

@Data
public class OutpatientDeskVo {
    private List<OutpatientPatientVo> waitingPatients;
    private List<OutpatientPatientVo> doingPatients;
}

