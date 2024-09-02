package org.openmrs.module.clientregistry.api;

import org.hl7.fhir.r4.model.Patient;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import java.util.List;

public interface CRPatientService {
	
	/**
	 * Queries patient by ID
	 */
	Patient getPatientById(String id);
	
	/**
	 * Queries patients through a PIXm manager using native IDs.
	 * 
	 * @param sourceIdentifier the source identifier of the patient
	 * @param sourceIdentifierSystem the system of the source identifier
	 * @param extraTargetSystems additional target systems
	 * @return a bundle containing patients that match the specified identifiers
	 */
	IBundleProvider getPatientsByPIX(String sourceIdentifier, String sourceIdentifierSystem, List<String> extraTargetSystems);
	
	/**
	 * Searches for patients, including fuzzy search.
	 * 
	 * @param patientSearchParams the parameters for searching patients
	 * @return a bundle containing patients that match the search criteria
	 */
	IBundleProvider searchPatients(PatientSearchParams patientSearchParams);
	
	/**
	 * Creates a patient record
	 * 
	 * @param patient the patient to create
	 * @return the created patient
	 */
	Patient createPatient(Patient patient);
	
	/**
	 * Updates a patient record.
	 * 
	 * @param patient the patient to update
	 * @return the updated patient
	 */
	Patient updatePatient(Patient patient);
	
	/**
	 * Purges a patient record from the registry.
	 * 
	 * @param patient the patient to purge
	 */
	void purgePatient(Patient patient);
}
