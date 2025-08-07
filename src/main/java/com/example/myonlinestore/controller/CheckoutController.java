package com.example.myonlinestore;

import com.example.myonlinestore.model.RequestData;
import com.example.myonlinestore.service.CaptureContextService;
import com.example.myonlinestore.service.JwtProcessorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.annotation.PostConstruct;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

@Controller
public class CheckoutController {

    private final CaptureContextService captureContextService;
    private final JwtProcessorService      jwtProcessorService;
    private final ObjectMapper            objectMapper;
    private final Environment             env;

    private RequestData requestData;
    private JsonNode    payloadJson;

    @Autowired
    public CheckoutController(CaptureContextService captureContextService,
                              JwtProcessorService     jwtProcessorService,
                              Environment             env) {
        this.captureContextService = captureContextService;
        this.jwtProcessorService   = jwtProcessorService;
        this.objectMapper          = new ObjectMapper();
        this.env                   = env;
    }
    
    /**
     * Lazily initialize the request data and payload JSON after the bean has been constructed.
     * This method is called after the bean has been constructed and all dependencies have been injected.
     */
    @PostConstruct
    public void initializeRequestData() {
        try {
            String payload     = Files.readString(Path.of(
                    new ClassPathResource("request-payload.json")
                            .getURI()));
            String headersJson = Files.readString(Path.of(
                    new ClassPathResource("request-headers.json")
                            .getURI()));
            Map<String,String> headers = objectMapper.readValue(headersJson, Map.class);

            this.payloadJson = objectMapper.readTree(payload);
            this.requestData = new RequestData(payload, headers);

            System.out.println("Lazily loaded request payload and headers from resources");
        } catch (Exception e) {
            System.err.println("Error loading request data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /* =========================================================================
       PUBLIC ENDPOINT
       ========================================================================= */
    @GetMapping("/checkout")
    public String showCheckout(Model model) {

        try {
            /* CyberSource round-trip ------------------------------------------------ */
            String jwt = captureContextService.getCaptureContext(requestData);

            /* Extract client library URL + integrity hash --------------------------- */
            Map<String,String> jwtDetails = jwtProcessorService.processAndPrintJwt(jwt);

            /* Populate view model --------------------------------------------------- */
            model.addAttribute("jwt", jwt);
            model.addAttribute("clientLibrary",          jwtDetails.get("clientLibrary"));
            model.addAttribute("clientLibraryIntegrity", jwtDetails.get("clientLibraryIntegrity"));

            /* Always take the amount from the **server-side JSON** ------------------ */
            String totalAmount = payloadJson.path("orderInformation")
                    .path("amountDetails")
                    .path("totalAmount")
                    .asText("0.01");
            model.addAttribute("totalAmount", totalAmount);

            return "checkout";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to get capture context: " + e.getMessage());
            return "error";
        }
    }
}
