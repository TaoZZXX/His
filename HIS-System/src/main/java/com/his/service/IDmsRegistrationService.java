package com.his.service;

import com.his.dto.RegistrationDto;

public interface IDmsRegistrationService {

    /**
     * 患者挂号
     * @param registrationDto 挂号信息
     */
    public void registration(RegistrationDto registrationDto);
}
