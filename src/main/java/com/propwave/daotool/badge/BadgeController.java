package com.propwave.daotool.badge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/badges")
public class BadgeController {
    final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private final BadgeProvider badgeProvider;
    @Autowired
    private final BadgeService badgeService;

    public BadgeController(BadgeProvider badgeProvider, BadgeService badgeService){
        this.badgeProvider = badgeProvider;
        this.badgeService = badgeService;
    }
}
