package com.tong.fpl.controller;

import com.tong.fpl.service.ICacheSerive;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by tong on 2020/8/24
 */
@RestController
@RequestMapping(value = "/cache")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CacheController {

	private final ICacheSerive cacheSerive;

}
