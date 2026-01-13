package com.spring.demo.model;

public record DeviceInfo(
        String device,
        String ip,
        String userAgent,
        String loginAt
) {}
