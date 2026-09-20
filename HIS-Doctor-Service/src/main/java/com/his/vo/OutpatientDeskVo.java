package com.his.vo;

import java.util.List;

public class OutpatientDeskVo {
    private List<OutpatientPatientVo> waitingPatients;
    private List<OutpatientPatientVo> doingPatients;
    public List<OutpatientPatientVo> getWaitingPatients() { return waitingPatients; }
    public void setWaitingPatients(List<OutpatientPatientVo> waitingPatients) { this.waitingPatients = waitingPatients; }
    public List<OutpatientPatientVo> getDoingPatients() { return doingPatients; }
    public void setDoingPatients(List<OutpatientPatientVo> doingPatients) { this.doingPatients = doingPatients; }
}
