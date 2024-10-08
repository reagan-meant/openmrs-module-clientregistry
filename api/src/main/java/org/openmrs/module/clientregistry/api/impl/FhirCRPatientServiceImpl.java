package org.openmrs.module.clientregistry.api.impl;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IOperationUntypedWithInputAndPartialOutput;
import ca.uhn.fhir.rest.gclient.IQuery;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.param.UriOrListParam;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.*;
import org.openmrs.module.clientregistry.ClientRegistryConfig;
import org.openmrs.module.clientregistry.api.CRPatientService;
import org.openmrs.module.clientregistry.providers.FhirCRConstants;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class FhirCRPatientServiceImpl implements CRPatientService {
	
	@Autowired
	@Qualifier("clientRegistryFhirClient")
	private IGenericClient fhirClient;
	
	/**
	 * Get patient identifiers from an external client registry's $ihe-pix implementation. Use the
	 * returned identifiers to then request a matching Patient bundle from the client registry.
	 */
	@Override
	public List<Patient> getCRPatients(String sourceIdentifier, String sourceIdentifierSystem, List<String> targetSystems) {
		// construct request to external FHIR $ihe-pix endpoint
		IOperationUntypedWithInputAndPartialOutput<Parameters> identifiersRequest = fhirClient
				.operation()
				.onType(FhirConstants.PATIENT)
				.named(FhirCRConstants.IHE_PIX_OPERATION)
				.withSearchParameter(Parameters.class, FhirCRConstants.SOURCE_IDENTIFIER, new TokenParam(sourceIdentifierSystem, sourceIdentifier));

		if (!targetSystems.isEmpty()) {
			identifiersRequest.andSearchParameter(FhirCRConstants.TARGET_SYSTEM, new StringParam(String.join(",", targetSystems)));
		}

		Parameters crMatchingParams = identifiersRequest.useHttpGet().execute();
		List<String> crIdentifiers = crMatchingParams.getParameter().stream()
				.filter(param -> Objects.equals(param.getName(), "targetId"))
				.map(param -> param.getValue().toString())
				.collect(Collectors.toList());

		if (crIdentifiers.isEmpty()) {
			return Collections.emptyList();
		}

		// construct and send request to external client registry
		Bundle patientBundle = fhirClient
				.search()
				.forResource(Patient.class)
				.where(new StringClientParam(Patient.SP_RES_ID).matches().values(crIdentifiers))
				.returnBundle(Bundle.class)
				.execute();
		
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
