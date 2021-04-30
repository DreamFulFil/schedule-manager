package org.dream.scheduled.tasks.service.rest;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("heartbeat")
public class HeartbeatService {

    @GetMapping("/")
    @PreAuthorize("hasAuthority('HEART')")
    public boolean alive(HttpServletRequest req) {
        String localAddr = req.getLocalAddr();
        String remoteHost = req.getRemoteHost();
        String remoteAddr = req.getRemoteAddr();
        String remoteUser = req.getRemoteUser();
        log.info("Local Address:{}", localAddr);
        log.info("Remote Host:{}", remoteHost);
        log.info("Remote Address:{}", remoteAddr);
        log.info("Remote Address:{}", remoteAddr);
        log.info("Remote User:{}", remoteUser);
        return true;
    }
    
}
