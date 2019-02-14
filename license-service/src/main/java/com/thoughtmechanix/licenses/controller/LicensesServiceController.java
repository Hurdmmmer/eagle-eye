package com.thoughtmechanix.licenses.controller;

import com.thoughtmechanix.licenses.model.License;
import com.thoughtmechanix.licenses.services.LicenseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/organizations/{organizationId}/licenses")
@Slf4j
public class LicensesServiceController {

    @Autowired
    private LicenseService licenseService;

    @RequestMapping(value="/",method = RequestMethod.GET)
    @ResponseBody
    public List<License> getLicenses(@PathVariable("organizationId") String organizationId) {

        return licenseService.getLicensesByOrg(organizationId);
    }
    @RequestMapping(value = "/{licenseId}", method = RequestMethod.GET)
    public License getLicenses(
            @PathVariable("organizationId") String organizationId,
            @PathVariable("licenseId") String licenseId) {

        return licenseService.getLicense(organizationId, licenseId);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> save(@PathVariable("organizationId") String organizationId) {
        License license = License.builder().organizationId(organizationId).build();
        try{
            licenseService.saveLicense(license);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ResponseEntity.badRequest().build();
    }

    @RequestMapping(value="/{licenseId}/{clientType}",method = RequestMethod.GET)
    public License getLicensesWithClient( @PathVariable("organizationId") String organizationId,
                                          @PathVariable("licenseId") String licenseId,
                                          @PathVariable("clientType") String clientType) {

        return licenseService.getLicense(organizationId,licenseId, clientType);
    }

    @RequestMapping(value="{licenseId}",method = RequestMethod.PUT)
    public void updateLicenses( @PathVariable("licenseId") String licenseId, @RequestBody License license) {
        licenseService.updateLicense(license);
    }

//    @RequestMapping(value="/",method = RequestMethod.POST)
//    public void saveLicenses(@RequestBody License license) {
//        licenseService.saveLicense(license);
//    }

    @RequestMapping(value="{licenseId}",method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLicenses( @PathVariable("licenseId") String licenseId, @RequestBody License license) {
        licenseService.deleteLicense(license);
    }
}
