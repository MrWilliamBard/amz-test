package amztest.rest;

import amztest.requestbucket.RequestBucket;
import amztest.requestbucket.IpBucketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(value = "/amz")
public class AmzTestController {

    private final IpBucketFactory ipBucketFactory;

    @Autowired
    public AmzTestController(IpBucketFactory ipBucketFactory) {
        this.ipBucketFactory = ipBucketFactory;
    }

    @GetMapping(value = "/test")
    public ResponseEntity test(HttpServletRequest request) {
        RequestBucket requestBucket = ipBucketFactory.getOrCreateBucketByIp(request.getRemoteAddr());
        if (requestBucket.add()) {
            return ResponseEntity.ok().build();
        } else return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
    }

}
