package com.tong.fpl.controller.api;

import com.tong.fpl.service.IQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by tong on 2021/5/10
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ApiReportController {

    private final IQueryService queryService;

}
