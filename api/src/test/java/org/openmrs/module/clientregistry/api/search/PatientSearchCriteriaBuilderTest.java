package org.openmrs.module.clientregistry.api.search;

import java.util.List;
import org.junit.Test;
import org.openmrs.module.fhir2.api.search.param.PatientSearchParams;

import ca.uhn.fhir.rest.gclient.ICriterion;
import ca.uhn.fhir.rest.param.StringAndListParam;
import ca.uhn.fhir.rest.param.StringOrListParam;
import ca.uhn.fhir.rest.param.StringParam;

public class PatientSearchCriteriaBuilderTest {
    
    private PatientSearchCriteriaBuilder builder = new PatientSearchCriteriaBuilder();
    
    @Test
    public void buildCriteria_shouldGenerateCriterionForName() {
        PatientSearchParams params = new PatientSearchParams();
        params.setName(createStringParam("Smith"));
        
        List<ICriterion<?>> criterions = builder.buildCriteria(params);
        // StringCriterion<StringClientParam> nameCriterion = (StringCriterion<StringClientParam>) criterions.get(0);
        
    }
    
    @Test
    public void buildCriteria_shouldGenerateCriterionForGivenName() {
        
    }
    
    @Test
    public void buildCriteria_shouldGenerateCriterionForFamilyName() {
        
    }
    
    private StringAndListParam createStringParam(String paramValue) {
        return new StringAndListParam().addAnd(new StringOrListParam().add(new StringParam(paramValue)));
    }
}
