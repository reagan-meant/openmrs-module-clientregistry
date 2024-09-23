package org.openmrs.module.clientregistry.api.search;

import java.io.Serializable;
import java.util.List;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.openmrs.module.fhir2.FhirConstants;
import org.openmrs.module.fhir2.api.FhirGlobalPropertyService;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;

public class CRSearchBundleProvider extends SimpleBundleProvider implements Serializable {
	
	private final FhirGlobalPropertyService globalPropertyService;
	
	public CRSearchBundleProvider(List<? extends IBaseResource> patientList, FhirGlobalPropertyService globalPropertyService) {
		super(patientList);
		this.globalPropertyService = globalPropertyService;
	}
	
	@Override
	public Integer preferredPageSize() {
		if (size() == null) {
			setSize(globalPropertyService.getGlobalProperty(FhirConstants.OPENMRS_FHIR_DEFAULT_PAGE_SIZE, 10));
		}
		return size();
	}
	
}
