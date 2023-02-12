package org.openmrs.module.clientregistry.api.impl;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.clientregistry.ClientRegistryConfig;
import org.openmrs.module.clientregistry.api.CRPatientService;
import org.openmrs.module.clientregistry.providers.FhirCRConstants;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FhirCRPatientServiceImpl implements CRPatientService {
	
	@Autowired
	private IGenericClient fhirClient;
	
	@Autowired
	private ClientRegistryConfig config;
	
	/**
	 * Constructs a $ihe-pix fhir client call to an external Client Registry returning any patients
	 * that match the given source identifier/system and target systems.
	 */
	@Override
	public List<Patient> getCRPatient(String sourceIdentifier, String sourceIdentifierSystem, List<String> targetSystems) {
		// construct and send request to external client registry
		IQuery<IBaseBundle> crRequest = fhirClient
		        .search()
		        .byUrl(
		            String.format("%s/%s", config.getClientRegistryServerUrl(), config.getClientRegistryGetPatientEndpoint()))
		        .where(
		            FhirCRConstants.SOURCE_IDENTIFIER_PARAM.exactly().systemAndIdentifier(sourceIdentifierSystem,
		                sourceIdentifier));
		
		if (!targetSystems.isEmpty()) {
			crRequest.and(FhirCRConstants.TARGET_SYSTEM_PARAM.matches().values(targetSystems));
		}
		
		Bundle patientBundle = crRequest.returnBundle(Bundle.class).execute();
		return parseCRPatientSearchResults(patientBundle);
	}
	
	@Override
	public List<Patient> searchCRForPatients(PatientSearchParams patientSearchParams) {
		return null;
	}
	
	/**
	 * Filter and parse out fhir patients from Client Registry Patient Search results
	 */
	private List<Patient> parseCRPatientSearchResults(Bundle patientBundle) {
        return patientBundle
                .getEntry()
                .stream()
                .filter(entry -> entry.hasType(FhirConstants.PATIENT))
                .map(entry -> (Patient) entry.getResource())
				.collect(Collectors.toList());
    }
}
