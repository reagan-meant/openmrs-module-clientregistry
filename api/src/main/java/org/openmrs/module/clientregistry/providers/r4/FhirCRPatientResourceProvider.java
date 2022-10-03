package org.openmrs.module.clientregistry.providers.r4;

import ca.uhn.fhir.model.valueset.BundleTypeEnum;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import lombok.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;
import ca.uhn.fhir.rest.annotation.Operation;
import ca.uhn.fhir.rest.annotation.OperationParam;
import org.openmrs.module.clientregistry.ClientRegistryConfig;
import org.openmrs.module.clientregistry.api.ClientRegistryManager;
import org.openmrs.module.clientregistry.providers.FhirCRConstants;
import org.openmrs.module.fhir2.api.annotations.R4Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PACKAGE;

@Component("crPatientFhirR4ResourceProvider")
@R4Provider
@Setter(PACKAGE)
public class FhirCRPatientResourceProvider implements IResourceProvider {
	
	@Autowired
	private ClientRegistryManager clientRegistryManager;
	
	@Autowired
	private ClientRegistryConfig config;
	
	@Override
	public Class<? extends IBaseResource> getResourceType() {
		return Patient.class;
	}
	
	/**
	 * FHIR endpoint to get Patient references from external client registry Example request: GET
	 * [fhirbase]/Patient/$ihe-pix?sourceIdentifier=1234[&targetSystem=system1,system2]
	 * 
	 * @param sourceIdentifierParam patient identifier
	 * @param targetSystemsParam (optional) Patient assigning authorities (ie systems) from which
	 *            the returned identifiers shall be selected. Use module defined default if not
	 *            provided.
	 * @return OpenMRS Patient corresponding to identifier (TODO this might change to a list of
	 *         identifier references returned by CR)
	 */
	@Operation(name = FhirCRConstants.IHE_PIX_OPERATION, idempotent=true, type = Patient.class, bundleType = BundleTypeEnum.SEARCHSET)
	public List<Patient> getCRPatientById(
			@OperationParam(name = FhirCRConstants.SOURCE_IDENTIFIER) StringParam sourceIdentifierParam,
	        @OperationParam(name = FhirCRConstants.TARGET_SYSTEM) StringOrListParam targetSystemsParam
	) {
		
		if (sourceIdentifierParam == null || sourceIdentifierParam.getValue() == null) {
			throw new InvalidRequestException("sourceIdentifier must be specified");
		}

		List<String> targetSystems = targetSystemsParam == null
				? Collections.emptyList()
				: targetSystemsParam.getValuesAsQueryTokens().stream().filter(Objects::nonNull).map(StringParam::getValue).collect(Collectors.toList());

		// If no targetSystem provided, use config defined default. Otherwise, take first targetSystem provided and
		// include in sourceIdentifier token. Remaining targetSystems included in targetSystem param passed to CR
		String sourceIdentifierSystem;
		if (targetSystems.isEmpty()) {
			sourceIdentifierSystem = config.getClientRegistryDefaultPatientIdentifierSystem();
		} else {
			sourceIdentifierSystem = targetSystems.get(0);
			targetSystems.remove(0);
		}

		if (sourceIdentifierSystem == null || sourceIdentifierSystem.isEmpty()) {
			throw new InvalidRequestException("ClientRegistry module does not have a default target system assigned " +
					"via the defaultPatientIdentifierSystem property. At least one targetSystem must be provided in " +
					"the request");
		}

		List<Patient> patients = clientRegistryManager.getPatientService().getCRPatient(
				sourceIdentifierParam.getValue(), sourceIdentifierSystem, targetSystems
		);

		if (patients.isEmpty()) {
			throw new ResourceNotFoundException("No Client Registry patients found.");
		}

		return patients;
	}
	
	@Search
	public List<Patient> searchClientRegistryPatients() {
		throw new NotImplementedOperationException("search client registry is not yet implemented");
	}
}
