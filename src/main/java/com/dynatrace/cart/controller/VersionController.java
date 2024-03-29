package com.dynatrace.cart.controller;

import com.dynatrace.cart.model.Version;
import com.dynatrace.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/version")
public class VersionController {
    @Autowired
    private CartRepository cartRepository;
    @Value("${service.version}")
    private String svcVer;
    @Value("${service.date}")
    private String svcDate;

    @GetMapping("")
    public Version getVersion() {
        return new Version("carts", svcVer, svcDate, "OK", "Count: " + cartRepository.count());
    }
}
