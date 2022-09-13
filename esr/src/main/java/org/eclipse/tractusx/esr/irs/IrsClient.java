package org.eclipse.tractusx.esr.irs;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class IrsClient {

    private final static String IRS_URL = "https://irs.dev.demo.catena-x.net";
    private final RestTemplate restTemplate;

    IrsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    StartJobResponse startJob(IrsRequest irsRequest) {
        return restTemplate.postForObject(IRS_URL + "/irs/jobs", irsRequest, StartJobResponse.class);
    }

    @Retry(name = "waiting-for-completed")
    IrsResponse getJobDetails(String jobId) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(IRS_URL);
        uriBuilder.path("/irs/jobs/").path(jobId);
        return restTemplate.getForObject(uriBuilder.build().toUri(), IrsResponse.class);
    }

}
