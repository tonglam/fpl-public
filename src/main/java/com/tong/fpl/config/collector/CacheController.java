package com.tong.fpl.config.collector;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by tong on 2020/8/23
 */
@RestController
@RequestMapping(value = "/api")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CacheController {

}
