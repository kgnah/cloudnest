package io.github.kgnah.cloudnest.controller;

import io.github.kgnah.cloudnest.dto.GithubWebhookPayload;
import io.github.kgnah.cloudnest.entity.Deployment;
import io.github.kgnah.cloudnest.service.DeploymentService;
import io.github.kgnah.cloudnest.service.GithubWebhookSignatureService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    private final DeploymentService deploymentService;
    private final GithubWebhookSignatureService signatureService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WebhookController(DeploymentService deploymentService, GithubWebhookSignatureService signatureService) {

        this.deploymentService = deploymentService;
        this.signatureService = signatureService;
    }

    @PostMapping("/github")
    public ResponseEntity<String> handleGithubWebhook(
            @RequestBody String rawpayload,
            @RequestHeader(value = "X-Hub-Signature-256", required = false)
            String signature,
            @RequestHeader(value = "X-Github-Event", required = false)
            String event) throws Exception {

        // 1. verify the webhook signature
        if (!signatureService.isValidSignature(rawpayload, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid webhook signature");
        }

        // 2. only process push events
        if (!"push".equals(event)) {
            return ResponseEntity.ok("Ignored event: " + event);
        }

        // 3. parse the json payload
        GithubWebhookPayload payload = objectMapper.readValue(rawpayload, GithubWebhookPayload.class);

        // 4. only deploy from main
        if (!"refs/heads/main".equals(payload.getRef())) {
            return ResponseEntity.ok("Ignored push to branch: " + payload.getRef());
        }

        // remove trailing .git from the webhook url
        String repositoryUrl = payload.getRepository().getClone_url();

        if (repositoryUrl.endsWith(".git")) {
            repositoryUrl = repositoryUrl.substring(0, repositoryUrl.length() - 4);
        }

        // 5. find the matching deployment
        List <Deployment> deployments = deploymentService.findAllByRepositoryUrl(
                repositoryUrl);

        System.out.println("Received GitHub webhook!");
        System.out.println("Branch: " + payload.getRef());
        System.out.println("Repository: " + repositoryUrl);

        for (Deployment deployment : deployments) {

            System.out.println("Matched deployment: " + deployment.getName());
            System.out.println("Deployment ID: " + deployment.getId());


            // 6. rebuild the Docker image from the latest repository code
            String newImage = deploymentService.rebuildDeploymentImage(deployment);

            // 7. replace the old container with a new one
            deploymentService.redeployContainer(deployment, newImage);
        }

        return ResponseEntity.ok("Successfully rebuilt and redeployed: " + deployments.size() + " deployment(s)");
    }
}
