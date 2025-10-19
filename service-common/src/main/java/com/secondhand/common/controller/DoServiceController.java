package com.secondhand.common.controller;

import com.secondhand.common.input.RequestParameter;
import com.secondhand.common.support.IApiService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping // root, we’ll map concrete paths on methods
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST}, originPatterns = "*")
@Slf4j
public class DoServiceController {

    private final ApplicationContext ctx;

    public DoServiceController(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    /** Local call: POST /doService */
    @PostMapping(value = "/doService", consumes = "application/json", produces = "application/json")
    public Map<String, Object> doServiceRoot(@RequestBody RequestParameter req, HttpServletRequest httpReq) {
        return delegate(req, httpReq);
    }

    /** Gateway call: POST /{module}/doService  (e.g. /user/doService, /message/doService, /note/doService) */
    @PostMapping(value = "/{module}/doService", consumes = "application/json", produces = "application/json")
    public Map<String, Object> doServiceWithModule(@PathVariable String module,
                                                   @RequestBody RequestParameter req,
                                                   HttpServletRequest httpReq) {
        // Optional: validate `module` against a property or just ignore it
        return delegate(req, httpReq);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> delegate(RequestParameter req, HttpServletRequest httpReq) {
        String ip = httpReq.getRemoteAddr();
        req.setIp(ip);
        log.info("doService ip={}, service={} version={}", ip, req.getService(), req.getVersion());

        String beanName = req.getService() + req.getVersion(); // e.g. userRegisterService + 1.0 -> userRegisterService1.0
        IApiService<Map<String, Object>> api = (IApiService<Map<String, Object>>) ctx.getBean(beanName);
        return api.handle(req);
    }
}

