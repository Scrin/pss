package org.assembly.pss.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "Status")
@RestController
@RequestMapping("/api")
public class Status extends AbstractController {

    @ApiResponses({
        @ApiResponse(code = 200, message = "Backend status")})
    @RequestMapping(method = RequestMethod.GET, value = "/status", produces = "application/json")
    public org.assembly.pss.bean.Status status() {
        org.assembly.pss.bean.Status status = new org.assembly.pss.bean.Status();
        status.setStatus("OK");
        status.setServerTime(System.currentTimeMillis());
        return status;
    }
}
