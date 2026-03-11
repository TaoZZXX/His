package com.his.controller;

import com.his.domain.Result;
import com.his.dto.RegistrationDto;
import com.his.service.IDmsRegistrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/registration")
public class RegistrationController {

    @Autowired
    private IDmsRegistrationService iDmsRegistrationService;

    @PostMapping("/registrations")
    public Result<?> createRegistration(@RequestBody RegistrationDto registrationDto) {
        iDmsRegistrationService.registration(registrationDto);
        return Result.success("挂号成功");
    }

}
